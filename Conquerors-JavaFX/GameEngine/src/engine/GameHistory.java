package engine;

import java.io.Serializable;
import java.util.HashMap;

public class GameHistory implements Serializable {

    Territory[] territoryHistory;
    Player[] playerHistory;
    HashMap<String,Integer> totalUnitsOnBoardHistory = new HashMap<>();

    public GameHistory(Territory[] territoryHistory, Player[] playerHistory, HashMap<String,Integer> totalUnitsOnBoardHistory){
        this.territoryHistory = territoryHistory;
        this.playerHistory = playerHistory;
        this.totalUnitsOnBoardHistory = totalUnitsOnBoardHistory;
    }

    public Territory[] getTerritoryHistory() {
        return territoryHistory;
    }

    public Player[] getPlayerHistory() {
        return playerHistory;
    }

    public HashMap<String,Integer> getTotalUnitsOnBoardHistory() { return totalUnitsOnBoardHistory; }
}
