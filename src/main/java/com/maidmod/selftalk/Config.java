package com.maidmod.selftalk;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * 模组配置（COMMON 档，管理员统筹）。
 * <p>
 * 注意：本模组为全新实现，配置文件结构不兼容旧版 maid_self_talk。
 * 配置文件名仍为 maid_self_talk-common.toml（Forge 自动生成），
 * 旧文件中的未知键会被 Forge 忽略，新键使用默认值。
 * 1.0.1 起自话/欢迎提示词硬编码于 {@link SelfTalkPrompts}，不再从本配置读取。
 * <p>
 * 注意：本分支为 Forge 1.20.1，<b>无 NeoForge 的 ConfigFileWatcher 热重载</b>——
 * 替换 toml 后需重启服务端才生效（1.21.1 线替换即生效，双分支行为不同）。
 */
public final class Config {
    /** 总开关 */
    public static ForgeConfigSpec.BooleanValue ENABLED;
    /** 是否允许玩家自定义配置（关闭后玩家设置项置灰，自话/互聊视为启用，自定义 Prompt 不注入、Tool 不开启） */
    public static ForgeConfigSpec.BooleanValue PLAYER_OPTION_ENABLED;
    /** 是否允许女仆的自话/互聊真正调用工具（Tool 功能总闸，玩家层面开关在此基础上二次约束） */
    public static ForgeConfigSpec.BooleanValue TOOL_CALL_ENABLED;

    /** 态 1：主人在线 */
    public static ForgeConfigSpec.BooleanValue STATE1_ENABLED;
    /** 态 1 最小触发间隔（秒） */
    public static ForgeConfigSpec.IntValue STATE1_MIN_INTERVAL;
    /** 态 1 最大触发间隔（秒） */
    public static ForgeConfigSpec.IntValue STATE1_MAX_INTERVAL;
    /** 态 1：半径多少格内有玩家时才触发 */
    public static ForgeConfigSpec.DoubleValue STATE1_PLAYER_RANGE;
    /** 态 1 自言自语保留上下文条数 */
    public static ForgeConfigSpec.IntValue STATE1_KEEP_SELF_TALK_COUNT;

    /** 态 2：主人离线但附近有玩家 */
    public static ForgeConfigSpec.BooleanValue STATE2_ENABLED;
    /** 态 2 最小触发间隔（秒） */
    public static ForgeConfigSpec.IntValue STATE2_MIN_INTERVAL;
    /** 态 2 最大触发间隔（秒） */
    public static ForgeConfigSpec.IntValue STATE2_MAX_INTERVAL;
    /** 态 2：半径多少格内有玩家时才触发 */
    public static ForgeConfigSpec.DoubleValue STATE2_PLAYER_RANGE;
    /** 态 2 自言自语保留上下文条数 */
    public static ForgeConfigSpec.IntValue STATE2_KEEP_SELF_TALK_COUNT;

    /** 欢迎语开关 */
    public static ForgeConfigSpec.BooleanValue WELCOME_ENABLED;
    /** 玩家登录后的欢迎触发窗口（tick，20 tick = 1 秒） */
    public static ForgeConfigSpec.IntValue WELCOME_WINDOW_TICKS;

    /** 欢迎语秒级限流：每秒最多放行的欢迎触发次数（所有女仆共享） */
    public static ForgeConfigSpec.IntValue MAX_TRIGGER_PER_SECOND;
    /** 自话放行随机间隔区间下限（秒） */
    public static ForgeConfigSpec.IntValue SELF_TALK_MIN_INTERVAL;
    /** 自话放行随机间隔区间上限（秒） */
    public static ForgeConfigSpec.IntValue SELF_TALK_MAX_INTERVAL;
    /** 顺延队列上限：每只女仆忙时积压的自话/互聊请求数上限，超出即吞（默认顺延，队列过多才吞） */
    public static ForgeConfigSpec.IntValue DEFER_QUEUE_MAX;

    /** 自话输出语言（TLM 官方模型设定多为英文，需要显式声明输出语言） */
    public static ForgeConfigSpec.ConfigValue<String> SELF_TALK_LANGUAGE;

    // ===== 互聊 =====
    /** 互聊总开关 */
    public static ForgeConfigSpec.BooleanValue INTER_CHAT_ENABLED;
    /** 互聊最小触发间隔（秒） */
    public static ForgeConfigSpec.IntValue INTER_CHAT_MIN_INTERVAL;
    /** 互聊最大触发间隔（秒） */
    public static ForgeConfigSpec.IntValue INTER_CHAT_MAX_INTERVAL;
    /** 互聊：半径多少格内有玩家时才触发 */
    public static ForgeConfigSpec.DoubleValue INTER_CHAT_PLAYER_RANGE;
    /** 互聊：自身多少格内有另一只女仆时才触发 */
    public static ForgeConfigSpec.DoubleValue INTER_CHAT_MAID_RANGE;
    /** 互聊保留轮数（问/答算一轮，单问也算一轮） */
    public static ForgeConfigSpec.IntValue INTER_CHAT_KEEP_ROUNDS;
    /** 互聊连续触发概率（0~1） */
    public static ForgeConfigSpec.DoubleValue INTER_CHAT_CHAIN_PROBABILITY;
    public static ForgeConfigSpec.IntValue INTER_CHAT_MAX_CHAIN_ROUNDS;
    /** 互聊对锁时长（秒）：A 对 C 发起互聊后，二者在连续互聊结束前互相对锁，超时兜底自动解除 */
    public static ForgeConfigSpec.IntValue INTER_CHAT_PAIR_LOCK_SECONDS;

    // ===== 环境感知（附近事件与自身状态） =====
    /** 环境感知总开关：附近死亡/玩家受伤与女仆自身感知的总闸（注入自话/互聊，欢迎语不受影响） */
    public static ForgeConfigSpec.BooleanValue EVENT_CONTEXT_ENABLED;
    /** 附近死亡与玩家受伤的感知半径（格），不约束女仆自身感知 */
    public static ForgeConfigSpec.DoubleValue EVENT_CONTEXT_RANGE;
    /** 每只女仆的事件缓冲容量（死亡/玩家受伤/自身受伤共用，溢出丢最旧） */
    public static ForgeConfigSpec.IntValue EVENT_CONTEXT_MAX_BUFFERED;
    /** 玩家受伤事件开关（受伤频率远高于死亡，默认关闭；不影响女仆自身感知） */
    public static ForgeConfigSpec.BooleanValue EVENT_CONTEXT_HURT_ENABLED;
    /** 自身感知开关（自身着火/缺氧/受伤） */
    public static ForgeConfigSpec.BooleanValue EVENT_CONTEXT_SELF_ENABLED;
    /** 受伤事件有效期（秒）：玩家受伤与自身受伤记录共用的有效时长 */
    public static ForgeConfigSpec.IntValue EVENT_CONTEXT_HURT_MAX_AGE_SECONDS;

    // ===== 独立聊天档案 =====
    /** 自话／欢迎语／互聊独立展示档案的总保留条数（每只女仆各自计算） */
    public static ForgeConfigSpec.IntValue CHAT_HISTORY_MAX_STORED;

    // ===== 附近女仆身份 =====
    /** 附近女仆身份上下文注入开关（仅启动时读取，改动需重启） */
    public static ForgeConfigSpec.BooleanValue MAID_IDENTITY_ENABLED;
    /** 附近女仆查询 Tool 开关（仅启动时读取，改动需重启） */
    public static ForgeConfigSpec.BooleanValue MAID_IDENTITY_TOOL_ENABLED;

    // ===== 睡眠：梦话与叫不醒 =====
    /** 梦话（呓语）开关：女仆熟睡时是否偶尔自发说一句梦话 */
    public static ForgeConfigSpec.BooleanValue SLEEP_TALK_ENABLED;
    /** 梦话最小间隔（秒） */
    public static ForgeConfigSpec.IntValue SLEEP_TALK_MIN_INTERVAL;
    /** 梦话最大间隔（秒） */
    public static ForgeConfigSpec.IntValue SLEEP_TALK_MAX_INTERVAL;
    /** 梦话：半径多少格内有玩家时才说（0 = 不要求有人在场） */
    public static ForgeConfigSpec.DoubleValue SLEEP_TALK_PLAYER_RANGE;
    /** 叫不醒开关：玩家叫熟睡的女仆时，前几次她只迷糊嘟囔 */
    public static ForgeConfigSpec.BooleanValue WAKE_RESISTANCE_ENABLED;
    /** 一轮连叫中需要叫醒她的最少次数（含） */
    public static ForgeConfigSpec.IntValue WAKE_MIN_CALLS;
    /** 一轮连叫中需要叫醒她的最多次数（含） */
    public static ForgeConfigSpec.IntValue WAKE_MAX_CALLS;
    /** 连叫计数清零时长（秒）：距上次叫她超过该时长即视为新一轮 */
    public static ForgeConfigSpec.IntValue WAKE_STREAK_RESET_SECONDS;
    /** 被叫醒后保持清醒的时长（秒，0 = 不处理） */
    public static ForgeConfigSpec.IntValue WAKE_STAY_AWAKE_SECONDS;
    /** 做梦开关：熟睡一会儿后由 LLM 按人设和白天的对话编出这一觉的梦 */
    public static ForgeConfigSpec.BooleanValue DREAM_ENABLED;
    /** 每次睡着做梦的概率（百分比） */
    public static ForgeConfigSpec.IntValue DREAM_CHANCE_PERCENT;
    /** 梦话是否取材于这一觉的梦 */
    public static ForgeConfigSpec.BooleanValue DREAM_TALK_REFERENCES;
    /** 醒来后是否把梦讲给主人 */
    public static ForgeConfigSpec.BooleanValue DREAM_REPORT_ENABLED;
    /** 自然醒来后多久内（秒）主人走近才会听到她讲梦 */
    public static ForgeConfigSpec.IntValue DREAM_REPORT_WINDOW_SECONDS;
    /** 她讲梦的那句话是否写进主模组的聊天历史 */
    public static ForgeConfigSpec.BooleanValue DREAM_WRITE_HISTORY;

    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("general");
        ENABLED = builder.comment("总开关，默认关闭。开启后女仆才会自言自语/欢迎/对话")
                .define("enabled", false);
        PLAYER_OPTION_ENABLED = builder.comment("""
                允许玩家自定义配置。玩家能否单独配置自己的女仆（自言自语、互聊、自定义 Prompt、Tool 调用）。
                关闭后玩家设置项置灰，自话/互聊视为启用，自定义 Prompt 不注入、Tool 调用不开启。""")
                .define("playerOptionEnabled", true);
        TOOL_CALL_ENABLED = builder.comment("""
                是否允许女仆的自言自语/互聊真正调用工具（切换工作任务、坐下、跟随等）。
                开启后女仆可以在自话/互聊中改变游戏状态，并显著增加 token 消耗，默认关闭。
                玩家层面的开关还需「允许玩家自定义配置」开启时才生效。""")
                .define("toolCallEnabled", false);
        builder.pop();

        builder.push("state_owner_online");
        STATE1_ENABLED = builder.comment("态 1：主人在线时女仆是否触发自言自语")
                .define("enabled", true);
        STATE1_MIN_INTERVAL = builder.comment("态 1 最小触发间隔（秒）")
                .defineInRange("minIntervalSeconds", 60, 10, 86400);
        STATE1_MAX_INTERVAL = builder.comment("态 1 最大触发间隔（秒）")
                .defineInRange("maxIntervalSeconds", 300, 10, 86400);
        STATE1_PLAYER_RANGE = builder.comment("态 1：半径多少格内有玩家时才触发")
                .defineInRange("playerRange", 16.0, 1.0, 512.0);
        STATE1_KEEP_SELF_TALK_COUNT = builder.comment("态 1：自言自语保留上下文条数。超过该条数时触发一次遗忘，仅保留最近一次自言自语")
                .defineInRange("keepSelfTalkCount", 5, 1, 50);
        builder.pop();

        builder.push("state_owner_offline");
        STATE2_ENABLED = builder.comment("态 2：主人离线但附近有玩家时女仆是否触发自言自语")
                .define("enabled", true);
        STATE2_MIN_INTERVAL = builder.comment("态 2 最小触发间隔（秒）")
                .defineInRange("minIntervalSeconds", 120, 10, 86400);
        STATE2_MAX_INTERVAL = builder.comment("态 2 最大触发间隔（秒）")
                .defineInRange("maxIntervalSeconds", 600, 10, 86400);
        STATE2_PLAYER_RANGE = builder.comment("态 2：半径多少格内有玩家时才触发")
                .defineInRange("playerRange", 32.0, 1.0, 512.0);
        STATE2_KEEP_SELF_TALK_COUNT = builder.comment("态 2：自言自语保留上下文条数。超过该条数时触发一次遗忘，仅保留最近一次自言自语")
                .defineInRange("keepSelfTalkCount", 3, 1, 50);
        builder.pop();

        builder.push("welcome");
        WELCOME_ENABLED = builder.comment("主人登录后女仆是否打招呼。欢迎语不受半径限制，加载区块内的女仆均可触发")
                .define("enabled", true);
        WELCOME_WINDOW_TICKS = builder.comment("玩家登录后的欢迎触发窗口（tick，20 tick = 1 秒）")
                .defineInRange("welcomeWindowTicks", 600, 20, 72000);
        builder.pop();

        builder.push("rate_limit");
        MAX_TRIGGER_PER_SECOND = builder.comment("""
                欢迎语限流：整个服务器每秒最多放行的欢迎触发次数（所有女仆共享）。
                防止主人登录时大量女仆同一瞬间并发建立 LLM 连接，导致端点 connect 超时。
                被限流的欢迎语不发请求、窗口期内每 tick 重试，玩家不会看到报错。""")
                .defineInRange("maxTriggerPerSecond", 1, 1, 20);
        SELF_TALK_MIN_INTERVAL = builder.comment("""
                自话限流：放行随机间隔区间下限（秒，全局共享）。
                两次自话派发之间至少间隔该时长，防启动/冷却同相时大量女仆并发建立 LLM 连接。
                被限流的自话不发请求、随机退避后重试，玩家不会看到报错。""")
                .defineInRange("selfTalkMinIntervalSeconds", 5, 1, 3600);
        SELF_TALK_MAX_INTERVAL = builder.comment("自话限流：放行随机间隔区间上限（秒），在区间内随机")
                .defineInRange("selfTalkMaxIntervalSeconds", 8, 1, 3600);
        DEFER_QUEUE_MAX = builder.comment("""
                顺延限流：每只女仆忙时积压的自话/互聊请求数上限（默认顺延）。
                顺延请求会在女仆空闲后按序派发（派发时才构建消息，保证上下文稳定）；
                积压超出该上限时才吞掉新请求（队列过多才吞，避免挤压自然触发队列）。""")
                .defineInRange("deferQueueMax", 2, 1, 10);
        builder.pop();

        builder.push("prompt");
        SELF_TALK_LANGUAGE = builder.comment("""
                自话输出语言（语言标签，如 zh_cn / en_us / ja_jp，支持任意合法语言标签，非法值回退 zh_cn）。
                注意：TLM 官方模型的人设设定多为英文，若不显式声明语言，女仆自话可能输出英文。
                此配置会：1) 作为设定占位符的替换语言；2) 向自话提示词注入对应语言的输出指令。
                自话/欢迎提示词本身已硬编码在 SelfTalkPrompts 中（1.0.1 起不再从本配置读取），
                旧配置文件中的 selfTalkPrompt / selfTalkPromptOwnerNearby / welcomePrompt 键被忽略，不影响运行。""")
                .define("selfTalkLanguage", "zh_cn");
        builder.pop();

        builder.push("inter_maid_chat");
        INTER_CHAT_ENABLED = builder.comment("女仆互聊总开关，默认关闭。开启后满足玩家距离与女仆间距离的女仆才会发起互聊")
                .define("enabled", false);
        INTER_CHAT_MIN_INTERVAL = builder.comment("互聊最小触发间隔（秒）")
                .defineInRange("minIntervalSeconds", 300, 10, 86400);
        INTER_CHAT_MAX_INTERVAL = builder.comment("互聊最大触发间隔（秒）")
                .defineInRange("maxIntervalSeconds", 600, 10, 86400);
        INTER_CHAT_PLAYER_RANGE = builder.comment("互聊：半径多少格内有玩家时才触发（玩家距离）")
                .defineInRange("playerRange", 16.0, 1.0, 512.0);
        INTER_CHAT_MAID_RANGE = builder.comment("互聊：自身多少格内有另一只女仆时才触发（女仆间距离）")
                .defineInRange("maidRange", 8.0, 1.0, 512.0);
        INTER_CHAT_KEEP_ROUNDS = builder.comment("互聊保留轮数。问/答算一轮，单问无答也算一轮；达到该轮数时触发遗忘，仅保留最近一条消息")
                .defineInRange("keepRounds", 5, 1, 50);
        INTER_CHAT_CHAIN_PROBABILITY = builder.comment("互聊连续触发概率（0~1），每轮回答后按此概率决定是否让对方继续回应")
                .defineInRange("chainProbability", 0.3, 0.0, 1.0);
        INTER_CHAT_MAX_CHAIN_ROUNDS = builder.comment("一次互聊会话的最大消息条数（含发起者消息），达到后强制结束本次互聊。为连续概率配到 1.0 等极端配置兜底，防止无限链式往返消耗 token")
                .defineInRange("maxChainRounds", 10, 1, 50);
        INTER_CHAT_PAIR_LOCK_SECONDS = builder.comment("""
                互聊对锁：A 对 C 发起互聊后，二者在「连续互聊结束」前互相对锁——
                不能发起、也不能被发起互聊（其它女仆的随机候选池不再包含这对）。
                链自然结束/请求失败/女仆死亡卸载时均会提前解锁；该时长仅为
                回调永不返回等无法感知的异常中断时的超时兜底。""")
                .defineInRange("pairLockSeconds", 90, 10, 600);
        builder.pop();

        builder.push("event_context");
        EVENT_CONTEXT_ENABLED = builder.comment("""
                环境感知总开关，默认开启。关闭后附近事件与女仆自身感知均不注入。
                开启后，女仆感知半径内发生的死亡与玩家受伤会被记录，连同女仆自身的着火、
                缺氧与自身受伤，一起整理成自然语言背景，注入该女仆下一次自言自语/互聊的提示词；
                欢迎语不注入。死亡只记录玩家、有主人的动物与其他女仆，普通生物死亡不记录。
                死亡文本取原版本地化消息：专用服务端为英文原文，单人/局域网随客户端语言。
                这些记录只存在于服务端内存与当次请求，不写入世界存档。""")
                .define("enabled", true);
        EVENT_CONTEXT_RANGE = builder.comment("附近死亡与玩家受伤的感知半径（格），不约束女仆自身感知")
                .defineInRange("range", 32.0, 1.0, 512.0);
        EVENT_CONTEXT_MAX_BUFFERED = builder.comment("""
                每只女仆的事件缓冲容量，死亡、玩家受伤与自身受伤共用，溢出丢弃最旧的一条。
                缓冲在下一次自言自语或互聊时被取用并清空（谁先派发谁消费）。""")
                .defineInRange("maxBufferedEvents", 5, 1, 20);
        EVENT_CONTEXT_HURT_ENABLED = builder.comment("""
                玩家受伤事件开关，默认关闭，不影响女仆自身感知。
                受伤发生频率远高于死亡，开启后提示词噪音明显增多；同样受感知半径约束。
                只在玩家实际掉血时记录（格挡成功、无敌帧内被忽略的伤害不记）。
                为控制积压，同一只女仆记录玩家受伤的最小间隔固定为 1 秒（不可配置），
                因此记录条数不等于实际受击次数。""")
                .define("hurtEnabled", false);
        EVENT_CONTEXT_SELF_ENABLED = builder.comment("""
                女仆自身感知开关，默认开启，与玩家受伤开关相互独立。
                包含：自身着火、水下缺氧，以及自身实际掉血（含伤害来源）。
                自身受伤不受感知半径约束，也不需要附近有玩家；只影响该女仆自己下一次
                自言自语/互聊，不会告知附近其它女仆。""")
                .define("selfEnabled", true);
        EVENT_CONTEXT_HURT_MAX_AGE_SECONDS = builder.comment("""
                受伤事件有效期（秒），玩家受伤与女仆自身受伤共用。
                超过该时长的受伤记录在下一次记录或派发时被淘汰，不再注入提示词；
                调小后已淘汰的记录不会因调大而恢复。死亡记录不设有效期。
                为避免积压，同一只女仆记录受伤的最小间隔固定为 1 秒（不可配置），
                短时间内多次受击只会保留其中一部分。""")
                .defineInRange("hurtMaxAgeSeconds", 60, 1, 600);
        builder.pop();

        builder.push("chat_history");
        CHAT_HISTORY_MAX_STORED = builder.comment("""
                自话、欢迎语与女仆互聊的独立展示档案总保留条数（每只女仆各自计算，包含已被手动隐藏的记录）。
                它只控制原版聊天记录界面里这些消息的保留上限，超出后按最旧顺序淘汰；
                不控制模型上下文——自话参与模型请求的条数上限固定为 512，不受此项影响。
                只允许在此配置文件修改，游戏内设置界面不提供该项。""")
                .defineInRange("maxStoredMessages", 1000, 1, 100000);
        builder.pop();

        builder.push("maid_identity");
        MAID_IDENTITY_ENABLED = builder.comment("""
                向「附近实体」上下文补充附近女仆的 UUID 与当前名称，让模型能区分同名女仆、
                识别改名前后的同一对象。仅影响随机的附近实体情境与 query_game_context 的查询结果，
                不改动 system 人设，也不拦截模型输出。
                名称取值：命名牌名称优先，未命名但使用 TLM 内置模型的返回该模型的预置名称
                （如「博丽灵梦」），使用 YSM 或第三方自定义模型的仍视为未命名。
                预置名称跟随发起查询的女仆的聊天语言，取不到该语言的翻译时用英文。
                是否注册此项在启动时读取一次，修改后需重启服务端；
                女仆名称等数据每次查询时实时读取，改名后下一次查询即为新名称，UUID 不变。
                专用服务器需修改服务端配置。默认开启。""")
                .define("enabled", true);
        MAID_IDENTITY_TOOL_ENABLED = builder.comment("""
                注册「查询附近女仆」Tool（query_nearby_maids），供模型自行决定是否调用：
                返回半径固定 32 格内每只女仆的 UUID、当前名称与到调用者的距离。
                名称取值与上面的上下文注入一致：命名牌名称优先，未命名的 TLM 内置模型用预置名称，
                未命名的 YSM / 第三方自定义模型视为未命名；预置名称跟随调用者的聊天语言。
                与上面的上下文注入相互独立——注入是被动地随情境给出，本工具是模型主动取用，
                两者都只降低模型照抄内部标识符的风险，不拦截输出。
                工具一旦注册，玩家聊天与自言自语/互聊的请求都会看到它（是否带工具仍由
                玩家级 Tool 开关决定）。是否注册在启动时读取一次，修改后需重启服务端。
                默认开启。""")
                .define("toolEnabled", true);
        builder.pop();

        builder.push("sleep");
        SLEEP_TALK_ENABLED = builder.comment("""
                梦话（呓语）开关，默认开启。
                女仆熟睡时会按各自随机间隔自发说一句梦话（由 LLM 按人设生成），
                以头顶气泡与聊天栏消息显示，附近玩家可见。
                本开关独立于玩家设置里的「睡觉时安静」：即使开启睡觉安静，梦话仍会触发。
                每次梦话消耗一次 LLM 请求。""")
                .define("enabled", true);
        SLEEP_TALK_MIN_INTERVAL = builder.comment("梦话最小间隔（秒）。同一只熟睡女仆两次梦话之间的最小间隔。")
                .defineInRange("minIntervalSeconds", 180, 30, 7200);
        SLEEP_TALK_MAX_INTERVAL = builder.comment("""
                梦话最大间隔（秒），实际间隔在 [最小, 最大] 内随机。
                两者倒置时按现有间隔配置的写法自动交换，不会出现负区间。""")
                .defineInRange("maxIntervalSeconds", 600, 30, 7200);
        SLEEP_TALK_PLAYER_RANGE = builder.comment("""
                梦话触发半径（格）：仅当该半径内有玩家时才说梦话（节省 Token）。
                0 表示不要求有人在场；此时聊天栏广播半径回落到 16 格。
                与自话的「玩家半径」同口径（半径内存在存活、非旁观玩家）。""")
                .defineInRange("playerRange", 16.0, 0.0, 512.0);
        WAKE_RESISTANCE_ENABLED = builder.comment("""
                叫不醒开关，默认开启。
                玩家对熟睡的女仆说话时，前几次她不会被叫醒，只迷迷糊糊嘟囔一句（不回答内容、不醒来），
                被叫够随机次数后才醒来并正常回复；醒来后的第一次回复会带上刚起床的状态提示。
                每次嘟囔消耗一次 LLM 请求；LLM 不可用时静默不回复（仍然计数）。""")
                .define("wakeResistanceEnabled", true);
        WAKE_MIN_CALLS = builder.comment("""
                一轮连叫中需要叫醒她的最少次数（含）。
                每次睡眠中的连叫开始时在 [最小, 最大] 内随机取整数 N：第 1..N-1 次只嘟囔，第 N 次醒来。
                1 表示第一次就叫醒。""")
                .defineInRange("wakeMinCalls", 2, 1, 20);
        WAKE_MAX_CALLS = builder.comment("""
                一轮连叫中需要叫醒她的最多次数（含）。
                与最小次数倒置时自动交换，不会出现空区间。""")
                .defineInRange("wakeMaxCalls", 5, 1, 20);
        WAKE_STREAK_RESET_SECONDS = builder.comment("""
                连叫计数清零时长（秒）：距上一次叫她超过该时长，连叫计数清零，
                下一次叫她视为新一轮并重新随机 N。""")
                .defineInRange("streakResetSeconds", 120, 1, 3600);
        WAKE_STAY_AWAKE_SECONDS = builder.comment("""
                被叫醒后保持清醒的时长（秒）：这段时间内她不会再走向床睡觉，
                避免夜间日程下刚被叫醒就立刻睡回去。0 表示不做处理。""")
                .defineInRange("stayAwakeSeconds", 120, 0, 3600);
        DREAM_ENABLED = builder.comment("""
                做梦开关，默认开启。
                女仆睡着时按下面的概率抽一次：这一觉有没有梦（不是每个晚上都有梦）。
                有梦的话，熟睡约 30 秒后会由 LLM 按人设、结合她和玩家之间已有的对话，编出这一觉做的梦
                （梦只存在内存里，不显示，服务器重启后清零）。每一觉最多一次请求；失败则这一觉没有梦。
                梦话取材于它、醒来后讲给主人，都是下面的独立开关；关闭本项则全部不生效。
                与梦话一样，仅当 sleep.playerRange 半径内有玩家时才生成（0 = 不要求有人在场）。""")
                .define("dreamEnabled", true);
        DREAM_CHANCE_PERCENT = builder.comment("""
                每次睡着做梦的概率（百分比，0~100），默认 20。
                每只女仆每次睡着时各抽一次：抽中才有这一觉的梦，没抽中就是安稳睡一觉
                （这一觉不生成梦、不讲梦；梦话照旧是泛泛的呓语）。0 表示永远不做梦，100 表示每觉都做梦。""")
                .defineInRange("dreamChancePercent", 20, 0, 100);
        DREAM_TALK_REFERENCES = builder.comment("""
                梦话取材于这一觉的梦，默认开启。
                有梦的夜晚里，她说的梦话都和梦有关：从梦里取一个片段说一句（梦生成出来之前先不说，
                最多等 3 分钟）；没有梦的夜晚照旧说泛泛的呓语。""")
                .define("dreamTalkReferencesDream", true);
        DREAM_REPORT_ENABLED = builder.comment("""
                醒来后把梦讲给主人，默认开启。
                自己睡醒的女仆会在主人走近（16 格内）时，用一两句话说起这个梦（每个梦最多说一次，
                多一次 LLM 请求，以头顶气泡和聊天栏显示）；被玩家叫醒的则把梦附在起床提示里，
                让她回答时可以顺口提一句，不另外发请求。""")
                .define("dreamReportEnabled", true);
        DREAM_REPORT_WINDOW_SECONDS = builder.comment("""
                自然醒来后，主人在多少秒内走近才会听到她讲梦（超过就不说了）。""")
                .defineInRange("dreamReportWindowSeconds", 300, 30, 3600);
        DREAM_WRITE_HISTORY = builder.comment("""
                她讲梦的那句话是否写进主模组的聊天历史，默认关闭。
                关闭时这句话只用于显示，不进入历史，不影响之后的对话与记忆整理；
                开启后她会在之后的对话里「记得」自己说过这个梦。梦本身从不写入历史。""")
                .define("dreamWriteHistory", false);
        builder.pop();

        SPEC = builder.build();
    }

    private Config() {
    }

    /**
     * 秒区间随机转 tick：min/max 倒置时自动交换（配置项无跨字段校验，
     * 管理员手改 toml 可能写出 min > max，交换后语义明确且区间恒正）。
     */
    public static int randomIntervalTicks(int minSeconds, int maxSeconds) {
        if (maxSeconds < minSeconds) {
            int temp = minSeconds;
            minSeconds = maxSeconds;
            maxSeconds = temp;
        }
        int minTicks = minSeconds * 20;
        int maxTicks = maxSeconds * 20;
        return minTicks + (int) (Math.random() * (maxTicks - minTicks + 1));
    }
}
