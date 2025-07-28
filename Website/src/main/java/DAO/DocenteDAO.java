package DAO;

import java.sql.Connection;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import BEANS.Appello;
import BEANS.Corso;
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
	
	
 	public void ModificaVoto(Valutazione voto, int corso, Date data, int studID) throws SQLException{
 		
		String query = "UPDATE Iscrizioni SET voto = ?, stato_valutazione = 'INSERITO' "
				+ "WHERE (stato_valutazione = 'NON_INSERITO' OR stato_valutazione = 'INSERITO') AND "
				+ "corso = ? AND data = ? AND studente = ?";
		connection.setAutoCommit(false);
		PreparedStatement pstatement = null;
		pstatement = connection.prepareStatement(query);
		pstatement.setString(1, voto.getVoto().toString());
		pstatement.setInt(2, corso);
		pstatement.setDate(3, data);
		pstatement.setInt(4, studID);
		pstatement.executeUpdate();
		pstatement.close();
		
	}
	
	public void PubblicaValutazioni(int corso, Date data) throws SQLException{
		String query = "UPDATE Iscrizioni SET stato_valutazione = 'PUBBLICATO' WHERE stato_valutazione = 'INSERITO' AND corso = ' AND data = ?";
		connection.setAutoCommit(false);
		PreparedStatement pstatement = null;
		try{
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, corso);
			pstatement.setDate(2, data);
			pstatement.executeUpdate();
			
		}catch(SQLException e) {
			connection.rollback();
		}finally {
			connection.setAutoCommit(true);
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close statement");
			}
		}
	}
	
	
	
}
