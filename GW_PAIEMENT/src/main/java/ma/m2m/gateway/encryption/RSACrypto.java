package ma.m2m.gateway.encryption;

import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import org.apache.commons.codec.binary.Base64;
import lombok.extern.slf4j.Slf4j;
import ma.m2m.gateway.utils.Traces;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Slf4j
public final class RSACrypto {

	public static final String KEY_ALGORITHM = "RSA";
	
	public static final Traces traces = new Traces();

	private RSACrypto() {
	}

	public static String encryptByPublicKeyWithDataSignedInMD5(String data, String publicKeyStr, String folder, String file) throws Exception {
		String encryptedData = "";
		try {
			byte[] publicKeyBytes = decodeKeyInBase64Format(publicKeyStr);
			// Public key acquisition
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			Key publicKey = keyFactory.generatePublic(x509KeySpec);
			// Encrypt data
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			String signature = HashingHelper.hachInMD5(data, folder, file);
			data += "+signature=" + signature;
			encryptedData = new String(Base64.encodeBase64(cipher.doFinal(data.getBytes())));
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,"[ERROR-RSA-ENCRYPTION] RSA Public key Encryption with input data signed in md5 has failed");
			throw e;
		}
		return encryptedData;
	}
	
	public static String encryptByPublicKeyWithoutSignature(String data, String publicKeyStr, String folder, String file) throws Exception {
		String encryptedData = "";
		try {
			byte[] publicKeyBytes = decodeKeyInBase64Format(publicKeyStr);
			// Public key acquisition
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			Key publicKey = keyFactory.generatePublic(x509KeySpec);
			// Encrypt data
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			encryptedData = new String(Base64.encodeBase64(cipher.doFinal(data.getBytes())));
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,"[ERROR-RSA-ENCRYPTION] RSA Public key Encryption with input data signed in md5 has failed");
			throw e;
		}
		return encryptedData;
	}

	public static String encryptByPublicKeyWithMD5Sign(String data, String publicKeyStr, String plainTxtSignature, String folder, String file)
			throws Exception {
		String encryptedData = "";
		try {
			byte[] publicKeyBytes = decodeKeyInBase64Format(publicKeyStr);
			// Public key acquisition
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			Key publicKey = keyFactory.generatePublic(x509KeySpec);
			// Encrypt data
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			String signature = HashingHelper.hachInMD5(plainTxtSignature, folder, file);
			data += "+signature=" + signature;

			encryptedData = new String(Base64.encodeBase64(cipher.doFinal(data.getBytes())));
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,"[ERROR-RSA-ENCRYPTION] RSA Public key Encryption with md5 signature has failed");
			throw e;
		}
		return encryptedData;
	}

	public static byte[] decodeKeyInBase64Format(String key) {
		return Base64.decodeBase64(key.getBytes());
	}

	public static String decryptByPrivateKey(byte[] data, String privateKeyStr, String folder, String file) throws Exception {
		try {
			byte[] privateKeyByteArray = decodeKeyInBase64Format(privateKeyStr);

			PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKeyByteArray);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);

			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, privateKey);

			return new String(cipher.doFinal(data), "UTF-8");
		} catch (Exception e) {
			traces.writeInFileTransaction(folder, file,"[ERROR-RSA-DECRYPTION] RSA Decryption by private key has failed");
			throw e;
		}
	}

}
