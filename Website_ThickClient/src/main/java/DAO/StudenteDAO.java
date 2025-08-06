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
		Studente studente = new Studente();
		String query = "SELECT * FROM Studenti NATURAL JOIN Utenti WHERE id = ?";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, this.studenteID);
			result = pstatement.executeQuery();
			if (result.next()) {
				studente.setID(studenteID);
				studente.setMatricola(result.getString("matricola"));
				studente.setNome(result.getString("nome"));
				studente.setCognome(result.getString("cognome"));
				studente.setEmail(result.getString("email"));
				studente.setCorsoLaurea(result.getString("corso_laurea"));
			}
		} 
		finally {
			if (result != null) try { result.close(); } catch (SQLException ignore) {}
			if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
		}
		return studente;
	}
	
	
	public boolean checkRegistrazione(int corsoID, Date data) throws SQLException {
		String query =" SELECT voto,stato_valutazione FROM Iscrizioni WHERE studente = ? AND corso = ? AND data = ?";
		boolean flag = false;
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, this.studenteID);
			pstatement.setInt(2, corsoID);
			pstatement.setDate(3, data);
			result = pstatement.executeQuery();
			if(result.next()) {
				flag = true;
			}
		} 
		finally {
			if (result != null) try { result.close(); } catch (SQLException ignore) {}
			if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
		}
		return flag;
	}
	
	
	public void rifiutaVoto(int corso, Date data) throws SQLException{
		String query = "UPDATE Iscrizioni SET stato_valutazione = 'RIFIUTATO' "
					 + "WHERE stato_valutazione = 'PUBBLICATO' AND studente = ? AND corso = ? AND data = ?";
		PreparedStatement pstatement = null;
		try {
			connection.setAutoCommit(false);
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, this.studenteID);
			pstatement.setInt(2, corso);
			pstatement.setDate(3, data);
			pstatement.executeUpdate();
			connection.commit();
		} 
		catch(SQLException e) {
			connection.rollback();
			throw new SQLException("Error rejecting the vote: " + e.getMessage());
		} 
		finally {
			connection.setAutoCommit(true);
			if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
		}
	}
		
	
	public Valutazione getVotoByAppello(int corso, Date data) throws SQLException {
		
		String query = "SELECT voto, stato_valutazione "
					 + "FROM iscrizioni "
					 + "WHERE studente = ? AND corso = ? AND data = ?";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, this.studenteID);
			pstatement.setInt(2, corso);
			pstatement.setDate(3, data);
			result = pstatement.executeQuery();
			Valutazione valutazione = null;
			if (result.next()) {
				valutazione = new Valutazione();
				String votoStr = result.getString("voto");
				valutazione.setVoto(votoStr);
				valutazione.setStatoValutazione(result.getString("stato_valutazione"));
			}
			if(valutazione.getStatoValutazione().toString().equals("INSERITO")) {
				valutazione.setStatoValutazione("NON_INSERITO");
				valutazione.setVoto(null);
			}
			return valutazione;
		} 
		finally {
			if (result != null) try { result.close(); } catch (SQLException ignore) {}
			if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
		}
	}
	
	
	public void getCorsiAndAppelliByStudente( ArrayList<Corso> corsi, ArrayList<ArrayList<Appello>> appelli) 
			throws SQLException {
	
		String query = "SELECT c.id   AS id_corso, c.nome AS nome_corso, c.cfu  AS cfu, i.data AS data_appello "
			+ "FROM Iscrizioni AS i LEFT JOIN Corsi AS c ON c.id = i.corso WHERE i.studente = ? ORDER BY c.nome, i.data DESC";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, this.studenteID);
			result = pstatement.executeQuery();
			int currCorso = -1;
			int newCorso = -1;
			Corso c = null;
			Appello a = null;
			ArrayList<Appello> newAppelli = null;
			if(result.next()){
				currCorso = result.getInt("id_corso");
				c = new Corso();
				c.setID(currCorso);
				c.setNome(result.getString("nome_corso"));
				c.setCfu(result.getInt("cfu"));
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
					newCorso = result.getInt("id_corso");
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
						c.setCfu(result.getInt("cfu"));
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
				corsi.clear();
				appelli.clear();
			}
		} 
		finally {
			if (result != null) try { result.close(); } catch (SQLException ignore) {}
			if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
		}
	}
	
	
}