# cronet-engine

[![Release](https://jitpack.io/v/niusounds/cronet-engine.svg)](https://jitpack.io/#niusounds/cronet-engine)

Ktor engine implementation using [Cronet](https://developer.android.com/guide/topics/connectivity/cronet) as backend which enables HTTP/3 with [ktor-client](https://ktor.io/docs/getting-started-ktor-client.html).

THIS IS **NOT** AN OFFICIAL IMPLEMENTATION.

Still very early stage. Currently simple `GET` request is only supported.

## Add dependencies

Step1: Add `https://jitpack.io` repository into settings.gradle(.kts)

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' } // <- Add this
    }
}
```

or legacy version (build.gradle)

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Step2: Add the dependency

```gradle
dependencies {
    implementation 'com.github.niusounds:cronet-engine:0.0.1'
}
```

## Create the client

```kotlin
val client = HttpClient(
    engine = Cronet.create {
        context = applicationContext
        config = { // this: CronetEngine.Builder
            enableBrotli(true)
            enableQuic(true)
        }
    }
)
```

You can pass existing CronetEngine instead.

```kotlin
val client = HttpClient(
    engine = Cronet.create {
        preconfigured = CronetEngine.Builder(applicationContext)
            .enableBrotli(true)
            .enableQuic(true)
            .build()
    }
)
```
