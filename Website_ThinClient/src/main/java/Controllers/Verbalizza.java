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
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import BEANS.Docente;
import DAO.DocenteDAO;


@WebServlet("/Verbalizza")
public class Verbalizza extends HttpServlet {
	
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
		
		HttpSession session = request.getSession();
		Docente docente = (Docente) session.getAttribute("user");
		if (docente == null || !docente.getRole().equals("DOCENTE")) {
			response.sendRedirect(request.getContextPath() + "/Login");
			return;
		}
		DocenteDAO docenteDAO = new DocenteDAO(connection, docente.getID());
		
		Integer corsoID = null;
		Date dataAppello = null;
		try {
			corsoID = Integer.parseInt(request.getParameter("corsoID"));
			dataAppello = Date.valueOf(request.getParameter("dataAppello"));
		} 
		catch (Exception e) {
			corsoID = null;
			dataAppello = null;
		}
		if( corsoID == null || dataAppello == null || corsoID < 0 || corsoID > 9999) {
			renderPageError(request, response, "Corso o data dell'appello non validi. Riprovare.");
			return;
		}
		
		int verbaleID = -1;
		try {
			verbaleID = docenteDAO.verbalizzaValutazioni(corsoID, dataAppello);
		} 
		catch (SQLException e) {
			renderPageError(request, response, "Errore durante la verbalizzazione dell'appello. Riprovare.");
			return;
		}
		if (verbaleID < 0) {
			renderPageError(request, response, "Errore durante la verbalizzazione dell'appello. Riprovare.");
			return;
		}
		String path = request.getContextPath();
		response.sendRedirect(path + "/MostraVerbaleCreato?verbaleID=" + verbaleID);
		
	}
	
	
	private void renderPageError(HttpServletRequest request, HttpServletResponse response, 
			String errorMessage) throws IOException {
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		ctx.setVariable("error", errorMessage);
		templateEngine.process("/WEB-INF/DocenteHome.html", ctx, response.getWriter());
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
