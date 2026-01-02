package debug;

import ru.vsu.cs.p_p_v.kriegspiel.sdk.game.*;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.*;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.cell.BoardCell;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.stats.UnitCombatStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Headless AI Simulation to verify AI never freezes or skips turns.
 * Runs 50 turns of AI vs AI gameplay in memory.
 */
public class AISimulation {
    
    /**
     * Simple headless Game implementation for simulation
     */
    static class HeadlessGame implements Game {
        private final Board board;
        private Team currentTurnTeam = Team.South;
        private int leftMoves = 5;
        private final List<BoardUnit> movedUnits = new ArrayList<>();
        private boolean isAttackUsed = false;
        private final IPlayerController player1;
        private final IPlayerController player2;
        
        public HeadlessGame(Board board, IPlayerController player1, IPlayerController player2) {
            this.board = board;
            this.player1 = player1;
            this.player2 = player2;
            board.updateConnections();
        }
        
        @Override
        public Team getCurrentTurnTeam() {
            return currentTurnTeam;
        }
        
        @Override
        public Team getMyTeam() {
            return currentTurnTeam;
        }
        
        @Override
        public int getLeftMoves() {
            return leftMoves;
        }
        
        @Override
        public boolean isAttackUsed() {
            return isAttackUsed;
        }
        
        @Override
        public void endTurn() {
            leftMoves = 5;
            movedUnits.clear();
            isAttackUsed = false;
            currentTurnTeam = currentTurnTeam == Team.North ? Team.South : Team.North;
        }
        
        @Override
        public int getBoardSizeX() {
            return board.xSize;
        }
        
        @Override
        public int getBoardSizeY() {
            return board.ySize;
        }
        
        @Override
        public BoardCell getBoardCell(Coordinate cellCoordinate) {
            return board.getCell(cellCoordinate);
        }
        
        @Override
        public Board getBoard() {
            return board;
        }
        
        @Override
        public boolean unitCanMove(Coordinate unitCoordinate) {
            BoardCell unitCell = board.getCell(unitCoordinate);
            if (unitCell == null)
                return false;
            
            BoardUnit unit = unitCell.getUnit();
            return leftMoves != 0 && !movedUnits.contains(unit) && unit != null && unit.hasConnection();
        }
        
        @Override
        public void moveUnit(Coordinate unitCoordinate, Coordinate destCellCoordinate) {
            if (leftMoves == 0)
                return;
            
            BoardCell unitCell = board.getCell(unitCoordinate);
            if (unitCell == null)
                return;
            
            BoardUnit unit = unitCell.getUnit();
            if (unit == null || movedUnits.contains(unit) || unit.getTeam() != currentTurnTeam || !unit.hasConnection())
                return;
            
            int unitSpeed = unit.getBaseStats().speed;
            if (Math.abs(unitCoordinate.x - destCellCoordinate.x) > unitSpeed
                    || Math.abs(unitCoordinate.y - destCellCoordinate.y) > unitSpeed)
                return;
            
            if (unit.move(destCellCoordinate)) {
                leftMoves--;
                movedUnits.add(unit);
                board.updateConnections();
            }
        }
        
        @Override
        public boolean unitCanBeCaptured(Coordinate unitCoordinate) {
            BoardCell cell = board.getCell(unitCoordinate);
            BoardUnit cellUnit = cell != null ? cell.getUnit() : null;
            if (cellUnit == null)
                return false;
            
            UnitCombatStats stats = getUnitCombatStats(unitCoordinate);
            return stats != null && (stats.attackScore - stats.defenseScore >= 2) && !isAttackUsed;
        }
        
        @Override
        public UnitCombatStats getUnitCombatStats(Coordinate unitCoordinate) {
            BoardCell unitCell = board.getCell(unitCoordinate);
            if (unitCell == null)
                return null;
            
            BoardUnit unit = unitCell.getUnit();
            if (unit == null)
                return null;
            
            return new UnitCombatStats(getUnitDefenseScore(unit), getUnitAttackScore(unit));
        }
        
        private int getUnitDefenseScore(BoardUnit unit) {
            List<BoardUnit> friendlyUnits = unit.getTeam() == Team.South ? board.getSouthUnits() : board.getNorthUnits();
            Coordinate unitPos = unit.getPosition();
            int defenseScore = unit.hasConnection() ? unit.getBaseStats().defense : 0;
            defenseScore += unit.getDefenseBuff();
            
            for (BoardUnit nextUnit : friendlyUnits) {
                if (nextUnit == unit || !nextUnit.hasConnection())
                    continue;
                
                Coordinate nextUnitPos = nextUnit.getPosition();
                int nextUnitRange = nextUnit.getBaseStats().range;
                if ((unitPos.x == nextUnitPos.x || unitPos.y == nextUnitPos.y
                        || Math.abs(unitPos.x - nextUnitPos.x) == Math.abs(unitPos.y - nextUnitPos.y))
                        && Math.abs(unitPos.x - nextUnitPos.x) <= nextUnitRange
                        && Math.abs(unitPos.y - nextUnitPos.y) <= nextUnitRange) {
                    defenseScore += nextUnit.getBaseStats().defense;
                }
            }
            
            return defenseScore;
        }
        
        private int getUnitAttackScore(BoardUnit unit) {
            List<BoardUnit> hostileUnits = unit.getTeam() == Team.South ? board.getNorthUnits() : board.getSouthUnits();
            Coordinate unitPos = unit.getPosition();
            int attackScore = 0;
            
            for (BoardUnit nextUnit : hostileUnits) {
                if (!nextUnit.hasConnection())
                    continue;
                
                Coordinate nextUnitPos = nextUnit.getPosition();
                int nextUnitRange = nextUnit.getBaseStats().range;
                if ((unitPos.x == nextUnitPos.x || unitPos.y == nextUnitPos.y
                        || Math.abs(unitPos.x - nextUnitPos.x) == Math.abs(unitPos.y - nextUnitPos.y))
                        && Math.abs(unitPos.x - nextUnitPos.x) <= nextUnitRange
                        && Math.abs(unitPos.y - nextUnitPos.y) <= nextUnitRange) {
                    attackScore += nextUnit.getBaseStats().attack;
                }
            }
            
            return attackScore;
        }
        
        @Override
        public void attackUnit(Coordinate unitCoordinate) {
            if (isAttackUsed)
                return;
            
            BoardCell unitCell = board.getCell(unitCoordinate);
            if (unitCell == null)
                return;
            
            BoardUnit unit = unitCell.getUnit();
            if (unit == null || unit.getTeam() == currentTurnTeam)
                return;
            
            UnitCombatStats combatStats = getUnitCombatStats(unit.getPosition());
            if (combatStats != null && combatStats.attackScore - combatStats.defenseScore >= 2) {
                board.removeUnit(unit);
                board.updateConnections();
                isAttackUsed = true;
            }
        }
        
        @Override
        public void addGameEventListener(GameEventListener gameListener) {
            // Not needed for headless simulation
        }
        
        @Override
        public IPlayerController getPlayerController(Team team) {
            return team == Team.South ? player1 : player2;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== AI SIMULATION TEST ===");
        System.out.println("Initializing headless game...");
        
        // Create board
        Board board = new Board();
        
        // Create AI players
        AIPlayer aiNorth = new AIPlayer();
        AIPlayer aiSouth = new AIPlayer();
        
        // Place units randomly
        Random random = new Random(42); // Fixed seed for reproducibility
        System.out.println("Placing units...");
        
        // Place 5 friendly (South) units
        List<Coordinate> usedPositions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Coordinate pos = getRandomValidPosition(board, usedPositions, random);
            usedPositions.add(pos);
            
            switch (i) {
                case 0 -> new Arsenal(board, Team.South, pos);
                case 1 -> new Infantry(board, Team.South, pos);
                case 2 -> new Cannon(board, Team.South, pos);
                case 3 -> new Cavalry(board, Team.South, pos);
                default -> new Relay(board, Team.South, pos);
            }
        }
        
        // Place 5 enemy (North) units
        for (int i = 0; i < 5; i++) {
            Coordinate pos = getRandomValidPosition(board, usedPositions, random);
            usedPositions.add(pos);
            
            switch (i) {
                case 0 -> new Arsenal(board, Team.North, pos);
                case 1 -> new Infantry(board, Team.North, pos);
                case 2 -> new Cannon(board, Team.North, pos);
                case 3 -> new Cavalry(board, Team.North, pos);
                default -> new Relay(board, Team.North, pos);
            }
        }
        
        // Update connections after placing units
        board.updateConnections();
        
        // Create headless game
        HeadlessGame game = new HeadlessGame(board, aiSouth, aiNorth);
        
        System.out.println("Starting 50-turn simulation...\n");
        
        // Stress test: 50 turns
        int failures = 0;
        for (int turn = 1; turn <= 50; turn++) {
            Team currentTeam = game.getCurrentTurnTeam();
            IPlayerController currentAI = currentTeam == Team.South ? aiSouth : aiNorth;
            
            // Get AI actions
            List<MoveCommand> moves = currentAI.getTurnActions(game);
            
            // Verification
            if (moves == null || moves.isEmpty()) {
                System.out.println("[FAIL] AI FROZE on Turn " + turn + " (Team: " + currentTeam + ")");
                failures++;
            } else {
                System.out.println("[PASS] Turn " + turn + " (Team: " + currentTeam + "): AI generated " + moves.size() + " moves");
                
                // Apply moves to the game state
                for (MoveCommand cmd : moves) {
                    try {
                        cmd.execute(game);
                    } catch (Exception e) {
                        System.err.println("Error executing move: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            // End turn
            game.endTurn();
        }
        
        System.out.println("\n=== SIMULATION COMPLETE ===");
        System.out.println("Total turns: 50");
        System.out.println("Failures (frozen turns): " + failures);
        if (failures == 0) {
            System.out.println("RESULT: SUCCESS - AI never froze!");
        } else {
            System.out.println("RESULT: FAILURE - AI froze " + failures + " time(s)");
        }
    }
    
    /**
     * Get a random valid position that is not occupied and not an obstacle
     */
    private static Coordinate getRandomValidPosition(Board board, List<Coordinate> usedPositions, Random random) {
        int maxAttempts = 1000;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = random.nextInt(board.xSize);
            int y = random.nextInt(board.ySize);
            Coordinate pos = new Coordinate(x, y);
            
            // Check if already used
            boolean alreadyUsed = false;
            for (Coordinate used : usedPositions) {
                if (used.x == pos.x && used.y == pos.y) {
                    alreadyUsed = true;
                    break;
                }
            }
            if (alreadyUsed)
                continue;
            
            // Check if cell is valid (not obstacle, no unit)
            BoardCell cell = board.getCell(pos);
            if (cell != null && !cell.isObstacle() && cell.getUnit() == null) {
                return pos;
            }
        }
        
        // Fallback: return first available position
        for (int y = 0; y < board.ySize; y++) {
            for (int x = 0; x < board.xSize; x++) {
                Coordinate pos = new Coordinate(x, y);
                boolean alreadyUsed = false;
                for (Coordinate used : usedPositions) {
                    if (used.x == pos.x && used.y == pos.y) {
                        alreadyUsed = true;
                        break;
                    }
                }
                if (!alreadyUsed) {
                    BoardCell cell = board.getCell(pos);
                    if (cell != null && !cell.isObstacle() && cell.getUnit() == null) {
                        return pos;
                    }
                }
            }
        }
        
        throw new RuntimeException("Could not find valid position for unit placement");
    }
}

