public class UiController {
    // singleton
    private static UiController instance;
    static UiController getInstance() {
        if (instance == null) {
            instance = new UiController();
        }
        return instance;
    }
    private UI ui;
    private UiController() {
        ui = UI.getInstance();
    }

    void showError(String text, boolean exit) {
        ui.showError(text,exit);
    }
    void showWindow() {
        ui.showWindow();
    }
    void invokeLogin() {
        new LoginWindow();
    }
}
