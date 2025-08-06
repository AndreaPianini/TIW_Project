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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import BEANS.Corso;
import BEANS.Studente;
import BEANS.Valutazione;
import DAO.CorsoDAO;
import DAO.StudenteDAO;


@WebServlet("/VediVoto")
public class VediVoto extends HttpServlet {
	
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
		
		HttpSession session = request.getSession();
		Studente studente = (Studente) session.getAttribute("user");
		StudenteDAO studenteDAO = new StudenteDAO(connection, studente.getID());
		CorsoDAO corsoDAO = new CorsoDAO(connection);
		Integer corsoID = null;
		Date dataAppello = null;
		Valutazione valutazione = null;
		Studente studInfo = null;
		Corso corso = null;
		
		try {
			corsoID = Integer.parseInt(request.getParameter("corsoID"));
		}
		catch(NumberFormatException e) {
			corsoID = null;
		}
		try {
			dataAppello = Date.valueOf(request.getParameter("dataAppello"));
		}
		catch(IllegalArgumentException e) {
			dataAppello = null;
		}
		
		if (corsoID == null || dataAppello == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	        response.getWriter().println("Corso o data appello non validi.");
	        return;
	    }
		
		try {
			if (!studenteDAO.checkRegistrazione(corsoID, dataAppello)) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		        response.getWriter().println("Lo studente non è iscritto all'appello indicato.");
				return;
			}
		} 
		catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        response.getWriter().println("Si è verificato un errore nel controllare l'iscrizione dello studente all'appello.");
			return;
		}
		
		try {
			valutazione = studenteDAO.getVotoByAppello(corsoID, dataAppello);
			
			if(valutazione == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		        response.getWriter().println("Nessuna valutazione trovata per questo appello.");
				return;
			}
			
			studInfo = studenteDAO.getStudenteInfo();
			
			if(studInfo == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		        response.getWriter().println("Nessuna informazione trovata per lo studente.");
				return;
			}
			
		} 
		catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        response.getWriter().println("Si è verificato un errore nel trovare le informazioni relative alla valutazione.");
			return;
		} 
		
		try {
			corso = corsoDAO.getCorsoById(corsoID);
		} 
		catch (SQLException e){
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        response.getWriter().println("Si è verificato un errore nel trovare le informazioni relative al corso.");
			return;
		}
		
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.add("valutazione", new Gson().toJsonTree(valutazione));
		jsonResponse.add("studInfo", new Gson().toJsonTree(studInfo));
		jsonResponse.add("corso", new Gson().toJsonTree(corso));
		jsonResponse.addProperty("dataAppello", dataAppello.toString());
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(jsonResponse.toString());
		
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
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
