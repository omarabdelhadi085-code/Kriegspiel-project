package ru.vsu.cs.p_p_v.kriegspiel.sdk.cell;

import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.Coordinate;

public class Empty extends BoardCell {
    public Empty(Coordinate coordinate) {
        super(coordinate, false);
    }
}
