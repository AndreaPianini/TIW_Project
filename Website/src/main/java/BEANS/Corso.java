package BEANS;

public class Corso {
	
	private int ID;
	private String nome;
	private int cfu;
	private int docenteID;


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
	
	public int getCfu() {
		return cfu;
	}
	public void setCfu(int cfu) {
		this.cfu = cfu;
	}
	
	public int getDocenteID() {
		return docenteID;
	}
	public void setDocenteID(int docenteID) {
		this.docenteID = docenteID;
	}
}
