package Controllers;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

@WebServlet("/Login")
public class Login extends HttpServlet {
    
	private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;

    public void init() throws ServletException {
    	
        ServletContext context = getServletContext();
        JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(context);
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setSuffix(".html");
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        
    }

    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
    	String path = "/WEB-INF/Login.html";
    	JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		String error = request.getParameter("error");
        if (error != null && !error.isEmpty()) {
            ctx.setVariable("error", error);
        }
		templateEngine.process(path, ctx, response.getWriter());
    }

    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    }
}
