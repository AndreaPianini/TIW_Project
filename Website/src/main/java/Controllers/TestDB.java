package Controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet("/testdb")
public class TestDB extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (PrintWriter out = response.getWriter()) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://127.0.0.1:3306/db_esami?serverTimezone=UTC";
            String user = "root";
            String password = "34rM30ut";

            Connection conn = DriverManager.getConnection(url, user, password);
            out.println("✅ Connessione riuscita!");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT c.id   AS id_corso, c.nome AS nome_corso, c.cfu  AS cfu, i.data AS data_appello\"\r\n"
            		+ "			+ \"FROM Iscrizioni AS i LEFT JOIN Corsi AS c ON c.id = i.corso WHERE i.studente = '6' ORDER BY c.nome, i.data DESC");
            if (rs.next()) {
                out.println("Numero righe in 'docenti': " + rs.getInt(1));
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace(response.getWriter());
        }
    }
}
