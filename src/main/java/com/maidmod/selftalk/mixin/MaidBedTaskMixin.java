package com.maidmod.selftalk.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidBedTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.maidmod.selftalk.MaidSleepTalkService;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 「叫不醒」的「醒后别马上又睡回去」：被叫醒后的保持清醒窗口内，不让 TLM 的 {@code MaidBedTask} 开始。
 * <p>
 * 夜间日程（REST）下，{@code MaidBedTask} 会让女仆走向床并 {@code startSleeping}，
 * 且「走向床」本身就发生在 {@code checkExtraStartConditions} 里。窗口内整个方法直接返回 {@code false}，
 * 她既不会走向床也不会再睡下，也就不需要靠反复 {@code stopSleeping} 去对抗，
 * 不会出现"上床又起来"的抖动。不改日程，也不动玩家的作息设置；窗口过去后她按 TLM 自己的作息行事。
 * <p>
 * {@code require = 0}：本 mod 的混入配置默认要求每个注入都必须命中，而这里的目标是主 Mod 的内部方法，
 * 主 Mod 将来改名时不应连累整个游戏启动失败。注入失败时只是退回到
 * {@code SelfTalkHandler} 里"窗口内又睡着就再叫醒"的兜底，表现和以前一样。
 */
@Mixin(value = MaidBedTask.class, remap = false)
public abstract class MaidBedTaskMixin {

    @Inject(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;"
            + "Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;)Z",
            remap = false, require = 0, cancellable = true, at = @At("HEAD"))
    private void maid_self_talk$noBedWhileStayingAwake(ServerLevel level, EntityMaid maid,
                                                       CallbackInfoReturnable<Boolean> cir) {
        if (MaidSleepTalkService.isStayingAwake(maid)) {
            cir.setReturnValue(false);
        }
    }
}
