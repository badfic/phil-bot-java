package com.badfic.philbot.listeners.antonia;

import com.badfic.philbot.config.Constants;
import com.badfic.philbot.listeners.phil.swampy.NonCommandSwampy;
import com.badfic.philbot.listeners.phil.swampy.PointsStat;
import com.google.common.collect.ImmutableList;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Service;

@Service
public class GrinchCommand extends NonCommandSwampy {
    private static final List<Long> POINTS_TO_TAKE = ImmutableList.of(0L, 0L, 0L, 0L, 100L, 500L, 1_000L);

    public long takePoints(Member member) {
        long points = Constants.pickRandom(POINTS_TO_TAKE);
        takePointsFromMember(points, member, PointsStat.NO_NO);
        return points;
    }

}
