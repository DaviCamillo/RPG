package com.RPG.TheLastRoar.frontend.screens;
import com.RPG.TheLastRoar.backend.models.Character;
import com.RPG.TheLastRoar.backend.models.Sword;
import com.RPG.TheLastRoar.backend.models.Armor;
import com.RPG.TheLastRoar.backend.models.Potion;
import com.RPG.TheLastRoar.backend.models.Item;

import java.util.List;

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

/**
 * ============================================================
 * InventoryScreen.java — Tela de inventário do jogador
 * ============================================================
 */
public class InventoryScreen {

    // Nome da família da sua fonte customizada
    private static final String FONT_PIXEL = "Press Start 2P";

    // =========================================================================
    // MÉTODO PRINCIPAL: Abre a tela de inventário como overlay
    // =========================================================================

    public static void open(StackPane mainLayout, Character player, Runnable onClose) {
        VBox painelInventario = construirPainel(player, () ->
            fechar(mainLayout, onClose)
        );

        StackPane overlay = new StackPane(painelInventario);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.80);");
        overlay.setAlignment(Pos.CENTER_LEFT);

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
        painel.setPadding(new Insets(0, 0, 0, 120));

        // ── Título ────────────────────────────────────────────────────────
        Text titulo = new Text("INVENTÁRIO");
        titulo.setFont(Font.font(FONT_PIXEL, FontWeight.NORMAL, 28)); // Tamanho reduzido para pixel font
        titulo.setFill(Color.web("#F0E6C0"));
        DropShadow sombraTitulo = new DropShadow();
        sombraTitulo.setColor(Color.web("#C8A000", 0.8));
        sombraTitulo.setRadius(24);
        sombraTitulo.setSpread(0.12);
        titulo.setEffect(sombraTitulo);
        VBox.setMargin(titulo, new Insets(0, 0, 12, 0));
        painel.getChildren().add(titulo);

        // ── Linha decorativa dourada ──────────────────────────────────────
        Line separador = new Line(0, 0, 280, 0);
        separador.setStroke(Color.web("#B8960C", 0.6));
        separador.setStrokeWidth(1.0);
        VBox.setMargin(separador, new Insets(4, 0, 24, 0));
        painel.getChildren().add(separador);

        // ── Seção: Equipamentos atuais ────────────────────────────────────
        Text labelEquipados = new Text("EQUIPADO");
        labelEquipados.setFont(Font.font(FONT_PIXEL, FontWeight.NORMAL, 12));
        labelEquipados.setFill(Color.web("#888070"));
        VBox.setMargin(labelEquipados, new Insets(0, 0, 12, 0));
        painel.getChildren().add(labelEquipados);

        HBox slotEspada = criarSlotEquipado(
            "⚔  Espada",
            player.getSword() != null ? player.getSword().getName() : "Nenhuma",
            "#C8A000"
        );
        VBox.setMargin(slotEspada, new Insets(0, 0, 8, 0));
        painel.getChildren().add(slotEspada);

        HBox slotArmadura = criarSlotEquipado(
            "🛡  Armadura",
            player.getEquippedArmor() != null ? player.getEquippedArmor().getName() : "Nenhuma",
            "#4A90D9"
        );
        VBox.setMargin(slotArmadura, new Insets(0, 0, 24, 0));
        painel.getChildren().add(slotArmadura);

        // ── Seção: Itens no inventário ────────────────────────────────────
        Text labelItens = new Text("ITENS NO INVENTÁRIO");
        labelItens.setFont(Font.font(FONT_PIXEL, FontWeight.NORMAL, 12));
        labelItens.setFill(Color.web("#888070"));
        VBox.setMargin(labelItens, new Insets(0, 0, 14, 0));
        painel.getChildren().add(labelItens);

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

    private static GridPane construirGradeItens(Character player,
                                                 HBox slotEspada, HBox slotArmadura) {
        GridPane grade = new GridPane();
        grade.setHgap(12);
        grade.setVgap(12);

        List<Item> itens = player.getInventory().getItems();

        if (itens.isEmpty()) {
            Label vazio = new Label("O inventário está vazio.");
            vazio.setFont(Font.font(FONT_PIXEL, 10));
            vazio.setTextFill(Color.web("#888070"));
            grade.add(vazio, 0, 0);
            return grade;
        }

        for (int i = 0; i < itens.size(); i++) {
            final Item item = itens.get(i);
            boolean estaEquipado = verificarEquipado(player, item);

            VBox card = criarCardItem(item, estaEquipado);

            card.setOnMouseClicked(e -> {
                if (item instanceof Sword espada) {
                    if (player.getSword() == espada) {
                        player.setSword(null);
                        atualizarTextoSlot(slotEspada, "Nenhuma");
                        card.setStyle(estiloNormal(item));
                        removerBadgeEquipado(card);
                    } else {
                        player.setSword(espada);
                        atualizarTextoSlot(slotEspada, espada.getName());
                        resetarDestaqueEspadasNaGrade(grade, player);
                        card.setStyle(estiloEquipado(item));
                        adicionarBadgeEquipado(card);
                    }

                } else if (item instanceof Armor armadura) {
                    if (player.getEquippedArmor() == armadura) {
                        player.setEquippedArmor(null);
                        atualizarTextoSlot(slotArmadura, "Nenhuma");
                        card.setStyle(estiloNormal(item));
                        removerBadgeEquipado(card);
                    } else {
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

    private static VBox criarCardItem(Item item, boolean equipado) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setPrefSize(140, 100); // Aumentado levemente para caber a pixel font
        card.setMaxSize(140, 100);
        card.setStyle(equipado ? estiloEquipado(item) : estiloNormal(item));
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setUserData(item);

        // ── Ícone do tipo (Mantido como Emoji para não quebrar) ────────────
        Label icone = new Label(iconeItem(item));
        icone.setFont(Font.font("Segoe UI Emoji", 24));

        // ── Nome ───────────────────────────────────────────────────────────
        Label nome = new Label(item.getName());
        nome.setFont(Font.font(FONT_PIXEL, FontWeight.NORMAL, 8));
        nome.setTextFill(Color.web("#E8DFC0"));
        nome.setWrapText(true);
        nome.setMaxWidth(120);
        nome.setAlignment(Pos.CENTER);

        // ── Atributo principal ─────────────────────────────────────────────
        Label lblAtributo = new Label(atributoItem(item));
        lblAtributo.setFont(Font.font(FONT_PIXEL, FontWeight.NORMAL, 7));
        lblAtributo.setTextFill(Color.web("#888070"));

        card.getChildren().addAll(icone, nome, lblAtributo);

        if (equipado) adicionarBadgeEquipado(card);

        card.setOnMouseEntered(e -> card.setOpacity(0.85));
        card.setOnMouseExited(e  -> card.setOpacity(1.0));

        return card;
    }

    private static void adicionarBadgeEquipado(VBox card) {
        card.getChildren().removeIf(n -> "badge_equipado".equals(n.getId()));

        Label badge = new Label("EQUIPADO");
        badge.setId("badge_equipado");
        badge.setFont(Font.font(FONT_PIXEL, FontWeight.NORMAL, 7));
        badge.setTextFill(Color.web("#00E5E5"));
        badge.setStyle(
            "-fx-background-color: rgba(0,200,200,0.15);" +
            "-fx-padding: 4 6;" +
            "-fx-background-radius: 4;"
        );
        card.getChildren().add(badge);
    }

    private static void removerBadgeEquipado(VBox card) {
        card.getChildren().removeIf(n -> "badge_equipado".equals(n.getId()));
    }

    // =========================================================================
    // VERIFICAÇÃO DE EQUIPAMENTO
    // =========================================================================

    private static boolean verificarEquipado(Character player, Item item) {
        if (player == null) return false;
        if (item instanceof Sword  && player.getSword()         == item) return true;
        if (item instanceof Armor  && player.getEquippedArmor() == item) return true;
        return false;
    }

    private static void resetarDestaqueEspadasNaGrade(GridPane grade, Character player) {
        grade.getChildren().forEach(node -> {
            if (node instanceof VBox card) {
                Object userData = card.getUserData();
                if (userData instanceof Sword espada && espada != player.getSword()) {
                    card.setStyle(estiloNormal(espada));
                    removerBadgeEquipado(card);
                }
            }
        });
    }

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
    // SLOT DE EQUIPAMENTO 
    // =========================================================================

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
        // O ícone do tipoSlot ("⚔  Espada") requer uma fonte que suporte emoji na primeira letra
        // Vou manter em Segoe UI aqui para não bugar o emoji e a formatação misturada.
        lblTipo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblTipo.setTextFill(Color.web("#888070"));
        lblTipo.setMinWidth(110);

        Label lblNome = new Label(nomeItem);
        lblNome.setId("nomeEquipado"); 
        lblNome.setFont(Font.font(FONT_PIXEL, FontWeight.NORMAL, 10)); // Fonte pixel
        lblNome.setTextFill(Color.web("#E8DFC0"));

        slot.getChildren().addAll(lblTipo, lblNome);
        return slot;
    }

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

    private static String corBorda(Item item) {
        if (item instanceof Sword)  return "#8B7028"; 
        if (item instanceof Armor)  return "#2A5A8B"; 
        if (item instanceof Potion) return "#2A7A3A"; 
        return "#443E30";
    }

    private static String iconeItem(Item item) {
        if (item instanceof Sword)  return "⚔";
        if (item instanceof Armor)  return "🛡";
        if (item instanceof Potion) return "🧪";
        return "📦";
    }

    private static String atributoItem(Item item) {
        if (item instanceof Sword s)  return "Dano: " + s.getDamage();
        if (item instanceof Armor a)  return "Defesa: " + a.getResistance();
        if (item instanceof Potion p) return "Cura: " + p.getHealedLife();
        return "Valor: " + item.getValue();
    }

    // =========================================================================
    // BOTÃO FECHAR
    // =========================================================================

    private static Button criarBotaoFechar() {
        Button btn = new Button("   FECHAR");
        btn.setPrefWidth(240);
        btn.setPrefHeight(48);
        btn.setAlignment(Pos.CENTER_LEFT);

        final String estNormal =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #D8D0C0;" +
            "-fx-padding: 0 0 0 20;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: transparent;" +
            "-fx-font-family: '" + FONT_PIXEL + "';" +
            "-fx-font-size: 14px;";
            
        final String estHover =
            "-fx-background-color: rgba(0, 210, 210, 0.15);" +
            "-fx-text-fill: white;" +
            "-fx-padding: 0 0 0 20;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0 0 0 3;" +
            "-fx-border-color: transparent;" +
            "-fx-font-family: '" + FONT_PIXEL + "';" +
            "-fx-font-size: 14px;";

        btn.setStyle(estNormal);
        btn.setOnMouseEntered(e -> { btn.setStyle(estHover);  btn.setText("> FECHAR"); });
        btn.setOnMouseExited(e ->  { btn.setStyle(estNormal); btn.setText("  FECHAR"); });
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

        javafx.scene.Node overlay = mainLayout.getChildren().get(mainLayout.getChildren().size() - 1);

        FadeTransition ft = new FadeTransition(Duration.millis(200), overlay);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            mainLayout.getChildren().remove(overlay);
            onClose.run();
        });
        ft.play();
    }
}



