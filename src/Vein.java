import processing.core.PImage;

import java.util.List;
import java.util.Optional;

public class Vein implements Entity, Active {
    public String kind;
    public Point position;
    public List<PImage> images;
    public int imageIndex;
    public int actionPeriod;

    private String id;
    private int resourceLimit;
    private int resourceCount;
    private int animationPeriod;

    public Vein(String id, Point position,
                  List<PImage> images, int resourceLimit, int resourceCount,
                  int actionPeriod, int animationPeriod) {
        this.kind = "VEIN";
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

    public void executeVeinActivity(WorldModel world,
                                    ImageStore imageStore, EventScheduler scheduler) {
        Optional<Point> openPt = world.findOpenAround(this.position);

        if (openPt.isPresent()) {
            Entity ore = Entity.createOre(ORE_ID_PREFIX + this.id,
                    openPt.get(), ORE_CORRUPT_MIN +
                            rand.nextInt(ORE_CORRUPT_MAX - ORE_CORRUPT_MIN),
                    imageStore.getImageList(ORE_KEY));
            world.addEntity(ore);
            scheduler.scheduleActions(ore, world, imageStore);
        }

        scheduler.scheduleEvent(this,
                Entity.createActivityAction(world, imageStore, this),
                this.actionPeriod);
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
        executeVeinActivity(world, imageStore, scheduler);
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
