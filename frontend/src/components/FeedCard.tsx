import { useState, useCallback } from 'react'
import {
  Card,
  CardContent,
  Box,
  Typography,
  Avatar,
  IconButton,
  Menu,
  MenuItem,
  Chip,
  Tooltip,
  CircularProgress,
} from '@mui/material'
import {
  RefreshCw,
  Pencil,
  Trash2,
  MoreVertical,
  Rss,
  ArrowUpRight,
} from 'lucide-react'
import type { Feed, Category } from '../api/client'

// Deterministischer Gradient aus Feed-Name
function getFeedGradient(name: string): string {
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  const h1 = Math.abs(hash % 360)
  const h2 = (h1 + 50) % 360
  return `linear-gradient(135deg, hsl(${h1}, 72%, 58%), hsl(${h2}, 72%, 42%))`
}

interface FeedCardProps {
  feed: Feed
  categories?: Category[]
  isRefreshing: boolean
  onRefresh: () => void
  onEdit: () => void
  onDelete: () => void
}

export function FeedCard({ feed, categories = [], isRefreshing, onRefresh, onEdit, onDelete }: FeedCardProps) {
  const [hovered, setHovered] = useState(false)
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null)

  const handleOpenMenu = useCallback((e: React.MouseEvent<HTMLElement>) => {
    e.stopPropagation()
    setAnchorEl(e.currentTarget)
  }, [])

  const handleCloseMenu = useCallback(() => {
    setAnchorEl(null)
  }, [])

  const gradient = getFeedGradient(feed.name)
  const hasImage = !!feed.imageUrl

  // Zugeordnete Kategorien auflösen
  const assignedCategories = (feed.categoryIds || [])
    .map(id => categories.find(c => c.id === id))
    .filter((c): c is Category => c !== undefined)

  // Formatierung für letzten Abruf
  const lastFetchedText = feed.lastFetchedAt
    ? new Date(feed.lastFetchedAt).toLocaleString('de-DE', {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    : null

  return (
    <Card
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      sx={{
        position: 'relative',
        overflow: 'hidden',
        height: 280,
        display: 'flex',
        flexDirection: 'column',
        borderRadius: 3,
        transition: 'all 0.3s ease',
        transform: hovered ? 'translateY(-4px)' : 'translateY(0)',
        boxShadow: (theme) => (hovered ? theme.shadows[6] : theme.shadows[2]),
        cursor: 'pointer',
      }}
    >
      {/* Top Gradient-Bar */}
      <Box
        sx={{
          height: 80,
          background: gradient,
          position: 'relative',
          display: 'flex',
          alignItems: 'flex-end',
          px: 2,
          pb: 0,
        }}
      >
        {/* Avatar overlapping the gradient bar */}
        <Avatar
          src={hasImage ? (feed.imageUrl as string) : undefined}
          sx={{
            width: 52,
            height: 52,
            bgcolor: hasImage ? 'transparent' : 'rgba(255,255,255,0.95)',
            color: hasImage ? 'inherit' : 'text.primary',
            fontWeight: 700,
            fontSize: '1.1rem',
            border: '3px solid',
            borderColor: 'background.paper',
            transform: 'translateY(26px)',
            boxShadow: 3,
          }}
        >
          {!hasImage && feed.name.charAt(0).toUpperCase()}
        </Avatar>

        {/* Overflow Menu — top right */}
        <Box sx={{ position: 'absolute', top: 8, right: 8 }}>
          <IconButton
            size="small"
            onClick={handleOpenMenu}
            sx={{
              color: 'rgba(255,255,255,0.9)',
              opacity: hovered ? 1 : 0,
              transition: 'opacity 0.2s ease',
              '&:hover': { bgcolor: 'rgba(255,255,255,0.2)' },
            }}
          >
            <MoreVertical size={18} />
          </IconButton>
          <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleCloseMenu}>
            <MenuItem
              onClick={() => {
                handleCloseMenu()
                onEdit()
              }}
            >
              <Pencil size={16} style={{ marginRight: 8 }} />
              Bearbeiten
            </MenuItem>
            <MenuItem
              onClick={() => {
                handleCloseMenu()
                onDelete()
              }}
              sx={{ color: 'error.main' }}
            >
              <Trash2 size={16} style={{ marginRight: 8 }} />
              Löschen
            </MenuItem>
          </Menu>
        </Box>

        {/* External link icon if URL present */}
        {feed.url && (
          <Tooltip title="Feed-URL öffnen">
            <IconButton
              size="small"
              component="a"
              href={feed.url}
              target="_blank"
              rel="noopener noreferrer"
              sx={{
                position: 'absolute',
                top: 8,
                right: 40,
                color: 'rgba(255,255,255,0.9)',
                opacity: hovered ? 1 : 0,
                transition: 'opacity 0.2s ease',
                '&:hover': { bgcolor: 'rgba(255,255,255,0.2)' },
              }}
              onClick={(e) => e.stopPropagation()}
            >
              <ArrowUpRight size={18} />
            </IconButton>
          </Tooltip>
        )}
      </Box>

      <CardContent
        sx={{
          pt: 4,
          pb: 1.5,
          px: 2,
          flexGrow: 1,
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        <Box sx={{ mb: 0.5 }}>
          <Typography variant="h6" sx={{ fontWeight: 700, fontSize: '1.05rem', lineHeight: 1.3 }}>
            {feed.name}
          </Typography>
        </Box>

        <Typography
          variant="body2"
          color="text.secondary"
          sx={{
            minHeight: '2.8em',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            lineHeight: 1.4,
          }}
        >
          {feed.description || '\u00A0'}
        </Typography>

        <Box sx={{ flexGrow: 1, minHeight: 8 }} />

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
          <Box sx={{ display: 'flex', gap: 0.75, flexWrap: 'wrap' }}>
            {assignedCategories.map(cat => (
              <Chip
                key={cat.id}
                size="small"
                label={cat.name}
                sx={{
                  height: 22,
                  fontSize: '0.7rem',
                  fontWeight: 600,
                  backgroundColor: cat.color,
                  color: '#fff',
                  textShadow: '0 1px 2px rgba(0,0,0,0.3)',
                }}
              />
            ))}
          </Box>

          <Box>
            <Chip
              size="small"
              icon={<Rss size={14} />}
              label={`${feed.articleCount} Artikel`}
              sx={{ height: 22, fontSize: '0.75rem' }}
            />
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="caption" color="text.secondary">
              {lastFetchedText ? `Letzter Abruf: ${lastFetchedText}` : 'Noch nicht abgerufen'}
            </Typography>
            <Tooltip title="Aktualisieren">
              <IconButton
                size="small"
                onClick={(e) => { e.stopPropagation(); onRefresh() }}
                disabled={isRefreshing}
                sx={{ p: 0.25, ml: -0.5 }}
              >
                {isRefreshing ? <CircularProgress size={14} /> : <RefreshCw size={14} />}
              </IconButton>
            </Tooltip>
          </Box>
        </Box>
      </CardContent>
    </Card>
  )
}
