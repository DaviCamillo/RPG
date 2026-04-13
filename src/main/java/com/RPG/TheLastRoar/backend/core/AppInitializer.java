package com.RPG.TheLastRoar.backend.core;
import com.RPG.TheLastRoar.App;
import com.RPG.TheLastRoar.backend.managers.EnemyManager;
import com.RPG.TheLastRoar.backend.models.Character;
import com.RPG.TheLastRoar.backend.models.Sword;
import com.RPG.TheLastRoar.frontend.npc.ShopNPC;
import com.RPG.TheLastRoar.frontend.screens.HudManager;
import com.RPG.TheLastRoar.frontend.screens.PauseMenu;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * AppCollisions e Inicialização — Gerencia colisões com NPC, RES umeto de timers e inicialização.
 *
 * RESPONSABILIDADE:
 *   - Inicializar o jogo (personagem, mapa, inimigos, HUD)
 *   - Verificar colisões com NPC
 *   - Resumir timers após batalha
 *   - Atualizar visibilidade do NPC
 */
public class AppInitializer {

    private App app;
    private static final double PLAYER_DISPLAY_SIZE = 80;
    private static final int    SPRITE_W            = 128;
    private static final int    SPRITE_H            = 128;

    public AppInitializer(App app) {
        this.app = app;
    }

    /**
     * Inicializa e inicia o jogo (novo ou carregado de save).
     *
     * @param stage    Stage principal
     * @param saveFile Nome do arquivo de save a carregar, ou null para novo jogo
     */
    public void startGame(Stage stage, String saveFile) {
        if (app.gameRunning) return;
        app.gameRunning = true;

        if (app.playerMovement != null) app.playerMovement.stop();
        if (app.enemyAI        != null) app.enemyAI.stop();

        try {
            // ── Dimensões da tela ─────────────────────────────────────────
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            app.screenW = screenBounds.getWidth();
            app.screenH = screenBounds.getHeight();

            // ── Mapa inicial ───────────────────────────────────────────────
            app.mapView = new ImageView(new Image(
                getClass().getResource("/images/mapa_padrao.png").toExternalForm()
            ));

            // ── Camada de jogo ────────────────────────────────────────────
            app.gameRoot = new Pane();
            app.gameRoot.setPrefSize(app.screenW, app.screenH);

            // ── Personagem ────────────────────────────────────────────────
            app.player = new Character(
                "Hero", 20, 0,
                new Sword("Madeira", 3, 6, "Comum", 4),
                new Image(getClass().getResource("/images/sprite_personagem.png").toExternalForm()),
                new Image(getClass().getResource("/images/personagem_battle.png").toExternalForm())
            );
            app.player.getInventory().addItem(app.player.getSword());

            // ── Sprite do personagem ──────────────────────────────────────
            app.playerView = new ImageView(app.player.getSprite());
            app.playerView.setViewport(new Rectangle2D(0, 0, SPRITE_W, SPRITE_H));
            app.playerView.setFitWidth(PLAYER_DISPLAY_SIZE);
            app.playerView.setFitHeight(PLAYER_DISPLAY_SIZE);
            app.playerView.setX((app.screenW / 2) - (PLAYER_DISPLAY_SIZE / 2));
            app.playerView.setY(app.screenH - PLAYER_DISPLAY_SIZE - 20);
            app.gameRoot.getChildren().add(app.playerView);

            // ── NPC da loja ───────────────────────────────────────────────
            app.npcView = ShopNPC.criarSprite();
            app.gameRoot.getChildren().add(app.npcView);

            // ── Inimigos ──────────────────────────────────────────────────
            app.defeatedEnemies = new boolean[app.MAP_LIST.length][10];
            app.enemyManager    = new EnemyManager(app.gameRoot, app.screenW, app.screenH, app.defeatedEnemies);

            // ── HUD ───────────────────────────────────────────────────────
            app.hudManager = new HudManager(stage);

            // ── Carrega save ou inicia mapa padrão ────────────────────────
            if (saveFile != null) {
                app.gameState.loadFromSave(saveFile, true);
            } else {
                app.currentMapIndex = 0;
                app.enemyManager.configureForMap(app.currentMapIndex);
                app.hudManager.atualizar(app.player);
            }

            updateNpcVisibility();

            // ── Menu de pausa ─────────────────────────────────────────────
            app.pauseMenu = new PauseMenu(
                app::togglePause,
                () -> app.gameState.saveToSlot("save1.json"),
                () -> app.gameState.saveToSlot("save2.json"),
                () -> app.gameState.saveToSlot("save3.json"),
                () -> app.gameState.loadFromSave("save1.json", false),
                () -> app.gameState.loadFromSave("save2.json", false),
                () -> app.gameState.loadFromSave("save3.json", false),
                () -> { app.togglePause(); app.showMainMenu(); }
            );

            // ── Botão de inventário (overlay) ─────────────────────────────
            app.uiElements.buildInventoryButton();

            // ── Layout principal ──────────────────────────────────────────
            app.mainLayout = new StackPane(
                app.mapView,
                app.gameRoot,
                app.hudManager.getLayout(),
                app.btnInventory,
                app.pauseMenu.getLayout()
            );
            app.mainLayout.setStyle("-fx-background-color: black;");
            app.mapView.fitWidthProperty().bind(stage.widthProperty());
            app.mapView.fitHeightProperty().bind(stage.heightProperty());

            // ── Controles e loop de jogo ──────────────────────────────────
            app.keyboardControls.setupKeyboardControls(app.masterScene);
            app.masterScene.setRoot(app.mainLayout);
            app.gameLoop.startGameTimers(stage, app.masterScene);

            Platform.runLater(() -> {
                if (!stage.isFullScreen()) stage.setFullScreen(true);
                app.mainLayout.requestFocus();
            });

            // Fade in da cena de jogo
            app.mainLayout.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(800), app.mainLayout);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            app.gameRunning = false;
            System.err.println("[App] Erro crítico ao iniciar o jogo:");
            e.printStackTrace();
        }
    }

    /**
     * Mostra o NPC apenas no mapa 0 (onde a loja existe).
     */
    public void updateNpcVisibility() {
        if (app.npcView != null) app.npcView.setVisible(app.currentMapIndex == 0);
    }

    /**
     * Verifica se o jogador colidiu com o NPC e abre a loja.
     */
    public void checkNpcCollision() {
        if (app.currentMapIndex != 0 || app.lojaAberta || app.npcView == null || app.isTransitioning) return;

        if (ShopNPC.verificarColisao(app.playerView.getX(), app.playerView.getY())) {
            app.playerMovement.stop();
            app.enemyAI.stop();
            App.resetMovement();
            app.lojaAberta = true;
            app.btnInventory.setVisible(false);

            ShopNPC.abrirLoja(app.mainLayout, app.player, app.hudManager, () -> {
                app.lojaAberta = false;
                app.btnInventory.setVisible(true);
                app.lastFrameTime = System.nanoTime();
                app.playerMovement.start();
                if (!app.enemyManager.isEmpty()) app.enemyAI.start();
                Platform.runLater(() -> app.mainLayout.requestFocus());
            });
        }
    }

    /**
     * Chamado após o fim da batalha (vitória ou fuga).
     * Remove o inimigo derrotado e reinicia os timers do jogo.
     */
    public void resumeTimers() {
        if (app.battleEnemyIndex != -1) {
            app.enemyManager.removeEnemy(app.battleEnemyIndex);
            app.battleEnemyIndex = -1;
        }

        App.resetMovement();
        app.lastFrameTime = System.nanoTime();
        app.mainLayout.setOpacity(1.0);

        Platform.runLater(() -> {
            app.mainLayout.requestFocus();
            app.playerMovement.start();
            if (!app.enemyManager.isEmpty()) app.enemyAI.start();
        });
    }
}






