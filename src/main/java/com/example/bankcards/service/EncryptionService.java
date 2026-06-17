package com.example.bankcards.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class EncryptionService {

    private final SecretKeySpec secretKey;
    private static final String ALGORITHM = "AES";

     public EncryptionService(@Value("${encryption.secret}") String base64Secret) {
         byte[] decodedKey = Base64.getDecoder().decode(base64Secret);
          this.secretKey = new SecretKeySpec(decodedKey, ALGORITHM);
    }

      public String encrypt(String plainText) {
        if (plainText == null) return null;
        try {
             Cipher cipher = Cipher.getInstance(ALGORITHM);
             cipher.init(Cipher.ENCRYPT_MODE, secretKey);
              byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
             return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка шифрования данных", e);
        }
    }

       public String decrypt(String encryptedText) {
        if (encryptedText == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
              cipher.init(Cipher.DECRYPT_MODE, secretKey);
             byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
              byte[] decryptedBytes = cipher.doFinal(decodedBytes);
             return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка расшифровки данных", e);
        }
    }
}