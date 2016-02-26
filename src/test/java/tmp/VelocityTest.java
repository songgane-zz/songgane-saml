package tmp;

import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

public class VelocityTest {

	public static void main(String[] args) {
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8");
		ve.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8");
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

		ve.init();

		Template t = ve.getTemplate("/templates/saml1-post-binding.vm");

		VelocityContext context = new VelocityContext();

		context.put("name", "World");

		StringWriter writer = new StringWriter();

		t.merge(context, writer);

		System.out.println(writer.toString());
	}

}
