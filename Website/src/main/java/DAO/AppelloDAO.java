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
	
//	public ArrayList<ArrayList<Appello>> GetAppelliByCorsoAndDocente(int docID, int corso) throws SQLException {
//		
//		ArrayList<ArrayList<Appello>> appelli = new ArrayList<>();
//		String query = "SELECT A.corso, A.data FROM Corsi AS C, Docente AS D, Appelli AS A WHERE D.id = ?"
//				+ "AND C.docente=D.id AND C.id = ? AND C.id=A.corso ORDER BY data DESC";
//		ResultSet result = null;
//		PreparedStatement pstatement = null;
//		pstatement = con.prepareStatement(query);
//		pstatement.setInt(1, docID);
//		pstatement.setInt(2, corso);
//		result = pstatement.executeQuery();
//	}
	
	
	public Appello GetAppelloByVerbale(int verID) throws SQLException {
		Appello appello = new Appello();
		String query = "SELECT DISTINCT corso, data FROM iscrizioni WHERE verbale = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		
		pstatement = con.prepareStatement(query);
		pstatement.setInt(1, verID);
		result = pstatement.executeQuery();
		appello.setCorso(result.getInt("corso"));
		appello.setData(result.getDate("data"));			
		
			if (result != null) {
				result.close();
			}
			
			if (pstatement != null) {
				pstatement.close();
			}
			
		
		return appello;
	}
}
