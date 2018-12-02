public class AnimatedEntityVisitor extends AllFalseEntityVisitor{

    @Override
    public Boolean visit(Miner miner) {
        return true;
    }

    @Override
    public Boolean visit(MinerFull minerFull) {
        return true;
    }

    @Override
    public Boolean visit(OreBlob oreBlob) {
        return true;
    }

    @Override
    public Boolean visit(Quake quake) {
        return true;
    }

}
