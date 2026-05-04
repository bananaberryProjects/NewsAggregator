import { useMemo } from 'react'
import { Box, Skeleton, Typography, Alert, Grid } from '@mui/material'
import type { Category, Feed } from '../../api/client'
import { CategoryCard } from '../CategoryCard'

interface CategoriesViewProps {
  categories: Category[]
  feeds: Feed[]
  loading: boolean
  onEdit: (category: Category) => void
  onDelete: (id: string) => void
}

export function CategoriesView({ categories, feeds, loading, onEdit, onDelete }: CategoriesViewProps) {
  // Feed-/Artikel-Zählung pro Kategorie im Frontend berechnen
  const categoryStats = useMemo(() => {
    const stats = new Map<string, { feedCount: number; articleCount: number }>()
    
    for (const cat of categories) {
      stats.set(cat.id, { feedCount: 0, articleCount: 0 })
    }

    for (const feed of feeds) {
      for (const catId of feed.categoryIds || []) {
        const s = stats.get(catId)
        if (s) {
          s.feedCount += 1
          s.articleCount += (feed.articleCount || 0)
        }
      }
    }

    return stats
  }, [categories, feeds])

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3, fontWeight: 600 }}>
        Kategorien ({categories.length})
      </Typography>

      {loading ? (
        <Grid container spacing={3}>
          {[1, 2, 3, 4].map((i) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={i}>
              <Box
                sx={{
                  height: 200,
                  overflow: 'hidden',
                  borderRadius: 3,
                  bgcolor: 'background.paper',
                }}
              >
                <Skeleton variant="rectangular" height={90} width="100%" sx={{ mb: 0 }} />
                <Box sx={{ p: 2, pt: 4 }}>
                  <Skeleton variant="text" width="60%" sx={{ mb: 1 }} />
                  <Box sx={{ display: 'flex', gap: 1 }}>
                    <Skeleton variant="rounded" width={80} height={24} />
                    <Skeleton variant="rounded" width={80} height={24} />
                  </Box>
                </Box>
              </Box>
            </Grid>
          ))}
        </Grid>
      ) : categories.length === 0 ? (
        <Alert severity="info">
          Noch keine Kategorien vorhanden.
        </Alert>
      ) : (
        <Grid container spacing={3}>
          {[...categories].sort((a, b) => a.name.localeCompare(b.name)).map((category) => {
            const stats = categoryStats.get(category.id) || { feedCount: 0, articleCount: 0 }
            return (
              <Grid size={{ xs: 12, sm: 6, md: 4 }} key={category.id}>
                <CategoryCard
                  category={category}
                  feedCount={stats.feedCount}
                  articleCount={stats.articleCount}
                  onEdit={() => onEdit(category)}
                  onDelete={() => onDelete(category.id)}
                />
              </Grid>
            )
          })}
        </Grid>
      )}
    </Box>
  )
}
