package ru.rlyqaz.divinesouls.entity

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import ru.rlyqaz.divinesouls.DivineSoulsMod
import ru.rlyqaz.divinesouls.entity.custom.VoidDragonEntity
import ru.rlyqaz.divinesouls.entity.custom.SorrowWraithEntity
import ru.rlyqaz.divinesouls.entity.custom.TwinWitherSkeletonEntity
import ru.rlyqaz.divinesouls.entity.custom.MeadowSlimeEntity
import ru.rlyqaz.divinesouls.entity.custom.SandyCrabEntity
import ru.rlyqaz.divinesouls.entity.custom.CaveGoblinEntity
import ru.rlyqaz.divinesouls.entity.custom.SwampSpiritEntity
import ru.rlyqaz.divinesouls.entity.custom.GiantWaspEntity
import ru.rlyqaz.divinesouls.entity.custom.PoisonDaggerEntity
import ru.rlyqaz.divinesouls.entity.custom.IceElementalEntity
import ru.rlyqaz.divinesouls.entity.custom.IceShardEntity
import ru.rlyqaz.divinesouls.entity.custom.JungleSpiderEntity
import ru.rlyqaz.divinesouls.entity.custom.ForestEntEntity
import ru.rlyqaz.divinesouls.entity.custom.SculkCrawlerEntity
import ru.rlyqaz.divinesouls.entity.custom.SculkStalkerEntity
import java.util.function.Supplier

/**
 * ====================================================================
 * НАЗВАНИЕ МОДА: Divine Souls (1.21.1 NeoForge)
 * КЛАСС: ModEntities (Регистратор RPG-мобов и сущностей)
 * ====================================================================
 */
object ModEntities {
    private val REGISTRY: DeferredRegister<EntityType<*>> =
        DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, DivineSoulsMod.MOD_ID)

    // ФИКС: Переписали через явный Supplier, чтобы убрать ошибку Overload resolution ambiguity на первой строчке!
    val VOID_DRAGON: Supplier<EntityType<VoidDragonEntity>> = REGISTRY.register("void_dragon", Supplier {
        EntityType.Builder.of({ type: EntityType<VoidDragonEntity>, level ->
            VoidDragonEntity(type, level)
        }, MobCategory.MONSTER)
            .sized(1.2f, 1.2f)
            .build("void_dragon")
    })

    // БАГФИКС: Используем явную обёртку Supplier, чтобы намертво уничтожить Overload resolution ambiguity!
    val SORROW_WRAITH = REGISTRY.register("sorrow_wraith", java.util.function.Supplier {
        net.minecraft.world.entity.EntityType.Builder.of({ type: net.minecraft.world.entity.EntityType<SorrowWraithEntity>, level ->
            SorrowWraithEntity(type, level)
        }, net.minecraft.world.entity.MobCategory.MONSTER)
            .sized(0.6f, 1.8f)
            .build("sorrow_wraith")
    })

    val TWIN_WITHER_SKELETON: Supplier<EntityType<TwinWitherSkeletonEntity>> = REGISTRY.register("twin_wither_skeleton", Supplier {
        EntityType.Builder.of(::TwinWitherSkeletonEntity, MobCategory.MONSTER)
            .sized(0.95f, 3.1f)
            .fireImmune()
            .build("twin_wither_skeleton")
    })

    val MEADOW_SLIME: Supplier<EntityType<MeadowSlimeEntity>> = REGISTRY.register("meadow_slime", Supplier {
        EntityType.Builder.of(::MeadowSlimeEntity, MobCategory.MONSTER)
            .sized(0.52f, 0.52f)
            .clientTrackingRange(10)
            .build("meadow_slime")
    })

    val SANDY_CRAB: Supplier<EntityType<SandyCrabEntity>> = REGISTRY.register("sandy_crab", Supplier {
        EntityType.Builder.of(::SandyCrabEntity, MobCategory.MONSTER)
            .sized(0.85f, 0.55f)
            .clientTrackingRange(10)
            .build("sandy_crab")
    })

    val CAVE_GOBLIN: Supplier<EntityType<CaveGoblinEntity>> = REGISTRY.register("cave_goblin", Supplier {
        EntityType.Builder.of(::CaveGoblinEntity, MobCategory.MONSTER)
            .sized(0.6f, 1.4f)
            .clientTrackingRange(10)
            .build("cave_goblin")
    })

    val SWAMP_SPIRIT: Supplier<EntityType<SwampSpiritEntity>> = REGISTRY.register("swamp_spirit", Supplier {
        EntityType.Builder.of(::SwampSpiritEntity, MobCategory.MONSTER)
            .sized(0.65f, 0.65f)
            .clientTrackingRange(12)
            .build("swamp_spirit")
    })

    val GIANT_WASP: Supplier<EntityType<GiantWaspEntity>> = REGISTRY.register("giant_wasp", Supplier {
        EntityType.Builder.of(::GiantWaspEntity, MobCategory.MONSTER)
            .sized(0.85f, 0.95f)
            .clientTrackingRange(10)
            .build("giant_wasp")
    })

    val POISON_DAGGER: Supplier<EntityType<PoisonDaggerEntity>> = REGISTRY.register("poison_dagger", Supplier {
        EntityType.Builder.of(::PoisonDaggerEntity, MobCategory.MISC)
            .sized(0.25f, 0.25f)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build("poison_dagger")
    })

    val ICE_ELEMENTAL: Supplier<EntityType<IceElementalEntity>> = REGISTRY.register("ice_elemental", Supplier {
        EntityType.Builder.of(::IceElementalEntity, MobCategory.MONSTER)
            .sized(0.95f, 1.35f)
            .clientTrackingRange(12)
            .build("ice_elemental")
    })

    val ICE_SHARD: Supplier<EntityType<IceShardEntity>> = REGISTRY.register("ice_shard", Supplier {
        EntityType.Builder.of(::IceShardEntity, MobCategory.MISC)
            .sized(0.3f, 0.3f)
            .clientTrackingRange(6)
            .updateInterval(10)
            .build("ice_shard")
    })

    val JUNGLE_SPIDER: Supplier<EntityType<JungleSpiderEntity>> = REGISTRY.register("jungle_spider", Supplier {
        EntityType.Builder.of(::JungleSpiderEntity, MobCategory.MONSTER)
            .sized(1.25f, 0.8f)
            .clientTrackingRange(8)
            .build("jungle_spider")
    })

    val FOREST_ENT: Supplier<EntityType<ForestEntEntity>> = REGISTRY.register("forest_ent", Supplier {
        EntityType.Builder.of(::ForestEntEntity, MobCategory.MONSTER)
            .sized(1.0f, 4.0f)
            .clientTrackingRange(10)
            .build("forest_ent")
    })

    val SCULK_CRAWLER: Supplier<EntityType<SculkCrawlerEntity>> = REGISTRY.register("sculk_crawler", Supplier {
        EntityType.Builder.of(::SculkCrawlerEntity, MobCategory.MONSTER)
            .sized(1.15f, 0.75f)
            .clientTrackingRange(10)
            .build("sculk_crawler")
    })

    val SCULK_STALKER: Supplier<EntityType<SculkStalkerEntity>> = REGISTRY.register("sculk_stalker", Supplier {
        EntityType.Builder.of(::SculkStalkerEntity, MobCategory.MONSTER)
            .sized(0.85f, 1.95f)
            .clientTrackingRange(12)
            .build("sculk_stalker")
    })

    fun register(eventBus: IEventBus) {
        REGISTRY.register(eventBus)
    }
}
