package ma.m2m.gateway.encryption;

import java.math.BigInteger;
import java.security.MessageDigest;

import lombok.extern.slf4j.Slf4j;
import ma.m2m.gateway.utils.Traces;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Slf4j
public class HashingHelper {

	// Constructeur privé pour empêcher l'instanciation
	private HashingHelper() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static final Traces traces = new Traces();

	public static String hachInMD5(String str, String folder, String file) {
		String strMD5Hash = "";
		try {
			MessageDigest messageDigestMD5 = MessageDigest.getInstance("MD5");
			messageDigestMD5.update(str.getBytes(), 0, str.length());
			strMD5Hash = new BigInteger(1, messageDigestMD5.digest()).toString(16);
		} catch (Exception e) {
			strMD5Hash = "-1";
			traces.writeInFileTransaction(folder, file,"[ERROR-HASHING-MD5] Exception thrown during the hashing");
		}
		return strMD5Hash;
	}

}
