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

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

class ECJPluginTest extends Specification {

    private static def GRADLE_VERSIONS = [
        "7.4.2",
        "7.5"
    ]

    @TempDir
    File projectDir
    File buildFile
    File settingsFile

    def setup() {
        buildFile = new File(projectDir, "build.gradle")
        settingsFile = new File(projectDir, "settings.gradle")
    }

    @Unroll
    def "run without explicit toolchain (Gradle #gradleVersion)"() {
        given:
        writeHelloWorld()
        buildFile << """\
            plugins {
                id 'java-library'
                id 'io.github.themrmilchmann.ecj'
            }
            
            repositories {
                mavenCentral()
            }
        """.stripIndent()

        when:
        def result = runGradle(gradleVersion, "build", "--info")

        then:
        new File(projectDir, "build/classes/java/main/com/example/Main.class").isFile()
        result.task(":compileJava").outcome == TaskOutcome.SUCCESS

        where:
        gradleVersion << GRADLE_VERSIONS
    }

    private runGradle(String version, String... args) {
        def arguments = []
        arguments.addAll(args)
        arguments.add("-s")

        GradleRunner.create()
            .withGradleVersion(version)
            .withProjectDir(projectDir)
            .withArguments(arguments)
            .withPluginClasspath()
            .build()
    }

    private void writeHelloWorld(File baseDir = projectDir) {
        File outputFile = new File(baseDir, "src/main/java/com/example/Main.java")
        outputFile.parentFile.mkdirs()
        outputFile.createNewFile()

        outputFile << """\
            package com.example;
            
            public class Main {
            
                public static void main(String[] args) {
                    System.out.println("Hello World!");
                }
            
            }
            """.stripIndent()
    }

    protected File createFile(String path, File baseDir = projectDir) {
        File file = new File(baseDir, path)
        if (!file.exists()) {
            assert file.parentFile.mkdirs() || file.parentFile.exists()
            file.createNewFile()
        }
        return file
    }

}