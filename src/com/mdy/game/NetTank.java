package com.mdy.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.mdy.net.Server;

public class NetTank implements Runnable{
	Socket socket;
	private Tank tank;
	private String str;
	private String[] string;
	BufferedReader reader; 
	public NetTank(Socket socket){
		this.socket=socket;
		{
			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				str = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("NetTank出错");
			}
			Server.createTank(str);
			string = str.split(" ");
			tank = new Tank(Integer.parseInt(string[0]), Integer.parseInt(string[1]),Integer.parseInt(string[2]), Integer.parseInt(string[3]), Integer.parseInt(string[4]));
			Game.NetTank.add(tank);
			Game.tank.add(tank);
		}
		System.out.println("创建成功");
	}
	
	public void run(){//客户端移动
		while(tank.flag){
			try {
				str = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			string = str.split(" ");
			if(string.length==1){
				int key = Integer.parseInt(str);
				tank.GetKey(key);
			}
			else{
				tank.x=Integer.parseInt(string[0]);
				tank.y=Integer.parseInt(string[1]);
				tank._direction=Integer.parseInt(string[2]);
			}
		}
		Game.NetTank.remove(tank);
		Game.tank.remove(tank);
	}
}