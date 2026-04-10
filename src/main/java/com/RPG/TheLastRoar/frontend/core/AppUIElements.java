package com.RPG.TheLastRoar.frontend.core;

import com.RPG.TheLastRoar.App;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * AppUIElements — Gerencia elementos da UI (botões, toasts, menus).
 *
 * RESPONSABILIDADE:
 *   - Criar e gerenciar botão de inventário
 *   - Exibir notificações toast (mensagens temporárias)
 *   - Estilizar elementos da interface
 */
public class AppUIElements {

    private App app;

    public AppUIElements(App app) {
        this.app = app;
    }

    /**
     * Cria o botão "🎒 Inventário (I)" no canto inferior direito.
     * setFocusTraversable(false) é CRUCIAL — impede que o botão roube
     * o foco das teclas de movimento quando o mouse passa sobre ele.
     */
    public void buildInventoryButton() {
        app.btnInventory = new Button("🎒 Inventário (I)");
        app.btnInventory.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        app.btnInventory.setFocusTraversable(false);

        final String styleNormal =
            "-fx-background-color: rgba(20, 20, 20, 0.7);" +
            "-fx-text-fill: #E8DFC0;" +
            "-fx-border-color: #B8960C; -fx-border-width: 2;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;" +
            "-fx-padding: 10 20; -fx-cursor: hand;";

        final String styleHover =
            "-fx-background-color: rgba(60, 50, 20, 0.9);" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-border-color: #FFD700; -fx-border-width: 2;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;" +
            "-fx-padding: 10 20; -fx-cursor: hand;";

        app.btnInventory.setStyle(styleNormal);
        app.btnInventory.setOnMouseEntered(e -> app.btnInventory.setStyle(styleHover));
        app.btnInventory.setOnMouseExited(e  -> app.btnInventory.setStyle(styleNormal));
        app.btnInventory.setOnAction(e -> {
            javafx.application.Platform.runLater(() -> app.mainLayout.requestFocus());
            app.openInventory();
        });

        StackPane.setAlignment(app.btnInventory, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(app.btnInventory, new Insets(0, 30, 30, 0));
    }

    /**
     * Exibe uma mensagem temporária no centro da tela que some após ~1.4s.
     * Usado para feedback de ações rápidas (usar poção, etc.).
     *
     * @param message Texto a exibir
     * @param color   Cor do texto
     */
    public void showToast(String message, Color color) {
        Label toast = new Label(message);
        toast.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        toast.setTextFill(color);
        toast.setStyle(
            "-fx-background-color: rgba(0,0,0,0.65);" +
            "-fx-padding: 12 28;" +
            "-fx-background-radius: 12;"
        );
        toast.setMouseTransparent(true);
        StackPane.setAlignment(toast, Pos.CENTER);
        app.mainLayout.getChildren().add(toast);
        toast.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toast);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(e -> {
            PauseTransition hold = new PauseTransition(Duration.millis(1000));
            hold.setOnFinished(ev -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(eff -> app.mainLayout.getChildren().remove(toast));
                fadeOut.play();
            });
            hold.play();
        });
        fadeIn.play();
    }
}




