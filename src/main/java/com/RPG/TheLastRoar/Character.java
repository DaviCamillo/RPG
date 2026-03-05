import java.util.List;
import java.util.Random;

public class Character {

    private String name;
    private int life;
    private int maxLife;
    private int coin;
    private int resistance;
    private Inventory inventory;
    private int nivel = 1;
    private int xp = 0;
    private int xpNecessary = 10;
    private Sword sword;
    private static final Random random = new Random();

    // CONSTRUTOR
    public Character(String name, int life, int resistance, Sword sword) {
        this.name = name;
        this.life = life;
        this.maxLife = life;
        this.resistance = resistance;
        this.coin = 0;
        this.inventory = new Inventory();
        this.sword = sword;
    }

    // GETTERS
    public String getName() { return name; }
    public int getLife() { return life; }
    public int getMaxLife() { return maxLife; }
    public int getCoin() { return coin; }
    public int getResistance() { return resistance; }
    public Inventory getInventory() { return inventory; }
    public int getNivel() { return nivel; }
    public int getXp() { return xp; }
    public int getXpNecessary() { return xpNecessary; }
    public Sword getSword() { return sword; }

    // CONTROLE DE VIDA
    public void setLife(int life) {
        this.life = Math.max(0, Math.min(life, maxLife));
    }

    public void addCoin(int amount) {
        if (amount > 0) {
            this.coin += amount;
        }
    }

    public void setSword(Sword sword) {
        if (sword != null) {
            this.sword = sword;
        }
    }

    // SISTEMA DE XP E LEVEL
    public void earnXp(int quantidade) {
        if (quantidade <= 0) return;

        xp += quantidade;
        System.out.println(name + " ganhou " + quantidade + " XP!");
        calculateLevel();
    }

    private void calculateLevel() {

        while (xp >= xpNecessary && nivel < 10) {

            xp -= xpNecessary;
            nivel++;
            xpNecessary += 5;

            System.out.println("Você subiu para o nível " + nivel + "!");

            maxLife += 2;
            life = maxLife;

            if (nivel == 5 || nivel == 10) {
                inventory.increaseSpace(5);
                System.out.println("Você ganhou +5 espaços no inventário!");
            }
        }

        if (nivel == 10 && xp >= xpNecessary) {
            xp = xpNecessary;
            System.out.println("Você está no nível máximo!");
        }
    }

    // ATAQUE ÚNICO
    public void attack(Monsters alvo) {

        if (alvo == null) return;

        System.out.println("\n" + name + " atacou " + alvo.getName() + "!");

        int danoFinal = Math.max(0, sword.calculateDamage() - alvo.getResistance());

        alvo.setLife(alvo.getLife() - danoFinal);

        System.out.println("Causou " + danoFinal + " de dano!");

        if (alvo.getLife() <= 0) {
            System.out.println(alvo.getName() + " morreu!");
        } else {
            System.out.println(alvo.getName() + " agora tem " + alvo.getLife() + " de vida.");
        }
    }

    // ATAQUE EM ÁREA
    public void attackArea(List<Monsters> alvos) {

        if (alvos == null || alvos.isEmpty()) return;

        int danoBase = sword.calculateDamage() / 2;

        for (int i = 0; i < alvos.size(); i++) {

            Monsters alvo = alvos.get(i);

            System.out.println("\n" + name + " atacou " + alvo.getName() + "!");

            int danoFinal = Math.max(0, danoBase - alvo.getResistance());

            alvo.setLife(alvo.getLife() - danoFinal);

            System.out.println("Causou " + danoFinal + " de dano!");

            if (alvo.getLife() <= 0) {
                System.out.println(alvo.getName() + " morreu!");
                alvos.remove(i);
                i--;
            }
        }
    }

    // TENTAR FUGIR
    public boolean leave() {

        int dado = random.nextInt(20) + 1;

        if (dado > 18) {
            System.out.println("Você fugiu!!");
            return true;
        } else {
            System.out.println("Você não conseguiu fugir!");
            return false;
        }
    }
}