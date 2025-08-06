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
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import BEANS.Docente;
import BEANS.Studente;
import DAO.DocenteDAO;


@WebServlet("/StudentiSenzaVoto")
@MultipartConfig
public class StudentiSenzaVoto extends HttpServlet {
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

        DocenteDAO docenteDAO = new DocenteDAO(connection, docente.getID());

        Integer corsoID;
        Date dataAppello;
        try {
            corsoID = Integer.parseInt(request.getParameter("corsoID"));
            dataAppello = Date.valueOf(request.getParameter("dataAppello"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Parametri corso o data appello non validi.");
            return;
        }

        ArrayList<Studente> iscritti = new ArrayList<>();

        try {
            if (!docenteDAO.isAutorizzato(corsoID)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().println("Non autorizzato a questo corso.");
                return;
            }

            iscritti = docenteDAO.getStudentiSenzaVoto(corsoID, dataAppello);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Errore nel recupero degli iscritti.");
            return;
        }

        if (iscritti.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("Nessun iscritto senza voto trovato.");
            return;
        }

        // Costruzione JSON
        JsonObject json = new JsonObject();
        json.addProperty("corsoID", corsoID);
        json.addProperty("dataAppello", dataAppello.toString());
     

        JsonArray iscrittiArray = new JsonArray();
        for (int i = 0; i < iscritti.size(); i++) {
            Studente s = iscritti.get(i);
            JsonObject studJson = new JsonObject();
            studJson.addProperty("id", s.getID());
            studJson.addProperty("matricola", s.getMatricola());
            studJson.addProperty("nome", s.getNome());
            studJson.addProperty("cognome", s.getCognome());
            iscrittiArray.add(studJson);
        }
        json.add("iscritti", iscrittiArray);

        String jsonString = new Gson().toJson(json);
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
        } catch (SQLException ignored) {}
    }

}
