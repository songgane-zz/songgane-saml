package songgane.saml2.sp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.xml.ConfigurationException;
import songgane.saml.constraints.SamlConstraints;
import songgane.saml.util.SamlUtil;

public class AssertionConsumerService extends HttpServlet {
    public AssertionConsumerService() {
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String html = "";
        String paramSamlResponse = request.getParameter(SamlConstraints.SAML_RESPONSE_PARAM);
        Response samlResponse = null;

        try {
            samlResponse = (Response) SamlUtil.convertBase64StrToSaml(paramSamlResponse);
            SamlUtil.printSamlToPrettyString(samlResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (samlResponse != null) {
            html += "<br/>samlResponse.getConsent() : " + samlResponse.getConsent();
            html += "<br/>samlResponse.getDestination() : " + samlResponse.getDestination();
            html += "<br/>samlResponse.getID() : " + samlResponse.getID();
            html += "<br/>samlResponse.getInResponseTo() : " + samlResponse.getInResponseTo();
            html += "<br/>samlResponse.getIssueInstant() : " + samlResponse.getIssueInstant();

            Status status = samlResponse.getStatus();

            if (status != null) {
                html += "<br/>status.getStatusCode() : " + status.getStatusCode().getValue();
                html += "<br/>status.getStatusMessage() : " + status.getStatusMessage().getMessage();
                html += "<br/>status.getStatusDetail() : " + status.getStatusDetail();
            }

            List<Assertion> assertions = samlResponse.getAssertions();
            for (Assertion assertion : assertions) {
                NameID nameID = assertion.getSubject().getNameID();
                String userInfo = nameID.getValue() + "@" + nameID.getNameQualifier();
                String assertionIssuedBy = samlResponse.getIssuer().getValue();

                html += "<br/>----------------------------------------";
                html += "<br/><p>Assertion issued by " + assertionIssuedBy + "</p>";
                html += "<br/><h1>" + userInfo + "</h1></p>";
                html += "<br/>----------------------------------------";
            }
        }

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(html);
        out.close();
    }
}
