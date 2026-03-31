package com.hachirouwu.createsolar;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

public class SolarPanelBlockEntity extends BlockEntity implements IHaveGoggleInformation {
    private int energy = 0;
    private int tickCounter = 0;
    private int lastOutput = 0;

    private final EnumSet<Direction> invalidSides = EnumSet.allOf(Direction.class);
    private final EnumMap<Direction, BlockCapabilityCache<IEnergyStorage, Direction>> cache = new EnumMap<>(Direction.class);
    private boolean firstTickState = true;

    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = Math.min(energy, maxExtract);
            if (!simulate) {
                energy -= extracted;
                setChanged();
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return SolarPanelBlockEntity.this.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_PANEL.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (firstTickState) {
            firstTick();
            firstTickState = false;
        }

        if (++tickCounter >= CreateSolarConfig.UPDATE_INTERVAL.get()) {
            tickCounter = 0;
            int newOutput = calculateOutput();
            if (newOutput != lastOutput) {
                lastOutput = newOutput;
                setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
                }
            }
            if (lastOutput > 0) {
                energy = Math.min(getMaxEnergyStored(), energy + lastOutput);
                setChanged();
            }
        }

        shareWithNeighbors();

        if (energy > 0) {
            IEnergyStorage internal = getEnergyStorage();
            Direction d = Direction.DOWN;
            if (isEnergyOutput(d)) {
                BlockCapabilityCache<IEnergyStorage, Direction> capCache = cache.get(d);
                if (capCache != null) {
                    IEnergyStorage ies = capCache.getCapability();
                    if (ies != null) {
                        int maxExtract = getMaxExtract();
                        int canReceive = ies.receiveEnergy(maxExtract, true);
                        if (canReceive > 0) {
                            int extracted = internal.extractEnergy(canReceive, false);
                            ies.receiveEnergy(extracted, false);
                        }
                    }
                }
            }
        }
    }

    private void shareWithNeighbors() {
        if (energy <= 0) return;

        Direction[] sides = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (Direction side : sides) {
            BlockPos neighborPos = worldPosition.relative(side);
            BlockEntity neighborBE = level.getBlockEntity(neighborPos);
            if (neighborBE instanceof SolarPanelBlockEntity neighbor) {
                if (energy > neighbor.energy) {
                    int diff = energy - neighbor.energy;
                    int toTransfer = diff / 2;
                    if (toTransfer > 0) {
                        energy -= toTransfer;
                        neighbor.energy += toTransfer;
                        setChanged();
                        neighbor.setChanged();
                    }
                }
            }
        }
    }

    private double getWeatherFactor() {
        if (level.isThundering()) {
            return 0.1;
        } else if (level.isRaining()) {
            return 0.5;
        } else {
            return 1.0;
        }
    }

    private int calculateOutput() {
        if (level == null) return 0;
        if (!level.canSeeSky(worldPosition)) return 0;

        long time = level.getDayTime() % 24000;
        double sunFactor = getSunFactor(time);
        double weatherFactor = getWeatherFactor();
        double altitudeFactor = getAltitudeFactor();

        double totalFactor = sunFactor * weatherFactor * altitudeFactor;
        totalFactor = Math.max(0, Math.min(1, totalFactor));

        return (int) (CreateSolarConfig.MAX_OUTPUT.get() * totalFactor);
    }

    private double getSunFactor(long time) {
        long startGen = 22000;
        long endGen = 14000;
        long totalLength = (24000 - startGen) + endGen;

        long peakStart = 4000;
        long peakEnd = 8000;

        long virtualTime;
        if (time >= startGen) {
            virtualTime = time - startGen;
        } else {
            virtualTime = (24000 - startGen) + time;
        }

        long peakStartVirtual = (peakStart - startGen + 24000) % 24000;
        long peakEndVirtual = (peakEnd - startGen + 24000) % 24000;

        if (virtualTime < peakStartVirtual) {
            return (double) virtualTime / peakStartVirtual;
        } else if (virtualTime <= peakEndVirtual) {
            return 1.0;
        } else {
            double progress = (double) (virtualTime - peakEndVirtual) / (totalLength - peakEndVirtual);
            return 1.0 - progress;
        }
    }

    private double getAltitudeFactor() {
        if (!CreateSolarConfig.ALTITUDE_DEPENDENCE_ENABLED.get()) {
            return 1.0;
        }
        int y = worldPosition.getY();
        int minY = CreateSolarConfig.ALTITUDE_MIN_Y.get();
        int maxY = CreateSolarConfig.ALTITUDE_MAX_Y.get();
        double minFactor = CreateSolarConfig.ALTITUDE_MIN_FACTOR.get();
        double maxFactor = CreateSolarConfig.ALTITUDE_MAX_FACTOR.get();

        int clampedY = Math.min(maxY, Math.max(minY, y));
        double factor = minFactor + (maxFactor - minFactor) * (clampedY - minY) / (double) (maxY - minY);
        return Math.max(minFactor, Math.min(maxFactor, factor));
    }

    private int getMaxEnergyStored() {
        return CreateSolarConfig.MAX_ENERGY_STORED.get();
    }

    public int getCurrentOutput() {
        return lastOutput;
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    private static String formatNumber(int number) {
        return String.format("%,d", number);
    }

    private static String formatNumberWithDecimals(double number) {
        if (number == (int) number) {
            return String.format("%,d", (int) number);
        } else {
            return String.format("%,.1f", number);
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        int output = getCurrentOutput();
        int maxOutput = CreateSolarConfig.MAX_OUTPUT.get();
        int efficiency = maxOutput > 0 ? (int) ((double) output / maxOutput * 100) : 0;
        double perTick = (double) output / CreateSolarConfig.UPDATE_INTERVAL.get();

        String labelOffset = "    ";
        String valueOffset = "     ";

        tooltip.add(Component.literal(labelOffset)
                .append(Component.translatable("tooltip.createsolar.solar_panel_info"))
                .withStyle(ChatFormatting.WHITE));

        tooltip.add(Component.literal(labelOffset)
                .append(Component.translatable("tooltip.createsolar.solar_power_efficiency"))
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.literal(valueOffset)
                .append(Component.literal(formatNumber(efficiency) + "% ☀")
                        .withStyle(ChatFormatting.YELLOW))
                .append(Component.translatable("tooltip.createsolar.at_current_sun_strength")
                        .withStyle(ChatFormatting.DARK_GRAY)));

        tooltip.add(Component.literal(labelOffset)
                .append(Component.translatable("tooltip.createsolar.energy_generated"))
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.literal(valueOffset)
                .append(Component.literal(formatNumberWithDecimals(perTick))
                        .withStyle(ChatFormatting.AQUA))
                .append(Component.translatable("tooltip.createsolar.fe_per_tick")
                        .withStyle(ChatFormatting.AQUA))
                .append(Component.translatable("tooltip.createsolar.at_current_sun_strength")
                        .withStyle(ChatFormatting.DARK_GRAY)));

        return true;
    }

    private void firstTick() {
        if (level == null || level.isClientSide()) return;
        for (Direction side : Direction.values()) {
            cache.put(side, BlockCapabilityCache.create(
                    Capabilities.EnergyStorage.BLOCK,
                    (ServerLevel) level,
                    getBlockPos().relative(side),
                    side.getOpposite(),
                    () -> !this.isRemoved(),
                    () -> invalidSides.add(side)
            ));
        }
    }

    private boolean isEnergyOutput(Direction side) {
        return side == Direction.DOWN;
    }

    private int getMaxExtract() {
        return CreateSolarConfig.MAX_OUTPUT.get();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("Energy", energy);
        tag.putInt("LastOutput", lastOutput);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        energy = tag.getInt("Energy");
        lastOutput = tag.getInt("LastOutput");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            energy = tag.getInt("Energy");
            lastOutput = tag.getInt("LastOutput");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energy);
        tag.putInt("LastOutput", lastOutput);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("Energy");
        lastOutput = tag.getInt("LastOutput");
    }
}
