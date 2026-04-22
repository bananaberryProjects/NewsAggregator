import { useState, useEffect, useCallback, useRef } from 'react'
import type { Article } from '../api/client'

interface UseInfiniteArticlesOptions {
  articles: Article[]
  batchSize?: number
  initialCount?: number
}

export function useInfiniteArticles({
  articles,
  batchSize = 9,
  initialCount = 18
}: UseInfiniteArticlesOptions) {
  const [displayCount, setDisplayCount] = useState(initialCount)
  const [hasMore, setHasMore] = useState(true)
  const observerRef = useRef<IntersectionObserver | null>(null)
  const loadMoreRef = useRef<HTMLDivElement>(null)

  // Reset display count when articles change (filter changes)
  useEffect(() => {
    setDisplayCount(initialCount)
    setHasMore(articles.length > initialCount)
  }, [articles.length, initialCount])

  const displayedArticles = articles.slice(0, displayCount)

  const loadMore = useCallback(() => {
    setDisplayCount((prev) => {
      const newCount = prev + batchSize
      if (newCount >= articles.length) {
        setHasMore(false)
        return articles.length
      }
      return newCount
    })
  }, [articles.length, batchSize])

  // Intersection Observer setup
  useEffect(() => {
    if (!hasMore) return

    const observer = new IntersectionObserver(
      (entries) => {
        const [entry] = entries
        if (entry.isIntersecting && hasMore) {
          loadMore()
        }
      },
      {
        root: null,
        rootMargin: '200px', // Load when 200px before the end
        threshold: 0.1
      }
    )

    observerRef.current = observer

    const currentRef = loadMoreRef.current
    if (currentRef) {
      observer.observe(currentRef)
    }

    return () => {
      if (currentRef && observerRef.current) {
        observerRef.current.unobserve(currentRef)
      }
    }
  }, [loadMore, hasMore])

  return {
    displayedArticles,
    hasMore,
    loadMoreRef,
    totalCount: articles.length
  }
}

export default useInfiniteArticles
