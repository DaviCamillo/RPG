package com.RPG.TheLastRoar;

public class Armor extends Item {

    private int resistance;

    public Armor(String name, int value, int size, int resistance) {
        super(name, value, size);
        this.resistance = resistance;
    }

    // GETTER
    public int getResistance() {
        return resistance;
    }

    // SETTER
    public void setResistance(int resistance) {
        if (resistance >= 0) {
            this.resistance = resistance;
        }
    }
}