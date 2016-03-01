package tmp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Test;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.x509.BasicX509Credential;

public class CredentialTest {

	@Test
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

			FileOutputStream fos = new FileOutputStream("/apps/src/workspace/keystore/rsa_private_keys.pkcs8");
			fos.write(Base64.encode(new PKCS8EncodedKeySpec(baPrivateKey).getEncoded()));
			fos.close();

			// Write Public Key (X.509)
			fos = new FileOutputStream("/apps/src/workspace/keystore/rsa_public_keys.x509");
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
		// byte[] pubkey = new
		// FileInputStream("/apps/src/workspace/keystore/rsa_public_keys.x509");

		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pubkey);
		KeyFactory factory = null;
		PublicKey publicKey = null;

		try {
			factory = KeyFactory.getInstance("RSA", "BC");
			publicKey = factory.generatePublic(publicKeySpec);
			// publicKey = factory.generatePrivate(publicKeySpec);
			RSAPublicKey idpRSAPubKey = SecurityHelper.buildJavaRSAPublicKey(key);
			System.out.println(idpRSAPubKey.getAlgorithm());
			System.out.println(idpRSAPubKey.getFormat());

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(publicKey);

		// SecurityHelper.getSimpleCredential(publicKey, privateKey)
	}

	public Credential getSigningCredential() throws Throwable {
		// create public key (cert) portion of credential
		InputStream inStream = new FileInputStream("/apps/src/workspace/keystore/rsa_public_keys.x509");
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate publicKey = (X509Certificate) cf.generateCertificate(inStream);
		inStream.close();

		// create private key
		RandomAccessFile raf = new RandomAccessFile("/apps/src/workspace/keystore/rsa_private_keys.pkcs8", "r");
		byte[] buf = new byte[(int) raf.length()];
		raf.readFully(buf);
		raf.close();

		PKCS8EncodedKeySpec kspec = new PKCS8EncodedKeySpec(buf);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = kf.generatePrivate(kspec);

		// create credential and initialize
		BasicX509Credential credential = new BasicX509Credential();
		credential.setEntityCertificate(publicKey);
		credential.setPrivateKey(privateKey);

		return credential;
	}

	public static void main(String args[]) {
		String signingKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCn1YvXLR5eP/rGq6o8BRF4T7Jssuyjaa+41ESmCThGB98gSPpA8fGMLkfZilZSpVvJsbog95QsFcZ7Agt6f3iiEkStLxYDeKqvpg3lXOp7XHHWPn1ps49xruFZk4yDfrdLuEbtQzafMJyJJOkBOSPklly2MhpKsYz3HkIWytPQfwIDAQAB";
		String idpRSAPubKeyBase64 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDfCVgF2Lvhu0Q35FvmAVGMXc3i"
				+ "1MojcqybcfVbfn0Tg/Aj5FvuAiDFg9KpGvMHDKdLOY+1xsKZqyIm58SFhW+5z51Y"
				+ "pnblHGjuDtPtPbtspQ7pAOsknnvbKZrx7RGNOJyQZE3Qn88Y5ZBNzABusqNXjrWl" + "U9m4a+XNIFqM4YbJLwIDAQAB";
		new CredentialTest().decode(idpRSAPubKeyBase64);
	}

}