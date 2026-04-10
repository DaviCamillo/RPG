package com.RPG.TheLastRoar.backend.models;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Monsters.java — Classe base de todos os inimigos do jogo
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Define as propriedades e comportamentos comuns a todos os inimigos.
 *   Subclasses (Goblin, GoblinExp, GoblinBoss) definem valores específicos.
 *
 * HIERARQUIA:
 *
 *   Monsters
 *    ├── Goblin      (inimigo básico do Mapa 0)
 *    ├── GoblinExp   (inimigo intermediário do Mapa 1)
 *    └── GoblinBoss  (boss do Mapa 2)
 *
 * TABELA DE ATRIBUTOS:
 *   ┌─────────────┬──────┬──────┬──────┬───────┬───────┬────────────┐
 *   │ Inimigo     │ HP   │ Dano │ Moed │ XP    │ Speed │ Resistance │
 *   ├─────────────┼──────┼──────┼──────┼───────┼───────┼────────────┤
 *   │ Goblin      │  8   │  2   │  3   │  2    │  20   │  0         │
 *   │ GoblinExp   │ 13   │ 99   │  5   │  4    │  20   │  1         │
 *   │ GoblinBoss  │ 25   │  7   │ 15   │  20   │  10   │  3         │
 *   └─────────────┴──────┴──────┴──────┴───────┴───────┴────────────┘
 *
 * COMO ADICIONAR NOVO INIMIGO:
 *   1. Crie uma classe que estende Monsters (no fim deste arquivo)
 *   2. Chame super() com os atributos desejados
 *   3. Defina imagePath e battleImagePath
 *   4. Adicione o novo inimigo em EnemyManager.configureForMap()
 *
 *   Exemplo:
 *     class Dragon extends Monsters {
 *         public Dragon() {
 *             super("Dragão", 50, 15, 30, 50, 8, 5);
 *             this.imagePath      = "/images/sprite_dragon.png";
 *             this.battleImagePath = "/images/dragon_battle.png";
 *         }
 *     }
 *
 * USADO POR:
 *   EnemyManager.java — instancia e gerencia os monstros no mapa
 *   Battle.java       — usa getDamage() no turno do inimigo
 *   Character.java    — alvo do ataque em attack(Monsters target)
 */
public class Monsters {

    // =========================================================================
    // FIELDS
    // =========================================================================

    /** Nome exibido na interface de batalha (ex: "Goblin"). */
    private String name;

    /** HP atual do inimigo. Começa igual ao maxLife. */
    private int life;

    /** HP máximo do inimigo. Usado para calcular a barra de HP. */
    private int maxLife;

    /** Dano base por ataque. Reduzido pela resistência do jogador. */
    private int damage;

    /** Moedas dropadas ao ser derrotado. Adicionadas ao personagem em Battle. */
    private int dropCoin;

    /** XP concedido ao jogador ao derrotar este inimigo. */
    private int dropXp;

    /** Velocidade de movimento horizontal no mapa (pixels por frame). */
    private int speed;

    /** Resistência do inimigo: reduz o dano recebido do jogador. */
    private int resistance;

    /** Caminho da sprite sheet usada no mapa (ex: "/images/sprite_goblin.png"). */
    protected String imagePath;

    /** Caminho da imagem de batalha exibida na tela de batalha. */
    protected String battleImagePath;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    /**
     * Cria um novo inimigo com os atributos fornecidos.
     *
     * @param name       Nome exibido na batalha
     * @param life       HP máximo (life atual inicia igual a este valor)
     * @param damage     Dano base por ataque
     * @param dropCoin   Moedas ganhas ao derrotar este inimigo
     * @param dropXp     XP ganho ao derrotar este inimigo
     * @param speed      Velocidade de movimento no mapa
     * @param resistance Pontos de resistência (reduz dano recebido)
     */
    public Monsters(String name, int life, int damage,
                    int dropCoin, int dropXp, int speed, int resistance) {
        this.name       = name;
        this.life       = life;
        this.maxLife    = life;     // maxLife guarda o valor inicial
        this.damage     = damage;
        this.dropCoin   = dropCoin;
        this.dropXp     = dropXp;
        this.speed      = speed;
        this.resistance = resistance;
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public String getName()             { return name; }
    public int getLife()                { return life; }
    public int getMaxLife()             { return maxLife; }
    public int getDamage()              { return damage; }
    public int getDropCoin()            { return dropCoin; }
    public int getDropXp()              { return dropXp; }
    public int getSpeed()               { return speed; }
    public int getResistance()          { return resistance; }
    public String getImagePath()        { return imagePath; }
    public String getBattleImagePath()  { return battleImagePath; }

    // =========================================================================
    // SETTERS
    // =========================================================================

    /**
     * Atualiza o HP atual do inimigo.
     * Garante que o valor fique entre 0 e maxLife.
     *
     * @param life Novo HP (automaticamente limitado entre 0 e maxLife).
     */
    public void setLife(int life) {
        this.life = Math.max(0, Math.min(life, maxLife));
    }

    // =========================================================================
    // COMBAT
    // =========================================================================

    /**
     * O inimigo ataca o personagem do jogador.
     *
     * CÁLCULO:
     *   dano_final = max(0, this.damage - player.getResistance())
     *   Garante que a resistência nunca resulte em cura involuntária.
     *
     * @param target Personagem que receberá o dano.
     */
    public void attack(Character target) {
        int finalDamage = Math.max(0, this.damage - target.getResistance());
        target.setLife(target.getLife() - finalDamage);
    }

    // =========================================================================
    // FACTORY METHODS (criação de grupos de inimigos)
    // =========================================================================

    /**
     * Cria uma lista com N Goblins comuns.
     *
     * @param amount Quantidade de goblins a criar.
     * @return Lista com os goblins criados.
     */
    public static List<Monsters> createGoblins(int amount) {
        List<Monsters> list = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            list.add(new Goblin());
        }
        return list;
    }

    /**
     * Cria uma lista com N GoblinsExp.
     *
     * @param amount Quantidade de goblins experientes a criar.
     * @return Lista com os goblins criados.
     */
    public static List<Monsters> createGoblinsExp(int amount) {
        List<Monsters> list = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            list.add(new GoblinExp());
        }
        return list;
    }

    /**
     * Cria uma lista com N GoblinBoss.
     *
     * @param amount Quantidade de bosses a criar.
     * @return Lista com os bosses criados.
     */
    public static List<Monsters> createGoblinBosses(int amount) {
        List<Monsters> list = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            list.add(new GoblinBoss());
        }
        return list;
    }
}

// =============================================================================
// SUBCLASSES DE MONSTROS
// =============================================================================
// Cada subclasse define apenas os atributos específicos via super().
// Para adicionar novos monstros, copie o padrão abaixo.
