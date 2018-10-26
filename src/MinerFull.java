import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class MinerFull implements Entity, Animated, Active {
    public String kind;
    public Point position;
    public List<PImage> images;
    public int imageIndex;
    public int actionPeriod;

    private String id;
    private int resourceLimit;
    private int resourceCount;
    private int animationPeriod;

    public MinerFull(String id, Point position,
                 List<PImage> images, int resourceLimit, int resourceCount,
                 int actionPeriod, int animationPeriod) {
        this.kind = "MINERFULL";
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.resourceLimit = resourceLimit;
        this.resourceCount = resourceCount;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
    }

    public int getAnimationPeriod() {
        return this.animationPeriod;
    }

    @Override
    public int getRepeatCount() {
        return 0;
    }

    public void nextImage() {
        this.imageIndex = (this.imageIndex + 1) % this.images.size();
    }


    public void executeMinerFullActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> fullTarget = world.findNearest(this.position, "BLACKSMITH");

        if (fullTarget.isPresent() && moveToFull(world, fullTarget.get(), scheduler)) {
            transformFull(world, scheduler, imageStore);
        } else {
            scheduler.scheduleEvent(this, Entity.createActivityAction(world, imageStore, this), this.actionPeriod);
        }
    }


    private boolean moveToFull(WorldModel world,
                               Entity target, EventScheduler scheduler) {
        if (Point.adjacent(this.position, target.getPosition())) {
            return true;
        } else {
            Point nextPos = nextPositionMiner(world, target.getPosition());

            if (!this.position.equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant(nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents(occupant.get());
                }

                world.moveEntity(this, nextPos);
            }
            return false;
        }
    }

    private void transformFull(WorldModel world,
                               EventScheduler scheduler, ImageStore imageStore) {
        // the active miner becomes an empty miner
        Entity miner = Entity.createMinerNotFull(this.id, this.resourceLimit,
                this.position, this.actionPeriod, this.animationPeriod,
                this.images);

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        world.addEntity(miner);
        scheduler.scheduleActions(miner, world, imageStore);
    }

    private Point nextPositionMiner(WorldModel world, Point destPos) {
        int horiz = Integer.signum(destPos.x - this.position.x);

        Point newPos = new Point(this.position.x + horiz,
                this.position.y);

        if (horiz == 0 || world.isOccupied(newPos)) {

            int vert = Integer.signum(destPos.y - this.position.y);

            newPos = new Point(this.position.x, this.position.y + vert);

            if (vert == 0 || world.isOccupied(newPos)) {

                newPos = this.position;
            }
        }

        return newPos;
    }

    @Override
    public Point getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(Point point) {
        this.position = point;
    }

    @Override
    public String getKind() {
        return this.kind;
    }

    @Override
    public int getActionPeriod() {
        return this.actionPeriod;
    }

    @Override
    public void executeActivity(WorldModel world,
                                ImageStore imageStore, EventScheduler scheduler) {
        executeMinerFullActivity(world, imageStore, scheduler);
    }

    @Override
    public List<PImage> getImages(){
        return this.images;
    }

    @Override
    public int getImageIndex(){
        return this.imageIndex;
    }
}
