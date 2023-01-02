package model;

public class Pair<T, U> {
    private Object o1;
    private Object o2;

    public Pair(Object o1, Object o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    public static <T, U> Pair<T, U> createPair(T t, U u) {
        return new Pair<T, U>(t, u);
    }

    public Object getO1() {
        return o1;
    }

    public void setO1(Object o1) {
        this.o1 = o1;
    }

    public Object getO2() {
        return o2;
    }

    public void setO2(Object o2) {
        this.o2 = o2;
    }
}
