package nl.ing.api.contacting.conf.business.kafka

import java.nio.ByteBuffer

/**
 * Copied from {@link com.ing.apisdk.toolkit.connectivity.kafka.avro.serde.AbstractEncryptionAwareSerDe}
 */
object EncryptionAwareSerDe {
  val ENCRYPTED_HEADER_KEY = "encrypted"
  val ENCRYPTED_VERSION_HEADER_KEY = "encryption-version"
  private val ENCRYPTION_VERSION = 1
  private val IS_ENCRYPTED = 1
  val isEncryptedByteBuffer: Array[Byte] = ByteBuffer.allocate(4).putInt(IS_ENCRYPTED).array
  val encryptionVersionByteBuffer: Array[Byte] = ByteBuffer.allocate(4).putInt(ENCRYPTION_VERSION).array
}

