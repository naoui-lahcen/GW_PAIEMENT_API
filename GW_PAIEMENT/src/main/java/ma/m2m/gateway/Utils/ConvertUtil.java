package ma.m2m.gateway.utils;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class ConvertUtil {

       public static final String FORMAT_DEFAUT = "yyyy-MM-dd";
       public static final String FRENCH_DEFAUT = "dd/MM/yy";
       public static final String FRENCH_DEFAUT_BIS = "dd/MM/yyyy HH:mm:ss";
       public static final String FORMAT_DATE = "ddMMyyyy";
       public static final String SQL_DEFAUT = "yyyy-MM-dd HH:mm:ss";
       public static final String TIME_ONLY = "HH:mm";
       public static final String FRENCH_DEFAUT_FORMAT = "dd/MM/yyyy";

       public static final String DEFAULT_UTIL_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";
       
       private static Logger logger = LogManager.getLogger(ConvertUtil.class);


       private ConvertUtil() {
       }

       // ------------------------------------------------------------------------
       // --------------FROM STRING TO SPECIFIC TYPES ----------------------------
       // ------------------------------------------------------------------------

       /**
        * Méthode permettant de convertir un String en Date<br>
        * 
        * @param data
        * @return Date
        * @throws SystemException
        */
       public static Date stringToDate(String data) {
             return stringToDate(data, FRENCH_DEFAUT);
       }

       /**
        * Méthode permettant de convertir un String en Date
        * 
        * @param data
        * @param pattern
        * @return Date
        * @throws TechnicalException
        */
       public static Date stringToDate(String data, String pattern) {

             if (data == null || "".equals(data)) {

                    return null;
             }

             SimpleDateFormat formatter = new SimpleDateFormat(pattern);
             Date res = null;
             try {
                    res = formatter.parse(data);
             } catch (ParseException e) {
            	 logger.info("======> stringToDate error. {}" , e);
             }
             return res;
       }

       /**
        * Méthode permettant de convertir un String en Date
        * 
        * @param data
        * @return Date
        */
       public static java.sql.Date stringToSqlDate(String data) {
             return stringToSqlDate(data, FRENCH_DEFAUT);
       }

       /**
        * Méthode permettant de convertir un String en Date Sql
        * 
        * @param data
        * @param pattern
        * @return java.sql.Date
        */
       public static java.sql.Date stringToSqlDate(String data, String pattern) {

             if (data == null || "".equals(data)
                           || data.length() != pattern.length()) {
                    return null;
             }

             java.sql.Date date = null;

             SimpleDateFormat sdf = new SimpleDateFormat(pattern);
             ParsePosition pp = new ParsePosition(0);
             date = new java.sql.Date(sdf.parse(data, pp).getTime());

             return date;
       }

       /**
        * methode convertissant une date en string
        * 
        * @param date
        * @return
        */
       public static String dateToString(Date date) {
             return dateToString(date, FRENCH_DEFAUT_FORMAT);
       }

       /**
        * methode convertissant une date en string
        * 
        * @param date
        * @param pattern
        * @return
        */
       public static String dateToString(Date date, String pattern) {

             if (date == null) {
                    return null;
             }
             
             String st;
             SimpleDateFormat sdf = new SimpleDateFormat(pattern);
             st = sdf.format(date);
             return st;
       }

       /**
        * Methode utilisée pour convertir un String en un long
        * 
        * @param String
        *            s
        * @return long l
        */
       public static Long stringToLong(String s) {
             Long l = null;
             if (s != null && s.isEmpty()) {
                    l = Long.valueOf(s);
             }
             return l;
       }

       public static Date roundDateToLower(Date date) {
             Calendar cal = Calendar.getInstance();
             cal.setTime(date);
             cal.set(Calendar.HOUR_OF_DAY, 0);
             cal.set(Calendar.MINUTE, 0);
             cal.set(Calendar.SECOND, 0);
             cal.set(Calendar.MILLISECOND, 0);

             return cal.getTime();
       }

       public static Date roundDateToUpper(Date date) {
             Calendar cal = Calendar.getInstance();
             cal.setTime(date);
             cal.set(Calendar.HOUR_OF_DAY, 23);
             cal.set(Calendar.MINUTE, 59);
             cal.set(Calendar.SECOND, 59);
             cal.set(Calendar.MILLISECOND, 999);

             return cal.getTime();
       }

       public static Date stringToTime(String data) {
             return stringToDate(data, TIME_ONLY);
       }

}

