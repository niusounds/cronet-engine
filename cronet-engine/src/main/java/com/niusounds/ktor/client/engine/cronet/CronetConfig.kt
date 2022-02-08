package com.niusounds.ktor.client.engine.cronet

import android.content.Context
import io.ktor.client.engine.*
import org.chromium.net.CronetEngine

/**
 * Configuration for [Cronet] client engine.
 */
class CronetConfig : HttpClientEngineConfig() {
    /**
     * Preconfigured CronetEngine instance instead of configuring one.
     */
    var preconfigured: CronetEngine? = null

    var context: Context? = getCurrentApplicationContext()
    var config: CronetEngine.Builder.() -> Unit = {}

    var followRedirects: Boolean = false
    var responseBufferSize: Int = 102400
}