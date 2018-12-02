import java.util.*;

final class EventScheduler {
    private PriorityQueue<Event> eventQueue;
    private Map<Entity, List<Event>> pendingEvents;
    private double timeScale;
    private EntityVisitor isAnimated;
    private EntityVisitor isActive;

    public EventScheduler(double timeScale) {
        this.eventQueue = new PriorityQueue<>(new EventComparator());
        this.pendingEvents = new HashMap<>();
        this.timeScale = timeScale;
        this.isAnimated = new AnimatedEntityVisitor();
        this.isActive = new ActiveEntityVisitor();
    }

    private void removePendingEvent(Event event) {
        List<Event> pending = this.pendingEvents.get(event.entity);

        if (pending != null) {
            pending.remove(event);
        }
    }

    public void updateOnTime(long time) {
        while (!this.eventQueue.isEmpty() &&
                this.eventQueue.peek().time < time) {
            Event next = this.eventQueue.poll();

            removePendingEvent(next);

            next.action.executeAction(this);
        }
    }

    public void scheduleEvent(Entity entity, Action action, long afterPeriod) {
        long time = System.currentTimeMillis() +
                (long) (afterPeriod * this.timeScale);
        Event event = new Event(action, time, entity);

        this.eventQueue.add(event);

        // update list of pending events for the given entity
        List<Event> pending = this.pendingEvents.getOrDefault(entity,
                new LinkedList<>());
        pending.add(event);
        this.pendingEvents.put(entity, pending);
    }

    public void unscheduleAllEvents(Entity entity) {
        List<Event> pending = this.pendingEvents.remove(entity);

        if (pending != null) {
            for (Event event : pending) {
                this.eventQueue.remove(event);
            }
        }
    }

    public void scheduleActions(Entity entity, WorldModel world, ImageStore imageStore){
        if ((Boolean)entity.accept(isActive)){
            Active activeEntity = (Active) entity;
            this.scheduleEvent(entity, Entity.createActivityAction(world, imageStore, entity), activeEntity.getActionPeriod());
        }
        if ((Boolean)entity.accept(isAnimated)){
            Animated animatedEntity = (Animated) entity;
            this.scheduleEvent(entity, Entity.createAnimationAction(animatedEntity.getRepeatCount(), entity), animatedEntity.getAnimationPeriod()); }
    }

}
