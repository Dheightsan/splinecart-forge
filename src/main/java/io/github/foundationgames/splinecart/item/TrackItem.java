package io.github.foundationgames.splinecart.item;

import io.github.foundationgames.splinecart.TrackType;
import io.github.foundationgames.splinecart.block.TrackTiesBlockEntity;
import io.github.foundationgames.splinecart.component.OriginComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackItem extends Item {
    public static final Map<TrackType, Item> ITEMS_BY_TYPE = new HashMap<>();

    public final TrackType track;

    public TrackItem(TrackType track, Properties settings) {
        super(settings);

        this.track = track;
        ITEMS_BY_TYPE.put(track, this);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && !context.getPlayer().mayBuild()) {
            return super.useOn(context);
        }

        var world = context.getLevel();
        var pos = context.getClickedPos();
        var stack = context.getItemInHand();

        if (world.getBlockEntity(pos) instanceof TrackTiesBlockEntity ties) {
            if (world.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            var origin = OriginComponent.get(stack);
            if (origin != null) {
                var oPos = origin.pos();
                if (!pos.equals(oPos) && world.getBlockEntity(oPos) instanceof TrackTiesBlockEntity oTies && oTies.next() == null && ties.prev() == null) {
                    oTies.setNext(pos, this.track);

                    world.playSound(null, pos, SoundEvents.IRON_GOLEM_REPAIR, SoundSource.BLOCKS, 1.5f, 0.7f);
                }

                OriginComponent.remove(stack);
            } else {
                new OriginComponent(pos).set(stack);
            }
        } else {
            var origin = OriginComponent.get(stack);
            if (origin != null) {
                if (world.isClientSide()) {
                    return InteractionResult.CONSUME;
                }

                OriginComponent.remove(stack);
            }
        }

        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        // 1.20.1 has no ItemLore component; same translation key as the 1.21.x lore.
        tooltip.add(Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));

        var origin = OriginComponent.get(stack);
        if (origin != null) {
            origin.addToTooltip(tooltip);
        }
    }
}
