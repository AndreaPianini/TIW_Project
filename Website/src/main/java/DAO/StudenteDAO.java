package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import BEANS.Appello;
import BEANS.Corso;
import BEANS.Studente;
import BEANS.Valutazione;

public class StudenteDAO {
	private Connection connection;
	private int studenteID;
	
	public StudenteDAO(Connection connection, int id) {
		this.connection = connection;
		this.studenteID = id;
	}
	
	public Studente getStudenteInfo() throws SQLException{
		Studente studInfo = new Studente();
		String query = "SELECT * FROM Studenti NATURAL JOIN Utenti WHERE id = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		pstatement = connection.prepareStatement(query);
		pstatement.setInt(1, this.studenteID);
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
	
	
	public boolean checkRegistrazione(int corsoID, Date data) throws SQLException {
		String query =" SELECT voto,stato_valutazione FROM Iscrizioni WHERE studente = ? AND corso = ? AND data = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		pstatement = connection.prepareStatement(query);
		pstatement.setInt(1, this.studenteID);
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
	
	
	public void rifiutaVoto(int corso, Date data) throws SQLException{
		String query = "UPDATE Iscrizioni SET stato_valutazione = 'RIFIUTATO' "
					 + "WHERE statovalutazione = 'PUBBLICATO' AND studente = ? AND corso = ? AND data = ?";
		connection.setAutoCommit(false);
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, this.studenteID);
			pstatement.setInt(2, corso);
			pstatement.setDate(3, data);
			pstatement.executeUpdate();
			connection.commit();
		} catch(SQLException e) {
			connection.rollback();
			throw new SQLException("Error rejecting the vote: " + e.getMessage());
		} finally {
			connection.setAutoCommit(true);
			pstatement.close();	
		}
		
			
		
	}
		
	
	public Valutazione getVotoByAppello(int corso, Date data) throws SQLException {
		
		String query = "SELECT voto, stato_valutazione "
					 + "FROM iscrizioni "
					 + "WHERE studente = ? AND corso = ? AND data = ?";
		PreparedStatement pstatement = connection.prepareStatement(query);
		pstatement.setInt(1, this.studenteID);
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
	
	
	public void getCorsiAndAppelliByStudente( ArrayList<Corso> corsi, ArrayList<ArrayList<Appello>> appelli) 
			throws SQLException {
	
		String query = "SELECT c.id   AS id_corso, c.nome AS nome_corso, c.cfu  AS cfu, i.data AS data_appello"
			+ "FROM Iscrizioni AS i JOIN Corsi AS c ON c.id = i.corso WHERE i.studente = ? ORDER BY c.nome ASC, i.data ASC";
		PreparedStatement pstatement = connection.prepareStatement(query);
		pstatement.setInt(1, this.studenteID);
		ResultSet result = pstatement.executeQuery();
		
		int currCorso = -1;
		int newCorso = -1;
		Corso c = null;
		Appello a = null;
		ArrayList<Appello> newAppelli = null;
		if(result.next()){
			currCorso = result.getInt("corso_id");
			c = new Corso();
			c.setID(currCorso);
			c.setNome(result.getString("nome_corso"));
			c.setCfu(result.getInt("cfu_corso"));
			corsi.add(c);
			//Check if the first course has an appello
			if (result.getDate("data_appello") != null) {
				a = new Appello();
				a.setCorso(currCorso);
				a.setData(result.getDate("data_appello"));
				newAppelli = new ArrayList<>();
				newAppelli.add(a);
				appelli.add(newAppelli);
			}
			else {
				newAppelli = new ArrayList<>();
				appelli.add(newAppelli);
			}
			while (result.next()) {
				newCorso = result.getInt("corso_id");
				if (newCorso == currCorso) {
					if(result.getDate("data_appello") != null) {
						a = new Appello();
						a.setCorso(currCorso);
						a.setData(result.getDate("data_appello"));
						newAppelli.add(a);
					}
				} 
				else {
					currCorso = newCorso;
					c = new Corso();
					c.setID(currCorso);
					c.setNome(result.getString("nome_corso"));
					c.setCfu(result.getInt("cfu_corso"));
					corsi.add(c);
					//Check if the curr course has an appello
					if (result.getDate("data_appello") != null) {
						newAppelli = new ArrayList<>();
						appelli.add(newAppelli);
						a = new Appello();
						a.setCorso(currCorso);
						a.setData(result.getDate("data_appello"));
						newAppelli.add(a);
					}
					else {
						newAppelli = new ArrayList<>();
						appelli.add(newAppelli);
					}
				}
			}
		}
		else {
			corsi = null;
			appelli = null;
		}
		
		result.close();
		pstatement.close();
		
	}
	
}
