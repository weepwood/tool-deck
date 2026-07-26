package com.weepwood.tooldeck.data

import android.content.Context
import com.weepwood.tooldeck.model.ToolExecutionRecord
import java.nio.charset.StandardCharsets
import java.util.Base64

class ToolPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("tool_deck", Context.MODE_PRIVATE)

    fun loadFavorites(): Set<String> =
        preferences.getStringSet(KEY_FAVORITES, emptySet()).orEmpty().toSet()

    fun saveFavorites(ids: Set<String>) {
        preferences.edit().putStringSet(KEY_FAVORITES, ids).apply()
    }

    fun loadHistory(): List<ToolExecutionRecord> {
        return preferences.getStringSet(KEY_HISTORY, emptySet())
            .orEmpty()
            .mapNotNull(::decodeRecord)
            .sortedByDescending { it.timestamp }
    }

    fun saveHistory(records: List<ToolExecutionRecord>) {
        val encoded = records
            .sortedByDescending { it.timestamp }
            .take(MAX_HISTORY)
            .map(::encodeRecord)
            .toSet()
        preferences.edit().putStringSet(KEY_HISTORY, encoded).apply()
    }

    fun loadThemeMode(): String = preferences.getString(KEY_THEME, "system") ?: "system"

    fun saveThemeMode(mode: String) {
        preferences.edit().putString(KEY_THEME, mode).apply()
    }

    private fun encodeRecord(record: ToolExecutionRecord): String {
        return listOf(
            encode(record.id),
            encode(record.toolId),
            encode(record.toolName),
            record.timestamp.toString(),
            encode(record.inputSummary),
            encode(record.outputSummary),
            record.succeeded.toString()
        ).joinToString(SEPARATOR)
    }

    private fun decodeRecord(value: String): ToolExecutionRecord? = runCatching {
        val parts = value.split(SEPARATOR)
        require(parts.size == 7)
        ToolExecutionRecord(
            id = decode(parts[0]),
            toolId = decode(parts[1]),
            toolName = decode(parts[2]),
            timestamp = parts[3].toLong(),
            inputSummary = decode(parts[4]),
            outputSummary = decode(parts[5]),
            succeeded = parts[6].toBooleanStrict()
        )
    }.getOrNull()

    private fun encode(value: String): String = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decode(value: String): String {
        val padded = value.padEnd((value.length + 3) / 4 * 4, '=')
        return String(Base64.getUrlDecoder().decode(padded), StandardCharsets.UTF_8)
    }

    private companion object {
        const val KEY_FAVORITES = "favorites"
        const val KEY_HISTORY = "history"
        const val KEY_THEME = "theme"
        const val SEPARATOR = "|"
        const val MAX_HISTORY = 50
    }
}
