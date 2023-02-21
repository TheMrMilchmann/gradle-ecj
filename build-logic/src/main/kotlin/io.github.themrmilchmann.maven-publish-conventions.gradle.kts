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
import io.github.themrmilchmann.build.*
import io.github.themrmilchmann.build.BuildType

plugins {
    signing
    `maven-publish`
    id("io.github.themrmilchmann.base-conventions")
}

publishing {
    repositories {
        maven {
            url = uri(deployment.repo)

            credentials {
                username = deployment.user
                password = deployment.password
            }
        }
    }
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set(project.name)
            url.set("https://github.com/TheMrMilchmann/gradle-toolchain-switches")

            licenses {
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/TheMrMilchmann/gradle-toolchain-switches/blob/master/LICENSE")
                        distribution.set("repo")
                    }
                }
            }

            developers {
                developer {
                    id.set("TheMrMilchmann")
                    name.set("Leon Linhart")
                    email.set("themrmilchmann@gmail.com")
                    url.set("https://github.com/TheMrMilchmann")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/TheMrMilchmann/gradle-toolchain-switches.git")
                developerConnection.set("scm:git:git://github.com/TheMrMilchmann/gradle-toolchain-switches.git")
                url.set("https://github.com/TheMrMilchmann/gradle-toolchain-switches.git")
            }
        }
    }
}

signing {
    isRequired = (deployment.type === BuildType.RELEASE)
    sign(publishing.publications)
}