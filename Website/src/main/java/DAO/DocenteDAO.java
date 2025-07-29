package DAO;

import java.sql.Connection;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import BEANS.Appello;
import BEANS.Corso;
import BEANS.Studente;
import BEANS.Valutazione;

public class DocenteDAO {
	
	private Connection connection;
	private int docenteID;
	
	public DocenteDAO(Connection connection, int id) {
		this.connection = connection;
		this.docenteID = id;
	}
	
	
	public void getCorsiAndAppelliByDocente( ArrayList<Corso> corsi, ArrayList<ArrayList<Appello>> appelli) 
			throws SQLException {
	
		String query = "SELECT c.id AS corso_id, c.nome AS nome_corso, c.cfu AS cfu_corso, a.data AS data_appello "
					 + "FROM Corsi c LEFT JOIN Appelli a ON c.id = a.corso "
				     + "WHERE c.docente = ? "
				     + "ORDER BY c.nome, a.data;";
		PreparedStatement pstatement = connection.prepareStatement(query);
		pstatement.setInt(1, docenteID);
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
	
	
	public void getIscrittiByAppello(int corsoID, Date dataAppello, ArrayList<Studente> iscritti, 
			ArrayList<Valutazione> voti) throws SQLException {
		
		String query = "SELECT s.matricola AS stud_matricola, u.nome AS stud_nome, u.cognome AS stud_cognome, "
				+ "u.email AS stud_email, s.corso_laurea AS stud_corso_laurea, i.id AS stud_id, "
				+ "i.voto AS voto, i.stato_valutazione AS stato_valutazione "
				+ "FROM Iscrizioni i, Studenti s, Utenti u "
				+ "WHERE u.id = s.id AND i.studente = u.id AND "
				+ "i.corso = ? AND i.data = ? "
				+ "ORDER BY s.cognome, s.nome;";
		
		PreparedStatement pstatement = connection.prepareStatement(query);
		pstatement.setInt(1, corsoID);
		pstatement.setDate(2, dataAppello);
		ResultSet result = pstatement.executeQuery();
		
		iscritti = new ArrayList<>();
		voti = new ArrayList<>();
		
		while (result.next()) {
			Studente studente = new Studente();
			studente.setID(result.getInt("stud_id"));
			studente.setNome(result.getString("stud_nome"));
			studente.setCognome(result.getString("stud_cognome"));
			studente.setMatricola(result.getString("stud_matricola"));
			studente.setEmail(result.getString("stud_email"));
			studente.setCorsoLaurea(result.getString("stud_corso_laurea"));
			
			Valutazione voto = new Valutazione();
			voto.setVoto(result.getString("voto"));
			voto.setStatoValutazione(result.getString("stato_valutazione"));
			
			iscritti.add(studente);
			voti.add(voto);
		}
		
		result.close();
		pstatement.close();
		
	}
	
	
 	public void modificaVoto(Valutazione voto, int corso, Date data, int studID) throws SQLException{
 		
		String query = "UPDATE Iscrizioni SET voto = ?, stato_valutazione = 'INSERITO' "
				+ "WHERE (stato_valutazione = 'NON_INSERITO' OR stato_valutazione = 'INSERITO') AND "
				+ "corso = ? AND data = ? AND studente = ?";
		
		connection.setAutoCommit(false);
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, voto.getVoto().toString());
			pstatement.setInt(2, corso);
			pstatement.setDate(3, data);
			pstatement.setInt(4, studID);
			pstatement.executeUpdate();
			connection.commit();
		}
		catch(SQLException e) {
			connection.rollback();
			throw new SQLException("Error updating the vote: " + e.getMessage());
		}
		finally {
			connection.setAutoCommit(true);
			pstatement.close();
		}
		
	}
	
 	
	public void pubblicaValutazioni(int corso, Date data) throws SQLException{
		
		String query = "UPDATE Iscrizioni SET stato_valutazione = 'PUBBLICATO' "
				+ "WHERE stato_valutazione = 'INSERITO' AND corso = ' AND data = ?";
		
		connection.setAutoCommit(false);
		PreparedStatement pstatement = null;
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, corso);
			pstatement.setDate(2, data);
			pstatement.executeUpdate();
			connection.commit();
		}
		catch(SQLException e) {
			connection.rollback();
			throw new SQLException("Error updating the vote: " + e.getMessage());
		}
		finally {
			connection.setAutoCommit(true);
			pstatement.close();	
		}
		
	}
	
	
	public void verbalizzaValutazioni(int corso, Date data) throws SQLException{
		
		connection.setAutoCommit(false);
		try {
		    String insertVerbale = "INSERT INTO Verbali (data_ora_creaz) VALUES (NOW())";
		    PreparedStatement psVerbale = connection.prepareStatement(insertVerbale, Statement.RETURN_GENERATED_KEYS);
		    psVerbale.executeUpdate();
		    ResultSet rs = psVerbale.getGeneratedKeys();
		    int idVerbale = -1;
		    if (rs.next()) {
		        idVerbale = rs.getInt(1);
		    } 
		    else {
		        throw new SQLException("Creazione verbale fallita, nessun ID ottenuto.");
		    }

		    String updateIscrizioni = "UPDATE Iscrizioni SET stato_valutazione = 'VERBALIZZATO', verbale = ? "
		                            + "WHERE (stato_valutazione = 'PUBBLICATO' OR stato_valutazione = 'RIFIUTATO') "
		                            + "AND corso = ? AND data = ?";
		    PreparedStatement psUpdate = connection.prepareStatement(updateIscrizioni);
		    psUpdate.setInt(1, idVerbale);
		    psUpdate.setInt(2, corso);
		    psUpdate.setDate(3, data);
		    psUpdate.executeUpdate();

		    connection.commit();
		    psVerbale.close();
		    psUpdate.close();
		} 
		catch (SQLException e) {
		    connection.rollback();
		    throw e; 
		} 
		finally {
		    connection.setAutoCommit(true);
		}
		
	}
	
	
}
