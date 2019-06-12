package gui;

import gui.components.DesignedPanel;
import gui.configs.ConfigFrame;
import gui.tabs.VisualisationTab;
import gui.tabs.HelpTab;
import gui.tabs.loadDataTab.DataLoadTab;
import gui.tabs.serverControlTab.ServerControlTab;
import gui.trainingFrame.TrainTab;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class ServerMainframe extends JFrame implements DataLoadTab.DataLoaderListener {

    //load, train, remote
    private ArrayList<JPanel> tabs;
    private ConfigFrame configs;
    private Dimension screen;
    private JPanel holderPanel;
    private CardLayout tabHolder;
    private JPanel mainPanel;
    private JPanel infoPanel;

    public ServerMainframe() {
        try {
            configs = new ConfigFrame();
            try {
                File f = new File("log.log");
                f.createNewFile();
                System.setOut(new PrintStream(f));
            } catch (IOException e) {
                e.printStackTrace();
            }
            setTitle("Neural net processing");
            screen = Toolkit.getDefaultToolkit().getScreenSize();
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setSize(new Dimension(screen.width / 2, screen.height / 2));
            setMinimumSize(new Dimension(600, 400));
            getContentPane().setLayout(new BorderLayout());
            tabHolder = new CardLayout();
            holderPanel = new DesignedPanel();
            mainPanel = new DesignedPanel(DesignedPanel.BG);
            mainPanel.setLayout(new BorderLayout());
            holderPanel.setLayout(tabHolder);
            mainPanel.add(holderPanel, BorderLayout.CENTER);
            infoPanel = new JPanel(new GridLayout(1, 3, 20, 20));
            initTabs();
            System.out.println("5");
            createMenu();
            System.out.println("6");
            add(infoPanel, BorderLayout.NORTH);
            add(mainPanel, BorderLayout.CENTER);
            setVisible(true);
        } catch (Throwable e){
            e.printStackTrace(System.out);
        }
    }

    private void createMenu() {
        JMenuBar menubar = new JMenuBar();
        menubar.setBackground(DesignedPanel.PRIMARY);
        menubar.setForeground(Color.WHITE);

        JMenu menu;
        //Settings
        menu = new JMenu("Settings");
        menu.setBackground(DesignedPanel.PRIMARY);
        menu.setForeground(Color.WHITE);
        JMenuItem config = menu.add("Config");
        config.addActionListener(e -> configs.setVisible(true));
        config.setBackground(DesignedPanel.PRIMARY);
        config.setForeground(Color.WHITE);
        menubar.add(menu);

        //Tabs
        menu = new JMenu("View");
        menu.setBackground(DesignedPanel.PRIMARY);
        menu.setForeground(Color.WHITE);
        menubar.add(menu);
        createTabMenuItems(menu);


        mainPanel.add(menubar, BorderLayout.NORTH);
    }

    private void initTabs() {
        tabs = new ArrayList<>();
        tabs.add(new HelpTab());
        System.out.println("help tab ready");
        tabs.add(new DataLoadTab(this));
        System.out.println("data tab ready");
        tabs.add(new TrainTab());
        System.out.println("train tab ready");
        tabs.add(new ServerControlTab());
        System.out.println("server tab ready");
        tabs.add(new VisualisationTab());
        System.out.println("visualization tab ready");


        for (JPanel t : tabs) {
            holderPanel.add(t);
            tabHolder.addLayoutComponent(t, t.getName());
            System.out.println("added " + t.getName());
        }
    }

    private void createTabMenuItems(JMenu menu) {
        for (int i = 0; i < tabs.size(); i++) {
            JPanel t = tabs.get(i);
            JMenuItem item = menu.add(t.getName());
            item.addActionListener(e -> tabHolder.show(holderPanel, t.getName()));
            item.setBackground(DesignedPanel.PRIMARY);
            item.setForeground(Color.WHITE);
        }
    }

    public static void main(String[] args) {
        new ServerMainframe();
    }

    @Override
    public void onDataChosen(File _targetDirectory) {

    }
}
