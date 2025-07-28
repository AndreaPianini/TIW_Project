package BEANS;

public class Valutazione {
	
	public  enum StatoValutazione {
		NON_INSERITO, INSERITO, PUBBLICATO, RIFIUTATO, VERBALIZZATO;
	}
	
	public enum Voto {
		ASSENTE, RIMANDATO, RIPROVATO, _18, _19, _20, _21, _22, _23, _24, _25, _26, _27, _28, _29, _30, _30L;
	}
	
	private Voto voto;
	private StatoValutazione statoValutazione;


	public Voto getVoto() {
		return voto;
	}
	public void setVoto(Voto voto) {
		this.voto = voto;
	}

	public StatoValutazione getStatoValutazione() {
		return statoValutazione;
	}
	public void setStatoValutazione(StatoValutazione statoValutazione) {
		this.statoValutazione = statoValutazione;
	}
	
	public boolean rifiutabile() {
		
		if ( voto.ordinal() >= Voto._18.ordinal() && voto.ordinal() <= Voto._30L.ordinal() 
				&& statoValutazione == StatoValutazione.PUBBLICATO ) {
			return true;
		}
		else {
			return false;
		}
		
	}

}


