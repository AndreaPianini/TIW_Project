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
import java.sql.DriverManager;
import java.sql.SQLException;

import BEANS.Utente;
import DAO.UtenteDAO;


@WebServlet("/CheckLogin")
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
		
		int ID = Integer.parseInt(request.getParameter("id"));
        String password = request.getParameter("password");

        UtenteDAO dao = new UtenteDAO(this.connection);
        Utente user = dao.checkCredenziali(ID, password);

        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            if (user.getRole().equals("Studente")) {
                response.sendRedirect(request.getContextPath() + "/studente/home");
            } 
            else if (user.getRole().equals("Docente")) {
                response.sendRedirect(request.getContextPath() + "/docente/home");
            } 
            else {
                // fallback per utente senza ruolo
                request.setAttribute("error", "Utente senza ruolo definito");
                request.setAttribute("id", ID);
                request.getRequestDispatcher("/WEB-INF/login.html").forward(request, response);
            }
        } 
        else {
            request.setAttribute("error", "Credenziali non valide");
            request.setAttribute("id", ID);
            request.getRequestDispatcher("/WEB-INF/login.html").forward(request, response);
        }
    }
	}

}
