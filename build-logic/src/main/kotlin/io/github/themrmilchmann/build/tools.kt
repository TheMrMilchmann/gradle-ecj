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
package io.github.themrmilchmann.build

import org.gradle.api.*
import org.gradle.kotlin.dsl.*

private const val DEPLOYMENT_KEY = "io.github.themrmilchmann.build.Deployment"

val Project.deployment: Deployment
    get() =
        if (extra.has(DEPLOYMENT_KEY)) {
            extra[DEPLOYMENT_KEY] as Deployment
        } else
            (when {
                hasProperty("release") -> Deployment(
                    BuildType.RELEASE,
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/",
                    getProperty("sonatypeS01Username"),
                    getProperty("sonatypeS01Password")
                )
                hasProperty("snapshot") -> Deployment(
                    BuildType.SNAPSHOT,
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/",
                    getProperty("sonatypeS01Username"),
                    getProperty("sonatypeS01Password")
                )
                else -> Deployment(BuildType.LOCAL, repositories.mavenLocal().url.toString())
            }).also { extra[DEPLOYMENT_KEY] = it }

fun Project.getProperty(k: String): String =
    if (extra.has(k))
        extra[k] as String
    else
        System.getenv(k) ?: ""