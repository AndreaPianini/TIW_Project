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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import BEANS.Utente;
import DAO.UtenteDAO;


@WebServlet("/CheckLogin")
@MultipartConfig
public class CheckLogin extends HttpServlet {
	
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
		
		Integer ID = null;
		try {
			ID = Integer.parseInt(request.getParameter("id"));
		}
		catch(NumberFormatException e) {
			ID = null;
		}
        String password = request.getParameter("password");
        
        if ( ID == null || ID < 0 || ID > 99999999 || password == null || password.isEmpty() ) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	response.getWriter().println("ID e Password devono essere validi e non vuoti");
        	response.setCharacterEncoding("UTF-8");
			return;
		}
        
        UtenteDAO dao = new UtenteDAO(this.connection);
        Utente user = null;
        try {
        	user = dao.checkCredenziali(ID, password);
        }
        catch (SQLException e) {
        	System.out.println("Failure in database credential checking");
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        	response.getWriter().println("Si è verificato un errore, riprovare");
        	response.setCharacterEncoding("UTF-8");
			return;
		}

        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("user", new Gson().toJsonTree(user));
            // Aggiunta ruolo manualmente
            jsonResponse.getAsJsonObject("user").addProperty("role", user.getRole());

            if (user.getRole().equals("STUDENTE")) {
            	request.getSession().setAttribute("user", user);
    			response.setStatus(HttpServletResponse.SC_OK);
    			response.setContentType("application/json");
    			response.setCharacterEncoding("UTF-8");
    			response.getWriter().write(jsonResponse.toString());
            } 
            else if (user.getRole().equals("DOCENTE")) {
            	request.getSession().setAttribute("user", user);
    			response.setStatus(HttpServletResponse.SC_OK);
    			response.setContentType("application/json");
    			response.setCharacterEncoding("UTF-8");
    			response.getWriter().write(jsonResponse.toString());
            } 
            else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().println("Ruolo non riconosciuto. Accesso negato.");
				response.setCharacterEncoding("UTF-8");
				return;
            }
        } 
        else {
        	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        	response.getWriter().println("ID o Password errati. Riprovare");
        	response.setCharacterEncoding("UTF-8");
        	return;
        }
        
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