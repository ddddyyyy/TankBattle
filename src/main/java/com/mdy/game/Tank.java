package com.mdy.game;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Tank extends MyImage implements Runnable{
	private boolean direction[]={false,false,false,false};
	public int _direction;
	public int id;

	public int pianyi;
	//敌人坦克的速度
	int speed=15;
	int per_x;
	int per_y;
    //坦克的血量
    int hp=Game.HP;
    //坦克的射的MP
    int mp=Game.MP;
	int key;

	boolean flag = true;
	boolean move = false;

	private LinkedList<Zuobiao> IsMove = new LinkedList<>();
	private LinkedList<Integer> Path = new LinkedList<>();
	private LinkedList<Integer> _Path = new LinkedList<>();

	class ETankMove implements Runnable{
		public void run(){
			while(flag){
				ETankMove();
				try {
					Thread.sleep(60);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class MyTankMove implements Runnable{
		public void run(){
			while(flag){
				while(move){
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

	synchronized public void ETankMove(){
		int n=0;
		int arr[]={37,38,39,40,16};//L U R D S
		if(!_Path.isEmpty()){
			switch(_Path.getLast()){
				case Game.UP:n=1;break;
				case Game.LEFT:n=0;break;
				case Game.RIGHT:n=2;break;
				case Game.DOWN:n=3;break;
			}
			if(_Path.getLast()!=_direction)
				GetKey(arr[n]);
			GetKey(arr[n]);
			if(!_Path.isEmpty()){
				_Path.removeLast();
			}
		}
		return;
	}

	/**
	 * 使用广度遍历算法，使用队列存储遍历的节点
	 * @param ax 坦克的X坐标
	 * @param ay 坦克的Y坐标
	 * @return 移动的路径
	 */
	private synchronized LinkedList<Integer> MiGong(int ax, int ay){
		Queue<Zuobiao> d_q = new LinkedList<>();
		d_q.offer(new Zuobiao(x,y));
		Zuobiao last = null;
		while(!d_q.isEmpty()){
			Zuobiao t = d_q.poll();
			int tx = t.x;
			int ty = t.y;
			int i;
			//遍历所有的方向
			for(i=0;i<4;++i){
				switch(i){
					case Game.UP: ty-=speed;break;
					case Game.LEFT: tx-=speed;break;
					case Game.RIGHT: tx+=speed;break;
					case Game.DOWN: ty+=speed;break;
				}
				boolean flag=true;
				//这里关于坐标的计算，需要结合目标的大小进行计算
				if(60<=tx&&tx<=1580&&60<=ty&&ty<=600){
					Rectangle r1 = new Rectangle(tx, ty, 60, 60);
					for(int n=0;n<Game.isNotMove.size();++n){
						if(r1.intersects(Game.isNotMove.get(n))){
							ty=t.y;
							tx=t.x;
							flag = false;
							break;
						}
					}
					Zuobiao z = new Zuobiao(tx,ty);
					for (Zuobiao aIsMove : IsMove) {
						if (aIsMove.x == z.x && aIsMove.y == z.y) {
							flag = false;
							break;
						}
					}
					if(flag){
						d_q.offer(z);
						IsMove.add(z);
						z.per=t;
						z.direction=i;
						last=z;
					}
					if(ax-pianyi<=z.x&&z.x<=ax+pianyi&&ay-pianyi<=z.y&&z.y<=ay+pianyi){
						break;
					}
				}
				tx=t.x;
				ty=t.y;
			}
			if(i!=4){
				break;
			}
		}
		while(last.per!=null){
			Path.add(last.direction);
			last=last.per;
		}
		return Path;
	}

	public Tank(int x, int y,int direction,int id,int pianyi) {
		super(x,y);
		per_x=x;
		per_y=y;
		this.direction[direction]=true;
		this._direction=direction;
		this.id=id;
		this.pianyi=pianyi;
		if(id<12){
			new Thread(this).start();
			new Thread(new Ai()).start();
			new Thread(new ETankMove()).start();
		}
		else{
			new Thread(new MyTankMove()).start();
		}
		new Thread(new TankMpRecover()).start();
	}
	private boolean isMoveable(){
		for(int i=0;i<Game.wall.size();++i){
			if(Game.wall.get(i).isIntersects(this)){
				if(Game.wall.get(i).id==0&&id<12){//电脑自动攻击
					this.GetKey(16);
				}
				else if(Game.wall.get(i).id==1){
				}
				return false;
			}
			else{
			}
		}
		for(int i=0;i<Game.tank.size();++i){
			if(Game.tank.get(i).isIntersects(this)&&!this.equals(Game.tank.get(i))){
				if(Game.tank.get(i).id>=12){
					GetKey(16);
				}
				return false;
			}
			else{

			}
		}
		return true;
	}
	public void GetKey(int n){
		int t_x=x;
		int t_y=y;
		per_y=y;
		per_x=x;
		if(n==KeyEvent.VK_UP){
			y-=speed;
			per_y-=speed/2;
			if(direction[Game.UP]&&isMoveable()){
				return;
			}
			else{
				y=t_y;
				per_y=y;
				if(!direction[Game.UP]){
					direction[Game.UP]=true;
					direction[_direction]=false;
					_direction=Game.UP;
				}
				else{
					return;
				}
			}
		}
		if(n==KeyEvent.VK_DOWN){
			y+=speed;
			per_y+=speed/2;
			if(direction[Game.DOWN]&&isMoveable()){
				return;
			}
			else{
				y=t_y;
				if(!direction[Game.DOWN]){
					direction[Game.DOWN]=true;
					direction[_direction]=false;
					_direction=Game.DOWN;
				}
				else{
					return;
				}
			}
		}
		if(n==KeyEvent.VK_LEFT){
			x-=speed;
			per_x-=speed/2;
			if(direction[Game.LEFT]&&isMoveable()){
				return;
			}
			else{
				x=t_x;
				per_x=x;
				if(!direction[Game.LEFT]){
					direction[Game.LEFT]=true;
					direction[_direction]=false;
					_direction=Game.LEFT;
				}
				else{
					return;
				}
			}
		}
		if(n==KeyEvent.VK_RIGHT){
			x+=speed;
			per_x+=speed;
			if(direction[Game.RIGHT]&&isMoveable()){
				return;
			}
			else{
				x=t_x;
				per_x=x;
				if(!direction[Game.RIGHT]){
					direction[Game.RIGHT]=true;
					direction[_direction]=false;
					_direction=Game.RIGHT;
				}
				else{
					return;
				}
			}
		}
		/*if(per_x!=x||per_y!=y&&Game.mode==4){
			Game.writer.println(String.valueOf(x)+" "+String.valueOf(y)+" "+String.valueOf(_direction));
		}*/
		if(n==KeyEvent.VK_SHIFT&&mp>0){//子弹的初始坐标自己算
			synchronized ("") {
				mp-=10;
			}
			if(_direction==Game.UP)
				Game.missile.add(new Missile(x+21,y-10,_direction,id));
			if(_direction==Game.DOWN)
				Game.missile.add(new Missile(x+20,y+60,_direction,id));
			if(_direction==Game.LEFT)
				Game.missile.add(new Missile(x-17,y+20,_direction,id));
			if(_direction==Game.RIGHT)
				Game.missile.add(new Missile(x+60,y+20,_direction,id));
		}
	}

	class Ai implements Runnable{
		public void run(){
			while(!Game.MyTank.isEmpty()&&flag){
				//synchronized ("") 
				{
					Path.clear();
					IsMove.clear();
					_Path=MiGong(Game.MyTank.getFirst().x, Game.MyTank.getFirst().y);
				}
				try {
					Thread.sleep(1200);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

	class TankMpRecover implements Runnable{
		public void run(){
			while(flag){
				synchronized ("") {
					if(mp<Game.MP)
						mp+=10;
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void run(){
		Random r = new Random();
		while(flag){
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
