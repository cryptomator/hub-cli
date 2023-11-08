package org.cryptomator.hubcli;

import com.google.common.io.BaseEncoding;
import org.cryptomator.cryptolib.common.MessageDigestSupplier;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;

class KeyHelper {

	private KeyHelper(){}

	public static ECPublicKey readX509EncodedEcPublicKey(byte[] bytes) throws GeneralSecurityException {
		var factory = KeyFactory.getInstance("EC");
		var key = factory.generatePublic(new X509EncodedKeySpec(bytes));
		if (key instanceof ECPublicKey ecPublicKey) {
			return ecPublicKey;
		} else {
			throw new InvalidKeyException();
		}
	}

	public static String getKeyId(ECPublicKey key) {
		try (var digest = MessageDigestSupplier.SHA256.instance()) {
			var d = digest.get().digest(key.getEncoded());
			return BaseEncoding.base16().upperCase().encode(d);
		}
	}
}
