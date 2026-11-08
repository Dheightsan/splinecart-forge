package io.github.foundationgames.splinecart;

import io.github.foundationgames.splinecart.block.TrackTiesBlock;
import io.github.foundationgames.splinecart.block.TrackTiesBlockEntity;
import io.github.foundationgames.splinecart.entity.TrackFollowerEntity;
import io.github.foundationgames.splinecart.item.TrackItem;
import io.github.foundationgames.splinecart.util.TrackProgress;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

// Forge 1.20.1 port of Splinecart by FoundationGames (MIT), from the NeoForge 1.21.1 branch.
@Mod(Splinecart.MODID)
public class Splinecart {
    public static final String MODID = "splinecart";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<TrackTiesBlock> TRACK_TIES = BLOCKS.register("track_ties",
            () -> new TrackTiesBlock(BlockBehaviour.Properties.copy(Blocks.RAIL)));
    public static final RegistryObject<BlockEntityType<TrackTiesBlockEntity>> TRACK_TIES_BE = BLOCK_ENTITY_TYPES.register("track_ties",
            () -> BlockEntityType.Builder.of(TrackTiesBlockEntity::new, TRACK_TIES.get()).build(null));

    // 1.20.1 has no ItemLore component; the lore line moves to appendHoverText with the same key.
    public static final RegistryObject<BlockItem> TRACK_TIES_ITEM = ITEMS.register("track_ties", () ->
            new BlockItem(TRACK_TIES.get(), new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
                    super.appendHoverText(stack, world, tooltip, flag);
                    tooltip.add(Component.translatable("item.splinecart.track_ties.desc").withStyle(ChatFormatting.GRAY));
                }
            });
    public static final RegistryObject<TrackItem> TRACK = ITEMS.register("track", () ->
            new TrackItem(TrackType.DEFAULT, new Item.Properties()));
    public static final RegistryObject<TrackItem> CHAIN_DRIVE_TRACK = ITEMS.register("chain_drive_track", () ->
            new TrackItem(TrackType.CHAIN_DRIVE, new Item.Properties()));
    public static final RegistryObject<TrackItem> MAGNETIC_TRACK = ITEMS.register("magnetic_track", () ->
            new TrackItem(TrackType.MAGNETIC, new Item.Properties()));

    public static final RegistryObject<EntityType<TrackFollowerEntity>> TRACK_FOLLOWER = ENTITY_TYPES.register("track_follower",
            () -> EntityType.Builder.<TrackFollowerEntity>of(TrackFollowerEntity::new, MobCategory.MISC).updateInterval(2).sized(0.25f, 0.25f).build("track_follower"));

    public static final TagKey<EntityType<?>> CARTS = TagKey.create(Registries.ENTITY_TYPE, id("carts"));

    public static final RegistryObject<CreativeModeTab> SPLINECART_GROUP = CREATIVE_MODE_TABS.register("splinecart", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.splinecart"))
                    .displayItems((ctx, e) -> {
                        e.accept(TRACK_TIES_ITEM.get());
                        e.accept(TRACK.get());
                        e.accept(CHAIN_DRIVE_TRACK.get());
                        e.accept(MAGNETIC_TRACK.get());
                    })
                    .icon(() -> new ItemStack(CHAIN_DRIVE_TRACK.get()))
                    .build());

    public Splinecart() {
        // Must run before TrackFollowerEntity class-loads: its defineId resolves the serializer at class-init.
        EntityDataSerializers.registerSerializer(TrackProgress.DATA_HANDLER);

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        ENTITY_TYPES.register(bus);
        CREATIVE_MODE_TABS.register(bus);

        bus.addListener(this::modifyCreativeTabs);
    }

    private void modifyCreativeTabs(BuildCreativeModeTabContentsEvent ev) {
        if (ev.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            ev.accept(TRACK_TIES_ITEM.get());
            ev.accept(TRACK.get());
            ev.accept(CHAIN_DRIVE_TRACK.get());
            ev.accept(MAGNETIC_TRACK.get());
        }
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
