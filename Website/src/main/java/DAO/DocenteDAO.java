package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DocenteDAO {
	private Connection con;
	private int id;
	
	public DocenteDAO(Connection connection, int i) {
		this.con = connection;
		this.id = i;
	}
	
	public void ModificaVoto(Valutazione voto, int corso, Date data, int studID) throws SQLException{
		String query = "UPDATE Iscrizioni SET voto = ?, stato_valutazione = 'INSERITO' WHERE (stato_valutazione = 'NON_INSERITO' OR stato_valutazione = 'INSERITO') AND "
				+ "corso = ? AND data = ? AND studente = ?";
		con.setAutoCommit(false);
		PreparedStatement pstatement = null;
		try{
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, voto);
			pstatement.setInt(2, corso);
			pstatement.setDate(3, data);
			pstatement.setInt(4, studID);
			pstatement.executeUpdate();
			
		}catch(SQLException e) {
			con.rollback();
		}finally {
			con.setAutoCommit(true);
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close statement");
			}
		}
		
	}
	
	public void PubblicaValutazioni(int corso, Date data) throws SQLException{
		String query = "UPDATE Iscrizioni SET stato_valutazione = 'PUBBLICATO' WHERE stato_valutazione = 'INSERITO' AND corso = ' AND data = ?";
		con.setAutoCommit(false);
		PreparedStatement pstatement = null;
		try{
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, corso);
			pstatement.setDate(2, data);
			pstatement.executeUpdate();
			
		}catch(SQLException e) {
			con.rollback();
		}finally {
			con.setAutoCommit(true);
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
