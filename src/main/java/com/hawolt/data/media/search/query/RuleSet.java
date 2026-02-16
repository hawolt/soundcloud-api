package com.hawolt.data.media.search.query;

import com.hawolt.data.media.hydratable.impl.Track;

import java.util.function.Predicate;



public interface RuleSet {

    Predicate<Track> getMaximumTagThresholdPredicate();

    Predicate<Track> getBlacklistedUserPredicate();

    Predicate<Track> getBlacklistedTagsPredicate();

    Predicate<Track> getAuthenticUserPredicate();

    Predicate<Track> getMandatoryTagsPredicate();

    Predicate<Track> getTimestampPredicate();

    Predicate<Track> getDurationPredicate();

    Predicate<Track> getStreamPredicate();

    Predicate<Track> getLikePredicate();
}
