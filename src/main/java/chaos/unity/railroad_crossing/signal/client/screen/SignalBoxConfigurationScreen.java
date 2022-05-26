package chaos.unity.railroad_crossing.signal.client.screen;

import chaos.unity.railroad_crossing.signal.SignalNetworking;
import chaos.unity.railroad_crossing.signal.common.block.entity.ISignalBox;
import chaos.unity.railroad_crossing.signal.common.block.entity.SignalBoxEmitterBlockEntity;
import chaos.unity.railroad_crossing.signal.common.block.entity.SignalBoxReceiverBlockEntity;
import chaos.unity.railroad_crossing.signal.common.block.entity.SyncableBlockEntity;
import chaos.unity.railroad_crossing.signal.common.data.SignalMode;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SignalBoxConfigurationScreen<T extends SyncableBlockEntity & ISignalBox> extends Screen {
    private static final Identifier TEXTURE = new Identifier("rc_signal", "textures/gui/container/generic_container.png");
    private final ButtonWidget[] buttonWidgets = new ButtonWidget[5];
    private int previousInactiveButtonIndex;
    private final T blockEntity;

    public SignalBoxConfigurationScreen(String translationKey, T blockEntity) {
        super(new TranslatableText(translationKey));

        this.blockEntity = blockEntity;
    }

    private void addButton(int index, int x, int y, int width, @NotNull SignalMode signalMode) {
        var buttonWidget = buttonWidgets[index] = addDrawableChild(new ButtonWidget(x, y, width, 20, new TranslatableText(signalMode.getTranslationKey()), button -> setSignalMode(index, button, signalMode)));

        if (blockEntity instanceof SignalBoxEmitterBlockEntity emitterBlockEntity && emitterBlockEntity.emittingSignalMode == signalMode) {
            previousInactiveButtonIndex = index;
            buttonWidget.active = false;
        }

        if (blockEntity.getSignal() == signalMode) {
            previousInactiveButtonIndex = index;
            buttonWidget.active = false;
        }
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - 176) / 2, y = (height - 90) / 2;
        addButton(0, x + 12, y + 55, 70, SignalMode.BLINK_RED);
        addButton(1, x + 94, y + 55, 70, SignalMode.BLINK_YELLOW);
        addButton(2, x + 7, y + 30, 50, SignalMode.RED);
        addButton(3, x + 63, y + 30, 50, SignalMode.YELLOW);
        addButton(4, x + 119, y + 30, 50, SignalMode.GREEN);
    }

    private void setSignalMode(int index, @NotNull ButtonWidget buttonWidget, @NotNull SignalMode signalMode) {
        buttonWidgets[previousInactiveButtonIndex].active = true;
        previousInactiveButtonIndex = index;
        buttonWidget.active = false;
        blockEntity.setSignal(signalMode);
        blockEntity.markDirtyAndSync();

        if (blockEntity instanceof SignalBoxEmitterBlockEntity emitterBlockEntity &&
                emitterBlockEntity.receiverPos != null &&
                blockEntity.getWorld().getBlockEntity(emitterBlockEntity.receiverPos) instanceof SignalBoxReceiverBlockEntity) {
            var buf = PacketByteBufs.create()
                    .writeBlockPos(emitterBlockEntity.receiverPos);

            ClientPlayNetworking.send(SignalNetworking.REQUEST_BLOCK_UPDATE, buf);
        }
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - 176) / 2, y = (height - 90) / 2;
        drawTexture(matrices, x, y, 0, 0, 176, 90);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawCenteredTextWithShadow(matrices, textRenderer, getTitle().asOrderedText(), width / 2, (height - 90) / 2 + 7, 0xFFFFFF);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;

        if (Objects.requireNonNull(client).options.inventoryKey.matchesKey(keyCode, scanCode)) {
            close();
            return true;
        }

        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
