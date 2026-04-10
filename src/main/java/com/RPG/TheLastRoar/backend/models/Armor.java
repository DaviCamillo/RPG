package com.RPG.TheLastRoar.backend.models;

/**
 * ============================================================
 * Armor.java — Armadura equipável do personagem
 * ============================================================
 *
 * HERANÇA: Armor extends Item
 *
 * RESPONSABILIDADE:
 *   Representa uma armadura que, ao ser equipada, adiciona
 *   pontos de resistência ao personagem.
 *
 *   A resistência reduz o dano recebido nas batalhas:
 *     dano_final = max(0, dano_inimigo - personagem.getResistance())
 *
 *   Onde getResistance() em Character.java retorna:
 *     resistência_base + armadura_equipada.getResistance()
 *
 * FLUXO DE EQUIPAMENTO:
 *   1. Jogador abre o inventário (InventoryScreen)
 *   2. Clica em uma Armor
 *   3. Character.setEquippedArmor(armor) é chamado
 *   4. Character.getResistance() passa a incluir a resistência da armadura
 *   5. HudManager.atualizar() reflete o novo valor no HUD
 *
 * USADO POR:
 *   Character.java       — equipa/desequipa via setEquippedArmor()
 *   InventoryScreen.java — exibe "Defesa: X" no card do item
 *   ShopNPC.java         — pode vender armaduras (se adicionado ao catálogo)
 */
public class Armor extends Item {

    // =========================================================================
    // FIELDS
    // =========================================================================

    /**
     * Pontos de resistência que esta armadura fornece quando equipada.
     * Reduz o dano recebido em batalha.
     * Nunca negativo.
     */
    private int resistance;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    /**
     * Cria uma nova armadura.
     *
     * @param name       Nome de exibição   (ex: "Cota de Malha")
     * @param value      Custo em ouro      (>= 0)
     * @param size       Slots no inventário (>= 1)
     * @param resistance Pontos de resistência fornecidos (>= 0)
     */
    public Armor(String name, int value, int size, int resistance) {
        super(name, value, size);   // Repassa nome, valor e tamanho para Item
        this.resistance = resistance;
    }

    // =========================================================================
    // GETTER
    // =========================================================================

    /**
     * @return Pontos de resistência fornecidos por esta armadura (>= 0).
     */
    public int getResistance() {
        return resistance;
    }

    // =========================================================================
    // SETTER (com validação)
    // =========================================================================

    /**
     * Atualiza os pontos de resistência da armadura.
     * Ignorado se {@code resistance} for negativo.
     *
     * @param resistance Novo valor de resistência (>= 0).
     */
    public void setResistance(int resistance) {
        if (resistance >= 0) {
            this.resistance = resistance;
        }
    }
}


