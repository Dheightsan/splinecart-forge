package io.github.foundationgames.splinecart.component;

import io.github.foundationgames.splinecart.util.SUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 1.20.1 has no data components; stored as ItemStack NBT instead.
public record OriginComponent(BlockPos pos) {
    public static final Component FIRST_SELECTION = Component.translatable("item.splinecart.track.origin").withStyle(ChatFormatting.YELLOW);
    public static final Component HOW_TO_CLEAR = Component.translatable("item.splinecart.track.clear_hint").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC);

    public static final String NBT_KEY = "origin_pos";

    public void addToTooltip(List<Component> tooltip) {
        tooltip.add(FIRST_SELECTION);
        tooltip.add(HOW_TO_CLEAR);
    }

    public static void save(CompoundTag nbt, OriginComponent component) {
        SUtil.putBlockPos(nbt, component.pos(), NBT_KEY);
    }

    public static @Nullable OriginComponent load(CompoundTag nbt) {
        if (!nbt.contains(NBT_KEY, Tag.TAG_INT_ARRAY)) return null;
        BlockPos bp = SUtil.getBlockPos(nbt, NBT_KEY);
        if (bp == null) return null;
        return new OriginComponent(bp);
    }

    public void set(ItemStack stack) {
        save(stack.getOrCreateTag(), this);
    }

    public static @Nullable OriginComponent get(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null) return null;
        return load(nbt);
    }

    public static void remove(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null) return;
        nbt.remove(NBT_KEY);
    }
}
