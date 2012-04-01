package us.crast.mondochest.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class StringTools {
	public static String md5String(String input) {
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(input.getBytes());
			byte messageDigest[] = algorithm.digest();
		            
			StringBuffer hexString = new StringBuffer();
			for (int i=0;i<messageDigest.length;i++) {
				String hs = Integer.toHexString(0xFF & messageDigest[i]);
				if (hs.length() == 1) hexString.append("0");
				hexString.append(hs);
			}
			return hexString.toString();
		}catch(NoSuchAlgorithmException nsae){
			return null;
		}
	}
	            
}
