import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import javax.swing.JOptionPane;

import java.awt.Point;
import java.awt.Dimension;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;

public class Shell {
    private double speed;

    private Point pos;
    private double[] realCoords;
    private double angle;
    
    private double xChange;
    private double yChange;
    
    private double diagAngle;
    private double diagLength;
    private Dimension bBox;
    
    // Used to control how many times the shell can bounce off of walls before exploding
    private int bounceNum;
    private int currentBounce;
    
    private BufferedImage shellImg;
    
    // Stores index of shell in arraylist, for easy deletion
    private int arrayListIndex;
    
    public Shell(double speed, double x, double y, double radians, int bounceNum) {
        loadImages();
        
        // Pythagoras to calculate length of diagonal
        diagLength = Math.sqrt(Math.pow(shellImg.getHeight(), 2) + 
                                Math.pow(shellImg.getWidth(), 2));

        // tan = opp/adj so angle = atan(opp/adj)
        diagAngle = Math.atan((double) shellImg.getWidth()/shellImg.getHeight());
        
        speed = speed;
        pos = new Point((int) x, (int) y);
        realCoords = new double[]{x, y};
        
        /* 
         * Perform speed and bounding box calculations in constructor, to avoid 
         * calculating the same thing every frame, wasting computing power.
         */
        angle = radians;
        xChange = speed * Math.sin(angle);
        yChange = speed * Math.cos(angle);
        
        bounceNum = bounceNum;
        currentBounce = 0;
        
        bBox = new Dimension(0, 0);
        calculateBoundingRect();
    }
    
    private void loadImages() {
        try {
            shellImg = ImageIO.read(new File("assets/images/shell.png"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading shell sprite: " 
                                                  + e.getMessage());
        }
    }
    
    private void calculateBoundingRect() {
        // Calculate bounding box
        double theta = Math.abs(angle);
        if (theta > Math.PI / 2) {
            // Keep theta between below 90 otherwise triangle angle calcs will break
            theta = (Math.PI / 2) - (theta - (Math.PI / 2));
        }
           
        // Trigonometry magic 
        double tempAngle = Math.toRadians(90) - theta - diagAngle;
        int bBoxHalfWidth = (int) ((diagLength / 2) * Math.cos(tempAngle));
        int bBoxHalfHeight = (int) ((diagLength / 2) * Math.cos(theta - diagAngle));
        
        bBox.width = bBoxHalfWidth;
        bBox.height = bBoxHalfHeight;
    }
    
    private void destroy() {
        Tanks.gameSurface.deleteShell(arrayListIndex);
    }
    
    public void setArrayListIndex(int i) {
        arrayListIndex = i;
    }
    
    public void update() {
        realCoords[0] += xChange;
        realCoords[1] -= yChange;
        
        pos.x = (int) realCoords[0];
        pos.y = (int) realCoords[1];
        
        boolean collided = false;
        
        // Check in both x and y dimensions to see if the bounding box intersects with the edge of the frame
        if (realCoords[0] - bBox.width < 0 || 
                realCoords[0] + bBox.width > GameSurface.GAME_WIDTH) {
            System.out.println("Wall go brr");
            collided = true;
        }
        
        if (realCoords[1] - bBox.height < 0 || 
                realCoords[1] + bBox.height > GameSurface.GAME_HEIGHT) {
            System.out.println("Ceiling go brr");
            collided = true;
        }
        
        if (collided) {
            Tanks.gameSurface.deleteShell(arrayListIndex);
        }
    }
    
    public void draw(Graphics g, ImageObserver observer) {
        // Create AffineTransform object that will rotate the image.
        AffineTransform at = new AffineTransform();
        at.translate(pos.x, pos.y); // Translate to desired position
        at.rotate(angle); // Rotate
        // Translate up and left by half of width and height, to centre the image
        at.translate(-shellImg.getWidth()/2, -shellImg.getHeight()/2);

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(shellImg, at, null);
    }
}