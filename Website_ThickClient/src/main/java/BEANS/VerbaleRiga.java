package BEANS;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class VerbaleRiga {
	private int id;
    private LocalDateTime data_ora_creaz;
    private int corsoId;
    private String nomeCorso;
    private LocalDate dataAppello;
    
    public int getId()                        { return id; }
    public void setId(int verbaleId)          { this.id = verbaleId; }

    public LocalDateTime getDataOra()       { return data_ora_creaz; }
    public void setDataOra(LocalDateTime t) { this.data_ora_creaz = t; }

    public int getCorsoId()                          { return corsoId; }
    public void setCorsoId(int corsoId)              { this.corsoId = corsoId; }

    public String getNomeCorso()                     { return nomeCorso; }
    public void setNomeCorso(String nomeCorso)       { this.nomeCorso = nomeCorso; }

    public LocalDate getDataAppello()                { return dataAppello; }
    public void setDataAppello(LocalDate d)          { this.dataAppello = d; }

}
