package ru.vsu.cs.p_p_v.kriegspiel.client.gui;

import ru.vsu.cs.p_p_v.kriegspiel.sdk.cache.ImageFileCached;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainWindow extends JFrame {
    public MainWindow() throws HeadlessException {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Kriegspiel");

        Image icon = ImageFileCached.readImage(new File("images/icon.png"));
        this.setIconImage(icon);

        setSize(750, 500);
    }

    public void setMainPanel(JPanel panel) {
        JPanel content = (JPanel) this.getContentPane();
        content.removeAll();
        content.add(panel);

        this.revalidate();
        this.repaint();
    }
}


