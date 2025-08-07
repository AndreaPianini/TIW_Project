package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import BEANS.Appello;

public class AppelloDAO {
	private Connection con;

	public AppelloDAO(Connection connection) {
		this.con=connection;
	}
	
	
	public Appello getAppelloByVerbale(int verID) throws SQLException {
		
		String query = "SELECT DISTINCT corso, data FROM Iscrizioni WHERE verbale = ?";
		Appello appello = null;
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, verID);
			result = pstatement.executeQuery();
			if (result.next()) {
				appello = new Appello();
				appello.setCorso(result.getInt("corso"));
				appello.setData(result.getDate("data"));
			}
		} 
		finally {
			if (result != null) try { result.close(); } catch (SQLException ignore) {}
			if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
		}
		return appello;
	}
	
	public boolean isVerbalizzabile(int corsoID, Date dataAppello) throws SQLException {
		String query = "SELECT 1 FROM Iscrizioni WHERE corso = ? AND data = ? AND stato_valutazione = 'PUBBLICATO'";
			PreparedStatement pstatement = null;
			ResultSet result = null;
			try {
				pstatement = con.prepareStatement(query);
				pstatement.setInt(1, corsoID);
				pstatement.setDate(2, dataAppello);
				result = pstatement.executeQuery();
				return result.next();
			}
			finally {
				if (result != null) try { result.close(); } catch (SQLException ignore) {}
				if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
			}
	}
	
}