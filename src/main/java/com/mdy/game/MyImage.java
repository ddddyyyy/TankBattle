package com.mdy.game;

import java.awt.Rectangle;

public class MyImage {
	int width = 60;
	int height = 60;
	public int x;
	public int y;

	MyImage(int x,int y){
		this.x=x;
		this.y=y;
	}
	Rectangle getRect(){
		return new Rectangle(x,y,width,height);
	}
	boolean isIntersects(MyImage other){
		return other.getRect().intersects(getRect());
	}
}
