package com.RPG.TheLastRoar.frontend.effects;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * ============================================================
 * FloatingDamageNumber.java — Números de dano animados
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Gerencia a animação individual de um número flutuante (damage número).
 *   Cada instância representa UM número que sobe e desaparece.
 *
 * CICLO DE VIDA:
 *   1. new FloatingDamageNumber(...) → Criação
 *   2. attachTo(pane)               → Adiciona à cena + inicia animação
 *   3. Timeline.play()              → Sobe e desaparece (1 segundo)
 *   4. onFinished()                 → Remove-se e morre
 *
 * TIPOS DE DANO:
 *   NORMAL    → Branco (#FFFFFF), tamanho 20px
 *   CRITICAL  → Vermelho (#FF4444), tamanho 26px, BOLD
 *   HEAL      → Verde (#44FF44), tamanho 22px
 *   MISS      → Cinza (#888888), tamanho 16px, itálico
 *
 * PROPRIEDADES VISUAIS:
 *   - Fonte: Press Start 2P (pixel RPG)
 *   - Sombra: Drop shadow sutil (#000000, 50% opacidade)
 *   - Animação: 1000ms (sobe 60px + fade out)
 *   - Curve: Linear (sem easing)
 *
 * EXEMPLO DE USO:
 *   FloatingDamageNumber num = new FloatingDamageNumber("45", "CRITICAL", 200, 300);
 *   num.attachTo(battleRoot);
 *   // → Mostra "45" em vermelho no ponto (200, 300), sobe e desaparece
 *
 * USADO POR:
 *   BattleEffects.animateFloatingDamage() — chamada principal
 *   Battle.java — poderia chamar diretamente se precisasse de controle fino
 */
public class FloatingDamageNumber {

    // =========================================================================
    // CONSTANTES DE CONFIGURAÇÃO
    // =========================================================================

    /** Duração total da animação em ms. */
    private static final int ANIMATION_DURATION_MS = 1000;

    /** Distância que o número sobe em pixels. */
    private static final double RISE_DISTANCE = 60.0;

    /** Nome da fonte pixelada. */
    private static final String FONT_PIXEL = "Press Start 2P";

    // =========================================================================
    // CAMPOS DE INSTÂNCIA
    // =========================================================================

    private final Text textNode;     // Nó visual do número
    private final String damageType; // NORMAL, CRITICAL, HEAL, MISS
    private final double startX;     // Posição X inicial
    private final double startY;     // Posição Y inicial
    private Pane parentPane;         // Pane pai (adicionado em attachTo)
    private Timeline animation;      // Timeline da animação

    // =========================================================================
    // CONSTRUTOR
    // =========================================================================

    /**
     * Cria um número flutuante com os parâmetros dados.
     * A animação NÃO inicia automaticamente — chamar attachTo() para iniciar.
     *
     * @param displayText Texto a mostrar (ex: "45", "MISS", "999")
     * @param damageType  Tipo de efeito: "NORMAL", "CRITICAL", "HEAL", "MISS"
     * @param x           Posição X inicial
     * @param y           Posição Y inicial
     */
    public FloatingDamageNumber(String displayText, String damageType, double x, double y) {
        this.damageType = damageType;
        this.startX = x;
        this.startY = y;

        // Cria o nó de texto
        this.textNode = new Text(displayText);
        this.textNode.setLayoutX(x);
        this.textNode.setLayoutY(y);

        // Aplica estilos conforme tipo de dano
        applyStyles();
    }

    // =========================================================================
    // ESTILOS — Aplicação de cores e fontes por tipo
    // =========================================================================

    /**
     * Aplica estilos visuais ao Text conforme o tipo de dano.
     *
     * Estilos:
     *   CRITICAL → Vermelho, 26px, BOLD, sombra forte
     *   NORMAL   → Branco, 20px, regular, sombra média
     *   HEAL     → Verde, 22px, regular, sombra leve
     *   MISS     → Cinza, 16px, itálico, sombra fraca
     */
    private void applyStyles() {
        switch (damageType) {
            case "CRITICAL" -> {
                textNode.setFont(Font.font(FONT_PIXEL, FontWeight.BOLD, 26));
                textNode.setFill(Color.web("#FF4444")); // Vermelho
                createShadow(Color.web("#000000"));
            }
            case "NORMAL" -> {
                textNode.setFont(Font.font(FONT_PIXEL, FontWeight.NORMAL, 20));
                textNode.setFill(Color.web("#FFFFFF")); // Branco
                createShadow(Color.web("#000000"));
            }
            case "HEAL" -> {
                textNode.setFont(Font.font(FONT_PIXEL, FontWeight.NORMAL, 22));
                textNode.setFill(Color.web("#44FF44")); // Verde
                createShadow(Color.web("#000000"));
            }
            case "MISS" -> {
                textNode.setFont(Font.font(FONT_PIXEL, FontWeight.NORMAL, 16));
                textNode.setFill(Color.web("#888888")); // Cinza
                textNode.setStyle("-fx-font-style: italic;");
                createShadow(Color.web("#000000"));
            }
        }
    }

    /**
     * Cria e aplica uma sombra drop shadow ao texto.
     *
     * @param shadowColor Cor da sombra
     */
    private void createShadow(Color shadowColor) {
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setColor(shadowColor.deriveColor(1, 1, 1, 0.5)); // 50% opacidade
        shadow.setRadius(4);
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        textNode.setEffect(shadow);
    }

    // =========================================================================
    // ATTACHING — Adiciona à cena e inicia animação
    // =========================================================================

    /**
     * Adiciona este número à pane pai e inicia a animação.
     *
     * FLUXO:
     *   1. Adiciona textNode ao pai
     *   2. Cria timeline de animação (sobe + fade out)
     *   3. Inicia a animação
     *   4. Remove-se ao terminar
     *
     * @param pane Pane para adicionar o número (geralmente root da batalha)
     */
    public void attachTo(Pane pane) {
        this.parentPane = pane;
        pane.getChildren().add(textNode);

        // Cria a animação de subida + desaparecimento
        createAnimation();
        animation.play();
    }

    // =========================================================================
    // ANIMAÇÃO — Timeline de subida e desvanecimento
    // =========================================================================

    /**
     * Cria a timeline da animação.
     * Implementação:
     *   - Y: startY → startY - RISE_DISTANCE (sobe)
     *   - Opacity: 1.0 → 0.0 (desvanece)
     *   - Duração: 1000ms
     *   - Curva: Linear (sem easing)
     */
    private void createAnimation() {
        double endY = startY - RISE_DISTANCE;

        animation = new Timeline(
            // Keyframe 0ms: posição e opacidade iniciais
            new KeyFrame(Duration.ZERO,
                new KeyValue(textNode.layoutYProperty(), startY),
                new KeyValue(textNode.opacityProperty(), 1.0)
            ),
            // Keyframe final: posição após subida + desvanecimento
            new KeyFrame(Duration.millis(ANIMATION_DURATION_MS),
                new KeyValue(textNode.layoutYProperty(), endY),
                new KeyValue(textNode.opacityProperty(), 0.0)
            )
        );

        // Remove-se da cena ao terminar
        animation.setOnFinished(event -> {
            if (parentPane != null && parentPane.getChildren().contains(textNode)) {
                parentPane.getChildren().remove(textNode);
            }
        });
    }

    // =========================================================================
    // CONTROLE — Métodos para controlar a animação
    // =========================================================================

    /**
     * Para a animação (pausa, não remove).
     * Usado se quisermos resumir depois.
     */
    public void pause() {
        if (animation != null) {
            animation.pause();
        }
    }

    /**
     * Resume a animação parada.
     */
    public void resume() {
        if (animation != null && animation.getStatus() == javafx.animation.Animation.Status.PAUSED) {
            animation.play();
        }
    }

    /**
     * Para e remove o número imediatamente.
     */
    public void stop() {
        if (animation != null) {
            animation.stop();
        }
        if (parentPane != null && parentPane.getChildren().contains(textNode)) {
            parentPane.getChildren().remove(textNode);
        }
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    /**
     * @return Nó visual do texto (permitesCustomização se necessário)
     */
    public Text getTextNode() {
        return textNode;
    }

    /**
     * @return Tipo de dano deste número
     */
    public String getDamageType() {
        return damageType;
    }

    /**
     * @return Status atual da animação
     */
    public javafx.animation.Animation.Status getAnimationStatus() {
        return animation != null ? animation.getStatus() : null;
    }
}
