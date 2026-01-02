package ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.stats;

public class UnitCombatStats {
    public final Integer defenseScore;
    public final Integer attackScore;

    public UnitCombatStats(Integer defenseScore, Integer attackScore) {
        this.defenseScore = defenseScore;
        this.attackScore = attackScore;
    }
}
