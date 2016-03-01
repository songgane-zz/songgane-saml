package songgane.saml2.sp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLException;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.RandomIdentifierGenerator;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import songgane.saml.util.SamlUtil;
import songgane.saml2.metadata.IdpMetadata;
import songgane.saml2.metadata.SpMetadata;

public class AuthnRequestController extends HttpServlet {
	public AuthnRequestController() {
		try {
			DefaultBootstrap.bootstrap();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	// http://stackoverflow.com/questions/26365664/how-do-i-correctly-digitally-sign-a-saml2-0-authnrequest
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String issuerId = "http://localhost:8070/songgane-saml2/sp";
		String singleSingleOnServiceUrl = "https://idp.ssocircle.com:443/sso/SSOPOST/metaAlias/ssocircle";
		String assertionConsumerServiceUrl = "http://localhost:8070/songgane-saml2/sp/AssertionConsumerService";

		AuthnRequest ar = createAuthNRequest(issuerId, singleSingleOnServiceUrl, assertionConsumerServiceUrl);

		String samlRequest = null;
		try {
			SamlUtil.printSamlToPrettyString(ar);
			samlRequest = SamlUtil.convertSamlToBase64Str(ar);
		} catch (Exception e) {
		}

		String html = "<form id=\"samlSpForm\" action=\"" + singleSingleOnServiceUrl + "\" method=\"post\">"
				+ "<input type=\"hidden\" name=\"SAMLRequest\" value=\"" + samlRequest
				+ "\"/><input type=\"submit\" value=\"\"/ style=\"display:hidden\">" + "</form>"
				+ "<script> document.getElementById('samlSpForm').submit(); </script>";

		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.print(html);
		out.close();
	}

	@SuppressWarnings("rawtypes")
	private AuthnRequest createAuthNRequest(String issuerId, String destination, String responseURL) {
		// Create BuilderFactory
		XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();

		// Create AuthnRequest
		SAMLObjectBuilder builder = (SAMLObjectBuilder) builderFactory.getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
		AuthnRequest authnRequest = (AuthnRequest) builder.buildObject();
		authnRequest.setVersion(SAMLVersion.VERSION_20);
		authnRequest.setIssueInstant(new DateTime());
		authnRequest.setID(new RandomIdentifierGenerator().generateIdentifier());

		// Set Issuer
		builder = (SAMLObjectBuilder) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
		Issuer issuer = (Issuer) builder.buildObject();
		issuer.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
		// issuer.setValue(issuerId);
		issuer.setValue("songgane-saml2");
		authnRequest.setIssuer(issuer);

		// Set destination
		authnRequest.setDestination(destination);

		// Set response URL
		authnRequest.setAssertionConsumerServiceURL(responseURL);

		return authnRequest;
	}

	@SuppressWarnings("unchecked")
	private String createAuthnRequest(String requestId) throws SAMLException {
		SpMetadata spConfig = null;
		IdpMetadata idpConfig = null;

		XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();

		SAMLObjectBuilder<AuthnRequest> builder = (SAMLObjectBuilder<AuthnRequest>) builderFactory
				.getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);

		SAMLObjectBuilder<Issuer> issuerBuilder = (SAMLObjectBuilder<Issuer>) builderFactory
				.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);

		AuthnRequest request = builder.buildObject();
		request.setAssertionConsumerServiceURL(spConfig.getAcs().toString());
		request.setDestination(idpConfig.getLoginUrl().toString());
		request.setIssueInstant(new DateTime());
		request.setID(requestId);

		Issuer issuer = issuerBuilder.buildObject();
		issuer.setValue(spConfig.getEntityId());
		request.setIssuer(issuer);

		try {
			// samlobject to xml dom object
			Element elem = Configuration.getMarshallerFactory().getMarshaller(request).marshall(request);

			// and to a string...
			Document document = elem.getOwnerDocument();
			DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
			LSSerializer serializer = domImplLS.createLSSerializer();
			serializer.getDomConfig().setParameter("xml-declaration", false);
			return serializer.writeToString(elem);
		} catch (MarshallingException e) {
			throw new SAMLException(e);
		}
	}
}
