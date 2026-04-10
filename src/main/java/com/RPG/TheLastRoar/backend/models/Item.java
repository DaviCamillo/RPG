package com.RPG.TheLastRoar.backend.models;

/**
 * ============================================================
 * Item.java — Classe base para TODOS os itens do jogo
 * ============================================================
 *
 * HIERARQUIA DE HERANÇA:
 *
 *   Item
 *    ├── Sword   (arma: causa dano)
 *    ├── Armor   (equipamento: fornece resistência)
 *    └── Potion  (consumível: restaura HP)
 *
 * RESPONSABILIDADE:
 *   Define as três propriedades compartilhadas por todo item:
 *
 *   ┌──────────┬──────────────────────────────────────────────┐
 *   │ Campo    │ Descrição                                    │
 *   ├──────────┼──────────────────────────────────────────────┤
 *   │ name     │ Nome exibido no inventário e na loja         │
 *   │ value    │ Custo em ouro (usado pelo ShopNPC)           │
 *   │ size     │ Slots que ocupa no Inventory                 │
 *   └──────────┴──────────────────────────────────────────────┘
 *
 *   Todos os setters validam o input para evitar estados inválidos.
 *
 * USADO POR:
 *   Inventory.java    — armazena itens
 *   ShopNPC.java      — vende itens
 *   InventoryScreen   — renderiza os cards de itens
 *   Battle.java       — filtra poções do inventário
 */
public class Item {

    // =========================================================================
    // FIELDS
    // =========================================================================

    /**
     * Nome de exibição mostrado na UI.
     * Exemplos: "Espada de Madeira", "Poção Pequena".
     */
    private String name;

    /**
     * Custo em ouro do item.
     * Igual a 0 para itens que não podem ser comprados/vendidos.
     * Nunca negativo.
     */
    private int value;

    /**
     * Slots do inventário que este item ocupa.
     * Sempre >= 1. Itens maiores (armaduras pesadas) podem ocupar 2-3 slots.
     */
    private int size;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    /**
     * Cria um novo item com as propriedades fornecidas.
     *
     * @param name  Nome de exibição  (ex: "Espada de Ferro")
     * @param value Custo em ouro     (>= 0)
     * @param size  Tamanho em slots  (>= 1)
     */
    public Item(String name, int value, int size) {
        this.name  = name;
        this.value = value;
        this.size  = size;
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    /** @return Nome de exibição deste item. */
    public String getName()  { return name; }

    /** @return Custo em ouro deste item (>= 0). */
    public int getValue()    { return value; }

    /** @return Quantidade de slots que este item ocupa (>= 1). */
    public int getSize()     { return size; }

    // =========================================================================
    // SETTERS (com validação)
    // =========================================================================

    /**
     * Atualiza o nome de exibição.
     * Ignorado se {@code name} for null ou em branco.
     *
     * @param name Novo nome de exibição.
     */
    public void setName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    /**
     * Atualiza o valor em ouro.
     * Ignorado se {@code value} for negativo.
     *
     * @param value Novo custo em ouro (>= 0).
     */
    public void setValue(int value) {
        if (value >= 0) {
            this.value = value;
        }
    }

    /**
     * Atualiza o tamanho em slots do inventário.
     * Ignorado se {@code size} for menor que 1.
     *
     * @param size Novo tamanho em slots (>= 1).
     */
    public void setSize(int size) {
        if (size > 0) {
            this.size = size;
        }
    }
}


