package com.RPG.TheLastRoar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.event.EventHandler;


public class StartScreen {

    private static final Color COR_TEXTO_NORMAL = Color.web("#D8D0C0");
    private static final Color COR_TEXTO_DISABLED = Color.web("#555040");
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

        Text pressKey = new Text("Pressione qualquer tecla");
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

        List<Button> menuButtons = new ArrayList<>();
        int[] selectedIdx = {0};
        boolean[] isSplashActive = {true};

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

        Button btnLoad = criarBotaoMenu("CARREGAR", fontMenu);
        btnLoad.setDisable(!temSave);
        btnLoad.setOnAction(e -> {
            boxSlots.setVisible(!boxSlots.isVisible());
            boxSlots.setManaged(boxSlots.isVisible());
        });

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

        // --- MONTAGEM DA PILHA (ORDEM É TUDO) ---
        // 1. Fundo Menu (atrás)
        // 2. Fundo Splash (frente do fundo menu)
        // 3. UI Elements (Menu, Rodapé, Splash Text)
        root.getChildren().addAll(fundoMenu, fundoSplash, conteudoMenu, rodape, splashContainer);
        StackPane.setAlignment(rodape, Pos.BOTTOM_RIGHT);

        // --- LÓGICA DE TRANSIÇÃO ---
        Runnable refreshUI = () -> {
            for (int i = 0; i < menuButtons.size(); i++) {
                aplicarEstiloBotao(menuButtons.get(i), i == selectedIdx[0]);
            }
        };

        EventHandler<InputEvent> handleSplash = e -> {
            if (isSplashActive[0]) {
                if (e instanceof MouseEvent && ((MouseEvent)e).getEventType() != MouseEvent.MOUSE_PRESSED) return;
                isSplashActive[0] = false;
                executarTransicao(fundoSplash, splashContainer, conteudoMenu, rodape);
            }
        };

        root.addEventFilter(MouseEvent.MOUSE_PRESSED, handleSplash);
        root.addEventFilter(KeyEvent.KEY_PRESSED, handleSplash);

        // --- NAVEGAÇÃO TECLADO ---
        root.setOnKeyPressed(e -> {
            if (isSplashActive[0]) return;
            int total = menuButtons.size();
            switch (e.getCode()) {
                case UP: case W:
                    do { selectedIdx[0] = (selectedIdx[0] - 1 + total) % total; }
                    while (menuButtons.get(selectedIdx[0]).isDisabled());
                    break;
                case DOWN: case S:
                    do { selectedIdx[0] = (selectedIdx[0] + 1) % total; }
                    while (menuButtons.get(selectedIdx[0]).isDisabled());
                    break;
                case ENTER: case SPACE:
                    menuButtons.get(selectedIdx[0]).fire();
                    break;
            }
            refreshUI.run();
        });

        refreshUI.run();
        Platform.runLater(root::requestFocus);
        return root;
    }

    private static Button criarBotaoMenu(String label, Font font) {
        Button b = new Button(label);
        b.setUserData(label);
        b.setFont(font);
        b.setMinWidth(450);
        b.setAlignment(Pos.CENTER_LEFT);
        return b;
    }

    private static void aplicarEstiloBotao(Button btn, boolean sel) {
        String txt = (String) btn.getUserData();
        if (btn.isDisabled()) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555040; -fx-padding: 5 0 5 20;");
            btn.setText("  " + txt);
        } else if (sel) {
            btn.setStyle("-fx-background-color: rgba(0, 229, 229, 0.1); -fx-border-color: #00E5E5; -fx-border-width: 0 0 0 5; -fx-text-fill: white; -fx-padding: 5 0 5 20;");
            btn.setText("> " + txt);
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #D8D0C0; -fx-padding: 5 0 5 20;");
            btn.setText("  " + txt);
        }
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
            b.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-border-color: " + (exists ? "#00E5E5" : "#333") + "; -fx-text-fill: " + (exists ? "#00E5E5" : "#444") + "; -fx-padding: 8 15;");
            if (exists) b.setOnAction(e -> load.accept(file));
            hb.getChildren().add(b);
        }
        return hb;
    }

    private static HBox criarRodape(Font font) {
        HBox hb = new HBox(25);
        hb.setPadding(new Insets(30));
        Text t1 = new Text("W/S: NAVEGAR"); t1.setFont(font); t1.setFill(Color.GRAY);
        Text t2 = new Text("ENTER: SELECIONAR"); t2.setFont(font); t2.setFill(Color.GRAY);
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