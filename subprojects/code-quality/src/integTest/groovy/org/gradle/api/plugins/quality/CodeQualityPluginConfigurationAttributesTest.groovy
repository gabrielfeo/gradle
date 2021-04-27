/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.plugins.quality

import org.gradle.integtests.fixtures.AbstractIntegrationSpec

class CodeQualityPluginConfigurationAttributesTest extends AbstractIntegrationSpec {

    def "code quality plugin runtime configuration is deprecated for consumption"() {
        given:
        settingsFile << """
            include("producer", "consumer")
            dependencyResolutionManagement {
                ${mavenCentralRepository()}
            }
        """
        file("producer/build.gradle") << """
            plugins {
                id("java-library")
                id("$plugin")
            }
            dependencies {
                implementation(localGroovy())
            }
        """

        when:
        file("consumer/build.gradle") << """
            plugins {
                id("java-library")
            }
            dependencies {
                implementation(project(path: ":producer", configuration: "$plugin"))
            }
        """

        then:
        executer.expectDocumentedDeprecationWarning("The $plugin configuration has been deprecated for consumption. This will fail with an error in Gradle 8.0. Consult the upgrading guide for further information: https://docs.gradle.org/current/userguide/upgrading_version_7.html#code_quality_plugin_configuration_consumption")
        succeeds("test")

        where:
        plugin << ['codenarc', 'pmd', 'checkstyle']
    }

}
