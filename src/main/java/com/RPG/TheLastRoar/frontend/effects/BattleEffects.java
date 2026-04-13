package com.RPG.TheLastRoar.frontend.effects;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * ============================================================
 * BattleEffects.java — Sistema centralizado de efeitos visuais
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Gerencia TODOS os efeitos visuais da batalha:
 *   - Screen shake (tremor de tela) em acertos críticos
 *   - Partículas de impacto diferenciadas por tipo de dano
 *   - Recuperação com fade-out automático
 *
 * EFEITOS FORNECIDOS:
 *   ┌──────────────────────────────────────────────────────────┐
 *   │ playScreenShake()           → Tremor de tela            │
 *   │ createImpactParticles()     → Partículas de impacto     │
 *   │ animateFloatingDamage()     → Números flutuantes        │
 *   └──────────────────────────────────────────────────────────┘
 *
 * TIPOS DE EFEITO:
 *   CRITICAL   → Vermelho (#FF4444), tremor forte, múltiplas partículas
 *   NORMAL     → Branco (#FFFFFF), tremor leve
 *   HEAL       → Verde (#44FF44), tremor mínimo
 *   MISS       → Cinza (#888888), sem tremor
 *
 * USADO POR:
 *   Battle.java         — chama em cada ação de combat (ataque, cura)
 *   BattleAnimations.java — integra com animações existentes
 */
public class BattleEffects {

    // =========================================================================
    // CONSTANTES DE CONFIGURAÇÃO
    // =========================================================================

    /** Duração do tremor de tela em ms. */
    private static final int SHAKE_DURATION_MS = 250;

    /** Número de tremores individuais durante a duração. */
    private static final int SHAKE_ITERATIONS = 8;

    /** Número de partículas criadas por impacto. */
    private static final int PARTICLES_COUNT = 12;

    /** Duração da animação de partículas em ms. */
    private static final int PARTICLES_DURATION_MS = 600;

    /** Raio da partícula individual. */
    private static final double PARTICLE_RADIUS = 4.0;

    // =========================================================================
    // SCREEN SHAKE — Tremor de tela em acertos críticos
    // =========================================================================

    /**
     * Reproduz um tremor de tela sutil.
     * Mais intenso em críticos, leve em dano normal.
     *
     * Implementação:
     *   1. Anima 8 pequenos deslocamentos da pane em direções aleatórias
     *   2. Cada tremor dura ~30ms
     *   3. Total: ~250ms
     *   4. Restora pane à posição original
     *
     * @param pane      Pane que será abalada (geralmente root da cena)
     * @param intensity "CRITICAL" (forte), "NORMAL" (leve), "WEAK" (mínimo)
     *
     * EXEMPLO:
     *   BattleEffects.playScreenShake(battleRoot, "CRITICAL");
     *   // Tela tremeu por 250ms
     */
    public static void playScreenShake(Pane pane, String intensity) {
        if (pane == null) return;

        double maxOffset = switch (intensity) {
            case "CRITICAL" -> 8.0;    // Crítico: tremor forte
            case "NORMAL"   -> 4.0;    // Normal: tremor médio
            case "WEAK"     -> 2.0;    // Fraco: tremor leve
            default         -> 2.0;
        };

        // Guarda posição original
        double originalX = pane.getTranslateX();
        double originalY = pane.getTranslateY();

        // Cria timeline com múltiplas keyframes de tremor
        Timeline shakeTimeline = new Timeline();
        shakeTimeline.setCycleCount(SHAKE_ITERATIONS);
        shakeTimeline.setAutoReverse(true);

        // Gera offsets aleatórios para cada tremor
        for (int i = 0; i < SHAKE_ITERATIONS; i++) {
            double randomX = (Math.random() - 0.5) * maxOffset;
            double randomY = (Math.random() - 0.5) * maxOffset;

            Duration keyTime = Duration.millis((i + 1) * (SHAKE_DURATION_MS / SHAKE_ITERATIONS));
            shakeTimeline.getKeyFrames().addAll(
                new KeyFrame(keyTime, new KeyValue(pane.translateXProperty(), randomX)),
                new KeyFrame(keyTime, new KeyValue(pane.translateYProperty(), randomY))
            );
        }

        // Restaura posição ao terminar
        shakeTimeline.setOnFinished(e -> {
            pane.setTranslateX(originalX);
            pane.setTranslateY(originalY);
        });

        shakeTimeline.play();
    }

    // =========================================================================
    // IMPACT PARTICLES — Partículas de impacto diferenciadas
    // =========================================================================

    /**
     * Cria partículas de impacto que explodem radialmente e desaparecem.
     * Cor e quantidade variam conforme o tipo de dano.
     *
     * Implementação:
     *   1. Cria N círculos pequenos na posição (x, y)
     *   2. Cada círculo se move em direção radial aleatória
     *   3. Fade out junto com movimento
     *   4. Remove-se do pai ao terminar
     *
     * TIPOS DE PARTÍCULA:
     *   CRITICAL  → Vermelho escuro (#CC3333), 15 partículas
     *   NORMAL    → Branco/amarelo (#FFDD44), 12 partículas
     *   HEAL      → Verde (#44DD44), 10 partículas
     *   MISS      → Cinza (#999999), 5 partículas
     *
     * @param pane      Pane onde adicionar as partículas
     * @param x         Posição X do impacto
     * @param y         Posição Y do impacto
     * @param damageType Tipo: "CRITICAL", "NORMAL", "HEAL", "MISS"
     *
     * EXEMPLO:
     *   BattleEffects.createImpactParticles(battleRoot, 400, 300, "CRITICAL");
     *   // Partículas vermelhas explodem do ponto (400, 300)
     */
    public static void createImpactParticles(Pane pane, double x, double y, String damageType) {
        if (pane == null) return;

        // Configuração por tipo de dano
        Color particleColor = switch (damageType) {
            case "CRITICAL" -> Color.web("#FF6666"); // Vermelho brilhante
            case "NORMAL"   -> Color.web("#FFDD44"); // Amarelo
            case "HEAL"     -> Color.web("#44FF44"); // Verde brilhante
            default         -> Color.web("#999999"); // Cinza (MISS)
        };

        int particleCount = switch (damageType) {
            case "CRITICAL" -> 15;
            case "NORMAL"   -> 12;
            case "HEAL"     -> 10;
            default         -> 5;
        };

        // Cria partículas
        for (int i = 0; i < particleCount; i++) {
            Circle particle = new Circle(PARTICLE_RADIUS, particleColor);
            particle.setOpacity(0.9);
            particle.setCenterX(x);
            particle.setCenterY(y);
            pane.getChildren().add(particle);

            // Calcula direção radial aleatória
            double angle = (Math.PI * 2 * i) / particleCount + (Math.random() - 0.5) * 0.4;
            double distance = 80.0 + Math.random() * 40; // 80-120 pixels
            double targetX = x + Math.cos(angle) * distance;
            double targetY = y + Math.sin(angle) * distance;

            // Timeline para movimento + fade out
            Timeline particleTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(particle.centerXProperty(), x),
                    new KeyValue(particle.centerYProperty(), y),
                    new KeyValue(particle.opacityProperty(), 0.9)
                ),
                new KeyFrame(Duration.millis(PARTICLES_DURATION_MS),
                    new KeyValue(particle.centerXProperty(), targetX),
                    new KeyValue(particle.centerYProperty(), targetY),
                    new KeyValue(particle.opacityProperty(), 0.0)
                )
            );

            particleTimeline.setOnFinished(e -> pane.getChildren().remove(particle));
            particleTimeline.play();
        }
    }

    // =========================================================================
    // FLOATING DAMAGE NUMBER — Números de dano animados
    // =========================================================================

    /**
     * Cria um número flutuante que sobe e desaparece.
     * Usada em conjunto com FloatingDamageNumber.java para gerenciamento.
     *
     * Este método é um helper que cria a animação de forma simplificada.
     * Para controle mais fino, use FloatingDamageNumber diretamente.
     *
     * CORES:
     *   Normal damage   → Branco (#FFFFFF)
     *   Critical damage → Vermelho (#FF4444)
     *   Heal            → Verde (#44FF44)
     *   Miss            → Cinza (#888888)
     *
     * ANIMAÇÃO:
     *   - Começa na posição (x, y)
     *   - Sobe 60 pixels em 1 segundo
     *   - Fade out simultaneamente
     *   - Remove-se ao terminar
     *
     * @param pane        Pane pai (geralmente arena/root da batalha)
     * @param x           Posição X inicial
     * @param y           Posição Y inicial
     * @param damage      Número display (ex: "45", "MISS")
     * @param isDamage    true=dano, false=cura
     * @param isCritical  true=crítico, false=normal
     *
     * EXEMPLO:
     *   BattleEffects.animateFloatingDamage(root, 200, 150, "45", true, true);
     *   // Mostra "45" em vermelho que sobe e desaparece
     */
    public static void animateFloatingDamage(Pane pane, double x, double y,
                                              String damage, boolean isDamage,
                                              boolean isCritical) {
        if (pane == null) return;

        FloatingDamageNumber floatingNum = new FloatingDamageNumber(
            damage,
            isDamage ? (isCritical ? "CRITICAL" : "NORMAL") : "HEAL",
            x, y
        );
        floatingNum.attachTo(pane);
    }

    // =========================================================================
    // UTILIDADE: Gerador de direções aleatórias
    // =========================================================================

    /**
     * Gera um ponto em direção aleatória.
     * Usado para calcular trajetória de partículas.
     *
     * @param distance Distância do ponto origem
     * @return Point2D com posição aleatória
     */
    private static Point2D getRandomDirection(double distance) {
        double angle = Math.random() * Math.PI * 2;
        double x = Math.cos(angle) * distance;
        double y = Math.sin(angle) * distance;
        return new Point2D(x, y);
    }

    // =========================================================================
    // UTILITÁRIO: Interpolar cores
    // =========================================================================

    /**
     * Interpola suavemente entre duas cores.
     * Usado para efeitos gradientes em partículas.
     *
     * @param startColor Cor inicial
     * @param endColor   Cor final
     * @param progress   Progresso 0.0-1.0
     * @return Cor interpolada
     */
    public static Color interpolateColor(Color startColor, Color endColor, double progress) {
        return new Color(
            startColor.getRed() + (endColor.getRed() - startColor.getRed()) * progress,
            startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * progress,
            startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * progress,
            startColor.getOpacity() + (endColor.getOpacity() - startColor.getOpacity()) * progress
        );
    }
}
