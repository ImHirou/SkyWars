package me.secretlovers.skywars.game.phases;

import me.secretlovers.skywars.game.Game;

import java.util.function.Consumer;

public interface Phase {
    public Consumer<Game> getStarting();
    public Consumer<Game> getEnding();
    public String getName();
}
