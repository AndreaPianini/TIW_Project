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
	
	
	public Utente checkCredenziali(int ID, String pwd) throws SQLException{
		
	    String query1 = "SELECT * FROM Utente WHERE id = ? AND password = ?";
        PreparedStatement pstmt = connection.prepareStatement(query1);
        pstmt.setString(1, ((Integer) ID).toString());
        pstmt.setString(2, pwd);
        ResultSet resultSet = pstmt.executeQuery();
        // Check if the result set has any rows
        if (resultSet.next()) {
        	String query2 = "SELECT * FROM Studente WHERE id = '" + ID +"'";
            Statement stmt2 = connection.createStatement();
            ResultSet resultSet2 = stmt2.executeQuery(query2);
            //The user is a student
            if (resultSet2.next()) {
				Studente studente = new Studente();
				studente.setID(resultSet.getInt("id"));
				studente.setNome(resultSet.getString("nome"));
				studente.setCognome(resultSet.getString("cognome"));
				studente.setEmail(resultSet.getString("email"));
				studente.setMatricola(resultSet2.getString("matricola"));
				studente.setCorsoLaurea(resultSet2.getString("corso_laurea"));
				return studente;
			}
            //The user should be a teacher
            else {
            	String query3 = "SELECT * FROM Docente WHERE id = '" + ID +"'";
                Statement stmt3 = connection.createStatement();
                ResultSet resultSet3 = stmt3.executeQuery(query3);
				if (resultSet3.next()) {
					Docente docente = new Docente();
					docente.setID(resultSet.getInt("id"));
					docente.setNome(resultSet.getString("nome"));
					docente.setCognome(resultSet.getString("cognome"));
					docente.setEmail(resultSet.getString("email"));
					return docente;
				}
				// The user is neither a student nor a teacher - error in the database
				else
					return null; // No matching user found
            }
		}
        //No matching user found (the result set is empty)
		else {
			return null;
		}
        
	}
}
