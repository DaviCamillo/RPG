package com.RPG.TheLastRoar.backend.managers;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * ============================================================
 * SaveManager.java — Sistema de save/load do jogo
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Serializa e desserializa o estado da partida em arquivos
 *   de texto simples no formato Java Properties (.json no nome
 *   mas conteúdo key=value).
 *
 * DADOS SALVOS:
 *   ┌──────────────────┬─────────────────────────────────────┐
 *   │ Chave            │ Descrição                           │
 *   ├──────────────────┼─────────────────────────────────────┤
 *   │ mapaAtual        │ Índice do mapa atual (0, 1 ou 2)    │
 *   │ posicaoX         │ Posição X do personagem no mapa     │
 *   │ posicaoY         │ Posição Y do personagem no mapa     │
 *   │ vidaPlayer       │ HP atual do personagem              │
 *   │ levelPlayer      │ Nível atual do personagem           │
 *   │ ouroPlayer       │ Moedas do personagem                │
 *   │ inimigosMortos   │ Matriz de inimigos derrotados       │
 *   └──────────────────┴─────────────────────────────────────┘
 *
 * FORMATO DA MATRIZ DE INIMIGOS:
 *   Mapa0_inimigo0,Mapa0_inimigo1;Mapa1_inimigo0,Mapa1_inimigo1;...
 *   Exemplo: "false,true;true,false,false" → 2 mapas com 2 e 3 inimigos
 *
 * SLOTS DE SAVE:
 *   save1.json, save2.json, save3.json
 *
 * USADO POR:
 *   App.java      — salvar (togglePause → salvarSlot) e carregar
 *   PauseMenu.java — botões de save/load habilitam/desabilitam com existe()
 *   StartScreen   — botão "CONTINUAR" verifica se existe algum save
 */
public class SaveManager {

    // =========================================================================
    // CHAVES DO ARQUIVO DE SAVE (constantes para evitar typos)
    // =========================================================================

    private static final String KEY_MAP      = "mapaAtual";
    private static final String KEY_POS_X    = "posicaoX";
    private static final String KEY_POS_Y    = "posicaoY";
    private static final String KEY_LIFE     = "vidaPlayer";
    private static final String KEY_LEVEL    = "levelPlayer";
    private static final String KEY_GOLD     = "ouroPlayer";
    private static final String KEY_ENEMIES  = "inimigosMortos";

    // =========================================================================
    // INNER CLASS — Contêiner de dados de save
    // =========================================================================

    /**
     * Contêiner imutável com todos os dados carregados de um arquivo de save.
     * Retornado por carregar() e consumido por App.java.
     */
    public static class SaveData {
        public int mapa;
        public double posX;
        public double posY;
        public int vida;
        public int level;
        public int ouro;
        public boolean[][] inimigosDerrotados;
    }

    // =========================================================================
    // SAVE
    // =========================================================================

    /**
     * Salva o estado atual do jogo em um arquivo de texto.
     *
     * FLUXO:
     *   1. Serializa a matriz de inimigos derrotados em string
     *   2. Monta o objeto Properties com todas as chaves
     *   3. Escreve no disco via Files.writeString()
     *
     * @param fileName            Nome do arquivo (ex: "save1.json")
     * @param currentMap          Índice do mapa atual
     * @param playerX             Posição X do personagem
     * @param playerY             Posição Y do personagem
     * @param player              Personagem do jogador
     * @param defeatedEnemies     Matriz [mapa][idInimigo] de derrotados
     */
    public static void salvar(String fileName, int currentMap, double playerX, double playerY, com.RPG.TheLastRoar.backend.models.Character player, boolean[][] defeatedEnemies) {
        try {
            Properties props = new Properties();
            props.setProperty(KEY_MAP,     String.valueOf(currentMap));
            props.setProperty(KEY_POS_X,   String.valueOf(playerX));
            props.setProperty(KEY_POS_Y,   String.valueOf(playerY));
            props.setProperty(KEY_LIFE,    String.valueOf(player.getLife()));
            props.setProperty(KEY_LEVEL,   String.valueOf(player.getNivel()));
            props.setProperty(KEY_GOLD,    String.valueOf(player.getCoin()));
            props.setProperty(KEY_ENEMIES, serializeDefeatedEnemies(defeatedEnemies));

            StringWriter writer = new StringWriter();
            props.store(writer, "Save Data - The Last Roar");
            Files.writeString(Paths.get(fileName), writer.getBuffer().toString());

            System.out.println("[SaveManager] Jogo salvo com sucesso em " + fileName);

        } catch (IOException e) {
            System.err.println("[SaveManager] Erro ao salvar: " + e.getMessage());
        }
    }

    // =========================================================================
    // LOAD
    // =========================================================================

    /**
     * Carrega o estado do jogo de um arquivo de save.
     *
     * FLUXO:
     *   1. Verifica se o arquivo existe
     *   2. Lê o conteúdo e carrega no Properties
     *   3. Extrai cada campo com valor padrão como fallback
     *   4. Desserializa a matriz de inimigos derrotados
     *
     * @param fileName   Nome do arquivo (ex: "save1.json")
     * @param totalMaps  Número total de mapas no jogo (para alocar a matriz)
     * @return {@code SaveData} com os dados do save, ou {@code null} se não encontrado.
     */
    public static SaveData carregar(String fileName, int totalMaps) {
        if (!existe(fileName)) {
            System.out.println("[SaveManager] Arquivo " + fileName + " não encontrado.");
            return null;
        }

        try {
            String content = Files.readString(Paths.get(fileName));
            Properties props = new Properties();
            props.load(new StringReader(content));

            SaveData data = new SaveData();
            data.mapa  = Integer.parseInt(props.getProperty(KEY_MAP,    "0"));
            data.posX  = Double.parseDouble(props.getProperty(KEY_POS_X,  "0.0"));
            data.posY  = Double.parseDouble(props.getProperty(KEY_POS_Y,  "0.0"));
            data.vida  = Integer.parseInt(props.getProperty(KEY_LIFE,   "20"));
            data.level = Integer.parseInt(props.getProperty(KEY_LEVEL,  "1"));
            data.ouro  = Integer.parseInt(props.getProperty(KEY_GOLD,   "0"));

            data.inimigosDerrotados = deserializeDefeatedEnemies(
                props.getProperty(KEY_ENEMIES, ""), totalMaps
            );

            System.out.println("[SaveManager] Jogo carregado com sucesso de " + fileName);
            return data;

        } catch (Exception e) {
            System.err.println("[SaveManager] Arquivo de save corrompido: " + e.getMessage());
            return null;
        }
    }

    // =========================================================================
    // UTILITY
    // =========================================================================

    /**
     * Verifica se um arquivo de save existe no disco.
     *
     * @param fileName Nome do arquivo a verificar.
     * @return {@code true} se o arquivo existe.
     */
    public static boolean existe(String fileName) {
        return Files.exists(Paths.get(fileName));
    }

    // =========================================================================
    // PRIVATE HELPERS — Serialização da matriz de inimigos
    // =========================================================================

    /**
     * Serializa a matriz de inimigos derrotados para string.
     *
     * FORMATO: "false,true,false;true,false"
     *   - Vírgulas separam inimigos dentro de um mapa
     *   - Ponto-e-vírgulas separam mapas diferentes
     *
     * @param defeatedEnemies Matriz [mapa][id] a serializar.
     * @return String formatada ou "" se null.
     */
    private static String serializeDefeatedEnemies(boolean[][] defeatedEnemies) {
        if (defeatedEnemies == null) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < defeatedEnemies.length; i++) {
            for (int j = 0; j < defeatedEnemies[i].length; j++) {
                sb.append(defeatedEnemies[i][j]);
                if (j < defeatedEnemies[i].length - 1) sb.append(",");
            }
            if (i < defeatedEnemies.length - 1) sb.append(";");
        }
        return sb.toString();
    }

    /**
     * Desserializa a string de inimigos derrotados de volta para matriz.
     * Compatível com saves antigos (menos mapas ou menos inimigos).
     *
     * @param serialized String no formato "false,true;true,false"
     * @param totalMaps  Total de mapas esperados (dimensão 0 da matriz)
     * @return Matriz [totalMaps][10] restaurada.
     */
    private static boolean[][] deserializeDefeatedEnemies(String serialized, int totalMaps) {
        boolean[][] result = new boolean[totalMaps][10]; // 10 inimigos por mapa como padrão

        if (serialized == null || serialized.isBlank()) return result;

        String[] mapsData = serialized.split(";");
        int mapsLimit     = Math.min(mapsData.length, totalMaps);

        for (int i = 0; i < mapsLimit; i++) {
            String[] enemies    = mapsData[i].split(",");
            int enemiesLimit    = Math.min(enemies.length, result[i].length);

            for (int j = 0; j < enemiesLimit; j++) {
                result[i][j] = Boolean.parseBoolean(enemies[j]);
            }
        }

        return result;
    }
}




