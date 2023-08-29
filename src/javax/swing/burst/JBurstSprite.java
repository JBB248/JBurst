package javax.swing.burst;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.swing.burst.animation.JBurstAnimationController;
import javax.swing.burst.graphics.JBurstGraphic;
import javax.swing.burst.graphics.frames.JBurstAtlasFrames;
import javax.swing.burst.graphics.frames.JBurstFrame;
import javax.swing.burst.graphics.frames.JBurstFramesCollection;

/**
 * A sprite class extension for the Java Swing package.
 * <p>
 * Allows for the use of static <i>and</i> animated sprites.
 */
public class JBurstSprite extends JBurstBasic 
{
    /**
     * The transparency of this sprite.
     * <p> <i>Currently unused</i>
     */
    public double alpha = 1.0f;

    /**
     * Whether or not this sprite should be smoothed at rendering.
     */
    public boolean antialiasing = false;

    /**
     * The manager to control animation property's of this sprite.
     * <p> Use functions from this to add and play animations.
     */
    public final JBurstAnimationController animation;

    private final Point2D.Double scale;

    private double angle = 0.0;

    private final Point framePoint;

    /**
     * A collection of all the frames used by this sprite.
     * <p>
     * Public access is provided for the sake of the animation classes, 
     * but it is strongly suggested that it be treated as <strong>read-only</strong>.
     */
    private JBurstFramesCollection frames;

    /**
     * The current frame being used in the drawing process.
     */
    private JBurstFrame frame;

    /**
     * Whether or not the sprite's bounding box outline should be drawn.
     */
    public boolean debugMode = false;

    /**
     * Constructs a new JBurstSprite at coordinates (0, 0);
     */
    public JBurstSprite() 
    {
        this(0, 0);
    }

    /**
     * Constructs a new JBurstSprite at coordinates ({@code x}, {@code y}).
     */
    public JBurstSprite(int x, int y) 
    {
        super();

        scale = new Point2D.Double(1.0, 1.0);
        framePoint = new Point();
        animation = new JBurstAnimationController(this);
        
        setLocation(x, y);
    }

    @Override
    public void update(double elapsed)
    {
        animation.update(elapsed);
    }
    
    @Override 
    public void paint(Graphics graphics)
    {
        if(frame == null || !exists || !visible || alpha == 0)
            return;

        BufferedImage frameImage = frame.graphic.image.getSubimage(
            frame.x, 
            frame.y, 
            frame.width, 
            frame.height
        );

        Point offset = new Point(frame.offset);

        BufferedImage image = new BufferedImage(frameImage.getWidth() + offset.x, frameImage.getHeight() + offset.y, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pixels = image.createGraphics();

        /* Image Manipulation */

        if(angle != 0)
        {
            int frameWidth = getSpriteWidth();
            int frameHeight = getSpriteHeight();

            int newWidth = (int)(Math.abs(frameWidth * Math.cos(angle)) + Math.abs(frameHeight * Math.sin(angle)));           
            int newHeight = (int)(Math.abs(frameWidth * Math.sin(angle)) + Math.abs(frameHeight * Math.cos(angle)));

            int deltaX = (newWidth - frameWidth) / 2;
            int deltaY = (newHeight - frameHeight) / 2;

            image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            pixels = image.createGraphics();

            if(debugMode)
            {
                pixels.setColor(Color.RED);
                pixels.drawRect(deltaX, deltaY, frameWidth - 1, frameHeight - 1);
            }

            pixels.rotate(angle, newWidth / 2, newHeight / 2);

            if(debugMode)
            {
                pixels.setColor(Color.BLUE);
                pixels.drawRect(deltaX, deltaY, frameWidth - 1, frameHeight - 1);
            }

            // System.out.println(deltaX + ", " + deltaY);

            offset.x += deltaX;
            offset.y += deltaY;

            setLocation(framePoint.x - deltaX, framePoint.y - deltaY);
            setSize(newWidth, newHeight);
            revalidate();
        }

        if(scale != null && (scale.x > 0 || scale.y > 0))
            pixels.scale(scale.x, scale.y);

        if(antialiasing)
            pixels.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw with offsets pre-applied to dilated/rotated image
        pixels.drawImage(frameImage, offset.x, offset.y, null);
        pixels.dispose();

        /**********************/

        if(debugMode)
            System.out.println(getWidth() + ", " + getHeight());

        if(debugMode)
            graphics.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

        graphics.drawImage(image, 0, 0,null);
    }

    /**
     * Loads this sprite as a rectangle of one solid color.
     * 
     * @param width     Width of rectangle
     * @param height    Height of rectangle
     * @param color     Color of rectangle
     * 
     * @return  This JBurstSprite. Useful for chaining.
     */
    public JBurstSprite makeGraphic(int width, int height, Color color)
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        
        graphics.setColor(color);
        graphics.fillRect(0, 0, width - 1, height - 1);
        graphics.dispose();

        return loadGraphic(JBurstGraphic.fromImage(image));
    }

    /**
     * Loads a graphic onto this sprite.
     * 
     * @param graphic   Image to be loaded onto this sprite.
     * 
     * @return  This JBurstSprite. Useful for chaining.
     * @see {@link JBurstGraphic}
     */
    public JBurstSprite loadGraphic(JBurstGraphic graphic) 
    {
        setFrame(new JBurstFrame(graphic, "Frame", 0, 0, graphic.getWidth(), graphic.getHeight()));
        frame.sourceSize.setLocation(frame.width, frame.height);

        this.frames = new JBurstFramesCollection(graphic);
        this.frames.pushFrame(frame);

        updateBounds();
        
        return this;
    }

    /**
     * Loads a graphic onto this sprite. 
     * However, unlike {@code loadGraphic()}, this will give it animation properties.
     * <p>
     * This version will take the provided graphic and split it into as many frames as it can
     * with the dimensions of {@code width} and {@code height}, adding each one to the sprite's
     * list of frames.
     * 
     * @param graphic   Image to be sliced and displayed
     * @param width     Width of frame used to slice
     * @param height    Height of frame used to slice
     * 
     * @return  This JBurstSprite. Useful for chaining.
     * @see {@link JBurstGraphic}
     */
    public JBurstSprite loadAnimatedGraphic(JBurstGraphic graphic, int width, int height)
    {
        int graphWidth = graphic.getWidth();
        int graphHeight = graphic.getHeight();

        if(width == 0) 
        {
            width = graphHeight;
			width = (width > graphWidth) ? graphWidth : width;
        }

        if (height == 0)
		{
			height = graphWidth;
			height = (height > graphHeight) ? graphHeight : height;
		}

        this.frames = new JBurstFramesCollection(graphic);

        int x = 0;
        int y = 0;
        for(int i = 0; y < graphHeight; i++)
        {
            String frameNum = "" + i;
            while(frameNum.length() < 4) frameNum = "0" + frameNum;
            
            JBurstFrame frame = new JBurstFrame(graphic, "frame" + frameNum, x, y, width, height);
            frame.sourceSize.setLocation(width, height);
            frame.checkFrame();
            frames.pushFrame(frame);

            x += width;
            if(x >= graphWidth)
            {
                x = 0;
                y += height;
            }
        }

        setFrame(frames.get(0));
        updateBounds();

        return this;
    }

    /**
     * Loads a frame collection from a spritesheet and designated animation file.
     * 
     * @param frames    Frame collection to be loaded
     */
    public JBurstFramesCollection loadFrames(JBurstAtlasFrames frames)
    {
        this.frames = frames;
        this.animation.clearAnimations();

        setFrame(frames.get(0));
        updateBounds();

        return frames;
    }

    /**
     * Sets the current frame of the sprite.
     * 
     * @param frame Frame to be set
     */
    public void setFrame(JBurstFrame frame)
    {
        JBurstFrame oldFrame = this.frame;

        this.frame = frame;

        firePropertyChange("frame", oldFrame, frame);

        if(oldFrame == null || oldFrame == frame) return;

        if(frame.width != oldFrame.width && frame.height != oldFrame.height)
        {
            updateBounds();
        }
    }

    /**
     * Returns a collection of all the frames used by this sprite, which may be {@code null}.
     * 
     * @return  a collection of this sprite's frames
     */
    public JBurstFramesCollection getFrames()
    {
        return frames;
    }

    /**
     * Sets the angle of rotation of this sprite, in degrees.
     * <p> For example, 180.0 would flip this sprite upside-down.
     * 
     * @param theta The amount to rotate this sprite by, in degrees
     */
    public void setAngleViaDegrees(double theta)
    {
        setAngleViaRadians(theta * (Math.PI / 180.0));
    }

    /**
     * Sets the angle of rotation of this sprite, in radians.
     * <p> For example, 180.0 would flip this sprite upside-down.
     * 
     * @param theta The amount to rotate this sprite by, in radians
     */
    public void setAngleViaRadians(double theta)
    {
        if(theta >= 2.0 * Math.PI)
            theta = 0.0;
        
        this.angle = theta;
    }

    /**
     * Sets the sizing scale of this sprite.
     * <p>
     * For example: providing 0.5 would halve the sprite in size.
     * <p>
     * <i>A value less then or equal to zero will be ignored.</i>
     * 
     * @param scale How big or small to make this sprite.
     */
    public void setScale(double scale)
    {
        setScale(scale, scale);
    }

    /**
     * Sets the sizing scale of this sprite.
     * <p>
     * For example: providing 0.5 to {@code scaleX} would halve the sprite in size, horizontally.
     * <p>
     * <i>Values less then or equal to zero will be ignored.</i>
     * 
     * @param scaleX    How big or small to make this sprite, horizontally.     
     * @param scaleY    How big or small to make this sprite, vertically.
     */
    public void setScale(double scaleX, double scaleY)
    {
        if(scaleX <= 0 && scaleY <= 0) return;

        scale.setLocation(scaleX, scaleY);
        updateBounds();
    }

    /**
     * Sets the size that this sprite's graphic should be drawn at, in pixels.
     * <p>
     * <i>Values less than or equal to zero will be ignored.</i>
     * 
     * @param width     New width of graphic
     * @param height    New height of graphic
     */
    public void setGraphicSize(int width, int height)
    {
        if(width <= 0 && height <= 0) return;

        double scaleX = ((double) width) / getFrameWidth();
        double scaleY = ((double) height) / getFrameHeight();

        if(width <= 0)
            scaleX = scaleY;
        else if(height <= 0)
            scaleY = scaleX;

        setScale(scaleX, scaleY);
    }

    private void updateBounds()
    {
        setBounds(getX(), getY(), getSpriteWidth(), getSpriteHeight());
        revalidate();
    }

    /**
     * Sets the x position of this sprite.
     * 
     * @param x the new x-coordinate of this sprite
     */
    public void setX(int x)
    {
        framePoint.x = x;
        setLocation(x, getY());
    }

    /**
     * Sets the y position of this sprite.
     * 
     * @param y the new y-coordinate of this sprite
     */
    public void setY(int y)
    {
        framePoint.y = y;
        setLocation(getX(), y);
    }

    /**
     * Sets the position of this sprite.
     * <p> This should be used in opposition to {@code setLocation()} 
     * as this will also update the sprite's relative position which is used
     * in calculating rotational offsets.
     * 
     * @param x the new x-coordinate of this sprite
     * @param y the new y-coordinate of this sprite
     */
    public void setPosition(int x, int y)
    {
        framePoint.setLocation(x, y);
        setLocation(x, y);
    }

    /**
     * Returns the width of this sprite with scaling calculations.
     * 
     * @return  the current width of this sprite
     */
    public int getSpriteWidth()
    {
        int width = 0;
        if(frame != null)
            width = (int) (frame.sourceSize.x * scale.x);

        return width;
    }

    /**
     * Returns the height of this sprite with scaling calculations.
     * 
     * @return  the current height of this sprite
     */
    public int getSpriteHeight()
    {
        int height = 0;
        if(frame != null)
            height = (int) (frame.sourceSize.y * scale.y);

        return height;
    }

    private int getFrameWidth()
    {
        int width = 0;
        if(frame != null)
            width = frame.width;
        
        return width;
    }

    private int getFrameHeight()
    {
        int height = 0;
        if(frame != null)
            height = frame.height;

        return height;
    }

    /**
     * Returns this sprite's graphic object, 
     * which may be {@code null}.
     * 
     * @return  this sprite's graphic object
     */
    public JBurstGraphic getGraphic()
    {
        JBurstGraphic graphic = null;
        if(frame != null)
            graphic = frame.graphic;

        return graphic;
    }

    /**
     * Returns a writable graphics object from this sprite's graphic,
     * which may be {@code null}.
     * 
     * @return a writable {@code Graphics2D} object
     */
    public Graphics2D getPixels()
    {
        Graphics2D pixels = null;
        JBurstGraphic graphic = getGraphic();
        if(graphic != null)
            pixels = graphic.getPixels();
        
        return pixels;
    }

    /**
     * Returns the amount of frames stored within this sprite's frame collection.
     * 
     * @return  the length if this sprite's frame collection
     */
    public int getNumFrames()
    {
        return frames.size();
    }

    @Override
    public String toString()
    {
        return "JBurstSprite ~ {x: " + getX() + ", y: " + getY() + ", width: " + getWidth() + ", height: " + getHeight() + "}";
    }
}
