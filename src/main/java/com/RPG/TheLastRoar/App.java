package com.RPG.TheLastRoar;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * ============================================================
 * App.java — Controlador principal do jogo "The Last Roar"
 * ============================================================
 */
public class App extends javafx.application.Application {

    // ── Dimensões da tela ──────────────────────────────────────────────────
    private double screenW;
    private double screenH;

    // ── Nós visuais principais ─────────────────────────────────────────────
    private Pane      gameRoot;
    private ImageView playerView;
    private ImageView mapView;
    private StackPane mainLayout;
    private Scene     cenaMestra;
    private Button    btnInventario; // NOVO: Botão de Inventário na UI

    // ── Entidade do jogador ────────────────────────────────────────────────
    private Character player;

    // ── Timers de animação ─────────────────────────────────────────────────
    public AnimationTimer playerMovement;
    public AnimationTimer enemyAI;

    // ── Estado de animação do sprite do jogador ────────────────────────────
    private int    direction     = 0;   // 0=baixo, 1=esquerda, 2=direita, 3=cima
    private int    frame         = 0;
    private long   lastFrameTime = 0;

    // ── Constantes de sprite e movimento ──────────────────────────────────
    private static final long   FRAME_DELAY        = 200_000_000L; // 200ms em nanosegundos
    private static final double SPEED              = 4;
    private static final int    SPRITE_W           = 128;
    private static final int    SPRITE_H           = 128;
    private static final double PLAYER_DISPLAY_SIZE = 80;

    // ── Animação dos inimigos ──────────────────────────────────────────────
    private int  enemyFrame         = 0;
    private long lastEnemyFrameTime = 0;

    // ── Flags de direção (atualizadas pelo teclado) ────────────────────────
    private static boolean up, down, left, right;

    // ── Flags de estado da tela ───────────────────────────────────────────
    private boolean isPaused         = false;
    private boolean lojaAberta       = false;
    private boolean inventarioAberto = false;

    // ── Sistema de mapas ──────────────────────────────────────────────────
    private final String[] LISTA_MAPAS = {
        "mapa_padrao.png", "mapa_padrao2.png", "mapa_padrao3.png"
    };
    private int  indiceMapa = 0;
    private boolean[][] inimigosDerrotados;

    // ── Gerenciadores de subsistemas ───────────────────────────────────────
    private HudManager   hudManager;
    private PauseMenu    pauseMenu;
    private EnemyManager enemyManager;

    // ── NPC vendedor ───────────────────────────────────────────────────────
    private ImageView npcView;

    // ── Índice do monstro em batalha ──────────────────────────────────────
    private int monstroEmBatalhaIndex = -1;

    // =========================================================================
    // UTILIDADE ESTÁTICA
    // =========================================================================

    public static void resetMovement() {
        up = down = left = right = false;
    }

    // =========================================================================
    // CICLO DE VIDA — JavaFX
    // =========================================================================

    @Override
    public void start(Stage stage) {
        StackPane menuLayout = StartScreen.createLayout(
            () -> IntroScreen.play(stage, cenaMestra, () -> iniciarJogo(stage, null)),
            () -> iniciarJogo(stage, StartScreen.getUltimoSave()),
            (slotName) -> iniciarJogo(stage, slotName)
        );

        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("[App] Ícone não encontrado: " + e.getMessage());
        }

        cenaMestra = new Scene(menuLayout, 800, 600);
        stage.setTitle("The Last Roar");
        stage.setScene(cenaMestra);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreen(true);
        stage.show();
    }

    // =========================================================================
    // INICIALIZAÇÃO DO JOGO
    // =========================================================================

    private void iniciarJogo(Stage stage, String saveFile) {
        try {
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            screenW = screenBounds.getWidth();
            screenH = screenBounds.getHeight();

            mapView = new ImageView(new Image(getClass().getResource("/images/mapa_padrao.png").toExternalForm()));

            gameRoot = new Pane();
            gameRoot.setPrefSize(screenW, screenH);

            player = new Character("Hero", 100, 2,
                new Sword("Madeira", 3, 6, "Comum", 4),
                new Image(getClass().getResource("/images/sprite_personagem.png").toExternalForm()),
                new Image(getClass().getResource("/images/personagem_battle.png").toExternalForm())
            );
            player.getInventory().addItem(player.getSword());

            playerView = new ImageView(player.getSprite());
            playerView.setViewport(new Rectangle2D(0, 0, SPRITE_W, SPRITE_H));
            playerView.setFitWidth(PLAYER_DISPLAY_SIZE);
            playerView.setFitHeight(PLAYER_DISPLAY_SIZE);
            playerView.setX((screenW / 2) - (PLAYER_DISPLAY_SIZE / 2));
            playerView.setY(screenH - PLAYER_DISPLAY_SIZE - 20);
            gameRoot.getChildren().add(playerView);

            npcView = ShopNPC.criarSprite();
            gameRoot.getChildren().add(npcView);

            inimigosDerrotados = new boolean[LISTA_MAPAS.length][10];
            enemyManager = new EnemyManager(gameRoot, screenW, screenH, inimigosDerrotados);

            hudManager = new HudManager(stage);

            if (saveFile != null) {
                carregarDeJson(saveFile, true);
            } else {
                indiceMapa = 0;
                enemyManager.configurarParaMapa(indiceMapa);
                hudManager.atualizar(player);
            }

            atualizarVisibilidadeNPC();

            pauseMenu = new PauseMenu(
                this::togglePause,
                () -> salvarSlot("save1.json"),
                () -> salvarSlot("save2.json"),
                () -> salvarSlot("save3.json"),
                () -> carregarDeJson("save1.json", false),
                () -> carregarDeJson("save2.json", false),
                () -> carregarDeJson("save3.json", false),
                () -> { togglePause(); showMainMenu(); }
            );

            // Cria o botão de inventário visual
            criarBotaoInventario();

            // Adiciona o botão de inventário no layout principal
            mainLayout = new StackPane(
                mapView,
                gameRoot,
                hudManager.getLayout(),
                btnInventario,  // <--- Botão adicionado à tela
                pauseMenu.getLayout()
            );
            mainLayout.setStyle("-fx-background-color: black;");
            mapView.fitWidthProperty().bind(stage.widthProperty());
            mapView.fitHeightProperty().bind(stage.heightProperty());

            configurarControlesDeTeclado();
            cenaMestra.setRoot(mainLayout);
            iniciarTimers(stage, cenaMestra);

            Platform.runLater(() -> { if (!stage.isFullScreen()) stage.setFullScreen(true); });

            mainLayout.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(800), mainLayout);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            System.err.println("[App] Erro crítico ao iniciar o jogo:");
            e.printStackTrace();
        }
    }

    // =========================================================================
    // CRIAÇÃO DO BOTÃO DE INVENTÁRIO (Canto Inferior Direito)
    // =========================================================================

    private void criarBotaoInventario() {
        btnInventario = new Button("🎒 Inventário (I)");
        btnInventario.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        // Estilo base do botão (fundo escuro, borda dourada igual HUD)
        String estNormal = 
            "-fx-background-color: rgba(20, 20, 20, 0.7);" +
            "-fx-text-fill: #E8DFC0;" +
            "-fx-border-color: #B8960C;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;";
            
        String estHover = 
            "-fx-background-color: rgba(60, 50, 20, 0.9);" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-border-color: #FFD700;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;";

        btnInventario.setStyle(estNormal);
        
        // Efeitos de Hover
        btnInventario.setOnMouseEntered(e -> btnInventario.setStyle(estHover));
        btnInventario.setOnMouseExited(e -> btnInventario.setStyle(estNormal));

        // Ação de Clique
        btnInventario.setOnAction(e -> {
            // Retorna o foco para o jogo após clicar
            Platform.runLater(() -> mainLayout.requestFocus());
            abrirInventario();
        });

        // Alinha no canto inferior direito com margem
        StackPane.setAlignment(btnInventario, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(btnInventario, new Insets(0, 30, 30, 0)); // Direita: 30, Baixo: 30
    }

    // =========================================================================
    // CONTROLES DE TECLADO
    // =========================================================================

    private void configurarControlesDeTeclado() {
        cenaMestra.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) togglePause();

            if (!isPaused && !lojaAberta && !inventarioAberto) {
                switch (e.getCode()) {
                    case W, UP    -> up    = true;
                    case S, DOWN  -> down  = true;
                    case A, LEFT  -> left  = true;
                    case D, RIGHT -> right = true;
                    case H        -> usarPocaoNoMapa();
                    case I        -> abrirInventario(); // O Atalho I continua funcionando
                    default       -> {}
                }
            }
        });

        cenaMestra.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case W, UP    -> up    = false;
                case S, DOWN  -> down  = false;
                case A, LEFT  -> left  = false;
                case D, RIGHT -> right = false;
                default       -> {}
            }
        });
    }

    // =========================================================================
    // USAR POÇÃO NO MAPA (tecla H)
    // =========================================================================

    private void usarPocaoNoMapa() {
        if (player.getLife() >= player.getMaxLife()) return;

        Potion pocaoUsada = null;
        for (Item item : player.getInventory().getItems()) {
            if (item instanceof Potion p) { pocaoUsada = p; break; }
        }
        if (pocaoUsada == null) return; 

        int curaReal = Math.min(pocaoUsada.getHealedLife(), player.getMaxLife() - player.getLife());
        player.heal(pocaoUsada.getHealedLife());
        player.getInventory().removeItem(pocaoUsada);
        hudManager.atualizar(player);

        exibirToast("+" + curaReal + " HP", Color.web("#00CC66"));
    }

    // =========================================================================
    // ABRIR INVENTÁRIO (Botão UI ou tecla I)
    // =========================================================================

    /**
     * Abre a tela de inventário como overlay.
     */
    private void abrirInventario() {
        // SEGURANÇA: Não abre se já estiver aberto, pausado ou na loja
        if (inventarioAberto || isPaused || lojaAberta) return;
        
        inventarioAberto = true;
        btnInventario.setVisible(false); // Esconde o botão enquanto o inventário está aberto
        
        playerMovement.stop();
        enemyAI.stop();
        resetMovement();

        InventoryScreen.open(mainLayout, player, () -> {
            inventarioAberto = false;
            btnInventario.setVisible(true); // Mostra o botão novamente
            playerMovement.start();
            if (!enemyManager.isEmpty()) enemyAI.start();
            Platform.runLater(() -> mainLayout.requestFocus());
        });
    }

    // =========================================================================
    // TOAST FLUTUANTE
    // =========================================================================

    private void exibirToast(String mensagem, Color cor) {
        Label toast = new Label(mensagem);
        toast.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        toast.setTextFill(cor);
        toast.setStyle(
            "-fx-background-color: rgba(0,0,0,0.65);" +
            "-fx-padding: 12 28;" +
            "-fx-background-radius: 12;"
        );
        toast.setMouseTransparent(true);
        StackPane.setAlignment(toast, Pos.CENTER);
        mainLayout.getChildren().add(toast);
        toast.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toast);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(e -> {
            PauseTransition espera = new PauseTransition(Duration.millis(1000));
            espera.setOnFinished(ev -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(eff -> mainLayout.getChildren().remove(toast));
                fadeOut.play();
            });
            espera.play();
        });
        fadeIn.play();
    }

    // =========================================================================
    // NPC VENDEDOR
    // =========================================================================

    private void atualizarVisibilidadeNPC() {
        if (npcView != null) npcView.setVisible(indiceMapa == 0);
    }

    private void verificarColisaoNPC() {
        if (indiceMapa != 0 || lojaAberta || npcView == null) return;

        if (ShopNPC.verificarColisao(playerView.getX(), playerView.getY())) {
            playerMovement.stop();
            enemyAI.stop();
            resetMovement();
            lojaAberta = true;
            btnInventario.setVisible(false); // Esconde botão de inventário na loja

            ShopNPC.abrirLoja(mainLayout, player, hudManager, () -> {
                lojaAberta = false; 
                btnInventario.setVisible(true); // Retorna botão
                lastFrameTime = System.nanoTime();
                playerMovement.start();
                if (!enemyManager.isEmpty()) enemyAI.start();
                Platform.runLater(() -> mainLayout.requestFocus());
            });
        }
    }

    // =========================================================================
    // MENU PRINCIPAL
    // =========================================================================

    public void showMainMenu() {
        if (playerMovement != null) playerMovement.stop();
        if (enemyAI        != null) enemyAI.stop();

        isPaused         = false;
        lojaAberta       = false;
        inventarioAberto = false;
        resetMovement();

        Battle.resetInBattle();

        Stage stage = (Stage) cenaMestra.getWindow();
        StackPane menuLayout = StartScreen.createLayout(
            () -> IntroScreen.play(stage, cenaMestra, () -> iniciarJogo(stage, null)),
            () -> iniciarJogo(stage, StartScreen.getUltimoSave()),
            (slotName) -> iniciarJogo(stage, slotName)
        );
        cenaMestra.setRoot(menuLayout);
        if (!stage.isFullScreen()) stage.setFullScreen(true);
    }

    // =========================================================================
    // PAUSA
    // =========================================================================

    private void togglePause() {
        if (lojaAberta || inventarioAberto) return;
        isPaused = !isPaused;

        if (isPaused) {
            playerMovement.stop();
            enemyAI.stop();
            resetMovement();
            btnInventario.setVisible(false); // Esconde botão na pausa
            pauseMenu.atualizarBotoesLoad(
                SaveManager.existe("save1.json"),
                SaveManager.existe("save2.json"),
                SaveManager.existe("save3.json")
            );
            pauseMenu.setVisible(true);
        } else {
            pauseMenu.setVisible(false);
            btnInventario.setVisible(true); // Retorna botão
            playerMovement.start();
            if (!enemyManager.isEmpty()) enemyAI.start();
        }
    }

    // =========================================================================
    // SAVE / LOAD
    // =========================================================================

    private void salvarSlot(String arquivo) {
        SaveManager.salvar(arquivo, indiceMapa,
            playerView.getX(), playerView.getY(), player, inimigosDerrotados);
        togglePause();
    }

    private void carregarDeJson(String arquivo, boolean isInitialLoad) {
        SaveManager.SaveData data = SaveManager.carregar(arquivo, LISTA_MAPAS.length);
        if (data == null) return;

        indiceMapa = data.mapa;

        for (int i = 0; i < data.inimigosDerrotados.length; i++) {
            System.arraycopy(data.inimigosDerrotados[i], 0,
                             inimigosDerrotados[i], 0,
                             data.inimigosDerrotados[i].length);
        }

        mapView.setImage(new Image(
            getClass().getResource("/images/" + LISTA_MAPAS[indiceMapa]).toExternalForm()));
        enemyManager.configurarParaMapa(indiceMapa);
        atualizarVisibilidadeNPC();

        playerView.setX(data.posX);
        playerView.setY(data.posY);
        player.setLife(data.vida);
        player.setNivel(data.level);
        player.setCoin(data.ouro);

        if (!isInitialLoad) togglePause();
        if (hudManager != null) hudManager.atualizar(player);
    }

    // =========================================================================
    // TROCA DE CENÁRIO (mapa)
    // =========================================================================

    private void trocarCenario(boolean avancar) {
        playerMovement.stop();
        enemyAI.stop();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), mainLayout);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            boolean trocou = false;

            if (avancar && indiceMapa < LISTA_MAPAS.length - 1) {
                indiceMapa++;
                playerView.setY(20);
                playerView.setX((screenW / 2) - (SPRITE_W / 2));
                trocou = true;
            } else if (!avancar && indiceMapa > 0) {
                indiceMapa--;
                playerView.setY(screenH - SPRITE_H - 20);
                playerView.setX((screenW / 2) - (SPRITE_W / 2));
                trocou = true;
            }

            if (!trocou) {
                playerMovement.start();
                if (!enemyManager.isEmpty()) enemyAI.start();
                return;
            }

            mapView.setImage(new Image(
                getClass().getResource("/images/" + LISTA_MAPAS[indiceMapa]).toExternalForm()));
            enemyManager.configurarParaMapa(indiceMapa);
            atualizarVisibilidadeNPC();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainLayout);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setOnFinished(ev -> {
                playerMovement.start();
                if (!enemyManager.isEmpty()) enemyAI.start();
            });
            fadeIn.play();
        });
        fadeOut.play();
    }

    // =========================================================================
    // PÓS-BATALHA
    // =========================================================================

    public void resumeTimers() {
        if (monstroEmBatalhaIndex != -1) {
            enemyManager.removerInimigo(monstroEmBatalhaIndex);
            monstroEmBatalhaIndex = -1;
        }

        resetMovement();
        lastFrameTime = System.nanoTime();
        mainLayout.setOpacity(1.0);

        Platform.runLater(() -> {
            mainLayout.requestFocus();
            playerMovement.start();
            if (!enemyManager.isEmpty()) enemyAI.start();
        });
    }

    // =========================================================================
    // TIMERS (LOOP PRINCIPAL DO JOGO)
    // =========================================================================

    private void iniciarTimers(Stage stage, Scene scene) {

        playerMovement = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (up) {
                    if (playerView.getY() <= 5) {
                        trocarCenario(true);
                    } else {
                        playerView.setY(playerView.getY() - SPEED);
                    }
                    setDirection(playerView, 3);
                    animate(playerView, now);

                } else if (down) {
                    if (playerView.getY() >= screenH - SPRITE_H - 5) {
                        trocarCenario(false);
                    } else {
                        playerView.setY(playerView.getY() + SPEED);
                    }
                    setDirection(playerView, 0);
                    animate(playerView, now);

                } else if (left) {
                    if (playerView.getX() > 5)
                        playerView.setX(playerView.getX() - SPEED);
                    setDirection(playerView, 1);
                    animate(playerView, now);

                } else if (right) {
                    if (playerView.getX() < screenW - SPRITE_W - 5)
                        playerView.setX(playerView.getX() + SPEED);
                    setDirection(playerView, 2);
                    animate(playerView, now);
                }

                verificarColisaoNPC();
            }
        };
        playerMovement.start();

        enemyAI = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastEnemyFrameTime > FRAME_DELAY) {
                    enemyFrame = (enemyFrame + 1) % 4;
                    lastEnemyFrameTime = now;
                }

                int colisao = enemyManager.atualizar(
                    playerView.getX(), playerView.getY(), enemyFrame);

                if (colisao != -1) {
                    playerMovement.stop();
                    enemyAI.stop();
                    
                    // Esconde botão durante a batalha
                    btnInventario.setVisible(false);

                    double dx = playerView.getX() - enemyManager.getView(colisao).getX();
                    playerView.setX(playerView.getX() + (dx > 0 ? 40 : -40));
                    monstroEmBatalhaIndex = colisao;

                    Monsters  monstro = enemyManager.getMonstro(colisao);
                    ImageView ev      = enemyManager.getView(colisao);

                    FadeTransition ft = new FadeTransition(Duration.millis(500), mainLayout);
                    ft.setFromValue(1);
                    ft.setToValue(0);
                    ft.setOnFinished(e -> {
                        Battle.startBattle(stage, cenaMestra, player, monstro,
                                           playerView, ev, App.this, mainLayout, hudManager);
                        // Ao final da batalha o botão precisa voltar,
                        // Você pode garantir que btnInventario.setVisible(true) aconteça no seu resumeTimers() se necessário.
                        btnInventario.setVisible(true);
                    });
                    ft.play();
                }
            }
        };
        enemyAI.start();
    }

    // =========================================================================
    // ANIMAÇÃO DO SPRITE DO JOGADOR
    // =========================================================================

    private void animate(ImageView view, long now) {
        if (now - lastFrameTime > FRAME_DELAY) {
            frame = (frame + 1) % 4;
            view.setViewport(new Rectangle2D(
                frame * SPRITE_W,
                direction * SPRITE_H,
                SPRITE_W, SPRITE_H
            ));
            lastFrameTime = now;
        }
    }

    private void setDirection(ImageView view, int newDirection) {
        if (direction != newDirection) {
            direction = newDirection;
            frame = 0;
            view.setViewport(new Rectangle2D(
                0, direction * SPRITE_H, SPRITE_W, SPRITE_H));
        }
    }

    public static void main(String[] args) { launch(); }
}