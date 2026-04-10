package com.RPG.TheLastRoar.backend.models;

/**
 * ============================================================
 * Potion.java — Item consumível de cura
 * ============================================================
 *
 * HERANÇA: Potion extends Item
 *
 * RESPONSABILIDADE:
 *   Representa uma poção que restaura pontos de vida (HP)
 *   ao ser consumida pelo personagem.
 *
 * FLUXO DE USO (no mapa):
 *   1. Jogador pressiona H (App.java → usarPocaoNoMapa)
 *   2. Primeira poção do inventário é encontrada
 *   3. character.heal(potion.getHealedLife()) é chamado
 *   4. Poção é removida do inventário
 *   5. HUD é atualizado com o novo HP
 *
 * FLUXO DE USO (em batalha):
 *   1. Jogador abre a Mochila (Battle.java)
 *   2. Lista de poções é exibida
 *   3. Ao clicar: heal aplicado, poção removida, turno passa para inimigo
 *
 * NOTA SOBRE CURA EXCESSIVA:
 *   O método Character.heal() limita a cura ao HP máximo.
 *   A poção não precisa se preocupar com overflow.
 *
 * USADO POR:
 *   Character.java       — consome a poção via heal()
 *   Battle.java          — lista poções na mochila de batalha
 *   App.java             — uso rápido no mapa com tecla H
 *   ShopNPC.java         — vende poções
 *   InventoryScreen.java — exibe "Cura: X" no card do item
 */
public class Potion extends Item {

    // =========================================================================
    // FIELDS
    // =========================================================================

    /**
     * Quantidade de HP restaurada ao usar esta poção.
     * Sempre > 0. O personagem pode curar no máximo até seu HP máximo.
     */
    private int healedLife;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    /**
     * Cria uma nova poção de cura.
     *
     * @param name       Nome de exibição     (ex: "Poção Pequena")
     * @param value      Custo em ouro        (>= 0)
     * @param size       Slots no inventário  (>= 1)
     * @param healedLife HP restaurado ao usar (>= 1)
     */
    public Potion(String name, int value, int size, int healedLife) {
        super(name, value, size);   // Repassa nome, valor e tamanho para Item
        this.healedLife = healedLife;
    }

    // =========================================================================
    // GETTER
    // =========================================================================

    /**
     * @return Quantidade de HP que esta poção restaura ao ser usada.
     */
    public int getHealedLife() {
        return healedLife;
    }

    // =========================================================================
    // SETTER (com validação)
    // =========================================================================

    /**
     * Atualiza a quantidade de HP curado.
     * Ignorado se {@code healedLife} for <= 0 (uma poção deve sempre curar algo).
     *
     * @param healedLife Novo valor de cura (>= 1).
     */
    public void setHealedLife(int healedLife) {
        if (healedLife > 0) {
            this.healedLife = healedLife;
        }
    }
}


