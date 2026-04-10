package com.RPG.TheLastRoar.backend.models;

/**
 * ============================================================
 * Goblin.java — Inimigo básico do Mapa 0
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Inimigo fraco, para ensinar a mecânica de batalha ao jogador.
 *
 * ATRIBUTOS:
 *   ┌───────────┬─────────────┐
 *   │ Campo     │ Valor       │
 *   ├───────────┼─────────────┤
 *   │ HP        │ 8           │
 *   │ Dano      │ 2           │
 *   │ Moedas    │ 3           │
 *   │ XP        │ 2           │
 *   │ Speed     │ 20          │
 *   │ Resistência│ 0          │
 *   └───────────┴─────────────┘
 *
 * USADO POR:
 *   EnemyManager.java — instancia 2 Goblins no Mapa 0
 *   Battle.java       — objetivo de combate
 */
public class Goblin extends Monsters {
    public Goblin() {
        super(
            "Goblin", // name
            8,        // life
            2,        // damage
            3,        // dropCoin
            2,        // dropXp
            20,       // speed
            0         // resistance (sem resistência)
        );
        this.imagePath       = "/images/sprite_goblin.png";
        this.battleImagePath = "/images/goblin.png";
    }
}
