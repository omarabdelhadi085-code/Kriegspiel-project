package ru.vsu.cs.p_p_v.kriegspiel.sdk.game;

import ru.vsu.cs.p_p_v.kriegspiel.sdk.cell.BoardCell;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.stats.UnitCombatStats;

public interface GameState {
    Team getCurrentTurnTeam();

    Team getMyTeam();

    int getLeftMoves();

    boolean isAttackUsed();

    int getBoardSizeX();

    int getBoardSizeY();

    BoardCell getBoardCell(Coordinate cellCoordinate);

    Board getBoard();

    UnitCombatStats getUnitCombatStats(Coordinate unitCoordinate);
}
