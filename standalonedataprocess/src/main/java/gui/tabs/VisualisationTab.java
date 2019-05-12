package gui.tabs;

import configWork.ConfigManager;
import gui.components.DesignedButton;
import gui.components.DesignedPanel;
import gui.graph.TripleGraphPanel;

import javax.swing.*;

public class VisualisationTab extends DesignedPanel {
    public VisualisationTab() {
        setName("Visualization");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JFileChooser fc = new JFileChooser(ConfigManager.loadProperty("net-in"));
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        TripleGraphPanel graphics = new TripleGraphPanel();
        add(new DesignedButton("...", () ->{
            if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                if(fc.getSelectedFile().isDirectory())
                    graphics.setUpFiles(fc.getSelectedFile());
                else graphics.setUpFile(fc.getSelectedFile());
            }
        }));
        add(graphics);
    }
}
