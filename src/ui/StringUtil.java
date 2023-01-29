package ui;

import mathutil.ImageUtil;

public class StringUtil
{
    private StringUtil() {
        throw new UnsupportedOperationException(ImageUtil.INSTANTIATION_ERR_MSG);
    }

    public static boolean isNotEmpty(String s) {
        return s != null && s.length() > 0;
    }
}
