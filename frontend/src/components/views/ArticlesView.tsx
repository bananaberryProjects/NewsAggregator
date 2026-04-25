import { useState, useEffect } from 'react'
import {
  Box,
  Card,
  Chip,
  Grid,
  Skeleton,
  Typography,
  Alert,
  Drawer,
  IconButton,
  Divider,
  Button,
  Badge,
  CircularProgress,
} from '@mui/material'
import { FilterList as FilterIcon, Close as CloseIcon } from '@mui/icons-material'
import { ArticleCard } from '../ArticleCard'
import { useInfiniteArticles } from '../../hooks/useInfiniteArticles'
import type { Article, Category } from '../../api/client'

interface ArticlesViewProps {
  articles: Article[]
  categories: Category[]
  loading: boolean
  articleStatuses: Record<number, { isRead?: boolean; isFavorite?: boolean }>
  updatingArticleId: number | null
  articlesFilter: 'all' | 'unread' | 'favorites'
  articlesCategoryFilter: string[]
  searchResults?: any[] | null
  isSearchActive: boolean
  searchTotalElements?: number
  searchTotalPages?: number
  searchPage?: number
  onSearchNextPage?: () => void
  onSearchReset?: () => void
  onFilterChange: (filter: 'all' | 'unread' | 'favorites') => void
  onCategoryFilterChange: (categories: string[]) => void
  onToggleRead: (articleId: number) => void
  onToggleFavorite: (articleId: number) => void
  onOpenReader: (article: Article) => void
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
  searchResults,
  isSearchActive,
  searchTotalElements,
  searchTotalPages,
  searchPage,
  onSearchNextPage,
  onSearchReset,
  onOpenReader,
}: ArticlesViewProps) {
  const [filterDrawerOpen, setFilterDrawerOpen] = useState(false)

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

  const articlesList = isSearchActive
    ? (searchResults ?? [])
    : getFilteredArticles()

  // Infinite Scroll nur bei normaler Ansicht: Initial 18, dann +9 pro Scroll
  const { displayedArticles, hasMore, loadMoreRef, totalCount } = useInfiniteArticles({
    articles: [...articlesList].sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime()),
    batchSize: 9,
    initialCount: 18
  })

  const isRead = (id: number) => articleStatuses[id]?.isRead ?? false
  const isFavorite = (id: number) => articleStatuses[id]?.isFavorite ?? false

  // Count active filters
  const activeFilterCount =
    (articlesFilter !== 'all' ? 1 : 0) +
    (articlesCategoryFilter.length > 0 ? 1 : 0)

  return (
    <Box sx={{ flexShrink: 0, width: '100%', overflowX: 'hidden' }}>
      {/* Header mit Filter-Button */}
      <Box sx={{ flexShrink: 0, display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 1 }}>
        <Box sx={{ flexShrink: 0, display: 'flex', alignItems: 'center', gap: 1, minWidth: 0, flex: 1, overflow: 'hidden' }}>
          <Typography variant="h5" sx={{ flexShrink: 0, fontWeight: 600, minWidth: 0 }} noWrap>
            {isSearchActive ? `Ergebnisse (${searchTotalElements ?? 0})` : `Artikel (${totalCount})`}
          </Typography>
          {isSearchActive && (
            <Button variant="outlined" size="small" onClick={onSearchReset}>
              Zurücksetzen
            </Button>
          )}
        </Box>

        <IconButton
          color="primary"
          onClick={() => setFilterDrawerOpen(true)}
          sx={{ flexShrink: 0, mr: 1, mt: 1,
            bgcolor: 'background.paper',
            boxShadow: 1,
            '&:hover': { bgcolor: 'background.paper', boxShadow: 2 },
          }}
        >
          <Badge badgeContent={activeFilterCount > 0 ? activeFilterCount : 0} color="error">
            <FilterIcon />
          </Badge>
        </IconButton>
      </Box>

      {/* Filter Drawer */}
      <Drawer
        anchor="right"
        open={filterDrawerOpen}
        onClose={() => setFilterDrawerOpen(false)}
        sx={{ flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: { xs: '85%', sm: 360 },
            p: 3,
            pt: { xs: 6, sm: 3 },
          },
        }}
      >
        {/* Swipe Handle für Mobile */}
        <Box
          sx={{ flexShrink: 0,
            display: { xs: 'flex', sm: 'none' },
            justifyContent: 'center',
            mb: 2,
            cursor: 'pointer',
          }}
          onClick={() => setFilterDrawerOpen(false)}
        >
          <Box
            sx={{ flexShrink: 0,
              width: 40,
              height: 4,
              bgcolor: 'grey.400',
              borderRadius: 2,
            }}
          />
        </Box>

        <Box sx={{ flexShrink: 0, display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 1 }}>
          <Typography variant="h6" sx={{ flexShrink: 0, fontWeight: 600 }}>
            Filter
          </Typography>
          <IconButton onClick={() => setFilterDrawerOpen(false)}>
            <CloseIcon />
          </IconButton>
        </Box>

        <Divider sx={{ flexShrink: 0, mb: 3 }} />

        {/* Status Filter */}
        <Typography variant="subtitle2" sx={{ flexShrink: 0, mb: 2, fontWeight: 600 }}>
          Status
        </Typography>
        <Box sx={{ flexShrink: 0, display: 'flex', flexWrap: 'wrap', gap: 1, mb: 4 }}>
          <Chip
            label="Alle"
            onClick={() => onFilterChange('all')}
            color={articlesFilter === 'all' ? 'primary' : 'default'}
            variant={articlesFilter === 'all' ? 'filled' : 'outlined'}
          />
          <Chip
            label="Ungelesen"
            onClick={() => onFilterChange('unread')}
            color={articlesFilter === 'unread' ? 'primary' : 'default'}
            variant={articlesFilter === 'unread' ? 'filled' : 'outlined'}
          />
          <Chip
            label="Favoriten"
            onClick={() => onFilterChange('favorites')}
            color={articlesFilter === 'favorites' ? 'primary' : 'default'}
            variant={articlesFilter === 'favorites' ? 'filled' : 'outlined'}
          />
        </Box>

        {/* Category Filter */}
        {categories.length > 0 && (
          <>
            <Typography variant="subtitle2" sx={{ flexShrink: 0, mb: 2, fontWeight: 600 }}>
              Kategorien
            </Typography>
            <Box sx={{ flexShrink: 0, display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              <Chip
                label="Alle"
                onClick={() => onCategoryFilterChange([])}
                color={articlesCategoryFilter.length === 0 ? 'primary' : 'default'}
                variant={articlesCategoryFilter.length === 0 ? 'filled' : 'outlined'}
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
                  sx={{ flexShrink: 0,
                    backgroundColor: articlesCategoryFilter.includes(category.id) ? category.color : 'transparent',
                    color: articlesCategoryFilter.includes(category.id) ? '#fff' : 'inherit',
                    borderColor: category.color,
                  }}
                  variant={articlesCategoryFilter.includes(category.id) ? 'filled' : 'outlined'}
                />
              ))}
            </Box>
          </>
        )}

        <Box sx={{ flexShrink: 0, flexGrow: 1 }} />

        {/* Reset Button */}
        <Button
          variant="outlined"
          fullWidth
          onClick={() => {
            onFilterChange('all')
            onCategoryFilterChange([])
          }}
          sx={{ flexShrink: 0, mt: 2 }}
        >
          Filter zurücksetzen
        </Button>

        <Button
          variant="contained"
          fullWidth
          onClick={() => setFilterDrawerOpen(false)}
          sx={{ flexShrink: 0, mt: 2, display: { xs: 'flex', sm: 'none' } }}
        >
          Fertig
        </Button>
      </Drawer>

      {/* Articles Grid with Infinite Scroll */}
      {loading ? (
        <Grid container spacing={3}>
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={i}>
              <Card sx={{ flexShrink: 0, height: 430 }}>
                <Skeleton variant="rectangular" height={200} />
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : articlesList.length === 0 ? (
        <Alert severity="info">
          Keine Artikel gefunden für den aktuellen Filter.
        </Alert>
      ) : (
        <>
          <Grid container spacing={2}>
            {(isSearchActive ? articlesList : displayedArticles).map((article) => (
              <Grid size={{ xs: 12, sm: 6, md: 4 }} key={article.id}>
                <ArticleCard
                  article={article}
                  isRead={isRead(article.id)}
                  isFavorite={isFavorite(article.id)}
                  updating={updatingArticleId === article.id}
                  hasContentHtml={!!article.contentHtml}
                  onToggleRead={() => onToggleRead(article.id)}
                  onToggleFavorite={() => onToggleFavorite(article.id)}
                  onOpenReader={() => onOpenReader(article)}
                />
              </Grid>
            ))}
          </Grid>
          
          {/* Load More Trigger Element */}
          <Box
            ref={loadMoreRef}
            sx={{ flexShrink: 0,
              height: 20,
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              mt: 3,
              mb: 3
            }}
          >
            {isSearchActive && searchTotalPages && searchPage !== undefined && searchPage + 1 < searchTotalPages && (
              <Box sx={{ flexShrink: 0, display: 'flex', justifyContent: 'center', mt: 4, mb: 2 }}>
                <Button variant="outlined" onClick={onSearchNextPage} disabled={!onSearchNextPage}>
                  Mehr laden
                </Button>
              </Box>
            )}

            {!isSearchActive && hasMore && (
              <CircularProgress size={24} />
            )}
          </Box>
          
          {/* Status Text */}
          <Typography 
            variant="body2" 
            color="text.secondary" 
            sx={{ flexShrink: 0, textAlign: 'center', mb: 2 }}
          >
            {isSearchActive
              ? `${articlesList.length} von ${searchTotalElements ?? 0} Ergebnissen`
              : hasMore 
                ? `${displayedArticles.length} von ${totalCount} Artikeln geladen`
                : `${totalCount} Artikel (alle geladen)`
            }
          </Typography>
        </>
      )}
    </Box>
  )
}
