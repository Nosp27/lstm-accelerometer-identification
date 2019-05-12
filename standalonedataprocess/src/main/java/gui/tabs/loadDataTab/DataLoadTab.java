package gui.tabs.loadDataTab;

import accelTest.DataPrepare;
import configWork.ConfigManager;
import configWork.ConfigType;
import gui.components.DesignedButton;
import gui.components.DesignedLabel;
import gui.components.DesignedPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

public class DataLoadTab extends DesignedPanel {
    private DataLoaderListener l;
    private File baseDir = new File(ConfigManager.loadProperty("base-dir"));
    private File targetDirectory, targetDirectory2;
    private volatile boolean dataLoaded;
    private JPanel buttonSide;
    public boolean isDataLoaded() {
        return dataLoaded;
    }

    JButton prepareBtn;
    JButton resetBtn;
    JButton chooseFileBtn, chooseFileBtn2;
    ArrayList<JLabel> dataLoadInfoLabels;

    JFileChooser fileChooser;

    public DataLoadTab(DataLoaderListener ll) {
        setName("Load");
        setLayout(new BorderLayout(10, 20));
        l = ll;
        createControls();

        add(addStatusPanel(), BorderLayout.CENTER);
    }


    void createControls() {
        //add file chooser
        fileChooser = new JFileChooser(baseDir);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        //add side bar for buttons
        buttonSide = new DesignedPanel(PRIMARY);
        buttonSide.setLayout(new BoxLayout(buttonSide, BoxLayout.Y_AXIS));

        //prepare data button
        prepareBtn = new DesignedButton("Prepare data");
        prepareBtn.setEnabled(false);
        prepareBtn.addActionListener(this::generateEvalData);
        prepareBtn.setEnabled(false);

        //add choose file button
        chooseFileBtn = new DesignedButton("Choose directory with your data...");
        chooseFileBtn.addActionListener(this::chooseFile);

        //add choose file button
        chooseFileBtn2 = new DesignedButton("Choose directory with others' data...");
        chooseFileBtn2.setEnabled(false);
        chooseFileBtn2.addActionListener(this::chooseFile);

        //reset
        resetBtn = new DesignedButton("Reset");
        resetBtn.addActionListener(e -> reset());
        resetBtn.setEnabled(false);

        buttonSide.add(chooseFileBtn);
        buttonSide.add(Box.createVerticalStrut(10));
        buttonSide.add(chooseFileBtn2);
        buttonSide.add(Box.createVerticalStrut(10));
        buttonSide.add(prepareBtn);
        buttonSide.add(Box.createVerticalStrut(10));
        buttonSide.add(resetBtn);
        buttonSide.add(Box.createVerticalStrut(30));
        createSwitch("- Limit", "+ Limit", "file-limit", buttonSide);

        add(buttonSide, BorderLayout.WEST);
    }

    private void createSwitch(String off, String on, String intProperty, Container c) {
        //create file chooser
        JButton jb = new DesignedButton(off);
        jb.addActionListener(e -> {
            if (jb.getText().equals(off)) {
                ConfigManager.saveProperty(intProperty, "1", ConfigType.INT);
                jb.setText(on);
            } else {
                ConfigManager.saveProperty(intProperty, "0", ConfigType.INT);
                jb.setText(off);
            }
        });
        ConfigManager.saveProperty(intProperty, "0", ConfigType.INT);

        c.add(jb);//add btn
    }

    JPanel addStatusPanel() {
        JPanel statusPanel = new DesignedPanel(BG);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));

        dataLoadInfoLabels = new ArrayList<>();
        dataLoadInfoLabels.add(new DesignedLabel());
        dataLoadInfoLabels.add(new DesignedLabel());
        dataLoadInfoLabels.add(new DesignedLabel());

        for (int i = 0; i < 3; i++) {
            dataLoadInfoLabels.add(new DesignedLabel());
            statusPanel.add(dataLoadInfoLabels.get(i));
            statusPanel.add(Box.createVerticalStrut(15));
        }

        return statusPanel;
    }

    void updateInfoLabels(String[] lines0, String[] lines1, String[] lines2) {
        String dir1 = targetDirectory == null ? "undefined" : ".../" + targetDirectory.getName();
        String dir2 = targetDirectory2 == null ? "undefined" : ".../" + targetDirectory2.getName();
        String s = "<html>";
        s += "<h2>Directory labelled (0): " + dir1 + "</h2>";
        if (lines0 != null)
            for (String line : lines0)
                s += line + "<br>";
        s += "</html>";
        dataLoadInfoLabels.get(0).setText(s);

        s = "<html>";
        s += "<h2>Directory labelled (1): " + dir2 + "</h2>";
        if (lines1 != null)
            for (String line : lines1)
                s += line + "<br>";
        s += "</html>";
        dataLoadInfoLabels.get(1).setText(s);

        s = "<html>";
        s += "<h2>Data processing results</h2>";
        if (lines2 != null)
            for (String line : lines2)
                s += line + "<br>";
        s += "</html>";
        dataLoadInfoLabels.get(2).setText(s);
    }

    //handler for prepare data button
    private void generateEvalData(ActionEvent e) {
        new Thread(() -> {
            prepareBtn.setEnabled(false);

            String s_clusterSize = ConfigManager.loadProperty("cluster-size");
            int clusterSize = Integer.parseInt(s_clusterSize);
            float evalRatio = Integer.parseInt(ConfigManager.loadProperty("eval-ratio")) * .01f;

            int[] clusters = DataPrepare.generateEvalData(0, 1, targetDirectory, targetDirectory2, true);
            String[] res1 = resultOfDataSeparation(clusters[0], targetDirectory);

            String[] res2 = resultOfDataSeparation(clusters[1], targetDirectory2);

            String[] total = new String[]{
                    "Total clusters: " + (clusters[0] + clusters[1]),
                    "Total labels for training: " + (int)((clusters[0] + clusters[1]) * clusterSize * evalRatio),
                    "Total labels for evaluation: " + (int)((clusters[0] + clusters[1]) * clusterSize * (1 - evalRatio)),
                    "Different files: " + DataPrepare.getFileNum()
            };
            updateInfoLabels(res1, res2, total);
            reset();
        }).start();
    }

    private String[] resultOfDataSeparation(int clusters1, File f) {
        String s_clusterSize = ConfigManager.loadProperty("cluster-size");
        int clusterSize = Integer.parseInt(s_clusterSize);
        float evalRatio = Integer.parseInt(ConfigManager.loadProperty("eval-ratio")) * .01f;

        return new String[]{
                "Data from files in .../" + f.getName(),
                "Clusters: " + clusters1,
                "Cluster size: " + s_clusterSize,
                "Training lines per cluster: " + clusterSize * evalRatio,
                "Eval lines per cluster: " + clusterSize * (1 - evalRatio)
        };
    }

    void reset() {
        resetBtn.setEnabled(false);
        prepareBtn.setEnabled(false);
        targetDirectory = null;
        chooseFileBtn.setEnabled(true);
        chooseFileBtn2.setEnabled(false);
    }

    private void chooseFile(ActionEvent e) {
        boolean firstDir = targetDirectory == null;
        int result = fileChooser.showOpenDialog(DataLoadTab.this);
        File d;
        if (result == JFileChooser.APPROVE_OPTION) {
            if (firstDir)
                d = targetDirectory = fileChooser.getSelectedFile();
            else
                d = targetDirectory2 = fileChooser.getSelectedFile();

            l.onDataChosen(d);

            if (firstDir)
                chooseFileBtn.setEnabled(false);

            chooseFileBtn2.setEnabled(firstDir);
            prepareBtn.setEnabled(!firstDir);
            resetBtn.setEnabled(true);
        }
    }

    public interface DataLoaderListener {
        void onDataChosen(File _targetDirectory);
    }
}
