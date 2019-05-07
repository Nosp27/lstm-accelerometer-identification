package gui.trainingFrame;

import gui.DesignControl;

import javax.swing.*;

public class TrainingFrame extends JFrame {
    public TrainingFrame(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        DesignControl.setBackgroundImage(this);

        TrainTab tt = new TrainTab();
        DesignControl.setTransparent(tt);
        add(tt);

        setSize(700,400);

        setVisible(true);
    }

    public static void main(String[] args) {
        new TrainingFrame();
    }
}
