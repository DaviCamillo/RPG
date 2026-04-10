package com.RPG.TheLastRoar.frontend.screens;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * ============================================================
 * PauseMenu.java — Menu de pausa
 * ============================================================
 */
public class PauseMenu {

    // Nome da família da sua fonte customizada
    private static final String FONT_PIXEL = "Press Start 2P";

    private VBox layout;
    private Button btnLoad1, btnLoad2, btnLoad3; // Guardamos para atualizar o estado de disable

    public PauseMenu(Runnable onResume,
                     Runnable onSave1, Runnable onSave2, Runnable onSave3,
                     Runnable onLoad1, Runnable onLoad2, Runnable onLoad3,
                     Runnable onMainMenu) {

        layout = new VBox(0);
        layout.setAlignment(Pos.CENTER_LEFT);
        // Padding esquerdo para não ficar colado na borda
        layout.setPadding(new Insets(0, 0, 0, 120));

        // Fundo escuro semitransparente para cobrir o jogo
        layout.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);");
        layout.setVisible(false);

        // ── TÍTULO ────────────────────────────────────────────────────────
        Text titulo = new Text("PAUSADO");
        titulo.setFont(Font.font(FONT_PIXEL, FontWeight.NORMAL, 28)); // Ajustado para pixel font
        titulo.setFill(Color.web("#F0E6C0"));

        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#C8A000", 0.8));
        sombra.setRadius(24);
        sombra.setSpread(0.12);
        titulo.setEffect(sombra);

        VBox.setMargin(titulo, new Insets(0, 0, 12, 0));
        layout.getChildren().add(titulo);

        // ── LINHA DECORATIVA ──────────────────────────────────────────────
        Line separador = new Line(0, 0, 240, 0);
        separador.setStroke(Color.web("#B8960C", 0.6));
        separador.setStrokeWidth(1.0);
        VBox.setMargin(separador, new Insets(4, 0, 30, 0));
        layout.getChildren().add(separador);

        // ── OPÇÕES PRINCIPAIS ─────────────────────────────────────────────

        Button btnResume = criarBotaoMenu("VOLTAR AO JOGO", "#00E5E5");
        btnResume.setOnAction(e -> onResume.run());
        VBox.setMargin(btnResume, new Insets(0, 0, 10, 0));
        layout.getChildren().add(btnResume);

        // ── SISTEMA DE SAVE ───────────────────────────────────────────────
        HBox slotsSave = criarPainelSlots("Salvar", onSave1, onSave2, onSave3);
        Button btnSave = criarBotaoMenu("SALVAR JOGO", "#00E5E5");
        btnSave.setOnAction(e -> toggleVisibilidade(slotsSave));
        
        VBox.setMargin(btnSave, new Insets(0, 0, 4, 0));
        VBox.setMargin(slotsSave, new Insets(0, 0, 10, 20));
        layout.getChildren().addAll(btnSave, slotsSave);

        // ── SISTEMA DE LOAD ───────────────────────────────────────────────
        HBox slotsLoad = criarPainelSlots("Carregar", onLoad1, onLoad2, onLoad3);
        // Pegamos as referências dos botões de load para desabilitar se o slot estiver vazio
        btnLoad1 = (Button) slotsLoad.getChildren().get(0);
        btnLoad2 = (Button) slotsLoad.getChildren().get(1);
        btnLoad3 = (Button) slotsLoad.getChildren().get(2);

        Button btnLoad = criarBotaoMenu("CARREGAR JOGO", "#00E5E5");
        btnLoad.setOnAction(e -> toggleVisibilidade(slotsLoad));

        VBox.setMargin(btnLoad, new Insets(0, 0, 4, 0));
        VBox.setMargin(slotsLoad, new Insets(0, 0, 30, 20));
        layout.getChildren().addAll(btnLoad, slotsLoad);

        // ── MENU PRINCIPAL ────────────────────────────────────────────────
        Button btnMenu = criarBotaoMenu("MENU PRINCIPAL", "#FF6666");
        btnMenu.setOnAction(e -> onMainMenu.run());
        layout.getChildren().add(btnMenu);
    }

    // =========================================================================
    // CRIAÇÃO DE COMPONENTES VISUAIS
    // =========================================================================

    /**
     * Cria os botões principais de texto sem borda.
     */
    private Button criarBotaoMenu(String texto, String corDestaqueHex) {
        Button btn = new Button("  " + texto);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPrefWidth(300);
        btn.setPrefHeight(40);

        final String estNormal = 
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #D8D0C0;" +
            "-fx-padding: 0 0 0 10;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: '" + FONT_PIXEL + "';" +
            "-fx-font-size: 12px;";

        final String estHover = 
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + corDestaqueHex + ";" +
            "-fx-padding: 0 0 0 10;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: '" + FONT_PIXEL + "';" +
            "-fx-font-size: 12px;";

        btn.setStyle(estNormal);
        btn.setOnMouseEntered(e -> { 
            btn.setStyle(estHover); 
            btn.setText("> " + texto); 
        });
        btn.setOnMouseExited(e -> { 
            btn.setStyle(estNormal); 
            btn.setText("  " + texto); 
        });

        return btn;
    }

    /**
     * Cria um HBox contendo 3 botões (Slot 1, 2 e 3).
     */
    private HBox criarPainelSlots(String prefixo, Runnable acao1, Runnable acao2, Runnable acao3) {
        HBox box = new HBox(12); // Espaçamento entre os botões
        box.setAlignment(Pos.CENTER_LEFT);
        box.setVisible(false);
        box.setManaged(true); // Mantém o espaço reservado para não "empurrar" o layout bruscamente

        Button b1 = criarBotaoSlot(prefixo + " 1", acao1);
        Button b2 = criarBotaoSlot(prefixo + " 2", acao2);
        Button b3 = criarBotaoSlot(prefixo + " 3", acao3);

        box.getChildren().addAll(b1, b2, b3);
        return box;
    }

    /**
     * Cria um botão pequeno estilo "Slot" com borda.
     */
    private Button criarBotaoSlot(String texto, Runnable acao) {
        Button btn = new Button(texto);
        
        final String estNormalSlot = 
            "-fx-background-color: rgba(20, 20, 20, 0.6);" +
            "-fx-border-color: #443E30;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: #A8A090;" +
            "-fx-padding: 6 12;" +
            "-fx-background-radius: 4;" +
            "-fx-border-radius: 4;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: '" + FONT_PIXEL + "';" +
            "-fx-font-size: 8px;";

        final String estHoverSlot = 
            "-fx-background-color: rgba(0, 200, 200, 0.2);" +
            "-fx-border-color: #00E5E5;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-padding: 6 12;" +
            "-fx-background-radius: 4;" +
            "-fx-border-radius: 4;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: '" + FONT_PIXEL + "';" +
            "-fx-font-size: 8px;";

        final String estDisableSlot = 
            "-fx-background-color: rgba(10, 10, 10, 0.4);" +
            "-fx-border-color: #222222;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: #555555;" +
            "-fx-padding: 6 12;" +
            "-fx-background-radius: 4;" +
            "-fx-border-radius: 4;" +
            "-fx-font-family: '" + FONT_PIXEL + "';" +
            "-fx-font-size: 8px;";

        btn.setStyle(estNormalSlot);

        btn.setOnMouseEntered(e -> { if (!btn.isDisabled()) btn.setStyle(estHoverSlot); });
        btn.setOnMouseExited(e ->  { if (!btn.isDisabled()) btn.setStyle(estNormalSlot); });
        
        // Quando o botão for desabilitado/habilitado dinamicamente, atualiza o visual
        btn.disabledProperty().addListener((obs, oldVal, isDisable) -> {
            if (isDisable) btn.setStyle(estDisableSlot);
            else btn.setStyle(estNormalSlot);
        });

        btn.setOnAction(e -> acao.run());
        return btn;
    }

    // =========================================================================
    // CONTROLE DE ESTADO
    // =========================================================================

    /**
     * Atualiza quais botões de LOAD devem estar clicáveis.
     */
    public void atualizarBotoesLoad(boolean slot1Existe, boolean slot2Existe, boolean slot3Existe) {
        if (btnLoad1 != null) btnLoad1.setDisable(!slot1Existe);
        if (btnLoad2 != null) btnLoad2.setDisable(!slot2Existe);
        if (btnLoad3 != null) btnLoad3.setDisable(!slot3Existe);
    }

    /**
     * Exibe ou oculta o menu de pausa com animação de fade in.
     */
    public void setVisible(boolean visible) {
        if (visible) {
            layout.setOpacity(0);
            layout.setVisible(true);
            FadeTransition ft = new FadeTransition(Duration.millis(250), layout);
            ft.setToValue(1);
            ft.play();
        } else {
            layout.setVisible(false);
        }
    }

    /**
     * Retorna o VBox raiz para ser adicionado ao StackPane do App.
     */
    public VBox getLayout() {
        return layout;
    }

    // =========================================================================
    // UTILITÁRIOS
    // =========================================================================

    /**
     * Alterna visibilidade de um painel de slots com animação de fade.
     */
    private static void toggleVisibilidade(HBox box) {
        if (box.isVisible()) {
            FadeTransition ft = new FadeTransition(Duration.millis(200), box);
            ft.setToValue(0);
            ft.setOnFinished(e -> box.setVisible(false));
            ft.play();
        } else {
            box.setOpacity(0);
            box.setVisible(true);
            FadeTransition ft = new FadeTransition(Duration.millis(300), box);
            ft.setToValue(1);
            ft.play();
        }
    }
}

