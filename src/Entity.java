import java.util.List;
import java.util.Optional;
import java.util.Random;

import processing.core.PImage;

interface Entity {

    public static final int ORE_REACH = 1;
    public static final int PROPERTY_KEY = 0;

    public static final String MINER_KEY = "miner";
    public static final int MINER_NUM_PROPERTIES = 7;
    public static final int MINER_ID = 1;
    public static final int MINER_COL = 2;
    public static final int MINER_ROW = 3;
    public static final int MINER_LIMIT = 4;
    public static final int MINER_ACTION_PERIOD = 5;
    public static final int MINER_ANIMATION_PERIOD = 6;

    public static final String OBSTACLE_KEY = "obstacle";
    public static final int OBSTACLE_NUM_PROPERTIES = 4;
    public static final int OBSTACLE_ID = 1;
    public static final int OBSTACLE_COL = 2;
    public static final int OBSTACLE_ROW = 3;

    public static final String BLOB_KEY = "blob";
    public static final String BLOB_ID_SUFFIX = " -- blob";
    public static final int BLOB_PERIOD_SCALE = 4;
    public static final int BLOB_ANIMATION_MIN = 50;
    public static final int BLOB_ANIMATION_MAX = 150;

    public static final String ORE_ID_PREFIX = "ore -- ";
    public static final int ORE_CORRUPT_MIN = 20000;
    public static final int ORE_CORRUPT_MAX = 30000;

    public static final String ORE_KEY = "ore";
    public static final int ORE_NUM_PROPERTIES = 5;
    public static final int ORE_ID = 1;
    public static final int ORE_COL = 2;
    public static final int ORE_ROW = 3;
    public static final int ORE_ACTION_PERIOD = 4;

    public static final String SMITH_KEY = "blacksmith";
    public static final int SMITH_NUM_PROPERTIES = 4;
    public static final int SMITH_ID = 1;
    public static final int SMITH_COL = 2;
    public static final int SMITH_ROW = 3;

    public static final String VEIN_KEY = "vein";
    public static final int VEIN_NUM_PROPERTIES = 5;
    public static final int VEIN_ID = 1;
    public static final int VEIN_COL = 2;
    public static final int VEIN_ROW = 3;
    public static final int VEIN_ACTION_PERIOD = 4;

    static final String QUAKE_KEY = "quake";
    static final String QUAKE_ID = "quake";
    static final int QUAKE_ACTION_PERIOD = 1100;
    static final int QUAKE_ANIMATION_PERIOD = 100;




    public static final Random rand = new Random();


    public Point getPosition();

    public void setPosition(Point point);

    public String getKind();

    public void nextImage();

    public List<PImage> getImages();

    public int getImageIndex();

    public static Action createAnimationAction(int repeatCount, Entity entity) {
        return new AnimationAction(entity, null, null, repeatCount);
    }

    public static Action createActivityAction(WorldModel world,
                                              ImageStore imageStore, Entity entity) {
        return new ActivityAction(entity, world, imageStore, 0);
    }

    public static Optional<Entity> nearestEntity(List<Entity> entities, Point pos) {
        if (entities.isEmpty()) {
            return Optional.empty();
        } else {
            Entity nearest = entities.get(0);
            int nearestDistance = Point.distanceSquared(nearest.getPosition(), pos);

            for (Entity other : entities) {
                int otherDistance = Point.distanceSquared(other.getPosition(), pos);

                if (otherDistance < nearestDistance) {
                    nearest = other;
                    nearestDistance = otherDistance;
                }
            }

            return Optional.of(nearest);
        }
    }

    public static Entity createBlacksmith(String id, Point position,
                                          List<PImage> images) {
        return new Blacksmith(id, position, images,
                0, 0, 0, 0);
    }

    public static Entity createMinerFull(String id, int resourceLimit,
                                         Point position, int actionPeriod, int animationPeriod,
                                         List<PImage> images) {
        return new MinerFull(id, position, images,
                resourceLimit, resourceLimit, actionPeriod, animationPeriod);
    }

    public static Entity createMinerNotFull(String id, int resourceLimit,
                                            Point position, int actionPeriod, int animationPeriod,
                                            List<PImage> images) {
        return new Miner(id, position, images,
                resourceLimit, 0, actionPeriod, animationPeriod);
    }

    public static Entity createObstacle(String id, Point position,
                                        List<PImage> images) {
        return new Obstacle(id, position, images,
                0, 0, 0, 0);
    }

    public static Entity createOre(String id, Point position, int actionPeriod,
                                   List<PImage> images) {
        return new Ore(id, position, images, 0, 0,
                actionPeriod, 0);
    }

    public static Entity createOreBlob(String id, Point position,
                                       int actionPeriod, int animationPeriod, List<PImage> images) {
        return new OreBlob(id, position, images,
                0, 0, actionPeriod, animationPeriod);
    }

    public static Entity createQuake(Point position, List<PImage> images) {
        return new Quake(QUAKE_ID, position, images,
                0, 0, QUAKE_ACTION_PERIOD, QUAKE_ANIMATION_PERIOD);
    }

    public static Entity createVein(String id, Point position, int actionPeriod,
                                    List<PImage> images) {
        return new Vein(id, position, images, 0, 0,
                actionPeriod, 0);
    }

}
