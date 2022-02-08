package com.niusounds.ktor.client.engine.cronet

import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import org.chromium.net.*
import org.chromium.net.CronetEngine
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CronetEngine(
    override val config: CronetConfig,
) : HttpClientEngineBase("com.niusounds.ktor.client.engine.cronet") {

    private val cronetEngine: CronetEngine by lazy {
        config.preconfigured ?: config.context?.let { context ->
            CronetEngine.Builder(context)
                .apply(config.config)
                .build()
        } ?: error("Initialization failed. Must pass preconfigured CronetEngine or context.")
    }

    private val executor by lazy {
        Executors.newFixedThreadPool(config.threadsCount)
    }

    override val dispatcher: CoroutineDispatcher by lazy {
        executor.asCoroutineDispatcher()
    }

    @InternalAPI
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        val callContext = callContext()

        return executeHttpRequest(callContext, data)
    }

    private suspend fun executeHttpRequest(
        callContext: CoroutineContext,
        data: HttpRequestData
    ): HttpResponseData = suspendCancellableCoroutine { continuation ->
        val requestTime = GMTDate()

        // All chunked response is written to this.
        val responseCache = ByteArrayOutputStream()

        val callback = object : UrlRequest.Callback() {
            override fun onRedirectReceived(
                request: UrlRequest,
                info: UrlResponseInfo,
                newLocationUrl: String
            ) {
                if (config.followRedirects) {
                    request.followRedirect()
                } else {
                    request.cancel()
                    continuation.resume(
                        info.toHttpResponseData(
                            requestTime = requestTime,
                            callContext = callContext,
                        )
                    )
                }
            }

            override fun onResponseStarted(request: UrlRequest, info: UrlResponseInfo) {
                request.read(ByteBuffer.allocateDirect(256))
            }

            override fun onReadCompleted(
                request: UrlRequest,
                info: UrlResponseInfo,
                byteBuffer: ByteBuffer
            ) {
                // Write current received response data to responseCache
                byteBuffer.flip()
                val tempByteArray = ByteArray(byteBuffer.remaining())
                byteBuffer.get(tempByteArray)
                responseCache.write(tempByteArray)

                // Reuse previous ByteBuffer to read continuous response
                byteBuffer.clear()
                request.read(byteBuffer)
            }

            override fun onSucceeded(request: UrlRequest, info: UrlResponseInfo) {
                continuation.resume(
                    info.toHttpResponseData(
                        requestTime = requestTime,
                        callContext = callContext,
                        responseBody = responseCache.toByteArray(),
                    )
                )
            }

            override fun onFailed(
                request: UrlRequest,
                info: UrlResponseInfo,
                error: CronetException
            ) {
                continuation.resumeWithException(error)
            }
        }

        val request = cronetEngine.newUrlRequestBuilder(
            data.url.toString(),
            callback,
            executor,
        ).apply {
            setHttpMethod(data.method.value)

            data.headers.flattenForEach { key, value ->
                addHeader(key, value)
            }

            data.body.toUploadDataProvider()?.let {
                setUploadDataProvider(it, executor)
            }

            data.body.contentType?.let {
                addHeader(HttpHeaders.ContentType, it.contentType)
            }
        }.build()

        request.start()

        continuation.invokeOnCancellation {
            request.cancel()
        }
    }
}

private fun UrlResponseInfo.toHttpResponseData(
    requestTime: GMTDate,
    callContext: CoroutineContext,
    responseBody: ByteArray? = null,
): HttpResponseData {
    return HttpResponseData(
        statusCode = HttpStatusCode.fromValue(httpStatusCode),
        requestTime = requestTime,
        headers = Headers.build {
            allHeaders.forEach { (key, value) ->
                appendAll(key, value)
            }
        },
        version = when (negotiatedProtocol) {
            "h2" -> HttpProtocolVersion.HTTP_2_0
            "h3" -> HttpProtocolVersion.QUIC
            "quic/1+spdy/3" -> HttpProtocolVersion.SPDY_3
            else -> HttpProtocolVersion.HTTP_1_1
        },
        body = responseBody?.let { ByteReadChannel(it) } ?: ByteReadChannel.Empty,
        callContext = callContext,
    )
}

private fun OutgoingContent.toUploadDataProvider(): UploadDataProvider? =
    when (this) {
        is OutgoingContent.NoContent -> null
        is OutgoingContent.ByteArrayContent -> {
            UploadDataProviders.create(bytes())
        }
        is OutgoingContent.ReadChannelContent,
        is OutgoingContent.WriteChannelContent,
        is OutgoingContent.ProtocolUpgrade -> error("UnsupportedContentType $this")
    }
