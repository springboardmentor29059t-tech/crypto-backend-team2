package com.internship.crypto_tracker.util;

import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtils {

    @Value("${app.encryption.key}")
    private String secretKeyString;

    public String encrypt(String data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(secretKeyString.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(secretKeyString.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
}