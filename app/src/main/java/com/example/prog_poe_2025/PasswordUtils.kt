package com.example.prog_poe_2025

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordUtils {

    private const val SALT_LENGTH = 16 // Salt length (in bytes)
    private const val HASHING_ITERATIONS = 10000 // Number of iterations (higher is better)
    private const val KEY_LENGTH = 256 // Key length (bits)

    // Function to hash the password
    fun hashPassword(password: String): String {
        // Generate salt
        val salt = generateSalt()

        // Create PBKDF2 hash
        val hashedPassword = hashWithPBKDF2(password, salt)

        // Return the hashed password and salt as a base64 encoded string
        return "$salt:${hashedPassword}"
    }

    // Function to verify the password against the stored hash
    fun verifyPassword(password: String, storedHash: String): Boolean {
        // Split the stored hash into salt and the actual hash
        val (salt, storedPasswordHash) = storedHash.split(":")

        // Hash the input password with the same salt
        val hashOfInputPassword = hashWithPBKDF2(password, salt)

        // Compare the hashes
        return storedPasswordHash == hashOfInputPassword
    }

    private fun generateSalt(): String {
        // Secure random salt generation
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) } // Return as hex string
    }

    private fun hashWithPBKDF2(password: String, salt: String): String {
        // Convert the salt from hex string back to byte array
        val saltBytes = hexStringToByteArray(salt)

        // Generate the PBKDF2 hash
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, HASHING_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hashedBytes = factory.generateSecret(spec).encoded

        // Return the hash as a hex string
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }

    // Convert hex string to byte array
    private fun hexStringToByteArray(s: String): ByteArray {
        val byteArray = ByteArray(s.length / 2)
        for (i in s.indices step 2) {
            val byteValue = Integer.parseInt(s.substring(i, i + 2), 16)
            byteArray[i / 2] = byteValue.toByte()
        }
        return byteArray
    }
}
