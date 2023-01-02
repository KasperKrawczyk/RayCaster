import mathutil.Util;

public class StringUtil
{
    private StringUtil() {
        throw new UnsupportedOperationException(Util.INSTANTIATION_ERR_MSG);
    }

    public static boolean isNotEmpty(String s) {
        return s != null && s.length() > 0;
    }
}
