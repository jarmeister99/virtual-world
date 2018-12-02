import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Ore extends Entity implements Active {

    public static final int ORE_CORRUPT_MIN = 20000;
    public static final int ORE_CORRUPT_MAX = 30000;
    public static final String ORE_KEY = "ore";
    public static final int ORE_NUM_PROPERTIES = 5;
    public static final int ORE_COL = 2;
    public static final int ORE_ROW = 3;
    public static final int ORE_ACTION_PERIOD = 4;

    public int actionPeriod;


    public Ore(Point position, List<PImage> images, int actionPeriod) {
        super(position, "ORE", images, 0);
        this.actionPeriod = actionPeriod;
    }

    // ACTIVE

    public int getActionPeriod() {
        return this.actionPeriod;
    }
    public void executeActivity(WorldModel world,
                                ImageStore imageStore, EventScheduler scheduler) {
        executeOreActivity(world, imageStore, scheduler);
    }

    private void executeOreActivity(WorldModel world,
                                    ImageStore imageStore, EventScheduler scheduler) {
        Point pos = getPosition();  // store current position before removing

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        Entity blob = Entity.createOreBlob(pos, this.actionPeriod / OreBlob.BLOB_PERIOD_SCALE, OreBlob.BLOB_ANIMATION_MIN + rand.nextInt(OreBlob.BLOB_ANIMATION_MAX - OreBlob.BLOB_ANIMATION_MIN), imageStore.getImageList(OreBlob.BLOB_KEY));

        world.addEntity(blob);
        scheduler.scheduleActions(blob, world, imageStore);
    }
    public <R> R accept(EntityVisitor<R> visitor){
        return visitor.visit(this);
    }

}
