package engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class Army implements Serializable {

    private int amount;
    private int competence;
    private final Unit unit;
    private final Player controllingPlayer;

    public Army(int amount, Unit unit, Player controllingPlayer){
        this.amount = amount;
        this.competence = unit.getMaxFirePower() * amount;
        this.unit = unit;
        this.controllingPlayer = controllingPlayer;
    }

    public Army(int amount, int competence, Unit unit, Player controllingPlayer){
        this.amount = amount;
        this.competence = competence;
        this.unit = unit;
        this.controllingPlayer = controllingPlayer;
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

    public void addAmount(int amountToAdd){
        this.amount += amountToAdd;
    }

    public Unit getUnit() {
        return unit;
    }

    public int setCompetence(int competenceToSet){
        this.competence = competenceToSet;
        return this.competence;
    }

    public void addCompetence(int competenceToAdd){
        this.competence += competenceToAdd;
    }

    public int reduceCompetence() {
        this.competence -= (getUnit().getCompetenceReduction() * amount);
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
                currentArmy.addAmount(nextArmy.getAmount());
                currentArmy.addCompetence(nextArmy.getCompetence());
                armies.remove(i);
                armiesSize--;
                i--;
            } else
               currentArmy = nextArmy;
        }
    }

    public Player getControllingPlayer(){
        return controllingPlayer;
    }
}
