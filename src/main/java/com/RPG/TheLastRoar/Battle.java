package com.RPG.TheLastRoar;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Battle {
    private static boolean inBattle = false;

    public static void startBattle(Stage stage, Scene mapScene, Character playerChar, Monsters monstro, 
                                   ImageView playerView, ImageView enemyView, App app, StackPane originalLayout) {
        if(inBattle) return;
        inBattle = true;
        
        // --- FUNDO E SPRITES ---
        ImageView bg = new ImageView(new Image(Battle.class.getResource("/images/battlebg2.png").toExternalForm()));
        bg.fitWidthProperty().bind(stage.widthProperty());
        bg.fitHeightProperty().bind(stage.heightProperty());

        ImageView pImg = new ImageView(playerChar.getBattleSprite());
        pImg.setFitWidth(200); pImg.setPreserveRatio(true);
        
        ImageView eImg = new ImageView(new Image(Battle.class.getResource(monstro.getBattleImagePath()).toExternalForm()));
        eImg.setFitWidth(200); eImg.setPreserveRatio(true);

        // --- PAINÉIS DE STATUS ---
        // 1. Status do Monstro (APENAS Vida e Nome, sem XP e sem Level)
        VBox opponentStatus = createMonsterStatusBox(
            monstro.getName(), 
            monstro.getLife(), 
            monstro.getMaxLife() 
        );
        StackPane.setMargin(opponentStatus, new Insets(50, 0, 0, 50));
        StackPane.setAlignment(opponentStatus, Pos.TOP_LEFT);

        // 2. Status do Jogador (Com Vida, Level e XP)
        VBox playerStatus = createPlayerStatusBox(
            playerChar.getName(), 
            playerChar.getNivel(), 
            playerChar.getLife(), 
            playerChar.getMaxLife(), 
            playerChar.getXp(),      
            playerChar.getMaxXp()    
        );
        StackPane.setMargin(playerStatus, new Insets(0, 50, 50, 0)); 
        StackPane.setAlignment(playerStatus, Pos.BOTTOM_RIGHT);

        // --- POSICIONAMENTO DOS LUTADORES ---
        VBox pSpriteBox = new VBox(pImg); pSpriteBox.setAlignment(Pos.BOTTOM_LEFT);
        VBox eSpriteBox = new VBox(eImg); eSpriteBox.setAlignment(Pos.TOP_RIGHT);
        
        pSpriteBox.setTranslateX(150); pSpriteBox.setTranslateY(-100);
        eSpriteBox.setTranslateX(-150); eSpriteBox.setTranslateY(150);

        StackPane arenaArea = new StackPane(pSpriteBox, eSpriteBox, opponentStatus, playerStatus);

        // --- MENU INFERIOR (AÇÕES) ---
        HBox bottomMenu = new HBox(15);
        bottomMenu.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 10; -fx-border-color: #555; -fx-border-width: 4 0 0 0;");
        bottomMenu.setPrefHeight(150);
        bottomMenu.setMinHeight(150); 
        bottomMenu.setAlignment(Pos.CENTER);

        Label promptText = new Label("O que " + playerChar.getName() + "\nvai fazer?");
        promptText.setStyle("-fx-background-color: white; -fx-border-color: #444; -fx-border-width: 5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15 30; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI', sans-serif;");
        promptText.setMaxHeight(Double.MAX_VALUE);
        promptText.setPrefWidth(500);
        HBox.setHgrow(promptText, Priority.ALWAYS);

        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(10);
        buttonGrid.setVgap(10);
        buttonGrid.setStyle("-fx-background-color: white; -fx-border-color: #444; -fx-border-width: 5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15;");
        buttonGrid.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // --- ESTADO 1: MENU PRINCIPAL ---
        Button fightBtn = createPokeButton("LUTAR", "#ff8c94");
        Button bagBtn = createPokeButton("MOCHILA", "#fcd059");
        Button pokemonBtn = createPokeButton("HERÓIS", "#85d685");
        Button runBtn = createPokeButton("FUGIR", "#7aa9f5");

        bagBtn.setDisable(true);
        pokemonBtn.setDisable(true);

        buttonGrid.add(fightBtn, 0, 0);
        buttonGrid.add(bagBtn, 1, 0);
        buttonGrid.add(pokemonBtn, 0, 1);
        buttonGrid.add(runBtn, 1, 1);

        bottomMenu.getChildren().addAll(promptText, buttonGrid);

        // --- MONTAGEM FINAL DO LAYOUT ---
        BorderPane uiLayer = new BorderPane();
        uiLayer.setCenter(arenaArea);
        uiLayer.setBottom(bottomMenu);
        
        StackPane root = new StackPane(bg, uiLayer);
        root.setOpacity(0);

        mapScene.setRoot(root);
        if (!stage.isFullScreen()) stage.setFullScreen(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
        fadeIn.setToValue(1);
        fadeIn.play();

        // --- LÓGICA DE EVENTOS ---

        // FUGIR
        runBtn.setOnAction(e -> {
            if(playerChar.leave()) {
                promptText.setText("Fugiu com sucesso!");
                PauseTransition pause = new PauseTransition(Duration.millis(800));
                pause.setOnFinished(ev -> exitBattle(stage, mapScene, app, root, originalLayout));
                pause.play();
            } else {
                promptText.setText("Não conseguiu fugir!");
                buttonGrid.setDisable(true);
                // Turno do monstro porque você falhou em fugir
                PauseTransition pause = new PauseTransition(Duration.millis(1000));
                pause.setOnFinished(ev -> enemyTurn(playerChar, monstro, playerStatus, pImg, root, app, buttonGrid));
                pause.play();
            }
        });

        fightBtn.setOnAction(e -> {
            promptText.setText("Escolha seu ataque!");
            buttonGrid.getChildren().clear();
            
            // --- ESTADO 2: MENU DE ATAQUE ---
            Button attackBtn = createPokeButton("ATACAR", "#ff8c94");
            Button backBtn = createPokeButton("VOLTAR", "#b3b3b3"); 
            
            buttonGrid.add(attackBtn, 0, 0);
            buttonGrid.add(backBtn, 1, 0);

            // VOLTAR
            backBtn.setOnAction(evBack -> {
                promptText.setText("O que " + playerChar.getName() + "\nvai fazer?");
                buttonGrid.getChildren().clear();
                buttonGrid.add(fightBtn, 0, 0);
                buttonGrid.add(bagBtn, 1, 0);
                buttonGrid.add(pokemonBtn, 0, 1);
                buttonGrid.add(runBtn, 1, 1);
            });

            // ATACAR
            attackBtn.setOnAction(attackEvent -> {
                buttonGrid.setDisable(true); 
                
                playAttackAnimation(pImg, eImg, () -> {
                    // Usando o método real de ataque da classe Character!
                    int dano = playerChar.attack(monstro); 
                    promptText.setText(playerChar.getName() + " causou " + dano + " de dano!");
                    
                    updateHpBar(opponentStatus, monstro.getLife(), monstro.getMaxLife(), false);
                    
                    if(monstro.getLife() <= 0) {
                        promptText.setText(monstro.getName() + " foi derrotado!");
                        
                        // DA XP AO JOGADOR! (Exemplo: 30 de XP)
                        playerChar.earnXp(30); 
                        
                        PauseTransition winPause = new PauseTransition(Duration.millis(1500));
                        winPause.setOnFinished(ev -> exitBattle(stage, mapScene, app, root, originalLayout));
                        winPause.play();
                    } else {
                        // Turno do inimigo
                        PauseTransition pause = new PauseTransition(Duration.millis(1200));
                        pause.setOnFinished(ev -> enemyTurn(playerChar, monstro, playerStatus, pImg, root, app, buttonGrid));
                        pause.play();
                    }
                });
            });
        });
    }

    // Método extraído para o turno do inimigo para organizar o código
    private static void enemyTurn(Character playerChar, Monsters monstro, VBox playerStatus, ImageView pImg, StackPane root, App app, GridPane buttonGrid) {
        // Exemplo: Monstro bate com dano fixo (pode trocar depois para monstro.attack())
        playerChar.setLife(playerChar.getLife() - 15); 
        updateHpBar(playerStatus, playerChar.getLife(), playerChar.getMaxLife(), true);
        playShakeAnimation(pImg);
        
        if (playerChar.getLife() <= 0) {
            triggerGameOver(root, app);
        } else {
            buttonGrid.setDisable(false); 
        }
    }

    // ==========================================
    // TELA DE GAME OVER
    // ==========================================
    private static void triggerGameOver(StackPane rootNode, App app) {
        StackPane gameOverScreen = new StackPane();
        gameOverScreen.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);"); 
        gameOverScreen.setOpacity(0); 

        Label deathText = new Label("Se fudeu");
        deathText.setStyle("-fx-text-fill: #ff0000; -fx-font-size: 100px; -fx-font-weight: bold; -fx-font-family: 'Impact', 'Segoe UI', sans-serif; -fx-effect: dropshadow(gaussian, rgba(255,0,0,0.5), 20, 0.5, 0, 0);");

        Button menuBtn = new Button("Voltar para o Menu");
        String btnStyle = "-fx-background-color: #222; -fx-text-fill: white; -fx-font-size: 20px; -fx-border-color: #ff0000; -fx-border-width: 2; -fx-cursor: hand; -fx-padding: 10 20;";
        String btnHoverStyle = "-fx-background-color: #ff0000; -fx-text-fill: white; -fx-font-size: 20px; -fx-border-color: #ff0000; -fx-border-width: 2; -fx-cursor: hand; -fx-padding: 10 20;";
        
        menuBtn.setStyle(btnStyle);
        menuBtn.setOnMouseEntered(e -> menuBtn.setStyle(btnHoverStyle));
        menuBtn.setOnMouseExited(e -> menuBtn.setStyle(btnStyle));

        menuBtn.setOnAction(e -> {
            inBattle = false; 
            app.showMainMenu(); 
        });

        StackPane.setAlignment(deathText, Pos.CENTER);
        StackPane.setAlignment(menuBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(menuBtn, new Insets(0, 50, 50, 0));

        gameOverScreen.getChildren().addAll(deathText, menuBtn);
        rootNode.getChildren().add(gameOverScreen); 

        FadeTransition fadeIn = new FadeTransition(Duration.millis(1500), gameOverScreen);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    // ==========================================
    // ESTILIZAÇÃO UI (AGORA DIVIDIDA PARA PLAYER E MONSTRO)
    // ==========================================

    private static VBox createPlayerStatusBox(String name, int level, int currentHp, int maxHp, int currentXp, int maxXp) {
        VBox box = buildBaseStatusBox(name, "Lv " + level, currentHp, maxHp, 130);
        
        double displayHp = Math.max(0, currentHp);
        Label hpNumbers = new Label((int)displayHp + " / " + maxHp);
        hpNumbers.setId("hpText");
        hpNumbers.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        HBox bottomRow = new HBox(hpNumbers);
        bottomRow.setAlignment(Pos.CENTER_RIGHT);
        box.getChildren().add(bottomRow);

        // Adicionando a barra de XP exclusiva do player
        HBox xpRow = new HBox(10);
        xpRow.setAlignment(Pos.CENTER_RIGHT);
        Label xpLabel = new Label("XP");
        xpLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4da6ff; -fx-background-color: #333; -fx-padding: 0 5; -fx-background-radius: 3;");
        
        ProgressBar xpBar = new ProgressBar((double) currentXp / Math.max(1, maxXp)); 
        xpBar.setId("xpBar");
        xpBar.setPrefWidth(200);
        xpBar.setPrefHeight(10);
        xpBar.setStyle("-fx-accent: #4da6ff; -fx-control-inner-background: #444; -fx-box-border: transparent; -fx-background-radius: 5;");
        
        xpRow.getChildren().addAll(xpLabel, xpBar);
        box.getChildren().add(xpRow);

        return box;
    }

    private static VBox createMonsterStatusBox(String name, int currentHp, int maxHp) {
        // Monstro usa altura 80 e não exibe Level nem XP
        return buildBaseStatusBox(name, "", currentHp, maxHp, 80);
    }

    private static VBox buildBaseStatusBox(String name, String levelString, int currentHp, int maxHp, int height) {
        VBox box = new VBox(5);
        box.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #333; -fx-border-width: 4; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 20;");
        box.setMaxWidth(300);
        box.setMaxHeight(height);

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label lvlLbl = new Label(levelString);
        lvlLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        topRow.getChildren().addAll(nameLbl, spacer, lvlLbl);

        HBox hpRow = new HBox(10);
        hpRow.setAlignment(Pos.CENTER_RIGHT);
        Label hpLabel = new Label("HP");
        hpLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e6b800; -fx-background-color: #333; -fx-padding: 0 5; -fx-background-radius: 3;");
        
        double displayHp = Math.max(0, currentHp); 
        ProgressBar hpBar = new ProgressBar(displayHp / maxHp);
        hpBar.setId("hpBar");
        hpBar.setPrefWidth(200);
        hpBar.setPrefHeight(15);
        
        String hpBarColor = (displayHp / maxHp) <= 0.2 ? "#ff3333" : "#48e85c";
        hpBar.setStyle("-fx-accent: " + hpBarColor + "; -fx-control-inner-background: #444; -fx-box-border: transparent; -fx-background-radius: 5;");
        
        hpRow.getChildren().addAll(hpLabel, hpBar);
        box.getChildren().addAll(topRow, hpRow);

        return box;
    }

    private static Button createPokeButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(160, 50);
        
        String baseStyle = "-fx-background-color: " + color + "; " +
                           "-fx-text-fill: white; " +
                           "-fx-font-size: 20px; " +
                           "-fx-font-weight: bold; " +
                           "-fx-border-color: #333; " +
                           "-fx-border-width: 3; " +
                           "-fx-border-radius: 5; " +
                           "-fx-background-radius: 5; " +
                           "-fx-cursor: hand; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 2, 2);";
                           
        String hoverStyle = baseStyle.replace("-fx-background-color: " + color, "-fx-background-color: derive(" + color + ", -15%)");

        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        
        return btn;
    }

    private static void updateHpBar(VBox statusBox, int currentHp, int maxHp, boolean isPlayer) {
        ProgressBar bar = (ProgressBar) statusBox.lookup("#hpBar");
        double displayHp = Math.max(0, currentHp); 
        
        if (bar != null) {
            bar.setProgress(displayHp / maxHp);
            String barColor = (displayHp / maxHp) <= 0.2 ? "#ff3333" : "#48e85c";
            bar.setStyle("-fx-accent: " + barColor + "; -fx-control-inner-background: #444; -fx-box-border: transparent; -fx-background-radius: 5;");
        }
        
        if (isPlayer) {
            Label text = (Label) statusBox.lookup("#hpText");
            if (text != null) text.setText((int)displayHp + " / " + maxHp);
        }
    }

    // ==========================================
    // ANIMAÇÕES E TRANSIÇÕES
    // ==========================================

    private static void playAttackAnimation(ImageView attacker, ImageView victim, Runnable onFinished) {
        TranslateTransition move = new TranslateTransition(Duration.millis(200), attacker);
        move.setByX(100);
        move.setByY(-50);
        move.setAutoReverse(true);
        move.setCycleCount(2);
        move.setOnFinished(e -> onFinished.run());
        move.play();
    }

    private static void playShakeAnimation(ImageView target) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), target);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    private static void exitBattle(Stage stage, Scene mapScene, App app, Pane rootLayout, StackPane originalLayout){
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), rootLayout);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            inBattle = false;
            mapScene.setRoot(originalLayout);
            originalLayout.setOpacity(1); 
            if (!stage.isFullScreen()) stage.setFullScreen(true);
            app.resumeTimers();
        });
        fadeOut.play();
    }
}