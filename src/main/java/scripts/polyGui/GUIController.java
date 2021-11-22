package scripts.polyGui;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import scripts.api.Globals;
import scripts.api.Task;
import com.allatori.annotations.DoNotRename;
import scripts.api.TimeElapse;
import scripts.api.Gold;
import scripts.fluffeeapi.Utilities;

@DoNotRename
public class GUIController implements Initializable {

    @DoNotRename
    private final Image img = new Image("https://jacksonjohnson.ca/gui/saradomin");

    @DoNotRename
    private final Image sawmill_woodcutting_guild = new Image("https://jacksonjohnson.ca/gui/sawmill");

    @DoNotRename
    private String[] tree_names = {
            "Tree",
            "Oak",
            "Willow",
            "Maple",
            "Yew",
            "Magic",
            "Redwood",
            "Mahogany",
            "Teak",
    };

    @DoNotRename
    private String[] tree_locations = {
            "Woodcutting Guild",
            "Draynor",
            "Varrock West",
            "Varrock Palace",
            "Falador",
            "Edgeville",
            "Port Sarim",
            "Seers' Village",
            "Isle Of Souls",
            "Falador East",
            "Catherby",
            "Lumbridge Castle",
            "Sorcerer's Tower",
    };

    @DoNotRename
    private String[] log_options = {
            "Bank",
            "Drop",
            "Fletch-Bank",
            "Fletch-Drop"
    };

    @DoNotRename
    private String[] log_option_plank = {
            "Plank-Bank"
    };

    @DoNotRename
    private GUIFX gui;

    @FXML
    @DoNotRename
    private Label labelHeader;

    @FXML
    @DoNotRename
    private Label labelSubHeader;

    @FXML
    @DoNotRename
    private Label labelWelcome;

    @FXML
    @DoNotRename
    private Label labelLevel;

    @FXML
    @DoNotRename
    private Label labelPlanking;

    @FXML
    @DoNotRename
    private Label labelGoldPerTask;

    @FXML
    @DoNotRename
    private Label labelTimeElapsed;

    @FXML
    @DoNotRename
    private Label labelOr;

    @FXML
    @DoNotRename
    private Label labelStatus;

    @FXML
    @DoNotRename
    private TextArea textAreaWelcome;

    @FXML
    @DoNotRename
    private TextField textFieldUntilLevel;

    @FXML
    @DoNotRename
    private TextField textFieldTimeElapsed;

    @FXML
    @DoNotRename
    private TextField textFieldGoldPerTask;

    @FXML
    @DoNotRename
    private ChoiceBox choiceBoxTree;

    @FXML
    @DoNotRename
    private ChoiceBox choiceBoxLocation;

    @FXML
    @DoNotRename
    private ChoiceBox choiceBoxLogOptions;

    @FXML
    @DoNotRename
    private Button btnCreateTask;

    @FXML
    @DoNotRename
    private Button btnUpdateTask;

    @FXML
    @DoNotRename
    private Button btnDeleteTask;

    @FXML
    @DoNotRename
    private Button btnSaveProfile;

    @FXML
    @DoNotRename
    private Button btnLoadProfile;

    @FXML
    @DoNotRename
    private Button btnStart;

    @FXML
    @DoNotRename
    private TableView<Task> tableViewMain;

    @FXML
    @DoNotRename
    private TableColumn<Task, String> colTree;

    @FXML
    @DoNotRename
    private TableColumn<Task, String> colLocation;

    @FXML
    @DoNotRename
    private TableColumn<Task, String> colLogOption;

    @FXML
    @DoNotRename
    private TableColumn<Task, Integer> colUntilLevel;

    @FXML
    @DoNotRename
    private TableColumn<Task, TimeElapse> colTimeElapsed;

    @FXML
    @DoNotRename
    private RadioButton upgradeAxeRdBtn;

    @FXML
    @DoNotRename
    private RadioButton infernalAxeRdBtn;

    @FXML
    @DoNotRename
    private RadioButton birdNestRdBtn;

    @FXML
    @DoNotRename
    private RadioButton worldHopRdBtn;

    @FXML
    @DoNotRename
    private RadioButton worldHopNoTreesRdBtn;

    @FXML
    @DoNotRename
    private RadioButton afkMicroSleepRdBtn;

    @FXML
    @DoNotRename
    private RadioButton replicateHumanFatigueRdBtn;

    @FXML
    @DoNotRename
    private RadioButton repeatShuffleRdBtn;

    @FXML
    @DoNotRename
    private RadioButton repeatRdBtn;

    @FXML
    @DoNotRename
    private RadioButton goldPerTaskRdBtn;

    @FXML
    @DoNotRename
    private RadioButton dontRepeatRdBtn;

    @FXML
    @DoNotRename
    private RadioButton useAllGoldRdBtn;

    @FXML
    @DoNotRename
    private Slider worldHopSlider;

    @FXML
    @DoNotRename
    private ImageView mainImgView;

    @FXML
    @DoNotRename
    private ImageView sawWoodcuttingGuildImgView;

    @Override
    @DoNotRename
    public void initialize(URL location, ResourceBundle resources) {
        initTreeCBox();
        //initLogOptionsCBox();
        onActionChoiceBoxTree();
        onActionTableViewMain();
        onActionLocationCBox();
        mainImgView.setImage(img);
        sawWoodcuttingGuildImgView.setImage(sawmill_woodcutting_guild);
        //colTree.setCellValueFactory(tree_property_value);
        //colLocation.setCellValueFactory(location_property_value);
        //colLogOption.setCellValueFactory(log_option_property_value);
        //colUntilLevel.setCellValueFactory(until_level_property_value);

        colTree.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Task, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Task, String> taskStringCellDataFeatures) {
                return new ReadOnlyObjectWrapper(taskStringCellDataFeatures.getValue().getTree());
            }
        });

        colLocation.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Task, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Task, String> taskStringCellDataFeatures) {
                return new ReadOnlyObjectWrapper(taskStringCellDataFeatures.getValue().getLocation());
            }
        });

        colLogOption.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Task, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Task, String> taskStringCellDataFeatures) {
                return new ReadOnlyObjectWrapper(taskStringCellDataFeatures.getValue().getLogOption());
            }
        });

        colUntilLevel.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Task, Integer>,
                ObservableValue<Integer>>() {
            @Override
            public ObservableValue<Integer> call(TableColumn.CellDataFeatures<Task, Integer> taskStringCellDataFeatures) {
                return new ReadOnlyObjectWrapper(taskStringCellDataFeatures.getValue().getUntilLevel());
            }
        });

        colTimeElapsed.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Task, TimeElapse>, ObservableValue<TimeElapse>>() {
            @Override
            public ObservableValue<TimeElapse> call(TableColumn.CellDataFeatures<Task, TimeElapse> taskTimeElapseCellDataFeatures) {
                return new ReadOnlyObjectWrapper<>(taskTimeElapseCellDataFeatures.getValue().getTime());
            }
        });
    }

    @DoNotRename
    private void saveTextToFile(String content, File file) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
            System.out.println("Successfully saved profile settings.");
        } catch (IOException ex) {
            Logger.getLogger(GUIController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    @DoNotRename
    private void btnCreateTaskPressed() {
        String tree = "";
        String location = "";
        String logOption = "";

        if (choiceBoxTree.getValue() != null) {
            tree = (String) choiceBoxTree.getValue();
        }

        if (choiceBoxLocation.getValue() != null) {
            location = (String) choiceBoxLocation.getValue();
        }

        if (choiceBoxLogOptions.getValue() != null) {
            logOption = (String) choiceBoxLogOptions.getValue();
        }

        boolean flag = true;

        if (choiceBoxTree != null && choiceBoxTree.getSelectionModel().isEmpty()) {
            flag = false;
            String status = "Please select a tree.";
            System.out.println(status);
        }

        if (choiceBoxLocation != null && choiceBoxLocation.getSelectionModel().isEmpty()) {
            flag = false;
            String status = "Please select a location.";
            System.out.println(status);
        }

        if (choiceBoxLogOptions != null && choiceBoxLogOptions.getSelectionModel().isEmpty()) {
            flag = false;
            String status = "Please select a log disposal option.";
            System.out.println(status);
        }

        if (!(textFieldUntilLevel.getText().isEmpty() || textFieldUntilLevel.getText().isBlank())
                && !(textFieldTimeElapsed.getText().isEmpty() || textFieldTimeElapsed.getText().isBlank())) {
            flag = false;
            String status = "Please select either time elapsed or until level.";
            System.out.println(status);
        }

        if ((textFieldUntilLevel.getText().isEmpty() || textFieldUntilLevel.getText().isBlank())
                && (textFieldTimeElapsed.getText().isEmpty() || textFieldTimeElapsed.getText().isBlank())) {
            flag = false;
            String status = "Please select either time elapsed or until level.";
            System.out.println(status);
        }

        if (!(textFieldUntilLevel.getText().isEmpty() || textFieldUntilLevel.getText().isBlank())
                && textFieldTimeElapsed.getText().isEmpty() || textFieldTimeElapsed.getText().isBlank()) {
            if (flag) {
                try {
                    final int level = Integer.parseInt(textFieldUntilLevel.getText());
                    if (level > 0 && level < 100) {
                        Task task = new Task(tree, location, logOption, level);
                        tableViewMain.getItems().add(task);
                        Globals.getTasks().add(task);
                    } else {
                        System.out.println("Level must be greater than zero and less than 100.");
                    }
                } catch (NumberFormatException formatException) {
                    System.out.println("Level must be numerical.");
                }
            }
        }

        if (!(textFieldTimeElapsed.getText().isEmpty() || textFieldTimeElapsed.getText().isBlank())
                && textFieldUntilLevel.getText().isEmpty() || textFieldUntilLevel.getText().isBlank()) {
            if (flag) {
                final String timeElapse = textFieldTimeElapsed.getText();
                if (timeElapse.matches("\\d\\d:\\d\\d:\\d\\d:\\d\\d")) {
                    Task task = new Task(tree, location, logOption, new TimeElapse(timeElapse));
                    tableViewMain.getItems().add(task);
                    Globals.getTasks().add(task);
                } else {
                    System.out.println("Incorrect time elapsed format. DAYS:HOURS:MINUTES:SECONDS - 00:00:00:00");
                }
            }
        }
    }

    @FXML
    @DoNotRename
    private void btnUpdateTaskPressed() {
        // TODO
        // add update validation before patch v1.06
        boolean flag = true;

        String tree = "";
        String location = "";
        String logOption = "";
        int level = 0;
        TimeElapse timer = new TimeElapse(textFieldTimeElapsed.getText());

        if (choiceBoxTree.getValue() != null) {
            tree = (String) choiceBoxTree.getValue();
        }

        if (choiceBoxLocation.getValue() != null) {
            location = (String) choiceBoxLocation.getValue();
        }

        if (choiceBoxLogOptions.getValue() != null) {
            logOption = (String) choiceBoxLogOptions.getValue();
        }

        if (!(textFieldUntilLevel.getText().isEmpty() || textFieldUntilLevel.getText().isBlank())
                && !(textFieldTimeElapsed.getText().isEmpty() || textFieldTimeElapsed.getText().isBlank())) {
            flag = false;
            System.out.println("Please select either time elapsed or until level.");
        }

        if ((textFieldUntilLevel.getText().isEmpty() || textFieldUntilLevel.getText().isBlank())
                && (textFieldTimeElapsed.getText().isEmpty() || textFieldTimeElapsed.getText().isBlank())) {
            flag = false;
            System.out.println("Please select either time elapsed or until level.");
        }

        if (!(textFieldUntilLevel.getText().isEmpty() || textFieldUntilLevel.getText().isBlank())
                && textFieldTimeElapsed.getText().isEmpty() || textFieldTimeElapsed.getText().isBlank()) {
            try {
                level = Integer.parseUnsignedInt(textFieldUntilLevel.getText());
            } catch (NumberFormatException formatException) {
                System.out.println("Level must be numerical.");
            }
            if (!(level > 0 && level < 100)) {
                flag = false;
                System.out.println("Level must be greater than zero and less than 100.");
            }
        }

        if (!(textFieldTimeElapsed.getText().isEmpty() || textFieldTimeElapsed.getText().isBlank())
                && textFieldUntilLevel.getText().isEmpty() || textFieldUntilLevel.getText().isBlank()) {
            if (flag) {
                final String timeElapse = textFieldTimeElapsed.getText();
                if (!timeElapse.matches("\\d\\d:\\d\\d:\\d\\d:\\d\\d")) {
                    flag = false;
                    System.out.println("Incorrect time elapsed format. DAYS:HOURS:MINUTES:SECONDS - 00:00:00:00");
                }
            }
        }

        if (flag) {
            Task selectTask = tableViewMain.getSelectionModel().getSelectedItem();
            int i = Globals.getTasks().indexOf(selectTask);
            if (i >= 0) {
                Task currentTask = Globals.getTasks().get(i);

                currentTask.setTree(tree);
                currentTask.setLocation(location);
                currentTask.setLogOption(logOption);
                currentTask.setUntilLevel(level);
                currentTask.setTime(timer);
                currentTask.setCompleteTask(location, tree);

                selectTask.setTree(tree);
                selectTask.setLocation(location);
                selectTask.setLogOption(logOption);
                selectTask.setUntilLevel(level);
                selectTask.setTime(timer);
                selectTask.setCompleteTask(location, tree);

                choiceBoxLocation.setValue(selectTask.getLocation());
                tableViewMain.refresh();
            }
        }
    }

    @FXML
    @DoNotRename
    private void btnDeleteTaskPressed() {
        int index = tableViewMain.getSelectionModel().getSelectedIndex();

        if (index > 0) {
            Globals.getTasks().remove(index);
            tableViewMain.getItems().remove(index);
        }
    }

    @FXML
    @DoNotRename
    private void btnSaveProfilePressed() {
        //StringBuilder content = new StringBuilder();
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

//        for (Task t : tableViewMain.getItems()) {
//            //content.append(t).append("/");
//            gson.toJson(t);
//        }

        FileChooser fileChooser = new FileChooser();

        //extension filter for text files
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(".json", "*.json");
        fileChooser.getExtensionFilters().add(extensionFilter);

        // create directory if doesn't exist
        if (!Files.exists(Paths.get(Utilities.getPolymorphicScriptsDirectory()))) {
            File dir = new File(Utilities.getPolymorphicScriptsDirectory());
            dir.mkdir();
        }

        fileChooser.setInitialDirectory(new File(Utilities.getPolymorphicScriptsDirectory()));

        // show save file
        File file = fileChooser.showSaveDialog(getGUI().getStage());

        if (file != null) {
            // save text tp file
            file = new File(file.getAbsolutePath());
            saveTextToFile(gson.toJson(tableViewMain.getItems()), file);
//            if (file.createNewFile()) {
//                //
//            } else {
//                if (file.delete()) {
//                    if (file.createNewFile()) {
//                        // save text tp file
//                        //gson.toJson(tableViewMain.getItems(), new FileWriter(file));
//                        saveTextToFile(gson.toJson(tableViewMain.getItems()), file);
//                    }
//                }
//            }
        }

    }

    @FXML
    @DoNotRename
    private void btnLoadProfilePressed() {
        FileChooser fileChooser = new FileChooser();

        //extension filter for text files
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(".json", "*.json");
        fileChooser.getExtensionFilters().add(extensionFilter);

        // create directory if doesn't exist
        if (!Files.exists(Paths.get(Utilities.getPolymorphicScriptsDirectory()))) {
            File dir = new File(Utilities.getPolymorphicScriptsDirectory());
            dir.mkdir();
        }

        // work dir
        fileChooser.setInitialDirectory(new File(Utilities.getPolymorphicScriptsDirectory()));

        // show open file
        File file = fileChooser.showOpenDialog(getGUI().getStage());

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        try {
            if (file.canRead()) {
                FileReader fileReader = new FileReader(file);
                Task[] task = gson.fromJson(fileReader, Task[].class);
                if (task != null && task.length > 0) {
                    tableViewMain.getItems().clear();
                    Globals.getTasks().clear();
                    for (Task t : task) {
                        System.out.println(t);
                        tableViewMain.getItems().add(t);
                        Globals.getTasks().add(t);
                    }
                }
            }
        } catch (FileNotFoundException exception) {
            System.out.println("Caught file not found.");
        } catch (NullPointerException nullPointerException) {
            System.out.println("Caught null pointer.");
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            System.out.println("Caught index out of bounds.");
        } catch (SecurityException securityException) {
            System.out.println("Caught security.");
        }

//        try (Scanner scanner = new Scanner(file)) {
//            while (scanner.hasNextLine()) {
//                data.append(scanner.nextLine());
//            }
//        }
//        catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        String[] splitData = data.toString().split("/");
//
//        if (splitData.length > 0) {
//            for (String s : splitData) {
//                if (s != null) {
//                    String[] ss = s.split("--");
//
//                    String tree = ss[0];
//                    String location = ss[1];
//                    String logOption = ss[2];
//                    int untilLevel = Integer.parseInt(ss[3]);
//                    String time = ss[5];
//
//                    System.out.println(tree);
//                    System.out.println(location);
//                    System.out.println(logOption);
//                    System.out.println(untilLevel);
//                    System.out.println(time);
//
//                    Task task = new Task(tree, location, logOption, untilLevel, new TimeElapse(time));
//
//                    tableViewMain.getItems().add(task);
//
//                    Globals.getTasks().add(task);
//
//                    System.out.println("new: " + Globals.getTasks().get(Globals.getTasks().indexOf(task)));
//                }
//            }
//        }

        tableViewMain.refresh();
    }

    @FXML
    @DoNotRename
    private void btnStartPressed() {
        boolean flag = true;

        if (upgradeAxeRdBtn.isSelected()) {
            Globals.setUpgradeAxe(true);
        } else {
            Globals.setSpecialAxe(true);
        }

        if (!birdNestRdBtn.isSelected()) {
            Globals.setPickUpBirdNest(false);
        }

        if (!worldHopRdBtn.isSelected()) {
            Globals.setWorldHop(false);

        } else {
            Globals.setWorldHopFactor((int) worldHopSlider.getValue());
        }

        if (!worldHopNoTreesRdBtn.isSelected()) {
            Globals.setWorldHopNoTreesAvailable(false);
        }

        if (!afkMicroSleepRdBtn.isSelected()) {
            Globals.setAntiBanMicroSleep(false);
        }

        if (!replicateHumanFatigueRdBtn.isSelected()) {
            Globals.setHumanFatigue(false);
        }

//        if (!dontRepeatRdBtn.isSelected()) {
//            Globals.setDontRepeat(false);
//        }

        if (repeatRdBtn.isSelected()) {
            Globals.setOnRepeat(true);
        }

        if (repeatShuffleRdBtn.isSelected()) {
            Globals.setOnRepeatShuffle(true);
        }

        if (useAllGoldRdBtn.isSelected()) {
            Globals.setUseAllGold(true);
            Globals.setUseGoldPerTask(false);
        } else {
            Gold.setGoldRegex(textFieldGoldPerTask.getText());
        }

        if (goldPerTaskRdBtn.isSelected() && Gold.calculateActualGoldRegex(Gold.getGoldRegex()) == 0) {
            flag = false;
            String status = "Incorrect gold format.";
            System.out.println(status);
        }

        if (tableViewMain.getItems().size() == 0) {
            flag = false;
            String status = "Please add a task before starting.";
            System.out.println(status);
        }

        if (flag) {
            getGUI().close();
            Globals.setStart(true);
        }

    }

    @FXML
    @DoNotRename
    private void onActionChoiceBoxTree() {
        choiceBoxTree.setOnAction((event) -> {
            String treeChoice = (String) choiceBoxTree.getSelectionModel().getSelectedItem();
            setLocationCBox(treeChoice);
            //System.out.println(treeChoice);
        });
    }

    @FXML
    @DoNotRename
    private void onActionTableViewMain() {
        tableViewMain.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Task>) change -> {

            tableViewMain.refresh();

            if (change.getList().size() > 0) {
                String tree = change.getList().get(0).getTree();
                String location1 = change.getList().get(0).getLocation();
                String logOption = change.getList().get(0).getLogOption();
                int untilLevel = change.getList().get(0).getUntilLevel();
                TimeElapse timer = change.getList().get(0).getTime();

                choiceBoxTree.setValue(tree);
                setLocationCBox(tree);
                choiceBoxLocation.setValue(location1);
                choiceBoxLogOptions.setValue(logOption);
                textFieldUntilLevel.setText(String.valueOf(untilLevel));

                if (timer != null) {
                    textFieldTimeElapsed.setText(timer.toString());
                }

                System.out.printf("%s%s%s%s%s%n",
                        choiceBoxTree.getValue(),
                        choiceBoxLocation.getValue(),
                        choiceBoxLogOptions.getValue(),
                        textFieldUntilLevel.getText(),
                        textFieldTimeElapsed.getText()
                );
            }
        });
    }

    @FXML
    @DoNotRename
    private void onActionGoldPerTaskRdBtn() {
        if (goldPerTaskRdBtn.isSelected()) {
            textFieldGoldPerTask.setVisible(true);
        }
    }

    @FXML
    @DoNotRename
    private void onActionUseAllGoldRdBtn() {
        if (useAllGoldRdBtn.isSelected()) {
            textFieldGoldPerTask.setVisible(false);
        }
    }

    @FXML
    @DoNotRename
    private void onActionWorldHopRdBtn() {
        if (!worldHopRdBtn.isSelected()) {
            worldHopSlider.setOpacity(0.5);
            worldHopSlider.setDisable(true);
        } else {
            worldHopSlider.setOpacity(1.0);
            worldHopSlider.setDisable(false);
        }
    }

    @FXML
    @DoNotRename
    private void onActionLocationCBox() {
        choiceBoxLocation.setOnAction(event -> {
            final String location = (String) choiceBoxLocation.getSelectionModel().getSelectedItem();

            choiceBoxLogOptions.getItems().clear();

            if (location != null && location.contains("Woodcutting Guild") && choiceBoxTree.getSelectionModel().getSelectedItem().equals("Oak")) {
                choiceBoxLogOptions.getItems().addAll(log_options);
                choiceBoxLogOptions.getItems().add(log_option_plank[0]);
            } else {
                // choiceBoxLogOptions.getItems().remove(log_option_plank[0]);
                choiceBoxLogOptions.getItems().addAll(log_options);
            }
        });
    }

    @DoNotRename
    private void setLocationCBox(String treeChoice) {
        choiceBoxLocation.getItems().clear();

        if (treeChoice != null) {
            switch (treeChoice.toLowerCase()) {
                case "tree": {
                    choiceBoxLocation.getItems().add(getTreeLocations()[2]); // varrock west
                    choiceBoxLocation.getItems().add(getTreeLocations()[11]); // lumbridge castle
                    choiceBoxLocation.getItems().add(getTreeLocations()[7]); // seers' village
                }
                break;
                case "oak": {
                    choiceBoxLocation.getItems().add(getTreeLocations()[0]); // woodcutting guild
                    choiceBoxLocation.getItems().add(getTreeLocations()[2]); // varrock west
                    choiceBoxLocation.getItems().add(getTreeLocations()[3]); // varrock palace
                    choiceBoxLocation.getItems().add(getTreeLocations()[9]); // falador east
                }
                break;
                case "willow": {
                    choiceBoxLocation.getItems().add(getTreeLocations()[0]); // woodcutting guild
                    choiceBoxLocation.getItems().add(getTreeLocations()[1]); // draynor
                    choiceBoxLocation.getItems().add(getTreeLocations()[6]); // port sarim
                    choiceBoxLocation.getItems().add(getTreeLocations()[10]); // catherby
                    choiceBoxLocation.getItems().add(getTreeLocations()[7]); // seers' village
                }
                break;
                case "maple": {
                    choiceBoxLocation.getItems().add(getTreeLocations()[0]); // woodcutting guild
                    choiceBoxLocation.getItems().add(getTreeLocations()[7]); // seers' village
                }
                break;
                case "yew": {
                    choiceBoxLocation.getItems().add(getTreeLocations()[0]); // woodcutting guild
                    choiceBoxLocation.getItems().add(getTreeLocations()[1]); // draynor
                    choiceBoxLocation.getItems().add(getTreeLocations()[4]); // falador
                    choiceBoxLocation.getItems().add(getTreeLocations()[5]); // edgeville
                    choiceBoxLocation.getItems().add(getTreeLocations()[3]); // varrock palace
                    choiceBoxLocation.getItems().add(getTreeLocations()[10]); // catherby
                    choiceBoxLocation.getItems().add(getTreeLocations()[7]); // seers' village
                }
                break;
                case "magic":
                case "magic tree": {
                    choiceBoxLocation.getItems().add(getTreeLocations()[0]); // woodcutting guild
                    choiceBoxLocation.getItems().add(getTreeLocations()[7]); // seers' village
                    choiceBoxLocation.getItems().add(getTreeLocations()[12]); // sorcerer's tower
                }
                break;
                case "redwood": {
                    choiceBoxLocation.getItems().add("North Lower Level"); // woodcutting guild
                    choiceBoxLocation.getItems().add("North Upper Level"); // woodcutting guild
                    choiceBoxLocation.getItems().add("South Lower Level"); // woodcutting guild
                    choiceBoxLocation.getItems().add("South Upper Level"); // woodcutting guild

                }
                break;
                case "mahogany":
                case "teak": {
                    choiceBoxLocation.getItems().add(getTreeLocations()[8]); // isle of souls
                }
            }
        }
    }

    @DoNotRename
    private void initTreeCBox() {
        for (String s : getTreeNames()) {
            choiceBoxTree.getItems().add(s);
        }
    }

    @DoNotRename
    private void initLogOptionsCBox() {
        for (String s : getLogOptions()) {
            choiceBoxLogOptions.getItems().add(s);
        }
    }

    @DoNotRename
    public void setGUI(GUIFX gui) {
        this.gui = gui;
    }

    @DoNotRename
    public GUIFX getGUI() {
        return this.gui;
    }

    @DoNotRename
    public String[] getTreeNames() {
        return tree_names;
    }

    @DoNotRename
    public void setTreeNames(String[] tree_names) {
        this.tree_names = tree_names;
    }

    @DoNotRename
    public String[] getTreeLocations() {
        return tree_locations;
    }

    @DoNotRename
    public void setTreeLocations(String[] tree_locations) {
        this.tree_locations = tree_locations;
    }

    @DoNotRename
    public String[] getLogOptions() {
        return log_options;
    }

    @DoNotRename
    public void setLogOptions(String[] log_options) {
        this.log_options = log_options;
    }

    public Label getLabelStatus() {
        return labelStatus;
    }

    public void setLabelStatus(Label labelStatus) {
        this.labelStatus = labelStatus;
    }
}