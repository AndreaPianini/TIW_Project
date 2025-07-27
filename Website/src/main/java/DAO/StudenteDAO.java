package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import BEANS.Studente;

public class StudenteDAO {
	private Connection con;
	private int id;
	
	public StudenteDAO(Connection connection, int i) {
		this.con = connection;
		this.id = i;
	}
	
	public Studente getStudenteInfo() throws SQLException{
		Studente studInfo = new Studente();
		String query = "SELECT * FROM studente NATURAL JOIN utente WHERE id = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try  {
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
		return studInfo;
		
	}
	
	public void RifiutaVoto(int corso, Date data) throws SQLException{
		String query = "UPDATE iscrizioni SET stato_valutazione = 'RIFIUTATO' WHERE statovalutazione = 'PUBBLICATO' AND studente = ? AND corso = ? AND data = ?";
		PreparedStatement pstatement = null;
		con.setAutoCommit(false);
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, this.id);
			pstatement.setInt(2, corso);
			pstatement.setDate(3, data);
			pstatement.executeUpdate();
			con.commit();
		} catch (SQLException e) {
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
