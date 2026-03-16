package com.RPG.TheLastRoar;

import java.util.ArrayList;
import java.util.List;

/**
 * Monsters.java - A CLASSE BASE DE TODOS OS INIMIGOS
 * 
 * O QUE FAZ:
 * Define as propriedades e o comportamento PADRÃO de todos os inimigos
 * Classes como Goblin, GoblinExp e GoblinBoss herdam desta classe
 * 
 * ESTRUTURA:
 * - Cada inimigo tem vida, dano, resistência, XP, moedas, etc
 * - Todos os inimigos podem atacar e receber dano
 * - Herança: Goblin -> Monsters
 * 
 * ADICIONAR NOVO INIMIGO:
 * Crie uma classe que herde de Monsters e defina os valores específicos
 * 
 * Exemplo:
 *   public class Dragão extends Monsters {
 *       public Dragão() {
 *           super("Dragão", 50, 10, 20, 5, 100, 10);
 *       }
 *   }
 */
public class Monsters {

    // ===== INFORMAÇÕES BÁSICAS DO MONSTRO =====
    private String name;        // Nome do inimigo (Goblin, Orc, Dragão, etc)
    private int life;           // Vida ATUAL do inimigo
    private int maxLife;        // Vida MÁXIMA (até quanto pode recuperar)
    private int damage;         // Dano que o inimigo faz ao atacar
    private int dropCoin;       // Moedas que o jogador ganha ao derrotar este inimigo
    private int dropXp;         // Experiência que o jogador ganha ao derrotar este inimigo
    private int speed;          // Velocidade de movimento (pixels por frame)
    private int resistance;     // Resistência/Defesa (reduz dano recebido)
    protected String imagePath;
    protected String battleImagePath;

    /**
     * CONSTRUTOR - Monsters()
     * 
     * Cria um novo inimigo com as propriedades fornecidas
     * 
     * Parâmetros:
     * - name: Nome do inimigo (ex: "Goblin")
     * - life: Vida máxima (a vida atual começa neste valor)
     * - damage: Força do ataque
     * - dropCoin: Moedas recompensadas
     * - dropXp: XP recompensado
     * - speed: Velocidade de movimento
     * - resistance: Quanto de dano é reduzido quando atacado
     */
    public Monsters(String name, int life, int damage, int dropCoin, int dropXp, int speed, int resistance) {
        this.name = name;
        this.life = life;           // Começa com vida máxima
        this.maxLife = life;        // Armazena qual é a vida máxima
        this.damage = damage;       // Quanto de dano faz
        this.dropCoin = dropCoin;   // Moedas que dá
        this.dropXp = dropXp;       // XP que dá
        this.speed = speed;         // Velocidade
        this.resistance = resistance;
    }

    /**
     * GETTERS - Métodos que retornam as informações do monstro
     * Use esses para obter informações sem poder mudar
     */
    public String getName() { return name; }              // Retorna o nome
    public int getLife() { return life; }                 // Retorna vida ATUAL
    public int getMaxLife() { return maxLife; }           // Retorna vida MÁXIMA
    public int getDamage() { return damage; }             // Retorna dano que faz
    public int getDropCoin() { return dropCoin; }         // Retorna moedas que dá
    public int getDropXp() { return dropXp; }             // Retorna XP que dá
    public int getSpeed() { return speed; }               // Retorna velocidade
    public int getResistance() { return resistance; }     // Retorna resistência
    public String getImagePath() { return imagePath; }    
    public String getBattleImagePath() { return battleImagePath; }
    /**
     * setLife(int life)
     * Altera a vida do inimigo
     * Valida para garantir que a vida fica entre 0 e maxLife
     * 
     * Parâmetro:
     * - life: O novo valor de vida
     * 
     * Exemplo:
     * inimigo.setLife(5);  // Muda a vida para 5
     * inimigo.setLife(-10); // Seria negativo, mas fica em 0
     * inimigo.setLife(1000); // Seria maior que o máximo, mas fica em maxLife
     */
    public void setLife(int life) {
        if (life < 0) life = 0;                // Garante que não fica negativo
        if (life > maxLife) life = maxLife;    // Garante que não passa do máximo
        this.life = life;                      // Atualiza a vida
    }

    /**
     * attack(Character alvo)
     * O inimigo ataca um personagem
     * 
     * COMO FUNCIONA:
     * 1. Calcula o dano final (dano do inimigo - resistência do jogador)
     * 2. Garante que o dano não é negativo (nunca cura!)
     * 3. Reduz a vida do jogador
     * 
     * Parâmetro:
     * - alvo: O personagem que será atacado
     * 
     * Exemplo:
     * goblin.attack(player);  // O goblin ataca o player
     */
    public void attack(Character alvo) {

        // CALCULA O DANO FINAL
        // Dano do inimigo MENOS resistência do jogador
        // Math.max garante que o resultado não é negativo
        int danoFinal = Math.max(0, this.damage - alvo.getResistance());

        // APLICA O DANO AO JOGADOR
        alvo.setLife(alvo.getLife() - danoFinal);
    }

    /**
     * ===== MÉTODOS PARA CRIAR GRUPOS DE INIMIGOS =====
     * Estes métodos facilitam criar múltiplos inimigos de uma vez
     */

    /**
     * criarGoblins(int quantidade)
     * Cria uma lista com vários Goblins comuns
     * 
     * Parâmetro:
     * - quantidade: Quantos goblins criar
     * 
     * Retorno:
     * - Uma List com os goblins criados
     * 
     * Exemplo:
     * List<Monsters> inimigos = Monsters.criarGoblins(5);  // Cria 5 goblins
     */
    public static List<Monsters> criarGoblins(int quantidade) {
        List<Monsters> lista = new ArrayList<>();  // Cria uma lista vazia
        
        // Cria quantos goblins foram solicitados
        for (int i = 0; i < quantidade; i++) {
            lista.add(new Goblin());  // Adiciona um novo goblin à lista
        }
        
        return lista;  // Retorna a lista preenchida
    }

    /**
     * criarGoblinsExp(int quantidade)
     * Cria uma lista com Goblins mais experientes (mais fortes)
     * 
     * Parâmetro:
     * - quantidade: Quantos goblins criar
     * 
     * Retorno:
     * - Uma List com os goblins criados
     */
    public static List<Monsters> criarGoblinsExp(int quantidade) {
        List<Monsters> lista = new ArrayList<>();  // Cria uma lista vazia
        
        for (int i = 0; i < quantidade; i++) {
            lista.add(new GoblinExp());  // Adiciona um GoblinExp
        }
        
        return lista;  // Retorna a lista preenchida
    }

    /**
     * criarGoblinBoss(int quantidade)
     * Cria uma lista com Goblins Boss (muito fortes)
     * 
     * Parâmetro:
     * - quantidade: Quantos bosses criar
     * 
     * Retorno:
     * - Uma List com os bosses criados
     */
    public static List<Monsters> criarGoblinBoss(int quantidade) {
        List<Monsters> lista = new ArrayList<>();  // Cria uma lista vazia
        
        for (int i = 0; i < quantidade; i++) {
            lista.add(new GoblinBoss());  // Adiciona um GoblinBoss
        }
        
        return lista;  // Retorna a lista preenchida
    }
}

class GoblinBoss extends Monsters {
    public GoblinBoss() {
        super("GoblinBoss", 25, 7, 15, 20, 10, 3);
        this.imagePath = "/images/sprite_goblin.png"; // <-- DEFINE A IMAGEM AQUI
        this.battleImagePath = "/images/goblin.png";
    }
}

class GoblinExp extends Monsters {
    public GoblinExp() {
        super("GoblinExp", 13, 99, 5, 4, 20, 1);
        this.imagePath = "/images/sprite_goblin.png"; // <-- DEFINE A IMAGEM AQUI
        this.battleImagePath = "/images/goblin.png";
    }
}

// Crie a classe do Goblin normal se não tiver:
class Goblin extends Monsters {
    public Goblin() {
        super("Goblin", 8, 2, 3, 2, 20, 0); // Exemplo de status
        this.imagePath = "/images/sprite_goblin.png";
        this.battleImagePath = "/images/goblin.png";
    }
}