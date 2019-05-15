package engine;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

public class Territory implements Serializable {

    private transient IntegerProperty id;
    private transient StringProperty conquerorTextProperty = new SimpleStringProperty("Netural");
    private int profit;
    private int armyThreshold;
    private boolean isConquered = false;
    private Player conqueringPlayer = null;
    private ArrayList<Army> armies = new ArrayList<>();

    public Territory(int id, int profit, int armyThreshold){
        this.id = new SimpleIntegerProperty(id);
        this.profit = profit;
        this.armyThreshold = armyThreshold;
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty getIdProperty() {
        return id;
    }

    public int getProfit() {
        return profit;
    }

    public int getArmyThreshold() {
        return armyThreshold;
    }

    public boolean isConquered() {
        return isConquered;
    }

    public Player getConqueringPlayer() {
        return conqueringPlayer;
    }

    public int getTotalFirepower(){
        int totalFirepower = 0;
        for(Army army : armies)
            totalFirepower += army.getCompetence();
        return totalFirepower;
    }

    public void setConqueredByPlayer(Player conqueringPlayer, ArrayList<Army> armies) {
        this.isConquered = true;
        this.conqueringPlayer = conqueringPlayer;
        this.conqueringPlayer.addTerritory(this);
        this.armies = armies;
        conquerorTextProperty.setValue("Conqueror: " + conqueringPlayer.getName());
    }

    public void setNeutral(){
        isConquered = false;
        conqueringPlayer.removeTerritory(this);
        conqueringPlayer = null;

        for(Army army : armies)
            army.getUnit().addToTotalOnBoard(-1 * army.getAmount());

        armies.clear();
        conquerorTextProperty.setValue("Neutral");
    }

    public StringProperty getConquerorTextProperty(){
        return conquerorTextProperty;
    }

    public ArrayList<Army> getArmies() {
        return armies;
    }

    public void setArmies(ArrayList<Army> armies) {
        this.armies = armies;
    }

    public int updateArmiesCompetence(){ // copy to territory and separate
        int competenceAfterReduction = 0;

        for(Army currentArmy : armies)
            competenceAfterReduction += currentArmy.reduceCompetence();

        return competenceAfterReduction;
    }

    public ArrayList<Army> attackDeterministic(ArrayList<Army> attackingArmy, ArrayList<Army> defendingArmy){

        attackingArmy.sort(Comparator.comparingInt(a -> a.getUnit().getRank()));
        defendingArmy.sort(Comparator.comparingInt(a -> a.getUnit().getRank()));

        Army currentAttackingArmy, currentDefendingArmy;
        ArrayList<Army> winningArmy;
        int attackingArmyIndex = 0, defendingArmyIndex = 0, attackingArmyHighestRank = 0, defendingArmyHighestRank = 0;
        int attackingArmySize = attackingArmy.size();
        int defendingArmySize = defendingArmy.size();
        int biggestArmySize = attackingArmySize > defendingArmySize ? attackingArmySize : defendingArmySize;

        for(int i = 0; i < biggestArmySize; i++){

            if(defendingArmyIndex < defendingArmySize)
                currentDefendingArmy = defendingArmy.get(defendingArmyIndex);
            else
                break;

            if(attackingArmyIndex < attackingArmySize)
                currentAttackingArmy = attackingArmy.get(attackingArmyIndex);
            else
                break;

            int currentAttackingUnitRank = currentAttackingArmy.getUnit().getRank();
            int currentDefendingUnitRank = currentDefendingArmy.getUnit().getRank();

            if(currentAttackingUnitRank == currentDefendingUnitRank){
                int currentAttackingArmyAmount = currentAttackingArmy.getAmount();
                int currentDefendingArmyAmount = currentDefendingArmy.getAmount();

                if(currentAttackingArmyAmount > currentDefendingArmyAmount){

                    deterministicAttackCalculateUnitDifferences(currentAttackingArmy, currentAttackingArmyAmount, currentDefendingArmyAmount);
                    defendingArmy.remove(defendingArmyIndex);
                    attackingArmyHighestRank = currentAttackingUnitRank;

                } else if(currentDefendingArmyAmount > currentAttackingArmyAmount) {

                    deterministicAttackCalculateUnitDifferences(currentDefendingArmy, currentDefendingArmyAmount, currentAttackingArmyAmount);
                    attackingArmy.remove(attackingArmyIndex);
                    defendingArmyHighestRank = currentDefendingUnitRank;

                } else { // currentAttackingArmyAmount == currentDefendingArmyAmount
                    attackingArmy.remove(attackingArmyIndex);
                    defendingArmy.remove(defendingArmyIndex);
                }

                attackingArmyIndex++;
                defendingArmyIndex++;

            } else { // Move to next rank army
                if(currentAttackingUnitRank > currentDefendingUnitRank)
                    defendingArmyIndex++;
                 else  // Then currentAttackingUnitRank < currentDefendingUnitRank
                    attackingArmyIndex++;

                attackingArmyHighestRank = currentAttackingUnitRank;
                defendingArmyHighestRank = currentDefendingUnitRank;
            }
        }

        if(attackingArmyHighestRank > defendingArmyHighestRank)
            winningArmy = attackingArmy;
         else  // (attackingArmyHighestRank <= defendingArmyHighestRank
            winningArmy = defendingArmy;

        return calculateAttackResults(winningArmy);
    }

    private void deterministicAttackCalculateUnitDifferences(Army winningArmy, int winningArmyAmount,int losingArmyAmount){
        int difference, currentCompetence;

        difference = winningArmyAmount - losingArmyAmount;
        winningArmy.setAmount(difference);
        currentCompetence = winningArmy.getCompetence();
        winningArmy.setCompetence(currentCompetence / winningArmyAmount * difference);
    }

    public ArrayList<Army> attackImFeelingLucky(ArrayList<Army> attackingArmy, ArrayList<Army> defendingArmy){
        int attackingTotalFirepower = 0, defendingTotalFirepower = 0, chance, winningFirepower, losingFirepower;
        double damageRatio;
        ArrayList<Army> winningArmy;

        for(Army army : attackingArmy)
            attackingTotalFirepower += army.getCompetence();

        for(Army army : defendingArmy)
            defendingTotalFirepower += army.getCompetence();

        chance = ThreadLocalRandom.current().nextInt(0, attackingTotalFirepower + defendingTotalFirepower);

        if(attackingTotalFirepower > chance){ // Attacking army wins
            winningArmy = attackingArmy;
            winningFirepower = attackingTotalFirepower;
            losingFirepower = defendingTotalFirepower;
        } else { // Defending army wins
            winningArmy = defendingArmy;
            winningFirepower = defendingTotalFirepower;
            losingFirepower = attackingTotalFirepower;
        }

        if(winningFirepower > losingFirepower)
            damageRatio = ((double)losingFirepower / (double)winningFirepower);
        else // (winningFirepower <= losingFirepower
            damageRatio = 0.5;

        for(Army army : winningArmy)
            army.setCompetence((int) Math.ceil(army.getCompetence() * (1.0 - damageRatio)));

        return calculateAttackResults(winningArmy);
    }

    private ArrayList<Army> calculateAttackResults(ArrayList<Army> winningArmy){

        if(winningArmy.size() > 0) {
            Player winningPlayer = winningArmy.get(0).getControllingPlayer();
            ArrayList<Army> copyOfWinningArmy = Army.cloneArmies(winningArmy, winningPlayer);
            int totalRemainingFirepower = 0;

            for (Army army : winningArmy)
                totalRemainingFirepower += army.getCompetence();

            if (winningPlayer != this.getConqueringPlayer()) { // winning player tries to conquer
                setNeutral();

                if (totalRemainingFirepower >= armyThreshold) { // If army is enough conquer the territory
                    setConqueredByPlayer(winningPlayer, winningArmy);
                    for (Army army : armies)
                        army.getUnit().addToTotalOnBoard(army.getAmount());
                } else { // If army is not enough return money

                    for (Army army : winningArmy)
                        winningPlayer.addMoney(Math.round(army.getUnit().getSingleFirePowerPrice() * army.getCompetence()));
                }
            } else { // winning player defends his territory
                if (totalRemainingFirepower < armyThreshold) { // If army is not enough lose territory and return money

                    for (Army army : winningArmy)
                        winningPlayer.addMoney(Math.round(army.getUnit().getSingleFirePowerPrice() * army.getCompetence()));

                    setNeutral();
                }
            }
            return copyOfWinningArmy;
        } else {
            setNeutral();
            return winningArmy;
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {

        out.defaultWriteObject();

        out.writeObject(id.get());
        out.writeObject(conquerorTextProperty.get());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

        in.defaultReadObject();

        id = new SimpleIntegerProperty((int) in.readObject());
        conquerorTextProperty = new SimpleStringProperty((String) in.readObject());
    }
}
