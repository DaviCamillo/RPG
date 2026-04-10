package com.RPG.TheLastRoar.frontend.screens;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * ============================================================
 * BattleUI.java — Fábrica de componentes visuais da batalha
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Centraliza a criação e atualização de TODOS os componentes
 *   visuais reutilizáveis da tela de batalha.
 *   Battle.java delega toda a construção de UI para esta classe.
 *
 * COMPONENTES FORNECIDOS:
 *   ┌──────────────────────────────────────────────────────────┐
 *   │ criarStatusJogador()  → VBox com HP, XP, nível          │
 *   │ criarStatusMonstro()  → VBox com HP do inimigo          │
 *   │ criarBotaoBatalha()   → Button estilizado (RPG-style)   │
 *   │ atualizarBarraHp()    → Atualiza barra com animação     │
 *   │ calcularCorHp()       → Cor da barra conforme % de HP   │
 *   └──────────────────────────────────────────────────────────┘
 *
 * ESTILO VISUAL:
 *   Fundo escuro (rgba 8,6,18), bordas douradas (#C8A000),
 *   fonte "Press Start 2P" (RPG pixelada), texto âmbar/dourado.
 *
 * CONSTANTES PÚBLICAS:
 *   COR_DIALOGO_*  — usadas pelo Battle.java para estilizar o prompt
 *   FONTE_RPG      — nome da fonte pixelada (importada pelo CSS)
 *
 * USADO POR:
 *   Battle.java           — delega toda criação de UI para cá
 *   BattleAnimations.java — usa FONTE_RPG e criarBotaoBatalha()
 */
public class BattleUI {

    // =========================================================================
    // CONSTANTES DE ESTILO
    // =========================================================================

    /** Fundo do box de diálogo da batalha (quase preto translúcido). */
    public static final String COR_DIALOGO_FUNDO   = "-fx-background-color: rgba(10, 8, 20, 0.95);";

    /** Borda dourada do box de diálogo. */
    public static final String COR_DIALOGO_BORDA   = "-fx-border-color: #C8A000; -fx-border-width: 3;";

    /** Border-radius do box de diálogo. */
    public static final String COR_DIALOGO_RADIUS  = "-fx-border-radius: 12; -fx-background-radius: 12;";

    /** Padding interno do box de diálogo. */
    public static final String COR_DIALOGO_PADDING = "-fx-padding: 18 28;";

    /** Nome da fonte pixelada usada em todos os textos da batalha. */
    public static final String FONTE_RPG           = "Press Start 2P";

    // =========================================================================
    // STATUS BOXES — Criação dos painéis de status
    // =========================================================================

    /**
     * Cria o painel de status do JOGADOR (canto inferior direito).
     * Inclui: nome, nível, barra de HP, texto HP numérico, barra de XP.
     *
     * @param name   Nome do personagem
     * @param level  Nível atual
     * @param hp     HP atual
     * @param hpMax  HP máximo
     * @param xp     XP atual
     * @param xpMax  XP necessário para o próximo nível
     * @return VBox pronto para ser adicionado ao StackPane da batalha.
     */
    public static VBox criarStatusJogador(String name, int level,
                                           int hp, int hpMax,
                                           int xp, int xpMax) {
        // Constrói a base compartilhada (nome, nível, barra HP)
        VBox box = buildStatusBase(name, "Lv " + level, hp, hpMax, 145);

        // ── Texto numérico do HP ──────────────────────────────────────────
        Label hpNum = new Label(Math.max(0, hp) + " / " + hpMax);
        hpNum.setId("hpText");
        hpNum.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #C8A000;" +
            "-fx-font-family: '" + FONTE_RPG + "', monospace;"
        );
        HBox hpRow = new HBox(hpNum);
        hpRow.setAlignment(Pos.CENTER_RIGHT);
        box.getChildren().add(hpRow);

        // ── Barra de XP ──────────────────────────────────────────────────
        HBox xpRow = new HBox(10);
        xpRow.setAlignment(Pos.CENTER_RIGHT);

        Label xpLbl = new Label("XP");
        xpLbl.setStyle(
            "-fx-font-size: 11px; -fx-font-weight: bold;" +
            "-fx-text-fill: #4da6ff;" +
            "-fx-background-color: #1a1a2e;" +
            "-fx-padding: 2 6; -fx-background-radius: 4;" +
            "-fx-font-family: '" + FONTE_RPG + "', monospace;"
        );

        ProgressBar xpBar = new ProgressBar((double) xp / Math.max(1, xpMax));
        xpBar.setId("xpBar");
        xpBar.setPrefWidth(190);
        xpBar.setPrefHeight(8);
        xpBar.setStyle(
            "-fx-accent: #4da6ff;" +
            "-fx-control-inner-background: #1a1a2e;" +
            "-fx-box-border: transparent;"
        );

        xpRow.getChildren().addAll(xpLbl, xpBar);
        box.getChildren().add(xpRow);

        return box;
    }

    /**
     * Cria o painel de status do MONSTRO (canto superior esquerdo).
     * Inclui: nome, barra de HP, texto HP numérico.
     *
     * @param name  Nome do monstro
     * @param hp    HP atual
     * @param hpMax HP máximo
     * @return VBox pronto para ser adicionado ao StackPane da batalha.
     */
    public static VBox criarStatusMonstro(String name, int hp, int hpMax) {
        VBox box = buildStatusBase(name, "", hp, hpMax, 90);

        // Texto numérico do HP do monstro (estilo cinza discreto)
        Label hpNum = new Label(Math.max(0, hp) + " / " + hpMax);
        hpNum.setId("hpText");
        hpNum.setStyle(
            "-fx-font-size: 11px; -fx-font-weight: bold;" +
            "-fx-text-fill: #888;" +
            "-fx-font-family: '" + FONTE_RPG + "', monospace;"
        );
        HBox row = new HBox(hpNum);
        row.setAlignment(Pos.CENTER_RIGHT);
        box.getChildren().add(row);

        return box;
    }

    /**
     * Constrói a estrutura visual base compartilhada entre os dois status boxes.
     * Inclui: cabeçalho (nome + nível) e barra de HP.
     *
     * @param name    Nome exibido no topo do box
     * @param level   Texto de nível ("Lv 3") ou "" se não aplicável
     * @param hp      HP atual
     * @param hpMax   HP máximo
     * @param maxH    Altura máxima do box em pixels
     * @return VBox com base visual montada.
     */
    private static VBox buildStatusBase(String name, String level,
                                         int hp, int hpMax, int maxH) {
        VBox box = new VBox(8);
        box.setStyle(
            "-fx-background-color: rgba(8, 6, 18, 0.92);" +
            "-fx-border-color: #C8A000;" +
            "-fx-border-width: 2.5;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 12 18;"
        );
        box.setMaxWidth(300);
        box.setMaxHeight(maxH);

        // ── Linha do topo: nome e nível lado a lado ───────────────────────
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label nameLbl = new Label(name);
        nameLbl.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: bold;" +
            "-fx-text-fill: #F0E6C0;" +
            "-fx-font-family: '" + FONTE_RPG + "', 'Segoe UI', sans-serif;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label levelLbl = new Label(level);
        levelLbl.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: bold;" +
            "-fx-text-fill: #C8A000;" +
            "-fx-font-family: '" + FONTE_RPG + "', monospace;"
        );

        top.getChildren().addAll(nameLbl, spacer, levelLbl);

        // ── Barra de HP ───────────────────────────────────────────────────
        HBox hpRow = new HBox(10);
        hpRow.setAlignment(Pos.CENTER_LEFT);

        Label hpLbl = new Label("HP");
        hpLbl.setStyle(
            "-fx-font-size: 12px; -fx-font-weight: bold;" +
            "-fx-text-fill: #C8A000;" +
            "-fx-background-color: #1a1a2e;" +
            "-fx-padding: 2 6; -fx-background-radius: 4;" +
            "-fx-font-family: '" + FONTE_RPG + "', monospace;"
        );

        double ratio = Math.max(0.0, (double) hp / Math.max(1, hpMax));
        ProgressBar hpBar = new ProgressBar(ratio);
        hpBar.setId("hpBar");
        hpBar.setPrefWidth(190);
        hpBar.setPrefHeight(14);
        hpBar.setStyle(
            "-fx-accent: " + calcularCorHp(ratio) + ";" +
            "-fx-control-inner-background: #1a1a2e;" +
            "-fx-box-border: transparent;"
        );

        hpRow.getChildren().addAll(hpLbl, hpBar);
        box.getChildren().addAll(top, hpRow);

        return box;
    }

    // =========================================================================
    // BUTTONS — Criação de botões estilizados
    // =========================================================================

    /**
     * Cria um botão no estilo RPG pixelado com hover interativo.
     *
     * @param text      Texto exibido no botão
     * @param baseColor Cor de fundo normal (hex CSS, ex: "#3a2a50")
     * @param hoverColor Cor de fundo ao passar o mouse (ex: "#5a3a70")
     * @return Button pronto para uso na batalha.
     */
    public static Button criarBotaoBatalha(String text, String baseColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setPrefSize(175, 60);
        btn.setWrapText(true);

        String styleBase =
            "-fx-background-color: " + baseColor + ";" +
            "-fx-text-fill: #F0E6C0;" +
            "-fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-font-family: '" + FONTE_RPG + "', 'Segoe UI', sans-serif;" +
            "-fx-border-color: #C8A000; -fx-border-width: 2;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;" +
            "-fx-cursor: hand; -fx-alignment: center-left;" +
            "-fx-padding: 8 14;";

        String styleHover =
            "-fx-background-color: " + hoverColor + ";" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-font-family: '" + FONTE_RPG + "', 'Segoe UI', sans-serif;" +
            "-fx-border-color: #FFD700; -fx-border-width: 2;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;" +
            "-fx-cursor: hand; -fx-alignment: center-left;" +
            "-fx-padding: 8 14;";

        btn.setStyle(styleBase);
        btn.setOnMouseEntered(e -> btn.setStyle(styleHover));
        btn.setOnMouseExited(e  -> btn.setStyle(styleBase));

        return btn;
    }

    // =========================================================================
    // HP BAR UPDATE — Atualização animada da barra de HP
    // =========================================================================

    /**
     * Atualiza a barra de HP de um status box com animação suave.
     * Procura os elementos pelo ID interno do VBox.
     *
     * @param status VBox do status (jogador ou monstro).
     * @param hp     HP atual.
     * @param hpMax  HP máximo.
     */
    public static void atualizarBarraHp(VBox status, int hp, int hpMax) {
        ProgressBar bar = (ProgressBar) status.lookup("#hpBar");
        double ratio    = Math.max(0.0, (double) hp / Math.max(1, hpMax));

        if (bar != null) {
            // Animação suave de 400ms para a transição da barra
            Timeline animation = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(bar.progressProperty(), bar.getProgress())),
                new KeyFrame(Duration.millis(400),
                    new KeyValue(bar.progressProperty(), ratio))
            );
            animation.play();

            // Atualiza a cor conforme o percentual de HP
            bar.setStyle(
                "-fx-accent: " + calcularCorHp(ratio) + ";" +
                "-fx-control-inner-background: #1a1a2e;" +
                "-fx-box-border: transparent;"
            );
        }

        // Atualiza o texto numérico HP se presente (label de texto)
        Label textLabel = (Label) status.lookup("#hpText");
        if (textLabel != null) {
            textLabel.setText(Math.max(0, hp) + " / " + hpMax);
        }
    }

    // =========================================================================
    // HELPER — Cor da barra de HP
    // =========================================================================

    /**
     * Retorna a cor CSS da barra de HP baseada no percentual atual.
     *
     * ┌───────────┬───────────────────────────────┐
     * │ Ratio     │ Cor (significado)             │
     * ├───────────┼───────────────────────────────┤
     * │ > 50%     │ Verde  (#2ECC71) — saudável   │
     * │ 20%–50%   │ Laranja (#F39C12) — atenção   │
     * │ < 20%     │ Vermelho (#E74C3C) — perigo   │
     * └───────────┴───────────────────────────────┘
     *
     * @param ratio Percentual de HP (0.0 a 1.0).
     * @return String com o código de cor hexadecimal CSS.
     */
    public static String calcularCorHp(double ratio) {
        if (ratio > 0.5) return "#2ECC71";  // Verde — saudável
        if (ratio > 0.2) return "#F39C12";  // Laranja — atenção
        return "#E74C3C";                   // Vermelho — perigo crítico
    }
}


