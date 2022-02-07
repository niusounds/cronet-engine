# ktor-client-cronet-engine

Ktor engine implementation using [Cronet](https://developer.android.com/guide/topics/connectivity/cronet) as backend which enables HTTP/3 with [ktor-client](https://ktor.io/docs/getting-started-ktor-client.html).

THIS IS **NOT** AN OFFICIAL IMPLEMENTATION.

Still very early stage. Currently simple `GET` request is only supported.

## Add dependencies

TODO

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
