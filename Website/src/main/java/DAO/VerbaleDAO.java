package DAO;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import BEANS.Studente;
import BEANS.Valutazione;
import BEANS.Verbale;

public class VerbaleDAO {
	private Connection con;

	public VerbaleDAO(Connection connection) {
		this.con=connection;
	}
	
	public List<Verbale> GetVerbaliByDocente(int docID) throws SQLException {
		List<Verbale> verbali = new ArrayList<>();
		String query = "SELECT DISTINCT I.verbale, V.data_ora_creaz AS data_ora_creazione"
				+ "FROM Iscrizioni I JOIN Corsi C ON I.corso = C.id JOIN Docenti D ON C.docente = D.id JOIN Verbali V ON I.verbale = V.id"
				+ "WHERE D.id = ?";
		PreparedStatement pstatement = con.prepareStatement(query);
		pstatement.setInt(1, docID);
		ResultSet result = pstatement.executeQuery();
		
		verbali = new ArrayList<>();
		while (result.next()) {
			Verbale v = new Verbale();
			v.setId(result.getInt("verbale"));
			v.setData_Ora((LocalDateTime) result.getObject("data_ora_creazione"));
			verbali.add(v);
		}
		result.close();
		pstatement.close();
		
		return verbali;
		
	}
	
	public Verbale getVerbaleInfo(int verID) throws SQLException {
		
		Verbale verbale = new Verbale();
		String query = "SELECT * FROM Verbali WHERE id = ?";
		PreparedStatement pstatement = con.prepareStatement(query);
		pstatement.setInt(1, verID);
		ResultSet result = pstatement.executeQuery();
			
		if (!result.next())// non ci sono verbali
			return null;
			
		verbale.setId(result.getInt("id"));

		verbale.setData_Ora((LocalDateTime) result.getObject("data_ora_creazione"));
		result.close();
		pstatement.close();	
		return verbale;
		
	}
	

	
	public void getStudentiAndInfoByVerbale(Verbale verbale, 
			ArrayList<Studente> studenti, ArrayList<Valutazione> valutazioni) throws SQLException{
		
		String query = "SELECT id, matricola, nome, cognome, voto , stato_valutazione, data_ora_creaz "
				 	 + "FROM Iscrizioni, Studenti, Verbali "
				 	 + "WHERE verbale = ? AND Iscrizioni.studente = Studenti.id AND Iscrizioni.verbale = Verbali.id";
		
		PreparedStatement pstatement = con.prepareStatement(query);
		pstatement.setInt(1, verbale.getId());
		ResultSet result = pstatement.executeQuery();
		
		studenti = new ArrayList<>();
		valutazioni = new ArrayList<>();
		if (result.next()) {
			verbale.setData_Ora((LocalDateTime) result.getObject("data_ora_creaz"));
		}
		else {
			// Se non ci sono studenti iscritti al verbale, ritorno
			result.close();
			pstatement.close();
			return;
		}
			
		do {
			Studente studente = new Studente();
			studente.setID(result.getInt("id"));
			studente.setMatricola(result.getString("matricola"));
			studente.setNome(result.getString("nome"));
			studente.setCognome(result.getString("cognome"));
			studenti.add(studente);
			
			Valutazione valutazione = new Valutazione();
			valutazione.setVoto(result.getString("voto"));
			valutazione.setStatoValutazione(result.getString("stato_valutazione"));
			valutazioni.add(valutazione);
		} while(result.next());

		result.close();
		pstatement.close();
	}

}