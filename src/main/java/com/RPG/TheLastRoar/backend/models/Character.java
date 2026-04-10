package com.RPG.TheLastRoar.backend.models;

import java.util.List;
import java.util.Random;

import com.RPG.TheLastRoar.backend.managers.Inventory;

import javafx.scene.image.Image;

/**
 * ============================================================
 * Character.java — Personagem principal do jogador
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Controla TODAS as propriedades e ações do herói:
 *   vida, XP, nível, inventário, equipamentos e combate.
 *
 * SISTEMA DE RESISTÊNCIA:
 *   getResistance() retorna TOTAL = base + armadura equipada.
 *   Exemplo: base = 2, armadura = 5 → total = 7.
 *   Usado em Battle.java para calcular o dano recebido.
 *
 * SISTEMA DE LEVEL-UP:
 *   Ao ganhar XP suficiente, calculateLevel() é chamado e:
 *   - Incrementa o nível
 *   - Restaura o HP ao máximo
 *   - Ajusta o XP necessário para o próximo nível (*1.5)
 *   - Nos níveis 5 e 10: aumenta o inventário em 5 slots
 *
 * TABELA DE ATRIBUTOS INICIAIS:
 *   name="Hero", life=20, maxLife=20, resistance=0
 *   sword=Espada de Madeira (dano 3-6), coin=0, nivel=1
 *
 * USADO POR:
 *   App.java          — instancia e controla o personagem no mapa
 *   Battle.java       — usa attack(), heal(), leave(), earnXp()
 *   HudManager.java   — lê os atributos para exibir no HUD
 *   ShopNPC.java      — verifica/remove moedas, adiciona itens
 *   InventoryScreen   — lê/equipa items via setSword/setEquippedArmor
 *   SaveManager.java  — serializa/desserializa o estado do personagem
 */
public class Character {

    // =========================================================================
    // FIELDS — Informações básicas
    // =========================================================================

    private String name;
    private int life;
    private int maxLife;
    private int coin;

    /** Resistência BASE do personagem (sem contar a armadura equipada). */
    private int baseResistance;

    private Inventory inventory;

    // =========================================================================
    // FIELDS — Sistema de level
    // =========================================================================

    private int level         = 1;
    private int xp            = 0;
    private int xpNecessary   = 10; // XP necessário para o próximo nível

    // =========================================================================
    // FIELDS — Equipamentos
    // =========================================================================

    private Sword sword;

    /** Armadura atualmente equipada. null = sem armadura. */
    private Armor equippedArmor;

    // =========================================================================
    // FIELDS — Sprites e utilitários
    // =========================================================================

    /** Sprite sheet usada no mapa (animação de movimento). */
    private Image sprite;

    /** Imagem estática exibida na tela de batalha. */
    private Image battleSprite;

    private static final Random random = new Random();

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    /**
     * Cria o personagem principal.
     *
     * @param name          Nome do herói
     * @param life          HP inicial (e máximo)
     * @param baseResistance Resistência base (sem armadura)
     * @param sword         Espada inicial equipada
     * @param sprite        Sprite sheet do mapa
     * @param battleSprite  Imagem da batalha
     */
    public Character(String name, int life, int baseResistance,
                     Sword sword, Image sprite, Image battleSprite) {
        this.name           = name;
        this.life           = life;
        this.maxLife        = life;
        this.baseResistance = baseResistance;
        this.coin           = 0;
        this.inventory      = new Inventory();
        this.sword          = sword;
        this.sprite         = sprite;
        this.battleSprite   = battleSprite;
        this.equippedArmor  = null; // Começa sem armadura equipada
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public String getName()         { return name; }
    public int getLife()            { return life; }
    public int getMaxLife()         { return maxLife; }
    public int getCoin()            { return coin; }
    public Inventory getInventory() { return inventory; }
    public int getNivel()           { return level; }
    public int getXp()              { return xp; }
    public int getXpNecessary()     { return xpNecessary; }
    public int getMaxXp()           { return xpNecessary; }
    public Sword getSword()         { return sword; }
    public Image getSprite()        { return sprite; }
    public Image getBattleSprite()  { return battleSprite; }
    public Armor getEquippedArmor() { return equippedArmor; }

    /**
     * Retorna a resistência TOTAL do personagem:
     * resistência base + resistência da armadura equipada (se houver).
     *
     * @return Resistência total (>= 0).
     */
    public int getResistance() {
        int total = baseResistance;
        if (equippedArmor != null) {
            total += equippedArmor.getResistance();
        }
        return total;
    }

    // =========================================================================
    // SETTERS
    // =========================================================================

    public void setNivel(int level)      { this.level = level; }
    public void setCoin(int coin)        { this.coin  = coin;  }
    public void setXp(int xp)           { this.xp    = xp;    }

    /**
     * Equipa uma espada. Pode ser null para desequipar.
     *
     * @param sword Nova espada, ou null para remover.
     */
    public void setSword(Sword sword) {
        this.sword = sword;
    }

    /**
     * Equipa uma armadura. Pode ser null para desequipar.
     *
     * @param armor Nova armadura, ou null para remover.
     */
    public void setEquippedArmor(Armor armor) {
        this.equippedArmor = armor;
    }

    // =========================================================================
    // HEALTH MANAGEMENT
    // =========================================================================

    /**
     * Define o HP atual.
     * Automaticamente limitado entre 0 e maxLife.
     *
     * @param life Novo valor de HP.
     */
    public void setLife(int life) {
        this.life = Math.max(0, Math.min(life, maxLife));
    }

    /**
     * @return {@code true} se o personagem ainda está vivo (life > 0).
     */
    public boolean isAlive() {
        return this.life > 0;
    }

    /**
     * Restaura HP ao personagem.
     * Limitado automaticamente ao maxLife.
     *
     * @param amount HP a restaurar (ignorado se <= 0).
     */
    public void heal(int amount) {
        if (amount > 0) {
            setLife(this.life + amount);
        }
    }

    // =========================================================================
    // ECONOMY
    // =========================================================================

    /**
     * Adiciona moedas ao personagem.
     *
     * @param amount Quantidade a adicionar (ignorado se <= 0).
     */
    public void addCoin(int amount) {
        if (amount > 0) {
            this.coin += amount;
        }
    }

    /**
     * Remove moedas do personagem se ele tiver o suficiente.
     *
     * @param amount Quantidade a remover (deve ser > 0).
     * @return {@code true} se a remoção foi bem-sucedida;
     *         {@code false} se não tiver moedas suficientes.
     */
    public boolean removeCoin(int amount) {
        if (amount > 0 && this.coin >= amount) {
            this.coin -= amount;
            return true;
        }
        return false;
    }

    // =========================================================================
    // XP & LEVELING
    // =========================================================================

    /**
     * Concede XP ao personagem e verifica se ocorreu level-up.
     *
     * @param amount Quantidade de XP a ganhar (ignorado se <= 0).
     * @return {@code true} se o personagem subiu de nível.
     */
    public boolean earnXp(int amount) {
        if (amount <= 0) return false;
        xp += amount;
        return calculateLevel();
    }

    /**
     * Verifica se o XP acumulado é suficiente para subir de nível.
     * Pode acontecer mais de um level-up consecutivo.
     *
     * EFEITOS DO LEVEL-UP:
     *   - level++
     *   - HP restaurado ao máximo
     *   - xpNecessary *= 1.5
     *   - Nos níveis 5 e 10: inventory.increaseSpace(5)
     *
     * @return {@code true} se ocorreu pelo menos um level-up.
     */
    private boolean calculateLevel() {
        boolean leveledUp = false;

        while (xp >= xpNecessary && level < 10) {
            xp          -= xpNecessary;
            level++;
            xpNecessary  = (int)(xpNecessary * 1.5);
            life         = maxLife; // Restaura HP ao subir de nível

            // Bônus de capacidade de inventário nos marcos de nível
            if (level == 5 || level == 10) {
                inventory.increaseSpace(5);
            }

            leveledUp = true;
        }

        // No level máximo (10), trava o XP no limite para não transbordar
        if (level == 10 && xp >= xpNecessary) {
            xp = xpNecessary;
        }

        return leveledUp;
    }

    // =========================================================================
    // COMBAT
    // =========================================================================

    /**
     * Ataca um único monstro com a espada equipada.
     *
     * CÁLCULO:
     *   dano_final = max(0, sword.calculateDamage() - target.getResistance())
     *
     * @param target Monstro a atacar.
     * @return Dano causado. Retorna 0 se sem espada ou alvo inválido.
     */
    public int attack(Monsters target) {
        if (target == null || target.getLife() <= 0 || sword == null) return 0;

        int rawDamage   = sword.calculateDamage();
        int finalDamage = Math.max(0, rawDamage - target.getResistance());
        target.setLife(target.getLife() - finalDamage);
        return finalDamage;
    }

    /**
     * Ataque em área: ataca vários monstros com metade do dano base.
     * Monstros mortos durante o ataque são removidos da lista.
     *
     * @param targets Lista de monstros alvo.
     * @return Dano total causado a todos os alvos.
     */
    public int attackArea(List<Monsters> targets) {
        if (targets == null || targets.isEmpty() || sword == null) return 0;

        int baseDamage       = sword.calculateDamage() / 2;
        int totalDamageDealt = 0;

        for (int i = 0; i < targets.size(); i++) {
            Monsters target     = targets.get(i);
            int finalDamage     = Math.max(0, baseDamage - target.getResistance());
            target.setLife(target.getLife() - finalDamage);
            totalDamageDealt   += finalDamage;

            // Remove monstros mortos da lista em tempo real
            if (target.getLife() <= 0) {
                targets.remove(i);
                i--;
            }
        }

        return totalDamageDealt;
    }

    /**
     * Tenta fugir da batalha.
     * Chance de sucesso: 50% (rola d20 > 10).
     *
     * @return {@code true} se a fuga foi bem-sucedida.
     */
    public boolean leave() {
        return random.nextInt(20) + 1 > 10;
    }
}


