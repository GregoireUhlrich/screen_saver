import javax.swing.JFrame;
import java.awt.geom.Point2D;
import javax.swing.JPanel;
import java.awt.GraphicsDevice.WindowTranslucency;
import static java.awt.GraphicsDevice.WindowTranslucency.*;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.event.*;
import java.awt.*;
import java.util.Random;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;


class ScienceHandler {
    static private boolean enabled = false;
    static private boolean histo   = false;
    static final public Color walkingColor = Color.yellow;
    static BufferedImage imageWalkingColor;
    static private boolean imageSet = false;

    public static void toggle() {
        enabled = !enabled;
    }

    public static void toggleHisto() {
        histo = !histo;
    }

    public static boolean enabled() {
        return enabled;
    }

    public static boolean histo() {
        return histo;
    }

    private static void setImage() {
        Color color = Color.yellow;
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int size = 180;
        imageWalkingColor = new BufferedImage(
                size+2, size+2, Transparency.BITMASK);
        Graphics2D graph = (Graphics2D)imageWalkingColor.getGraphics();
        Point2D center = new Point2D.Double(size/2,
                                            size/2);
        float radius = (float)(size/2);
        Point2D focus  = new Point2D.Double(3*size/4,
                                            size/4);
        float[] dist = {0.f, 0.2f, 0.5f, 0.85f, 0.95f, 1.0f};
        Color[] colors = {new Color(255, 255, 255, 110),
                          new Color(255, 255, 255, 40),
                          new Color(0, 0, 0, 0),
                          new Color(r, g, b, 70),
                          new Color(r, g, b, 140),
                          new Color(r, g, b, 255)};
        RadialGradientPaint p = 
            new RadialGradientPaint(center,
                                    radius,
                                    focus,
                                    dist,
                                    colors,
                                    CycleMethod.NO_CYCLE);
        graph.setPaint(p);
        graph.fillOval(0, 0, size, size);
        Stroke oldStroke = graph.getStroke();
        graph.setStroke(new BasicStroke(2));
        graph.drawOval(0, 0, size, size);
        graph.setStroke(oldStroke);
        graph.dispose();
    }

    public static void paint(Graphics g, int x, int y) {
        if (!imageSet)
            setImage();
        g.drawImage(imageWalkingColor, x, y, null);
    }
}

class Gravity {
    private static Random rand = new Random();
    private static double acceleration = 0.00015;
    private static int time = 0;
    private static int sign = 1;
    public static int xmax = 1920;
    public static int ymax = 1080;

    static void accelerate() {
        acceleration *= 1.1;
    }

    static void deccelerate() {
        acceleration *= 0.9;
    }

    static void applyGravity(Boule b, double time) {
        Gravity.time += time;
        //if(Gravity.time > 10_000_000) {
        //    sign *= -1;
        //    Gravity.time = 0;
        //}
        double v = b.getVelocity();
        double btheta = b.getTheta();
        double vbx = v*Math.sin(btheta);
        double vby = -v*Math.cos(btheta);
        int x = (int)b.getX();
        int y = (int)b.getY();
        double dv = sign*acceleration*time;
        if (x < xmax/9) 
            vby += dv;
        else if (x > 4*xmax/9 && x < 5*xmax/9)
            vby -= dv;
        else if (x > 8*xmax/9)
            vby += dv;

        int signY = 1;
        if (x > xmax/2)
            signY *= -1;
        if (y < ymax/5) 
            vbx -= dv*signY;
        else if (y > 4*ymax/5)
            vbx += dv*signY;
        else
            return;

        v = Math.sqrt(vbx*vbx+vby*vby);
        btheta = Math.atan2(vbx, -vby);
        b.setVelocity(v);
        b.setTheta(btheta);
    }
}

class Boule {
    public static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private final int size;

    private double x;
    private double y;
    private double velocity;
    private double theta;
    private int nHits = 0;

    private Color color;
    private BufferedImage image;

    public Boule(int xmax, int ymax) {
        velocity = 0.3;
        int min = (int)screenSize.getHeight()/30;
        int max = (int)screenSize.getHeight()/15;
        Random rand = new Random();
        size = rand.nextInt(max-min)+min;
        x = rand.nextInt(xmax-size);
        y = rand.nextInt(ymax-size);
        theta = Math.random()*2*Math.PI-Math.PI;
        resetColor();
    }

    public int size() {
        return size;
    }

    public void hit() {
        ++nHits;
    }

    public int getNHits() {
        return nHits;
    }

    public void resetNHits() {
        nHits = 0;
    }

    public void resetColor() {
        Random rand = new Random();
        int r = rand.nextInt(75);
        int g = 50  + rand.nextInt(205);
        int b = 100 + rand.nextInt(156);
        color = new Color(r, g, b);
        setColor();
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

    public Color getDrawColor() {
        return new Color(color.getRed(),
                         color.getGreen(),
                         color.getBlue(),
                         80);
    }

    public Color getColor() {
        return color;
    }


    public boolean compareColor(Color color) {
        return color == this.color;
    }

    public void setColor(Color color) {
        this.color = color;
        setColor();
    }

    private void setColor() {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int border = 2;
        image = new BufferedImage(size+2*border, size+2*border, Transparency.BITMASK);
        Graphics2D graph = (Graphics2D)image.getGraphics();
        graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
        Point2D center = new Point2D.Double(size/2,
                                            size/2);
        float radius = (float)(size/2);
        Point2D focus  = new Point2D.Double(3*size/4,
                                            size/4);
        float[] dist = {0.f, 0.2f, 0.5f, 0.85f, 0.95f, 1.0f};
        Color[] colors = {new Color(255, 255, 255, 110),
                          new Color(255, 255, 255, 40),
                          new Color(0, 0, 0, 0),
                          new Color(r, g, b, 70),
                          new Color(r, g, b, 140),
                          new Color(r, g, b, 255)};
        RadialGradientPaint p = 
            new RadialGradientPaint(center,
                                    radius,
                                    focus,
                                    dist,
                                    colors,
                                    CycleMethod.NO_CYCLE);
        graph.setPaint(p);
        graph.fillOval(border, border, size, size);
        Stroke oldStroke = graph.getStroke();
        graph.setStroke(new BasicStroke(border));
        graph.drawOval(border, border, size, size);
        graph.setStroke(oldStroke);
        graph.dispose();
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

    public void paint(Graphics g) {
        if (false && color == Color.yellow)
            ScienceHandler.paint(g, (int)x, (int)y);
        else
            g.drawImage(image, (int)x-1, (int)y-1, null);
    }
}
class Game{
    int nBoules;
    final int nWalker = 5;
    int xmax;
    int ymax;
    private ArrayList<Boule> boules;
    Histo histo;
    BufferedImage image;
    BufferedImage bg;

    public Game(int nBoules, int xmax, int ymax) {
        this.nBoules = nBoules;
        this.xmax = xmax;
        this.ymax = ymax;
        boules = new ArrayList<Boule>();
        for (int i = 0; i != nBoules; ++i)
            boules.add(new Boule(this.xmax, this.ymax));
        resetColor();
        histo = new Histo(boules);
        try {
            image = ImageIO.read(new File("fond4.jpg"));
        } catch(IOException e) {
            System.out.println("File not found!");
        }
        try {
            bg = getBackgroundImage();
        } catch(AWTException e) {
            bg = new BufferedImage(1,1,0);
        }
    }

    private void testBounceX(Boule b) {
        double x = b.getX();
        double theta = b.getTheta();
        double pi = Math.PI;
        if (x < 0) 
            b.bounceX(xmax);
        else if (x > xmax - b.size())
            b.bounceX(xmax);
    }

    private void testBounceY(Boule b) {
        double y = b.getY();
        double theta = b.getTheta();
        double pi = Math.PI;
        if (y < 0)  
            b.bounceY(ymax);
        else if (y > ymax - b.size()) 
            b.bounceY(ymax);
    }

    private double distance(Boule b1, Boule b2) {
        return Math.sqrt(
                Math.pow(b1.getX() - b2.getX() + (b1.size()-b2.size())/2, 2)
              + Math.pow(b1.getY() - b2.getY() + (b1.size()-b2.size())/2, 2));
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
        if (r == 0)
            return;
        double value = scalar(vrx, vry, rx, ry)/Math.pow(r,2);
        if (value <= 0)
            return;

        if(b1.compareColor(ScienceHandler.walkingColor) || b2.compareColor(ScienceHandler.walkingColor)) {
            if (b1.compareColor(ScienceHandler.walkingColor))
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
        if (ratioE == 0)
            return;

        b1.setVelocity(Math.sqrt(1/ratioE*scalar(vx1, vy1, vx1, vy1)));
        b2.setVelocity(Math.sqrt(1/ratioE*scalar(vx2, vy2, vx2, vy2)));
    }

    private void testCollisions() {
        for (int i = 0; i != boules.size()-1; ++i)  {
            Boule b1 = boules.get(i);
            for (int j = i+1; j != boules.size(); ++j)  {
                Boule b2 = boules.get(j);
                if (distance(b1, b2) <= (b1.size()+b2.size())/2.)
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
            b.resetNHits();
        }
        for (int i = 0; i != nWalker; ++i)
            boules.get(i).setColor(ScienceHandler.walkingColor);
    }

    public void mouseEvent(int x, int y) {
        for (int i = 0; i != boules.size(); ++i) {
            Boule b = boules.get(i);
            double distance = Math.sqrt(
                    Math.pow(x - (b.getX()+b.size()/2), 2)
                  + Math.pow(y - (b.getY()+b.size()/2), 2)
                    );
            if (b.getColor() != ScienceHandler.walkingColor && distance < b.size()) {
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

    static public BufferedImage getBackgroundImage() throws AWTException
    {
        Robot rbt = new Robot();
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension dim = tk.getScreenSize();
        BufferedImage background = rbt.createScreenCapture(
                new Rectangle(0, 1, (int) dim.getWidth(), (int) dim.getHeight()));
        return background;
    }

    public void paint(Graphics g) {
        //g.drawImage(bg, 0, 0, null);
        g.drawImage(image, 0, 0, null);
        g.setColor(new Color(0,0,0,32));
        g.fillRect(0, 0, xmax, ymax);
        for (int i = 0; i != boules.size(); ++i) {
            Boule b = boules.get(i);
            b.paint(g);
            if (ScienceHandler.enabled()) {
                g.setFont(new Font("TimesRoman", Font.PLAIN, 15));
                g.setColor(Color.black);
                g.drawString(""+i,
                    b.size()+(int)b.getX()-b.size()/3-20,
                    (int)b.getY()+b.size()/3);
            }
        }
        int width = 500;
        int height = 300;
        if (ScienceHandler.histo())
            histo.paint(g, xmax-width, ymax-height-50, width, height);
        g.dispose();
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
        g.drawLine(x+1, y, x+1, y+height);
        g.drawLine(x, y+height-1, x+width, y+height-1);
        g.setColor(ScienceHandler.walkingColor);
        int widthBin = (width - 3) / boules.size();
        int posX = 2;
        int nmax = 0;
        int nmin = boules.get(0).getNHits();
        double nmean = 0;
        for (int i = 0; i != boules.size(); ++i)  {
            int nHits = boules.get(i).getNHits();
            nmean += nHits;
            if (nHits > nmax)
                nmax = nHits;
            if (nHits < nmin)
                nmin = nHits;
        }
        nmean /= 1*boules.size();

        double stepY;
        if (nmax > 0)
            stepY = (height / (1.*nmax))*0.8;
        else 
            stepY = 0;
        int offsetX = x;
        int offsetY = y + height;
        for (int i = 0; i != boules.size(); ++i) {
            int n = boules.get(i).getNHits();
            int sizeY = (int)(n*stepY);
            g.setColor(ScienceHandler.walkingColor);
            g.fillRect(offsetX+posX, offsetY-sizeY,
                       widthBin, sizeY);
            g.setColor(Color.black);
            g.drawRect(offsetX+posX, offsetY-sizeY,
                       widthBin, sizeY);
            g.setFont(new Font("TimesRoman", Font.PLAIN, 13));
            if (ScienceHandler.enabled())
                g.drawString(""+i,
                    offsetX+posX+3,
                    offsetY-5);

            posX += widthBin;
        }
        g.setColor(Color.white);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
        g.drawString("Std/Mean = " + (double)((int)(getStd()*100))/100, x + width/8, y+10);
        g.drawString("Max = " + nmax, x + 2*width/3, y+10);
        g.drawString("Min = " + nmin, x + 2*width/3, y+30);
        g.drawString("Mean = " + nmean, x + 2*width/3, y+50);
    }

    public double getStd() {
        double nmean = 0;
        for (int i = 0; i != boules.size(); ++i)
            nmean += boules.get(i).getNHits();
        nmean = nmean / boules.size();
        double std_mean = 0;
        for (int i = 0; i != boules.size(); ++i)
            std_mean += Math.pow(boules.get(i).getNHits() - nmean, 2);
        return Math.sqrt(std_mean)/nmean;
    }
}


@SuppressWarnings("serial") 
class Panneau extends JPanel {
    Game game;
    private float panelAlfa;
    private float childrenAlfa;

    public Panneau() {
        //this.panelAlfa = panelAlfa;
        //this.childrenAlfa = childrenAlfa;
        setOpaque(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        //Paint p =
        //    new Paint(0.0f, 0.0f, new Color(0,0,0,0), true);
        //        0.0f, getHeight(), new Color(0,0,0,0), true);
        //g2d.setPaint(p);
        //g2d.setColor(new Color(0,0,0,0));
        //g2d.fillRect(0, 0, getWidth(), getHeight());
        //setBackground(getBackground());
        //g.fillRect(0, 0, getWidth(), getHeight());
        //super.paintComponent(g);
        super.paintComponent(g);
        game.paint(g);
    }

    public void setGame(Game g) {
        game = g;
    }
}

@SuppressWarnings("serial")
class Fenetre extends JFrame implements KeyListener, MouseListener {

    private Panneau pan = new Panneau();
    private final int sizeX = 1000;
    private final int sizeY = 800;
    private final int nBoules = 200;
    private Game game;
    long time;
    boolean running = true;
    
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
        else if (c == 't')
            ScienceHandler.toggle();
        else if (c == 'h')
            ScienceHandler.toggleHisto();
        else if (c == 's')
            System.exit(0);
        else if (c == '+')
            Gravity.accelerate();
        else if (c == '-')
            Gravity.deccelerate();
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
        setUndecorated(true);
        this.add(pan);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
         // Determine what the GraphicsDevice can support.
        GraphicsDevice vc;
	
	//Initialize the vc with the Screen Device
        GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
        vc=ge.getDefaultScreenDevice();
        setResizable(false);
        vc.setFullScreenWindow(this);

        this.setVisible(true);
        addKeyListener(this);
        addMouseListener(this);
        requestFocus();
        time = System.currentTimeMillis();
        go();
        this.setVisible(false);
    }

    private void go(){
        while (running) {
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
