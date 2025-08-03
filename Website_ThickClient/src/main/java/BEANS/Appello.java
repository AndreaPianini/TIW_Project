package BEANS;

import java.sql.Date;

public class Appello {
	
	private int corso;
	private Date data;
	
	public int getCorso() {
		return corso;
	}
	public void setCorso(int corsoID) {
		this.corso = corsoID;
	}
	
	
	public Date getData() {
		return data;
	}
	public void setData(Date data) {
		this.data = data;
	}

}
