package com.RPG.TheLastRoar.backend.managers;

import java.util.ArrayList;
import java.util.List;

import com.RPG.TheLastRoar.backend.models.Goblin;
import com.RPG.TheLastRoar.backend.models.GoblinBoss;
import com.RPG.TheLastRoar.backend.models.GoblinExp;
import com.RPG.TheLastRoar.backend.models.Monsters;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

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

        // Instancia inimigos apropriados para o mapa selecionado
        spawnEnemiesForMap(mapIndex);
    }

    /**
     * Instancia os inimigos apropriados para um mapas específico.
     * Padrão: mapas são compostos por diferentes tipos e quantidades de inimigos.
     *
     * @param mapIndex Índice do mapa
     */
    private void spawnEnemiesForMap(int mapIndex) {
        switch (mapIndex) {
            case 0 -> spawnMap0Enemies();
            case 1 -> spawnMap1Enemies();
            case 2 -> spawnMap2Enemies();
        }
    }

    /**
     * Instancia inimigos do Mapa 0 (introdutório): 2 Goblins normais.
     */
    private void spawnMap0Enemies() {
        addEnemy(new Goblin(), 6, screenW * 0.3, screenH * 0.1, 128, 80);
        addEnemy(new Goblin(), 1, screenW * 0.7, screenH * 0.2, 128, 80);
    }

    /**
     * Instancia inimigos do Mapa 1 (intermediário): 2 GoblinsExp.
     */
    private void spawnMap1Enemies() {
        addEnemy(new GoblinExp(), 0, screenW * 0.5, screenH * 0.1, 128, 80);
        addEnemy(new GoblinExp(), 1, screenW * 0.2, screenH * 0.15, 128, 80);
    }

    /**
     * Instancia inimigos do Mapa 2 (boss): 1 GoblinBoss (sprite grande).
     */
    private void spawnMap2Enemies() {
        addEnemy(new GoblinBoss(), 0, screenW * 0.5, screenH * 0.1, 256, 256);
    }

    /**
     * Instancia um inimigo no mapa se ainda não foi derrotado.
     * Resposta à persistência: cada inimigo tem um ID único por mapa,
     * e se foi derrotado antes, este método não o instancia novamente.
     *
     * PROPRIEDADES GUARDADAS NA VIEW (para uso no update()):
     *   "mapId"       → ID único do inimigo neste mapa (para matriz de derrotas)
     *   "dirMove"     → direção de movimento (1.0 = direita, -1.0 = esquerda)
     *   "spriteSize"  → tamanho do frame na sprite sheet (px)
     *   "displaySize" → tamanho de exibição na tela (px)
     *
     * @param monster     Instância do monstro a adicionar
     * @param uniqueId    ID único deste inimigo neste mapa
     * @param x           Posição X inicial na tela
     * @param y           Posição Y inicial na tela
     * @param spriteSize  Tamanho do frame na sprite sheet (128 ou 256)
     * @param displaySize Tamanho de exibição na tela (80, 128, 256)
     */
    private void addEnemy(Monsters monster, int uniqueId,
                           double x, double y, int spriteSize, int displaySize) {

        // Verifica se este inimigo já foi derrotado (não reaparece)
        if (defeatedEnemies[currentMapIndex][uniqueId]) {
            return;
        }

        // Cria ImageView do inimigo com sprite sheet carregada
        ImageView enemyView = createEnemyImageView(monster, spriteSize, displaySize, x, y);
        if (enemyView == null) {
            return;  // Erro ao carregar imagem
        }

        // Armazena metadados necessários para atualização
        storeEnemyProperties(enemyView, uniqueId, spriteSize, displaySize);

        // Adiciona à lista de gerenciamento
        monsters.add(monster);
        views.add(enemyView);
        gameRoot.getChildren().add(enemyView);
    }

    /**
     * Cria uma ImageView para um inimigo a partir de sua sprite sheet.
     * Resolve o caminho da imagem e carrega o recurso.
     *
     * @return ImageView pronto para exibição, ou null se falhar
     */
    private ImageView createEnemyImageView(Monsters monster, int spriteSize,
                                          int displaySize, double x, double y) {
        String imagePath = resolveImagePath(monster.getImagePath());

        java.net.URL imageUrl = getClass().getResource(imagePath);
        if (imageUrl == null) {
            System.err.println("[EnemyManager] Imagem não encontrada: " + imagePath);
            return null;
        }

        // Cria ImageView e configura dimensões
        ImageView view = new ImageView(new Image(imageUrl.toExternalForm()));
        view.setViewport(new Rectangle2D(0, 0, spriteSize, spriteSize));
        view.setFitWidth(displaySize);
        view.setFitHeight(displaySize);
        view.setX(x);
        view.setY(y);

        return view;
    }

    /**
     * Resolve o caminho da imagem, adicionando "/" se necessário.
     */
    private String resolveImagePath(String imagePath) {
        return imagePath.startsWith("/") ? imagePath : "/" + imagePath;
    }

    /**
     * Armazena metadados na ImageView para uso durante animações e movimento.
     */
    private void storeEnemyProperties(ImageView view, int uniqueId,
                                     int spriteSize, int displaySize) {
        view.getProperties().put("mapId", uniqueId);
        view.getProperties().put("dirMove", 1.0);  // Começa movendo para direita
        view.getProperties().put("spriteSize", spriteSize);
        view.getProperties().put("displaySize", (double) displaySize);
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
     * OTIMIZAÇÕES:
     *   - Cache de propriedades para evitar múltiplos HashMap.get()
     *   - Squared distance para evitar Math.sqrt() em colisão
     *
     * @param playerX    Posição X atual do jogador
     * @param playerY    Posição Y atual do jogador
     * @param enemyFrame Frame atual da animação (0–3, sincronizado com App)
     * @return Índice do inimigo que colidiu, ou -1 se não houve colisão.
     */
    public int update(double playerX, double playerY, int enemyFrame) {
        for (int i = 0; i < views.size(); i++) {
            ImageView view = views.get(i);

            // ── Cache de propriedades (evita HashMap.get() múltiplas vezes) ──
            double dirMove = (double) view.getProperties().get("dirMove");
            int spriteSize = (int) view.getProperties().get("spriteSize");
            double displaySize = (double) view.getProperties().get("displaySize");

            // ── Movimento horizontal ─────────────────────────────────────
            double newX = view.getX() + dirMove;
            view.setX(newX);

            // ── Bounce nas bordas da tela (atualiza dirMove se necessário) ──
            boolean changedDirection = false;
            if (newX > screenW - displaySize - 50) {
                dirMove = -1.0;
                changedDirection = true;
            } else if (newX < 50) {
                dirMove = 1.0;
                changedDirection = true;
            }
            // Só atualiza a propriedade se houve mudança
            if (changedDirection) {
                view.getProperties().put("dirMove", dirMove);
            }

            // ── Animação: seleciona frame e direção na sprite sheet ───────
            // Linha da sprite: 2 = andando para direita, 1 = andando para esquerda
            int spriteRow = (dirMove > 0) ? 2 : 1;
            view.setViewport(new Rectangle2D(
                enemyFrame * spriteSize,
                spriteRow * spriteSize,
                spriteSize,
                spriteSize
            ));

            // ── Verificação de colisão (distância euclidiana ao quadrado) ──
            // Compara o centro do jogador com o centro do inimigo
            // OTIMIZAÇÃO: usa squared distance para evitar Math.sqrt()
            double dx = (playerX + 32) - (newX + displaySize / 2);
            double dy = (playerY + 32) - (view.getY() + displaySize / 2);
            double distanceSquared = dx * dx + dy * dy;
            double collisionRadiusSquared = (displaySize * 0.4) * (displaySize * 0.4);

            if (distanceSquared < collisionRadiusSquared) {
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


