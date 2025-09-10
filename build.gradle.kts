/*
 * Copyright (c) 2022-2024 Leon Linhart
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
import io.github.themrmilchmann.gradle.toolchainswitches.ExperimentalToolchainSwitchesApi
import io.github.themrmilchmann.gradle.toolchainswitches.inferLauncher
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(buildDeps.plugins.binary.compatibility.validator)
    alias(buildDeps.plugins.gradle.toolchain.switches)
    alias(buildDeps.plugins.kotlin.jvm)
    alias(buildDeps.plugins.kotlin.plugin.samwithreceiver)
    alias(buildDeps.plugins.plugin.publish)
    id("io.github.themrmilchmann.maven-publish-conventions")
    `jvm-test-suite`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }

    withJavadocJar()
    withSourcesJar()
}

kotlin {
    explicitApi()

    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_2_2
        languageVersion = KotlinVersion.KOTLIN_2_2

        jvmTarget = JvmTarget.JVM_17

        freeCompilerArgs.add("-Xjdk-release=17")
    }
}

gradlePlugin {
    website = "https://github.com/TheMrMilchmann/gradle-ecj"
    vcsUrl = "https://github.com/TheMrMilchmann/gradle-ecj.git"

    plugins {
        register("ecj") {
            id = "io.github.themrmilchmann.ecj"
            displayName = "Gradle Eclipse Compiler for Java Plugin"
            description = "A Gradle plugin for using the Eclipse Compiler for Java (ECJ) for compiling Java files"
            tags.addAll("compile", "ecj", "eclipse compiler for java", "java")

            implementationClass = "io.github.themrmilchmann.gradle.ecj.plugins.ECJPlugin"
        }
    }
}

samWithReceiver {
    annotation("org.gradle.api.HasImplicitReceiver")
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        withType<JvmTestSuite>().configureEach {
            useJUnitJupiter()

            dependencies {
                implementation(platform(buildDeps.junit.bom))
                implementation(buildDeps.junit.jupiter.api)
                implementation(buildDeps.junit.jupiter.params)
                runtimeOnly(buildDeps.junit.jupiter.engine)
                runtimeOnly(buildDeps.junit.platform.launcher)
            }
        }

        val test by getting

        register<JvmTestSuite>("functionalTest") {
            dependencies {
                implementation(gradleTestKit())
                runtimeOnly(layout.files(tasks.named("pluginUnderTestMetadata")))
            }

            targets.configureEach {
                testTask.configure {
                    shouldRunAfter(test)
                }
            }
        }
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 17
    }

    withType<Test>().configureEach {
        useJUnitPlatform()

        @OptIn(ExperimentalToolchainSwitchesApi::class)
        javaLauncher.set(inferLauncher(default = project.javaToolchains.launcherFor {
            languageVersion = JavaLanguageVersion.of(17)
        }))

        /*
         * Fully disable parallel execution on CI to avoid running into memory
         * limits on GitHub Actions.
         *
         * See https://github.com/TheMrMilchmann/gradle-ecj/issues/11
         * See https://github.com/gradle/gradle/issues/12247
         */
        val parallelExecution = providers.environmentVariable("CI")
            .map { !it.toBoolean() }
            .orElse(true)

        inputs.property("junit.jupiter.execution.parallel.enabled", parallelExecution)

        systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")

        doFirst {
            systemProperty("junit.jupiter.execution.parallel.enabled", parallelExecution.get())
        }
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name = "Gradle Eclipse Compiler for Java Plugin"
            description = "A Gradle plugin for using the Eclipse Compiler for Java (ECJ) for compiling Java files"
        }
    }
}

dependencies {
    compileOnlyApi(kotlin("stdlib"))
    compileOnlyApi(libs.gradle.api) {
        capabilities {
            // https://github.com/gradle/gradle/issues/29483
            requireCapability("org.gradle.experimental:gradle-public-api-internal")
        }
    }
}
