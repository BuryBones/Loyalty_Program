public class Main {

    public static void main(String[] args) {
        UI ui = UI.getInstance();
        UiController uic = UiController.getInstance();
        uic.invokeLogin();
    }
}
