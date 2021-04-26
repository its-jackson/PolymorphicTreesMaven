//package scripts;
//
//import scripts.api.*;
//
//import javax.swing.*;
//import javax.swing.plaf.nimbus.NimbusLookAndFeel;
//import javax.swing.text.SimpleAttributeSet;
//import javax.swing.text.StyleConstants;
//import java.awt.*;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class GUI extends JFrame {
//    private JLabel slogan;
//    private JLabel trees;
//    private JLabel optionLabel;
//    private JLabel presetLocation;
//    private JLabel axeType;
//    private JLabel header;
//
//    private JButton startButton;
//    private JButton progressiveModeButton;
//
//    private JComboBox<String> woodcuttingAreaCombobox;
//    private JComboBox<String> treeNameCombobox;
//
//    private JRadioButton bank;
//    private JRadioButton drop;
//    private JRadioButton infernalAxeActive;
//
//    private ButtonGroup buttonGroup;
//    private ButtonGroup buttonGroup2;
//
//    private JMenuBar jMenuBar;
//    private JMenu aboutMenu, fileMenu, helpMenu;
//    private JMenuItem item1, item2, item3, item4;
//
//    private final Font HEADER_FONT = new Font("Verdana", Font.BOLD, 24);
//    private final Font SLOGAN_FONT = new Font("Verdana", Font.ITALIC, 12);
//
//    private final String[] tree_names = {
//            "Oak",
//            "Willow",
//            "Maple",
//            "Yew",
//            "Magic",
//            "Redwood North LOWER LEVEL",
//            "Redwood South LOWER LEVEL",
//            "Redwood North UPPER LEVEL",
//            "Redwood South UPPER LEVEL"
//    };
//    private final String[] woodcutting_location_names = {
//            "Woodcutting Guild",
//            "Draynor",
//            "Varrock West",
//            "Varrock Palace",
//            "Falador",
//            "Edgeville",
//            "Port Sarim",
//            "Seers' Village"
//    };
//    private final String[] axe_names = {
//            "Infernal axe ACTIVE"
//    };
//
//    public GUI() {
//        init();
//    }
//
//    private void setWorkingArea(String location, String tree) {
//        String completeLocation =
//                location.concat(" ")
//                        .concat(tree)
//                        .toLowerCase();
//
//        switch (completeLocation) {
//            case "seers' village magic": {
//                Globals.LOCATION = Location.SEERS_VILLAGE_MAGICS;
//                Globals.TREE = "Magic tree";
//            }
//            break;
//            case "woodcutting guild redwood north upper level": {
//                Globals.LOCATION = Location.REDWOOD_NORTH_UPPER_LEVEL;
//                Globals.TREE = "Redwood";
//            }
//            break;
//            case "woodcutting guild redwood north lower level": {
//                Globals.LOCATION = Location.REDWOOD_NORTH;
//                Globals.TREE = "Redwood";
//            }
//            break;
//            case "woodcutting guild redwood south lower level": {
//                Globals.LOCATION = Location.REDWOOD_SOUTH;
//                Globals.TREE = "Redwood";
//            }
//            break;
//            case "woodcutting guild redwood south upper level": {
//                Globals.LOCATION = Location.REDWOOD_SOUTH_UPPER_LEVEL;
//                Globals.TREE = "Redwood";
//            }
//            break;
//            case "woodcutting guild oak": {
//                Globals.LOCATION = Location.WOODCUTTING_GUILD_OAKS;
//                Globals.TREE = "Oak";
//            }
//            break;
//            case "woodcutting guild maple": {
//                Globals.LOCATION = Location.WOODCUTTING_GUILD_MAPLES;
//                Globals.TREE = "Maple";
//            }
//            break;
//            case "woodcutting guild willow": {
//                Globals.LOCATION = Location.WOODCUTTING_GUILD_WILLOWS;
//                Globals.TREE = "Willow";
//            }
//            break;
//            case "woodcutting guild magic": {
//                Globals.LOCATION = Location.WOODCUTTING_GUILD_MAGICS;
//                Globals.TREE = "Magic tree";
//
//            }
//            break;
//            case "woodcutting guild yew": {
//                Globals.LOCATION = Location.WOODCUTTING_GUILD_YEWS;
//                Globals.TREE = "Yew";
//            }
//            break;
//            case "draynor yew": {
//                Globals.LOCATION = Location.DRAYNOR_YEWS;
//                Globals.TREE = "Yew";
//            }
//            break;
//            case "draynor willow": {
//                Globals.LOCATION = Location.DRAYNOR_WILLOWS;
//                Globals.TREE = "Willow";
//            }
//            break;
//            case "falador yew": {
//                Globals.LOCATION = Location.FALADOR_YEWS;
//                Globals.TREE = "Yew";
//            }
//            break;
//            case "edgeville yew": {
//                Globals.LOCATION = Location.EDGEVILLE_YEWS;
//                Globals.TREE = "Yew";
//            }
//            break;
//            case "varrock west tree": {
//                Globals.LOCATION = Location.VARROCK_WEST_TREES;
//                Globals.TREE = "Tree";
//            }
//            break;
//            case "varrock west oak": {
//                Globals.LOCATION = Location.VARROCK_WEST_OAKS;
//                Globals.TREE = "Oak";
//            }
//            break;
//            case "varrock palace oak": {
//                Globals.LOCATION = Location.VARROCK_PALACE_OAKS;
//                Globals.TREE = "Oak";
//            }
//            break;
//            case "varrock palace yew": {
//                Globals.LOCATION = Location.VARROCK_PALACE_YEWS;
//                Globals.TREE = "Yew";
//            }
//            break;
//            case "port sarim willow": {
//                Globals.LOCATION = Location.PORT_SARIM_WILLOWS;
//                Globals.TREE = "Willow";
//            }
//        }
//    }
//
//    private void runProgressiveMode(Progressive runnable) {
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        executorService.execute(runnable);
//        executorService.shutdown();
//    }
//
//    private void init() {
//        slogan = new JLabel("\"Saradomin made the integers, all else is the work of man\"");
//        header = new JLabel("Polymorphic Auto Woodcutter");
//        optionLabel = new JLabel("(4) Options");
//        presetLocation = new JLabel("(1) Preset Locations");
//        trees = new JLabel("(2) Trees");
//        axeType = new JLabel("(3) Axe");
//        woodcuttingAreaCombobox = new JComboBox<>(woodcutting_location_names);
//        treeNameCombobox = new JComboBox<>(tree_names);
//        bank = new JRadioButton("Bank");
//        drop = new JRadioButton("Drop");
//        infernalAxeActive = new JRadioButton(axe_names[0]);
//        startButton = new JButton();
//        progressiveModeButton = new JButton();
//        buttonGroup = new ButtonGroup();
//        buttonGroup2 = new ButtonGroup();
//
//        item1 = new JMenuItem("Exit");
//        item2 = new JMenuItem("About Polymorphic");
//        item3 = new JMenuItem("View Help");
//        item4 = new JMenuItem("Send Feedback");
//
//        jMenuBar = new JMenuBar();
//        aboutMenu = new JMenu("About");
//        fileMenu = new JMenu("File");
//        helpMenu = new JMenu("Help");
//
//        fileMenu.add(item1);
//        aboutMenu.add(item2);
//        helpMenu.add(item3);
//        helpMenu.add(item4);
//
//        jMenuBar.add(fileMenu);
//        jMenuBar.add(aboutMenu);
//        jMenuBar.add(helpMenu);
//
//        header.setBounds(35, 0, 420, 75);
//        slogan.setBounds(35, 55, 400, 30);
//        trees.setBounds(35, 150, 100, 25);
//        presetLocation.setBounds(35, 100, 120, 25);
//        optionLabel.setBounds(35, 250, 70, 25);
//        bank.setBounds(200, 250, 50, 25);
//        drop.setBounds(255, 250, 50, 25);
//        infernalAxeActive.setBounds(200, 200, 140,25);
//        axeType.setBounds(35, 200, 150, 25);
//        woodcuttingAreaCombobox.setBounds(200, 100, 230, 25);
//        treeNameCombobox.setBounds(200, 150, 230, 25);
//        startButton.setBounds(400, 300, 75, 30);
//        progressiveModeButton.setBounds(200, 300, 180, 30);
//
//        slogan.setFont(SLOGAN_FONT);
//        header.setFont(HEADER_FONT);
//
//        startButton.setText("Start");
//        startButton.setFocusable(false);
//
//        progressiveModeButton.setText("Progressive F2P 1-60");
//        progressiveModeButton.setFocusable(false);
//
//        buttonGroup.add(bank);
//        buttonGroup.add(drop);
//
//        this.add(header);
//        this.add(slogan);
//        this.add(presetLocation);
//        this.add(trees);
//        this.add(optionLabel);
//        this.add(bank);
//        this.add(drop);
//        this.add(infernalAxeActive);
//        this.add(axeType);
//        this.add(woodcuttingAreaCombobox);
//        this.add(treeNameCombobox);
//        this.add(startButton);
//        this.add(progressiveModeButton);
//
//        item1.addActionListener(e -> System.exit(0));
//
//        item2.addActionListener(e -> {
//            String message = ("Hello, my name is Jackson. I'm a college student in Ontario;");
//            String message4 = (" learning software engineering technology.");
//            String message2 = (" My main goal is to develop strong Java programming skills including debugging.");
//            String message3 = (" I plan to keep Polymorphic Auto Woodcutter up to date and relevant for several years" +
//                    ". I will always be improving and updating this script no matter what." );
//
//            JFrame aboutFrame = new JFrame("About Polymorphic");
//
//            aboutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//            aboutFrame.setSize(400,150);
//            aboutFrame.setResizable(false);
//            aboutFrame.setLocationRelativeTo(null);
//            aboutFrame.setVisible(true);
//
//            Container containerPane = aboutFrame.getContentPane();
//            JTextPane textPane = new JTextPane();
//            SimpleAttributeSet attributeSet = new SimpleAttributeSet();
//            StyleConstants.setBold(attributeSet, true);
//
//            // Set the attributes before adding text
//            textPane.setCharacterAttributes(attributeSet, true);
//            textPane.setText(message + message4 + message2 + message3);
//            textPane.setEditable(false);
//            textPane.setFocusable(false);
//
//            JScrollPane scrollPane = new JScrollPane(textPane);
//            containerPane.add(scrollPane, BorderLayout.CENTER);
//
//            aboutFrame.add(scrollPane);
//        });
//
//        woodcuttingAreaCombobox.addActionListener(e -> {
//            // clear all items before updating tree names combobox, respectively
//            treeNameCombobox.removeAllItems();
//
//            switch (woodcuttingAreaCombobox.getSelectedIndex()) {
//                case 0: { // woodcutting guild
//                    treeNameCombobox.addItem("Oak");
//                    treeNameCombobox.addItem("Willow");
//                    treeNameCombobox.addItem("Maple");
//                    treeNameCombobox.addItem("Yew");
//                    treeNameCombobox.addItem("Magic");
//
//                    treeNameCombobox.addItem("Redwood North LOWER LEVEL");
//                    treeNameCombobox.addItem("Redwood South LOWER LEVEL");
//                    treeNameCombobox.addItem("Redwood North UPPER LEVEL");
//                    treeNameCombobox.addItem("Redwood South UPPER LEVEL");
//                }
//                break;
//                case 1: { // draynor
//                    treeNameCombobox.addItem("Willow");
//                    treeNameCombobox.addItem("Yew");
//                }
//                break;
//                case 2: { // varrock west
//                    treeNameCombobox.addItem("Tree");
//                    treeNameCombobox.addItem("Oak");
//                }
//                break;
//                case 3: { // varrock palace
//                    treeNameCombobox.addItem("Oak");
//                    treeNameCombobox.addItem("Yew");
//                }
//                break;
//                case 4:
//                case 5: { // falador or edgeville
//                    treeNameCombobox.addItem("Yew");
//                }
//                break;
//                case 6: { // port sarim
//                    treeNameCombobox.addItem("Willow");
//                }
//                break;
//                case 7: { // seers village magics
//                    treeNameCombobox.addItem("Magic");
//                }
//            }
//        });
//
//        treeNameCombobox.addActionListener(e -> {
//
//        });
//
//        startButton.addActionListener(e -> {
//            if (e.getSource() == startButton) {
//                boolean fail = false;
//
//                if (!(bank.isSelected() || drop.isSelected())) {
//                    JOptionPane.showMessageDialog(null, "Please choose your options.", "Error",
//                            JOptionPane.ERROR_MESSAGE);
//                    fail = true;
//                }
//
//                if (!fail) {
//                    int response = JOptionPane.showInternalConfirmDialog(null, "Are you ready to start?", "Confirmation",
//                            JOptionPane.YES_NO_CANCEL_OPTION);
//
//                    if (response == JOptionPane.YES_OPTION) {
//                        String location = woodcuttingAreaCombobox.getItemAt(woodcuttingAreaCombobox.getSelectedIndex());
//                        String tree = treeNameCombobox.getItemAt(treeNameCombobox.getSelectedIndex());
//                        setWorkingArea(location, tree);
//
//                        if (bank.isSelected()) {
//                            Globals.BANK = true;
//                        }
//
//                        if (infernalAxeActive.isSelected()) {
//                            Globals.SPECIAL_AXE = true;
//                        }
//
//                        Globals.START = true;
//                        startButton.setEnabled(false);
//                        progressiveModeButton.setEnabled(false);
//                        this.setVisible(false);
//                    }
//                }
//
//            }
//        });
//
//        progressiveModeButton.addActionListener(e -> {
//            JOptionPane.showMessageDialog(null, "Warning, this is an experimental function. It does function " +
//                            "\ncorrectly, however you may experience some issues.", "Warning",
//                    JOptionPane.WARNING_MESSAGE);
//
//            int response = JOptionPane.showInternalConfirmDialog(null, "Are you ready to start?", "Confirmation",
//                    JOptionPane.YES_NO_CANCEL_OPTION);
//
//            if (response == JOptionPane.YES_OPTION) {
//                runProgressiveMode(new Progressive());
//                progressiveModeButton.setEnabled(false);
//                startButton.setEnabled(false);
//                this.setVisible(false);
//            }
//        });
//
//        this.setJMenuBar(jMenuBar);
//        this.setTitle("Polymorphic Auto Woodcutter V1.02");
//        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        this.setLayout(new BorderLayout());
//        this.setResizable(false);
//        this.setSize(500, 400);
//        this.setLocationRelativeTo(null); // center the frame on construction
//
//        JOptionPane.showMessageDialog(null, "Hello! Welcome To Polymorphic Auto Woodcutter V1.02", "Welcome",
//                JOptionPane.INFORMATION_MESSAGE);
//    }
//
//    public static void main(String[] args) {
//        try {
//            UIManager.setLookAndFeel(new NimbusLookAndFeel());
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        new GUI().setVisible(true);
//    }
//}