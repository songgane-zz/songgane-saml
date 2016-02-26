package songgane.saml.test;

import javax.xml.namespace.QName;

import static org.junit.Assert.*;
import org.junit.Before;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BaseTestCase {
	private static Logger logger = LoggerFactory.getLogger(BaseTestCase.class);

	/** Parser manager used to parse XML. */
	protected static BasicParserPool parser;

	/** XMLObject marshaller factory. */
	protected static MarshallerFactory marshallerFactory;

	/** XMLObject unmarshaller factory. */
	protected static UnmarshallerFactory unmarshallerFactory;

	@Before
	public void before() {
		try {
			DefaultBootstrap.bootstrap();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		parser = new BasicParserPool();
		marshallerFactory = Configuration.getMarshallerFactory();
		unmarshallerFactory = Configuration.getUnmarshallerFactory();
	}

	public XMLObject buildXMLObject(QName objectQName) {
		XMLObjectBuilder builder = Configuration.getBuilderFactory().getBuilder(objectQName);
		if (builder == null) {
			fail("Unable to retrieve builder for object QName " + objectQName);
		}
		return builder.buildObject(objectQName.getNamespaceURI(), objectQName.getLocalPart(), objectQName.getPrefix());
	}

	protected XMLObject unmarshallElement(String elementFile) {
		try {
			Document doc = parser.parse(BaseTestCase.class.getResourceAsStream(elementFile));
			Element samlElement = doc.getDocumentElement();

			Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(samlElement);
			if (unmarshaller == null) {
				fail("Unable to retrieve unmarshaller by DOM Element");
			}

			return unmarshaller.unmarshall(samlElement);
		} catch (XMLParserException e) {
			fail("Unable to parse element file " + elementFile);
		} catch (UnmarshallingException e) {
			fail("Unmarshalling failed when parsing element file " + elementFile + ": " + e);
		}

		return null;
	}
}
