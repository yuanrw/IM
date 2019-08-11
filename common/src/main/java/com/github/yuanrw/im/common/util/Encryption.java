package com.github.yuanrw.im.common.util;

import com.github.yuanrw.im.common.exception.ImException;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Date: 2019-05-06
 * Time: 17:34
 *
 * @author yrw
 */
public class Encryption {

    public static byte[] encrypt(String key, String initVector, byte[] value) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            SecretKey secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            return Base64.encodeBase64(cipher.doFinal(value));
        } catch (Exception e) {
            throw new ImException("[Encryption] encrypt chat msg failed", e);
        }
    }

    public static byte[] decrypt(String key, String initVector, byte[] encrypted) {
        try {
            SecretKeySpec sKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, iv);
            return cipher.doFinal(Base64.decodeBase64(encrypted));
        } catch (Exception e) {
            throw new ImException("[Encryption] decrypt chat msg failed", e);
        }
    }
}
