package com.mdy.game;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.*;

public class Tank extends MyImage implements Runnable {


    ExecutorService executorService = Executors.newCachedThreadPool();

    //线程睡眠的时间
    //MP的恢复时间
    final static int MP_TIME = 1000;
    //AI移动下一步的时间
    final static int MOVE_TIME = 100;
    //是否存活，用于线程终止
    boolean flag = true;

    //下一个位移的坐标
    private Coord next;
    //使用栈存放广度遍历算法得到的移动的路径
    private volatile Stack<Coord> result;
    private Future<Stack<Coord>> stackFuture;

    private boolean direction[] = {false, false, false, false};
    int _direction;

    int id;

    //敌人坦克的速度
    int speed;
    //坦克的血量
    int hp = Game.HP;
    //坦克的MP
    int mp = Game.MP;
    //当前位移的按键
    int key;
    //是否可以移动，用于处理连续按键响应
    boolean move = false;

    //-------------------多线程线程所用到的内部类------------------------start
    //AI坦克移动的线程
    class ETankMove implements Runnable {
        public void run() {
            int count = 0;
            while (flag) {
                ETankMove();
                //这里没移动五次就从新计算路径
                if (count++ >= 5) {
                    stackFuture = executorService.submit(new TaskWithPath());
                    count = 0;
                }
                try {
                    Thread.sleep(MOVE_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //路径获得的线程
    class TaskWithPath implements Callable<Stack<Coord>> {

        /**
         * 任务的具体过程，一旦任务传给ExecutorService的submit方法，
         * 则该方法自动在一个线程上执行
         */
        public Stack<Coord> call() throws InterruptedException {
            //该返回结果将被Future的get方法得到
            Thread.sleep(100);
            return GetPath();
        }
    }

    //按键监听的进程
    class MyTankMove implements Runnable {
        public void run() {
            while (flag) {
                while (move) {
                    GetKey(key);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //----------------线程内部类-----------------end
    private final static int arr[] = {37, 38, 39, 40, 16};//L U R D S

    //根据计算得到的路径进行移动
    private void ETankMove() {
        int n;
        //这里位移方向判断直接计算,可能比较抽象。。。
        //存在线程安全问题，由于坐标可能错位一位以上，因此只能采用>=
        if (null != next && !next.equals(coord)) {
            if (coord.x - next.x <= -1) {
                n = 2;
            } else if (coord.x - next.x >= 1) {
                n = 0;
            } else {
                if (coord.y - next.y <= -1) {
                    n = 3;
                } else {
                    n = 1;
                }
            }
            GetKey(arr[n]);
//            if (Game.map[next.y][next.x] == Game.WALLS) {
//                GetKey(arr[4]);
//            }
        } else {
            if (stackFuture.isDone()) {
                try {
                    result = stackFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            //获得下一个路径
            if (null != result && result.size() != 0) {
                next = result.pop();
            }
        }
    }


    /**
     * 使用广度遍历算法，使用队列存储遍历的节点
     *
     * @return 移动的路径
     */
    private Stack<Coord> GetPath() {
        Coord target = Game.tanks.get(Game.P1_TAG).coord;
        Queue<Coord> d_q = new LinkedBlockingQueue<>();
        ArrayList<Coord> IsMove = new ArrayList<>();
        d_q.offer(coord);
        Coord last = null;
        boolean flag;
        while (!d_q.isEmpty()) {
            Coord t = d_q.poll();
            int tx = t.x;
            int ty = t.y;
            int i;
            //遍历所有的方向
            for (i = 0; i < 4; ++i) {
                switch (i) {
                    case Game.UP:
                        ty -= 1;
                        break;
                    case Game.LEFT:
                        tx -= 1;
                        break;
                    case Game.RIGHT:
                        tx += 1;
                        break;
                    case Game.DOWN:
                        ty += 1;
                        break;
                }
                //判断该点是否可行
                flag = true;
                Coord z = new Coord(tx, ty);
                //检查是否为目标终点
                if (z.equals(target)) {
                    z.per = t;
                    last = z;
                    break;
                }
                //检查该坐标是否已经遍历了
                for (Coord c : IsMove) {
                    if (c.equals(z)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    //通过数组，判断是否这一点可以走
                    flag = (Game.map[ty][tx] == Game.BLANK || Game.map[ty][tx] == Game.WALLS);
                }
                //该点可以用
                if (flag) {
                    //将坐标纳入已经遍历的队列中
                    d_q.offer(z);
                    IsMove.add(z);
                    z.per = t;
                    last = z;
                }
                //重新选择方向遍历
                tx = t.x;
                ty = t.y;
            }
            //如果没有四个方向都遍历完就跳出，说明已经找到了终点
            if (i != 4) {
                break;
            }
        }
        Stack<Coord> coords = new Stack<>();
        while (null != last && last.per != null) {
            coords.push(last);
            last = last.per;
        }
        return coords;
    }

    Tank(Coord coord, int direction, int id) {
        super(coord);
        this.direction[direction] = true;
        this._direction = direction;
        this.id = id;
        if (id < Game.PLAY_1) {
            result = GetPath();
            stackFuture = executorService.submit(new TaskWithPath());
            executorService.execute(new ETankMove());
            System.out.println(executorService.toString());
        } else {
            executorService.execute(new MyTankMove());
        }
        executorService.execute(new TankMpRecover());
    }

    /**
     * @return 是否可以移动
     */
    private boolean isMovable() {
        //检测障碍物
        for (Wall wall : Game.walls.values()) {
            if (wall.isIntersects(this)) {
                if (id < Game.PLAY_1 && wall.id == Game.WALL)
                    GetKey(16);
                return true;
            }
        }
        //检测坦克
        for (Tank tank : Game.tanks.values()) {
            if (tank.isIntersects(this) && !this.equals(tank)) {
                //如果是玩家就攻击
                if (id < Game.PLAY_1 && tank.id >= Game.PLAY_1) {
                    GetKey(16);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 处理坦克移动
     *
     * @param n 移动按键值
     */
    void GetKey(int n) {
        int t_x = x;
        int t_y = y;
        //判断按键
        switch (n) {
            //判断移动基本上都是先假设已经移动然后判断移动之后是否会发生重叠
            //如果坐标发生了一整格的改变，那就更新map
            case KeyEvent.VK_UP: {
                y -= speed;
                if (!direction[Game.UP] || isMovable()) {
                    y = t_y;
                    if (!direction[Game.UP]) {
                        direction[Game.UP] = true;
                        direction[_direction] = false;
                        _direction = Game.UP;
                    } else {
                        return;
                    }
                }
                break;
            }
            case KeyEvent.VK_DOWN: {
                y += speed;
                if (!direction[Game.DOWN] || isMovable()) {
                    y = t_y;
                    if (!direction[Game.DOWN]) {
                        direction[Game.DOWN] = true;
                        direction[_direction] = false;
                        _direction = Game.DOWN;
                    } else {
                        return;
                    }
                }
                break;
            }
            case KeyEvent.VK_LEFT: {
                x -= speed;
                if (!direction[Game.LEFT] || isMovable()) {
                    x = t_x;
                    if (!direction[Game.LEFT]) {
                        direction[Game.LEFT] = true;
                        direction[_direction] = false;
                        _direction = Game.LEFT;
                    } else {
                        return;
                    }
                }
                break;
            }
            case KeyEvent.VK_RIGHT: {
                x += speed;
                if (!direction[Game.RIGHT] || isMovable()) {
                    x = t_x;
                    if (!direction[Game.RIGHT]) {
                        direction[Game.RIGHT] = true;
                        direction[_direction] = false;
                        _direction = Game.RIGHT;
                    } else {
                        return;
                    }
                }
                break;
            }
            case KeyEvent.VK_SHIFT: {
                if (mp > 0) {
                    synchronized ("KEY") {
                        mp -= 10;
                    }
                    if (_direction == Game.UP)
                        Game.missile.add(new Missile(x + Game.width / 2, y - Missile.m_h, _direction, id));
                    if (_direction == Game.DOWN)
                        Game.missile.add(new Missile(x + Game.width / 2, y + Game.height + Missile.m_h, _direction, id));
                    if (_direction == Game.LEFT)
                        Game.missile.add(new Missile(x - Missile.m_w, y + Game.height / 2, _direction, id));
                    if (_direction == Game.RIGHT)
                        Game.missile.add(new Missile(x + Missile.m_w + Game.width, y + Game.height / 2, _direction, id));
                    return;
                }
                break;
            }
        }
        //如果坐标发生了一整格的变化，就更新二维数组
        if (t_y != y | t_x != x) {
            t_y = y / Game.height;
            t_x = x / Game.width;
            if ((t_y != coord.y && y % Game.height == 0) | (x % Game.width == 0 && t_x != coord.x)) {
                Game.map[t_y][t_x] = Game.map[coord.y][coord.x];
                Game.map[coord.y][coord.x] = Game.BLANK;
                coord.x = t_x;
                coord.y = t_y;
                if (id == Game.PLAY_1) Game.printMap();
            }
        }
    }

    /**
     * 蓝条的恢复
     */
    class TankMpRecover implements Runnable {
        public void run() {
            while (flag) {
                synchronized ("MP") {
                    if (mp < Game.MP)
                        mp += 10;
                }
                try {
                    Thread.sleep(MP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 每隔一段随机时间自动发射子弹
     */
    public void run() {
        Random r = new Random();
        while (flag) {
            try {
                Thread.sleep(r.nextInt(5000));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            GetKey(16);
        }
    }
}
