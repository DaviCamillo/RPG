package com.RPG.TheLastRoar;

import java.util.List;
import java.util.Random;

import javafx.scene.image.Image;

/**
 * Character.java
 * Representa o personagem do jogador
 * Controla vida, XP, level, inventário, armas e resistência
 */
public class Character {

    // Informações básicas do personagem
    private String name;              // Nome do personagem
    private int life;                 // Pontos de vida atuais
    private int maxLife;              // Pontos de vida máximos
    private int coin;                 // Dinheiro do personagem
    private int resistance;           // Resistência a dano do personagem
    private Inventory inventory;      // Mochila que armazena itens
    
    // Sistema de leveling
    private int nivel = 1;            // Nível atual (1-10)
    private int xp = 0;               // Experiência atual
    private int xpNecessary = 10;     // Experiência necessária para o próximo level
    
    // Equipamento do personagem
    private Sword sword;              // Arma equipada
    
    // Utilitários
    private static final Random random = new Random();  // Para gerar números aleatórios
    private Image sprite;             // Imagem do personagem no mapa
    private Image battleSprite;       // Imagem do personagem em batalha

    /**
     * CONSTRUTOR
     */
    public Character(String name, int life, int resistance, Sword sword, Image sprite, Image battleSprite) {
        this.name = name;
        this.life = life;              
        this.maxLife = life;           
        this.resistance = resistance;  
        this.coin = 0;                 
        this.inventory = new Inventory();  
        this.sword = sword;            
        this.sprite = sprite;          
        this.battleSprite = battleSprite;  
    }

    /**
     * GETTERS 
     */
    public String getName() { return name; }              
    public int getLife() { return life; }                 
    public int getMaxLife() { return maxLife; }           
    public int getCoin() { return coin; }                 
    public int getResistance() { return resistance; }     
    public Inventory getInventory() { return inventory; } 
    public int getNivel() { return nivel; }               
    public int getXp() { return xp; }                     
    public int getXpNecessary() { return xpNecessary; }   
    public int getMaxXp() { return xpNecessary; } // ALIAS IMPORTANTE PARA A TELA DE BATALHA!
    public Sword getSword() { return sword; }             
    public Image getSprite() { return sprite; }
    public Image getBattleSprite() { return battleSprite; }

    /**
     * STATUS E CONDIÇÕES
     */
    public boolean isAlive() {
        return this.life > 0;
    }

    public void setLife(int life) {
        this.life = Math.max(0, Math.min(life, maxLife));
    }

    // Método novo para usar poções no futuro
    public void heal(int amount) {
        if (amount > 0) {
            setLife(this.life + amount); // setLife já trava a vida no maxLife automaticamente
        }
    }

    /**
     * SISTEMA ECONÔMICO
     */
    public void addCoin(int amount) {
        if (amount > 0) {  
            this.coin += amount;  
        }
    }

    // Método novo para fazer compras em lojas
    public boolean removeCoin(int amount) {
        if (amount > 0 && this.coin >= amount) {
            this.coin -= amount;
            return true; // Compra efetuada com sucesso
        }
        return false; // Sem dinheiro suficiente
    }

    /**
     * EQUIPAMENTOS
     */
    public void setSword(Sword sword) {
        if (sword != null) {  
            this.sword = sword;  
        }
    }

/**
     * SISTEMA DE XP E LEVEL
     * @return true se o jogador subiu de nível, false caso contrário.
     */
    public boolean earnXp(int quantidade) {
        if (quantidade <= 0) return false;  

        xp += quantidade;  
        return calculateLevel(); // Retorna o resultado do cálculo
    }
private boolean calculateLevel() {
        boolean subiuDeNivel = false;

        while (xp >= xpNecessary && nivel < 10) {
            xp -= xpNecessary;      
            nivel++;                
            
            // NOVO: Curva exponencial. Cada nível exige 50% a mais de XP que o anterior.
            // Ex: Nvl 2 = 10xp, Nvl 3 = 15xp, Nvl 4 = 22xp, Nvl 5 = 33xp...
            xpNecessary = (int)(xpNecessary * 1.5); 

            // BUFFS DE LEVEL UP         
            life = maxLife;         // Aumenta a vida maxima

            if (nivel == 5 || nivel == 10) {
                inventory.increaseSpace(5);  
            }
            
            subiuDeNivel = true; // Marca que o level up aconteceu
        }

        // Trava o XP se já estiver no nível máximo
        if (nivel == 10 && xp >= xpNecessary) {
            xp = xpNecessary;  
        }
        
        return subiuDeNivel;
    }

    /**
     * COMBATE
     * Agora retorna o dano causado para podermos mostrar na tela de UI da Batalha depois!
     */
    public int attack(Monsters alvo) {
        if (alvo == null || alvo.getLife() <= 0) return 0;

        int danoFinal = Math.max(0, sword.calculateDamage() - alvo.getResistance());
        alvo.setLife(alvo.getLife() - danoFinal);
        
        return danoFinal; // Retorna o dano para a UI
    }

    public int attackArea(List<Monsters> alvos) {
        if (alvos == null || alvos.isEmpty()) return 0;

        int danoBase = sword.calculateDamage() / 2;
        int totalDanoCausado = 0;

        for (int i = 0; i < alvos.size(); i++) {
            Monsters alvo = alvos.get(i);
            int danoFinal = Math.max(0, danoBase - alvo.getResistance());
            
            alvo.setLife(alvo.getLife() - danoFinal);
            totalDanoCausado += danoFinal;

            if (alvo.getLife() <= 0) {
                alvos.remove(i);
                i--;
            }
        }
        
        return totalDanoCausado;
    }

    // TENTAR FUGIR
    public boolean leave() {
        int dado = random.nextInt(20) + 1;
        return dado > 10; // Reduzi a dificuldade de fugir de 18 (10% de chance) para 10 (50% de chance), para não ser frustrante.
    }
}