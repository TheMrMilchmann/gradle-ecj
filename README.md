# Gradle Eclipse Compiler for Java Plugin

[![License](https://img.shields.io/badge/license-MIT-green.svg?style=flat-square&label=License)](https://github.com/TheMrMilchmann/gradle-ecj/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.themrmilchmann.gradle.ecj/gradle-ecj.svg?style=flat-square&label=Maven%20Central)](https://maven-badges.herokuapp.com/maven-central/io.github.themrmilchmann.gradle.ecj/gradle-ecj)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v.svg?style=flat-square&&label=Gradle%20Plugin%20Portal&logo=Gradle&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fio%2Fgithub%2Fthemrmilchmann%2Fecj%2Fio.github.themrmilchmann.ecj.gradle.plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/io.github.themrmilchmann.ecj)
![Gradle](https://img.shields.io/badge/Gradle-7.4-green.svg?style=flat-square&color=1ba8cb&logo=Gradle)
![Java](https://img.shields.io/badge/Java-8-green.svg?style=flat-square&color=b07219&logo=Java)

A Gradle plugin for using the Eclipse Compiler for Java (ECJ) for compiling Java files.

This plugin is loosely based on [Niklas Walter's Gradle Eclipse Compiler for Java Plugin](https://github.com/TwoStone/gradle-eclipse-compiler-plugin).


## Usage

## Gradle Groovy DSL

```groovy
plugins {
  id "io.github.themrmilchmann.ecj" version "0.1.0"
}
```

Usually, simply applying the plugin is enough to cover most use-cases. However,
in some scenarios, the ECJ artifact may be changed.

### Configuring ECJ (via dependency)

```groovy
dependencies {
    ecj "org.eclipse.jdt:ecj:3.32.0"
}
```

### Configuring ECJ (via extension)

```groovy
ecj {
  compilerGroupId = "org.eclipse.jdt"
  compilerArtifactId = "ecj"
  compilerVersion = "3.32.0"
}
```

## Gradle Kotlin DSL

### Applying the plugin

```kotlin
plugins {
    id("io.github.themrmilchmann.ecj") version "0.1.0"
}
```

Usually, simply applying the plugin is enough to cover most use-cases. However,
in some scenarios, the ECJ artifact may be changed.

### Configuring ECJ (via dependency)

```kotlin
dependencies {
    ecj("org.eclipse.jdt:ecj:3.32.0")
}
```

### Configuring ECJ (via extension)

```kotlin
ecj {
  compilerGroupId.set("org.eclipse.jdt")
  compilerArtifactId.set("ecj")
  compilerVersion.set("3.32.0")
}
```


## Compatibility Map

| Gradle | Minimal plugin version |
|--------|------------------------|
| 7.4    | 0.1.0                  |


## Plugin defaults

| Plugin version | Default ECJ version |
|----------------|---------------------|
| 0.2.0+         | 3.32.0              |
| 0.1.0+         | 3.30.0              |


## Building from source

### Setup

This project uses [Gradle's toolchain support](https://docs.gradle.org/8.0.2/userguide/toolchains.html)
to detect and select the JDKs required to run the build. Please refer to the
build scripts to find out which toolchains are requested.

An installed JDK 1.8 (or later) is required to use Gradle.

### Building

Once the setup is complete, invoke the respective Gradle tasks using the
following command on Unix/macOS:

    ./gradlew <tasks>

or the following command on Windows:

    gradlew <tasks>

Important Gradle tasks to remember are:
- `clean`                   - clean build results
- `build`                   - assemble and test the Java library
- `publishToMavenLocal`     - build and install all public artifacts to the
                              local maven repository

Additionally `tasks` may be used to print a list of all available tasks.


## License

```
Copyright (c) 2022-2023 Leon Linhart

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```