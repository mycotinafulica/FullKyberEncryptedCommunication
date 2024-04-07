package org.amber.asparagus.fkec.interceptors

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import org.amber.asparagus.fkec.ApplicationState
import org.amber.asparagus.fkec.crypto.Utils

class RequestInterceptor : Interceptor {
    companion object {
        private const val METHOD_GET  = "GET"
        private const val METHOD_POST = "POST"
    }
    override fun intercept(chain: Interceptor.Chain): Response {
        println("Proceeding with request interceptor")

        val request = chain.request()

        if(request.url.toString().contains("pre-handshake")){
            return chain.proceed(request)
        }

        if(request.method != METHOD_GET
            && request.method != METHOD_POST) {
            return chain.proceed(request)
        }

        val requestBuilder = request.newBuilder()
        requestBuilder.addHeader("x-session-info", ApplicationState.sessionInfo)

        val payloadToEncrypt = StringBuilder()

        if(request.method == METHOD_POST) {
            extractBodyContent(payloadToEncrypt, request.body, requestBuilder)
        }
        extractQueryParameters(payloadToEncrypt, request, requestBuilder)
        extractHeaders(payloadToEncrypt, request, requestBuilder)

        println("Payload to encrypt : $payloadToEncrypt")

        val encryptedPayload = Utils.aesGcmEncrypt(payloadToEncrypt.toString(), ApplicationState.sessionKey)

        if(request.method == METHOD_GET) {
            reconstructQueryParamsForGetRequest(request, requestBuilder, encryptedPayload)
        }
        else if(request.method == METHOD_POST) {
            reconstructBody(request, requestBuilder, encryptedPayload)
        }

        val modifiedRequest = requestBuilder.build()

        return chain.proceed(modifiedRequest)
    }

    private fun reconstructBody(request: Request,
                                requestBuilder: Request.Builder,
                                encryptedData: String) {
        if(request.body != null) {
            val newRequestBody = encryptedData.toRequestBody(
                "text/plain".toMediaType()
            )

            requestBuilder.method(request.method, newRequestBody)
        }
    }

    private fun reconstructQueryParamsForGetRequest(request: Request,
                                                    requestBuilder: Request.Builder,
                                                    encryptedData: String) {
        val httpBuilder = request.url.newBuilder()

        request.url.queryParameterNames.forEach {
            httpBuilder.removeAllQueryParameters(it)
        }
        httpBuilder.addQueryParameter("data", encryptedData)
    }

    private fun extractHeaders(sb: StringBuilder,
                               request: Request,
                               requestBuilder: Request.Builder) {
        sb.append("~~~HEADERS~~~")

        var headerStringified = ""
        request.headers.names().forEach {
            if(it.startsWith("x-")
                && it != "x-orig-content-type"
                && it != "x-session-info") {

                requestBuilder.removeHeader(it)
                val values = request.headers.values(it)

                values.forEach {value ->
                    headerStringified += "$it:$value||"
                }
            }
        }

        if(headerStringified.endsWith("||")) {
            sb.append(headerStringified.substring(0, headerStringified.length - 2))
        }
        else {
            sb.append(headerStringified)
        }
    }

    private fun extractQueryParameters(sb: StringBuilder,
                                       request: Request,
                                       requestBuilder : Request.Builder) {
        val httpBuilder = request.url.newBuilder()

        request.url.queryParameterNames.forEach {
            httpBuilder.removeAllQueryParameters(it)
        }

        val splitRequestUrl  = request.url.toString().split("?")
        var queryParamString = ""
        if(splitRequestUrl.size > 1) {
            queryParamString = splitRequestUrl[1]
        }

        sb.append("~~~PARAMS~~~")
        sb.append(queryParamString)

        requestBuilder.url(httpBuilder.build())
    }

    private fun extractBodyContent(sb: StringBuilder,
                                   requestBody: RequestBody?,
                                   requestBuilder : Request.Builder) {
        sb.append("~~~BODY~~~")

        if(requestBody != null) {
            requestBuilder.addHeader("x-orig-content-type"
                , requestBody.contentType().toString())

            val buffer = Buffer()
            requestBody.writeTo(buffer)
            sb.append(buffer.readUtf8())
        }
    }
}