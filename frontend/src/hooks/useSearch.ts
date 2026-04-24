import { useState, useCallback } from 'react'
import { articlesApi, Page, Article } from '../api/client'

interface UseSearchResult {
  results: Article[] | null
  loading: boolean
  query: string
  pageData: Page<Article> | null
  hasMore: boolean
  search: (q: string) => Promise<void>
  nextPage: () => Promise<void>
  reset: () => void
}

export function useSearch(): UseSearchResult {
  const [results, setResults] = useState<Article[] | null>(null)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState('')
  const [pageData, setPageData] = useState<Page<Article> | null>(null)

  const search = useCallback(async (q: string) => {
    if (!q.trim()) {
      reset()
      return
    }
    setLoading(true)
    setQuery(q)
    try {
      const data = await articlesApi.searchFullText(q.trim(), 0, 20)
      setPageData(data)
      setResults(data.content)
    } catch (err) {
      console.error('Search failed:', err)
      setResults([])
      setPageData(null)
    } finally {
      setLoading(false)
    }
  }, [])

  const nextPage = useCallback(async () => {
    if (!pageData || pageData.last || loading || !query) return
    setLoading(true)
    try {
      const data = await articlesApi.searchFullText(query, pageData.number + 1, 20)
      setPageData(data)
      setResults(prev => [...(prev || []), ...data.content])
    } catch (err) {
      console.error('Next page failed:', err)
    } finally {
      setLoading(false)
    }
  }, [pageData, loading, query])

  const reset = useCallback(() => {
    setResults(null)
    setPageData(null)
    setQuery('')
  }, [])

  return {
    results,
    loading,
    query,
    pageData,
    hasMore: pageData !== null && !pageData.last && pageData.totalPages > pageData.number + 1,
    search,
    nextPage,
    reset,
  }
}
