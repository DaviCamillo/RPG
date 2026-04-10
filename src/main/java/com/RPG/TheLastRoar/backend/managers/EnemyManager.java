package com.RPG.TheLastRoar.backend.managers;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import com.RPG.TheLastRoar.backend.models.Monsters;
import com.RPG.TheLastRoar.backend.models.Goblin;
import com.RPG.TheLastRoar.backend.models.GoblinExp;
import com.RPG.TheLastRoar.backend.models.GoblinBoss;

/**
 * ============================================================
 * EnemyManager.java — Gerencia os inimigos no mapa
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Cria, anima, move e verifica colisão dos inimigos visíveis
 *   no mapa do jogo. Também gerencia sua remoção após derrota.
 *
 * FLUXO PRINCIPAL:
 *   1. App.java chama configureForMap(index) ao entrar em um mapa
 *   2. Inimigos vivos são instanciados e adicionados ao gameRoot
 *   3. A cada frame, App.java chama update() → move + anima + colisão
 *   4. Se colisão: App.java inicia Battle.startBattle()
 *   5. Após batalha: App.java chama removeEnemy(index)
 *
 * SISTEMA DE PERSISTÊNCIA DE DERROTAS:
 *   A matriz defeatedEnemies[mapa][id] impede que inimigos
 *   derrotados reapareçam ao trocar de mapa e voltar.
 *   Esta matriz é salva/carregada pelo SaveManager.
 *
 * CONFIGURAÇÃO DOS MAPAS:
 *   ┌────────┬───────────────────────────────────────────────┐
 *   │ Mapa 0 │ 2x Goblin (sprite 128x128, display 80x80)     │
 *   │ Mapa 1 │ 2x GoblinExp (sprite 128x128, display 80x80)  │
 *   │ Mapa 2 │ 1x GoblinBoss (sprite 256x256, display 256x256│
 *   └────────┴───────────────────────────────────────────────┘
 *
 * COLISÃO:
 *   Usa distância euclidiana entre centro do jogador e centro do inimigo.
 *   Raio de colisão = 40% do tamanho de display do inimigo.
 *
 * USADO POR:
 *   App.java — configureForMap(), update(), removeEnemy(), getMonstro()
 */
public class EnemyManager {

    // =========================================================================
    // FIELDS
    // =========================================================================

    /** Painel raiz do jogo. Inimigos são adicionados/removidos dele. */
    private final Pane gameRoot;

    private final double screenW;
    private final double screenH;

    /** Lista dos dados lógicos dos monstros ativos. */
    private final List<Monsters>  monsters = new ArrayList<>();

    /** Lista das imagens dos monstros ativos (sincronizada com monsters). */
    private final List<ImageView> views    = new ArrayList<>();

    /**
     * Referência à matriz de inimigos derrotados.
     * Compartilhada com App.java e SaveManager.
     * Formato: [índiceMapa][idÚnicoInimigo] = true se derrotado.
     */
    private final boolean[][] defeatedEnemies;

    /** Índice do mapa atualmente configurado. */
    private int currentMapIndex = 0;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    /**
     * @param gameRoot        Painel onde os inimigos serão renderizados
     * @param screenW         Largura da tela (para movimento e colisão)
     * @param screenH         Altura da tela
     * @param defeatedEnemies Matriz compartilhada de inimigos derrotados
     */
    public EnemyManager(Pane gameRoot, double screenW, double screenH,
                         boolean[][] defeatedEnemies) {
        this.gameRoot        = gameRoot;
        this.screenW         = screenW;
        this.screenH         = screenH;
        this.defeatedEnemies = defeatedEnemies;
    }

    // =========================================================================
    // MAP CONFIGURATION
    // =========================================================================

    /**
     * Limpa os inimigos do mapa anterior e gera os do novo mapa.
     * Respeita a matriz defeatedEnemies — inimigos já derrotados não reaparecem.
     *
     * @param mapIndex Índice do novo mapa (0 = default, 1 = intermediário, 2 = boss)
     */
    public void configureForMap(int mapIndex) {
        this.currentMapIndex = mapIndex;

        // Remove todos os inimigos do mapa anterior
        gameRoot.getChildren().removeAll(views);
        monsters.clear();
        views.clear();

        // Adiciona os inimigos corretos para o mapa selecionado
        switch (mapIndex) {
            case 0 -> {
                // Mapa inicial: 2 Goblins normais
                addEnemy(new Goblin(),    6, screenW * 0.3, screenH * 0.1, 128, 80);
                addEnemy(new Goblin(),    1, screenW * 0.7, screenH * 0.2, 128, 80);
            }
            case 1 -> {
                // Mapa intermediário: 2 GoblinsExp
                addEnemy(new GoblinExp(), 0, screenW * 0.5, screenH * 0.1, 128, 80);
                addEnemy(new GoblinExp(), 1, screenW * 0.2, screenH * 0.15, 128, 80);
            }
            case 2 -> {
                // Mapa do boss: 1 GoblinBoss (sprite grande 256x256)
                addEnemy(new GoblinBoss(), 0, screenW * 0.5, screenH * 0.1, 256, 256);
            }
        }
    }

    /**
     * Instancia um inimigo no mapa se ainda não foi derrotado.
     *
     * PROPRIEDADES GUARDADAS NA VIEW (para uso no update()):
     *   "mapId"      → id único do inimigo neste mapa
     *   "dirMove"    → direção de movimento (1.0 = direita, -1.0 = esquerda)
     *   "spriteSize" → tamanho do frame na sprite sheet (px)
     *   "displaySize"→ tamanho de exibição na tela (px)
     *
     * @param monster     Instância do monstro a adicionar
     * @param uniqueId    ID único deste inimigo neste mapa (usado na matriz)
     * @param x           Posição X inicial
     * @param y           Posição Y inicial
     * @param spriteSize  Tamanho do frame na sprite sheet (128 ou 256)
     * @param displaySize Tamanho de exibição na tela (80, 128, 256)
     */
    private void addEnemy(Monsters monster, int uniqueId,
                           double x, double y, int spriteSize, int displaySize) {

        // Verifica se este inimigo já foi derrotado anteriormente
        if (defeatedEnemies[currentMapIndex][uniqueId]) return;

        // Resolve o caminho da imagem
        String path = monster.getImagePath();
        if (!path.startsWith("/")) path = "/" + path;

        java.net.URL imageUrl = getClass().getResource(path);
        if (imageUrl == null) {
            System.err.println("[EnemyManager] Imagem não encontrada: " + path);
            return;
        }

        // Cria e configura a ImageView do inimigo
        ImageView view = new ImageView(new Image(imageUrl.toExternalForm()));
        view.setViewport(new Rectangle2D(0, 0, spriteSize, spriteSize));
        view.setFitWidth(displaySize);
        view.setFitHeight(displaySize);
        view.setX(x);
        view.setY(y);

        // Armazena propriedades necessárias para atualização frame a frame
        view.getProperties().put("mapId",       uniqueId);
        view.getProperties().put("dirMove",      1.0);     // Começa movendo para a direita
        view.getProperties().put("spriteSize",   spriteSize);
        view.getProperties().put("displaySize",  (double) displaySize);

        monsters.add(monster);
        views.add(view);
        gameRoot.getChildren().add(view);
    }

    // =========================================================================
    // FRAME UPDATE — Movimento, Animação e Colisão
    // =========================================================================

    /**
     * Atualiza todos os inimigos a cada frame:
     *   1. Move horizontalmente (bounce nas bordas da tela)
     *   2. Anima o frame da sprite sheet
     *   3. Verifica colisão com o jogador
     *
     * @param playerX    Posição X atual do jogador
     * @param playerY    Posição Y atual do jogador
     * @param enemyFrame Frame atual da animação (0–3, sincronizado com App)
     * @return Índice do inimigo que colidiu, ou -1 se não houve colisão.
     */
    public int update(double playerX, double playerY, int enemyFrame) {
        for (int i = 0; i < views.size(); i++) {
            ImageView view = views.get(i);

            // Recupera propriedades armazenadas
            double dirMove   = (double) view.getProperties().get("dirMove");
            int    spriteSize = (int)    view.getProperties().get("spriteSize");
            double displaySize = (double) view.getProperties().get("displaySize");

            // ── Movimento horizontal ─────────────────────────────────────
            view.setX(view.getX() + dirMove);

            // ── Animação: seleciona frame e direção na sprite sheet ───────
            // Linha da sprite: 2 = andando para direita, 1 = andando para esquerda
            int spriteRow = (dirMove > 0) ? 2 : 1;
            view.setViewport(new Rectangle2D(
                enemyFrame * spriteSize,
                spriteRow  * spriteSize,
                spriteSize,
                spriteSize
            ));

            // ── Bounce nas bordas da tela ─────────────────────────────────
            if (view.getX() > screenW - displaySize - 50) {
                view.getProperties().put("dirMove", -1.0); // Inverte para esquerda
            }
            if (view.getX() < 50) {
                view.getProperties().put("dirMove",  1.0); // Inverte para direita
            }

            // ── Verificação de colisão (distância euclidiana) ─────────────
            // Compara o centro do jogador com o centro do inimigo
            double dx = (playerX + 32) - (view.getX() + displaySize / 2);
            double dy = (playerY + 32) - (view.getY() + displaySize / 2);
            double distance = Math.sqrt(dx * dx + dy * dy);
            double collisionRadius = displaySize * 0.4; // 40% do tamanho como raio

            if (distance < collisionRadius) {
                return i; // Colisão detectada — retorna o índice deste inimigo
            }
        }

        return -1; // Nenhuma colisão neste frame
    }

    // =========================================================================
    // ENEMY REMOVAL
    // =========================================================================

    /**
     * Remove permanentemente um inimigo após ser derrotado em batalha.
     * Marca o inimigo como derrotado na matriz para persistência.
     *
     * @param index Índice do inimigo nas listas internas.
     */
    public void removeEnemy(int index) {
        if (index < 0 || index >= views.size()) return;

        ImageView view = views.get(index);

        // 1. Marca como derrotado para persistência (não reaparece ao voltar)
        Object idObj = view.getProperties().get("mapId");
        if (idObj != null) {
            int mapId = (int) idObj;
            defeatedEnemies[currentMapIndex][mapId] = true;
        }

        // 2. Remove visualmente do cenário
        gameRoot.getChildren().remove(view);

        // 3. Remove das listas internas (evita processamento de fantasma)
        views.remove(index);
        monsters.remove(index);

        System.out.println("[EnemyManager] Inimigo removido. Restam: " + monsters.size());
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    /**
     * @param index Índice do inimigo.
     * @return Dados lógicos do monstro na posição index.
     */
    public Monsters getMonstro(int index) { return monsters.get(index); }

    /**
     * @param index Índice do inimigo.
     * @return ImageView (sprite) do monstro na posição index.
     */
    public ImageView getView(int index) { return views.get(index); }

    /**
     * @return {@code true} se não há inimigos ativos no mapa atual.
     */
    public boolean isEmpty() { return views.isEmpty(); }
}


