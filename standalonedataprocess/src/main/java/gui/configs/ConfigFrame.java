package gui.configs;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


import configWork.ConfigManager;
import configWork.ConfigType;
import gui.components.DesignedButton;
import gui.components.DesignedLabel;
import gui.components.DesignedPanel;
import gui.components.DesignedSlider;

public class ConfigFrame extends JFrame {
    ArrayList<JLabel> labels = new ArrayList<>();

    public ConfigFrame() {
        boolean haveProperConfigs = ConfigManager.check();

        int numberOfProps = 5;
        GridLayout gl = new GridLayout(numberOfProps, 2, 30, 20);

        setContentPane(new DesignedPanel(DesignedPanel.BG2));

        getContentPane().setLayout(gl);
        createFileChooser("Accelerometer Data Path", "loaded-data", true);
        createFileChooser("Data input for neural network", "net-in", true);
        createFileChooser("Neural network model", "net-out", false);
        createSlider("Cluster size", 500, 2500, "cluster-size");
        createSlider("Evaluation-Testing ratio", 50, 95, "eval-ratio");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                hide();
            }

            @Override
            public void windowIconified(WindowEvent e) {
                hide();
            }

            private void hide() {
                setVisible(false);
            }
        });

        pack();

        if(!haveProperConfigs)
            setVisible(true);
    }

    private void createFileChooser(String label, String pathProperty, boolean dirsOnly) {
        //create file chooser
        JFileChooser chooser = new JFileChooser(ConfigManager.loadProperty("base-dir"));
        if (dirsOnly)
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JButton chooseFileBtn = new DesignedButton();
        chooseFileBtn.addActionListener(e -> {
            if (chooser.showOpenDialog(ConfigFrame.this) == JFileChooser.APPROVE_OPTION) {
                ConfigManager.saveProperty(pathProperty, chooser.getSelectedFile().getAbsolutePath(), ConfigType.PATH);
            }
        });
        chooseFileBtn.setText("...");

        //add components
        add(new DesignedLabel(label));
        add(chooseFileBtn);//add btn
    }

    private void createSlider(String label, int min, int max, String intProperty) {
        //create file chooser
        int value = Integer.parseInt(ConfigManager.loadProperty(intProperty));
        JSlider slider = new DesignedSlider(min, max, value);
        JLabel jlabel = new DesignedLabel(label);
        int index = labels.size();
        labels.add(jlabel);

        slider.addChangeListener(e -> {
            ConfigManager.saveProperty(intProperty, Integer.toString(slider.getValue()), ConfigType.INT);
            onChangeProperty(index, slider.getValue());
        });
        onChangeProperty(index, slider.getValue());

        //add components
        add(jlabel);
        add(slider);//add btn
    }

    private void onChangeProperty(int index, int value) {
        String prevText = labels.get(index).getText();
        int braceIndex = prevText.indexOf(" (");
        if (braceIndex == -1)
            labels.get(index).setText(prevText + String.format(" (%d)", value));
        else
            labels.get(index).setText(prevText.substring(0, braceIndex) + String.format(" (%d)", value));
    }
}
