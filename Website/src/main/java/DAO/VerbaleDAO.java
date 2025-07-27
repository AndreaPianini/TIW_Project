package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import BEANS.Studente;

public class VerbaleDAO {
	private Connection con;

	public VerbaleDAO(Connection connection) {
		this.con=connection;
	}
	
	public List<Verbali> GetVerbaliByDocente(int docID) throws SQLException {
		List<Verbali> verbali = new ArrayList<>();
		String query = "SELECT DISTINCT verbale, data_ora_creazione FROM Docente AS D, Iscrizioni AS I, Verbali AS V "
				+ "WHERE I.docente=D.id AND V.id=I.verbale AND I.docente=?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, docID);
			result = pstatement.executeQuery();
			while (result.next()) {
				Verbali v = new Verbali();
				v.setIdCorso(result.getInt("verbale"));
				v.setData(result.getTimestamp("data_ora_creazione"));
				verbali.add(v);
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
		return verbali;
		
	}
	
	public Verbali GetVerbaleInfo(int verID) throws SQLException {
		Verbali verbale = new Verbali();
		String query = "SELECT * FROM Verbali WHERE id = ?";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		
		try {
			
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, verID);
			result = pstatement.executeQuery();
			
			if (!result.next())// non ci sono verbali
				return null;
			
			verbale.setId(result.getInt("codice"));
			verbale.setOra(result.getTimestamp("data_ora_creazione"));
			
			
		} catch (SQLException e) {
			return null;
		} finally {
			try {
				if (result != null) {
					result.close();
				}
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
			}

		}
		return verbale;
		
	}
	
	public Map<Studente, String> findDatiVerbale(int codiceVerbale) throws SQLException{
		Map<Studente,String> datiVerbale = new HashMap<>();
		Studente studente;
		String voto;
		String query = "SELECT id, matricola, nome, cognome, voto FROM iscrizioni, Studente "
				+ "WHERE verbale = ? AND studente=id";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		
		pstatement = con.prepareStatement(query);
		pstatement.setInt(1, codiceVerbale);
		result = pstatement.executeQuery();
		
		while(result.next()) {
			studente = new Studente();
			studente.setID(result.getInt("id"));
			studente.setMatricola(result.getString("matricola"));
			studente.setNome(result.getString("nome"));
			studente.setCognome(result.getString("cognome"));
			voto = result.getString("voto");
			datiVerbale.put(studente, voto);
		}
		return datiVerbale;
	}
}
