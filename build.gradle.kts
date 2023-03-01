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
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

@Suppress("DSL_SCOPE_VIOLATION") // See https://github.com/gradle/gradle/issues/22797
plugins {
    groovy
    alias(libs.plugins.gradle.toolchain.switches)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.samwithreceiver)
    alias(libs.plugins.plugin.publish)
    id("io.github.themrmilchmann.maven-publish-conventions")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }

    withJavadocJar()
    withSourcesJar()
}

kotlin {
    explicitApi()

    target {
        compilations.all {
            compilerOptions.configure {
                apiVersion.set(KotlinVersion.KOTLIN_1_4)
                languageVersion.set(KotlinVersion.KOTLIN_1_8)
            }
        }
    }
}

gradlePlugin {
    website.set("https://github.com/TheMrMilchmann/gradle-ecj")
    vcsUrl.set("https://github.com/TheMrMilchmann/gradle-ecj.git")

    plugins {
        create("ecj") {
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
    withType<Test>().configureEach {
        useJUnitPlatform()

        systemProperty("junit.jupiter.execution.parallel.enabled", true)
        systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    }
}

val emptyJar = tasks.register<Jar>("emptyJar") {
    destinationDirectory.set(buildDir.resolve("emptyJar"))
    archiveBaseName.set("io.github.themrmilchmann.ecj.gradle.plugin")
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        if (name == "ecjPluginMarkerMaven") {
            artifact(emptyJar)
            artifact(emptyJar) { classifier = "javadoc" }
            artifact(emptyJar) { classifier = "sources" }
        }

        pom {
            name.set("Gradle Eclipse Compiler for Java Plugin")
            description.set("A Gradle plugin for using the Eclipse Compiler for Java (ECJ) for compiling Java files")

            packaging = "jar"
        }
    }
}

dependencies {
    testImplementation(platform(libs.spock.bom))
    testImplementation(libs.spock.core)
}