package GUI;

import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.image.*;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class GUI extends Application {


    int n = 9, nrOfElevators = 3; // numarul de etaje influenteaza startul lifturilor
    double elevatorWidth;
    double floorHeight;
    double maxHeight = 150;
    private double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
    private double screenHeight = Screen.getPrimary().getVisualBounds().getHeight() + 71;
    private Image hallwayImage, elevatorImage;

    // cand n este mic (2-6) -> liftul incepe prea sus (trebuie tras in jos)
    // cand n este mare (8+) -> liftul incepe prea jos (trebuie tras in sus)

    // cand n este mic -> + mai mult
    // cand n este mai mare -> + mai putin

    @Override
    public void start(Stage primaryStage) throws Exception {
        calculateFloorHeight(n, screenHeight);
        hallwayImage = new Image(new FileInputStream("/Users/razvanchichirau/Desktop/Elevators/TestJavaFX/images/hallway.jpeg"));
        elevatorImage = new Image(new FileInputStream("/Users/razvanchichirau/Desktop/Elevators/TestJavaFX/images/elevator.png"));
        Pane root = renderBackground();

        List<ImageView> elevatorViews = new ArrayList<>();
        for (int i = 0; i < nrOfElevators; i++) {
            ImageView elevatorView = renderElevator(i, 0, elevatorImage);
            root.getChildren().add(elevatorView);
            elevatorViews.add(elevatorView);
        }
        Scene scene = new Scene(root, screenWidth, screenHeight);


        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();

        int conversion = convertHeight();
        System.out.println(conversion);

        final int[] floor = {0};

        Thread thread = new Thread(() -> {
            // Simulate receiving information every second
            while (true) {
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    Path path = new Path();
                    path.getElements().add(new MoveTo(elevatorViews.get(2).getX() + 53, (screenHeight - floorHeight) - floorHeight * (floor[0] - 1) + conversion));
                    path.getElements().add(new LineTo(elevatorViews.get(2).getX() + 53, (screenHeight - floorHeight) - floorHeight * floor[0] + conversion));
                    PathTransition pathTransition = new PathTransition();
                    pathTransition.setDuration(Duration.millis(4000));
                    pathTransition.setPath(path);
                    pathTransition.setNode(elevatorViews.get(2));
                    pathTransition.play();
                });
                floor[0]++;
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void calculateFloorHeight(int n, double screenHeight){
        if(screenHeight / n > 150) { // incap toate
            floorHeight = maxHeight;
        } else {
            floorHeight = screenHeight / n;
        }

        elevatorWidth = 0.07 * screenWidth;
    }

    private ImageView renderElevator(int elevatorId, int floor, Image elevatorImage){
        ImageView elevatorView = new ImageView(elevatorImage);
        elevatorView.setFitHeight(floorHeight);
        elevatorView.setFitWidth(elevatorWidth);
        elevatorView.setY((screenHeight - floorHeight) - floorHeight * floor);
        elevatorView.setX(elevatorWidth * elevatorId);
        return elevatorView;
    }

    private Pane renderBackground(){
        Pane root = new Pane();
        root.setBackground(Background.fill(Color.BISQUE));

        double currentHeight = screenHeight - floorHeight;
        double currentWidth = 0;

        for (int i = 0; i < n; i++) {
            ImageView hallwayView = new ImageView(hallwayImage);
            hallwayView.setFitHeight(floorHeight);
            hallwayView.setFitWidth(screenWidth - nrOfElevators * elevatorWidth);
            hallwayView.setY(currentHeight);
            hallwayView.setX(nrOfElevators * elevatorWidth);

            root.getChildren().add(hallwayView);

            currentHeight -= floorHeight;
        }
        return root;
    }

    private int convertHeight(){ // pentru 6 -> 4, 5 -> 2.5, 4 -> 1.5, 3 -> 1.25, 2 -> 1
        if(n <= 7) return (int) (68 + (8-n) * multiplier());
        else return (68 - (n-3) * 3);
    }

    private double multiplier(){
        switch (n){
            case 2: return 1;
            case 3: return 1.25;
            case 4: return 1.5;
            case 5: return 2.5;
            case 6: return 4;
            default: return 0;
        }
    }
}
