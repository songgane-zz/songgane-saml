package tmp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.opensaml.xml.security.SecurityHelper;

public class CredentialTest {

	public void makeRSAKey() {
		try {
			Security.addProvider(new BouncyCastleProvider());
			// generate private key, public key

			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
			kpg.initialize(1024);

			// kpg.initialize(64);
			KeyPair kp = kpg.generateKeyPair();
			byte[] baPrivateKey = kp.getPrivate().getEncoded();
			byte[] baPublicKey = kp.getPublic().getEncoded();

			FileOutputStream fos = new FileOutputStream("c:/songgane/rsa_private_keys.pkcs8");
			fos.write(Base64.encode(new PKCS8EncodedKeySpec(baPrivateKey).getEncoded()));
			fos.close();

			// Write Public Key (X.509)
			fos = new FileOutputStream("c:/songgane/rsa_public_keys.x509");
			fos.write(Base64.encode(new X509EncodedKeySpec(baPublicKey).getEncoded()));
			fos.close();
		} catch (IOException e) {
			System.err.println(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void decode(String key) {
		Security.addProvider(new BouncyCastleProvider());
		byte[] pubkey = Base64.decode(key.getBytes());

		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pubkey);
		KeyFactory factory = null;
		PublicKey publicKey = null;

		try {
			factory = KeyFactory.getInstance("RSA", "BC");
			publicKey = factory.generatePublic(publicKeySpec);
			RSAPublicKey idpRSAPubKey = SecurityHelper.buildJavaRSAPublicKey(key);
			System.out.println(idpRSAPubKey.getAlgorithm());
			System.out.println(idpRSAPubKey.getFormat());

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(publicKey);
	}

	public static void main(String args[]) {
		String signingKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCn1YvXLR5eP/rGq6o8BRF4T7Jssuyjaa+41ESmCThGB98gSPpA8fGMLkfZilZSpVvJsbog95QsFcZ7Agt6f3iiEkStLxYDeKqvpg3lXOp7XHHWPn1ps49xruFZk4yDfrdLuEbtQzafMJyJJOkBOSPklly2MhpKsYz3HkIWytPQfwIDAQAB";
		String idpRSAPubKeyBase64 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDfCVgF2Lvhu0Q35FvmAVGMXc3i"
				+ "1MojcqybcfVbfn0Tg/Aj5FvuAiDFg9KpGvMHDKdLOY+1xsKZqyIm58SFhW+5z51Y"
				+ "pnblHGjuDtPtPbtspQ7pAOsknnvbKZrx7RGNOJyQZE3Qn88Y5ZBNzABusqNXjrWl" + "U9m4a+XNIFqM4YbJLwIDAQAB";
		new CredentialTest().decode(idpRSAPubKeyBase64);
	}

}