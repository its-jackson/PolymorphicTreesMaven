package scripts.api;

/**
 * The super class of all subclass nodes.
 */

public abstract class Node {

    private final Worker worker = new Worker();

    public abstract void execute(Task task);
    public abstract boolean validate(Task task);
    public abstract void debug(String status);

    public Worker getWorker() {
        return worker;
    }
}
