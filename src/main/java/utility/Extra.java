package utility;

import java.security.SecureRandom;
import java.util.*;

public class Extra {
    private static final Random random = new SecureRandom();

    /**
     * Takes a percent value betwwen 0 and 100 and returns true with a probability of that value
     */
    public static boolean percentChance(double percent) {
        assert(percent >= 0);
        return random.nextDouble() < percent/100;
    }

    public static <T> T getRandomElement(List<T> list) {
        assert(!list.isEmpty());
        return list.get(random.nextInt(list.size()));
    }

    public static int randomInt() { return random.nextInt(); }
    public static int randomInt(int bound) { return random.nextInt(bound); }

    public static <T> Comparator<T> randomOrder() {
        IdentityHashMap<Object, UUID> randomIDs = new IdentityHashMap<>();
        return (v1, v2) -> {
            UUID id1 = randomIDs.computeIfAbsent(v1, x -> UUID.randomUUID());
            UUID id2 = randomIDs.computeIfAbsent(v2, x -> UUID.randomUUID());
            return id1.compareTo(id2);
        };
    }
}
