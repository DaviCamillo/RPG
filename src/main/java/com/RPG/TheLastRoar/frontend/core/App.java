package com.RPG.TheLastRoar.frontend.core;


import com.RPG.TheLastRoar.backend.managers.EnemyManager;
import com.RPG.TheLastRoar.backend.managers.SaveManager;
import com.RPG.TheLastRoar.backend.models.Character;
import com.RPG.TheLastRoar.backend.models.Item;
import com.RPG.TheLastRoar.backend.models.Potion;
import com.RPG.TheLastRoar.frontend.screens.Battle;
import com.RPG.TheLastRoar.frontend.screens.HudManager;
import com.RPG.TheLastRoar.frontend.screens.IntroScreen;
import com.RPG.TheLastRoar.frontend.screens.InventoryScreen;
import com.RPG.TheLastRoar.frontend.screens.PauseMenu;
import com.RPG.TheLastRoar.frontend.screens.StartScreen;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * ============================================================
 * App.java — Controlador Principal do Jogo "The Last Roar"
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Ponto de entrada JavaFX. Gerencia o loop de jogo,
 *   troca de cenários, colisões, pausa e transições de cenas.
 *
 * FLUXO GERAL:
 *
 *   start()
 *    └─► StartScreen → opção escolhida
 *         ├─► Novo Jogo → IntroScreen → startGame()
 *         ├─► Continuar → startGame(ultimoSave)
 *         └─► Carregar  → startGame(slotEscolhido)
 *
 *   startGame()
 *    ├─► Inicializa mapa, personagem, inimigos, HUD
 *    ├─► Cria playerMovementTimer (move sprite + detecta bordas)
 *    └─► Cria enemyAITimer (move inimigos + detecta colisão)
 *
 *   Colisão com inimigo
 *    └─► Battle.startBattle() → tela de batalha
 *         └─► Após batalha: resumeTimers() → volta ao mapa
 *
 *   Tecla ESC → togglePause() ↔ PauseMenu
 *   Tecla I   → abrirInventario() → InventoryScreen
 *   NPC       → ShopNPC.abrirLoja()
 *   Bordas    → trocarCenario() → próximo/anterior mapa
 *
 * ESTADOS DO JOGO (flags booleanos):
 *   isPaused         — jogo pausado via ESC
 *   lojaAberta       — loja do NPC aberta
 *   inventarioAberto — tela de inventário aberta
 *   isTransitioning  — troca de mapa em andamento
 *   gameRunning      — jogo iniciado (evita múltiplos startGame)
 *
 * SPRITE DO JOGADOR:
 *   Sprite sheet 4x4 frames (128x128px cada):
 *   Linhas: 0=baixo, 1=esquerda, 2=direita, 3=cima
 *   Colunas: frames 0-3 de animação de movimento
 *
 * MAPAS:
 *   mapa_padrao.png  (índice 0) — Goblin × 2 + NPC loja
 *   mapa_padrao2.png (índice 1) — GoblinExp × 2
 *   mapa_padrao3.png (índice 2) — GoblinBoss × 1
 */
public class App extends javafx.application.Application {

    // =========================================================================
    // FIELDS — Dimensões da tela (PUBLIC para acesso pelos delegadores)
    // =========================================================================

    public double screenW;
    public double screenH;

    // =========================================================================
    // FIELDS — Componentes da cena do jogo (PUBLIC para acesso pelos delegadores)
    // =========================================================================

    public Pane      gameRoot;      // Camada onde os sprites são renderizados
    public ImageView playerView;    // Sprite do personagem
    public ImageView mapView;       // Imagem de fundo do mapa
    public StackPane mainLayout;    // Layout principal (mapa + HUD + menus)
    public Scene     masterScene;   // Cena JavaFX principal (reutilizada entre telas)
    public Button    btnInventory;  // Botão flutuante de atalho do inventário

    // =========================================================================
    // FIELDS — Dados do jogo (PUBLIC para acesso pelos delegadores)
    // =========================================================================

    public Character player;

    /** Timers do loop de jogo. Separados para controle independente. */
    public AnimationTimer playerMovement;
    public AnimationTimer enemyAI;

    // =========================================================================
    // FIELDS — Timing do jogo (PUBLIC para acesso pelos delegadores)
    // =========================================================================

    public long lastFrameTime = 0;

    // =========================================================================
    // FIELDS — Estado das teclas pressionadas (controles)
    // =========================================================================

    /** Flags static das teclas de movimento (lidas pelos AnimationTimers). */
    public static boolean keyUp, keyDown, keyLeft, keyRight;

    // =========================================================================
    // FIELDS — Estado do jogo (flags de bloqueio de entrada)
    // =========================================================================

    public boolean isPaused         = false;
    public boolean lojaAberta       = false;
    public boolean inventarioAberto = false;
    public boolean isTransitioning  = false;
    public boolean gameRunning      = false; // Evita iniciar o jogo mais de uma vez

    // =========================================================================
    // FIELDS — Mapas e inimigos
    // =========================================================================

    public final String[] MAP_LIST = {
        "mapa_padrao.png", "mapa_padrao2.png", "mapa_padrao3.png"
    };
    public int       currentMapIndex = 0;
    public boolean[][] defeatedEnemies;  // [mapa][idInimigo]

    // =========================================================================
    // FIELDS — Managers e NPC
    // =========================================================================

    public HudManager   hudManager;
    public PauseMenu    pauseMenu;
    public EnemyManager enemyManager;
    public ImageView    npcView;

    /** Índice do inimigo que iniciou a batalha atual (-1 = sem batalha). */
    public int battleEnemyIndex = -1;

    // =========================================================================
    // FIELDS — Delegadores (responsáveis por diferentes funcionalidades)
    // =========================================================================

    public AppGameLoop      gameLoop;
    public AppKeyboardControls keyboardControls;
    public AppGameState     gameState;
    public AppUIElements    uiElements;
    public AppInitializer   initializer;

    // =========================================================================
    // UTILITY
    // =========================================================================

    /**
     * Reseta todos os flags de movimento.
     * Chamado ao pausar, abrir menus ou entrar em batalha.
     */
    public static void resetMovement() {
        keyUp = keyDown = keyLeft = keyRight = false;
    }

    // =========================================================================
    // JAVAFX ENTRY POINT
    // =========================================================================

    @Override
    public void start(Stage stage) {
        // Initialize delegators
        gameLoop = new AppGameLoop(this);
        keyboardControls = new AppKeyboardControls(this);
        gameState = new AppGameState(this);
        uiElements = new AppUIElements(this);
        initializer = new AppInitializer(this);

        // Carrega o ícone da janela
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("[App] Ícone não encontrado: " + e.getMessage());
        }

        // Monta o menu inicial
        StackPane menuLayout = StartScreen.createLayout(
            () -> IntroScreen.play(stage, masterScene, () -> initializer.startGame(stage, null)),
            () -> initializer.startGame(stage, StartScreen.getUltimoSave()),
            (slotName) -> initializer.startGame(stage, slotName)
        );

        masterScene = new Scene(menuLayout, 800, 600);
        stage.setTitle("The Last Roar");
        stage.setScene(masterScene);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreen(true);
        stage.show();

        Platform.runLater(menuLayout::requestFocus);
    }

    // =========================================================================
    // GAME STATE — Pausa, Inventário, Menu Principal
    // =========================================================================

    /**
     * Alterna entre pausado e em jogo.
     */
    public void togglePause() {
        if (lojaAberta || inventarioAberto || isTransitioning) return;
        isPaused = !isPaused;

        if (isPaused) {
            playerMovement.stop();
            enemyAI.stop();
            resetMovement();
            btnInventory.setVisible(false);
            pauseMenu.atualizarBotoesLoad(
                SaveManager.existe("save1.json"),
                SaveManager.existe("save2.json"),
                SaveManager.existe("save3.json")
            );
            pauseMenu.setVisible(true);
        } else {
            pauseMenu.setVisible(false);
            btnInventory.setVisible(true);
            playerMovement.start();
            if (!enemyManager.isEmpty()) enemyAI.start();
            Platform.runLater(() -> mainLayout.requestFocus());
        }
    }

    /**
     * Para os timers, reseta todos os estados e volta ao menu inicial.
     */
    public void showMainMenu() {
        if (playerMovement != null) playerMovement.stop();
        if (enemyAI        != null) enemyAI.stop();

        gameRunning      = false;
        isPaused         = false;
        lojaAberta       = false;
        inventarioAberto = false;
        isTransitioning  = false;
        resetMovement();

        Battle.resetInBattle();

        Stage stage = (Stage) masterScene.getWindow();
        StackPane menuLayout = StartScreen.createLayout(
            () -> IntroScreen.play(stage, masterScene, () -> initializer.startGame(stage, null)),
            () -> initializer.startGame(stage, StartScreen.getUltimoSave()),
            (slotName) -> initializer.startGame(stage, slotName)
        );
        masterScene.setRoot(menuLayout);
        if (!stage.isFullScreen()) stage.setFullScreen(true);
        Platform.runLater(menuLayout::requestFocus);
    }

    // =========================================================================
    // QUICK POTION USE — Uso rápido de poção (tecla H)
    // =========================================================================

    /**
     * Usa a primeira poção disponível no inventário (atalho tecla H).
     */
    public void usePotionOnMap() {
        if (player.getLife() >= player.getMaxLife()) return;

        Potion foundPotion = null;
        for (Item item : player.getInventory().getItems()) {
            if (item instanceof Potion p) { foundPotion = p; break; }
        }
        if (foundPotion == null) return;

        int actualHeal = Math.min(foundPotion.getHealedLife(),
                                  player.getMaxLife() - player.getLife());
        player.heal(foundPotion.getHealedLife());
        player.getInventory().removeItem(foundPotion);
        hudManager.atualizar(player);

        uiElements.showToast("+" + actualHeal + " HP", javafx.scene.paint.Color.web("#00CC66"));
    }

    // =========================================================================
    // INVENTORY — Abertura do inventário
    // =========================================================================

    /**
     * Abre a tela de inventário como overlay.
     */
    public void openInventory() {
        if (inventarioAberto || isPaused || lojaAberta || isTransitioning) return;

        inventarioAberto = true;
        btnInventory.setVisible(false);
        playerMovement.stop();
        enemyAI.stop();
        resetMovement();

        InventoryScreen.open(mainLayout, player, () -> {
            inventarioAberto = false;
            btnInventory.setVisible(true);
            playerMovement.start();
            if (!enemyManager.isEmpty()) enemyAI.start();
            Platform.runLater(() -> mainLayout.requestFocus());
        });
    }

    // =========================================================================
    // PUBLIC DELEGATED METHODS — Chamadas pelos delegadores
    // =========================================================================

    /**
     * Método público para changeMap chamado pelo AppGameState.
     */
    public void changeMap(String direction) {
        gameState.changeMap(direction);
    }

    /**
     * Método público para updateNpcVisibility chamado pelo AppInitializer.
     */
    public void updateNpcVisibility() {
        initializer.updateNpcVisibility();
    }

    /**
     * Método público para checkNpcCollision chamado pelo AppGameLoop.
     */
    public void checkNpcCollision() {
        initializer.checkNpcCollision();
    }

    /**
     * Método público para resumeTimers chamado por Battle.java.
     */
    public void resumeTimers() {
        initializer.resumeTimers();
    }

    // =========================================================================
    // MAIN
    // =========================================================================

    public static void main(String[] args) { launch(); }
}


