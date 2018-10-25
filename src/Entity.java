import java.util.List;
import java.util.Optional;
import java.util.Random;

import processing.core.PImage;

interface Entity {

    static final String BLOB_KEY = "blob";
    static final String BLOB_ID_SUFFIX = " -- blob";
    static final int BLOB_PERIOD_SCALE = 4;
    static final int BLOB_ANIMATION_MIN = 50;
    static final int BLOB_ANIMATION_MAX = 150;

    static final String QUAKE_KEY = "quake";
    static final String QUAKE_ID = "quake";
    static final int QUAKE_ACTION_PERIOD = 1100;
    static final int QUAKE_ANIMATION_PERIOD = 100;

    static final String ORE_ID_PREFIX = "ore -- ";
    static final int ORE_CORRUPT_MIN = 20000;
    static final int ORE_CORRUPT_MAX = 30000;
    static final String ORE_KEY = "ore";


    static final Random rand = new Random();

//    public Action createAnimationAction(int repeatCount) {
//        return new AnimationAction(ActionKind.ANIMATION, this, null, null, repeatCount);
//    }
//
//    public Action createActivityAction(WorldModel world,
//                                       ImageStore imageStore) {
//        return new ActivityAction(ActionKind.ACTIVITY, this, world, imageStore, 0);
//    }



    public static Optional<Entity> nearestEntity(List<Entity> entities, Point pos) {
        if (entities.isEmpty()) {
            return Optional.empty();
        } else {
            Entity nearest = entities.get(0);
            int nearestDistance = Point.distanceSquared(nearest.position, pos);

            for (Entity other : entities) {
                int otherDistance = Point.distanceSquared(other.position, pos);

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
        return new Blacksmith(EntityKind.BLACKSMITH, id, position, images,
                0, 0, 0, 0);
    }

    public static Entity createMinerFull(String id, int resourceLimit,
                                         Point position, int actionPeriod, int animationPeriod,
                                         List<PImage> images) {
        return new Miner(EntityKind.MINER_FULL, id, position, images,
                resourceLimit, resourceLimit, actionPeriod, animationPeriod);
    }

    public static Entity createMinerNotFull(String id, int resourceLimit,
                                            Point position, int actionPeriod, int animationPeriod,
                                            List<PImage> images) {
        return new Miner(EntityKind.MINER_NOT_FULL, id, position, images,
                resourceLimit, 0, actionPeriod, animationPeriod);
    }

    public static Entity createObstacle(String id, Point position,
                                        List<PImage> images) {
        return new Obstacle(EntityKind.OBSTACLE, id, position, images,
                0, 0, 0, 0);
    }

    public static Entity createOre(String id, Point position, int actionPeriod,
                                   List<PImage> images) {
        return new Ore(EntityKind.ORE, id, position, images, 0, 0,
                actionPeriod, 0);
    }

    public static Entity createOreBlob(String id, Point position,
                                       int actionPeriod, int animationPeriod, List<PImage> images) {
        return new OreBlob(EntityKind.ORE_BLOB, id, position, images,
                0, 0, actionPeriod, animationPeriod);
    }

    public static Entity createQuake(Point position, List<PImage> images) {
        return new Quake(EntityKind.QUAKE, QUAKE_ID, position, images,
                0, 0, QUAKE_ACTION_PERIOD, QUAKE_ANIMATION_PERIOD);
    }

    public static Entity createVein(String id, Point position, int actionPeriod,
                                    List<PImage> images) {
        return new Vein(EntityKind.VEIN, id, position, images, 0, 0,
                actionPeriod, 0);
    }

}
