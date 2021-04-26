package scripts.api.antiban;

public enum SpecialAttackPreference {
    MAIN_HUD(1),
    COMBAT_TAB(2);

    private final int code;

    SpecialAttackPreference(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
