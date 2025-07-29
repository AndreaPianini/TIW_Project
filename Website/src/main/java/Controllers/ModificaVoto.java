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
import BEANS.Valutazione;
import DAO.DocenteDAO;
import DAO.StudenteDAO;


@WebServlet("/ModificaVoto")
public class ModificaVoto extends HttpServlet {
	
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
		DocenteDAO docenteDAO = new DocenteDAO(connection, docente.getID());
		Integer studID = null;
		Integer corsoID = null;
		Date dataAppello = null;
		Valutazione valutazione = new Valutazione();
		
		try {
			studID = Integer.parseInt(request.getParameter("studenteID"));
		}
		catch(NumberFormatException e) {
			studID = null;
		}
		try {
			corsoID = Integer.parseInt(request.getParameter("corsoID"));
		}
		catch(NumberFormatException e) {
			corsoID = null;
		}
		try {
			dataAppello = Date.valueOf(request.getParameter("dataAppello"));
		}
		catch(Exception e) {
			dataAppello = null;
		}
		try {
			valutazione.setVoto(request.getParameter("voto"));
		}
		catch(IllegalArgumentException e) {
			valutazione = null;
		}
		
		if(studID == null || corsoID == null || dataAppello == null || valutazione == null) {
			renderPageError(request, response, "Parametri inseriti errati. Riprovare.");
			return;
		}
		
		StudenteDAO studenteDao = new StudenteDAO(connection, studID);
		try {
			if (!studenteDao.checkRegistrazione(corsoID, dataAppello)) {
				renderPageError(request, response, "Studente non iscritto all'appello.");
				return;
			}	
		} 
		catch (SQLException e) {
			renderPageError(request, response, "Si è verificato un errore. Riprovare.");
			return;
		}
		try {
			docenteDAO.modificaVoto(valutazione, corsoID, dataAppello, studID);
		} 
		catch (SQLException e) {
			renderPageError(request, response, "Si è verificato un errore durante la modifica del voto.");
			return;
		}
		String ctxpath = getServletContext().getContextPath();
		String path = ctxpath + "/VediIscritti?corsoAppello=" + corsoID + "&dataAppello=" + dataAppello + "&sortBy=id&order=ASC";
		response.sendRedirect(path);
		
	}
	
	private void renderPageError(HttpServletRequest request, HttpServletResponse response, 
			String errorMessage) throws IOException {
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		ctx.setVariable("error", errorMessage);
		templateEngine.process("/WEB-INF/Modifica.html", ctx, response.getWriter());
	}

	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {
		}
	}

}
