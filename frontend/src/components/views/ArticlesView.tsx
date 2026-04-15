import { Box, Card, Chip, Skeleton, Typography, Alert } from '@mui/material'
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
        <Typography variant="h4" sx={{ fontWeight: 600 }}>
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
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, justifyContent: 'center' }}>
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <Card key={i} sx={{ width: 480, height: 420 }}>
              <Skeleton variant="rectangular" height={200} />
            </Card>
          ))}
        </Box>
      ) : articlesList.length === 0 ? (
        <Alert severity="info">
          Keine Artikel gefunden für den aktuellen Filter.
        </Alert>
      ) : (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, justifyContent: 'center' }}>
          {[...articlesList]
            .sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime())
            .map((article) => (
              <ArticleCard
                key={article.id}
                article={article}
                isRead={isRead(article.id)}
                isFavorite={isFavorite(article.id)}
                updating={updatingArticleId === article.id}
                onToggleRead={() => onToggleRead(article.id)}
                onToggleFavorite={() => onToggleFavorite(article.id)}
              />
            ))}
        </Box>
      )}
    </Box>
  )
}
