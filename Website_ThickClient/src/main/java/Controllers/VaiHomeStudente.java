package Controllers;

import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import BEANS.Appello;
import BEANS.Corso;
import BEANS.Studente;
import DAO.StudenteDAO;

@WebServlet("/VaiHomeStudente")
public class VaiHomeStudente extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public void init() throws ServletException {
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);
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
		
		HttpSession session = request.getSession();
		Studente studente = (Studente) session.getAttribute("user");
		StudenteDAO studenteDAO = new StudenteDAO(connection, studente.getID());
		ArrayList<Corso> corsi = new ArrayList<>();
		ArrayList<ArrayList<Appello>> appelli = new ArrayList<>();
		try {
			studenteDAO.getCorsiAndAppelliByStudente(corsi, appelli);
		} 
		catch (SQLException e) {
			renderPageError(request, response, "Si è verificato un errore durante il recupero dei corsi ed appelli.");
			return;
		}
		if (corsi.isEmpty() ) {
			renderPageError(request, response, "Nessun corso trovato per lo studente.");
			return;
		}
		String path = "/WEB-INF/StudenteHome.html";
		JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
        ctx.setVariable("corsi", corsi);
        ctx.setVariable("appelli", appelli);
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
		templateEngine.process("/WEB-INF/StudenteHome.html", ctx, response.getWriter());
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