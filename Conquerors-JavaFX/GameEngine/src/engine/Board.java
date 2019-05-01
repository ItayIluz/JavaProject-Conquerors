package engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class Board implements Serializable {

    private int rows;
    private int columns;
    private int defaultTerritoryProfit;
    private int defaultTerritoryArmyThreshold;
    private ArrayList<Territory> predefinedTerritories;
    private Territory[] territories;

    public Board(Board oldBoard){
        this(oldBoard.rows, oldBoard.columns, oldBoard.defaultTerritoryProfit, oldBoard.defaultTerritoryArmyThreshold, oldBoard.predefinedTerritories);
    }

    public Board(int rows, int columns, int defaultTerritoryProfit, int defaultTerritoryArmyThreshold, ArrayList<Territory> predefinedTerritories){
        this.rows = rows;
        this.columns = columns;
        this.defaultTerritoryProfit = defaultTerritoryProfit;
        this.defaultTerritoryArmyThreshold = defaultTerritoryArmyThreshold;
        this.predefinedTerritories = predefinedTerritories;

        territories = new Territory[rows*columns];

        int definedTerritoriesIndex = 0;
        predefinedTerritories.sort(Comparator.comparingInt(Territory::getId));

        Territory currentDefinedTerritory;
        for(int i = 0; i < rows*columns; i++){

            if(definedTerritoriesIndex < predefinedTerritories.size())
               currentDefinedTerritory = predefinedTerritories.get(definedTerritoriesIndex);
            else
                currentDefinedTerritory = null;

            if(currentDefinedTerritory != null && (i+1) == currentDefinedTerritory.getId()){
                territories[i] = new Territory((i+1), currentDefinedTerritory.getProfit(), currentDefinedTerritory.getArmyThreshold());
                definedTerritoriesIndex++;
            } else {
                territories[i] = new Territory((i+1), defaultTerritoryProfit, defaultTerritoryArmyThreshold);
            }
        }
    }

    public Territory[] getTerritories(){
        return territories;
    }

    public int getRows(){
        return rows;
    }

    public int getColumns(){
        return columns;
    }

    public boolean isTerritoryInPlayerRange(int territoryIndex, Player player){

        if((territoryIndex - 1) >= 0 && territories[territoryIndex - 1].getConqueringPlayer() == player) // Check left
            return true;
        else if((territoryIndex + 1) < rows*columns && territories[territoryIndex + 1].getConqueringPlayer() == player) // Check right
            return true;
        else if((territoryIndex - columns) >= 0 && territories[territoryIndex - columns].getConqueringPlayer() == player) // Check up
            return true;
        else if((territoryIndex + columns) < rows*columns && territories[territoryIndex + columns].getConqueringPlayer() == player) // Check down
            return true;
        else
            return false; // Not in range
    }

    public void setTerritories(Territory[] territories){
        this.territories = territories;
    }
}
