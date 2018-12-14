package app.provider.plain;

public class Bar {

    private final Foo foo;

    public Bar(Foo foo) {
        this.foo = foo;
    }

    public Foo getFoo() {
        return this.foo;
    }
}
