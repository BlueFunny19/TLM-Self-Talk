package com.maidmod.selftalk;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.MaidAIChatManager;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMMessage;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMSite;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.AIConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * 睡眠功能服务：梦话（呓语）与「叫不醒」的嘟囔。
 * <p>
 * 两条链共用一套 AI 前置与派发方式（与 {@link MaidSelfTalkService}／{@link MaidInterChatService} 同口径）：
 * <ol>
 *   <li>人设 system 消息沿用 {@link SelfTalkContexts#fetchCleanedMessages}（与玩家 chat 同构，
 *       保证 LLM 提供商上下文前缀缓存一致，并做未配对 tool 消息清洗）；</li>
 *   <li>指令取 {@link SleepTalkPrompts} 的中／英版本，语言既非中文也非英文时用英文版并在末尾追加
 *       {@link SelfTalkContexts#languageInstruction}（现有惯例）；</li>
 *   <li>user 消息<b>不写入</b> TLM 历史（系统内部消息，不出现在聊天记录 UI 中），
 *       回复也不写历史——见 {@link SleepTalkCallback}；</li>
 *   <li>不带 Tool（{@code needAddTools = false}），不进 Tool 循环。</li>
 * </ol>
 * <p>
 * 所有方法都必须在 <b>服务端主线程</b>调用（女仆 tick 与 TLM 玩家 chat 入口都是主线程）。
 */
public final class MaidSleepTalkService {

    /**
     * 梦话／嘟囔的聊天栏广播半径兜底（格）：配置 {@code sleep.playerRange = 0}
     * 表示「不要求有人在场」，此时用该固定半径广播，保持「气泡 + 聊天栏」的显示口径。
     */
    private static final double FALLBACK_BROADCAST_RANGE = 16.0;

    /** 「叫不醒」判定中视为「与该女仆相关」的玩家半径（格）：主人之外的玩家需在此范围内 */
    private static final double RELATED_PLAYER_RANGE = 8.0;

    /** 醒来提示的有效时长（tick）：30 秒内到达的第一次正常回复才附带，避免过期提示串到别的对话 */
    private static final long WAKE_NOTE_VALID_TICKS = 30 * 20L;

    /** 梦话／嘟囔回复的最大字符数：只取一小句，避免长段独白 */
    private static final int MAX_SPEECH_CHARS = 40;

    private MaidSleepTalkService() {
    }

    // ===== AI 前置 =====

    /**
     * 睡眠功能的 AI 前置是否满足：有 AI 会话管理、LLM 总开关开启、站点可用、女仆有人设。
     * <p>
     * 与 {@link MaidInterChatService#canTriggerInterChat} 同一组判断（沿用 service 层现有口径）。
     * 仅做只读判断，不产生副作用；必须在服务端主线程调用。
     */
    public static boolean canTriggerSleepTalk(EntityMaid maid) {
        if (maid == null) {
            return false;
        }
        MaidAIChatManager chatManager = maid.getAiChatManager();
        if (chatManager == null) return false;
        if (!AIConfig.LLM_ENABLED.get()) return false;
        LLMSite site = chatManager.getLLMSite();
        if (site == null || !site.enabled()) return false;
        // 无人设（无自定义设定且无模型默认设定）→ 不触发，不自动生成
        if (chatManager.customSetting.isBlank() && chatManager.getSetting().isEmpty()) return false;
        return true;
    }

    // ===== 梦话（呓语）=====

    /**
     * 触发一次梦话：人设 system 消息 + {@link SleepTalkPrompts#dreamTalk} 指令，
     * 回复由 {@link SleepTalkCallback} 显示为气泡并广播。
     *
     * @param broadcastRange 聊天栏广播半径（格）
     * @return 是否实际发起请求
     */
    public static boolean triggerDreamTalk(EntityMaid maid, double broadcastRange) {
        if (!canTriggerSleepTalk(maid)) {
            return false;
        }
        MaidAIChatManager chatManager = maid.getAiChatManager();
        String language = resolveLanguage(chatManager);
        List<LLMMessage> messages = SelfTalkContexts.fetchCleanedMessages(chatManager, language, "sleep-talk");
        if (messages == null) {
            return false;
        }
        String prompt = applyLanguage(SleepTalkPrompts.dreamTalk(SelfTalkContexts.isChinese(language)), language);
        messages.add(LLMMessage.userChat(maid, prompt));
        return dispatch(maid, chatManager, messages, broadcastRange);
    }

    // ===== 叫不醒：迷糊嘟囔 =====

    /**
     * 触发一次「迷糊嘟囔」：人设 system 消息 + {@link SleepTalkPrompts#mumbleReply} 指令，
     * 并把玩家这句话<b>原样</b>接在指令之后（只用于让她把声音混进梦里，不进入 TLM 历史）。
     *
     * @param playerMessage  玩家刚才说的话（可能为空：仅作为声音素材）
     * @param broadcastRange 聊天栏广播半径（格）
     * @return 是否实际发起请求
     */
    public static boolean triggerMumble(EntityMaid maid, String playerMessage, double broadcastRange) {
        if (!canTriggerSleepTalk(maid)) {
            return false;
        }
        MaidAIChatManager chatManager = maid.getAiChatManager();
        String language = resolveLanguage(chatManager);
        List<LLMMessage> messages = SelfTalkContexts.fetchCleanedMessages(chatManager, language, "sleep-talk");
        if (messages == null) {
            return false;
        }
        StringBuilder userContent = new StringBuilder(
                applyLanguage(SleepTalkPrompts.mumbleReply(SelfTalkContexts.isChinese(language)), language));
        if (!StringUtils.isBlank(playerMessage)) {
            // 玩家原话原样附在指令之后（不另加自造文字）；段标签照现有口径剥除
            userContent.append("\n\n").append(SegmentTags.stripTagsFromPlayerInput(playerMessage));
        }
        messages.add(LLMMessage.userChat(maid, userContent.toString()));
        return dispatch(maid, chatManager, messages, broadcastRange);
    }

    /**
     * 派发睡眠请求：置 pending（与自话/互聊共用「同一只女仆同时只有一个在途请求」的约束）后提交 LLM 客户端。
     * <p>
     * 同步阶段就抛异常（如 site.url 非法）时复位 pending，绝不向上抛——
     * 调用方可能处于实体 tick 或 TLM 玩家聊天入口。
     */
    private static boolean dispatch(EntityMaid maid, MaidAIChatManager chatManager, List<LLMMessage> messages,
                                    double broadcastRange) {
        SelfTalkState.State state = SelfTalkState.get(maid.getId());
        state.sleepTalkPending = true;
        state.sleepTalkPendingSinceTick = maid.level().getServer().getTickCount();
        try {
            LLMSite site = chatManager.getLLMSite();
            site.client().chat(new SleepTalkCallback(chatManager, messages, broadcastRange));
        } catch (Throwable t) {
            state.sleepTalkPending = false;
            state.sleepTalkPendingSinceTick = -1;
            MaidSelfTalkMod.LOGGER.warn("Failed to dispatch sleep-talk request for maid {}", maid.getId(), t);
            return false;
        }
        return true;
    }

    // ===== 叫不醒：玩家聊天拦截（由 MaidAIChatManagerMixin 在 chat() HEAD 调用）=====

    /**
     * 玩家对熟睡女仆说话时的处理入口。
     *
     * @return {@code true} 表示调用方应当取消本次 {@code chat()}（继续迷糊嘟囔）；
     *         {@code false} 表示放行（叫醒她的那一次，或本功能不适用）
     */
    public static boolean interceptPlayerChat(EntityMaid maid, ServerPlayer player, String message) {
        try {
            if (maid == null || player == null) {
                return false;
            }
            if (!Config.ENABLED.get() || !Config.WAKE_RESISTANCE_ENABLED.get()) {
                return false;
            }
            // 「真玩家」判定：ServerPlayer 参数本身已是真实玩家（TLM 内部调用传 null 会在 chat() 内部 NPE，
            // 本 mod 自话/互聊/梦话从不走 chat()）；旁观者不参与
            if (player.isRemoved() || player.isSpectator()) {
                return false;
            }
            if (!(maid.level() instanceof ServerLevel level)) {
                return false;
            }
            if (!maid.isSleeping()) {
                return false;
            }
            // 只拦「与该女仆有关」的玩家：主人，或 8 格内的玩家。
            // 其余一律放行——避免拦截别的 Mod 通过 chat() 发起的内部调用（如记忆整理）
            if (!isRelatedPlayer(maid, player)) {
                return false;
            }
            SelfTalkState.State state = SelfTalkState.get(maid.getId());
            long nowTick = level.getServer().getTickCount();
            WakeResistance.Decision decision = WakeResistance.onPlayerCall(state.wakeCalls, nowTick,
                    Config.WAKE_MIN_CALLS.get(), Config.WAKE_MAX_CALLS.get(),
                    Config.WAKE_STREAK_RESET_SECONDS.get(), Math.random());
            if (decision == WakeResistance.Decision.WAKE) {
                // 第 N 次：叫醒她并放行 chat()，让正常流程继续回复；
                // 醒来后的第一次正常回复会附带「刚被叫醒」提示（见 consumeWakeNote）
                maid.stopSleeping();
                state.justWokenTick = nowTick;
                int stayAwakeSeconds = Config.WAKE_STAY_AWAKE_SECONDS.get();
                state.stayAwakeUntilTick = stayAwakeSeconds > 0 ? nowTick + stayAwakeSeconds * 20L : 0;
                state.nextStayAwakeCheckTick = 0;
                return false;
            }
            // 第 1..N-1 次：取消 chat()，改为一次迷糊嘟囔。
            // 同一只女仆同时只允许一个在途 Self Talk 请求：忙时退化为不回复（静默），但仍然计数
            if (!isBusy(state)) {
                triggerMumble(maid, message, sleepTalkBroadcastRange());
            }
            return true;
        } catch (Throwable t) {
            // 绝不向上抛：本方法位于 TLM 玩家聊天派发入口，异常会直接打断玩家聊天
            MaidSelfTalkMod.LOGGER.error("Failed to handle wake resistance for maid {}",
                    maid == null ? -1 : maid.getId(), t);
            return false;
        }
    }

    /**
     * 取用「刚被叫醒」提示（供 MaidAIChatManagerMixin 的 userChat @Redirect 调用）。
     * <p>
     * 标记存在且在有效期内（30 秒）时返回提示文本并<b>立即清除</b>；否则返回 null。
     * 语言处理与梦话一致：中／英文用对应版本，其余语言用英文版 + 语言指令。
     * 必须在服务端主线程调用。
     */
    public static String consumeWakeNote(EntityMaid maid) {
        if (maid == null) {
            return null;
        }
        SelfTalkState.State state = SelfTalkState.peek(maid.getId());
        if (state == null || state.justWokenTick < 0) {
            return null;
        }
        long nowTick = maid.level().getServer().getTickCount();
        long elapsed = nowTick - state.justWokenTick;
        state.justWokenTick = -1;
        if (elapsed > WAKE_NOTE_VALID_TICKS) {
            return null;
        }
        String language = resolveLanguage(maid.getAiChatManager());
        return applyLanguage(SleepTalkPrompts.wakeUpNote(SelfTalkContexts.isChinese(language)), language);
    }

    /**
     * 她此刻是否处在「被叫醒后保持清醒」的窗口里。
     * <p>
     * 供 {@code MaidBedTaskMixin} 调用：窗口内不让她开始走向床、上床睡觉。
     * 只读状态，不创建记录；非服务端、没有记录、窗口已过期都返回 {@code false}。
     */
    public static boolean isStayingAwake(EntityMaid maid) {
        if (maid == null || !(maid.level() instanceof ServerLevel level)) {
            return false;
        }
        SelfTalkState.State state = SelfTalkState.peek(maid.getId());
        return state != null
                && WakeResistance.isStayAwake(state.stayAwakeUntilTick, level.getServer().getTickCount());
    }

    /** 该玩家是否与女仆相关：主人，或（同维度）8 格内的玩家 */
    private static boolean isRelatedPlayer(EntityMaid maid, ServerPlayer player) {
        UUID ownerUuid = maid.getOwnerUUID();
        if (ownerUuid != null && ownerUuid.equals(player.getUUID())) {
            return true;
        }
        return maid.level() == player.level()
                && maid.distanceToSqr(player) <= RELATED_PLAYER_RANGE * RELATED_PLAYER_RANGE;
    }

    /** 是否已有在途请求（自话／互聊／梦话），或玩家 chat 计数未清零 */
    private static boolean isBusy(SelfTalkState.State state) {
        return state.selfTalkPending || state.interChatPending || state.sleepTalkPending
                || state.playerChatCount > 0;
    }

    /** 梦话／嘟囔的聊天栏广播半径（配置 0 时回落到固定值） */
    public static double sleepTalkBroadcastRange() {
        double range = Config.SLEEP_TALK_PLAYER_RANGE.get();
        return range > 0 ? range : FALLBACK_BROADCAST_RANGE;
    }

    /**
     * 本次对话语言：优先女仆已记录的聊天语言（玩家 chat 过则为客户端语言，保证上下文前缀缓存一致），
     * 否则用配置默认（与自话／互聊同口径）。
     */
    static String resolveLanguage(MaidAIChatManager chatManager) {
        return SelfTalkContexts.sanitizeLanguage(StringUtils.isBlank(chatManager.chatLanguage)
                ? Config.SELF_TALK_LANGUAGE.get() : chatManager.chatLanguage);
    }

    /**
     * 语言包装：中文／英文直接用对应版本的提示词；其余语言用英文版并在末尾追加语言指令
     * （与 {@link SelfTalkContexts#languageInstruction} 的既有惯例一致）。
     */
    private static String applyLanguage(String base, String language) {
        if (SelfTalkContexts.isChinese(language) || isEnglish(language)) {
            return base;
        }
        return base + SelfTalkContexts.languageInstruction(language);
    }

    /** 语言是否英文（en_us / en） */
    private static boolean isEnglish(String language) {
        return SelfTalkContexts.sanitizeLanguage(language).startsWith("en");
    }

    /**
     * 回复文本规范化：取纯文本、去掉首尾成对引号、压掉换行、截断到 {@value #MAX_SPEECH_CHARS} 个字符。
     * <p>
     * 梦话与嘟囔都只应是一小句，超长内容按字符截断（不做词边界处理）。
     */
    static String normalizeSpeech(String raw) {
        if (raw == null) {
            return StringUtils.EMPTY;
        }
        String text = raw.replace('\r', ' ').replace('\n', ' ').trim();
        text = trimQuotes(text);
        if (text.length() > MAX_SPEECH_CHARS) {
            text = text.substring(0, MAX_SPEECH_CHARS).trim();
            text = trimQuotes(text);
        }
        return text;
    }

    /** 去掉首尾成对引号（中英引号；只有两侧成对时才剥除，避免误删正文符号） */
    private static String trimQuotes(String text) {
        String result = text;
        boolean changed = true;
        while (changed && result.length() >= 2) {
            changed = false;
            char first = result.charAt(0);
            char last = result.charAt(result.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')
                    || (first == '\u201c' && last == '\u201d')
                    || (first == '\u300c' && last == '\u300d')
                    || (first == '\u2018' && last == '\u2019')) {
                result = result.substring(1, result.length() - 1).trim();
                changed = true;
            }
        }
        return result;
    }
}
