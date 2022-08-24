package io.squark.swaggercombine

import com.fasterxml.jackson.core.PrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.models.Swagger
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.DataOutput


class SwaggerSpecificationsWriterTest {

    @get:Rule
    var folder = TemporaryFolder()

    @Test
    fun test() {
        val outputFilePath = "${folder.root.path}/output.json"
        val commandLineProperties: CommandLineProperties = mock {
            on { getArguments(CommandLineArguments.OUTPUT_FILE) }.doReturn(listOf(outputFilePath))
        }

        val objectMapper: ObjectMapper = mock {
            on { writer(any< PrettyPrinter>())}.doReturn(it!!)
            doNothing().`when`(it).writeValue(any<DataOutput>(), any())
        }

        val swaggerSpecificationsWriter = SwaggerSpecificationsWriter(commandLineProperties, objectMapper)
        swaggerSpecificationsWriter.writeSwaggerSpecification(Swagger())
    }

}
