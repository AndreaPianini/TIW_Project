package Controllers;

import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import BEANS.Appello;
import BEANS.Corso;
import BEANS.Studente;
import DAO.StudenteDAO;

@WebServlet("/VaiHomeStudente")
@MultipartConfig
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
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        response.getWriter().println("Si è verificato un errore durante il recupero dei corsi ed appelli.");
	        return;
			
		}
		if (corsi.isEmpty() ) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	        response.getWriter().println("Nessun corso trovato per lo studente.");
			return;
		}
		
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.add("corsi", new Gson().toJsonTree(corsi));
		jsonResponse.add("appelli", new Gson().toJsonTree(appelli));
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(jsonResponse.toString());
		
	}
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	
	public void destroy() {
		try {
			if (connection != null) connection.close();
		} 
		catch (SQLException sqle) {}
	}
	
}