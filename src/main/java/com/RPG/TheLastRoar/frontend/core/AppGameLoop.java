package com.RPG.TheLastRoar.frontend.core;

import com.RPG.TheLastRoar.App;
import com.RPG.TheLastRoar.backend.models.Monsters;
import com.RPG.TheLastRoar.frontend.screens.Battle;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * AppGameLoop — Gerencia os AnimationTimers e loops de jogo.
 *
 * RESPONSABILIDADE:
 *   - Criar e gerenciar playerMovementTimer (movimento + sprite)
 *   - Criar e gerenciar enemyAITimer (IA + colisão)
 *   - Controlar animações de inimigos
 *   - Detectar colisões com inimigos e iniciar batalhas
 */
public class AppGameLoop {

    private App app;
    private double screenW;
    private double screenH;

    // Animação do sprite do jogador
    private int  spriteDirection  = 0;
    private int  animFrame        = 0;
    private long lastFrameTime    = 0;

    // Animação dos inimigos
    private int  enemyAnimFrame       = 0;
    private long lastEnemyFrameTime   = 0;

    private static final long   FRAME_DELAY         = 200_000_000L;
    private static final double MOVE_SPEED          = 4;
    private static final int    SPRITE_W            = 128;
    private static final int    SPRITE_H            = 128;
    private static final double PLAYER_DISPLAY_SIZE = 80;

    public AppGameLoop(App app) {
        this.app = app;
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        this.screenW = screenBounds.getWidth();
        this.screenH = screenBounds.getHeight();
    }

    /**
     * Cria e inicia os dois AnimationTimers principais do jogo.
     */
    public void startGameTimers(Stage stage, javafx.scene.Scene scene) {
        app.playerMovement = createPlayerMovementTimer();
        app.playerMovement.start();

        app.enemyAI = createEnemyAITimer(stage, scene);
        app.enemyAI.start();
    }

    /**
     * Cria o AnimationTimer responsável por:
     *   - Mover o personagem conforme as teclas pressionadas
     *   - Atualizar a animação do sprite (direção + frame)
     *   - Detectar bordas da tela → trocar de mapa
     *   - Verificar colisão com o NPC da loja
     */
    private AnimationTimer createPlayerMovementTimer() {
        return new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (App.keyUp) {
                    if (app.playerView.getY() <= 5) {
                        app.changeMap("CIMA");
                    } else {
                        app.playerView.setY(app.playerView.getY() - MOVE_SPEED);
                    }
                    updatePlayerSprite(app.playerView, 3, now);

                } else if (App.keyDown) {
                    if (app.playerView.getY() >= screenH - PLAYER_DISPLAY_SIZE - 5) {
                        app.changeMap("BAIXO");
                    } else {
                        app.playerView.setY(app.playerView.getY() + MOVE_SPEED);
                    }
                    updatePlayerSprite(app.playerView, 0, now);

                } else if (App.keyLeft) {
                    if (app.playerView.getX() <= 5) {
                        app.changeMap("ESQUERDA");
                    } else {
                        app.playerView.setX(app.playerView.getX() - MOVE_SPEED);
                    }
                    updatePlayerSprite(app.playerView, 1, now);

                } else if (App.keyRight) {
                    if (app.playerView.getX() >= screenW - PLAYER_DISPLAY_SIZE - 5) {
                        app.changeMap("DIREITA");
                    } else {
                        app.playerView.setX(app.playerView.getX() + MOVE_SPEED);
                    }
                    updatePlayerSprite(app.playerView, 2, now);
                }

                app.checkNpcCollision();
            }
        };
    }

    /**
     * Cria o AnimationTimer responsável por:
     *   - Avançar o frame de animação dos inimigos
     *   - Atualizar posição e sprite de cada inimigo
     *   - Detectar colisão com o jogador
     *   - Iniciar batalha se colidiu
     */
    private AnimationTimer createEnemyAITimer(Stage stage, javafx.scene.Scene scene) {
        return new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastEnemyFrameTime > FRAME_DELAY) {
                    enemyAnimFrame     = (enemyAnimFrame + 1) % 4;
                    lastEnemyFrameTime = now;
                }

                int collision = app.enemyManager.update(
                    app.playerView.getX(), app.playerView.getY(), enemyAnimFrame
                );

                if (collision != -1) {
                    app.playerMovement.stop();
                    app.enemyAI.stop();
                    app.btnInventory.setVisible(false);

                    double dx = app.playerView.getX() - app.enemyManager.getView(collision).getX();
                    app.playerView.setX(app.playerView.getX() + (dx > 0 ? 40 : -40));

                    app.battleEnemyIndex = collision;

                    Monsters  monster   = app.enemyManager.getMonstro(collision);
                    ImageView enemyView = app.enemyManager.getView(collision);

                    FadeTransition ft = new FadeTransition(Duration.millis(500), app.mainLayout);
                    ft.setFromValue(1);
                    ft.setToValue(0);
                    ft.setOnFinished(e -> {
                        Battle.startBattle(
                            stage, scene, app.player, monster,
                            app.playerView, enemyView,
                            app, app.mainLayout, app.hudManager
                        );
                        app.btnInventory.setVisible(true);
                    });
                    ft.play();
                }
            }
        };
    }

    /**
     * Atualiza o viewport da sprite sheet do jogador.
     */
    private void updatePlayerSprite(ImageView view, int newDirection, long now) {
        if (spriteDirection != newDirection) {
            spriteDirection = newDirection;
            animFrame       = 0;
            view.setViewport(new Rectangle2D(0, spriteDirection * SPRITE_H, SPRITE_W, SPRITE_H));
        }

        if (now - lastFrameTime > FRAME_DELAY) {
            animFrame     = (animFrame + 1) % 4;
            view.setViewport(new Rectangle2D(
                animFrame * SPRITE_W,
                spriteDirection * SPRITE_H,
                SPRITE_W, SPRITE_H
            ));
            lastFrameTime = now;
        }
    }
}





