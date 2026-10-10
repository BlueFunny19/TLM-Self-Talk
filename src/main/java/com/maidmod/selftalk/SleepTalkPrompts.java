package com.maidmod.selftalk;

/**
 * 睡眠相关的三段提示词（梦话 / 被叫时迷糊嘟囔 / 被叫醒后的反应），中英各一套。
 * <p>
 * 调用方按女仆当前对话语言是否中文（{@code SelfTalkContexts.isChinese}）选择 {@code zh}；
 * 语言既不是中文也不是英文时用英文版，并照现有惯例在末尾追加语言指令
 * （{@code SelfTalkContexts.languageInstruction}）。
 * <p>
 * 提示词集中在这里统一维护，业务代码只通过这些方法取文字，不要在别处另拼。
 */
public final class SleepTalkPrompts {
    private SleepTalkPrompts() {
    }

    /** 女仆熟睡时自发说梦话（呓语）的指令：只输出一句梦话。 */
    public static String dreamTalk(boolean zh) {
        return zh ? """
                你现在正在熟睡，说的是梦话。
                请结合你的人设，说一句简短的梦话或含糊的呓语（不超过 20 个字）：可以是梦见的事、白天放不下的牵挂、对主人的嘟囔，也可以只是"嗯……""呼……"之类含糊的声音。
                要求：
                - 你是在说梦话，没有醒来，也不是在和谁对话；不要提到自己在做梦或在说梦话。
                - 只输出这一句梦话本身，不要加引号、动作描写或任何解释。
                """.trim() : """
                You are fast asleep and talking in your sleep.
                Based on your persona, say one short line of sleep-talk or a muddled murmur (no more than 20 words): something you are dreaming about, a worry from the day, a mumble about your owner, or just a vague sound like "mm..." or "zzz...".
                Requirements:
                - You are talking in your sleep: you have not woken up and you are not talking to anyone; do not mention dreaming or sleep-talking.
                - Output only that one line itself, without quotation marks, action descriptions or any explanation.
                """.trim();
    }

    /** 玩家在叫熟睡的女仆、但她还没被叫醒时：只用一两句含混的话回应，不回答玩家的内容。 */
    public static String mumbleReply(boolean zh) {
        return zh ? """
                你正在熟睡，玩家刚才在叫你，但你没有被叫醒。
                请只用一两句含混的梦话或迷迷糊糊的嘟囔回应（不超过 20 个字）：不要真正回答玩家说的内容，也不要醒来；可以把玩家的声音混进梦里，也可以只是翻个身哼哼两声。保持符合你的人设。
                只输出这一两句话本身，不要加引号、动作描写或任何解释。
                """.trim() : """
                You are fast asleep. The player just called you, but you have not woken up.
                Reply with only one or two muddled sleep-talk lines or a drowsy mumble (no more than 20 words): do not really answer what the player said and do not wake up; you may blend the player's voice into your dream, or just turn over and hum. Stay true to your persona.
                Output only those one or two lines themselves, without quotation marks, action descriptions or any explanation.
                """.trim();
    }

    /** 被反复叫醒后的第一次正常回复前，附在玩家这句话上的提示，让她按性格自然地"起床"。 */
    public static String wakeUpNote(boolean zh) {
        return zh
                ? "（你刚才在熟睡，被玩家反复叫了好几次才被吵醒。请按你的性格自然地反应：迷糊、撒娇、有起床气，还是温柔，都由你的人设决定；先用一两句话表现出刚醒来的状态，再回应玩家说的话。回应时不要提到这条提示。）"
                : "(You were fast asleep and were only woken after the player called you several times. React naturally according to your personality: groggy, clingy, grumpy or gentle is up to your persona; first show in a sentence or two that you have just woken up, then respond to what the player said. Do not mention this note in your reply.)";
    }
}
