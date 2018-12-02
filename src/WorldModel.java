import processing.core.PImage;

import java.util.*;

final class WorldModel {
    public int numRows;
    public int numCols;
    public Set<Entity> entities;

    private Background background[][];
    private Entity occupancy[][];

    private static final String BGND_KEY = "background";
    private static final int BGND_NUM_PROPERTIES = 4;
    private static final int BGND_ID = 1;
    private static final int BGND_COL = 2;
    private static final int BGND_ROW = 3;


    public WorldModel(int numRows, int numCols, Background defaultBackground) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.background = new Background[numRows][numCols];
        this.occupancy = new Entity[numRows][numCols];
        this.entities = new HashSet<>();

        for (int row = 0; row < numRows; row++) {
            Arrays.fill(this.background[row], defaultBackground);
        }
    }

    public Optional<Entity> findNearest(Point pos,
                                        String entityKind) {
        List<Entity> ofType = new LinkedList<>();
        for (Entity entity : this.entities) {
            if (entity.getKind() == entityKind) {
                ofType.add(entity);
            }
        }

        return Entity.nearestEntity(ofType, pos);
    }

    public void removeEntity(Entity entity) {
        removeEntityAt(entity.getPosition());
    }

    private void removeEntityAt(Point pos) {
        if (withinBounds(pos)
                && getOccupancyCell(pos) != null) {
            Entity entity = getOccupancyCell(pos);

         /* this moves the entity just outside of the grid for
            debugging purposes */
            entity.setPosition(new Point(-1, -1));
            this.entities.remove(entity);
            setOccupancyCell(pos, null);
        }
    }

    private boolean withinBounds(Point pos) {
        return pos.y >= 0 && pos.y < this.numRows &&
                pos.x >= 0 && pos.x < this.numCols;
    }

    private Entity getOccupancyCell(Point pos) {
        return this.occupancy[pos.y][pos.x];
    }

    private void setOccupancyCell(Point pos, Entity entity) {
        this.occupancy[pos.y][pos.x] = entity;
    }

    public void addEntity(Entity entity) {
        if (withinBounds(entity.getPosition())) {
            this.setOccupancyCell(entity.getPosition(), entity);
            this.entities.add(entity);
        }
    }

    public void moveEntity(Entity entity, Point pos) {
        Point oldPos = entity.getPosition();
        if (withinBounds(pos) && !pos.equals(oldPos)) {
            setOccupancyCell(oldPos, null);
            removeEntityAt(pos);
            setOccupancyCell(pos, entity);
            entity.setPosition(pos);
        }
    }

    public Optional<Entity> getOccupant(Point pos) {
        if (isOccupied(pos)) {
            return Optional.of(getOccupancyCell(pos));
        } else {
            return Optional.empty();
        }
    }

    public boolean isOccupied(Point pos) {
        return withinBounds(pos) &&
                getOccupancyCell(pos) != null;
    }

    public Optional<Point> findOpenAround(Point pos) {

        // for dy = -1, dy < 1, dy++
        for (int dy = -Miner.ORE_REACH; dy <= Miner.ORE_REACH; dy++) {

            // for dx = -1, dx < 1, dx++
            for (int dx = -Miner.ORE_REACH; dx <= Miner.ORE_REACH; dx++) {

                // make a new point in the direction of <dy, dx>
                // starts at 1 down 1 left, then 1 down 0 left, then 1 down 1 right
                // then 0 down 1 left, then 0 down 0 right, then 0 down 1 right
                // ~~~~~~
                //  678
                //  4X5
                //  123
                // ~~~~~~
                Point newPt = new Point(pos.x + dx, pos.y + dy);

                // if the point is inside the world  and it isn't occupied
                if (withinBounds(newPt) &&
                        !isOccupied(newPt)) {

                    // return the point
                    return Optional.of(newPt);
                }
            }
        }

        // no valid spot found
        return Optional.empty();
    }

    // load a bunch of entities onto the map
    public void load(Scanner in, ImageStore imageStore) {
        int lineNumber = 0;
        while (in.hasNextLine()) {
            try {
                if (!processLine(in.nextLine(), this, imageStore)) {
                    System.err.println(String.format("invalid entry on line %d",
                            lineNumber));
                }
            } catch (NumberFormatException e) {
                System.err.println(String.format("invalid entry on line %d",
                        lineNumber));
            } catch (IllegalArgumentException e) {
                System.err.println(String.format("issue on line %d: %s",
                        lineNumber, e.getMessage()));
            }
            lineNumber++;
        }
    }

    // puts each entity on the map
    private boolean processLine(String line, WorldModel world,
                                ImageStore imageStore) {
        String[] properties = line.split("\\s");
        if (properties.length > 0) {
            switch (properties[Entity.PROPERTY_KEY]) {
                case BGND_KEY:
                    return parseBackground(properties, world, imageStore);
                case Miner.MINER_KEY:
                    return parseMiner(properties, world, imageStore);
                case Obstacle.OBSTACLE_KEY:
                    return parseObstacle(properties, world, imageStore);
                case Ore.ORE_KEY:
                    return parseOre(properties, world, imageStore);
                case Blacksmith.SMITH_KEY:
                    return parseSmith(properties, world, imageStore);
                case Vein.VEIN_KEY:
                    return parseVein(properties, world, imageStore);
            }
        }

        return false;
    }

    private boolean parseBackground(String[] properties,
                                    WorldModel world, ImageStore imageStore) {
        if (properties.length == BGND_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[BGND_COL]),
                    Integer.parseInt(properties[BGND_ROW]));
            String id = properties[BGND_ID];
            world.setBackground(pt, new Background(id, imageStore.getImageList(id)));
        }

        return properties.length == BGND_NUM_PROPERTIES;
    }

    private boolean parseMiner(String[] properties, WorldModel world,
                               ImageStore imageStore) {
        if (properties.length == Miner.MINER_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Miner.MINER_COL]),
                    Integer.parseInt(properties[Miner.MINER_ROW]));
            Entity entity = Entity.createMinerNotFull(
                    Integer.parseInt(properties[Miner.MINER_LIMIT]),
                    pt,
                    Integer.parseInt(properties[Miner.MINER_ACTION_PERIOD]),
                    Integer.parseInt(properties[Miner.MINER_ANIMATION_PERIOD]),
                    imageStore.getImageList(Miner.MINER_KEY));
            tryAddEntity(world, entity);
        }

        return properties.length == Miner.MINER_NUM_PROPERTIES;
    }

    private boolean parseObstacle(String[] properties, WorldModel world,
                                  ImageStore imageStore) {
        if (properties.length == Obstacle.OBSTACLE_NUM_PROPERTIES) {
            Point pt = new Point(
                    Integer.parseInt(properties[Obstacle.OBSTACLE_COL]),
                    Integer.parseInt(properties[Obstacle.OBSTACLE_ROW]));
            Entity entity = Entity.createObstacle(pt, imageStore.getImageList(Obstacle.OBSTACLE_KEY));
            tryAddEntity(world, entity);
        }

        return properties.length == Obstacle.OBSTACLE_NUM_PROPERTIES;
    }

    private boolean parseOre(String[] properties, WorldModel world,
                             ImageStore imageStore) {
        if (properties.length == Ore.ORE_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Ore.ORE_COL]),
                    Integer.parseInt(properties[Ore.ORE_ROW]));
            Entity entity = Entity.createOre(
                    pt, Integer.parseInt(properties[Ore.ORE_ACTION_PERIOD]),
                    imageStore.getImageList(Ore.ORE_KEY));
            tryAddEntity(world, entity);
        }

        return properties.length == Ore.ORE_NUM_PROPERTIES;
    }

    private boolean parseSmith(String[] properties, WorldModel world,
                               ImageStore imageStore) {
        if (properties.length == Blacksmith.SMITH_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Blacksmith.SMITH_COL]),
                    Integer.parseInt(properties[Blacksmith.SMITH_ROW]));
            Entity entity = Entity.createBlacksmith(pt, imageStore.getImageList(Blacksmith.SMITH_KEY));
            tryAddEntity(world, entity);
        }

        return properties.length == Blacksmith.SMITH_NUM_PROPERTIES;
    }

    private boolean parseVein(String[] properties, WorldModel world,
                              ImageStore imageStore) {
        if (properties.length == Vein.VEIN_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Vein.VEIN_COL]), Integer.parseInt(properties[Vein.VEIN_ROW]));
            Entity entity = Entity.createVein(pt, Integer.parseInt(properties[Vein.VEIN_ACTION_PERIOD]), imageStore.getImageList(Vein.VEIN_KEY));
            tryAddEntity(world, entity);
        }

        return properties.length == Vein.VEIN_NUM_PROPERTIES;
    }

    private void tryAddEntity(WorldModel world, Entity entity) {
        if (isOccupied(entity.getPosition())) {
            // arguably the wrong type of exception, but we are not
            // defining our own exceptions yet
            throw new IllegalArgumentException("position occupied");
        }

        world.addEntity(entity);
    }


    private void setBackground(Point pos, Background background) {
        if (withinBounds(pos)) {
            setBackgroundCell(pos, background);
        }
    }

    private void setBackgroundCell(Point pos, Background background) {
        this.background[pos.y][pos.x] = background;
    }


    public Optional<PImage> getBackgroundImage(Point pos) {
        if (withinBounds(pos)) {
            return Optional.of(Functions.getCurrentImage(getBackgroundCell(pos)));
        } else {
            return Optional.empty();
        }
    }

    private Background getBackgroundCell(Point pos) {
        return this.background[pos.y][pos.x];
    }


}
