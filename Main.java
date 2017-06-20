package com.Gast9ra;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;


public class Main extends Application {

    public static final int TILE_SIZE = 20;
    public static final int GRID_WIDTH = 15;
    public static final int GRID_HEIGHT = 20;

    private double time;
    private GraphicsContext g;

    private int[][] grid = new int[GRID_WIDTH][GRID_HEIGHT];

    private List<Tetromino> original = new ArrayList<>();
    private List<Tetromino> tetrominos = new ArrayList<>();

    private Tetromino selected;

    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

        Canvas canvas = new Canvas(GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);
        g = canvas.getGraphicsContext2D();

        root.getChildren().addAll(canvas);

        original.add(new Tetromino(Color.BLUE,
                new Piece(0, Direction.DOWN),
                new Piece(1, Direction.LEFT),
                new Piece(1, Direction.RIGHT),
                new Piece(2, Direction.RIGHT)
        ));
        original.add(new Tetromino(Color.BLACK,
                new Piece(0, Direction.DOWN),
                new Piece(1,Direction.DOWN,Direction.LEFT),
                new Piece(1,Direction.DOWN),
                new Piece(1,Direction.RIGHT)));

        original.add(new Tetromino(Color.RED,
                new Piece(0, Direction.DOWN),
                new Piece(1, Direction.LEFT),
                new Piece(1, Direction.RIGHT),
                new Piece(1, Direction.DOWN)
        ));

        original.add(new Tetromino(Color.YELLOW,
                new Piece(0, Direction.DOWN),
                new Piece(1, Direction.LEFT),
                new Piece(1,Direction.DOWN,Direction.RIGHT),
                new Piece(1,Direction.DOWN)
        ));

        original.add(new Tetromino(Color.GREEN,
                new Piece(0, Direction.DOWN),
                new Piece(1, Direction.RIGHT),
                new Piece(2, Direction.RIGHT),
                new Piece(1, Direction.DOWN)));

        original.add(new Tetromino(Color.GRAY,
                new Piece(0, Direction.DOWN),
                new Piece(1, Direction.RIGHT),
                new Piece(1, Direction.RIGHT, Direction.DOWN),
                new Piece(1, Direction.DOWN)));

        spawn();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                time += 0.017;

                if (time >= 0.5) {
                    update();
                    render();
                    time = 0;
                }
            }
        };
        timer.start();

        return root;
    }

    private void update() {
        makeMove(p -> p.move(Direction.DOWN), p -> p.move(Direction.UP), true);
    }

    private void render() {
        g.clearRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

        tetrominos.forEach(p -> p.draw(g));
    }

    private void placePiece(Piece piece) {
        grid[piece.getX()][piece.getY()]++;
    }

    private void removePiece(Piece piece) {
        grid[piece.getX()][piece.getY()]--;
    }

    private boolean isOffscreen(Piece piece) {
        return piece.getX() < 0 || piece.getX() >= GRID_WIDTH
                || piece.getY() < 0 || piece.getY() >= GRID_HEIGHT;
    }

    private void makeMove(Consumer<Tetromino> onSuccess, Consumer<Tetromino> onFail, boolean endMove) {
        selected.getPieces().forEach(this::removePiece);

        onSuccess.accept(selected);

        boolean offscreen = selected.getPieces().stream().anyMatch(this::isOffscreen);

        if (!offscreen) {
            selected.getPieces().forEach(this::placePiece);
        } else {
            onFail.accept(selected);

            selected.getPieces().forEach(this::placePiece);

            if (endMove) {
                sweep();
            }

            return;
        }

        if (!isValidState()) {
            selected.getPieces().forEach(this::removePiece);

            onFail.accept(selected);

            selected.getPieces().forEach(this::placePiece);

            if (endMove) {
                sweep();
            }
        }
    }

    private boolean isValidState() {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (grid[x][y] > 1) {
                    return false;
                }
            }
        }

        return true;
    }

    private void sweep() {
        List<Integer> rows = sweepRows();
        rows.forEach(row -> {
            for (int x = 0; x < GRID_WIDTH; x++) {
                for (Tetromino tetromino : tetrominos) {
                    tetromino.detach(x, row);
                }

                grid[x][row]--;
            }
        });

        rows.forEach(row -> {
            tetrominos.stream().forEach(tetromino -> {
                tetromino.getPieces().stream()
                        .filter(piece -> piece.getY() < row)
                        .forEach(piece -> {
                            removePiece(piece);
                            piece.setY(piece.getY()+1);
                            placePiece(piece);
                        });
            });
        });

        spawn();
    }

    private List<Integer> sweepRows() {
        List<Integer> rows = new ArrayList<>();

        outer:
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (grid[x][y] != 1) {
                    continue outer;
                }
            }

            rows.add(y);
        }

        return rows;
    }

    private void spawn() {
        Tetromino tetromino = original.get(new Random().nextInt(original.size())).copy();
        tetromino.move(GRID_WIDTH / 2, 0);

        selected = tetromino;

        tetrominos.add(tetromino);
        tetromino.getPieces().forEach(this::placePiece);

        if (!isValidState()) {
            System.out.println("Game Over");
            System.exit(0);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP||e.getCode() == KeyCode.W) {
                makeMove(p -> p.rotate(), p -> p.rotateBack(), false);
            } else if (e.getCode() == KeyCode.LEFT||e.getCode() ==KeyCode.A) {
                makeMove(p -> p.move(Direction.LEFT), p -> p.move(Direction.RIGHT), false);
            } else if (e.getCode() == KeyCode.RIGHT||e.getCode() == KeyCode.D) {
                makeMove(p -> p.move(Direction.RIGHT), p -> p.move(Direction.LEFT), false);
            } else if (e.getCode() == KeyCode.SPACE) {
                makeMove(p -> p.move(Direction.DOWN), p -> p.move(Direction.UP), true);
            }

            render();
        });

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
