package com.RPG.TheLastRoar;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Classe principal do jogo "The Last Roar".
 * Responsabilidade: orquestrar os subsistemas (HUD, Pausa, Save, Inimigos, Timers).
 * Lógica de negócio específica fica em cada manager dedicado.
 */
public class App extends javafx.application.Application {

    // ---- Dimensões da tela ----
    private double screenW;
    private double screenH;

    // ---- Elementos visuais principais ----
    private Pane      gameRoot;
    private ImageView playerView;
    private ImageView mapView;
    private StackPane mainLayout;
    private Scene     cenaMestra;

    // ---- Dados do jogador ----
    private Character player;

    // ---- Timers ----
    public AnimationTimer playerMovement;
    public AnimationTimer enemyAI;

    // ---- Animação do jogador ----
    private int    direction      = 0;
    private int    frame          = 0;
    private long   lastFrameTime  = 0;
    private final long   frameDelay     = 200_000_000L;
    private final double speed         = 4;
    private final int    spriteWidth   = 128;
    private final int    spriteHeight  = 128;
    private final double personagemTamanhoTela = 80;
    // ---- Animação dos inimigos ----
    private int  enemyFrame         = 0;
    private long lastEnemyFrameTime = 0;
    private final int enemySpriteWidth  = 128;
    private final int enemySpriteHeight = 128;

    // ---- Teclas de movimento ----
    private static boolean up, down, left, right;

    // ---- Estado de pausa ----
    private boolean isPaused = false;

    // ---- Mapas ----
    private final String[] LISTA_MAPAS = {"mapa_padrao.png", "mapa_padrao2.png", "mapa_padrao3.png"};
    private int indiceMapa = 0;
    private boolean[][] inimigosDerrotados;

    // ---- Subsistemas ----
    private HudManager   hudManager;
    private PauseMenu    pauseMenu;
    private EnemyManager enemyManager;

    // ---- Batalha em curso ----
    private int monstroEmBatalhaIndex = -1;

    // ==========================================
    // UTILITÁRIOS ESTÁTICOS
    // ==========================================

    public static void resetMovement() {
        up = down = left = right = false;
    }

    // ==========================================
    // CICLO DE VIDA JAVAFX
    // ==========================================

    @Override
    public void start(Stage stage) {
        StackPane menuLayout = StartScreen.createLayout(
            () -> iniciarJogo(stage, null),
            () -> iniciarJogo(stage, StartScreen.getUltimoSave()),
            (slotName) -> iniciarJogo(stage, slotName)
        );

        cenaMestra = new Scene(menuLayout, 800, 600);
        stage.setTitle("The Last Roar");
        stage.setScene(cenaMestra);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreen(true);
        stage.show();
    }

    // ==========================================
    // INICIALIZAÇÃO DO JOGO
    // ==========================================

    private void iniciarJogo(Stage stage, String saveFile) {
        try {
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            screenW = screenBounds.getWidth();
            screenH = screenBounds.getHeight();

            // Mapa
            mapView = new ImageView(
                new Image(getClass().getResource("/images/mapa_padrao.png").toExternalForm())
            );

            // Área de jogo
            gameRoot = new Pane();
            gameRoot.setPrefSize(screenW, screenH);

            // Jogador
            player = new Character("Hero", 100, 2,
                new Sword("Madeira", 3, 6, "Comum", 4),
                new Image(getClass().getResource("/images/sprite_personagem.png").toExternalForm()),
                new Image(getClass().getResource("/images/personagem_battle.png").toExternalForm())
            );

            playerView = new ImageView(player.getSprite());
            // O corte (Viewport) usa o tamanho real da imagem (256)
            playerView.setViewport(new Rectangle2D(0, 0, spriteWidth, spriteHeight));
            
            // O tamanho visual (Fit) usa o tamanho que você quer na tela
            playerView.setFitWidth(personagemTamanhoTela);
            playerView.setFitHeight(personagemTamanhoTela);
            
            // Ajuste da posição central para considerar o novo tamanho
            playerView.setX((screenW / 2) - (personagemTamanhoTela / 2));
            playerView.setY(screenH - personagemTamanhoTela - 20);
            gameRoot.getChildren().add(playerView);

            // Inimigos
            inimigosDerrotados = new boolean[LISTA_MAPAS.length][10];
            enemyManager = new EnemyManager(gameRoot, screenW, screenH, inimigosDerrotados);

            // Save ou novo jogo
            // No App.java, dentro de carregarDeJson ou antes de chamá-lo
            if (this.hudManager != null) {
                this.hudManager.atualizar(player);
            } else {
                // Caso o HUD ainda não exista, apenas carregue os dados 
                // e deixe para atualizar o HUD quando ele for criado.
                System.out.println("Aviso: HudManager ainda não inicializado.");
            }

            // Subsistemas de UI
            hudManager = new HudManager(stage);
            hudManager.atualizar(player);

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

            // Layout principal
            mainLayout = new StackPane(
                mapView, gameRoot,
                hudManager.getLayout(),
                pauseMenu.getLayout()
            );
            mainLayout.setStyle("-fx-background-color: black;");
            mapView.fitWidthProperty().bind(stage.widthProperty());
            mapView.fitHeightProperty().bind(stage.heightProperty());

            // Controles de teclado
            cenaMestra.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) togglePause();
                if (!isPaused) {
                    if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP)    up    = true;
                    if (e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN)  down  = true;
                    if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT)  left  = true;
                    if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) right = true;
                }
            });
            cenaMestra.setOnKeyReleased(e -> {
                if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP)    up    = false;
                if (e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN)  down  = false;
                if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT)  left  = false;
                if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) right = false;
            });

            cenaMestra.setRoot(mainLayout);
            iniciarTimers(stage, cenaMestra);

            Platform.runLater(() -> { if (!stage.isFullScreen()) stage.setFullScreen(true); });

            mainLayout.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(800), mainLayout);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            System.err.println("Erro ao iniciar o jogo:");
            e.printStackTrace();
        }
    }

    // ==========================================
    // MENU PRINCIPAL
    // ==========================================

    public void showMainMenu() {
        if (playerMovement != null) playerMovement.stop();
        if (enemyAI        != null) enemyAI.stop();

        isPaused = false;
        resetMovement();

        Stage stage = (Stage) cenaMestra.getWindow();
        StackPane menuLayout = StartScreen.createLayout(
            () -> iniciarJogo(stage, null),
            () -> iniciarJogo(stage, StartScreen.getUltimoSave()),
            (slotName) -> iniciarJogo(stage, slotName)
        );

        cenaMestra.setRoot(menuLayout);
        if (!stage.isFullScreen()) stage.setFullScreen(true);
    }

    // ==========================================
    // PAUSA
    // ==========================================

    private void togglePause() {
        isPaused = !isPaused;

        if (isPaused) {
            playerMovement.stop();
            enemyAI.stop();
            resetMovement();

            pauseMenu.atualizarBotoesLoad(
                SaveManager.existe("save1.json"),
                SaveManager.existe("save2.json"),
                SaveManager.existe("save3.json")
            );
            pauseMenu.setVisible(true);
        } else {
            pauseMenu.setVisible(false);
            playerMovement.start();
            if (!enemyManager.isEmpty()) enemyAI.start();
        }
    }

    // ==========================================
    // SAVE / LOAD (delegam ao SaveManager)
    // ==========================================

    private void salvarSlot(String arquivo) {
        SaveManager.salvar(
            arquivo, indiceMapa,
            playerView.getX(), playerView.getY(),
            player, inimigosDerrotados
        );
        togglePause();
    }

    private void carregarDeJson(String arquivo, boolean isInitialLoad) {
        SaveManager.SaveData data = SaveManager.carregar(arquivo, LISTA_MAPAS.length);
        if (data == null) return;

        indiceMapa = data.mapa;
        System.arraycopy(data.inimigosDerrotados, 0, inimigosDerrotados, 0, inimigosDerrotados.length);

        mapView.setImage(
            new Image(getClass().getResource("/images/" + LISTA_MAPAS[indiceMapa]).toExternalForm())
        );
        enemyManager.configurarParaMapa(indiceMapa);

        playerView.setX(data.posX);
        playerView.setY(data.posY);
        player.setLife(data.vida);
        player.setNivel(data.level);
        player.setCoin(data.ouro);

        if (!isInitialLoad) togglePause();
        hudManager.atualizar(player);
    }

    // ==========================================
    // TROCA DE CENÁRIO
    // ==========================================

    private void trocarCenario(boolean avancar) {
        playerMovement.stop();
        enemyAI.stop();

        FadeTransition ft = new FadeTransition(Duration.millis(300), mainLayout);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            if (avancar && indiceMapa < LISTA_MAPAS.length - 1) {
                indiceMapa++;
                playerView.setY(screenH - spriteHeight - 20);
                playerView.setX((screenW / 2) - (spriteWidth / 2));
            } else if (!avancar && indiceMapa > 0) {
                indiceMapa--;
                playerView.setY(20);
                playerView.setX((screenW / 2) - (spriteWidth / 2));
            } else {
                playerMovement.start();
                if (!enemyManager.isEmpty()) enemyAI.start();
                return;
            }

            mapView.setImage(
                new Image(getClass().getResource("/images/" + LISTA_MAPAS[indiceMapa]).toExternalForm())
            );
            enemyManager.configurarParaMapa(indiceMapa);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainLayout);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setOnFinished(ev -> {
                playerMovement.start();
                if (!enemyManager.isEmpty()) enemyAI.start();
            });
            fadeIn.play();
        });
        ft.play();
    }

    // ==========================================
    // PÓS-BATALHA
    // ==========================================

    public void resumeTimers() {
        resetMovement();

        if (monstroEmBatalhaIndex != -1) {
            Monsters monstro = enemyManager.getMonstro(monstroEmBatalhaIndex);
            if (monstro.getLife() <= 0) {
                enemyManager.removerInimigo(monstroEmBatalhaIndex);
            }
            monstroEmBatalhaIndex = -1;
        }

        FadeTransition ft = new FadeTransition(Duration.millis(500), mainLayout);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        playerMovement.start();
        if (!enemyManager.isEmpty()) enemyAI.start();

        hudManager.atualizar(player);
    }

    // ==========================================
    // TIMERS DE ANIMAÇÃO
    // ==========================================

    private void iniciarTimers(Stage stage, Scene scene) {
        playerMovement = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (up) {
                    if (playerView.getY() <= 5) {
                        if (indiceMapa < LISTA_MAPAS.length - 1) trocarCenario(true);
                        else playerView.setY(5);
                    } else {
                        playerView.setY(playerView.getY() - speed);
                    }
                    setDirection(playerView, 3);
                    animate(playerView);
                } else if (down) {
                    if (playerView.getY() >= screenH - spriteHeight - 5) {
                        if (indiceMapa > 0) trocarCenario(false);
                        else playerView.setY(screenH - spriteHeight - 5);
                    } else {
                        playerView.setY(playerView.getY() + speed);
                    }
                    setDirection(playerView, 0);
                    animate(playerView);
                } else if (left) {
                    if (playerView.getX() > 5) playerView.setX(playerView.getX() - speed);
                    setDirection(playerView, 1);
                    animate(playerView);
                } else if (right) {
                    if (playerView.getX() < screenW - spriteWidth - 5)
                        playerView.setX(playerView.getX() + speed);
                    setDirection(playerView, 2);
                    animate(playerView);
                }
            }
        };
        playerMovement.start();

        enemyAI = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastEnemyFrameTime > frameDelay) {
                    enemyFrame = (enemyFrame + 1) % 4;
                    lastEnemyFrameTime = now;
                }

                int colisao = enemyManager.atualizar(playerView.getX(), playerView.getY(), enemyFrame);
                if (colisao != -1) {
                    playerMovement.stop();
                    enemyAI.stop();

                    double dx = playerView.getX() - enemyManager.getView(colisao).getX();
                    playerView.setX(playerView.getX() + (dx > 0 ? 40 : -40));
                    monstroEmBatalhaIndex = colisao;

                    Monsters monstro = enemyManager.getMonstro(colisao);
                    ImageView ev     = enemyManager.getView(colisao);

                    FadeTransition ft = new FadeTransition(Duration.millis(500), mainLayout);
                    ft.setFromValue(1);
                    ft.setToValue(0);
ft.setOnFinished(e ->
    Battle.startBattle(stage, cenaMestra, player, monstro, playerView, ev, App.this, mainLayout)
);
                    ft.play();
                }
            }
        };
        enemyAI.start();
    }

    // ==========================================
    // ANIMAÇÃO DO SPRITE DO JOGADOR
    // ==========================================

    private void animate(ImageView view) {
        long now = System.nanoTime();
        if (now - lastFrameTime > frameDelay) {
            frame = (frame + 1) % 4;
            view.setViewport(new Rectangle2D(
                frame * spriteWidth,
                direction * spriteHeight,
                spriteWidth, spriteHeight
            ));
            lastFrameTime = now;
        }
    }

    private void setDirection(ImageView view, int newDirection) {
        if (direction != newDirection) {
            direction = newDirection;
            frame = 0;
            view.setViewport(new Rectangle2D(0, direction * spriteHeight, spriteWidth, spriteHeight));
        }
    }

    // ==========================================
    // MAIN
    // ==========================================

    public static void main(String[] args) {
        launch();
    }
}
