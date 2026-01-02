package ru.vsu.cs.p_p_v.kriegspiel.sdk.unit.stats;

public class UnitBaseStats {
    public final Integer speed;
    public final Integer range;
    public final Integer attack;
    public final Integer defense;

    public UnitBaseStats(Integer speed, Integer range, Integer attack, Integer defense) {
        this.speed = speed;
        this.range = range;
        this.attack = attack;
        this.defense = defense;
    }
}
