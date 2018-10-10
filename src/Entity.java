import java.util.List;
import java.util.Optional;
import java.util.Random;

import processing.core.PImage;

final class Entity {
    public EntityKind kind;
    public String id;
    public Point position;
    public List<PImage> images;
    public int imageIndex;
    public int resourceLimit;
    public int resourceCount;
    public int actionPeriod;
    public int animationPeriod;


    public static final Random rand = new Random();

    public Entity(EntityKind kind, String id, Point position,
                  List<PImage> images, int resourceLimit, int resourceCount,
                  int actionPeriod, int animationPeriod) {
        this.kind = kind;
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
        switch (this.kind) {
            case MINER_FULL:
            case MINER_NOT_FULL:
            case ORE_BLOB:
            case QUAKE:
                return this.animationPeriod;
            default:
                throw new UnsupportedOperationException(
                        String.format("getAnimationPeriod not supported for %s",
                                this.kind));
        }
    }

    public void nextImage() {
        this.imageIndex = (this.imageIndex + 1) % this.images.size();
    }

    public static Optional<Entity> nearestEntity(List<Entity> entities, Point pos) {
        if (entities.isEmpty()) {
            return Optional.empty();
        } else {
            Entity nearest = entities.get(0);
            int nearestDistance = Point.distanceSquared(nearest.position, pos);

            for (Entity other : entities) {
                int otherDistance = Point.distanceSquared(other.position, pos);

                if (otherDistance < nearestDistance) {
                    nearest = other;
                    nearestDistance = otherDistance;
                }
            }

            return Optional.of(nearest);
        }
    }

    // EXECUTE METHODS

    public void executeMinerFullActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> fullTarget = world.findNearest(this.position, EntityKind.BLACKSMITH);

        if (fullTarget.isPresent() && moveToFull(world, fullTarget.get(), scheduler)) {
            transformFull(world, scheduler, imageStore);
        } else {
            scheduler.scheduleEvent(this, createActivityAction(world, imageStore), this.actionPeriod);
        }
    }

    public void executeMinerNotFullActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> notFullTarget = world.findNearest(this.position,
                EntityKind.ORE);

        if (!notFullTarget.isPresent() ||
                !moveToNotFull(world, notFullTarget.get(), scheduler) ||
                !transformNotFull(world, scheduler, imageStore)) {
            scheduler.scheduleEvent(this, createActivityAction(world, imageStore), this.actionPeriod);
        }
    }

    public void executeOreActivity(WorldModel world,
                                   ImageStore imageStore, EventScheduler scheduler) {
        Point pos = this.position;  // store current position before removing

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        Entity blob = createOreBlob(this.id + Functions.BLOB_ID_SUFFIX,
                pos, this.actionPeriod / Functions.BLOB_PERIOD_SCALE,
                Functions.BLOB_ANIMATION_MIN +
                        rand.nextInt(Functions.BLOB_ANIMATION_MAX - Functions.BLOB_ANIMATION_MIN),
                imageStore.getImageList(Functions.BLOB_KEY));

        world.addEntity(blob);
        scheduler.scheduleActions(blob, world, imageStore);
    }

    public void executeOreBlobActivity(WorldModel world,
                                       ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> blobTarget = world.findNearest(this.position, EntityKind.VEIN);
        long nextPeriod = this.actionPeriod;

        if (blobTarget.isPresent()) {
            Point tgtPos = blobTarget.get().position;

            if (moveToOreBlob(world, blobTarget.get(), scheduler)) {
                Entity quake = createQuake(tgtPos, imageStore.getImageList(Functions.QUAKE_KEY));

                world.addEntity(quake);
                nextPeriod += this.actionPeriod;
                scheduler.scheduleActions(quake, world, imageStore);
            }
        }

        scheduler.scheduleEvent(this, createActivityAction(world, imageStore), nextPeriod);
    }

    public void executeQuakeActivity(WorldModel world,
                                     ImageStore imageStore, EventScheduler scheduler) {
        scheduler.unscheduleAllEvents(this);
        world.removeEntity(this);
    }

    public void executeVeinActivity(WorldModel world,
                                    ImageStore imageStore, EventScheduler scheduler) {
        Optional<Point> openPt = world.findOpenAround(this.position);

        if (openPt.isPresent()) {
            Entity ore = createOre(Functions.ORE_ID_PREFIX + this.id,
                    openPt.get(), Functions.ORE_CORRUPT_MIN +
                            rand.nextInt(Functions.ORE_CORRUPT_MAX - Functions.ORE_CORRUPT_MIN),
                    imageStore.getImageList(Functions.ORE_KEY));
            world.addEntity(ore);
            scheduler.scheduleActions(ore, world, imageStore);
        }

        scheduler.scheduleEvent(this,
                createActivityAction(world, imageStore),
                this.actionPeriod);
    }


    // MOVE METHODS


    public boolean moveToOreBlob(WorldModel world, Entity target, EventScheduler scheduler) {
        if (Point.adjacent(this.position, target.position)) {
            world.removeEntity(target);
            scheduler.unscheduleAllEvents(target);
            return true;
        } else {
            Point nextPos = nextPositionOreBlob(world, target.position);

            if (!this.position.equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant(nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents(occupant.get());
                }

                world.moveEntity(this, nextPos);
            }
            return false;
        }
    }

    public boolean moveToFull(WorldModel world,
                              Entity target, EventScheduler scheduler) {
        if (Point.adjacent(this.position, target.position)) {
            return true;
        } else {
            Point nextPos = nextPositionMiner(world, target.position);

            if (!this.position.equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant(nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents(occupant.get());
                }

                world.moveEntity(this, nextPos);
            }
            return false;
        }
    }

    public boolean moveToNotFull(WorldModel world,
                                 Entity target, EventScheduler scheduler) {

        // if the miner is adjacent to the target
        if (Point.adjacent(this.position, target.position)) {

            // increment the miner's resources
            this.resourceCount += 1;

            // remove the target
            world.removeEntity(target);

            // unschedule all of the target's events
            scheduler.unscheduleAllEvents(target);

            return true;
        } else {

            // move the miner towards the target
            Point nextPos = nextPositionMiner(world, target.position);

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

    public void transformFull(WorldModel world,
                              EventScheduler scheduler, ImageStore imageStore) {
        // the active miner becomes an empty miner
        Entity miner = createMinerNotFull(this.id, this.resourceLimit,
                this.position, this.actionPeriod, this.animationPeriod,
                this.images);

        world.removeEntity(this);
        scheduler.unscheduleAllEvents(this);

        world.addEntity(miner);
        scheduler.scheduleActions(miner, world, imageStore);
    }

    public boolean transformNotFull(WorldModel world,
                                    EventScheduler scheduler, ImageStore imageStore) {
        if (this.resourceCount >= this.resourceLimit) {
            // the active miner becomes a full miner
            Entity miner = createMinerFull(this.id, this.resourceLimit,
                    this.position, this.actionPeriod, this.animationPeriod,
                    this.images);

            // nobody will ever know the difference
            world.removeEntity(this);
            scheduler.unscheduleAllEvents(this);

            world.addEntity(miner);
            scheduler.scheduleActions(miner, world, imageStore);

            return true;
        }

        return false;
    }

    // CREATE METHODS

    public Action createAnimationAction(int repeatCount) {
        return new Action(ActionKind.ANIMATION, this, null, null, repeatCount);
    }

    public Action createActivityAction(WorldModel world,
                                       ImageStore imageStore) {
        return new Action(ActionKind.ACTIVITY, this, world, imageStore, 0);
    }

    public static Entity createBlacksmith(String id, Point position,
                                          List<PImage> images) {
        return new Entity(EntityKind.BLACKSMITH, id, position, images,
                0, 0, 0, 0);
    }

    public static Entity createMinerFull(String id, int resourceLimit,
                                         Point position, int actionPeriod, int animationPeriod,
                                         List<PImage> images) {
        return new Entity(EntityKind.MINER_FULL, id, position, images,
                resourceLimit, resourceLimit, actionPeriod, animationPeriod);
    }

    public static Entity createMinerNotFull(String id, int resourceLimit,
                                            Point position, int actionPeriod, int animationPeriod,
                                            List<PImage> images) {
        return new Entity(EntityKind.MINER_NOT_FULL, id, position, images,
                resourceLimit, 0, actionPeriod, animationPeriod);
    }

    public static Entity createObstacle(String id, Point position,
                                        List<PImage> images) {
        return new Entity(EntityKind.OBSTACLE, id, position, images,
                0, 0, 0, 0);
    }

    public static Entity createOre(String id, Point position, int actionPeriod,
                                   List<PImage> images) {
        return new Entity(EntityKind.ORE, id, position, images, 0, 0,
                actionPeriod, 0);
    }

    public static Entity createOreBlob(String id, Point position,
                                       int actionPeriod, int animationPeriod, List<PImage> images) {
        return new Entity(EntityKind.ORE_BLOB, id, position, images,
                0, 0, actionPeriod, animationPeriod);
    }

    public static Entity createQuake(Point position, List<PImage> images) {
        return new Entity(EntityKind.QUAKE, Functions.QUAKE_ID, position, images,
                0, 0, Functions.QUAKE_ACTION_PERIOD, Functions.QUAKE_ANIMATION_PERIOD);
    }

    public static Entity createVein(String id, Point position, int actionPeriod,
                                    List<PImage> images) {
        return new Entity(EntityKind.VEIN, id, position, images, 0, 0,
                actionPeriod, 0);
    }

    // NEXT POSITION METHODS

    public Point nextPositionOreBlob(WorldModel world,
                                     Point destPos) {
        int horiz = Integer.signum(destPos.x - this.position.x);
        Point newPos = new Point(this.position.x + horiz,
                this.position.y);

        Optional<Entity> occupant = world.getOccupant(newPos);

        if (horiz == 0 ||
                (occupant.isPresent() && !(occupant.get().kind == EntityKind.ORE))) {
            int vert = Integer.signum(destPos.y - this.position.y);
            newPos = new Point(this.position.x, this.position.y + vert);
            occupant = world.getOccupant(newPos);

            if (vert == 0 ||
                    (occupant.isPresent() && !(occupant.get().kind == EntityKind.ORE))) {
                newPos = this.position;
            }
        }

        return newPos;
    }

    // find the next point a miner should visit (finding a path)
    public Point nextPositionMiner(WorldModel world, Point destPos) {
        // 1 if the destination is on the right of the entity
        // -1 if the destination is on the left of the entity
        // 0 if the destination is on the same column as entity
        int horiz = Integer.signum(destPos.x - this.position.x);

        // create a new position that is 1 step in the x direction
        // and 1 step in the y direction from the entity towards
        // the destination point
        Point newPos = new Point(this.position.x + horiz,
                this.position.y);

        // if the destination is on the same column as the entity
        // OR
        // an entity is on the new point
        if (horiz == 0 || world.isOccupied(newPos)) {

            // 1 if the destination is below the entity
            // -1 if the destination is above the entity
            // 0 if the destination is on the entity
            int vert = Integer.signum(destPos.y - this.position.y);

            // create a new position by moving vertically in the direction towards the destination
            newPos = new Point(this.position.x, this.position.y + vert);

            // if the destination is on the same row as the entity
            // OR
            // an entity is on the new point
            if (vert == 0 || world.isOccupied(newPos)) {

                // set the miner's position to the new position
                newPos = this.position;
            }
        }

        return newPos;
    }
}
