import { Box, Card, Avatar, IconButton, Skeleton, Typography, Alert, Grid } from '@mui/material'
import { Delete as DeleteIcon, Edit as EditIcon } from '@mui/icons-material'
import {
  Label,
  Folder,
  Star,
  Bookmark,
  Favorite,
  Home,
  Work,
  School,
  Sports,
  MusicNote,
  Movie,
  Book,
  Computer,
  Phone,
  Email,
  CalendarToday,
  ShoppingCart,
  Restaurant,
  Flight,
  DirectionsCar,
  CurrencyBitcoin,
  TrendingUp,
  Newspaper
} from '@mui/icons-material'
import type { Category } from '../../api/client'

// Icon-Mapping für häufig verwendete Icons - wird auch in Dialogen verwendet
export const ICON_MAP: Record<string, React.ComponentType> = {
  label: Label,
  folder: Folder,
  star: Star,
  bookmark: Bookmark,
  favorite: Favorite,
  home: Home,
  work: Work,
  school: School,
  sports: Sports,
  music: MusicNote,
  movie: Movie,
  book: Book,
  computer: Computer,
  phone: Phone,
  email: Email,
  calendar: CalendarToday,
  shopping: ShoppingCart,
  restaurant: Restaurant,
  flight: Flight,
  car: DirectionsCar,
  crypto: CurrencyBitcoin,
  economy: TrendingUp,
  news: Newspaper
}

// Hilfsfunktion um das Icon dynamisch zu laden
function getIconComponent(iconName: string) {
  const normalizedName = (iconName || 'label').toLowerCase().trim()
  return ICON_MAP[normalizedName] || Label
}

interface CategoriesViewProps {
  categories: Category[]
  loading: boolean
  onDelete: (id: string) => void
  onEdit: (category: Category) => void
}

export function CategoriesView({ categories, loading, onDelete, onEdit }: CategoriesViewProps) {
  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3, fontWeight: 600 }}>
        Kategorien ({categories.length})
      </Typography>

      {loading ? (
        <Grid container spacing={3}>
          {[1, 2, 3, 4].map((i) => (
            <Grid size={{ xs: 12, sm: 6 }} key={i}>
              <Card sx={{ height: 120 }}>
                <Skeleton variant="rectangular" height={120} />
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : categories.length === 0 ? (
        <Alert severity="info">
          Noch keine Kategorien vorhanden.
        </Alert>
      ) : (
        <Grid container spacing={3}>
          {categories.map((category) => {
            const IconComponent = getIconComponent(category.icon)
            return (
              <Grid size={{ xs: 12, sm: 6 }} key={category.id}>
                <Card
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    p: 2,
                    borderLeft: `4px solid ${category.color}`,
                  }}
                >
                  <Avatar sx={{ width: 56, height: 56, mr: 2, bgcolor: category.color }}>
                    <IconComponent />
                  </Avatar>
                  <Box sx={{ flexGrow: 1 }}>
                    <Typography variant="h6" sx={{ fontWeight: 600 }}>
                      {category.name}
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', gap: 1 }}>
                    <IconButton onClick={() => onEdit(category)} color="primary">
                      <EditIcon />
                    </IconButton>
                    <IconButton onClick={() => onDelete(category.id)} color="error">
                      <DeleteIcon />
                    </IconButton>
                  </Box>
                </Card>
              </Grid>
            )
          })}
        </Grid>
      )}
    </Box>
  )
}
