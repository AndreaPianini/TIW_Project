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
import java.util.ArrayList;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import BEANS.Docente;
import BEANS.Studente;
import BEANS.Valutazione;
import DAO.DocenteDAO;

@WebServlet("/VediIscritti")
public class VediIscritti extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public void init() throws ServletException {
        try {
            ServletContext context = getServletContext();
            String driver = context.getInitParameter("dbDriver");
            String url = context.getInitParameter("dbUrl");
            String user = context.getInitParameter("dbUser");
            String password = context.getInitParameter("dbPassword");
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);

            JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(context);
            WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);
            templateResolver.setTemplateMode(TemplateMode.HTML);
            templateResolver.setSuffix(".html");
            this.templateEngine = new TemplateEngine();
            this.templateEngine.setTemplateResolver(templateResolver);
        } catch (ClassNotFoundException e) {
            throw new UnavailableException("Can't load database driver");
        } catch (SQLException e) {
            throw new UnavailableException("Couldn't get db connection");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        Docente docente = (Docente) session.getAttribute("user");
        if (docente == null) {
            response.sendRedirect(getServletContext().getContextPath() + "/index.html");
            return;
        }

        DocenteDAO docenteDAO = new DocenteDAO(connection, docente.getID());

        // Parametri obbligatori
        Integer corsoID = null;
        Date dataAppello = null;
        try {
            corsoID = Integer.parseInt(request.getParameter("corsoID"));
        } catch (NumberFormatException ignored) {}
        try {
            dataAppello = Date.valueOf(request.getParameter("dataAppello"));
        } catch (Exception ignored) {}

        // ----------------- nuova funzionalità: ordinamento -----------------
        String sortBy = request.getParameter("sortBy");
        String order  = request.getParameter("order");

        ArrayList<String> sortByWhitelist = new ArrayList<>();
        sortByWhitelist.add("matricola");
        sortByWhitelist.add("cognome");
        sortByWhitelist.add("nome");
        sortByWhitelist.add("email");
        sortByWhitelist.add("corsoLaurea");
        sortByWhitelist.add("voto");
        sortByWhitelist.add("stato");

        ArrayList<String> orderWhitelist = new ArrayList<>();
        orderWhitelist.add("ASC");
        orderWhitelist.add("DESC");

        if (sortBy == null || !sortByWhitelist.contains(sortBy)) {
            sortBy = "matricola";
        }
        if (order == null || !orderWhitelist.contains(order)) {
            order = "ASC";
        }
        // --------------------------------------------------------------------

        if (corsoID == null || dataAppello == null) {
            renderPageError(request, response, "Corso o data appello non validi.");
            return;
        }

        ArrayList<Studente> iscritti = new ArrayList<>();
        ArrayList<Valutazione> voti   = new ArrayList<>();
        try {
            if (!docenteDAO.isAutorizzato(corsoID)) {
                renderPageError(request, response, "Non sei abilitato a modificare questo corso.");
                return;
            }

            docenteDAO.getIscrittiByAppello(corsoID, dataAppello, sortBy, order, iscritti, voti);
        } catch (SQLException e) {
            e.printStackTrace();
            renderPageError(request, response, "Si è verificato un errore durante il recupero degli iscritti.");
            return;
        }

        if (iscritti.isEmpty()) {
            renderPageError(request, response, "Nessun iscritto trovato per l'appello.");
            return;
        }

        String path = "/WEB-INF/Iscritti.html";
        JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
        ctx.setVariable("iscritti", iscritti);
        ctx.setVariable("voti", voti);
        ctx.setVariable("corsoID", corsoID);
        ctx.setVariable("dataAppello", dataAppello);
        ctx.setVariable("sortBy", sortBy);   // necessario alla view per mostrare lo stato del sort
        ctx.setVariable("order",  order);
        templateEngine.process(path, ctx, response.getWriter());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    /* ----------------------- utils ----------------------- */
    private void renderPageError(HttpServletRequest request, HttpServletResponse response, String errorMessage) throws IOException {
        JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
        ctx.setVariable("error", errorMessage);
        templateEngine.process("/WEB-INF/Iscritti.html", ctx, response.getWriter());
    }

    public void destroy() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }
}
