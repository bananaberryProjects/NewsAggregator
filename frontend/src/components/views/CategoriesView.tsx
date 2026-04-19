import { Box, Card, Avatar, IconButton, Skeleton, Typography, Alert } from '@mui/material'
import { Delete as DeleteIcon } from '@mui/icons-material'
import type { Category } from '../../api/client'

interface CategoriesViewProps {
  categories: Category[]
  loading: boolean
  onDelete: (id: string) => void
}

export function CategoriesView({ categories, loading, onDelete }: CategoriesViewProps) {
  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3, fontWeight: 600 }}>
        Kategorien ({categories.length})
      </Typography>

      {loading ? (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3 }}>
          {[1, 2, 3, 4].map((i) => (
            <Card key={i} sx={{ width: 300, height: 120 }}>
              <Skeleton variant="rectangular" height={120} />
            </Card>
          ))}
        </Box>
      ) : categories.length === 0 ? (
        <Alert severity="info">
          Noch keine Kategorien vorhanden.
        </Alert>
      ) : (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3 }}>
          {categories.map((category) => (
            <Card
              key={category.id}
              sx={{
                display: 'flex',
                alignItems: 'center',
                p: 2,
                width: 300,
                borderLeft: `4px solid ${category.color}`,
              }}
            >
              <Avatar sx={{ width: 48, height: 48, mr: 2, bgcolor: category.color }}>
                {category.name.charAt(0)}
              </Avatar>
              <Box sx={{ flexGrow: 1 }}>
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  {category.name}
                </Typography>
              </Box>
              <IconButton onClick={() => onDelete(category.id)} color="error">
                <DeleteIcon />
              </IconButton>
            </Card>
          ))}
        </Box>
      )}
    </Box>
  )
}
