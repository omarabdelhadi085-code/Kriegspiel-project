package ru.vsu.cs.p_p_v.kriegspiel.sdk.game;

import com.google.gson.Gson;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.parser.CellJson;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.parser.UnitJson;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.*;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.cell.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Board {
    private final BoardCell[][] board;
    private final List<BoardUnit> northUnits = new CopyOnWriteArrayList<>();
    private final List<BoardUnit> southUnits = new CopyOnWriteArrayList<>();

    public final int ySize = 20;
    public final int xSize = 25;

    public Board() {
        board = new BoardCell[ySize][xSize];
        for (int yIndex = 0; yIndex < ySize; yIndex++) {
            for (int xIndex = 0; xIndex < xSize; xIndex++) {
                board[yIndex][xIndex] = new Empty(new Coordinate(xIndex, yIndex));
            }
        }
    }

    public void appendFieldFromFile(Path path) throws IOException {
        String jsonString = Files.readString(path);

        appendFieldFromJson(jsonString);
    }

    public void appendFieldFromJson(String jsonString) {
        CellJson[] cells = new Gson().fromJson(jsonString, CellJson[].class);

        for (CellJson cellJson : cells) {
            int x = cellJson.getX();
            int y = cellJson.getY();
            x--;
            y--;

            BoardCell curCell = null;
            switch (cellJson.getType()) {
                case "empty" -> curCell = new Empty(new Coordinate(x, y));
                case "fortress" -> curCell = new Fortress(new Coordinate(x, y));
                case "mountain" -> curCell = new Mountain(new Coordinate(x, y));
                case "mountainPass" -> curCell = new MountainPass(new Coordinate(x, y));
            }

            board[y][x] = curCell;
        }
    }

    /*
     * public void appendFieldFromCellTypeData(List<CellTypeData> cellTypeData) {
     * for (CellTypeData cellData : cellTypeData) {
     * BoardCell curCell = null;
     * switch (cellData.type) {
     * case "frt" -> curCell = new Fortress(cellData.coordinate);
     * case "mnt" -> curCell = new Mountain(cellData.coordinate);
     * case "mps" -> curCell = new MountainPass(cellData.coordinate);
     * }
     * 
     * board[cellData.coordinate.y][cellData.coordinate.x] = curCell;
     * }
     * }
     */

    public void appendUnitsFromFile(Path path) throws IOException {
        String jsonString = Files.readString(path);

        appendUnitsFromJson(jsonString);
    }

    public void appendUnitsFromJson(String jsonString) {
        UnitJson[] units = new Gson().fromJson(jsonString, UnitJson[].class);

        for (UnitJson unitJson : units) {
            int x = unitJson.getX();
            int y = unitJson.getY();
            x--;
            y--;
            Team team = null;
            switch (unitJson.getTeam()) {
                case "north" -> team = Team.North;
                case "south" -> team = Team.South;
            }

            BoardUnit unit = null;
            switch (unitJson.getType()) {
                case "arsenal" -> unit = new Arsenal(this, team, new Coordinate(x, y));
                case "cannon" -> unit = new Cannon(this, team, new Coordinate(x, y));
                case "cavalry" -> unit = new Cavalry(this, team, new Coordinate(x, y));
                case "infantry" -> unit = new Infantry(this, team, new Coordinate(x, y));
                case "relay" -> unit = new Relay(this, team, new Coordinate(x, y));
                case "swiftCannon" -> unit = new SwiftCannon(this, team, new Coordinate(x, y));
                case "swiftRelay" -> unit = new SwiftRelay(this, team, new Coordinate(x, y));
            }
        }
    }

    /*
     * public void appendUnitsFromUnitData(List<UnitData> unitData) {
     * for (UnitData unit : unitData) {
     * int x = unit.position.x;
     * int y = unit.position.y;
     * Team team = unit.team;
     * 
     * BoardUnit boardUnit = null;
     * switch (unit.type) {
     * case "ars" -> boardUnit = new Arsenal(this, team, new Coordinate(x, y));
     * case "can" -> boardUnit = new Cannon(this, team, new Coordinate(x, y));
     * case "cav" -> boardUnit = new Cavalry(this, team, new Coordinate(x, y));
     * case "inf" -> boardUnit = new Infantry(this, team, new Coordinate(x, y));
     * case "rel" -> boardUnit = new Relay(this, team, new Coordinate(x, y));
     * case "swC" -> boardUnit = new SwiftCannon(this, team, new Coordinate(x, y));
     * case "swR" -> boardUnit = new SwiftRelay(this, team, new Coordinate(x, y));
     * }
     * }
     * }
     */

    public void clearCellConnections(Team team) {
        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                BoardCell curCell = getCell(new Coordinate(x, y));

                if (team == Team.North)
                    curCell.resetNorthConnection();
                else
                    curCell.resetSouthConnection();
            }
        }
    }

    /*
     * public void appendCellConnections(List<CellConnectionsData>
     * cellConnectionsData) {
     * for (CellConnectionsData connectionsData : cellConnectionsData) {
     * BoardCell cell = getCell(connectionsData.coordinate);
     * 
     * List<ConnectionDirection> northConnections =
     * connectionsData.getNorthConnections();
     * List<ConnectionDirection> southConnections =
     * connectionsData.getSouthConnections();
     * 
     * cell.setHasSouthConnection(connectionsData.hasSouthConnection);
     * cell.setHasNorthConnection(connectionsData.hasNorthConnection);
     * 
     * for (ConnectionDirection dir : northConnections)
     * cell.addConnection(dir, Team.North);
     * for (ConnectionDirection dir : southConnections)
     * cell.addConnection(dir, Team.South);
     * }
     * }
     */

    public BoardCell getCell(Coordinate cellCoordinate) {
        if (cellCoordinate.x < 0 || cellCoordinate.x > xSize - 1 || cellCoordinate.y < 0
                || cellCoordinate.y > ySize - 1) {
            return null;
        }

        return board[cellCoordinate.y][cellCoordinate.x];
    }

    public List<BoardUnit> getNorthUnits() {
        return Collections.unmodifiableList(northUnits);
    }

    public List<BoardUnit> getNorthArsenalUnits() {
        return northUnits.stream().filter(unit -> unit instanceof ArsenalUnit).collect(Collectors.toList());
    }

    public List<BoardUnit> getNorthRelayUnits() {
        return northUnits.stream().filter(unit -> unit instanceof RelayUnit).collect(Collectors.toList());
    }

    public boolean addNorthUnit(BoardUnit boardUnit) {
        return northUnits.add(boardUnit);
    }

    public List<BoardUnit> getSouthUnits() {
        return Collections.unmodifiableList(southUnits);
    }

    public List<BoardUnit> getSouthArsenalUnits() {
        return southUnits.stream().filter(unit -> unit instanceof ArsenalUnit).collect(Collectors.toList());
    }

    public List<BoardUnit> getSouthRelayUnits() {
        return southUnits.stream().filter(unit -> unit instanceof RelayUnit).collect(Collectors.toList());
    }

    public boolean addSouthUnit(BoardUnit boardUnit) {
        return southUnits.add(boardUnit);
    }

    public void removeUnit(BoardUnit boardUnit) {
        getCell(boardUnit.getPosition()).setUnit(null);

        if (boardUnit.getTeam() == Team.North) {
            northUnits.remove(boardUnit);
        } else {
            southUnits.remove(boardUnit);
        }
    }

    public void removeAllUnits() {
        for (BoardUnit unit : northUnits) {
            getCell(unit.getPosition()).setUnit(null);
        }
        northUnits.clear();

        for (BoardUnit unit : southUnits) {
            getCell(unit.getPosition()).setUnit(null);
        }
        southUnits.clear();
    }

    public void updateConnections() {
        updateTeamConnections(getNorthArsenalUnits(), Team.North);
        updateTeamConnections(getSouthArsenalUnits(), Team.South);
    }

    private void updateTeamConnections(List<BoardUnit> arsenalList, Team unitsTeam) {
        clearCellConnections(unitsTeam);

        Queue<BoardUnit> unitsToProcess = new LinkedList<>(arsenalList);
        List<RelayUnit> visitedRelays = new ArrayList<>();
        while (!unitsToProcess.isEmpty()) {
            BoardUnit unit = unitsToProcess.poll();
            int x = unit.getPosition().x;
            int y = unit.getPosition().y;

            for (int curX = x; curX < xSize; curX++) {
                if (!updateCellConnection(unitsTeam, unitsToProcess, visitedRelays, ConnectionDirection.Horizontal, y,
                        curX))
                    break;
            }
            for (int curX = x; curX >= 0; curX--) {
                if (!updateCellConnection(unitsTeam, unitsToProcess, visitedRelays, ConnectionDirection.Horizontal, y,
                        curX))
                    break;
            }
            for (int curY = y; curY < ySize; curY++) {
                if (!updateCellConnection(unitsTeam, unitsToProcess, visitedRelays, ConnectionDirection.Vertical, curY,
                        x))
                    break;
            }
            for (int curY = y; curY >= 0; curY--) {
                if (!updateCellConnection(unitsTeam, unitsToProcess, visitedRelays, ConnectionDirection.Vertical, curY,
                        x))
                    break;
            }

            for (int curX = x, curY = y; curX >= 0 && curY >= 0; curX--, curY--) {
                if (!updateCellConnection(unitsTeam, unitsToProcess, visitedRelays, ConnectionDirection.DiagonalMajor,
                        curY, curX))
                    break;
            }
            for (int curX = x, curY = y; curX >= 0 && curY < ySize; curX--, curY++) {
                if (!updateCellConnection(unitsTeam, unitsToProcess, visitedRelays, ConnectionDirection.DiagonalMinor,
                        curY, curX))
                    break;
            }
            for (int curX = x, curY = y; curX < xSize && curY < ySize; curX++, curY++) {
                if (!updateCellConnection(unitsTeam, unitsToProcess, visitedRelays, ConnectionDirection.DiagonalMajor,
                        curY, curX))
                    break;
            }
            for (int curX = x, curY = y; curX < xSize && curY >= 0; curX++, curY--) {
                if (!updateCellConnection(unitsTeam, unitsToProcess, visitedRelays, ConnectionDirection.DiagonalMinor,
                        curY, curX))
                    break;
            }
        }
    }

    private boolean updateCellConnection(Team unitsTeam, Queue<BoardUnit> unitsToProcess, List<RelayUnit> visitedRelays,
            ConnectionDirection dir, int y, int x) {
        BoardCell cell = getCell(new Coordinate(x, y));
        BoardUnit cellUnit = cell.getUnit();
        if (cell.isObstacle() || (cellUnit != null && cellUnit.getTeam() != unitsTeam))
            return false;

        if (cellUnit != null) {
            if (!cellUnit.hasConnection()) {
                List<BoardUnit> friendlyUnits = new ArrayList<>();
                switch (cellUnit.getTeam()) {
                    case South -> friendlyUnits = getSouthUnits();
                    case North -> friendlyUnits = getNorthUnits();
                }

                Queue<BoardUnit> unitsToCheck = new LinkedList<>();
                unitsToCheck.add(cellUnit);

                while (!unitsToCheck.isEmpty()) {
                    BoardUnit curUnit = unitsToCheck.poll();
                    Coordinate unitPos = curUnit.getPosition();
                    BoardCell curCell = getCell(unitPos);
                    if (unitsTeam == Team.North)
                        curCell.setHasNorthConnection(true);
                    else
                        curCell.setHasSouthConnection(true);

                    for (BoardUnit nextUnit : friendlyUnits) {
                        if (nextUnit == curUnit)
                            continue;

                        Coordinate nextUnitPos = nextUnit.getPosition();
                        int range = 1;
                        if ((unitPos.x == nextUnitPos.x || unitPos.y == nextUnitPos.y
                                || Math.abs(unitPos.x - nextUnitPos.x) == Math.abs(unitPos.y - nextUnitPos.y))
                                && Math.abs(unitPos.x - nextUnitPos.x) <= range
                                && Math.abs(unitPos.y - nextUnitPos.y) <= range
                                && !nextUnit.hasConnection()) {
                            unitsToCheck.add(nextUnit);
                        }
                    }
                }
            }

            if (cellUnit instanceof RelayUnit && !visitedRelays.contains(cellUnit)) {
                visitedRelays.add((RelayUnit) cellUnit);
                unitsToProcess.add(cellUnit);
            }
        }

        cell.addConnection(dir, unitsTeam);
        return true;
    }
}
