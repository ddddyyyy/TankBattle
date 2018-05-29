package com.mdy.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import com.mdy.game.Game;


public class Server implements Runnable{
	private ServerSocket server;
	Socket s;
	BufferedReader reader;
	private static LinkedList<Socket> socket = new LinkedList<>();
	public Server() throws IOException{
		server = new ServerSocket(6666);
		server.setReuseAddress(true);
		System.out.println("�������׽����Ѵ���");
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
	
	class checklive implements Runnable{
		Socket _socket;
		public checklive(Socket s){
			this._socket=s;
		}
		public void run(){
			while(!_socket.isClosed()){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.out.println("�Ͽ�������");
					break;
				}
			}
			getSocket().remove(_socket);
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
			System.out.println(String.valueOf(s.getPort())+"������");
			try {
					PrintWriter _writer = new PrintWriter(s.getOutputStream(),true);
					String t = String.valueOf(Game.MyTank.getFirst().x)+" "+String.valueOf(Game.MyTank.getFirst().y)+" "+String.valueOf(Game.MyTank.getFirst()._direction)+" "+String.valueOf(Game.MyTank.getFirst().id)+" "+String.valueOf(Game.MyTank.getFirst().pianyi);
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

	public static void setSocket(LinkedList<Socket> socket) {
		Server.socket = socket;
	}
}
