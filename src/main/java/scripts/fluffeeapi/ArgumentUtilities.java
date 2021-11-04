package scripts.fluffeeapi;


import java.util.HashMap;

/**
 * Fluffee's Argument Utility Class
 */

public class ArgumentUtilities {

    /**
     * Converts the raw hashmap received in #passArguments to a hashmap with useful key/values
     * Arguments are parsed in the following format: "setting1: value1; setting2: value2;"
     *
     * @param userInput - Text inputted into the arguments box on the Script Selection screen.
     * @return - HashMap of the converted arguments, or null if no arguments are found.
     */
    public static HashMap<String, String> get(HashMap<String, String> userInput) {
        if (userInput.keySet().contains("custom_input")) {
            return getFromString(userInput.get("custom_input"));
        } else if (userInput.keySet().contains("autostart")) {
            return getFromString(userInput.get("autostart"));
        }
        return new HashMap<>();
    }

    /**
     * Takes the converted arguments hashmap and creates a usable hashmap with a key and values.
     *
     * @param argumentsString - Initial arguments hashmap converted to a string, and with the input type removed.
     * @return - HashMap of the converted arguments.
     */
    public static HashMap<String, String> getFromString(String argumentsString) {
        HashMap<String, String> arguments = new HashMap<>();
        if (argumentsString == null) {
            return arguments;
        }

        String[] argList = argumentsString.split(";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Split everything by ;
        for (String currentArgument : argList) {
            if (!currentArgument.contains(":")) {
                break;
            }
            String[] argumentSplit = currentArgument.split(":");
            arguments.put(
                    argumentSplit[0].replaceAll("^\"|\"$", ""),
                    argumentSplit[1].replaceAll("^\"|\"$", "")
            );
        }
        return arguments;
    }
}
