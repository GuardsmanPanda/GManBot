package utility;

import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.UUID;

public class Extra {


    public static <T> Comparator<T> randomOrder() {
        IdentityHashMap<Object, UUID> randomIDs = new IdentityHashMap<>();
        return (v1, v2) -> {
            UUID id1 = randomIDs.computeIfAbsent(v1, x -> UUID.randomUUID());
            UUID id2 = randomIDs.computeIfAbsent(v2, x -> UUID.randomUUID());
            return id1.compareTo(id2);
        };
    }
}
