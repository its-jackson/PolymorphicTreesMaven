package scripts.nodes.woodcutting;

import org.tribot.api.General;
import scripts.api.Globals;
import scripts.api.Node;
import scripts.api.Task;

/**
 * Purpose of class: Dispose the logs accordingly to the current task.
 *
 */

public class LogDisposal extends Node {
    private final Bank bank_node = new Bank();
    private final Drop drop_node = new Drop();
    private final Fletch fletch_node = new Fletch();
    private final Plank plank_node = new Plank();

    private Node logDisposalNode;

    @Override
    public void execute(Task task) {
        // sets log disposal node and will perform the log disposal method.
        performLogDisposal(task);
    }

    @Override
    public boolean validate(Task task) {
        switch (task.getLogOption().toLowerCase()) {
            case "bank", "plank-bank" -> {
                return bank_node.validate(task);
            }
            case "drop" -> {
                return drop_node.validate(task);
            }
            case "fletch-bank", "fletch-drop" -> {
                return fletch_node.validate(task);
            }
        }
        return false;
    }

    @Override
    public void debug(String status) {
        Globals.STATE = (status);
        General.println("[Log Disposal Control] " + status);
    }

    private void performLogDisposal(Task task) {
        if (task.shouldBank() || task.shouldPlankThenBank()) {
            debug("Bank");
            setLogDisposalNode(this.bank_node);
            this.bank_node.execute(task);
        } else if (task.shouldDrop()) {
            debug("Drop");
            setLogDisposalNode(this.drop_node);
            this.drop_node.execute(task);
        } else if (task.shouldFletchThenBank()) {
            debug("Fletch then bank");
            setLogDisposalNode(this.fletch_node);
            this.fletch_node.execute(task);
        } else if (task.shouldFletchThenDrop()) {
            debug("Fletch then drop");
            setLogDisposalNode(this.fletch_node);
            this.fletch_node.execute(task);
        }
    }

    public Node getLogDisposalNode() {
        return logDisposalNode;
    }

    public void setLogDisposalNode(Node logDisposalNode) {
        this.logDisposalNode = logDisposalNode;
    }
}
