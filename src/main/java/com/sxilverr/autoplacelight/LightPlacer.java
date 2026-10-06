package com.sxilverr.autoplacelight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LightPlacer {
    private static final Direction[] ANCHORS = {
            Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP
    };
    private static final long RETRY_DELAY = 40L;
    private static final int MAX_LIGHT_STALL = 40;

    private static final Map<Long, Long> ATTEMPTED = new HashMap<>();

    private static List<BlockPos> offsets = List.of();
    private static int offsetHorizontal = -1;
    private static int offsetVertical = -1;

    private static boolean enabled;
    private static int cooldown;
    private static int lightStall;

    private LightPlacer() {
    }

    public static void toggle() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        enabled = !enabled;
        player.displayClientMessage(Component.translatable(enabled
                ? "message.autoplacelight.enabled"
                : "message.autoplacelight.disabled"), true);
    }

    public static void tick(double blockReach) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.gameMode == null) {
            cooldown = 0;
            lightStall = 0;
            ATTEMPTED.clear();
            return;
        }

        if (!enabled || minecraft.screen != null || !player.isAlive() || !player.getAbilities().mayBuild) {
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (level.getLightEngine().hasLightWork() && lightStall < MAX_LIGHT_STALL) {
            lightStall++;
            return;
        }
        lightStall = 0;

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof BlockItem item && Settings.isPlaceable(stack)) {
                if (place(minecraft, player, level, hand, stack, item, blockReach)) {
                    cooldown = Settings.placeInterval;
                }
                return;
            }
        }
    }

    private static boolean place(Minecraft minecraft, LocalPlayer player, ClientLevel level, InteractionHand hand,
                                 ItemStack stack, BlockItem item, double blockReach) {
        long now = level.getGameTime();
        ATTEMPTED.values().removeIf(when -> now - when >= RETRY_DELAY);

        BlockPos origin = player.blockPosition();
        BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();

        for (BlockPos offset : offsets()) {
            target.setWithOffset(origin, offset);
            if (level.isOutsideBuildHeight(target) || ATTEMPTED.containsKey(target.asLong())) {
                continue;
            }
            int brightness = Settings.ignoreSunlight
                    ? level.getBrightness(LightLayer.BLOCK, target)
                    : level.getMaxLocalRawBrightness(target);
            if (brightness > Settings.lightLevel
                    || Settings.dontPlaceInFluid && !level.getFluidState(target).isEmpty()
                    || Settings.checkWalls && isObstructed(level, player, target)) {
                continue;
            }

            BlockHitResult hit = findAnchor(level, player, hand, stack, item, target, blockReach);
            if (hit != null) {
                ATTEMPTED.put(target.asLong(), now);
                if (minecraft.gameMode.useItemOn(player, hand, hit).consumesAction()) {
                    player.swing(hand);
                }
                return true;
            }
        }

        return false;
    }

    private static BlockHitResult findAnchor(ClientLevel level, LocalPlayer player, InteractionHand hand,
                                             ItemStack stack, BlockItem item, BlockPos target, double blockReach) {
        Vec3 eye = player.getEyePosition();

        for (Direction anchor : ANCHORS) {
            BlockPos support = target.relative(anchor);
            Vec3 center = Vec3.atCenterOf(support);
            if (level.getBlockState(support).isAir() || eye.distanceToSqr(center) > blockReach * blockReach) {
                continue;
            }

            Direction face = anchor.getOpposite();
            BlockHitResult hit = new BlockHitResult(center.relative(face, 0.5D), face, support, false);
            BlockPlaceContext context = new BlockPlaceContext(player, hand, stack, hit);
            if (!context.canPlace() || !context.getClickedPos().equals(target)) {
                continue;
            }

            BlockState placed = item.getBlock().getStateForPlacement(context);
            if (placed != null && placed.canSurvive(level, target)
                    && level.isUnobstructed(placed, target, CollisionContext.of(player))) {
                return hit;
            }
        }

        return null;
    }

    private static boolean isObstructed(ClientLevel level, LocalPlayer player, BlockPos target) {
        BlockHitResult clip = level.clip(new ClipContext(player.getEyePosition(), Vec3.atCenterOf(target),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return clip.getType() == HitResult.Type.BLOCK && !clip.getBlockPos().equals(target);
    }

    private static List<BlockPos> offsets() {
        int horizontal = Settings.horizontalRadius;
        int vertical = Settings.verticalRadius;
        if (horizontal != offsetHorizontal || vertical != offsetVertical) {
            offsets = BlockPos.betweenClosedStream(-horizontal, -vertical, -horizontal, horizontal, vertical, horizontal)
                    .map(BlockPos::immutable)
                    .sorted(Comparator.comparingDouble(offset -> offset.distSqr(BlockPos.ZERO)))
                    .toList();
            offsetHorizontal = horizontal;
            offsetVertical = vertical;
        }
        return offsets;
    }
}
