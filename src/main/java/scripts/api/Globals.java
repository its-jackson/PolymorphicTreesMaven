package scripts.api;

import org.tribot.api2007.types.RSObject;

import java.util.*;
import java.util.List;

/**
 * Globals class contain all the variables necessary for this script to function correctly.
 *
 * IMPORTANT NOTE: Anyone who is looking at this class, it should use the singleton pattern where there
 *          is only one instance of this object for wide system usage.
 */

public class Globals {

    // class should use singleton pattern
    // will leave as it is currently.

    // gui and script state
    private static long currentTime;

    private static String state = "State";
    private static boolean start;
    private static boolean progressive;

    private static int treeFactor = 7;
    private static int worldHopFactor = 5;
    private static int birdNestCount = 0;

    private static boolean worldHop = true;
    private static boolean worldHopNoTreesAvailable = true;
    private static boolean pickUpBirdNest = true;
    private static boolean upgradeAxe = true;
    private static boolean specialAxe;
    private static boolean antiBanMicroSleep = true;
    private static boolean humanFatigue = true;
    private static boolean useAllGold;
    private static boolean useGoldPerTask = true;

    private static boolean onRepeatShuffle;
    private static boolean onRepeat;

    // trees
    private static RSObject currentWorkingTree;
    private static RSObject nextWorkingTree;

    //
    private static double currentFatigueMultiple;

    // sleep arraylist
    private static List<Integer> waitTimes = new ArrayList<>();

    // task arraylist
    private static List<Task> tasks = new ArrayList<>();

    // private constructor
    private Globals(){}

    public static long getCurrentTime() {
        return currentTime;
    }

    public static void setCurrentTime(long currentTime) {
        Globals.currentTime = currentTime;
    }

    public static String getState() {
        return state;
    }

    public static void setState(String state) {
        Globals.state = state;
    }

    public static boolean isStart() {
        return start;
    }

    public static void setStart(boolean start) {
        Globals.start = start;
    }

    public static boolean isProgressive() {
        return progressive;
    }

    public static void setProgressive(boolean progressive) {
        Globals.progressive = progressive;
    }

    public static int getTreeFactor() {
        return treeFactor;
    }

    public static void setTreeFactor(int treeFactor) {
        Globals.treeFactor = treeFactor;
    }

    public static int getWorldHopFactor() {
        return worldHopFactor;
    }

    public static void setWorldHopFactor(int worldHopFactor) {
        Globals.worldHopFactor = worldHopFactor;
    }

    public static int getBirdNestCount() {
        return birdNestCount;
    }

    public static void setBirdNestCount(int birdNestCount) {
        Globals.birdNestCount = birdNestCount;
    }

    public static boolean isWorldHop() {
        return worldHop;
    }

    public static void setWorldHop(boolean worldHop) {
        Globals.worldHop = worldHop;
    }

    public static boolean isWorldHopNoTreesAvailable() {
        return worldHopNoTreesAvailable;
    }

    public static void setWorldHopNoTreesAvailable(boolean worldHopNoTreesAvailable) {
        Globals.worldHopNoTreesAvailable = worldHopNoTreesAvailable;
    }

    public static boolean isPickUpBirdNest() {
        return pickUpBirdNest;
    }

    public static void setPickUpBirdNest(boolean pickUpBirdNest) {
        Globals.pickUpBirdNest = pickUpBirdNest;
    }

    public static boolean isUpgradeAxe() {
        return upgradeAxe;
    }

    public static void setUpgradeAxe(boolean upgradeAxe) {
        Globals.upgradeAxe = upgradeAxe;
    }

    public static boolean isSpecialAxe() {
        return specialAxe;
    }

    public static void setSpecialAxe(boolean specialAxe) {
        Globals.specialAxe = specialAxe;
    }

    public static boolean isAntiBanMicroSleep() {
        return antiBanMicroSleep;
    }

    public static void setAntiBanMicroSleep(boolean antiBanMicroSleep) {
        Globals.antiBanMicroSleep = antiBanMicroSleep;
    }

    public static boolean isHumanFatigue() {
        return humanFatigue;
    }

    public static void setHumanFatigue(boolean humanFatigue) {
        Globals.humanFatigue = humanFatigue;
    }

    public static boolean isUseAllGold() {
        return useAllGold;
    }

    public static void setUseAllGold(boolean useAllGold) {
        Globals.useAllGold = useAllGold;
    }

    public static boolean isUseGoldPerTask() {
        return useGoldPerTask;
    }

    public static void setUseGoldPerTask(boolean useGoldPerTask) {
        Globals.useGoldPerTask = useGoldPerTask;
    }

    public static boolean isOnRepeatShuffle() {
        return onRepeatShuffle;
    }

    public static void setOnRepeatShuffle(boolean onRepeatShuffle) {
        Globals.onRepeatShuffle = onRepeatShuffle;
    }

    public static boolean isOnRepeat() {
        return onRepeat;
    }

    public static void setOnRepeat(boolean onRepeat) {
        Globals.onRepeat = onRepeat;
    }

    public static RSObject getCurrentWorkingTree() {
        return currentWorkingTree;
    }

    public static void setCurrentWorkingTree(RSObject currentWorkingTree) {
        Globals.currentWorkingTree = currentWorkingTree;
    }

    public static RSObject getNextWorkingTree() {
        return nextWorkingTree;
    }

    public static void setNextWorkingTree(RSObject nextWorkingTree) {
        Globals.nextWorkingTree = nextWorkingTree;
    }

    public static double getCurrentFatigueMultiple() {
        return currentFatigueMultiple;
    }

    public static void setCurrentFatigueMultiple(double currentFatigueMultiple) {
        Globals.currentFatigueMultiple = currentFatigueMultiple;
    }

    public static List<Integer> getWaitTimes() {
        return waitTimes;
    }

    public static void setWaitTimes(List<Integer> waitTimes) {
        Globals.waitTimes = waitTimes;
    }

    public static List<Task> getTasks() {
        return tasks;
    }

    public static void setTasks(List<Task> tasks) {
        Globals.tasks = tasks;
    }
}
