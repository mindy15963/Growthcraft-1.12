package growthcraft.core.shared.effect;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * Puts the entity out if they're on fire, use EffectIgnite, if you want
 * the entity to burn like a holiday fire cracker.
 */
public class EffectExtinguish extends AbstractEffect {
    // REVISE_ME 0

    @Override
    public void apply(World world, Entity entity, Random random, Object data) {
        entity.extinguish();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void getActualDescription(List<String> list) {
        list.add(I18n.translateToLocal("effect.extinguish.desc"));
    }

    @Override
    protected void readFromNBT(NBTTagCompound data) {
    }

    @Override
    protected void writeToNBT(NBTTagCompound data) {
    }
}
