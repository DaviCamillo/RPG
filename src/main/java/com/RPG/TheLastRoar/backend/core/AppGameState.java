package com.RPG.TheLastRoar.backend.core;

import com.RPG.TheLastRoar.App;
import com.RPG.TheLastRoar.backend.managers.SaveManager;

import javafx.animation.FadeTransition;
import javafx.scene.image.Image;
import javafx.util.Duration;

/**
 * AppGameState — Gerencia save, load e transições de mapa.
 *
 * RESPONSABILIDADE:
 *   - Salvar e carregar estados do jogo
 *   - Gerenciar transições entre mapas com fade out/in
 *   - Teleportar personagem ao trocar de mapa
 *   - Rastrear inimigos derrotados por mapa
 */
public class AppGameState {

    private App app;
    private static final double PLAYER_DISPLAY_SIZE = 80;

    public AppGameState(App app) {
        this.app = app;
    }

    /**
     * Salva o estado atual em um slot de save.
     * Pausa o jogo e reinicia o menu após salvar.
     *
     * @param fileName Nome do arquivo de save (ex: "save1.json")
     */
    public void saveToSlot(String fileName) {
        SaveManager.salvar(fileName, app.currentMapIndex,
                           app.playerView.getX(), app.playerView.getY(),
                           app.player, app.defeatedEnemies);
        app.togglePause(); // Fecha o menu de pausa após salvar
    }

    /**
     * Carrega um arquivo de save e atualiza o estado do jogo.
     *
     * @param fileName     Nome do arquivo de save
     * @param isInitialLoad Se true, é o carregamento inicial (não fecha pausa)
     */
    public void loadFromSave(String fileName, boolean isInitialLoad) {
        SaveManager.SaveData data = SaveManager.carregar(fileName, app.MAP_LIST.length);
        if (data == null) return;

        // Restaura o estado do mapa
        app.currentMapIndex = data.mapa;
        for (int i = 0; i < data.inimigosDerrotados.length; i++) {
            System.arraycopy(data.inimigosDerrotados[i], 0,
                             app.defeatedEnemies[i], 0,
                             data.inimigosDerrotados[i].length);
        }

        // Atualiza mapa visível e inimigos
        app.mapView.setImage(new Image(
            getClass().getResource("/images/" + app.MAP_LIST[app.currentMapIndex]).toExternalForm()
        ));
        app.enemyManager.configureForMap(app.currentMapIndex);
        app.updateNpcVisibility();

        // Restaura atributos do personagem
        app.playerView.setX(data.posX);
        app.playerView.setY(data.posY);
        app.player.setLife(data.vida);
        app.player.setNivel(data.level);
        app.player.setCoin(data.ouro);

        if (!isInitialLoad) app.togglePause(); // Fecha menu de pausa após carregar
        if (app.hudManager != null) app.hudManager.atualizar(app.player);
    }

    /**
     * Transiciona para o mapa anterior ou próximo com fade out/in.
     * Teleporta o personagem para o lado oposto ao que ele saiu.
     *
     * @param direction "CIMA", "BAIXO", "ESQUERDA" ou "DIREITA"
     */
    public void changeMap(String direction) {
        if (app.isTransitioning) return;

        // Calcula o índice do próximo mapa
        int newIndex = app.currentMapIndex;
        if (direction.equals("CIMA") || direction.equals("DIREITA")) {
            newIndex++;
        } else {
            newIndex--;
        }

        // Não faz nada se estiver nos limites
        if (newIndex >= app.MAP_LIST.length || newIndex < 0) return;

        app.isTransitioning = true;
        app.playerMovement.stop();
        app.enemyAI.stop();
        App.resetMovement();

        final int finalIndex = newIndex;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), app.mainLayout);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            app.currentMapIndex = finalIndex;

            // Teleporta o personagem para o lado oposto de onde saiu
            switch (direction) {
                case "CIMA"     -> app.playerView.setY(app.screenH - PLAYER_DISPLAY_SIZE - 20);
                case "BAIXO"    -> app.playerView.setY(20);
                case "ESQUERDA" -> app.playerView.setX(app.screenW - PLAYER_DISPLAY_SIZE - 20);
                case "DIREITA"  -> app.playerView.setX(20);
            }

            // Atualiza o mapa e recarrega os inimigos
            app.mapView.setImage(new Image(
                getClass().getResource("/images/" + app.MAP_LIST[app.currentMapIndex]).toExternalForm()
            ));
            app.enemyManager.configureForMap(app.currentMapIndex);
            app.updateNpcVisibility();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), app.mainLayout);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setOnFinished(ev -> {
                app.lastFrameTime   = System.nanoTime();
                app.isTransitioning = false;
                app.playerMovement.start();
                if (!app.enemyManager.isEmpty()) app.enemyAI.start();
            });
            fadeIn.play();
        });
        fadeOut.play();
    }
}







