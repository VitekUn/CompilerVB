package CodeGenerator;

public class RegisterASM {
    private String name;
    private boolean inWork;

    public RegisterASM(String name, boolean inWork) {
        this.name = name;
        this.inWork = inWork;
    }

    public String getName() {
        return name;
    }

    public boolean isInWork() {
        return inWork;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInWork(boolean inWork) {
        this.inWork = inWork;
    }
}
