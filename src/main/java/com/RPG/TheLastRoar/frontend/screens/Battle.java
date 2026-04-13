package com.RPG.TheLastRoar.frontend.screens;

import java.util.List;

import com.RPG.TheLastRoar.App;
import com.RPG.TheLastRoar.backend.models.Character;
import com.RPG.TheLastRoar.backend.models.Monsters;
import com.RPG.TheLastRoar.backend.models.Potion;
import com.RPG.TheLastRoar.frontend.controllers.BattleAnimations;
import com.RPG.TheLastRoar.frontend.effects.BattleEffects;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

/**
 * ============================================================
 * Battle.java — Sistema de batalha por turnos
 * ============================================================
 *
 * RESPONSABILIDADE:
 *   Gerencia TODA a lógica da tela de batalha:
 *   troca de cena, turnos, vitória, derrota e retorno ao mapa.
 *
 * FLUXO COMPLETO DE UMA BATALHA:
 *
 *   App detecta colisão
 *        │
 *        ▼
 *   startBattle() ──► troca a cena (mapScene → battleRoot)
 *        │
 *        ▼
 *   Turno do Jogador  ──► LUTAR  → animarAtaque → dano → verifica morte
 *        │               MOCHILA → lista poções → usa poção → turno inimigo
 *        │               FUGIR   → tenta fuga → falha → turno inimigo
 *        │
 *        ▼
 *   Turno do Inimigo  ──► calcula dano (getDamage - getResistance)
 *        │               ▼
 *        │          Jogador vivo? → sim → reativa botões
 *        │                        → não → ativarGameOver()
 *        ▼
 *   Vitória/Fuga → sairDaBatalha() → restaura cena do mapa → resumeTimers()
 *
 * CORREÇÃO BUG — FLAG "inBattle":
 *   O flag estático inBattle impede batalhas simultâneas.
 *   Deve ser resetado em TODOS os caminhos de saída da batalha:
 *   - Vitória/Fuga: resetado em sairDaBatalha()
 *   - Game Over:    resetado em BattleAnimations.ativarGameOver()
 *   - showMainMenu: App chama resetInBattle() como garantia extra
 *
 * DELEGAÇÕES:
 *   - Todos os componentes visuais → BattleUI.java
 *   - Todas as animações → BattleAnimations.java
 *
 * USADO POR:
 *   App.java — chama startBattle() ao detectar colisão no mapa
 */
public class Battle {

    // =========================================================================
    // FLAG DE ESTADO
    // =========================================================================

    /**
     * Flag estático que impede múltiplas batalhas simultâneas.
     * Resetado via resetInBattle() em todos os caminhos de saída.
     */
    private static boolean inBattle = false;

    /**
     * Reseta o flag inBattle para false.
     * Deve ser chamado ao voltar ao mapa (vitória/fuga) ou ao menu principal.
     */
    public static void resetInBattle() {
        inBattle = false;
    }

    // =========================================================================
    // START BATTLE — Ponto de entrada da batalha
    // =========================================================================

    /**
     * Inicia a tela de batalha, substituindo temporariamente a cena do mapa.
     *
     * @param stage          Janela principal JavaFX
     * @param mapScene       Cena do mapa (será restaurada ao sair da batalha)
     * @param playerChar     Personagem do jogador
     * @param monster        Inimigo desta batalha
     * @param playerView     Sprite do jogador no mapa (referência, não usada na batalha)
     * @param enemyView      Sprite do inimigo no mapa (referência, não usada na batalha)
     * @param app            Instância do App (para chamar resumeTimers/showMainMenu)
     * @param originalLayout StackPane original do mapa (restaurado ao terminar)
     * @param hudManager     HUD do jogo (atualizado após batalha)
     */
    public static void startBattle(Stage stage, Scene mapScene, Character playerChar,
                                    Monsters monster, ImageView playerView, ImageView enemyView,
                                    App app, StackPane originalLayout, HudManager hudManager) {
        if (inBattle) return; // Evita batalha duplicada
        inBattle = true;

        // ── Monta a cena da batalha ───────────────────────────────────────
        StackPane battleRoot = buildBattleScene(
            stage, mapScene, playerChar, monster,
            app, originalLayout, hudManager
        );

        // ── Substitui a cena do mapa pela batalha com fade in ─────────────
        battleRoot.setOpacity(0);
        mapScene.setRoot(battleRoot);
        if (!stage.isFullScreen()) stage.setFullScreen(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), battleRoot);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    // =========================================================================
    // BUILD SCENE — Montagem da UI de batalha
    // =========================================================================

    /**
     * Constrói e retorna o StackPane raiz da tela de batalha.
     * Divide a responsabilidade em submétodos para maior clareza.
     *
     * ESTRUTURA DA CENA:
     *   StackPane (battleRoot)
     *    ├── ImageView (background)
     *    └── BorderPane (uiLayer)
     *         ├── CENTER: arenaArea (sprites + status boxes)
     *         └── BOTTOM: bottomMenu (prompt + botões de ação)
     */
    private static StackPane buildBattleScene(Stage stage, Scene mapScene,
                                               Character playerChar, Monsters monster,
                                               App app, StackPane originalLayout,
                                               HudManager hudManager) {
        // ── Fundo da batalha ──────────────────────────────────────────────
        ImageView bg = loadBackground(stage);

        // ── Sprites dos combatentes ───────────────────────────────────────
        ImageView playerImg = loadPlayerSprite(playerChar);
        ImageView enemyImg  = loadEnemySprite(monster);

        // ── Caixas de status (HP, XP, nível) ─────────────────────────────
        VBox opponentStatus = buildOpponentStatus(monster);
        VBox playerStatus   = buildPlayerStatus(playerChar);

        // ── Arena (sprites + status) ──────────────────────────────────────
        StackPane arenaArea = buildArena(playerImg, enemyImg, opponentStatus, playerStatus);

        // ── Menu inferior (prompt + botões) ──────────────────────────────
        Label    promptLabel = buildPromptLabel(playerChar);
        GridPane buttonGrid  = new GridPane();
        buttonGrid.setHgap(10);
        buttonGrid.setVgap(10);
        buttonGrid.setStyle(buildButtonGridStyle());
        buttonGrid.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        HBox bottomMenu = buildBottomMenu(promptLabel, buttonGrid);

        // ── Layout raiz da batalha ────────────────────────────────────────
        BorderPane uiLayer = new BorderPane();
        uiLayer.setCenter(arenaArea);
        uiLayer.setBottom(bottomMenu);

        StackPane root = new StackPane(bg, uiLayer);

        // ── Conecta os botões de ação ────────────────────────────────────
        wireActionButtons(
            root, stage, mapScene, playerChar, monster,
            playerImg, enemyImg, opponentStatus, playerStatus,
            promptLabel, buttonGrid, app, originalLayout, hudManager
        );

        return root;
    }

    // =========================================================================
    // ACTION BUTTONS — Conecta a lógica aos botões
    // =========================================================================

    /**
     * Cria os 4 botões de ação e conecta suas lógicas.
     * Usa um Runnable 'backToMenu' para restaurar o menu principal a qualquer hora.
     */
    private static void wireActionButtons(StackPane root, Stage stage, Scene mapScene,
                                           Character playerChar, Monsters monster,
                                           ImageView playerImg, ImageView enemyImg,
                                           VBox opponentStatus, VBox playerStatus,
                                           Label promptLabel, GridPane buttonGrid,
                                           App app, StackPane originalLayout,
                                           HudManager hudManager) {

        Button fightBtn  = BattleUI.criarBotaoBatalha("LUTAR",   "#3a1a1a", "#6a2a2a");
        Button bagBtn    = BattleUI.criarBotaoBatalha("MOCHILA", "#1a2a1a", "#2a5a2a");
        Button heroesBtn = BattleUI.criarBotaoBatalha("HEROIS",  "#1a2a1a", "#2a5a2a");
        Button runBtn    = BattleUI.criarBotaoBatalha("FUGIR",   "#1a1a3a", "#2a2a6a");
        heroesBtn.setDisable(true); // Não implementado

        buttonGrid.add(fightBtn,  0, 0);
        buttonGrid.add(bagBtn,    1, 0);
        buttonGrid.add(heroesBtn, 0, 1);
        buttonGrid.add(runBtn,    1, 1);

        // Restaura o menu de ação principal (chamado após submenus)
        Runnable backToMenu = () -> {
            promptLabel.setText("O que " + playerChar.getName() + "\nvai fazer?");
            buttonGrid.getChildren().clear();
            buttonGrid.add(fightBtn,  0, 0);
            buttonGrid.add(bagBtn,    1, 0);
            buttonGrid.add(heroesBtn, 0, 1);
            buttonGrid.add(runBtn,    1, 1);
            buttonGrid.setDisable(false);
        };

        // ── FUGIR ─────────────────────────────────────────────────────────
        runBtn.setOnAction(e -> handleRunAction(
            playerChar, monster, playerStatus, playerImg,
            root, app, buttonGrid, promptLabel, hudManager, backToMenu,
            stage, mapScene, originalLayout
        ));

        // ── LUTAR ─────────────────────────────────────────────────────────
        fightBtn.setOnAction(e -> handleFightAction(
            playerChar, monster, playerImg, enemyImg,
            opponentStatus, playerStatus, root, app,
            buttonGrid, promptLabel, hudManager,
            backToMenu, stage, mapScene, originalLayout
        ));

        // ── MOCHILA ───────────────────────────────────────────────────────
        bagBtn.setOnAction(e -> handleBagAction(
            playerChar, monster, playerStatus, playerImg,
            root, app, buttonGrid, promptLabel, hudManager, backToMenu
        ));
    }

    // =========================================================================
    // ACTION HANDLERS — Lógica de cada ação do jogador
    // =========================================================================

    /**
     * Lida com a ação FUGIR.
     * Sucesso → sai da batalha. Falha → passa turno para o inimigo.
     */
    private static void handleRunAction(Character playerChar, Monsters monster,
                                         VBox playerStatus, ImageView playerImg,
                                         StackPane root, App app, GridPane buttonGrid,
                                         Label promptLabel, HudManager hudManager,
                                         Runnable backToMenu, Stage stage, Scene mapScene,
                                         StackPane originalLayout) {
        if (playerChar.leave()) {
            // Fuga bem-sucedida
            promptLabel.setText("Fugiu com sucesso!");
            buttonGrid.setDisable(true);
            PauseTransition pause = new PauseTransition(Duration.millis(800));
            pause.setOnFinished(ev ->
                exitBattle(stage, mapScene, app, root, originalLayout, hudManager, playerChar));
            pause.play();
        } else {
            // Fuga falhou — inimigo ataca
            promptLabel.setText("Não conseguiu fugir!");
            buttonGrid.setDisable(true);
            PauseTransition pause = new PauseTransition(Duration.millis(1000));
            pause.setOnFinished(ev ->
                executeEnemyTurn(playerChar, monster, playerStatus, playerImg,
                                 root, app, buttonGrid, promptLabel, hudManager));
            pause.play();
        }
    }

    /**
     * Lida com a ação LUTAR.
     * Abre submenu com botão "ATACAR" e "VOLTAR".
     */
    private static void handleFightAction(Character playerChar, Monsters monster,
                                           ImageView playerImg, ImageView enemyImg,
                                           VBox opponentStatus, VBox playerStatus,
                                           StackPane root, App app, GridPane buttonGrid,
                                           Label promptLabel, HudManager hudManager,
                                           Runnable backToMenu, Stage stage, Scene mapScene,
                                           StackPane originalLayout) {
        promptLabel.setText("Escolha seu ataque!");
        buttonGrid.getChildren().clear();

        Button attackBtn = BattleUI.criarBotaoBatalha("ATACAR", "#6a2a2a", "#8a3a3a");
        Button backBtn   = BattleUI.criarBotaoBatalha("VOLTAR", "#2a2a2a", "#4a4a4a");
        buttonGrid.add(attackBtn, 0, 0);
        buttonGrid.add(backBtn,   1, 0);

        backBtn.setOnAction(ev -> backToMenu.run());

        attackBtn.setOnAction(atk -> {
            buttonGrid.setDisable(true);

            // Animação de ataque → lógica só roda após o visual terminar
            BattleAnimations.animarAtaque(playerImg, enemyImg, () -> {
                int damage = playerChar.attack(monster);
                // 20% de chance de crítico (baseado em chance, não em comparação de dano)
                boolean isCritical = Math.random() < 0.20;
                
                BattleAnimations.animarHitFlash(enemyImg);
                
                // NOVOS EFEITOS: Números flutuantes, partículas e screen shake
                double damageX = enemyImg.getLayoutX() + enemyImg.getFitWidth() / 2;
                double damageY = enemyImg.getLayoutY() + enemyImg.getFitHeight() / 2;
                
                // Mostra número flutuante com tipo correto
                String damageType = isCritical ? "CRITICAL" : "NORMAL";
                BattleEffects.animateFloatingDamage(root, damageX, damageY, String.valueOf(damage), true, isCritical);
                
                // Partículas de impacto diferenciadas
                BattleEffects.createImpactParticles(root, damageX, damageY, damageType);
                
                // Screen shake proporcional ao dano
                if (isCritical) {
                    BattleEffects.playScreenShake(root, "CRITICAL");
                } else {
                    BattleEffects.playScreenShake(root, "NORMAL");
                }
                
                BattleUI.atualizarBarraHp(opponentStatus, monster.getLife(), monster.getMaxLife());
                String messagePrefix = isCritical ? " ACERTO CRÍTICO: " : " ";
                promptLabel.setText(playerChar.getName() + messagePrefix + damage + " de dano!");

                if (monster.getLife() <= 0) {
                    // ── Vitória ───────────────────────────────────────────
                    playerChar.earnXp(30);
                    int coins = monster.getDropCoin();
                    playerChar.addCoin(coins);
                    if (hudManager != null) hudManager.atualizar(playerChar);

                    promptLabel.setText(monster.getName() + " derrotado!\n"
                                       + "+" + coins + " moedas  +30 XP");

                    PauseTransition win = new PauseTransition(Duration.millis(1800));
                    win.setOnFinished(ev ->
                        exitBattle(stage, mapScene, app, root,
                                   originalLayout, hudManager, playerChar));
                    win.play();

                } else {
                    // ── Inimigo sobreviveu → turno do inimigo ─────────────
                    PauseTransition pause = new PauseTransition(Duration.millis(1200));
                    pause.setOnFinished(ev ->
                        executeEnemyTurn(playerChar, monster, playerStatus, playerImg,
                                         root, app, buttonGrid, promptLabel, hudManager));
                    pause.play();
                }
            });
        });
    }

    /**
     * Lida com a ação MOCHILA.
     * Lista poções do inventário. Usar poção passa o turno ao inimigo.
     */
    private static void handleBagAction(Character playerChar, Monsters monster,
                                         VBox playerStatus, ImageView playerImg,
                                         StackPane root, App app, GridPane buttonGrid,
                                         Label promptLabel, HudManager hudManager,
                                         Runnable backToMenu) {
        buttonGrid.getChildren().clear();

        // Filtra apenas poções do inventário
        List<Potion> potions = playerChar.getInventory().getItems().stream()
            .filter(it -> it instanceof Potion)
            .map(it -> (Potion) it)
            .toList();

        if (potions.isEmpty()) {
            promptLabel.setText("Sem poções no inventário!");
            Button back = BattleUI.criarBotaoBatalha("VOLTAR", "#2a2a2a", "#4a4a4a");
            buttonGrid.add(back, 0, 0);
            back.setOnAction(ev -> backToMenu.run());
            return;
        }

        promptLabel.setText("Escolha uma poção:");

        int col = 0, row = 0;
        for (int i = 0; i < Math.min(potions.size(), 3); i++) {
            final Potion potion = potions.get(i);
            Button potionBtn = BattleUI.criarBotaoBatalha(
                potion.getName() + "\n+" + potion.getHealedLife() + " HP",
                "#1a4a1a", "#2a6a2a"
            );
            potionBtn.setPrefSize(160, 60);

            potionBtn.setOnAction(useEvent -> {
                // Aplica cura e remove do inventário
                playerChar.heal(potion.getHealedLife());
                playerChar.getInventory().removeItem(potion);

                BattleUI.atualizarBarraHp(playerStatus, playerChar.getLife(), playerChar.getMaxLife());
                
                // NOVOS EFEITOS: Números flutuantes verdes, partículas de cura
                double healX = playerImg.getLayoutX() + playerImg.getFitWidth() / 2;
                double healY = playerImg.getLayoutY() + playerImg.getFitHeight() / 2;
                
                // Mostra número flutuante com tipo HEAL (verde)
                BattleEffects.animateFloatingDamage(root, healX, healY, 
                    "+" + potion.getHealedLife(), false, false); // false = não é dano, é cura
                
                // Partículas de cura (verde)
                BattleEffects.createImpactParticles(root, healX, healY, "HEAL");
                
                promptLabel.setText("Usou " + potion.getName() + "!\n+" + potion.getHealedLife() + " HP");
                if (hudManager != null) hudManager.atualizar(playerChar);

                // Usar item passa o turno para o inimigo
                buttonGrid.setDisable(true);
                PauseTransition pause = new PauseTransition(Duration.millis(1200));
                pause.setOnFinished(ev ->
                    executeEnemyTurn(playerChar, monster, playerStatus, playerImg,
                                     root, app, buttonGrid, promptLabel, hudManager));
                pause.play();
            });

            buttonGrid.add(potionBtn, col, row);
            col++;
            if (col > 1) { col = 0; row++; }
        }

        Button back = BattleUI.criarBotaoBatalha("VOLTAR", "#2a2a2a", "#4a4a4a");
        buttonGrid.add(back, col, row);
        back.setOnAction(ev -> backToMenu.run());
    }

    // =========================================================================
    // ENEMY TURN — Turno do inimigo
    // =========================================================================

    /**
     * Executa o turno do inimigo:
     *   1. Calcula dano (getDamage() - getResistance(), mínimo 0)
     *   2. Aplica o dano ao jogador
     *   3. Anima o tremor no sprite do jogador
     *   4. Atualiza a barra de HP e o HUD
     *   5. Verifica se o jogador morreu → Game Over ou reativa botões
     *
     * @param promptLabel Exibe a mensagem de dano recebido
     */
    private static void executeEnemyTurn(Character playerChar, Monsters monster,
                                          VBox playerStatus, ImageView playerImg,
                                          StackPane root, App app, GridPane buttonGrid,
                                          Label promptLabel, HudManager hudManager) {

        int damage = Math.max(0, monster.getDamage() - playerChar.getResistance());
        boolean isCritical = Math.random() > 0.7; // 30% crítico
        if (isCritical) damage = (int)(damage * 1.5);
        
        playerChar.setLife(playerChar.getLife() - damage);

        // Atualiza a UI
        BattleUI.atualizarBarraHp(playerStatus, playerChar.getLife(), playerChar.getMaxLife());
        BattleAnimations.animarTremor(playerImg);
        BattleAnimations.animarHitFlash(playerImg);
        
        // NOVOS EFEITOS: Números flutuantes, partículas e screen shake
        double damageX = playerImg.getLayoutX() + playerImg.getFitWidth() / 2;
        double damageY = playerImg.getLayoutY() + playerImg.getFitHeight() / 2;
        
        // Mostra número flutuante com tipo correto
        String damageType = isCritical ? "CRITICAL" : "NORMAL";
        BattleEffects.animateFloatingDamage(root, damageX, damageY, String.valueOf(damage), true, isCritical);
        
        // Partículas de impacto diferenciadas
        BattleEffects.createImpactParticles(root, damageX, damageY, damageType);
        
        // Screen shake proporcional ao dano
        if (isCritical) {
            BattleEffects.playScreenShake(root, "CRITICAL");
        } else {
            BattleEffects.playScreenShake(root, "NORMAL");
        }
        
        if (hudManager != null) hudManager.atualizar(playerChar);

        String message = isCritical 
            ? monster.getName() + " causou " + damage + " de DANO CRÍTICO!"
            : monster.getName() + " causou " + damage + " de dano!";
        promptLabel.setText(message);

        if (playerChar.getLife() <= 0) {
            // Jogador morreu — exibe tela de Game Over
            BattleAnimations.ativarGameOver(root, app);
        } else {
            // Jogador sobreviveu — reativa os botões para o próximo turno
            PauseTransition pause = new PauseTransition(Duration.millis(800));
            pause.setOnFinished(e -> buttonGrid.setDisable(false));
            pause.play();
        }
    }

    // =========================================================================
    // EXIT BATTLE — Retorno ao mapa (vitória ou fuga)
    // =========================================================================

    /**
     * Sai da batalha com fade out e restaura a cena do mapa.
     * Chamado apenas nos caminhos de vitória e fuga bem-sucedida.
     * Reseta inBattle = false neste método.
     */
    private static void exitBattle(Stage stage, Scene mapScene, App app,
                                    Pane rootLayout, StackPane originalLayout,
                                    HudManager hudManager, Character playerChar) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), rootLayout);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            inBattle = false; // Reseta o flag ao sair pelo caminho de vitória/fuga
            mapScene.setRoot(originalLayout);
            originalLayout.setOpacity(1);
            if (!stage.isFullScreen()) stage.setFullScreen(true);
            if (hudManager != null) hudManager.atualizar(playerChar);
            app.resumeTimers();
        });
        fadeOut.play();
    }

    // =========================================================================
    // PRIVATE BUILDERS — Subdivisão da construção da cena
    // =========================================================================

    /** Carrega e configura o fundo da batalha. */
    private static ImageView loadBackground(Stage stage) {
        ImageView bg = new ImageView();
        try {
            var url = Battle.class.getResource("/images/battlebg2.png");
            if (url != null) bg.setImage(new Image(url.toExternalForm()));
        } catch (Exception ex) {
            System.err.println("[Battle] Fundo não encontrado: " + ex.getMessage());
        }
        bg.fitWidthProperty().bind(stage.widthProperty());
        bg.fitHeightProperty().bind(stage.heightProperty());
        return bg;
    }

    /** Carrega o sprite de batalha do jogador. */
    private static ImageView loadPlayerSprite(Character playerChar) {
        ImageView img = new ImageView();
        if (playerChar.getBattleSprite() != null) {
            img.setImage(playerChar.getBattleSprite());
        }
        img.setFitWidth(200);
        img.setPreserveRatio(true);
        return img;
    }

    /** Carrega o sprite de batalha do inimigo. */
    private static ImageView loadEnemySprite(Monsters monster) {
        ImageView img = new ImageView();
        try {
            var url = Battle.class.getResource(monster.getBattleImagePath());
            if (url != null) img.setImage(new Image(url.toExternalForm()));
        } catch (Exception ex) {
            System.err.println("[Battle] Sprite do monstro não encontrado: " + ex.getMessage());
        }
        img.setFitWidth(200);
        img.setPreserveRatio(true);
        return img;
    }

    /** Cria o status box do inimigo (canto superior esquerdo). */
    private static VBox buildOpponentStatus(Monsters monster) {
        VBox box = BattleUI.criarStatusMonstro(
            monster.getName(), monster.getLife(), monster.getMaxLife()
        );
        StackPane.setMargin(box, new Insets(50, 0, 0, 50));
        StackPane.setAlignment(box, Pos.TOP_LEFT);
        return box;
    }

    /** Cria o status box do jogador (canto inferior direito). */
    private static VBox buildPlayerStatus(Character playerChar) {
        VBox box = BattleUI.criarStatusJogador(
            playerChar.getName(), playerChar.getNivel(),
            playerChar.getLife(), playerChar.getMaxLife(),
            playerChar.getXp(), playerChar.getMaxXp()
        );
        StackPane.setMargin(box, new Insets(0, 50, 50, 0));
        StackPane.setAlignment(box, Pos.BOTTOM_RIGHT);
        return box;
    }

    /** Monta a área central com sprites e status boxes. */
    private static StackPane buildArena(ImageView playerImg, ImageView enemyImg,
                                         VBox opponentStatus, VBox playerStatus) {
        VBox playerBox = new VBox(playerImg);
        VBox enemyBox  = new VBox(enemyImg);
        playerBox.setAlignment(Pos.BOTTOM_LEFT);
        enemyBox.setAlignment(Pos.TOP_RIGHT);
        playerBox.setTranslateX(150);
        playerBox.setTranslateY(-100);
        enemyBox.setTranslateX(-150);
        enemyBox.setTranslateY(150);
        return new StackPane(playerBox, enemyBox, opponentStatus, playerStatus);
    }

    /** Cria o label de prompt (texto de ação atual). */
    private static Label buildPromptLabel(Character playerChar) {
        Label label = new Label("O que " + playerChar.getName() + "\nvai fazer?");
        label.setStyle(
            "-fx-background-color: rgba(8, 6, 18, 0.95);" +
            "-fx-border-color: #C8A000; -fx-border-width: 3;" +
            "-fx-border-radius: 12; -fx-background-radius: 12;" +
            "-fx-padding: 18 28;" +
            "-fx-font-size: 20px; -fx-font-weight: bold;" +
            "-fx-text-fill: #F0E6C0;" +
            "-fx-font-family: '" + BattleUI.FONTE_RPG + "', sans-serif;"
        );
        label.setMaxHeight(Double.MAX_VALUE);
        label.setPrefWidth(500);
        HBox.setHgrow(label, Priority.ALWAYS);
        return label;
    }

    /** Monta o menu inferior com prompt e grid de botões. */
    private static HBox buildBottomMenu(Label promptLabel, GridPane buttonGrid) {
        HBox menu = new HBox(15);
        menu.setStyle(
            "-fx-background-color: rgba(8, 6, 18, 0.95);" +
            "-fx-padding: 10;" +
            "-fx-border-color: #C8A000;" +
            "-fx-border-width: 3 0 0 0;"
        );
        menu.setPrefHeight(150);
        menu.setMinHeight(150);
        menu.setAlignment(Pos.CENTER);
        menu.getChildren().addAll(promptLabel, buttonGrid);
        return menu;
    }

    /** Retorna o estilo CSS do GridPane dos botões. */
    private static String buildButtonGridStyle() {
        return "-fx-background-color: rgba(8, 6, 18, 0.95);" +
               "-fx-border-color: #C8A000; -fx-border-width: 3;" +
               "-fx-border-radius: 12; -fx-background-radius: 12;" +
               "-fx-padding: 15;";
    }
}




