package com.mdy.game;

import javax.swing.JPanel;

import com.mdy.net.Server;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class Game extends JPanel {
	private static final long serialVersionUID = 3514701851303922198L;
	
	//客户端网络通讯所需变量
	Socket socket;
	static PrintWriter writer;
	
	public static int mode;
	static boolean live;
	private Graphics2D g2;
	private Image OffScrennImage;	
	public static Image array[] = new Image[23];
	//一般图像的大小
	private static final int width=60;
	private static final int height=60;
	//坦克的血量和弹药数
	static final int HP=60;
	static final int MP=60;
	//坦克的移动区域
	private int screenwidth=1200;
	private int screenheight = 900;
	//坦克的移动
	public static final int UP=3;
	public static final int DOWN=0;
	public static final int LEFT=1;
	public static final int RIGHT=2;
	//图像的位置
	public static int walls=0;
	public static int steels=1;
	public static int enemy1=0;
	public static int enemy2=4;
	public static int enemy3=8;	
	public static int play1=12;
	public static int play2=16;
	public static int tankmissile=22;
		
	static LinkedList<Rectangle> isNotMove = new LinkedList<>();
	
	static LinkedList<Missile> missile = new LinkedList<>();
	
	static LinkedList<Wall> wall = new LinkedList<>();
	
	public static LinkedList<Tank> MyTank = new LinkedList<>();
	public static LinkedList<Tank> tank = new LinkedList<>();
	static Map<Integer,Tank> ETank = new HashMap<>();
	//客户端的坦克
	public static Map<String,Tank> CNetTank = new HashMap<>();
	//服务端的坦克
	public static LinkedList<Tank> NetTank = new LinkedList<>();
	
	class Draw implements Runnable{
		public void run() {
			while(live){
				repaint();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}	

	class MissileMove implements Runnable{
		public void run() {
			while(live){
				synchronized ("") {
					for(int i=0;i<missile.size();++i){
						if(!missile.isEmpty()){
							if(missile.get(i).Move()&&live){
								missile.remove(i);
							}
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

	private class KeyBoardListener extends KeyAdapter{
		public void keyPressed(KeyEvent e){
			super.keyPressed(e);
			int key = e.getKeyCode();
			if(key<65){
				if(key!=KeyEvent.VK_SHIFT&&!MyTank.isEmpty()){
					MyTank.getFirst().key=key;
					MyTank.getFirst().move=true;
				}
			}
			else{
				if(key!=KeyEvent.VK_G&&!MyTank.isEmpty()){
					switch (key){
					case KeyEvent.VK_W:key = KeyEvent.VK_UP;break;
					case KeyEvent.VK_A:key = KeyEvent.VK_LEFT;break;
					case KeyEvent.VK_S:key = KeyEvent.VK_DOWN;break;
					case KeyEvent.VK_D:key = KeyEvent.VK_RIGHT;break;
					}
					MyTank.getLast().key=key;
					MyTank.getLast().move=true;
				}
			}
		}
		public void keyReleased(KeyEvent e){
			super.keyReleased(e);
			int key = e.getKeyCode();
			if(key<65){
				if(!MyTank.isEmpty()){
					if(key!=KeyEvent.VK_SHIFT&&key==MyTank.getFirst().key){
						MyTank.getFirst().move=false;
					}
					else{
						MyTank.getFirst().GetKey(key);
						if(mode==4){
							writer.println(Integer.toString(key));
						}
						if(mode==3){
							for(Socket s:Server.getSocket()){
								try {
									writer = new PrintWriter(s.getOutputStream(),true);
									writer.println("server"+" "+String.valueOf(key));
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}
					}
				}
			}
			else{
				switch (key){
				case KeyEvent.VK_W:key = KeyEvent.VK_UP;break;
				case KeyEvent.VK_A:key = KeyEvent.VK_LEFT;break;
				case KeyEvent.VK_S:key = KeyEvent.VK_DOWN;break;
				case KeyEvent.VK_D:key = KeyEvent.VK_RIGHT;break;
				case KeyEvent.VK_G:key = KeyEvent.VK_SHIFT;break;
				}
				if(!MyTank.isEmpty()){
					if(key!=KeyEvent.VK_SHIFT&&key==MyTank.getLast().key){
						MyTank.getLast().move=false;
					}
					else{
						MyTank.getLast().GetKey(key);
						if(mode==4){
							writer.println(Integer.toString(key));
						}
					}
				}
			}
		}
	}

	public static void init_ETank(){
		Zuobiao EB1 = new Zuobiao(height,width);
		Zuobiao EB2 = new Zuobiao(600,600);
		if(!ETank.containsKey(1)){
			Tank t = new Tank(EB2.x,EB2.y,UP,enemy1,20);
			ETank.put(1 , t);
			tank.add(t);
		}
		if(!ETank.containsKey(2)){
			Tank t = new Tank(400,400,DOWN,enemy2,20);
			ETank.put(2 , t);
			tank.add(t);
		}
		if(!ETank.containsKey(3)){
			Tank t = new Tank(300,300,DOWN,enemy3,20);
			ETank.put(3 , t);
			tank.add(t);
		}
		if(!ETank.containsKey(4)){
			Tank t = new Tank(EB1.x,EB1.y,DOWN,enemy3,20);
			ETank.put(4 , t);
			tank.add(t);
		}
	}
	
	public static void init_Tank(int mode){
		if(mode==4){
			Tank p1 = new Tank(300,100,DOWN,play1,0);
			p1.speed=20;
			tank.add(p1);
			MyTank.add(p1);
			return;
		}
		Tank p1 = new Tank(600,100,DOWN,play2,0);
		p1.speed=20;
		tank.add(p1);
		MyTank.add(p1);
		if(mode==2){
			Tank p2 = new Tank(300,100,DOWN,play1,0);
			p2.speed=20;
			tank.add(p2);
			MyTank.add(p2);
		}
	}
	
	public void init_map(){
		for(int i=0;i<screenwidth/width;++i){
			for(int j=0;j<screenheight/height-3;++j){
				if(i==0||i==screenwidth/width-1||j==0||j==screenheight/height-4){
					wall.add(new Wall(i*width,j*height,1));
				}
			}
		}
		
		for(int i=1;i<16;++i){
			Wall t = new Wall(60*i,540,i&1);
			wall.add(t);
			if((i&1)==1){
				isNotMove.add(t.getRect());
			}
		}
	}

	class send implements Runnable{
		public void run(){
			while(live){
				for(Socket s:Server.getSocket()){
					try {
						writer = new PrintWriter(s.getOutputStream(),true);
						writer.println("server"+" "+String.valueOf(MyTank.getFirst().x)+" "+String.valueOf(MyTank.getFirst().y)+" "+String.valueOf(MyTank.getFirst()._direction));
					} catch (IOException e) {
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
	
	synchronized public void paint(Graphics g){
		super.paint(g);
		g2 = (Graphics2D)g;
		Color c = g2.getColor();
		for(int i=0;i<wall.size();++i){
			g2.drawImage(array[0+wall.get(i).id],wall.get(i).x,wall.get(i).y,null);
		}
		if(mode==4){
			//writer = new PrintWriter(socket.getOutputStream(),true);
			writer.println(String.valueOf(MyTank.getFirst().x)+" "+String.valueOf(MyTank.getFirst().y)+" "+String.valueOf(MyTank.getFirst()._direction));
			//writer.close();
		}
		
		for(int i=0;i<tank.size();++i){
			if(!tank.isEmpty()){
				g2.drawImage(array[2+tank.get(i)._direction+tank.get(i).id],tank.get(i).x,tank.get(i).y,null);
				g2.setColor(Color.RED);
				g2.draw3DRect(tank.get(i).x, tank.get(i).y+5, HP, 5, true);
				g2.fill3DRect(tank.get(i).x, tank.get(i).y+5,tank.get(i).hp, 5, true);			
				g2.setColor(Color.BLUE);
				g2.draw3DRect(tank.get(i).x, tank.get(i).y+10, MP, 5, true);
				g2.fill3DRect(tank.get(i).x, tank.get(i).y+10,tank.get(i).mp, 5, true);			
				g2.setColor(c);
			}
		}
		for(int i=0;i<missile.size();++i){
			if(!missile.isEmpty())
			g2.drawImage(array[22],missile.get(i).x,missile.get(i).y,null);
		}
	}
	
	synchronized public void update(Graphics g) {
        super.update(g);
        if(OffScrennImage == null)
            OffScrennImage = this.createImage(screenwidth, screenheight);
        Graphics goffscrenn = OffScrennImage.getGraphics();    //设置一个内存画笔颜色为前景图片颜色
        Color c = goffscrenn.getColor();    //还是先保存前景颜色
        goffscrenn.setColor(Color.BLACK);    //设置内存画笔颜色为绿色
        goffscrenn.fillRect(0, 0, screenwidth, screenheight);    //画成图片，大小为游戏大小
        goffscrenn.setColor(c);    //还原颜色
        g.drawImage(OffScrennImage, 0, 0, null);    //在界面画出保存的图片
        paint(goffscrenn);    //把内存画笔调用给paint
    }

	public Game(int mode) throws InterruptedException {
		setForeground(Color.WHITE);
		setBackground(Color.BLACK);
		setBounds(0, 0, 1600, 900);	
		setLayout(null);
		Game.mode=mode;
		init_map();
		init_Tank(mode);
		addKeyListener(new KeyBoardListener());
		live=true;
		new Thread(new MissileMove()).start();
		new Thread(new Draw()).start();
		if(mode==1)
		init_ETank();
		if(mode==3){
			new Thread(new send()).start();
		}
	}
	
	public Game(int mode,Socket socket) {
		init_Tank(mode);
		try {
			writer = new PrintWriter(socket.getOutputStream(),true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.println(String.valueOf(MyTank.getFirst().x)+" "+String.valueOf(MyTank.getFirst().y)+" "+String.valueOf(MyTank.getFirst()._direction)+" "+String.valueOf(MyTank.getFirst().id)+" "+String.valueOf(MyTank.getFirst().pianyi));
		setForeground(Color.WHITE);
		setBackground(Color.BLACK);
		setBounds(0, 0, 1600, 900);	
		setLayout(null);
		Game.mode=mode;
		this.socket=socket;
		init_map();
		addKeyListener(new KeyBoardListener());
		live=true;
		new Thread(new MissileMove()).start();
		new Thread(new Draw()).start();
	}
}
