package DAO;

import java.sql.Connection;
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
		ResultSet result = null;
		PreparedStatement pstatement = null;
		
		pstatement = con.prepareStatement(query);
		pstatement.setInt(1, verID);
		result = pstatement.executeQuery();
		if (result.next()) {         
            appello = new Appello();
            appello.setCorso(result.getInt("corso"));
            appello.setData(result.getDate("data"));
        }			
		
			if (result != null) {
				result.close();
			}
			
			if (pstatement != null) {
				pstatement.close();
			}
			
		
		return appello;
	}
}
