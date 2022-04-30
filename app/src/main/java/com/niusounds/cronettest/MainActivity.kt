package com.niusounds.cronettest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.niusounds.cronettest.databinding.ActivityMainBinding
import com.niusounds.ktor.client.engine.cronet.Cronet
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val tag = "CronetTest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val client = HttpClient(
            engine = Cronet.create {
                // must pass context
                context = applicationContext

                // config is optional
                config = {
                    enableBrotli(true)
                    enableQuic(true)
                }

                // or, pass preconfigured CronetEngine.
                // If this is passed, context and config are ignored.
//                preconfigured = CronetEngine.Builder(applicationContext)
//                    .enableBrotli(true)
//                    .enableQuic(true)
//                    .build()

                // If followRedirects is true, redirect is handled by CronetEngine.
                // Default is false (but still be handled by ktor layer).
//                followRedirects = true
            }
        )
        ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)

            button.setOnClickListener {
                lifecycleScope.launch {
                    try {
                        val res = client.get("https://cloudflare-quic.com/")
                        button.text = "Status ${res.status} Protocol ${res.version}"
                        Log.d(tag, res.bodyAsText())
                    } catch (e: ServerResponseException) {
                        button.text = "Status ${e.response.status} Protocol ${e.response.version}"
                        Log.e(tag, "$e")
                    }
                }
            }
        }
    }
}