package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import BEANS.Docente;
import BEANS.Studente;
import BEANS.Utente;

public class UtenteDAO {
	
	private Connection connection;
	
	public UtenteDAO(Connection conn) {
		this.connection = conn;
	}
	
	
//	public Utente checkCredenziali(int ID, String pwd) throws SQLException{
//		
//	    String query1 = "SELECT * FROM Utenti WHERE id = ? AND password = ?";
//        PreparedStatement pstmt = connection.prepareStatement(query1);
//        pstmt.setInt(1, ID);
//        pstmt.setString(2, pwd);
//        ResultSet resultSet = pstmt.executeQuery();
//        // Check if the result set has any rows
//        if (resultSet.next()) {
//        	String query2 = "SELECT * FROM Studenti WHERE id = '" + ID +"'";
//            Statement stmt2 = connection.createStatement();
//            ResultSet resultSet2 = stmt2.executeQuery(query2);
//            //The user is a student
//            if (resultSet2.next()) {
//				Studente studente = new Studente();
//				studente.setID(resultSet.getInt("id"));
//				studente.setNome(resultSet.getString("nome"));
//				studente.setCognome(resultSet.getString("cognome"));
//				studente.setEmail(resultSet.getString("email"));
//				studente.setMatricola(resultSet2.getString("matricola"));
//				studente.setCorsoLaurea(resultSet2.getString("corso_laurea"));
//				return studente;
//			}
//            //The user should be a teacher
//            else {
//            	String query3 = "SELECT * FROM Docenti WHERE id = '" + ID +"'";
//                Statement stmt3 = connection.createStatement();
//                ResultSet resultSet3 = stmt3.executeQuery(query3);
//				if (resultSet3.next()) {
//					Docente docente = new Docente();
//					docente.setID(resultSet.getInt("id"));
//					docente.setNome(resultSet.getString("nome"));
//					docente.setCognome(resultSet.getString("cognome"));
//					docente.setEmail(resultSet.getString("email"));
//					return docente;
//				}
//				// The user is neither a student nor a teacher - error in the database
//				else
//					return null; // No matching user found
//            }
//		}
//        //No matching user found (the result set is empty)
//		else {
//			return null;
//		}
//        
//	}
	
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
