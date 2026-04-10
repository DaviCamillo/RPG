package com.RPG.TheLastRoar.frontend.screens;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public class IntroScreen {

    public record IntroScene(
            String imagePath,
            String legendText,
            String audioPath,
            double durationSec
    ) {}

    // Variável estática para guardar o áudio tocando atualmente (evita sobreposição)
    private static AudioClip currentAudio = null;

    public static void play(Stage stage, Scene mainScene, Runnable onFinished) {
        List<IntroScene> cenas = criarCenas();
        
        // Garante que qualquer áudio anterior seja parado ao iniciar
        pararAudioAtual();

        StackPane introRoot = construirLayout(stage, cenas, onFinished);

        // Configura o evento de pular com ESC
        introRoot.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                pularIntro(onFinished, introRoot);
            }
        });

        // Configura a cena inicial
        introRoot.setOpacity(0);
        mainScene.setRoot(introRoot);
        
        // ESSENCIAL: Garante que o painel receba os eventos de teclado!
        introRoot.requestFocus();

        FadeTransition ft = new FadeTransition(Duration.millis(600), introRoot);
        ft.setToValue(1);
        ft.play();
    }

    private static List<IntroScene> criarCenas() {
        List<IntroScene> cenas = new ArrayList<>();

        cenas.add(new IntroScene(
            "/images/intro/cena1.png",
            "Há muito tempo, as terras de Eldrath viviam em paz...",
            "/audio/intro/narracao1.wav",
            5.0
        ));

        cenas.add(new IntroScene(
            "/images/intro/cena2.png",
            "Até que as criaturas das trevas romperam o equilíbrio...",
            "/audio/intro/narracao2.wav",
            5.0
        ));

        cenas.add(new IntroScene(
            "/images/intro/cena3.png",
            "Um único guerreiro se levantou para responder ao chamado.",
            "/audio/intro/narracao3.wav",
            5.0
        ));

        cenas.add(new IntroScene(
            "/images/intro/cena4.png",
            "Esta é a história do Último Rugido.",
            null,
            4.0
        ));

        return cenas;
    }

    private static StackPane construirLayout(Stage stage, List<IntroScene> cenas, Runnable onFinished) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: black;");
        root.setFocusTraversable(true);

        ImageView imgView = new ImageView();
        imgView.setPreserveRatio(false);
        imgView.fitWidthProperty().bind(stage.widthProperty());
        imgView.fitHeightProperty().bind(stage.heightProperty());

        VBox legendaBox = new VBox();
        legendaBox.setAlignment(Pos.CENTER);
        legendaBox.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.65);" +
            "-fx-padding: 18 40;"
        );
        legendaBox.setMaxHeight(120);

        Text legendaText = new Text("");
        legendaText.setFont(Font.font("Palatino Linotype", FontWeight.NORMAL, 22));
        legendaText.setFill(Color.web("#EDE8DC"));
        legendaText.setTextAlignment(TextAlignment.CENTER);
        legendaText.setWrappingWidth(900);
        legendaBox.getChildren().add(legendaText);
        StackPane.setAlignment(legendaBox, Pos.BOTTOM_CENTER);

        Text dica = new Text("Clique para avançar  |  ESC para pular");
        dica.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        dica.setFill(Color.web("#FFFFFF", 0.4));
        StackPane.setAlignment(dica, Pos.BOTTOM_RIGHT);
        javafx.scene.layout.StackPane.setMargin(dica, new javafx.geometry.Insets(0, 20, 8, 0));

        root.getChildren().addAll(imgView, legendaBox, dica);

        int[] indexAtual = {0};
        PauseTransition[] timerRef = {null};

        Runnable avancarCena = new Runnable() {
            @Override
            public void run() {
                // Ao avançar a cena (seja por tempo ou clique), para o áudio atual!
                pararAudioAtual();

                indexAtual[0]++;
                if (indexAtual[0] < cenas.size()) {
                    FadeTransition fadeOutImg = new FadeTransition(Duration.millis(400), imgView);
                    fadeOutImg.setToValue(0);
                    fadeOutImg.setOnFinished(e -> 
                        showCena(indexAtual[0], cenas, imgView, legendaText, timerRef, this)
                    );
                    fadeOutImg.play();

                    FadeTransition fadeOutLeg = new FadeTransition(Duration.millis(300), legendaText);
                    fadeOutLeg.setToValue(0);
                    fadeOutLeg.play();
                } else {
                    pularIntro(onFinished, root);
                }
            }
        };

        // Usa setOnMousePressed no lugar de addEventFilter para não "vazar" o clique
        root.setOnMousePressed(e -> {
            if (timerRef[0] != null) timerRef[0].stop();
            avancarCena.run();
        });

        showCena(0, cenas, imgView, legendaText, timerRef, avancarCena);

        return root;
    }

    private static void showCena(int index, List<IntroScene> cenas, ImageView imgView,
                                 Text legendaText, PauseTransition[] timerRef, Runnable onNext) {
        
        if (index >= cenas.size()) {
            System.out.println("Fim das cenas de introdução! Carregando a próxima tela...");
            
            // --- CÓDIGO DE CARREGAMENTO DA PRÓXIMA TELA ---
            try {
                // 1. Pega a janela atual (Stage)
                javafx.stage.Stage stage = (javafx.stage.Stage) imgView.getScene().getWindow();
                
                // 2. Carrega o arquivo FXML da próxima tela (MUDE O NOME AQUI SE PRECISAR)
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(IntroScreen.class.getResource("/MenuPrincipal.fxml"));
                javafx.scene.Parent root = loader.load();
                
                // 3. Define a nova cena e mostra
                javafx.scene.Scene novaCena = new javafx.scene.Scene(root);
                stage.setScene(novaCena);
                stage.show();
                
            } catch (Exception e) {
                System.out.println("Erro ao carregar a próxima tela:");
                e.printStackTrace();
            }
            // ----------------------------------------------
            
            return; 
        }

        IntroScene cena = cenas.get(index);

        try {
            var url = IntroScreen.class.getResource(cena.imagePath());
            imgView.setImage(url != null ? new javafx.scene.image.Image(url.toExternalForm()) : null);
        } catch (Exception ex) {
            imgView.setImage(null);
        }

        imgView.setOpacity(0);
        FadeTransition fadeImg = new FadeTransition(Duration.millis(800), imgView);
        fadeImg.setToValue(1);
        fadeImg.play();

        legendaText.setOpacity(0);
        legendaText.setText(cena.legendText());
        FadeTransition fadeLeg = new FadeTransition(Duration.millis(600), legendaText);
        fadeLeg.setDelay(Duration.millis(400));
        fadeLeg.setToValue(1);
        fadeLeg.play();

        tocarAudio(cena.audioPath());

        PauseTransition timer = new PauseTransition(Duration.seconds(cena.durationSec()));
        timer.setOnFinished(e -> onNext.run());
        timerRef[0] = timer;
        timer.play();
    }
    private static void tocarAudio(String audioPath) {
        if (audioPath == null || audioPath.isBlank()) return;

        try {
            var url = IntroScreen.class.getResource(audioPath);
            if (url == null) return;
            
            // Para qualquer áudio que ainda esteja tocando antes de iniciar o próximo
            pararAudioAtual();
            
            currentAudio = new AudioClip(url.toExternalForm());
            currentAudio.setVolume(0.85);
            currentAudio.play();
        } catch (Exception ex) {
            System.out.println("[IntroScreen] Erro no áudio: " + ex.getMessage());
        }
    }

    // Método auxiliar para parar o áudio e limpar a referência
    private static void pararAudioAtual() {
        if (currentAudio != null && currentAudio.isPlaying()) {
            currentAudio.stop();
        }
        currentAudio = null;
    }

    private static void pularIntro(Runnable onFinished, StackPane introRoot) {
        // Para o áudio imediatamente se o jogador pular
        pararAudioAtual();
        
        // Remove os eventos para evitar memory leaks ou disparos acidentais depois que fechar
        introRoot.setOnKeyPressed(null);
        introRoot.setOnMousePressed(null);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), introRoot);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> onFinished.run());
        fadeOut.play();
    }
}

