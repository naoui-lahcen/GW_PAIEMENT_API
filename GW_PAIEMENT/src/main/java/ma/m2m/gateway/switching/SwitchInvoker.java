package ma.m2m.gateway.switching;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;
import ma.m2m.gateway.utils.Util;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Slf4j
public class SwitchInvoker implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	transient Socket socket;
	transient BufferedReader lire;
	transient PrintWriter ecrire;
	private String trame;

	public String getTrame() {
		return trame;
	}

	public void setTrame(String trame) {
		this.trame = trame;
	}

	public SwitchInvoker() {
		// TODO: Cette méthode est laissée vide à des fins de conception future.
	}

	public String recupererCodereponse(String trameserveur) {
		Vector<TrameTLV> trames = traiterTrame(trameserveur);
		TrameTLV trametlv = new TrameTLV();
		trametlv.setTag("020");
		String code = trames.get(trames.indexOf(trametlv)).getValeur();
		return code;
	}

	public Vector<TrameTLV> traiterTrame(String trame) {
		Vector<TrameTLV> trames = new Vector<TrameTLV>();

		for (int i = 0; i < trame.length(); i++) {
			TrameTLV champs = new TrameTLV();
			champs.setTag(trame.substring(i, i + 3));
			champs.setLongueur(trame.substring(i + 3, i + 6));
			champs.setValeur(trame.substring(i + 6, i + 6 + Integer.parseInt(champs.getLongueur())));
			trames.add(champs);
			i = i + 6 + Integer.parseInt(champs.getLongueur()) - 1;
		}
		return trames;
	}

	@Deprecated
	public String CompleteSize(String champ, int size, String folder, String file) {
		int longueur = champ.length();
		String resultat;
		while (longueur < size) {
			champ = champ + " ";
			longueur++;
		}
		resultat = champ;
		return resultat;
	}

	@Deprecated
	public String formatageCHamps(String chmp, int nbr, String folder, String file) {
		String champs = "";
		// TODO: log.info("Champ----> " + chmp);
		// TODO: Util.writeInFileTransaction(folder, file,
		// TODO: "Champ----> " + chmp);
		for (int i = chmp.length(); i < nbr; i++) {
			champs += "0";
		}
		champs += chmp;
		return champs;
	}

	public static String addLeadingZerosToNumber(int num, int paddingSize) {
		return StringUtils.leftPad(Integer.toString(num), paddingSize, '0');
	}

	public static String padStringWithSpaces(String str, int paddingSize) {
		return StringUtils.rightPad(str, paddingSize, ' ');
	}

	public static String addLeadingZerosToStringNumber(String str, int paddingSize) {
		if (str == null)
			return null;
		StringBuilder strBuilder = new StringBuilder();
		for (int i = str.length(); i < paddingSize; i++)
			strBuilder.append('0');
		return strBuilder.toString().concat(str);
	}

	@SuppressWarnings("all")
	public String recupererValueTag(String trameserveur, String tag, String folder, String file) {
		String code = null;
		try {
			Vector<TrameTLV> trames = traiterTrame(trameserveur);
			TrameTLV trame = new TrameTLV();
			trame.setTag(tag);
			code = trames.get(trames.indexOf(trame)).getValeur();
		} catch (ArrayIndexOutOfBoundsException e) {
			Util.writeInFileTransaction(folder, file,
					"[GW-EXCEPTION-EXCHANGE-SWITCH-RESPONSE-PROCESSING] Processing of the following tag from switch response has failed due to its absence => {"
							+ tag + "}");
		}
		return code;
	}

	public static void main(String[] args) {

	}

}
