/**
 * Copyright (C) 2022 Oleg Aleksandrov
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.squark.swaggercombine

import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset

/**
 * Utility class for parse swagger files.
 *
 * @author Oleg Aleksandrov
 * @since 0.2.0
 */
open class SwaggerSpecificationsReader(private val commandLineProperties: CommandLineProperties) {
    open val swaggers: List<Swagger> = readSwaggerSpecifications()

    @Throws(Exception::class)
    private fun readSwaggerSpecifications(): List<Swagger> {
        val inputFiles = commandLineProperties.getArguments(CommandLineArguments.INPUT_FILE)
        val swaggerParser = SwaggerParser()

        return inputFiles.map {
            val file = File(it)
            if (!file.exists()) {
                throw Exception("File not found: " + file.absolutePath)
            }

            swaggerParser.parse(IOUtils.toString(FileInputStream(file), Charset.defaultCharset()))
        }
    }

}
