package Controllers;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import BEANS.Appello;
import BEANS.Corso;
import BEANS.Studente;
import BEANS.Valutazione;
import BEANS.Verbale;
import DAO.AppelloDAO;
import DAO.CorsoDAO;
import DAO.VerbaleDAO;


@WebServlet("/MostraVerbaleCreato")
public class MostraVerbaleCreato extends HttpServlet {

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

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String idVerbaleStr = request.getParameter("verbaleID");
		if (idVerbaleStr == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing verbale ID");
			return;
		}

		int verbaleID;
		try {
			verbaleID = Integer.parseInt(idVerbaleStr);
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid verbale ID");
			return;
		}

		VerbaleDAO verbaleDAO = new VerbaleDAO(connection);
		AppelloDAO appelloDAO = new AppelloDAO(connection);
		CorsoDAO corsoDAO = new CorsoDAO(connection);

		Verbale verbale = new Verbale();
		verbale.setId(verbaleID);

		ArrayList<Studente> studenti = new ArrayList<>();
		ArrayList<Valutazione> valutazioni = new ArrayList<>();
		Appello appello = null;
		Corso corso = null;

		try {
			verbaleDAO.getStudentiAndInfoByVerbale(verbale, studenti, valutazioni);
			if (studenti.isEmpty() || valutazioni.isEmpty()) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("No students or evaluations found for this verbale");
				return;
			}

			appello = appelloDAO.getAppelloByVerbale(verbaleID);
			if (appello == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("No appello found for this verbale");
				return;
			}

			corso = corsoDAO.getCorsoById(appello.getCorso());

		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Database error");
			return;
		}

		// Costruzione JSON
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty("verbaleId", verbale.getId());
		jsonResponse.addProperty("verbaleDataOra", verbale.getData_Ora().toString());

		JsonObject corsoObj = new JsonObject();
		corsoObj.addProperty("id", corso.getID());
		corsoObj.addProperty("nome", corso.getNome());
		corsoObj.addProperty("cfu", corso.getCfu());
		jsonResponse.add("corso", corsoObj);

		JsonObject appelloObj = new JsonObject();
		appelloObj.addProperty("data", appello.getData().toString());
		jsonResponse.add("appello", appelloObj);

		JsonArray studentiArray = new JsonArray();
		for (int i = 0; i < studenti.size(); i++) {
			Studente stud = studenti.get(i);
			Valutazione val = valutazioni.get(i);
			JsonObject studObj = new JsonObject();
			studObj.addProperty("matricola", stud.getMatricola());
			studObj.addProperty("nome", stud.getNome());
			studObj.addProperty("cognome", stud.getCognome());
			studObj.addProperty("voto", val.getVoto().toString());
			studentiArray.add(studObj);
		}
		jsonResponse.add("studenti", studentiArray);

		String json = new Gson().toJson(jsonResponse);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);  // accetta GET e POST
	}

	public void destroy() {
		try {
			if (connection != null) connection.close();
		} catch (SQLException sqle) {}
	}
}

