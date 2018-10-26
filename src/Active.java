interface Active {
    public int getActionPeriod();
    public void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler);
}
