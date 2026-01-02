package ru.vsu.cs.p_p_v.kriegspiel.sdk.game;

import java.util.List;

public interface IPlayerController {
    List<MoveCommand> getTurnActions(GameState currentState);
}
