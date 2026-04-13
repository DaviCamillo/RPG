package com.RPG.TheLastRoar.frontend.screens;
import java.util.List;
import java.util.stream.Collectors;

import com.RPG.TheLastRoar.backend.models.Armor;
import com.RPG.TheLastRoar.backend.models.Character;
import com.RPG.TheLastRoar.backend.models.Item;
import com.RPG.TheLastRoar.backend.models.Potion;
import com.RPG.TheLastRoar.backend.models.Sword;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
        VBox painelInventario = construirPainelModerno(player, () ->
            fechar(mainLayout, onClose)
        );

        StackPane overlay = new StackPane(painelInventario);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        overlay.setAlignment(Pos.CENTER_LEFT);

        overlay.setOpacity(0);
        mainLayout.getChildren().add(overlay);

        FadeTransition ft = new FadeTransition(Duration.millis(300), overlay);
        ft.setToValue(1.0);
        ft.play();
    }

    // =========================================================================
    // CONSTRUÇÃO DO PAINEL MODERNO COM FILTROS E BUSCA
    // =========================================================================

    private static VBox construirPainelModerno(Character player, Runnable onClose) {
        VBox painel = new VBox(16);
        painel.setPadding(new Insets(40, 60, 40, 140));
        painel.setStyle(
            "-fx-background-color: rgba(15, 12, 30, 0.95);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: #C8A000;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 16;"
        );
        painel.setMaxWidth(920);

        // ── HEADER ─────────────────────────────────────────────────────────
        VBox header = criarHeader();
        painel.getChildren().add(header);

        // ── FILTROS ────────────────────────────────────────────────────────
        HBox filtros = criarFiltros();
        painel.getChildren().add(filtros);

        // ── BUSCA ──────────────────────────────────────────────────────────
        TextField searchBar = criarBarraBusca();
        painel.getChildren().add(searchBar);

        // ── EQUIPADOS ──────────────────────────────────────────────────────
        VBox equipados = criarSecaoEquipados(player);
        painel.getChildren().add(equipados);

        // ── INVENTÁRIO (SCROLLABLE) ────────────────────────────────────────
        VBox inventarioArea = new VBox(12);
        inventarioArea.setStyle("-fx-fill-height: true;");
        
        ScrollPane scrollItens = new ScrollPane();
        scrollItens.setContent(inventarioArea);
        scrollItens.setFitToWidth(true);
        scrollItens.setStyle(
            "-fx-control-inner-background: rgba(10, 8, 20, 0.7);" +
            "-fx-border-color: #C8A000;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-padding: 12;"
        );
        scrollItens.setPrefHeight(300);
        VBox.setVgrow(scrollItens, Priority.ALWAYS);
        painel.getChildren().add(scrollItens);

        // ── CONTROLE DE FILTROS E BUSCA ────────────────────────────────────
        final String[] filtroAtivo = {"TODOS"};

        Runnable atualizarItens = () -> {
            inventarioArea.getChildren().clear();
            List<Item> itens = filtrarItens(player, filtroAtivo[0], searchBar.getText());
            if (itens.isEmpty()) {
                Label vazio = new Label("Nenhum item encontrado");
                vazio.setStyle("-fx-text-fill: #888070; -fx-font-size: 12;");
                inventarioArea.getChildren().add(vazio);
            } else {
                GridPane grade = construirGradeItensModerna(player, itens);
                inventarioArea.getChildren().add(grade);
            }
        };

        // EVENT: Filtros
        for (javafx.scene.Node node : filtros.getChildren()) {
            if (node instanceof Button btn) {
                btn.setOnAction(e -> {
                    filtroAtivo[0] = btn.getText();
                    atualizarItens.run();
                });
            }
        }

        // EVENT: Busca
        searchBar.textProperty().addListener((obs, old, newVal) -> atualizarItens.run());

        // Carga inicial
        atualizarItens.run();

        // ── BOTÃO FECHAR ───────────────────────────────────────────────────
        Button btnFechar = criarBotaoFechar();
        btnFechar.setOnAction(e -> onClose.run());
        painel.getChildren().add(btnFechar);

        return painel;
    }

    // =========================================================================
    // COMPONENTES MODERNOS
    // =========================================================================

    private static VBox criarHeader() {
        VBox header = new VBox(8);
        
        Text titulo = new Text("INVENTÁRIO");
        titulo.setFont(Font.font(FONT_PIXEL, FontWeight.BOLD, 28));
        titulo.setFill(Color.web("#F0E6C0"));
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#C8A000"));
        sombra.setRadius(12);
        titulo.setEffect(sombra);
        
        Line sep = new Line(0, 0, 280, 0);
        sep.setStroke(Color.web("#C8A000"));
        sep.setStrokeWidth(1.5);
        
        header.getChildren().addAll(titulo, sep);
        return header;
    }

    private static HBox criarFiltros() {
        HBox filtros = new HBox(12);
        filtros.setAlignment(Pos.CENTER_LEFT);
        
        String[] categorias = {"TODOS", "EQUIPAMENTOS", "CONSUMÍVEIS"};
        for (String cat : categorias) {
            Button btn = criarBotaoFiltro(cat);
            filtros.getChildren().add(btn);
        }
        
        return filtros;
    }

    private static Button criarBotaoFiltro(String categoria) {
        Button btn = new Button(categoria);
        btn.setPrefSize(140, 36);
        btn.setFont(Font.font(FONT_PIXEL, FontWeight.BOLD, 11));
        btn.setStyle(
            "-fx-background-color: rgba(50, 40, 60, 0.8);" +
            "-fx-text-fill: #B0A080;" +
            "-fx-border-color: #664400;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e ->
            btn.setStyle(
                "-fx-background-color: rgba(100, 80, 120, 0.9);" +
                "-fx-text-fill: #F0E6C0;" +
                "-fx-border-color: #C8A000;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            )
        );
        btn.setOnMouseExited(e ->
            btn.setStyle(
                "-fx-background-color: rgba(50, 40, 60, 0.8);" +
                "-fx-text-fill: #B0A080;" +
                "-fx-border-color: #664400;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            )
        );
        
        return btn;
    }

    private static TextField criarBarraBusca() {
        TextField search = new TextField();
        search.setPromptText("🔍 Buscar item...");
        search.setPrefHeight(40);
        search.setStyle(
            "-fx-font-family: '" + FONT_PIXEL + "', monospace;" +
            "-fx-font-size: 11;" +
            "-fx-background-color: rgba(30, 25, 50, 0.9);" +
            "-fx-text-fill: #E8DFC0;" +
            "-fx-padding: 8 14;" +
            "-fx-border-color: #C8A000;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-control-inner-background: rgba(30, 25, 50, 0.9);"
        );
        
        return search;
    }

    private static Button criarBotaoFechar() {
        Button btn = new Button("FECHAR");
        btn.setPrefWidth(200);
        btn.setPrefHeight(40);
        btn.setStyle(
            "-fx-font-family: '" + FONT_PIXEL + "';" +
            "-fx-font-size: 12;" +
            "-fx-background-color: rgba(50, 40, 60, 0.8);" +
            "-fx-text-fill: #B0A080;" +
            "-fx-border-color: #664400;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e ->
            btn.setStyle(
                "-fx-font-family: '" + FONT_PIXEL + "';" +
                "-fx-font-size: 12;" +
                "-fx-background-color: rgba(100, 80, 120, 0.9);" +
                "-fx-text-fill: #F0E6C0;" +
                "-fx-border-color: #C8A000;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            )
        );
        btn.setOnMouseExited(e ->
            btn.setStyle(
                "-fx-font-family: '" + FONT_PIXEL + "';" +
                "-fx-font-size: 12;" +
                "-fx-background-color: rgba(50, 40, 60, 0.8);" +
                "-fx-text-fill: #B0A080;" +
                "-fx-border-color: #664400;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            )
        );

        return btn;
    }

    private static VBox criarSecaoEquipados(Character player) {
        VBox secao = new VBox(12);
        
        Text titulo = new Text("⚔ EQUIPADO");
        titulo.setFont(Font.font(FONT_PIXEL, FontWeight.BOLD, 13));
        titulo.setFill(Color.web("#888070"));
        
        HBox slots = new HBox(12);
        slots.setAlignment(Pos.CENTER_LEFT);
        
        HBox slotEspada = criarSlotEquipado(
            player.getSword() != null ? player.getSword().getName() : "Nenhuma",
            "#C8A000"
        );
        slots.getChildren().add(slotEspada);
        
        HBox slotArmadura = criarSlotEquipado(
            player.getEquippedArmor() != null ? player.getEquippedArmor().getName() : "Nenhuma",
            "#4A90D9"
        );
        slots.getChildren().add(slotArmadura);
        
        secao.getChildren().addAll(titulo, slots);
        return secao;
    }

    private static HBox criarSlotEquipado(String itemName, String color) {
        HBox slot = new HBox(8);
        slot.setAlignment(Pos.CENTER_LEFT);
        slot.setPadding(new Insets(10, 14, 10, 14));
        slot.setStyle(
            "-fx-background-color: rgba(25, 20, 40, 0.8);" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
        slot.setPrefWidth(220);
        
        Label nome = new Label(itemName);
        nome.setFont(Font.font(FONT_PIXEL, FontWeight.BOLD, 11));
        nome.setTextFill(Color.web(color));
        
        slot.getChildren().add(nome);
        return slot;
    }

    // =========================================================================
    // FILTROS E BUSCA
    // =========================================================================

    private static List<Item> filtrarItens(Character player, String categoria, String busca) {
        List<Item> itens = player.getInventory().getItems();
        
        // Filtra por categoria
        itens = itens.stream().filter(item -> {
            if (categoria.equals("TODOS")) return true;
            if (categoria.equals("EQUIPAMENTOS")) return item instanceof Sword || item instanceof Armor;
            if (categoria.equals("CONSUMÍVEIS")) return item instanceof Potion;
            return true;
        }).collect(Collectors.toList());
        
        // Filtra por busca
        if (busca != null && !busca.isEmpty()) {
            String buscaLower = busca.toLowerCase();
            itens = itens.stream()
                .filter(item -> item.getName().toLowerCase().contains(buscaLower))
                .collect(Collectors.toList());
        }
        
        return itens;
    }

    // =========================================================================
    // GRADE DE ITENS MODERNA
    // =========================================================================

    private static GridPane construirGradeItensModerna(Character player, List<Item> itens) {
        GridPane grade = new GridPane();
        grade.setHgap(12);
        grade.setVgap(12);
        
        int col = 0, row = 0;
        for (Item item : itens) {
            VBox cardItem = criarCardItemModerno(item, player);
            grade.add(cardItem, col, row);
            
            col++;
            if (col >= 3) { col = 0; row++; }
        }
        
        return grade;
    }

    private static VBox criarCardItemModerno(Item item, Character player) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12, 12, 12, 12));
        card.setStyle(
            "-fx-background-color: rgba(35, 30, 50, 0.9);" +
            "-fx-border-color: #664400;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
        card.setPrefWidth(200);
        card.setCursor(javafx.scene.Cursor.HAND);
        
        Label nome = new Label(item.getName());
        nome.setFont(Font.font(FONT_PIXEL, FontWeight.BOLD, 11));
        nome.setTextFill(Color.web("#E8DFC0"));
        
        String atributos = gerarAtributos(item);
        Label desc = new Label(atributos);
        desc.setFont(Font.font(FONT_PIXEL, 9));
        desc.setTextFill(Color.web("#B0A080"));
        desc.setWrapText(true);
        
        Label comparacao = gerarComparacao(item, player);
        if (comparacao != null) {
            comparacao.setFont(Font.font(FONT_PIXEL, 9));
            comparacao.setWrapText(true);
            card.getChildren().add(comparacao);
        }
        
        card.getChildren().addAll(nome, desc);
        
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: rgba(60, 50, 80, 0.95);" +
                "-fx-border-color: #C8A000;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
            );
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: rgba(35, 30, 50, 0.9);" +
                "-fx-border-color: #664400;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
            );
        });
        
        return card;
    }

    private static String gerarAtributos(Item item) {
        if (item instanceof Sword s) {
            return "Dano: " + s.getDamage();
        } else if (item instanceof Armor a) {
            return "Defesa: " + a.getResistance();
        } else if (item instanceof Potion p) {
            return "Cura: +" + p.getHealedLife() + " HP";
        }
        return "";
    }

    private static Label gerarComparacao(Item item, Character player) {
        Label label = null;
        
        if (item instanceof Sword sword) {
            Sword equipada = player.getSword();
            if (equipada != null) {
                int diff = sword.getDamage() - equipada.getDamage();
                String cor = diff > 0 ? "#44FF44" : (diff < 0 ? "#FF4444" : "#FFFFFF");
                String sinal = diff > 0 ? "+" : "";
                label = new Label(sinal + diff + " Dano");
                label.setTextFill(Color.web(cor));
            }
        } else if (item instanceof Armor armor) {
            Armor equipada = player.getEquippedArmor();
            if (equipada != null) {
                int diff = armor.getResistance() - equipada.getResistance();
                String cor = diff > 0 ? "#44FF44" : (diff < 0 ? "#FF4444" : "#FFFFFF");
                String sinal = diff > 0 ? "+" : "";
                label = new Label(sinal + diff + " Defesa");
                label.setTextFill(Color.web(cor));
            }
        }
        
        return label;
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



