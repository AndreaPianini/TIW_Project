package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import BEANS.Corso;


public class CorsoDAO {
	private Connection con;

	public CorsoDAO(Connection connection) {
		this.con = connection;
	}
	
	public ArrayList<Corso> GetCorsiByStudente(int studID) throws SQLException {
		ArrayList<Corso> corsi = new ArrayList<>();
		String query = "SELECT * FROM StudSegueCorso, Corsi WHERE Corso=id AND Studente = ? ORDER BY nome DESC";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		
		pstatement = con.prepareStatement(query);
		pstatement.setInt(1, studID);
		result = pstatement.executeQuery();
		while (result.next()) {
			Corso c = new Corso();
			c.setID(result.getInt("id"));
			c.setNome(result.getString("nome"));
			corsi.add(c);
		}	
		
		if (result != null) {
			result.close();
			}
			
		if (pstatement != null) {
			pstatement.close();
		}
			
		
		return corsi;
	}

}
