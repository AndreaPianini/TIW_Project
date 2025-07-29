package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import BEANS.Studente;
import BEANS.Valutazione;

public class StudenteDAO {
	private Connection con;
	private int id;
	
	public StudenteDAO(Connection connection, int id) {
		this.con = connection;
		this.id = id;
	}
	
	public Studente getStudenteInfo() throws SQLException{
		Studente studInfo = new Studente();
		String query = "SELECT * FROM studente NATURAL JOIN utente WHERE id = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		pstatement = con.prepareStatement(query);
		pstatement.setInt(1, this.id);
		result = pstatement.executeQuery();
		if (result.next()) {
			studInfo.setMatricola(result.getString("matricola"));
			studInfo.setNome(result.getString("nome"));
			studInfo.setCognome(result.getString("cognome"));
			studInfo.setEmail(result.getString("email"));
			studInfo.setCorsoLaurea(result.getString("corso_laurea"));
			}
		

		if (result != null) {
			result.close();
		}
		
		if (pstatement != null) {
			pstatement.close();
		}
			
		return studInfo;
		
	}
	
	public boolean CheckRegistrazione(int corsoID, Date data) throws SQLException {
		String query =" SELECT voto,stato_valutazione FROM Iscrizioni WHERE studente = ? AND corso = ? AND data = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		pstatement = con.prepareStatement(query);
		pstatement.setInt(1, this.id);
		pstatement.setInt(2, corsoID);
		pstatement.setDate(3, data);
		result = pstatement.executeQuery();
		
		if(result.next()) {
			return true;
		}
		
		if (result != null) {
			result.close();
		}
		
		if (pstatement != null) {
			pstatement.close();
		}
			
		return false;
		
	}
	
	public void RifiutaVoto(int corso, Date data) throws SQLException{
		String query = "UPDATE iscrizioni SET stato_valutazione = 'RIFIUTATO' "
					 + "WHERE statovalutazione = 'PUBBLICATO' AND studente = ? AND corso = ? AND data = ?";
		PreparedStatement pstatement = null;
		pstatement = con.prepareStatement(query);
		pstatement.setInt(1, this.id);
		pstatement.setInt(2, corso);
		pstatement.setDate(3, data);
		pstatement.executeUpdate();
		
		if (pstatement != null) {
			pstatement.close();
		}
			
		
	}
		
	
	public Valutazione getVotoByAppello(int corso, Date data) throws SQLException {
		
		String query = "SELECT voto, stato_valutazione "
					 + "FROM iscrizioni "
					 + "WHERE studente = ? AND corso = ? AND data = ?";
		PreparedStatement pstatement = con.prepareStatement(query);
		pstatement.setInt(1, this.id);
		pstatement.setInt(2, corso);
		pstatement.setDate(3, data);
		ResultSet result = pstatement.executeQuery();
		
		Valutazione valutazione = null;
		if (result.next()) {
			valutazione = new Valutazione();
			valutazione.setVoto(result.getString("voto"));
			valutazione.setStatoValutazione(result.getString("stato_valutazione"));
		}
		pstatement.close();
		return valutazione;
		
	}
	
}
