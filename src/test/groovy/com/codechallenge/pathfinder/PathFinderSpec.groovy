package com.codechallenge.pathfinder


import spock.lang.Shared
import spock.lang.Specification

import static com.codechallenge.pathfinder.Direction.*
import static com.codechallenge.samplemaps.Maps.*

class PathFinderSpec extends Specification {

    @Shared
    def pathFinder = new PathFinder()

    def "the only valid characters are @, x, -, |, +, 'whitespace' and capital letters A-Z"() {
        expect:
        pathFinder.isValidCharacter(c as char)

        where:
        c << ['@', 'x', '-', '|', '+', ' ', 'A', 'B',
              'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
              'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
              'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z']
    }

    def 'when map contains invalid character throw exception'() {
        when:
        pathFinder.getStartPositionOrThrow(invalidMap10InvalidCharacter)

        then:
        def e = thrown(InvalidMapException)
        e.message == 'Invalid character b at position 0,7'
    }

    def 'when map is missing start character throw exception'() {
        when:
        pathFinder.getStartPositionOrThrow(invalidMap1MissingStartCharacter)

        then:
        def e = thrown(InvalidMapException)
        e.message == 'Missing start character'
    }

    def 'when map contains multiple start characters throw exception'() {
        when:
        pathFinder.getStartPositionOrThrow(invalidMap)

        then:
        def e = thrown(InvalidMapException)
        e.message == 'Multiple start characters'

        where:
        invalidMap << [invalidMap3MultipleStarts,
                       invalidMap4MultipleStarts,
                       invalidMap5MultipleStarts]
    }

    def 'when map is missing end character throw exception'() {
        when:
        pathFinder.getStartPositionOrThrow(invalidMap2MissingEndCharacter)

        then:
        def e = thrown(InvalidMapException)
        e.message == 'Missing end character'
    }

    def 'get the start character position of the map'() {
        expect:
        pathFinder.getStartPositionOrThrow(map1) == new Position(0, 0)
    }

    def 'valid positions are non-whitespace characters inside the map boundary'() {
        expect:
        pathFinder.isValidPosition(position, map1) == isValid

        where:
        position             | isValid
        new Position(2, 2)   | true
        new Position(-1, -1) | false
        new Position(1, 1)   | false
    }

    def 'collect characters'() {
        given:
        def path = new StringBuilder()
        def letters = new StringBuilder()

        when: 'collect is called multiple times'
        pathFinder.collect('@' as char, false, path, letters)
        pathFinder.collect('|' as char, false, path, letters)
        pathFinder.collect('|' as char, false, path, letters)
        pathFinder.collect('A' as char, false, path, letters)
        pathFinder.collect('A' as char, false, path, letters)
        pathFinder.collect('B' as char,  true, path, letters)
        pathFinder.collect('x' as char, false, path, letters)

        then: 'all characters are collected into "path"'
        path.toString() == '@||AABx'
        and: 'unvisited letters are collected into "letters"'
        letters.toString() == 'AA'
    }

    def 'should change direction when character at position is +'() {
        expect:
        pathFinder.checkShouldChangeDirection(
                new Position(0, 8), RIGHT, map3)
    }

    def 'should change direction when direction is NONE'() {
        expect:
        pathFinder.checkShouldChangeDirection(
                new Position(0, 0), NONE, map3)
    }

    def 'should change direction when next position is not valid'() {
        expect:
        pathFinder.checkShouldChangeDirection(
                new Position(4, 8), DOWN, map3)
    }

    def 'next position from #position in #direction is #next'() {
        expect:
        pathFinder.next(position, direction) == next

        where:
        position           | direction | next
        new Position(1, 1) | UP        | new Position(0, 1)
        new Position(1, 1) | RIGHT     | new Position(1, 2)
        new Position(1, 1) | DOWN      | new Position(2, 1)
        new Position(1, 1) | LEFT      | new Position(1, 0)
    }

    def 'allowed directions change from #direction are #allowed'() {
        expect:
        pathFinder.allowedDirectionChange(direction) == allowed

        where:
        direction | allowed
        UP        | [LEFT, RIGHT]
        DOWN      | [LEFT, RIGHT]
        LEFT      | [UP, DOWN]
        RIGHT     | [UP, DOWN]
        NONE      | [UP, RIGHT, DOWN, LEFT]
    }

    def 'when there are no valid directions to continue from given position throw exception'() {
        when:
        pathFinder.getNewDirectionOrThrow(position, direction, map)

        then:
        def e = thrown(InvalidMapException)
        e.message == "No valid directions from position ${position.row()},${position.col()}"

        where:
        map                   | position           | direction
        invalidMap7BrokenPath | new Position(1, 5) | DOWN
        invalidMap9FakeTurn   | new Position(0, 4) | RIGHT
    }

    def 'when there are multiple valid directions to continue from given position throw exception'() {
        when:
        pathFinder.getNewDirectionOrThrow(position, direction, map)

        then:
        def e = thrown(InvalidMapException)
        e.message == "Multiple valid directions from position ${position.row()},${position.col()}"

        where:
        map                              | position           | direction
        invalidMap6ForkInPath            | new Position(2, 7) | RIGHT
        invalidMap8MultipleStartingPaths | new Position(0, 4) | NONE
    }

    def 'get the single valid direction to continue from given position'() {
        expect:
        pathFinder.getNewDirectionOrThrow(new Position(4, 8), DOWN, map3) == LEFT
    }

    def 'find path returns correct result if no errors occur'() {
        expect:
        pathFinder.findPath(map) == result

        where:
        map  | result
        map1 | new Result('@---A---+|C|+---+|+-B-x', 'ACB')
        map2 | new Result('@|A+---B--+|+--C-+|-||+---D--+|x', 'ABCD')
        map3 | new Result('@---A---+|||C---+|+-B-x', 'ACB')
        map4 | new Result('@-G-O-+|+-+|O||+-O-N-+|I|+-+|+-I-+|ES|x', 'GOONIES')
        map5 | new Result('@B+++B|+-L-+A+++A-+Hx', 'BLAH')
        map6 | new Result('@-A--+|+-B--x', 'AB')
        map7 | new Result('@ABCDEFGHIJKDLMNx', 'ABCDEFGHIJKLMN')
        map8 | new Result('@x', '')
    }

    def 'find path throws exception if map is invalid'() {
        when:
        pathFinder.findPath(map)

        then:
        thrown(InvalidMapException)

        where:
        map << [invalidMap1MissingStartCharacter,
                invalidMap2MissingEndCharacter,
                invalidMap3MultipleStarts,
                invalidMap4MultipleStarts,
                invalidMap5MultipleStarts,
                invalidMap6ForkInPath,
                invalidMap7BrokenPath,
                invalidMap8MultipleStartingPaths,
                invalidMap9FakeTurn,
                invalidMap10InvalidCharacter]
    }

}
