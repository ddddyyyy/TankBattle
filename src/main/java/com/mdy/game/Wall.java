package com.mdy.game;

/**
 * 游戏的各种方块的抽象
 */
class Wall extends MyImage{

    //识别为那种方块的id
	int id;

	Wall(Coord coord,int id){
		super(coord);
		this.id = id;
	}
}
