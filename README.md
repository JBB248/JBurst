# JBurst

### What is it?
JBurst is a non-revolutionary addition to [Java Swing](https://docs.oracle.com/javase/tutorial/uiswing) modeled from [HaxeFlixel](https://haxeflixel.com) that adds sprites with animation and simple transformation methods.

### Why did I make it?
I always found it frustrating how JLabel only really supported static images.

(This was once intended to be a very basic game engine, but I've since lost interest)

### Notes
- Layout managers cause the sprites to behave wierdly, so they usually have to be deactivated.

- I worked on this in my free time during high school so it may have problems here and there. If you find one, report it! I may look into it.

## How do I use it?
### Requirements
JBurst requires a JDK of at least 1.8.

### Packages
The binaries for the latest release can be found in the [releases](https://github.com/JBB248/JBurst/releases/tag/v0.5.1) tab.

### Maven
Maven users will need to add a few extra nodes to their `pom.xml` in order to use it:

- First, this repository's `mvn-repo` branch must be included as a repository
```xml
<repositories>
  ...
  <repository>
    <id>jburst-home</id>
    <name>jburst-home</name>
    <url>https://raw.github.com/jbb248/jburst/mvn-repo/</url>

    <releases>
      <!-- Checksums haven't been working, so this ignores a long-winded warning. -->
      <checksumPolicy>ignore</checksumPolicy>
    </releases>
  </repository>
</repositories>
```

- Second, the package must be included as a dependency
```xml
<dependencies>
  ...
  <dependency>
    <groupId>com.github.jbb248</groupId>
    <artifactId>jburst</artifactId>
    <version>0.5.1</version>
  </dependency>
</dependencies>
```
*Note: Due to the nature of the maven repository, only one version can be hosted at a time. As JBurst recieves updates, older versions will no longer be available through this method and will need to be added ***locally***. [This stack overflow question](https://stackoverflow.com/questions/4955635/how-to-add-local-jar-files-to-a-maven-project) should be able to relieve any confusion on how to do that.*

### Use
Then it's as simple as:
```java
import javax.swing.JFrame;
import com.github.jbb248.jburst.JBurstSprite;

public class App
{
    public static void main(String[] args)
    {
        JFrame window = new JFrame("Example");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(500, 500);
        window.setLayout(null); // This line is important!

        int x = 50, y = 50;
        int frameWidth = 100, frameHeight = 150;
        int[] frameIndices = new int[] {0, 1, 2, 3};
        int frameRate = 30;
        boolean looped = true;

        JBurstSprite sprite = new JBurstSprite(x, y);
        sprite.loadAnimatedGraphic("my-spritesheet.png", frameWidth, frameHeight);
        sprite.animation.add("idle", frameIndices, frameRate, looped);
        sprite.animation.play("idle");
        sprite.start();

        window.add(sprite);
        window.setVisible(true);
    }
}
```

### Demos
- A more in depth example using JBurst can be found within [demo.zip](https://github.com/JBB248/JBurst/blob/main/demo.zip).

- [Java Swing Tutorials](https://docs.oracle.com/javase/tutorial/uiswing)

## Dependencies
- JBurst uses [json-simple-1.1.1](https://github.com/fangyidong/json-simple) for json support.

## Helping
There are things that I definitely do not know about Swing and other things that I'm unfamiliar with regarding Java. So, if you have any suggestions (or warnings) let me hear them.

# HaxeFlixel
If you enjoy using this, give [HaxeFlixel](https://haxeflixel.com) a shot. A large amount of JBurst's animation system is modeled on their game engine, so it would feel wrong not to mention them.
