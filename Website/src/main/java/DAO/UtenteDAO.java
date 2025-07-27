package DAO;

import java.sql.Connection;

import BEANS.Utente;

public class UtenteDAO {
	private Connection connection;
	
	public UtenteDAO(Connection conn) {
		this.connection = conn;
	}
	
	
	public Utente checkCredenziali(int ID, String pwd) {
		return null;
	}
}
