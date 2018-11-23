package com.mdy.net;

import com.mdy.game.Game;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;


public class Server implements Runnable{
	private ServerSocket server;
	private Socket s;
	private static LinkedList<Socket> socket = new LinkedList<>();
	public Server() throws IOException{
		server = new ServerSocket(6666);
		server.setReuseAddress(true);
		System.out.println("服务器启动成功");
	}
	
	public static void createTank(String string){
		for(int i=0;i<getSocket().size()-1;++i){
			try {
				PrintWriter writer = new PrintWriter(getSocket().get(i).getOutputStream(),true);
				writer.println("create"+" "+String.valueOf(getSocket().get(i).getPort())+" "+string);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void run(){
		while(!server.isClosed()){
			try {
				s = server.accept();
				getSocket().add(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(String.valueOf(s.getPort())+"连接");
			try {
					PrintWriter _writer = new PrintWriter(s.getOutputStream(),true);
					String t = String.valueOf(Game.MyTank.getFirst().x)+" "+String.valueOf(Game.MyTank.getFirst().y)+" "+String.valueOf(Game.MyTank.getFirst()._direction)+" "+String.valueOf(Game.MyTank.getFirst().id)+" "+String.valueOf(Game.MyTank.getFirst().offset);
					_writer.println("create"+" "+"server"+" "+t);
			} catch (IOException e) {
				e.printStackTrace();
			}
			new Thread(new com.mdy.game.NetTank(s)).start();
		}
	}

	public static LinkedList<Socket> getSocket() {
		return socket;
	}

}
