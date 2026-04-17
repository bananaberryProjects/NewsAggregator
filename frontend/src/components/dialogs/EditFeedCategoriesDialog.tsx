import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Chip,
  Box,
  Typography,
  CircularProgress,
} from '@mui/material'
import type { Feed, Category } from '../../api/client'

interface EditFeedCategoriesDialogProps {
  open: boolean
  onClose: () => void
  onSubmit: () => void
  feed: Feed | null
  selectedCategories: string[]
  toggleCategory: (id: string) => void
  categories: Category[]
  loading: boolean
}

export function EditFeedCategoriesDialog({
  open,
  onClose,
  onSubmit,
  feed,
  selectedCategories,
  toggleCategory,
  categories,
  loading,
}: EditFeedCategoriesDialogProps) {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        Kategorien zuweisen
        {feed && (
          <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 0.5 }}>
            {feed.name}
          </Typography>
        )}
      </DialogTitle>
      <DialogContent>
        {categories.length === 0 ? (
          <Typography color="text.secondary" sx={{ py: 2 }}>
            Keine Kategorien vorhanden. Erstellen Sie zuerst Kategorien im Kategorien-Menü.
          </Typography>
        ) : (
          <Box sx={{ mt: 2 }}>
            <Box sx={{ mb: 2 }}>Wählen Sie die Kategorien für diesen Feed:</Box>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {categories.map((cat) => (
                <Chip
                  key={cat.id}
                  label={cat.name}
                  onClick={() => toggleCategory(cat.id)}
                  sx={{
                    backgroundColor: selectedCategories.includes(cat.id) ? cat.color : 'transparent',
                    color: selectedCategories.includes(cat.id) ? '#fff' : 'inherit',
                    borderColor: cat.color,
                    '&:hover': {
                      backgroundColor: selectedCategories.includes(cat.id)
                        ? cat.color
                        : `${cat.color}20`,
                    },
                  }}
                  variant={selectedCategories.includes(cat.id) ? 'filled' : 'outlined'}
                />
              ))}
            </Box>
          </Box>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button
          onClick={onSubmit}
          variant="contained"
          disabled={loading || categories.length === 0}
        >
          {loading ? <CircularProgress size={20} /> : 'Speichern'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
