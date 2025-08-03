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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import BEANS.Docente;
import BEANS.Studente;
import BEANS.Valutazione;
import DAO.DocenteDAO;
import DAO.StudenteDAO;


@WebServlet("/Modifica")
public class Modifica extends HttpServlet {

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

		Integer studenteID, corsoID;
		Date dataAppello;
		try {
			studenteID = Integer.parseInt(request.getParameter("studenteID"));
			corsoID = Integer.parseInt(request.getParameter("corsoID"));
			dataAppello = Date.valueOf(request.getParameter("dataAppello"));
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Parametri non validi.");
			return;
		}

		DocenteDAO docenteDAO = new DocenteDAO(connection, docente.getID());
		StudenteDAO studenteDAO = new StudenteDAO(connection, studenteID);
		Studente studente;
		Valutazione valutazione;

		try {
			if (!docenteDAO.isAutorizzato(corsoID)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				response.getWriter().println("Non autorizzato.");
				return;
			}

			studente = studenteDAO.getStudenteInfo();
			valutazione = studenteDAO.getVotoByAppello(corsoID, dataAppello);

			if (studente == null || valutazione == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("Studente o valutazione non trovati.");
				return;
			}

			// Costruisci JSON
			JsonObject json = new JsonObject();
			JsonObject studJson = new JsonObject();
			studJson.addProperty("id", studente.getID());
			studJson.addProperty("nome", studente.getNome());
			studJson.addProperty("cognome", studente.getCognome());
			studJson.addProperty("matricola", studente.getMatricola());
			json.add("studente", studJson);

			json.addProperty("valutazione", valutazione.getVoto().toString());
			json.addProperty("corsoID", corsoID);
			json.addProperty("dataAppello", dataAppello.toString());

			// Voti possibili
			JsonArray votiPossibili = new JsonArray();
			for (Valutazione.Voto v : Valutazione.Voto.values()) {
				votiPossibili.add(v.toString());
			}
			json.add("votiPossibili", votiPossibili);

			String jsonString = new Gson().toJson(json);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(jsonString);

		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore database.");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);  // supporta GET e POST
	}

	public void destroy() {
		try {
			if (connection != null) connection.close();
		} catch (SQLException sqle) {}
	}
}

