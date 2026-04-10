package com.RPG.TheLastRoar.backend.models;

/**
 * ============================================================
 * GoblinExp.java — Inimigo intermediário do Mapa 1
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Inimigo punitivo com dano elevadíssimo (99) para fase intermediária.
 *   Representa um desafio significativo ao jogador.
 *
 * ATRIBUTOS:
 *   ┌───────────┬─────────────┐
 *   │ Campo     │ Valor       │
 *   ├───────────┼─────────────┤
 *   │ HP        │ 13          │
 *   │ Dano      │ 99 (ALTO!)  │
 *   │ Moedas    │ 5           │
 *   │ XP        │ 4           │
 *   │ Speed     │ 20          │
 *   │ Resistência│ 1          │
 *   └───────────┴─────────────┘
 *
 * ⚠️ AVISO: Dano muito elevado de propósito — cuidado na batalha!
 *
 * USADO POR:
 *   EnemyManager.java — instancia 2 GoblinExp no Mapa 1
 *   Battle.java       — objetivo de combate
 */
public class GoblinExp extends Monsters {
    public GoblinExp() {
        super(
            "GoblinExp", // name
            13,          // life
            99,          // damage (alto de propósito — cuidado!)
            5,           // dropCoin
            4,           // dropXp
            20,          // speed
            1            // resistance
        );
        this.imagePath       = "/images/sprite_goblinexperiente.png";
        this.battleImagePath = "/images/goblin.png";
    }
}
