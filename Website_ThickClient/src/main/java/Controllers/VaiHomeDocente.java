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
import BEANS.Docente;
import DAO.DocenteDAO;

@WebServlet("/VaiHomeDocente")
@MultipartConfig
public class VaiHomeDocente extends HttpServlet {

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
		catch (ClassNotFoundException | SQLException e) {
			throw new UnavailableException("Database connection failed");
		}
	}

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

	    HttpSession session = request.getSession();
	    Docente docente = (Docente) session.getAttribute("user");
	    if (docente == null) {
	        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	        return;
	    }

	    DocenteDAO docenteDAO = new DocenteDAO(connection, docente.getID());
	    ArrayList<Corso> corsi = new ArrayList<>();
	    ArrayList<ArrayList<Appello>> appelli = new ArrayList<>();

	    try {
	        docenteDAO.getCorsiAndAppelliByDocente(corsi, appelli);
	    } 
	    catch (SQLException e) {
	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        response.getWriter().println("Database access failed");
	        return;
	    }
	    if (corsi.isEmpty()) {
	        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	        response.getWriter().println("Nessun corso trovato per il docente.");
	        return;
	    }
	    
	    JsonObject json = new JsonObject();
	    json.add("corsi",   new Gson().toJsonTree(corsi));
	    json.add("appelli", new Gson().toJsonTree(appelli));

	    response.setStatus(HttpServletResponse.SC_OK);
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    response.getWriter().write(json.toString());
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
		try {
			if (connection != null) connection.close();
		} 
		catch (SQLException e) {}
	}
}

