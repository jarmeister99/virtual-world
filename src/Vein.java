import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Vein extends Entity implements Active {

    public static final String VEIN_KEY = "vein";
    public static final int VEIN_NUM_PROPERTIES = 5;
    public static final int VEIN_COL = 2;
    public static final int VEIN_ROW = 3;
    public static final int VEIN_ACTION_PERIOD = 4;

    private int actionPeriod;

    public Vein(Point position, List<PImage> images, int actionPeriod) {
        super(position, "VEIN", images, 0);
        this.actionPeriod = actionPeriod;
    }

    // ACTIVE

    public int getActionPeriod() {
        return this.actionPeriod;
    }
    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        executeVeinActivity(world, imageStore, scheduler);
    }

    private void executeVeinActivity(WorldModel world,
                                    ImageStore imageStore, EventScheduler scheduler) {
        Optional<Point> openPt = world.findOpenAround(getPosition());

        if (openPt.isPresent()) {
            Entity ore = Entity.createOre(openPt.get(), Ore.ORE_CORRUPT_MIN + rand.nextInt(Ore.ORE_CORRUPT_MAX - Ore.ORE_CORRUPT_MIN), imageStore.getImageList(Ore.ORE_KEY));
            world.addEntity(ore);
            scheduler.scheduleActions(ore, world, imageStore);
        }

        scheduler.scheduleEvent(this, Entity.createActivityAction(world, imageStore, this), this.actionPeriod);
    }
    public <R> R accept(EntityVisitor<R> visitor){
        return visitor.visit(this);
    }
}
