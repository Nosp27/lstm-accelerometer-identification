package gui.tabs.serverControlTab;

import configWork.ConfigManager;
import gui.components.DesignedButton;
import gui.components.DesignedLabel;
import gui.components.DesignedPanel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.Vector;

public class DataFeedPanel extends DesignedPanel {
    private JLabel dataFeedInfoLabel;
    private JButton fileChooseBtn;

    private DataFeedListener l;
    public DataFeedPanel(DataFeedListener ll){
        l = ll;
        createLabel(PRIMARY, 15);
    }
    private JLabel createLabel(Color bg, int border) {
        setBackground(bg);
        setBorder(new BevelBorder(BevelBorder.LOWERED, Color.white, Color.GRAY));
        getBorder().getBorderInsets(this).set(border, border, border, border);
        dataFeedInfoLabel = new DesignedLabel();
        dataFeedInfoLabel.setForeground(Color.WHITE);
        dataFeedInfoLabel.setText("Recognition mode is disabled");
        JFileChooser chooser = new JFileChooser(ConfigManager.loadProperty("net-in"));
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".csv") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "recorded data";
            }
        });

        fileChooseBtn = new DesignedButton("Recognize file");
        fileChooseBtn.setBackground(Color.BLACK);
        fileChooseBtn.addActionListener(e->{
            if(chooser.showOpenDialog(DataFeedPanel.this) == JFileChooser.APPROVE_OPTION){
                l.onFileFed(chooser.getSelectedFile());
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(dataFeedInfoLabel);
        add(fileChooseBtn);
        return dataFeedInfoLabel;
    }

    public void setFeedMode(boolean _feed){
        dataFeedInfoLabel.setText("Recognition mode is " + ( _feed ? "enabled" : "disabled"));
        fileChooseBtn.setVisible(_feed);
    }

    public void setResult(Vector<Double> result) {
        double part = Double.NaN;
        String verdict = "Incorrect data";
        if(result != null) {
            part = result.lastElement();
            verdict = part <= .5 ? "User verified" : "User changed";
        }

        String s = "<html>";
        s+="<h2>" + verdict + "</h2>";
        s+="Result: " + part + "<br>";
        s+="</html>";
        dataFeedInfoLabel.setText(s);
    }

    interface DataFeedListener{
        void onFileFed(File f);
    }
}
