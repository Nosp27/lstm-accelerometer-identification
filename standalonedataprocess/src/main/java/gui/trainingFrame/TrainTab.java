package gui.trainingFrame;

import accelTest.LRNN;
import configWork.ConfigManager;
import gui.components.DesignedButton;
import gui.components.DesignedLabel;
import gui.components.DesignedPanel;
import gui.graph.GraphRender;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class TrainTab extends DesignedPanel implements LRNN.TrainDataListener {

    private final Boolean netLock = true;//sync object
    volatile LRNN net;
    volatile boolean netCleared;

    private JLabel trainingLabel;
    private JLabel statusLabel;
    private JLabel netInfo;
    private int epochNum = 0;

    private GraphRender gr;

    JButton resetBtn;

    public TrainTab() {
        setName("Train");
        setLayout(new BorderLayout(10, 15));

        initlayout();
    }

    private void initlayout() {
        System.out.println("train tab layout");
        initButtons();
        System.out.println("train tab init buttons complete");
        JPanel infoPanel = new DesignedPanel(BG);
        infoPanel.setLayout(new GridLayout(2, 2, 15, 15));
        trainingLabel = new DesignedLabel();
        statusLabel = new DesignedLabel("Status: disabled");
        netInfo = new DesignedLabel("Neural network info");
        infoPanel.add(statusLabel);
        infoPanel.add(trainingLabel);
        infoPanel.add(netInfo);
        infoPanel.add(gr = GraphRender.getBottomTrimmedGraph());
        add(infoPanel, BorderLayout.CENTER);
    }

    private void initButtons() {
        JPanel buttonsHolder = new DesignedPanel(PRIMARY);
        buttonsHolder.setLayout(new BoxLayout(buttonsHolder, BoxLayout.Y_AXIS));

        //stopBtn
        JButton stopBtn = new DesignedButton("Stop");

        //train btn
        JButton trainBtn = new DesignedButton("Train");

        System.out.println("learning thread launch");
        learningThread lt = new learningThread(trainBtn, stopBtn);
        System.out.println("learning thread launch complete");

        stopBtn.addActionListener(e -> {
            stopBtn.setEnabled(false);
            lt.switchThread(true);
            trainingLabel.setText("Net saved");
        });
        trainBtn.addActionListener(e -> {
            trainingLabel.setText("Training...");
            if (lt.getState() == Thread.State.NEW)
                lt.start();
            else lt.switchThread(false);
        });

        String net_outPath = ConfigManager.loadProperty("net-out");
        resetBtn = new DesignedButton("Reset");
        resetBtn.addActionListener(e -> {
            new Thread(() -> {
                synchronized (netLock) {
                    if (net != null) net.getNet().clear();
                    new File(net_outPath).delete();
                    statusLabel.setText("Network resets");
                    epochNum = 0;
                    resetBtn.setEnabled(false);
                }
                gr.reset();
            }).start();
        });
        resetBtn.setEnabled(new File(net_outPath).exists());

        buttonsHolder.add(trainBtn);
        buttonsHolder.add(Box.createVerticalStrut(10));
        buttonsHolder.add(stopBtn);
        buttonsHolder.add(Box.createVerticalStrut(30));
        buttonsHolder.add(resetBtn);
        add(buttonsHolder, BorderLayout.WEST);
    }

    private String getNetInfo(MultiLayerNetwork network, File loadedFrom) {
        String s = "";
        s += "<html>";
        if (loadedFrom != null)
            s += "Network loaded from .../" + loadedFrom.getName() + "<br>";
        s += "Neural network configuration:<br>" +
                "Layers: " + network.getnLayers() + "<br>" +
                "Neurons: <br>" +
                "Input size: " + network.layerInputSize(0) + "<br>";

        for (int i = 0; i < network.getnLayers() - 2; i++)
            s += "Hidden layer " + (i + 1) + " size: " + network.layerInputSize(i + 1) + "<br>";

        s += "Output layer size: " + network.layerSize(2) + "<br>";
        s += "</html>";
        return s;
    }

    private void setButtonPanelText(Evaluation e) {
        if (trainingLabel.getText().endsWith("saved"))
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

        trainingLabel.setText(s);
    }

    @Override
    public void onGetStats(Evaluation evaluation) {
        setButtonPanelText(evaluation);
        gr.addPoint(evaluation.accuracy());
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

            File loadedFrom = new File(ConfigManager.loadProperty("net-out"));
            synchronized (netLock) {
                if (net == null) {
                    net = new LRNN();
                    try {
                        net.loadNet(loadedFrom);
                    } catch (IOException e) {
                        loadedFrom = null;
                    }
                    netInfo.setText(getNetInfo(net.getNet(), loadedFrom));
                }
            }

            epochNum = 0;
            while (true) {
                synchronized (netLock) {
                    if(loadedFrom != null && !loadedFrom.exists())
                        loadedFrom = null;

                    if (loadedFrom != null)
                        statusLabel.setText("Status: loaded from .../" + loadedFrom.getName() + ", running");
                    else statusLabel.setText("Status: running");

                    net.trainNet(TrainTab.this, new IStopLearning() {
                        @Override
                        public boolean stopped() {
                            return !running;
                        }
                    });

                    try {
                        loadedFrom = new File(ConfigManager.loadProperty("net-out"));
                        net.saveNet(loadedFrom);
                        statusLabel.setText("Status: Net stopped. Saved to " + loadedFrom.getName());
                    } catch (IOException e) {
                        statusLabel.setText("Status: Net stopped. Saving to " + loadedFrom.getName() + " aborted");
                        loadedFrom = null;
                    }
                    resetBtn.setEnabled(true);
                }

                trainBtn.setEnabled(true);
                stopBtn.setEnabled(false);

                try {
                    while (!running)
                        Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                trainBtn.setEnabled(false);
                stopBtn.setEnabled(true);
            }
        }
    }

    public interface IStopLearning {
        boolean stopped();
    }
}
