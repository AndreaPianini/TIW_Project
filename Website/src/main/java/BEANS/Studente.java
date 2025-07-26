package BEANS;

public class Studente extends Utente{
	
	private String matricola;
	private String corsoLaurea;
	
	
	public String getMatricola() {
		return matricola;
	}
	public void setMatricola(String matricola) {
		this.matricola = matricola;
	}
	
	public String getCorsoLaurea() {
		return corsoLaurea;
	}
	public void setCorsoLaurea(String corsoLaurea) {
		this.corsoLaurea = corsoLaurea;
	}
	
	@Override
	public String getRole() {
		return "Studente";
	}
	
}
