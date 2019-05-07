package gui.trainingFrame;

import accelTest.LRNN;
import configWork.ConfigManager;
import gui.DesignControl;
import org.deeplearning4j.eval.Evaluation;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class TrainTab extends JPanel implements LRNN.TrainDataListener {

    LRNN net;
    private JLabel statusLabel;
    private int epochNum = 0;

    public TrainTab() {
        setName("Train");
        setLayout(new BorderLayout());

        setBackground(Color.DARK_GRAY);

        initlayout();
    }

    private void initlayout() {
        initButtons();

        statusLabel = new JLabel("Status: disabled");
        statusLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        statusLabel.setForeground(Color.WHITE);

        add(statusLabel);
    }

    private void initButtons() {
        JPanel buttonsHolder = new JPanel();
        buttonsHolder.setLayout(new BoxLayout(buttonsHolder, BoxLayout.Y_AXIS));
        DesignControl.setTransparent(buttonsHolder);

        //stopBtn
        JButton stopBtn = new JButton("Stop");

        //train btn
        JButton trainBtn = new JButton("Train");

        learningThread lt = new learningThread(trainBtn, stopBtn);

        stopBtn.addActionListener(e -> {
            stopBtn.setEnabled(false);
            lt.switchThread(true);
            statusLabel.setText("Net saved");
        });
        trainBtn.addActionListener(e -> {
            statusLabel.setText("Training...");
            lt.start();
        });

        DesignControl.setTransparent(buttonsHolder);

        buttonsHolder.add(trainBtn);
        buttonsHolder.add(stopBtn);
        add(buttonsHolder, BorderLayout.WEST);
    }

    void createSlider(String name, ChangeListener l) {
        JPanel container = new JPanel();
        container.setLayout(new FlowLayout());

        JSlider s = new JSlider();
        s.addChangeListener(l);
        container.add(new JLabel(name + ": "));
        container.add(s);
    }


    private void setButtonPanelText(Evaluation e) {
        String newLine = "<br>";

        String s = "";
        s += "<html>";
        s += "Net training running..." + newLine;
        s += "File: " + new File(ConfigManager.loadProperty("net-in")).getName();
        s += "Epoch: " + epochNum++ + newLine;
        s += "Accuracy: " + String.format("%.2f", e.accuracy()) + newLine;
        s += "_____________" + newLine;
        s += "</html>";

        statusLabel.setText(s);
    }

    @Override
    public void onGetStats(Evaluation evaluation) {
        setButtonPanelText(evaluation);
    }

    class learningThread extends Thread {
        private boolean running;
        private JButton trainBtn, stopBtn;

        public synchronized void switchThread(boolean stopped) {
            running = !stopped;
        }

        public learningThread(JButton trainBtn, JButton stopBtn) {
            this.trainBtn = trainBtn;
            this.stopBtn = stopBtn;

            stopBtn.setEnabled(false);
            trainBtn.setEnabled(true);
        }

        public void run() {
            running = true;
            trainBtn.setEnabled(false);
            stopBtn.setEnabled(true);

            if (net == null)
                net = new LRNN();

            epochNum = 0;
            net.trainNet(TrainTab.this, new IStopLearning() {
                @Override
                public boolean stopped() {
                    return !running;
                }
            });

            try {
                net.saveNet(new File(ConfigManager.loadProperty("net-out"), "network-data.nnd"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            trainBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        }
    }

    public interface IStopLearning {
        boolean stopped();
    }
}
