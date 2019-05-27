package engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class Army implements Serializable {

    private Territory inTerritory;
    private int amount;
    private int competence;
    private final Unit unit;
    private Player controllingPlayer;
    private boolean isNew = false;

    public Army(int amount, Unit unit, Player controllingPlayer){
        this.inTerritory = null;
        this.amount = amount;
        this.competence = unit.getMaxFirePower() * amount;
        this.controllingPlayer = controllingPlayer;
        this.unit = unit;
    }

    public Army(Territory inTerritory, int amount, Unit unit, Player controllingPlayer){
        this.inTerritory = inTerritory;
        this.amount = amount;
        this.competence = unit.getMaxFirePower() * amount;
        this.controllingPlayer = controllingPlayer;
        this.unit = unit;
    }

    public Army(Territory inTerritory, int amount, int competence, Unit unit, Player controllingPlayer){
        this.inTerritory = inTerritory;
        this.amount = amount;
        this.competence = competence;
        this.controllingPlayer = controllingPlayer;
        this.unit = unit;
    }

    public int getAmount() {
        return amount;
    }

    public int getCompetence() {
        return competence;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getTotalCost(){
        return unit.getPurchasePrice() * amount;
    }

    public Unit getUnit() {
        return unit;
    }

    public Player getControllingPlayer() {
        return controllingPlayer;
    }

    public void setCompetence(int competenceToSet){
        this.competence = competenceToSet;
    }

    public int reduceCompetence() {
        competence = competence - getUnit().getCompetenceReduction() * amount;
        return this.competence;
    }

    public void fixCompetence(){
        this.competence = unit.getMaxFirePower() * amount;
    }

    public int calculateCompetenceFixCost(){
        return Math.round(unit.getSingleFirePowerPrice() * ((unit.getMaxFirePower() * amount) - competence));
    }

    public static void uniteSameTypeArmies(ArrayList<Army> armies){
        armies.sort(Comparator.comparing((Army a) -> a.getUnit().getType()));
        Army currentArmy = armies.get(0);
        int armiesSize = armies.size();
        for(int i = 1; i < armiesSize; i++){
            Army nextArmy = armies.get(i);
            if(currentArmy.getUnit().getType().equals(nextArmy.getUnit().getType())){
                currentArmy.amount = currentArmy.amount + nextArmy.amount;
                currentArmy.competence = currentArmy.competence + nextArmy.competence;
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
}
