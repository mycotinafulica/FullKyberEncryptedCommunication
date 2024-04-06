package org.amber.asparagus.fkec

import org.amber.asparagus.fkec.crypto.SessionDb
import org.amber.asparagus.fkec.crypto.CryptoUtils
import org.amber.asparagus.fkec.dto.HandshakeInput
import org.amber.asparagus.fkec.dto.HandshakeOutput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/fkec/pre-handshake")
class PreHandshakeController {
    @Autowired
    @Qualifier("privateKey")
    private lateinit var privateKey: String

    @PostMapping("/handshake")
    fun handshake(@RequestBody handshakeInput: HandshakeInput): ResponseEntity<HandshakeOutput> {
        val encapsulatedKey = handshakeInput.encapsulatedSecret

        // Please use proper logging framework for production
        println("Encapsulated key : $encapsulatedKey")

        val sessionKey      = CryptoUtils.decapsulateKey(privateKey, encapsulatedKey)
        val sessionId       = UUID.randomUUID().toString()

        println("Session ID  : $sessionId")
        println("Session key : ${Base64.getEncoder().encodeToString(sessionKey)}")

        SessionDb.sessions.put(sessionId, sessionKey)

        val response = HandshakeOutput("00", CryptoUtils.aesGcmEncrypt(sessionId, sessionKey))

        return ResponseEntity<HandshakeOutput>(response, HttpStatus.OK)
    }
}