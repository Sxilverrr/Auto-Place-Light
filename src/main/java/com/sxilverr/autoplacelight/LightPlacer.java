package com.sxilverr.autoplacelight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LightPlacer {
    private static final Direction[] ANCHORS = {
            Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP
    };
    private static final InteractionHand[] HANDS = {
            InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND
    };
    private static final long RETRY_DELAY = 40L;
    private static final int MAX_LIGHT_STALL = 40;

    private static final Map<Long, Long> ATTEMPTED = new HashMap<>();

    private static List<Vec3i> offsets = List.of();
    private static int offsetHorizontal = -1;
    private static int offsetVertical = -1;

    private static boolean enabled;
    private static boolean primed;
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
            primed = false;
            ATTEMPTED.clear();
            return;
        }

        if (!primed) {
            primed = true;
            cooldown = 0;
            lightStall = 0;
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

        if (tryPlace(minecraft, player, level, blockReach)) {
            cooldown = Settings.placeInterval;
        }
    }

    private static boolean tryPlace(Minecraft minecraft, LocalPlayer player, ClientLevel level, double blockReach) {
        BlockItem blockItem = null;
        InteractionHand hand = InteractionHand.MAIN_HAND;

        for (InteractionHand candidate : HANDS) {
            ItemStack held = player.getItemInHand(candidate);
            if (held.getItem() instanceof BlockItem item && Settings.isPlaceable(held)) {
                blockItem = item;
                hand = candidate;
                break;
            }
        }

        if (blockItem == null) {
            return false;
        }

        ItemStack stack = player.getItemInHand(hand);
        long now = level.getGameTime();
        ATTEMPTED.values().removeIf(when -> now - when >= RETRY_DELAY);

        int threshold = Settings.lightLevel;
        boolean ignoreSunlight = Settings.ignoreSunlight;
        boolean dontPlaceInFluid = Settings.dontPlaceInFluid;
        boolean checkWalls = Settings.checkWalls;

        BlockPos origin = player.blockPosition();
        BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();

        for (Vec3i offset : offsets()) {
            target.set(origin.getX() + offset.getX(), origin.getY() + offset.getY(), origin.getZ() + offset.getZ());
            if (level.isOutsideBuildHeight(target)) {
                continue;
            }
            if (ATTEMPTED.containsKey(target.asLong())) {
                continue;
            }
            int brightness = ignoreSunlight
                    ? level.getBrightness(LightLayer.BLOCK, target)
                    : level.getMaxLocalRawBrightness(target);
            if (brightness > threshold) {
                continue;
            }
            if (dontPlaceInFluid && !level.getFluidState(target).isEmpty()) {
                continue;
            }
            if (checkWalls && isObstructed(level, player, target)) {
                continue;
            }

            BlockHitResult hit = findAnchor(level, player, hand, stack, blockItem.getBlock(), target, blockReach);
            if (hit == null) {
                continue;
            }

            ATTEMPTED.put(target.asLong(), now);
            place(minecraft, player, hand, hit);
            return true;
        }

        return false;
    }

    private static BlockHitResult findAnchor(ClientLevel level, LocalPlayer player, InteractionHand hand,
                                             ItemStack stack, Block block, BlockPos target, double blockReach) {
        Vec3 eye = player.getEyePosition();
        double reachSqr = blockReach * blockReach;

        for (Direction anchor : ANCHORS) {
            BlockPos support = target.relative(anchor);
            if (level.getBlockState(support).isAir()) {
                continue;
            }
            if (eye.distanceToSqr(Vec3.atCenterOf(support)) > reachSqr) {
                continue;
            }

            Direction face = anchor.getOpposite();
            Vec3 location = Vec3.atCenterOf(support)
                    .add(face.getStepX() * 0.5D, face.getStepY() * 0.5D, face.getStepZ() * 0.5D);
            BlockHitResult hit = new BlockHitResult(location, face, support, false);

            BlockPlaceContext context = new BlockPlaceContext(player, hand, stack, hit);
            if (!context.canPlace() || !context.getClickedPos().equals(target)) {
                continue;
            }

            BlockState placed = block.getStateForPlacement(context);
            if (placed == null || !placed.canSurvive(level, target)) {
                continue;
            }
            if (!level.isUnobstructed(placed, target, CollisionContext.of(player))) {
                continue;
            }

            return hit;
        }

        return null;
    }

    private static boolean isObstructed(ClientLevel level, LocalPlayer player, BlockPos target) {
        BlockHitResult clip = level.clip(new ClipContext(player.getEyePosition(), Vec3.atCenterOf(target),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return clip.getType() == HitResult.Type.BLOCK && !clip.getBlockPos().equals(target);
    }

    private static void place(Minecraft minecraft, LocalPlayer player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = minecraft.gameMode.useItemOn(player, hand, hit);
        if (result.shouldSwing()) {
            player.swing(hand);
        }
    }

    private static List<Vec3i> offsets() {
        int horizontal = Settings.horizontalRadius;
        int vertical = Settings.verticalRadius;
        if (horizontal != offsetHorizontal || vertical != offsetVertical) {
            List<Vec3i> built = new ArrayList<>();
            for (int x = -horizontal; x <= horizontal; x++) {
                for (int y = -vertical; y <= vertical; y++) {
                    for (int z = -horizontal; z <= horizontal; z++) {
                        built.add(new Vec3i(x, y, z));
                    }
                }
            }
            built.sort(Comparator.comparingInt(o -> o.getX() * o.getX() + o.getY() * o.getY() + o.getZ() * o.getZ()));
            offsets = List.copyOf(built);
            offsetHorizontal = horizontal;
            offsetVertical = vertical;
        }
        return offsets;
    }
}
