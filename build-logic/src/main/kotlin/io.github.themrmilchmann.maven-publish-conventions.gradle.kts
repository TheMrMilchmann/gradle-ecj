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
plugins {
    id("io.github.themrmilchmann.base-conventions")
    `maven-publish`
    signing
}

publishing {
    repositories {
        val sonatypeBaseUrl: String? by project
        val sonatypeUsername: String? by project
        val sonatypePassword: String? by project
        val stagingRepositoryId: String? by project

        if (sonatypeBaseUrl != null && sonatypeUsername != null && sonatypePassword != null && stagingRepositoryId != null) {
            maven {
                url = uri("$sonatypeBaseUrl/service/local/staging/deployByRepositoryId/$stagingRepositoryId/")

                credentials {
                    username = sonatypeUsername
                    password = sonatypePassword
                }
            }
        }
    }
    publications.withType<MavenPublication>().configureEach {
        pom {
            name = project.name
            url = "https://github.com/TheMrMilchmann/gradle-ecj"

            licenses {
                licenses {
                    license {
                        name = "MIT"
                        url = "https://github.com/TheMrMilchmann/gradle-ecj/blob/master/LICENSE"
                        distribution = "repo"
                    }
                }
            }

            developers {
                developer {
                    id = "TheMrMilchmann"
                    name = "Leon Linhart"
                    email = "themrmilchmann@gmail.com"
                    url = "https://github.com/TheMrMilchmann"
                }
            }

            scm {
                connection = "scm:git:git://github.com/TheMrMilchmann/gradle-ecj.git"
                developerConnection = "scm:git:git://github.com/TheMrMilchmann/gradle-ecj.git"
                url = "https://github.com/TheMrMilchmann/gradle-ecj.git"
            }
        }
    }
}

signing {
    // Only require signing when publishing to a non-local maven repository
    setRequired { gradle.taskGraph.allTasks.any { it is PublishToMavenRepository } }

    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)

    sign(publishing.publications)
}