import { Box, Card, Chip, Grid, Paper, Skeleton, Typography, Alert } from '@mui/material'
import {
  Feed as FeedIcon,
  Article as ArticleIcon,
  Favorite as FavoriteIcon,
  Label as LabelIcon,
} from '@mui/icons-material'
import { ArticleCard } from '../ArticleCard'
import type { Article, Feed, Category } from '../../api/client'

interface DashboardViewProps {
  feeds: Feed[]
  articles: Article[]
  categories: Category[]
  loading: boolean
  error: string | null
  articleStatuses: Record<string, { isRead?: boolean; isFavorite?: boolean }>
  updatingArticleId: string | null
  dashboardFilter: 'all' | 'unread' | 'favorites'
  dashboardCategoryFilter: string[]
  onFilterChange: (filter: 'all' | 'unread' | 'favorites') => void
  onCategoryFilterChange: (categories: string[]) => void
  onToggleRead: (articleId: string) => void
  onToggleFavorite: (articleId: string) => void
}

export function DashboardView({
  feeds,
  articles,
  categories,
  loading,
  error,
  articleStatuses,
  updatingArticleId,
  dashboardFilter,
  dashboardCategoryFilter,
  onFilterChange,
  onCategoryFilterChange,
  onToggleRead,
  onToggleFavorite,
}: DashboardViewProps) {
  const getFilteredArticles = () => {
    let result = [...articles]

    if (dashboardCategoryFilter.length > 0) {
      result = result.filter(a =>
        a.categoryIds?.some(catId => dashboardCategoryFilter.includes(catId))
      )
    }

    switch (dashboardFilter) {
      case 'unread':
        return result.filter(a => !articleStatuses[a.id]?.isRead)
      case 'favorites':
        return result.filter(a => articleStatuses[a.id]?.isFavorite)
      default:
        return result
    }
  }

  const dashboardArticles = getFilteredArticles()
    .sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime())
    .slice(0, 10)

  const isRead = (id: string) => articleStatuses[id]?.isRead ?? false
  const isFavorite = (id: string) => articleStatuses[id]?.isFavorite ?? false

  return (
    <Box>
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Paper elevation={2} sx={{ p: 2, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white' }}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <FeedIcon sx={{ fontSize: 40, mr: 2, opacity: 0.8 }} />
              <Box>
                <Typography variant="h4" sx={{ fontWeight: 700 }}>
                  {loading ? <Skeleton width={60} /> : feeds.length}
                </Typography>
                <Typography variant="body2">Feeds</Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Paper elevation={2} sx={{ p: 2, background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', color: 'white' }}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <ArticleIcon sx={{ fontSize: 40, mr: 2, opacity: 0.8 }} />
              <Box>
                <Typography variant="h4" sx={{ fontWeight: 700 }}>
                  {loading ? <Skeleton width={60} /> : articles.length}
                </Typography>
                <Typography variant="body2">Artikel</Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Paper elevation={2} sx={{ p: 2, background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', color: 'white' }}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <FavoriteIcon sx={{ fontSize: 40, mr: 2, opacity: 0.8, color: 'error.main' }} />
              <Box>
                <Typography variant="h4" sx={{ fontWeight: 700 }}>
                  {loading ? <Skeleton width={60} /> : articles.filter(a => articleStatuses[a.id]?.isFavorite).length}
                </Typography>
                <Typography variant="body2">Favoriten</Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Paper elevation={2} sx={{ p: 2, background: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', color: 'white' }}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <LabelIcon sx={{ fontSize: 40, mr: 2, opacity: 0.8 }} />
              <Box>
                <Typography variant="h4" sx={{ fontWeight: 700 }}>
                  {loading ? <Skeleton width={60} /> : categories.length}
                </Typography>
                <Typography variant="body2">Kategorien</Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>
      </Grid>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Box sx={{ mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
          <Typography variant="h5" sx={{ fontWeight: 600 }}>
            Neueste Artikel
          </Typography>

          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 1 }}>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, justifyContent: 'flex-end' }}>
              <Chip
                label="Alle"
                onClick={() => onFilterChange('all')}
                color={dashboardFilter === 'all' ? 'primary' : 'default'}
                variant={dashboardFilter === 'all' ? 'filled' : 'outlined'}
                size="small"
              />
              <Chip
                label="Ungelesen"
                onClick={() => onFilterChange('unread')}
                color={dashboardFilter === 'unread' ? 'primary' : 'default'}
                variant={dashboardFilter === 'unread' ? 'filled' : 'outlined'}
                size="small"
              />
              <Chip
                label="Favoriten"
                onClick={() => onFilterChange('favorites')}
                color={dashboardFilter === 'favorites' ? 'primary' : 'default'}
                variant={dashboardFilter === 'favorites' ? 'filled' : 'outlined'}
                size="small"
              />
            </Box>

            {categories.length > 0 && (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, justifyContent: 'flex-end' }}>
                <Chip
                  label="Alle"
                  onClick={() => onCategoryFilterChange([])}
                  color={dashboardCategoryFilter.length === 0 ? 'primary' : 'default'}
                  variant={dashboardCategoryFilter.length === 0 ? 'filled' : 'outlined'}
                  size="small"
                />
                {categories.map((category) => (
                  <Chip
                    key={category.id}
                    label={category.name}
                    onClick={() => {
                      onCategoryFilterChange(
                        dashboardCategoryFilter.includes(category.id)
                          ? dashboardCategoryFilter.filter(id => id !== category.id)
                          : [...dashboardCategoryFilter, category.id]
                      )
                    }}
                    sx={{
                      backgroundColor: dashboardCategoryFilter.includes(category.id) ? category.color : 'transparent',
                      color: dashboardCategoryFilter.includes(category.id) ? '#fff' : 'inherit',
                      borderColor: category.color,
                    }}
                    variant={dashboardCategoryFilter.includes(category.id) ? 'filled' : 'outlined'}
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
              <Grid size={{ xs: 12, sm: 6 }} key={i}>
                <Card sx={{ height: 420 }}>
                  <Skeleton variant="rectangular" height={200} />
                </Card>
              </Grid>
            ))}
          </Grid>
        ) : dashboardArticles.length === 0 ? (
          <Alert severity="info">
            Keine Artikel gefunden für den aktuellen Filter.
          </Alert>
        ) : (
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, justifyContent: 'center' }}>
            {dashboardArticles.map((article) => (
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
    </Box>
  )
}
