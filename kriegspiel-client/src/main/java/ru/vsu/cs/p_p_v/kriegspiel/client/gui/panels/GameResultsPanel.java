package ru.vsu.cs.p_p_v.kriegspiel.client.gui.panels;

import ru.vsu.cs.p_p_v.kriegspiel.client.gui.GUI;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.Team;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GameResultsPanel extends JPanel {
    private GUI gui;

    public GameResultsPanel(GUI gui, Team winner) {
        this.gui = gui;
        this.setLayout(new GridBagLayout());

        JLabel labelInfo = new JLabel(String.format("The %s team won!", winner));
        labelInfo.setFont(labelInfo.getFont().deriveFont(50F));

        JPanel buttonsPanel = createButtonPanel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NORTH;

        gbc.gridy = 0;
        gbc.weighty = 0.5;
        this.add(labelInfo, gbc);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 0.5;
        gbc.weightx = 1;
        this.add(buttonsPanel, gbc);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        JButton buttonReturnToMainMenu = new JButton("Return to main menu");

        buttonReturnToMainMenu.setPreferredSize(new Dimension(180, 25));

        buttonReturnToMainMenu.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.showMenu();
            }
        });

        GridBagConstraints gbcButtons = new GridBagConstraints();
        gbcButtons.insets = new Insets(5, 0, 0, 0);

        gbcButtons.gridy = 0;
        buttonPanel.add(buttonReturnToMainMenu, gbcButtons);

        return buttonPanel;
    }
}
