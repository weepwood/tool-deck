package com.weepwood.tooldeck.model

enum class ToolCategory(val displayName: String) {
    TEXT("文本"),
    ENCODING("编码"),
    DEVELOPER("开发"),
    SECURITY("安全")
}

enum class ToolOutputKind {
    TEXT,
    CODE
}

data class ChoiceOption(
    val value: String,
    val label: String
)

sealed interface ToolInputField {
    val key: String
    val label: String

    data class Text(
        override val key: String,
        override val label: String,
        val placeholder: String = "",
        val defaultValue: String = "",
        val required: Boolean = true,
        val multiline: Boolean = false,
        val sensitive: Boolean = false
    ) : ToolInputField

    data class Choice(
        override val key: String,
        override val label: String,
        val options: List<ChoiceOption>,
        val defaultValue: String
    ) : ToolInputField

    data class Toggle(
        override val key: String,
        override val label: String,
        val defaultValue: Boolean = false
    ) : ToolInputField
}

data class ToolDescriptor(
    val id: String,
    val name: String,
    val description: String,
    val category: ToolCategory,
    val tags: List<String>,
    val inputFields: List<ToolInputField>,
    val outputKind: ToolOutputKind = ToolOutputKind.TEXT,
    val supportsOffline: Boolean = true,
    val version: Int = 1
)

data class ToolInput(
    val values: Map<String, String>
) {
    operator fun get(key: String): String = values[key].orEmpty()
    fun boolean(key: String): Boolean = values[key]?.toBooleanStrictOrNull() ?: false
}

sealed interface ToolResult {
    data class Success(
        val value: String,
        val kind: ToolOutputKind = ToolOutputKind.TEXT,
        val metadata: Map<String, String> = emptyMap()
    ) : ToolResult

    data class Failure(
        val message: String,
        val errorCode: String = "INVALID_INPUT"
    ) : ToolResult
}

data class ToolExecutionRecord(
    val id: String,
    val toolId: String,
    val toolName: String,
    val timestamp: Long,
    val inputSummary: String,
    val outputSummary: String,
    val succeeded: Boolean
)
