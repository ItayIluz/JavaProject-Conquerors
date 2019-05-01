package engine;

import java.io.Serializable;

public class Unit implements Serializable {

    private String type;
    private int rank;
    private int purchasePrice;
    private int maxFirePower;
    private int competenceReduction;
    private float singleFirePowerPrice;

    public Unit(String type, int rank, int purchasePrice, int maxFirePower, int competenceReduction){
        this.type = type;
        this.rank = rank;
        this.purchasePrice = purchasePrice;
        this.maxFirePower = maxFirePower;
        this.competenceReduction = competenceReduction;
        this.singleFirePowerPrice = (float) purchasePrice / (float) maxFirePower;
    }

    public String getType(){
        return type;
    }

    public int getRank() {
        return rank;
    }

    public int getPurchasePrice() {
        return purchasePrice;
    }

    public int getMaxFirePower() {
        return maxFirePower;
    }

    public int getCompetenceReduction() {
        return competenceReduction;
    }

    public float getSingleFirePowerPrice(){
        return singleFirePowerPrice;
    }
}
