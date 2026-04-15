import { useState, useCallback } from 'react'
import { articlesApi, type Article } from '../api/client'

export function useArticles() {
  const [articles, setArticles] = useState<Article[]>([])
  const [articleStatuses, setArticleStatuses] = useState<Record<string, { isRead?: boolean; isFavorite?: boolean }>>({})
  const [updatingArticleId, setUpdatingArticleId] = useState<string | null>(null)

  const loadArticles = useCallback(async () => {
    const [articlesData, readStatuses, favoriteStatuses] = await Promise.all([
      articlesApi.getAll(),
      articlesApi.getReadArticles().catch(() => []),
      articlesApi.getFavoriteArticles().catch(() => []),
    ])
    
    setArticles(articlesData)
    
    const statusMap: Record<string, { isRead?: boolean; isFavorite?: boolean }> = {}
    readStatuses.forEach((status) => {
      statusMap[status.articleId] = { ...statusMap[status.articleId], isRead: true }
    })
    favoriteStatuses.forEach((status) => {
      statusMap[status.articleId] = { ...statusMap[status.articleId], isFavorite: true }
    })
    setArticleStatuses(statusMap)
  }, [])

  const toggleRead = async (articleId: string) => {
    setUpdatingArticleId(articleId)
    try {
      const isCurrentlyRead = articleStatuses[articleId]?.isRead
      if (isCurrentlyRead) {
        await articlesApi.markAsUnread(articleId)
      } else {
        await articlesApi.markAsRead(articleId)
      }
      setArticleStatuses(prev => ({
        ...prev,
        [articleId]: {
          ...prev[articleId],
          isRead: !prev[articleId]?.isRead
        }
      }))
    } catch (err) {
      console.error('Error toggling read status:', err)
    } finally {
      setUpdatingArticleId(null)
    }
  }

  const toggleFavorite = async (articleId: string) => {
    setUpdatingArticleId(articleId)
    try {
      await articlesApi.toggleFavorite(articleId)
      setArticleStatuses(prev => ({
        ...prev,
        [articleId]: {
          ...prev[articleId],
          isFavorite: !prev[articleId]?.isFavorite
        }
      }))
    } catch (err) {
      console.error('Error toggling favorite:', err)
    } finally {
      setUpdatingArticleId(null)
    }
  }

  const isRead = (articleId: string) => articleStatuses[articleId]?.isRead ?? false
  const isFavorite = (articleId: string) => articleStatuses[articleId]?.isFavorite ?? false

  const getFilteredArticles = (
    articlesList: Article[],
    filter: 'all' | 'unread' | 'favorites',
    categoryFilter?: string[]
  ) => {
    let result = articlesList

    if (categoryFilter && categoryFilter.length > 0) {
      result = result.filter(a => 
        a.categoryIds?.some(catId => categoryFilter.includes(catId))
      )
    }

    switch (filter) {
      case 'unread':
        return result.filter(a => !articleStatuses[a.id]?.isRead)
      case 'favorites':
        return result.filter(a => articleStatuses[a.id]?.isFavorite)
      default:
        return result
    }
  }

  return {
    articles,
    articleStatuses,
    updatingArticleId,
    loadArticles,
    toggleRead,
    toggleFavorite,
    isRead,
    isFavorite,
    getFilteredArticles,
  }
}
