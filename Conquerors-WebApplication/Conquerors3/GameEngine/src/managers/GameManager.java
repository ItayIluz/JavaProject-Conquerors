package managers;

import engine.GameData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {

    private final Map<String, GameData> games;

    public GameManager() {
        games = new ConcurrentHashMap<>();
    }

    public synchronized Map<String, GameData> getGames() {
        return games;
    }

    public boolean isGameExists(String gameName) {
        return games.containsKey(gameName);
    }
}
