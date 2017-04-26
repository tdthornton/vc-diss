package com.appspot.vcdiss.utils.security;

import com.google.api.client.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Utility class to avoid repetition of hashing and salt generation.
 */
public class SecurityUtils {
    public static String hash(String password, String salt) {

        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 1, 256);
            SecretKey key = skf.generateSecret(spec);
            byte[] res = key.getEncoded();

            return Base64.encodeBase64String(res);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

    }

    public static String getNewSalt() {

        try {
            SecureRandom sr = null;

            sr = SecureRandom.getInstance("SHA1PRNG");


            byte[] bytes = new byte[32];
            sr.nextBytes(bytes);
            return Base64.encodeBase64String(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Auth issue");
    }
}
