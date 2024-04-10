package org.amber.asparagus.fkec.interceptors

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.amber.asparagus.fkec.ApplicationState
import org.amber.asparagus.fkec.crypto.Utils

class KyberResponseInterceptor: Interceptor {
    companion object {
        private const val METHOD_GET  = "GET"
        private const val METHOD_POST = "POST"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // if handshake hasn't been done, then skip
        if(request.url.toString().contains("pre-handshake")){
            return chain.proceed(request)
        }

        // only support GET & POST method as of now
        if(request.method != METHOD_GET
            && request.method != METHOD_POST
        ) {
            return chain.proceed(request)
        }

        val response = chain.proceed(chain.request())
        val modifiedResponseBuilder = response.newBuilder()

        val plainResponseBody = extractAndDecryptResponseBody(response.body!!)
        val splitHeader = plainResponseBody.split("~~~HEADERS~~~")

        if(splitHeader.size > 1) {
            addExtractedHeaders(modifiedResponseBuilder, splitHeader[1])
        }

        val splitBody = splitHeader[0].split("~~~BODY~~~")
        if(splitBody.size > 1) {
            modifiedResponseBuilder.body(
                splitBody[1].toResponseBody("application/json".toMediaType())
            )
        }

        return modifiedResponseBuilder.build()
    }

    private fun addExtractedHeaders(respBuilder: Response.Builder, headerPart: String){
        val headerPairs = headerPart.split("||")
        headerPairs.forEach {
            val pair = it.split(":")
            respBuilder.header(pair[0], pair[1])
        }
    }

    private fun extractAndDecryptResponseBody(responseBody: ResponseBody): String {
        val encryptedBody = responseBody.string()
        return Utils.aesGcmDecrypt(encryptedBody, ApplicationState.sessionKey)
    }
}