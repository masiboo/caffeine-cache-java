package nl.ing.api.contacting.conf.business.kafka;

import java.nio.ByteBuffer;

public final class EncryptionAwareSerDeJava {
    public static final String ENCRYPTED_HEADER_KEY = "encrypted";
    public static final String ENCRYPTED_VERSION_HEADER_KEY = "encryption-version";
    public static final int ENCRYPTION_VERSION = 1;
    public static final int IS_ENCRYPTED = 1;
    public static final byte[] IS_ENCRYPTED_BYTE_BUFFER = ByteBuffer.allocate(4).putInt(IS_ENCRYPTED).array();
    public static final byte[] ENCRYPTION_VERSION_BYTE_BUFFER = ByteBuffer.allocate(4).putInt(ENCRYPTION_VERSION).array();

    private EncryptionAwareSerDeJava() {}
}
