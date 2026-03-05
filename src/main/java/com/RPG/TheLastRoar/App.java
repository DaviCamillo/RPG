package com.RPG.TheLastRoar;

public class App {

    public static void main(String[] args) {

        // ESPADAS
        Sword adagaComum = new Sword("Adaga Comum", 0, 3, "Comum", 2);
        Sword adagaRara = new Sword("Adaga Rara", 0, 5, "Rara", 2);
        Sword adagaLendaria = new Sword("Adaga Lendaria", 0, 7, "Lendaria", 2);

        Sword katanaComum = new Sword("Katana Comum", 0, 5, "Comum", 5);
        Sword katanaRara = new Sword("Katana Rara", 0, 7, "Rara", 5);
        Sword katanaLendaria = new Sword("Katana Lendaria", 0, 10, "Lendaria", 5);

        Sword espadaComum = new Sword("Espada Longa Comum", 0, 7, "Comum", 7);
        Sword espadaRara = new Sword("Espada Longa Rara", 0, 10, "Rara", 7);
        Sword espadaLendaria = new Sword("Espada Longa Lendaria", 0, 12, "Lendaria", 7);

        Sword espadaVelha = new Sword("Espada Velha", 0, 5, "Comum", 4);

        // ARMADURAS
        Armor armaduraLeve = new Armor("Armadura Leve", 25, 2, 2);
        Armor armaduraMedia = new Armor("Armadura Media", 50, 4, 3);
        Armor armaduraPesada = new Armor("Armadura Pesada", 80, 8, 5);

        // POÇÃO
        Potion pocaoVida = new Potion("Poção de Vida", 5, 2, 20);


    }
}