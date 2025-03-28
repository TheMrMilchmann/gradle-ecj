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
    alias(buildDeps.plugins.gradle.plugin.functional.test)
    alias(buildDeps.plugins.gradle.toolchain.switches)
    alias(buildDeps.plugins.kotlin.jvm)
    alias(buildDeps.plugins.kotlin.plugin.samwithreceiver)
    alias(buildDeps.plugins.plugin.publish)
    id("io.github.themrmilchmann.maven-publish-conventions")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }

    withJavadocJar()
    withSourcesJar()
}

kotlin {
    explicitApi()

    target {
        compilations.all {
            compilerOptions.configure {
                apiVersion = KotlinVersion.KOTLIN_1_8
                languageVersion = KotlinVersion.KOTLIN_1_8
            }
        }

        compilations.named("main").configure {
            compilerOptions.configure {
                @Suppress("DEPRECATION")
                apiVersion = KotlinVersion.KOTLIN_1_4

                /*
                 * 1.4 is deprecated, but we need it to stay compatible with old
                 * Gradle versions anyway. Thus, we suppress the compiler's
                 * warning.
                 */
                freeCompilerArgs.add("-Xsuppress-version-warnings")
            }
        }
    }
}

gradlePlugin {
    compatibility {
        minimumGradleVersion = "7.4"
    }

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

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 8
    }

    withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }

    withType<Test>().configureEach {
        useJUnitPlatform()

        @OptIn(ExperimentalToolchainSwitchesApi::class)
        javaLauncher.set(inferLauncher(default = project.javaToolchains.launcherFor {
            languageVersion = JavaLanguageVersion.of(23)
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

    withType<Jar>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true

        includeEmptyDirs = false
    }
}

val emptyJar = tasks.register<Jar>("emptyJar") {
    destinationDirectory = layout.buildDirectory.dir("emptyJar")
    archiveBaseName = "io.github.themrmilchmann.ecj.gradle.plugin"
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        if (name == "ecjPluginMarkerMaven") {
            artifact(emptyJar)
            artifact(emptyJar) { classifier = "javadoc" }
            artifact(emptyJar) { classifier = "sources" }
        }

        pom {
            name = "Gradle Eclipse Compiler for Java Plugin"
            description = "A Gradle plugin for using the Eclipse Compiler for Java (ECJ) for compiling Java files"
        }
    }
}

dependencies {
    compileOnlyApi(kotlin("stdlib"))

    functionalTestImplementation(kotlin("stdlib"))
    functionalTestImplementation(platform(buildDeps.junit.bom))
    functionalTestImplementation(buildDeps.junit.jupiter.api)
    functionalTestImplementation(buildDeps.junit.jupiter.params)
    functionalTestImplementation("dev.gradleplugins:gradle-test-kit:7.6.4")
    functionalTestRuntimeOnly(buildDeps.junit.jupiter.engine)
}