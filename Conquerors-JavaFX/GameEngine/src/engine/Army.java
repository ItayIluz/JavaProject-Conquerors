package engine;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class Army implements Serializable {

    private Territory inTerritory;
    private transient IntegerProperty amount;
    private transient IntegerProperty competence;
    private final Unit unit;
    private Player controllingPlayer;
    private boolean isNew = false;

    public Army(int amount, Unit unit, Player controllingPlayer){
        this.inTerritory = null;
        this.amount = new SimpleIntegerProperty(amount);
        this.competence = new SimpleIntegerProperty(unit.getMaxFirePower() * amount);
        this.controllingPlayer = controllingPlayer;
        this.unit = unit;
    }

    public Army(Territory inTerritory, int amount, Unit unit, Player controllingPlayer){
        this.inTerritory = inTerritory;
        this.amount = new SimpleIntegerProperty(amount);
        this.competence = new SimpleIntegerProperty(unit.getMaxFirePower() * amount);
        this.controllingPlayer = controllingPlayer;
        this.unit = unit;
    }

    public Army(Territory inTerritory, int amount, int competence, Unit unit, Player controllingPlayer){
        this.inTerritory = inTerritory;
        this.amount = new SimpleIntegerProperty(amount);
        this.competence = new SimpleIntegerProperty(competence);
        this.controllingPlayer = controllingPlayer;
        this.unit = unit;
    }

    public int getAmount() {
        return amount.get();
    }

    public IntegerProperty getAmountProperty() {
        return amount;
    }

    public int getCompetence() {
        return competence.get();
    }

    public IntegerProperty getCompetenceProperty() {
        return competence;
    }

    public void setAmount(int amount) {
        unit.addToTotalOnBoard(-1 * this.amount.get());
        unit.addToTotalOnBoard(amount);
        this.amount.set(amount);
    }

    public int getTotalCost(){
        return unit.getPurchasePrice() * amount.get();
    }

    public Unit getUnit() {
        return unit;
    }

    public Player getControllingPlayer() {
        return controllingPlayer;
    }

    public void setCompetence(int competenceToSet){
        this.competence.set(competenceToSet);
    }

    public int reduceCompetence() {
        competence.set(competence.get() - getUnit().getCompetenceReduction() * amount.get());
        return this.competence.get();
    }

    public void fixCompetence(){
        this.competence.set(unit.getMaxFirePower() * amount.get());
    }

    public int calculateCompetenceFixCost(){
        return Math.round(unit.getSingleFirePowerPrice() * ((unit.getMaxFirePower() * amount.get()) - competence.get()));
    }

    public IntegerProperty getCompetenceFixCostProperty(){
        return new SimpleIntegerProperty(calculateCompetenceFixCost());
    }

    public static void uniteSameTypeArmies(ArrayList<Army> armies){
        armies.sort(Comparator.comparing((Army a) -> a.getUnit().getType()));
        Army currentArmy = armies.get(0);
        int armiesSize = armies.size();
        for(int i = 1; i < armiesSize; i++){
            Army nextArmy = armies.get(i);
            if(currentArmy.getUnit().getType().equals(nextArmy.getUnit().getType())){
                currentArmy.amount.set(currentArmy.amount.get() + nextArmy.amount.get());
                currentArmy.competence.set(currentArmy.competence.get() + nextArmy.competence.get());
                armies.remove(i);
                armiesSize--;
                i--;
            } else
               currentArmy = nextArmy;
        }
    }

    public Territory getInTerritory() {
        return inTerritory;
    }

    public void setInTerritory(Territory inTerritory) {
        this.inTerritory = inTerritory;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public static ArrayList<Army> cloneArmies(ArrayList<Army> toClone, Player controllingPlayer){
        ArrayList<Army> clonedArmies = new ArrayList<>();

        for (Army army : toClone)
            clonedArmies.add(new Army(army.getInTerritory(), army.getAmount(), army.getCompetence(), army.getUnit(), controllingPlayer));

        return clonedArmies;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {

        out.defaultWriteObject();

        out.writeObject(amount.get());
        out.writeObject(competence.get());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

        in.defaultReadObject();

        amount = new SimpleIntegerProperty((int) in.readObject());
        competence = new SimpleIntegerProperty((int) in.readObject());
    }
}
