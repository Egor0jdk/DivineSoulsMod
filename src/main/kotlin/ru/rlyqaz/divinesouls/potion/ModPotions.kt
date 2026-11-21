package ru.rlyqaz.divinesouls.potion

import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.InstantenousMobEffect
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.Potions
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent
import net.neoforged.neoforge.registries.DeferredRegister
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.mana.ManaManager
import ru.rlyqaz.divinesouls.mana.ModAttachments

class InstantManaEffect : InstantenousMobEffect(MobEffectCategory.BENEFICIAL, 0x2699FF) {

    override fun applyEffectTick(entity: LivingEntity, amplifier: Int): Boolean {
        restoreMana(entity, amplifier, 1.0)
        return true
    }

    override fun applyInstantenousEffect(
        source: Entity?,
        indirectSource: Entity?,
        entity: LivingEntity,
        amplifier: Int,
        health: Double
    ) {
        restoreMana(entity, amplifier, health)
    }

    private fun restoreMana(entity: LivingEntity, amplifier: Int, multiplier: Double) {
        val player = entity as? ServerPlayer ?: return
        val data = player.getData(ModAttachments.MANA.get())
        // Уровень I (amplifier = 0) -> 50 маны; Уровень II (amplifier = 1) -> 100 маны
        val baseRestore = 50 * (amplifier + 1)
        val amount = (baseRestore * multiplier).toInt().coerceAtLeast(1)

        val newMana = (data.mana + amount).coerceAtMost(data.maxMana)
        player.setData(ModAttachments.MANA.get(), data.copy(mana = newMana))
        ManaManager.syncToClient(player)
    }
}

object ModPotions {
    val MOB_EFFECTS: DeferredRegister<MobEffect> =
        DeferredRegister.create(Registries.MOB_EFFECT, DivineSoulsMod.MOD_ID)

    val POTIONS: DeferredRegister<Potion> =
        DeferredRegister.create(Registries.POTION, DivineSoulsMod.MOD_ID)

    val INSTANT_MANA_EFFECT: Holder<MobEffect> =
        MOB_EFFECTS.register("instant_mana", java.util.function.Supplier { InstantManaEffect() })

    val MANA_POTION: Holder<Potion> = POTIONS.register("mana_potion", java.util.function.Supplier {
        Potion(MobEffectInstance(INSTANT_MANA_EFFECT, 1, 0))
    })

    val STRONG_MANA_POTION: Holder<Potion> = POTIONS.register("strong_mana_potion", java.util.function.Supplier {
        Potion("mana_potion", MobEffectInstance(INSTANT_MANA_EFFECT, 1, 1))
    })

    fun register(modEventBus: IEventBus) {
        MOB_EFFECTS.register(modEventBus)
        POTIONS.register(modEventBus)
    }

    @SubscribeEvent
    fun onRegisterBrewingRecipes(event: RegisterBrewingRecipesEvent) {
        val builder = event.builder

        // 1. Неловкое зелье + Лазурит = Зелье маны (50 ед.)
        builder.addMix(Potions.AWKWARD, Items.LAPIS_LAZULI, MANA_POTION)

        // 2. Зелье маны + Светопыль = Зелье маны II (100 ед.)
        builder.addMix(MANA_POTION, Items.GLOWSTONE_DUST, STRONG_MANA_POTION)
    }
}
