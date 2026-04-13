package com.RPG.TheLastRoar.backend.core;

import com.RPG.TheLastRoar.App;
import com.RPG.TheLastRoar.backend.models.Monsters;
import com.RPG.TheLastRoar.frontend.screens.Battle;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

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

    private final App app;
    private final double screenWidth;
    private final double screenHeight;

    // OTIMIZAÇÃO: Cache de limites de borda para evitar recálculos por frame
    private final double topBoundary;
    private final double bottomBoundary;
    private final double leftBoundary;
    private final double rightBoundary;

    // Animação do sprite do jogador (movimento)
    private int playerSpriteDirection = 0;
    private int playerAnimationFrame = 0;
    private long playerLastFrameTime = 0;

    // Animação dos inimigos (movimento)
    private int enemyAnimationFrame = 0;
    private long enemyLastFrameTime = 0;

    /**
     * Construtor: inicializa dimensões da tela a partir do monitor primário.
     * Pré-calcula limites de borda para otimizações de movimento.
     */
    public AppGameLoop(App app) {
        this.app = app;
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        this.screenWidth = screenBounds.getWidth();
        this.screenHeight = screenBounds.getHeight();

        // OTIMIZAÇÃO: Pré-calcula os limites de borda para uso nos handlers
        this.topBoundary = GameConstants.SCREEN_EDGE_MARGIN;
        this.bottomBoundary = screenHeight - GameConstants.PLAYER_DISPLAY_SIZE
                            - GameConstants.SCREEN_EDGE_MARGIN;
        this.leftBoundary = GameConstants.SCREEN_EDGE_MARGIN;
        this.rightBoundary = screenWidth - GameConstants.PLAYER_DISPLAY_SIZE
                           - GameConstants.SCREEN_EDGE_MARGIN;
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
     *
     * Refatorado: Métodos privados extraem processamento de movimento
     * para melhor legibilidade e manutenção.
     */
    private AnimationTimer createPlayerMovementTimer() {
        return new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (App.keyUp) {
                    handleVerticalMovement(-GameConstants.PLAYER_MOVE_SPEED, 3,
                                          GameConstants.DIRECTION_UP, now);
                } else if (App.keyDown) {
                    handleVerticalMovement(GameConstants.PLAYER_MOVE_SPEED, 0,
                                          GameConstants.DIRECTION_DOWN, now);
                } else if (App.keyLeft) {
                    handleHorizontalMovement(-GameConstants.PLAYER_MOVE_SPEED, 1,
                                            GameConstants.DIRECTION_LEFT, now);
                } else if (App.keyRight) {
                    handleHorizontalMovement(GameConstants.PLAYER_MOVE_SPEED, 2,
                                            GameConstants.DIRECTION_RIGHT, now);
                }

                app.checkNpcCollision();
            }
        };
    }

    /**
     * Processa movimento vertical (cima/baixo) do jogador.
     * Alterna mapas se colide com borda da tela.
     *
     * OTIMIZAÇÃO: Usa limites pré-calculados em vez de recalcular cada frame.
     *
     * @param deltaY Mudança em Y (negativa = cima, positiva = baixo)
     * @param directionIndex Índice da direção no sprite sheet
     * @param mapDirection Direção para trocar mapa
     * @param time Tempo para sincronizar animação
     */
    private void handleVerticalMovement(double deltaY, int directionIndex,
                                       String mapDirection, long time) {
        double newY = app.playerView.getY() + deltaY;
        boolean atEdge = (deltaY < 0 && newY <= topBoundary) ||
                         (deltaY > 0 && newY >= bottomBoundary);

        if (atEdge) {
            app.changeMap(mapDirection);
        } else {
            app.playerView.setY(newY);
        }
        updatePlayerSprite(app.playerView, directionIndex, time);
    }

    /**
     * Processa movimento horizontal (esquerda/direita) do jogador.
     * Alterna mapas se colide com borda da tela.
     *
     * OTIMIZAÇÃO: Usa limites pré-calculados em vez de recalcular cada frame.
     *
     * @param deltaX Mudança em X (negativa = esquerda, positiva = direita)
     * @param directionIndex Índice da direção no sprite sheet
     * @param mapDirection Direção para trocar mapa
     * @param time Tempo para sincronizar animação
     */
    private void handleHorizontalMovement(double deltaX, int directionIndex,
                                         String mapDirection, long time) {
        double newX = app.playerView.getX() + deltaX;
        boolean atEdge = (deltaX < 0 && newX <= leftBoundary) ||
                         (deltaX > 0 && newX >= rightBoundary);

        if (atEdge) {
            app.changeMap(mapDirection);
        } else {
            app.playerView.setX(newX);
        }
        updatePlayerSprite(app.playerView, directionIndex, time);
    }

    /**
     * Cria o AnimationTimer responsável por:
     *   - Avançar o frame de animação dos inimigos
     *   - Atualizar posição e sprite de cada inimigo
     *   - Detectar colisão com o jogador
     *   - Iniciar batalha se colidiu
     *
     * Refatorado: Lógica de colisão extraída para método privado.
     */
    private AnimationTimer createEnemyAITimer(Stage stage, javafx.scene.Scene scene) {
        return new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Avança animação dos inimigos a cada intervalo
                if (now - enemyLastFrameTime > GameConstants.ANIMATION_FRAME_DELAY) {
                    enemyAnimationFrame = (enemyAnimationFrame + 1) % 4;
                    enemyLastFrameTime = now;
                }

                // Atualiza posições dos inimigos e verifica colisão
                int collidedEnemyIndex = app.enemyManager.update(
                    app.playerView.getX(),
                    app.playerView.getY(),
                    enemyAnimationFrame
                );

                if (collidedEnemyIndex != -1) {
                    handleEnemyCollision(stage, scene, collidedEnemyIndex);
                }
            }
        };
    }

    /**
     * Processa colisão com um inimigo: para timers, anima transição
     * e inicia a tela de batalha.
     *
     * @param stage Janela principal JavaFX
     * @param scene Cena do mapa (será restaurada após batalha)
     * @param enemyIndex Índice do inimigo que colidiu
     */
    private void handleEnemyCollision(Stage stage, javafx.scene.Scene scene, int enemyIndex) {
        // Para timers de movimento
        app.playerMovement.stop();
        app.enemyAI.stop();
        app.btnInventory.setVisible(false);

        // Afasta jogador do inimigo para evitar múltiplas colisões
        double enemyX = app.enemyManager.getView(enemyIndex).getX();
        double pushDistance = (app.playerView.getX() >= enemyX) ? 40 : -40;
        app.playerView.setX(app.playerView.getX() + pushDistance);

        // Marca inimigo para a batalha
        app.battleEnemyIndex = enemyIndex;

        // Prepara dados da batalha
        Monsters enemyMonster = app.enemyManager.getMonstro(enemyIndex);
        javafx.scene.image.ImageView enemyView = app.enemyManager.getView(enemyIndex);

        // Transição com fade out
        FadeTransition fadeOut = new FadeTransition(
            javafx.util.Duration.millis(GameConstants.FADE_TRANSITION_DURATION),
            app.mainLayout
        );
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            Battle.startBattle(
                stage, scene, app.player, enemyMonster,
                app.playerView, enemyView,
                app, app.mainLayout, app.hudManager
            );
            app.btnInventory.setVisible(true);
        });
        fadeOut.play();
    }

    /**
     * Atualiza o viewport do sprite sheet do jogador para animar movimento.
     * Muda a linha (direção) se necessário e avança os frames de animação.
     *
     * @param spriteView Imagem do sprite do jogador
     * @param newDirection Nova direção (0=baixo, 1=esquerda, 2=direita, 3=cima)
     * @param frameTime Tempo atual (para sincronização de frames)
     */
    private void updatePlayerSprite(javafx.scene.image.ImageView spriteView, int newDirection, long frameTime) {
        // Se mudou direção, reseta animação na nova direção
        if (playerSpriteDirection != newDirection) {
            playerSpriteDirection = newDirection;
            playerAnimationFrame = 0;
            setViewportForDirection(spriteView, 0, newDirection);
        }

        // Avança frame de animação a cada intervalo
        if (frameTime - playerLastFrameTime > GameConstants.ANIMATION_FRAME_DELAY) {
            playerAnimationFrame = (playerAnimationFrame + 1) % 4;
            setViewportForDirection(spriteView, playerAnimationFrame, newDirection);
            playerLastFrameTime = frameTime;
        }
    }

    /**
     * Define o viewport do sprite sheet para exibir um frame específico.
     *
     * @param spriteView Imagem do sprite
     * @param frameIndex Índice do frame de animação (0-3)
     * @param directionIndex Índice da direção (linha no sprite sheet)
     */
    private void setViewportForDirection(javafx.scene.image.ImageView spriteView,
                                        int frameIndex, int directionIndex) {
        int spriteX = frameIndex * GameConstants.PLAYER_SPRITE_SIZE;
        int spriteY = directionIndex * GameConstants.PLAYER_SPRITE_SIZE;

        spriteView.setViewport(new Rectangle2D(
            spriteX,
            spriteY,
            GameConstants.PLAYER_SPRITE_SIZE,
            GameConstants.PLAYER_SPRITE_SIZE
        ));
    }
}





