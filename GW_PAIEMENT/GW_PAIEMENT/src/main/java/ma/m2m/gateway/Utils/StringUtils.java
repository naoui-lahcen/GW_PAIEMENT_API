package ma.m2m.gateway.Utils;

public class StringUtils {

    private StringUtils() {
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
