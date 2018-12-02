import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Obstacle extends Entity {

    public static final String OBSTACLE_KEY = "obstacle";
    public static final int OBSTACLE_NUM_PROPERTIES = 4;
    public static final int OBSTACLE_COL = 2;
    public static final int OBSTACLE_ROW = 3;

    public Obstacle(Point position, List<PImage> images) {
        super(position, "OBSTACLE", images, 0);
    }
    public <R> R accept(EntityVisitor<R> visitor){
        return visitor.visit(this);
    }
}
