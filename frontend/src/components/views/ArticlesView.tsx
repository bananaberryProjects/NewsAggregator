import { Box, Card, Chip, Grid, Skeleton, Typography, Alert } from '@mui/material'
import { useEffect } from 'react'
import { ArticleCard } from '../ArticleCard'
import type { Article, Category } from '../../api/client'

interface ArticlesViewProps {
  articles: Article[]
  categories: Category[]
  loading: boolean
  articleStatuses: Record<string, { isRead?: boolean; isFavorite?: boolean }>
  updatingArticleId: string | null
  articlesFilter: 'all' | 'unread' | 'favorites'
  articlesCategoryFilter: string[]
  onFilterChange: (filter: 'all' | 'unread' | 'favorites') => void
  onCategoryFilterChange: (categories: string[]) => void
  onToggleRead: (articleId: string) => void
  onToggleFavorite: (articleId: string) => void
}

export function ArticlesView({
  articles,
  categories,
  loading,
  articleStatuses,
  updatingArticleId,
  articlesFilter,
  articlesCategoryFilter,
  onFilterChange,
  onCategoryFilterChange,
  onToggleRead,
  onToggleFavorite,
}: ArticlesViewProps) {
  // Persist filter to localStorage on changes
  useEffect(() => {
    localStorage.setItem('articles-filter', articlesFilter)
  }, [articlesFilter])

  // Load category filter from localStorage on mount
  useEffect(() => {
    const saved = localStorage.getItem('articles-categories')
    if (saved) {
      try {
        const parsed = JSON.parse(saved) as string[]
        if (parsed.length > 0) {
          onCategoryFilterChange(parsed)
        }
      } catch {
        // Invalid JSON, ignore
      }
    }
  }, [])

  // Persist category filter to localStorage on changes
  useEffect(() => {
    localStorage.setItem('articles-categories', JSON.stringify(articlesCategoryFilter))
  }, [articlesCategoryFilter])

  const getFilteredArticles = () => {
    let result = [...articles]

    if (articlesCategoryFilter.length > 0) {
      result = result.filter(a =>
        a.categoryIds?.some(catId => articlesCategoryFilter.includes(catId))
      )
    }

    switch (articlesFilter) {
      case 'unread':
        return result.filter(a => !articleStatuses[a.id]?.isRead)
      case 'favorites':
        return result.filter(a => articleStatuses[a.id]?.isFavorite)
      default:
        return result
    }
  }

  const articlesList = getFilteredArticles()

  const isRead = (id: string) => articleStatuses[id]?.isRead ?? false
  const isFavorite = (id: string) => articleStatuses[id]?.isFavorite ?? false

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 600 }}>
          Alle Artikel ({articlesList.length})
        </Typography>

        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 1 }}>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, justifyContent: 'flex-end' }}>
            <Chip
              label="Alle"
              onClick={() => onFilterChange('all')}
              color={articlesFilter === 'all' ? 'primary' : 'default'}
              variant={articlesFilter === 'all' ? 'filled' : 'outlined'}
              size="small"
            />
            <Chip
              label="Ungelesen"
              onClick={() => onFilterChange('unread')}
              color={articlesFilter === 'unread' ? 'primary' : 'default'}
              variant={articlesFilter === 'unread' ? 'filled' : 'outlined'}
              size="small"
            />
            <Chip
              label="Favoriten"
              onClick={() => onFilterChange('favorites')}
              color={articlesFilter === 'favorites' ? 'primary' : 'default'}
              variant={articlesFilter === 'favorites' ? 'filled' : 'outlined'}
              size="small"
            />
          </Box>

          {categories.length > 0 && (
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, justifyContent: 'flex-end' }}>
              <Chip
                label="Alle"
                onClick={() => onCategoryFilterChange([])}
                color={articlesCategoryFilter.length === 0 ? 'primary' : 'default'}
                variant={articlesCategoryFilter.length === 0 ? 'filled' : 'outlined'}
                size="small"
              />
              {categories.map((category) => (
                <Chip
                  key={category.id}
                  label={category.name}
                  onClick={() => {
                    onCategoryFilterChange(
                      articlesCategoryFilter.includes(category.id)
                        ? articlesCategoryFilter.filter(id => id !== category.id)
                        : [...articlesCategoryFilter, category.id]
                    )
                  }}
                  sx={{
                    backgroundColor: articlesCategoryFilter.includes(category.id) ? category.color : 'transparent',
                    color: articlesCategoryFilter.includes(category.id) ? '#fff' : 'inherit',
                    borderColor: category.color,
                  }}
                  variant={articlesCategoryFilter.includes(category.id) ? 'filled' : 'outlined'}
                  size="small"
                />
              ))}
            </Box>
          )}
        </Box>
      </Box>

      {loading ? (
        <Grid container spacing={3}>
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} sx={{ mx: 'auto',  }} key={i}>
              <Card sx={{ height: 600 }}>
                <Skeleton variant="rectangular" height={250} />
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : articlesList.length === 0 ? (
        <Alert severity="info">
          Keine Artikel gefunden für den aktuellen Filter.
        </Alert>
      ) : (
        <Grid container spacing={2}>
          {[...articlesList]
            .sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime())
            .map((article) => (
              <Grid size={{ xs: 12, sm: 6, md: 4 }} sx={{ mx: 'auto',  }} key={article.id}>
                <ArticleCard
                  article={article}
                  isRead={isRead(article.id)}
                  isFavorite={isFavorite(article.id)}
                  updating={updatingArticleId === article.id}
                  onToggleRead={() => onToggleRead(article.id)}
                  onToggleFavorite={() => onToggleFavorite(article.id)}
                />
              </Grid>
            ))}
        </Grid>
      )}
    </Box>
  )
}
