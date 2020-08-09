package Semantics;

public class Scope {
    private String type;
    private String name;

    public Scope(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
