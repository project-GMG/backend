package eusyaeusya.gmg.domain.place.util;

import eusyaeusya.gmg.domain.event.entity.Event;
import eusyaeusya.gmg.domain.place.entity.Place;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

public class RecommendationScoreCalculator {
    private static final double RATING_WEIGHT = 10.0;
    private static final double DISLIKE_PENALTY = 5.0;
    private static final double MATCHING_BONUS = 3.0;
    private static final int TIME_SLOT_INTERVAL_MINUTES = 30;

    public static double calculateScore(
            Place place,
            Event event,
            int categoryDislikeCount,
            double weightedMatchingRate
    ) {
        double baseScore = calculateRatingScore(place);
        double dislikePenalty = calculateDislikePenalty(categoryDislikeCount);
        double matchingBonus = calculateMatchingBonus(weightedMatchingRate);
        return baseScore - dislikePenalty + matchingBonus;
    }

    private static double calculateRatingScore(Place place) {
        double rating = place.getRating().doubleValue();
        return rating * RATING_WEIGHT;
    }

    private static double calculateDislikePenalty(int dislikeCount) {
        return dislikeCount * DISLIKE_PENALTY;
    }

    private static double calculateMatchingBonus(double weightedMatchingRate) {
        return weightedMatchingRate * MATCHING_BONUS;
    }

    public static double calculateWeightedMatchingRate(
            Place place,
            Event event,
            Map<LocalDate, Map<LocalTime, Double>> intensityMap
    ) {
        double totalIntensity = 0.0;
        int totalEventSlots = 0;

        for (LocalDate currentDate : event.getSelectedDates()) {
            // 전체 이벤트 슬롯 수 계산
            LocalTime slotTime = event.getTimeStart();
            while (slotTime.isBefore(event.getTimeEnd())) {
                totalEventSlots++;
                slotTime = slotTime.plusMinutes(TIME_SLOT_INTERVAL_MINUTES);
            }

            // 장소 영업시간과 이벤트 시간의 교집합 구간
            OpeningHours.TimeRange overlap = place.getOpeningHours()
                    .getOverlapRange(currentDate.getDayOfWeek(), event.getTimeStart(), event.getTimeEnd());

            if (overlap != null) {
                LocalTime overlapSlot = overlap.start();
                while (overlapSlot.isBefore(overlap.end())) {
                    Double intensity = intensityMap
                            .getOrDefault(currentDate, Map.of())
                            .getOrDefault(overlapSlot, 0.0);
                    totalIntensity += intensity;
                    overlapSlot = overlapSlot.plusMinutes(TIME_SLOT_INTERVAL_MINUTES);
                }
            }
        }

        return totalEventSlots == 0 ? 0.0 : totalIntensity / totalEventSlots;
    }

    public static double calculateFallbackMatchingRate(int matchingDays, int totalDays) {
        if (totalDays == 0) {
            return 0.0;
        }
        return (double) matchingDays / totalDays;
    }

    public static int countMatchingDays(Place place, Event event) {
        return event.countDaysMatching(place::isOpenOn);
    }
}
