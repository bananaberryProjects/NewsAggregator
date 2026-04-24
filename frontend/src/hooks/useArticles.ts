import { useState, useCallback } from 'react'
import { articlesApi, type Article } from '../api/client'

export function useArticles() {
  const [articles, setArticles] = useState<Article[]>([])
  const [articleStatuses, setArticleStatuses] = useState<Record<number, { isRead?: boolean; isFavorite?: boolean }>>({})
  const [updatingArticleId, setUpdatingArticleId] = useState<number | null>(null)

  const loadArticles = useCallback(async () => {
    const [articlesData, readStatuses, favoriteStatuses] = await Promise.all([
      articlesApi.getAll(),
      articlesApi.getReadArticles().catch(() => []),
      articlesApi.getFavoriteArticles().catch(() => []),
    ])
    
    setArticles(articlesData)
    
    const statusMap: Record<number, { isRead?: boolean; isFavorite?: boolean }> = {}
    readStatuses.forEach((status) => {
      statusMap[status.articleId] = { ...statusMap[status.articleId], isRead: true }
    })
    favoriteStatuses.forEach((status) => {
      statusMap[status.articleId] = { ...statusMap[status.articleId], isFavorite: true }
    })
    setArticleStatuses(statusMap)
  }, [])

  const toggleRead = async (articleId: number) => {
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
        [articleId]: { ...prev[articleId], isRead: !isCurrentlyRead }
      }))
    } finally {
      setUpdatingArticleId(null)
    }
  }

  const toggleFavorite = async (articleId: number) => {
    setUpdatingArticleId(articleId)
    try {
      const isCurrentlyFavorite = articleStatuses[articleId]?.isFavorite
      await articlesApi.toggleFavorite(articleId)
      setArticleStatuses(prev => ({
        ...prev,
        [articleId]: { ...prev[articleId], isFavorite: !isCurrentlyFavorite }
      }))
    } finally {
      setUpdatingArticleId(null)
    }
  }

  const isRead = (articleId: number) => articleStatuses[articleId]?.isRead ?? false
  const isFavorite = (articleId: number) => articleStatuses[articleId]?.isFavorite ?? false

  const getFilteredArticles = useCallback((filter: string, searchQuery?: string) => {
    let result = articles

    if (searchQuery && searchQuery.trim()) {
      const query = searchQuery.toLowerCase()
      result = result.filter(a =>
        a.title.toLowerCase().includes(query) ||
        (a.description ?? '').toLowerCase().includes(query) ||
        a.feedName.toLowerCase().includes(query)
      )
    }

    if (filter === 'unread') {
      return result.filter(a => !articleStatuses[a.id]?.isRead)
    } else if (filter === 'favorites') {
      return result.filter(a => articleStatuses[a.id]?.isFavorite)
    }

    return result
  }, [articles, articleStatuses])

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
