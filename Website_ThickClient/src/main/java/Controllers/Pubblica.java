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

import BEANS.Docente;
import DAO.DocenteDAO;


@WebServlet("/Pubblica")
public class Pubblica extends HttpServlet {
	
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Parametri non validi.");
			return;
		}
		
		if (corsoID < 0 || corsoID > 9999) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("ID corso non valido.");
			return;
		}

		DocenteDAO docenteDAO = new DocenteDAO(connection, docente.getID());
		try {
			docenteDAO.pubblicaValutazioni(corsoID, dataAppello);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Errore durante la pubblicazione.");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK); 
	}

	public void destroy() {
		try {
			if (connection != null) connection.close();
		} catch (SQLException sqle) {}
	}
}

