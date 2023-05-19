package GUI;

import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.image.*;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Client;
import org.example.Main;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.FileInputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GUI extends Application {


    int nrOfFloors, nrOfElevators;
    double elevatorWidth, floorHeight, maxHeight = 150;
    private final double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
    private final double screenHeight = Screen.getPrimary().getVisualBounds().getHeight() + 71;
    private Image hallwayImage, elevatorImage, personImage;
    private final String currentDir = System.getProperty("user.dir");
    private List<Integer> oldFloors;
    private List<PathTransition> elevatorTransitions;
    private List<ImageView> elevatorViews;
    private List<Integer> floorHeights;
    private List<List<ImageView>> personViews;
    public static List<Integer> personElevator;
    public static List<Integer> getDestinationsFromMain;
    private Map<ImageView, Integer> personElevatorsMap;
    private Pane root;
    private int conversion;

    @Override
    public void start(Stage primaryStage) throws Exception {
        elevatorViews = new ArrayList<>(); // client 2 8 0 90   client 6 3 1 80  client 4 1 0 80  client 4 8 0 90
        elevatorTransitions = new ArrayList<>();
        floorHeights = new ArrayList<>();
        personViews = new ArrayList<>();
        getDestinationsFromMain = new ArrayList<>();
        personElevator = new ArrayList<>();
        personElevatorsMap = new HashMap<>();

        nrOfFloors = Main.numberOfFloors;
        nrOfElevators = Main.numberOfElevators;

        calculateFloorHeight(nrOfFloors, screenHeight);
        hallwayImage = new Image(new FileInputStream(currentDir + "/src/static/hallway.jpeg"));
        elevatorImage = new Image(new FileInputStream(currentDir + "/src/static/elevator.png"));
        personImage = new Image(new FileInputStream(currentDir + "/src/static/person.png"));

        root = renderBackground();

        Scene scene = new Scene(root, screenWidth, screenHeight);


        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();

        conversion = convertHeight();

        for(int i = 0; i<nrOfFloors; i++) floorHeights.add((int) ((screenHeight - floorHeight) - floorHeight * i + conversion * 1.05));

        oldFloors = new ArrayList<>(Main.floorsToGo);

        Thread thread = new Thread(() -> {
            // Simulate receiving information every 2 seconds
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(this::refresh);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private TextField createNumericField() {
        TextField textField = new TextField();

        // Set up a TextFormatter to allow only digits in the TextField
        TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
            if (change.getText().matches("\\d*")) {
                return change;
            }
            return null;
        });

        textField.setTextFormatter(textFormatter);

        return textField;
    }

    private void refresh(){

        //fetch and show clients
        int size;
        while((size = getDestinationsFromMain.size()) != 0){
            int destination =  getDestinationsFromMain.get(size-1);
            int personElev = personElevator.get(size-1);
            ImageView personView = new ImageView(personImage);
            personView.setFitHeight(floorHeight);
            personView.setFitWidth(100);
            personView.setX(personViews.get(destination).size() * 40 + elevatorWidth * nrOfElevators);
            personView.setY(floorHeights.get(destination) - conversion);

            personViews.get(destination).add(personView);
            personElevatorsMap.put(personView, personElev);
            personElevator.remove(size-1);
            getDestinationsFromMain.remove(size-1);
        }

        clearAndRenderPersons();


        // make animations for elevators
       for(int i = 0, n = oldFloors.size(); i<n; i++){
            int oldFloor = oldFloors.get(i), newFloor = Main.floorsToGo.get(i);
            if(newFloor != oldFloor && newFloor != -1) {
                System.out.println("schimb tranzitia");
                changeElevatorTransition(i, newFloor);
            }
        }

       oldFloors = new ArrayList<>(Main.floorsToGo);
   }

    private void clearAndRenderPersons() {
        root.getChildren().removeIf(node -> node instanceof ImageView imageView && imageView.getImage() == personImage);

        for(List<ImageView> image : personViews){
            for(ImageView view : image){
                root.getChildren().add(view);
            }
        }
    }


    private void  changeElevatorTransition(int elevatorId, int newFloor){
       ImageView elevator = elevatorViews.get(elevatorId);
       Point2D imageViewPosition = elevator.localToScene(elevator.getBoundsInLocal().getMinX(), elevator.getBoundsInLocal().getMinY());
       double goToY = floorHeights.get(newFloor);
       double currentY = imageViewPosition.getY() + conversion;
       final double[] axisY = {elevator.localToScene(elevator.getBoundsInLocal().getCenterX(), elevator.getBoundsInLocal().getCenterY()).getY()};
       final AtomicReference<Double>[] currentFloor = new AtomicReference[]{new AtomicReference<>(Math.ceil(((screenHeight - axisY[0]) / floorHeight - 1)))};
       System.out.println("Current floor: " + currentFloor[0] + "ceiled value: " + Math.ceil(currentFloor[0].get()));
       System.out.println("Old Floor: " + oldFloors.get(elevatorId));
       if(Math.ceil(currentFloor[0].get()) == oldFloors.get(elevatorId) || (oldFloors.get(elevatorId) == -1 && Math.ceil(currentFloor[0].get()) == currentFloor[0].get())) {
           System.out.println("AM INTRAT AICI...");
           updatePersons(currentFloor[0].get(), elevatorId);
           oldFloors.set(elevatorId, newFloor);
       }

       PathTransition oldPathTransition = elevatorTransitions.get(elevatorId);
        if(oldPathTransition != null) oldPathTransition.stop();

        Path path = new Path();
        path.getElements().add(new MoveTo(elevatorViews.get(elevatorId).getX() + 53, currentY));
        path.getElements().add(new LineTo(elevatorViews.get(elevatorId).getX() + 53, goToY));
        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(Math.abs(currentY - goToY) * (Main.TRAVERSE_FLOOR / (145.0 - nrOfFloors)))); //TODO: 1 px = ... miliseconds, => abs(goToY - currentY) * ... miliseconds
        pathTransition.setPath(path);
        pathTransition.setNode(elevator);
        pathTransition.setOnFinished(event -> {
            axisY[0] = elevator.localToScene(elevator.getBoundsInLocal().getCenterX(), elevator.getBoundsInLocal().getCenterY()).getY();
            currentFloor[0].set(Math.ceil(((screenHeight - axisY[0]) / floorHeight - 1)));
            updatePersons(currentFloor[0].get(), elevatorId);
        });
        clearAndRenderPersons();
       elevatorTransitions.set(elevatorId, pathTransition);
       pathTransition.play();
   }

    private void updatePersons(double currentFloor, int elevatorId){
        Iterator<ImageView> iterator = personViews.get((int) currentFloor).iterator();
        System.out.println("Elevator " + elevatorId + " at currentFloor: " + (int) currentFloor);
        while (iterator.hasNext()) {
            ImageView image = iterator.next();
            if (personElevatorsMap.get(image) == elevatorId) {
                System.out.println("VOI ELIMINA CLIENTUL!!!");
                personElevatorsMap.remove(image);
                iterator.remove();
            }
        }
    } // client 2 0 1 100  client 2 7 0 100  client 6 8 0 100  client 3 1 1 100

    private void calculateFloorHeight(int n, double screenHeight){
        if(screenHeight / n > 150) { // incap toate
            floorHeight = maxHeight;
        } else {
            floorHeight = screenHeight / n;
        }

        elevatorWidth = 0.07 * screenWidth;
        System.out.println("Elevator width: " + elevatorWidth);
    }

    private ImageView renderElevator(int elevatorId, Image elevatorImage){
        ImageView elevatorView = new ImageView(elevatorImage);
        elevatorView.setFitHeight(floorHeight);
        elevatorView.setFitWidth(elevatorWidth);
        elevatorView.setY((screenHeight - floorHeight) - floorHeight * 0);
        elevatorView.setX(elevatorWidth * elevatorId);
        return elevatorView;
    }

    private Pane renderBackground(){
        Pane root = new Pane();
        root.setBackground(Background.fill(Color.BISQUE));

        double currentHeight = screenHeight - floorHeight;

        for (int i = 0; i < nrOfFloors; i++) {
            personViews.add(new ArrayList<>());
            ImageView hallwayView = new ImageView(hallwayImage);
            hallwayView.setFitHeight(floorHeight);
            hallwayView.setFitWidth(screenWidth - nrOfElevators * elevatorWidth);
            hallwayView.setY(currentHeight);
            hallwayView.setX(nrOfElevators * elevatorWidth);

            root.getChildren().add(hallwayView);

            currentHeight -= floorHeight;
        }

        for (int i = 0; i < nrOfElevators; i++) {
            ImageView elevatorView = renderElevator(i, elevatorImage);
            root.getChildren().add(elevatorView);
            elevatorViews.add(elevatorView);
            elevatorTransitions.add(null);
        }

        TextField inputFrom = createNumericField(), inputTo = createNumericField(), elevatorId = createNumericField(), weight = createNumericField();
        Label labelFrom = new Label("from: "), labelTo = new Label("to: "), labelElevator = new Label("ID: "), labelWeight = new Label("weight: ");
        Button submitData = new Button("Submit data");
        submitData.setOnAction(event -> {
            int valueTo = Integer.parseInt(inputTo.getText());
            int valueFrom = Integer.parseInt(inputFrom.getText());
            int valueElevatorId = Integer.parseInt(elevatorId.getText());
            int valueWeight = Integer.parseInt(weight.getText());

            if(Main.validateInput(valueFrom, valueTo, valueElevatorId, valueWeight)){
                Main.access("write", valueElevatorId, new Client(valueFrom, valueTo, valueWeight, valueElevatorId));
            }

        });
        inputFrom.setOnAction(e -> submitData.fire());
        inputTo.setOnAction(e -> submitData.fire());
        elevatorId.setOnAction(e -> submitData.fire());
        weight.setOnAction(e -> submitData.fire());

        inputFrom.setPrefWidth(65);
        inputTo.setPrefWidth(65);
        elevatorId.setPrefWidth(65);
        weight.setPrefWidth(65);

        labelFrom.setStyle("-fx-padding: 4 0 0 0");
        labelTo.setStyle("-fx-padding: 4 0 0 0");
        labelElevator.setStyle("-fx-padding: 4 0 0 0");
        labelWeight.setStyle("-fx-padding: 4 0 0 0");

        HBox inputBox = new HBox();
        inputBox.setSpacing(10);
        inputBox.setPadding(new Insets(5));
        inputBox.getChildren().addAll(labelFrom, inputFrom, labelTo, inputTo, labelElevator, elevatorId, labelWeight, weight);

        VBox box = new VBox();
        box.setSpacing(10);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(inputBox, submitData);

        box.setLayoutX(screenWidth - 465);
        box.setStyle("-fx-background-color: #FFE4C4;");


        root.getChildren().add(box);

        return root;
    }

    private int convertHeight(){ // pentru 6 -> 4, 5 -> 2.5, 4 -> 1.5, 3 -> 1.25, 2 -> 1
        if(nrOfFloors <= 7) return (int) (68 + (8-nrOfFloors) * multiplier());
        else return (68 - (nrOfFloors-3) * 3);
    }

    private double multiplier(){
        return switch (nrOfFloors) {
            case 2 -> 1;
            case 3 -> 1.25;
            case 4 -> 1.5;
            case 5 -> 2.5;
            case 6 -> 4;
            default -> 0;
        };
    }
}

// client 2 8 0 40
// client 6 1 1 40
// client 3 9 2 100
// client 9 1 0 40
// client 2 8 0 40
