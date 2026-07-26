package com.weepwood.tooldeck.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weepwood.tooldeck.BuildConfig
import com.weepwood.tooldeck.engine.ToolExecutor
import com.weepwood.tooldeck.model.ToolCategory
import com.weepwood.tooldeck.model.ToolInput
import com.weepwood.tooldeck.model.ToolInputField
import com.weepwood.tooldeck.model.ToolOutputKind
import com.weepwood.tooldeck.model.ToolResult
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class MainDestination(
    val label: String,
    val icon: ImageVector
) {
    HOME("首页", Icons.Default.Home),
    TOOLS("工具", Icons.Default.Apps),
    HISTORY("历史", Icons.Default.History),
    SETTINGS("设置", Icons.Default.Settings)
}

@Composable
fun ToolDeckApp(state: ToolDeckState) {
    var destination by remember { mutableStateOf(MainDestination.HOME) }
    var activeTool by remember { mutableStateOf<ToolExecutor?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (activeTool != null) {
            ToolRunnerScreen(
                state = state,
                tool = activeTool!!,
                onBack = { activeTool = null }
            )
            return@Surface
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 760.dp
            if (useRail) {
                Row(Modifier.fillMaxSize()) {
                    NavigationRail {
                        Spacer(Modifier.height(20.dp))
                        MainDestination.entries.forEach { item ->
                            NavigationRailItem(
                                selected = destination == item,
                                onClick = { destination = item },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) }
                            )
                        }
                    }
                    VerticalDivider(modifier = Modifier.fillMaxHeight())
                    MainScreen(
                        modifier = Modifier.weight(1f),
                        state = state,
                        destination = destination,
                        onOpenTool = { activeTool = it }
                    )
                }
            } else {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            MainDestination.entries.forEach { item ->
                                NavigationBarItem(
                                    selected = destination == item,
                                    onClick = { destination = item },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) },
                                    colors = NavigationBarItemDefaults.colors()
                                )
                            }
                        }
                    }
                ) { padding ->
                    MainScreen(
                        modifier = Modifier.padding(padding),
                        state = state,
                        destination = destination,
                        onOpenTool = { activeTool = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainScreen(
    modifier: Modifier,
    state: ToolDeckState,
    destination: MainDestination,
    onOpenTool: (ToolExecutor) -> Unit
) {
    when (destination) {
        MainDestination.HOME -> HomeScreen(modifier, state, onOpenTool)
        MainDestination.TOOLS -> ToolsScreen(modifier, state, onOpenTool)
        MainDestination.HISTORY -> HistoryScreen(modifier, state, onOpenTool)
        MainDestination.SETTINGS -> SettingsScreen(modifier, state)
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier,
    state: ToolDeckState,
    onOpenTool: (ToolExecutor) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val favorites = state.tools.filter { it.descriptor.id in state.favorites }
    val recent = state.history.mapNotNull { record ->
        state.tools.firstOrNull { it.descriptor.id == record.toolId }
    }.distinctBy { it.descriptor.id }.take(4)
    val searchResults = remember(query, state.tools) {
        if (query.isBlank()) emptyList() else state.tools.filter { it.matches(query) }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ToolDeck",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "把常用工具集中到一个原生应用",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("搜索工具、功能或标签") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(18.dp)
            )
        }

        if (query.isNotBlank()) {
            item { SectionTitle("搜索结果", "${searchResults.size} 个工具") }
            if (searchResults.isEmpty()) {
                item { EmptyCard("没有找到匹配的工具") }
            } else {
                items(searchResults, key = { it.descriptor.id }) { tool ->
                    ToolListCard(tool, tool.descriptor.id in state.favorites, onOpenTool)
                }
            }
        } else {
            item { HeroCard() }

            if (favorites.isNotEmpty()) {
                item { SectionTitle("收藏工具", "快速访问") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(favorites, key = { it.descriptor.id }) { tool ->
                            CompactToolCard(tool, onOpenTool)
                        }
                    }
                }
            }

            if (recent.isNotEmpty()) {
                item { SectionTitle("最近使用", "继续上次操作") }
                items(recent, key = { it.descriptor.id }) { tool ->
                    ToolListCard(tool, tool.descriptor.id in state.favorites, onOpenTool)
                }
            }

            item { SectionTitle("工具分类", "${state.tools.size} 个离线工具") }
            item {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    modifier = Modifier.height(260.dp),
                    userScrollEnabled = false,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ToolCategory.entries) { category ->
                        val count = state.tools.count { it.descriptor.category == category }
                        CategoryCard(category, count) {
                            state.tools.firstOrNull { it.descriptor.category == category }?.let(onOpenTool)
                        }
                    }
                }
            }

            item { SectionTitle("推荐工具", "全部支持离线运行") }
            items(state.tools.take(4), key = { it.descriptor.id }) { tool ->
                ToolListCard(tool, tool.descriptor.id in state.favorites, onOpenTool)
            }
        }
    }
}

@Composable
private fun HeroCard() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.OfflineBolt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("离线优先", fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "工具在设备本地执行",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "首个版本不上传输入内容，适合日常开发、文本处理与编码转换。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ToolsScreen(
    modifier: Modifier,
    state: ToolDeckState,
    onOpenTool: (ToolExecutor) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<ToolCategory?>(null) }
    val tools = state.tools.filter { tool ->
        (category == null || tool.descriptor.category == category) && tool.matches(query)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("工具", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "搜索、分类并调用内置工具",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("搜索工具") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(18.dp)
            )
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = category == null,
                    onClick = { category = null },
                    label = { Text("全部") }
                )
                ToolCategory.entries.forEach { item ->
                    FilterChip(
                        selected = category == item,
                        onClick = { category = item },
                        label = { Text(item.displayName) }
                    )
                }
            }
        }
        item {
            Text(
                "${tools.size} 个工具",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(tools, key = { it.descriptor.id }) { tool ->
            ToolListCard(tool, tool.descriptor.id in state.favorites, onOpenTool)
        }
        if (tools.isEmpty()) item { EmptyCard("当前筛选条件下没有工具") }
    }
}

@Composable
private fun HistoryScreen(
    modifier: Modifier,
    state: ToolDeckState,
    onOpenTool: (ToolExecutor) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("历史", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "仅保存摘要，不记录敏感输入",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (state.history.isNotEmpty()) {
                    IconButton(onClick = state::clearHistory) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "清空历史")
                    }
                }
            }
        }

        if (state.history.isEmpty()) {
            item { EmptyCard("还没有工具执行记录") }
        } else {
            items(state.history, key = { it.id }) { record ->
                val tool = state.tools.firstOrNull { it.descriptor.id == record.toolId }
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = tool != null) { tool?.let(onOpenTool) },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    ListItem(
                        headlineContent = { Text(record.toolName, fontWeight = FontWeight.SemiBold) },
                        supportingContent = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(record.outputSummary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(
                                    formatTime(record.timestamp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingContent = {
                            Icon(
                                imageVector = if (record.succeeded) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (record.succeeded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        },
                        trailingContent = {
                            if (tool != null) Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    modifier: Modifier,
    state: ToolDeckState
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("设置", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("外观、数据与应用信息", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            SettingsCard(title = "外观") {
                Text("主题模式", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("system" to "跟随系统", "light" to "浅色", "dark" to "深色").forEach { (value, label) ->
                        FilterChip(
                            selected = state.themeMode == value,
                            onClick = { state.setThemeMode(value) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
        item {
            SettingsCard(title = "数据与隐私") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("本地执行", fontWeight = FontWeight.Medium)
                        Text(
                            "当前版本没有网络权限，工具输入不会上传。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = state::clearHistory) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("清空执行历史")
                }
            }
        }
        item {
            SettingsCard(title = "关于") {
                ListItem(
                    headlineContent = { Text("ToolDeck") },
                    supportingContent = { Text("版本 ${BuildConfig.VERSION_NAME}") },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
                )
                HorizontalDivider()
                Text(
                    "Kotlin + Jetpack Compose 构建的可扩展安卓原生工具中心。",
                    modifier = Modifier.padding(top = 14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolRunnerScreen(
    state: ToolDeckState,
    tool: ToolExecutor,
    onBack: () -> Unit
) {
    val descriptor = tool.descriptor
    val values = remember(descriptor.id) {
        mutableStateMapOf<String, String>().apply {
            descriptor.inputFields.forEach { field ->
                this[field.key] = when (field) {
                    is ToolInputField.Text -> field.defaultValue
                    is ToolInputField.Choice -> field.defaultValue
                    is ToolInputField.Toggle -> field.defaultValue.toString()
                }
            }
        }
    }
    var result by remember(descriptor.id) { mutableStateOf<ToolResult?>(null) }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(descriptor.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { state.toggleFavorite(descriptor.id) }) {
                        Icon(
                            if (descriptor.id in state.favorites) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "收藏工具"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ToolIcon(descriptor.category, modifier = Modifier.size(52.dp))
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(descriptor.description, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "离线运行 · v${descriptor.version}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            descriptor.inputFields.forEach { field ->
                item(key = field.key) {
                    ToolInputControl(
                        field = field,
                        value = values[field.key].orEmpty(),
                        onValueChange = { values[field.key] = it }
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        val validation = validateInputs(descriptor.inputFields, values)
                        result = validation ?: tool.execute(ToolInput(values.toMap()))
                        state.recordExecution(tool, ToolInput(values.toMap()), result!!)
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("执行工具", fontSize = 16.sp)
                }
            }

            result?.let { current ->
                item {
                    ResultCard(
                        result = current,
                        onCopy = {
                            val text = when (current) {
                                is ToolResult.Success -> current.value
                                is ToolResult.Failure -> current.message
                            }
                            clipboard.setText(AnnotatedString(text))
                        },
                        onShare = {
                            val text = when (current) {
                                is ToolResult.Success -> current.value
                                is ToolResult.Failure -> current.message
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(intent, "分享工具结果"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolInputControl(
    field: ToolInputField,
    value: String,
    onValueChange: (String) -> Unit
) {
    when (field) {
        is ToolInputField.Text -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(field.label) },
                placeholder = { if (field.placeholder.isNotBlank()) Text(field.placeholder) },
                minLines = if (field.multiline) 5 else 1,
                maxLines = if (field.multiline) 12 else 1,
                singleLine = !field.multiline,
                visualTransformation = VisualTransformation.None,
                shape = RoundedCornerShape(16.dp)
            )
        }
        is ToolInputField.Choice -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(field.label, style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    field.options.forEach { option ->
                        FilterChip(
                            selected = value == option.value,
                            onClick = { onValueChange(option.value) },
                            label = { Text(option.label) }
                        )
                    }
                }
            }
        }
        is ToolInputField.Toggle -> {
            OutlinedCard(shape = RoundedCornerShape(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(field.label, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = value.toBooleanStrictOrNull() ?: false,
                        onCheckedChange = { onValueChange(it.toString()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    result: ToolResult,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val success = result is ToolResult.Success
    val value = when (result) {
        is ToolResult.Success -> result.value
        is ToolResult.Failure -> result.message
    }
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (success) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (success) "执行结果" else "执行失败", fontWeight = FontWeight.Bold)
                }
                Row {
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "复制")
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "分享")
                    }
                }
            }
            SelectionContainer {
                Text(
                    text = value.ifBlank { "（空结果）" },
                    modifier = Modifier.fillMaxWidth(),
                    fontFamily = if (result is ToolResult.Success && result.kind == ToolOutputKind.CODE) FontFamily.Monospace else FontFamily.Default,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

@Composable
private fun ToolListCard(
    tool: ToolExecutor,
    favorite: Boolean,
    onOpenTool: (ToolExecutor) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().clickable { onOpenTool(tool) },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        ListItem(
            headlineContent = { Text(tool.descriptor.name, fontWeight = FontWeight.SemiBold) },
            supportingContent = {
                Text(tool.descriptor.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
            },
            leadingContent = { ToolIcon(tool.descriptor.category) },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (favorite) Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        )
    }
}

@Composable
private fun CompactToolCard(tool: ToolExecutor, onOpenTool: (ToolExecutor) -> Unit) {
    Card(
        modifier = Modifier.width(170.dp).clickable { onOpenTool(tool) },
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ToolIcon(tool.descriptor.category)
            Text(tool.descriptor.name, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(
                tool.descriptor.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CategoryCard(category: ToolCategory, count: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolIcon(category)
            Text(category.displayName, fontWeight = FontWeight.Bold)
            Text("$count 个工具", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ToolIcon(category: ToolCategory, modifier: Modifier = Modifier.size(44.dp)) {
    val icon = when (category) {
        ToolCategory.TEXT -> Icons.Default.TextFields
        ToolCategory.ENCODING -> Icons.Default.Code
        ToolCategory.DEVELOPER -> Icons.Default.DataObject
        ToolCategory.SECURITY -> Icons.Default.Lock
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyCard(message: String) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

private fun ToolExecutor.matches(query: String): Boolean {
    if (query.isBlank()) return true
    val needle = query.trim().lowercase()
    return descriptor.name.lowercase().contains(needle) ||
        descriptor.description.lowercase().contains(needle) ||
        descriptor.category.displayName.lowercase().contains(needle) ||
        descriptor.tags.any { it.lowercase().contains(needle) }
}

private fun validateInputs(
    fields: List<ToolInputField>,
    values: Map<String, String>
): ToolResult.Failure? {
    val missing = fields.filterIsInstance<ToolInputField.Text>()
        .firstOrNull { it.required && values[it.key].isNullOrBlank() }
    return missing?.let { ToolResult.Failure("请填写${it.label}") }
}

private fun formatTime(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(timestamp))
}
