package burst.graphics.frames;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import burst.graphics.JBurstGraphic;
import burst.util.JBurstDestroyUtil.IBurstDestroyable;

/**
 * A specialized rectangle used by the animation classes.
 * 
 * @author Joe Bray
 * <p> Modeled from <a href="https://api.haxeflixel.com/flixel/graphics/frames/FlxFrame.html">FlxFrame</a>
 */
public class JBurstFrame implements IBurstDestroyable
{
    public Rectangle frame;

    /**
     * The name of this frame.
     */
    public String name;

    /**
     * The parent graphic of this frame.
     */
    public JBurstGraphic graphic;

    /**
     * Original (uncropped) image size.
     */
    public Dimension sourceSize;

    /**
     * Frame offset from the top left corner of original image.
     */
    public Point offset;

    public double angle;

    public JBurstFrame(JBurstGraphic graphic)
    {
        this(graphic, 0);
    }

    public JBurstFrame(JBurstGraphic graphic, double angle)
    {
        this.graphic = graphic;

        sourceSize = new Dimension();
        offset = new Point();
    }

    public BufferedImage paint(BufferedImage image)
    {
        if(image == null)
            image = new BufferedImage(sourceSize.width, sourceSize.height, BufferedImage.TYPE_INT_ARGB);
        else
            clearFrame(image);

        Graphics2D graphics = image.createGraphics();
        graphics.drawImage(graphic.image.getSubimage(frame.x, frame.y, frame.width, frame.height), offset.x, offset.y, null);
        graphics.dispose();

        return image;
    }

    public void clearFrame(BufferedImage image)
    {
        Graphics2D graphics = image.createGraphics();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        graphics.setColor(new Color(0));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.dispose();
    }

    public JBurstFrame copyTo(JBurstFrame clone)
    {
        if(clone == null)
            clone = new JBurstFrame(graphic, angle);
        else
        {
            clone.graphic = graphic;
            clone.angle = angle;
            clone.frame = null;
        }

        clone.offset.setLocation(offset);
        clone.sourceSize.setSize(sourceSize);
        clone.frame = new Rectangle(frame);
        clone.name = name;

        return clone;
    }

    @Override
    public void destroy()
    {
        name = null;
        graphic = null;
        sourceSize = null;
        offset = null;
    }

    @Override
    public String toString()
    {
        return "BurstFrame ~ {name: " + name + " x: " + frame.x + ", y: " + frame.y + ", width: " + frame.width + ", height: " + frame.height + "}";
    }
}
