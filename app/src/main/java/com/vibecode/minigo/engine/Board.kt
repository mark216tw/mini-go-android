package com.vibecode.minigo.engine

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val color: Stone,
    val stones: Set<Point>,
    val liberties: Set<Point>,
)

@Serializable
data class Board(
    val size: Int,
    val cells: List<Stone>,
) {
    init {
        require(size >= 2) { "Board size must be at least 2" }
        require(cells.size == size * size) { "Cell count does not match board size" }
    }

    fun index(x: Int, y: Int): Int = y * size + x

    fun inBounds(x: Int, y: Int): Boolean = x in 0 until size && y in 0 until size

    fun inBounds(point: Point): Boolean = inBounds(point.x, point.y)

    fun get(x: Int, y: Int): Stone = cells[index(x, y)]

    fun get(point: Point): Stone = get(point.x, point.y)

    fun isEmpty(point: Point): Boolean = get(point) == Stone.EMPTY

    fun place(point: Point, stone: Stone): Board {
        require(inBounds(point)) { "Point outside board" }
        require(isEmpty(point)) { "Point is already occupied" }
        val updated = cells.toMutableList()
        updated[index(point.x, point.y)] = stone
        return Board(size, updated)
    }

    fun remove(points: Collection<Point>): Board {
        if (points.isEmpty()) return this
        val updated = cells.toMutableList()
        points.forEach { updated[index(it.x, it.y)] = Stone.EMPTY }
        return Board(size, updated)
    }

    fun neighbors(point: Point): List<Point> {
        val result = ArrayList<Point>(4)
        if (point.x > 0) result.add(Point(point.x - 1, point.y))
        if (point.x < size - 1) result.add(Point(point.x + 1, point.y))
        if (point.y > 0) result.add(Point(point.x, point.y - 1))
        if (point.y < size - 1) result.add(Point(point.x, point.y + 1))
        return result
    }

    fun diagonals(point: Point): List<Point> {
        val result = ArrayList<Point>(4)
        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                val nx = point.x + dx
                val ny = point.y + dy
                if (inBounds(nx, ny)) result.add(Point(nx, ny))
            }
        }
        return result
    }

    fun groupAt(point: Point): Group? {
        if (!inBounds(point)) return null
        val color = get(point)
        if (color == Stone.EMPTY) return null

        val stones = LinkedHashSet<Point>()
        val liberties = LinkedHashSet<Point>()
        val pending = ArrayDeque<Point>()
        pending.add(point)
        stones.add(point)

        while (pending.isNotEmpty()) {
            val current = pending.removeFirst()
            for (neighbor in neighbors(current)) {
                when (get(neighbor)) {
                    Stone.EMPTY -> liberties.add(neighbor)
                    color -> if (stones.add(neighbor)) pending.add(neighbor)
                    else -> Unit
                }
            }
        }
        return Group(color, stones, liberties)
    }

    fun groups(color: Stone): List<Group> {
        val seen = HashSet<Point>()
        val result = ArrayList<Group>()
        for (y in 0 until size) {
            for (x in 0 until size) {
                val point = Point(x, y)
                if (get(point) != color || point in seen) continue
                val group = groupAt(point) ?: continue
                seen.addAll(group.stones)
                result.add(group)
            }
        }
        return result
    }

    companion object {
        fun empty(size: Int): Board = Board(size, List(size * size) { Stone.EMPTY })

        fun fromAscii(rows: List<String>): Board {
            val size = rows.size
            val cells = ArrayList<Stone>(size * size)
            for (row in rows) {
                require(row.length == size) { "Rows must be square" }
                for (char in row) {
                    cells.add(
                        when (char) {
                            '.', ' ' -> Stone.EMPTY
                            'X', 'B', 'x', 'b' -> Stone.BLACK
                            'O', 'W', 'o', 'w' -> Stone.WHITE
                            else -> error("Unknown cell: $char")
                        },
                    )
                }
            }
            return Board(size, cells)
        }
    }
}
