package com.RPG.TheLastRoar;

// Importações necessárias para o funcionamento do jogo
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Classe principal do jogo "The Last Roar", um RPG simples em JavaFX.
 * Esta classe gerencia a interface gráfica, movimento do jogador, inimigos,
 * batalhas, save/load e transições entre cenários.
 */
public class App extends javafx.application.Application {

    // ==========================================
    // CAMPOS DA CLASSE (VARIÁVEIS GLOBAIS)
    // ==========================================
    // ==========================================
    // VARIÁVEIS DA HUD (Interface do Jogador)
    // ==========================================

    //HotBar
    private HBox hudLayout;
    private javafx.scene.control.ProgressBar hpBar;
    private javafx.scene.control.ProgressBar xpBar;
    private Label lblHp;
    private Label lblXp;
    private Label lblGold;
    // Timers para animações: movimento do jogador e IA dos inimigos
    public AnimationTimer playerMovement;
    public AnimationTimer enemyAI;

    // Elementos visuais principais
    private Pane gameRoot;        // Container para elementos do jogo
    private ImageView playerView; // Imagem do jogador
    private ImageView mapView;    // Imagem do mapa atual
    private StackPane mainLayout; // Layout principal que empilha mapa, jogo e menu
    private Character player;     // Objeto do jogador com atributos

    private Scene cenaMestra;     // Cena principal da aplicação

    // Dados dos mapas e inimigos
    private final String[] LISTA_MAPAS = {"mapa_padrao.png", "mapa_padrao2.png", "mapa_padrao3.png"};
    private int indiceMapa = 0; // Índice do mapa atual
    private boolean[][] inimigosDerrotados = new boolean[LISTA_MAPAS.length][10]; // Matriz de inimigos derrotados por mapa

    // Listas de inimigos atuais na tela
    private List<Monsters> monstrosAtuais = new ArrayList<>();
    private List<ImageView> inimigosViewsAtuais = new ArrayList<>();
    private int monstroEmBatalhaIndex = -1; // Índice do monstro em batalha (-1 se nenhum)

    // Variáveis para animação do jogador
    private long lastFrameTime = 0;
    private int direction = 0; // Direção atual (0=baixo, 1=esquerda, 2=direita, 3=cima)
    private final long frameDelay = 200_000_000; // Delay entre frames em nanosegundos
    private double speed = 4; // Velocidade de movimento
    private int frame = 0; // Frame atual da animação
    private final int spriteWidth = 64;  // Largura do sprite do jogador
    private final int spriteHeight = 64; // Altura do sprite do jogador

    // Variáveis para animação dos inimigos
    private int enemyFrame = 0;
    private long lastEnemyFrameTime = 0;
    private final int enemySpriteWidth = 128;  // Largura do sprite dos inimigos
    private final int enemySpriteHeight = 128; // Altura do sprite dos inimigos

    // Estados das teclas de movimento (estáticos para acesso global)
    private static boolean up, down, left, right;

    // Dimensões da tela
    private double screenW;
    private double screenH;

    // Estado do jogo: pausado ou não
    private boolean isPaused = false;
    private VBox pauseMenu; // Menu de pausa

    // Botões do menu de pausa para controle dinâmico
    private Button btnLoadSlot1, btnLoadSlot2, btnLoadSlot3, btnLoadMenu;

    // ==========================================
    // MÉTODOS UTILITÁRIOS
    // ==========================================

    /**
     * Reseta os estados das teclas de movimento para falso.
     * Usado quando o jogo é pausado ou após batalhas.
     */
    public static void resetMovement() {
        up = down = left = right = false;
    }


    // ==========================================
    // SISTEMA DE HUD (VISUAL NO TOPO DA TELA)
    // ==========================================

   private Label lblLevel; // Adicione esta variável lá no topo com as outras da HUD

    private void criarHUD() {
        hudLayout = new HBox(15);
        hudLayout.setAlignment(Pos.CENTER_LEFT);
        hudLayout.setStyle("-fx-background-color: rgba(20, 20, 20, 0.8); -fx-background-radius: 10; -fx-border-color: #DAA520; -fx-border-width: 3; -fx-border-radius: 10; -fx-padding: 10px;");
        hudLayout.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);

        ImageView avatar = null;
        try {
            avatar = new ImageView(new Image(getClass().getResource("/images/avatar.png").toExternalForm()));
            avatar.setFitWidth(64);
            avatar.setFitHeight(64);
        } catch (Exception e) {
            avatar = new ImageView();
        }

        VBox statsBox = new VBox(5);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        // --- NOVO: Label de Nível ---
        lblLevel = new Label("Nível: 1");
        lblLevel.setTextFill(Color.CYAN);
        lblLevel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Barra de Vida
        HBox hpBox = new HBox(10);
        lblHp = new Label("HP: 100/100");
        lblHp.setTextFill(Color.WHITE);
        hpBar = new javafx.scene.control.ProgressBar(1.0);
        hpBar.setPrefWidth(150);
        hpBar.setStyle("-fx-accent: #e74c3c;");
        hpBox.getChildren().addAll(lblHp, hpBar);

        // Barra de XP
        HBox xpBox = new HBox(10);
        lblXp = new Label("XP: 0/100");
        lblXp.setTextFill(Color.WHITE);
        xpBar = new javafx.scene.control.ProgressBar(0.0);
        xpBar.setPrefWidth(150);
        xpBar.setStyle("-fx-accent: #2ecc71;");
        xpBox.getChildren().addAll(lblXp, xpBar);

        lblGold = new Label("Ouro: 0");
        lblGold.setTextFill(Color.GOLD);
        lblGold.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Adicionamos o Level no topo da caixa de stats
        statsBox.getChildren().addAll(lblLevel, hpBox, xpBox, lblGold);
        if (avatar.getImage() != null) hudLayout.getChildren().add(avatar);
        hudLayout.getChildren().add(statsBox);

        StackPane.setAlignment(hudLayout, Pos.TOP_LEFT);
        StackPane.setMargin(hudLayout, new javafx.geometry.Insets(20));
    }

public void atualizarHUD() {
        if (player != null) {
            // 1. Atualiza Vida
            int vidaAtual = player.getLife();
            // Idealmente, sua classe Character deveria ter um getMaxLife(), mas se for fixo em 100:
            int vidaMaxima = 100; 
            lblHp.setText("HP: " + vidaAtual + " / " + vidaMaxima);
            hpBar.setProgress((double) vidaAtual / vidaMaxima);

            // 2. Atualiza Nível
            // Descomentei e ajustei. Requer que a classe Character tenha o método getLevel()
            lblLevel.setText("Nível: " + player.getNivel());

            // 3. Atualiza XP
            // Requer que a classe Character tenha o método getXp()
            int xpAtual = player.getXp(); 
            // Se o XP máximo para subir de nível variar, você precisará de um getMaxXp() no Character. 
            // Deixei 100 como padrão por enquanto.
            int xpMaximo = 100; 
            lblXp.setText("XP: " + xpAtual + " / " + xpMaximo);
            xpBar.setProgress((double) xpAtual / xpMaximo);
            
            // 4. Atualiza Ouro
            lblGold.setText("Ouro: " + player.getCoin());
        }
    }
    /**
     * Cria um botão estilizado que pode conter uma imagem e um texto.
     * @param texto Texto do botão
     * @param imagePath Caminho da imagem. Pode ser null se não quiser imagem.
     * @return Botão configurado
     */
    private Button criarBotaoComImagem(String texto, String imagePath) {
        Button btn = new Button(texto);
        
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                Image img = new Image(getClass().getResource(imagePath).toExternalForm());
                ImageView icon = new ImageView(img);
                icon.setFitWidth(24);  // Tamanho do ícone
                icon.setFitHeight(24);
                btn.setGraphic(icon);
            } catch (Exception e) {
                System.out.println("Aviso: Imagem do botão não encontrada: " + imagePath);
            }
        }
        
        btn.setStyle("-fx-font-size: 14px; -fx-padding: 5px 15px; -fx-background-color: #555; -fx-text-fill: white; -fx-cursor: hand;");
        return btn;
    }
    // ==========================================
    // MÉTODOS PRINCIPAIS DA APLICAÇÃO
    // ==========================================

    /**
     * Método principal do JavaFX: inicializa a aplicação.
     * Cria o menu inicial e configura a janela principal.
     */
    @Override
    public void start(Stage stage) {
        // Cria o layout do menu inicial usando a classe StartScreen
        StackPane menuLayout = StartScreen.createLayout(
            () -> iniciarJogo(stage, null),           // Novo jogo
            () -> iniciarJogo(stage, StartScreen.getUltimoSave()), // Continuar último save
            (slotName) -> iniciarJogo(stage, slotName) // Carregar save específico
        );

        // Cria a cena principal com o menu inicial
        cenaMestra = new Scene(menuLayout, 800, 600);

        // Configura a janela
        stage.setTitle("The Last Roar");
        stage.setScene(cenaMestra);
        stage.setFullScreenExitHint(""); // Remove hint de saída
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); // Desabilita ESC para sair
        stage.setFullScreen(true); // Inicia em tela cheia
        stage.show();
    }

    private void iniciarJogo(Stage stage, String saveFile) {
        try {
            // Pega o tamanho da tela do jogador
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            screenW = screenBounds.getWidth();
            screenH = screenBounds.getHeight();

            // Configura o mapa
            mapView = new ImageView(new Image(getClass().getResource("/images/mapa_padrao.png").toExternalForm()));

            // Área onde o jogador e inimigos vão se mover
            gameRoot = new Pane();
            gameRoot.setPrefSize(screenW, screenH);

            // Instancia o jogador (Ajuste os atributos conforme a sua classe Character)
            player = new Character("Hero", 100, 2, new Sword("Madeira", 3, 6, "Comum", 4),
                    new Image(getClass().getResource("/images/12.png").toExternalForm()),
                    new Image(getClass().getResource("/images/guts.png").toExternalForm()));

            // Configura a imagem (sprite) do jogador
            playerView = new ImageView(player.getSprite());
            playerView.setViewport(new Rectangle2D(0, 0, spriteWidth, spriteHeight));
            playerView.setFitWidth(64);
            playerView.setFitHeight(64);

            // Posição inicial do jogador
            playerView.setX((screenW / 2) - (spriteWidth / 2));
            playerView.setY(screenH - spriteHeight - 20);

            gameRoot.getChildren().add(playerView);

            // Carrega o save ou inicia um jogo novo
            if (saveFile != null) {
                carregarDeJson(saveFile, true);
            } else {
                inimigosDerrotados = new boolean[LISTA_MAPAS.length][10];
                indiceMapa = 0;
                configurarInimigosPorMapa(0);
            }

            // Cria os elementos da interface (Menus e HUD)
            criarMenuPausa();
            
            // --- AQUI ENTRA A SUA HUD NOVA ---
            criarHUD();       
            atualizarHUD();   

            // Monta a tela principal juntando tudo. 
            // A ordem importa: mapa no fundo, depois personagens, depois HUD, depois Menu de Pausa por cima de tudo
            mainLayout = new StackPane(mapView, gameRoot, hudLayout, pauseMenu);
            mainLayout.setStyle("-fx-background-color: black;");

            // Faz o mapa esticar para cobrir a tela inteira
            mapView.fitWidthProperty().bind(stage.widthProperty());
            mapView.fitHeightProperty().bind(stage.heightProperty());

            // Controles do teclado (Movimento e Pausa)
            cenaMestra.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    togglePause();
                }

                if (!isPaused) {
                    if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP) up = true;
                    if (e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN) down = true;
                    if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) left = true;
                    if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) right = true;
                }
            });

            cenaMestra.setOnKeyReleased(e -> {
                if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP) up = false;
                if (e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN) down = false;
                if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) left = false;
                if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) right = false;
            });

            // Aplica o layout na cena
            cenaMestra.setRoot(mainLayout);

            // Inicia a lógica de movimento e inimigos
            iniciarTimers(stage, cenaMestra);

            // Garante que o jogo fique em tela cheia
            Platform.runLater(() -> {
                if (!stage.isFullScreen()) {
                    stage.setFullScreen(true);
                }
            });

            // Efeito de transição suave (Fade in) ao entrar no jogo
            mainLayout.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(800), mainLayout);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            System.err.println("Erro ao iniciar o jogo:");
            e.printStackTrace();
        }
    }

    /**
     * Retorna para o Menu Principal (StartScreen).
     * Interrompe os timers atuais e recria a tela inicial.
     */
    public void showMainMenu() {
        // 1. Para os timers para o jogo parar de processar no fundo
        if (playerMovement != null) playerMovement.stop();
        if (enemyAI != null) enemyAI.stop();
        
        isPaused = false;
        resetMovement();

        // 2. Recupera o Stage (Janela principal)
        Stage stage = (Stage) cenaMestra.getWindow();

        // 3. Recria o layout do Menu Inicial (mesma lógica do método start)
        StackPane menuLayout = StartScreen.createLayout(
            () -> iniciarJogo(stage, null),
            () -> iniciarJogo(stage, StartScreen.getUltimoSave()),
            (slotName) -> iniciarJogo(stage, slotName)
        );

        // 4. Joga o Menu na tela
        cenaMestra.setRoot(menuLayout);

        // 5. Garante que fique em tela cheia
        if (!stage.isFullScreen()) {
            stage.setFullScreen(true);
        }
    }

    // ==========================================
    // MÉTODOS DO MENU DE PAUSA
    // ==========================================

    /**
     * Cria o menu de pausa com opções de salvar, carregar e sair.
     */
   private void criarMenuPausa() {
        pauseMenu = new VBox(20);
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");
        pauseMenu.setVisible(false);

        Label title = new Label("JOGO PAUSADO");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));

        // --- BOTÃO: VOLTAR AO JOGO ---
        Button btnResume = criarBotaoComImagem("Voltar ao Jogo", "/images/resume_icon.png");
        // Sobrescrevemos o estilo padrão do método para manter a cor original e o tamanho maior
        btnResume.setStyle("-fx-font-size: 20px; -fx-padding: 10px 20px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
        btnResume.setOnAction(e -> togglePause());

        // --- SUBMENU: SLOTS DE SALVAR ---
        HBox boxSalvar = new HBox(10);
        boxSalvar.setAlignment(Pos.CENTER);
        boxSalvar.setVisible(false);
        
        Button s1 = criarBotaoComImagem("Salvar Slot 1", "/images/save_icon.png");
        s1.setOnAction(e -> salvarEmJson("save1.json"));
        Button s2 = criarBotaoComImagem("Salvar Slot 2", "/images/save_icon.png");
        s2.setOnAction(e -> salvarEmJson("save2.json"));
        Button s3 = criarBotaoComImagem("Salvar Slot 3", "/images/save_icon.png");
        s3.setOnAction(e -> salvarEmJson("save3.json"));
        boxSalvar.getChildren().addAll(s1, s2, s3);

        // --- BOTÃO: SALVAR JOGO (Abre os slots) ---
        Button btnSave = criarBotaoComImagem("Salvar Jogo", "/images/save_menu_icon.png");
        btnSave.setStyle("-fx-font-size: 20px; -fx-padding: 10px 20px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
        btnSave.setOnAction(e -> boxSalvar.setVisible(!boxSalvar.isVisible()));

        // --- SUBMENU: SLOTS DE CARREGAR ---
        HBox boxCarregar = new HBox(10);
        boxCarregar.setAlignment(Pos.CENTER);
        boxCarregar.setVisible(false);
        
        btnLoadSlot1 = criarBotaoComImagem("Carregar Slot 1", "/images/load_icon.png");
        btnLoadSlot1.setOnAction(e -> carregarDeJson("save1.json", false));
        btnLoadSlot2 = criarBotaoComImagem("Carregar Slot 2", "/images/load_icon.png");
        btnLoadSlot2.setOnAction(e -> carregarDeJson("save2.json", false));
        btnLoadSlot3 = criarBotaoComImagem("Carregar Slot 3", "/images/load_icon.png");
        btnLoadSlot3.setOnAction(e -> carregarDeJson("save3.json", false));
        boxCarregar.getChildren().addAll(btnLoadSlot1, btnLoadSlot2, btnLoadSlot3);

        // --- BOTÃO: CARREGAR JOGO (Abre os slots) ---
        btnLoadMenu = criarBotaoComImagem("Carregar Jogo", "/images/load_menu_icon.png");
        btnLoadMenu.setStyle("-fx-font-size: 20px; -fx-padding: 10px 20px; -fx-background-color: #FF9800; -fx-text-fill: white; -fx-cursor: hand;");
        btnLoadMenu.setOnAction(e -> boxCarregar.setVisible(!boxCarregar.isVisible()));

        // --- BOTÃO: SAIR PARA O MENU ---
        Button btnExit = criarBotaoComImagem("Sair para o Menu", "/images/air.png");
        btnExit.setStyle("-fx-font-size: 20px; -fx-padding: 10px 20px; -fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
        btnExit.setOnAction(e -> {
            togglePause();
            showMainMenu();
        });

        // Adiciona tudo na tela
        pauseMenu.getChildren().addAll(title, btnResume, btnSave, boxSalvar, btnLoadMenu, boxCarregar, btnExit);
    }
    // ==========================================
    // SISTEMA DE SAVE / LOAD
    // ==========================================

    /**
     * Salva o estado atual do jogo em um arquivo JSON.
     * @param arquivo Nome do arquivo para salvar
     */
    private void salvarEmJson(String arquivo) {
        try {
            StringBuilder sbInimigos = new StringBuilder();
            for (int i = 0; i < inimigosDerrotados.length; i++) {
                for (int j = 0; j < inimigosDerrotados[i].length; j++) {
                    sbInimigos.append(inimigosDerrotados[i][j]);
                    if (j < inimigosDerrotados[i].length - 1) sbInimigos.append(",");
                }
                if (i < inimigosDerrotados.length - 1) sbInimigos.append(";");
            }

            // --- AQUI VOCÊ ADICIONA AS COISAS NOVAS PARA SALVAR ---
            // Exemplo: pegando os dados do player (descomente e ajuste os métodos)
            int levelPlayer = 1; // player.getLevel();
            int ouroPlayer = 50; // player.getGold();

            String json = "{\n" +
                          "  \"mapaAtual\": " + indiceMapa + ",\n" +
                          "  \"posicaoX\": " + playerView.getX() + ",\n" +
                          "  \"posicaoY\": " + playerView.getY() + ",\n" +
                          "  \"vidaPlayer\": " + player.getLife() + ",\n" +
                          "  \"levelPlayer\": " + levelPlayer + ",\n" +
                          "  \"ouroPlayer\": " + ouroPlayer + ",\n" +
                          "  \"inimigosMortos\": \"" + sbInimigos.toString() + "\"\n" +
                          "}";

            Files.write(Paths.get(arquivo), json.getBytes());
            System.out.println("Jogo Salvo com Sucesso no " + arquivo + "!");

            togglePause();

        } catch (IOException e) {
            System.err.println("Erro ao salvar o jogo: " + e.getMessage());
        }
    }


    /**
     * Carrega o estado do jogo de um arquivo JSON.
     * @param arquivo Nome do arquivo para carregar
     * @param isInitialLoad Se é carregamento inicial (não pausa o jogo)
     */
    private void carregarDeJson(String arquivo, boolean isInitialLoad) {
        try {
            if (!Files.exists(Paths.get(arquivo))) {
                System.out.println("Arquivo " + arquivo + " não encontrado!");
                return;
            }

            String conteudoJson = new String(Files.readAllBytes(Paths.get(arquivo)));
            String textoLimpo = conteudoJson.replaceAll("[\\{\\}\"\\s]", "");

            int mapaSalvo = 0;
            double posX = 0;
            double posY = 0;
            int vidaSalva = 100;
            
            // Variáveis para receber os dados novos do JSON
            int levelSalvo = 1;
            int ouroSalvo = 0;
            String inimigosSalvos = "";

            String[] atributos = textoLimpo.split("[:,]");
            for (int i = 0; i < atributos.length; i++) {
                if (atributos[i].equals("mapaAtual")) mapaSalvo = Integer.parseInt(atributos[i+1]);
                if (atributos[i].equals("posicaoX")) posX = Double.parseDouble(atributos[i+1]);
                if (atributos[i].equals("posicaoY")) posY = Double.parseDouble(atributos[i+1]);
                if (atributos[i].equals("vidaPlayer")) vidaSalva = Integer.parseInt(atributos[i+1]);
                
                // --- LENDO AS COISAS NOVAS DO JSON ---
                if (atributos[i].equals("levelPlayer")) levelSalvo = Integer.parseInt(atributos[i+1]);
                if (atributos[i].equals("ouroPlayer")) ouroSalvo = Integer.parseInt(atributos[i+1]);
                
                if (atributos[i].equals("inimigosMortos")) {
                    inimigosSalvos = textoLimpo.substring(textoLimpo.indexOf("inimigosMortos:") + 15);
                }
            }

            if (!inimigosSalvos.isEmpty()) {
                String[] mapasString = inimigosSalvos.split(";");
                for (int i = 0; i < mapasString.length; i++) {
                    String[] inims = mapasString[i].split(",");
                    for (int j = 0; j < inims.length; j++) {
                        inimigosDerrotados[i][j] = Boolean.parseBoolean(inims[j]);
                    }
                }
            }

            indiceMapa = mapaSalvo;
            mapView.setImage(new Image(getClass().getResource("/images/" + LISTA_MAPAS[indiceMapa]).toExternalForm()));
            configurarInimigosPorMapa(indiceMapa);

            playerView.setX(posX);
            playerView.setY(posY);
            player.setLife(vidaSalva);
            
            // --- APLICANDO OS ATRIBUTOS NOVOS NO PLAYER ---
            // Descomente e ajuste de acordo com os métodos da sua classe Character:
            // player.setLevel(levelSalvo);
            // player.setGold(ouroSalvo);

            System.out.println("Jogo Carregado do " + arquivo + "!");

            if (!isInitialLoad) {
                togglePause();
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar o jogo: " + e.getMessage());
        }
        atualizarHUD();
    }

    // ==========================================

    // CONTROLE DE PAUSA
    // ==========================================

    /**
     * Alterna entre pausar e despausar o jogo.
     * Quando pausado, mostra o menu e para animações.
     * Quando despausado, esconde menu e retoma animações.
     */
    private void togglePause() {
        isPaused = !isPaused;

        if (isPaused) {
            playerMovement.stop();
            enemyAI.stop();
            resetMovement();

            // Checa se os arquivos existem paraahabilitar os botões de Load
            boolean f1 = Files.exists(Paths.get("save1.json"));
            boolean f2 = Files.exists(Paths.get("save2.json"));
            boolean f3 = Files.exists(Paths.get("save3.json"));

            btnLoadSlot1.setDisable(!f1);
            btnLoadSlot1.setText(f1 ? "Carregar Slot 1" : "Slot 1 (Vazio)");
            btnLoadSlot2.setDisable(!f2);
            btnLoadSlot2.setText(f2 ? "Carregar Slot 2" : "Slot 2 (Vazio)");
            btnLoadSlot3.setDisable(!f3);
            btnLoadSlot3.setText(f3 ? "Carregar Slot 3" : "Slot 3 (Vazio)");

            btnLoadMenu.setDisable(!f1 && !f2 && !f3);
            btnLoadMenu.setText((!f1 && !f2 && !f3) ? "Carregar Jogo (Vazio)" : "Carregar Jogo");

            pauseMenu.setVisible(true);
        } else {
            pauseMenu.setVisible(false);
            playerMovement.start();
            if (!inimigosViewsAtuais.isEmpty()) enemyAI.start();
        }
    }
    // ==========================================
    // SISTEMA DE ANIMAÇÃO E MOVIMENTO
    // ==========================================

    /**
     * Inicia os timers de animação para movimento do jogador e IA dos inimigos.
     * @param stage Janela principal
     * @param scene Cena atual
     */
    private void iniciarTimers(Stage stage, Scene scene) {
        // Timer para movimento do jogador
        playerMovement = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Movimento para cima
                if (up) {
                    if (playerView.getY() <= 5) {
                        // Tenta avançar para próximo mapa
                        if (indiceMapa < LISTA_MAPAS.length - 1) trocarCenario(true);
                        else playerView.setY(5); // Limite da tela
                    } else {
                        playerView.setY(playerView.getY() - speed);
                    }
                    setDirection(playerView, 3); // Direção cima
                    animate(playerView);
                }
                // Movimento para baixo
                else if (down) {
                    if (playerView.getY() >= screenH - spriteHeight - 5) {
                        // Tenta voltar para mapa anterior
                        if (indiceMapa > 0) trocarCenario(false);
                        else playerView.setY(screenH - spriteHeight - 5); // Limite da tela
                    } else {
                        playerView.setY(playerView.getY() + speed);
                    }
                    setDirection(playerView, 0); // Direção baixo
                    animate(playerView);
                }
                // Movimento para esquerda
                else if (left) {
                    if (playerView.getX() > 5) {
                        playerView.setX(playerView.getX() - speed);
                    }
                    setDirection(playerView, 1); // Direção esquerda
                    animate(playerView);
                }
                // Movimento para direita
                else if (right) {
                    if (playerView.getX() < screenW - spriteWidth - 5) {
                        playerView.setX(playerView.getX() + speed);
                    }
                    setDirection(playerView, 2); // Direção direita
                    animate(playerView);
                }
            }
        };
        playerMovement.start();

        // Timer para IA dos inimigos
        enemyAI = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Animação dos inimigos (muda frame a cada delay)
                if (now - lastEnemyFrameTime > frameDelay) {
                    enemyFrame = (enemyFrame + 1) % 4;
                    lastEnemyFrameTime = now;
                }

                // Processa cada inimigo
                for (int i = 0; i < inimigosViewsAtuais.size(); i++) {
                    ImageView ev = inimigosViewsAtuais.get(i);
                    Monsters monstro = monstrosAtuais.get(i);

                    // Movimento horizontal do inimigo
                    double dirMove = (double) ev.getProperties().get("dirMove");
                    ev.setX(ev.getX() + dirMove);

                    // Define direção da animação baseada no movimento
                    int eDir = (dirMove > 0) ? 2 : 1; // 2=direita, 1=esquerda
                    ev.setViewport(new Rectangle2D(enemyFrame * enemySpriteWidth, eDir * enemySpriteHeight, enemySpriteWidth, enemySpriteHeight));

                    // Inverte direção nas bordas da tela
                    if (ev.getX() > screenW - 150) ev.getProperties().put("dirMove", -1.0);
                    if (ev.getX() < 50) ev.getProperties().put("dirMove", 1.0);

                    // Verifica colisão com jogador para iniciar batalha
                    double dx = playerView.getX() - ev.getX();
                    double dy = playerView.getY() - ev.getY();
                    double distance = Math.sqrt(dx*dx + dy*dy);

                    if (distance < 50) {
                        // Inicia batalha
                        playerMovement.stop();
                        enemyAI.stop();
                        playerView.setX(playerView.getX() + (dx > 0 ? 40 : -40)); // Afasta jogador

                        monstroEmBatalhaIndex = i;

                        // Transição para tela de batalha
                        FadeTransition ft = new FadeTransition(Duration.millis(500), mainLayout);
                        ft.setFromValue(1);
                        ft.setToValue(0);
                        ft.setOnFinished(evTransition -> Battle.startBattle(stage, scene, player, monstro, playerView, ev, App.this, mainLayout));
                        ft.play();
                        return;
                    }
                }
            }
        };
        enemyAI.start();
    }

    // ==========================================
    // SISTEMA DE CENÁRIOS E INIMIGOS
    // ==========================================

    /**
     * Troca para o próximo ou anterior cenário (mapa).
     * @param avancar true para avançar, false para voltar
     */
    private void trocarCenario(boolean avancar) {
        playerMovement.stop();
        enemyAI.stop();

        // Animação de fade-out
        FadeTransition ft = new FadeTransition(Duration.millis(300), mainLayout);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);

        ft.setOnFinished(e -> {
            if (avancar && indiceMapa < LISTA_MAPAS.length - 1) {
                indiceMapa++;
                playerView.setY(screenH - spriteHeight - 20); // Posição inferior
                playerView.setX((screenW / 2) - (spriteWidth / 2)); // Centro horizontal
            } else if (!avancar && indiceMapa > 0) {
                indiceMapa--;
                playerView.setY(20); // Posição superior
                playerView.setX((screenW / 2) - (spriteWidth / 2)); // Centro horizontal
            } else {
                // Não pode trocar, retoma movimento
                playerMovement.start();
                if (!inimigosViewsAtuais.isEmpty()) enemyAI.start();
                return;
            }

            // Carrega novo mapa
            mapView.setImage(new Image(getClass().getResource("/images/" + LISTA_MAPAS[indiceMapa]).toExternalForm()));

            // Configura inimigos do novo mapa
            configurarInimigosPorMapa(indiceMapa);

            // Animação de fade-in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainLayout);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setOnFinished(ev -> {
                playerMovement.start();
                if (!inimigosViewsAtuais.isEmpty()) enemyAI.start();
            });
            fadeIn.play();
        });
        ft.play();
    }

    /**
     * Configura os inimigos para um mapa específico.
     * @param mapa Índice do mapa
     */
    private void configurarInimigosPorMapa(int mapa) {
        // Remove inimigos atuais
        gameRoot.getChildren().removeAll(inimigosViewsAtuais);
        monstrosAtuais.clear();
        inimigosViewsAtuais.clear();

        // Adiciona inimigos baseados no mapa
        if (mapa == 0) {
            adicionarInimigo(new Goblin(), 0, screenW * 0.3, screenH * 0.1);
            adicionarInimigo(new Goblin(), 1, screenW * 0.7, screenH * 0.2);
        }
        else if (mapa == 1) {
            adicionarInimigo(new GoblinExp(), 0, screenW * 0.5, screenH * 0.1);
            adicionarInimigo(new Goblin(), 1, screenW * 0.2, screenH * 0.15);
        }
        else if (mapa == 2) {
            adicionarInimigo(new GoblinBoss(), 0, screenW * 0.5, screenH * 0.1);
        }
    }

    /**
     * Adiciona um inimigo ao mapa atual se não foi derrotado.
     * @param monstro Objeto do monstro
     * @param idUnico ID único do inimigo no mapa
     * @param startX Posição X inicial
     * @param startY Posição Y inicial
     */
    private void adicionarInimigo(Monsters monstro, int idUnico, double startX, double startY) {
        if (inimigosDerrotados[indiceMapa][idUnico]) return; // Já derrotado

        // Carrega imagem do monstro
        String path = monstro.getImagePath();
        if (!path.startsWith("/")) path = "/" + path;

        java.net.URL imageUrl = getClass().getResource(path);

        if (imageUrl == null) {
            System.err.println("ERRO FATAL: Imagem não encontrada no caminho: " + path);
            return;
        }

        // Cria ImageView do inimigo
        ImageView view = new ImageView(new Image(imageUrl.toExternalForm()));
        view.setViewport(new Rectangle2D(0, 0, enemySpriteWidth, enemySpriteHeight));
        view.setFitWidth(80);
        view.setFitHeight(80);
        view.setX(startX);
        view.setY(startY);

        // Propriedades para identificação e movimento
        view.getProperties().put("idNoMapa", idUnico);
        view.getProperties().put("dirMove", 1.0); // Movimento inicial para direita

        // Adiciona às listas
        monstrosAtuais.add(monstro);
        inimigosViewsAtuais.add(view);
        gameRoot.getChildren().add(view);
    }

    // ==========================================
    // CONTROLE DE BATALHA
    // ==========================================

    /**
     * Retoma os timers após uma batalha.
     * Remove inimigos derrotados e reinicia movimento.
     */
    public void resumeTimers() {
        resetMovement();

        if (monstroEmBatalhaIndex != -1) {
            Monsters monstroLutado = monstrosAtuais.get(monstroEmBatalhaIndex);
            ImageView viewLutada = inimigosViewsAtuais.get(monstroEmBatalhaIndex);

            if (monstroLutado.getLife() <= 0) {
                // Inimigo derrotado: marca como morto e remove da tela
                int idNoMapa = (int) viewLutada.getProperties().get("idNoMapa");
                inimigosDerrotados[indiceMapa][idNoMapa] = true;

                gameRoot.getChildren().remove(viewLutada);
                inimigosViewsAtuais.remove(monstroEmBatalhaIndex);
                monstrosAtuais.remove(monstroEmBatalhaIndex);
            }
            monstroEmBatalhaIndex = -1;
        }

        // Animação de fade-in de volta ao jogo
        FadeTransition ft = new FadeTransition(Duration.millis(500), mainLayout);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        playerMovement.start();
        if (!inimigosViewsAtuais.isEmpty()) enemyAI.start();

        atualizarHUD();
    }

    // ==========================================
    // ANIMAÇÕES
    // ==========================================

    /**
     * Anima um sprite mudando o frame atual.
     * @param view ImageView a animar
     */
    private void animate(ImageView view) {
        long now = System.nanoTime();
        if (now - lastFrameTime > frameDelay) {
            frame = (frame + 1) % 4; // Cicla entre 4 frames
            view.setViewport(new Rectangle2D(frame * spriteWidth, direction * spriteHeight, spriteWidth, spriteHeight));
            lastFrameTime = now;
        }
    }

    /**
     * Define a direção da animação do sprite.
     * @param view ImageView a alterar
     * @param newDirection Nova direção (0=baixo, 1=esquerda, 2=direita, 3=cima)
     */
    private void setDirection(ImageView view, int newDirection) {
        if (direction != newDirection) {
            direction = newDirection;
            frame = 0; // Reseta frame ao mudar direção
            view.setViewport(new Rectangle2D(0, direction * spriteHeight, spriteWidth, spriteHeight));
        }
    }

    // ==========================================
    // MÉTODO MAIN
    // ==========================================

    /**
     * Método main: ponto de entrada da aplicação JavaFX.
     * @param args Argumentos da linha de comando
     */
    public static void main(String[] args) {
        launch(); // Inicia a aplicação JavaFX
    }
}