package Controllers;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import BEANS.Docente;
import BEANS.VerbaleRiga;
import DAO.VerbaleDAO;


@WebServlet("/MostraVerbali")
@MultipartConfig
public class MostraVerbali extends HttpServlet {

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
		} catch (ClassNotFoundException | SQLException e) {
			throw new UnavailableException("Database connection failed");
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		Docente docente = (Docente) session.getAttribute("user");
		if (docente == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		VerbaleDAO verbaleDao = new VerbaleDAO(connection);
		ArrayList<VerbaleRiga> verbali;

		try {
			verbali = verbaleDao.getVerbaliByDocente(docente.getID());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore nel recupero dei verbali.");
			return;
		}

		if (verbali == null || verbali.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println("Nessun verbale trovato.");
			return;
		}

		// Costruisci JSON
		JsonArray verbaliArray = new JsonArray();
		for (VerbaleRiga v : verbali) {
			JsonObject obj = new JsonObject();
			obj.addProperty("verbaleID", v.getId());
			obj.addProperty("data_ora", v.getDataOra().toString());
			obj.addProperty("nomeCorso", v.getNomeCorso());
			obj.addProperty("dataAppello", v.getDataAppello().toString());
			verbaliArray.add(obj);
		}

		String jsonString = new Gson().toJson(verbaliArray);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(jsonString);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
		try {
			if (connection != null) connection.close();
		} catch (SQLException sqle) {}
	}
}

