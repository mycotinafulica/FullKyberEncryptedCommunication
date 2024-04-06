package org.amber.asparagus.fkec.crypto

import org.bouncycastle.jcajce.SecretKeyWithEncapsulation
import org.bouncycastle.jcajce.spec.KEMGenerateSpec
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.Security
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class Utils {
    companion object {
        fun registerBouncyCastleProvider() {
            if(Security.getProvider("BCPQC") == null){
                Security.addProvider(BouncyCastlePQCProvider())
            }
            val provider = Security.getProvider("BCPQC")
            println(provider)
        }

        fun generateEncapsulatedAesKey(publicKey: ByteArray): SecretKeyWithEncapsulation {
            val x509EncodedKeySpec = X509EncodedKeySpec(publicKey)
            val keyFactory         = KeyFactory.getInstance("KYBER", "BCPQC")
            val publicKeyFmt       = keyFactory.generatePublic(x509EncodedKeySpec)

            val keyGen = KeyGenerator.getInstance("KYBER", "BCPQC")
            keyGen.init(KEMGenerateSpec(publicKeyFmt, "AES"), SecureRandom())

            return keyGen.generateKey() as SecretKeyWithEncapsulation
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
    }
}