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
package io.github.themrmilchmann.gradle.ecj

import io.github.themrmilchmann.gradle.ecj.plugins.ECJPlugin
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public abstract class ECJExtension @Inject constructor(objects: ObjectFactory) {

    public val compilerGroupId: Property<String> = objects.property<String>().convention(ECJPlugin.DEFAULT_DEPENDENCY_GROUP)
    public val compilerArtifactId: Property<String> = objects.property<String>().convention(ECJPlugin.DEFAULT_DEPENDENCY_ARTIFACT)
    public val compilerVersion: Property<String> = objects.property<String>().convention(ECJPlugin.DEFAULT_DEPENDENCY_VERSION)

    init {
        compilerGroupId.finalizeValueOnRead()
        compilerArtifactId.finalizeValueOnRead()
        compilerVersion.finalizeValueOnRead()
    }

}