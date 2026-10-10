package com.maidmod.selftalk;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.LLMCallback;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.MaidAIChatManager;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.response.ResponseChat;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMMessage;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.UUID;

/**
 * 睡眠专用（梦话 / 迷糊嘟囔）的 LLM 回调。
 * <p>
 * 与 {@link SelfTalkCallback}／{@link InterChatCallback} 的关键差异：
 * <ul>
 *   <li>父类构造器第三参数传 {@code true}，<b>不创建</b> TLM 默认的「少女思考中」等待气泡：
 *       玩家叫熟睡女仆时不应看到思考提示（{@link MaidAIChatManager} 的玩家 chat 才有等待气泡）；</li>
 *   <li>{@code needAddTools = false}：模型看不到任何工具定义，天然不会调用 tool，也不进 Tool 循环；</li>
 *   <li>{@link #onSuccess} <b>不调父类</b>：不写 TLM 历史（梦话/嘟囔不出现在聊天记录界面）、
 *       不执行遗忘、不广播 {@link MaidChatReplyEvent}、不走 TTS；显示口径完全由本类接管：
 *       头顶气泡 + 聊天栏 {@code <女仆名> 文字}（附近玩家可见，主人由
 *       {@code ChatBubbleManager.addLLMChatText} 通知，故广播时跳过主人避免重复）；</li>
 *   <li>{@link #onFailure} <b>不调父类</b>：父类会给主人发红色错误文本并移除气泡——
 *       睡眠请求失败应当静默（玩家不会看到任何报错）。</li>
 * </ul>
 * 状态写入一律调度回服务端主线程；回调到达时若女仆已经醒了，则丢弃这句话。
 */
public class SleepTalkCallback extends LLMCallback {

    /** 聊天框广播半径（格）：范围内存活玩家可见梦话/嘟囔 */
    private final double broadcastRange;

    public SleepTalkCallback(MaidAIChatManager chatManager, List<LLMMessage> messages, double broadcastRange) {
        super(chatManager, messages, true);
        this.broadcastRange = broadcastRange;
        this.needAddTools = false;
    }

    @Override
    public void onSuccess(ResponseChat responseChat) {
        // 防御性剥离段标签（本类请求不含段标签，与互聊路径同口径）
        SegmentTags.stripResponse(responseChat);
        String text = MaidSleepTalkService.normalizeSpeech(responseChat.getChatText());
        EntityMaid maid = getMaid();
        Runnable finish = () -> {
            if (maid == null) {
                return;
            }
            clearPending(maid);
            if (!maid.isAlive() || text.isBlank()) {
                return;
            }
            // 回调到达时已经醒了：这句梦话/嘟囔不再显示
            if (!maid.isSleeping()) {
                return;
            }
            if (maid.level() instanceof ServerLevel serverLevel) {
                // 与 TLM 一致：气泡更新投递到服务端主线程任务队列
                serverLevel.getServer().submit(() -> maid.getChatBubbleManager().addLLMChatText(text, 0L));
            }
            broadcastToNearby(maid, text);
        };
        if (isOnServerThread()) {
            finish.run();
        } else {
            runOnServerThread(finish);
        }
    }

    @Override
    public void onFailure(HttpRequest request, Throwable throwable, int errorCode) {
        // 不调父类：失败必须静默（父类会给主人发红色错误文本）。只复位 pending，防卡死
        EntityMaid maid = getMaid();
        if (maid == null) {
            return;
        }
        Runnable finish = () -> clearPending(maid);
        if (isOnServerThread()) {
            finish.run();
        } else {
            runOnServerThread(finish);
        }
    }

    /** 复位本女仆的睡眠请求 pending（服务端主线程；用不创建状态的查询） */
    private void clearPending(EntityMaid maid) {
        SelfTalkState.State state = SelfTalkState.peek(maid.getId());
        if (state != null && state.sleepTalkPending) {
            state.sleepTalkPending = false;
            state.sleepTalkPendingSinceTick = -1;
        }
    }

    /**
     * 把内容广播到附近玩家的聊天框，格式与自话/互聊一致：{@code <女仆名> 内容}。
     * <p>
     * 注意：{@code ChatBubbleManager.addLLMChatText} 已给<b>主人</b>发送同格式聊天栏消息，
     * 此处跳过主人，只广播给其他附近玩家，避免重复。
     */
    private void broadcastToNearby(EntityMaid maid, String chatText) {
        if (!(maid.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        UUID ownerUuid = maid.getOwnerUUID();
        Component message = Component.literal("<")
                .append(maid.getName())
                .append("> ")
                .append(chatText)
                .withStyle(ChatFormatting.GRAY);
        AABB box = maid.getBoundingBox().inflate(broadcastRange);
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, box,
                p -> p.isAlive() && !p.isSpectator() && !p.getUUID().equals(ownerUuid))) {
            player.sendSystemMessage(message);
        }
    }
}
