package DAO;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import BEANS.Studente;
import BEANS.Verbale;

public class VerbaleDAO {
	private Connection con;

	public VerbaleDAO(Connection connection) {
		this.con=connection;
	}
	
	public List<Verbale> GetVerbaliByDocente(int docID) throws SQLException {
		List<Verbale> verbali = new ArrayList<>();
		String query = "SELECT DISTINCT verbale, data_ora_creazione FROM Docenti AS D, Iscrizioni AS I, Verbali AS V "
				+ "WHERE I.docente=D.id AND V.id=I.verbale AND I.docente=?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		
		pstatement = con.prepareStatement(query);
		pstatement.setInt(1, docID);
		result = pstatement.executeQuery();
		while (result.next()) {
			Verbale v = new Verbale();
			v.setId(result.getInt("verbale"));
			v.setData_Ora((LocalDateTime) result.getObject("data_ora_creazione"));
			verbali.add(v);
		}
		
		if (result != null) {
			result.close();
		}
			
		if (pstatement != null) {
			pstatement.close();
		}
			
	
		return verbali;
		
	}
	
	public Verbale GetVerbaleInfo(int verID) throws SQLException {
		Verbale verbale = new Verbale();
		String query = "SELECT * FROM Verbali WHERE id = ?";
		PreparedStatement pstatement = null;
		ResultSet result = null;
					
		pstatement = con.prepareStatement(query);
		pstatement.setInt(1, verID);
		result = pstatement.executeQuery();
			
		if (!result.next())// non ci sono verbali
			return null;
			
		verbale.setId(result.getInt("codice"));
		verbale.setData_Ora((LocalDateTime) result.getObject("data_ora_creazione"));
		
			
		
		if (result != null) {
			result.close();
		}
		if (pstatement != null) {
			pstatement.close();
		}
			
		return verbale;
		
	}
	
	public Map<Studente, String> findDatiVerbale(int codiceVerbale) throws SQLException{
		Map<Studente,String> datiVerbale = new HashMap<>();
		Studente studente;
		String voto;
		String query = "SELECT id, matricola, nome, cognome, voto FROM Iscrizioni, Studenti "
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
