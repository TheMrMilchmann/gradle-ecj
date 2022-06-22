# Samples

This directory contains sample projects that may be used as reference for
setting up and configuring the plugin.

The samples also serve as integration tests and are regularly tested during [CI](../.github/workflows/ci.yml)
runs. To run any of the samples locally, go to the respective directory and use
the following command on Unix/macOS:

    ./gradlew build --include-build ../../

or the following command on Windows:

    gradlew build --include ..\..\

For most changes, it is safe to assume that everything works as expected when
the build succeeds. However, if you want to verify that the correct files are
used, you can use the `--debug` flag to enable Gradle's debug logging.