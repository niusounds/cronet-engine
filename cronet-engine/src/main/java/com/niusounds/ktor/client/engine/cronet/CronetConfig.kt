package com.niusounds.ktor.client.engine.cronet

import android.content.Context
import io.ktor.client.engine.*
import org.chromium.net.CronetEngine

class CronetConfig : HttpClientEngineConfig() {
    var preconfigured: CronetEngine? = null

    var context: Context? = getCurrentApplicationContext()
    var config: CronetEngine.Builder.() -> Unit = {}

    var followRedirects: Boolean = false
}