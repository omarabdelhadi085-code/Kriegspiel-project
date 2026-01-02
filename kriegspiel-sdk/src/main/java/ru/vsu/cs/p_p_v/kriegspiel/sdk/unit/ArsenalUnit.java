package ru.vsu.cs.p_p_v.kriegspiel.sdk.unit;

import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.Board;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.Coordinate;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.Team;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.stats.UnitBaseStats;

public abstract class ArsenalUnit extends BoardUnit {
    public ArsenalUnit(Board board, Team team, Coordinate position, UnitBaseStats baseStats, String name) {
        super(board, team, position, baseStats, name);
    }
}
