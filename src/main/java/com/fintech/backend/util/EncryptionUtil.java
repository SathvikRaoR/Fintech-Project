package com.fintech.backend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@Slf4j
public class EncryptionUtil {

    @Value("${aes.key:#{T(java.lang.System).getenv('FINTECH_AES_KEY')}}")
    private String aesKey;

    private static final String ALGORITHM = "AES";

    public String encrypt(String plainText) {
        try {
            if (aesKey == null || aesKey.isEmpty()) {
                log.warn("AES key not configured, returning plaintext");
                return plainText;
            }

            // Pad key to 16 bytes if needed
            byte[] decodedKey = padKey(aesKey.getBytes());
            SecretKeySpec secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Encryption failed: {}", e.getMessage());
            return plainText;
        }
    }

    public String decrypt(String encryptedText) {
        try {
            if (aesKey == null || aesKey.isEmpty()) {
                log.warn("AES key not configured, returning ciphertext");
                return encryptedText;
            }

            byte[] decodedKey = padKey(aesKey.getBytes());
            SecretKeySpec secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage());
            return encryptedText;
        }
    }

    private byte[] padKey(byte[] key) {
        if (key.length >= 16) {
            return java.util.Arrays.copyOf(key, 16);
        }
        byte[] paddedKey = new byte[16];
        System.arraycopy(key, 0, paddedKey, 0, key.length);
        return paddedKey;
    }
}
