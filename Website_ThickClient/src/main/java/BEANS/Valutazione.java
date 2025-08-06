package BEANS;

public class Valutazione {
	
	public  enum StatoValutazione {
		NON_INSERITO, INSERITO, PUBBLICATO, RIFIUTATO, VERBALIZZATO;

		@Override
		public String toString() {
			switch (this) {
				case NON_INSERITO: return "NON INSERITO";
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
	public void setVoto(String voto) throws IllegalArgumentException {
		switch (voto) {
			case null: this.voto = null; break;
			case "18": this.voto = Voto._18; break;
			case "19": this.voto = Voto._19; break;
			case "20": this.voto = Voto._20; break;
			case "21": this.voto = Voto._21; break;
			case "22": this.voto = Voto._22; break;
			case "23": this.voto = Voto._23; break;
			case "24": this.voto = Voto._24; break;
			case "25": this.voto = Voto._25; break;
			case "26": this.voto = Voto._26; break;
			case "27": this.voto = Voto._27; break;
			case "28": this.voto = Voto._28; break;
			case "29": this.voto = Voto._29; break;
			case "30": this.voto = Voto._30; break;
			case "30L": this.voto = Voto._30L; break;
			case "ASSENTE": this.voto = Voto.ASSENTE; break;
			case "RIMANDATO": this.voto = Voto.RIMANDATO; break;
			case "RIPROVATO": this.voto = Voto.RIPROVATO; break;
			default: throw new IllegalArgumentException("Unknown Voto: " + voto);
		}
	}

	public StatoValutazione getStatoValutazione() {
		return statoValutazione;
	}
	public void setStatoValutazione(String statoValutazione) throws IllegalArgumentException {
		switch (statoValutazione) {
			case "NON_INSERITO": this.statoValutazione = StatoValutazione.NON_INSERITO; break;
			case "NON INSERITO": this.statoValutazione = StatoValutazione.NON_INSERITO; break;
			case "INSERITO": this.statoValutazione = StatoValutazione.INSERITO; break;
			case "PUBBLICATO": this.statoValutazione = StatoValutazione.PUBBLICATO; break;
			case "RIFIUTATO": this.statoValutazione = StatoValutazione.RIFIUTATO; break;
			case "VERBALIZZATO": this.statoValutazione = StatoValutazione.VERBALIZZATO; break;
			default: throw new IllegalArgumentException("Unknown StatoValutazione: " + statoValutazione);
		}
	}
	
	public boolean rifiutabile() {
		
		if ( voto == null || statoValutazione == null ) {
			return false;
		}
		if ( voto.ordinal() >= Voto._18.ordinal() && voto.ordinal() <= Voto._30L.ordinal() 
				&& statoValutazione == StatoValutazione.PUBBLICATO ) {
			return true;
		}
		else {
			return false;
		}
		
	}

}