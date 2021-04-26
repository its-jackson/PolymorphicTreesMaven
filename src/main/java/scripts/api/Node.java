package scripts.api;

public abstract class Node {
    public abstract void execute(Task task);
    public abstract boolean validate(Task task);
    public abstract void debug(String status);
}
