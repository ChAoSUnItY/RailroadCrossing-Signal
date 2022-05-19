package chaos.unity.signal.mixin;

import chaos.unity.signal.common.world.IntervalData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract Vec3d getPos();

    @Inject(method = "setRemoved", at = @At("HEAD"))
    public void injectSetRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        if (((Entity) (Object) this) instanceof AbstractMinecartEntity minecartEntity && getWorld() instanceof ServerWorld world) {
            var pos = new BlockPos(getPos());
            var intervalData = IntervalData.getOrCreate(world);
            var interval = intervalData.getByIntervalPath(pos);

            if (interval != null) {
                interval.markCleared(world, minecartEntity);
            }
        }
    }
}
