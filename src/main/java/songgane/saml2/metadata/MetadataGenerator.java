package songgane.saml2.metadata;

import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.X509Certificate;
import org.opensaml.xml.signature.X509Data;

import songgane.saml.util.SamlUtil;

public class MetadataGenerator {

	public EntityDescriptor generateIDPMetadata(String entityId, String location, String binding, String signingKey) {
		EntityDescriptor descriptor = (EntityDescriptor) SamlUtil.buildXMLObject(EntityDescriptor.DEFAULT_ELEMENT_NAME);
		descriptor.setEntityID(entityId);

		IDPSSODescriptor idpSsoDescriptor = (IDPSSODescriptor) SamlUtil
				.buildXMLObject(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);

		// http://stackoverflow.com/questions/954337/why-use-an-x-509-certificate-to-encrypt-xml-why-not-just-transmit-over-https
		KeyDescriptor keyDescriptor = (KeyDescriptor) SamlUtil.buildXMLObject(KeyDescriptor.DEFAULT_ELEMENT_NAME);
		keyDescriptor.setUse(UsageType.ENCRYPTION);

		KeyInfo keyInfo = (KeyInfo) SamlUtil.buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);

		X509Certificate cert = (X509Certificate) SamlUtil.buildXMLObject(X509Certificate.DEFAULT_ELEMENT_NAME);
		cert.setValue(signingKey);

		X509Data xmlX509Data = (X509Data) SamlUtil.buildXMLObject(X509Data.DEFAULT_ELEMENT_NAME);
		xmlX509Data.getX509Certificates().add(cert);

		keyInfo.getX509Datas().add(xmlX509Data);

		keyDescriptor.setKeyInfo(keyInfo);
		idpSsoDescriptor.getKeyDescriptors().add(keyDescriptor);

		SingleLogoutService singleLogoutService = (SingleLogoutService) SamlUtil
				.buildXMLObject(SingleLogoutService.DEFAULT_ELEMENT_NAME);
		singleLogoutService.setBinding(binding);
		singleLogoutService.setLocation(location);
		idpSsoDescriptor.getSingleLogoutServices().add(singleLogoutService);
		// md:NameIDFormat

		descriptor.getRoleDescriptors().add(idpSsoDescriptor);

		return descriptor;
	}
}
