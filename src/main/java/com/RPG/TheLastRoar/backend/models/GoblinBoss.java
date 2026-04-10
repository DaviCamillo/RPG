package com.RPG.TheLastRoar.backend.models;

/**
 * ============================================================
 * GoblinBoss.java — Boss do Mapa 2
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Boss final com stats balanceados — alto HP, dano moderado, resistência elevada.
 *   Usa sprite grande (256x256) no mapa para destacar importância.
 *
 * ATRIBUTOS:
 *   ┌───────────┬─────────────┐
 *   │ Campo     │ Valor       │
 *   ├───────────┼─────────────┤
 *   │ HP        │ 25          │
 *   │ Dano      │ 7           │
 *   │ Moedas    │ 15          │
 *   │ XP        │ 20          │
 *   │ Speed     │ 10          │
 *   │ Resistência│ 3          │
 *   └───────────┴─────────────┘
 *
 * DESIFIO:
 *   Inimigo final que requer estratégia e preparação.
 *   A resistência elevada torna ataques menos efetivos.
 *
 * USADO POR:
 *   EnemyManager.java — instancia 1 GoblinBoss no Mapa 2
 *   Battle.java       — objetivo de combate
 */
public class GoblinBoss extends Monsters {
    public GoblinBoss() {
        super(
            "GoblinBoss", // name
            25,           // life
            7,            // damage
            15,           // dropCoin
            20,           // dropXp
            10,           // speed
            3             // resistance
        );
        this.imagePath       = "/images/sprite_Baginga.png";
        this.battleImagePath = "/images/goblin.png";
    }
}
