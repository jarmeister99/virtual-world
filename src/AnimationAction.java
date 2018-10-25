public class AnimationAction implements Action{
    private ActionKind kind;
    private Entity entity;
    private WorldModel world;
    private ImageStore imageStore;
    private int repeatCount;

    // an AnimationAction has a KIND, ENTITY, WORLDMODEL, IMAGESTORE, and REPEATCOUNT
    public AnimationAction(ActionKind kind, Entity entity, WorldModel world,
                           ImageStore imageStore, int repeatCount) {
        this.kind = kind;
        this.entity = entity;
        this.world = world;
        this.imageStore = imageStore;
        this.repeatCount = repeatCount;
    }

    // call executeAnimationAction
    public void executeAction(EventScheduler scheduler) {
        executeAnimationAction(scheduler);
    }

    private void executeAnimationAction(EventScheduler scheduler) {
        // the entity belonging to this action has its image advanced
        this.entity.nextImage();

        // if this action has a repeat count
        if (this.repeatCount != 1) {
            // schedule another animation action with a repeat count 1 lower
            scheduler.scheduleEvent(this.entity, this.entity.createAnimationAction(Math.max(this.repeatCount - 1, 0)), this.entity.getAnimationPeriod());
        }
    }
}

