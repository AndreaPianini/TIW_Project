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
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;

import BEANS.Docente;
import BEANS.Valutazione;
import DAO.DocenteDAO;
import DAO.StudenteDAO;


@WebServlet("/ModificaVoto")
@MultipartConfig
public class ModificaVoto extends HttpServlet {

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

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		Docente docente = (Docente) session.getAttribute("user");
		if (docente == null || !docente.getRole().equals("DOCENTE")) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Utente non autenticato.");
			return;
		}

		Integer studID, corsoID;
		Date dataAppello;
		String votoParam = request.getParameter("voto");

		try {
			studID = Integer.parseInt(request.getParameter("studenteID"));
			corsoID = Integer.parseInt(request.getParameter("corsoID"));
			dataAppello = Date.valueOf(request.getParameter("dataAppello"));
		} 
		catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Parametri non validi.");
			return;
		}

		Valutazione valutazione;
		try {
			valutazione = new Valutazione();
			valutazione.setVoto(votoParam);
		} 
		catch (IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Voto non valido.");
			return;
		}

		StudenteDAO studenteDao = new StudenteDAO(connection, studID);
		try {
			if (!studenteDao.checkRegistrazione(corsoID, dataAppello)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Studente non iscritto all'appello.");
				return;
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore durante il controllo dell'iscrizione.");
			return;
		}

		try {
			DocenteDAO docenteDAO = new DocenteDAO(connection, docente.getID());
			docenteDAO.modificaVoto(valutazione, corsoID, dataAppello, studID);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore durante la modifica del voto.");
			return;
		}

		// Risposta OKS
		response.setStatus(HttpServletResponse.SC_OK);
	}

	public void destroy() {
		try {
			if (connection != null) connection.close();
		} catch (SQLException sqle) {}
	}
}

