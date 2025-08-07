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
				     + "ORDER BY c.nome, a.data DESC;";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, docenteID);
			result = pstatement.executeQuery();
			
			int currCorso = -2;
			int newCorso = -1;
			Corso c = null;
			Appello a = null;
			ArrayList<Appello> newAppelli = null;
			
			corsi.clear();
			appelli.clear();
			if (!result.next()) {
				return;
			}
			do{
				newCorso = result.getInt("corso_id");
				//If the course is the first one or it's different from the previous one
				if (newCorso != currCorso) {
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
				//If the course is the same as the previous one
				else {
					if(result.getDate("data_appello") != null) {
						a = new Appello();
						a.setCorso(currCorso);
						a.setData(result.getDate("data_appello"));
						newAppelli.add(a);
					}
				} 
			}while (result.next());
		} 
		finally {
			if (result != null) try { result.close(); } catch (SQLException ignore) {}
			if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
		}
	}
	
	
    
    public void getIscrittiByAppello(int corsoID, Date dataAppello,
                                     ArrayList<Studente> iscritti,
                                     ArrayList<Valutazione> voti) throws SQLException {


        String query = "SELECT "
                     + "s.matricola         AS matricola, "
                     + "u.cognome           AS cognome, "
                     + "u.nome              AS nome, "
                     + "u.email             AS email, "
                     + "s.corso_laurea      AS corsoLaurea, "
                     + "i.studente          AS stud_id, "
                     + "i.voto              AS voto, "
                     + "i.stato_valutazione AS stato "
                     + "FROM   Iscrizioni i "
                     + "JOIN   Studenti  s ON i.studente = s.id "
                     + "JOIN   Utenti    u ON s.id       = u.id "
                     + "JOIN   Corsi     c ON i.corso    = c.id "
                     + "WHERE  i.corso = ? AND i.data = ? AND c.docente = ? ";

        PreparedStatement pstatement = null;
        ResultSet result = null;
        try {
            pstatement = connection.prepareStatement(query);
            pstatement.setInt(1, corsoID);
            pstatement.setDate(2, dataAppello);
            pstatement.setInt(3, docenteID);

            result = pstatement.executeQuery();

            iscritti.clear();
            voti.clear();
            while (result.next()) {
                Studente stud = new Studente();
                stud.setID(result.getInt("stud_id"));
                stud.setNome(result.getString("nome"));
                stud.setCognome(result.getString("cognome"));
                stud.setMatricola(result.getString("matricola"));
                stud.setEmail(result.getString("email"));
                stud.setCorsoLaurea(result.getString("corsoLaurea"));

                Valutazione val = new Valutazione();
                val.setVoto(result.getString("voto"));
                val.setStatoValutazione(result.getString("stato"));

                iscritti.add(stud);
                voti.add(val);
            }
        } 
        finally {
            if (result != null) try { result.close(); } catch (SQLException ignore) {}
            if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
        }
        
    }

	
	
 	public void modificaVoto(Valutazione voto, int corso, Date data, int studID) throws SQLException{
 		
 		String query = "UPDATE Iscrizioni SET voto = ?, stato_valutazione = 'INSERITO' "
 				 + "WHERE (stato_valutazione = 'NON_INSERITO' OR stato_valutazione = 'INSERITO') "
 				 + "AND corso = ? AND data = ? AND studente = ? "
 				 + "AND EXISTS (SELECT 1 FROM Corsi c WHERE c.id = Iscrizioni.corso AND c.docente = ?)";
 		PreparedStatement pstatement = null;
 		try {
 			connection.setAutoCommit(false);
 			pstatement = connection.prepareStatement(query);
 			pstatement.setString(1, voto.getVoto().toString());
 			pstatement.setInt(2, corso);
 			pstatement.setDate(3, data);
 			pstatement.setInt(4, studID);
 			pstatement.setInt(5, docenteID);
 			pstatement.executeUpdate();
 			connection.commit();
 			connection.setAutoCommit(true);
 		}
 		catch(SQLException e) {
 			connection.rollback();
 			throw new SQLException("Error updating the vote: " + e.getMessage());
 		}
 		finally {
 			connection.setAutoCommit(true);
 			if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
 		}
 		
 	}
	
 	
	public void pubblicaValutazioni(int corso, Date data) throws SQLException{
		
		String query = "UPDATE Iscrizioni SET stato_valutazione = 'PUBBLICATO' "
				+ "WHERE stato_valutazione = 'INSERITO' AND corso = ? AND data = ?";
		
		
		PreparedStatement pstatement = null;
		try{
			connection.setAutoCommit(false);
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, corso);
			pstatement.setDate(2, data);
			pstatement.executeUpdate();
			connection.commit();
			connection.setAutoCommit(true);
		}
		catch(SQLException e) {
			connection.rollback();
			throw new SQLException("Error updating the vote: " + e.getMessage());
		}
		finally {
			connection.setAutoCommit(true);
			if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
		}
		
	}
	
	//da fare - controllo che ci siano valutazioni da verbalizzare
	public int verbalizzaValutazioni(int corso, Date data) throws SQLException{
		
		int idVerbale = -1;
		PreparedStatement psVerbale = null;
		PreparedStatement psUpdate = null;
		ResultSet rs = null;
		try {
			connection.setAutoCommit(false);
		    String insertVerbale = "INSERT INTO Verbali (data_ora_creaz) VALUES (NOW())";
		    psVerbale = connection.prepareStatement(insertVerbale, Statement.RETURN_GENERATED_KEYS);
		    psVerbale.executeUpdate();
		    rs = psVerbale.getGeneratedKeys();
		    if (rs.next()) {
		        idVerbale = rs.getInt(1);
		    } 
		    else {
		        throw new SQLException("Creazione verbale fallita, nessun ID ottenuto.");
		    }
		    
		    String updateIscrizioni1 = "UPDATE Iscrizioni SET voto = 'RIMANDATO' "
                    				 + "WHERE stato_valutazione = 'RIFIUTATO' AND corso = ? AND data = ?";
			psUpdate = connection.prepareStatement(updateIscrizioni1);
			psUpdate.setInt(1, corso);
			psUpdate.setDate(2, data);
			psUpdate.executeUpdate();
			psUpdate.close();

		    String updateIscrizioni2 = "UPDATE Iscrizioni SET stato_valutazione = 'VERBALIZZATO', verbale = ? "
		                             + "WHERE (stato_valutazione = 'PUBBLICATO' OR stato_valutazione = 'RIFIUTATO') "
		                             + "AND corso = ? AND data = ?";
		    psUpdate = connection.prepareStatement(updateIscrizioni2);
		    psUpdate.setInt(1, idVerbale);
		    psUpdate.setInt(2, corso);
		    psUpdate.setDate(3, data);
		    psUpdate.executeUpdate();

		    connection.commit();
		    return idVerbale;
		} 
		catch (SQLException e) {
		    connection.rollback();
		    throw e; 
		} 
		finally {
			connection.setAutoCommit(true);
			if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
			if (psVerbale != null) try { psVerbale.close(); } catch (SQLException ignore) {}
			if (psUpdate != null) try { psUpdate.close(); } catch (SQLException ignore) {}
		}
	}
	
	
	public boolean isAutorizzato(int corsoID) throws SQLException {
		
		String query = "SELECT 1 FROM Corsi WHERE id = ? AND docente = ?";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, corsoID);
			pstatement.setInt(2, docenteID);
			result = pstatement.executeQuery();
			if (result.next()) {
				return true;
			}
			else {
				return false;
			}
		} 
		finally {
			if (result != null) try { result.close(); } catch (SQLException ignore) {}
			if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
		}
	}
	
	public ArrayList<Studente> getStudentiSenzaVoto (int corsoID, Date dataAppello) throws SQLException {
		ArrayList<Studente> studenti = new ArrayList<>();
		String query = "SELECT s.id AS id, s.matricola AS matricola, u.cognome AS cognome, u.nome AS nome "
				+ "FROM Iscrizioni i JOIN Studenti s ON i.studente = s.id JOIN Utenti u ON s.id = u.id "
				+ "WHERE i.corso = ? AND i.data = ? AND i.stato_valutazione = 'NON_INSERITO'";
		
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setInt(1, corsoID);
			pstatement.setDate(2, dataAppello);
			result = pstatement.executeQuery();
			while (result.next()) {
				Studente s = new Studente();
				s.setID(result.getInt("id"));
				s.setNome(result.getString("nome"));
				s.setCognome(result.getString("cognome"));
				s.setMatricola(result.getString("matricola"));
				studenti.add(s);
			}
		} 
		finally {
			if (result != null) try { result.close(); } catch (SQLException ignore) {}
			if (pstatement != null) try { pstatement.close(); } catch (SQLException ignore) {}
		}
		return studenti;
	}
	
}