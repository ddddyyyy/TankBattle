package com.mdy.game;

import javax.swing.*;


public class Missile extends MyImage {
    private int direction;
    private final static int speed = 10;
    private final static int damage = 10;
    //子弹的长宽
    final static int m_w = 10;
    final static int m_h = 10;
    private int id;

    Missile(int x, int y, int direction, int _id) {
        super(x, y);
        this.height = 10;
        this.width = 10;
        this.direction = direction;
        this.id = _id;
    }

    /**
     * @return 子弹是否碰到物体
     */
    private boolean isMeet() {

        for (Wall wall : Game.walls.values()) {
            if (wall.isIntersects(this)) {
                if (wall.id == Game.WALL) {
                    Game.map[wall.coord.y][wall.coord.x] = Game.BLANK;
                    Game.walls.remove(wall.hashCode());
                }
                return true;
            }
        }

        for (Tank tank : Game.tanks.values()) {
            if (tank.isIntersects(this)) {
                //子弹的伤害
                if (id != tank.id) {
                    tank.hp -= damage / 10;
                }
                if (tank.hp <= 0) {
                    //如果该坦克不属于玩家控制的话就不进行下一步的处理
                    if (tank.id >= Game.PLAY_1) {
                        tank.flag = false;
                        if (Game.mode == Mode.Single) {
                            if (tank.equals(Game.tanks.get(tank.id))) {
                                Game.ShutDown();
                                return true;
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, tank.id == Game.PLAY_1 ? "p2 win!!" : "p2 win!!");
                            Game.ShutDown();
                            //游戏结束，一定是受到了攻击
                            return true;
                        }
                    }
                    synchronized (Game.map){
                        Game.map[tank.coord.y][tank.coord.x] = Game.BLANK;
                    }
                    tank.flag = false;
                    Game.tanks.remove(tank.id);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @return 是否碰撞到物体
     */
    boolean Move() {
        switch (direction) {
            case Game.UP:
                y -= speed;
                break;
            case Game.DOWN:
                y += speed;
                break;
            case Game.LEFT:
                x -= speed;
                break;
            case Game.RIGHT:
                x += speed;
                break;
        }
        return isMeet();
    }
}
