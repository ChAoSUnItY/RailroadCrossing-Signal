package chaos.unity.signal.mixin;

import chaos.unity.signal.common.world.IntervalData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin extends Entity {
    private AbstractMinecartEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract Direction getMovementDirection();

    @Inject(method = "tick", at = @At("HEAD"))
    private void injectTick(CallbackInfo ci) {
        if (getWorld() instanceof ServerWorld world) {
            var direction = getMovementDirection();
            var pos = new BlockPos(getPos());
            var intervalData = IntervalData.getOrCreate(world);
            var interval = intervalData.getByIntervalPath(pos);

            if (interval != null) {
                var velocity = getVelocity();

                if (velocity.x < 0.001 && velocity.y < 0.001 && velocity.z < 0.001) {
                    // Mark interval is blocked by at least 1 cart
                    interval.markBlocked(world);
                } else {
                    // Mark interval has at least 1 cart moving inside
                    interval.markMoving(world);
                }
            } else {
                // Tracks last pos is in any interval, if yes, then we can conclude that this cart just leaved the interval
                var lastPos = pos.add(direction.getOpposite().getVector());
                interval = intervalData.getByIntervalPath(lastPos);

                if (interval != null) {
                    interval.markCleared(world);
                }
            }
        }
    }
}
