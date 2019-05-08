package gui.trainingFrame;

import accelTest.LRNN;
import configWork.ConfigManager;
import gui.DesignControl;
import gui.components.DesignedButton;
import gui.components.DesignedPanel;
import org.deeplearning4j.eval.Evaluation;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class TrainTab extends DesignedPanel implements LRNN.TrainDataListener {

    LRNN net;
    private JLabel trainingInfo;
    private JLabel statusLabel;
    private int epochNum = 0;

    public TrainTab() {
        super("C:\\Users\\Nosp\\IdeaProjects\\NetworkTest\\standalonedataprocess\\src\\main\\resources\\bg.jpg");
        setName("Train");
        setLayout(new BorderLayout(15,15));

        initlayout();
    }

    private void initlayout() {
        initButtons();

        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        infoPanel.setBackground(Color.DARK_GRAY);

        trainingInfo = new JLabel();
        trainingInfo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        trainingInfo.setForeground(Color.WHITE);

        statusLabel = new JLabel("Status: disabled");
        statusLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        statusLabel.setForeground(Color.WHITE);

        infoPanel.add(statusLabel);
        infoPanel.add(trainingInfo);
        add(infoPanel,BorderLayout.CENTER);
    }

    private void initButtons() {
        JPanel buttonsHolder = new JPanel();
        buttonsHolder.setLayout(new BoxLayout(buttonsHolder, BoxLayout.Y_AXIS));
        DesignControl.setTransparent(buttonsHolder);

        //stopBtn
        JButton stopBtn = new DesignedButton("Stop");

        //train btn
        JButton trainBtn = new DesignedButton("Train");

        learningThread lt = new learningThread(trainBtn, stopBtn);

        stopBtn.addActionListener(e -> {
            stopBtn.setEnabled(false);
            lt.switchThread(true);
            trainingInfo.setText("Net saved");
        });
        trainBtn.addActionListener(e -> {
            trainingInfo.setText("Training...");
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
        if(trainingInfo.getText().endsWith("saved"))
            return;
        String newLine = "<br>";

        String s = "";
        s += "<html>";
        s += "Net training running..." + newLine;
        s += "File: " + new File(ConfigManager.loadProperty("net-in")).getName();
        s += "Epoch: " + epochNum++ + newLine;
        s += "Accuracy: " + String.format("%.2f", e.accuracy()) + newLine;
        s += "_____________" + newLine;
        s += "</html>";

        trainingInfo.setText(s);
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
            statusLabel.setText("Status: running");

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
                File f = new File(ConfigManager.loadProperty("net-out"), "network-data.nnd");
                net.saveNet(f);
                statusLabel.setText("Status: Net stopped. Saved to " + f.getName());
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
