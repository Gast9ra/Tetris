package com.Gast9ra;

import java.util.Arrays;
import java.util.List;

public class Piece {

    private int distance;
    private List<Direction> directions;
    private Tetromino parent;
    private int x, y;

    public Piece(int distance, Direction... direction) {
        this.distance = distance;
        this.directions = Arrays.asList(direction);
    }

    public void setParent(Tetromino parent) {
        this.parent = parent;

        int dx = 0, dy = 0;

        for (Direction d : directions) {
            dx += distance * d.x;
            dy += distance * d.y;
        }

        x = parent.getX() + dx;
        y = parent.getY() + dy;
    }

    public void setDirection(Direction... direction) {
        this.directions = Arrays.asList(direction);

        int dx = 0, dy = 0;

        for (Direction d : directions) {
            dx += distance * d.x;
            dy += distance * d.y;
        }

        x = parent.getX() + dx;
        y = parent.getY() + dy;
    }

    public Piece copy() {
        return new Piece(distance, directions.toArray(new Direction[0]));
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public List<Direction> getDirections() {
        return directions;
    }

    public void setDirections(List<Direction> directions) {
        this.directions = directions;
    }

    public Tetromino getParent() {
        return parent;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
