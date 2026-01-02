package ru.vsu.cs.p_p_v.kriegspiel.client.gui;

import ru.vsu.cs.p_p_v.kriegspiel.client.gui.panels.*;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.*;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.Game;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.GameEventListener;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.Team;

import javax.swing.*;
import java.nio.file.Path;

public class GUI {
    Game game;
    MainWindow window;

    public void run() {
        window = new MainWindow();
        window.setLocationRelativeTo(null);

        showMenu();
    }

    public void showMenu() {
        MenuPanel menuPanel = new MenuPanel(this);

        window.setMainPanel(menuPanel);
        window.setVisible(true);
    }

    public void showGameResults(Team winner) {
        GameResultsPanel resultsPanel = new GameResultsPanel(this, winner);

        window.setMainPanel(resultsPanel);
    }

    public void startNewLocalGame() {
        game = new LocalGame(Path.of("field.json"), Path.of("units.json"), new HumanPlayer(), new AIPlayer());

        window.setMainPanel(new GamePanel(game));

        game.addGameEventListener(new GameEventListener() {
            @Override
            public void onWin(Team winner) {
                showGameResults(winner);
            }
        });
    }

    public void exit() {
        window.dispose();
    }
}
