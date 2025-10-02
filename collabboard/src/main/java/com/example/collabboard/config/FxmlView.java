package com.example.collabboard.config;

public enum FxmlView {
    LOGIN {
        @Override
        public String getFxmlFile() {
            return "/fxml/LoginView.fxml";
        }
    },
    SIGNUP {
        @Override
        public String getFxmlFile() {
            return "/fxml/SignupView.fxml";
        }
    };
    // You can add DASHBOARD, BOARD_VIEW etc. later

    public abstract String getFxmlFile();
}
