package com.badfic.philbot.listeners.phil.swampy;

import com.badfic.philbot.data.DiscordUser;
import java.util.function.BiConsumer;
import java.util.function.ToLongFunction;

public enum PointsStat {
    MOD_GIVE(DiscordUser::getModGavePoints, DiscordUser::setModGavePoints),
    MOD_TAKE(DiscordUser::getModTookPoints, DiscordUser::setModTookPoints),
    UPVOTED(DiscordUser::getUpvotedPoints, DiscordUser::setUpvotedPoints),
    UPVOTER(DiscordUser::getUpvoterPoints, DiscordUser::setUpvoterPoints),
    DOWNVOTED(DiscordUser::getDownvotedPoints, DiscordUser::setDownvotedPoints),
    DOWNVOTER(DiscordUser::getDownvoterPoints, DiscordUser::setDownvoterPoints),
    SWIPER(DiscordUser::getSwiperPoints, DiscordUser::setSwiperPoints),
    SWIPER_PARTICIPATIONS(DiscordUser::getSwiperParticipations, DiscordUser::setSwiperParticipations),
    SLOTS_CLOSE_ENOUGH(DiscordUser::getSlotsCloseEnoughPoints, DiscordUser::setSlotsCloseEnoughPoints),
    SLOTS_WINNER_WINNER(DiscordUser::getSlotsWinnerWinnerPoints, DiscordUser::setSlotsWinnerWinnerPoints),
    SLOTS_LOSSES(DiscordUser::getSlotsLosses, DiscordUser::setSlotsLosses),
    MAP(DiscordUser::getMapsPoints, DiscordUser::setMapsPoints),
    TRIVIA(DiscordUser::getTriviaPoints, DiscordUser::setTriviaPoints),
    TRICK_OR_TREAT(DiscordUser::getTrickOrTreatPoints, DiscordUser::setTrickOrTreatPoints),
    SCOOTER(DiscordUser::getScooterPoints, DiscordUser::setScooterPoints),
    SCOOTER_PARTICIPANT(DiscordUser::getScooterParticipant, DiscordUser::setScooterParticipant),
    SWEEPSTAKES(DiscordUser::getSweepstakesPoints, DiscordUser::setSweepstakesPoints),
    NO_NO(DiscordUser::getNoNoPoints, DiscordUser::setNoNoPoints),
    MESSAGE(DiscordUser::getMessagePoints, DiscordUser::setMessagePoints),
    PICTURE_MESSAGE(DiscordUser::getPictureMessagePoints, DiscordUser::setPictureMessagePoints),
    BOOST(DiscordUser::getBoostPoints, DiscordUser::setBoostPoints),
    VOICE_CHAT(DiscordUser::getVoiceChatPoints, DiscordUser::setVoiceChatPoints),
    REACTOR_POINTS(DiscordUser::getReactPoints, DiscordUser::setReactPoints),
    REACTED_POINTS(DiscordUser::getReactedPoints, DiscordUser::setReactedPoints),
    SHREKONING(DiscordUser::getShrekoningPoints, DiscordUser::setShrekoningPoints),
    STONKS(DiscordUser::getStonksPoints, DiscordUser::setStonksPoints),
    TAXES(DiscordUser::getTaxesPoints, DiscordUser::setTaxesPoints),
    ROBINHOOD(DiscordUser::getRobinhoodPoints, DiscordUser::setRobinhoodPoints),
    FIGHT(DiscordUser::getFightPoints, DiscordUser::setFightPoints);

    private final ToLongFunction<DiscordUser> getter;
    private final BiConsumer<DiscordUser, Long> setter;

    PointsStat(ToLongFunction<DiscordUser> getter, BiConsumer<DiscordUser, Long> setter) {
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
