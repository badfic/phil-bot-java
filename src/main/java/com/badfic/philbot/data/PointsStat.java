package com.badfic.philbot.data;

import java.util.function.BiConsumer;
import java.util.function.ToLongFunction;

public enum PointsStat {
    MOD_GIVE(DiscordUser::getModGavePoints, DiscordUser::setModGavePoints),
    MOD_TAKE(DiscordUser::getModTookPoints, DiscordUser::setModTookPoints),
    UPVOTED(DiscordUser::getUpvotedPoints, DiscordUser::setUpvotedPoints),
    UPVOTER(DiscordUser::getUpvoterPoints, DiscordUser::setUpvoterPoints),
    DOWNVOTED(DiscordUser::getDownvotedPoints, DiscordUser::setDownvotedPoints),
    DOWNVOTER(DiscordUser::getDownvoterPoints, DiscordUser::setDownvoterPoints),
    SLOTS_CLOSE_ENOUGH(DiscordUser::getSlotsCloseEnoughPoints, DiscordUser::setSlotsCloseEnoughPoints),
    SLOTS_WINNER_WINNER(DiscordUser::getSlotsWinnerWinnerPoints, DiscordUser::setSlotsWinnerWinnerPoints),
    SLOTS_LOSSES(DiscordUser::getSlotsLosses, DiscordUser::setSlotsLosses),
    MAP(DiscordUser::getMapsPoints, DiscordUser::setMapsPoints),
    TRIVIA(DiscordUser::getTriviaPoints, DiscordUser::setTriviaPoints),
    QUOTE_TRIVIA(DiscordUser::getQuoteTriviaPoints, DiscordUser::setQuoteTriviaPoints),
    TRICK_OR_TREAT(DiscordUser::getTrickOrTreatPoints, DiscordUser::setTrickOrTreatPoints),
    NO_NO(DiscordUser::getNoNoPoints, DiscordUser::setNoNoPoints),
    MESSAGE(DiscordUser::getMessagePoints, DiscordUser::setMessagePoints),
    PICTURE_MESSAGE(DiscordUser::getPictureMessagePoints, DiscordUser::setPictureMessagePoints),
    BOOST(DiscordUser::getBoostPoints, DiscordUser::setBoostPoints),
    VOICE_CHAT(DiscordUser::getVoiceChatPoints, DiscordUser::setVoiceChatPoints),
    REACTOR_POINTS(DiscordUser::getReactPoints, DiscordUser::setReactPoints),
    REACTED_POINTS(DiscordUser::getReactedPoints, DiscordUser::setReactedPoints),
    FIGHT(DiscordUser::getFightPoints, DiscordUser::setFightPoints);

    private final ToLongFunction<DiscordUser> getter;
    private final BiConsumer<DiscordUser, Long> setter;

    PointsStat(final ToLongFunction<DiscordUser> getter, final BiConsumer<DiscordUser, Long> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public ToLongFunction<DiscordUser> getter() {
        return getter;
    }

    public BiConsumer<DiscordUser, Long> setter() {
        return setter;
    }
}
