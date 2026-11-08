package io.github.foundationgames.splinecart.mixin.client;

import io.github.foundationgames.splinecart.SplinecartClient;
import io.github.foundationgames.splinecart.block.entity.TrackTiesBlockEntityRenderer;
import io.github.foundationgames.splinecart.entity.TrackFollowerEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(value = {LevelRenderer.class}, priority = 1500)
public class WorldRendererMixin {
    @Shadow
    @Final
    private Set<BlockEntity> globalBlockEntities;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private AtomicBoolean needsFrustumUpdate;

    // 1.20.1 has no SectionOcclusionGraph.consumeFrustumUpdate; forcing needsFrustumUpdate
    // makes setupRender's compareAndSet gate re-run applyFrustum every frame, which is the
    // same effect (occlusion culling follows the quaternion camera instead of xRot/yRot).
    @Inject(method = "setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V",
            require = 0, at = @At("HEAD"))
    private void splinecart$updateChunkOcclusionCullingWhileOnTrack(Camera camera, Frustum frustum, boolean hasCapturedFrustum, boolean isSpectator, CallbackInfo ci) {
        if (SplinecartClient.CFG_ROTATE_CAMERA.get()) {
            Entity entity = minecraft.getCameraEntity();
            while (entity != null) {
                entity = entity.getVehicle();

                if (entity instanceof TrackFollowerEntity) {
                    this.needsFrustumUpdate.set(true);
                    return;
                }
            }
        }
    }

    @Inject(method = "blockChanged(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;I)V", at = @At("TAIL"))
    private void splinecart$updateBlockEntityVbos(BlockGetter world, BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        if (SplinecartClient.CFG_VBOS.get()) {
            var chunkPos = SectionPos.of(pos);
            try {
                TrackTiesBlockEntityRenderer.queueVboRebuildsForChunkUpdate(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ(), globalBlockEntities);
            } catch (ConcurrentModificationException ignored) {}
        }
    }
}
