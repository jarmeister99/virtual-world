public class AllFalseEntityVisitor implements EntityVisitor<Boolean>{

    @Override
    public Boolean visit(Blacksmith blacksmith) {
        return false;
    }

    @Override
    public Boolean visit(Miner miner) {
        return false;
    }

    @Override
    public Boolean visit(MinerFull minerFull) {
        return false;
    }

    @Override
    public Boolean visit(Obstacle obstacle) {
        return false;
    }

    @Override
    public Boolean visit(Ore ore) {
        return false;
    }

    @Override
    public Boolean visit(OreBlob oreBlob) {
        return false;
    }

    @Override
    public Boolean visit(Quake quake) {
        return false;
    }

    @Override
    public Boolean visit(Vein vein) {
        return false;
    }
}
