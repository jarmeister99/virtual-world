import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Ore implements Entity, Active {
    public String kind;
    public Point position;
    public List<PImage> images;
    public int imageIndex;
    public int actionPeriod;

    private String id;
    private int resourceLimit;
    private int resourceCount;
    private int animationPeriod;

    public Ore(String id, Point position,
                  List<PImage> images, int resourceLimit, int resourceCount,
                  int actionPeriod, int animationPeriod) {
        this.kind = "ORE";
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
        this.resourceLimit = resourceLimit;
        this.resourceCount = resourceCount;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
    }


    public void nextImage() {
        this.imageIndex = (this.imageIndex + 1) % this.images.size();
    }

    public void executeOreActivity(WorldModel world,
                                   ImageStore imageStore, EventScheduler scheduler) {
        Point pos = this.position;  // store current position before removing

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        Entity blob = Entity.createOreBlob(this.id + BLOB_ID_SUFFIX,
                pos, this.actionPeriod / BLOB_PERIOD_SCALE,
                BLOB_ANIMATION_MIN +
                        rand.nextInt(BLOB_ANIMATION_MAX - BLOB_ANIMATION_MIN),
                imageStore.getImageList(BLOB_KEY));

        world.addEntity(blob);
        scheduler.scheduleActions(blob, world, imageStore);
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
        executeOreActivity(world, imageStore, scheduler);
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
