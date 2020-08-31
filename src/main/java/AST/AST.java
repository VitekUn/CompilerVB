package AST;

import java.util.ArrayList;
import java.util.List;

public class AST {
    private String name;
    private String type;
    private int line; //Номер строки
    private List<AST> children;

    public AST(String name, String type, int line, List<AST> children) {
        this.name = name;
        this.type = type;
        this.line = line;
        this.children = children;
    }

    public AST(String name, String type) {
        this.name = name;
        this.type = type;
        this.line = 0;
        this.children = new ArrayList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setChildren(List<AST> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getLine() {
        return line;
    }

    public List<AST> getChildren() {
        return children;
    }

    public void add_child (AST node) {
        this.children.add(node);
    }
}
