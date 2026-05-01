import {
  Card,
  CardContent,
  CardMedia,
  CardActions,
  Typography,
  Button,
  Chip,
  Box,
  IconButton,
  CircularProgress,
  Tooltip,
} from '@mui/material'
import { ExternalLink, Calendar, CheckCircle2, FileText, Eye, EyeOff, Heart, BookOpen } from 'lucide-react'
import type { Article, Category } from '../api/client'
import { stripHtml } from '../utils'

const PLACEHOLDER_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDgwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNDgwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iIzMzNzVkYiIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE4IiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPk5ld3M8L3RleHQ+PC9zdmc+'

interface ArticleCardProps {
  article: Article
  categories?: Category[]
  isRead: boolean
  isFavorite: boolean
  updating: boolean
  hasContentHtml: boolean
  onToggleRead: () => void
  onToggleFavorite: () => void
  onOpenReader: () => void
}

function getCategoryForArticle(article: Article, categories: Category[] = []): Category | undefined {
  if (!article.categoryIds || article.categoryIds.length === 0) return undefined
  return categories.find(c => c.id === article.categoryIds![0])
}

export function ArticleCard({
  article,
  categories = [],
  isRead,
  isFavorite,
  updating,
  hasContentHtml,
  onToggleRead,
  onToggleFavorite,
  onOpenReader,
}: ArticleCardProps) {
  const primaryCategory = getCategoryForArticle(article, categories)

  return (
    <Card
      sx={{
        width: '100%',
        height: 430,
        display: 'flex',
        flexDirection: 'column',
        border: '1px solid',
        borderColor: 'divider',
        transition: 'all 0.3s ease',
        opacity: isRead ? 0.75 : 1,
        '&:hover': {
          transform: 'translateY(-6px)',
          boxShadow: (theme) => theme.shadows[8],
          borderColor: 'primary.main',
        },
      }}
    >
      {/* Image area with overlay badges */}
      <Box sx={{ position: 'relative', overflow: 'hidden', height: 200 }}>
        <CardMedia
          component="img"
          height={200}
          image={article.imageUrl || PLACEHOLDER_IMAGE}
          alt={article.title}
          sx={{
            objectFit: 'cover',
            width: '100%',
            display: 'block',
            transition: 'transform 0.3s ease',
            '.MuiCard-root:hover &': {
              transform: 'scale(1.05)',
            },
          }}
        />

        {/* Category badge — top-left overlay */}
        {primaryCategory && (
          <Chip
            size="small"
            label={primaryCategory.name}
            sx={{
              position: 'absolute',
              top: 8,
              left: 8,
              backgroundColor: primaryCategory.color,
              color: '#fff',
              fontWeight: 600,
              textShadow: '0 1px 2px rgba(0,0,0,0.3)',
              boxShadow: '0 1px 4px rgba(0,0,0,0.2)',
              zIndex: 1,
            }}
          />
        )}

        {/* Unread dot — top-right */}
        {!isRead && (
          <Box
            sx={{
              position: 'absolute',
              top: 10,
              right: 10,
              width: 10,
              height: 10,
              borderRadius: '50%',
              backgroundColor: 'primary.main',
              boxShadow: '0 0 0 2px rgba(255,255,255,0.8)',
              zIndex: 1,
            }}
          />
        )}
      </Box>

      <CardContent sx={{ flexGrow: 1, overflow: 'hidden' }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1, minWidth: 0 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Chip size="small" label={article.feedName || 'News'} color="primary" sx={{ maxWidth: 120, overflow: 'hidden', textOverflow: 'ellipsis' }} />
            {hasContentHtml && (
              <Tooltip title="Vollständiger Artikel verfügbar">
                <Box sx={{ color: 'primary.main', ml: 0.5 }} component="span">
                  <BookOpen size={16} />
                </Box>
              </Tooltip>
            )}
          </Box>
          <Typography variant="caption" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <Calendar size={14} />
            {article.publishedAt ? new Date(article.publishedAt).toLocaleDateString('de-DE', {
              day: 'numeric', month: 'short', year: 'numeric',
            }) : 'Kein Datum'}
          </Typography>
        </Box>

        <Typography
          variant="h6"
          sx={{
            fontSize: '1rem',
            fontWeight: isRead ? 500 : 600,
            mb: 1,
            lineHeight: 1.3,
            display: '-webkit-box',
            WebkitLineClamp: 3,
            WebkitBoxOrient: 'vertical',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            maxHeight: '3.9em',
            wordBreak: 'break-word',
            color: isRead ? 'text.secondary' : 'text.primary',
            transition: 'color 0.3s ease',
          }}
        >
          {isRead && (
            <Box component="span" sx={{ color: 'success.main', mr: 0.5, verticalAlign: 'middle', display: 'inline-flex' }}>
              <CheckCircle2 size={16} />
            </Box>
          )}
          {article.title}
        </Typography>

        <Typography
          variant="body2"
          color="text.secondary"
          sx={{
            mb: 1,
            maxHeight: 60,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            display: '-webkit-box',
            WebkitLineClamp: 3,
            WebkitBoxOrient: 'vertical',
            lineHeight: 1.4,
          }}
          title={stripHtml(article.description)}
        >
          {stripHtml(article.description, 300) || 'Keine Beschreibung verfügbar'}
        </Typography>
      </CardContent>

      <CardActions sx={{ justifyContent: 'space-between', px: 2, pb: 2 }}>
        <Button
          size="small"
          startIcon={hasContentHtml ? <FileText size={18} /> : <ExternalLink size={18} />}
          onClick={hasContentHtml ? onOpenReader : () => window.open(article.link, '_blank')}
        >
          {hasContentHtml ? 'Weiterlesen' : 'Lesen'}
        </Button>

        <Box>
          <IconButton
            size="small"
            onClick={onToggleRead}
            disabled={updating}
            sx={{ mr: 0.5 }}
            title={isRead ? 'Als ungelesen markieren' : 'Als gelesen markieren'}
          >
            {updating ? (
              <CircularProgress size={20} />
            ) : isRead ? (
              <EyeOff size={20} />
            ) : (
              <Eye size={20} />
            )}
          </IconButton>

          <IconButton
            size="small"
            onClick={onToggleFavorite}
            disabled={updating}
            title={isFavorite ? 'Aus Favoriten entfernen' : 'Zu Favoriten hinzufügen'}
          >
            {updating ? (
              <CircularProgress size={20} />
            ) : isFavorite ? (
              <Box sx={{ color: 'error.main', display: 'flex' }}>
                <Heart size={20} fill="currentColor" />
              </Box>
            ) : (
              <Heart size={20} />
            )}
          </IconButton>
        </Box>
      </CardActions>
    </Card>
  )
}
