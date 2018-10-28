package spineware;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Loading extends JFrame{
    private final int x = 25, y = 25, w = 100, h = 100, arc_angle = 30;
    private int start_angle = 0;
    private final Panel panel;
    public Loading(){
        this.setTitle("SpineWare - Cargando...");
        this.setUndecorated(true);
        this.setResizable(false);
        this.setSize(150, 150);
        this.setLocationRelativeTo(null);
        this.setIconImage(Spineware.getIcon());
        panel = new Panel();
        this.setBackground(new Color(0, 0, 0, 0));
        this.getContentPane().add(panel);
        this.setVisible(true);
    }
    private class Panel extends JPanel{
        private final Timer timer;
        private final Color Colors[] = {new Color(25, 25, 25), new Color(50, 50, 50), new Color(75, 75, 75), new Color(100, 100, 100), new Color(125, 125, 125), new Color(150, 150, 150), new Color(175, 175, 175), new Color(200, 200, 200), new Color(225, 225, 225), new Color(250, 250, 250), Color.WHITE};
        public Panel(){
            timer = new Timer();
            TimerTask timer_task = new TimerTask(){
                @Override
                public void run(){
                    Panel.this.repaint();
                }
            };
            timer.schedule(timer_task, 500, 500);
            this.setOpaque(false);
        }
        public void kill(){
            timer.cancel();
            timer.purge();
        }
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            start_angle = start_angle == 360 ? 0 : start_angle+30;
            g.fillArc(x, y, w, h, start_angle, arc_angle);
            int i = 0;
            int total = Colors.length;
            while (i != total){
                g.setColor(Colors[i++]);
                g.fillArc(x, y, w, h, start_angle-(i*30), arc_angle);
            }
            g.setColor(Color.WHITE);
            g.drawString("Cargando...", 50, 140);
            g.setColor(Color.BLACK);
        }
    }
    @Override
    public void dispose(){
        panel.kill();
        super.dispose();
    }
}
