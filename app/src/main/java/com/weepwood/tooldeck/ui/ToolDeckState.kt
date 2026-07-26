package com.weepwood.tooldeck.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.weepwood.tooldeck.data.ToolPreferences
import com.weepwood.tooldeck.engine.BuiltInTools
import com.weepwood.tooldeck.engine.ToolExecutor
import com.weepwood.tooldeck.model.ToolExecutionRecord
import com.weepwood.tooldeck.model.ToolInput
import com.weepwood.tooldeck.model.ToolInputField
import com.weepwood.tooldeck.model.ToolResult
import java.util.UUID

class ToolDeckState(
    private val preferences: ToolPreferences
) {
    val tools: List<ToolExecutor> = BuiltInTools.all

    var favorites by mutableStateOf(preferences.loadFavorites())
        private set

    var history by mutableStateOf(preferences.loadHistory())
        private set

    private var currentThemeMode by mutableStateOf(preferences.loadThemeMode())

    val themeMode: String
        get() = currentThemeMode

    fun toggleFavorite(toolId: String) {
        favorites = if (toolId in favorites) favorites - toolId else favorites + toolId
        preferences.saveFavorites(favorites)
    }

    fun recordExecution(tool: ToolExecutor, input: ToolInput, result: ToolResult) {
        val sensitiveKeys = tool.descriptor.inputFields
            .filterIsInstance<ToolInputField.Text>()
            .filter { it.sensitive }
            .map { it.key }
            .toSet()
        val inputSummary = input.values.entries
            .firstOrNull { (key, value) -> key !in sensitiveKeys && value.isNotBlank() }
            ?.value
            ?.singleLine(80)
            ?: if (input.values.isEmpty()) "无输入参数" else "已隐藏输入内容"
        val outputSummary = when (result) {
            is ToolResult.Success -> result.value.singleLine(120)
            is ToolResult.Failure -> result.message.singleLine(120)
        }
        val record = ToolExecutionRecord(
            id = UUID.randomUUID().toString(),
            toolId = tool.descriptor.id,
            toolName = tool.descriptor.name,
            timestamp = System.currentTimeMillis(),
            inputSummary = inputSummary,
            outputSummary = outputSummary,
            succeeded = result is ToolResult.Success
        )
        history = (listOf(record) + history).take(50)
        preferences.saveHistory(history)
    }

    fun clearHistory() {
        history = emptyList()
        preferences.saveHistory(history)
    }

    fun setThemeMode(mode: String) {
        currentThemeMode = mode
        preferences.saveThemeMode(mode)
    }

    private fun String.singleLine(maxLength: Int): String {
        val normalized = replace(Regex("\\s+"), " ").trim()
        return if (normalized.length <= maxLength) normalized else normalized.take(maxLength - 1) + "…"
    }
}
