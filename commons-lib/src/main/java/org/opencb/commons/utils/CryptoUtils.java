package org.opencb.commons.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class CryptoUtils {

    private CryptoUtils() {
    }

    public static byte[] encryptAES(String strToEncrypt, byte[] key)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.encodeBase64(cipher.doFinal(strToEncrypt.getBytes()));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(CryptoUtils.class.getName()).log(Level.SEVERE, "This should not happen", ex);
            throw ex;
        }
    }

    public static String decryptAES(String strToDecrypt, byte[] key)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(Base64.decodeBase64(strToDecrypt));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            Logger.getLogger(CryptoUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    public static byte[] encryptSha1(String strToEncrypt) {
        return DigestUtils.sha1(strToEncrypt);
    }
    
}
