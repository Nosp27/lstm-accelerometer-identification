package gui;

import gui.components.DesignedButton;
import gui.components.DesignedPanel;
import gui.configs.ConfigFrame;
import gui.tabs.loadDataTab.DataLoadTab;
import gui.tabs.serverControlTab.ServerControlTab;
import gui.trainingFrame.TrainTab;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class DataPrepareWnd extends JFrame implements DataLoadTab.DataLoaderListener {

    //load, train, remote
    ArrayList<JPanel> tabs;

    File learningDataDirectory;

    ConfigFrame configs = new ConfigFrame();
    Dimension screen;
    JPanel holderPanel;
    CardLayout tabHolder;
    JPanel mainPanel;
    JPanel infoPanel;

    public DataPrepareWnd() {
        setName("Server Window");
        DesignControl.setBackgroundImage(this);
        screen = Toolkit.getDefaultToolkit().getScreenSize();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(new Dimension(screen.width / 2, screen.height / 2));

        getContentPane().setLayout(new BorderLayout());

        tabHolder = new CardLayout();
        holderPanel = new DesignedPanel();

        mainPanel = new DesignedPanel("C:\\Users\\Nosp\\IdeaProjects\\NetworkTest\\standalonedataprocess\\src\\main\\resources\\bg.jpg");
        mainPanel.setLayout(new BorderLayout());
        holderPanel.setLayout(tabHolder);
        mainPanel.add(holderPanel, BorderLayout.CENTER);

        infoPanel = new JPanel(new GridLayout(1, 3, 20, 20));


        initTabs();

        createMenu();

        add(infoPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void createMenu() {
        JMenuBar menubar = new JMenuBar();
        JMenu menu;
        //Settings
        menu = new JMenu("Settings");
        JMenuItem config = menu.add("Config");
        config.addActionListener(e -> configs.setVisible(true));
        menubar.add(menu);

        //Tabs
        menu = new JMenu("View");
        menubar.add(menu);
        createTabMenuItems(menu);


        mainPanel.add(menubar, BorderLayout.NORTH);
    }

    private void initTabs() {
        tabs = new ArrayList<>();
        tabs.add(new DataLoadTab(this));
        tabs.add(new TrainTab());
        tabs.add(new ServerControlTab());

        for (JPanel t : tabs) {
            holderPanel.add(t);
            tabHolder.addLayoutComponent(t, t.getName());
        }
    }

    private void createTabMenuItems(JMenu menu) {
        for (int i = 0; i < tabs.size(); i++) {
            DesignedButton btn = new DesignedButton();
            JPanel t = tabs.get(i);

            btn.setText(t.getName());
            btn.addActionListener(e -> tabHolder.show(holderPanel, t.getName()));

            menu.add(t.getName()).addActionListener(e -> tabHolder.show(holderPanel, t.getName()));
        }
    }

    public static void main(String[] args) {
        new DataPrepareWnd();
    }

    @Override
    public void onDataChosen(File _targetDirectory) {
        learningDataDirectory = _targetDirectory;
    }
}
