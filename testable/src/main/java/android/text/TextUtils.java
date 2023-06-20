package android.text;

/**
 * @author gordon
 * @date 2023/2/2
 */
public class TextUtils {
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }
}
