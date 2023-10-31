package ma.m2m.gateway.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
// import org.apache.commons.io.FileUtils;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Slf4j
public class Util {

	public enum CardIssuer {
		VISA("^4[0-9]{12}(?:[0-9]{3})?$", "VISA"),
		MASTERCARD("^(?:5[1-7][0-9]{2}|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[01][0-9]|2720)[0-9]{12}$", "MASTER"),
		MAESTRO("^(5018|5020|5038|6304|6759|6761|6763)[0-9]{8,15}$", "Maestro"), AMEX("^3[47][0-9]{13}$", "AMEX"),
		DINERS("^3(?:0[0-5]|[68][0-9])[0-9]{11}$", "Diners"), DISCOVER("^6(?:011|5[0-9]{2})[0-9]{12}$", "DISCOVER"),
		JCB("^(?:2131|1800|35\\d{3})\\d{11}$", "JCB");

		private String regex;
		private String issuerName;

		CardIssuer(String regex, String issuerName) {
			this.regex = regex;
			this.issuerName = issuerName;
		}

		public boolean matches(String card) {
			return card.matches(this.regex);
		}

		public String getIssuerName() {
			return this.issuerName;
		}

		/**
		 * get an enum from a card number
		 * 
		 * @param card
		 * @return
		 */
		public static CardIssuer checkcard_issuer(String card) {
			for (CardIssuer cc : CardIssuer.values()) {
				if (cc.matches(card)) {
					return cc;
				}
			}
			return null;
		}

		/**
		 * get an enum from an issuerName
		 * 
		 * @param issuerName
		 * @return
		 */
		public static CardIssuer checkcard_issuerByIssuerName(String issuerName) {
			for (CardIssuer cc : CardIssuer.values()) {
				if (cc.getIssuerName().equals(issuerName)) {
					return cc;
				}
			}
			return null;
		}
	}

	// fonction de creation dossier de jour et le fichier trace par transaction
	public static void creatFileTransaction(String input) {

		LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
		String folder = date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));

		String path = "D:/GW_LOGS/" + folder;

		File myObj = new File(path);
		if (myObj.mkdir()) {
			// log.info("======> New folder: " + myObj.getName());
		} else {
			// log.info("======> Folder already exists.");
		}

		File myfile = new File(path + "/" + input + ".trc");
		try {
			if (myfile.createNewFile()) {
				// log.info("======> New file: " + myfile.getName());
			} else {
				// log.info("======> File already exists.");
			}
		} catch (IOException e) {
			// log.error("======> Creation file error. ", e);
		}
	}

	// fonction d'ecrire dans le fichier trace
	public static void writeInFileTransaction(String folder, String file, String input) {
		LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
		String dateTr = date.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
		// 2023-10-06 
		// traces vide dans le fichier d'aujourdh'ui dans le dossier ddMMyyyy et par contre il trace dans le dossier (dd-1MMyyyy) correctement
		// corerection initiation du dossier dans chaque trace
		folder = date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		try {

			FileWriter myWriter = new FileWriter("D:/GW_LOGS/" + folder + "/" + file + ".trc", true);

			myWriter.write(dateTr + "   " + input + System.getProperty("line.separator"));

			myWriter.close();
		} catch (IOException e) {
			System.out.println("======> An error occurred. ");
			e.printStackTrace();
		}
	}

	// For PADSS 3.2
	public static void save_dmd_3(String cvv3, String iddmd, String idcmd, String idcomid) {

		char[] acvv = new char[3];
		acvv[0] = cvv3.charAt(0);
		acvv[1] = cvv3.charAt(1);
		acvv[2] = cvv3.charAt(2);
		String text = new String(acvv);
		BufferedWriter bw;
		try {
			String n_file = "D:/transactions/dmdpay_" + iddmd + "_" + idcmd + "_" + idcomid + ".trc";
			FileWriter fw = new FileWriter(n_file);
			bw = new BufferedWriter(fw);

			bw.write(text);
			bw.close();
			fw.close();
		} catch (Exception e) {
			System.out.println("save_dmd_3 ======> An error occurred. ");
			e.printStackTrace();
		}

	}

	public static String read_dmd_3(String iddmd, String idcmd, String idcomid) {

		String cvv3 = null;
		try {
			String n_file = "D:/transactions/dmdpay_" + iddmd + "_" + idcmd + "_" + idcomid + ".trc";
			FileReader freader = new FileReader(n_file);
			BufferedReader bf = new BufferedReader(freader);
			String ln = bf.readLine();
			if (ln != null)
				cvv3 = ln.trim();
			bf.close();
			freader.close();

		} catch (Exception e) {
			System.out.println("read_dmd_3 ======> An error occurred. ");
			e.printStackTrace();
		}
		return cvv3;

	}

	public static void clean_dmd_3(String iddmd, String idcmd, String idcomid) {

//		try {
//			String n_file = "D:\\transactions\\dmdpay_" + iddmd + "_" + idcmd + "_" + idcomid + ".trc";
//			File file_trs = new File(n_file);
//			boolean isFileDelete = FileUtils.deleteQuietly(file_trs);
//			System.out.println("isFileDelete :[" + isFileDelete + " [" + n_file + "]");
//
//		} catch (Exception e) {
//			System.out.println("clean_dmd_3 ======> An error occurred. ");
//			e.printStackTrace();
//		}

	}

	public static int isCardValid(final String cardnumber) {
		if ((cardnumber == null) || (cardnumber.length() < 13) || (cardnumber.length() > 19))
			return 1;
		if (!luhnCheck(cardnumber))
			return 2;
		return 0;
	}

	public static int getCardIss(final String cardnumber) {

		CardIssuer iss = CardIssuer.checkcard_issuer(cardnumber);

		if (iss == null)
			return 0;

		String iss_name = iss.getIssuerName();
		if (iss_name.equals("VISA"))
			return 1;
		if (iss_name.equals("MASTER"))
			return 2;
		if (iss_name.equals("AMEX"))
			return 3;
		if (iss_name.equals("Diners"))
			return 4;
		if (iss_name.equals("DISCOVER"))
			return 5;
		if (iss_name.equals("JCB"))
			return 6;
		return 0;

	}

	public static boolean luhnCheck(String cardNumber) {
		int sum = 0;
		boolean b_flag = false;
		for (int i = cardNumber.length() - 1; i >= 0; i--) {
			int n = Integer.parseInt(cardNumber.substring(i, i + 1));
			if (b_flag) {
				n *= 2;
				if (n > 9) {
					n = (n % 10) + 1;
				}
			}
			sum += n;
			b_flag = !b_flag;
		}
		return (sum % 10 == 0);

	}

	public static String formatagePan(String pan) {
		String formattedPan = "";
		if (pan.length() == 16)
			formattedPan = pan.concat("???");
		if (pan.length() == 17)
			formattedPan = pan.concat("??");
		if (pan.length() == 18)
			formattedPan = pan.concat("?");
		if (pan.length() == 19)
			formattedPan = pan;
		return formattedPan;
	}

	public static String formatCard(String numCarte) {
		if (numCarte == null || numCarte.isEmpty()) {
			return "";
		}
		numCarte = numCarte.trim();
		StringBuilder num = new StringBuilder(numCarte);

		if (numCarte.length() == 16) {
			num.replace(6, 12, "******");
			return num.toString();
		} else if (numCarte.length() == 19) {
			if (num.substring(16, 19).equals("???")) {
				num.delete(16, 19);
				num.replace(6, 12, "******");
				return num.toString();
			} else {
				num.replace(6, 15, "*********");
				return num.toString();
			}

		} else {
			return "";
		}

	}
	public static String formatageCHamps(String chmp, int nbr) {
		String champs = "";

		for (int i = chmp.length(); i < nbr; i++) {
			champs += "0";
		}
		champs += chmp;
		return champs;
	}
	
	public static int convertToJulian(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int year = calendar.get(Calendar.YEAR);
		String syear = String.format("%04d", year).substring(2);
		int century = Integer.parseInt(String.valueOf(((year / 100) + 1)).substring(1));
		int julian = Integer.parseInt(String.format("%d%s%03d", century, syear, calendar.get(Calendar.DAY_OF_YEAR)));
		return julian;
	}
	
	public static String pad_merchant(String str, int size, char padChar) {
		int tmp_size = 0;

		if (str == null)
			return "DLOCAL             "; // FIXPACK12082022
		if (str.length() < 1)
			return "DLOCAL             ";

		String s_tmp = "";
		try {
			tmp_size = str.length();
			if (tmp_size <= 19) {
				s_tmp = str;
			} else {
				s_tmp = str.substring(0, size);
			} // FIXPACK29082022

			StringBuilder padded = new StringBuilder(s_tmp);
			while (padded.length() < size) {
				padded.append(padChar);
			}
			return padded.toString();

		} catch (Exception e) {
			return "DLOCAL             ";
		}
	}
	
	public static String getGeneratedRRN() {
		return Util.getDateNowInJulianSIDFormat()
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH"))
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("mm"))
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("ss"))
				+ String.format("%02d", ThreadLocalRandom.current().nextInt(1, 100));
	}
	
	public static String getDateNowInJulianSIDFormat() {
		int jour = LocalDate.now().getDayOfMonth();
		int mois = LocalDate.now().getMonthValue();
		int annee = LocalDate.now().getYear() % 100;

		int i, julianSomme;
		for (i = 1, julianSomme = 0; i < mois; i++) {
			if ((i == 1) || (i == 3) || (i == 5) || (i == 7) || (i == 8) || (i == 10) || (i == 12))
				julianSomme += 31;
			if ((i == 4) || (i == 6) || (i == 9) || (i == 11))
				julianSomme += 30;
			if (i == 2)
				if (annee % 4 == 0)
					julianSomme += 29;
				else
					julianSomme += 28;
		}
		julianSomme += jour;

		return String.format("%1d%03d",  annee % 10, julianSomme);
	}
	public static int generateNumTransaction(String folder, String file, Date date) {
		Traces traces = new Traces();
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int year = calendar.get(Calendar.YEAR);

		String syear = String.format("%04d", year).substring(2);
		traces.writeInFileTransaction(folder, file, "syear---> NumSequ: " + syear);

		int msec = calendar.get(Calendar.MILLISECOND);
		traces.writeInFileTransaction(folder, file, "msec---> NumSequ: " + msec);

		int sec = calendar.get(Calendar.SECOND);
		traces.writeInFileTransaction(folder, file, "sec---> NumSequ: " + sec);

		// Random randomGenerator = new Random();
		// int randomInt = randomGenerator.nextInt(99);
		String formatDateS = String.format("%s%d%d", syear, msec, sec);
		traces.writeInFileTransaction(folder, file, "formatDateS---> NumSequ: " + sec);
		int Seq = 0;
		if (String.format("%s%d%d", syear, msec, sec) != null && !String.format("%s%d%d", syear, msec, sec).equals("")
				&& String.format("%s%d%d", syear, msec, sec).length() >= 6) {
			traces.writeInFileTransaction(folder, file, "String.format---> NumSequ: " + String.format("%s%d%d", syear, msec, sec));
			Seq = Integer.parseInt(String.format("%s%d%d", syear, msec, sec).substring(0, 6));
		} else if (String.format("%s%d%d", syear, msec, sec) != null
				&& !String.format("%s%d%d", syear, msec, sec).equals("")
				&& String.format("%s%d%d", syear, msec, sec).length() < 6) {
			traces.writeInFileTransaction(folder, file, "String.format---> NumSequ: " + String.format("%s%d%d", syear, msec, sec));
			Seq = Integer.parseInt(String.format("%s%d%d", syear, msec, sec).substring(0,
					String.format("%s%d%d", syear, msec, sec).length()));
		}
		return Seq;

	}
	// display carte pcidss
	public static String displayCard(String numCarte) {

		if (numCarte == null || numCarte.isEmpty()) {

			return null;

		}

		numCarte = numCarte.trim();

		StringBuilder num = new StringBuilder(numCarte);

		if (numCarte.length() == 16) {

			num.replace(6, 12, "******");

			return num.toString();

		} else if (numCarte.length() == 19) {

			if ("???".equals(num.substring(16, 19))) {

				num.delete(16, 19);

				num.replace(6, 12, "******");

				return num.toString();

			} else {

				num.replace(6, 15, "*********");

				return num.toString();

			}

		} else {

			return null;

		}

	}

	public static String genTokenCom(String numcommande, String merchant_id) {

		int length = 5;
		boolean useLetters = true;
		boolean useNumbers = true;
		String generatedString1 = RandomStringUtils.random(length, useLetters, useNumbers);
		String generatedString2 = RandomStringUtils.random(length, useLetters, useNumbers);

		String token = (generatedString1 + numcommande + generatedString2 + merchant_id).toUpperCase();

		return token;

	}

    public static String generateCardToken(String merchant_id) {

    	  DateFormat dateFormat = new SimpleDateFormat("yyddMMHHmmss");
    	
          Calendar dateCalendar = Calendar.getInstance();
          
          String StrSysDate = dateFormat.format(dateCalendar.getTime());

          UUID randomUUID = UUID.randomUUID();

          String randomtokenCard = randomUUID.toString().replaceAll("-", "").substring(0, 4);
          
          String tokenCard = (StrSysDate+randomtokenCard).toUpperCase();
          

          return tokenCard;

    }


	public static String hachInMD5(String str) {
		String strMD5Hash = "";
		try {
			MessageDigest messageDigestMD5 = MessageDigest.getInstance("MD5");
			messageDigestMD5.update(str.getBytes(), 0, str.length());
			strMD5Hash = new BigInteger(1, messageDigestMD5.digest()).toString(16);
		} catch (Exception e) {
			strMD5Hash = "-1";
		}
		return strMD5Hash;
	}

	public enum CODEREP {
		OK("00"),
		KO("96");

		 public final String label;

		private CODEREP(String label) {
		        this.label = label;
		}
		
		 public static CODEREP findCODEREPByCode(String label) {
		        for (CODEREP code : CODEREP.values()) {
		            if (code.label.equals(label)) {
		                return code;
		            }
		        }
		        return null;
		    }

	}
}
