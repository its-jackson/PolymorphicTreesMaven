package scripts.api.sulliuscep;

import org.tribot.api2007.types.RSObject;
import scripts.dax_api.walker_engine.interaction_handling.InteractionHelper;

public class SulliuscepPathObjectHandler {
    public static boolean clearVines(RSObject vines) {
        if (vines == null) {
            return false;
        }
        final boolean result = InteractionHelper.focusCamera(vines);
        return result && InteractionHelper.click(vines, "Clear");
    }

    public static boolean chopThickVines(RSObject thickVines) {
        if (thickVines == null) {
            return false;
        }
        final boolean result = InteractionHelper.focusCamera(thickVines);
        return result && InteractionHelper.click(thickVines, "Chop");
    }

}
