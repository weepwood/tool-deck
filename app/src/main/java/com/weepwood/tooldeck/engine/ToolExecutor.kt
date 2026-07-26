package com.weepwood.tooldeck.engine

import com.weepwood.tooldeck.model.ToolDescriptor
import com.weepwood.tooldeck.model.ToolInput
import com.weepwood.tooldeck.model.ToolResult

interface ToolExecutor {
    val descriptor: ToolDescriptor
    fun execute(input: ToolInput): ToolResult
}

class FunctionalTool(
    override val descriptor: ToolDescriptor,
    private val runner: (ToolInput) -> ToolResult
) : ToolExecutor {
    override fun execute(input: ToolInput): ToolResult = runCatching {
        runner(input)
    }.getOrElse { throwable ->
        ToolResult.Failure(
            message = throwable.message ?: "工具执行失败",
            errorCode = "EXECUTION_ERROR"
        )
    }
}
