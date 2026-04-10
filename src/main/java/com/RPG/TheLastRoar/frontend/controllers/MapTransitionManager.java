package com.RPG.TheLastRoar.frontend.controllers;

import com.RPG.TheLastRoar.backend.managers.EnemyManager;

import javafx.animation.FadeTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * ============================================================
 * MapTransitionManager.java — Troca de Cenários/Mapas
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Gerencia a transição entre mapas com fade out/in e teletransporte
 *   do personagem para o lado oposto de onde saiu.
 *
 * FLUXO DE TRANSIÇÃO:
 *   1. Detecta que player chegou na borda (playerMovementTimer)
 *   2. Chama changeMap() com a direção ("CIMA", "BAIXO", etc)
 *   3. Fade out 300ms → troca mapa no meio → Fade in 300ms
 *   4. Posição do player é ajustada conforme a direção
 *   5. EnemyManager é reconfigurado para o novo mapa
 *   6. AnimationTimers são reiniciados
 *
 * MAPA ÍNDICES:
 *   0: mapa_padrao.png     (Goblin × 2 + NPC loja)
 *   1: mapa_padrao2.png    (GoblinExp × 2)
 *   2: mapa_padrao3.png    (GoblinBoss × 1)
 *
 * DIRECTIONALITY:
 *   CIMA / DIREITA     → Próximo mapa (index++)
 *   BAIXO / ESQUERDA   → Mapa anterior (index--)
 *
 * USADO POR: App.java (playerMovementTimer callback)
 * DEPENDENCIES: EnemyManager
 *
 * @author RPG Team
 */
public class MapTransitionManager {

    // =========================================================================
    // FIELDS — Configuração de mapas
    // =========================================================================

    private final String[] mapList;
    private int currentMapIndex;
    private double screenW;
    private double screenH;

    // =========================================================================
    // FIELDS — Referências
    // =========================================================================

    private ImageView playerView;
    private ImageView mapView;
    private EnemyManager enemyManager;

    // =========================================================================
    // FIELDS — Estado de transição
    // =========================================================================

    private boolean isTransitioning = false;

    // =========================================================================
    // FIELDS — Callbacks
    // =========================================================================

    private Runnable onTransitionStart;
    private Runnable onTransitionEnd;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    /**
     * Cria o gerenciador de transição de mapas.
     *
     * @param mapList           Lista de nomes de mapas
     * @param currentMapIndex   Índice inicial do mapa
     * @param playerView        ImageView do sprite do player
     * @param mapView           ImageView do fundo do mapa
     * @param enemyManager      Gerenciador de inimigos (será reconfigurado)
     * @param screenW           Largura da tela
     * @param screenH           Altura da tela
     * @param onTransitionStart Callback ao iniciar transição (usar para pausar timers)
     * @param onTransitionEnd   Callback ao terminar transição (usar para retomar timers)
     */
    public MapTransitionManager(String[] mapList, int currentMapIndex,
                               ImageView playerView, ImageView mapView,
                               EnemyManager enemyManager,
                               double screenW, double screenH,
                               Runnable onTransitionStart,
                               Runnable onTransitionEnd) {
        this.mapList = mapList;
        this.currentMapIndex = currentMapIndex;
        this.playerView = playerView;
        this.mapView = mapView;
        this.enemyManager = enemyManager;
        this.screenW = screenW;
        this.screenH = screenH;
        this.onTransitionStart = onTransitionStart;
        this.onTransitionEnd = onTransitionEnd;
    }

    // =========================================================================
    // PUBLIC METHODS
    // =========================================================================

    /**
     * Transiciona para o mapa anterior ou próximo com fade out/in.
     *
     * MAPEAMENTO:
     *   "CIMA" / "DIREITA"   → mapa[currentIndex + 1]
     *   "BAIXO" / "ESQUERDA" → mapa[currentIndex - 1]
     *
     * @param direction String indicando a direção ("CIMA", "BAIXO", "ESQUERDA", "DIREITA")
     * @param onComplete Callback ao concluir a transição
     */
    public void changeMap(String direction, Runnable onComplete) {
        if (isTransitioning) return;

        // ── Calcula próximo índice ────────────────────────────────────────
        int newIndex = currentMapIndex;
        if (direction.equals("CIMA") || direction.equals("DIREITA")) {
            newIndex++;
        } else {
            newIndex--;
        }

        // ── Valida limites ────────────────────────────────────────────────
        if (newIndex >= mapList.length || newIndex < 0) return;

        isTransitioning = true;
        if (onTransitionStart != null) {
            onTransitionStart.run();
        }

        final int finalIndex = newIndex;
        final String finalDirection = direction;

        // ── Fade out ──────────────────────────────────────────────────────
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300),
                                                     mapView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> performMapChange(finalIndex, finalDirection,
                                                     onComplete));
        fadeOut.play();
    }

    /**
     * Retorna o índice do mapa atual.
     */
    public int getCurrentMapIndex() {
        return currentMapIndex;
    }

    /**
     * Define o índice do mapa (usado ao carregar save).
     */
    public void setCurrentMapIndex(int newIndex) {
        this.currentMapIndex = newIndex;
    }

    /**
     * Retorna se está em transição.
     */
    public boolean isTransitioning() {
        return isTransitioning;
    }

    // =========================================================================
    // PRIVATE METHODS
    // =========================================================================

    /**
     * Executa a troca de mapa propriamente dita (chamada no meio do fade).
     */
    private void performMapChange(int newIndex, String direction, Runnable onComplete) {
        currentMapIndex = newIndex;

        // ── Teleporta o personagem para o lado oposto ─────────────────────
        switch (direction) {
            case "CIMA" -> playerView.setY(screenH - PlayerMovementController.PLAYER_DISPLAY_SIZE - 20);
            case "BAIXO" -> playerView.setY(20);
            case "ESQUERDA" -> playerView.setX(screenW - PlayerMovementController.PLAYER_DISPLAY_SIZE - 20);
            case "DIREITA" -> playerView.setX(20);
        }

        // ── Atualiza o mapa visível ───────────────────────────────────────
        try {
            mapView.setImage(new Image(
                getClass().getResource("/images/" + mapList[currentMapIndex]).toExternalForm()
            ));
        } catch (NullPointerException ex) {
            System.err.println("[MapTransitionManager] Erro ao carregar mapa: "
                             + mapList[currentMapIndex]);
        }

        // ── Recarrega inimigos para o novo mapa ───────────────────────────
        if (enemyManager != null) {
            enemyManager.configureForMap(currentMapIndex);
        }

        // ── Fade in ───────────────────────────────────────────────────────
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mapView);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(ev -> {
            isTransitioning = false;
            if (onTransitionEnd != null) {
                onTransitionEnd.run();
            }
            if (onComplete != null) {
                onComplete.run();
            }
        });
        fadeIn.play();
    }
}


