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
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, studID);
			result = pstatement.executeQuery();
			while (result.next()) {
				Corso c = new Corso();
				c.setID(result.getInt("id"));
				c.setNome(result.getString("nome"));
				corsi.add(c);
			}
		} 
		finally {
			if (result != null) try { result.close(); } catch (SQLException ignore) {}
			if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
		}
		return corsi;
	}
	
	
	public Corso getCorsoById(int corsoID) throws SQLException {
		Corso corso = new Corso();
		String query= "SELECT * FROM Corsi WHERE id=?";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, corsoID);
			result = pstatement.executeQuery();
			if (result.next()) {
				corso.setID(corsoID);
				corso.setCfu(result.getInt("cfu"));
				corso.setNome(result.getString("nome"));
				corso.setDocenteID(result.getInt("docente"));
			}
		} 
		finally {
			if (result != null) try { result.close(); } catch (SQLException ignore) {}
			if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
		}
		return corso;
	}

}