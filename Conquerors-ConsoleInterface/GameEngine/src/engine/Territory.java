package engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Territory implements Serializable {

    private int id;
    private int profit;
    private int armyThreshold;
    private boolean isConquered = false;
    private int conqueringPlayerId = -1;

    public Territory(int id, int profit, int armyThreshold){
        this.id = id;
        this.profit = profit;
        this.armyThreshold = armyThreshold;
    }

    public int getId() {
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

    public int getConqueringPlayerId() {
        return conqueringPlayerId;
    }

    public void setConqueredByPlayer(int conqueringPlayerId) {
        this.isConquered = true;
        this.conqueringPlayerId = conqueringPlayerId;
    }

    public void setNeutral(){
        this.isConquered = false;
        this.conqueringPlayerId = -1;
    }

    public void attackImFeelingLucky(ArrayList<Army> attackingArmy, ArrayList<Army> defendingArmy){
        int attackingTotalFirepower = 0;
        int defendingTotalFirepower = 0;
        int chance;

        for(Army army : attackingArmy)
            attackingTotalFirepower += army.getCompetence();

        for(Army army : defendingArmy)
            defendingTotalFirepower += army.getCompetence();

        chance = ThreadLocalRandom.current().nextInt(0, attackingTotalFirepower + defendingTotalFirepower);

        if(attackingTotalFirepower > chance) // Attacking army wins
            calculateAttackResults(attackingArmy, attackingTotalFirepower, defendingArmy, defendingTotalFirepower);
        else  // Defending army wins
            calculateAttackResults(defendingArmy, defendingTotalFirepower, attackingArmy, attackingTotalFirepower);
    }

    private void calculateAttackResults(ArrayList<Army> winningArmy, int winningFirepower, ArrayList<Army> losingArmy, int losingFirepower){
        double damageRatio;
        Player winningPlayer = winningArmy.get(0).getControllingPlayer();
        Player losingPlayer = losingArmy.get(0).getControllingPlayer();
        int totalRemainingFirepower = 0;

        if(winningFirepower > losingFirepower)
            damageRatio = ((double)losingFirepower / (double)winningFirepower);
         else // (winningFirepower <= losingFirepower
            damageRatio = 0.5;

        for(Army army : winningArmy)
            totalRemainingFirepower += army.setCompetence((int) Math.ceil(army.getCompetence() * (1.0 - damageRatio)));

        if(winningPlayer.getId() != this.getConqueringPlayerId()){ // winning player tries to conquer
            losingPlayer.loseTerritory(this);

            if(totalRemainingFirepower >= armyThreshold)
                winningPlayer.conquerTerritory(this, winningArmy);
            else {
                for(Army army : winningArmy)
                    winningPlayer.addMoney(Math.round(army.getUnit().getSingleFirePowerPrice() * army.getCompetence()));
            }
        } else { // winning player defends his territory
            if(totalRemainingFirepower < armyThreshold) {
                winningPlayer.loseTerritory(this);
                for(Army army : winningArmy)
                    winningPlayer.addMoney(Math.round(army.getUnit().getSingleFirePowerPrice() * army.getCompetence()));
            }
        }
    }
}
