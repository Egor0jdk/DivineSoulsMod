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

    fun register(eventBus: IEventBus) {
        REGISTRY.register(eventBus)
    }
}
