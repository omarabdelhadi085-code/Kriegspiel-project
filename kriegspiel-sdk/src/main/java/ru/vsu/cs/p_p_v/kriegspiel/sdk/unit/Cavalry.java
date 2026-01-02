package ru.vsu.cs.p_p_v.kriegspiel.sdk.unit;

import ru.vsu.cs.p_p_v.kriegspiel.sdk.cache.ImageFileCached;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.Board;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.Coordinate;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.Team;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.stats.UnitBaseStats;

import java.awt.*;
import java.io.File;

public class Cavalry extends BoardUnit {
    public Cavalry(Board board, Team team, Coordinate position) {
        super(board, team, position, new UnitBaseStats(2, 2, 4, 5), "Cavalry");
    }

    @Override
    public Image getImageRepresentation() {
        String imgPath = String.format("images/units/%s/cavalry.png", getTeam() == Team.North ? "north" : "south");

        return ImageFileCached.readImage(new File(imgPath));
    }
}
