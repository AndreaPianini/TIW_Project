package BEANS;

public class Valutazione {
	
	public  enum StatoValutazione {
		NON_INSERITO, INSERITO, PUBBLICATO, RIFIUTATO, VERBALIZZATO;

		@Override
		public String toString() {
			switch (this) {
				case NON_INSERITO: return "NON_INSERITO";
				case INSERITO: return "INSERITO";
				case PUBBLICATO: return "PUBBLICATO";
				case RIFIUTATO: return "RIFIUTATO";
				case VERBALIZZATO: return "VERBALIZZATO";
				default: return super.toString();
			}
		}
	}
	
	public enum Voto {
		ASSENTE, RIMANDATO, RIPROVATO, _18, _19, _20, _21, _22, _23, _24, _25, _26, _27, _28, _29, _30, _30L;

		@Override
		public String toString() {
			switch (this) {
				case _18: return "18";
				case _19: return "19";
				case _20: return "20";
				case _21: return "21";
				case _22: return "22";
				case _23: return "23";
				case _24: return "24";
				case _25: return "25";
				case _26: return "26";
				case _27: return "27";
				case _28: return "28";
				case _29: return "29";
				case _30: return "30";
				case _30L: return "30L";
				case ASSENTE: return "ASSENTE";
				case RIMANDATO: return "RIMANDATO";
				case RIPROVATO: return "RIPROVATO";
				default: return super.toString();
			}
		}
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