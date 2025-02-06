package ma.m2m.gateway.utils;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class StringUtils {

    private StringUtils() {
    }

    public static boolean isNullOrEmpty(String param) {
		return param == null || param.trim().isEmpty();
	}
    
    public static boolean isEmpty(String in) {
          return in == null || "".equals(in);
    }

    public static String prepare(String chaine) {
          return chaine != null ? chaine.trim().toUpperCase() : null;
    }
    
    public static String trim(String chaine) {
          return chaine != null ? chaine.trim(): null;
    }
}
