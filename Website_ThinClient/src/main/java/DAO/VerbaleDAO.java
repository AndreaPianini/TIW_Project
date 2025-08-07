package DAO;

import java.sql.Connection;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import BEANS.Studente;
import BEANS.Valutazione;
import BEANS.Verbale;
import BEANS.VerbaleRiga;

public class VerbaleDAO {
	private Connection con;

	public VerbaleDAO(Connection connection) {
		this.con=connection;
	}
	
	
	public ArrayList<VerbaleRiga> getVerbaliByDocente(int docID) throws SQLException {
    
		String query = "SELECT DISTINCT V.id AS verbale_id, V.data_ora_creaz AS data_ora, " +
				       "C.id AS corso_id, C.nome AS nome_corso, I.data AS data_appello " +
				       "FROM   Corsi C " +
				       "JOIN   Iscrizioni I ON I.corso = C.id " +
				       "JOIN   Verbali   V ON V.id    = I.verbale " +
				       "WHERE  C.docente = ? " +
				       "ORDER  BY C.nome ASC, I.data ASC, V.id ASC";
	    PreparedStatement pstatement = null;
	    ResultSet result = null;
	    ArrayList<VerbaleRiga> righe = new ArrayList<>();
	    try {
	        pstatement = con.prepareStatement(query);
	        pstatement.setInt(1, docID);
	        result = pstatement.executeQuery();
	        while (result.next()) {
	            VerbaleRiga r = new VerbaleRiga();
	            r.setId(result.getInt("verbale_id"));
	            r.setDataOra(result.getObject("data_ora", LocalDateTime.class));
	            r.setNomeCorso(result.getString("nome_corso"));
	            r.setDataAppello(result.getDate("data_appello").toLocalDate());
	            righe.add(r);
	        }
	    } 
	    finally {
	        if (result != null) try { result.close(); } catch (SQLException ignore) {}
	        if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
	    }
	    return righe;
	}

	
	public Verbale getVerbaleInfo(int verID) throws SQLException {
    
		Verbale verbale = new Verbale();
	    String query = "SELECT * FROM Verbali WHERE id = ?";
	    PreparedStatement pstatement = null;
	    ResultSet result = null;
	    try {
	        pstatement = con.prepareStatement(query);
	        pstatement.setInt(1, verID);
	        result = pstatement.executeQuery();
	        if (!result.next())
	            return null;
	        verbale.setId(result.getInt("id"));
	        verbale.setData_Ora((java.time.LocalDateTime) result.getObject("data_ora_creaz"));
	    } 
	    finally {
	        if (result != null) try { result.close(); } catch (SQLException ignore) {}
	        if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
	    }
	    return verbale;
	}

	
	public void getStudentiAndInfoByVerbale(Verbale verbale, 
        ArrayList<Studente> studenti, ArrayList<Valutazione> valutazioni) throws SQLException{
		
	    String query = "SELECT s.id AS stud_id, s.matricola AS matricola, u.nome AS nome, u.cognome AS cognome, "
	    			 + "i.voto AS voto, i.stato_valutazione As stato_valutazione, v.data_ora_creaz AS data_ora_creaz "
	    			 + "FROM Iscrizioni i "
	    			 + "JOIN Studenti s ON i.studente = s.id "
	    			 + "JOIN Utenti u ON s.id = u.id "
	    			 + "JOIN Verbali v ON i.verbale = v.id "
	    			 + "WHERE i.verbale = ?";
	    PreparedStatement pstatement = null;
	    ResultSet result = null;
	    try {
	        pstatement = con.prepareStatement(query);
	        pstatement.setInt(1, verbale.getId());
	        result = pstatement.executeQuery();
	        studenti.clear();
	        valutazioni.clear();
	        if (result.next()) {
	            verbale.setData_Ora((LocalDateTime) result.getObject("data_ora_creaz"));
	            do {
	                Studente studente = new Studente();
	                studente.setID(result.getInt("stud_id"));
	                studente.setMatricola(result.getString("matricola"));
	                studente.setNome(result.getString("nome"));
	                studente.setCognome(result.getString("cognome"));
	                Valutazione valutazione = new Valutazione();
	                valutazione.setVoto(result.getString("voto"));
	                valutazione.setStatoValutazione(result.getString("stato_valutazione"));
	                studenti.add(studente);
	                valutazioni.add(valutazione);
	            } while (result.next());
	        }
	    } 
	    finally {
	        if (result != null) try { result.close(); } catch (SQLException ignore) {}
	        if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
	    }
	}
	
	
	public boolean isDocenteAutorizzato(int docenteID, int verbaleID) throws SQLException {
		
		String query = "SELECT 1 "
					 + "FROM Iscrizioni i JOIN Corsi c ON i.corso = c.id "
					 + "WHERE c.docente = ? AND i.verbale = ? ";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, docenteID);
			pstatement.setInt(2, verbaleID);
			result = pstatement.executeQuery();
			return result.next(); // se esiste almeno una riga, il docente è autorizzato
		} 
		finally {
			if (result != null) try { result.close(); } catch (SQLException ignore) {}
			if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
		}
		
	}
	
	
}

