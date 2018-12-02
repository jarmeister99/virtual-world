import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Miner extends Entity implements Animated, Active{

    public static final String MINER_KEY = "miner";
    public static final int MINER_NUM_PROPERTIES = 7;
    public static final int MINER_COL = 2;
    public static final int MINER_ROW = 3;
    public static final int MINER_LIMIT = 4;
    public static final int MINER_ACTION_PERIOD = 5;
    public static final int MINER_ANIMATION_PERIOD = 6;
    public static final int ORE_REACH = 1;

    private int resourceLimit;
    private int resourceCount;
    private int animationPeriod;
    private int actionPeriod;

    public Miner(Point position,
                  List<PImage> images, int resourceLimit, int resourceCount,
                  int actionPeriod, int animationPeriod) {

        super(position, "MINER", images, 0);
        this.resourceLimit = resourceLimit;
        this.resourceCount = resourceCount;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
    }

    public <R> R accept(EntityVisitor<R> visitor){
        return visitor.visit(this);
    }

    public int getResourceLimit() {
        return resourceLimit;
    }

    public int getResourceCount() {
        return resourceCount;
    }
    // ANIMATED

    public int getAnimationPeriod() {
        return this.animationPeriod;
    }
    public int getRepeatCount() {
        return 0;
    }

    // ACTIVE

    public void executeActivity(WorldModel world,
                                ImageStore imageStore, EventScheduler scheduler) {
        executeMinerNotFullActivity(world, imageStore, scheduler);
    }

    public int getActionPeriod() {
        return this.actionPeriod;
    }



    private void executeMinerNotFullActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> notFullTarget = world.findNearest(getPosition(), "ORE");

        if (!notFullTarget.isPresent() ||
                !moveTo(world, notFullTarget.get(), scheduler) ||
                !transformNotFull(world, scheduler, imageStore)) {
            scheduler.scheduleEvent(this, Entity.createActivityAction(world, imageStore, this), this.actionPeriod);
        }
    }

    private boolean moveTo(WorldModel world, Entity target, EventScheduler scheduler) {

        // if the miner is adjacent to the target
        if (Point.adjacent(getPosition(), target.getPosition())) {

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
            move(nextPos, world, scheduler);
            return false;
        }
    }

    private boolean transformNotFull(WorldModel world,
                                     EventScheduler scheduler, ImageStore imageStore) {
        if (this.resourceCount >= this.resourceLimit) {
            Entity miner = Entity.createMinerFull(this.resourceLimit,
                    getPosition(), this.actionPeriod, this.animationPeriod,
                    getImages());

            world.removeEntity(this);
            scheduler.unscheduleAllEvents(this);

            world.addEntity(miner);
            scheduler.scheduleActions(miner, world, imageStore);

            return true;
        }

        return false;
    }

    public Point nextPositionMiner(WorldModel world, Point destPos) {
        int horiz = Integer.signum(destPos.x - this.getPosition().x);

        Point newPos = new Point(this.getPosition().x + horiz,
                this.getPosition().y);

        if (horiz == 0 || world.isOccupied(newPos)) {

            int vert = Integer.signum(destPos.y - this.getPosition().y);

            newPos = new Point(this.getPosition().x, this.getPosition().y + vert);

            if (vert == 0 || world.isOccupied(newPos)) {

                newPos = this.getPosition();
            }
        }

        return newPos;
    }


}
