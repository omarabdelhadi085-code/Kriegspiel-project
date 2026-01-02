package ru.vsu.cs.p_p_v.kriegspiel.client.gui;

import ru.vsu.cs.p_p_v.kriegspiel.client.gui.panels.BoardPanel;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.cell.BoardCell;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.Game;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.BoardUnit;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.stats.UnitBaseStats;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.stats.UnitCombatStats;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UnitStatsOverlay {
    private final int intervalToShowOverlayMs = 1000;

    private final BoardPanel boardPanel;
    private final Game game;

    private Point mousePoint = null;
    private long hoverTimeMs = 0;
    private boolean shouldDraw = false;
    private BoardCell hoveredCell = null;
    private BoardUnit hoveredUnit = null;
    private UnitBaseStats hoveredUnitBaseStats = null;
    private UnitCombatStats hoveredUnitCombatStats = null;

    public UnitStatsOverlay(BoardPanel boardPanel) {
        this.boardPanel = boardPanel;
        this.game = boardPanel.getGame();

        this.boardPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                updateHoveredCell(null);
                boardPanel.repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mousePoint = e.getPoint();

                BoardCell currentlyHoveredCell = boardPanel.getCellByPanelPoint(e.getPoint());
                if (currentlyHoveredCell != hoveredCell) {
                    updateHoveredCell(currentlyHoveredCell);
                } else if (shouldDraw) {
                    boardPanel.repaint();
                }
            }
        });

        Timer hoverTimeUpdater = new Timer(50, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hoveredCell != null) {
                    hoverTimeMs += 50;
                    if (hoverTimeMs >= intervalToShowOverlayMs && !shouldDraw) {
                        shouldDraw = true;

                        hoveredUnit = hoveredCell.getUnit();
                        if (hoveredUnit != null) {
                            hoveredUnitBaseStats = hoveredUnit.getBaseStats();
                            hoveredUnitCombatStats = game.getUnitCombatStats(hoveredUnit.getPosition());
                        }

                        boardPanel.repaint();
                    }
                }
            }
        });
        hoverTimeUpdater.start();
    }

    private void updateHoveredCell(BoardCell newHoveredCell) {
        hoveredCell = newHoveredCell;
        hoveredUnitBaseStats = null;
        hoveredUnitCombatStats = null;
        hoverTimeMs = 0;

        if (shouldDraw) {
            shouldDraw = false;
            boardPanel.repaint();
        }
    }

    private JTextPane getTextPaneForStats(String text) {
        SimpleAttributeSet aSetStats = new SimpleAttributeSet();
        StyleConstants.setFontSize(aSetStats, 10);
        StyleConstants.setLineSpacing(aSetStats, -0.25F);

        JTextPane textPaneStats = new JTextPane();
        textPaneStats.setEditable(false);
        textPaneStats.setText(text);
        textPaneStats.selectAll();
        textPaneStats.setParagraphAttributes(aSetStats, false);
        textPaneStats.setMargin(new Insets(0, 1, 3, 0));

        return textPaneStats;
    }

    public void draw(Graphics2D g) {
        if (!shouldDraw || hoveredUnit == null)
            return;

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new LineBorder(Color.black, 1));
        panel.setBackground(Color.white);

        JLabel labelName = new JLabel(hoveredUnit.getName());
        labelName.setFont(g.getFont().deriveFont(14.0F));
        labelName.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(labelName);

        JSeparator separator = new JSeparator();
        separator.setForeground(Color.black);
        separator.setBackground(Color.black);
        panel.add(separator);

        JLabel labelStats = new JLabel("Stats");
        labelStats.setFont(g.getFont().deriveFont(12.0F));
        labelStats.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(labelStats);

        String stats = String.format("Attack: %d\nDefense: %d\nRange: %d\nSpeed: %d",
                hoveredUnitBaseStats.attack,
                hoveredUnitBaseStats.defense,
                hoveredUnitBaseStats.range,
                hoveredUnitBaseStats.speed);
        JTextPane textPaneStats = getTextPaneForStats(stats);
        panel.add(textPaneStats);

        JLabel labelCombat = new JLabel("Combat");
        labelCombat.setBorder(new EmptyBorder(0, 0, 0, 0));
        labelCombat.setFont(g.getFont().deriveFont(12.0F));
        labelCombat.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(labelCombat);

        String combat = String.format("Attack: %d\nDefense: %d",
                hoveredUnitCombatStats.attackScore,
                hoveredUnitCombatStats.defenseScore);
        JTextPane textPaneCombat = getTextPaneForStats(combat);
        panel.add(textPaneCombat);

        panel.setDoubleBuffered(false);
        panel.setSize(panel.getPreferredSize());
        panel.doLayout();

        Point worldMousePoint = boardPanel.pointScreenToWorld(mousePoint);

        int x = Math.min(boardPanel.getBoardWidth() - panel.getWidth(), worldMousePoint.x + 5);
        int y = Math.max(0, worldMousePoint.y - panel.getHeight());
        g.translate(x, y);

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        panel.paint(g);
    }
}
