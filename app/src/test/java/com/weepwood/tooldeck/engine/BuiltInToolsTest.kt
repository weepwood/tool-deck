package com.weepwood.tooldeck.engine

import com.weepwood.tooldeck.model.ToolInput
import com.weepwood.tooldeck.model.ToolResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltInToolsTest {
    @Test
    fun `base64 tool encodes and decodes utf8 text`() {
        val tool = requireNotNull(BuiltInTools.find("encoding.base64"))
        val encoded = tool.execute(ToolInput(mapOf("content" to "ToolDeck 工具", "mode" to "encode")))
        assertTrue(encoded is ToolResult.Success)

        val value = (encoded as ToolResult.Success).value
        val decoded = tool.execute(ToolInput(mapOf("content" to value, "mode" to "decode")))
        assertEquals("ToolDeck 工具", (decoded as ToolResult.Success).value)
    }

    @Test
    fun `json formatter preserves strings and formats nesting`() {
        val tool = requireNotNull(BuiltInTools.find("text.json_formatter"))
        val result = tool.execute(
            ToolInput(mapOf("content" to "{\"name\":\"ToolDeck\",\"items\":[1,2]}", "mode" to "pretty"))
        )
        val value = (result as ToolResult.Success).value
        assertTrue(value.contains("\"name\": \"ToolDeck\""))
        assertTrue(value.contains("\n"))
    }

    @Test
    fun `sha256 tool returns known digest`() {
        val tool = requireNotNull(BuiltInTools.find("security.sha256"))
        val result = tool.execute(ToolInput(mapOf("content" to "abc")))
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            (result as ToolResult.Success).value
        )
    }

    @Test
    fun `case converter creates camel case`() {
        val tool = requireNotNull(BuiltInTools.find("text.case_converter"))
        val result = tool.execute(ToolInput(mapOf("content" to "hello tool deck", "mode" to "camel")))
        assertEquals("helloToolDeck", (result as ToolResult.Success).value)
    }

    @Test
    fun `password generator validates minimum length`() {
        val tool = requireNotNull(BuiltInTools.find("security.password_generator"))
        val result = tool.execute(
            ToolInput(mapOf("length" to "6", "numbers" to "true", "symbols" to "true"))
        )
        assertTrue(result is ToolResult.Failure)
    }
}
