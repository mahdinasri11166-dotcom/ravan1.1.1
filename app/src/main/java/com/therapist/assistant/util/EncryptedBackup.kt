package com.therapist.assistant.util

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * پشتیبان رمزدار با رمز عبور کاربر (AES-GCM).
 * فرمت خروجی متنی: DNG1:<base64(salt+iv+cipher)>
 * از java.util.Base64 استفاده می‌کند تا تست واحد روی JVM/CI هم کار کند.
 */
object EncryptedBackup {

    private const val PREFIX = "DNG1:"
    private const val ITERATIONS = 120_000
    private const val KEY_LEN = 256
    private const val SALT_LEN = 16
    private const val IV_LEN = 12

    fun isEncrypted(payload: String): Boolean = payload.trim().startsWith(PREFIX)

    fun encrypt(plainJson: String, password: String): String {
        require(password.isNotBlank()) { "برای رمزنگاری، رمزی وارد کنید (یا بدون رمز ذخیره کنید)" }
        val salt = ByteArray(SALT_LEN).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)
        val iv = ByteArray(IV_LEN).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(plainJson.toByteArray(Charsets.UTF_8))
        val out = ByteArray(salt.size + iv.size + encrypted.size)
        System.arraycopy(salt, 0, out, 0, salt.size)
        System.arraycopy(iv, 0, out, salt.size, iv.size)
        System.arraycopy(encrypted, 0, out, salt.size + iv.size, encrypted.size)
        return PREFIX + Base64.getEncoder().encodeToString(out)
    }

    fun decrypt(payload: String, password: String): String {
        val raw = payload.trim().removePrefix(PREFIX)
        val all = Base64.getDecoder().decode(raw)
        require(all.size > SALT_LEN + IV_LEN + 16) { "داده نامعتبر" }
        val salt = all.copyOfRange(0, SALT_LEN)
        val iv = all.copyOfRange(SALT_LEN, SALT_LEN + IV_LEN)
        val encrypted = all.copyOfRange(SALT_LEN + IV_LEN, all.size)
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        val plain = cipher.doFinal(encrypted)
        return String(plain, Charsets.UTF_8)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LEN)
        val secret = factory.generateSecret(spec).encoded
        return SecretKeySpec(secret, "AES")
    }
}
