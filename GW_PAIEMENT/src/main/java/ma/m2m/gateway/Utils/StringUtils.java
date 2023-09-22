package ma.m2m.gateway.Utils;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

public class StringUtils {

    private StringUtils() {
    }

    public static boolean isNullOrEmpty(String param) {
		return param == null || param.trim().length() == 0;
	}
    
    public static boolean isEmpty(String in) {
          if (in == null || "".equals(in)) {
                 return true;
          }
          return false;
    }

    public static String prepare(String chaine) {
          return chaine != null ? chaine.trim().toUpperCase() : null;
    }
    
    public static String trim(String chaine) {
          return chaine != null ? chaine.trim(): null;
    }
}
