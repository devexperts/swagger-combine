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

import org.junit.Assert
import org.junit.Test

/**
 * Tests for [CommandLinePropertiesParser]
 *
 * @author Oleg Aleksandrov
 * @since 0.2.0
 */
class CommandLinePropertiesParserTest {

    @Test
    fun `Inputs set up correctly`() {
        val inputFileName1 = "test1.json"
        val inputFileName2 = "test2.json"

        val parser = CommandLinePropertiesParser(
            arrayOf(
                CommandLineArguments.INPUT_FILE.value,
                inputFileName1,
                CommandLineArguments.INPUT_FILE.value,
                inputFileName2,
            )
        )

        val inputs = parser.get(CommandLineArguments.INPUT_FILE)
        Assert.assertTrue("Inputs should contains $inputFileName1", inputs.contains(inputFileName1))
        Assert.assertTrue("Inputs should contains $inputFileName2", inputs.contains(inputFileName2))
    }

    @Test
    fun `Output set up correctly`() {
        val outputFileName = "output.json"
        val parser = CommandLinePropertiesParser(
            arrayOf(
                CommandLineArguments.OUTPUT_FILE.value,
                outputFileName,
            )
        )

        val outputs = parser.get(CommandLineArguments.OUTPUT_FILE)
        Assert.assertTrue("Outputs should contains $outputFileName", outputs.contains(outputFileName))
    }

    @Test
    fun `Strip base path set up correctly`() {
        val stripBasePath = true
        val parser = CommandLinePropertiesParser(
            arrayOf(
                CommandLineArguments.STRIP_BASE_PATH.value,
                stripBasePath.toString()
            )
        )

        val stripBasePaths = parser.get(CommandLineArguments.STRIP_BASE_PATH)
        Assert.assertTrue("Strip base paths should contains $stripBasePath", stripBasePaths.contains(stripBasePath.toString()))
    }

    @Test
    fun `Default properties value`() {
        val parser = CommandLinePropertiesParser(arrayOf())

        Assert.assertTrue("Inputs should be empty", parser.get(CommandLineArguments.INPUT_FILE).isEmpty())
        Assert.assertTrue("Output file name should be empty", parser.get(CommandLineArguments.OUTPUT_FILE).isEmpty())
        Assert.assertTrue("Strip base path should be empty", parser.get(CommandLineArguments.STRIP_BASE_PATH).isEmpty())
    }

    @Test(expected = RuntimeException::class)
    fun `Wrong argument`() {
        CommandLinePropertiesParser(
            arrayOf(
                "--wrong argument",
                "wrong argument",
            )
        )
    }
}
