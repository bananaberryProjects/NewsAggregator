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
} from '@mui/material'
import {
  Pencil,
  Trash2,
  MoreVertical,
  FolderOpen,
  Rss,
  Tag,
  Folder,
  Star,
  Bookmark,
  Heart,
  Home,
  Briefcase,
  GraduationCap,
  Trophy,
  Music,
  Film,
  BookOpen,
  Monitor,
  Smartphone,
  Mail,
  Calendar,
  ShoppingCart,
  UtensilsCrossed,
  Plane,
  Car,
  Bitcoin,
  TrendingUp,
  Newspaper,
} from 'lucide-react'
import type { Category } from '../api/client'
import type { LucideIcon } from 'lucide-react'

const LUCIDE_ICON_MAP: Record<string, LucideIcon> = {
  label: Tag,
  folder: Folder,
  star: Star,
  bookmark: Bookmark,
  favorite: Heart,
  home: Home,
  work: Briefcase,
  school: GraduationCap,
  sports: Trophy,
  music: Music,
  movie: Film,
  book: BookOpen,
  computer: Monitor,
  phone: Smartphone,
  email: Mail,
  calendar: Calendar,
  shopping: ShoppingCart,
  restaurant: UtensilsCrossed,
  flight: Plane,
  car: Car,
  crypto: Bitcoin,
  economy: TrendingUp,
  news: Newspaper,
}

function getCategoryIcon(iconName: string): LucideIcon {
  return LUCIDE_ICON_MAP[(iconName || '').toLowerCase().trim()] || FolderOpen
}

interface CategoryCardProps {
  category: Category
  feedCount: number
  articleCount: number
  onEdit: () => void
  onDelete: () => void
}

// Gradient aus der Kategoriefarbe generieren
function getCategoryGradient(color: string): string {
  // Hellerer und dunklerer Abgeleiteter Ton
  return `linear-gradient(135deg, ${color} 0%, ${color}dd 50%, ${color}bb 100%)`
}

export function CategoryCard({ category, feedCount, articleCount, onEdit, onDelete }: CategoryCardProps) {
  const [hovered, setHovered] = useState(false)
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null)

  const handleOpenMenu = useCallback((e: React.MouseEvent<HTMLElement>) => {
    e.stopPropagation()
    setAnchorEl(e.currentTarget)
  }, [])

  const handleCloseMenu = useCallback(() => {
    setAnchorEl(null)
  }, [])

  const gradient = getCategoryGradient(category.color)

  return (
    <Card
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      sx={{
        position: 'relative',
        overflow: 'hidden',
        height: 200,
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
          height: 60,
          background: gradient,
          position: 'relative',
          display: 'flex',
          alignItems: 'flex-end',
          px: 2,
          pb: 0,
        }}
      >
        {/* Icon-Avatar overlapping the gradient bar */}
        <Avatar
          sx={{
            width: 52,
            height: 52,
            bgcolor: 'rgba(255,255,255,0.95)',
            fontSize: '1.1rem',
            border: '3px solid',
            borderColor: 'background.paper',
            transform: 'translateY(26px)',
            boxShadow: 3,
          }}
        >
          {(() => {
            const Icon = getCategoryIcon(category.icon)
            return <Icon size={26} style={{ color: category.color }} />
          })()}
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
          <Typography variant="h6" sx={{ fontWeight: 700, fontSize: '1.1rem', lineHeight: 1.3 }}>
            {category.name}
          </Typography>
        </Box>

        <Box sx={{ flexGrow: 1, minHeight: 8 }} />

        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'center' }}>
          <Chip
            size="small"
            icon={<Rss size={14} />}
            label={`${feedCount} Feed${feedCount !== 1 ? 's' : ''}`}
            sx={{ height: 24, fontSize: '0.75rem' }}
          />
          <Chip
            size="small"
            icon={<FolderOpen size={14} />}
            label={`${articleCount} Artikel`}
            sx={{ height: 24, fontSize: '0.75rem' }}
          />
        </Box>
      </CardContent>
    </Card>
  )
}
