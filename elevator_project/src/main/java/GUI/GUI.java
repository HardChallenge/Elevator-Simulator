package GUI;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.TranslateTransition;
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
import org.example.Main;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUI extends Application {


    int nrOfFloors, nrOfElevators; // numarul de etaje influenteaza startul lifturilor
    double elevatorWidth;
    double floorHeight;
    double maxHeight = 150;
    private double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
    private double screenHeight = Screen.getPrimary().getVisualBounds().getHeight() + 71;
    private Image hallwayImage, elevatorImage;
    private String currentDir = System.getProperty("user.dir");
    private List<Integer> floorsToGo;
    private List<PathTransition> elevatorTransitions;
    private List<ImageView> elevatorViews;
    private int conversion;

    @Override
    public void start(Stage primaryStage) throws Exception {
        elevatorViews = new ArrayList<>();
        elevatorTransitions = new ArrayList<>();
        nrOfFloors = Main.numberOfFloors;
        nrOfElevators = Main.numberOfElevators;

        calculateFloorHeight(nrOfFloors, screenHeight);
        hallwayImage = new Image(new FileInputStream(currentDir + "/src/static/hallway.jpeg"));
        elevatorImage = new Image(new FileInputStream(currentDir + "/src/static/elevator.png"));
        Pane root = renderBackground();

        for (int i = 0; i < nrOfElevators; i++) {
            ImageView elevatorView = renderElevator(i, 0, elevatorImage);
            root.getChildren().add(elevatorView);
            elevatorViews.add(elevatorView);
            elevatorTransitions.add(null);
        }
        Scene scene = new Scene(root, screenWidth, screenHeight);


        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();

        conversion = convertHeight();

        floorsToGo = Main.floorsToGo;

        Thread thread = new Thread(() -> {
            // Simulate receiving information every second
            while (true) {
                try {
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(this::refresh);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

   private void refresh(){
        List<Integer> newFloors = Main.floorsToGo;
       for(int i = 0, n = newFloors.size(); i<n; i++){
            int newFloor = newFloors.get(i);
            if(newFloor != -1) changeElevatorTransition(i, newFloor);
        }

        floorsToGo = newFloors;
   }


   private void changeElevatorTransition(int elevatorId, int newFloor){
       ImageView elevator = elevatorViews.get(elevatorId);
       double currentY = elevator.getTranslateY();
       PathTransition oldPathTransition = elevatorTransitions.get(elevatorId);

       if(oldPathTransition != null) oldPathTransition.stop();

       Path path = new Path();
       path.getElements().add(new MoveTo(elevatorViews.get(elevatorId).getX() + 53, elevatorViews.get(elevatorId).getTranslateY() + conversion));
       path.getElements().add(new LineTo(elevatorViews.get(elevatorId).getX() + 53, (screenHeight - floorHeight) - floorHeight * newFloor + conversion));
       PathTransition pathTransition = new PathTransition();
       pathTransition.setDuration(Duration.millis(6000)); //TODO: change the duration dynamic
       pathTransition.setPath(path);
       pathTransition.setNode(elevatorViews.get(elevatorId));

       elevatorTransitions.set(elevatorId, pathTransition);
       pathTransition.play();
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

        for (int i = 0; i < nrOfFloors; i++) {
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
        if(nrOfFloors <= 7) return (int) (68 + (8-nrOfFloors) * multiplier());
        else return (68 - (nrOfFloors-3) * 3);
    }

    private double multiplier(){
        switch (nrOfFloors){
            case 2: return 1;
            case 3: return 1.25;
            case 4: return 1.5;
            case 5: return 2.5;
            case 6: return 4;
            default: return 0;
        }
    }
}
