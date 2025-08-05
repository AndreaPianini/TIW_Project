package BEANS;

public abstract class Utente {
	
	private int ID;
	private String nome;
	private String cognome;
	private String email;
	
	
	public int getID() {
		return ID;
	}
	public void setID(int id) {
		this.ID = id;
	}
	
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public String getCognome() {
		return cognome;
	}
	public void setCognome(String cognome) {
		this.cognome = cognome;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public abstract String getRole();
	
}
