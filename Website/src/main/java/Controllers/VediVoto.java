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

import BEANS.Studente;
import BEANS.Valutazione;
import DAO.StudenteDAO;


@WebServlet("/VediVoto")
public class VediVoto extends HttpServlet {
	
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
		
		HttpSession session = request.getSession();
		Studente studente = (Studente) session.getAttribute("user");
		StudenteDAO studenteDAO = new StudenteDAO(connection, studente.getID());
		Integer corsoID = null;
		Date dataAppello = null;
		Valutazione voto = null;
		Studente studInfo = null;
		try {
			corsoID = Integer.parseInt(request.getParameter("id"));
		}
		catch(NumberFormatException e) {
			corsoID = null;
		}
		try {
			dataAppello = Date.valueOf(request.getParameter("data"));
		}
		catch(IllegalArgumentException e) {
			dataAppello = null;
		}
		
		if (corsoID == null || dataAppello == null) {
			renderPageError(request, response,
					"Corso o data appello non validi.");
			return;
		}
		
		try {
			if (!studenteDAO.checkRegistrazione(corsoID, dataAppello)) {
				renderPageError(request, response, "Lo studente non è iscritto all'appello.");
				return;
			}
		} 
		catch (SQLException e) {
			renderPageError(request, response,
					"Si è verificato un errore nel controllare l'iscrizione dello studente all'appello.");
			return;
		}
		
		try {
			voto = studenteDAO.getVotoByAppello(corsoID, dataAppello);
			if(voto == null) {
				renderPageError(request, response, "Nessuna valutazione trovata per questo appello.");
				return;
			}
			
			studInfo = studenteDAO.getStudenteInfo();
			if(studInfo == null) {
				renderPageError(request, response, "Nessuna informazione trovata per lo studente.");
				return;
			}
		} 
		catch (SQLException e) {
			renderPageError(request, response,
					"Si è verificato un errore nel trovare le informazioni relative alla valutazione.");
			return;
		} 
		
		String path = "/WEB-INF/Valutazione.html";
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
        ctx.setVariable("voto", voto);
        ctx.setVariable("studInfo", studInfo);
		templateEngine.process(path, ctx, response.getWriter());
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	
	private void renderPageError(HttpServletRequest request, HttpServletResponse response, 
			String errorMessage) throws IOException {
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		ctx.setVariable("error", errorMessage);
		templateEngine.process("/WEB-INF/Valutazione.html", ctx, response.getWriter());
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
