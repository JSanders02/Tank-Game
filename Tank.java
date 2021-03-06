import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import java.awt.Point;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.MouseInfo;

import java.awt.geom.AffineTransform;

class Tank extends Sprite {
    // Constants and instance variables
    public final double BASE_SPEED = 200 * ((double)GameSurface.FRAME_TIME / 1000); // Px/frame
    public final double TURN_SPEED = Math.toRadians(144) * ((double)GameSurface.FRAME_TIME / 1000); // rad/frame
    
    protected Point aim;
    protected double turretAngle;
    protected double speed;
    
    protected double xChange;
    protected double yChange;
    
    private String tankType;
    
    private BufferedImage[] turretAnimFrames; // Stores sprite images
    private boolean animateTurret;
    private int turretFrame;
    private int turretAnimCounter;
    private boolean hasFired; // Tank has requested to fire
    
    private int queuedShells; // Stores data for shell to be fired
    private int numShells; // Number of shells fired that haven't exploded
    
    protected int shellSpeedMult;
    protected int shellBounceNum;
    
    public Tank(String type, int x, int y, double radians, int speedMult) {
        super(new String[]{type+"TankBase.png", type+"TankTurret.png"}, x, y, radians);
        tankType = type;
        
        // Initialise aim as a default Point, which will be updated later
        aim = new Point(0,0);
        
        speed = BASE_SPEED * speedMult;
        
        turretAnimFrames = new BufferedImage[4];
        turretAnimFrames[0] = imageList.get(1);
        
        loadAnimationFrames();
        
        hasFired = false;
        animateTurret = false;
        turretFrame = 0;
        turretAnimCounter = 0;
        
        queuedShells = 0;
        numShells = 0;
    }
    
    private void fireShell() {
        queuedShells -= 1;
        numShells ++;
        // Calculate shell position to be at the end of the turret
        double turretLength = (imageList.get(1).getHeight() / 2) + 10;
        int shellX = (int) (pos.x + Math.sin(turretAngle) * turretLength);
        int shellY = (int) (pos.y - Math.cos(turretAngle) * turretLength);
        
        // Adds a shell to GameSurface's arraylist
        Tanks.gameSurface.addShell(new Shell(shellSpeedMult, shellX, shellY, 
                                             turretAngle, shellBounceNum, this));
                                             
                                             
    }
    
    protected void queueShell() {
        if (numShells + queuedShells < 5) {
            queuedShells++;
            hasFired = true;
            animateTurret = true;
        }
    }
    
    public void shellDestroyed() {
        numShells -= 1;
    }
    
    public void kill() {
        Tanks.gameSurface.deleteTank(arrayListIndex);
    }
    
    public void update() {
       super.animate(1, 0);
        
        // Animation for turret
        if (animateTurret) {
            turretAnimCounter++;
            if (turretAnimCounter >= ANIMATION_FRAMES / 5) {
                turretAnimCounter = 0;
                turretFrame ++;
                if (turretFrame == 2) {
                    fireShell();
                } else if (turretFrame > turretAnimFrames.length - 1) {
                    turretFrame = 0;
                    if (queuedShells == 0) animateTurret = false;
                }
                imageList.set(1, turretAnimFrames[turretFrame]);
            }
        }
        
        if (angle < -Math.PI) {
            angle += Math.PI * 2;
        } else if (angle > Math.PI) {
            angle -= Math.PI * 2;
        }
        
        //Calculate necessary x and y changes for given rotation
        xChange = speed * Math.sin(angle);
        yChange = speed * Math.cos(angle);
        
        calculateBoundingRect();
        
        // Check in both x and y dimensions to see if the bounding box intersects with the edge of the frame
        if (realCoords[0] - bBox.width < 0) {
            realCoords[0] = 0 + bBox.width;
            pos.x = 0 + bBox.width;
        } else if (realCoords[0] + bBox.width > GameSurface.GAME_WIDTH) {
            realCoords[0] = GameSurface.GAME_WIDTH - bBox.width;
            pos.x = GameSurface.GAME_WIDTH - bBox.width;
        }
        
        if (realCoords[1] - bBox.height < 0) {
            realCoords[1] = 0 + bBox.height;
            pos.y = 0 + bBox.height;
        } else if (realCoords[1] + bBox.height > GameSurface.GAME_HEIGHT) {
            realCoords[1] = GameSurface.GAME_HEIGHT - bBox.height;
            pos.y = GameSurface.GAME_HEIGHT - bBox.height;
        }
    }
    
    @Override
    public void draw(Graphics g, ImageObserver observer) {
        super.draw(g, observer);
        // Create AffineTransform object that will rotate the image.
        AffineTransform at = new AffineTransform();
        at.translate(pos.x, pos.y); // Translate to desired position
        at.rotate(turretAngle); // Rotate (No need to convert to radians as turretAngle is already in radians)
        // Translate up and left by half of width and height, to centre the image
        at.translate(-imageList.get(1).getWidth()/2, -imageList.get(1).getHeight()/2);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(imageList.get(1), at, null);
    }
    
    private void loadAnimationFrames() {
        // Turret animation and tank animation have the same number of frames, so no need for second loop
        for (int i=1; i<animationFrames.length; i++) {
            try {
                animationFrames[i] = ImageIO.read(new File("assets/images/" + 
                                                            tankType + 
                                                            "TankBase" + i + 
                                                            ".png"));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error loading tank base " +
                                                     "sprites: " + e.getMessage());
            }
            
            try {
                turretAnimFrames[i] = ImageIO.read(new File("assets/images/" + 
                                                            tankType + 
                                                            "TankTurret" + i + 
                                                            ".png"));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error loading tank turret " +
                                                     "sprites: " + e.getMessage());
            }
        }
    }
}