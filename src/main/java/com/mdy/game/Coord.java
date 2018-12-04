package com.mdy.game;


/**
 * 寻路算法所需的结构体
 * 这里的x和y是分别指在二维数组中的行和列
 * 因此当使用这个该类对数组的值进行修改的时候
 * 应该使用map[y][x]=xxx
 */
class Coord {
    int x;
    int y;
    Coord per;
    boolean isMove;

    Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Coord) {
            Coord c = (Coord) obj;
            return x == c.x && y == c.y;
        }
        return false;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
