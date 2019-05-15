package engine;

import javafx.beans.property.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Unit implements Serializable {

    private transient StringProperty type;
    private transient IntegerProperty rank;
    private transient IntegerProperty purchasePrice;
    private transient IntegerProperty maxFirePower;
    private transient IntegerProperty competenceReduction;
    private transient FloatProperty singleFirePowerPrice;
    private transient IntegerProperty totalOnBoard = new SimpleIntegerProperty(0);

    public Unit(String type, int rank, int purchasePrice, int maxFirePower, int competenceReduction){
        this.type = new SimpleStringProperty(type);
        this.rank = new SimpleIntegerProperty(rank);
        this.purchasePrice = new SimpleIntegerProperty(purchasePrice);
        this.maxFirePower = new SimpleIntegerProperty(maxFirePower);
        this.competenceReduction = new SimpleIntegerProperty(competenceReduction);
        this.singleFirePowerPrice = new SimpleFloatProperty((float) purchasePrice / (float) maxFirePower);
    }

    public Unit(Unit copyUnit){
        this.type = copyUnit.type;
        this.rank = copyUnit.rank;
        this.purchasePrice = copyUnit.purchasePrice;
        this.maxFirePower = copyUnit.maxFirePower;
        this.competenceReduction = copyUnit.competenceReduction;
        this.singleFirePowerPrice = new SimpleFloatProperty((float) copyUnit.purchasePrice.get() / (float) copyUnit.maxFirePower.get());
    }

    public String getType(){
        return type.get();
    }

    public StringProperty getTypeProperty(){ return type; }

    public int getRank() { return rank.get(); }

    public IntegerProperty getRankProperty(){ return rank; }

    public int getPurchasePrice() {
        return purchasePrice.get();
    }

    public IntegerProperty getPurchasePriceProperty(){ return purchasePrice; }

    public int getMaxFirePower() {
        return maxFirePower.get();
    }

    public IntegerProperty getMaxFirePowerProperty(){ return maxFirePower; }

    public int getCompetenceReduction() {
        return competenceReduction.get();
    }

    public IntegerProperty getCompetenceReductionProperty(){ return competenceReduction; }

    public float getSingleFirePowerPrice(){
        return singleFirePowerPrice.get();
    }

    public FloatProperty getSingleFirePowerPriceProperty(){ return singleFirePowerPrice; }

    public void setTotalOnBoard(int toSet) { totalOnBoard.set(toSet); }

    public int getTotalOnBoard() {
        return totalOnBoard.get();
    }

    public IntegerProperty getTotalOnBoardProperty() { return totalOnBoard; }

    public void addToTotalOnBoard(int toAdd){ totalOnBoard.set(totalOnBoard.get() + toAdd);}

    private void writeObject(ObjectOutputStream out) throws IOException {

        out.defaultWriteObject();

        out.writeObject(type.get());
        out.writeObject(rank.get());
        out.writeObject(purchasePrice.get());
        out.writeObject(maxFirePower.get());
        out.writeObject(competenceReduction.get());
        out.writeObject(singleFirePowerPrice.get());
        out.writeObject(totalOnBoard.get());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

        in.defaultReadObject();

        type = new SimpleStringProperty((String) in.readObject());
        rank = new SimpleIntegerProperty((int) in.readObject());
        purchasePrice = new SimpleIntegerProperty((int) in.readObject());
        maxFirePower = new SimpleIntegerProperty((int) in.readObject());
        competenceReduction = new SimpleIntegerProperty((int) in.readObject());
        singleFirePowerPrice = new SimpleFloatProperty((float) in.readObject());
        totalOnBoard = new SimpleIntegerProperty((int) in.readObject());
    }
}
