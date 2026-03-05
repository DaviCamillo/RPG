public class Potion extends Item {

    private int healedLife;

    public Potion(String name, int value, int size, int healedLife) {
        super(name, value, size);
        this.healedLife = healedLife;
    }

    // GETTER
    public int getHealedLife() {
        return healedLife;
    }

    // SETTER
    public void setHealedLife(int healedLife) {
        if (healedLife > 0) {
            this.healedLife = healedLife;
        }
    }
}