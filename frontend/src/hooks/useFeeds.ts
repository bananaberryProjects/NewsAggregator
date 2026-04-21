import { useState, useCallback } from 'react'
import { feedsApi, feedCategoriesApi, type Feed } from '../api/client'

export function useFeeds() {
  const [feeds, setFeeds] = useState<Feed[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [refreshingFeedId, setRefreshingFeedId] = useState<string | null>(null)

  const loadFeeds = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)
      const feedsData = await feedsApi.getAll()
      setFeeds(feedsData)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unbekannter Fehler')
    } finally {
      setLoading(false)
    }
  }, [])

  const addFeed = async (url: string, name: string, categoryIds: string[]) => {
    const newFeed = await feedsApi.add({ 
      name: name.trim() || undefined as any, 
      url: url.trim() 
    })
    
    if (categoryIds.length > 0) {
      await feedCategoriesApi.assign(newFeed.id, categoryIds)
    }
    
    await loadFeeds()
    return newFeed
  }

  const updateFeed = async (feedId: string, name: string, url: string, description: string, extractContent?: boolean) => {
    await feedsApi.update(feedId, { 
      name: name.trim(), 
      url: url.trim(),
      description: description.trim() || undefined,
      extractContent
    })
    await loadFeeds()
  }

  const deleteFeed = async (feedId: string) => {
    await feedsApi.delete(feedId)
    await loadFeeds()
  }

  const refreshFeed = async (feed: Feed) => {
    try {
      setRefreshingFeedId(feed.id)
      await feedsApi.fetchArticles(feed.id)
      await loadFeeds()
    } finally {
      setRefreshingFeedId(null)
    }
  }

  const assignCategories = async (feedId: string, categoryIds: string[]) => {
    await feedCategoriesApi.assign(feedId, categoryIds)
    await loadFeeds()
  }

  return {
    feeds,
    loading,
    error,
    refreshingFeedId,
    loadFeeds,
    addFeed,
    deleteFeed,
    refreshFeed,
    assignCategories,
    setError,
    updateFeed,
  }
}
