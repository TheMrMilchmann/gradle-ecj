/*
 * Copyright (c) 2022 Leon Linhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.themrmilchmann.gradle.ecj.plugins

import io.github.themrmilchmann.gradle.ecj.internal.utils.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.compile.*
import org.gradle.jvm.toolchain.*
import org.gradle.kotlin.dsl.*

public class ECJPlugin : Plugin<Project> {

    private companion object {

        const val ECJ_CONFIGURATION_NAME = "ecj"

        const val DEFAULT_DEPENDENCY_GROUP = "org.eclipse.jdt"
        const val DEFAULT_DEPENDENCY_ARTIFACT = "ecj"
        const val DEFAULT_DEPENDENCY_VERSION = "3.30.0"

        const val MAIN = "org.eclipse.jdt.internal.compiler.batch.Main"

        /* The version for which a toolchain is requested if the project's toolchain is not compatible. */
        const val PREFERRED_JAVA_VERSION = 17

        /* The version required to run ECJ. */
        const val REQUIRED_JAVA_VERSION = 11

    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun apply(target: Project): Unit = applyTo(target) project@{
        /*
         * Make sure that the JavaPlugin is applied before this plugin, since we have to override some property
         * conventions. (Configuration happens in the same order in which the configuration actions are added.)
         */
        pluginManager.apply(JavaPlugin::class)

        val ecjConfiguration = configurations.create(ECJ_CONFIGURATION_NAME) {
            defaultDependencies {
                dependencies {
                    add(create(group = DEFAULT_DEPENDENCY_GROUP, name = DEFAULT_DEPENDENCY_ARTIFACT, version = DEFAULT_DEPENDENCY_VERSION))
                }
            }
        }

        val java = extensions.getByType<JavaPluginExtension>()
        val javaToolchains = extensions.getByType<JavaToolchainService>()

        tasks.withType<JavaCompile> {
            /* Overwrite the javaCompiler to make sure that it is not inferred from the toolchain. */
            javaCompiler.convention(this@project.provider { null })

            /* ECJ does not support generating JNI headers. Make sure the property is not used. */
            options.headerOutputDirectory.convention(null as Directory?)
            options.headerOutputDirectory.set(null as Directory?)
            options.headerOutputDirectory.finalizeValue()

            afterEvaluate {
                val toolchain = if (java.toolchain.languageVersion.orNull?.canCompileOrRun(REQUIRED_JAVA_VERSION) == true) {
                    java.toolchain
                } else {
                    java.toolchain { languageVersion.set(JavaLanguageVersion.of(PREFERRED_JAVA_VERSION)) }
                }

                val javaLauncher = javaToolchains.launcherFor(toolchain).orNull ?: error("Could not get launcher for toolchain: $toolchain")

                options.isFork = true
                options.forkOptions.executable = javaLauncher.executablePath.asFile.absolutePath

                val prevJvmArgs = options.forkOptions.jvmArgs
                options.forkOptions.jvmArgs = buildList(capacity = (prevJvmArgs?.size ?: 0) + 3) {
                    if (prevJvmArgs != null) addAll(prevJvmArgs)

                    add("-cp")
                    add(ecjConfiguration.asPath)
                    add(MAIN)
                }
            }
        }
    }

}