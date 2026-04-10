package com.RPG.TheLastRoar.frontend.screens;
import com.RPG.TheLastRoar.backend.models.Character;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * ============================================================
 * HudManager.java — Heads-Up Display (HUD) do jogador
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Cria e atualiza o painel de status visível durante o jogo.
 *   Exibido no canto superior esquerdo da tela.
 *
 * INFORMAÇÕES EXIBIDAS:
 *   ┌──────────────────────────────────────────┐
 *   │  Nível: 3                                │
 *   │  HP: 18/20  [████████████░░░]            │
 *   │  XP: 7/15   [░░░░░████████░]             │
 *   │  Resistência: 5                          │
 *   │  🪙 Ouro: 42                             │
 *   └──────────────────────────────────────────┘
 *
 * ATUALIZAÇÃO:
 *   Chamar atualizar(player) sempre que:
 *   - HP muda (batalha, poção)
 *   - XP/nível muda (vitória em batalha)
 *   - Ouro muda (compra na loja, drop de inimigo)
 *   - Resistência muda (equipar/desequipar armadura)
 *
 * USADO POR:
 *   App.java     — cria e mantém a referência; chama atualizar()
 *   Battle.java  — chama atualizar() ao terminar batalha
 *   ShopNPC.java — chama atualizar() após compra
 */
public class HudManager {

    // =========================================================================
    // FIELDS — Componentes da UI (mantidos para atualização dinâmica)
    // =========================================================================

    private HBox        hudLayout;
    private ProgressBar hpBar;
    private ProgressBar xpBar;
    private Label       lblHp;
    private Label       lblXp;
    private Label       lblGold;
    private Label       lblLevel;
    private Label       lblResistance;

    /** Referência ao Stage principal (reservada para expansões futuras). */
    @SuppressWarnings("unused")
    private final Stage stage;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    /**
     * Cria o HUD e monta todos os componentes visuais.
     *
     * @param stage Stage principal do JavaFX (referência futura).
     */
    public HudManager(Stage stage) {
        this.stage = stage;
        buildHud();
    }

    // =========================================================================
    // BUILD — Criação dos componentes visuais
    // =========================================================================

    /**
     * Constrói o layout do HUD com todos os seus elementos.
     * Chamado uma única vez no constructor.
     */
    private void buildHud() {
        hudLayout = new HBox(15);
        hudLayout.setAlignment(Pos.CENTER_LEFT);
        hudLayout.setStyle(
            "-fx-background-color: rgba(20, 20, 20, 0.8);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #DAA520;" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 10;" +
            "-fx-padding: 10px;"
        );
        hudLayout.setMaxSize(
            javafx.scene.layout.Region.USE_PREF_SIZE,
            javafx.scene.layout.Region.USE_PREF_SIZE
        );

        VBox statsBox = new VBox(8);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        // ── Nível ─────────────────────────────────────────────────────────
        lblLevel = new Label("Nível: 1");
        lblLevel.setTextFill(Color.CYAN);
        lblLevel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        statsBox.getChildren().add(lblLevel);

        // ── Barra de HP ───────────────────────────────────────────────────
        HBox hpBox = new HBox(12);
        lblHp = new Label("HP: 20/20");
        lblHp.setTextFill(Color.WHITE);
        lblHp.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        hpBar = new ProgressBar(1.0);
        hpBar.setPrefWidth(160);
        hpBar.setPrefHeight(12);
        hpBar.setStyle("-fx-accent: #e74c3c; -fx-background-color: #333;");
        hpBox.getChildren().addAll(lblHp, hpBar);
        statsBox.getChildren().add(hpBox);

        // ── Barra de XP ───────────────────────────────────────────────────
        HBox xpBox = new HBox(12);
        lblXp = new Label("XP: 0/10");
        lblXp.setTextFill(Color.WHITE);
        lblXp.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        xpBar = new ProgressBar(0.0);
        xpBar.setPrefWidth(160);
        xpBar.setPrefHeight(12);
        xpBar.setStyle("-fx-accent: #2ecc71; -fx-background-color: #333;");
        xpBox.getChildren().addAll(lblXp, xpBar);
        statsBox.getChildren().add(xpBox);

        // ── Resistência (inclui armadura equipada via getResistance()) ────
        lblResistance = new Label("Resistência: 0");
        lblResistance.setId("lblResistencia");   // ID para lookup em atualizar()
        lblResistance.setTextFill(Color.ORANGE);
        lblResistance.setFont(Font.font("Arial", FontWeight.MEDIUM, 14));
        statsBox.getChildren().add(lblResistance);

        // ── Ouro ─────────────────────────────────────────────────────────
        lblGold = new Label("🪙 Ouro: 0");
        lblGold.setTextFill(Color.GOLD);
        lblGold.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        statsBox.getChildren().add(lblGold);

        hudLayout.getChildren().add(statsBox);

        // Posicionamento no StackPane (canto superior esquerdo com margem)
        javafx.scene.layout.StackPane.setAlignment(hudLayout, Pos.TOP_LEFT);
        javafx.scene.layout.StackPane.setMargin(hudLayout, new Insets(20));
    }

    // =========================================================================
    // UPDATE — Atualização dos valores em tempo real
    // =========================================================================

    /**
     * Atualiza TODOS os elementos do HUD com os dados atuais do personagem.
     *
     * Deve ser chamado sempre que qualquer atributo do personagem mudar.
     * Usa player.getMaxLife() e player.getXpNecessary() de forma dinâmica
     * para refletir corretamente os valores após level-up.
     *
     * @param player Personagem cujos dados serão exibidos. Ignorado se null.
     */
    public void atualizar(Character player) {
        if (player == null) return;

        // ── HP ─────────────────────────────────────────────────────────────
        int currentHp = player.getLife();
        int maxHp     = player.getMaxLife();  // Dinâmico — correto após level-up
        lblHp.setText("HP: " + currentHp + "/" + maxHp);
        hpBar.setProgress((double) currentHp / Math.max(1, maxHp));

        // ── Nível ─────────────────────────────────────────────────────────
        lblLevel.setText("Nível: " + player.getNivel());

        // ── XP ─────────────────────────────────────────────────────────────
        int currentXp = player.getXp();
        int maxXp     = player.getXpNecessary();  // Dinâmico — cresce a cada nível
        lblXp.setText("XP: " + currentXp + "/" + maxXp);
        xpBar.setProgress((double) currentXp / Math.max(1, maxXp));

        // ── Resistência total (base + armadura equipada) ──────────────────
        lblResistance.setText("Resistência: " + player.getResistance());

        // ── Ouro ─────────────────────────────────────────────────────────
        lblGold.setText("🪙 Ouro: " + player.getCoin());
    }

    // =========================================================================
    // GETTER
    // =========================================================================

    /**
     * @return O painel HBox raiz para ser adicionado ao StackPane principal.
     */
    public HBox getLayout() {
        return hudLayout;
    }
}



