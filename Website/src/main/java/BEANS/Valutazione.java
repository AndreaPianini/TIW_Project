package BEANS;

public class Valutazione {
	private String voto;
	private StatoValutazione statoValutazione;


	public String getVoto() {
		return voto;
	}

	public void setVoto(String voto) {
		this.voto = voto;
	}

	public StatoValutazione getStatoValutazione() {
		return statoValutazione;
	}

	public void setStatoValutazione(StatoValutazione statoValutazione) {
		this.statoValutazione = statoValutazione;
	}
	
	public boolean rifiutabile() {
		
		int votoNumerico=0;
		
		try {
			votoNumerico=Integer.parseInt(voto);
		}catch(NumberFormatException e) {
			if(voto.equals("30L")) {
				votoNumerico=31;
			}
		}
		return( votoNumerico>=18 && votoNumerico <=31 && statoValutazione==StatoValutazione.PUBBLICATO );
	}

}


