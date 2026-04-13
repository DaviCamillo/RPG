package com.RPG.TheLastRoar.backend.core;

/**
 * ============================================================
 * GameConstants.java — Constantes centralizadas do jogo
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Centraliza todas as constantes do jogo para evitar números
 *   mágicos espalhados pelo código e facilitar manutenção.
 *
 * BENEFÍCIOS:
 *   - Fácil ajuste de balanceamento (velocidade, dano, etc.)
 *   - Evita duplicação de valores
 *   - Melhora legibilidade do código
 *
 * USADO POR:
 *   AppGameLoop, EnemyManager, Battle, Character, etc.
 */
public final class GameConstants {

    // =========================================================================
    // Display & Screen
    // =========================================================================

    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;

    // =========================================================================
    // Player Movement & Animation
    // =========================================================================

    /** Velocidade de movimento do jogador (pixels por frame). */
    public static final double PLAYER_MOVE_SPEED = 4.0;

    /** Tamanho de exibição do sprite do jogador no mapa. */
    public static final double PLAYER_DISPLAY_SIZE = 80.0;

    /** Tamanho de cada frame do sprite sheet do jogador. */
    public static final int PLAYER_SPRITE_SIZE = 128;

    /** Delay entre frames de animação (nanosegundos). */
    public static final long ANIMATION_FRAME_DELAY = 200_000_000L;

    // =========================================================================
    // Enemy Animation & Movement
    // =========================================================================

    public static final int ENEMY_SPRITE_BASE_SIZE = 128;
    public static final double ENEMY_DISPLAY_SIZE_SMALL = 80.0;
    public static final double ENEMY_DISPLAY_SIZE_BOSS = 256.0;
    public static final int ENEMY_BOSS_SPRITE_SIZE = 256;

    // =========================================================================
    // Collision Detection
    // =========================================================================

    /** Margem de borda da tela para detecção de mudança de mapa. */
    public static final double SCREEN_EDGE_MARGIN = 5.0;

    /** Fator de raio de colisão (% do tamanho do display). */
    public static final double COLLISION_RADIUS_FACTOR = 0.4;

    // =========================================================================
    // UI & Controls
    // =========================================================================

    /** Duração de transições de fade (milissegundos). */
    public static final int FADE_TRANSITION_DURATION = 500;

    /** Duração de pause entre turnos em batalha (milissegundos). */
    public static final int BATTLE_PAUSE_DURATION = 1000;

    // =========================================================================
    // Game Maps
    // =========================================================================

    public static final String MAP_INTRO = "mapa_padrao.png";
    public static final String MAP_INTERMEDIATE = "mapa_padrao2.png";
    public static final String MAP_BOSS = "mapa_padrao3.png";

    // =========================================================================
    // Map Directions
    // =========================================================================

    public static final String DIRECTION_UP = "CIMA";
    public static final String DIRECTION_DOWN = "BAIXO";
    public static final String DIRECTION_LEFT = "ESQUERDA";
    public static final String DIRECTION_RIGHT = "DIREITA";

    // =========================================================================
    // Player Initial Stats
    // =========================================================================

    public static final int PLAYER_INITIAL_HP = 20;
    public static final int PLAYER_INITIAL_COINS = 0;
    public static final int PLAYER_INITIAL_LEVEL = 1;
    public static final int PLAYER_INITIAL_XP = 0;
    public static final int PLAYER_INITIAL_XP_REQUIRED = 10;
    public static final int PLAYER_BASE_RESISTANCE = 0;

    // =========================================================================
    // Character Level Up
    // =========================================================================

    /** Multiplicador de XP necessário por nível. */
    public static final double XP_MULTIPLIER = 1.5;

    /** Níveis que ganham slots de inventário (+5 slots). */
    public static final int[] INVENTORY_EXPANSION_LEVELS = { 5, 10 };

    /** Aumento de slots por expansão. */
    public static final int INVENTORY_EXPANSION_SIZE = 5;

    // =========================================================================
    // Battle System
    // =========================================================================

    /** Taxa de sucesso de fuga do combate. */
    public static final double ESCAPE_SUCCESS_RATE = 0.5;

    // =========================================================================
    // File Paths
    // =========================================================================

    public static final String SAVE_SLOT_1 = "save1.json";
    public static final String SAVE_SLOT_2 = "save2.json";
    public static final String SAVE_SLOT_3 = "save3.json";
    public static final String IMAGE_LOGO_PATH = "/images/logo.png";

    // =========================================================================
    // Private Constructor
    // =========================================================================

    /**
     * Construtor privado para evitar instanciação.
     * GameConstants deve ser usado apenas com membros estáticos.
     */
    private GameConstants() {
        throw new UnsupportedOperationException(
            "GameConstants não deve ser instanciada."
        );
    }
}
