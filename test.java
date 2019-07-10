import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Event;
import java.awt.event.*;
import java.awt.Font;
import java.util.Random;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;

class Gravity {
    private static Random rand = new Random();
    private static double acceleration = 0.00025;
    private static int time = 0;
    private static int sign = 1;

    static void applyGravity(Boule b, double time) {
        Gravity.time += time;
        if(Gravity.time > 100_000) {
            sign *= -1;
            Gravity.time = 0;
        }
        double v = b.getVelocity();
        double btheta = b.getTheta();
        double vbx = v*Math.sin(btheta);
        double vby = -v*Math.cos(btheta);
        if (b.getY() < 2*Boule.size) {
            vbx += sign*acceleration*time;
        }
        else if (b.getY() > 5*Boule.size) {
            vbx -= sign*acceleration*time;
        }
        if (b.getX() < 2*Boule.size) {
            vby -= sign*acceleration*time;
        }
        else if (b.getX() > 7*Boule.size) {
            vby += sign*acceleration*time;
        }
        else
            return;

        v = Math.sqrt(vbx*vbx+vby*vby);
        btheta = Math.atan2(vbx, -vby);
        b.setVelocity(v);
        b.setTheta(btheta);
    }
}

class Boule {

    public static final int size = 150;

    private double x;
    private double y;
    private double velocity;
    private double theta;
    private int nHits = 0;

    private Color color;

    public Boule(int xmax, int ymax) {
        velocity = 0.3;
        Random rand = new Random();
        x = rand.nextInt(xmax-size);
        y = rand.nextInt(ymax-size);
        theta = Math.random()*2*Math.PI-Math.PI;
        resetColor();
    }

    public void hit() {
        ++nHits;
    }

    public int getNHits() {
        return nHits;
    }

    public void resetColor() {
        Random rand = new Random();
        int indexColor = rand.nextInt(6);
        switch(indexColor) {
            case 0:  color = Color.blue;    break;
            case 1:  color = Color.green;   break;
            case 2:  color = Color.yellow;  break;
            case 3:  color = Color.orange;  break;
            case 4:  color = Color.magenta; break;
            case 5:  color = Color.cyan;    break;
            default: color = Color.black;
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public double getTheta() {
        return theta;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color t_color) {
        this.color = t_color;
    }

    public void move(double time) {
        double deltaX = velocity*time*Math.sin(theta);
        double deltaY = -velocity*time*Math.cos(theta);
        x += deltaX;
        y += deltaY;
        if (velocity > 0.3) {
            velocity = 0.3 + (0.99)*(velocity-0.3);
        }
    }

    public void bounceX(int xmax) {
        theta = -theta;
        if (x < 0)
            x = 0;
        else
            x = xmax-size;
    }

    public void bounceY(int ymax) {
        theta = Math.PI-theta;
        if (y < 0)
            y = 0;
        else
            y = ymax-size;
    }
}
class Game{
    int nBoules;
    int xmax;
    int ymax;
    private ArrayList<Boule> boules;
    Histo histo;

    public Game(int nBoules, int xmax, int ymax) {
        this.nBoules = nBoules;
        this.xmax = xmax;
        this.ymax = ymax;
        boules = new ArrayList<Boule>();
        for (int i = 0; i != nBoules; ++i)
            boules.add(new Boule(this.xmax, this.ymax));
        boules.get(0).setColor(Color.red);
        histo = new Histo(boules);
    }

    private void testBounceX(Boule b) {
        double x = b.getX();
        double theta = b.getTheta();
        double pi = Math.PI;
        if (x < 0) 
            b.bounceX(xmax);
        else if (x > xmax - Boule.size)
            b.bounceX(xmax);
    }

    private void testBounceY(Boule b) {
        double y = b.getY();
        double theta = b.getTheta();
        double pi = Math.PI;
        if (y < 0)  
            b.bounceY(ymax);
        else if (y > ymax - Boule.size) 
            b.bounceY(ymax);
    }

    private double distance(Boule b1, Boule b2) {
        return Math.sqrt(Math.pow(b1.getX() - b2.getX(), 2)
                       + Math.pow(b1.getY() - b2.getY(), 2));
    }

    private double scalar(double x1, double y1, double x2, double y2) {
        return x1*x2 + y1*y2;
    }

    private void collision(Boule b1, Boule b2) {

        double x1 = b1.getX();
        double y1 = b1.getY();
        double v1 = b1.getVelocity();
        double theta1 = b1.getTheta();
        double vx1 = v1*Math.sin(theta1);
        double vy1 = -v1*Math.cos(theta1);

        double x2 = b2.getX();
        double y2 = b2.getY();
        double v2 = b2.getVelocity();
        double theta2 = b2.getTheta();
        double vx2 = v2*Math.sin(theta2);
        double vy2 = -v2*Math.cos(theta2);

        double r = distance(b1, b2);
        double rx = x2 - x1;
        double ry = y2 - y1;

        double vrx = vx1 - vx2;
        double vry = vy1 - vy2;
        double value = scalar(vrx, vry, rx, ry)/Math.pow(r,2);
        if (value <= 0)
            return;

        if(b1.getColor() == Color.red || b2.getColor() == Color.red) {
            if (b1.getColor() == Color.red)
                b1.hit();
            else
                b2.hit();
            Color foo = b1.getColor();
            b1.setColor(b2.getColor());
            b2.setColor(foo);
        }

        double Etot = Math.pow(vx1, 2) + Math.pow(vx2, 2)
                    + Math.pow(vy1, 2) + Math.pow(vy2, 2);
        vx1 -= value*rx;
        vy1 -= value*ry;
        vx2 += value*rx;
        vy2 += value*ry;
        double ratioE = Math.pow(vx1, 2) + Math.pow(vx2, 2)
                      + Math.pow(vy1, 2) + Math.pow(vy2, 2);
        ratioE = ratioE / Etot;

        double theta1_new = Math.atan2(vx1, -vy1);
        double theta2_new = Math.atan2(vx2, -vy2);
        b1.setTheta(theta1_new);
        b2.setTheta(theta2_new);

        b1.setVelocity(Math.sqrt(1/ratioE*scalar(vx1, vy1, vx1, vy1)));
        b2.setVelocity(Math.sqrt(1/ratioE*scalar(vx2, vy2, vx2, vy2)));
    }

    private void testCollisions() {
        for (int i = 0; i != boules.size()-1; ++i)  {
            Boule b1 = boules.get(i);
            for (int j = i+1; j != boules.size(); ++j)  {
                Boule b2 = boules.get(j);
                if (distance(b1, b2) < Boule.size)
                    collision(b1, b2);
            }
        }
    }

    public void setXmax(int xmax) {
        this.xmax = xmax;
    }

    public void setYmax(int ymax) {
        this.ymax = ymax;
    }

    public void resetColor() {
        for (int i = 0; i != boules.size(); ++i) {
            Boule b = boules.get(i);
            b.resetColor();
        }
    }

    public void mouseEvent(int x, int y) {
        for (int i = 0; i != boules.size(); ++i) {
            Boule b = boules.get(i);
            double distance = Math.sqrt(
                    Math.pow(x - (b.getX()+Boule.size/2), 2)
                  + Math.pow(y - (b.getY()+Boule.size/2), 2)
                    );
            if (distance < Boule.size) {
                b.resetColor();
                return;
            }
        }
    }

    public void update(double time) {
        for (int i = 0; i != boules.size(); ++i) {
            Boule b = boules.get(i);
            b.move(time);
            testBounceX(b);
            testBounceY(b);
            Gravity.applyGravity(b, time);
        }
        testCollisions();
    }

    public void paint(Graphics g) {
        for (int i = 0; i != boules.size(); ++i) {
            Boule b = boules.get(i);
            g.setColor(b.getColor());
            g.fillOval((int)b.getX(), (int)b.getY(), Boule.size, Boule.size);
            g.setColor(Color.white);
            g.drawOval((int)b.getX(), (int)b.getY(), Boule.size, Boule.size);
        }
        int width = 500;
        int height = 300;
        histo.paint(g, xmax-width, ymax-height-50, width, height);
    }
}

class Histo {
    private ArrayList<Boule> boules;

    Histo(ArrayList<Boule> boules) {
        this.boules = boules;
    }

    public void paint(Graphics g, int x, int y, int width, int height) {
        g.setColor(Color.black);
        // g.fillRect(x, y, width, height);
        g.setColor(Color.white);
        g.drawLine(x+1, y, x+1, y+height);
        g.drawLine(x, y+height-1, x+width, y+height-1);
        g.setColor(Color.red);
        int widthBin = (width - 3) / boules.size();
        int posX = 2;
        int nmax = 0;
        for (int i = 0; i != boules.size(); ++i) 
            if (boules.get(i).getNHits() > nmax)
                nmax = boules.get(i).getNHits();
        int stepY = (int)((height / nmax)*0.8);
        int offsetX = x;
        int offsetY = y + height;
        for (int i = 0; i != boules.size(); ++i) {
            int n = boules.get(i).getNHits();

            // g.drawLine(offsetX+posX, offsetY,
            //            offsetX+posX, offsetY-n*stepY);

            // g.drawLine(offsetX+posX, offsetY-n*stepY,
            //            offsetX+posX+widthBin, offsetY-n*stepY);

            // g.drawLine(offsetX+posX+widthBin, offsetY-n*stepY,
            //            offsetX+posX+widthBin, offsetY);

            g.setColor(Color.red);
            g.fillRect(offsetX+posX, offsetY-n*stepY,
                       widthBin, n*stepY);
            g.setColor(Color.white);
            g.drawRect(offsetX+posX, offsetY-n*stepY,
                       widthBin, n*stepY);

            posX += widthBin;
        }
        g.setColor(Color.white);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
        g.drawString("Max = " + nmax, x + width/2, y+10);
    }
}

@SuppressWarnings("serial") 
class Panneau extends JPanel {
    Game game;

    public void setGame(Game g) {
        game = g;
    }

    public void paintComponent(Graphics g){
        g.setColor(Color.black);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        game.paint(g);
    }
}

@SuppressWarnings("serial")
class Fenetre extends JFrame implements KeyListener, MouseListener {

    private Panneau pan = new Panneau();
    private final int sizeX = 1000;
    private final int sizeY = 800;
    private final int nBoules = 25;
    private Game game;
    long time;
    
    @Override
    public void mouseClicked(MouseEvent e) {
        game.mouseEvent(e.getX(), e.getY());
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (c == 'r')
            game.resetColor();
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public Fenetre(){        
        game = new Game(nBoules, sizeX, sizeY);
        pan.setGame(game);
        this.setTitle("Animation");
        this.setSize(sizeX, sizeY);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setContentPane(pan);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setVisible(true);
        addKeyListener(this);
        addMouseListener(this);
        requestFocus();
        time = System.currentTimeMillis();
        go();
    }

    private void go(){
        while (true) {
            long current = System.currentTimeMillis();
            Dimension dim = getSize();
            game.setXmax(dim.width);
            game.setYmax(dim.height);
            game.update(current - time);
            time = current;
            pan.repaint();
            // Comme on dit : la pause s'impose ! Ici, trois millièmes de seconde
            try {
              Thread.sleep(3);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
        }
    }
}

public class test {
  public static void main(String[] args){
      Fenetre fen = new Fenetre();
  }
}
