/*
 * Copyright (c) 2022-2023 Leon Linhart
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

import io.github.themrmilchmann.gradle.ecj.ECJExtension
import io.github.themrmilchmann.gradle.ecj.internal.utils.*
import org.gradle.api.*
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.*
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.compile.*
import org.gradle.jvm.toolchain.*
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.util.GradleVersion

public class ECJPlugin : Plugin<Project> {

    internal companion object {

        const val ECJ_CONFIGURATION_NAME = "ecj"

        const val DEFAULT_DEPENDENCY_GROUP = "org.eclipse.jdt"
        const val DEFAULT_DEPENDENCY_ARTIFACT = "ecj"
        const val DEFAULT_DEPENDENCY_VERSION = "3.32.0"

        const val MAIN = "org.eclipse.jdt.internal.compiler.batch.Main"

        /* The version for which a toolchain is requested if the project's toolchain is not compatible. */
        const val PREFERRED_JAVA_VERSION = 17

        /* The version required to run ECJ. */
        const val REQUIRED_JAVA_VERSION = 11

        private val GRADLE_8_0 = GradleVersion.version("8.0")

    }

    override fun apply(target: Project): Unit = applyTo(target) project@{
        /*
         * Make sure that the JavaPlugin is applied before this plugin, since we have to override some properties.
         * (Configuration happens in the same order in which the configuration actions are added.)
         */
        pluginManager.apply(JavaPlugin::class.java)

        val ecjExtension = extensions.create("ecj", ECJExtension::class.java)

        val ecjConfiguration = configurations.create(ECJ_CONFIGURATION_NAME) {
            isCanBeConsumed = false
            isCanBeResolved = true

            defaultDependencies {
                val compilerGroupId = ecjExtension.compilerGroupId.orNull ?: error("ECJ compilerGroupId may not be null")
                val compilerArtifactId = ecjExtension.compilerArtifactId.orNull ?: error("ECJ compilerArtifactId may not be null")
                val compilerVersion = ecjExtension.compilerVersion.orNull ?: error("ECJ compilerVersion may not be null")

                dependencies.add(target.dependencies.create("$compilerGroupId:$compilerArtifactId:$compilerVersion"))
            }
        }

        val java = extensions.getByType(JavaPluginExtension::class.java)
        val javaToolchains = extensions.getByType(JavaToolchainService::class.java)

        tasks.withType(JavaCompile::class.java).configureEach {
            /* ECJ does not support generating JNI headers. Make sure the property is not used. */
            options.headerOutputDirectory.set(provider { null })

            options.isFork = true
            options.forkOptions.jvmArgumentProviders.add(ECJCommandLineArgumentProvider(ecjConfiguration))

            /*
             * forkOptions.executable is, unfortunately, not a property. Setting it eagerly here (at configuration time)
             * would be a bad decision and could lead to ordering issues. Thus, we create a provider here instead.
             * However, we still have to register this provider as task input for proper incremental builds.
             * Unfortunately, it is not possible to replicate the functionality of @Nested for programmatically defined
             * task inputs. Hence, we can only use the languageVersion for now. This could lead to some undesired cache
             * hits but the chance should be low and there is very little risk of this being an issue.
             */
            val defaultJavaCompiler = provider {
                if (java.toolchain.languageVersion.orNull?.canCompileOrRun(REQUIRED_JAVA_VERSION) == true) {
                    javaToolchains.compilerFor(java.toolchain).orNull ?: error("Could not get launcher for toolchain: ${java.toolchain}")
                } else {
                    javaToolchains.compilerFor {
                        languageVersion.set(JavaLanguageVersion.of(PREFERRED_JAVA_VERSION))
                    }.orNull ?: error("Could not provision launcher for Java $PREFERRED_JAVA_VERSION")
                }
            }

            if (GradleVersion.current() < GRADLE_8_0) {
                /* Overwrite the javaCompiler to make sure that it is not inferred from the toolchain. */
                javaCompiler.set(provider { null })
                inputs.property("javaLauncher", defaultJavaCompiler.map { it.metadata.languageVersion.asInt() })
            } else {
                this.javaCompiler.convention(defaultJavaCompiler)
            }

            /* See https://docs.gradle.org/7.4.2/userguide/validation_problems.html#implementation_unknown */
            @Suppress("ObjectLiteralToLambda")
            doFirst(object : Action<Task> {
                override fun execute(t: Task) {
                    val javacExecutable = javaCompiler.orElse(defaultJavaCompiler).get().executablePath.asFile

                    /*
                     * We replace the "javac" part of the original executable name instead of simply resolving "java" to
                     * account for file extensions.
                     */
                    val javaExecutable = javacExecutable.resolveSibling(javacExecutable.name.replace("javac", "java"))

                    options.forkOptions.executable = javaExecutable.absolutePath
                }
            })
        }
    }

    private class ECJCommandLineArgumentProvider(
        @get:Classpath
        val compilerClasspath: FileCollection
    ) : CommandLineArgumentProvider {

        override fun asArguments(): MutableIterable<String> =
            mutableListOf("-cp", compilerClasspath.asPath, MAIN)

    }

}