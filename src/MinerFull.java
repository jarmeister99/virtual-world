import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class MinerFull extends Miner {


    public MinerFull(Point position, List<PImage> images, int resourceLimit, int resourceCount, int actionPeriod, int animationPeriod) {
        super(position, images, resourceLimit, resourceCount, actionPeriod, animationPeriod);
    }

    public <R> R accept(EntityVisitor<R> visitor){
        return visitor.visit(this);
    }

    public void executeActivity(WorldModel world,
                                ImageStore imageStore, EventScheduler scheduler) {
        executeMinerFullActivity(world, imageStore, scheduler);
    }

    public void executeMinerFullActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> fullTarget = world.findNearest(getPosition(), "BLACKSMITH");

        if (fullTarget.isPresent() && moveTo(world, fullTarget.get(), scheduler)) {
            transformFull(world, scheduler, imageStore);
        } else {
            scheduler.scheduleEvent(this, Entity.createActivityAction(world, imageStore, this), getActionPeriod());
        }
    }


    private boolean moveTo(WorldModel world,
                           Entity target, EventScheduler scheduler) {
        if (Point.adjacent(getPosition(), target.getPosition())) {
            return true;
        } else {
            Point nextPos = nextPositionMiner(world, target.getPosition());

            move(nextPos, world, scheduler);
            return false;
        }
    }


    private void transformFull(WorldModel world,
                               EventScheduler scheduler, ImageStore imageStore) {
        // the active miner becomes an empty miner
        Entity miner = Entity.createMinerNotFull(getResourceLimit(),
                getPosition(), getActionPeriod(), getAnimationPeriod(),
                getImages());

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        world.addEntity(miner);
        scheduler.scheduleActions(miner, world, imageStore);
    }


}
