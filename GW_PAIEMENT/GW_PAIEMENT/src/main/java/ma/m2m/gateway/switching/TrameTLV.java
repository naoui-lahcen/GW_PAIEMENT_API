package ma.m2m.gateway.switching;

import java.io.Serializable;
import java.util.Objects;

public class TrameTLV implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String tag;
	private String longueur;
	private String valeur;

	public TrameTLV() {
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getLongueur() {
		return longueur;
	}

	public void setLongueur(String longueur) {
		this.longueur = longueur;
	}

	public String getValeur() {
		return valeur;
	}

	public void setValeur(String valeur) {
		this.valeur = valeur;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TrameTLV other = (TrameTLV) obj;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(tag, longueur, valeur);
	}

}
