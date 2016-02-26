package songgane.saml2.metadata;

import org.junit.Test;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.xml.io.MarshallingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import songgane.saml.test.BaseTestCase;
import songgane.saml.util.SamlUtil;

public class MetadataGeneratorTest extends BaseTestCase {
	private static Logger logger = LoggerFactory.getLogger(MetadataGeneratorTest.class);
	private static final String signingKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCn1YvXLR5eP/rGq6o8BRF4T7Jssuyjaa+41ESmCThGB98gSPpA8fGMLkfZilZSpVvJsbog95QsFcZ7Agt6f3iiEkStLxYDeKqvpg3lXOp7XHHWPn1ps49xruFZk4yDfrdLuEbtQzafMJyJJOkBOSPklly2MhpKsYz3HkIWytPQfwIDAQAB";

	@Test
	public void testGenerateIdpMetadata() {
		EntityDescriptor entityDescriptor = new MetadataGenerator().generateIDPMetadata("sample",
				"http://localhost:8080/saml/idp", SAMLConstants.SAML2_POST_BINDING_URI, signingKey);

		try {
			SamlUtil.printSamlToPrettyString(entityDescriptor);
		} catch (MarshallingException e) {
			logger.error(e.getMessage(), e);
		}
		logger.debug("test");
	}
}
