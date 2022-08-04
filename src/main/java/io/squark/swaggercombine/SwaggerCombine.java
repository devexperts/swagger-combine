/**
 * Copyright (C) 2007 Erik Håkansson
 * Copyright (C) 2022 Oleg Aleksandrov
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.squark.swaggercombine;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.swagger.models.Model;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.DeserializationModule;
import io.swagger.util.Json;

/**
 * Created by erik on 2017-01-05.
 */
public class SwaggerCombine {

    private Swagger firstSwagger;
    private List<Swagger> swaggers = new ArrayList<>();
    private Properties properties;

    public SwaggerCombine(List<String> files, Properties properties) throws Exception {
        this.properties = properties;
        boolean first = true;
        for (String fileName : files) {
            File file = new File(fileName);
            if (!file.exists()) {
                throw new Exception("File not found: " + file.getAbsolutePath());
            }
            SwaggerParser swaggerParser = new SwaggerParser();
            Swagger swagger = swaggerParser.parse(IOUtils.toString(new FileInputStream(file)));
            if (first) {
                this.firstSwagger = swagger;
                first = false;
            } else {
                swaggers.add(swagger);
            }
        }
    }

    public Swagger combine() {

        boolean stripBasePath = (properties != null &&
                Boolean.parseBoolean(properties.getProperty("--stripBasePath", "false")));
        for (Swagger swagger : swaggers) {
            if (swagger.getTags() != null) {
                for (Tag tag : swagger.getTags()) {
                    firstSwagger.tag(tag);
                }
            }
            if (swagger.getSchemes() != null) {
                for (Scheme scheme : swagger.getSchemes()) {
                    firstSwagger.scheme(scheme);
                }
            }
            if (swagger.getConsumes() != null) {
                for (String consumes : swagger.getConsumes()) {
                    firstSwagger.consumes(consumes);
                }
            }
            if (swagger.getProduces() != null) {
                for (String produces : swagger.getProduces()) {
                    firstSwagger.produces(produces);
                }
            }
            if (swagger.getSecurity() != null) {
                for (SecurityRequirement securityRequirement : swagger.getSecurity()) {
                    firstSwagger.security(securityRequirement);
                }
            }
            if (swagger.getPaths() != null) {
                for (Map.Entry<String, Path> path : swagger.getPaths().entrySet()) {
                    if (stripBasePath) {
                        String replacedPath = path.getKey();
                        if (path.getKey().startsWith(firstSwagger.getBasePath()) || path.getKey().startsWith("/" + firstSwagger.getBasePath())) {
                            replacedPath = path.getKey()
                                    .replace(firstSwagger.getBasePath(), "")
                                    .replaceAll("//", "/");
                        }
                        firstSwagger.path(replacedPath, path.getValue());
                    } else {
                        firstSwagger.path(path.getKey(), path.getValue());
                    }
                }
            }

            if (swagger.getSecurityDefinitions() != null) {
                for (Map.Entry<String, SecuritySchemeDefinition> securityDefinition :
                        swagger.getSecurityDefinitions().entrySet()) {
                    firstSwagger.securityDefinition(securityDefinition.getKey(), securityDefinition.getValue());
                }
            }
            if (swagger.getDefinitions() != null) {
                for (Map.Entry<String, Model> definition : swagger.getDefinitions().entrySet()) {
                    firstSwagger.addDefinition(definition.getKey(), definition.getValue());
                }
            }
            if (swagger.getParameters() != null) {
                for (Map.Entry<String, Parameter> parameter : swagger.getParameters().entrySet()) {
                    firstSwagger.parameter(parameter.getKey(), parameter.getValue());
                }
            }
            if (swagger.getResponses() != null) {
                for (Map.Entry<String, Response> response : swagger.getResponses().entrySet()) {
                    firstSwagger.response(response.getKey(), response.getValue());
                }
            }
            if (swagger.getVendorExtensions() != null) {
                for (Map.Entry<String, Object> vendorExtension : swagger.getVendorExtensions().entrySet()) {
                    firstSwagger.vendorExtension(vendorExtension.getKey(), vendorExtension.getValue());
                }
            }
        }
        return firstSwagger;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: swagger-combine base.json swagger2.json swagger3.json");
            System.out.println("Please use at least two input files");
            System.exit(1);
        }
        List<String> arguments = new ArrayList<>();
        Properties properties = new Properties();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String[] split = arg.split("=");
                properties.setProperty(split[0], split[1]);
            } else {
                arguments.add(arg);
            }
        }
        SwaggerCombine swaggerCombine = new SwaggerCombine(arguments, properties);
        Swagger combined = swaggerCombine.combine();

        ObjectMapper mapper = Json.mapper();

        Module deserializerModule = new DeserializationModule(true, true);
        mapper.registerModule(deserializerModule);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String outputFile = properties.getProperty("--outputFile");
        if (outputFile == null) {
            outputFile = "result.json";
        }
        File output = new File(outputFile);
        mapper.writer(new DefaultPrettyPrinter()).writeValue(output, combined);

        System.out.println("Done.");
    }

}
