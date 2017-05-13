package utility;

import java.util.Random;

public class MoreUtility {
    private static Random random = new Random();

    public static int nextRandomInt(int startInclusive, int endExclusive) {
        assert (endExclusive > startInclusive);
        return random.nextInt(endExclusive - startInclusive) + startInclusive;
    }

}
