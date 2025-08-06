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

import BEANS.Studente;
import DAO.StudenteDAO;


@WebServlet("/RifiutaVoto")
public class RifiutaVoto extends HttpServlet {
	
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
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		Studente studente = (Studente) session.getAttribute("user");
		
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
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        response.getWriter().println("Corso o data dell'appello non validi. Riprovare.");
	        return;
		}
		
		StudenteDAO studenteDAO = new StudenteDAO(connection, studente.getID());
		try {
			studenteDAO.rifiutaVoto(corsoID, dataAppello);
		} 
		catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        response.getWriter().println("Si è verificato un errore durante il rifiuto del voto. Riprovare.");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("Voto rifiutato con successo.");
		
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
