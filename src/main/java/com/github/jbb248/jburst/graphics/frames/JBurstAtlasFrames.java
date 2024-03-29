package com.github.jbb248.jburst.graphics.frames;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.awt.Point;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.github.jbb248.jburst.graphics.JBurstGraphic;
import com.github.jbb248.jburst.util.XmlParser;

/**
 * A collection of frames stored as an atlas. 
 * <p> Also includes texture atlas parsing methods.
 * 
 * @author Joe Bray
 * <p> Modeled from <a href="https://api.haxeflixel.com/flixel/graphics/frames/FlxAtlasFrames.html">FlxAtlasFrames</a>
 */
public class JBurstAtlasFrames extends JBurstFramesCollection 
{
    /**
     * Parsing method for sparrow texture atlases.
     * 
     * @param source          file location of desired spritesheet to be loaded and parsed.
     * @param description   parsing instructions file location (Should be .xml)
     */
    public static JBurstAtlasFrames fromSparrow(String source, String description)
    {
        return fromSparrow(JBurstGraphic.fromFile(source), description);
    }

    /**
     * Parsing method for sparrow texture atlases.
     * 
     * @param graphic       spritesheet to be parsed.
     * @param description   parsing instructions file location (Should be .xml)
     */
    public static JBurstAtlasFrames fromSparrow(JBurstGraphic graphic, String description) 
    {
        if(graphic == null || description == null || !new File(description).exists())
            return null;

        JBurstAtlasFrames frames = new JBurstAtlasFrames(graphic);
        
        NodeList data = XmlParser.parse(description).getElementsByTagName("SubTexture");
        for(int i = 0; i < data.getLength(); i++) 
        {
            Node item = data.item(i);

            if(item.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element attribute = (Element) item;
            String name = attribute.getAttribute("name");
            boolean trimmed = (!attribute.getAttribute("frameX").isEmpty());

            Rectangle rect = 
                new Rectangle(
                    Integer.parseInt(attribute.getAttribute("x")),
                    Integer.parseInt(attribute.getAttribute("y")),
                    Integer.parseInt(attribute.getAttribute("width")),
                    Integer.parseInt(attribute.getAttribute("height"))
                );

            Rectangle size;
            if(trimmed)
                size = new Rectangle(
                    Integer.parseInt(attribute.getAttribute("frameX")),
                    Integer.parseInt(attribute.getAttribute("frameY")),
                    Integer.parseInt(attribute.getAttribute("frameWidth")),
                    Integer.parseInt(attribute.getAttribute("frameHeight"))
                );
            else
                size = new Rectangle(
                    0,
                    0,
                    rect.width,
                    rect.height
                );

            Point offset = new Point(-size.x, -size.y);
            Point sourceSize = new Point(size.width, size.height);

            frames.addAtlasFrame(rect, sourceSize, offset, name);
        }

        return frames;
    }

    /**
     * Parsing method for texture atlases in JSON format.
     * <p> 
     * <i>Note: There is currently no way to load rotated sprites.</i>
     * 
     * @param source        file location of desired spritesheet to be loaded and parsed.
     * @param description   parsing instructions file location (Should be .json)
     */
    public static JBurstAtlasFrames fromJsonPacker(String source, String description)
    {
        return fromJsonPacker(JBurstGraphic.fromFile(source), description);
    }

    /**
     * Parsing method for texture atlases in JSON format.
     * <p> 
     * <i>Note: There is currently no way to load rotated sprites.</i>
     * 
     * @param graphic       spritesheet to be parsed.
     * @param description   parsing instructions file location (Should be .json)
     */
    @SuppressWarnings("unchecked")
    public static JBurstAtlasFrames fromJsonPacker(JBurstGraphic graphic, String description)
    {
        if(graphic == null || description == null)
            return null;

        JBurstAtlasFrames frames = new JBurstAtlasFrames(graphic);

        JSONObject data;

        try 
        {
            StringBuilder content = new StringBuilder();
            Scanner scanner = new Scanner(new File(description));

            while(scanner.hasNextLine()) content.append(scanner.nextLine());
            scanner.close();

            trimJsonContent(content);

            data = (JSONObject) new JSONParser().parse(content.toString());
        } 
        catch(IOException e) 
        {
            System.out.println("File not found: " + description);
            return null;
        }
        catch(ParseException e)
        {
            System.out.println("Error parsing JSON file: " + description);
            return null;
        }

        JSONArray frameList;
        Object framesObj = data.get("frames");

        if(framesObj instanceof JSONArray)
        {
            frameList = (JSONArray) framesObj;
        }
        else
        {
            JSONObject frameHash = (JSONObject) framesObj;
            frameList = new JSONArray();

            for(Object key : frameHash.keySet())
            {
                JSONObject frame = (JSONObject) frameHash.get(key);
                frame.put("filename", key.toString());

                frameList.add(frame);
            }

            frameList.sort((o1, o2) -> {
                String name1 = ((JSONObject) o1).get("filename").toString();
                String name2 = ((JSONObject) o2).get("filename").toString();

                return name1.compareToIgnoreCase(name2);
            });
        }

        for(int i = 0; i < frameList.size(); i++)
        {
            JSONObject frameData = (JSONObject) frameList.get(i);

            String name = frameData.get("filename").toString();
            JSONObject frame = (JSONObject) frameData.get("frame");
            // boolean rotated = (Boolean) frameData.get("rotated"); // I don't currently frame rotation system.
            // boolean trimmed = (Boolean) frameData.get("trimmed");
            JSONObject spriteSourceSize = (JSONObject) frameData.get("spriteSourceSize");
            JSONObject sourceSize = (JSONObject) frameData.get("sourceSize");

            Rectangle rect = new Rectangle(
                ((Long) frame.get("x")).intValue(),
                ((Long) frame.get("y")).intValue(),
                ((Long) frame.get("w")).intValue(),
                ((Long) frame.get("h")).intValue()
            );

            frames.addAtlasFrame(
                rect, 
                new Point(
                    ((Long) sourceSize.get("w")).intValue(), 
                    ((Long) sourceSize.get("h")).intValue()), 
                new Point(
                    ((Long) spriteSourceSize.get("x")).intValue(), 
                    ((Long) spriteSourceSize.get("y")).intValue()), 
                name
            );
        }

        return frames;
    }

    /**
     * JSONs will sometimes have weird data at the beginning and ends. 
     * We don't want this.
     * 
     * @param content   JSON data to be checked.
     */
    private static void trimJsonContent(StringBuilder content)
    {
        while(content.charAt(0) != '{')
            content.deleteCharAt(0);
        while(content.charAt(content.length() - 1) != '}')
            content.deleteCharAt(content.length() - 1);
    }

    /**
     * Constructs a new JBurstAtlasFrames.
     * 
     * @param graphic   parent graphic to be used by this frame collection.
     */
    private JBurstAtlasFrames(JBurstGraphic graphic)
    {
        super(graphic);
    }
}
