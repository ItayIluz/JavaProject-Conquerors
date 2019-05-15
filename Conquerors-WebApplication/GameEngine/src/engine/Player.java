package engine;

import javafx.beans.property.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class Player implements Serializable {

    private transient IntegerProperty id;
    private transient StringProperty name;
    private transient IntegerProperty money;
    private transient Color color;
    private transient StringProperty colorName;
    private boolean surrendered = false;
    private HashSet<Territory> ownedTerritories = new HashSet<>();

    private void writeObject(ObjectOutputStream out) throws IOException {

        out.defaultWriteObject();

        out.writeObject(id.get());
        out.writeObject(name.get());
        out.writeObject(money.get());
        out.writeDouble(color.getRed());
        out.writeDouble(color.getGreen());
        out.writeDouble(color.getBlue());
        out.writeDouble(color.getOpacity());
        out.writeObject(colorName.get());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

        in.defaultReadObject();

        id = new SimpleIntegerProperty((int) in.readObject());
        name = new SimpleStringProperty((String) in.readObject());
        money = new SimpleIntegerProperty((int) in.readObject());
        double red = in.readDouble();
        double green = in.readDouble();
        double blue = in.readDouble();
        double opacity = in.readDouble();
        color = Color.color(red, green, blue, opacity);
        colorName = new SimpleStringProperty((String) in.readObject());
    }

    public Player(int id, String name){
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.money = new SimpleIntegerProperty(0);
    }

    public Player(int id, String name, int money, Color color, String colorName){
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.money = new SimpleIntegerProperty(money);
        this.color = color;
        this.colorName = new SimpleStringProperty(colorName);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty getIdProperty() { return id; }

    public void setMoney(int money) {
        this.money = new SimpleIntegerProperty(money);
    }

    public String getName() { return name.get(); }

    public StringProperty getNameProperty() { return name; }

    public String getNameAndId() {
        return name.get() + " (" + id.get() + ")";
    }

    public int getMoney() {
        return money.get();
    }

    public IntegerProperty getMoneyProperty(){ return money; }

    public void subtractMoney(int moneyToSubtract) { money.set(money.get() - moneyToSubtract);}

    public void addMoney(int moneyToAdd) { money.set(money.get() + moneyToAdd);}

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
        return money.get() >= moneyToCompare;
    }

    public ArrayList<Territory> gainProfitsAndUpdateArmyCompetence(){
        int competenceAfterReduction;
        ArrayList<Territory> toRemove = new ArrayList<>();

        money.set(money.get() + getTerritoriesTotalProfit());

        for(Territory currentTerritory : ownedTerritories){

            competenceAfterReduction = currentTerritory.updateArmiesCompetence();

            if(currentTerritory.getArmyThreshold() > competenceAfterReduction)
                toRemove.add(currentTerritory);
        }

        for (Territory currentTerritory : toRemove)
            currentTerritory.setNeutral();

        return toRemove;
    }

    public static Player[] reconstructPlayers(Player[] players, int initialFunds){
        Player[] playersToReconstruct = new Player[players.length];

        for(int i = 0; i < players.length; i++)
            playersToReconstruct[i] = new Player(players[i].id.get(), players[i].name.get(), initialFunds, players[i].color, players[i].colorName.get());

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
        this.colorName = new SimpleStringProperty(colorName);
    }

    public Color getColor() { return color; }

    public String getColorName() { return colorName.get(); }

    public StringProperty getColorNameProperty() { return colorName; }

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
