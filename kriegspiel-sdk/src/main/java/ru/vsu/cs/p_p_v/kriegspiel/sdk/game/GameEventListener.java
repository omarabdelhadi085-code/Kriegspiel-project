package ru.vsu.cs.p_p_v.kriegspiel.sdk.game;

public interface GameEventListener {
    default void onNextTurn() {}
    default void onUnitMove(MoveUnitResult result) {};
    default void onAttack(AttackUnitResult result) {};
    default void onWin(Team winner) {};
}
