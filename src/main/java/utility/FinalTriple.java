package utility;

/**
 * Created by Dons on 27-04-2017.
 */
public class FinalTriple<E, F, G> {
    public final E first;
    public final F second;
    public final G third;

    public FinalTriple(E first, F second, G third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}
