package Controllers;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import BEANS.Utente;
import DAO.UtenteDAO;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;


@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public void init() throws ServletException {
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);

			// Thymeleaf setup
			JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(context);
			WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);
			templateResolver.setTemplateMode(TemplateMode.HTML);
			templateResolver.setSuffix(".html");
			this.templateEngine = new TemplateEngine();
			this.templateEngine.setTemplateResolver(templateResolver);
		} 
		catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} 
		catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
	}

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Integer ID = null;
		try {
			ID = Integer.parseInt(request.getParameter("id"));
		}
		catch(NumberFormatException e) {
			ID = null;
		}
        String password = request.getParameter("password");
        
        if ( ID == null || ID < 0 || ID > 99999999 || password == null || password.isEmpty() ) {
        	renderPageError(request, response, "ID o Password non validi. Riprovare");
			return;
		}
        
        UtenteDAO dao = new UtenteDAO(this.connection);
        Utente user = null;
        try {
        	user = dao.checkCredenziali(ID, password);
        }
        catch (SQLException e) {
        	System.out.println("Failure in database credential checking");
        	renderPageError(request, response, "Si è verificato un errore, riprovare");
			return;
		}

        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            String path = getServletContext().getContextPath();

            if (user.getRole().equals("Studente")) {
                response.sendRedirect(path + "/VaiHomeStudente");
            } 
            else if (user.getRole().equals("Docente")) {
                response.sendRedirect(path + "/VaiHomeDocente");
            } 
            else {
                renderPageError(request, response, "Utente senza ruolo definito");
                return;
            }
        } 
        else {
        	renderPageError(request, response, "ID o Password errati. Riprovare");
        }
        
	}
	
	
	private void renderPageError(HttpServletRequest request, HttpServletResponse response, 
			String errorMessage) throws IOException {
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		ctx.setVariable("error", errorMessage);
		templateEngine.process("/WEB-INF/Login.html", ctx, response.getWriter());
	}
	
	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} 
		catch (SQLException sqle) {
		}
	}

}