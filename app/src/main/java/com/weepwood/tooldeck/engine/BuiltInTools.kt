package com.weepwood.tooldeck.engine

import com.weepwood.tooldeck.model.ChoiceOption
import com.weepwood.tooldeck.model.ToolCategory
import com.weepwood.tooldeck.model.ToolDescriptor
import com.weepwood.tooldeck.model.ToolInputField
import com.weepwood.tooldeck.model.ToolOutputKind
import com.weepwood.tooldeck.model.ToolResult
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Locale
import java.util.UUID

object BuiltInTools {
    val all: List<ToolExecutor> = listOf(
        jsonFormatter(),
        base64Codec(),
        urlCodec(),
        timestampConverter(),
        uuidGenerator(),
        sha256Calculator(),
        lineDeduplicator(),
        caseConverter(),
        jwtDecoder(),
        passwordGenerator()
    )

    fun find(id: String): ToolExecutor? = all.firstOrNull { it.descriptor.id == id }

    private fun jsonFormatter() = FunctionalTool(
        descriptor = ToolDescriptor(
            id = "text.json_formatter",
            name = "JSON 格式化",
            description = "格式化、压缩并检查 JSON 的基本结构",
            category = ToolCategory.DEVELOPER,
            tags = listOf("JSON", "格式化", "开发"),
            inputFields = listOf(
                ToolInputField.Text(
                    key = "content",
                    label = "JSON 内容",
                    placeholder = "粘贴 JSON…",
                    multiline = true
                ),
                ToolInputField.Choice(
                    key = "mode",
                    label = "处理方式",
                    options = listOf(
                        ChoiceOption("pretty", "格式化"),
                        ChoiceOption("minify", "压缩")
                    ),
                    defaultValue = "pretty"
                )
            ),
            outputKind = ToolOutputKind.CODE
        )
    ) { input ->
        val content = input["content"]
        if (content.isBlank()) return@FunctionalTool ToolResult.Failure("请输入 JSON 内容")
        val result = JsonTextProcessor.process(content, pretty = input["mode"] != "minify")
        ToolResult.Success(result, ToolOutputKind.CODE)
    }

    private fun base64Codec() = FunctionalTool(
        descriptor = ToolDescriptor(
            id = "encoding.base64",
            name = "Base64 编解码",
            description = "在普通文本与 Base64 之间转换",
            category = ToolCategory.ENCODING,
            tags = listOf("Base64", "编码", "解码"),
            inputFields = listOf(
                ToolInputField.Text(
                    key = "content",
                    label = "内容",
                    placeholder = "输入文本或 Base64…",
                    multiline = true
                ),
                ToolInputField.Choice(
                    key = "mode",
                    label = "处理方式",
                    options = listOf(
                        ChoiceOption("encode", "编码"),
                        ChoiceOption("decode", "解码")
                    ),
                    defaultValue = "encode"
                )
            ),
            outputKind = ToolOutputKind.CODE
        )
    ) { input ->
        val content = input["content"]
        if (content.isBlank()) return@FunctionalTool ToolResult.Failure("请输入内容")
        val value = if (input["mode"] == "decode") {
            String(Base64.getDecoder().decode(content.trim()), StandardCharsets.UTF_8)
        } else {
            Base64.getEncoder().encodeToString(content.toByteArray(StandardCharsets.UTF_8))
        }
        ToolResult.Success(value, ToolOutputKind.CODE)
    }

    private fun urlCodec() = FunctionalTool(
        descriptor = ToolDescriptor(
            id = "encoding.url",
            name = "URL 编解码",
            description = "对 URL 参数和文本进行百分号编解码",
            category = ToolCategory.ENCODING,
            tags = listOf("URL", "百分号", "编码"),
            inputFields = listOf(
                ToolInputField.Text(
                    key = "content",
                    label = "内容",
                    placeholder = "输入需要处理的内容…",
                    multiline = true
                ),
                ToolInputField.Choice(
                    key = "mode",
                    label = "处理方式",
                    options = listOf(
                        ChoiceOption("encode", "编码"),
                        ChoiceOption("decode", "解码")
                    ),
                    defaultValue = "encode"
                )
            ),
            outputKind = ToolOutputKind.CODE
        )
    ) { input ->
        val content = input["content"]
        if (content.isBlank()) return@FunctionalTool ToolResult.Failure("请输入内容")
        val value = if (input["mode"] == "decode") {
            URLDecoder.decode(content, StandardCharsets.UTF_8.name())
        } else {
            URLEncoder.encode(content, StandardCharsets.UTF_8.name()).replace("+", "%20")
        }
        ToolResult.Success(value, ToolOutputKind.CODE)
    }

    private fun timestampConverter() = FunctionalTool(
        descriptor = ToolDescriptor(
            id = "developer.timestamp",
            name = "时间戳转换",
            description = "识别秒或毫秒时间戳，并转换为本地与 UTC 时间",
            category = ToolCategory.DEVELOPER,
            tags = listOf("时间戳", "日期", "UTC"),
            inputFields = listOf(
                ToolInputField.Text(
                    key = "timestamp",
                    label = "Unix 时间戳",
                    placeholder = "例如 1720000000；留空使用当前时间",
                    required = false
                )
            ),
            outputKind = ToolOutputKind.CODE
        )
    ) { input ->
        val raw = input["timestamp"].trim()
        val instant = if (raw.isBlank()) {
            Instant.now()
        } else {
            val value = raw.toLongOrNull()
                ?: return@FunctionalTool ToolResult.Failure("时间戳必须是整数")
            if (raw.length >= 13) Instant.ofEpochMilli(value) else Instant.ofEpochSecond(value)
        }
        val localFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX")
            .withZone(ZoneId.systemDefault())
        val utcFormatter = DateTimeFormatter.ISO_INSTANT
        val output = buildString {
            appendLine("秒级时间戳：${instant.epochSecond}")
            appendLine("毫秒时间戳：${instant.toEpochMilli()}")
            appendLine("本地时间：${localFormatter.format(instant)}")
            append("UTC 时间：${utcFormatter.format(instant)}")
        }
        ToolResult.Success(output, ToolOutputKind.CODE)
    }

    private fun uuidGenerator() = FunctionalTool(
        descriptor = ToolDescriptor(
            id = "developer.uuid",
            name = "UUID 生成器",
            description = "一次生成 1 到 20 个随机 UUID v4",
            category = ToolCategory.DEVELOPER,
            tags = listOf("UUID", "随机", "标识符"),
            inputFields = listOf(
                ToolInputField.Text(
                    key = "count",
                    label = "生成数量",
                    placeholder = "1",
                    defaultValue = "1",
                    required = false
                ),
                ToolInputField.Toggle(
                    key = "uppercase",
                    label = "转换为大写"
                )
            ),
            outputKind = ToolOutputKind.CODE
        )
    ) { input ->
        val count = input["count"].ifBlank { "1" }.toIntOrNull()
            ?: return@FunctionalTool ToolResult.Failure("数量必须是整数")
        if (count !in 1..20) return@FunctionalTool ToolResult.Failure("数量需要在 1 到 20 之间")
        val output = List(count) { UUID.randomUUID().toString() }
            .joinToString("\n")
            .let { if (input.boolean("uppercase")) it.uppercase(Locale.ROOT) else it }
        ToolResult.Success(output, ToolOutputKind.CODE)
    }

    private fun sha256Calculator() = FunctionalTool(
        descriptor = ToolDescriptor(
            id = "security.sha256",
            name = "SHA-256 摘要",
            description = "计算文本的 SHA-256 十六进制摘要",
            category = ToolCategory.SECURITY,
            tags = listOf("SHA-256", "哈希", "摘要"),
            inputFields = listOf(
                ToolInputField.Text(
                    key = "content",
                    label = "文本",
                    placeholder = "输入需要计算摘要的文本…",
                    multiline = true,
                    sensitive = true
                )
            ),
            outputKind = ToolOutputKind.CODE
        )
    ) { input ->
        val bytes = input["content"].toByteArray(StandardCharsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        ToolResult.Success(digest.joinToString("") { "%02x".format(it) }, ToolOutputKind.CODE)
    }

    private fun lineDeduplicator() = FunctionalTool(
        descriptor = ToolDescriptor(
            id = "text.dedupe_lines",
            name = "文本行去重",
            description = "按原顺序移除重复行，可忽略空白和大小写",
            category = ToolCategory.TEXT,
            tags = listOf("文本", "去重", "行处理"),
            inputFields = listOf(
                ToolInputField.Text(
                    key = "content",
                    label = "多行文本",
                    placeholder = "每行一条内容…",
                    multiline = true
                ),
                ToolInputField.Toggle("trim", "忽略行首尾空白", true),
                ToolInputField.Toggle("ignoreCase", "忽略大小写")
            )
        )
    ) { input ->
        val seen = linkedSetOf<String>()
        val output = input["content"].lineSequence().filter { line ->
            val trimmed = if (input.boolean("trim")) line.trim() else line
            val key = if (input.boolean("ignoreCase")) trimmed.lowercase(Locale.ROOT) else trimmed
            seen.add(key)
        }.joinToString("\n") { if (input.boolean("trim")) it.trim() else it }
        ToolResult.Success(output)
    }

    private fun caseConverter() = FunctionalTool(
        descriptor = ToolDescriptor(
            id = "text.case_converter",
            name = "命名格式转换",
            description = "转换大写、小写、驼峰、蛇形和短横线命名",
            category = ToolCategory.TEXT,
            tags = listOf("大小写", "camelCase", "snake_case"),
            inputFields = listOf(
                ToolInputField.Text(
                    key = "content",
                    label = "文本",
                    placeholder = "例如 hello tool deck",
                    multiline = true
                ),
                ToolInputField.Choice(
                    key = "mode",
                    label = "目标格式",
                    options = listOf(
                        ChoiceOption("upper", "UPPER CASE"),
                        ChoiceOption("lower", "lower case"),
                        ChoiceOption("title", "Title Case"),
                        ChoiceOption("camel", "camelCase"),
                        ChoiceOption("snake", "snake_case"),
                        ChoiceOption("kebab", "kebab-case")
                    ),
                    defaultValue = "camel"
                )
            )
        )
    ) { input ->
        val content = input["content"]
        if (content.isBlank()) return@FunctionalTool ToolResult.Failure("请输入文本")
        val words = content
            .replace(Regex("([a-z0-9])([A-Z])"), "${'$'}1 ${'$'}2")
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.isNotBlank() }
        val value = when (input["mode"]) {
            "upper" -> content.uppercase(Locale.ROOT)
            "lower" -> content.lowercase(Locale.ROOT)
            "title" -> words.joinToString(" ") { word -> word.lowercase().replaceFirstChar { it.titlecase() } }
            "snake" -> words.joinToString("_") { it.lowercase(Locale.ROOT) }
            "kebab" -> words.joinToString("-") { it.lowercase(Locale.ROOT) }
            else -> words.map { it.lowercase(Locale.ROOT) }
                .mapIndexed { index, word -> if (index == 0) word else word.replaceFirstChar { it.titlecase() } }
                .joinToString("")
        }
        ToolResult.Success(value)
    }

    private fun jwtDecoder() = FunctionalTool(
        descriptor = ToolDescriptor(
            id = "developer.jwt_decoder",
            name = "JWT 解析",
            description = "离线解析 JWT Header 与 Payload，不验证签名",
            category = ToolCategory.DEVELOPER,
            tags = listOf("JWT", "Token", "JSON"),
            inputFields = listOf(
                ToolInputField.Text(
                    key = "token",
                    label = "JWT",
                    placeholder = "xxxxx.yyyyy.zzzzz",
                    multiline = true,
                    sensitive = true
                )
            ),
            outputKind = ToolOutputKind.CODE
        )
    ) { input ->
        val parts = input["token"].trim().split('.')
        if (parts.size < 2) return@FunctionalTool ToolResult.Failure("JWT 至少应包含 Header 和 Payload")
        fun decodePart(part: String): String {
            val padded = part.padEnd((part.length + 3) / 4 * 4, '=')
            return String(Base64.getUrlDecoder().decode(padded), StandardCharsets.UTF_8)
        }
        val header = JsonTextProcessor.process(decodePart(parts[0]), pretty = true)
        val payload = JsonTextProcessor.process(decodePart(parts[1]), pretty = true)
        ToolResult.Success("Header\n$header\n\nPayload\n$payload", ToolOutputKind.CODE)
    }

    private fun passwordGenerator() = FunctionalTool(
        descriptor = ToolDescriptor(
            id = "security.password_generator",
            name = "随机密码生成器",
            description = "使用安全随机数生成器创建本地密码",
            category = ToolCategory.SECURITY,
            tags = listOf("密码", "随机", "安全"),
            inputFields = listOf(
                ToolInputField.Text("length", "密码长度", "16", "16", required = false),
                ToolInputField.Toggle("numbers", "包含数字", true),
                ToolInputField.Toggle("symbols", "包含符号", true)
            ),
            outputKind = ToolOutputKind.CODE
        )
    ) { input ->
        val length = input["length"].ifBlank { "16" }.toIntOrNull()
            ?: return@FunctionalTool ToolResult.Failure("长度必须是整数")
        if (length !in 8..128) return@FunctionalTool ToolResult.Failure("长度需要在 8 到 128 之间")
        val alphabet = buildString {
            append("ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz")
            if (input.boolean("numbers")) append("23456789")
            if (input.boolean("symbols")) append("!@#%&*+-_=?.")
        }
        val random = SecureRandom()
        val value = buildString(length) {
            repeat(length) { append(alphabet[random.nextInt(alphabet.length)]) }
        }
        ToolResult.Success(value, ToolOutputKind.CODE)
    }
}

internal object JsonTextProcessor {
    fun process(raw: String, pretty: Boolean): String {
        val input = raw.trim()
        if (input.isEmpty()) throw IllegalArgumentException("JSON 不能为空")

        val output = StringBuilder()
        val stack = ArrayDeque<Char>()
        var inString = false
        var escaped = false
        var indent = 0
        var index = 0

        fun appendIndent() {
            repeat(indent) { output.append("  ") }
        }

        while (index < input.length) {
            val char = input[index]
            if (inString) {
                output.append(char)
                if (escaped) {
                    escaped = false
                } else if (char == '\\') {
                    escaped = true
                } else if (char == '"') {
                    inString = false
                }
                index++
                continue
            }

            when (char) {
                '"' -> {
                    inString = true
                    output.append(char)
                }
                '{', '[' -> {
                    stack.add(char)
                    output.append(char)
                    indent++
                    if (pretty && nextNonWhitespace(input, index + 1) !in listOf('}', ']')) {
                        output.append('\n')
                        appendIndent()
                    }
                }
                '}', ']' -> {
                    val expected = if (char == '}') '{' else '['
                    if (stack.removeLastOrNull() != expected) throw IllegalArgumentException("JSON 括号不匹配")
                    indent--
                    if (pretty && output.isNotEmpty() && output.last() !in listOf('{', '[', '\n')) {
                        output.append('\n')
                        appendIndent()
                    }
                    output.append(char)
                }
                ',' -> {
                    output.append(char)
                    if (pretty) {
                        output.append('\n')
                        appendIndent()
                    }
                }
                ':' -> {
                    output.append(if (pretty) ": " else ":")
                }
                ' ', '\n', '\r', '\t' -> Unit
                else -> output.append(char)
            }
            index++
        }

        if (inString) throw IllegalArgumentException("JSON 字符串未闭合")
        if (stack.isNotEmpty()) throw IllegalArgumentException("JSON 括号未闭合")
        return output.toString()
    }

    private fun nextNonWhitespace(value: String, start: Int): Char? {
        for (index in start until value.length) {
            if (!value[index].isWhitespace()) return value[index]
        }
        return null
    }
}
