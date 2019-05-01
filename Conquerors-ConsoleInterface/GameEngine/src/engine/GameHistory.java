package engine;

import java.io.Serializable;

public class GameHistory implements Serializable {

    Territory[] territoryHistory;
    Player[] playerHistory;

    public GameHistory(Territory[] territoryHistory, Player[] playerHistory){
        this.territoryHistory = territoryHistory;
        this.playerHistory = playerHistory;
    }

    public Territory[] getTerritoryHistory() {
        return territoryHistory;
    }

    public Player[] getPlayerHistory() {
        return playerHistory;
    }
}
