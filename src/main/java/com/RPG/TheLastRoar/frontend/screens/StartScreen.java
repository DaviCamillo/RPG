package com.RPG.TheLastRoar.frontend.screens;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class StartScreen {

    private static final Duration DURACO_TRANSICAO = Duration.millis(1200);

    public static StackPane createLayout(Runnable onNewGame, Runnable onContinue, Consumer<String> onLoadSlot) {
        StackPane root = new StackPane();
        root.setPrefSize(1280, 720); // Tamanho da janela, mas a imagem escala para 1080p

        // --- CARREGAMENTO DE FONTES ---
        String fontPath = "/fonts/PressStart2P-Regular.ttf";
        Font fontSplash = Font.loadFont(StartScreen.class.getResourceAsStream(fontPath), 22);
        Font fontMenu = Font.loadFont(StartScreen.class.getResourceAsStream(fontPath), 18);
        Font fontSmall = Font.loadFont(StartScreen.class.getResourceAsStream(fontPath), 10);

        // --- FUNDOS (Tratados como 1920x1080) ---
        ImageView fundoMenu = new ImageView();
        ImageView fundoSplash = new ImageView();
        try {
            fundoSplash.setImage(new Image(StartScreen.class.getResourceAsStream("/images/background.png"), 1920, 1080, false, true));
            fundoMenu.setImage(new Image(StartScreen.class.getResourceAsStream("/images/background_menu.png"), 1920, 1080, false, true));
        } catch (Exception e) {
            System.out.println("Erro ao carregar imagens.");
        }

        configurarImageFullHD(fundoSplash, root);
        configurarImageFullHD(fundoMenu, root);

        // --- TELA DE SPLASH ---
        VBox splashContainer = new VBox();
        splashContainer.setAlignment(Pos.BOTTOM_CENTER);
        splashContainer.setPadding(new Insets(0, 0, 150, 0));
        splashContainer.setMouseTransparent(true);

        Text pressKey = new Text("Pressione qualquer tecla ou clique");
        pressKey.setFont(fontSplash != null ? fontSplash : Font.font("Monospaced", 22));
        pressKey.setFill(Color.WHITE);

        FadeTransition pulse = new FadeTransition(Duration.seconds(1.2), pressKey);
        pulse.setFromValue(1.0); pulse.setToValue(0.2);
        pulse.setAutoReverse(true); pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
        splashContainer.getChildren().add(pressKey);

        // --- CONTEÚDO DO MENU ---
        VBox conteudoMenu = new VBox(15);
        conteudoMenu.setAlignment(Pos.CENTER_LEFT);
        conteudoMenu.setPadding(new Insets(0, 0, 0, 100));
        conteudoMenu.setOpacity(0);
        conteudoMenu.setVisible(false);

        Line separador = new Line(0, 0, 300, 0);
        separador.setStroke(Color.web("#B8960C", 0.5));
        conteudoMenu.getChildren().add(separador);

        // Variáveis de Estado
        List<Button> menuButtons = new ArrayList<>();
        int[] selectedIdx = {0};
        boolean[] isSplashActive = {true};
        boolean[] inLoadMenu = {false};
        int[] selectedSlotIdx = {0};

        // Botões do Menu
        Button btnNovo = criarBotaoMenu("NOVO JOGO", fontMenu);
        btnNovo.setOnAction(e -> onNewGame.run());

        Button btnCont = criarBotaoMenu("CONTINUAR", fontMenu);
        boolean temSave = getUltimoSave() != null;
        btnCont.setDisable(!temSave);
        btnCont.setOnAction(e -> onContinue.run());

        HBox boxSlots = criarBoxSlots(onLoadSlot, fontSmall);
        boxSlots.setVisible(false);
        boxSlots.setManaged(false);

        Button btnLoad = criarBotaoMenu("CARREGAR JOGO", fontMenu);
        btnLoad.setDisable(!temSave);

        Button btnSair = criarBotaoMenu("SAIR", fontMenu);
        btnSair.setOnAction(e -> Platform.exit());

        menuButtons.add(btnNovo);
        menuButtons.add(btnCont);
        menuButtons.add(btnLoad);
        menuButtons.add(btnSair);
        conteudoMenu.getChildren().addAll(btnNovo, btnCont, btnLoad, boxSlots, btnSair);

        // Rodapé com Dicas
        HBox rodape = criarRodape(fontSmall);
        rodape.setOpacity(0);

        root.getChildren().addAll(fundoMenu, fundoSplash, conteudoMenu, rodape, splashContainer);
        StackPane.setAlignment(rodape, Pos.BOTTOM_RIGHT);

        // --- ATUALIZAÇÃO VISUAL (UI) ---
        Runnable refreshUI = () -> {
            // Estiliza Menu Principal
            for (int i = 0; i < menuButtons.size(); i++) {
                boolean isSelected = (i == selectedIdx[0]) && !inLoadMenu[0];
                aplicarEstiloBotao(menuButtons.get(i), isSelected, menuButtons.get(i).isDisabled());
            }
            // Estiliza Sub-menu de Slots
            if (inLoadMenu[0]) {
                for (int i = 0; i < 3; i++) {
                    Button btn = (Button) boxSlots.getChildren().get(i);
                    aplicarEstiloSlot(btn, i == selectedSlotIdx[0], btn.isDisabled());
                }
            } else {
                for (int i = 0; i < 3; i++) {
                    Button btn = (Button) boxSlots.getChildren().get(i);
                    aplicarEstiloSlot(btn, false, btn.isDisabled());
                }
            }
        };

        // --- AÇÃO DO BOTÃO CARREGAR (Toggle) ---
        btnLoad.setOnAction(e -> {
            inLoadMenu[0] = !inLoadMenu[0];
            boxSlots.setVisible(inLoadMenu[0]);
            boxSlots.setManaged(inLoadMenu[0]);
            if (inLoadMenu[0]) {
                // Seleciona o primeiro slot não-vazio automaticamente
                selectedSlotIdx[0] = 0;
                while (selectedSlotIdx[0] < 3 && ((Button)boxSlots.getChildren().get(selectedSlotIdx[0])).isDisabled()) {
                    selectedSlotIdx[0]++;
                }
                if (selectedSlotIdx[0] >= 3) selectedSlotIdx[0] = 0; // Prevenção de erro
            }
            refreshUI.run();
        });

        // --- SINCRONIZAÇÃO DO RATO ---
        // Quando passa o rato, o menu atualiza a seleção como se fosse o teclado
        for (int i = 0; i < menuButtons.size(); i++) {
            final int index = i;
            menuButtons.get(i).setOnMouseEntered(e -> {
                if (!menuButtons.get(index).isDisabled()) {
                    selectedIdx[0] = index;
                    inLoadMenu[0] = false; 
                    boxSlots.setVisible(false);
                    boxSlots.setManaged(false);
                    refreshUI.run();
                }
            });
        }

        for (int i = 0; i < 3; i++) {
            final int index = i;
            Button slotBtn = (Button) boxSlots.getChildren().get(i);
            slotBtn.setOnMouseEntered(e -> {
                if (!slotBtn.isDisabled()) {
                    inLoadMenu[0] = true;
                    selectedSlotIdx[0] = index;
                    refreshUI.run();
                }
            });
        }

        // --- INTERAÇÕES DE SPLASH (Corta o primeiro clique) ---
        EventHandler<InputEvent> handleSplash = e -> {
            if (isSplashActive[0]) {
                if (e instanceof MouseEvent && ((MouseEvent)e).getEventType() != MouseEvent.MOUSE_PRESSED) return;
                isSplashActive[0] = false;
                executarTransicao(fundoSplash, splashContainer, conteudoMenu, rodape);
                e.consume(); // Consome o evento para não disparar botões escondidos
            }
        };

        root.addEventFilter(MouseEvent.MOUSE_PRESSED, handleSplash);
        root.addEventFilter(KeyEvent.KEY_PRESSED, handleSplash);

        // --- NAVEGAÇÃO DE TECLADO ---
        root.setOnKeyPressed(e -> {
            if (isSplashActive[0]) return; // Proteção extra
            
            // 1. NAVEGAÇÃO NOS SLOTS (A / D ou Setas Esquerda/Direita)
            if (inLoadMenu[0]) {
                switch (e.getCode()) {
                    case LEFT: case A:
                        do { selectedSlotIdx[0] = (selectedSlotIdx[0] - 1 + 3) % 3; }
                        while (((Button)boxSlots.getChildren().get(selectedSlotIdx[0])).isDisabled());
                        e.consume();
                        break;
                    case RIGHT: case D:
                        do { selectedSlotIdx[0] = (selectedSlotIdx[0] + 1) % 3; }
                        while (((Button)boxSlots.getChildren().get(selectedSlotIdx[0])).isDisabled());
                        e.consume();
                        break;
                    case ESCAPE: case BACK_SPACE: case UP: case W: case DOWN: case S:
                        // Volta para o menu principal
                        inLoadMenu[0] = false;
                        boxSlots.setVisible(false);
                        boxSlots.setManaged(false);
                        e.consume();
                        break;
                    case ENTER: case SPACE:
                        Button slotBtn = (Button) boxSlots.getChildren().get(selectedSlotIdx[0]);
                        if (!slotBtn.isDisabled()) slotBtn.fire();
                        e.consume();
                        break;
                    default: break;
                }
            } 
            // 2. NAVEGAÇÃO NO MENU PRINCIPAL (W / S ou Setas Cima/Baixo)
            else {
                int total = menuButtons.size();
                switch (e.getCode()) {
                    case UP: case W:
                        do { selectedIdx[0] = (selectedIdx[0] - 1 + total) % total; }
                        while (menuButtons.get(selectedIdx[0]).isDisabled());
                        e.consume();
                        break;
                    case DOWN: case S:
                        do { selectedIdx[0] = (selectedIdx[0] + 1) % total; }
                        while (menuButtons.get(selectedIdx[0]).isDisabled());
                        e.consume();
                        break;
                    case RIGHT: case D: case ENTER: case SPACE:
                        Button btnPressionado = menuButtons.get(selectedIdx[0]);
                        btnPressionado.fire(); // Dispara o botão de Carregar, Sair, Novo, etc.
                        e.consume();
                        break;
                    default: break;
                }
            }
            refreshUI.run();
        });

        refreshUI.run();
        Platform.runLater(root::requestFocus);
        return root;
    }

    // =========================================================================
    // UTILITÁRIOS VISUAIS
    // =========================================================================

    private static Button criarBotaoMenu(String label, Font font) {
        Button b = new Button(label);
        b.setUserData(label);
        b.setFont(font);
        b.setMinWidth(450);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setFocusTraversable(false); // CRUCIAL para não roubar as setinhas do teclado
        return b;
    }

    private static HBox criarBoxSlots(Consumer<String> load, Font font) {
        HBox hb = new HBox(15);
        hb.setPadding(new Insets(10, 0, 10, 40));
        for (int i = 1; i <= 3; i++) {
            String file = "save" + i + ".json";
            boolean exists = new File(file).exists();
            Button b = new Button(exists ? "SLOT " + i : "VAZIO");
            b.setFont(font);
            b.setDisable(!exists);
            b.setFocusTraversable(false);
            if (exists) b.setOnAction(e -> load.accept(file));
            hb.getChildren().add(b);
        }
        return hb;
    }

    // =========================================================================
    // ESTILIZAÇÃO E ANIMAÇÃO
    // =========================================================================

    private static void aplicarEstiloBotao(Button btn, boolean isSelected, boolean isDisabled) {
        String txt = (String) btn.getUserData();
        if (isDisabled) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555040; -fx-padding: 5 0 5 20; -fx-cursor: default;");
            btn.setText("  " + txt);
        } else if (isSelected) {
            btn.setStyle("-fx-background-color: rgba(0, 229, 229, 0.1); -fx-border-color: #00E5E5; -fx-border-width: 0 0 0 5; -fx-text-fill: white; -fx-padding: 5 0 5 20; -fx-cursor: hand;");
            btn.setText("> " + txt);
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #D8D0C0; -fx-padding: 5 0 5 20; -fx-cursor: hand;");
            btn.setText("  " + txt);
        }
    }

    private static void aplicarEstiloSlot(Button btn, boolean isSelected, boolean isDisabled) {
        if (isDisabled) {
            btn.setStyle("-fx-background-color: rgba(10,10,10,0.4); -fx-border-color: #333; -fx-text-fill: #555040; -fx-padding: 8 15; -fx-cursor: default;");
        } else if (isSelected) {
            btn.setStyle("-fx-background-color: rgba(0,200,200,0.2); -fx-border-color: #00E5E5; -fx-border-width: 1; -fx-text-fill: #FFFFFF; -fx-padding: 8 15; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-border-color: #00E5E5; -fx-border-width: 1; -fx-text-fill: #00E5E5; -fx-padding: 8 15; -fx-cursor: hand;");
        }
    }

    private static HBox criarRodape(Font font) {
        HBox hb = new HBox(25);
        hb.setPadding(new Insets(30));
        Text t1 = new Text("W/S/A/D: NAVEGAR"); t1.setFont(font); t1.setFill(Color.GRAY);
        Text t2 = new Text("ENTER/CLIQUE: SELECIONAR"); t2.setFont(font); t2.setFill(Color.GRAY);
        hb.getChildren().addAll(t1, t2);
        return hb;
    }

    private static void executarTransicao(ImageView imgSplash, VBox splash, VBox menu, HBox rodape) {
        FadeTransition fImg = new FadeTransition(DURACO_TRANSICAO, imgSplash);
        fImg.setToValue(0);

        ScaleTransition sImg = new ScaleTransition(DURACO_TRANSICAO, imgSplash);
        sImg.setToX(1.1); sImg.setToY(1.1);

        FadeTransition fSplash = new FadeTransition(Duration.millis(500), splash);
        fSplash.setToValue(0);

        fSplash.setOnFinished(e -> {
            splash.setVisible(false);
            menu.setVisible(true);
            FadeTransition fm = new FadeTransition(Duration.millis(800), menu);
            fm.setToValue(1);
            FadeTransition fr = new FadeTransition(Duration.millis(800), rodape);
            fr.setToValue(1);
            new ParallelTransition(fm, fr).play();
        });

        new ParallelTransition(fImg, sImg, fSplash).play();
    }

    private static void configurarImageFullHD(ImageView iv, StackPane root) {
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        InvalidationListener resizer = obs -> {
            double w = root.getWidth();
            double h = root.getHeight();
            if (w <= 0 || h <= 0) return;
            if (w / h >= 1920.0 / 1080.0) {
                iv.setFitWidth(w); iv.setFitHeight(0);
            } else {
                iv.setFitHeight(h); iv.setFitWidth(0);
            }
        };
        root.widthProperty().addListener(resizer);
        root.heightProperty().addListener(resizer);
    }

    public static String getUltimoSave() {
        for (int i = 1; i <= 3; i++) {
            File f = new File("save" + i + ".json");
            if (f.exists()) return f.getName();
        }
        return null;
    }
}

