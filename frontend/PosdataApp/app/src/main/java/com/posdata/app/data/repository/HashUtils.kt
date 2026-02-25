import java.security.MessageDigest

object HashUtils {
    fun sha512(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-512").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}