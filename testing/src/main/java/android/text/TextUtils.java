package android.text;

/**
 * @author gordon
 * @date 2022/10/18
 */
public class TextUtils {
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }
}
