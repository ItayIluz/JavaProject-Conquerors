package engine;

import javafx.beans.property.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class Player implements Serializable {

    private int id;
    private String name;
    private int money;
    private Color color;
    private String colorName;
    private boolean surrendered = false;
    private HashSet<Territory> ownedTerritories = new HashSet<>();

    public Player(int id, String name){
        this.id = id;
        this.name = name;
        this.money = 0;
    }

    public Player(int id, String name, int money, Color color, String colorName){
        this.id = id;
        this.name = name;
        this.money = money;
        this.color = color;
        this.colorName = colorName;
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

    public void subtractMoney(int moneyToSubtract) { money -= moneyToSubtract;}

    public void addMoney(int moneyToAdd) { money += moneyToAdd;}

    public HashSet<Territory> getOwnedTerritories() {
        return ownedTerritories;
    }

    public void setOwnedTerritories(HashSet<Territory> ownedTerritories){
        this.ownedTerritories = ownedTerritories;
    }

    public void addTerritory(Territory territory) {
        ownedTerritories.add(territory);
    }

    public void removeTerritory(Territory territory){
        ownedTerritories.remove(territory);
    }

    public boolean hasEnoughMoney(int moneyToCompare){
        return money >= moneyToCompare;
    }

    public ArrayList<Territory> gainProfitsAndUpdateArmyCompetence(){
        int competenceAfterReduction;
        ArrayList<Territory> toRemove = new ArrayList<>();

        money += getTerritoriesTotalProfit();

        for(Territory currentTerritory : ownedTerritories){

            competenceAfterReduction = currentTerritory.updateArmiesCompetence();

            if(currentTerritory.getArmyThreshold() > competenceAfterReduction)
                toRemove.add(currentTerritory);
        }

        for (Territory currentTerritory : toRemove)
            currentTerritory.setNeutral();

        return toRemove;
    }

    public static ArrayList<Player> reconstructPlayers(ArrayList<Player> players, int initialFunds){
        ArrayList<Player> playersToReconstruct = new ArrayList<>(players.size());

        for(int i = 0; i < players.size(); i++)
            playersToReconstruct.add(i, new Player(players.get(i).id, players.get(i).name, initialFunds, players.get(i).color, players.get(i).colorName));

        return playersToReconstruct;
    }

    public int getTerritoriesTotalProfit(){
        int totalProfits = 0;
        for(Territory territory : ownedTerritories)
            totalProfits += territory.getProfit();

        return totalProfits;
    }

    public void setColor(Color color, String colorName){
        this.color = color;
        this.colorName = colorName;
    }

    public Color getColor() { return color; }

    public String getColorName() { return colorName; }

    public boolean isSurrendered() {
        return surrendered;
    }

    public HashSet<Territory> setSurrendered(boolean surrendered) {
        HashSet<Territory> territoriesToRemove = null;
        this.surrendered = surrendered;
        if(surrendered) {
            territoriesToRemove = new HashSet<>(ownedTerritories);
            for (Territory territory : territoriesToRemove)
                territory.setNeutral();
        }
        return territoriesToRemove;
    }
}
