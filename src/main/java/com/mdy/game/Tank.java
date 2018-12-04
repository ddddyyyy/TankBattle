package com.mdy.game;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.*;

public class Tank extends MyImage implements Runnable {


    ExecutorService executorService = Executors.newCachedThreadPool();

    //线程睡眠的时间
    //MP的恢复时间
    final static int MP_TIME = 1000;
    //AI移动下一步的时间
    final static int MOVE_TIME = 50;
    //是否存活，用于线程终止
    boolean flag = true;

    //下一个位移的坐标
    private volatile Coord next;
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

    //这里采用只要移动一格就重新计算路径获得下一步要到的格子的坐标的策略
    //另外一种解决思路是每移动几个步骤就重新计算，但由于可能导致起始坐标发生改变，因此不采用

    //但是使用第一张方法可能会导致一种情况，当坦克不能移动的时候是无法计算坐标的
    //由于第一张情况会忽略下一格以及下下格的有坦克的情况，但不会忽略玩家坦克
    //因此会出现当夹击主坦克后另外两个坦克互相卡住无法移动的情况
    //因此这里这里统计重复移动同一个方向的次数，超出次数就向后退一格的策略
    //本来是打算重新计算路径的，但发现效果不是很好
    //实际上在获得路线的算法上进行过优化，但是效果不好，基本上还是会出现两个坦克卡住的情况

    //实际上还出现过坦克一直来回反复运动的情况，本来是打算通过修该寻路算法中测试不同的方向的时候随机，当同样发现效果感人，放弃
    class ETankMove implements Runnable {
        public void run() {
            int d = _direction;
            int count = 0;
            while (flag) {
                if (stackFuture.isDone()) {
                    try {
                        //使用栈存放广度遍历算法得到的移动的路径
                        Stack<Coord> result = stackFuture.get();
                        //获得下一个路径
                        if (null != result && result.size() != 0 && null == next) {
                            next = result.pop();
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                ETankMove();
                //为了防止两个坦克为了竞争同一个前面的方块而卡住
                // 这里采用如果在同一个移动方向停滞过久就往反方向移动一格的方法
                if (d == _direction) {
                    if (++count > 80) {
                        int n;
                        switch (d) {
                            case Game.UP:
                                n = KeyEvent.VK_DOWN;
                                break;
                            case Game.DOWN:
                                n = KeyEvent.VK_UP;
                                break;
                            case Game.LEFT:
                                n = KeyEvent.VK_RIGHT;
                                break;
                            case Game.RIGHT:
                                n = KeyEvent.VK_LEFT;
                                break;
                            default:
                                n = KeyEvent.VK_SHIFT;
                        }
                        try {

//                            next = GetPath().pop();
                            for (int j = 0; j < 5; ++j) {
                                GetKey(n);
                                Thread.sleep(MOVE_TIME);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        count = 0;
                    }
                } else {
                    d = _direction;
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
        public Stack<Coord> call() {
            //该返回结果将被Future的get方法得到
//            Random random = new Random();
//            Thread.sleep(random.nextInt(50));
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

    //根据计算得到的路径进行移动
    private void ETankMove() {
        //这里位移方向判断直接计算,可能比较抽象。。。
        //存在线程安全问题，由于坐标可能错位一位以上，因此只能采用>=
        if (null != next && !next.equals(coord)) {
            if (Math.abs(coord.x - next.x) > 1 || Math.abs(coord.y - next.y) > 1) {
                System.out.println(Thread.currentThread().getName() + ":" + coord.toString() + "->" + next.toString());
            }
            GetKey(GetDirection(coord, next));
//            if (Game.map[next.y][next.x] == Game.WALLS) {
//                GetKey(arr[4]);
//            }
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
        IsMove.add(coord);
        d_q.offer(coord);
        Coord last = null;
        boolean flag;
        while (!d_q.isEmpty()) {
            Coord t = d_q.poll();
            int tx = t.x;
            int ty = t.y;
            int i;
            //遍历所有的方向
//            Random r = new Random(System.currentTimeMillis());
            for (i = 0; i < 4; ++i) {
                switch (i /*+ (r.nextInt(2)) % 4*/) {
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
                    if (flag) {
                        Coord temp = new Coord(z.x, z.y);
                        switch (i) {
                            case Game.UP:
                                temp.y -= 1;
                                break;
                            case Game.LEFT:
                                temp.x -= 1;
                                break;
                            case Game.RIGHT:
                                temp.x += 1;
                                break;
                            case Game.DOWN:
                                temp.y += 1;
                                break;
                        }
                        flag = (Game.map[temp.y][temp.x] == Game.BLANK || Game.map[temp.y][temp.x] == Game.WALLS || Game.map[temp.y][temp.x] == Game.P1_TAG || Game.map[temp.y][temp.x] == this.hashCode());
                    }
                }
                //该点可以用
                if (flag) {
                    //将坐标纳入已经遍历的队列中
                    d_q.offer(z);
                    z.per = t;
                    last = z;
                }
                IsMove.add(z);
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
            stackFuture = executorService.submit(new TaskWithPath());
            executorService.execute(new ETankMove());
            System.out.println(executorService.toString());
        } else {
            executorService.execute(new MyTankMove());
        }
        executorService.execute(new TankMpRecover());
    }


    int GetDirection(Coord coord, Coord next) {
        int n;
        if (coord.x - next.x <= -1) {
            n = KeyEvent.VK_RIGHT;
        } else if (coord.x - next.x >= 1) {
            n = KeyEvent.VK_LEFT;
        } else {
            if (coord.y - next.y <= -1) {
                n = KeyEvent.VK_DOWN;
            } else {
                n = KeyEvent.VK_UP;
            }
        }
        return n;
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
            if (((t_y != coord.y) || (t_x != coord.x)) && (x % Game.width == 0 && y % Game.height == 0)) {
                Game.map[t_y][t_x] = Game.map[coord.y][coord.x];
                Game.map[coord.y][coord.x] = Game.BLANK;
                coord.x = t_x;
                coord.y = t_y;
//                if (id == Game.PLAY_1)  Game.printMap();
                if (id <= Game.PLAY_1) {
                    stackFuture = executorService.submit(new TaskWithPath());
                    next = null;
                }

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
