package org.amber.asparagus.fkec.crypto

import org.bouncycastle.jcajce.spec.KEMExtractSpec
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.Security
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoUtils {
    companion object {
        fun registerBouncyCastleProvider() {
            if(Security.getProvider("BCPQC") == null){
                Security.addProvider(BouncyCastlePQCProvider())
            }
            val provider = Security.getProvider("BCPQC")
            println(provider)
        }

        fun decapsulateKey(privateKey: String, encapsulatedKey: String): ByteArray {
            val privateKeyFmt        = loadPkcs8KyberPrivateKey(privateKey)
            val encapsulatedKeyBytes = Base64.getDecoder().decode(encapsulatedKey)
            val keyGen = KeyGenerator.getInstance("KYBER", "BCPQC")
            keyGen.init(KEMExtractSpec(privateKeyFmt, encapsulatedKeyBytes, "AES"), SecureRandom())

            return keyGen.generateKey().encoded
        }

        fun aesGcmDecrypt(cipherText: String, key: ByteArray): String {
            val cipherTextBytes = Base64.getDecoder().decode(cipherText)

            val ivPart     = cipherTextBytes.copyOfRange(0, 12)
            val cipherPart = cipherTextBytes.copyOfRange(12, cipherTextBytes.size)

            val cipher           = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmParameterSpec = GCMParameterSpec(128, ivPart)
            val secretKey        = SecretKeySpec(key, "AES")

            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)

            val plainText = cipher.doFinal(cipherPart)
            return String(plainText, StandardCharsets.UTF_8)
        }

        fun aesGcmEncrypt(plain: String, key : ByteArray): String {
            val iv = ByteArray(12)
            SecureRandom.getInstanceStrong().nextBytes(iv)

            val cipher           = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmParameterSpec = GCMParameterSpec(128, iv)
            val secretKey        = SecretKeySpec(key, "AES")

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)

            val cipherTextBytes = cipher.doFinal(plain.toByteArray(StandardCharsets.UTF_8))
            val final           = iv.plus(cipherTextBytes)

            return Base64.getEncoder().encodeToString(final)
        }

        private fun loadPkcs8KyberPrivateKey(privateKey: String): PrivateKey {
            val privateKeyDecoded   = Base64.getDecoder().decode(privateKey)
            val pkcs8EncodedKeySpec = PKCS8EncodedKeySpec(privateKeyDecoded)
            var keyFactory: KeyFactory? = null
            keyFactory = KeyFactory.getInstance("KYBER", "BCPQC")
            return keyFactory.generatePrivate(pkcs8EncodedKeySpec)
        }
    }
}