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

/**
 * Utility class for parse command line arguments.
 *
 * @author Oleg Aleksandrov
 * @since 0.2.0
 */
open class CommandLineProperties(args: Array<String>) {
    private val properties: Map<CommandLineArguments, List<String>> = parseCommandLineProperties(args)

    /**
     * Returns list of values associated to given command line argument.
     * @since 0.2.0
     */
    open fun getArguments(arg: CommandLineArguments): List<String> {
        return properties[arg] ?: emptyList()
    }

    private fun parseCommandLineProperties(args: Array<String>): Map<CommandLineArguments, List<String>> {
        val properties = mutableMapOf<CommandLineArguments, MutableList<String>>()

        for (i in args.indices) {
            val arg = args[i]
            if (arg.startsWith("--")) {
                val key: CommandLineArguments = parseCommandLineKey(arg)
                    ?: throw RuntimeException("Unknown command line argument: $arg")

                val value = args[i + 1]

                val arguments = properties.getOrDefault(key, mutableListOf())
                arguments.add(value)
                properties[key] = arguments
            }
        }

        return properties
    }

    private fun parseCommandLineKey(key: String): CommandLineArguments? {
        return CommandLineArguments.values().firstOrNull { key == it.value }
    }
}
