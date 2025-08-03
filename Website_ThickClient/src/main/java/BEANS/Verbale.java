package BEANS;

import java.time.LocalDateTime;

public class Verbale {
	private int id;
	private LocalDateTime data_ora_creaz;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public LocalDateTime getData_Ora() {
		return data_ora_creaz;
	}
	public void setData_Ora(LocalDateTime dataOra) {
		this.data_ora_creaz = dataOra;
	}

}
