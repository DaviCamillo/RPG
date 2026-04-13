package com.RPG.TheLastRoar.backend.core;

import com.RPG.TheLastRoar.App;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * AppKeyboardControls — Gerencia configuração de controles de teclado.
 *
 * RESPONSABILIDADE:
 *   - Registrar handlers de KEY_PRESSED e KEY_RELEASED
 *   - Mapear teclas para ações do jogo (movimento, pausa, inventário, etc.)
 *   - Respeitar o estado do jogo (pausado, loja aberta, etc.)
 */
public class AppKeyboardControls {

    private App app;
    private EventHandler<KeyEvent> keyPressHandler;
    private EventHandler<KeyEvent> keyReleaseHandler;

    public AppKeyboardControls(App app) {
        this.app = app;
    }

    /**
     * Registra os filtros de teclado na cena mestre.
     * Remove handlers antigos primeiro para evitar duplicação ao reiniciar.
     */
    public void setupKeyboardControls(Scene masterScene) {
        // Remove handlers anteriores para evitar duplicação
        if (keyPressHandler   != null) masterScene.removeEventFilter(KeyEvent.KEY_PRESSED,  keyPressHandler);
        if (keyReleaseHandler != null) masterScene.removeEventFilter(KeyEvent.KEY_RELEASED, keyReleaseHandler);

        keyPressHandler = e -> {
            // ESC sempre funciona (pausa/retoma)
            if (e.getCode() == KeyCode.ESCAPE) {
                app.togglePause();
                e.consume();
                return;
            }

            // Demais teclas só funcionam quando o jogo está livre
            if (!app.isPaused && !app.lojaAberta && !app.inventarioAberto
                          && !app.isTransitioning && app.gameRunning) {
                switch (e.getCode()) {
                    case W, UP    -> { App.keyUp    = true; e.consume(); }
                    case S, DOWN  -> { App.keyDown  = true; e.consume(); }
                    case A, LEFT  -> { App.keyLeft  = true; e.consume(); }
                    case D, RIGHT -> { App.keyRight = true; e.consume(); }
                    case H        -> { app.usePotionOnMap(); e.consume(); }
                    case I        -> { app.openInventory();  e.consume(); }
                    default       -> {}
                }
            }
        };

        keyReleaseHandler = e -> {
            switch (e.getCode()) {
                case W, UP    -> App.keyUp    = false;
                case S, DOWN  -> App.keyDown  = false;
                case A, LEFT  -> App.keyLeft  = false;
                case D, RIGHT -> App.keyRight = false;
                default       -> {}
            }
        };

        masterScene.addEventFilter(KeyEvent.KEY_PRESSED,  keyPressHandler);
        masterScene.addEventFilter(KeyEvent.KEY_RELEASED, keyReleaseHandler);
    }

    /**
     * Remove os handlers de teclado da cena (chamado ao fechar o jogo).
     */
    public void removeKeyboardControls(Scene masterScene) {
        if (keyPressHandler   != null) masterScene.removeEventFilter(KeyEvent.KEY_PRESSED,  keyPressHandler);
        if (keyReleaseHandler != null) masterScene.removeEventFilter(KeyEvent.KEY_RELEASED, keyReleaseHandler);
    }
}




