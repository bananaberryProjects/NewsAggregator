import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Chip,
  Alert,
  Box,
  CircularProgress,
} from '@mui/material'
import type { Category } from '../../api/client'

interface AddFeedDialogProps {
  open: boolean
  onClose: () => void
  onSubmit: () => void
  url: string
  setUrl: (url: string) => void
  name: string
  setName: (name: string) => void
  selectedCategories: string[]
  toggleCategory: (id: string) => void
  categories: Category[]
  loading: boolean
  error: string | null
}

export function AddFeedDialog({
  open,
  onClose,
  onSubmit,
  url,
  setUrl,
  name,
  setName,
  selectedCategories,
  toggleCategory,
  categories,
  loading,
  error,
}: AddFeedDialogProps) {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Feed hinzufügen</DialogTitle>
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        <TextField
          label="Feed URL"
          fullWidth
          margin="normal"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          placeholder="https://example.com/feed.xml"
        />
        <TextField
          label="Name (optional)"
          fullWidth
          margin="normal"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Mein Feed"
        />
        {categories.length > 0 && (
          <Box sx={{ mt: 2 }}>
            <Box sx={{ mb: 1 }}>Kategorien:</Box>
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
        <Button onClick={onSubmit} variant="contained" disabled={loading || !url.trim()}>
          {loading ? <CircularProgress size={20} /> : 'Hinzufügen'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
