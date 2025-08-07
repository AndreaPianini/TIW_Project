package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import BEANS.Docente;
import BEANS.Studente;
import BEANS.Utente;

public class UtenteDAO {
	
	private Connection connection;
	
	public UtenteDAO(Connection conn) {
		this.connection = conn;
	}
	
	
	public Utente checkCredenziali(int ID, String pwd) throws SQLException {
	    String queryUtente = "SELECT * FROM Utenti WHERE id = ? AND password = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(queryUtente)) {
	        pstmt.setInt(1, ID);
	        pstmt.setString(2, pwd);
	        try (ResultSet rsUtente = pstmt.executeQuery()) {
	            if (rsUtente.next()) {
	                String nome = rsUtente.getString("nome");
	                String cognome = rsUtente.getString("cognome");
	                String email = rsUtente.getString("email");

	                // Verifica se è studente
	                String queryStud = "SELECT matricola, corso_laurea FROM Studenti WHERE id = ?";
	                try (PreparedStatement psStud = connection.prepareStatement(queryStud)) {
	                    psStud.setInt(1, ID);
	                    try (ResultSet rsStud = psStud.executeQuery()) {
	                        if (rsStud.next()) {
	                            Studente s = new Studente();
	                            s.setID(ID);
	                            s.setNome(nome);
	                            s.setCognome(cognome);
	                            s.setEmail(email);
	                            s.setMatricola(rsStud.getString("matricola"));
	                            s.setCorsoLaurea(rsStud.getString("corso_laurea"));
	                            return s;
	                        }
	                    }
	                }

	                // Verifica se è docente
	                String queryDoc = "SELECT id FROM Docenti WHERE id = ?";
	                try (PreparedStatement psDoc = connection.prepareStatement(queryDoc)) {
	                    psDoc.setInt(1, ID);
	                    try (ResultSet rsDoc = psDoc.executeQuery()) {
	                        if (rsDoc.next()) {
	                            Docente d = new Docente();
	                            d.setID(ID);
	                            d.setNome(nome);
	                            d.setCognome(cognome);
	                            d.setEmail(email);
	                            return d;
	                        }
	                    }
	                }
	            }
	        }
	    }
	    return null;
	}
}
