package Token;

public class Token {
    private String token;
    private String type;
    private int line;

    public Token(String token, String type, int line) {
        this.token = token;
        this.type = type;
        this.line = line;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public int getLine() {
        return line;
    }
}
