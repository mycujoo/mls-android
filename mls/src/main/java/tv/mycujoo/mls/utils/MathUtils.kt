package tv.mycujoo.mls.utils;

public class MathUtils {

    /**
     * Convert long to int safely. Similar with Math.toIntExact() in Java 8.
     *
     * @param numLong Number of type long to convert.
     * @return int version of input.
     * @throws ArithmeticException If input overflows int.
     */
    public static int safeLongToInt(long numLong) {
        if ((int) numLong != numLong) {
            throw new ArithmeticException("Input overflows int.\n");
        }
        return (int) numLong;
    }
}
