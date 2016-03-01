package songgane.saml2.sp;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SPController extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String html = "";

        html += "<h1>Welcome to songgane world!! (Service Provider)</h1>";
        html += "<p>Click <a href=\"/songgane-saml2/sp/AuthnRequest\">here</a> to authenticate about you.</p>";

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(html);
        out.close();
    }
}
