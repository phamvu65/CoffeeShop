package com.vuxnye.coffeeshop.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class ViewManager {
    private static ViewManager instance;
    private BorderPane mainBorderPane;

    private ViewManager() {}

    public static ViewManager getInstance() {
        if (instance == null) instance = new ViewManager();
        return instance;
    }

    public void setMainBorderPane(BorderPane pane) {
        this.mainBorderPane = pane;
    }

    public void switchView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            if (mainBorderPane != null) {
                mainBorderPane.setCenter(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}