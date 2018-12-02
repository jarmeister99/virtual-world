public class ActivityAction implements Action {
    private Entity entity;
    private WorldModel world;
    private ImageStore imageStore;
    private int repeatCount;

    // an ActivityAction has a KIND, ENTITY, WORLDMODEL, IMAGESTORE, and REPEATCOUNT
    public ActivityAction(Entity entity, WorldModel world,
                          ImageStore imageStore, int repeatCount) {
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
        Active activeEntity = (Active) this.entity;
        activeEntity.executeActivity(world, imageStore, scheduler);

    }
}

