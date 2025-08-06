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
import com.google.gson.JsonArray;

import BEANS.Docente;
import DAO.DocenteDAO;


@WebServlet("/Verbalizza")
public class Verbalizza extends HttpServlet {

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

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		Docente docente = (Docente) session.getAttribute("user");
		if (docente == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		Integer corsoID = null;
		Date dataAppello = null;
		try {
			corsoID = Integer.parseInt(request.getParameter("corsoID"));
			dataAppello = Date.valueOf(request.getParameter("dataAppello"));
		} 
		catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Corso o data dell'appello non validi.");
			return;
		}

		int verbaleID = -1;
		try {
			DocenteDAO docenteDAO = new DocenteDAO(connection, docente.getID());
			verbaleID = docenteDAO.verbalizzaValutazioni(corsoID, dataAppello);
		} 
		catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore durante la verbalizzazione.");
			return;
		}

		if (verbaleID < 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Verbalizzazione non riuscita.");
			return;
		}

		// Rispondi con ID verbale in JSON
		String json = new Gson().toJson(verbaleID);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}

	public void destroy() {
		try {
			if (connection != null) connection.close();
		} catch (SQLException sqle) {}
	}
}

