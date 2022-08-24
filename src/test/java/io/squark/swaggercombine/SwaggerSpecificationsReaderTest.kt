package io.squark.swaggercombine

import org.junit.Assert
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.File

class SwaggerSpecificationsReaderTest {
    private val filePaths = getFilePaths(listOf("version1.json", "version2.json", "version3.json"))

    @Test
    fun `should read swagger specifications`() {
        val commandLineProperties: CommandLineProperties = mock {
            on { getArguments(CommandLineArguments.INPUT_FILE) }.doReturn(filePaths)
        }

        val swaggerSpecificationsReader = SwaggerSpecificationsReader(commandLineProperties)

        Assert.assertEquals(swaggerSpecificationsReader.swaggers.size, filePaths.size)
    }

    @Test(expected = Exception::class)
    fun `should not read swagger specifications`() {
        val commandLineProperties: CommandLineProperties = mock {
            on { getArguments(CommandLineArguments.INPUT_FILE) }.doReturn(listOf("path"))
        }

        SwaggerSpecificationsReader(commandLineProperties)
    }

    private fun getFilePaths(names: List<String>): List<String> {
        return names.map {
            val classLoader = javaClass.classLoader
            val file = File(classLoader.getResource(it)!!.file)
            file.absolutePath
        }
    }
}
