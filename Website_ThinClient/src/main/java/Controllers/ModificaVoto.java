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
	    if (docente == null || !docente.getRole().equals("DOCENTE")) {
			response.sendRedirect(request.getContextPath() + "/Login");
			return;
		}
	    DocenteDAO docenteDAO = new DocenteDAO(connection, docente.getID());

	    Integer studID = null;
	    Integer corsoID = null;
	    Date dataAppello = null;
	    try {
	        studID = Integer.parseInt(request.getParameter("studenteID"));
	        corsoID = Integer.parseInt(request.getParameter("corsoID"));
	        dataAppello = Date.valueOf(request.getParameter("dataAppello"));
	    } 
	    catch (Exception e) {
	        renderPageError(request, response, "Parametri non validi.");
	        return;
	    }
	    
	    Valutazione valutazione = new Valutazione();
	    try {
	        String votoParam = request.getParameter("voto");
	        valutazione.setVoto(votoParam);
	    } 
	    catch (IllegalArgumentException e) {
	        renderPageError(request, response, "Voto non valido.");
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
	        renderPageError(request, response, "Errore durante il controllo dell'iscrizione.");
	        return;
	    }

	    try {
	    	if (!docenteDAO.isAutorizzato(corsoID)) {
                renderPageError(request, response, "Non sei autorizzato a modificare questo corso.");
                return;
            }
	        docenteDAO.modificaVoto(valutazione, corsoID, dataAppello, studID);
	    } 
	    catch (SQLException e) {
	        renderPageError(request, response, "Errore durante la modifica del voto.");
	        return;
	    }
	    String ctxpath = getServletContext().getContextPath();
	    String path = ctxpath + "/VediIscritti?corsoID=" + corsoID + "&dataAppello=" + dataAppello + 
	    						"&sortBy=matricola&order=ASC";
	    response.sendRedirect(path);
	    
	}
	
	private void renderPageError(HttpServletRequest request, HttpServletResponse response, 
			String errorMessage) throws IOException {
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
		WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
		ctx.setVariable("error", errorMessage);
		templateEngine.process("/WEB-INF/ModificaVoto.html", ctx, response.getWriter());
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
