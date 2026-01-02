package ru.vsu.cs.p_p_v.kriegspiel.sdk.game;

import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.BoardUnit;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.stats.UnitCombatStats;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.cell.BoardCell;
import ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.Arsenal;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Comparator;

public class AIPlayer implements IPlayerController {

    @Override
    public List<MoveCommand> getTurnActions(GameState currentState) {
        System.out.println("[AI_DEBUG] STARTING TURN CALCULATION.");
        List<MoveCommand> bestMoves = new ArrayList<>();
        List<MoveCommand> backupMoves = new ArrayList<>();
        int validMovesFound = 0;
        
        try {
            Board board = currentState.getBoard();
            if (board == null) {
                System.out.println("[AI_DEBUG] CRITICAL: Board is null. Returning empty list.");
                return bestMoves;
            }

            Team myTeam = currentState.getMyTeam();
            List<BoardUnit> myUnits = (myTeam == Team.North) ? board.getNorthUnits() : board.getSouthUnits();

            // PriorityQueue to store CandidateMoves, sorted by score descending
            PriorityQueue<CandidateMove> pq = new PriorityQueue<>(
                    Comparator.comparingInt(CandidateMove::getScore).reversed());

            int[][] directions = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 }, { 1, 1 }, { 1, -1 }, { -1, 1 },
                    { -1, -1 } };
            int boardW = currentState.getBoardSizeX();
            int boardH = currentState.getBoardSizeY();

            for (BoardUnit unit : myUnits) {
                Coordinate startPos = unit.getPosition();

                for (int[] dir : directions) {
                    int nx = startPos.x + dir[0];
                    int ny = startPos.y + dir[1];

                    // Bounds check
                    if (nx < 0 || nx >= boardW || ny < 0 || ny >= boardH) {
                        continue;
                    }

                    Coordinate target = new Coordinate(nx, ny);
                    
                    // Check connectivity first - this is what we count
                    if (verifyConnectivity(unit, target, currentState)) {
                        validMovesFound++;
                        
                        BoardCell targetCell = board.getCell(target);
                        boolean isEnemy = targetCell.getUnit() != null && targetCell.getUnit().getTeam() != myTeam;

                        MoveCommand cmd;
                        if (isEnemy) {
                            cmd = game -> game.attackUnit(target);
                        } else {
                            cmd = game -> game.moveUnit(startPos, target);
                        }
                        
                        // Score the move
                        int score = scoreMove(unit, target, currentState);
                        
                        // Add to best moves if score > 0
                        if (score > 0) {
                            pq.add(new CandidateMove(score, cmd));
                        } else {
                            // Add to backup moves (valid but low/negative score)
                            backupMoves.add(cmd);
                        }
                    }
                }
            }

            // Select top 5 from high-scoring moves
            int movesToSelect = 5;
            while (!pq.isEmpty() && movesToSelect > 0) {
                CandidateMove best = pq.poll();
                bestMoves.add(best.command);
                movesToSelect--;
            }
            
            // DESPERATION MODE: If no positive moves, use backup moves
            boolean usedBackupMoves = false;
            if (bestMoves.isEmpty() && !backupMoves.isEmpty()) {
                System.out.println("[AI_DEBUG] CRITICAL: No positive moves found. Total valid options considered: " + validMovesFound + ". Using backup moves.");
                // Take up to 5 backup moves
                int backupCount = Math.min(5, backupMoves.size());
                for (int i = 0; i < backupCount; i++) {
                    bestMoves.add(backupMoves.get(i));
                }
                usedBackupMoves = true;
            }
            
            // Log before returning
            if (bestMoves.isEmpty()) {
                System.out.println("[AI_DEBUG] CRITICAL: No positive moves found. Total valid options considered: " + validMovesFound + ".");
            } else {
                System.out.println("[AI_DEBUG] RETURNING " + bestMoves.size() + " moves.");
            }
            
            // Enhanced logging
            String strategy;
            if (bestMoves.isEmpty()) {
                strategy = "None";
            } else if (usedBackupMoves) {
                strategy = "Backup";
            } else {
                strategy = "Aggressive";
            }
            System.out.println("[AI_STATUS] Moves Calculated: " + bestMoves.size() + " | Strategy: " + strategy);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[AI_FATAL] AI CRASHED: " + e.getMessage());
            System.out.println("[AI_CRITICAL_FAILURE] Turn skipped due to exception: " + e.getMessage());
            return new ArrayList<>(); // Return empty list, don't throw
        }

        return bestMoves;
    }

    private static class CandidateMove {
        int score;
        MoveCommand command;

        public CandidateMove(int score, MoveCommand command) {
            this.score = score;
            this.command = command;
        }

        public int getScore() {
            return score;
        }
    }

    private void logAI(String stage, String message, boolean success) {
        System.out.println("[AI_AUDIT] | STAGE: " + stage + " | STATUS: " + success + " | MSG: " + message);
    }

    private int scoreMove(BoardUnit unit, Coordinate target, GameState state) {
        int attackScore = 0;
        int distanceScore = 0;
        int safeScore = 0;
        
        Board board = state.getBoard();
        if (board == null)
            return -9999;

        BoardCell targetCell = board.getCell(target);
        boolean isEnemyAt = targetCell.getUnit() != null && targetCell.getUnit().getTeam() != unit.getTeam();

        // Attack Opportunity
        if (isEnemyAt) {
            UnitCombatStats unitStats = state.getUnitCombatStats(target);
            // Combat logic: Capture if attack score - defense score >= 2
            // My unit's attack contribution to the target is included in the target's
            // attack score (hostile to unit)
            // Wait, getUnitCombatStats(unit) returns (defense, attack).
            // Attack score is "Attack against this unit".
            // So if I am attacking target, I check target's stats.
            // if (targetStats.attackScore - targetStats.defenseScore >= 2) -> capture
            if (unitStats != null && (unitStats.attackScore - unitStats.defenseScore >= 2)) {
                attackScore += 50;
            }
        }

        if (!isEnemyAt) {
            // Defense: Fortress or Mountain Pass
            // Need to check cell type. Assuming class names or helpers.
            String cellClass = targetCell.getClass().getSimpleName();
            if (cellClass.equals("Fortress") || cellClass.equals("MountainPass")) {
                safeScore += 10;
            }

            // Arsenal Threat: Adjacent to enemy Arsenal
            if (isAdjacentToEnemyArsenal(target, unit.getTeam(), board)) {
                attackScore += 100;
            }
        }

        // Safety Override
        if (!verifyConnectivity(unit, target, state)) {
            return -9999;
        }

        // Distance Heuristic: Calculate distance to closest enemy Arsenal
        int distanceToEnemyArsenal = getDistanceToClosestEnemyArsenal(target, unit.getTeam(), board, state);
        int maxBoardDistance = state.getBoardSizeX() + state.getBoardSizeY(); // Manhattan distance max
        distanceScore = (maxBoardDistance - distanceToEnemyArsenal) * 2;

        // Center Control: Bonus for controlling central columns
        int centerControlBonus = 0;
        if (target.x >= 8 && target.x <= 12) {
            centerControlBonus = 5;
        }

        // Randomness: Human factor to break ties
        int randomness = (int)(Math.random() * 5);

        // Calculate total score
        int totalScore = attackScore + distanceScore + safeScore + centerControlBonus + randomness;

        logAI("Evaluation",
                "Unit " + unit.getName() + " to Target (" + target.x + "," + target.y + 
                ") Score: " + totalScore + " (Attack: " + attackScore + " | Distance: " + distanceScore + 
                " | Safe: " + safeScore + " | Center: " + centerControlBonus + " | Random: " + randomness + ")", true);
        return totalScore;
    }

    private boolean isAdjacentToEnemyArsenal(Coordinate target, Team myTeam, Board board) { // Helper
        int x = target.x;
        int y = target.y;
        // Check neighbors
        int[][] directions = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 }, { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
        for (int[] dir : directions) {
            Coordinate neighbor = new Coordinate(x + dir[0], y + dir[1]);
            BoardCell cell = board.getCell(neighbor);
            if (cell != null && cell.getUnit() instanceof Arsenal && cell.getUnit().getTeam() != myTeam) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculate Manhattan distance to the closest enemy Arsenal
     */
    private int getDistanceToClosestEnemyArsenal(Coordinate target, Team myTeam, Board board, GameState state) {
        Team enemyTeam = (myTeam == Team.North) ? Team.South : Team.North;
        List<BoardUnit> enemyUnits = (enemyTeam == Team.North) ? board.getNorthUnits() : board.getSouthUnits();
        
        int minDistance = Integer.MAX_VALUE;
        
        for (BoardUnit unit : enemyUnits) {
            if (unit instanceof Arsenal) {
                Coordinate arsenalPos = unit.getPosition();
                // Manhattan distance
                int distance = Math.abs(target.x - arsenalPos.x) + Math.abs(target.y - arsenalPos.y);
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }
        }
        
        // If no enemy arsenal found, return max distance
        if (minDistance == Integer.MAX_VALUE) {
            return state.getBoardSizeX() + state.getBoardSizeY();
        }
        
        return minDistance;
    }

    private boolean verifyConnectivity(BoardUnit unit, Coordinate target, GameState currentState) {
        Board board = currentState.getBoard();
        if (board == null)
            return false;

        Coordinate originalPos = unit.getPosition();
        BoardCell targetCell = board.getCell(target);
        BoardUnit targetUnit = targetCell.getUnit();

        boolean isAttack = targetUnit != null && targetUnit.getTeam() != unit.getTeam();

        if (isAttack) {
            // Simulate Attack: Remove enemy, Check connectivity (unit stays in place)
            // Per game rules, attacking does not move the unit.
            // So we just check if removing the enemy breaks anything (unlikely)
            // or if we rely on connection that is safe.
            // Actually, if we just stay put, we just need to ensure we are connected NOW.
            // Since move doesn't happen, we check 'unit.hasConnection()' as is?
            // But maybe the user implies "If I make this move/attack, am I safe?"
            // Let's assume safely that for attack, we verify we currently have connection.
            // But if `target` was blocking a connection path? (Enemy usually doesn't
            // provide connection).
            // So verifying connectivity for attack is:
            // 1. Temporarily remove enemy.
            // 2. Update connections.
            // 3. Check if unit is connected.
            // 4. Restore enemy.

            board.removeUnit(targetUnit); // Temporarily remove
            board.updateConnections();
            boolean connected = unit.hasConnection();

            // Restore
            // removal uses 'removeUnit' which clears cell link.
            // We need to add it back.
            if (targetUnit.getTeam() == Team.North)
                board.addNorthUnit(targetUnit);
            else
                board.addSouthUnit(targetUnit);
            targetCell.setUnit(targetUnit);

            // board.updateConnections(); // Restore state fully?
            // Actually, the calling context might expect clean state.
            // Better:
            // board.updateConnections() called at start of verify?
            // We should restore properly.
            board.updateConnections();

            if (!connected) {
                logAI("Move Safety", "Unit disconnected at target", false);
            }
            return connected;
        } else {
            // Try to move
            if (!unit.move(target)) {
                return false;
            }

            // Check connectivity
            board.updateConnections();
            boolean connected = unit.hasConnection();

            if (!connected) {
                logAI("Move Safety", "Unit disconnected at target", false);
            }

            // Revert move
            unit.move(originalPos);
            board.updateConnections();

            return connected;
        }
    }
}
