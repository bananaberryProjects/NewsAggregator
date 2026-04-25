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
import {
  Launch as LaunchIcon,
  CalendarToday as CalendarTodayIcon,
  Favorite as FavoriteIcon,
  FavoriteBorder as FavoriteBorderIcon,
  Visibility as VisibilityIcon,
  VisibilityOff as VisibilityOffIcon,
  CheckCircle as CheckCircleIcon,
  Article as ArticleIcon,
  MenuBook as MenuBookIcon,
} from '@mui/icons-material'
import type { Article } from '../api/client'
import { stripHtml } from '../utils'

const PLACEHOLDER_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDgwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNDgwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iIzMzNzVkYiIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE4IiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPk5ld3M8L3RleHQ+PC9zdmc+'

interface ArticleCardProps {
  article: Article
  isRead: boolean
  isFavorite: boolean
  updating: boolean
  hasContentHtml: boolean
  onToggleRead: () => void
  onToggleFavorite: () => void
  onOpenReader: () => void
}

export function ArticleCard({
  article,
  isRead,
  isFavorite,
  updating,
  hasContentHtml,
  onToggleRead,
  onToggleFavorite,
  onOpenReader,
}: ArticleCardProps) {
  return (
    <Card
      sx={{
        width: '100%',
        height: 430,
        display: 'flex',
        flexDirection: 'column',
        transition: 'transform 0.2s, box-shadow 0.2s',
        opacity: isRead ? 0.85 : 1,
        '&:hover': {
          transform: 'translateY(-4px)',
          boxShadow: 6,
        },
      }}
    >
      <CardMedia
        component="img"
        height={200}
        image={article.imageUrl || PLACEHOLDER_IMAGE}
        alt={article.title}
        sx={{ objectFit: 'cover', width: '100%', display: 'block' }}
      />
      <CardContent sx={{ flexGrow: 1, overflow: 'hidden' }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1, minWidth: 0 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Chip size="small" label={article.feedName || 'News'} color="primary" sx={{ maxWidth: 120, overflow: 'hidden', textOverflow: 'ellipsis' }} />
            {hasContentHtml && (
              <Tooltip title="Vollständiger Artikel verfügbar">
                <MenuBookIcon
                  fontSize="small"
                  color="primary"
                  sx={{ ml: 0.5 }}
                />
              </Tooltip>
            )}
          </Box>
          <Typography variant="caption" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <CalendarTodayIcon fontSize="inherit" />
            {article.publishedAt ? new Date(article.publishedAt).toLocaleDateString('de-DE', {
              day: 'numeric', month: 'short', year: 'numeric',
            }) : 'Kein Datum'}
          </Typography>
        </Box>
        
        <Typography 
          variant="h6" 
          sx={{ 
            fontSize: '1rem', 
            fontWeight: 600, 
            mb: 1, 
            lineHeight: 1.3,
            display: '-webkit-box',
            WebkitLineClamp: 3,
            WebkitBoxOrient: 'vertical',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            maxHeight: '3.9em',
            wordBreak: 'break-word',
          }}
        >
          {isRead && (
            <CheckCircleIcon 
              fontSize="small" 
              color="success" 
              sx={{ mr: 0.5, verticalAlign: 'middle' }} 
            />
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
            lineHeight: 1.4
          }}
          title={stripHtml(article.description)}
        >
          {stripHtml(article.description, 300) || 'Keine Beschreibung verfügbar'}
        </Typography>
      </CardContent>
      
      <CardActions sx={{ justifyContent: 'space-between', px: 2, pb: 2 }}>
        <Button
          size="small"
          startIcon={hasContentHtml ? <ArticleIcon /> : <LaunchIcon />}
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
              <VisibilityOffIcon color="action" />
            ) : (
              <VisibilityIcon color="primary" />
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
              <FavoriteIcon color="error" />
            ) : (
              <FavoriteBorderIcon color="action" />
            )}
          </IconButton>
        </Box>
      </CardActions>
    </Card>
  )
}
