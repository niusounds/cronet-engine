package com.niusounds.ktor.client.engine.cronet

import android.content.Context

/**
 * Retrieve current application context with reflection.
 * https://stackoverflow.com/questions/2002288/static-way-to-get-context-in-android
 *
 * This is required for auto initialization with ServiceLoader
 * because it is not possible to pass Context to [CronetConfig] with auto initialization.
 * I strongly recommend to not to rely auto initialization and
 * manually pass [CronetEngine] to HttpClient.
 */
internal fun getCurrentApplicationContext(): Context? =
    runCatching {
        Class.forName("android.app.ActivityThread")
            .getMethod("currentApplication")
            .invoke(null) as Context
    }.getOrNull()