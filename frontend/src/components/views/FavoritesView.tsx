import { Box, Card, Skeleton, Typography, Alert } from '@mui/material'
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
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 600 }}>
        Favoriten ({favoriteArticles.length})
      </Typography>

      {loading ? (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, justifyContent: 'center' }}>
          {[1, 2, 3, 4].map((i) => (
            <Card key={i} sx={{ width: 480, height: 420 }}>
              <Skeleton variant="rectangular" height={200} />
            </Card>
          ))}
        </Box>
      ) : favoriteArticles.length === 0 ? (
        <Alert severity="info">
          Noch keine Favoriten. Klicke auf das Bookmark-Symbol bei einem Artikel, um ihn zu den Favoriten hinzuzufügen.
        </Alert>
      ) : (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, justifyContent: 'center' }}>
          {[...favoriteArticles]
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
