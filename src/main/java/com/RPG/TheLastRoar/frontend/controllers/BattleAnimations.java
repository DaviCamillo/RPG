package com.RPG.TheLastRoar.frontend.controllers;

import com.RPG.TheLastRoar.App;
import com.RPG.TheLastRoar.frontend.screens.Battle;
import com.RPG.TheLastRoar.frontend.screens.BattleUI;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * ============================================================
 * BattleAnimations.java — Animações da tela de batalha
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Centraliza todas as animações visuais que ocorrem durante
 *   batalhas. Battle.java não faz animações diretamente —
 *   delega para esta classe.
 *
 * ANIMAÇÕES DISPONÍVEIS:
 *   ┌──────────────────────┬───────────────────────────────────────────┐
 *   │ Método               │ Descrição                                 │
 *   ├──────────────────────┼───────────────────────────────────────────┤
 *   │ animarAtaque()       │ Sprite avança em direção ao inimigo       │
 *   │ animarTremor()       │ Sprite chacoalha ao receber dano          │
 *   │ animarHitFlash()     │ Sprite pisca branco ao ser atingida       │
 *   │ exibirDanoFlutuante()│ Número de dano sobe e some na tela        │
 *   │ ativarGameOver()     │ Tela de Game Over com animação de entrada │
 *   └──────────────────────┴───────────────────────────────────────────┘
 *
 * PADRÃO DE USO:
 *   Todas as animações que precisam de callback pós-animação recebem
 *   um Runnable onFinished que é executado ao término da animação.
 *   Isso garante que a lógica de jogo só executa após o visual.
 *
 * USADO POR:
 *   Battle.java — chama as animações nos momentos corretos dos turnos
 */
public class BattleAnimations {

    // =========================================================================
    // ATTACK ANIMATION
    // =========================================================================

    /**
     * Anima o ataque do personagem: sprite avança, golpeia e recua.
     *
     * SEQUÊNCIA:
     *   1. Sprite avança 120px para a direita e 40px para cima (180ms)
     *   2. Sprite recua de volta à posição original (220ms)
     *   3. onFinished é chamado (onde o dano é calculado)
     *
     * A lógica de dano DEVE ficar no onFinished para sincronizar
     * com o visual — não calcule antes.
     *
     * @param attacker  Sprite que se move (jogador ou monstro)
     * @param victim    Sprite que recebe (referência visual, não anima)
     * @param onFinished Callback com a lógica de dano a aplicar
     */
    public static void animarAtaque(ImageView attacker, ImageView victim, Runnable onFinished) {
        // Avanço em direção ao inimigo
        TranslateTransition advance = new TranslateTransition(Duration.millis(180), attacker);
        advance.setByX(120);
        advance.setByY(-40);

        // Retorno à posição original
        TranslateTransition retreat = new TranslateTransition(Duration.millis(220), attacker);
        retreat.setByX(-120);
        retreat.setByY(40);

        SequentialTransition seq = new SequentialTransition(advance, retreat);
        seq.setOnFinished(e -> onFinished.run());
        seq.play();
    }

    // =========================================================================
    // DAMAGE RECEIVED ANIMATIONS
    // =========================================================================

    /**
     * Anima o tremor da sprite ao receber dano (chacoalha horizontalmente).
     *
     * PARÂMETROS:
     *   - 8 ciclos de ±12px a 40ms por ciclo = ~320ms de tremor total
     *   - Retorna automaticamente à posição X original ao terminar
     *
     * @param target Sprite que recebe o tremor.
     */
    public static void animarTremor(ImageView target) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(40), target);
        shake.setByX(12);
        shake.setCycleCount(8);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> target.setTranslateX(0)); // Garante reset da posição
        shake.play();
    }

    /**
     * Anima o flash branco na sprite ao ser atingida.
     * Pisca duas vezes — efeito visual de dano recebido.
     * Após o flash, adiciona um DropShadow colorido permanente:
     *   - Jogador (scaleX == -1): sombra dourada
     *   - Inimigo: sombra vermelha
     *
     * @param target Sprite que recebe o flash de dano.
     */
    public static void animarHitFlash(ImageView target) {
        ColorAdjust flash = new ColorAdjust();
        flash.setBrightness(0);
        target.setEffect(flash);

        // Dois picos de brilho máximo simulando dois flashes rápidos
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,        new KeyValue(flash.brightnessProperty(), 0)),
            new KeyFrame(Duration.millis(80),  new KeyValue(flash.brightnessProperty(), 1.0)),
            new KeyFrame(Duration.millis(160), new KeyValue(flash.brightnessProperty(), 0)),
            new KeyFrame(Duration.millis(240), new KeyValue(flash.brightnessProperty(), 0.8)),
            new KeyFrame(Duration.millis(320), new KeyValue(flash.brightnessProperty(), 0))
        );

        // Após o flash, aplica sombra colorida permanente no sprite
        timeline.setOnFinished(e -> {
            DropShadow shadow = new DropShadow();
            boolean isPlayer  = (target.getScaleX() == -1);
            shadow.setColor(isPlayer
                ? Color.web("#FFD700", 0.6)  // Sombra dourada no jogador
                : Color.web("#FF0000", 0.7)  // Sombra vermelha no inimigo
            );
            shadow.setRadius(30);
            shadow.setSpread(0.15);
            target.setEffect(shadow);
        });
        timeline.play();
    }

    // =========================================================================
    // FLOATING DAMAGE NUMBER
    // =========================================================================

    /**
     * Exibe um número de dano (ou cura) flutuando na tela e sumindo.
     *
     * COMPORTAMENTO:
     *   - Dano: vermelho, aparece no lado esquerdo/centro, sobe 80px e some
     *   - Cura:  verde,   aparece no lado direito/centro, sobe 80px e some
     *   - Fonte maior para valores >= 10 (impacto visual maior)
     *   - Duração total: ~1100ms (300ms visível + 800ms fade out)
     *
     * @param root    StackPane raiz da batalha (onde o número é adicionado)
     * @param value   Valor numérico a exibir
     * @param isHeal  Se true, mostra em verde com "+"; se false, vermelho com "-"
     */
    public static void exibirDanoFlutuante(StackPane root, int value, boolean isHeal) {
        Label dmgLabel = new Label((isHeal ? "+" : "-") + value);
        dmgLabel.setStyle(
            "-fx-font-size: " + (value >= 10 ? "52" : "40") + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'Impact', '" + BattleUI.FONTE_RPG + "', sans-serif;" +
            "-fx-text-fill: " + (isHeal ? "#2ECC71" : "#E74C3C") + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 4, 0.5, 0, 0);"
        );
        dmgLabel.setMouseTransparent(true);

        // Posicionamento: cura à direita, dano à esquerda
        StackPane.setAlignment(dmgLabel, isHeal ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        StackPane.setMargin(dmgLabel,    isHeal
            ? new Insets(0, 200, 150, 0)
            : new Insets(100, 0, 0, 200)
        );

        root.getChildren().add(dmgLabel);

        // Sobe enquanto some
        TranslateTransition rise = new TranslateTransition(Duration.millis(800), dmgLabel);
        rise.setByY(-80);

        FadeTransition fade = new FadeTransition(Duration.millis(800), dmgLabel);
        fade.setDelay(Duration.millis(300));   // Aguarda 300ms visível antes de sumir
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        ParallelTransition anim = new ParallelTransition(rise, fade);
        anim.setOnFinished(e -> root.getChildren().remove(dmgLabel));
        anim.play();
    }

    // =========================================================================
    // GAME OVER SCREEN
    // =========================================================================

    /**
     * Exibe a tela de Game Over sobre a batalha com animação de entrada.
     *
     * SEQUÊNCIA DE ANIMAÇÕES:
     *   1. Overlay escuro aparece com fade in (800ms)
     *   2. Texto "GAME OVER" escala de 0.3x para 1.0x (600ms)
     *   3. Subtítulo e botão surgem com fade in (500ms)
     *   4. Texto "GAME OVER" pulsa suavemente no loop infinito
     *
     * @param rootNode StackPane raiz da batalha (overlay é adicionado aqui)
     * @param app      Instância do App para chamar showMainMenu() ao voltar
     */
    public static void ativarGameOver(StackPane rootNode, App app) {
        // ── Overlay ───────────────────────────────────────────────────────
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(10, 0, 0, 0.9);");
        overlay.setOpacity(0);

        // ── Texto principal ───────────────────────────────────────────────
        Label title = new Label("GAME OVER");
        title.setStyle(
            "-fx-text-fill: #CC0000;" +
            "-fx-font-size: 96px; -fx-font-weight: bold;" +
            "-fx-font-family: 'Impact', '" + BattleUI.FONTE_RPG + "', sans-serif;"
        );
        title.setScaleX(0.3);
        title.setScaleY(0.3);

        // ── Subtítulo ─────────────────────────────────────────────────────
        Label subtitle = new Label("Você foi derrotado...");
        subtitle.setStyle(
            "-fx-text-fill: #888;" +
            "-fx-font-size: 22px;" +
            "-fx-font-family: '" + BattleUI.FONTE_RPG + "', 'Segoe UI', sans-serif;"
        );
        subtitle.setOpacity(0);

        // ── Botão de voltar ao menu ───────────────────────────────────────
        Button menuBtn = BattleUI.criarBotaoBatalha("↩  VOLTAR AO MENU", "#8B0000", "#5C0000");
        menuBtn.setOpacity(0);
        menuBtn.setOnAction(e -> {
            Battle.resetInBattle(); // Limpa o flag antes de voltar
            app.showMainMenu();
        });

        // ── Monta o layout do overlay ─────────────────────────────────────
        VBox content = new VBox(24, title, subtitle, menuBtn);
        content.setAlignment(Pos.CENTER);
        overlay.getChildren().add(content);
        StackPane.setAlignment(content, Pos.CENTER);
        rootNode.getChildren().add(overlay);

        // ── Sequência de animações ────────────────────────────────────────
        FadeTransition fadeIn    = new FadeTransition(Duration.millis(800), overlay);
        fadeIn.setToValue(1);

        ScaleTransition scaleUp  = new ScaleTransition(Duration.millis(600), title);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        // Pulso infinito no texto após a entrada
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO,        new KeyValue(title.scaleXProperty(), 1.0)),
            new KeyFrame(Duration.millis(700),  new KeyValue(title.scaleXProperty(), 1.05)),
            new KeyFrame(Duration.millis(1400), new KeyValue(title.scaleXProperty(), 1.0))
        );
        pulse.setCycleCount(Timeline.INDEFINITE);

        FadeTransition fadeSubtitle = new FadeTransition(Duration.millis(500), subtitle);
        fadeSubtitle.setToValue(1);
        FadeTransition fadeBtn      = new FadeTransition(Duration.millis(500), menuBtn);
        fadeBtn.setToValue(1);

        // Encadeia: fade in overlay → escala título → surgem subtítulo+botão → pulso
        SequentialTransition sequence = new SequentialTransition(
            fadeIn, scaleUp, new ParallelTransition(fadeSubtitle, fadeBtn)
        );
        sequence.setOnFinished(e -> pulse.play());
        sequence.play();
    }
}


