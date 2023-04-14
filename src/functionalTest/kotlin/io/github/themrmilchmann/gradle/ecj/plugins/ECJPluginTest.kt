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

import org.gradle.api.JavaVersion
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.PrintWriter
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Suppress("FunctionName")
class ECJPluginTest {

    private companion object {

        @JvmStatic
        private fun provideGradleVersions(): List<String> = buildList {
            // See https://docs.gradle.org/current/userguide/compatibility.html
            val javaVersion = JavaVersion.current()

            add("8.1")
            add("8.0.2")
            /*
             * We cannot support Gradle 8.0 (and 8.0.1) due to a regression that
             * prevents us from using a Java executable to launch ECJ.
             *
             * See https://github.com/gradle/gradle/issues/23990
             */
            add("7.6.1")
            add("7.6")

            @Suppress("UnstableApiUsage")
            if (javaVersion >= JavaVersion.VERSION_19) return@buildList

            add("7.5.1")
            add("7.5")

            @Suppress("UnstableApiUsage")
            if (javaVersion >= JavaVersion.VERSION_18) return@buildList

            add("7.4.2")
            add("7.4.1")
            add("7.4")
        }

    }

    @field:TempDir
    lateinit var projectDir: Path

    private val buildFile: Path get() = projectDir.resolve("build.gradle")
    private val settingsFile: Path get() = projectDir.resolve("settings.gradle")

    @ParameterizedTest
    @MethodSource("provideGradleVersions")
    fun `Run without project toolchain`(gradleVersion: String) {
        writeSettingsFile(gradleVersion)
        writeSourceFile()

        buildFile.writeText(
            """
            plugins {
                id 'java-library'
                id 'io.github.themrmilchmann.ecj'
            }
            
            repositories {
                mavenCentral()
            }
            """.trimIndent()
        )

        var buildResult = runGradleBuild(gradleVersion)
        assertEquals(TaskOutcome.SUCCESS, buildResult.task(":compileJava")?.outcome)
        assertTrue(buildResult.output.contains("Compiling with Java command line compiler '.+[/\\\\]bin[/\\\\]java(.exe)?'.".toRegex()))

        // Test up-to-date checks
        buildResult = runGradleBuild(gradleVersion)
        assertEquals(TaskOutcome.UP_TO_DATE, buildResult.task(":compileJava")?.outcome)
    }

    @ParameterizedTest
    @MethodSource("provideGradleVersions")
    fun `Run with compatible project toolchain`(gradleVersion: String) {
        writeSettingsFile(gradleVersion)
        writeSourceFile()

        buildFile.writeText(
            """
            plugins {
                id 'java-library'
                id 'io.github.themrmilchmann.ecj'
            }
            
            java {
                toolchain {
                    languageVersion = JavaLanguageVersion.of(17)
                }
            }
            
            repositories {
                mavenCentral()
            }
            """.trimIndent()
        )

        var buildResult = runGradleBuild(gradleVersion)
        assertEquals(TaskOutcome.SUCCESS, buildResult.task(":compileJava")?.outcome)
        assertTrue(buildResult.output.contains("Compiling with Java command line compiler '.+[/\\\\]bin[/\\\\]java(.exe)?'.".toRegex()))

        // Test up-to-date checks
        buildResult = runGradleBuild(gradleVersion)
        assertEquals(TaskOutcome.UP_TO_DATE, buildResult.task(":compileJava")?.outcome)
    }

    @ParameterizedTest
    @MethodSource("provideGradleVersions")
    fun `Run with incompatible project toolchain`(gradleVersion: String) {
        writeSettingsFile(gradleVersion)
        writeSourceFile()

        buildFile.writeText(
            """
            plugins {
                id 'java-library'
                id 'io.github.themrmilchmann.ecj'
            }
            
            java {
                toolchain {
                    languageVersion = JavaLanguageVersion.of(8)
                }
            }
            
            repositories {
                mavenCentral()
            }
            """.trimIndent()
        )

        var buildResult = runGradleBuild(gradleVersion)
        assertEquals(TaskOutcome.SUCCESS, buildResult.task(":compileJava")?.outcome)
        assertTrue(buildResult.output.contains("Compiling with Java command line compiler '.+[/\\\\]bin[/\\\\]java(.exe)?'.".toRegex()))

        // Test up-to-date checks
        buildResult = runGradleBuild(gradleVersion)
        assertEquals(TaskOutcome.UP_TO_DATE, buildResult.task(":compileJava")?.outcome)
    }

    private fun runGradleBuild(gradleVersion: String): BuildResult =
        GradleRunner.create()
            .withArguments("build", "--info")
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .withProjectDir(projectDir.toFile())
            .forwardStdError(PrintWriter(System.err))
            .forwardStdOutput(PrintWriter(System.out))
            .build()

    private fun writeSettingsFile(gradleVersion: String) {
        if (gradleVersion < "8.0") return

        settingsFile.writeText(
            """
            pluginManagement {
                plugins {
                    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.4.0'
                }
            }
            
            plugins {
                id 'org.gradle.toolchains.foojay-resolver-convention'
            }
            """.trimIndent()
        )
    }

    private fun writeSourceFile() {
        projectDir.resolve("src/main/java/com/example")
            .createDirectories()
            .resolve("Main.java")
            .writeText(
                """
                package com.example;
                
                public class Main {
            
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                
                }
                """.trimIndent()
            )

    }

}