package ru.vsu.cs.p_p_v.kriegspiel.sdk.cell;

import ru.vsu.cs.p_p_v.kriegspiel.sdk.cache.ImageFileCached;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.Coordinate;

import java.awt.*;
import java.io.File;

public class Fortress extends BoardCell{
    public Fortress(Coordinate coordinate) {
        super(coordinate, false, 4);
    }

    @Override
    public Image getBackgroundImage() {
        String imgPath = "images/cells/fortress.png";

        return ImageFileCached.readImage(new File(imgPath));
    }
}
