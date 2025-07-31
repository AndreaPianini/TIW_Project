package Controllers;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import BEANS.Appello;
import BEANS.Corso;
import BEANS.Studente;
import BEANS.Valutazione;
import BEANS.Verbale;
import DAO.AppelloDAO;
import DAO.CorsoDAO;
import DAO.VerbaleDAO;


@WebServlet("/MostraVerbaleCreato")
public class MostraVerbaleCreato extends HttpServlet {
	
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

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		VerbaleDAO verbaleDAO = new VerbaleDAO(connection);
		Verbale verbale = new Verbale();
		AppelloDAO appelloDAO = new AppelloDAO(connection);
		Appello appello = null;
		ArrayList<Studente> studenti = new ArrayList<>();
		ArrayList<Valutazione> valutazioni = new ArrayList<>();
		
		Integer verbaleID = null;
		try {
			verbaleID = Integer.parseInt(request.getParameter("verbaleID"));
		} 
		catch (Exception e) {
			verbaleID = null;
		}
		if( verbaleID == null || verbaleID < 0 ) {
			renderPageError(request, response, "Verbale non valido. Riprovare.");
			return;
		}
		verbale.setId(verbaleID);
		try {
			verbaleDAO.getStudentiAndInfoByVerbale(verbale, studenti, valutazioni);
		} 
		catch (SQLException e) {
			renderPageError(request, response, "Si è verificato un errore durante il recupero delle informazioni del verbale.");
			return;
		}
		if (studenti.isEmpty() || valutazioni.isEmpty()) {
			renderPageError(request, response, "Nessuno studente trovato per il verbale selezionato.");
			return;
		}
		
		try {
			appello = appelloDAO.getAppelloByVerbale(verbale.getId());
		} 
		catch (SQLException e) {
			renderPageError(request, response, "Si è verificato un errore durante il recupero dell'appello.");
			return;
		}
		if (appello == null) {
			renderPageError(request, response, "Nessun appello trovato per il verbale selezionato.");
			return;
		}
		Corso corso;
		try {
			corso = new CorsoDAO(connection).getCorsoById(appello.getCorso());
		} 
		catch (SQLException e) {
			renderPageError(request, response, "Si è verificato un errore durante il recupero del corso.");
			return;
		}
		String path = "/WEB-INF/VerbaleCreato.html";
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
        ctx.setVariable("corsoInfo", corso);
        ctx.setVariable("verbale", verbale);
        ctx.setVariable("studenti", studenti);
        ctx.setVariable("valutazioni", valutazioni);
        ctx.setVariable("appello", appello);
		templateEngine.process(path, ctx, response.getWriter());
	}
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
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
		} catch (SQLException sqle) {
		}
	}

}
