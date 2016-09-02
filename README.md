# ForgerClient
A simple Android clientfor the ForgerServer

# Building
Before you build the client, you'll need to set one variable. Go to ForgerClient/app/src/main/java/alex/forgerclient/MainActivity.java and change the UPLOAD_URL variable to whatever URL your server will reside at.

The build itself is also implemented using a Gradle Wrapper. If you're still unclear on how to use it, refer to the server setup description above.

In order to build the release APK, simply run:
`./gradlew assembleRelease.`

For other options, run:
`./gradlew tasks`

After that, you may find the APK at /ForgerClient/app/build/outputs/apk/