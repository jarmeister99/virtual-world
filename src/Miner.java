import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Miner implements Entity, Animated, Active{

    public String kind;
    public Point position;
    public List<PImage> images;
    public int imageIndex;
    public int actionPeriod;

    private String id;
    private int resourceLimit;
    private int resourceCount;
    private int animationPeriod;

    public Miner(String id, Point position,
                  List<PImage> images, int resourceLimit, int resourceCount,
                  int actionPeriod, int animationPeriod) {
        this.kind = "MINER";
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



    public void executeMinerNotFullActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> notFullTarget = world.findNearest(this.position, "ORE");

        if (!notFullTarget.isPresent() ||
                !moveToNotFull(world, notFullTarget.get(), scheduler) ||
                !transformNotFull(world, scheduler, imageStore)) {
            scheduler.scheduleEvent(this, Entity.createActivityAction(world, imageStore, this), this.actionPeriod);
        }
    }

    private boolean moveToNotFull(WorldModel world, Entity target, EventScheduler scheduler) {

        // if the miner is adjacent to the target
        if (Point.adjacent(this.position, target.getPosition())) {

            // increment the miner's resources
            this.resourceCount += 1;

            // remove the target
            world.removeEntity(target);

            // unschedule all of the target's events
            scheduler.unscheduleAllEvents(target);

            return true;
        } else {

            // move the miner towards the target
            Point nextPos = nextPositionMiner(world, target.getPosition());

            // if the miner isn't already at the next spot they should be
            if (!this.position.equals(nextPos)) {

                // current occupant is evicted
                Optional<Entity> occupant = world.getOccupant(nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents(occupant.get());
                }

                // miner moves in
                world.moveEntity(this, nextPos);
            }
            return false;
        }
    }

    // TRANSFORM METHODS


    private boolean transformNotFull(WorldModel world,
                                     EventScheduler scheduler, ImageStore imageStore) {
        if (this.resourceCount >= this.resourceLimit) {
            Entity miner = Entity.createMinerFull(this.id, this.resourceLimit,
                    this.position, this.actionPeriod, this.animationPeriod,
                    this.images);

            world.removeEntity(this);
            scheduler.unscheduleAllEvents(this);

            world.addEntity(miner);
            scheduler.scheduleActions(miner, world, imageStore);

            return true;
        }

        return false;
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
    public void executeActivity(WorldModel world,
                                ImageStore imageStore, EventScheduler scheduler) {
        executeMinerNotFullActivity(world, imageStore, scheduler);
    }

    @Override
    public int getActionPeriod() {
        return this.actionPeriod;
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
