package com.RPG.TheLastRoar;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;

/**
 * ============================================================
 * InventoryScreen.java — Tela de inventário do jogador
 * ============================================================
 *
 * RESPONSABILIDADE:
 * Exibe os itens do inventário em uma grade visual,
 * permitindo equipar e desequipar espadas e armaduras.
 *
 * FLUXO DE USO:
 * 1. Jogador pressiona tecla I (configurado no App)
 * 2. App para os timers e chama InventoryScreen.open(...)
 * 3. O jogador interage com os itens
 * 4. Ao fechar, App retoma os timers via callback onClose
 *
 * CORREÇÃO BUG 3 — HIGHLIGHT DO INVENTÁRIO NÃO RESETAVA:
 *   Causa: criarCardItem() nunca chamava card.setUserData(item).
 *   Os métodos resetarDestaqueEspadasNaGrade() e
 *   resetarDestaqueArmadurasNaGrade() fazem card.getUserData() para
 *   identificar qual item pertence a cada card — sem o setUserData(),
 *   getUserData() sempre retornava null, o reset nunca ocorria, e
 *   múltiplos cards ficavam com a borda ciano de "equipado".
 *   Correção: adicionado card.setUserData(item) em criarCardItem().
 *
 * ESTRUTURA DO LAYOUT:
 *  ┌──────────────────────────────────────┐
 *  │  INVENTÁRIO ──────────────────────── │  ← título + linha dourada
 *  │  EQUIPADO                            │
 *  │  ⚔  Espada  |  Espada de Madeira     │  ← slot espada atual
 *  │  🛡 Armadura |  (nenhuma)            │  ← slot armadura atual
 *  │  ITENS NO INVENTÁRIO                 │
 *  │  [Adaga] [Espada Longa] [Armadura]   │  ← grade 4 colunas
 *  │                       [FECHAR]       │
 *  └──────────────────────────────────────┘
 */
public class InventoryScreen {

    // =========================================================================
    // MÉTODO PRINCIPAL: Abre a tela de inventário como overlay
    // =========================================================================

    /**
     * Abre o inventário sobre o jogo com animação de fade.
     *
     * @param mainLayout StackPane principal do jogo
     * @param player     Personagem (fonte dos itens e equipamentos)
     * @param onClose    Callback chamado ao fechar (App retoma os timers aqui)
     */
    public static void open(StackPane mainLayout, Character player, Runnable onClose) {
        VBox painelInventario = construirPainel(player, () ->
            fechar(mainLayout, onClose)
        );

        // Overlay escuro semitransparente — escurece o jogo ao fundo
        StackPane overlay = new StackPane(painelInventario);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.80);");
        overlay.setAlignment(Pos.CENTER_LEFT); // Painel alinhado à esquerda

        // Animação de entrada
        overlay.setOpacity(0);
        mainLayout.getChildren().add(overlay);

        FadeTransition ft = new FadeTransition(Duration.millis(250), overlay);
        ft.setToValue(1.0);
        ft.play();
    }

    // =========================================================================
    // CONSTRUÇÃO DO PAINEL PRINCIPAL
    // =========================================================================

    private static VBox construirPainel(Character player, Runnable onClose) {
        VBox painel = new VBox(0);
        painel.setAlignment(Pos.CENTER_LEFT);
        painel.setPadding(new Insets(0, 0, 0, 120)); // Margem estilo Ruined King

        // ── Título ────────────────────────────────────────────────────────
        Text titulo = new Text("INVENTÁRIO");
        titulo.setFont(Font.font("Palatino Linotype", FontWeight.BOLD, 48));
        titulo.setFill(Color.web("#F0E6C0"));
        DropShadow sombraTitulo = new DropShadow();
        sombraTitulo.setColor(Color.web("#C8A000", 0.8));
        sombraTitulo.setRadius(24);
        sombraTitulo.setSpread(0.12);
        titulo.setEffect(sombraTitulo);
        VBox.setMargin(titulo, new Insets(0, 0, 6, 0));
        painel.getChildren().add(titulo);

        // ── Linha decorativa dourada ──────────────────────────────────────
        Line separador = new Line(0, 0, 280, 0);
        separador.setStroke(Color.web("#B8960C", 0.6));
        separador.setStrokeWidth(1.0);
        VBox.setMargin(separador, new Insets(4, 0, 24, 0));
        painel.getChildren().add(separador);

        // ── Seção: Equipamentos atuais ────────────────────────────────────
        Text labelEquipados = new Text("EQUIPADO");
        labelEquipados.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        labelEquipados.setFill(Color.web("#888070"));
        VBox.setMargin(labelEquipados, new Insets(0, 0, 8, 0));
        painel.getChildren().add(labelEquipados);

        // Slot da espada atual
        HBox slotEspada = criarSlotEquipado(
            "⚔  Espada",
            player.getSword() != null ? player.getSword().getName() : "Nenhuma",
            "#C8A000"
        );
        VBox.setMargin(slotEspada, new Insets(0, 0, 6, 0));
        painel.getChildren().add(slotEspada);

        // Slot da armadura atual
        HBox slotArmadura = criarSlotEquipado(
            "🛡  Armadura",
            player.getEquippedArmor() != null ? player.getEquippedArmor().getName() : "Nenhuma",
            "#4A90D9"
        );
        VBox.setMargin(slotArmadura, new Insets(0, 0, 24, 0));
        painel.getChildren().add(slotArmadura);

        // ── Seção: Itens no inventário ────────────────────────────────────
        Text labelItens = new Text("ITENS NO INVENTÁRIO");
        labelItens.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        labelItens.setFill(Color.web("#888070"));
        VBox.setMargin(labelItens, new Insets(0, 0, 10, 0));
        painel.getChildren().add(labelItens);

        // Grade de 4 colunas com os itens
        GridPane grade = construirGradeItens(player, slotEspada, slotArmadura);
        VBox.setMargin(grade, new Insets(0, 0, 28, 0));
        painel.getChildren().add(grade);

        // ── Botão fechar ──────────────────────────────────────────────────
        Button btnFechar = criarBotaoFechar();
        btnFechar.setOnAction(e -> onClose.run());
        painel.getChildren().add(btnFechar);

        return painel;
    }

    // =========================================================================
    // GRADE DE ITENS
    // =========================================================================

    /**
     * Constrói a grade visual com todos os itens do inventário.
     * Cada item é um card clicável que equipa ou desequipa o item.
     *
     * CORREÇÃO BUG 3: Agora chama card.setUserData(item) para que os
     * métodos resetarDestaque*NaGrade() consigam identificar o item
     * associado a cada card e resetar o estilo corretamente.
     */
    private static GridPane construirGradeItens(Character player,
                                                 HBox slotEspada, HBox slotArmadura) {
        GridPane grade = new GridPane();
        grade.setHgap(12);
        grade.setVgap(12);

        List<Item> itens = player.getInventory().getItems();

        if (itens.isEmpty()) {
            Label vazio = new Label("O inventário está vazio.");
            vazio.setFont(Font.font("Segoe UI", 16));
            vazio.setTextFill(Color.web("#888070"));
            grade.add(vazio, 0, 0);
            return grade;
        }

        for (int i = 0; i < itens.size(); i++) {
            final Item item = itens.get(i);
            boolean estaEquipado = verificarEquipado(player, item);

            // CORREÇÃO BUG 3: criarCardItem agora chama setUserData internamente
            VBox card = criarCardItem(item, estaEquipado);

            // ── Lógica de clique: equipar ou desequipar ───────────────────
            card.setOnMouseClicked(e -> {
                if (item instanceof Sword espada) {
                    if (player.getSword() == espada) {
                        // Já equipada → DESEQUIPA
                        player.setSword(null);
                        atualizarTextoSlot(slotEspada, "Nenhuma");
                        card.setStyle(estiloNormal(item));
                        removerBadgeEquipado(card);
                    } else {
                        // Não equipada → EQUIPA
                        player.setSword(espada);
                        atualizarTextoSlot(slotEspada, espada.getName());
                        // Remove destaque de outras espadas antes de aplicar neste card
                        resetarDestaqueEspadasNaGrade(grade, player);
                        card.setStyle(estiloEquipado(item));
                        adicionarBadgeEquipado(card);
                    }

                } else if (item instanceof Armor armadura) {
                    if (player.getEquippedArmor() == armadura) {
                        // Já equipada → DESEQUIPA
                        player.setEquippedArmor(null);
                        atualizarTextoSlot(slotArmadura, "Nenhuma");
                        card.setStyle(estiloNormal(item));
                        removerBadgeEquipado(card);
                    } else {
                        // Não equipada → EQUIPA
                        player.setEquippedArmor(armadura);
                        atualizarTextoSlot(slotArmadura, armadura.getName());
                        resetarDestaqueArmadurasNaGrade(grade, player);
                        card.setStyle(estiloEquipado(item));
                        adicionarBadgeEquipado(card);
                    }
                }
            });

            int coluna = i % 4;
            int linha  = i / 4;
            grade.add(card, coluna, linha);
        }

        return grade;
    }

    // =========================================================================
    // CARD VISUAL DO ITEM
    // =========================================================================

    /**
     * Cria o card visual de um item.
     *
     * CORREÇÃO BUG 3: Agora chama card.setUserData(item) logo após criar
     * o card. Sem isso, getUserData() retornava null nos métodos de reset
     * e o destaque "equipado" nunca era removido de cards antigos.
     *
     * @param item       Item a representar
     * @param equipado   Se true, exibe borda ciano e badge "EQUIPADO"
     */
    private static VBox criarCardItem(Item item, boolean equipado) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setPrefSize(130, 90);
        card.setMaxSize(130, 90);
        card.setStyle(equipado ? estiloEquipado(item) : estiloNormal(item));
        card.setCursor(javafx.scene.Cursor.HAND);

        // CORREÇÃO BUG 3: associa o item ao card para que os métodos
        // resetarDestaque*NaGrade() consigam identificar o item correto.
        card.setUserData(item);

        // ── Ícone do tipo ──────────────────────────────────────────────────
        Label icone = new Label(iconeItem(item));
        icone.setFont(Font.font("Segoe UI Emoji", 22));

        // ── Nome ───────────────────────────────────────────────────────────
        Label nome = new Label(item.getName());
        nome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        nome.setTextFill(Color.web("#E8DFC0"));
        nome.setWrapText(true);
        nome.setMaxWidth(110);
        nome.setAlignment(Pos.CENTER);

        // ── Atributo principal ─────────────────────────────────────────────
        Label lblAtributo = new Label(atributoItem(item));
        lblAtributo.setFont(Font.font("Segoe UI", 11));
        lblAtributo.setTextFill(Color.web("#888070"));

        card.getChildren().addAll(icone, nome, lblAtributo);

        // Badge "EQUIPADO" se aplicável
        if (equipado) adicionarBadgeEquipado(card);

        // ── Hover: leve escurecimento (não interfere com o estilo equipado) ─
        card.setOnMouseEntered(e -> card.setOpacity(0.85));
        card.setOnMouseExited(e  -> card.setOpacity(1.0));

        return card;
    }

    // ── Gerenciamento do badge "EQUIPADO" ────────────────────────────────

    /**
     * Adiciona o badge "EQUIPADO" ao card (se ainda não tiver).
     * O badge é identificado pelo ID "badge_equipado".
     */
    private static void adicionarBadgeEquipado(VBox card) {
        // Remove badge existente antes de adicionar (evita duplicatas)
        card.getChildren().removeIf(n -> "badge_equipado".equals(n.getId()));

        Label badge = new Label("EQUIPADO");
        badge.setId("badge_equipado");
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        badge.setTextFill(Color.web("#00E5E5"));
        badge.setStyle(
            "-fx-background-color: rgba(0,200,200,0.15);" +
            "-fx-padding: 2 6;" +
            "-fx-background-radius: 4;"
        );
        card.getChildren().add(badge);
    }

    /** Remove o badge "EQUIPADO" do card, se existir. */
    private static void removerBadgeEquipado(VBox card) {
        card.getChildren().removeIf(n -> "badge_equipado".equals(n.getId()));
    }

    // =========================================================================
    // VERIFICAÇÃO DE EQUIPAMENTO
    // =========================================================================

    /**
     * Verifica se um item está atualmente equipado pelo jogador.
     * Suporta null em player (usado pelos métodos de reset).
     */
    private static boolean verificarEquipado(Character player, Item item) {
        if (player == null) return false;
        if (item instanceof Sword  && player.getSword()         == item) return true;
        if (item instanceof Armor  && player.getEquippedArmor() == item) return true;
        return false;
    }

    // =========================================================================
    // RESET DE DESTAQUE NA GRADE
    // =========================================================================

    /**
     * Percorre a grade e remove o borda ciano de todos os cards de espada
     * que NÃO são a espada atualmente equipada.
     * Chamado antes de equipar uma nova espada.
     *
     * CORREÇÃO BUG 3: Agora funciona porque criarCardItem() chama
     * card.setUserData(item) — getUserData() retorna o Item correto.
     */
    private static void resetarDestaqueEspadasNaGrade(GridPane grade, Character player) {
        grade.getChildren().forEach(node -> {
            if (node instanceof VBox card) {
                Object userData = card.getUserData();
                // Só processa cards de espada que não sejam a equipada atualmente
                if (userData instanceof Sword espada && espada != player.getSword()) {
                    card.setStyle(estiloNormal(espada));
                    removerBadgeEquipado(card);
                }
            }
        });
    }

    /** Mesmo que resetarDestaqueEspadasNaGrade, mas para armaduras. */
    private static void resetarDestaqueArmadurasNaGrade(GridPane grade, Character player) {
        grade.getChildren().forEach(node -> {
            if (node instanceof VBox card) {
                Object userData = card.getUserData();
                if (userData instanceof Armor armadura && armadura != player.getEquippedArmor()) {
                    card.setStyle(estiloNormal(armadura));
                    removerBadgeEquipado(card);
                }
            }
        });
    }

    // =========================================================================
    // SLOT DE EQUIPAMENTO (mostra o que está equipado)
    // =========================================================================

    /**
     * Cria uma linha visual mostrando o equipamento atual num slot.
     *
     * @param tipoSlot    Texto do tipo (ex: "⚔  Espada")
     * @param nomeItem    Nome do item equipado (ou "Nenhuma")
     * @param corDestaque Cor da borda esquerda do slot
     */
    private static HBox criarSlotEquipado(String tipoSlot, String nomeItem, String corDestaque) {
        HBox slot = new HBox(14);
        slot.setAlignment(Pos.CENTER_LEFT);
        slot.setStyle(
            "-fx-background-color: rgba(20,20,20,0.7);" +
            "-fx-border-color: " + corDestaque + " transparent transparent transparent;" +
            "-fx-border-width: 0 0 0 3;" +
            "-fx-padding: 10 16;" +
            "-fx-background-radius: 6;"
        );
        slot.setMaxWidth(380);

        Label lblTipo = new Label(tipoSlot);
        lblTipo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblTipo.setTextFill(Color.web("#888070"));
        lblTipo.setMinWidth(110);

        Label lblNome = new Label(nomeItem);
        lblNome.setId("nomeEquipado"); // ID usado por atualizarTextoSlot()
        lblNome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblNome.setTextFill(Color.web("#E8DFC0"));

        slot.getChildren().addAll(lblTipo, lblNome);
        return slot;
    }

    /**
     * Atualiza o texto do nome no slot de equipamento.
     * Busca o Label pelo ID "nomeEquipado".
     */
    private static void atualizarTextoSlot(HBox slot, String novoNome) {
        slot.getChildren().stream()
            .filter(n -> n instanceof Label lbl && "nomeEquipado".equals(lbl.getId()))
            .map(n -> (Label) n)
            .findFirst()
            .ifPresent(lbl -> lbl.setText(novoNome));
    }

    // =========================================================================
    // ESTILOS DOS CARDS
    // =========================================================================

    private static String estiloNormal(Item item) {
        return "-fx-background-color: rgba(20,20,20,0.75);" +
               "-fx-border-color: " + corBorda(item) + ";" +
               "-fx-border-width: 1.5;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;";
    }

    private static String estiloEquipado(Item item) {
        return "-fx-background-color: rgba(0,180,180,0.18);" +
               "-fx-border-color: #00E5E5;" +
               "-fx-border-width: 2;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;";
    }

    /** Cor da borda do card por tipo de item. */
    private static String corBorda(Item item) {
        if (item instanceof Sword)  return "#8B7028"; // Dourado para espadas
        if (item instanceof Armor)  return "#2A5A8B"; // Azul para armaduras
        if (item instanceof Potion) return "#2A7A3A"; // Verde para poções
        return "#443E30";
    }

    /** Emoji/ícone do tipo de item. */
    private static String iconeItem(Item item) {
        if (item instanceof Sword)  return "⚔";
        if (item instanceof Armor)  return "🛡";
        if (item instanceof Potion) return "🧪";
        return "📦";
    }

    /** Texto do atributo principal do item. */
    private static String atributoItem(Item item) {
        if (item instanceof Sword s)  return "Dano: " + s.getDamage() + " | " + s.getType();
        if (item instanceof Armor a)  return "Defesa: " + a.getResistance();
        if (item instanceof Potion p) return "Cura: " + p.getHealedLife() + " HP";
        return "Valor: " + item.getValue();
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
            "-fx-background-color: rgba(0, 210, 210, 0.15);" +
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

    /**
     * Fecha o inventário com fade out e chama onClose.
     * onClose é o callback do App que reseta inventarioAberto e retoma os timers.
     */
    private static void fechar(StackPane mainLayout, Runnable onClose) {
        if (mainLayout.getChildren().isEmpty()) {
            onClose.run();
            return;
        }

        // O overlay do inventário é sempre o último filho adicionado
        javafx.scene.Node overlay =
            mainLayout.getChildren().get(mainLayout.getChildren().size() - 1);

        FadeTransition ft = new FadeTransition(Duration.millis(200), overlay);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            mainLayout.getChildren().remove(overlay);
            onClose.run();
        });
        ft.play();
    }
}
