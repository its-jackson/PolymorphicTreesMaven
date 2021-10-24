package scripts.fluffeeapi;

import org.tribot.util.Util;

import java.io.File;


/**
 * Created by Fluffee on 16/05/2017.
 */
@SuppressWarnings("unused")
public class Utilities {

    /**
     * Gets the Path to the FluffeeScripts directory, used for file writing.
     *
     * @return Path to directory as a String.
     */
    public static String getPolymorphicScriptsDirectory() {
        return Util.getWorkingDirectory().getAbsolutePath() + File.separator + "PolymorphicScripts" + File.separator;
    }
}
