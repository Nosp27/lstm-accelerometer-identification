package gui;

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

    Dimension screen;
    JPanel holderPanel;
    CardLayout tabHolder;
    File learningDataDirectory;

    ConfigFrame configs = new ConfigFrame();

    public DataPrepareWnd() {
        setName("Server Window");
        DesignControl.setBackgroundImage(this);
        screen = Toolkit.getDefaultToolkit().getScreenSize();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(new Dimension(screen.width / 2, screen.height / 2));

        getContentPane().setLayout(new BorderLayout(20, 30));
        tabHolder = new CardLayout();
        getContentPane().add((holderPanel = new JPanel(tabHolder)), BorderLayout.CENTER);

        initTabs();

        createMenu();

        setVisible(true);
    }

    private void createMenu() {
        JMenuBar menubar = new JMenuBar();
        JMenu menu;
        //Settings
        menu = new JMenu("Settings");
        JMenuItem config = menu.add("Config");
        config.addActionListener(e->configs.setVisible(true));
        menubar.add(menu);

        //Tabs
        menu = new JMenu("View");
        menubar.add(menu);
        createTabMenuItems(menu);


        add(menubar, BorderLayout.NORTH);
    }

    private void initTabs() {
        tabs = new ArrayList<>();
        tabs.add(new DataLoadTab(this));
        tabs.add(new TrainTab());
        tabs.add(new ServerControlTab());

        for (JPanel t : tabs) {
            tabHolder.addLayoutComponent(t, t.getName());
            holderPanel.add(t);
        }
    }

    private void createTabMenuItems(JMenu menu) {
        for (int i = 0; i < tabs.size(); i++) {
            JButton btn = new JButton();
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
