package ru.vsu.cs.p_p_v.kriegspiel.client.gui.panels;

import ru.vsu.cs.p_p_v.kriegspiel.client.gui.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MenuPanel extends JPanel {
    private GUI gui;

    public MenuPanel(GUI gui) {
        this.gui = gui;
        this.setLayout(new GridBagLayout());

        JPanel buttonPanel = createButtonPanel();

        JLabel logoText = new JLabel("Kriegspiel");
        logoText.setFont(logoText.getFont().deriveFont(50F));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NORTH;

        gbc.gridy = 0;
        gbc.weighty = 0.333;
        this.add(logoText, gbc);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 0.666;
        gbc.weightx = 1;
        this.add(buttonPanel, gbc);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        JButton buttonNewLocal = new JButton("Play");
        JButton buttonExit = new JButton("Exit");

        buttonNewLocal.setPreferredSize(new Dimension(180, 25));
        buttonExit.setPreferredSize(new Dimension(180, 25));

        buttonNewLocal.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.startNewLocalGame();
            }
        });
        buttonExit.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.exit();
            }
        });

        GridBagConstraints gbcButtons = new GridBagConstraints();
        gbcButtons.insets = new Insets(5, 0, 0, 0);

        gbcButtons.gridy = 0;
        buttonPanel.add(buttonNewLocal, gbcButtons);
        gbcButtons.gridy = 1;
        buttonPanel.add(buttonExit, gbcButtons);

        return buttonPanel;
    }
}
