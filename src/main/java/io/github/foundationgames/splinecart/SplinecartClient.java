package io.github.foundationgames.splinecart;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.foundationgames.splinecart.block.TrackGeometry;
import io.github.foundationgames.splinecart.block.entity.ClientTrackGeometry;
import io.github.foundationgames.splinecart.block.entity.TrackTiesBlockEntityRenderer;
import io.github.foundationgames.splinecart.config.Config;
import io.github.foundationgames.splinecart.config.ConfigOption;
import io.github.foundationgames.splinecart.util.SUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;

// On Forge 1.20.1 there is no second @Mod(dist = CLIENT) entry point; the two
// EventBusSubscriber scans (MOD bus here, FORGE bus in GameEvents) replace
// SplinecartClientMod + the manual addListener calls of the original.
@Mod.EventBusSubscriber(modid = Splinecart.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SplinecartClient {
    public static final Config CONFIG = new Config("splinecart_client",
            () -> FMLPaths.CONFIGDIR.get().resolve("splinecart").resolve("splinecart_client.properties"));

    public static final ConfigOption.BooleanOption CFG_ROTATE_CAMERA = CONFIG.optBool("rotate_camera", true);
    public static final ConfigOption.BooleanOption CFG_VBOS = CONFIG.optBool("vbos", false);
    public static final ConfigOption.IntOption CFG_TRACK_RESOLUTION = CONFIG.optInt("track_resolution", 3, 1, 16);
    public static final ConfigOption.IntOption CFG_TRACK_RENDER_DISTANCE = CONFIG.optInt("track_render_distance", 8, 4, 32);

    public static ShaderInstance entityCutoutNoCullUvTransformProgram;

    private static final SplinecartHud HUD = new SplinecartHud();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent ev) {
        onInitializeClient();
    }

    public static void onInitializeClient() {
        SUtil.TICK_DELTA = () -> Minecraft.getInstance().getFrameTime();

        try {
            CONFIG.load();
        } catch (IOException e) {
            Splinecart.LOGGER.error("Error loading client config on mod init", e);
        }

        TrackGeometry.CONSTRUCTOR = ClientTrackGeometry::new;
    }

    public static ShaderInstance getProgramEntityCutoutNoCullUvTransform() {
        return entityCutoutNoCullUvTransformProgram;
    }

    public static RenderType renderLayerEntityCutoutNoCullUvTransform(ResourceLocation texture, float x, float y) {
        return RenderTypes.entityCutoutNoCullUvTransform(texture, x, y);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        try {
            var shader = new ShaderInstance(
                    event.getResourceProvider(),
                    Splinecart.id("splinecart_rendertype_entity_cutout_no_cull_uv_transform"),
                    DefaultVertexFormat.NEW_ENTITY
            );
            event.registerShader(shader, program -> entityCutoutNoCullUvTransformProgram = program);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers ev) {
        ev.registerBlockEntityRenderer(Splinecart.TRACK_TIES_BE.get(), TrackTiesBlockEntityRenderer::new);
        ev.registerEntityRenderer(Splinecart.TRACK_FOLLOWER.get(), NoopRenderer::new);
    }

    @Mod.EventBusSubscriber(modid = Splinecart.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class GameEvents {
        @SubscribeEvent
        public static void registerCommands(RegisterClientCommandsEvent event) {
            event.getDispatcher().register(
                    LiteralArgumentBuilder.<CommandSourceStack>literal("splinecartc")
                            .then(CONFIG.command(LiteralArgumentBuilder.literal("config"),
                                    CommandSourceStack::sendSystemMessage))
            );
        }

        @SubscribeEvent
        public static void onRenderPost(RenderGuiEvent.Post event) {
            HUD.onHudRender(event.getGuiGraphics(), event.getPartialTick());
        }
    }

    // In 1.20.1 the composite-state shards are protected in RenderStateShard, so the
    // custom layer factory has to live in a RenderType subclass.
    private static final class RenderTypes extends RenderType {
        private RenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                            boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        }

        static RenderType entityCutoutNoCullUvTransform(ResourceLocation texture, float x, float y) {
            return create(
                    "splinecart_entity_cutout_no_cull_uv_transform",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    1536,
                    true, false,
                    CompositeState.builder()
                            .setShaderState(new RenderStateShard.ShaderStateShard(SplinecartClient::getProgramEntityCutoutNoCullUvTransform))
                            .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                            .setTexturingState(new RenderStateShard.OffsetTexturingStateShard(x, y))
                            .setTransparencyState(NO_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setLightmapState(LIGHTMAP)
                            .setOverlayState(OVERLAY)
                            .createCompositeState(false)
            );
        }
    }
}
