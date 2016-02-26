package songgane.saml2.idp;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.joda.time.DateTime;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.StatusCodeBuilder;
import org.opensaml.saml2.core.impl.StatusMessageBuilder;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.MarshallingException;

import songgane.saml.constraints.SamlConstraints;
import songgane.saml.util.SAMLWriter;
import songgane.saml.util.SamlUtil;
import songgane.saml.util.SAMLWriter.SAMLInputContainer;

public class SingleSignOnServiceController extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final String USERID = "paco";
	private final String PASSWORD = "123";

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Obtener la sesion del cliente
		HttpSession session = request.getSession();
		String html = "";

		if (session != null && !session.isNew()) {
			// Obtener datos del formulario
			String userId = request.getParameter("userId");
			String userPwd = request.getParameter("userPwd");

			if (autenticacionCorrecta(userId, userPwd)) {
				// Obtener datos de la sesion
				// DateTime issueInstant = (DateTime)
				// session.getAttribute("issueInstant");
				String requestId = (String) session.getAttribute("requestId");
				String destination = (String) session.getAttribute("destination");
				String assertionConsumerServiceURL = (String) session.getAttribute("assertionConsumerServiceURL");
				String issuerId = (String) session.getAttribute("issuerId");
				// String nameQualifier = (String)
				// session.getAttribute("nameQualifier");

				// Construir respuesta
				SAMLInputContainer input = new SAMLInputContainer();
				input.setStrIssuer(issuerId); // service provider
				input.setStrNameID(USERID); // nombre del usuario en su
											// dominio
				input.setStrNameQualifier("localhost"); // dominio del
														// usuario
				input.setSessionId(requestId); // sesion entre usuario y
												// IDP
												// (la misma que con el
												// SP
												// nos vale)

				Assertion assertion = SAMLWriter.buildDefaultAssertion(input);

				List<Assertion> assertions = new LinkedList<Assertion>();
				assertions.add(assertion);
				Response samlResponse = createResponse(issuerId, requestId, destination, assertions);
				String stringResponse = null;
				try {
					stringResponse = SamlUtil.convertSamlToBase64Str(samlResponse);
				} catch (MarshallingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// stringResponse = stringResponse.replaceAll("\"", "'");

				String address = "http://" + assertionConsumerServiceURL;

				html += "<p style=\"color:green\">Autenticacion OK. Redirigiendo... </p>";
				html += "<form id=\"formulario\" action=\"" + address + "\" method=\"post\">"
						+ "<input type=\"hidden\" name=\"SAMLResponse\" value=\"" + stringResponse
						+ "\"/><input type=\"submit\" value=\"\"/ style=\"display:hidden\">"
						+ "</form><h2>Redirigiendo...</h2>"
						+ "<script> var formulario = document.getElementById('formulario'); formulario.submit(); </script>";
				session.setAttribute("stringResponse", stringResponse);
			} else {
				html += "<h1>Bienvenido a Identity Provider</h1><p style=\"color:red\">¡Autenticacion incorrecta!</p><p>Por favor, indentifiquese:</p>";
				html += "<form id=\"formulario\" action=\"/saml/idp/" + SamlConstraints.SAML_SINGLESIGNONSERVICE_URI
						+ "\" method=\"post\">" + "Usuario:<input type=\"text\" name=\"user\"></br>"
						+ "Contraseña:<input type=\"password\" name=\"pass\"></br>"
						+ "<input type=\"submit\" value=\"Identificar\"/></form>";
				session.invalidate();
			}
		}
	}

	private boolean autenticacionCorrecta(String user, String password) {
		return user.equals(USERID) && password.equals(PASSWORD);
	}

	@SuppressWarnings("rawtypes")
	public Response createResponse(String issuerId, String requestId, String destination, List<Assertion> assertions) {
		XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
		// Create Response
		SAMLObjectBuilder builder = (SAMLObjectBuilder) builderFactory.getBuilder(Response.DEFAULT_ELEMENT_NAME);
		Response response = (Response) builder.buildObject();
		// Set request Id
		response.setInResponseTo(requestId);
		// Set Issuer
		Issuer issuer = new IssuerBuilder().buildObject();
		issuer.setValue("idp");
		response.setIssuer(issuer);
		response.setIssueInstant(new DateTime());

		// Set status code and message
		StatusCode statusCode = new StatusCodeBuilder().buildObject();
		statusCode.setValue(StatusCode.SUCCESS_URI);
		StatusMessage statusMessage = new StatusMessageBuilder().buildObject();
		statusMessage.setMessage("OK");
		builder = (SAMLObjectBuilder) builderFactory.getBuilder(Status.DEFAULT_ELEMENT_NAME);
		Status responseStatus = (Status) builder.buildObject();
		responseStatus.setStatusCode(statusCode);
		responseStatus.setStatusMessage(statusMessage);
		response.setStatus(responseStatus);

		// Include assertions
		response.getAssertions().addAll(assertions);
		// response.getEncryptedAssertions().addAll(encryptedAssertions);
		// Set destination
		response.setDestination(destination);
		return response;
	}
}
