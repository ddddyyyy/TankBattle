package com.mdy.game;

import java.awt.Rectangle;

public class MyImage {
	public  int width = 60;
	public  int height = 60;
	public int x;
	public int y;
	
	public MyImage(int x,int y){
		this.x=x;
		this.y=y;
	}
	public Rectangle getRect(){
		return new Rectangle(x,y,width,height);
	}
	boolean isIntersects(MyImage other){
		if(other.getRect().intersects(getRect())){
			return true;
		}
		else{
			return false;
		}
	}
}
