package com.projetbdd;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;

public class GradientHeader extends JPanel {
    private static boolean isDarkTheme = false;

    public static void setDarkTheme(boolean dark) {
        isDarkTheme = dark;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color startColor, endColor;
        if (isDarkTheme) {
            startColor = new Color(50, 100, 180);
            endColor = new Color(30, 70, 150);
        } else {
            startColor = new Color(19, 72, 171);
            endColor = new Color(42, 130, 228);
        }

        GradientPaint gradient = new GradientPaint(
                0, 0, startColor,
                getWidth(), getHeight(), endColor);
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 0, 0);
        g2.dispose();
    }
}
