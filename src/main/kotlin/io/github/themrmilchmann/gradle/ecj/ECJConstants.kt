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
package io.github.themrmilchmann.gradle.ecj

@Suppress("MayBeConstant")
public object ECJConstants {

    public val ECJ_CONFIGURATION_NAME: String = "ecj"

    public val DEFAULT_DEPENDENCY_GROUP: String = "org.eclipse.jdt"
    public val DEFAULT_DEPENDENCY_ARTIFACT: String = "ecj"
    public val DEFAULT_DEPENDENCY_VERSION: String = "3.32.0"

    public val MAIN: String = "org.eclipse.jdt.internal.compiler.batch.Main"

    /* The version for which a toolchain is requested if the project's toolchain is not compatible. */
    public val PREFERRED_JAVA_VERSION: Int = 17

    /* The version required to run ECJ. */
    public val REQUIRED_JAVA_VERSION: Int = 11

}