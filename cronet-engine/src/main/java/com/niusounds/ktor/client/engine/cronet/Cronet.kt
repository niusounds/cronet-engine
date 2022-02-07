package com.niusounds.ktor.client.engine.cronet

import io.ktor.client.*
import io.ktor.client.engine.*

/**
 * [HttpClientEngineFactory] using a [CronetEngine] based backend implementation
 * with the associated configuration [CronetConfig].
 */
object Cronet : HttpClientEngineFactory<CronetConfig> {
    override fun create(block: CronetConfig.() -> Unit): HttpClientEngine =
        CronetEngine(CronetConfig().apply(block))
}

class CronetEngineContainer : HttpClientEngineContainer {
    override val factory: HttpClientEngineFactory<*> = Cronet

    override fun toString(): String = "Cronet"
}