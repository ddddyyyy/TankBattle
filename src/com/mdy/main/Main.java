package com.mdy.main;


import com.mdy.game.Game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Main extends JFrame {

    private JFrame play;
    private Game game = null;
    private com.mdy.net.Server server;
    private com.mdy.net.Clien clien;
    public static boolean live;
	private static int PlayTime=0;


	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
            try {
                Main frame = new Main();
                frame.setResizable(false);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
	}

    private void play(int mode){
		play = new JFrame("坦克大战    "+"游玩时间："+String.valueOf(PlayTime)+"s");
		live=true;
		play.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setVisible(false);
		try {
			if(mode<4){
				game = new Game(mode);
			}
			else if(mode==4){
				game = new Game(mode,clien.socket);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		play.setContentPane(game);
		play.setBounds(game.getBounds());
		play.setVisible(true);
		play.setResizable(false);
		game.requestFocus();
		new Thread(new CheckLive()).start();
	}

    private void init_image(){
		Game.array[0] = new ImageIcon(Main.class.getResource("/walls.gif")).getImage();
		Game.array[1] = new ImageIcon(Main.class.getResource("/steels.gif")).getImage();
		Game.array[2] = new ImageIcon(Main.class.getResource("/enemy1D.gif")).getImage();
		Game.array[3] = new ImageIcon(Main.class.getResource("/enemy1L.gif")).getImage();
		Game.array[4] = new ImageIcon(Main.class.getResource("/enemy1R.gif")).getImage();
		Game.array[5] = new ImageIcon(Main.class.getResource("/enemy1U.gif")).getImage();
		Game.array[6] = new ImageIcon(Main.class.getResource("/enemy2D.gif")).getImage();
		Game.array[7] = new ImageIcon(Main.class.getResource("/enemy2L.gif")).getImage();
		Game.array[8] = new ImageIcon(Main.class.getResource("/enemy2R.gif")).getImage();
		Game.array[9] = new ImageIcon(Main.class.getResource("/enemy2U.gif")).getImage();
		Game.array[10] = new ImageIcon(Main.class.getResource("/enemy3D.gif")).getImage();
		Game.array[11] = new ImageIcon(Main.class.getResource("/enemy3L.gif")).getImage();
		Game.array[12] = new ImageIcon(Main.class.getResource("/enemy3R.gif")).getImage();
		Game.array[13] = new ImageIcon(Main.class.getResource("/enemy3U.gif")).getImage();
		Game.array[14] = new ImageIcon(Main.class.getResource("/p1tankD.gif")).getImage();
		Game.array[15] = new ImageIcon(Main.class.getResource("/p1tankL.gif")).getImage();
		Game.array[16] = new ImageIcon(Main.class.getResource("/p1tankR.gif")).getImage();
		Game.array[17] = new ImageIcon(Main.class.getResource("/p1tankU.gif")).getImage();
		Game.array[18] = new ImageIcon(Main.class.getResource("/p2tankD.gif")).getImage();
		Game.array[19] = new ImageIcon(Main.class.getResource("/p2tankL.gif")).getImage();
		Game.array[20] = new ImageIcon(Main.class.getResource("/p2tankR.gif")).getImage();
		Game.array[21] = new ImageIcon(Main.class.getResource("/p2tankU.gif")).getImage();
		Game.array[22] = new ImageIcon(Main.class.getResource("/tankmissile.gif")).getImage();
	}

	class CheckLive implements Runnable{
		public void run(){
			while(live){
				play.setTitle("坦克大战    "+"游玩时间："+String.valueOf(PlayTime++)+"s");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(clien!=null){
				try {
					clien.socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			play.dispose();
			play.remove(game);
			play=null;
			game=null;
			setVisible(true);
		}
	}

    private Main() {
		init_image();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screensize.width-600)/3,(screensize.height-600)/3 ,600 , 600);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setForeground(Color.WHITE);
		panel.setBackground(Color.BLACK);
		getContentPane().add(panel);
		panel.setLayout(null);


		JLabel lblNewLabel = new JLabel("New label");
		lblNewLabel.setBackground(Color.WHITE);
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setForeground(Color.WHITE);
		lblNewLabel.setIcon(new ImageIcon(Main.class.getResource("/title.gif")));
		lblNewLabel.setBounds(10, 10, 577, 213);
		panel.add(lblNewLabel);

		JButton btnNewButton = new JButton("");
		btnNewButton.setBackground(Color.BLACK);
		btnNewButton.setBounds(224, 243, 144, 34);
		btnNewButton.addActionListener(e -> play(1));
		btnNewButton.setIcon(new ImageIcon(Main.class.getResource("/单人游戏.gif")));
		btnNewButton.setBorderPainted(false);
		panel.add(btnNewButton);

		JButton btnNewButton_1 = new JButton("");
		btnNewButton_1.setBounds(224, 298, 144, 34);
		btnNewButton_1.setIcon(new ImageIcon(Main.class.getResource("/双人游戏.gif")));
		btnNewButton_1.setBorderPainted(false);
		btnNewButton_1.addActionListener(e -> play(2));
		panel.add(btnNewButton_1);

		JButton btnNewButton_2 = new JButton("联网对战");
		btnNewButton_2.setEnabled(true);
		btnNewButton_2.addActionListener(arg0 -> {
            JDialog jd = new JDialog();
            jd.setBounds((getBounds().width-250),(getBounds().height-250) , 250, 250);
            jd.setVisible(true);
            JButton jb1 = new JButton("我是服务器");
            JButton jb2 = new JButton("我是客户端");
            jd.setLayout(new GridLayout(1,2,0,0));
            jd.add(jb1);
            jd.add(jb2);
            jb1.addActionListener(arg01 -> {
                try {
                    server = new com.mdy.net.Server();
                    play(3);
                    new Thread(server).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            jb2.addActionListener(e -> {
                try {
                    clien = new com.mdy.net.Clien();
                    play(4);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
        });
		btnNewButton_2.setBounds(224, 352 , 144, 34);
		panel.add(btnNewButton_2);
	}
}
