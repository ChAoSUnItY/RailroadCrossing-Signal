package chaos.unity.signal.mixin;

import chaos.unity.signal.SignalNetworking;
import chaos.unity.signal.common.block.SignalBlock;
import chaos.unity.signal.common.blockentity.SignalBlockEntity;
import chaos.unity.signal.common.item.RadioLinkerItem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Final
    @Shadow
    private MinecraftClient client;

    @Inject(method = "render",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/util/math/MatrixStack;F)V")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/util/math/MatrixStack;F)V"))
    public void injectRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        var player = Objects.requireNonNull(client.player);
        var hitResult = player.raycast(Objects.requireNonNull(client.interactionManager).getReachDistance(), tickDelta, false);

        if (hitResult instanceof BlockHitResult blockHitResult) {
            var targetPos = blockHitResult.getBlockPos();
            var world = client.world;

            if (world.getBlockEntity(targetPos) instanceof SignalBlockEntity sbe && player.getStackInHand(player.getActiveHand()).getItem() instanceof RadioLinkerItem) {
                var buf = PacketByteBufs.create();
                buf.writeBlockPos(sbe.getPos());
                ClientPlayNetworking.send(SignalNetworking.REQUEST_HIGHLIGHT_SIGNALS, buf);
            }
        }
    }
}
