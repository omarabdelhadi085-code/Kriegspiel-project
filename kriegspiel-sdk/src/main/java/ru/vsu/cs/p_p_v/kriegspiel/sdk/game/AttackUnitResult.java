package ru.vsu.cs.p_p_v.kriegspiel.sdk.game;

public enum AttackUnitResult {
    NotMyTurn,
    NoAttackLeft,
    UnitInSameTeam,
    IncorrectUnitCoordinates,
    Capture,
    ForcedRetreat,
    None
}
