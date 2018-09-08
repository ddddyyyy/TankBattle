package com.mdy.game;

/**
 * 游戏的各种方块的抽象
 */
class Wall extends MyImage{

	int id;
	Wall(int x, int y, int id) {
		super(x, y);
		this.id=id;
	}
}
