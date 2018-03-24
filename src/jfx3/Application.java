package jfx3;

public class Application {
//
//    private BorderPane first;
//    private BorderPane second;
//    public ChoosePlayer initScreen;
//    public TwoPlayerScene boardScene;
//
//    public static void main(String... args) {
//        launch(args);
//    }
//
//    private PaneManager t;
//
//    @Override
//    public void start(Stage primaryStage) throws IOException {
//        loadStage(primaryStage);
//        if(DEBUG > 0)
//        new Application().loadStage(new Stage());
//    }
//
//    private void loadStage(Stage stage) throws IOException {
//        FXMLLoader fxmlLoader;
//        fxmlLoader = new FXMLLoader();
//        fxmlLoader.setLocation(getClass().getResource("../ChoosePlayer.fxml"));
//        first = fxmlLoader.load();
//        initScreen = fxmlLoader.getController();
//
//        fxmlLoader = new FXMLLoader();
//        fxmlLoader.setLocation(getClass().getResource("../TwoPlayerScene.fxml"));
//        second = fxmlLoader.load();
//        boardScene = fxmlLoader.getController();
//
//        // Create a container
//        AnchorPane root = new AnchorPane();
//        root.getChildren().addAll(first, second);
//        //rootPane.setBackground(new Background(new BackgroundFill(Color.valueOf("red"), null, null)));
//
//        Scene scene = new Scene(root);
//        stage.setScene(scene);
//
//        // Hide the next element by setting the transition
//        t = new PaneManager();
//        root.heightProperty().addListener(t);
//
//        // Show the stage to calculate the initial offset
//        stage.setTitle("Choose a player");
//        stage.setOnCloseRequest(e -> {
//            if(t.frac >= 1){
//                t.play();
//                e.consume();
//                return;
//            }
//            initScreen.closeImpl();
//            boardScene.closeImpl();
//            //Platform.exit();
//        });
//        stage.show();
//
//        // Enlarge the root of the loaded element to the scene size
//        first.prefHeightProperty().bind(root.heightProperty());
//        first.prefWidthProperty().bind(root.widthProperty());
//        second.prefHeightProperty().bind(root.heightProperty());
//        second.prefWidthProperty().bind(root.widthProperty());
//
//        initScreen.activeImpl();
//        initScreen.activatedImpl();
//        if(DEBUG == -1) {
//            // DEBUG: click start automatically
//            initScreen.opponentTypeComputer.setSelected(true);
//            initScreen.clickedStart(null);
//            //boardScene.animationSpeedChoiceBox.getSelectionModel().selectLast();
//        }
//
//        //first.prefHeightProperty().unbind();
//        //second.prefHeightProperty().unbind();
//        //root.heightProperty().removeListener(t);
//        if (DEBUG == 1){
//            initScreen.opponentTypeNetwork.setSelected(true);
//            initScreen.clickedStart(null);
//            DEBUG++;
//        } else if (DEBUG == 2){
//            initScreen.opponentTypeHost.setSelected(true);
//            initScreen.clickedStart(null);
//            DEBUG++;
//        }
//    }
//
//    private static int DEBUG = -1; // 0 to turn off

}
