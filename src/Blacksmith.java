import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Blacksmith extends Entity {

    public static final String SMITH_KEY = "blacksmith";
    public static final int SMITH_NUM_PROPERTIES = 4;
    public static final int SMITH_COL = 2;
    public static final int SMITH_ROW = 3;

    public Blacksmith(Point position, List<PImage> images) {
        super(position, "BLACKSMITH", images, 0);
    }

    public <R> R accept(EntityVisitor<R> visitor){
        return visitor.visit(this);
    }
}
