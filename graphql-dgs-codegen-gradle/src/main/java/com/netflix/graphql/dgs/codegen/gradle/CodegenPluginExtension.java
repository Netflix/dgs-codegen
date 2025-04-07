/*
 *
 *  Copyright 2020 Netflix, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.netflix.graphql.dgs.codegen.gradle;

import org.gradle.api.provider.Property;

/**
 * Configuration for adding the client core dependency.
 */
public abstract class CodegenPluginExtension {

    /**
     * Enabled by default
     */
    public CodegenPluginExtension() {
        getClientCoreConventionsEnabled().convention(true);
    }

    /**
     * @return Enable adding client core dependency
     */
    public abstract Property<Boolean> getClientCoreConventionsEnabled();

    /**
     * @return Version of the client core library to add
     */
    public abstract Property<String> getClientCoreVersion();

    /**
     * Describes the configuration/scope that the client-core library is going to be added to.
     * @return Scope of the added dependency. Defaults to {@link ClientUtilsConventions#GRADLE_CLASSPATH_CONFIGURATION}
     */
    public abstract Property<String> getClientCoreScope();
}
