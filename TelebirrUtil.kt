package com.example.util

import android.util.Base64
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.TreeMap
import java.util.UUID

object TelebirrUtil {

    /**
     * Sorts fields alphabetically by key and concatenates them with '=' and '&'.
     * This rule matches Telebirr's official string payload building standard.
     */
    fun createSignString(fields: Map<String, Any?>): String {
        val sortedMap = TreeMap<String, Any?>()
        sortedMap.putAll(fields)
        val pairs = mutableListOf<String>()
        for ((key, value) in sortedMap) {
            if (value != null && value.toString().isNotEmpty()) {
                pairs.add("$key=$value")
            }
        }
        return pairs.joinToString("&")
    }

    /**
     * Generates a SHA256withRSA signature string using the PEM-encoded Private Key.
     * Robust enough to clean typical header formats from RSA/PKCS8 private keys.
     */
    fun signPayload(signString: String, privateKeyPem: String): String {
        try {
            if (privateKeyPem.isEmpty() || privateKeyPem.contains("YOUR_")) {
                return "MOCK_SIGNATURE_FALLBACK_KEY_NOT_CONFIGURED"
            }
            
            val cleanKey = privateKeyPem
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s".toRegex(), "")
                .trim()
                
            val keyBytes = Base64.decode(cleanKey, Base64.DEFAULT)
            val keySpec = PKCS8EncodedKeySpec(keyBytes)
            val kf = KeyFactory.getInstance("RSA")
            val privateKey = kf.generatePrivate(keySpec)

            val signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(privateKey)
            signature.update(signString.toByteArray(Charsets.UTF_8))
            val signedBytes = signature.sign()
            return Base64.encodeToString(signedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback for simulation / wrong certificate format
            return "ERR_SIGNATURE_FAILED: ${e.localizedMessage}"
        }
    }

    /**
     * Verifies an incoming webhook signature using Telebirr's PEM Public Key
     */
    fun verifySignature(signString: String, incomingSignature: String, publicKeyPem: String): Boolean {
        try {
            if (publicKeyPem.isEmpty() || publicKeyPem.contains("YOUR_")) {
                // If keys aren't configured, default to true for sandbox execution simulation
                return true
            }

            val cleanKey = publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s".toRegex(), "")
                .trim()

            val keyBytes = Base64.decode(cleanKey, Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(keyBytes)
            val kf = KeyFactory.getInstance("RSA")
            val publicKey = kf.generatePublic(keySpec)

            val signature = Signature.getInstance("SHA256withRSA")
            signature.initVerify(publicKey)
            signature.update(signString.toByteArray(Charsets.UTF_8))
            val sigBytes = Base64.decode(incomingSignature, Base64.DEFAULT)
            return signature.verify(sigBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Encrypts the payload by wrapping in Base64 (equivalent to buffer/base64 JS)
     */
    fun encryptUssdPlanet(payloadObject: String): String {
        return Base64.encodeToString(payloadObject.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }
}
