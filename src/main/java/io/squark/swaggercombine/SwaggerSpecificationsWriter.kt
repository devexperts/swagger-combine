package io.squark.swaggercombine

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.swagger.models.Swagger
import io.swagger.util.DeserializationModule
import io.swagger.util.Json
import java.io.File

open class SwaggerSpecificationsWriter(
    private val commandLineProperties: CommandLineProperties,
    private var mapper: ObjectMapper = createObjectMapper(),
) {

    open fun writeSwaggerSpecification(combined: Swagger) {
        val outputFile: String = commandLineProperties.getArguments(CommandLineArguments.OUTPUT_FILE)
            .stream()
            .findFirst()
            .orElse("result.json")

        val output = File(outputFile)
        mapper.writer(DefaultPrettyPrinter()).writeValue(output, combined)
    }

    companion object {
        @JvmStatic
        fun createObjectMapper(): ObjectMapper {
            val mapper = Json.mapper()

            val deserializerModule: Module = DeserializationModule(true, true)
            mapper.registerModule(deserializerModule)
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            return mapper
        }
    }

}
