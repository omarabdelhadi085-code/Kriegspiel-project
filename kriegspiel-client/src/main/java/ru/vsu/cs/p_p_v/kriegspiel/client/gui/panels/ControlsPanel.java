package ru.vsu.cs.p_p_v.kriegspiel.client.gui.panels;

import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.*;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.BoardUnit;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.Arsenal;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class ControlsPanel extends JPanel {
    private final Game game;
    JLabel labelMyTeam = new JLabel();
    JLabel labelCurrentTurn = new JLabel();
    JLabel labelLeftMoves = new JLabel();
    JLabel labelIsAttackUsed = new JLabel();
    JLabel labelStatus = new JLabel();
    JButton buttonNextTurn = new JButton();

    public ControlsPanel(Game game) {
        this.game = game;
        this.setLayout(new GridBagLayout());
        this.setBorder(new EmptyBorder(5, 5, 5, 5));

        updateState();

        buttonNextTurn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                game.endTurn();
                checkAndRunAI();
            }
        });
        buttonNextTurn.setText("End turn");

        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        int gridY = 0;

        /*
         * if (game.isOnlineGame()) {
         * gbc.gridy = gridY++;
         * gbc.anchor = GridBagConstraints.CENTER;
         * container.add(labelMyTeam, gbc);
         * }
         */

        gbc.gridy = gridY++;
        gbc.anchor = GridBagConstraints.CENTER;
        container.add(labelCurrentTurn, gbc);

        gbc.gridy = gridY++;
        gbc.anchor = GridBagConstraints.CENTER;
        container.add(labelLeftMoves, gbc);
        labelLeftMoves.setBorder(new EmptyBorder(0, 0, 0, 1));

        gbc.gridy = gridY++;
        gbc.anchor = GridBagConstraints.CENTER;
        container.add(labelIsAttackUsed, gbc);

        gbc.gridy = gridY++;
        gbc.anchor = GridBagConstraints.CENTER;
        container.add(labelStatus, gbc);
        labelStatus.setFont(labelStatus.getFont().deriveFont(Font.BOLD));

        gbc.gridy = gridY++;
        gbc.anchor = GridBagConstraints.CENTER;
        container.add(buttonNextTurn, gbc);

        this.add(container);

        game.addGameEventListener(new GameEventListener() {
            @Override
            public void onNextTurn() {
                updateState();
            }

            @Override
            public void onUnitMove(MoveUnitResult result) {
                updateState();
            }

            @Override
            public void onAttack(AttackUnitResult result) {
                updateState();
            }
        });
    }

    public void updateState() {
        labelMyTeam.setText(String.format("My team: %s", game.getMyTeam()));

        Team currentTeam = game.getCurrentTurnTeam();
        labelCurrentTurn.setText(String.format("Current turn: %s", currentTeam));
        labelLeftMoves.setText(String.format("Unit movements left: %d", game.getLeftMoves()));
        labelIsAttackUsed.setText(String.format("Is attack used: %s", game.isAttackUsed() ? "Yes" : "No"));

        // Only enable if my team is current team AND it's a Human
        boolean isMyTurn = game.getMyTeam() == currentTeam;
        IPlayerController controller = game.getPlayerController(currentTeam);
        boolean isHuman = controller instanceof HumanPlayer;

        buttonNextTurn.setEnabled(isMyTurn && isHuman);
        
        // Update status label
        if (!isHuman && !isMyTurn) {
            labelStatus.setText(">> OPPONENT IS THINKING....");
            labelStatus.setForeground(Color.ORANGE);
        } else {
            labelStatus.setText("");
        }

        labelMyTeam.repaint();
        labelCurrentTurn.repaint();
        labelLeftMoves.repaint();
        labelIsAttackUsed.repaint();
        labelStatus.repaint();
    }

    /**
     * Check if the game has ended by checking if either team has no arsenals
     */
    private boolean isGameOver() {
        Board board = game.getBoard();
        if (board == null) {
            return false;
        }
        
        // Check if North has any arsenals
        boolean northHasArsenal = false;
        for (BoardUnit unit : board.getNorthUnits()) {
            if (unit instanceof Arsenal) {
                northHasArsenal = true;
                break;
            }
        }
        
        // Check if South has any arsenals
        boolean southHasArsenal = false;
        for (BoardUnit unit : board.getSouthUnits()) {
            if (unit instanceof Arsenal) {
                southHasArsenal = true;
                break;
            }
        }
        
        // Game is over if one team has no arsenals
        return !northHasArsenal || !southHasArsenal;
    }
    
    /**
     * Find and repaint the BoardPanel component
     */
    private void repaintBoardPanel() {
        // Find the parent GamePanel and then the BoardPanel
        Container parent = this.getParent();
        while (parent != null) {
            if (parent instanceof JPanel) {
                // Look for BoardPanel in the container
                Component[] components = ((JPanel) parent).getComponents();
                for (Component comp : components) {
                    if (comp.getClass().getSimpleName().equals("BoardPanel")) {
                        comp.repaint();
                        return;
                    }
                    // Also check nested containers
                    if (comp instanceof Container) {
                        findAndRepaintBoardPanel((Container) comp);
                    }
                }
            }
            parent = parent.getParent();
        }
    }
    
    /**
     * Recursively find BoardPanel and repaint it
     */
    private void findAndRepaintBoardPanel(Container container) {
        Component[] components = container.getComponents();
        for (Component comp : components) {
            if (comp.getClass().getSimpleName().equals("BoardPanel")) {
                comp.repaint();
                return;
            }
            if (comp instanceof Container) {
                findAndRepaintBoardPanel((Container) comp);
            }
        }
    }

    private void checkAndRunAI() {
        Team currentTeam = game.getCurrentTurnTeam();
        IPlayerController controller = game.getPlayerController(currentTeam);

        if (controller instanceof AIPlayer) {
            buttonNextTurn.setEnabled(false); // Lock UI button (though updateState helps too)
            
            // Update status to show AI is thinking
            labelStatus.setText(">> OPPONENT IS THINKING....");
            labelStatus.setForeground(Color.ORANGE);
            labelStatus.repaint();

            // Run AI in background
            SwingWorker<List<MoveCommand>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<MoveCommand> doInBackground() throws Exception {
                    // Calculate moves (heavy logic)
                    if (game instanceof GameState) {
                        return controller.getTurnActions((GameState) game);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        List<MoveCommand> moves = get();
                        if (moves != null && !moves.isEmpty()) {
                            // Process moves on background thread, but apply them on EDT
                            new Thread(() -> {
                                try {
                                    // Clear thinking message when moves start executing
                                    SwingUtilities.invokeLater(() -> {
                                        labelStatus.setText("");
                                        labelStatus.repaint();
                                    });
                                    
                                    // Apply moves one by one with delay
                                    for (MoveCommand cmd : moves) {
                                        // Check if game has ended before applying move
                                        if (isGameOver()) {
                                            break; // Stop applying moves if game ended
                                        }
                                        
                                        // Apply move on EDT (UI thread)
                                        SwingUtilities.invokeLater(() -> {
                                            cmd.execute(game);
                                            // Trigger repaint of the board panel
                                            repaintBoardPanel();
                                        });
                                        
                                        // CRITICAL: 300ms delay between moves for visual feedback
                                        // Sleep happens on background thread, outside invokeLater
                                        Thread.sleep(300);
                                    }
                                    
                                    // End AI turn (only if game hasn't ended)
                                    SwingUtilities.invokeLater(() -> {
                                        if (!isGameOver()) {
                                            game.endTurn();
                                            // Check if next is also AI? (e.g. AI vs AI)
                                            // Recursion check:
                                            checkAndRunAI();
                                        }
                                    });
                                } catch (InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }).start();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            worker.execute();
        }
    }
}
