package ru.rlyqaz.divinesouls.item

import net.minecraft.world.item.*
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import ru.rlyqaz.divinesouls.DivineSoulsMod
import java.util.function.Supplier

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: ModWeapons (Оружие, Боевые инструменты и Магия)
 * ====================================================================
 */
object ModWeapons {
    private val REGISTRY: DeferredRegister.Items = DivineSoulsMod.ITEMS

    // --- КРИСТАЛЬНЫЙ ТИР ЭНДА (END CRYSTAL TIER) ---

    // Кристальный меч: 3 (модификатор меча) + 7 (базовый урон материала) = 10 единиц (плюс 1 рука = 11 общего урона!)
    val END_CRYSTAL_SWORD: Supplier<Item> = registerWeapon("end_crystal_sword") {
        SwordItem(ModItemTier.END_CRYSTAL, Item.Properties().attributes(
            SwordItem.createAttributes(ModItemTier.END_CRYSTAL, 3f, -2.4f)
        ))
    }

    // Кристальная кирка: Урон 8
    val END_CRYSTAL_PICKAXE: Supplier<Item> = registerWeapon("end_crystal_pickaxe") {
        PickaxeItem(ModItemTier.END_CRYSTAL, Item.Properties().attributes(
            PickaxeItem.createAttributes(ModItemTier.END_CRYSTAL, 1f, -2.8f)
        ))
    }

    // Кристальный топор: Урон 12
    val END_CRYSTAL_AXE: Supplier<Item> = registerWeapon("end_crystal_axe") {
        AxeItem(ModItemTier.END_CRYSTAL, Item.Properties().attributes(
            AxeItem.createAttributes(ModItemTier.END_CRYSTAL, 5f, -3.0f)
        ))
    }

    // Кристальная лопата: Урон 8.5
    val END_CRYSTAL_SHOVEL: Supplier<Item> = registerWeapon("end_crystal_shovel") {
        ShovelItem(ModItemTier.END_CRYSTAL, Item.Properties().attributes(
            ShovelItem.createAttributes(ModItemTier.END_CRYSTAL, 1.5f, -3.0f)
        ))
    }

    // Кристальная мотыга: 1 урон (-7f + 7f + 1 = 1) и 5 скорость атаки (4.0 + 1.0f = 5)
    val END_CRYSTAL_HOE: Supplier<Item> = registerWeapon("end_crystal_hoe") {
        HoeItem(ModItemTier.END_CRYSTAL, Item.Properties().attributes(
            HoeItem.createAttributes(ModItemTier.END_CRYSTAL, -7f, 1.0f)
        ))
    }

    // Кристальный щит Энда (для тарана при рывке на Элитрах)
    val END_CRYSTAL_SHIELD: Supplier<Item> = registerWeapon("end_crystal_shield") {
        object : ShieldItem(Item.Properties().durability(1008).rarity(Rarity.RARE)) {
            override fun isValidRepairItem(toRepair: ItemStack, repair: ItemStack): Boolean {
                return repair.`is`(ModItems.END_CRYSTAL_GEM.get())
            }
        }
    }

    // --- ОРУЖИЕ БОССОВ И МИНИ-БОССОВ ---

    // Булава Скорби: 10 (бонус) + 4 (базовый урон незерита) + 1 (рука) = 15 общего урона, перезарядка ~2.2 сек (-3.55f)
    val WITHER_MACE: Supplier<Item> = registerWeapon("wither_mace") {
        object : SwordItem(
            Tiers.NETHERITE,
            Item.Properties()
                .durability(1200)
                .rarity(Rarity.EPIC)
                .attributes(
                    SwordItem.createAttributes(Tiers.NETHERITE, 10, -3.55f)
                )
        ) {
            override fun postHurtEnemy(
                stack: ItemStack,
                target: net.minecraft.world.entity.LivingEntity,
                attacker: net.minecraft.world.entity.LivingEntity
            ) {
                target.addEffect(
                    net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.WITHER,
                        80,
                        1
                    )
                )
                super.postHurtEnemy(stack, target, attacker)
            }
        }
    }

    // Дубинка с каменными шипами: аналог каменного топора (9 урона, скорость 0.8), лёгкое отбрасывание и 30% шанс замедления
    val SPIKED_STONE_CLUB: Supplier<Item> = registerWeapon("spiked_stone_club") {
        object : SwordItem(
            Tiers.STONE,
            Item.Properties()
                .durability(160)
                .attributes(
                    SwordItem.createAttributes(Tiers.STONE, 7, -3.2f)
                )
        ) {
            override fun postHurtEnemy(
                stack: ItemStack,
                target: net.minecraft.world.entity.LivingEntity,
                attacker: net.minecraft.world.entity.LivingEntity
            ) {
                // Лёгкое дополнительное отбрасывание
                val look = attacker.lookAngle
                target.knockback(0.35, -look.x, -look.z)

                // 30% шанс оглушить (Замедление I на 2 секунды)
                if (attacker.random.nextFloat() < 0.30f) {
                    target.addEffect(
                        net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                            40,
                            0
                        )
                    )
                }
                super.postHurtEnemy(stack, target, attacker)
            }
        }
    }

    val POISON_THROWING_DAGGER: Supplier<Item> = registerWeapon("poison_throwing_dagger") {
        ru.rlyqaz.divinesouls.item.custom.PoisonThrowingDaggerItem(
            Item.Properties().stacksTo(32)
        )
    }

    val FROST_STAFF: Supplier<Item> = registerWeapon("frost_staff") {
        ru.rlyqaz.divinesouls.item.custom.FrostStaffItem(
            Item.Properties().durability(280).rarity(net.minecraft.world.item.Rarity.UNCOMMON)
        )
    }

    // Хелпер для чистой регистрации
    private fun <T : Item> registerWeapon(name: String, itemSupplier: () -> T): Supplier<T> {
        return REGISTRY.register(name, itemSupplier)
    }

    fun register(eventBus: IEventBus) {
        // Оставляем пустым для триггера загрузки полей мода
    }
}
