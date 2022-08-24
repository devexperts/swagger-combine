/**
 * Copyright (C) 2007 Erik Håkansson Copyright (C) 2022 Oleg Aleksandrov
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
package io.squark.swaggercombine;

import io.swagger.models.Model;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;

import java.util.List;
import java.util.Map;

/**
 * Created by erik on 2017-01-05.
 */
public class SwaggerCombine {

    private final CommandLineProperties properties;

    private final List<Swagger> swaggers;

    public SwaggerCombine(CommandLineProperties properties, List<Swagger> swaggers) {
        this.properties = properties;
        this.swaggers = swaggers;
    }

    public Swagger combine() {
        String useStripBasePath =
                properties.getArguments(CommandLineArguments.STRIP_BASE_PATH)
                        .stream()
                        .findFirst()
                        .orElse("false");

        boolean stripBasePath = Boolean.parseBoolean(useStripBasePath);

        Swagger combinedSwagger = swaggers.get(0);

        for (Swagger swagger : swaggers) {
            if (swagger.getTags() != null) {
                for (Tag tag : swagger.getTags()) {
                    combinedSwagger.tag(tag);
                }
            }
            if (swagger.getSchemes() != null) {
                for (Scheme scheme : swagger.getSchemes()) {
                    combinedSwagger.scheme(scheme);
                }
            }
            if (swagger.getConsumes() != null) {
                for (String consumes : swagger.getConsumes()) {
                    combinedSwagger.consumes(consumes);
                }
            }
            if (swagger.getProduces() != null) {
                for (String produces : swagger.getProduces()) {
                    combinedSwagger.produces(produces);
                }
            }
            if (swagger.getSecurity() != null) {
                for (SecurityRequirement securityRequirement : swagger.getSecurity()) {
                    combinedSwagger.security(securityRequirement);
                }
            }
            if (swagger.getPaths() != null) {
                for (Map.Entry<String, Path> path : swagger.getPaths().entrySet()) {
                    if (stripBasePath) {
                        String replacedPath = path.getKey();
                        if (path.getKey().startsWith(combinedSwagger.getBasePath()) || path.getKey().startsWith("/" + combinedSwagger.getBasePath())) {
                            replacedPath = path.getKey()
                                    .replace(combinedSwagger.getBasePath(), "")
                                    .replaceAll("//", "/");
                        }
                        combinedSwagger.path(replacedPath, path.getValue());
                    } else {
                        combinedSwagger.path(path.getKey(), path.getValue());
                    }
                }
            }

            if (swagger.getSecurityDefinitions() != null) {
                for (Map.Entry<String, SecuritySchemeDefinition> securityDefinition :
                        swagger.getSecurityDefinitions().entrySet()) {
                    combinedSwagger.securityDefinition(securityDefinition.getKey(), securityDefinition.getValue());
                }
            }
            if (swagger.getDefinitions() != null) {
                for (Map.Entry<String, Model> definition : swagger.getDefinitions().entrySet()) {
                    combinedSwagger.addDefinition(definition.getKey(), definition.getValue());
                }
            }
            if (swagger.getParameters() != null) {
                for (Map.Entry<String, Parameter> parameter : swagger.getParameters().entrySet()) {
                    combinedSwagger.parameter(parameter.getKey(), parameter.getValue());
                }
            }
            if (swagger.getResponses() != null) {
                for (Map.Entry<String, Response> response : swagger.getResponses().entrySet()) {
                    combinedSwagger.response(response.getKey(), response.getValue());
                }
            }
            if (swagger.getVendorExtensions() != null) {
                for (Map.Entry<String, Object> vendorExtension : swagger.getVendorExtensions().entrySet()) {
                    combinedSwagger.vendorExtension(vendorExtension.getKey(), vendorExtension.getValue());
                }
            }
        }
        return combinedSwagger;
    }

    public static void main(String[] args) throws Exception {
        CommandLineProperties properties = new CommandLineProperties(args);

        if (properties.getArguments(CommandLineArguments.INPUT_FILE).size() < 2) {
            System.out.println("usage: swagger-combine base.json swagger2.json swagger3.json");
            System.out.println("Please use at least two input files");
            System.exit(1);
        }

        SwaggerSpecificationsReader swaggerSpecificationsParser = new SwaggerSpecificationsReader(properties);

        SwaggerCombine swaggerCombine = new SwaggerCombine(properties, swaggerSpecificationsParser.getSwaggers());
        Swagger combined = swaggerCombine.combine();

        SwaggerSpecificationsWriter swaggerSpecificationsWriter = new SwaggerSpecificationsWriter(properties, SwaggerSpecificationsWriter.createObjectMapper());

        swaggerSpecificationsWriter.writeSwaggerSpecification(combined);

        System.out.println("Done.");
    }

}
