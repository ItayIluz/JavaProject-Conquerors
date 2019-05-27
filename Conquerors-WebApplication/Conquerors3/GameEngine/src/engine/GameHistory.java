package engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class GameHistory implements Serializable {

    Territory[] territoryHistory;
    ArrayList<Player> playerHistory;

    public GameHistory(Territory[] territoryHistory, ArrayList<Player> playerHistory){
        this.territoryHistory = territoryHistory;
        this.playerHistory = playerHistory;
    }

    public Territory[] getTerritoryHistory() {
        return territoryHistory;
    }

    public ArrayList<Player> getPlayerHistory() {
        return playerHistory;
    }
}
