package songgane.saml2.idp;

import java.io.File;

import org.junit.Test;
import org.opensaml.common.SAMLException;

import songgane.saml.test.BaseTestCase;
import songgane.saml2.metadata.IdpMetadata;

public class IdpConfigTest extends BaseTestCase {

	@Test
	public void testConfig() throws SAMLException {
		File metafile = new File("/apps/src/workspace/songgane-saml/src/main/resources/idp.ssocircle-metadata.xml");
		IdpMetadata idpConfig = new IdpMetadata(metafile);
		System.out.println(idpConfig.getEntityId());
		System.out.println(idpConfig.getLoginUrl());
		System.out.println(idpConfig.getCert());
	}
}
