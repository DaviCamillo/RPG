package com.RPG.TheLastRoar.frontend.controllers;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * ============================================================
 * InputController.java — Gerenciamento de Entrada do Teclado
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Centraliza TODO o controle de teclado, registrando e removendo
 *   handlers de entrada conforme o estado do jogo (pausado, em batalha, etc).
 *
 * USA FLAGS ESTÁTICOS PARA TECLAS:
 *   keyUp, keyDown, keyLeft, keyRight — lidas pelos AnimationTimers
 *   Ao trocar de direção, o AnimationTimer detecta e atualiza a animação.
 *
 * MAPEAMENTO DE TECLAS:
 *   W / ↑       → movimento cima
 *   S / ↓       → movimento baixo
 *   A / ←       → movimento esquerda
 *   D / →       → movimento direita
 *   H           → usar poção rápida (atalho)
 *   I           → abrir inventário
 *   ESC         → pausar/retomar (sempre funciona)
 *
 * CALLBACKS:
 *   onEscapePressed()   → pausar o jogo
 *   onPotionKeyPressed() → usar poção rápida
 *   onInventoryKeyPressed() → abrir inventário
 *
 * CHAMADO POR: App.java no startGame()
 * REMOVIDO POR: App.java ao mostrar menu principal ou sair
 *
 * @author RPG Team
 */
public class InputController {

    // =========================================================================
    // FLAGS ESTÁTICOS — Estado das teclas de movimento
    // =========================================================================

    /** Flags de movimento lidas pelos AnimationTimers. */
    public static boolean keyUp, keyDown, keyLeft, keyRight;

    // =========================================================================
    // FIELDS — Callbacks e estado
    // =========================================================================

    /** Cena JavaFX onde os handlers serão registrados. */
    private Scene masterScene;

    /** Callbacks do controlador do jogo (App). */
    private Runnable onEscapePressed;
    private Runnable onPotionKeyPressed;
    private Runnable onInventoryKeyPressed;

    /** Flags interna para controlar quando as entradas devem funcionar. */
    private boolean isPaused;
    private boolean lojaAberta;
    private boolean inventarioAberto;
    private boolean isTransitioning;
    private boolean gameRunning;

    /** Referências aos handlers para removê-los depois. */
    private EventHandler<KeyEvent> keyPressHandler;
    private EventHandler<KeyEvent> keyReleaseHandler;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    /**
     * Cria um novo controlador de entrada.
     *
     * @param masterScene              Cena onde registrar os handlers
     * @param onEscapePressed          Callback ao apertar ESC
     * @param onPotionKeyPressed       Callback ao apertar H
     * @param onInventoryKeyPressed    Callback ao apertar I
     */
    public InputController(Scene masterScene,
                          Runnable onEscapePressed,
                          Runnable onPotionKeyPressed,
                          Runnable onInventoryKeyPressed) {
        this.masterScene = masterScene;
        this.onEscapePressed = onEscapePressed;
        this.onPotionKeyPressed = onPotionKeyPressed;
        this.onInventoryKeyPressed = onInventoryKeyPressed;
    }

    // =========================================================================
    // PUBLIC METHODS — Configuração e limpeza
    // =========================================================================

    /**
     * Registra os handlers de teclado na cena.
     * Remove handlers antigos primeiro para evitar duplicação.
     */
    public void setupKeyboardControls() {
        // Remove handlers anteriores se existirem
        if (keyPressHandler != null) {
            masterScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressHandler);
        }
        if (keyReleaseHandler != null) {
            masterScene.removeEventFilter(KeyEvent.KEY_RELEASED, keyReleaseHandler);
        }

        keyPressHandler = e -> handleKeyPressed(e);
        keyReleaseHandler = e -> handleKeyReleased(e);

        masterScene.addEventFilter(KeyEvent.KEY_PRESSED, keyPressHandler);
        masterScene.addEventFilter(KeyEvent.KEY_RELEASED, keyReleaseHandler);
    }

    /**
     * Remove todos os handlers de teclado (ao sair do jogo ou ir para menu).
     */
    public void removeKeyboardHandlers() {
        if (keyPressHandler != null) {
            masterScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressHandler);
        }
        if (keyReleaseHandler != null) {
            masterScene.removeEventFilter(KeyEvent.KEY_RELEASED, keyReleaseHandler);
        }
        resetMovement();
    }

    /**
     * Reseta todos os flags de movimento.
     * Chamado ao pausar, abrir menus ou entrar em batalha.
     */
    public static void resetMovement() {
        keyUp = keyDown = keyLeft = keyRight = false;
    }

    /**
     * Atualiza o estado de bloqueio de entrada (para saber quando ignorar teclas).
     *
     * @param isPaused            Jogo está pausado
     * @param lojaAberta          Loja do NPC está aberta
     * @param inventarioAberto    Inventário está aberto
     * @param isTransitioning     Está em transição de mapa
     * @param gameRunning         Jogo iniciado
     */
    public void updateGameState(boolean isPaused, boolean lojaAberta,
                               boolean inventarioAberto, boolean isTransitioning,
                               boolean gameRunning) {
        this.isPaused = isPaused;
        this.lojaAberta = lojaAberta;
        this.inventarioAberto = inventarioAberto;
        this.isTransitioning = isTransitioning;
        this.gameRunning = gameRunning;
    }

    // =========================================================================
    // PRIVATE METHODS — Handlers de teclado
    // =========================================================================

    /**
     * Handler chamado ao apertar uma tecla.
     * ESC sempre funciona (pausa/retoma);
     * demais teclas só funcionam quando o jogo está livre.
     */
    private void handleKeyPressed(KeyEvent e) {
        // ESC sempre funciona (pausa/retoma)
        if (e.getCode() == KeyCode.ESCAPE) {
            if (onEscapePressed != null) {
                onEscapePressed.run();
            }
            e.consume();
            return;
        }

        // Demais teclas só funcionam quando o jogo está livre
        if (!isPaused && !lojaAberta && !inventarioAberto
            && !isTransitioning && gameRunning) {
            switch (e.getCode()) {
                case W, UP    -> { keyUp = true; e.consume(); }
                case S, DOWN  -> { keyDown = true; e.consume(); }
                case A, LEFT  -> { keyLeft = true; e.consume(); }
                case D, RIGHT -> { keyRight = true; e.consume(); }
                case H        -> {
                    if (onPotionKeyPressed != null) {
                        onPotionKeyPressed.run();
                    }
                    e.consume();
                }
                case I        -> {
                    if (onInventoryKeyPressed != null) {
                        onInventoryKeyPressed.run();
                    }
                    e.consume();
                }
                default       -> {}
            }
        }
    }

    /**
     * Handler chamado ao soltar uma tecla.
     * Apenas reseta os flags de movimento.
     */
    private void handleKeyReleased(KeyEvent e) {
        switch (e.getCode()) {
            case W, UP    -> keyUp = false;
            case S, DOWN  -> keyDown = false;
            case A, LEFT  -> keyLeft = false;
            case D, RIGHT -> keyRight = false;
            default       -> {}
        }
    }
}


