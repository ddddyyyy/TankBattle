package com.mdy.game;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * 使用代码生成游戏所需贴图。
 */
public final class TextureFactory {

    private TextureFactory() {
    }

    public static Image createWallTile(int width, int height) {
        BufferedImage image = rgb(width, height);
        Graphics2D g2 = image.createGraphics();
        quality(g2);

        g2.setPaint(new GradientPaint(0, 0, new Color(199, 113, 90), width, height, new Color(150, 73, 56)));
        g2.fillRect(0, 0, width, height);

        int rowHeight = Math.max(5, height / 4);
        int colWidth = Math.max(8, width / 3);
        g2.setColor(new Color(97, 50, 38));
        for (int y = 0; y < height; y += rowHeight) {
            int offset = ((y / rowHeight) % 2 == 0) ? 0 : colWidth / 2;
            g2.drawLine(0, y, width, y);
            for (int x = -offset; x < width; x += colWidth) {
                g2.drawLine(x + offset, y, x + offset, Math.min(height, y + rowHeight));
            }
        }
        g2.dispose();
        return image;
    }

    public static Image createSteelTile(int width, int height) {
        BufferedImage image = rgb(width, height);
        Graphics2D g2 = image.createGraphics();
        quality(g2);

        g2.setPaint(new GradientPaint(0, 0, new Color(204, 210, 216), width, height, new Color(113, 125, 139)));
        g2.fillRect(0, 0, width, height);

        g2.setColor(new Color(90, 96, 104, 180));
        for (int x = 0; x < width; x += Math.max(4, width / 8)) {
            g2.drawLine(x, 0, x, height);
        }

        int rivet = Math.max(3, width / 9);
        g2.setColor(new Color(70, 74, 80));
        g2.fillOval(2, 2, rivet, rivet);
        g2.fillOval(width - rivet - 2, 2, rivet, rivet);
        g2.fillOval(2, height - rivet - 2, rivet, rivet);
        g2.fillOval(width - rivet - 2, height - rivet - 2, rivet, rivet);

        g2.dispose();
        return image;
    }

    public static Image createTankSprite(int width, int height, int direction, Color body, Color cannon, Color outline) {
        BufferedImage image = argb(width, height);
        Graphics2D g2 = image.createGraphics();
        quality(g2);

        int trackW = Math.max(6, width / 5);
        g2.setColor(new Color(55, 55, 55));

        if (direction == Game.LEFT || direction == Game.RIGHT) {
            g2.fillRoundRect(2, 2, width - 4, trackW, 4, 4);
            g2.fillRoundRect(2, height - trackW - 2, width - 4, trackW, 4, 4);
        } else {
            g2.fillRoundRect(2, 2, trackW, height - 4, 4, 4);
            g2.fillRoundRect(width - trackW - 2, 2, trackW, height - 4, 4, 4);
        }

        g2.setColor(body);
        int bodyX = width / 4;
        int bodyY = height / 4;
        int bodyW = width / 2;
        int bodyH = height / 2;
        g2.fillRoundRect(bodyX, bodyY, bodyW, bodyH, 10, 10);

        g2.setColor(body.brighter());
        g2.fillOval(width / 3, height / 3, width / 3, height / 3);

        g2.setStroke(new BasicStroke(Math.max(3f, width / 9f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(cannon);
        switch (direction) {
            case Game.DOWN:
                g2.drawLine(width / 2, height / 2, width / 2, height - 3);
                break;
            case Game.LEFT:
                g2.drawLine(width / 2, height / 2, 3, height / 2);
                break;
            case Game.RIGHT:
                g2.drawLine(width / 2, height / 2, width - 3, height / 2);
                break;
            default:
                g2.drawLine(width / 2, height / 2, width / 2, 3);
                break;
        }

        g2.setColor(outline);
        g2.drawRoundRect(bodyX, bodyY, bodyW, bodyH, 10, 10);
        g2.dispose();
        return image;
    }

    public static Image createMissileSprite(int width, int height) {
        BufferedImage image = argb(width, height);
        Graphics2D g2 = image.createGraphics();
        quality(g2);

        g2.setColor(new Color(255, 228, 130));
        g2.fillOval(0, 0, width, height);
        g2.setColor(new Color(255, 120, 70));
        g2.fillOval(width / 3, height / 4, width / 2, height / 2);
        g2.setColor(new Color(180, 70, 50));
        g2.drawOval(0, 0, width - 1, height - 1);

        g2.dispose();
        return image;
    }

    public static Icon createTitleIcon(int width, int height) {
        BufferedImage image = argb(width, height);
        Graphics2D g2 = image.createGraphics();
        quality(g2);

        g2.setPaint(new GradientPaint(0, 0, new Color(22, 30, 45), 0, height, new Color(7, 10, 18)));
        g2.fillRect(0, 0, width, height);

        g2.setPaint(new GradientPaint(0, 0, new Color(255, 214, 92), width, 0, new Color(255, 120, 65)));
        g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(36, height / 4)));
        String text = "TANK BATTLE";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (width - fm.stringWidth(text)) / 2;
        int ty = (height + fm.getAscent()) / 2 - 14;
        g2.drawString(text, tx, ty);

        g2.setFont(new Font("SansSerif", Font.PLAIN, Math.max(16, height / 10)));
        g2.setColor(new Color(200, 225, 255));
        String sub = "Classic Arcade";
        FontMetrics sm = g2.getFontMetrics();
        g2.drawString(sub, (width - sm.stringWidth(sub)) / 2, ty + 34);

        g2.dispose();
        return new ImageIcon(image);
    }

    public static Icon createMenuButtonIcon(int width, int height, String text, Color background) {
        BufferedImage image = argb(width, height);
        Graphics2D g2 = image.createGraphics();
        quality(g2);

        Shape button = new RoundRectangle2D.Float(0, 0, width - 1, height - 1, 12, 12);
        g2.setPaint(new GradientPaint(0, 0, background.brighter(), 0, height, background.darker()));
        g2.fill(button);
        g2.setColor(new Color(30, 30, 30, 170));
        g2.draw(button);

        g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(16, height / 2 + 2)));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (width - fm.stringWidth(text)) / 2;
        int ty = (height - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(Color.WHITE);
        g2.drawString(text, tx, ty);

        g2.dispose();
        return new ImageIcon(image);
    }

    private static BufferedImage rgb(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    private static BufferedImage argb(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    private static void quality(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
}
