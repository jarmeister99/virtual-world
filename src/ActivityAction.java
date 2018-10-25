public class ActivityAction implements Action{
    private ActionKind kind;
    private Entity entity;
    private WorldModel world;
    private ImageStore imageStore;
    private int repeatCount;

    // an ActivityAction has a KIND, ENTITY, WORLDMODEL, IMAGESTORE, and REPEATCOUNT
    public ActivityAction(ActionKind kind, Entity entity, WorldModel world,
                          ImageStore imageStore, int repeatCount) {
        this.kind = kind;
        this.entity = entity;
        this.world = world;
        this.imageStore = imageStore;
        this.repeatCount = repeatCount;
    }

    // call executeActivityAction
    public void executeAction(EventScheduler scheduler) {
        executeActivityAction(scheduler);
    }

    private void executeActivityAction(EventScheduler scheduler) {
        // the entity that belongs to this action executes its activity
        switch (this.entity.kind) {
            case MINER_FULL:
                this.entity.executeMinerFullActivity(this.world,
                        this.imageStore, scheduler);
                break;

            case MINER_NOT_FULL:
                this.entity.executeMinerNotFullActivity(this.world,
                        this.imageStore, scheduler);
                break;

            case ORE:
                this.entity.executeOreActivity(this.world, this.imageStore,
                        scheduler);
                break;

            case ORE_BLOB:
                this.entity.executeOreBlobActivity(this.world,
                        this.imageStore, scheduler);
                break;

            case QUAKE:
                this.entity.executeQuakeActivity(this.world, this.imageStore,
                        scheduler);
                break;

            case VEIN:
                this.entity.executeVeinActivity(this.world, this.imageStore,
                        scheduler);
                break;

            default:
                throw new UnsupportedOperationException(
                        String.format("executeActivityAction not supported for %s",
                                this.entity.kind));
        }
    }
}

