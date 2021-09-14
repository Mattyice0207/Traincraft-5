package train.entity.ai;

import ebf.tim.entities.EntityTrainCore;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCache;

public class EntityAIFearHorn extends EntityAIBase{
	
	private EntityAnimal entity;
    private double randPosX;
    private double randPosY;
    private double randPosZ;
	
	public EntityAIFearHorn(EntityAnimal e) {
		entity = e;
        this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		if(entity.getEntityToAttack() instanceof EntityTrainCore) {
			entity.detachHome();
            Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(entity, 10, 8, Vec3.createVectorHelper(entity.getEntityToAttack().posX, entity.getEntityToAttack().posY, entity.getEntityToAttack().posZ));

            if (vec3 == null)
            {
                return false;
            } else {
                this.randPosX = vec3.xCoord;
                this.randPosY = vec3.yCoord;
                this.randPosZ = vec3.zCoord;
    			entity.setTarget(null);
                return true;
            }
		}
		return false;
	}
	
    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
    	tryMoveToXYZ(this.randPosX, this.randPosY, this.randPosZ, 2.0D);
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        return !this.entity.getNavigator().noPath();
    }
    
    /**
     * Returns the path to the given coordinates
     */
    private PathEntity getPathToXYZ(double x, double y, double z) {
        return getEntityPathToXYZ(MathHelper.floor_double(x), (int)y, MathHelper.floor_double(z), 
                (int)entity.getNavigator().getPathSearchRange()+8, false, false, false);
    }

    /**
     * Try to find and set a path to XYZ. Returns true if successful.
     */
    private boolean tryMoveToXYZ(double x, double y, double z, double speed)
    {
        PathEntity pathentity = this.getPathToXYZ((double)MathHelper.floor_double(x), (double)((int)y), (double)MathHelper.floor_double(z));
        return entity.getNavigator().setPath(pathentity, speed);
    }
    
    private PathEntity getEntityPathToXYZ(int targetX, int targetY, int targetZ, int range, boolean canPassOpenDoor, boolean canPassClosedDoor, boolean canSwim) {
        int x = MathHelper.floor_double(entity.posX);
        int y = MathHelper.floor_double(entity.posY);
        int z = MathHelper.floor_double(entity.posZ);

        ChunkCache chunkcache = new ChunkCache(entity.worldObj,
                x - range, y - range, z - range,
                x + range, y + range, z + range,
                0);
        PathEntity pathentity = (new TCPathFinder(chunkcache, canPassOpenDoor, canPassClosedDoor, false, canSwim)).createEntityPathTo(entity, targetX, targetY, targetZ, range);
        return pathentity;
    }
}
