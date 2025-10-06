package com.example.collabboard.config;

public enum FxmlView {
    LOGIN {
        @Override
        public String getTitle() {
            return "CollabBoard - Login";
        }

        @Override
        public String getFxmlFile() {
            return "/fxml/LoginView.fxml";
        }
    },
    SIGNUP {
        @Override
        public String getTitle() { return "CollabBoard - Sign Up"; }

        @Override
        public String getFxmlFile() { return "/fxml/SignupView.fxml"; }
    },
    DASHBOARD {
        @Override
        public String getTitle() { return "CollabBoard - Dashboard"; }

        @Override
        public String getFxmlFile() { return "/fxml/DashboardView.fxml"; }
    },
    // ADD THIS ENUM CONSTANT
    FORGOT_PASSWORD {
        @Override
        public String getTitle() { return "CollabBoard - Forgot Password"; }

        @Override
        public String getFxmlFile() { return "/fxml/ForgotPasswordView.fxml"; }
    },
    // ADD THIS ENUM CONSTANT
    RESET_PASSWORD {
        @Override
        public String getTitle() { return "CollabBoard - Reset Password"; }

        @Override
        public String getFxmlFile() { return "/fxml/ResetPasswordView.fxml"; }
    },
    MAIN {
        @Override
        public String getTitle() { return "CollabBoard - Main Application"; }

        @Override
        public String getFxmlFile() { return "/fxml/MainView.fxml"; }
    };

    public abstract String getTitle();
    public abstract String getFxmlFile();
}

