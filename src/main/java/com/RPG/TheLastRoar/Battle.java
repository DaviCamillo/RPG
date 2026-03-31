package com.RPG.TheLastRoar;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * ============================================================
 * Battle.java — Sistema de batalha por turnos
 * ============================================================
 *
 * RESPONSABILIDADE:
 * Gerencia toda a lógica da tela de batalha: troca de cena,
 * turno do jogador (atacar, usar mochila, fugir), turno do
 * inimigo, vitória, derrota (game over) e retorno ao mapa.
 *
 * CORREÇÕES NESTA VERSÃO:
 *
 *   BUG 1 — ROLLBACK/TRAVA PÓS-BATALHA:
 *     Causa: 'inBattle' é static — se o jogo terminasse em Game Over
 *     e o jogador voltasse ao menu, inBattle ficava true para sempre,
 *     impedindo qualquer nova batalha na mesma sessão.
 *     Correção: Adicionado resetInBattle() público. App.showMainMenu()
 *     chama este método SEMPRE ao voltar ao menu, garantindo estado limpo.
 *     O método triggerGameOver() também chama resetInBattle() antes de
 *     redirecionar para o menu, fechando o ciclo.
 *
 *   MOCHILA FUNCIONAL:
 *     - Lista todas as Potion do inventário com botões clicáveis
 *     - Ao usar a poção: cura o jogador, remove do inventário, passa o turno
 *     - Mensagem "Sem poções!" se inventário vazio
 *
 *   DROP DE MOEDAS ao vencer:
 *     - playerChar.addCoin(monstro.getDropCoin()) chamado ao derrotar o monstro
 *     - Mensagem mostra "+X moedas  +30 XP"
 *     - hudManager.atualizar() chamado para refletir as moedas no HUD
 */
public class Battle {

    // Flag estático que impede múltiplas batalhas simultâneas.
    // CORREÇÃO BUG 1: resetInBattle() permite resetar este flag externamente.
    private static boolean inBattle = false;

    /**
     * Reseta o flag de batalha.
     * Chamado por App.showMainMenu() e por triggerGameOver().
     * Garante que o jogo nunca fique preso no estado "em batalha".
     */
    public static void resetInBattle() {
        inBattle = false;
    }

    // =========================================================================
    // INICIAR BATALHA
    // =========================================================================

    /**
     * Inicia a tela de batalha substituindo a cena do mapa.
     *
     * @param stage          Janela principal
     * @param mapScene       Cena do mapa (será restaurada ao sair)
     * @param playerChar     Personagem do jogador
     * @param monstro        Inimigo desta batalha
     * @param playerView     Sprite do jogador no mapa (referência, não usado na batalha)
     * @param enemyView      Sprite do inimigo no mapa (referência, não usado na batalha)
     * @param app            Instância do App (para chamar resumeTimers/showMainMenu)
     * @param originalLayout StackPane original do mapa (para restaurar ao sair)
     * @param hudManager     HUD do jogo (para atualizar HP/moedas após batalha)
     */
    public static void startBattle(Stage stage, Scene mapScene, Character playerChar,
                                   Monsters monstro, ImageView playerView, ImageView enemyView,
                                   App app, StackPane originalLayout, HudManager hudManager) {
        // Evita iniciar batalha se já estiver em uma
        if (inBattle) return;
        inBattle = true;

        // ── Fundo da batalha ──────────────────────────────────────────────
        ImageView bg = new ImageView();
        try {
            var bgUrl = Battle.class.getResource("/images/battlebg2.png");
            if (bgUrl != null) bg.setImage(new Image(bgUrl.toExternalForm()));
        } catch (Exception ex) {
            System.err.println("[Battle] Erro ao carregar fundo: " + ex.getMessage());
        }
        bg.fitWidthProperty().bind(stage.widthProperty());
        bg.fitHeightProperty().bind(stage.heightProperty());

        // ── Sprites dos combatentes ───────────────────────────────────────
        ImageView pImg = new ImageView(); // Sprite do jogador
        try {
            if (playerChar.getBattleSprite() != null)
                pImg.setImage(playerChar.getBattleSprite());
        } catch (Exception ex) { /* ignora se não encontrar */ }
        pImg.setFitWidth(200);
        pImg.setPreserveRatio(true);

        ImageView eImg = new ImageView(); // Sprite do inimigo
        try {
            var eUrl = Battle.class.getResource(monstro.getBattleImagePath());
            if (eUrl != null) eImg.setImage(new Image(eUrl.toExternalForm()));
        } catch (Exception ex) {
            System.err.println("[Battle] Erro ao carregar sprite do monstro: " + ex.getMessage());
        }
        eImg.setFitWidth(200);
        eImg.setPreserveRatio(true);

        // ── Caixas de status ──────────────────────────────────────────────
        // Status do inimigo (canto superior esquerdo)
        VBox opponentStatus = criarStatusMonstro(
            monstro.getName(), monstro.getLife(), monstro.getMaxLife());
        StackPane.setMargin(opponentStatus, new Insets(50, 0, 0, 50));
        StackPane.setAlignment(opponentStatus, Pos.TOP_LEFT);

        // Status do jogador (canto inferior direito)
        VBox playerStatus = criarStatusJogador(
            playerChar.getName(), playerChar.getNivel(),
            playerChar.getLife(), playerChar.getMaxLife(),
            playerChar.getXp(), playerChar.getMaxXp());
        StackPane.setMargin(playerStatus, new Insets(0, 50, 50, 0));
        StackPane.setAlignment(playerStatus, Pos.BOTTOM_RIGHT);

        // ── Área da arena ─────────────────────────────────────────────────
        VBox pSpriteBox = new VBox(pImg);
        VBox eSpriteBox = new VBox(eImg);
        pSpriteBox.setAlignment(Pos.BOTTOM_LEFT);
        eSpriteBox.setAlignment(Pos.TOP_RIGHT);
        pSpriteBox.setTranslateX(150);
        pSpriteBox.setTranslateY(-100);
        eSpriteBox.setTranslateX(-150);
        eSpriteBox.setTranslateY(150);
        StackPane arenaArea = new StackPane(pSpriteBox, eSpriteBox, opponentStatus, playerStatus);

        // ── Menu inferior ─────────────────────────────────────────────────
        HBox bottomMenu = new HBox(15);
        bottomMenu.setStyle(
            "-fx-background-color: #2a2a2a;" +
            "-fx-padding: 10;" +
            "-fx-border-color: #555;" +
            "-fx-border-width: 4 0 0 0;"
        );
        bottomMenu.setPrefHeight(150);
        bottomMenu.setMinHeight(150);
        bottomMenu.setAlignment(Pos.CENTER);

        // Caixa de texto da ação atual
        Label promptText = new Label("O que " + playerChar.getName() + "\nvai fazer?");
        promptText.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #444;" +
            "-fx-border-width: 5;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 15 30;" +
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'Segoe UI', sans-serif;"
        );
        promptText.setMaxHeight(Double.MAX_VALUE);
        promptText.setPrefWidth(500);
        HBox.setHgrow(promptText, Priority.ALWAYS);

        // Grade de botões de ação
        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(10);
        buttonGrid.setVgap(10);
        buttonGrid.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #444;" +
            "-fx-border-width: 5;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 15;"
        );
        buttonGrid.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // ── Botões principais ─────────────────────────────────────────────
        Button fightBtn   = criarBotaoBatalha("LUTAR",   "#ff8c94");
        Button bagBtn     = criarBotaoBatalha("MOCHILA", "#fcd059");
        Button pokemonBtn = criarBotaoBatalha("HEROIS",  "#85d685");
        Button runBtn     = criarBotaoBatalha("FUGIR",   "#7aa9f5");
        pokemonBtn.setDisable(true); // Não implementado nesta versão

        buttonGrid.add(fightBtn,   0, 0);
        buttonGrid.add(bagBtn,     1, 0);
        buttonGrid.add(pokemonBtn, 0, 1);
        buttonGrid.add(runBtn,     1, 1);

        bottomMenu.getChildren().addAll(promptText, buttonGrid);

        // ── Layout raiz da batalha ─────────────────────────────────────────
        BorderPane uiLayer = new BorderPane();
        uiLayer.setCenter(arenaArea);
        uiLayer.setBottom(bottomMenu);

        StackPane root = new StackPane(bg, uiLayer);
        root.setOpacity(0);

        mapScene.setRoot(root);
        if (!stage.isFullScreen()) stage.setFullScreen(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
        fadeIn.setToValue(1);
        fadeIn.play();

        // ── Helper: restaura o menu principal de ações ────────────────────
        // Chamado após submenu (ex: fechar o menu de ataques ou da mochila)
        Runnable voltarMenu = () -> {
            promptText.setText("O que " + playerChar.getName() + "\nvai fazer?");
            buttonGrid.getChildren().clear();
            buttonGrid.add(fightBtn,   0, 0);
            buttonGrid.add(bagBtn,     1, 0);
            buttonGrid.add(pokemonBtn, 0, 1);
            buttonGrid.add(runBtn,     1, 1);
            buttonGrid.setDisable(false);
        };

        // =========================================================================
        // AÇÃO: FUGIR
        // =========================================================================
        runBtn.setOnAction(e -> {
            if (playerChar.leave()) {
                // Fuga bem-sucedida
                promptText.setText("Fugiu com sucesso!");
                buttonGrid.setDisable(true);
                PauseTransition p = new PauseTransition(Duration.millis(800));
                p.setOnFinished(ev ->
                    sairDaBatalha(stage, mapScene, app, root, originalLayout, hudManager, playerChar));
                p.play();
            } else {
                // Fuga falhou — inimigo ataca
                promptText.setText("Não conseguiu fugir!");
                buttonGrid.setDisable(true);
                PauseTransition p = new PauseTransition(Duration.millis(1000));
                p.setOnFinished(ev ->
                    turnoInimigo(playerChar, monstro, playerStatus, pImg,
                                 root, app, buttonGrid, promptText, hudManager));
                p.play();
            }
        });

        // =========================================================================
        // AÇÃO: LUTAR
        // =========================================================================
        fightBtn.setOnAction(e -> {
            promptText.setText("Escolha seu ataque!");
            buttonGrid.getChildren().clear();

            Button attackBtn = criarBotaoBatalha("ATACAR", "#ff8c94");
            Button backBtn   = criarBotaoBatalha("VOLTAR", "#b3b3b3");
            buttonGrid.add(attackBtn, 0, 0);
            buttonGrid.add(backBtn,   1, 0);

            // Volta ao menu principal sem usar turno
            backBtn.setOnAction(ev -> voltarMenu.run());

            attackBtn.setOnAction(atk -> {
                buttonGrid.setDisable(true);

                // Animação de ataque — a lógica roda após a animação terminar
                animarAtaque(pImg, eImg, () -> {
                    int dano = playerChar.attack(monstro);
                    promptText.setText(playerChar.getName() + " causou " + dano + " de dano!");
                    atualizarBarraHp(opponentStatus, monstro.getLife(), monstro.getMaxLife(), false);

                    if (monstro.getLife() <= 0) {
                        // ── Vitória ──────────────────────────────────────
                        playerChar.earnXp(30);
                        int moedas = monstro.getDropCoin();
                        playerChar.addCoin(moedas);

                        if (hudManager != null) hudManager.atualizar(playerChar);

                        promptText.setText(monstro.getName() + " derrotado!\n"
                                           + "+" + moedas + " moedas  +30 XP");

                        PauseTransition win = new PauseTransition(Duration.millis(1800));
                        win.setOnFinished(ev ->
                            sairDaBatalha(stage, mapScene, app, root,
                                          originalLayout, hudManager, playerChar));
                        win.play();

                    } else {
                        // Inimigo ainda vivo — turno do inimigo
                        PauseTransition p = new PauseTransition(Duration.millis(1200));
                        p.setOnFinished(ev ->
                            turnoInimigo(playerChar, monstro, playerStatus, pImg,
                                         root, app, buttonGrid, promptText, hudManager));
                        p.play();
                    }
                });
            });
        });

        // =========================================================================
        // AÇÃO: MOCHILA (usar poções)
        // =========================================================================
        bagBtn.setOnAction(e -> {
            buttonGrid.getChildren().clear();

            // Filtra apenas poções do inventário
            List<Potion> pocoes = playerChar.getInventory().getItems().stream()
                .filter(it -> it instanceof Potion)
                .map(it -> (Potion) it)
                .toList();

            if (pocoes.isEmpty()) {
                promptText.setText("Sem poções no inventário!");
                Button back = criarBotaoBatalha("VOLTAR", "#b3b3b3");
                buttonGrid.add(back, 0, 0);
                back.setOnAction(ev -> voltarMenu.run());
                return;
            }

            promptText.setText("Escolha uma poção:");

            int col = 0, row = 0;
            for (int i = 0; i < Math.min(pocoes.size(), 3); i++) {
                final Potion p = pocoes.get(i);
                Button btn = criarBotaoBatalha(p.getName() + "\n+" + p.getHealedLife() + " HP", "#85d685");
                btn.setPrefSize(160, 60);

                btn.setOnAction(useEvent -> {
                    // Aplica a cura e remove do inventário
                    playerChar.heal(p.getHealedLife());
                    playerChar.getInventory().removeItem(p);

                    // Atualiza barra de HP na batalha e na HUD do mapa
                    atualizarBarraHp(playerStatus, playerChar.getLife(), playerChar.getMaxLife(), true);
                    promptText.setText("Usou " + p.getName() + "!\n+" + p.getHealedLife() + " HP recuperado.");
                    if (hudManager != null) hudManager.atualizar(playerChar);

                    // Usar item passa o turno para o inimigo
                    buttonGrid.setDisable(true);
                    PauseTransition pausa = new PauseTransition(Duration.millis(1200));
                    pausa.setOnFinished(ev ->
                        turnoInimigo(playerChar, monstro, playerStatus, pImg,
                                     root, app, buttonGrid, promptText, hudManager));
                    pausa.play();
                });

                buttonGrid.add(btn, col, row);
                col++;
                if (col > 1) { col = 0; row++; }
            }

            // Botão VOLTAR sempre disponível
            Button back = criarBotaoBatalha("VOLTAR", "#b3b3b3");
            buttonGrid.add(back, col, row);
            back.setOnAction(ev -> voltarMenu.run());
        });
    }

    // =========================================================================
    // TURNO DO INIMIGO
    // =========================================================================

    /**
     * Executa o turno do inimigo:
     * 1. Calcula dano (monstro.getDamage() - resistência do jogador)
     * 2. Aplica o dano
     * 3. Atualiza a HUD
     * 4. Verifica se o jogador morreu
     *
     * @param promptText Passado para exibir mensagem de dano
     */
    private static void turnoInimigo(Character playerChar, Monsters monstro,
                                      VBox playerStatus, ImageView pImg, StackPane root,
                                      App app, GridPane buttonGrid, Label promptText,
                                      HudManager hudManager) {
        // Dano real = dano do monstro - resistência do jogador (mínimo 0)
        int dano = Math.max(0, monstro.getDamage() - playerChar.getResistance());
        playerChar.setLife(playerChar.getLife() - dano);

        // Atualiza interface
        atualizarBarraHp(playerStatus, playerChar.getLife(), playerChar.getMaxLife(), true);
        animarTremor(pImg);
        if (hudManager != null) hudManager.atualizar(playerChar);

        if (playerChar.getLife() <= 0) {
            // Jogador morreu — exibe Game Over
            ativarGameOver(root, app);
        } else {
            // Jogador sobreviveu — reativa os botões
            buttonGrid.setDisable(false);
        }
    }

    // =========================================================================
    // GAME OVER
    // =========================================================================

    /**
     * Exibe a tela de Game Over sobre a batalha.
     *
     * CORREÇÃO BUG 1: resetInBattle() é chamado AQUI antes de redirecionar
     * para o menu, garantindo que o flag seja limpo mesmo neste caminho.
     * App.showMainMenu() também chama resetInBattle() como garantia extra.
     */
    private static void ativarGameOver(StackPane rootNode, App app) {
        // Overlay escuro sobre a batalha
        StackPane go = new StackPane();
        go.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        go.setOpacity(0);

        Label txt = new Label("Game Over");
        txt.setStyle(
            "-fx-text-fill: #ff0000;" +
            "-fx-font-size: 100px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'Impact', sans-serif;" +
            "-fx-effect: dropshadow(gaussian, rgba(255,0,0,0.5), 20, 0.5, 0, 0);"
        );

        // Estilos do botão de voltar ao menu
        final String estNormal =
            "-fx-background-color: #222;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 20px;" +
            "-fx-border-color: #ff0000;" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 20;";
        final String estHover =
            "-fx-background-color: #ff0000;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 20px;" +
            "-fx-border-color: #ff0000;" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 20;";

        Button menuBtn = new Button("Voltar para o Menu");
        menuBtn.setStyle(estNormal);
        menuBtn.setOnMouseEntered(e -> menuBtn.setStyle(estHover));
        menuBtn.setOnMouseExited(e  -> menuBtn.setStyle(estNormal));
        menuBtn.setOnAction(e -> {
            // CORREÇÃO BUG 1: reseta inBattle antes de voltar ao menu
            resetInBattle();
            app.showMainMenu();
        });

        StackPane.setAlignment(txt, Pos.CENTER);
        StackPane.setAlignment(menuBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(menuBtn, new Insets(0, 50, 50, 0));
        go.getChildren().addAll(txt, menuBtn);
        rootNode.getChildren().add(go);

        FadeTransition f = new FadeTransition(Duration.millis(1500), go);
        f.setToValue(1);
        f.play();
    }

    // =========================================================================
    // SAIR DA BATALHA (vitória ou fuga)
    // =========================================================================

    /**
     * Realiza o fade out da batalha e restaura a cena do mapa.
     * Chamado apenas no caminho de vitória ou fuga bem-sucedida.
     */
    private static void sairDaBatalha(Stage stage, Scene mapScene, App app,
                                       Pane rootLayout, StackPane originalLayout,
                                       HudManager hudManager, Character playerChar) {
        FadeTransition fo = new FadeTransition(Duration.millis(500), rootLayout);
        fo.setToValue(0);
        fo.setOnFinished(e -> {
            // CORREÇÃO BUG 1: inBattle = false antes de retornar ao mapa
            inBattle = false;
            mapScene.setRoot(originalLayout);
            originalLayout.setOpacity(1);
            if (!stage.isFullScreen()) stage.setFullScreen(true);
            // Atualiza HUD completa ao retornar (HP, moedas, XP)
            if (hudManager != null) hudManager.atualizar(playerChar);
            app.resumeTimers();
        });
        fo.play();
    }

    // =========================================================================
    // HELPERS DE UI — STATUS BOXES
    // =========================================================================

    /** Cria a caixa de status do jogador (com XP bar). */
    private static VBox criarStatusJogador(String nome, int nivel, int hp, int hpMax,
                                            int xp, int xpMax) {
        VBox box = construirBaseStatus(nome, "Lv " + nivel, hp, hpMax, 130);

        // Linha do HP numérico
        Label hpNum = new Label(Math.max(0, hp) + " / " + hpMax);
        hpNum.setId("hpText");
        hpNum.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        HBox row = new HBox(hpNum);
        row.setAlignment(Pos.CENTER_RIGHT);
        box.getChildren().add(row);

        // Barra de XP
        HBox xpRow = new HBox(10);
        xpRow.setAlignment(Pos.CENTER_RIGHT);
        Label xpLbl = new Label("XP");
        xpLbl.setStyle(
            "-fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-text-fill: #4da6ff;" +
            "-fx-background-color: #333;" +
            "-fx-padding: 0 5;" +
            "-fx-background-radius: 3;"
        );
        ProgressBar xpBar = new ProgressBar((double) xp / Math.max(1, xpMax));
        xpBar.setId("xpBar");
        xpBar.setPrefWidth(200);
        xpBar.setPrefHeight(10);
        xpBar.setStyle(
            "-fx-accent: #4da6ff;" +
            "-fx-control-inner-background: #444;" +
            "-fx-box-border: transparent;" +
            "-fx-background-radius: 5;"
        );
        xpRow.getChildren().addAll(xpLbl, xpBar);
        box.getChildren().add(xpRow);
        return box;
    }

    /** Cria a caixa de status do monstro (sem XP bar). */
    private static VBox criarStatusMonstro(String nome, int hp, int hpMax) {
        return construirBaseStatus(nome, "", hp, hpMax, 80);
    }

    /**
     * Constrói a base visual compartilhada entre status do jogador e do monstro.
     *
     * @param h Altura máxima da caixa
     */
    private static VBox construirBaseStatus(String nome, String nivel, int hp, int hpMax, int h) {
        VBox box = new VBox(5);
        box.setStyle(
            "-fx-background-color: #f0f0f0;" +
            "-fx-border-color: #333;" +
            "-fx-border-width: 4;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 20;"
        );
        box.setMaxWidth(300);
        box.setMaxHeight(h);

        // Linha de topo: nome e nível
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        Label nLbl = new Label(nome);
        nLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label lLbl = new Label(nivel);
        lLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        top.getChildren().addAll(nLbl, sp, lLbl);

        // Barra de HP
        HBox hpRow = new HBox(10);
        hpRow.setAlignment(Pos.CENTER_RIGHT);
        Label hpLbl = new Label("HP");
        hpLbl.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: bold;" +
            "-fx-text-fill: #e6b800;" +
            "-fx-background-color: #333;" +
            "-fx-padding: 0 5;" +
            "-fx-background-radius: 3;"
        );
        double d = Math.max(0, hp);
        ProgressBar hpBar = new ProgressBar(d / hpMax);
        hpBar.setId("hpBar");
        hpBar.setPrefWidth(200);
        hpBar.setPrefHeight(15);
        String cor = (d / hpMax) <= 0.2 ? "#ff3333" : "#48e85c";
        hpBar.setStyle(
            "-fx-accent: " + cor + ";" +
            "-fx-control-inner-background: #444;" +
            "-fx-box-border: transparent;" +
            "-fx-background-radius: 5;"
        );
        hpRow.getChildren().addAll(hpLbl, hpBar);
        box.getChildren().addAll(top, hpRow);
        return box;
    }

    // =========================================================================
    // HELPERS DE UI — BOTÕES
    // =========================================================================

    /**
     * Cria um botão estilizado no padrão Pokémon-like.
     *
     * @param texto Rótulo do botão
     * @param cor   Cor de fundo (hex CSS)
     */
    private static Button criarBotaoBatalha(String texto, String cor) {
        Button btn = new Button(texto);
        btn.setPrefSize(160, 50);
        String base =
            "-fx-background-color: " + cor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: #333;" +
            "-fx-border-width: 3;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 2, 2);";
        String hover = base.replace(
            "-fx-background-color: " + cor,
            "-fx-background-color: derive(" + cor + ", -15%)");
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    // =========================================================================
    // HELPERS DE UI — ATUALIZAÇÃO DAS BARRAS
    // =========================================================================

    /**
     * Atualiza a barra de HP no status box.
     *
     * @param status   VBox do status (jogador ou monstro)
     * @param hp       HP atual
     * @param hpMax    HP máximo
     * @param isPlayer Se true, também atualiza o label de HP numérico
     */
    private static void atualizarBarraHp(VBox status, int hp, int hpMax, boolean isPlayer) {
        ProgressBar bar = (ProgressBar) status.lookup("#hpBar");
        double d = Math.max(0, hp);
        if (bar != null) {
            bar.setProgress(d / hpMax);
            String cor = (d / hpMax) <= 0.2 ? "#ff3333" : "#48e85c";
            bar.setStyle(
                "-fx-accent: " + cor + ";" +
                "-fx-control-inner-background: #444;" +
                "-fx-box-border: transparent;" +
                "-fx-background-radius: 5;"
            );
        }
        if (isPlayer) {
            Label t = (Label) status.lookup("#hpText");
            if (t != null) t.setText((int) d + " / " + hpMax);
        }
    }

    // =========================================================================
    // ANIMAÇÕES
    // =========================================================================

    /**
     * Animação de ataque: o atacante avança em direção ao inimigo e volta.
     * A lógica real do ataque roda APÓS a animação terminar (onFinished).
     *
     * @param atacante  Sprite que se move
     * @param vitima    Sprite que recebe o golpe (visualmente)
     * @param onFinished Callback com a lógica de dano a aplicar
     */
    private static void animarAtaque(ImageView atacante, ImageView vitima, Runnable onFinished) {
        TranslateTransition move = new TranslateTransition(Duration.millis(200), atacante);
        move.setByX(100);
        move.setByY(-50);
        move.setAutoReverse(true);
        move.setCycleCount(2);
        move.setOnFinished(e -> onFinished.run());
        move.play();
    }

    /**
     * Animação de tremor: o alvo chacoalha horizontalmente ao receber dano.
     *
     * @param alvo Sprite que recebe o tremor
     */
    private static void animarTremor(ImageView alvo) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), alvo);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
}
