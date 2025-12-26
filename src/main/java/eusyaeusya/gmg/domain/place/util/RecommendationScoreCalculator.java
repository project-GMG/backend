package eusyaeusya.gmg.domain.place.util;

import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.place.entity.Place;

public class RecommendationScoreCalculator {
    private static final double RATING_WEIGHT = 10.0;
    private static final double DISLIKE_PENALTY = 5.0;
    private static final double MATCHING_DAYS_BONUS = 3.0;

    public static double calculateScore(Place place, Event event, int dislikeCount) {
        double baseScore = calculateRatingScore(place);
        double dislikePenalty = calculateDislikePenalty(dislikeCount);
        double matchingBonus = calculateMatchingDaysBonus(place, event);

        return baseScore - dislikePenalty + matchingBonus;
    }

    private static double calculateRatingScore(Place place) {
        double rating = place.getRating().doubleValue();
        return rating * RATING_WEIGHT;
    }

    private static double calculateDislikePenalty(int dislikeCount) {
        return dislikeCount * DISLIKE_PENALTY;
    }

    private static double calculateMatchingDaysBonus(Place place, Event event) {
        int matchingDays = countMatchingDays(place, event);
        int totalDays = event.getTotalDays(); // Event에게 위임

        if (totalDays == 0) {
            return 0.0;
        }

        double matchingRate = (double) matchingDays / totalDays;
        return matchingRate * MATCHING_DAYS_BONUS;
    }

    public static int countMatchingDays(Place place, Event event) {
        return event.countDaysMatching(place::isOpenOn);
    }
}
