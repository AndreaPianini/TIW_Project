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

/**
 * Servlet implementation class ModificaVoto
 */
@WebServlet("/ModificaVoto")
public class ModificaVoto extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ModificaVoto() {
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
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		Docente docente = (Docente) session.getAttribute("user");
		DocenteDAO docenteDAO = new DocenteDAO(connection, docente.getID());
		Integer studID = null;
		Integer corsoID = null;
		Date dataAppello = null;
		Valutazione voto = null;
		
		try {
			studID = Integer.parseInt(request.getParameter("studId"));
		}
		catch(NumberFormatException e) {
			studID = null;
		}
		try {
			corsoID = Integer.parseInt(request.getParameter("corsoId"));
		}
		catch(NumberFormatException e) {
			corsoID = null;
		}
		try {
			dataAppello = Date.valueOf(request.getParameter("data"));
		}
		catch(NumberFormatException e) {
			dataAppello = null;
		}
		try {
			String valutazione = request.getParameter("voto");
			//controlla che il voto sia valido e poi assegnalo a voto
		}
		catch(NumberFormatException e) {
			voto = null;
		}
		
		if(studID != null && corsoID != null && dataAppello != null && voto != null) {
			//controlla che il professore insegni il corso
			
			StudenteDAO studenteDao = new StudenteDAO(connection, studID);
			try {
				if (!studenteDao.checkRegistrazione(corsoID, dataAppello)) {
					renderPageError(request, response, "Studente non iscritto all'appello.");
					return;
				} 
			} catch (SQLException e) {
				renderPageError(request, response, "Si è verificato un errore durante il controllo dell'iscrizione.");
				return;
			}
			try {
				docenteDAO.modificaVoto(voto, corsoID, dataAppello, studID);
			} catch (SQLException e) {
				renderPageError(request, response, "Si è verificato un errore durante la modifica del voto.");
				return;
			}
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
