
This is a demo of the Kotlin Work Manager Extension for Couchbase Lite Replicators

This project requires Java 17 to compile.  If you get an unintelligible message like this:
```
> Could not resolve all files for configuration ':classpath'.
   > Could not resolve com.android.tools.build:gradle:8.0.2.
     Required by:
         project : > com.android.application:com.android.application.gradle.plugin:8.0.2
      > No matching variant of com.android.tools.build:gradle:8.0.2 was found. The consumer was configured to find a library for use during runtime, compatible with Java 8, packaged as a jar, and its dependencies declared externally, as well as attribute 'org.gradle.plugin.api-version' with value '8.0.2' but:
          - Variant 'apiElements' capability com.android.tools.build:gradle:8.0.2 declares a library, packaged as a jar, and its dependencies declared externally:
```

It is likely because you need to use a more recent version of Java to do the build.

Confirm that you have a Java 17 JDK installed on your machine.  Then, from Android Studio, select:
Android Studio > Settings > Build, Execution and Deployment > Build Tools > Gradle

... and choose the installed Java 17 JDK

