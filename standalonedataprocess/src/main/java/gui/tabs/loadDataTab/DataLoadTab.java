package gui.tabs.loadDataTab;

import accelTest.DataPrepare_ALT;
import configWork.ConfigManager;
import gui.DesignControl;
import gui.graph.GraphRender;
import gui.tabs.loadDataTab.dataLoader.YReader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;

public class DataLoadTab extends JPanel {
    DataLoaderListener l;
    File baseDir = new File(ConfigManager.loadProperty("base-dir"));
    File targetDirectory, targetDirectory2;

    volatile boolean dataLoaded;

    JPanel buttonSide;
    GraphRender render;

    public boolean isDataLoaded() {
        return dataLoaded;
    }

    JButton prepareBtn;
    JButton resetBtn;
    JButton chooseFileBtn, chooseFileBtn2;

    JFileChooser fileChooser;

    public DataLoadTab(DataLoaderListener ll) {
        setName("Load");
        setLayout(new BorderLayout(10, 20));
        l = ll;

        createControls();

        render = new GraphRender();
        add(render, BorderLayout.CENTER);
    }

    void createControls() {

        //add file chooser
        fileChooser = new JFileChooser(baseDir);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //

        //add side bar for buttons
        buttonSide = new JPanel();
        buttonSide.setLayout(new BoxLayout(buttonSide, BoxLayout.Y_AXIS));

        //prepare data button
        prepareBtn = new JButton("Prepare data");
        prepareBtn.setEnabled(false);
        prepareBtn.addActionListener(this::generateEvalData);
        prepareBtn.setEnabled(false);

        //add choose file button
        chooseFileBtn = new JButton("Choose directory with your data...");
        chooseFileBtn.addActionListener(this::chooseFile);

        //add choose file button
        chooseFileBtn2 = new JButton("Choose directory with others' data...");
        chooseFileBtn2.setEnabled(false);
        chooseFileBtn2.addActionListener(this::chooseFile);

        //reset
        resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> reset());
        resetBtn.setEnabled(false);

        DesignControl.setTransparent(this);
        DesignControl.setTransparent(buttonSide);

        buttonSide.add(chooseFileBtn);
        buttonSide.add(chooseFileBtn2);
        buttonSide.add(prepareBtn);
        buttonSide.add(resetBtn);


        add(buttonSide, BorderLayout.WEST);
    }

    //handler for prepare data button
    private void generateEvalData(ActionEvent e) {
        new Thread(() -> {
            prepareBtn.setEnabled(false);
            DataPrepare_ALT.generateEvalData(targetDirectory, .8f, 2000, 0, true);
            DataPrepare_ALT.generateEvalData(targetDirectory2, .8f, 2000, 0, false);
            reset();
        }).start();
    }

    void reset() {
        resetBtn.setEnabled(false);
        prepareBtn.setEnabled(false);
        targetDirectory = null;
        chooseFileBtn.setEnabled(true);
        chooseFileBtn2.setEnabled(false);
    }

    void drawGraph() {
        try {
            YReader yr = new YReader(new File(targetDirectory, "t\\features"));
            for (int i = 0; i < 5; i++)
                render.addPoints(yr.readAll());
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
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
