### 0.2.0

_Released 2023 Mar 06_

#### Improvements

- Added support for Gradle 8.0.2.
    - On Gradle 8.0.2 and later, the `javaCompiler` property of `JavaCompile`
      tasks is now respected and can be used to configure the JVM to be used to
      invoke the compiler.
    - Additionally, the up-to-date check should behave more predictably on Gradle 8.
- Reworked the plugin to avoid eager task creation by migration to Gradle's APIs
  for lazy configuration. [[GH-10](https://github.com/TheMrMilchmann/gradle-ecj/pull/10)] (Thanks to @arturbosch)
- Updated default ECJ version to `3.32.0` (from `3.30.0`).