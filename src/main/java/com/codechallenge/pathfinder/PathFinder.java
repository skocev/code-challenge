package com.codechallenge.pathfinder;

import java.util.HashSet;
import java.util.List;

import static com.codechallenge.pathfinder.Direction.*;

public class PathFinder {

    public Result findPath(char[][] map) {
        var path = new StringBuilder();
        var letters = new StringBuilder();
        var visitedPositions = new HashSet<Position>();
        var position = getStartPositionOrThrow(map);
        var direction = NONE;

        while (true) {
            var c = map[position.row()][position.col()];
            collect(c, visitedPositions.contains(position), path, letters);
            visitedPositions.add(position);
            if (c == 'x') {
                return new Result(path.toString(), letters.toString());
            }

            if (checkShouldChangeDirection(position, direction, map)) {
                direction = getNewDirectionOrThrow(position, direction, map);
            }
            position = next(position, direction);
        }
    }

    void collect(char c, boolean isVisited, StringBuilder path, StringBuilder letters) {
        path.append(c);
        if (c >= 'A' && c <= 'Z' && !isVisited) {
            letters.append(c);
        }
    }

    Position getStartPositionOrThrow(char[][] map) {
        Position start = null;
        int startCount = 0, endCount = 0;

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                var c = map[i][j];
                if (!isValidCharacter(c)) {
                    throw new InvalidMapException(
                        "Invalid character %s at position %d,%d".formatted(c, i, j));
                }

                if (c == '@') {
                    startCount++;
                    start = new Position(i, j);
                } else if (c == 'x') {
                    endCount++;
                }
            }
        }

        if (startCount == 0) {
            throw new InvalidMapException("Missing start character");
        } else if (startCount > 1) {
            throw new InvalidMapException("Multiple start characters");
        } else if (endCount == 0) {
            throw new InvalidMapException("Missing end character");
        }

        return start;
    }

    boolean checkShouldChangeDirection(Position position, Direction direction, char[][] map) {
        return direction == NONE
            || map[position.row()][position.col()] == '+'
            || !isValidPosition(next(position, direction), map);
    }

    Direction getNewDirectionOrThrow(Position position, Direction direction, char[][] map) {
        List<Direction> validDirections =
            allowedDirectionChange(direction).stream()
                .filter((dir) -> isValidPosition(next(position, dir), map))
                .toList();

        if (validDirections.isEmpty()) {
            throw new InvalidMapException(
                "No valid directions from position %d,%d".formatted(position.row(), position.col()));
        } else if (validDirections.size() > 1) {
            throw new InvalidMapException(
                "Multiple valid directions from position %d,%d".formatted(position.row(), position.col()));
        }

        return validDirections.getFirst();
    }

    List<Direction> allowedDirectionChange(Direction direction) {
        return switch (direction) {
            case UP, DOWN -> List.of(LEFT, RIGHT);
            case LEFT, RIGHT -> List.of(UP, DOWN);
            case NONE -> List.of(UP, RIGHT, DOWN, LEFT);
        };
    }

    Position next(Position position, Direction direction) {
        return new Position(position.row() + direction.row(), position.col() + direction.col());
    }

    boolean isValidPosition(Position position, char[][] map) {
        return position.row() >= 0
            && position.col() >= 0
            && position.row() < map.length
            && position.col() < map[position.row()].length
            && map[position.row()][position.col()] != ' ';
    }

    boolean isValidCharacter(char c) {
        return c == '@'
            || c == 'x'
            || c == '-'
            || c == '|'
            || c == '+'
            || c == ' '
            || c >= 'A' && c <= 'Z';
    }

}
