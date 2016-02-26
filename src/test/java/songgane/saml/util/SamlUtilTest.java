package songgane.saml.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opensaml.xml.XMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import songgane.saml.test.BaseTestCase;
import songgane.saml.util.SamlUtil;

public class SamlUtilTest extends BaseTestCase {
	private static Logger logger = LoggerFactory.getLogger(SamlUtilTest.class);
	private static final String sample = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" AssertionConsumerServiceURL=\"http://soaer.com:8080\" ForceAuthn=\"false\" ID=\"A71AB3E13\" IsPassive=\"false\" IssueInstant=\"2016-01-08T11:14:32.466Z\" ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Version=\"2.0\"><samlp:Issuer xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:assertion\">http://localhost:9080/ServiceProvider/</samlp:Issuer><saml2p:NameIDPolicy xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\" AllowCreate=\"true\" SPNameQualifier=\"http://localhost:9080/ServiceProvider/\" /><saml2p:RequestedAuthnContext xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\" Comparison=\"exact\"><saml:AuthnContextClassRef xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport</saml:AuthnContextClassRef></saml2p:RequestedAuthnContext></samlp:AuthnRequest>";

	@Test
	public void testConvertStringToSaml() {
		logger.debug("original : " + sample);

		try {
			XMLObject obj = SamlUtil.convertStringToSaml(sample);
			SamlUtil.printSamlToPrettyString(obj);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Test
	public void testEncodenDecodeBase64Str() {
		logger.debug("original : " + sample);

		String encode = SamlUtil.encodeBase64(sample);
		logger.debug("encode : " + encode);

		String decode = SamlUtil.decodeBase64(encode);
		logger.debug("decode : " + decode);

		assertEquals(encode, encode);
	}
}
