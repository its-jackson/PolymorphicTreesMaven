package scripts.nodes.woodcutting;

import org.tribot.api.General;
import scripts.api.Globals;
import scripts.api.Node;
import scripts.api.Task;

/**
 * Purpose of class: Dispose the logs accordingly to the current task.
 *
 * Updated 11/05/2021 - Changed naming convention for final variables.
 */

public class LogDisposal extends Node {

    private final Bank bankNode = new Bank();
    private final Drop dropNode = new Drop();
    private final Fletch fletchNode = new Fletch();

    private Node logDisposalNode;

    @Override
    public void execute(Task task) {
        // sets log disposal node and will perform the log disposal method.
        performLogDisposal(task);
    }

    @Override
    public boolean validate(Task task) {
        switch (task.getLogOption().toLowerCase()) {
            case "bank":
                case "plank-bank": {
                return getBankNode().validate(task);
            }
            case "drop":  {
                return getDropNode().validate(task);
            }
            case "fletch-bank":
                case "fletch-drop": {
                return getFletchNode().validate(task);
            }
        }
        return false;
    }

    @Override
    public void debug(String status) {
        Globals.setState(status);
        General.println("[Log Disposal Control] " + status);
    }

    private void performLogDisposal(Task task) {
        if (task.shouldBank() || task.shouldPlankThenBank()) {
            debug("Bank");
            setLogDisposalNode(getBankNode());
            getBankNode().execute(task);
        } else if (task.shouldDrop()) {
            debug("Drop");
            setLogDisposalNode(getDropNode());
            getDropNode().execute(task);
        } else if (task.shouldFletchThenBank()) {
            debug("Fletch then bank");
            setLogDisposalNode(getFletchNode());
            getFletchNode().execute(task);
        } else if (task.shouldFletchThenDrop()) {
            debug("Fletch then drop");
            setLogDisposalNode(getFletchNode());
            getFletchNode().execute(task);
        }
    }

    public Bank getBankNode() {
        return bankNode;
    }

    public Drop getDropNode() {
        return dropNode;
    }

    public Fletch getFletchNode() {
        return fletchNode;
    }

    public Node getLogDisposalNode() {
        return logDisposalNode;
    }

    public void setLogDisposalNode(Node logDisposalNode) {
        this.logDisposalNode = logDisposalNode;
    }
}
