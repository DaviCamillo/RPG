import java.util.ArrayList;
import java.util.List;

public class Monsters {

    private String name;
    private int life;
    private int maxLife;
    private int damage;
    private int dropCoin;
    private int dropXp;
    private int speed;
    private int resistance;

    public Monsters(String name, int life, int damage,int dropCoin, int dropXp,int speed, int resistance) {
        this.name = name;
        this.life = life;
        this.maxLife = life;
        this.damage = damage;
        this.dropCoin = dropCoin;
        this.dropXp = dropXp;
        this.speed = speed;
        this.resistance = resistance;
    }

    // GETTERS
    public String getName() { return name; }
    public int getLife() { return life; }
    public int getMaxLife() { return maxLife; }
    public int getDamage() { return damage; }
    public int getDropCoin() { return dropCoin; }
    public int getDropXp() { return dropXp; }
    public int getSpeed() { return speed; }
    public int getResistance() { return resistance; }

    public void setLife(int life) {
        if (life < 0) life = 0;
        if (life > maxLife) life = maxLife;
        this.life = life;
    }

    public void attack(Character alvo) {

        int danoFinal = Math.max(0, this.damage - alvo.getResistance());

        alvo.setLife(alvo.getLife() - danoFinal);

        System.out.println(this.name + " atacou " + alvo.getName() +
                " causando " + danoFinal + " de dano!");

        if (alvo.getLife() <= 0) {
            System.out.println(alvo.getName() + " morreu!");
        } else {
            System.out.println(alvo.getName() + " agora tem " +
                    alvo.getLife() + "/" + alvo.getMaxLife() + " de vida.");
        }
    }

    // CRIADORES DE MONSTROS
    public static List<Monsters> criarGoblins(int quantidade) {
        List<Monsters> lista = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            lista.add(new Goblin());
        }
        return lista;
    }

    public static List<Monsters> criarGoblinsExp(int quantidade) {
        List<Monsters> lista = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            lista.add(new GoblinExp());
        }
        return lista;
    }

    public static List<Monsters> criarGoblinBoss(int quantidade) {
        List<Monsters> lista = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            lista.add(new GoblinBoss());
        }
        return lista;
    }
}

// CLASSES FILHAS
class Goblin extends Monsters {
    public Goblin() {
        super("Goblin", 8, 2, 3, 2, 20, 0);
    }
}

class GoblinExp extends Monsters {
    public GoblinExp() {
        super("Goblin Experiente", 12, 3, 5, 4, 18, 0);
    }
}

class GoblinBoss extends Monsters {
    public GoblinBoss() {
        super("Bazinga", 25, 5, 10, 10, 16, 4);
    }
}