import processing.core.PImage;

import java.util.*;

final class WorldModel {
    public int numRows;
    public int numCols;
    public Background background[][];
    public Entity occupancy[][];
    public Set<Entity> entities;

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
                                        EntityKind kind) {
        List<Entity> ofType = new LinkedList<>();
        for (Entity entity : this.entities) {
            if (entity.kind == kind) {
                ofType.add(entity);
            }
        }

        return Entity.nearestEntity(ofType, pos);
    }

    public void removeEntity(Entity entity) {
        removeEntityAt(entity.position);
    }

    public void removeEntityAt(Point pos) {
        if (withinBounds(pos)
                && getOccupancyCell(pos) != null) {
            Entity entity = getOccupancyCell(pos);

         /* this moves the entity just outside of the grid for
            debugging purposes */
            entity.position = new Point(-1, -1);
            this.entities.remove(entity);
            setOccupancyCell(pos, null);
        }
    }

    public boolean withinBounds(Point pos) {
        return pos.y >= 0 && pos.y < this.numRows &&
                pos.x >= 0 && pos.x < this.numCols;
    }

    public Entity getOccupancyCell(Point pos) {
        return this.occupancy[pos.y][pos.x];
    }

    public void setOccupancyCell(Point pos, Entity entity) {
        this.occupancy[pos.y][pos.x] = entity;
    }

    public void addEntity(Entity entity) {
        if (withinBounds(entity.position)) {
            this.setOccupancyCell(entity.position, entity);
            this.entities.add(entity);
        }
    }

    public void moveEntity(Entity entity, Point pos) {
        Point oldPos = entity.position;
        if (withinBounds(pos) && !pos.equals(oldPos)) {
            setOccupancyCell(oldPos, null);
            removeEntityAt(pos);
            setOccupancyCell(pos, entity);
            entity.position = pos;
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
        for (int dy = -Functions.ORE_REACH; dy <= Functions.ORE_REACH; dy++) {

            // for dx = -1, dx < 1, dx++
            for (int dx = -Functions.ORE_REACH; dx <= Functions.ORE_REACH; dx++) {

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
    public boolean processLine(String line, WorldModel world,
                               ImageStore imageStore) {
        String[] properties = line.split("\\s");
        if (properties.length > 0) {
            switch (properties[Functions.PROPERTY_KEY]) {
                case Functions.BGND_KEY:
                    return parseBackground(properties, world, imageStore);
                case Functions.MINER_KEY:
                    return parseMiner(properties, world, imageStore);
                case Functions.OBSTACLE_KEY:
                    return parseObstacle(properties, world, imageStore);
                case Functions.ORE_KEY:
                    return parseOre(properties, world, imageStore);
                case Functions.SMITH_KEY:
                    return parseSmith(properties, world, imageStore);
                case Functions.VEIN_KEY:
                    return parseVein(properties, world, imageStore);
            }
        }

        return false;
    }

    public boolean parseBackground(String[] properties,
                                   WorldModel world, ImageStore imageStore) {
        if (properties.length == Functions.BGND_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Functions.BGND_COL]),
                    Integer.parseInt(properties[Functions.BGND_ROW]));
            String id = properties[Functions.BGND_ID];
            world.setBackground(pt, new Background(id, imageStore.getImageList(id)));
        }

        return properties.length == Functions.BGND_NUM_PROPERTIES;
    }

    public boolean parseMiner(String[] properties, WorldModel world,
                              ImageStore imageStore) {
        if (properties.length == Functions.MINER_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Functions.MINER_COL]),
                    Integer.parseInt(properties[Functions.MINER_ROW]));
            Entity entity = Entity.createMinerNotFull(properties[Functions.MINER_ID],
                    Integer.parseInt(properties[Functions.MINER_LIMIT]),
                    pt,
                    Integer.parseInt(properties[Functions.MINER_ACTION_PERIOD]),
                    Integer.parseInt(properties[Functions.MINER_ANIMATION_PERIOD]),
                    imageStore.getImageList(Functions.MINER_KEY));
            tryAddEntity(world, entity);
        }

        return properties.length == Functions.MINER_NUM_PROPERTIES;
    }

    public boolean parseObstacle(String[] properties, WorldModel world,
                                 ImageStore imageStore) {
        if (properties.length == Functions.OBSTACLE_NUM_PROPERTIES) {
            Point pt = new Point(
                    Integer.parseInt(properties[Functions.OBSTACLE_COL]),
                    Integer.parseInt(properties[Functions.OBSTACLE_ROW]));
            Entity entity = Entity.createObstacle(properties[Functions.OBSTACLE_ID],
                    pt, imageStore.getImageList(Functions.OBSTACLE_KEY));
            tryAddEntity(world, entity);
        }

        return properties.length == Functions.OBSTACLE_NUM_PROPERTIES;
    }

    public boolean parseOre(String[] properties, WorldModel world,
                            ImageStore imageStore) {
        if (properties.length == Functions.ORE_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Functions.ORE_COL]),
                    Integer.parseInt(properties[Functions.ORE_ROW]));
            Entity entity = Entity.createOre(properties[Functions.ORE_ID],
                    pt, Integer.parseInt(properties[Functions.ORE_ACTION_PERIOD]),
                    imageStore.getImageList(Functions.ORE_KEY));
            tryAddEntity(world, entity);
        }

        return properties.length == Functions.ORE_NUM_PROPERTIES;
    }

    public boolean parseSmith(String[] properties, WorldModel world,
                              ImageStore imageStore) {
        if (properties.length == Functions.SMITH_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Functions.SMITH_COL]),
                    Integer.parseInt(properties[Functions.SMITH_ROW]));
            Entity entity = Entity.createBlacksmith(properties[Functions.SMITH_ID],
                    pt, imageStore.getImageList(Functions.SMITH_KEY));
            tryAddEntity(world, entity);
        }

        return properties.length == Functions.SMITH_NUM_PROPERTIES;
    }

    public boolean parseVein(String[] properties, WorldModel world,
                             ImageStore imageStore) {
        if (properties.length == Functions.VEIN_NUM_PROPERTIES) {
            Point pt = new Point(Integer.parseInt(properties[Functions.VEIN_COL]),
                    Integer.parseInt(properties[Functions.VEIN_ROW]));
            Entity entity = Entity.createVein(properties[Functions.VEIN_ID],
                    pt,
                    Integer.parseInt(properties[Functions.VEIN_ACTION_PERIOD]),
                    imageStore.getImageList(Functions.VEIN_KEY));
            tryAddEntity(world, entity);
        }

        return properties.length == Functions.VEIN_NUM_PROPERTIES;
    }

    public void tryAddEntity(WorldModel world, Entity entity) {
        if (isOccupied(entity.position)) {
            // arguably the wrong type of exception, but we are not
            // defining our own exceptions yet
            throw new IllegalArgumentException("position occupied");
        }

        world.addEntity(entity);
    }


    public void setBackground(Point pos, Background background) {
        if (withinBounds(pos)) {
            setBackgroundCell(pos, background);
        }
    }

    public void setBackgroundCell(Point pos, Background background) {
        this.background[pos.y][pos.x] = background;
    }


    public Optional<PImage> getBackgroundImage(Point pos) {
        if (withinBounds(pos)) {
            return Optional.of(Functions.getCurrentImage(getBackgroundCell(pos)));
        } else {
            return Optional.empty();
        }
    }

    public Background getBackgroundCell(Point pos) {
        return this.background[pos.y][pos.x];
    }




}
