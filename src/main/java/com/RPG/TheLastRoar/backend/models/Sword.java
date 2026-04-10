package com.RPG.TheLastRoar.backend.models;

import java.util.Random;

/**
 * ============================================================
 * Sword.java — Arma equipável do personagem
 * ============================================================
 *
 * HERANÇA: Sword extends Item
 *
 * RESPONSABILIDADE:
 *   Representa uma arma. Cada arma tem um nome que define
 *   sua mecânica de combate única em calculateDamage().
 *
 * MECÂNICAS POR TIPO DE ARMA:
 *
 *   ┌──────────────┬─────────────────────────────────────────────────────┐
 *   │ Nome         │ Mecânica de dano                                    │
 *   ├──────────────┼─────────────────────────────────────────────────────┤
 *   │ Adaga        │ 2 ataques por turno. Cada um pode ser crítico (2x)  │
 *   │              │ com d20 = 20 (5% chance por ataque).                │
 *   ├──────────────┼─────────────────────────────────────────────────────┤
 *   │ Katana       │ 1 ataque. Crítico apenas com d20 = 20 (5%).         │
 *   │              │ Multiplicador do crítico varia por raridade:        │
 *   │              │   Comum → 2x  |  Rara → 3x  |  Lendaria → 4x      │
 *   ├──────────────┼─────────────────────────────────────────────────────┤
 *   │ Espada Longa │ 1 ataque. Limiar do crítico varia por raridade:     │
 *   │              │   Comum    → d20 >= 20 (5%)                         │
 *   │              │   Rara     → d20 >= 18 (15%)                        │
 *   │              │   Lendaria → d20 >= 15 (30%)                        │
 *   │              │ Crítico = 2x dano.                                  │
 *   ├──────────────┼─────────────────────────────────────────────────────┤
 *   │ (padrão)     │ 1 ataque. Crítico apenas com d20 = 20 → 2x dano.   │
 *   └──────────────┴─────────────────────────────────────────────────────┘
 *
 * COMO ADICIONAR NOVA ARMA:
 *   Adicione um novo case dentro de calculateDamage() com o nome exato
 *   da arma e implemente a mecânica de dano desejada.
 *
 * USADO POR:
 *   Character.java       — equipa a espada e chama calculateDamage()
 *   InventoryScreen.java — exibe o atributo "Dano: X"
 *   ShopNPC.java         — vende espadas
 */
public class Sword extends Item {

    // =========================================================================
    // FIELDS
    // =========================================================================

    /**
     * Dano base da arma.
     * É o valor mínimo garantido antes de modificadores de crítico.
     * Exemplo: damage = 5 → ao menos 5 de dano por ataque.
     */
    private int damage;

    /**
     * Tipo/raridade da arma.
     * Valores válidos: "Comum", "Rara", "Lendaria"
     * Impacta críticos na Katana e na Espada Longa.
     */
    private String type;

    /** Gerador de números aleatórios compartilhado (simulação do dado d20). */
    private static final Random random = new Random();

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    /**
     * Cria uma nova espada.
     *
     * @param name   Nome da espada  (define a mecânica em calculateDamage)
     * @param value  Custo em ouro
     * @param damage Dano base por ataque
     * @param type   Raridade: "Comum", "Rara" ou "Lendaria"
     * @param size   Slots no inventário
     */
    public Sword(String name, int value, int damage, String type, int size) {
        super(name, value, size);   // Repassa nome, valor e tamanho para Item
        this.damage = damage;
        this.type   = type;
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    /** @return Dano base desta arma. */
    public int getDamage() { return damage; }

    /** @return Raridade desta arma ("Comum", "Rara", "Lendaria"). */
    public String getType() { return type; }

    // =========================================================================
    // SETTERS (com validação)
    // =========================================================================

    /**
     * Atualiza o dano base.
     * Ignorado se {@code damage} for <= 0.
     *
     * @param damage Novo dano base (>= 1).
     */
    public void setDamage(int damage) {
        if (damage > 0) {
            this.damage = damage;
        }
    }

    /**
     * Atualiza a raridade da arma.
     * Ignorado se {@code type} for null ou em branco.
     *
     * @param type Nova raridade (ex: "Rara").
     */
    public void setType(String type) {
        if (type != null && !type.isBlank()) {
            this.type = type;
        }
    }

    // =========================================================================
    // COMBAT — DAMAGE CALCULATION
    // =========================================================================

    /**
     * Calcula o dano total de um ataque com esta arma.
     *
     * FLUXO INTERNO:
     *   1. Rola um dado virtual d20 (1–20)
     *   2. Identifica o nome da arma
     *   3. Aplica a mecânica específica daquele tipo de arma
     *   4. Retorna o dano total
     *
     * @return Dano total deste ataque (>= 0).
     */
    public int calculateDamage() {

        int totalDamage = 0;                    // Acumula o dano total
        int roll        = random.nextInt(20) + 1; // d20: valor entre 1 e 20
        String weapon   = getName();            // Nome define a mecânica

        switch (weapon) {

            // ── ADAGA ─────────────────────────────────────────────────────
            // Dois ataques independentes por turno.
            // Cada ataque pode ser crítico (d20 = 20 → 2x dano).
            case "Adaga" -> {
                for (int i = 0; i < 2; i++) {
                    int attackRoll = random.nextInt(20) + 1; // rola separado para cada ataque
                    int hit = damage;
                    if (attackRoll == 20) {
                        hit *= 2; // Crítico: dobra o dano deste ataque
                    }
                    totalDamage += hit;
                }
            }

            // ── KATANA ────────────────────────────────────────────────────
            // Um ataque. Crítico devastador cuja força depende da raridade.
            // d20 = 20 → multiplica o dano pelo fator da raridade.
            case "Katana" -> {
                int critMultiplier = switch (type) {
                    case "Rara"     -> 3; // Rara:     3x dano no crítico
                    case "Lendaria" -> 4; // Lendária: 4x dano no crítico
                    default         -> 2; // Comum:    2x dano no crítico
                };
                totalDamage = (roll == 20) ? damage * critMultiplier : damage;
            }

            // ── ESPADA LONGA ──────────────────────────────────────────────
            // Um ataque. Chance de crítico (2x dano) varia pela raridade.
            // Raridade mais alta → limiar menor → mais fácil acertar crítico.
            case "Espada Longa" -> {
                int critThreshold = switch (type) {
                    case "Rara"     -> 18; // Rara:     crítico com d20 >= 18 (15% chance)
                    case "Lendaria" -> 15; // Lendária: crítico com d20 >= 15 (30% chance)
                    default         -> 20; // Comum:    crítico apenas com d20 = 20 (5%)
                };
                totalDamage = (roll >= critThreshold) ? damage * 2 : damage;
            }

            // ── ARMAS PADRÃO ──────────────────────────────────────────────
            // Qualquer outra arma não listada acima.
            // Um ataque. Crítico apenas com d20 = 20 → 2x dano.
            default -> {
                totalDamage = (roll == 20) ? damage * 2 : damage;
            }
        }

        return totalDamage;
    }
}


