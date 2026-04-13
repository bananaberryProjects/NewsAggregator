package com.newsaggregator.infrastructure.adapter.persistence.repository;

import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleReadStatusRepository extends JpaRepository<ArticleReadStatus, Long> {

    Optional<ArticleReadStatus> findByArticleIdAndUserId(String articleId, String userId);

    List<ArticleReadStatus> findByUserId(String userId);

    List<ArticleReadStatus> findByUserIdAndIsRead(String userId, boolean isRead);

    List<ArticleReadStatus> findByUserIdAndIsFavorite(String userId, boolean isFavorite);

    boolean existsByArticleIdAndUserId(String articleId, String userId);
}
