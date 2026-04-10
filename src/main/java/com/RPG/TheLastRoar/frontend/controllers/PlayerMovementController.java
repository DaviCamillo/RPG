package com.RPG.TheLastRoar.frontend.controllers;

import javafx.animation.AnimationTimer;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;

/**
 * ============================================================
 * PlayerMovementController.java — Movimento e Animação do Player
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Gerencia TODA a lógica de movimento do personagem no mapa:
 *   - Mover sprite conforme teclas (W/S/A/D)
 *   - Atualizar animação do sprite sheet (direção + frame)
 *   - Detectar chegada nas bordas da tela (chamar changeMap)
 *   - Verificar colisão com NPC (chamar checkNpcCollision)
 *
 * SPRITE SHEET:
 *   4 filas (linhas):  0=baixo, 1=esquerda, 2=direita, 3=cima
 *   4 colunas (frames): 0-3 de animação de movimento
 *   Cada frame = 128x128 pixels
 *
 * ALGORITMO:
 *   1. Lê flags de tecla (InputController.key*)
 *   2. Se mudou direção: reinicia frame 0, atualiza viewport
 *   3. Se tempo > FRAME_DELAY: avança frame, atualiza viewport
 *   4. Verifica colisão com bordas/NPC
 *
 * USADO POR: App.java (startGameTimers)
 * DEPENDENCIES: InputController, EnemyManager, ShopNPC, MapTransitionManager
 *
 * @author RPG Team
 */
public class PlayerMovementController {

    // =========================================================================
    // CONSTANTS
    // =========================================================================

    /** Intervalo de tempo entre frames de animação (200ms em nanosegundos). */
    private static final long FRAME_DELAY = 200_000_000L;

    /** Velocidade de movimento do personagem em pixels por frame. */
    public static final double MOVE_SPEED = 4.0;

    /** Dimensões de cada frame na sprite sheet. */
    public static final int SPRITE_W = 128;
    public static final int SPRITE_H = 128;

    /** Tamanho do sprite exibido na tela (redimensionado para visualização). */
    public static final double PLAYER_DISPLAY_SIZE = 80.0;

    // =========================================================================
    // FIELDS — Estado da animação
    // =========================================================================

    /**
     * Direção atual do sprite:
     *   0 = baixo,  1 = esquerda,  2 = direita,  3 = cima
     */
    private int spriteDirection = 0;

    /** Frame atual de animação (0-3). */
    private int animFrame = 0;

    /** Timestamp da última atualização de frame (nanosegundos). */
    private long lastFrameTime = 0;

    // =========================================================================
    // FIELDS — Referências e dimensões
    // =========================================================================

    private ImageView playerView;
    private double screenW;
    private double screenH;

    // =========================================================================
    // FIELDS — Callbacks
    // =========================================================================

    private Runnable onCheckNpcCollision;
    private Runnable onCheckMapBounds;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    /**
     * Cria o controlador de movimento do player.
     *
     * @param playerView                ImageView do sprite do player
     * @param screenW                   Largura da tela
     * @param screenH                   Altura da tela
     * @param onCheckNpcCollision       Callback ao detectar possível colisão NPC
     * @param onCheckMapBounds          Callback ao verificar bordas do mapa
     */
    public PlayerMovementController(ImageView playerView, double screenW, double screenH,
                                   Runnable onCheckNpcCollision,
                                   Runnable onCheckMapBounds) {
        this.playerView = playerView;
        this.screenW = screenW;
        this.screenH = screenH;
        this.onCheckNpcCollision = onCheckNpcCollision;
        this.onCheckMapBounds = onCheckMapBounds;
    }

    // =========================================================================
    // PUBLIC METHODS
    // =========================================================================

    /**
     * Cria o AnimationTimer responsável por:
     *   - Mover o personagem conforme as teclas pressionadas
     *   - Atualizar a animação do sprite (direção + frame)
     *   - Detectar bordas da tela → trocar de mapa
     *   - Verificar colisão com o NPC da loja
     *
     * @return AnimationTimer pronto para iniciar
     */
    public AnimationTimer createMovementTimer() {
        return new AnimationTimer() {
            @Override
            public void handle(long now) {
                // ── Lê flags de movimento e move o sprite ─────────────────
                if (InputController.keyUp) {
                    if (playerView.getY() <= 5) {
                        onCheckMapBounds.run();  // Detecta borda superior
                    } else {
                        playerView.setY(playerView.getY() - MOVE_SPEED);
                    }
                    updatePlayerSprite(3, now); // 3 = direção cima

                } else if (InputController.keyDown) {
                    if (playerView.getY() >= screenH - PLAYER_DISPLAY_SIZE - 5) {
                        onCheckMapBounds.run();  // Detecta borda inferior
                    } else {
                        playerView.setY(playerView.getY() + MOVE_SPEED);
                    }
                    updatePlayerSprite(0, now); // 0 = direção baixo

                } else if (InputController.keyLeft) {
                    if (playerView.getX() <= 5) {
                        onCheckMapBounds.run();  // Detecta borda esquerda
                    } else {
                        playerView.setX(playerView.getX() - MOVE_SPEED);
                    }
                    updatePlayerSprite(1, now); // 1 = direção esquerda

                } else if (InputController.keyRight) {
                    if (playerView.getX() >= screenW - PLAYER_DISPLAY_SIZE - 5) {
                        onCheckMapBounds.run();  // Detecta borda direita
                    } else {
                        playerView.setX(playerView.getX() + MOVE_SPEED);
                    }
                    updatePlayerSprite(2, now); // 2 = direção direita
                }

                // ── Verifica colisão com NPC ──────────────────────────────
                if (onCheckNpcCollision != null) {
                    onCheckNpcCollision.run();
                }
            }
        };
    }

    /**
     * Reset de animação (chamado ao pausar, abrir menus, etc).
     */
    public void resetAnimation() {
        lastFrameTime = System.nanoTime();
    }

    // =========================================================================
    // PRIVATE METHODS — Animação
    // =========================================================================

    /**
     * Atualiza o viewport da sprite sheet do jogador.
     * Muda a linha (direção) e avança o frame a cada FRAME_DELAY nanosegundos.
     *
     * @param newDirection Nova direção: 0=baixo, 1=esq, 2=dir, 3=cima
     * @param now          Timestamp atual em nanosegundos
     */
    private void updatePlayerSprite(int newDirection, long now) {
        // Muda de direção: reinicia o frame para evitar corte visual
        if (spriteDirection != newDirection) {
            spriteDirection = newDirection;
            animFrame = 0;
            playerView.setViewport(new Rectangle2D(0, spriteDirection * SPRITE_H,
                                                   SPRITE_W, SPRITE_H));
        }

        // Avança o frame de animação em intervalos regulares
        if (now - lastFrameTime > FRAME_DELAY) {
            animFrame = (animFrame + 1) % 4;
            playerView.setViewport(new Rectangle2D(
                animFrame * SPRITE_W,
                spriteDirection * SPRITE_H,
                SPRITE_W, SPRITE_H
            ));
            lastFrameTime = now;
        }
    }
}


