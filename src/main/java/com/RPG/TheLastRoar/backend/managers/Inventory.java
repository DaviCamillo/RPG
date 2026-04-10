package com.RPG.TheLastRoar.backend.managers;

import java.util.ArrayList;
import java.util.List;

import com.RPG.TheLastRoar.backend.models.Item;

/**
 * ============================================================
 * Inventory.java — Mochila do personagem
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Gerencia a coleção de itens carregados pelo personagem.
 *   Controla capacidade máxima de slots e evita overflow.
 *
 * MODELO DE CAPACIDADE:
 *   ┌─────────────────────────────────────────────────────────┐
 *   │  maxSpace = 20 slots (padrão)                           │
 *   │  usedSpace = soma dos item.getSize() de todos os itens  │
 *   │  freeSpace = maxSpace - usedSpace                       │
 *   └─────────────────────────────────────────────────────────┘
 *
 * EXPANSÃO DE CAPACIDADE:
 *   - Level 5:  +5 slots (Character.calculateLevel → increaseSpace(5))
 *   - Level 10: +5 slots (Character.calculateLevel → increaseSpace(5))
 *
 * USADO POR:
 *   Character.java       — carrega o inventário do personagem
 *   InventoryScreen.java — exibe os itens ao jogador
 *   Battle.java          — filtra poções para uso em batalha
 *   App.java             — uso rápido de poção com tecla H
 *   ShopNPC.java         — adiciona itens comprados
 */
public class Inventory {

    // =========================================================================
    // FIELDS
    // =========================================================================

    /** Número máximo de slots disponíveis. Começa em 20 e pode aumentar. */
    private int maxSpace = 20;

    /** Slots atualmente ocupados pelos itens na mochila. */
    private int usedSpace = 0;

    /** Lista de itens armazenados na mochila. */
    private List<Item> items = new ArrayList<>();

    // =========================================================================
    // ITEM MANAGEMENT
    // =========================================================================

    /**
     * Tenta adicionar um item à mochila.
     *
     * FLUXO:
     *   1. Valida que o item não é null
     *   2. Verifica se há slots suficientes (usedSpace + item.size <= maxSpace)
     *   3. Adiciona o item e atualiza usedSpace
     *
     * @param item Item a adicionar (não pode ser null).
     * @return {@code true} se adicionado com sucesso;
     *         {@code false} se o item é null ou não há espaço.
     */
    public boolean addItem(Item item) {
        if (item == null) return false;

        if (usedSpace + item.getSize() <= maxSpace) {
            items.add(item);
            usedSpace += item.getSize();
            return true;
        }

        return false; // Inventário cheio
    }

    /**
     * Remove um item da mochila e libera os slots ocupados.
     *
     * @param item Item a remover (deve estar presente na mochila).
     * @return {@code true} se removido com sucesso;
     *         {@code false} se o item não estava na mochila.
     */
    public boolean removeItem(Item item) {
        if (items.remove(item)) {
            usedSpace -= item.getSize();
            return true;
        }
        return false;
    }

    // =========================================================================
    // CAPACITY MANAGEMENT
    // =========================================================================

    /**
     * Aumenta o número máximo de slots do inventário.
     * Chamado ao subir de nível (levels 5 e 10).
     *
     * @param amount Quantidade de slots a adicionar (deve ser > 0).
     */
    public void increaseSpace(int amount) {
        if (amount > 0) {
            maxSpace += amount;
        }
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    /** @return Capacidade máxima total em slots. */
    public int getMaxSpace() { return maxSpace; }

    /** @return Slots atualmente ocupados pelos itens. */
    public int getUsedSpace() { return usedSpace; }

    /** @return Slots livres disponíveis para novos itens. */
    public int getFreeSpace() { return maxSpace - usedSpace; }

    /** @return Lista imutável com todos os itens na mochila. */
    public List<Item> getItems() { return items; }
}


