import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class mineSweeper extends Application {

    private static final int TITLE_SIZE = 40;
    private static final int WIDTH = 403;
    private static final int HIGH = 301;
    private static final int Y_TITLES = (HIGH - 20) / TITLE_SIZE;
    private static final int X_TITLES = WIDTH / TITLE_SIZE;
    public static final Tile[][] grid = new Tile[X_TITLES][Y_TITLES];
    private short flagnum = 0;
    private Color themeColor = Color.LIGHTGREY;
    private Scene scene;
    private final Label howMuchFlags = new Label();
    private Double diffLvl = 0.05;
    private short bombCount = 0;
    private Color flagColor = Color.RED;

    private final Pane root = new Pane();

    private Parent createContent() {
        root.getChildren().clear();
        flagnum = 0;
        bombCount = 0;
        root.setPrefSize(WIDTH, HIGH);
        for (int y = 0; y < Y_TITLES; y++) {
            for (int x = 0; x < X_TITLES; x++) {
                boolean bomb = (Math.random() < diffLvl);
                if (bomb) bombCount++;
                Tile tile = new Tile(x, y, bomb);
                grid[x][y] = tile;
                root.getChildren().add(tile.sp);
            }
        }
        Label howMuchBombs = new Label();
        ImageView bombImg = new ImageView(new Image("/icon.png"));
        bombImg.setFitHeight(20);
        bombImg.setFitWidth(20);
        bombImg.setX(10);
        Rectangle flagRec = new Rectangle(12, 12);
        flagRec.setFill(flagColor);
        flagRec.setX(75);
        flagRec.setY(3);
        howMuchBombs.setText(String.valueOf(bombCount));
        howMuchBombs.setLayoutX(30);
        howMuchFlags.setLayoutX(60);
        howMuchFlags.setText(String.valueOf(flagnum));
        root.getChildren().addAll(howMuchBombs, bombImg, howMuchFlags, flagRec);
        for (int y = 0; y < Y_TITLES; y++) {
            for (int x = 0; x < X_TITLES; x++) {
                Tile tile = grid[x][y];

                if (tile.hasBomb) continue;
                int bombs = (int) Neighbors(tile).stream().filter(t -> t.hasBomb).count();
                if (bombs > 0) {
                    switch (bombs) {
                        case (1) -> tile.text.setFill(Color.BLUE);
                        case (2) -> tile.text.setFill(Color.GREEN);
                        case (3) -> tile.text.setFill(Color.RED);
                        case (4) -> tile.text.setFill(Color.DARKBLUE);
                        case (5) -> tile.text.setFill(Color.DARKRED);
                        case (6) -> tile.text.setFill(Color.DARKGREEN);
                        case (7) -> tile.text.setFill(Color.DARKVIOLET);
                        case (8) -> tile.text.setFill(Color.BLACK);
                    }
                    tile.text.setText(String.valueOf(bombs));
                }
            }
        }
        return root;
    }

    public static List<Tile> Neighbors(Tile tile) {
        List<Tile> neighbors = new ArrayList<>();
        int[] points = new int[]{
                -1, -1,
                -1, 0,
                -1, 1,
                0, -1,
                0, 1,
                1, -1,
                1, 0,
                1, 1
        };
        for (int i = 0; i < points.length; i++) {
            int dx = points[i];
            int dy = points[++i];

            int newX = tile.x + dx;
            int newY = tile.y + dy;

            if (newX >= 0 && newX < mineSweeper.X_TITLES && newY >= 0 && newY < mineSweeper.Y_TITLES) {
                neighbors.add(grid[newX][newY]);
            }
        }
        return neighbors;
    }

    public class Tile {
        private final int x;
        private final int y;
        private final boolean hasBomb;
        private final StackPane sp = new StackPane();
        Text text = new Text();
        private boolean isOpen = false;
        private final Rectangle border = new Rectangle(mineSweeper.TITLE_SIZE - 2, mineSweeper.TITLE_SIZE - 2);

        public Tile(int x, int y, boolean hasBomb) {
            this.x = x;
            this.y = y;
            this.hasBomb = hasBomb;
            text.setText(hasBomb ? " " : "");
            text.setVisible(false);
            text.setFont(new Font(20));
            border.setArcHeight(8);
            border.setArcWidth(8);
            border.setStroke(Color.GREY);
            border.setFill(themeColor);
            ImageView bombImage = new ImageView("/icon.png");
            bombImage.setFitWidth(TITLE_SIZE - 5);
            bombImage.setFitHeight(TITLE_SIZE - 5);
            if (hasBomb) {
                sp.getChildren().add(bombImage);
            }
            sp.getChildren().addAll(border, text);
            sp.setTranslateX(x * mineSweeper.TITLE_SIZE + 2);
            sp.setTranslateY(y * mineSweeper.TITLE_SIZE + 20);
            sp.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && border.getFill() != flagColor) {
                    open();
                }
                if (mouseEvent.getButton() == MouseButton.SECONDARY && border.getFill() == themeColor) {
                    border.setFill(flagColor);
                    flagnum++;
                    root.getChildren().remove(howMuchFlags);
                    howMuchFlags.setText(String.valueOf(flagnum));
                    root.getChildren().add(howMuchFlags);
                } else if (mouseEvent.getButton() == MouseButton.SECONDARY && border.getFill() == flagColor) {
                    border.setFill(themeColor);
                    flagnum--;
                    root.getChildren().remove(howMuchFlags);
                    howMuchFlags.setText(String.valueOf(flagnum));
                    root.getChildren().add(howMuchFlags);
                }
                if (flagnum == bombCount) {
                    boolean win = true;
                    for (int y1 = 0; y1 < Y_TITLES; y1++) {
                        for (int x1 = 0; x1 < X_TITLES; x1++) {
                            Tile tile = grid[x][y];

                            if (!tile.hasBomb && tile.border.getFill() == flagColor ||
                                    tile.hasBomb && tile.border.getFill() != flagColor)
                                win = false;
                        }
                    }
                    if (win) {
                        gameOverWindow("YOU WIN", "RESTART");
                        scene.setRoot(createContent());
                    }
                }
            });
        }

        public void     open() {
            if (isOpen) return;

            isOpen = true;
            text.setVisible(true);
            border.setFill(null);
            if (hasBomb) {
                for (int y = 0; y < Y_TITLES; y++) {
                    for (int x = 0; x < X_TITLES; x++) {
                        Tile tile = grid[x][y];

                        if (tile.hasBomb) tile.border.setFill(null);

                    }
                }

                gameOverWindow("GAME OVER", "Restart");
                scene.setRoot(createContent());
                return;
            }
            if (text.getText().isEmpty()) {
                Neighbors(this).forEach(Tile::open);
            }
        }
    }


    public void gameOverWindow(String textOfMenu, String textOfBut) {
        Stage window = new Stage();
        window.initModality(Modality.WINDOW_MODAL);
        Pane pane = new Pane();
        Text text = new Text(textOfMenu);
        text.setX(35);
        text.setY(50);
        text.setFont(new Font(25));
        text.setFill(Color.RED);
        Button restartButton = new Button(textOfBut);
        restartButton.setOnAction(e -> window.close());
        restartButton.setMinWidth(50);
        restartButton.setLayoutX(75);
        restartButton.setLayoutY(100);
        ObservableList<String> themeVariants = FXCollections.observableArrayList("Stanadrt Mode", "Dark Mode");
        ChoiceBox<String> theme = new ChoiceBox<>(themeVariants);
        theme.setLayoutX(85);
        theme.setLayoutY(200);
        Label chooseTheme = new Label("Color theme:");
        chooseTheme.setLayoutX(10);
        chooseTheme.setLayoutY(205);
        theme.setOnAction(actionEvent -> {
            if (theme.getValue().equals("Dark Mode")) {
                themeColor = Color.BLACK;
                root.setBackground(new Background(new BackgroundFill(Color.DARKGREY, null, null)));
            } else {
                themeColor = Color.LIGHTGREY;
                root.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
            }
        });
        ObservableList<String> diffVariants = FXCollections.observableArrayList("Easy", "Medium", "Hard", "Dead");
        ChoiceBox<String> diffLevel = new ChoiceBox<>(diffVariants);
        Label chooseLvl = new Label("Difficulty level:");
        chooseLvl.setLayoutX(10);
        chooseLvl.setLayoutY(155);
        diffLevel.setLayoutX(100);
        diffLevel.setLayoutY(150);
        diffLevel.setOnAction(actionEvent -> {
            switch (diffLevel.getValue()) {
                case ("Easy") -> diffLvl = 0.1;
                case ("Medium") -> diffLvl = 0.2;
                case ("Hard") -> diffLvl = 0.6;
                case ("Dead") -> diffLvl = 1.0;
            }
        });
        ObservableList<String> flagColors = FXCollections.observableArrayList("RED", "GREEN", "BLUE", "PINK");
        ChoiceBox<String> flagChoose = new ChoiceBox<>(flagColors);
        Label flagCol = new Label("Flag color:");
        flagChoose.setLayoutX(100);
        flagChoose.setLayoutY(250);
        flagCol.setLayoutX(10);
        flagCol.setLayoutY(255);
        flagChoose.setOnAction(e -> flagColor = Color.valueOf(flagChoose.getValue()));
        pane.getChildren().addAll(text, restartButton, diffLevel, chooseLvl, theme, chooseTheme, flagChoose, flagCol);
        Scene scene = new Scene(pane, 205, 290);
        window.setScene(scene);
        InputStream iconStream = getClass().getResourceAsStream("/icon.png");
        Image imageIcon = new Image(Objects.requireNonNull(iconStream));
        window.getIcons().add(imageIcon);
        window.setTitle("MineSweeper");
        window.showAndWait();
    }

    @Override
    public void start(Stage stage) {
        stage.setResizable(false);
        scene = new Scene(createContent());
        scene.setOnKeyPressed(keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.ESCAPE) stage.close();
                }
        );

        InputStream iconStream = getClass().getResourceAsStream("/icon.png");
        Image imageIcon = new Image(Objects.requireNonNull(iconStream));
        stage.getIcons().add(imageIcon);
        stage.setTitle("MineSweeper");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
