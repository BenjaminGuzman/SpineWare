package spineware;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Healthynnovation
 */
public class Validate {
    private static final Pattern USERNAME = Pattern.compile("[a-zA-Z ]*$");
    private static Matcher match;
    public static boolean name(String str){
        match = USERNAME.matcher(str);
        return match.matches();
    }
    public static String getMatch(String str){
        match = Pattern.compile("[a-zA-Z ]").matcher(str);
        match.matches();
        return match.group();
    }
}
