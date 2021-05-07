package scripts.api;

import org.tribot.api.General;
import org.tribot.api2007.Login;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSObject;

/**
 * Purpose of class: Parallelize finding objects; take advantage of multi-core
 *                      architecture to enhance performance.
 */

public class FindObject implements Runnable {
    private RSObject[] objects;
    private Task task;

    @Override
    public void run() {
        boolean run = true;
        while (run) {
            General.sleep(2000, 3000);
            if (Login.STATE.INGAME == Login.getLoginState() && Workable.isInLocation(getTask(), Player.getRSPlayer())) {
                if (getTask() != null && Workable.nearObjects(Globals.treeFactor, getTask().getTree())) {
                    while (Workable.isWorking()) {
                        General.sleep(2000, 3000);
                        System.out.printf("Worker is chopping%sis waiting.", Thread.currentThread().getName());
                    }
                    System.out.printf("%sis locating%sfor distance%d", Thread.currentThread().getName(), getTask().getTree(), Globals.treeFactor);
                    locateObjects();
                }
            } else {
                System.out.printf("%s is sleeping", Thread.currentThread().getName());
            }
        }
    }

    public void locateObjects() {
        RSObject[] objects = Objects.findNearest(Globals.treeFactor, getTask().getTree());
        if (objects != null && objects.length > 0) {
            setObjects(objects);
        } else {
            setObjects(null);
        }
        Globals.objectsNear = getObjects();
    }

    public RSObject[] getObjects() {
        return objects;
    }

    public void setObjects(RSObject[] objects) {
        this.objects = objects;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
