package com.mdy.game;


/**
 * 寻路算法所需的结构体
 */
class Zuobiao {
	int x;
	int y;
	Zuobiao per;
	int direction;
	Zuobiao(int x,int y){
		this.x=x;
		this.y=y;
	}
}
