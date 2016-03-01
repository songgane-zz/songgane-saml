package songgane.saml2.sp;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.RandomIdentifierGenerator;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilderFactory;
import songgane.saml.util.SamlUtil;

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

        AuthnRequest ar = createAuthNRequest(issuerId,
                singleSingleOnServiceUrl,
                assertionConsumerServiceUrl);

        String samlRequest = null;
        try {
            SamlUtil.printSamlToPrettyString(ar);
            samlRequest = SamlUtil.convertSamlToBase64Str(ar);
        } catch (Exception e) {}

        String html = "<form id=\"samlSpForm\" action=\"" + singleSingleOnServiceUrl + "\" method=\"post\">"
                + "<input type=\"hidden\" name=\"SAMLRequest\" value=\"" + samlRequest
                + "\"/><input type=\"submit\" value=\"\"/ style=\"display:hidden\">"
                + "</form>"
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
        //issuer.setValue(issuerId);
        issuer.setValue("songgane-saml2");
        authnRequest.setIssuer(issuer);

        // Set destination
        authnRequest.setDestination(destination);

        // Set response URL
        authnRequest.setAssertionConsumerServiceURL(responseURL);

        return authnRequest;
    }
}
