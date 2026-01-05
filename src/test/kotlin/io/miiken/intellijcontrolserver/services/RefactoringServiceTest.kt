package io.miiken.intellijcontrolserver.services

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class RefactoringServiceTest {
    
    @ParameterizedTest(name = "{2}")
    @CsvSource(
        delimiter = '|',
        quoteCharacter = '~',
        value = [
            "start something|0|whole word at start",
            "method start|7|whole word at end",
            "start the start process|0,10|multiple occurrences",
            "method start now|7|space boundaries",
            "~start,stop,restart~|0|comma boundaries",
            "~\"start\"~|1|with quotes",
            "(start)|1|with parentheses",
            "Metrics.Timed.start|14|after period in dotted path",
            "~logger.info(\"start / Starting MCP server\")~|13|real world logger",
            "~@Timed(\"start\")~|8|annotation with simple name",
            "~@Metrics(\"api.start\")~|14|annotation with dotted path end",
            "~@Metrics(\"api.start.time\")~|14|annotation with dotted path middle",
            "~@Timed(\"start\") @Logged(\"start\")~|8,25|multiple annotations",
            "~@Timed(name = \"api.operations.start\")~|30|Kotlin annotation syntax",
            "~@Name(\"auto-start-service\")~|12|hyphenated value",
            "~@Path(\"/api/start/process\")~|12|slash-separated path",
            "~@Config(\"{\\\"method\\\":\\\"start\\\"}\")~|23|JSON-like structure",
            "~@Description(\"Use start command\")~|18|standalone identifier in sentence",
            "~@Info(\"Call start() to begin\")~|12|identifier in sentence context"
        ]
    )
    fun `findWordBoundaryMatches - should find matches`(text: String, indices: String, description: String) {
        val expected = indices.split(",").map { it.toInt() }
        val result = RefactoringService.findWordBoundaryMatches(text, "start")
        assertEquals(expected, result, "Failed for: $description")
    }
    
    @ParameterizedTest(name = "{1}")
    @CsvSource(
        delimiter = '|',
        quoteCharacter = '~',
        value = [
            "starting|part of larger word",
            "restart|compound word prefix",
            "autoStart|middle of camelCase",
            "start_time|underscore suffix",
            "_start|underscore prefix",
            "~@Description(\"method started\")~|past tense verb",
            "~@Description(\"This method starts the process\")~|verb conjugation",
            "~@Status(\"restarting\")~|middle of word in annotation",
            "~@Config(\"autoStart\")~|camelCase in annotation"
        ]
    )
    fun `findWordBoundaryMatches - should not find matches`(text: String, description: String) {
        val result = RefactoringService.findWordBoundaryMatches(text, "start")
        assertEquals(emptyList<Int>(), result, "Failed for: $description")
    }
}
