package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CorsoDAO {
	private Connection con;

	public CorsoDAO(Connection connection) {
		this.con = connection;
	}
	
	public List<Corsi> GetCorsiByStudente(int studID) throws SQLException {
		List<Corsi> corsi = new ArrayList<>();
		String query = "SELECT * FROM StudSegueCorso, Corsi WHERE Corso=id AND Studente = ? ORDER BY nome DESC";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, studID);
			result = pstatement.executeQuery();
			while (result.next()) {
				Corsi c = new Corsi();
				c.setId(result.getInt("id"));
				c.setNome(result.getString("nome"));
				corsi.add(c);
			}
		} catch (SQLException e) {
			throw new SQLException(e);

		} finally {
			try {
				if (result != null) {
					result.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close result");
			}
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close statement");
			}
		}
		return corsi;
	}

}
