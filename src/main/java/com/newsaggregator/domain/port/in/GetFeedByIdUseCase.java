package com.newsaggregator.domain.port.in;

import com.newsaggregator.domain.model.Feed;

/**
 * Use Case: Feed nach ID abrufen.
 */
public interface GetFeedByIdUseCase {
    Feed getFeedById(Long id);
}
