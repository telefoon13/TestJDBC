// JDBC test By Mike D.
package be.vdab;

import java.io.Serializable;

public class Rekening implements Comparable<Rekening>,Serializable {

	//Variabele
	private static final long serialVersionUID = 1L;
	private final long rekNR;
	
	//Constructor
	public Rekening(long rekNR) {
		if (rekNR < 100_000_000_000L || rekNR > 999_999_999_999L){
			throw new RekeningException("Rekening nummer moet een getal met 12 cijfers zijn");
		} else if (!chekRekNr(rekNR)) {
			throw new RekeningException("Foutief rekening nummer");
		} else {
			this.rekNR = rekNR;
			
		}
	}
	
	//Getter
	public long getRekNR() {
		return rekNR;
	}

	//Methodes
	private boolean chekRekNr(Long reknr) {
		long eerste10 = reknr/100;
		long laaste2 = reknr % 10;
		long controle = eerste10 % 97;
		return controle == laaste2;
	}

	@Override
	public String toString() {
		return "NieuweRekening [rekNR=" + rekNR + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (rekNR ^ (rekNR >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rekening other = (Rekening) obj;
		if (rekNR != other.rekNR)
			return false;
		return true;
	}

	@Override
	public int compareTo(Rekening o) {
		if (this.getRekNR() == o.getRekNR()){
			return 0;
		} else if (this.getRekNR() > o.getRekNR()) {
			return 1;
		} else {
			return -1;
		}
	}
	
	
	
}
