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

/**
 * Servlet implementation class VediVoto
 */
@WebServlet("/VediVoto")
public class VediVoto extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VediVoto() {
        super();
        // TODO Auto-generated constructor stub
    }
    
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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
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
		catch(IllegalArgumentException e) {
			corsoID = null;
		}
		try {
			dataAppello = Date.valueOf(request.getParameter("data"));
		}
		catch(IllegalArgumentException e) {
			dataAppello = null;
		}
		
		if (corsoID != null && dataAppello != null) {
			try {
				if (!studenteDAO.checkRegistrazione(corsoID, dataAppello)) {
					renderPageError(request, response, "Lo studente non è iscritto all'appello.");
					return;
				}
			} catch (SQLException e) {
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
				
			} catch (SQLException e) {
				renderPageError(request, response,
						"Si è verificato un errore nel trovare le informazioni relative alla valutazione.");
				return;
			} 
		} else {
			renderPageError(request, response,
					"Corso o data appello non validi.");
			return;
		}
		
		String path = "/WEB-INF/Valutazione.html";
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
        ctx.setVariable("voto", voto);
        ctx.setVariable("studInfo", studInfo);
		templateEngine.process(path, ctx, response.getWriter());
	}
	
	private void renderPageError(HttpServletRequest request, HttpServletResponse response, 
			String errorMessage) throws IOException {
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		ctx.setVariable("error", errorMessage);
		templateEngine.process("/WEB-INF/StudenteHome.html", ctx, response.getWriter());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
