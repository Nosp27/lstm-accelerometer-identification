package gui.configs;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;


import configWork.ConfigManager;
import configWork.ConfigType;

public class ConfigFrame extends JFrame {
    public ConfigFrame() {
        int numberOfProps = 3;
        GridLayout gl = new GridLayout(numberOfProps, 2, 30, 20);

        getContentPane().setLayout(gl);
        createFileChooser("Accelerometer Data Path", "accel-data", true);
        createFileChooser("Data input for neural network", "net-in", true);
        createFileChooser("Neural network model", "net-out", true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                hide();
            }

            @Override
            public void windowIconified(WindowEvent e) {
                hide();
            }

            private void hide(){
                ConfigManager.configSave();
                setVisible(false);
            }
        });

        pack();
    }

    private void createFileChooser(String label, String pathProperty, boolean dirsOnly) {
        //create file chooser
        JFileChooser chooser = new JFileChooser(ConfigManager.loadProperty("base-dir"));
        if (dirsOnly)
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JButton chooseFileBtn = new JButton();
        chooseFileBtn.addActionListener(e -> {
            if (chooser.showOpenDialog(ConfigFrame.this) == JFileChooser.APPROVE_OPTION) {
                ConfigManager.saveProperty(pathProperty, chooser.getSelectedFile().getAbsolutePath(), ConfigType.PATH);
            }
        });
        chooseFileBtn.setText("...");

        //add components
        add(new JLabel(label));
        add(chooseFileBtn);//add btn
    }
}
