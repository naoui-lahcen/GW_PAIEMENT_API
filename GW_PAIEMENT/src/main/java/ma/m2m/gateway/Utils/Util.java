package ma.m2m.gateway.utils;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import ma.m2m.gateway.dto.*;
import ma.m2m.gateway.threedsecure.ThreeDSecureResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.springframework.ui.Model;


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
		public static CardIssuer checkCardIssuer(String card) {
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
		public static CardIssuer checkCardIssuerByIssuerName(String issuerName) {
			for (CardIssuer cc : CardIssuer.values()) {
				if (cc.getIssuerName().equals(issuerName)) {
					return cc;
				}
			}
			return null;
		}
	}
	
	private static final Logger logger = LogManager.getLogger(Util.class);
	public static final String ETOILES = "******";
	public static final String ETOILESS = "*********";
	
	public static final String MERCHANTNAME = "DLOCAL             ";
	
	public static final String FORMAT_DEFAUT = "yyyy-MM-dd";

	//public static final DateFormat dateFormatSimple = new SimpleDateFormat(FORMAT_DEFAUT);

	// Liste des TLD autorisés (à personnaliser si nécessaire)
	private static final String[] VALID_TLDS = {
			"com", "fr", "it", "net", "org", "edu", "gov", "eu", "ma"
	};

	public static boolean isValidEmail(String email) {
		if (email == null || email.isEmpty()) return false;

		// Regex basique pour valider l’email
		String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		if (!Pattern.matches(emailRegex, email)) {
			return false;
		}

		// Extraire le TLD (après le dernier point)
		String[] parts = email.split("\\.");
		String tld = parts[parts.length - 1].toLowerCase();

		// Vérifier si le TLD est dans la liste autorisée
		for (String validTld : VALID_TLDS) {
			if (tld.equals(validTld)) {
				return true;
			}
		}

		return false;
	}

	public static void creatFileTransaction(String input) {

		LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
		String folder = date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));

		String path = "D:/GW_LOGS/" + folder;

		File myObj = new File(path);
		if (myObj.mkdir()) {
			logger.info("======> New folder: {}" , myObj.getName());
		}

		File myfile = new File(path + "/" + input + ".trc");
		try {
			if (myfile.createNewFile()) {
				logger.info("======> New file: {}" , myfile.getName());
			}
		} catch (IOException e) {
			logger.error("======> Creation file error." , e);
		}
	}
	
	@SuppressWarnings("unused") // Indique que certains paramètres ne sont pas utilisés
	public static void writeInFileTransaction(String folder, String file, String input) {
		LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
		String dateTr = date.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
		String formattedFolder = date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		try(FileWriter myWriter = new FileWriter("D:/GW_LOGS/" + formattedFolder + "/" + file + ".trc", true)) {
			myWriter.write(dateTr + "   " + input + System.getProperty("line.separator"));
		} catch (IOException e) {
			logger.error("======> An error occurred.", e);
		}
	}

	public static int isCardValid(final String cardnumber) {
		if ((cardnumber == null) || (cardnumber.length() < 13) || (cardnumber.length() > 19))
			return 1;
		if (!luhnCheck(cardnumber))
			return 2;
		/*if(cardnumber.startsWith("35"))
			return 35;
		if(cardnumber.startsWith("34"))
			return 34;
		if(cardnumber.startsWith("37"))
			return 37;*/
		if (!isVisaOrMastercard(cardnumber)) {
			return 3;  // Visa ou Mastercard
		}
		return 0;
	}

	public static boolean isVisaOrMastercard(String numCarte) {
		if (numCarte == null || numCarte.length() < 4) {
			return false;
		}

		// Nettoyer la chaîne (enlever espaces, tirets éventuels)
		String cleanNum = numCarte.replaceAll("\\s+", "").replaceAll("-", "");

		// Extraire les préfixes
		int prefix1 = Integer.parseInt(cleanNum.substring(0, 1));       // 1 chiffre
		int prefix2 = Integer.parseInt(cleanNum.substring(0, 2));       // 2 chiffres
		int prefix4 = Integer.parseInt(cleanNum.substring(0, 4));       // 4 chiffres

		// Vérifier Visa
		if(prefix1 == 4) {
			return true;
		}

		// Vérifier Mastercard
		else if((prefix2 >= 51 && prefix2 <= 55) || (prefix4 >= 2221 && prefix4 <= 2720)) {
			return true;
		}

		// Vérifier Maestro
		else if((prefix2 == 50) || (prefix2 >= 56 && prefix2 <= 59)) {
			return true;
		}

		return false;
	}

	public static int getCardIss(final String cardnumber) {

		CardIssuer iss = CardIssuer.checkCardIssuer(cardnumber);

		if (iss == null)
			return 0;

		String issName = iss.getIssuerName();
		if (issName.equals("VISA"))
			return 1;
		if (issName.equals("MASTER"))
			return 2;
		if (issName.equals("AMEX"))
			return 3;
		if (issName.equals("Diners"))
			return 4;
		if (issName.equals("DISCOVER"))
			return 5;
		if (issName.equals("JCB"))
			return 6;
		return 0;

	}

	public static int card_switch(String folder, String file, String cardnumber, boolean isNat, String server) {

		Traces traces = new Traces();
		// 0 TO NAPS AUTO 20
		// 1 TO CMI
		// 2 TO VISA or MASTERCARD NATIONAL IUSSER
		// 5 TO VISA or MASTERCARD INTERNATIONAL, uknown

		traces.writeInFileTransaction(folder, file, "card_switch start ...");

		if (!isNat) {
			traces.writeInFileTransaction(folder, file, "is_nat == false checking international orgin");
			int iss_origin = Util.getCardIss(cardnumber);
			// iss_origin
			// 0 unkown
			// VISA 1
			// MASTERCARD 2
			// AMEX
			// Diners
			// DISCOVER
			// JCB

			// if(iss_origin==3 || iss_origin==4 || iss_origin==6) return 5;

			// to improve

			if (iss_origin == 1) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 11;
			}
			if (iss_origin == 2) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 12;
			}
			if (iss_origin == 3) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 13;
			}
			if (iss_origin == 4) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 14;
			}
			if (iss_origin == 5) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 15;
			}
			if (iss_origin == 0) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 16;
			}

			{
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 16;
			} // uknown international

		} else {

			if (server.equalsIgnoreCase("20")) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 0;
			}
			if (server.equalsIgnoreCase("21")) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 1;
			}
			if (server.equalsIgnoreCase("55")) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 2;
			}

			if (server.equalsIgnoreCase("61")) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 3;
			}
			if (server.equalsIgnoreCase("62")) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 4;
			}

			if (server.equalsIgnoreCase("71")) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 5;
			}
			if (server.equalsIgnoreCase("72")) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 6;
			}
			if (server.equalsIgnoreCase("73")) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 7;
			}

			if (server.equalsIgnoreCase("06")) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 8;
			}
			if (server.equalsIgnoreCase("08")) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 9;
			}

			if (server.equalsIgnoreCase("EMPTY")) {
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 10;
			}
			{
				traces.writeInFileTransaction(folder, file, "card_switch END");
				return 10;
			}

		}

	}

	public static boolean luhnCheck(String cardNumber) {
		int sum = 0;
		boolean bFlag = false;
		for (int i = cardNumber.length() - 1; i >= 0; i--) {
			int n = Integer.parseInt(cardNumber.substring(i, i + 1));
			if (bFlag) {
				n *= 2;
				if (n > 9) {
					n = (n % 10) + 1;
				}
			}
			sum += n;
			bFlag = !bFlag;
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
			num.replace(6, 12, ETOILES);
			return num.toString();
		} else if (numCarte.length() == 19) {
			if (num.substring(16, 19).equals("???")) {
				num.delete(16, 19);
				num.replace(6, 12, ETOILES);
				return num.toString();
			} else {
				num.replace(6, 15, ETOILESS);
				return num.toString();
			}

		} else {
			return "";
		}

	}

	public static String getTLVPCIDSS(String tlv, String folder, String file) {
		String TLVPCIDSS = "";
		try {
			if (tlv == null || tlv.length() < 29) {
				throw new IllegalArgumentException("Chaîne trop courte pour contenir une carte");
			}
			String prefix = tlv.substring(0, 13);
			String cardField;
			String suffix;
			boolean hasQuestionMarks = tlv.length() >= 32 && tlv.substring(29, 32).equals("???");

			if (hasQuestionMarks) {
				// Carte + ??? → positions 13 à 32
				cardField = tlv.substring(13, 29); // 16 chiffres
				suffix = tlv.substring(32);        // après les ???
			} else {
				// Carte normale → positions 13 à 29
				cardField = tlv.substring(13, 29); // 16 chiffres
				suffix = tlv.substring(29);        // reste
			}

			String maskedCard = cardField.substring(0, 6) + "******" + cardField.substring(12);
			TLVPCIDSS = prefix + maskedCard + (hasQuestionMarks ? "???" : "") + suffix;

		} catch (Exception e) {
			Util.writeInFileTransaction(folder, file, "[GW-EXCEPTION] getTLVPCIDSS " + formatException(e));
		}

		return TLVPCIDSS;
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
		int tmpSize = 0;

		if (str == null)
			return MERCHANTNAME;
		if (str.length() < 1)
			return MERCHANTNAME;

		String sTmp = "";
		try {
			tmpSize = str.length();
			if (tmpSize <= 19) {
				sTmp = str;
			} else {
				sTmp = str.substring(0, size);
			}

			StringBuilder padded = new StringBuilder(sTmp);
			while (padded.length() < size) {
				padded.append(padChar);
			}
			return padded.toString();

		} catch (Exception e) {
			return MERCHANTNAME;
		}
	}
	@SuppressWarnings("squid:S2245") // Suppression de l'avertissement pour l'utilisation de ThreadLocalRandom
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
			if ((i == 1) || (i == 3) || (i == 5) || (i == 7) || (i == 8) || (i == 10) || (i == 12)) {
				julianSomme += 31;
			}
				
			if ((i == 4) || (i == 6) || (i == 9) || (i == 11)) {
				julianSomme += 30;
			}
				
			if (i == 2) {
				if (annee % 4 == 0) {
					julianSomme += 29;
				}else {
					julianSomme += 28;
				}					
			}				
		}
		julianSomme += jour;

		return String.format("%1d%03d",  annee % 10, julianSomme);
	}
	
	@SuppressWarnings("deprecation")
	public static int generateNumTransaction(String folder, String file, Date date) {
		Traces traces = new Traces();
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int year = calendar.get(Calendar.YEAR);

		String syear = String.format("%04d", year).substring(2);
		
		int msec = calendar.get(Calendar.MILLISECOND);

		int sec = calendar.get(Calendar.SECOND);

		String formsdd = "%s%d%d";
		int Seq = 0;
		if (String.format(formsdd, syear, msec, sec) != null && !String.format(formsdd, syear, msec, sec).isEmpty()
				&& String.format(formsdd, syear, msec, sec).length() >= 6) {
			traces.writeInFileTransaction(folder, file, "String.format---> NumSequ: " + String.format(formsdd, syear, msec, sec));
			Seq = Integer.parseInt(String.format(formsdd, syear, msec, sec).substring(0, 6));
		} else if (String.format(formsdd, syear, msec, sec) != null
				&& !String.format(formsdd, syear, msec, sec).isEmpty()
				&& String.format(formsdd, syear, msec, sec).length() < 6) {
			traces.writeInFileTransaction(folder, file, "String.format---> NumSequ: " + String.format(formsdd, syear, msec, sec));
			Seq = Integer.parseInt(String.format(formsdd, syear, msec, sec).substring(0,
					String.format(formsdd, syear, msec, sec).length()));
		}
		return Seq;

	}

	public static String formatNumTrans(String numTrans) {
		if (numTrans == null) {
			return "000000";
		}
		// Complète à gauche avec des zéros pour obtenir une longueur de 6
		return String.format("%6s", numTrans).replace(' ', '0');
	}

	public static String displayCard(String numCarte) {

		if (numCarte == null || numCarte.isEmpty()) {

			return null;

		}

		numCarte = numCarte.trim();

		StringBuilder num = new StringBuilder(numCarte);

		if (numCarte.length() == 16) {

			num.replace(6, 12, ETOILES);

			return num.toString();

		} else if (numCarte.length() == 19) {

			if ("???".equals(num.substring(16, 19))) {

				num.delete(16, 19);

				num.replace(6, 12, ETOILES);

				return num.toString();

			} else {

				num.replace(6, 15, ETOILESS);

				return num.toString();

			}

		} else {

			return null;

		}

	}
	@SuppressWarnings("squid:S2245") // Suppression intentionnelle : La sécurité cryptographique n'est pas requise ici
	public static String genTokenCom(String numcommande, String merchantId) {

		int length = 5;
		boolean useLetters = true;
		boolean useNumbers = true;
		
		String generatedString1 = RandomStringUtils.random(length, useLetters, useNumbers);
		String generatedString2 = RandomStringUtils.random(length, useLetters, useNumbers);

        return (generatedString1 + numcommande + generatedString2 + merchantId).toUpperCase();
	}
	
	@SuppressWarnings("squid:S2245") // Suppression intentionnelle : La sécurité cryptographique n'est pas requise ici
	public static String genCommande(String merchantId) {

		int length = 5;
		boolean useLetters = true;
		boolean useNumbers = true;
		String generatedString1 = RandomStringUtils.random(length, useLetters, useNumbers);
		String generatedString2 = RandomStringUtils.random(length, useLetters, useNumbers);

        return (generatedString1 + generatedString2 + merchantId).toUpperCase();
	}

    public static String generateCardToken(String idClient) {

    	  DateFormat dateFormat = new SimpleDateFormat("yyddMMHHmmss");
    	
          Calendar dateCalendar = Calendar.getInstance();
          
          String StrSysDate = dateFormat.format(dateCalendar.getTime());

          UUID randomUUID = UUID.randomUUID();

          String randomtokenCard = randomUUID.toString().replace("-", "").substring(0, 4);

        return (StrSysDate+randomtokenCard).toUpperCase();
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
	
    public static String convertCVVNumericToAlphabetic(String cvvNumeric) {
        StringBuilder convertedCVV = new StringBuilder();

        for (int i = 0; i < cvvNumeric.length(); i++) {
            char digitChar = cvvNumeric.charAt(i);
            if (Character.isDigit(digitChar)) {
                int digit = Character.getNumericValue(digitChar);
                if (digit >= 0 && digit <= 9) {
                    char letter = (char) ('A' + digit);
                    convertedCVV.append(letter);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        return convertedCVV.toString();
    }

    public static String convertCVVAlphabeticToNumeric(String cvvAlphabetic) {
        StringBuilder convertedCVV = new StringBuilder();

        for (int i = 0; i < cvvAlphabetic.length(); i++) {
            char letter = cvvAlphabetic.charAt(i);
            if (Character.isLetter(letter)) {
                int digit = letter - 'A';
                convertedCVV.append(digit);
            } else {
                return null;
            }
        }

        return convertedCVV.toString();
    }
    
	public static void formatAmount(DemandePaiementDto demandeDto) {
	    DecimalFormat df = new DecimalFormat("0.00");
	    String formattedAmount = df.format(demandeDto.getMontant()).replace(",", ".");
	    demandeDto.setMontantStr(formattedAmount);
	}
	
	public static String sanitizeAmount(String amount) {
	    if (amount == null || amount.isEmpty()) {
	        amount = "0.00";
	    }
	    if (amount.contains(",")) {
	        amount = amount.replace(",", ".");
	    }
	    return amount;
	}
	
	public static void formatDateExp(String expirationDate, Cartes carte, String folder, String file) {
		try {
			Traces traces = new Traces();
			DateFormat dateFormatSimple = new SimpleDateFormat(FORMAT_DEFAUT);
			LocalDate localDate = LocalDate.parse(expirationDate);
			Month mois = localDate.getMonth();
			Integer year = localDate.getYear();
			carte.setYear(year);
			String moisStr = String.format("%s", mois);
			List<String> list = new ArrayList<>();
			list.add(moisStr);
			MonthDto month = mapToFrenchMonth(moisStr);
			carte.setMois(month.getMonth());
			carte.setMoisValue(month.getValue());
			
			Calendar dateCalendar = Calendar.getInstance();
			Date dateToken = dateCalendar.getTime();

			String expirydateFormated = carte.getYear() + "-" + carte.getMoisValue() + "-" + "01";
			traces.writeInFileTransaction(folder, file,
					"cardtokenDto expirydate formated : " + expirydateFormated);
			Date dateExp = dateFormatSimple.parse(expirydateFormated);
			if(dateExp.before(dateToken)) {
				traces.writeInFileTransaction(folder, file, "date exiration est inferieur à l adate systeme : " + dateExp + " < " + dateToken);
				carte.setMoisValue("xxxx");
				carte.setMois("xxxx");
				carte.setYear(1111);
				carte.setExpired(true);
			}
			traces.writeInFileTransaction(folder, file,"isExpired : " + carte.isExpired());
		} catch (Exception e) {
			logger.error("" ,e);
		}
	}
	
	@SuppressWarnings("all")
	public static MonthDto mapToFrenchMonth(String month) {

		MonthDto exp = new MonthDto();
		if (month.equals("JANUARY")) {
			month = "Janvier";
			exp.setMonth(month);
			exp.setValue("01");
		} else if (month.toString().equals("FEBRUARY")) {
			month = "Février";
			exp.setMonth(month);
			exp.setValue("02");
		} else if (month.toString().equals("MARCH")) {
			month = "Mars";
			exp.setMonth(month);
			exp.setValue("03");
		} else if (month.toString().equals("APRIL")) {
			month = "Avril";
			exp.setMonth(month);
			exp.setValue("04");
		} else if (month.toString().equals("MAY")) {
			month = "Mai";
			exp.setMonth(month);
			exp.setValue("05");
		} else if (month.toString().equals("JUNE")) {
			month = "Juin";
			exp.setMonth(month);
			exp.setValue("06");
		} else if (month.toString().equals("JULY")) {
			month = "Juillet";
			exp.setMonth(month);
			exp.setValue("07");
		} else if (month.toString().equals("AUGUST")) {
			month = "Aout";
			exp.setMonth(month);
			exp.setValue("08");
		} else if (month.toString().equals("SEPTEMBER")) {
			month = "Septembre";
			exp.setMonth(month);
			exp.setValue("09");
		} else if (month.toString().equals("OCTOBER")) {
			month = "Octobre";
			exp.setMonth(month);
			exp.setValue("10");
		} else if (month.toString().equals("NOVEMBER")) {
			month = "Novembre";
			exp.setMonth(month);
			exp.setValue("11");
		} else if (month.toString().equals("DECEMBER")) {
			month = "Décembre";
			exp.setMonth(month);
			exp.setValue("12");
		}

		return exp;
	}

	@SuppressWarnings("all")
	public static String getMsgError(String folder, String file, LinkRequestDto linkRequestDto, String msg, String coderep) {
		Traces traces = new Traces();
		traces.writeInFileTransaction(folder, file, "*********** Start getMsgError() ************** ");
		logger.info("*********** Start getMsgError() ************** ");

		JSONObject jso = new JSONObject();
		if (linkRequestDto != null) {
			jso.put("orderid", linkRequestDto.getOrderid() == null ? "" : linkRequestDto.getOrderid());
			jso.put("merchantid", linkRequestDto.getMerchantid() == null ? "" : linkRequestDto.getMerchantid());
			jso.put("amount", linkRequestDto.getAmount() == null ? "" : linkRequestDto.getAmount());
		}
		jso.put("statuscode", coderep == null ? "17" : coderep);
		jso.put("status", msg);
		jso.put("etataut", "N");

		traces.writeInFileTransaction(folder, file, "json : " + jso.toString());
		logger.info("json : " + jso.toString());

		traces.writeInFileTransaction(folder, file, "*********** End getMsgError() ************** ");
		logger.info("*********** End getMsgError() ************** ");
		return jso.toString();
	}

	@SuppressWarnings("all")
	public static String  getMsgErrorV2(String folder, String file, TransactionRequestDto trsRequestDto, String msg, String coderep) {
		String rep = "";
		LinkRequestDto linkRequestDto = new LinkRequestDto();
		if (trsRequestDto != null) {
			linkRequestDto.setOrderid(trsRequestDto.getOrderid());
			linkRequestDto.setMerchantid(trsRequestDto.getMerchantid());
			linkRequestDto.setAmount(trsRequestDto.getAmount());
		} else {
			linkRequestDto.setOrderid(null);
			linkRequestDto.setMerchantid(null);
			linkRequestDto.setAmount(null);
		}

		rep = getMsgError(folder,file,linkRequestDto,msg,coderep);
		return rep;
	}

	@SuppressWarnings("all")
	public static String formatMontantTrame(String folder, String file, String amount, String orderid, String merchantid,
									  LinkRequestDto linkRequestDto) {
		Traces traces = new Traces();
		String montanttrame = "";
		String[] mm;
		String[] m;
		try {
			if (amount.contains(",")) {
				amount = amount.replace(",", ".");
			}
			if (!amount.contains(".") && !amount.contains(",")) {
				amount = amount + "." + "00";
			}
			traces.writeInFileTransaction(folder,  file,"montant : [" + amount + "]");

			String montantt = amount + "";

			mm = montantt.split("\\.");
			if (mm[1].length() == 1) {
				montanttrame = amount + "0";
			} else {
				montanttrame = amount + "";
			}

			m = montanttrame.split("\\.");
			if (m[1].equals("0")) {
				montanttrame = montanttrame.replace(".", "0");
			} else
				montanttrame = montanttrame.replace(".", "");
			montanttrame = formatageCHamps(montanttrame, 12);
		} catch (Exception err3) {
			traces.writeInFileTransaction(folder,  file,
					"authorization 500 Error during  amount formatting for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + formatException(err3));

			return getMsgError(folder, file, linkRequestDto, "authorization 500 Error during  amount formatting", null);
		}
		return montanttrame;
	}

	public static String formatException(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		String stackTrace = "";
		try {
			ex.printStackTrace(pw);
			stackTrace = sw.toString();
		} catch(Exception e) {
			stackTrace = "";
		}
		return stackTrace;
	}


	@SuppressWarnings("all")
	public static String formatMontantRechargeTrame(String folder, String file, String amount, String orderid, String merchantid,
											  DemandePaiementDto dmd, String page, Model model) {
		Traces traces = new Traces();
		String montantRechgtrame;
		String[] mm;
		String[] m;
		DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
		try {
			montantRechgtrame = "";

			String amount1 = calculMontantSansOperation(dmd);

			if (amount1.contains(",")) {
				amount1 = amount1.replace(",", ".");
			}
			if (!amount1.contains(".") && !amount1.contains(",")) {
				amount1 = amount1 + "." + "00";
			}
			traces.writeInFileTransaction(folder, file,
					"montant recharge sans frais : [" + amount1 + "]");

			String montantt = amount1 + "";

			mm = montantt.split("\\.");
			if (mm[1].length() == 1) {
				montantRechgtrame = amount1 + "0";
			} else {
				montantRechgtrame = amount1 + "";
			}

			m = montantRechgtrame.split("\\.");
			if (m[1].equals("0")) {
				montantRechgtrame = montantRechgtrame.replace(".", "0");
			} else
				montantRechgtrame = montantRechgtrame.replace(".", "");
			montantRechgtrame = Util.formatageCHamps(montantRechgtrame, 12);
			traces.writeInFileTransaction(folder, file,
					"montantRechgtrame sans frais : [" + montantRechgtrame + "]");
		} catch (Exception err3) {
			traces.writeInFileTransaction(folder, file,
					"recharger 500 Error during  amount formatting for given orderid:[" + orderid
							+ "] and merchantid:[" + merchantid + "]" + Util.formatException(err3));
			demandeDtoMsg.setMsgRefus("Erreur lors du formatage du montant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			page = "result";
			return page;
		}
		return montantRechgtrame;
	}

	@SuppressWarnings("all")
	public static String formatMontantTrame(String folder, String file, String amount, String orderid, String merchantid,
									  DemandePaiementDto dmd, Model model) {
		Traces traces = new Traces();
		String montanttrame = "";
		String[] mm;
		String[] m;
		DemandePaiementDto demandeDtoMsg = new DemandePaiementDto();
		try {
			amount = calculMontantTotalOperation(dmd);

			if (amount.contains(",")) {
				amount = amount.replace(",", ".");
			}
			if (!amount.contains(".") && !amount.contains(",")) {
				amount = amount + "." + "00";
			}
			//logger.info("montant recharge avec frais : [" + amount + "]");
			traces.writeInFileTransaction(folder, file,
					"montant recharge avec frais : [" + amount + "]");

			String montantt = amount + "";

			mm = montantt.split("\\.");
			if (mm[1].length() == 1) {
				montanttrame = amount + "0";
			} else {
				montanttrame = amount + "";
			}

			m = montanttrame.split("\\.");
			if (m[1].equals("0")) {
				montanttrame = montanttrame.replace(".", "0");
			} else
				montanttrame = montanttrame.replace(".", "");
			montanttrame = Util.formatageCHamps(montanttrame, 12);
		} catch (Exception err3) {
			traces.writeInFileTransaction(folder, file,
					"authorization 500 Error during  amount formatting for given orderid:["
							+ orderid + "] and merchantid:[" + merchantid + "]" + Util.formatException(err3));
			demandeDtoMsg.setMsgRefus("Erreur lors du formatage du montant");
			model.addAttribute("demandeDto", demandeDtoMsg);
			String page0 = "result";
			traces.writeInFileTransaction(folder, file, "Fin processRequestMobile ()");
			logger.info("Fin processRequestMobile ()");
			return page0;
		}
		return montanttrame;
	}

	public static String calculMontantTotalOperation(DemandePaiementDto dto) {
		if (dto.getMontant() == null) {
			dto.setMontant(0.00);
		}
		if (dto.getFrais() == null) {
			dto.setFrais(0.00);
		}
		double mnttotalopp = dto.getMontant() + dto.getFrais();
		return String.format("%.2f", mnttotalopp).replace(",", ".");
	}

	public static String calculMontantSansOperation(DemandePaiementDto dto) {
		if (dto.getMontant() == null) {
			dto.setMontant(0.00);
		}
		return String.format("%.2f", dto.getMontant()).replace(",", ".");
	}

	public static String getHtmlCreqFrompArs(ThreeDSecureResponse threeDSecureResponse, String folder, String file) {
		Traces traces = new Traces();
		JsonObject pGcqOb = new JsonObject();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String threeDSSessionData = "";
		boolean threeDSSDb=false;
		String acsURL = "";
		String htmlCreq = "";
		try {
			pGcqOb.remove("messageType");
			pGcqOb.addProperty("messageType", "CReq");
			pGcqOb.addProperty("messageVersion", threeDSecureResponse.getMessageVersion());
			pGcqOb.addProperty("threeDSServerTransID", threeDSecureResponse.getThreeDSServerTransID());
			pGcqOb.addProperty("acsTransID", threeDSecureResponse.getAcsTransID());
			pGcqOb.addProperty("challengeWindowSize", "05");
			String creqJson = gson.toJson(pGcqOb);
			if(pGcqOb.has("threeDSSessionData")){
				threeDSSessionData= pGcqOb.get("threeDSSessionData").getAsString();
				threeDSSDb = true;

			}
			//String creqJson = pGcqJS;
			byte[] encodedCreq = Base64.encodeBase64(creqJson.getBytes());
			String encodedCreq64Str = new String(encodedCreq);
			traces.writeInFileTransaction(folder, file, "encodedCreq64Str: " + encodedCreq64Str);
			System.out.println(encodedCreq64Str);

			byte[] encodedCreqbase64URL = Base64.encodeBase64URLSafe(creqJson.getBytes("UTF-8"));
			String encodedCreq64URLStr = new String(encodedCreqbase64URL, "UTF-8");
			traces.writeInFileTransaction(folder, file, "encodedCreq64URLStr: " + encodedCreq64URLStr);
			System.out.println(encodedCreq64URLStr);
			acsURL = threeDSecureResponse.getAcsURL();

			// commenter pour les tests recertification 2.2.0
			//htmlCreq = "<form  action=\'" + acsURL + "\' method=\'post\' enctype=\'application/x-www-form-urlencoded\'><input type=\'hidden\' name=\'creq\' value=\'" + new String(encodedCreq) + "\' />";

			htmlCreq = "<form  action=\'" + acsURL + "\' method=\'post\' enctype=\'application/x-www-form-urlencoded\'><input type=\'hidden\' name=\'creq\' value=\'" + encodedCreq64URLStr + "\' />";

			if(threeDSSDb){
				htmlCreq += "<input type=\'hidden\' name=\'threeDSSessionData\' value=\'b\'"+ threeDSSessionData +"\'\' />";
			}
			htmlCreq+="</form>";

		} catch(Exception ex) {
			traces.writeInFileTransaction(folder, file, "Exception: " + formatException(ex));
		}


		return htmlCreq;
	}
}
