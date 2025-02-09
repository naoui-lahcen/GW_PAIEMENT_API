package ma.m2m.gateway.utils;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class DateUtil {


	public static final String FORMAT_DEFAUT = "yyyy-MM-dd";

	public static final String FRENCH_DEFAUT = "dd/MM/yy";

	public static final String FORMAT_DDMMYY = "dd-MM-yy";

	public static final String DF_DD_MM_YYYY_HH_MM = "dd/MM/yy HH:mm";

	public static final String DF_DD_MM_YYYY_A_HH_MM_SS = "dd/MM/yy à HH:mm:ss";
	
	public static final String DF_DD_MM_YY = "ddMMyy";
	
	public static final String DF_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	
	public static final String DF_DD_MM_YYYY = "dd/MM/yyyy";
	
	public static final String DFF_YYYY_MM_DD_HH_MM_SS = "yyyy/MM/dd HH:mm:ss";
	
	public static final String DFF_DD_MM_YYYY = "dd-MM-yyyy";
	

	protected static final String[] MOIS = { "janvier", "février", "mars", "avril", "mai", "juin", "juillet", "août",
			"septembre", "octobre", "novembre", "décembre" };

	protected static final Map<String, Integer> MOIS_BY_NUM_LIST = new HashMap<>();

	public static final long MS_PAR_JOUR = 1000L * 60 * 60 * 24;

	public static final String DATE_SEPARATOR = "/";
	/**
	 * @author pour le calcule des jours entre 2 dates
	 */
	public static final long DAY_IN_MILLIS = 1000L * 60 * 60 * 24;

	private DateUtil() {
	}

	/**
	 * Méthode permettant de récupérer la date courante
	 * 
	 * @return Date
	 */
	public static Date currentDate() {
		SimpleDateFormat formatter = new SimpleDateFormat(DF_DD_MM_YYYY);
		String strDate = formatter.format(new Date());
		return ConvertUtil.stringToDate(strDate);
	}

	public static String currentHour() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		return formatter.format(new Date());
	}

	/**
	 * Méthode permettant de récupérer l'année courant
	 * 
	 * @return int
	 */

	public static int currentYear() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
		return Integer.parseInt(formatter.format(currentDate()));
	}

	public static Date lastDayOfYear() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, currentYear());
		cal.set(Calendar.MONTH, 11);
		cal.set(Calendar.DAY_OF_MONTH, 31);
		return cal.getTime();
	}

	/**
	 * Current year string.
	 *
	 * @return the string
	 */

	public static String currentYearString() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
		return formatter.format(currentDate());
	}

	/**
	 * Méthode permettant de retourner une date du type "dd/mm/yyyy" avec comme
	 * paramètre le jour, le mois et l'année
	 * 
	 * @return Date
	 * @throws ParseException
	 */

	public static Date format(String day, String month, String year) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(DF_DD_MM_YYYY);
		return formatter.parse(day + "/" + month + "/" + year);
	}

	/**
	 * Méthode permettant de retourner une date du type "dd/mm/yyyy" avec comme
	 * paramètre la date sous forme de String, et le pattern
	 * 
	 * @return Date
	 * @throws ParseException
	 */

	public static Date format(String date, String pattern) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.parse(date);
	}

	/**
	 * Méthode retourne vrai si les deux dates correspondent au même jour<br>
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean isSameDay(Date date1, Date date2) {
		String jour1 = ConvertUtil.dateToString(date1, "ddMMyyyy");
		String jour2 = ConvertUtil.dateToString(date2, "ddMMyyyy");
		return jour1.equals(jour2);
	}

	/**
	 * Méthode retourne vrai si la premiere date est un jour avant le date suivante
	 * <br>
	 * 
	 * @param date1
	 * @param date2
	 * @return boolean
	 */
	public static boolean isNextDay(Date date1, Date date2) {
		if (isSameDay(date1, date2)) {
			return false;
		}
		return date1.before(date2);
	}

	/**
	 * Méthode permettant comparer une date par rapport à la date du jour
	 * 
	 * @param date
	 * @return boolean
	 */
	public static boolean isCurrentDay(Date date) {
		return isSameDay(date, new Date());
	}

	/**
	 * Méthode permettant de vérifier si un champ de type String est une date
	 * 
	 * @param data
	 * @param pattern
	 * @return boolean
	 */
	public static boolean isDate(String data, String pattern) {

		if (data == null || "".equals(data)) {
			return false;
		}

		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		Date date = null;
		try {
			date = formatter.parse(data);
		} catch (ParseException e) {
			return false;
		}

		String dateReverse = formatter.format(date);

		return data.equals(dateReverse);
	}

	/**
	 * Méthode permettant convertir une java.util.Date en java.sql.Date
	 * 
	 * @param date
	 * @return java.sql.Date
	 */
	public static java.sql.Date toSqlDate(Date date) {
		return new java.sql.Date(date.getTime());
	}

	/**
	 * Méthode permettant d'ajouter un nombre de jours à une date
	 * 
	 * @param date    de départ
	 * @param nbJours nombre de jours à ajouter
	 * @return date de départ + nbJours
	 */
	public static java.util.Date addJours(Date date, int nbJours) {
		Calendar now = Calendar.getInstance();
		now.setTime(date);
		now.add(Calendar.DAY_OF_YEAR, nbJours);
		return now.getTime();
	}

	/**
	 * Méthode permettant d'ajouter un nombre de mois à une date
	 * 
	 * @param date   de départ
	 * @param nbMois nombre de mois à ajouter
	 * @return date de départ + nbMois
	 */
	public static java.util.Date addMois(Date date, int nbMois) {
		Calendar now = Calendar.getInstance();

		now.setTime(date);
		now.add(Calendar.MONTH, nbMois);
		return now.getTime();
	}

	public static String getLastDay(Integer year, Integer month) {

		GregorianCalendar calendar = new GregorianCalendar();
		month = month - 1;
		calendar.set(year, month, 1);
		int dayInt = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

		return Integer.toString(dayInt);
	}

	/**
	 * Méthode permettant d'obtenir le jour d'une date
	 * 
	 * @param date de départ
	 * @return jour
	 */
	public static int getDay(Date date) {
		Calendar calendar;

		calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Méthode permettant d'obtenir le mois d'une date (janvier = 1, ...)
	 * 
	 * @param date de départ
	 * @return mois
	 */
	public static int getMonth(Date date) {
		Calendar calendar;

		calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * Méthode permettant d'obtenir l'annee d'une date
	 * 
	 * @param date de départ
	 * @return annee
	 */
	public static int getYear(Date date) {
		Calendar calendar;

		calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	public static int getHour(Date date) {
		Calendar calendar;

		calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	public static int getMin(Date date) {
		Calendar calendar;

		calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.MINUTE);
	}

	/**
	 * Méthode de test pour une date en chaîne de caractère avec le format par
	 * défaut
	 * 
	 * @param DateUtil
	 * @return boolean
	 */
	public static boolean isDate(String sdt) {
		return DateUtil.isDate(sdt, FORMAT_DEFAUT);
	}

	public static String formatCalendar(String format, Calendar fDate) {

		if (fDate == null) {
			return "";
		}

		final SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(fDate.getTime());
	}


	public static String formatCalendarDDMMYYHHMMSSConcat(Calendar fDate) {
		return formatCalendar("ddMMyyyyHHmmss", fDate);
	}

	public static String formatDateLong(Date date) {
		DateFormat dateFormatLong = new SimpleDateFormat(DFF_YYYY_MM_DD_HH_MM_SS);
		return dateFormatLong.format(date);
	}

	/**
	 * recupere le nombre de jour entre deux date
	 * 
	 * @author amtarji
	 * @param dateDebut
	 * @param dateFin
	 * @return
	 */
	public static int getNumberOfDays(Date dateDebut, Date dateFin) {
		return (int) ((ConvertUtil.roundDateToLower(dateFin).getTime()
				- ConvertUtil.roundDateToLower(dateDebut).getTime()) / DAY_IN_MILLIS);
	}

	public static Date getDate1970() {
		return ConvertUtil.stringToDate("01/01/1970", FRENCH_DEFAUT);
	}

	public static Date timeOnly(Date date) {
		return ConvertUtil.stringToTime(ConvertUtil.dateToString(date, ConvertUtil.TIME_ONLY));
	}

	public static java.util.Date addAnnes(Date date, int nbrAnnee) {
		Calendar now = Calendar.getInstance();
		now.setTime(date);
		now.add(Calendar.YEAR, nbrAnnee);
		return now.getTime();
	}

	public static java.util.Date addHeures(Date date, int nbrHour) {
		Calendar now = Calendar.getInstance();
		now.setTime(date);
		now.add(Calendar.HOUR, nbrHour);
		return now.getTime();
	}

	/**
	 * Méthode permettant de retourner une date du type "ddMMyy" avec comme
	 * paramètre la date sous format string.
	 * 
	 * @param date    the date
	 * @param pattern the pattern
	 * @return the string
	 * @throws ParseException the parse exception
	 */
	public static String formatDDMMYY(String startDt) throws ParseException {
		DateFormat formatter = new SimpleDateFormat(FORMAT_DEFAUT);
		Date date = formatter.parse(startDt);
		SimpleDateFormat newFormat = new SimpleDateFormat(DF_DD_MM_YY);
		return newFormat.format(date);
	}

	public static String formatHHMMSS(String startDt) throws ParseException {
		DateFormat formatter = new SimpleDateFormat("hh:mm:ss");
		Date date = formatter.parse(startDt);
		SimpleDateFormat newFormat = new SimpleDateFormat("hhmmss");
		return newFormat.format(date);
	}

	public static String formatDDMMYYhhmmss(String startDt) throws ParseException {
		DateFormat formatter = new SimpleDateFormat(DF_YYYY_MM_DD_HH_MM_SS);
		Date date = formatter.parse(startDt);
		SimpleDateFormat newFormat = new SimpleDateFormat(DF_DD_MM_YY);
		return newFormat.format(date);
	}

	public static String formatDDMMYYInverse(String startDt) throws ParseException {
		DateFormat formatter = new SimpleDateFormat(DF_DD_MM_YY);
		Date date = formatter.parse(startDt);
		SimpleDateFormat newFormat = new SimpleDateFormat(DFF_DD_MM_YYYY);
		return newFormat.format(date);
	}

	public static String formatDDMMYYInverseFrench(String startDt) throws ParseException {
		DateFormat formatter = new SimpleDateFormat(DF_DD_MM_YY);
		Date date = formatter.parse(startDt);
		SimpleDateFormat newFormat = new SimpleDateFormat(DF_DD_MM_YYYY);
		return newFormat.format(date);
	}

	public static double dateToJulian(Calendar date) {
		int year = date.get(Calendar.YEAR);
		int month = date.get(Calendar.MONTH) + 1;
		int day = date.get(Calendar.DAY_OF_MONTH);
		int hour = date.get(Calendar.HOUR_OF_DAY);
		int minute = date.get(Calendar.MINUTE);
		int second = date.get(Calendar.SECOND);

		double extra = (100.0 * year) + month - 190002.5;
		return (367.0 * year) - (Math.floor(7.0 * (year + Math.floor((month + 9.0) / 12.0)) / 4.0))
				+ Math.floor((275.0 * month) / 9.0) + day + ((hour + ((minute + (second / 60.0)) / 60.0)) / 24.0)
				+ 1721013.5 - ((0.5 * extra) / Math.abs(extra)) + 0.5;
	}

	public static String juliandate(String date) {

		String dateOut;
		int jour;
		int mois;
		int annee;
		int i;
		int somme;
		int iAn;

		somme = 0;
		mois = Integer.valueOf(date.substring(2, 4));
		annee = Integer.valueOf(date.substring(0, 2));
		jour = Integer.valueOf(date.substring(4, 6));

		iAn = annee % 10;

		for (i = 1; i < mois; i++) {
			if ((i == 1) || (i == 3) || (i == 5) || (i == 7) || (i == 8) || (i == 10) || (i == 12)) {
				somme += 31;
			}
				
			if ((i == 4) || (i == 6) || (i == 9) || (i == 11)) {
				somme += 30;
			}
				
			if (i == 2) {
				if (annee % 4 == 0) {
					somme += 29;
				}else {
					somme += 28;
				}					
			}
		}

		somme += jour;
		dateOut = String.valueOf(iAn).concat(String.valueOf(somme));
		return dateOut;

	}

}
