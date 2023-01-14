package mathutil;

import static mathutil.ImageUtil.INSTANTIATION_ERR_MSG;

public class TrigUtil {

    private TrigUtil() {
        throw new UnsupportedOperationException(INSTANTIATION_ERR_MSG);
    }

    /**
     * Returns the hyperbolic secant of a <code>x</code>>.
     * @param x The value which the hyperbolic secant is to be returned for.
     * @return The hyperbolic secant value of the input.
     */
    public static double sech(double x) {
        return 1.0 / Math.cosh(x);
    }

}
