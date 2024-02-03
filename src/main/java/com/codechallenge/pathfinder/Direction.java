package com.codechallenge.pathfinder;

enum Direction {

    UP(-1, 0),
    RIGHT(0, 1),
    DOWN(1, 0),
    LEFT(0, -1),
    NONE(0, 0);

    private final int row;
    private final int col;

    Direction(int row, int col) {
        this.row = row;
        this.col = col;
    }

    int row() {
        return row;
    }

    int col() {
        return col;
    }

}
