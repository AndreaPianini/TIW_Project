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
import BEANS.Studente;
import BEANS.Valutazione;
import DAO.DocenteDAO;
import DAO.StudenteDAO;


@WebServlet("/Modifica")
public class Modifica extends HttpServlet {
	
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
    	Docente docente = (Docente) session.getAttribute("user");
		if (docente == null || !docente.getRole().equals("DOCENTE")) {
			response.sendRedirect(request.getContextPath() + "/Login");
			return;
		}
		
    	Integer studenteID = null, corsoID = null;
    	Date dataAppello = null;
    	
    	try {
			studenteID = Integer.parseInt(request.getParameter("studenteID"));
		}
		catch(NumberFormatException e) {
			studenteID = null;
		}
		try {
			corsoID = Integer.parseInt(request.getParameter("corsoID"));
		}
		catch(Exception e) {
			corsoID = null;
		}
		try {
			dataAppello = Date.valueOf(request.getParameter("dataAppello"));
		}
		catch(Exception e) {
			dataAppello = null;
		}
		
		if (studenteID == null || corsoID == null || dataAppello == null) {
			renderPageError(request, response, "Parametri non validi.");
			return;
		}
		
		DocenteDAO docenteDAO = new DocenteDAO(connection, docente.getID());
	   	StudenteDAO studenteDAO = new StudenteDAO(connection, studenteID);
	   	Studente studente = null;
	   	Valutazione valutazione = null;
		try {
			if (!docenteDAO.isAutorizzato((int)corsoID)) {
				renderPageError(request, response, "Non sei abilitato a modificare questo corso.");
				return;
			}
			studente = studenteDAO.getStudenteInfo();
			valutazione = docenteDAO.getValutazioneByStudenteAppello(studenteID, corsoID, dataAppello);
		} 
		catch (SQLException e) {
			renderPageError(request, response, "Si è verificato un errore. Riprovare");
			return;
		}
		
		if (studente == null || valutazione == null) {
			renderPageError(request, response, "Si è verificato un errore. Riprovare");
			return;
		}
		
		String path = "/WEB-INF/ModificaVoto.html";
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
        ctx.setVariable("studente", studente);
        ctx.setVariable("valutazione", valutazione);
        ctx.setVariable("corsoID", corsoID);
        ctx.setVariable("dataAppello", dataAppello);
        ctx.setVariable("votiPossibili", BEANS.Valutazione.Voto.values());
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
		templateEngine.process("/WEB-INF/Iscritti.html", ctx, response.getWriter());
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
