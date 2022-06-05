import javafx.application.Application;
import javafx.stage.Stage;

public class Driver extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        InputDialog inputDialog = new InputDialog(primaryStage);

        //Test.buildTestPane(100, 100);

    }
}
