package ru.vsu.cs.p_p_v.kriegspiel.sdk.game;

import ru.vsu.cs.p_p_v.kriegspiel.sdk.cell.BoardCell;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.*;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.stats.UnitBaseStats;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.stats.UnitCombatStats;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class LocalGame implements Game {
    protected final Board board;

    private Team currentTurnTeam = Team.South;
    private int leftMoves = 5;
    private final List<BoardUnit> movedUnits = new ArrayList<>();
    private boolean isAttackUsed = false;

    private final List<GameEventListener> eventListeners = new ArrayList<>();

    private final IPlayerController player1;
    private final IPlayerController player2;

    public LocalGame(Path fieldJson, Path unitsJson, IPlayerController player1, IPlayerController player2) {
        this.player1 = player1;
        this.player2 = player2;
        board = new Board();

        try {
            board.appendFieldFromFile(fieldJson);
            board.appendUnitsFromFile(unitsJson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        board.updateConnections();
    }

    public void processTurn() {
        IPlayerController currentPlayer = currentTurnTeam == Team.South ? player1 : player2;
        List<MoveCommand> actions = currentPlayer.getTurnActions(this);
        if (actions != null) {
            for (MoveCommand cmd : actions) {
                cmd.execute(this);
            }
        }
    }

    public Team getCurrentTurnTeam() {
        return currentTurnTeam;
    }

    @Override
    public Team getMyTeam() {
        return currentTurnTeam;
    }

    // @Override
    // public boolean isOnlineGame() {
    // return false;
    // }

    public int getLeftMoves() {
        return leftMoves;
    }

    public boolean isAttackUsed() {
        return isAttackUsed;
    }

    public void endTurn() {
        leftMoves = 5;
        movedUnits.clear();
        isAttackUsed = false;
        currentTurnTeam = currentTurnTeam == Team.North ? Team.South : Team.North;

        for (GameEventListener listener : eventListeners)
            listener.onNextTurn();
    }

    public int getBoardSizeX() {
        return board.xSize;
    }

    public int getBoardSizeY() {
        return board.ySize;
    }

    public BoardCell getBoardCell(Coordinate cellCoordinate) {
        return board.getCell(cellCoordinate);
    }

    private Team getWinner() {
        boolean isSouthWon = true;
        for (BoardUnit unit : board.getNorthUnits()) {
            if (unit instanceof Arsenal) {
                isSouthWon = false;
                break;
            }
        }
        if (isSouthWon)
            return Team.South;

        boolean isNorthWon = true;
        for (BoardUnit unit : board.getSouthUnits()) {
            if (unit instanceof Arsenal) {
                isNorthWon = false;
                break;
            }
        }
        if (isNorthWon)
            return Team.North;

        return Team.None;
    }

    public boolean unitCanMove(Coordinate unitCoordinate) {
        BoardCell unitCell = board.getCell(unitCoordinate);
        if (unitCell == null)
            return false;

        BoardUnit unit = unitCell.getUnit();

        return leftMoves != 0 && !movedUnits.contains(unit) && unit.hasConnection();
    }

    public void moveUnit(Coordinate unitCoordinate, Coordinate destCellCoordinate) {
        if (leftMoves == 0) {
            triggerMoveEvent(MoveUnitResult.NoMovementsLeft);
            return;
        }

        BoardCell unitCell = board.getCell(unitCoordinate);
        if (unitCell == null) {
            triggerMoveEvent(MoveUnitResult.IncorrectUnitCoordinates);
            return;
        }

        BoardUnit unit = unitCell.getUnit();
        if (unit == null) {
            triggerMoveEvent(MoveUnitResult.IncorrectUnitCoordinates);
            return;
        }
        if (movedUnits.contains(unit)) {
            triggerMoveEvent(MoveUnitResult.UnitAlreadyMoved);
            return;
        }
        if (unit.getTeam() != currentTurnTeam) {
            triggerMoveEvent(MoveUnitResult.UnitInDifferentTeam);
            return;
        }
        if (!unit.hasConnection()) {
            triggerMoveEvent(MoveUnitResult.NoConnection);
            return;
        }

        int unitSpeed = unit.getBaseStats().speed;
        if (Math.abs(unitCoordinate.x - destCellCoordinate.x) > unitSpeed
                || Math.abs(unitCoordinate.y - destCellCoordinate.y) > unitSpeed) {
            triggerMoveEvent(MoveUnitResult.UnitLackSpeed);
            return;
        }

        if (!unit.move(destCellCoordinate)) {
            triggerMoveEvent(MoveUnitResult.IncorrectCellCoordinates);
            return;
        }

        leftMoves--;
        movedUnits.add(unit);
        board.updateConnections();

        triggerMoveEvent(MoveUnitResult.Success);
    }

    private void triggerMoveEvent(MoveUnitResult result) {
        for (GameEventListener listener : eventListeners)
            listener.onUnitMove(result);
    }

    public boolean unitCanBeCaptured(Coordinate unitCoordinate) {
        BoardCell cell = board.getCell(unitCoordinate);
        BoardUnit cellUnit = cell.getUnit();
        if (cellUnit == null) {
            return false;
        }

        return getUnitAttackScore(cellUnit) - getUnitDefenseScore(cellUnit) >= 2 && !isAttackUsed;
    }

    @Override
    public UnitCombatStats getUnitCombatStats(Coordinate unitCoordinate) {
        BoardCell unitCell = board.getCell(unitCoordinate);
        if (unitCell == null) {
            return null;
        }

        BoardUnit unit = unitCell.getUnit();
        if (unit == null) {
            return null;
        }

        return new UnitCombatStats(getUnitDefenseScore(unit), getUnitAttackScore(unit));
    }

    private int getUnitDefenseScore(BoardUnit unit) {
        List<BoardUnit> friendlyUnits = new ArrayList<>();
        switch (unit.getTeam()) {
            case South -> friendlyUnits = board.getSouthUnits();
            case North -> friendlyUnits = board.getNorthUnits();
        }

        Coordinate unitPos = unit.getPosition();
        int defenseScore = unit.hasConnection() ? unit.getBaseStats().defense : 0;
        defenseScore += unit.getDefenseBuff();

        for (BoardUnit nextUnit : friendlyUnits) {
            if (nextUnit == unit || !nextUnit.hasConnection())
                continue;

            Coordinate nextUnitPos = nextUnit.getPosition();
            UnitBaseStats nextUnitBaseStats = nextUnit.getBaseStats();

            int nextUnitRange = nextUnitBaseStats.range;
            if (unitPos.x == nextUnitPos.x || unitPos.y == nextUnitPos.y
                    || Math.abs(unitPos.x - nextUnitPos.x) == Math.abs(unitPos.y - nextUnitPos.y)) {
                if (Math.abs(unitPos.x - nextUnitPos.x) <= nextUnitRange
                        && Math.abs(unitPos.y - nextUnitPos.y) <= nextUnitRange) {
                    defenseScore += nextUnitBaseStats.defense;
                }
            }
        }

        return defenseScore;
    }

    private int getUnitAttackScore(BoardUnit unit) {
        List<BoardUnit> hostileUnits = new ArrayList<>();
        switch (unit.getTeam()) {
            case South -> hostileUnits = board.getNorthUnits();
            case North -> hostileUnits = board.getSouthUnits();
        }

        Coordinate unitPos = unit.getPosition();
        int attackScore = 0;
        for (BoardUnit nextUnit : hostileUnits) {
            if (!nextUnit.hasConnection())
                continue;

            Coordinate nextUnitPos = nextUnit.getPosition();
            UnitBaseStats nextUnitBaseStats = nextUnit.getBaseStats();

            int nextUnitRange = nextUnitBaseStats.range;
            if (unitPos.x == nextUnitPos.x || unitPos.y == nextUnitPos.y
                    || Math.abs(unitPos.x - nextUnitPos.x) == Math.abs(unitPos.y - nextUnitPos.y)) {
                if (Math.abs(unitPos.x - nextUnitPos.x) <= nextUnitRange
                        && Math.abs(unitPos.y - nextUnitPos.y) <= nextUnitRange) {
                    attackScore += nextUnitBaseStats.attack;
                }
            }
        }

        return attackScore;
    }

    public void attackUnit(Coordinate unitCoordinate) {
        if (isAttackUsed) {
            triggerAttackEvent(AttackUnitResult.NoAttackLeft);
            return;
        }

        BoardCell unitCell = board.getCell(unitCoordinate);
        if (unitCell == null) {
            triggerAttackEvent(AttackUnitResult.IncorrectUnitCoordinates);
            return;
        }

        BoardUnit unit = unitCell.getUnit();
        if (unit == null) {
            triggerAttackEvent(AttackUnitResult.IncorrectUnitCoordinates);
            return;
        }
        if (unit.getTeam() == currentTurnTeam) {
            triggerAttackEvent(AttackUnitResult.UnitInSameTeam);
            return;
        }

        UnitCombatStats combatStats = getUnitCombatStats(unit.getPosition());

        AttackUnitResult result;
        if (combatStats.attackScore - combatStats.defenseScore == 1) {
            result = AttackUnitResult.ForcedRetreat;
        } else if (combatStats.attackScore - combatStats.defenseScore >= 2) {
            board.removeUnit(unit);
            board.updateConnections();
            isAttackUsed = true;
            result = AttackUnitResult.Capture;
        } else {
            result = AttackUnitResult.None;
        }

        triggerAttackEvent(result);

        Team winner = getWinner();
        if (winner != Team.None) {
            for (GameEventListener listener : eventListeners)
                listener.onWin(winner);
        }
    }

    private void triggerAttackEvent(AttackUnitResult result) {
        for (GameEventListener listener : eventListeners)
            listener.onAttack(result);
    }

    public void addGameEventListener(GameEventListener gameListener) {
        this.eventListeners.add(gameListener);
    }

    public Board getBoard() {
        return board;
    }

    @Override
    public IPlayerController getPlayerController(Team team) {
        return team == Team.South ? player1 : player2;
    }
}
