import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Quake extends Entity implements Animated, Active {

    public static final String QUAKE_KEY = "quake";
    public static final int QUAKE_ACTION_PERIOD = 1100;
    public static final int QUAKE_ANIMATION_PERIOD = 100;

    private int actionPeriod;
    private int animationPeriod;



    public Quake(Point position, List<PImage> images, int actionPeriod, int animationPeriod) {
        super(position, "QUAKE", images, 0);
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
        executeQuakeActivity(world, imageStore, scheduler);
    }

    private void executeQuakeActivity(WorldModel world,
                                      ImageStore imageStore, EventScheduler scheduler) {
        scheduler.unscheduleAllEvents(this);
        world.removeEntity(this);
    }
    public <R> R accept(EntityVisitor<R> visitor){
        return visitor.visit(this);
    }


}
