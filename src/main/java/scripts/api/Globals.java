package scripts.api;

import org.tribot.api2007.types.RSObject;

import java.util.*;
import java.util.List;

/**
 * Globals class contain all the variables necessary for this script to function correctly.
 */

public class Globals {
    // gui and script state
    public static long currentTime;

    public static String STATE = "State";
    public static boolean START;
    public static boolean PROGRESSIVE;

    public static int treeFactor = 5;
    public static int birdNestCount = 0;
    public static int worldHopFactor = 5;

    public static boolean worldHop;
    public static boolean worldHopNoTreesAvailable;
    public static boolean pickUpBirdNest;
    public static boolean upgradeAxe;
    public static boolean specialAxe;
    public static boolean antiBanMicroSleep;
    public static boolean humanFatigue;

    //
    public static RSObject[] objectsNear;
    public static RSObject currentWorkingTree;
    public static RSObject nextWorkingTree;

    // fatigue system variables
    public static int var1000;
    public static int var1001;
    public static int var1002;

    // sleep arraylist
    public static List<Integer> waitTimes = new ArrayList<>();

    // task arraylist
    public static List<Task> tasks = new ArrayList<>();

    public static final String desc = ("# Polymorphic's Auto Woodcutter v1.04\n");

    // private constructor
    private Globals(){}
}
