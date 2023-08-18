package ma.m2m.gateway.switching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;
import ma.m2m.gateway.Utils.Util;

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
	}

	// addeb by lnaoui 2023-01-09 prblm time out
	@SuppressWarnings("finally")
	public String recupererTrameFromSwitch(String trameclient, String Host, int port, int readTimeout, String commande,
			String folder, String file) {
		try {
			Util.writeInFileTransaction(folder, file,
					"SwitchInvoker DEBUT recupererTrameFromSwitch commande :" + commande);
			socket = new Socket(Host, port);
			// socket.setSoTimeout(readTimeout);
			socket.setSoTimeout(120000);
			ecrire = new PrintWriter(socket.getOutputStream());

			ecrire.print(trameclient);

			ecrire.flush();

			// socket.shutdownOutput();
			lire = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			trame = (char) lire.read() + "";
			while (lire.ready()) {
				trame += (char) lire.read();
				// Util.writeInFileTransaction(folder, file,
				// "SwitchInvoker FIN recupererTrameFromSwitch commande/readTrame :" + commande
				// + "/" + trame);
			}

			lire.close();
			ecrire.close();
			socket.close();

		} catch (Exception ex) {
			trame = "readTimeout" + trameclient;
			Util.writeInFileTransaction(folder, file, "recupererTrameFromSwitch UnknownHostException commande : "
					+ commande + " " + ex.getMessage() + " " + ex);
		}

		Util.writeInFileTransaction(folder, file,
				"SwitchInvoker FIN recupererTrameFromSwitch commande/Trame :" + commande + "/" + trame);
		return trame;

	}

	@SuppressWarnings("finally")
	public String recupererTrame(String trameclient, String Host, int port, int readTimeout, String folder,
			String file) {
		try {
			socket = new Socket(Host, port);
			socket.setSoTimeout(readTimeout);
			ecrire = new PrintWriter(socket.getOutputStream());

			Util.writeInFileTransaction(folder, file, "******************** DEBUT APPEL SWITCH *********************");
			ecrire.print(trameclient);

			ecrire.flush();

			// socket.shutdownOutput();
			lire = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			trame = (char) lire.read() + "";
			while (lire.ready()) {
				trame += (char) lire.read();
			}

			lire.close();
			ecrire.close();
			socket.close();

			Util.writeInFileTransaction(folder, file,
					"*********************** FIN APPEL SWITCH *************************");

		} catch (UnknownHostException e) {
			Util.writeInFileTransaction(folder, file, e.getMessage() + " " + e);
		} catch (IOException e) {
			// if (socket.getSoTimeout() == 15000) {
			// augmenter timeout à 30 s
			if (socket.getSoTimeout() == 30000) {
				trame = "-1";
				Util.writeInFileTransaction(folder, file, "trame Switch = timeout");
			}
			Util.writeInFileTransaction(folder, file, e.getMessage() + " " + e);
		} finally {
			return trame;
		}
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
		// log.info("Longueur---> " + longueur);
		// Util.writeInFileTransaction(folder, file,
		// "Longueur---> " + longueur);
		String resultat;
		while (longueur < size) {
			champ = champ + " ";
			longueur++;
		}

		resultat = champ;
		// log.info("Resultaaat-----------> " + resultat);
		// Util.writeInFileTransaction(folder, file,
		// "Resultaaat-----------> " + resultat);
		return resultat;
	}

	@Deprecated
	public String formatageCHamps(String chmp, int nbr, String folder, String file) {
		String champs = "";
		// log.info("Champ----> " + chmp);
		// Util.writeInFileTransaction(folder, file,
		// "Champ----> " + chmp);
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
		// log.info(addLeadingZerosToNumber(7, 3));
		// log.info(padStringWithSpaces("000345Z", 10).concat("#"));

		// String trameserveur =
		// "000001300101640171310116553190030010008008021901490090072190149014012000000010065015003504016006310720017006084547066012021308881369018006004168019006953688023001H02000200046021Champ
		// 001 non présent0210027308000620073109800299";
		// String gwTrame2 = "";

		SwitchInvoker acquirerInvoker = new SwitchInvoker();
		// String numAuto = acquirerInvoker.recupererValueTag(trameserveur, "022");
		// System.out.println(numAuto);
	}

}
