// CLASSE BASE DOS MONSTROS
// ========================
public class Monsters {

    String name;
    int life;
    int maxLife;
    int damage;
    int dropCoin;
    int dropXp;
    int speed;
    int resistance;
    public Monsters(String name, int life, int damage, int dropCoin, int dropXp, int speed, int resistance) {
        this.name = name;
        this.life = life;        // Vida atual começa cheia
        this.maxLife = life;     // Vida máxima igual à vida inicial
        this.damage = damage;
        this.dropCoin = dropCoin;
        this.dropXp = dropXp;
        this.speed = speed;
        this.resistance = resistance;
    }

    // Método de ataque contra o personagem
    public void atack(Character alvo) {

        // Calcula o dano final considerando a resistência do personagem
        int danoFinal = this.damage - alvo.resistance;

        // Impede que o dano seja negativo
        if (danoFinal < 0) {
            danoFinal = 0;
        }

        // Aplica o dano
        alvo.life -= danoFinal;

        System.out.println(this.name + " atacou " + alvo.name +
                           " causando " + danoFinal + " de dano!");

        // Verifica se o personagem morreu
        if (alvo.life <= 0) {
            alvo.life = 0;
            System.out.println(alvo.name + " morreu!");
        } else {
            System.out.println(alvo.name + " agora tem " +
                               alvo.life + "/" + alvo.maxLife + " de vida.");
        }
    }
}


// ========================
// GOBLIN NORMAL
// ========================
// Só permite definir o nome.
// Todos os outros atributos já são padrão.
class Goblin extends Monsters {

    public Goblin(String name) {

        // name
        // life
        // damage
        // dropCoin
        // dropXp
        // speed
        // resistance
        super(name, 8, 2, 3, 2, 20, 0);
    }
}


// ========================
// GOBLIN EXPERIENTE
// ========================
// Versão mais forte do goblin normal
class Goblin_exp extends Monsters {

    public Goblin_exp(String name) {

        // Mais vida, mais dano, mais recompensa
        super(name, 12, 3, 5, 4, 18, 0);
    }
}


// ========================
// GOBLIN BOSS
// ========================
// Inimigo mais forte da dungeon
class Goblin_boss extends Monsters {

    public Goblin_boss(String name) {

        // Muito mais vida
        // Mais dano
        // Possui resistência (reduz dano recebido)
        // Dropa mais moedas e XP
        super(name, 25, 5, 10, 10, 16, 4);
    }
}