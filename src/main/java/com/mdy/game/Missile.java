package com.mdy.game;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JOptionPane;

import com.mdy.net.Clien;


public class Missile extends MyImage{
	private int direction;
	private int speed=10;
	private int damage=10;
	private int id;
	
	public Missile(int x,int y,int direction,int _id){
		super(x,y);
		height=17;
		width=17;
		this.direction=direction;
		id=_id;
	}
	synchronized public boolean isMeet(){
		for(int i=0;i<Game.wall.size();++i){
			if(Game.wall.get(i).isIntersects(this)){
				if(Game.wall.get(i).id==0){
					Game.wall.remove(i);
				}
				return true;
			}
			else{
				continue;
			}
		}
		for(int i=0;i<Game.tank.size();++i){
			if(Game.tank.get(i).isIntersects(this)){
				if(id!=Game.tank.get(i).id){
					Game.tank.get(i).hp-=damage;
				}
				if(Game.tank.get(i).hp<=0){
					if(Game.tank.get(i).id<12){
						Collection<Tank> coil = Game.ETank.values();
						Iterator<Tank> it = coil.iterator();
						while(it.hasNext()){
							if(it.next().equals(Game.tank.get(i))){
								Game.tank.get(i).flag=false;
								Game.tank.remove(i);
								it.remove();
								Game.init_ETank();
								break;
							}
						}
						
					}
					else{
						Game.tank.get(i).flag=false;
						if(Game.mode!=3){
							if(Game.tank.get(i).equals(Game.MyTank.getFirst())){
								Game.live=false;
								com.mdy.main.Main.live=false;
								Iterator<Tank> it = Game.tank.iterator();
								while(it.hasNext()){
									it.next().flag=false;
								}
								Game.isNotMove.clear();
								Game.ETank.clear();
								Game.MyTank.clear();
								Game.tank.clear();
								Game.wall.clear();
								Game.missile.clear();
							}
							else{
								if(Game.mode==4){
									Clien.delTank(Game.tank.get(i));
									Game.tank.remove(i);
									if(Game.tank.size()==1){
										JOptionPane.showMessageDialog(null,"you win!!!");
										com.mdy.main.Main.live=false;
										Game.live=false;
									}
								}
							}
						}
						else{
							Game.tank.remove(i);
							if(Game.tank.size()==1){
								JOptionPane.showMessageDialog(null,String.valueOf(Game.tank.getFirst().id)+"win!!");
								com.mdy.main.Main.live=false;
								Game.live=false;
							}
						}
					}
				}
				return true;
			}
			else{
				continue;
			}
		}
		return false;
	}
	public boolean Move(){
		if(direction==Game.UP){
			y-=speed;
			if(isMeet()){
				return true;
			}
		}
		if(direction==Game.DOWN){
			y+=speed;
			if(isMeet()){
				return true;
			}
		}
		if(direction==Game.LEFT){
			x-=speed;
			if(isMeet()){
				return true;
			}
		}
		if(direction==Game.RIGHT){
			x+=speed;
			if(isMeet()){
				return true;
			}
		}
		return false;
	}
}
