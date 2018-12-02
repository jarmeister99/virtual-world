import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class OreBlob extends Entity implements Animated, Active {

    public static final String BLOB_KEY = "blob";
    public static final int BLOB_PERIOD_SCALE = 4;
    public static final int BLOB_ANIMATION_MIN = 50;
    public static final int BLOB_ANIMATION_MAX = 150;

    private int actionPeriod;
    private int animationPeriod;

    public OreBlob(Point position, List<PImage> images, int actionPeriod, int animationPeriod) {
        super(position, "OREBLOB", images, 0);
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
    }

    // ANIMATED

    public int getAnimationPeriod() {
        return this.animationPeriod;
    }
    public int getRepeatCount() {
        return 0;
    }

    // ACTIVE

    public int getActionPeriod() {
        return this.actionPeriod;
    }
    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        executeOreBlobActivity(world, imageStore, scheduler);
    }

    private void executeOreBlobActivity(WorldModel world,
                                        ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> blobTarget = world.findNearest(getPosition(), "VEIN");
        long nextPeriod = this.actionPeriod;

        if (blobTarget.isPresent()) {
            Point tgtPos = blobTarget.get().getPosition();

            if (moveToOreBlob(world, blobTarget.get(), scheduler)) {
                Entity quake = Entity.createQuake(tgtPos, imageStore.getImageList(Quake.QUAKE_KEY));

                world.addEntity(quake);
                nextPeriod += this.actionPeriod;
                scheduler.scheduleActions(quake, world, imageStore);
            }
        }

        scheduler.scheduleEvent(this, Entity.createActivityAction(world, imageStore, this), nextPeriod);
    }


    private boolean moveToOreBlob(WorldModel world, Entity target, EventScheduler scheduler) {
        if (Point.adjacent(getPosition(), target.getPosition())) {
            world.removeEntity(target);
            scheduler.unscheduleAllEvents(target);
            return true;
        } else {
            Point nextPos = nextPositionOreBlob(world, target.getPosition());

            move(nextPos, world, scheduler);
            return false;
        }
    }

    private Point nextPositionOreBlob(WorldModel world,
                                      Point destPos) {
        int horiz = Integer.signum(destPos.x - getPosition().x);
        Point newPos = new Point(getPosition().x + horiz,
                getPosition().y);

        Optional<Entity> occupant = world.getOccupant(newPos);

        if (horiz == 0 ||
                (occupant.isPresent() && !(occupant.get().getKind() == "ORE"))) {
            int vert = Integer.signum(destPos.y - getPosition().y);
            newPos = new Point(getPosition().x, getPosition().y + vert);
            occupant = world.getOccupant(newPos);

            if (vert == 0 ||
                    (occupant.isPresent() && !(occupant.get().getKind() == "ORE"))) {
                newPos = getPosition();
            }
        }

        return newPos;
    }
    public <R> R accept(EntityVisitor<R> visitor){
        return visitor.visit(this);
    }

}
