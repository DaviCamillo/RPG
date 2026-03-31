package com.RPG.TheLastRoar;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * ============================================================
 * ShopNPC.java — NPC vendedor posicionado no mapa 0
 * ============================================================
 *
 * RESPONSABILIDADE:
 * Representa o NPC ferreiro visível no mapa 0. Quando o jogador
 * se aproxima, App.java detecta a colisão e chama abrirLoja().
 * A tela da loja é um overlay sobre o jogo (igual ao PauseMenu).
 *
 * CORREÇÃO BUG 2 — LOJA NÃO FECHAVA:
 * Causa original: ShopNPC tinha seu próprio flag estático 'lojaAberta'.
 * Se App.lojaAberta e ShopNPC.lojaAberta ficassem fora de sincronia
 * (ex: por erro de callback), a loja bloqueava permanentemente.
 *
 * Correção: O campo 'lojaAberta' foi COMPLETAMENTE REMOVIDO deste arquivo.
 * App.java é o ÚNICO responsável por gerenciar o estado da loja.
 * abrirLoja() agora é uma função pura — apenas monta a UI e chama
 * onClose quando o jogador pressiona FECHAR. Quem seta/reseta o flag
 * é exclusivamente App.verificarColisaoNPC().
 *
 * ITENS À VENDA:
 * Armas  : Adaga (10), Espada Longa (25), Katana (50)
 * Poções : Poção Pequena (5), Poção Grande (15)
 */
public class ShopNPC {

    // ── Posição e tamanho do NPC no mapa ──────────────────────────────────
    public static final double NPC_X    = 200;
    public static final double NPC_Y    = 100;
    public static final double NPC_SIZE = 70;

    // ── Raio de proximidade para abrir a loja ──────────────────────────────
    private static final double RAIO_COLISAO = 80;

    // =========================================================================
    // CRIAÇÃO DO SPRITE DO NPC
    // =========================================================================

    public static ImageView criarSprite() {
        ImageView npcView = new ImageView();
        try {
            var url = ShopNPC.class.getResource("/images/npc_vendedor.png");
            if (url != null) {
                npcView.setImage(new Image(url.toExternalForm()));
            } else {
                System.out.println("[ShopNPC] npc_vendedor.png não encontrado, usando placeholder.");
            }
        } catch (Exception e) {
            System.out.println("[ShopNPC] Erro ao carregar sprite: " + e.getMessage());
        }

        npcView.setFitWidth(NPC_SIZE);
        npcView.setFitHeight(NPC_SIZE);
        npcView.setX(NPC_X);
        npcView.setY(NPC_Y);
        npcView.setPreserveRatio(true);
        npcView.setId("npc_vendedor");
        return npcView;
    }

    // =========================================================================
    // VERIFICAÇÃO DE COLISÃO
    // =========================================================================

    public static boolean verificarColisao(double playerX, double playerY) {
        double npcCentroX  = NPC_X + NPC_SIZE / 2;
        double npcCentroY  = NPC_Y + NPC_SIZE / 2;
        double playerCentroX = playerX + 40;
        double playerCentroY = playerY + 40;

        double dx = playerCentroX - npcCentroX;
        double dy = playerCentroY - npcCentroY;
        return Math.sqrt(dx * dx + dy * dy) < RAIO_COLISAO;
    }

    // =========================================================================
    // ABERTURA DA LOJA
    // =========================================================================

    public static void abrirLoja(StackPane mainLayout, Character player,
                                  HudManager hudManager, Runnable onClose) {
        // Monta o painel da loja
        VBox painel = construirPainelLoja(player, hudManager, () ->
            fechar(mainLayout, onClose)
        );

        // Overlay escuro semitransparente
        StackPane overlay = new StackPane(painel);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.82);");
        overlay.setAlignment(Pos.CENTER_LEFT);

        // Animação de entrada com fade
        overlay.setOpacity(0);
        mainLayout.getChildren().add(overlay);

        FadeTransition ft = new FadeTransition(Duration.millis(250), overlay);
        ft.setToValue(1.0);
        ft.play();
    }

    // =========================================================================
    // CONSTRUÇÃO DO PAINEL DA LOJA
    // =========================================================================

    private static VBox construirPainelLoja(Character player, HudManager hudManager,
                                             Runnable onClose) {
        VBox painel = new VBox(0);
        painel.setAlignment(Pos.CENTER_LEFT);
        painel.setPadding(new Insets(0, 0, 0, 120));

        // ── Título ────────────────────────────────────────────────────────
        Text titulo = new Text("LOJA DO FERREIRO");
        titulo.setFont(Font.font("Palatino Linotype", FontWeight.BOLD, 44));
        titulo.setFill(Color.web("#F0E6C0"));
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#C8A000", 0.8));
        sombra.setRadius(20);
        titulo.setEffect(sombra);
        VBox.setMargin(titulo, new Insets(0, 0, 6, 0));
        painel.getChildren().add(titulo);

        // ── Linha decorativa ──────────────────────────────────────────────
        Line sep = new Line(0, 0, 320, 0);
        sep.setStroke(Color.web("#B8960C", 0.6));
        sep.setStrokeWidth(1.0);
        VBox.setMargin(sep, new Insets(4, 0, 16, 0));
        painel.getChildren().add(sep);

        // ── Ouro do jogador ───────────────────────────────────────────────
        Label lblOuro = new Label("Ouro:  " + player.getCoin() + "  \uD83E\uDE99");
        lblOuro.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblOuro.setTextFill(Color.GOLD);
        VBox.setMargin(lblOuro, new Insets(0, 0, 20, 4));
        painel.getChildren().add(lblOuro);

        // ── Seção: Armas ──────────────────────────────────────────────────
        Text lblArmas = new Text("ARMAS");
        lblArmas.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblArmas.setFill(Color.web("#888070"));
        VBox.setMargin(lblArmas, new Insets(0, 0, 8, 4));
        painel.getChildren().add(lblArmas);

        adicionarItemLoja(painel, new Sword("Adaga",       10, 5, "Comum", 2),
                10, "Ataca 2x por turno", player, lblOuro, hudManager);
        adicionarItemLoja(painel, new Sword("Espada Longa", 25, 8, "Rara",  3),
                25, "Crítico com 18+",    player, lblOuro, hudManager);
        adicionarItemLoja(painel, new Sword("Katana",      50, 10, "Rara",  3),
                50, "Crítico 3x",         player, lblOuro, hudManager);

        // ── Seção: Poções ──────────────────────────────────────────────────
        Text lblPocoes = new Text("POÇÕES");
        lblPocoes.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblPocoes.setFill(Color.web("#888070"));
        VBox.setMargin(lblPocoes, new Insets(14, 0, 8, 4));
        painel.getChildren().add(lblPocoes);

        adicionarItemLoja(painel, new Potion("Poção Pequena", 5,  1, 30),
                5,  "Restaura 30 HP", player, lblOuro, hudManager);
        adicionarItemLoja(painel, new Potion("Poção Grande",  15, 1, 70),
                15, "Restaura 70 HP", player, lblOuro, hudManager);

        // ── Botão Fechar ──────────────────────────────────────────────────
        Button btnFechar = criarBotaoFechar();
        VBox.setMargin(btnFechar, new Insets(20, 0, 0, 0));
        btnFechar.setOnAction(e -> onClose.run());
        painel.getChildren().add(btnFechar);

        return painel;
    }

    // =========================================================================
    // LINHA DE ITEM DA LOJA
    // =========================================================================

    private static void adicionarItemLoja(VBox painel, Item item, int preco,
                                           String descricao, Character player,
                                           Label lblOuro, HudManager hudManager) {
        HBox linha = new HBox(14);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setMaxWidth(560);
        linha.setStyle(
            "-fx-background-color: rgba(20,20,20,0.6);" +
            "-fx-border-color: #443E30;" +
            "-fx-border-width: 0 0 0 2;" +
            "-fx-padding: 10 16 10 12;" +
            "-fx-background-radius: 6;"
        );
        VBox.setMargin(linha, new Insets(0, 0, 6, 0));

        // Ícone do tipo
        Label icone = new Label(item instanceof Sword ? "⚔" : "🧪");
        icone.setFont(Font.font("Segoe UI Emoji", 20));

        // Nome e atributo
        VBox infoBox = new VBox(2);
        Label nome = new Label(item.getName());
        nome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        nome.setTextFill(Color.web("#E8DFC0"));

        String atributo;
        if      (item instanceof Sword s)  atributo = "Dano: " + s.getDamage() + "  |  " + descricao;
        else if (item instanceof Potion p) atributo = "+" + p.getHealedLife() + " HP  |  " + descricao;
        else                               atributo = descricao;

        Label desc = new Label(atributo);
        desc.setFont(Font.font("Segoe UI", 12));
        desc.setTextFill(Color.web("#888070"));
        infoBox.getChildren().addAll(nome, desc);

        // Espaçador flexível
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Preço
        Label lblPreco = new Label(preco + " \uD83E\uDE99");
        lblPreco.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblPreco.setTextFill(Color.GOLD);
        lblPreco.setMinWidth(70);

        // Estilos do botão COMPRAR
        final String estNormal =
            "-fx-background-color: rgba(0,180,180,0.15);" +
            "-fx-border-color: #00E5E5;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-text-fill: #00E5E5;" +
            "-fx-padding: 6 14;" +
            "-fx-cursor: hand;";
        final String estHover =
            "-fx-background-color: rgba(0,200,200,0.30);" +
            "-fx-border-color: #00E5E5;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 6 14;" +
            "-fx-cursor: hand;";
        final String estDisable =
            "-fx-background-color: rgba(20,20,20,0.4);" +
            "-fx-border-color: #443E30;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-text-fill: #443E30;" +
            "-fx-padding: 6 14;";

        Button btnComprar = new Button("COMPRAR");
        btnComprar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnComprar.setStyle(estNormal);
        btnComprar.setOnMouseEntered(e -> { if (!btnComprar.isDisabled()) btnComprar.setStyle(estHover); });
        btnComprar.setOnMouseExited(e ->  { if (!btnComprar.isDisabled()) btnComprar.setStyle(estNormal); });

        // Desabilita se o jogador não tiver moedas suficientes ao abrir
        if (player.getCoin() < preco) {
            btnComprar.setDisable(true);
            btnComprar.setStyle(estDisable);
        }

        // ── Lógica de compra ──────────────────────────────────────────────
        btnComprar.setOnAction(e -> {
            // Verifica se tem ouro
            if (player.getCoin() < preco) {
                nome.setTextFill(Color.web("#FF6666")); // Flash vermelho
                PauseTransition ptSemOuro = new PauseTransition(Duration.millis(600));
                ptSemOuro.setOnFinished(ev -> nome.setTextFill(Color.web("#E8DFC0")));
                ptSemOuro.play();
                return;
            }

            // Verifica inventário cheio
            boolean adicionado = player.getInventory().addItem(item);
            if (!adicionado) {
                nome.setTextFill(Color.web("#FF9944")); // Flash laranja
                desc.setText("Inventário cheio!");
                PauseTransition ptInvCheio = new PauseTransition(Duration.millis(600));
                ptInvCheio.setOnFinished(ev -> {
                    nome.setTextFill(Color.web("#E8DFC0"));
                    desc.setText(atributo);
                });
                ptInvCheio.play();
                return;
            }

            // Desconta as moedas e atualiza a interface
            player.removeCoin(preco);
            lblOuro.setText("Ouro:  " + player.getCoin() + "  \uD83E\uDE99");
            if (hudManager != null) hudManager.atualizar(player);

            // Feedback visual no botão: "COMPRADO!"
            btnComprar.setText("COMPRADO!");
            btnComprar.setDisable(true); // Evita spam de duplo clique enquanto a animação roda
            btnComprar.setStyle(
                "-fx-background-color: rgba(0,180,80,0.25);" +
                "-fx-border-color: #00CC66;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 5;" +
                "-fx-background-radius: 5;" +
                "-fx-text-fill: #00CC66;" +
                "-fx-padding: 6 14;"
            );

            // Restaura o botão após 1.5s
            PauseTransition ptComprado = new PauseTransition(Duration.millis(1500));
            ptComprado.setOnFinished(ev -> {
                btnComprar.setText("COMPRAR");
                if (player.getCoin() < preco) {
                    btnComprar.setDisable(true);
                    btnComprar.setStyle(estDisable);
                } else {
                    btnComprar.setDisable(false);
                    btnComprar.setStyle(estNormal);
                }
            });
            ptComprado.play();
        });

        linha.getChildren().addAll(icone, infoBox, spacer, lblPreco, btnComprar);
        painel.getChildren().add(linha);
    }

    // =========================================================================
    // BOTÃO FECHAR
    // =========================================================================

    private static Button criarBotaoFechar() {
        Button btn = new Button("   FECHAR");
        btn.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 20));
        btn.setPrefWidth(240);
        btn.setPrefHeight(48);
        btn.setAlignment(Pos.CENTER_LEFT);

        final String estNormal =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #D8D0C0;" +
            "-fx-padding: 0 0 0 20;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: transparent;" +
            "-fx-font-size: 20px;";
        final String estHover =
            "-fx-background-color: rgba(0,210,210,0.15);" +
            "-fx-text-fill: white;" +
            "-fx-padding: 0 0 0 20;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0 0 0 3;" +
            "-fx-border-color: transparent;" +
            "-fx-font-size: 20px;";

        btn.setStyle(estNormal);
        btn.setOnMouseEntered(e -> { btn.setStyle(estHover);  btn.setText("›  FECHAR"); });
        btn.setOnMouseExited(e ->  { btn.setStyle(estNormal); btn.setText("   FECHAR"); });
        return btn;
    }

    // =========================================================================
    // FECHAR COM FADE
    // =========================================================================

    private static void fechar(StackPane mainLayout, Runnable onClose) {
        if (mainLayout.getChildren().isEmpty()) {
            onClose.run();
            return;
        }

        Node overlay = mainLayout.getChildren().get(mainLayout.getChildren().size() - 1);

        FadeTransition ft = new FadeTransition(Duration.millis(200), overlay);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            mainLayout.getChildren().remove(overlay);
            onClose.run();
        });
        ft.play();
    }
}