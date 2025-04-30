package com.example.prog_poe_2025

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordUtils {

    private const val SALT_LENGTH = 16 // Salt length (in bytes)
    private const val HASHING_ITERATIONS = 10000 // Number of iterations (higher is better)
    private const val KEY_LENGTH = 256 // Key length (bits)

    // Function to hash the password
    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val hashedPassword = hashWithPBKDF2(password, salt)
        return "$salt:$hashedPassword"
    }

    // Function to verify the password against the stored hash
    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split(":")
            if (parts.size != 2) return false

            val salt = parts[0]
            val storedPasswordHash = parts[1]

            val hashOfInputPassword = hashWithPBKDF2(password, salt)
            storedPasswordHash == hashOfInputPassword
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun generateSalt(): String {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) } // hex string
    }

    private fun hashWithPBKDF2(password: String, salt: String): String {
        val saltBytes = hexStringToByteArray(salt)
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, HASHING_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hashedBytes = factory.generateSecret(spec).encoded
        return hashedBytes.joinToString("") { "%02x".format(it) } // hex string
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val result = ByteArray(s.length / 2)
        for (i in s.indices step 2) {
            result[i / 2] = Integer.parseInt(s.substring(i, i + 2), 16).toByte()
        }
        return result
    }
}
