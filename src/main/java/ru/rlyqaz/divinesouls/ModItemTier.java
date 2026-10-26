package ru.rlyqaz.divinesouls;

import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.LazyValue;
import java.util.function.Supplier;

public enum ModItemTier implements IItemTier {
    // ИСПРАВЛЕНО: Уровень 4, Прочность 3000 (вместо 2500), Скорость копания 12.0F (незерит — 9.0), Бонус урона 7.0F, Зачаровываемость 22
    END_CRYSTAL(4, 3000, 12.0F, 7.0F, 22, () -> Ingredient.of(ModItems.END_CRYSTAL_GEM.get()));

    private final int harvestLevel;
    private final int maxUses;
    private final float efficiency;
    private final float attackDamage;
    private final int enchantability;
    private final LazyValue<Ingredient> repairMaterial;

    ModItemTier(int harvestLevel, int maxUses, float efficiency, float attackDamage, int enchantability, Supplier<Ingredient> repairMaterial) {
        this.harvestLevel = harvestLevel;
        this.maxUses = maxUses;
        this.efficiency = efficiency;
        this.attackDamage = attackDamage;
        this.enchantability = enchantability;
        this.repairMaterial = new LazyValue<>(repairMaterial);
    }

    // ИСПРАВЛЕНО: В твоих маппингах метод прочности называется getUses()
    @Override
    public int getUses() {
        return this.maxUses;
    }

    // ИСПРАВЛЕНО: В твоих маппингах метод скорости называется getSpeed()
    @Override
    public float getSpeed() {
        return this.efficiency;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.attackDamage;
    }

    @Override
    public int getLevel() {
        return this.harvestLevel;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairMaterial.get();
    }
}
