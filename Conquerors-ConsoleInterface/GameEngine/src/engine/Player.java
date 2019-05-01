package engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Player implements Serializable {

    private final int id;
    private final String name;
    private int money;
    private HashMap<Territory, ArrayList<Army>> ownedTerritories;

    public Player(int id, String name){
        this.id = id;
        this.name = name;
        this.money = 0;
        this.ownedTerritories = new HashMap<>();
    }

    public Player(int id, String name, int money){
        this.id = id;
        this.name = name;
        this.money = money;
        this.ownedTerritories = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getName() { return name; }

    public String getNameAndId() {
        return name + " (" + id + ")";
    }

    public int getMoney() {
        return money;
    }

    public void subtractMoney(int moneyToSubtract) { this.money -= moneyToSubtract;}

    public void addMoney(int moneyToAdd) { this.money += moneyToAdd;}

    public HashMap<Territory, ArrayList<Army>> getOwnedTerritories() {
        return ownedTerritories;
    }

    public void setOwnedTerritories(HashMap<Territory, ArrayList<Army>> ownedTerritories){
        this.ownedTerritories = ownedTerritories;
    }

    public void conquerTerritory(Territory territory, ArrayList<Army>  army) {
        ownedTerritories.put(territory, army);
        territory.setConqueredByPlayer(id);
    }

    public void loseTerritory(Territory territory){
        ownedTerritories.remove(territory);
        territory.setNeutral();
    }

    public boolean hasEnoughMoney(int moneyToCompare){
        return money >= moneyToCompare;
    }

    public void gainProfitsAndUpdateCompetence(){
        int moneyToAdd = 0;
        ArrayList<Territory> toRemove = new ArrayList<>();

        for(Map.Entry<Territory, ArrayList<Army>> entry : ownedTerritories.entrySet()){
            Territory currentTerritory = entry.getKey();
            ArrayList<Army> armies = entry.getValue();
            int competenceAfterReduction = 0;

            moneyToAdd += currentTerritory.getProfit();

            for(Army currentArmy : armies)
                competenceAfterReduction += currentArmy.reduceCompetence();

            if(currentTerritory.getArmyThreshold() > competenceAfterReduction)
                toRemove.add(currentTerritory);
        }

        money += moneyToAdd;

        for(int i = 0; i < toRemove.size(); i++){
            Territory currentTerritory = toRemove.get(i);
            ownedTerritories.remove(currentTerritory);
            currentTerritory.setNeutral();
        }
    }

    public static Player[] reconstructPlayers(Player[] players, int initialFunds){
        Player[] playersToReconstruct = new Player[players.length];

        for(int i = 0; i < players.length; i++)
            playersToReconstruct[i] = new Player(players[i].getId(), players[i].getName(), initialFunds);

        return playersToReconstruct;
    }

    public int getTotalProfits(){
        int totalProfits = 0;
        for(Territory territory : ownedTerritories.keySet())
            totalProfits += territory.getProfit();

        return totalProfits;
    }
}
