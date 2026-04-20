import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Box,
  Typography,
  Chip,
  CircularProgress,
  Alert,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material'
import { useState } from 'react'
import { ICON_MAP } from '../views/CategoriesView'

const PRESET_COLORS = [
  '#667eea', // Lila
  '#764ba2', // Dunkles Lila
  '#f093fb', // Pink
  '#f5576c', // Rot
  '#4facfe', // Hellblau
  '#00f2fe', // Cyan
  '#43e97b', // Grün
  '#fa709a', // Rosa
  '#fee140', // Gelb
  '#30cfd0', // Türkis
  '#a8edea', // Mint
  '#fed6e3', // Hellrosa
  '#d299c2', // Mauve
  '#fef9d7', // Creme
  '#ffecd2', // Peach
]

interface AddCategoryDialogProps {
  open: boolean
  onClose: () => void
  onSubmit: (name: string, color: string, icon: string) => void
  loading: boolean
}

export function AddCategoryDialog({
  open,
  onClose,
  onSubmit,
  loading,
}: AddCategoryDialogProps) {
  const [name, setName] = useState('')
  const [color, setColor] = useState('#667eea')
  const [icon, setIcon] = useState('label')
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = () => {
    setError(null)
    if (!name.trim()) {
      setError('Name ist erforderlich')
      return
    }
    onSubmit(name.trim(), color, icon)
    // Reset nach erfolgreichem Submit
    setName('')
    setColor('#667eea')
    setIcon('label')
  }

  const handleClose = () => {
    setError(null)
    setName('')
    setColor('#667eea')
    setIcon('label')
    onClose()
  }

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>Neue Kategorie</DialogTitle>
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, mt: 1 }}>
          <TextField
            label="Name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            fullWidth
            required
            disabled={loading}
            autoFocus
          />

          <Box>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>
              Farbe
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {PRESET_COLORS.map((c) => (
                <Chip
                  key={c}
                  sx={{
                    backgroundColor: c,
                    width: 40,
                    height: 40,
                    borderRadius: '50%',
                    cursor: 'pointer',
                    border: color === c ? '3px solid #333' : '2px solid transparent',
                    '&:hover': {
                      opacity: 0.8,
                    },
                  }}
                  onClick={() => setColor(c)}
                />
              ))}
            </Box>
          </Box>

          <FormControl fullWidth disabled={loading}>
            <InputLabel id="icon-select-label">Icon</InputLabel>
            <Select
              labelId="icon-select-label"
              value={icon}
              label="Icon"
              onChange={(e) => setIcon(e.target.value)}
            >
              {Object.entries(ICON_MAP).sort(([a], [b]) => a.localeCompare(b)).map(([name, IconComponent]) => (
                <MenuItem key={name} value={name}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <IconComponent />
                    <Typography sx={{ textTransform: 'capitalize' }}>
                      {name}
                    </Typography>
                  </Box>
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} disabled={loading}>
          Abbrechen
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={loading}
          startIcon={loading ? <CircularProgress size={20} /> : undefined}
        >
          {loading ? 'Erstellen...' : 'Erstellen'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
