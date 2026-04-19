import { Box, Card, Grid, Skeleton, Typography, Alert } from '@mui/material'
import { ArticleCard } from '../ArticleCard'
import type { Article } from '../../api/client'

interface FavoritesViewProps {
  articles: Article[]
  loading: boolean
  articleStatuses: Record<string, { isRead?: boolean; isFavorite?: boolean }>
  updatingArticleId: string | null
  onToggleRead: (articleId: string) => void
  onToggleFavorite: (articleId: string) => void
}

export function FavoritesView({
  articles,
  loading,
  articleStatuses,
  updatingArticleId,
  onToggleRead,
  onToggleFavorite,
}: FavoritesViewProps) {
  const favoriteArticles = articles.filter(a => articleStatuses[a.id]?.isFavorite)

  const isRead = (id: string) => articleStatuses[id]?.isRead ?? false
  const isFavorite = (id: string) => articleStatuses[id]?.isFavorite ?? false

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3, fontWeight: 600 }}>
        Favoriten ({favoriteArticles.length})
      </Typography>

      {loading ? (
        <Grid container spacing={3}>
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} sx={{ mx: 'auto' }} key={i}>
              <Card sx={{ height: 430 }}>
                <Skeleton variant="rectangular" height={200} />
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : favoriteArticles.length === 0 ? (
        <Alert severity="info">
          Noch keine Favoriten. Klicke auf das Bookmark-Symbol bei einem Artikel, um ihn zu den Favoriten hinzuzufuegen.
        </Alert>
      ) : (
        <Grid container spacing={2}>
          {[...favoriteArticles]
            .sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime())
            .map((article) => (
              <Grid size={{ xs: 12, sm: 6, md: 4 }} sx={{ mx: 'auto' }} key={article.id}>
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
