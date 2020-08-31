package com.mdy.game;


import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mdy.main.Main.executorService;


/**
 * 坦克游戏界面
 */
public class Game extends JPanel {
    private static Image[] array = new Image[23];
    private Image OffScreenImage;
    //玩家1坦克的id
    static int P1_TAG;
    //玩家2
    private static int P2_TAG;


    static {
        // 图片资源数组
        array[0] = new ImageIcon(Game.class.getResource("/img/walls.gif")).getImage();
        array[1] = new ImageIcon(Game.class.getResource("/img/steels.gif")).getImage();
        array[2] = new ImageIcon(Game.class.getResource("/img/enemy1D.gif")).getImage();
        array[3] = new ImageIcon(Game.class.getResource("/img/enemy1L.gif")).getImage();
        array[4] = new ImageIcon(Game.class.getResource("/img/enemy1R.gif")).getImage();
        array[5] = new ImageIcon(Game.class.getResource("/img/enemy1U.gif")).getImage();
        array[6] = new ImageIcon(Game.class.getResource("/img/enemy2D.gif")).getImage();
        array[7] = new ImageIcon(Game.class.getResource("/img/enemy2L.gif")).getImage();
        array[8] = new ImageIcon(Game.class.getResource("/img/enemy2R.gif")).getImage();
        array[9] = new ImageIcon(Game.class.getResource("/img/enemy2U.gif")).getImage();
        array[10] = new ImageIcon(Game.class.getResource("/img/enemy3D.gif")).getImage();
        array[11] = new ImageIcon(Game.class.getResource("/img/enemy3L.gif")).getImage();
        array[12] = new ImageIcon(Game.class.getResource("/img/enemy3R.gif")).getImage();
        array[13] = new ImageIcon(Game.class.getResource("/img/enemy3U.gif")).getImage();
        array[14] = new ImageIcon(Game.class.getResource("/img/p1tankD.gif")).getImage();
        array[15] = new ImageIcon(Game.class.getResource("/img/p1tankL.gif")).getImage();
        array[16] = new ImageIcon(Game.class.getResource("/img/p1tankR.gif")).getImage();
        array[17] = new ImageIcon(Game.class.getResource("/img/p1tankU.gif")).getImage();
        array[18] = new ImageIcon(Game.class.getResource("/img/p2tankD.gif")).getImage();
        array[19] = new ImageIcon(Game.class.getResource("/img/p2tankL.gif")).getImage();
        array[20] = new ImageIcon(Game.class.getResource("/img/p2tankR.gif")).getImage();
        array[21] = new ImageIcon(Game.class.getResource("/img/p2tankU.gif")).getImage();
        array[22] = new ImageIcon(Game.class.getResource("/img/tankmissile.gif")).getImage();
    }


    static Mode mode;

    public static AtomicBoolean live = new AtomicBoolean(false);

    //坦克的移动区域
    private final static int screenWidth = 900;
    private final static int screenHeight = 600;


    //一般图像的大小
    static final int width = 40;
    static final int height = 40;
    //坦克的血量和弹药数
    static final int HP = width;
    static final int MP = width;


    //坦克的移动
    static final int DOWN = 0;
    static final int LEFT = 1;
    static final int RIGHT = 2;
    static final int UP = 3;
    //map上图像的标志
    final static int BLANK = -1;
    final static int WALLS = -2;
    private final static int STEELS = -3;
    //图像的标志
    final static int WALL = 0;
    private final static int STEEL = 1;
    private final static int ENEMY_1 = 0;
    private final static int ENEMY_2 = 4;
    private final static int ENEMY_3 = 8;
    //玩家控制的坦克的编号（图像数组中的编号）
    final static int PLAY_1 = 12;
    private final static int PLAY_2 = 16;

    //地图，储存除了子弹以外的东西
    //注意当使用Coord的x和y的时候是map[y][x]
    public volatile static int[][] map;

    public volatile static ConcurrentHashMap<Integer, Tank> tanks = new ConcurrentHashMap<>();

    public volatile static ConcurrentHashMap<Integer, Wall> walls = new ConcurrentHashMap<>();

    public static final ArrayList<Missile> missile = new ArrayList<>();


    /**
     * 初始化敌方AI坦克
     */
    private void init_ETank() {
        Coord coord = randomCoord();
        Tank tank = new Tank(coord, DOWN, ENEMY_1);
        tank.speed = 10;
        map[coord.y][coord.x] = tank.id;
        tanks.put(tank.id, tank);
        coord = randomCoord();
        tank = new Tank(coord, DOWN, ENEMY_2);
        tank.speed = 10;
        map[coord.y][coord.x] = tank.id;
        tanks.put(tank.id, tank);
        coord = randomCoord();
        tank = new Tank(coord, DOWN, ENEMY_3);
        tank.speed = 10;
        map[coord.y][coord.x] = tank.id;
        tanks.put(tank.hashCode(), tank);
    }

    /**
     * 初始化玩家的坦克
     */
    private void init_Tank() {
        Coord coord = randomCoord();
        Tank p1 = new Tank(coord, DOWN, PLAY_1);
        p1.speed = 20;
        P1_TAG = p1.id;
        map[coord.y][coord.x] = p1.id;
        tanks.put(p1.id, p1);
        //双人模式
        if (mode == Mode.Double) {
            coord = randomCoord();
            Tank p2 = new Tank(coord, DOWN, PLAY_2);
            p2.speed = 10;
            P2_TAG = p2.id;
            map[coord.y][coord.x] = p2.id;
            tanks.put(p2.id, p2);
        } else if (mode == Mode.Single) {
            init_ETank();
        }
    }

    /**
     * 初始化地图
     */
    private void init_map() {

        int x = screenWidth / Game.width;
        int y = screenHeight / Game.height - 1;

        map = new int[y][x];

        for (int i = 0; i < y; ++i) {
            for (int j = 0; j < x; ++j) {
                if (i == 0 || i == y - 1 || j == 0 || j == x - 1) {
                    map[i][j] = STEELS;
                    Wall wall = new Wall(new Coord(j, i), STEEL);
                    walls.put(wall.hashCode(), wall);
                } else {
                    map[i][j] = BLANK;
                }
            }
        }
        //随机
//        for (int i = 0; i < x * y / 2; ++i) {
//            //Coord的y对应数组的行
//            Coord c = randomCoord();
//            map[c.y][c.x] = WALLS;
//            Wall wall = new Wall(c, WALL);
//            walls.put(wall.hashCode(), wall);
//        }

    }


    /**
     * 打印二维地图数组
     */
    @SuppressWarnings("unused")
    static void printMap() {
        System.out.println("------------------------------start----------------------------");
        for (int[] map : map) {
            for (int m : map) {
                System.out.print(m + " ");
            }
            System.out.println();
        }
        System.out.println("-------------------------------------end--------------------------------");
    }

    /**
     * 随机坦克的坐标
     */
    private Coord randomCoord() {
        Random random = new Random(System.currentTimeMillis());
        int x, y;
        do {
            y = random.nextInt(map.length);
            x = random.nextInt(map[0].length);
        } while (map[y][x] != BLANK);
        return new Coord(x, y);
    }


    public Game(Mode mode) {
        setForeground(Color.WHITE);
        setBackground(Color.BLACK);
        setBounds(0, 0, screenWidth, screenHeight);
        setLayout(null);
        Game.mode = mode;
        init_map();
        init_Tank();
        addKeyListener(new KeyBoardListener());
        executorService.submit(new MissileMove());
        executorService.submit(new Draw());
    }


    /**
     * 图像重绘线程
     */
    class Draw implements Runnable {
        public void run() {
            while (live.get()) {
                repaint();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 子弹移动的线程
     */
    static class MissileMove implements Runnable {
        public void run() {
            while (live.get()) {
                synchronized (missile) {
                    for (int i = missile.size() - 1; i >= 0; --i) {
                        if (live.get() && missile.get(i).Move()) {
                            missile.remove(i);
                        }
                    }
                }
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 绘制坦克和血条蓝条
     */
    private void paintTank(Graphics2D g2, Tank tank) {
        //血条和蓝条的高度
        int h = 5;
        g2.drawImage(array[2 + tank._direction + tank.id], tank.x, tank.y, width, height, null);
        g2.setColor(Color.RED);
        g2.draw3DRect(tank.x, tank.y + 1, HP, h, true);
        g2.fill3DRect(tank.x, tank.y + 1, tank.hp, h, true);
        g2.setColor(Color.BLUE);
        g2.draw3DRect(tank.x, tank.y + 1 + h, MP, h, true);
        g2.fill3DRect(tank.x, tank.y + 1 + h, tank.mp, h, true);
    }

    /**
     * 重绘函数
     */
    synchronized public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;

        //绘画墙体
        for (Wall wall : walls.values()) {
            g2.drawImage(array[wall.id], wall.x, wall.y, width, height, null);
        }

        //绘制坦克
        for (Tank tank : tanks.values()) {
            paintTank(g2, tank);
        }

        //子弹绘画
        for (Missile m : missile) {
            g2.drawImage(array[22], m.x, m.y, m.width, m.height, null);
        }


    }

    //缓存绘图
    synchronized public void update(Graphics g) {
        super.update(g);
        if (OffScreenImage == null)
            OffScreenImage = this.createImage(screenWidth, screenHeight);
        Graphics goffscrenn = OffScreenImage.getGraphics();    //设置一个内存画笔颜色为前景图片颜色
        Color c = goffscrenn.getColor();    //还是先保存前景颜色
        goffscrenn.setColor(Color.BLACK);    //设置内存画笔颜色为绿色
        goffscrenn.fillRect(0, 0, screenWidth, screenHeight);    //画成图片，大小为游戏大小
        goffscrenn.setColor(c);    //还原颜色
        g.drawImage(OffScreenImage, 0, 0, null);    //在界面画出保存的图片
        paint(goffscrenn);    //把内存画笔调用给paint
    }

    /**
     * 监听按键
     */
    private static class KeyBoardListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            int key = e.getKeyCode();
            //区分两种不同的按键
            //ASDWG为P2的按键
            //上下左右+SHITF为P1按键
            if (key < 65) {
                if (key != KeyEvent.VK_SHIFT && tanks.get(P1_TAG) != null) {
                    tanks.get(P1_TAG).key = key;
                    tanks.get(P1_TAG).move = true;
                }
                if (key == KeyEvent.VK_ESCAPE) {
                    ShutDown();
                }
            } else {
                if (key != KeyEvent.VK_G && tanks.get(P2_TAG) != null) {
                    switch (key) {
                        case KeyEvent.VK_W:
                            key = KeyEvent.VK_UP;
                            break;
                        case KeyEvent.VK_A:
                            key = KeyEvent.VK_LEFT;
                            break;
                        case KeyEvent.VK_S:
                            key = KeyEvent.VK_DOWN;
                            break;
                        case KeyEvent.VK_D:
                            key = KeyEvent.VK_RIGHT;
                            break;
                    }
                    tanks.get(P2_TAG).key = key;
                    tanks.get(P2_TAG).move = true;
                }
            }
        }

        public void keyReleased(KeyEvent e) {
            super.keyReleased(e);
            int key = e.getKeyCode();
            if (key < 65) {
                if (tanks.get(P1_TAG) != null) {
                    if (key != KeyEvent.VK_SHIFT && key == tanks.get(P1_TAG).key) {
                        tanks.get(P1_TAG).move = false;
                    } else {
                        tanks.get(P1_TAG).GetKey(key);
                    }
                }
            } else {
                switch (key) {
                    case KeyEvent.VK_W:
                        key = KeyEvent.VK_UP;
                        break;
                    case KeyEvent.VK_A:
                        key = KeyEvent.VK_LEFT;
                        break;
                    case KeyEvent.VK_S:
                        key = KeyEvent.VK_DOWN;
                        break;
                    case KeyEvent.VK_D:
                        key = KeyEvent.VK_RIGHT;
                        break;
                    case KeyEvent.VK_G:
                        key = KeyEvent.VK_SHIFT;
                        break;
                }
                if (null != tanks.get(P2_TAG)) {
                    if (key != KeyEvent.VK_SHIFT && key == tanks.get(P2_TAG).key) {
                        tanks.get(P2_TAG).move = false;
                    } else {
                        tanks.get(P2_TAG).GetKey(key);
                    }
                }
            }
        }
    }

    static void ShutDown() {
        Game.live.getAndSet(false);
        //停止所有的线程
        for (Tank tank : Game.tanks.values()) {
            tank.flag = false;
        }
        executorService.shutdown();
        executorService = Executors.newCachedThreadPool();
        Game.walls.clear();
        Game.missile.clear();
        Game.tanks.clear();
    }


}
