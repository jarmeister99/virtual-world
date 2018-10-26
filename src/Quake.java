import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Quake implements Entity, Animated, Active {
    public String kind;
    public Point position;
    public List<PImage> images;
    public int imageIndex;
    public int actionPeriod;

    private String id;
    private int resourceLimit;
    private int resourceCount;
    private int animationPeriod;



    public Quake(String id, Point position,
                  List<PImage> images, int resourceLimit, int resourceCount,
                  int actionPeriod, int animationPeriod) {
        this.kind = "QUAKE";
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

    public void executeQuakeActivity(WorldModel world,
                                     ImageStore imageStore, EventScheduler scheduler) {
        scheduler.unscheduleAllEvents(this);
        world.removeEntity(this);
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
    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        executeQuakeActivity(world, imageStore, scheduler);
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
