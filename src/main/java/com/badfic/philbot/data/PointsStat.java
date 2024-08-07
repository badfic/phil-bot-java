package com.badfic.philbot.data;

import java.util.function.BiConsumer;
import java.util.function.ToLongFunction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
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

    public final ToLongFunction<DiscordUser> getter;
    public final BiConsumer<DiscordUser, Long> setter;
}
