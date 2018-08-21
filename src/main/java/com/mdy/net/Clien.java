package com.mdy.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;

import com.mdy.game.Game;
import com.mdy.game.Tank;


public class Clien{ 
	public Socket socket;
	String str;
	String[] string;
	BufferedReader reader;
	
	public static void delTank(Tank tank){
		Collection<Tank> coil;
		coil = Game.CNetTank.values();
		Iterator<Tank> it = coil.iterator();
		while(it.hasNext()){
			if(it.equals(tank)){
				it.remove();
				break;
			}
		}
		return;
	}
	
	public Clien() throws IOException{
		socket = new Socket("localhost",6666);
		System.out.println("连接成功");
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		new Thread(()->{
			while(!socket.isClosed()){
				try {
					str = reader.readLine();
					string = str.split(" ");
					if(string[0].equals("create")){
						Tank tank = new Tank(Integer.parseInt(string[2]), Integer.parseInt(string[3]),Integer.parseInt(string[4]), Integer.parseInt(string[5]), Integer.parseInt(string[6]));
						Game.CNetTank.put(string[1],tank);
						Game.tank.add(tank);
					}
					else{
						if(string.length>2){
							Game.CNetTank.get(string[0]).x=Integer.parseInt(string[1]);
							Game.CNetTank.get(string[0]).y=Integer.parseInt(string[2]);
							Game.CNetTank.get(string[0])._direction=Integer.parseInt(string[3]);
						}
						else{
							Game.CNetTank.get(string[0]).GetKey(Integer.parseInt(string[1]));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Client出错");
					break;
				}
			}
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
}
