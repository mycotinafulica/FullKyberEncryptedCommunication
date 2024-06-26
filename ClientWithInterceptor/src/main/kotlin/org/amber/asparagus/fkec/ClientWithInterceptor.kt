package org.amber.asparagus.fkec

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.amber.asparagus.fkec.crypto.Utils
import org.amber.asparagus.fkec.dto.HandshakeInput
import org.amber.asparagus.fkec.dto.HandshakeOutput
import org.amber.asparagus.fkec.dto.TransactionRequest
import org.amber.asparagus.fkec.dto.TransactionResponse
import org.amber.asparagus.fkec.interceptors.KyberRequestInterceptor
import org.amber.asparagus.fkec.interceptors.KyberResponseInterceptor
import java.util.*

class ClientWithInterceptor {
    companion object {
        private val gson = Gson()
        @JvmStatic
        fun main(args: Array<String>) {
            Utils.registerBouncyCastleProvider()

            val client     = createOkhttpClient()
            val sessionKey = generateSessionKey()

            val handshakeRequest  = createHandshakeRequest(sessionKey)
            val handshakeResponse = client.newCall(handshakeRequest).execute()
            val handshakeOutputObj = gson.fromJson(handshakeResponse.body!!.string(), HandshakeOutput::class.java)

            ApplicationState.sessionInfo = handshakeOutputObj.sessionInfo

//            println("Session info : ${ApplicationState.sessionInfo}")

            val basicRequest = createBasicTransactionRequest()
            val response = client.newCall(basicRequest).execute()

            val respObj = gson.fromJson(response.body?.string(), TransactionResponse::class.java)

            println(respObj)
        }

        private fun createHandshakeRequest(secret: String): Request {
            val handshakeRequestObj = HandshakeInput(secret)

            val json = gson.toJson(handshakeRequestObj)
            val requestBody = json.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("http://localhost:8080/fkec/pre-handshake/handshake")
                .post(requestBody)
                .build()

            return request
        }

        private fun generateSessionKey(): String {
            val classLoader = Companion::class.java.classLoader
            val inputStream = classLoader.getResourceAsStream("public.crt")

            val publicKey = inputStream!!.readAllBytes()

            println("Public key : ${Base64.getEncoder().encodeToString(publicKey)}")

            val encapsulatedKey = Utils.generateEncapsulatedAesKey(publicKey)
            val encapsulatedB64 = Base64.getEncoder().encodeToString(encapsulatedKey.encapsulation)

            println("Generated Aes key : ${Base64.getEncoder().encodeToString(encapsulatedKey.encoded)}")
            println("EncapsulatedKey : ${Base64.getEncoder().encodeToString(encapsulatedKey.encapsulation)}")

            ApplicationState.sessionKey = encapsulatedKey.encoded

            return encapsulatedB64
        }

        private fun createBasicTransactionRequest(): Request {
            val requestBodyObj = TransactionRequest("myField", 0, true)
            val json        = gson.toJson(requestBodyObj)
            val requestBody = json.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("http://localhost:8080/fkec/post-handshake/transaction?param1=pararam&param2=2500")
                .post(requestBody)
                .header("x-header1", "MyHeader1")
                .header("x-header2", "MyHeader2")
                .build()

            return request
        }

        private fun createOkhttpClient(): OkHttpClient {
            val loggingInterceptor = HttpLoggingInterceptor {
                println(it)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY // Set the desired log level
            }

            return OkHttpClient.Builder()
                .addInterceptor(KyberRequestInterceptor())
                .addInterceptor(loggingInterceptor)
                .addInterceptor(KyberResponseInterceptor())
                .build()
        }
    }
}