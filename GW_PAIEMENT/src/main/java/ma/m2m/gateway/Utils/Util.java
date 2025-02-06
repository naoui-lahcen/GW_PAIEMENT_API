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

import ma.m2m.gateway.dto.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;


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
		return 0;
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

    public static String generateCardToken(String merchantId) {

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
			}
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
}
