import java.util.List;
import java.util.Optional;
import java.util.Random;

import processing.core.PImage;

abstract class Entity {

    public static final int PROPERTY_KEY = 0;

    private Point position;
    private String kind;
    private List<PImage> images;
    private int imageIndex;

    public static final Random rand = new Random();

    public Entity(Point position, String kind, List<PImage> images, int imageIndex){
        this.position = position;
        this.kind = kind;
        this.images = images;
        this.imageIndex = imageIndex;
    }

    public abstract <R> R accept(EntityVisitor<R> visitor);

    public Point getPosition(){
        return this.position;
    }

    public void setPosition(Point point){
        this.position = point;
    }

    public String getKind(){
        return this.kind;
    }

    public void nextImage() {
        this.imageIndex = (this.imageIndex + 1) % this.images.size();
    }

    public List<PImage> getImages(){
        return this.images;
    }

    public int getImageIndex(){
        return this.imageIndex;
    }

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

    public void move(Point nextPos, WorldModel world, EventScheduler scheduler){
        if (!getPosition().equals(nextPos)) {
            Optional<Entity> occupant = world.getOccupant(nextPos);
            occupant.ifPresent(scheduler::unscheduleAllEvents);
            world.moveEntity(this, nextPos);
        }
    }

    public static Entity createBlacksmith(Point position, List<PImage> images) {
        return new Blacksmith(position, images);
    }

    public static Entity createMinerFull(int resourceLimit, Point position, int actionPeriod, int animationPeriod, List<PImage> images) {
        return new MinerFull(position, images, resourceLimit, resourceLimit, actionPeriod, animationPeriod);
    }

    public static Entity createMinerNotFull(int resourceLimit, Point position, int actionPeriod, int animationPeriod, List<PImage> images) {
        return new Miner(position, images,
                resourceLimit, 0, actionPeriod, animationPeriod);
    }

    public static Entity createObstacle(Point position, List<PImage> images) {
        return new Obstacle(position, images);
    }

    public static Entity createOre(Point position, int actionPeriod, List<PImage> images) {
        return new Ore(position, images, actionPeriod);
    }

    public static Entity createOreBlob(Point position, int actionPeriod, int animationPeriod, List<PImage> images) {
        return new OreBlob(position, images, actionPeriod, animationPeriod);
    }

    public static Entity createQuake(Point position, List<PImage> images) {
        return new Quake(position, images, Quake.QUAKE_ACTION_PERIOD, Quake.QUAKE_ANIMATION_PERIOD);
    }

    public static Entity createVein(Point position, int actionPeriod, List<PImage> images) {
        return new Vein(position, images, actionPeriod);
    }

}
