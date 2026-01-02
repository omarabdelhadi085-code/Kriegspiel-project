package ru.vsu.cs.p_p_v.kriegspiel.sdk.game;

import java.util.Comparator;

public class Coordinate {
    public final int x;
    public final int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;

        Coordinate other = (Coordinate) o;

        return x == other.x && y == other.y;
    }
}
