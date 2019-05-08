package gui.tabs.helpTab;

import gui.components.DesignedButton;
import gui.components.DesignedLabel;
import gui.components.DesignedPanel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class HelpTab extends DesignedPanel {
    public HelpTab() {
        super(Color.DARK_GRAY);
        setLayout(new BorderLayout(20,20));
        setBorder(new BevelBorder(BevelBorder.RAISED));

        JLabel helpLabel = new DesignedLabel(setBaseHelp());
        helpLabel.setVerticalAlignment(SwingConstants.TOP);

        JPanel switches = new DesignedPanel(Color.DARK_GRAY);

        switches.setLayout(new BoxLayout(switches, BoxLayout.Y_AXIS));
        switches.add(new DesignedButton("General", ()->helpLabel.setText(setBaseHelp())));
        switches.add(Box.createVerticalStrut(10));
        switches.add(new DesignedButton("Data loading", ()->helpLabel.setText(setDataLoadHelp())));
        switches.add(Box.createVerticalStrut(10));
        switches.add(new DesignedButton("Training network", ()->helpLabel.setText(setTrainingHelp())));
        switches.add(Box.createVerticalStrut(10));
        switches.add(new DesignedButton("Data loading", ()->helpLabel.setText(setRemoteHelp())));
        switches.add(Box.createVerticalStrut(10));

        add(new DesignedLabel(headerText()), BorderLayout.NORTH);
        add(switches, BorderLayout.WEST);
        add(helpLabel, BorderLayout.CENTER);

    }

    private String headerText(){
        String s = "<html>";
        s += "<h1>Neural network Workstation Help</h1>";
        s += "</html>";
        return s;
    }

    private String setBaseHelp() {
        String s = "<html>";
        s += "<h1>General</h1>";
        s += "Programm has 3 tabs<br>" +
                "Data load tab<br>" +
                "Training tab<br>" +
                "Remote tab<br>" +
                "They allow you to train neural network for person recognition using accelerometer data";
        s += "</html>";
        return s;
    }

    private String setDataLoadHelp() {
        String s = "<html>";
        s += "<h1>Data load" + "</h1>";
        s += "Panel allows to set up datasets for neural network training. " +
                "You need two datasets for proper training. First is accelerometer data of your own device, " +
                "Second is data about other people, who participate in the network training process." + "<br>";
        s += "</html>";
        return s;
    }

    private String setTrainingHelp() {
        String s = "<html>";
        s += "<h1>Training" + "</h1>";
        s += "After the data was set up, you need to train the network. In order to do that, click in the \"Tabs\" menu" +
                "and select \"Train\". Then press train button to begin." + "<br>";
        s += "</html>";
        return s;
    }

    private String setRemoteHelp() {
        String s = "<html>";
        s += "<h1>Remote" + "</h1>";
        s += "Your mobile device can be remotely connected to this software for real-time data processing and user recognition. " +
                "In the \"Remote\" tab you can see how many files with accelerometer data have been transferred from remote device." + "<br>";
        s += "</html>";
        return s;
    }
}
