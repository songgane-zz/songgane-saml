package songgane.saml.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.namespace.QName;

import org.opensaml.common.SAMLObject;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SamlUtil {
	protected static Logger logger = LoggerFactory.getLogger(SamlUtil.class);

	public static XMLObject buildXMLObject(QName objectQName) {
		XMLObjectBuilder builder = Configuration.getBuilderFactory().getBuilder(objectQName);
		if (builder == null) {
			logger.error("Unable to retrieve builder for object QName " + objectQName);
		}

		return builder.buildObject(objectQName.getNamespaceURI(), objectQName.getLocalPart(), objectQName.getPrefix());
	}

	public static void printSamlToPrettyString(XMLObject object) throws MarshallingException {
		MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
		Marshaller marshaller = marshallerFactory.getMarshaller(object);
		org.w3c.dom.Element subjectElement = marshaller.marshall(object);

		logger.info(XMLHelper.prettyPrintXML(subjectElement));
	}

	public static String convertSamlToString(XMLObject object) throws MarshallingException {
		MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
		Marshaller marshaller = marshallerFactory.getMarshaller(object);
		org.w3c.dom.Element subjectElement = marshaller.marshall(object);

		return XMLHelper.nodeToString(subjectElement);
	}

	public static String encodeBase64(String str) {
		return Base64.encodeBytes(str.getBytes(), Base64.DONT_BREAK_LINES);
	}

	public static String convertSamlToBase64Str(XMLObject object) throws MarshallingException {
		return encodeBase64(convertSamlToString(object));
	}

	public static String decodeBase64(String str) {
		byte[] decodedBytes = Base64.decode(str);
		return new String(decodedBytes);
	}

	public static SAMLObject convertStringToSaml(String str)
			throws UnsupportedEncodingException, XMLParserException, UnmarshallingException {
		InputStream is = new ByteArrayInputStream(str.getBytes("UTF-8"));

		ParserPool parser = Configuration.getParserPool();
		Document doc = parser.parse(is);
		Element samlElement = doc.getDocumentElement();
		UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
		Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(samlElement);
		return (SAMLObject) unmarshaller.unmarshall(samlElement);
	}

	public static SAMLObject convertBase64StrToSaml(String str)
			throws MarshallingException, UnsupportedEncodingException, XMLParserException, UnmarshallingException {
		return convertStringToSaml(decodeBase64(str));
	}
}
