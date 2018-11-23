package com.mdy.game;


/**
 * 寻路算法所需的结构体
 */
class Coordination {
	int x;
	int y;
	Coordination per;
	int direction;
	Coordination(int x, int y){
		this.x=x;
		this.y=y;
	}
}
