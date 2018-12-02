package com.mdy.main;


import com.mdy.game.Game;
import com.mdy.game.Mode;

import javax.swing.*;
import java.awt.*;

/**
 * 选择模式界面，主界面
 */
public class Main extends JFrame {

    private JFrame play;
    private Game game = null;
    private static int PlayTime = 0;


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

    private void play(Mode mode) {
        play = new JFrame("Live" + ":" + String.valueOf(PlayTime++) + "s");
        play.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(false);
        game = new Game(mode);
        play.setContentPane(game);
        play.setBounds(game.getBounds());
        play.setVisible(true);
        play.setResizable(false);
        game.requestFocus();
        new Thread(new CheckLive()).start();
    }


    class CheckLive implements Runnable {
        public void run() {
            while (Game.live) {
                play.setTitle("TankBattle" + " Live" + String.valueOf(PlayTime++) + "s");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            game.removeAll();
            play.remove(game);
            play.dispose();
            play = null;
            game = null;
            setVisible(true);
        }
    }

    private Main() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 600) / 3, (screenSize.height - 600) / 3, 600, 600);
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
        lblNewLabel.setIcon(new ImageIcon(Main.class.getResource("/img/title.gif")));
        lblNewLabel.setBounds(10, 10, 577, 213);
        panel.add(lblNewLabel);

        JButton btnNewButton = new JButton("");
        btnNewButton.setBackground(Color.BLACK);
        btnNewButton.setBounds(224, 243, 144, 34);
        btnNewButton.setIcon(new ImageIcon(Main.class.getResource("/img/SinglePlayer.gif")));
        btnNewButton.addActionListener(e -> play(Mode.Single));
        btnNewButton.setBorderPainted(false);
        panel.add(btnNewButton);

        JButton btnNewButton_1 = new JButton("");
        btnNewButton_1.setBounds(224, 298, 144, 34);
        btnNewButton_1.setIcon(new ImageIcon(Main.class.getResource("/img/DoublePlayer.gif")));
        btnNewButton_1.setBorderPainted(false);
        btnNewButton_1.addActionListener(e -> play(Mode.Double));
        panel.add(btnNewButton_1);
    }
}
