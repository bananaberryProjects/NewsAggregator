import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Box,
  Chip,
  Typography,
  CircularProgress,
  Alert,
} from '@mui/material'
import { useState, useEffect } from 'react'
import type { Feed, Category } from '../../api/client'

interface EditFeedDialogProps {
  open: boolean
  onClose: () => void
  onSubmit: (name: string, url: string, description: string, categoryIds: string[]) => void
  feed: Feed | null
  loading: boolean
  categories: Category[]
  selectedCategories: string[]
  onToggleCategory: (id: string) => void
}

export function EditFeedDialog({
  open,
  onClose,
  onSubmit,
  feed,
  loading,
  categories,
  selectedCategories,
  onToggleCategory,
}: EditFeedDialogProps) {
  const [name, setName] = useState('')
  const [url, setUrl] = useState('')
  const [description, setDescription] = useState('')
  const [errors, setErrors] = useState<{ name?: string; url?: string }>({})
  const [submitError, setSubmitError] = useState<string | null>(null)

  useEffect(() => {
    if (open && feed) {
      setName(feed.name)
      setUrl(feed.url)
      setDescription(feed.description || '')
      setErrors({})
      setSubmitError(null)
    }
  }, [open, feed])

  const validate = () => {
    const newErrors: { name?: string; url?: string } = {}
    if (!name.trim()) {
      newErrors.name = 'Name ist erforderlich'
    }
    if (!url.trim()) {
      newErrors.url = 'URL ist erforderlich'
    } else {
      try {
        new URL(url)
      } catch {
        newErrors.url = 'Bitte geben Sie eine gültige URL ein'
      }
    }
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = () => {
    setSubmitError(null)
    if (validate()) {
      try {
        onSubmit(name.trim(), url.trim(), description.trim(), selectedCategories)
      } catch (error) {
        setSubmitError(error instanceof Error ? error.message : 'Ein Fehler ist aufgetreten')
      }
    }
  }

  const handleClose = () => {
    setSubmitError(null)
    setErrors({})
    onClose()
  }

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        Feed bearbeiten
        {feed && (
          <Box component="span" sx={{ display: 'block', fontSize: '0.875rem', color: 'text.secondary', mt: 0.5 }}>
            {feed.name}
          </Box>
        )}
      </DialogTitle>
      <DialogContent>
        {submitError && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {submitError}
          </Alert>
        )}
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, mt: 1 }}>
          <TextField
            label="Name"
            value={name}
            onChange={(e) => {
              setName(e.target.value)
              if (errors.name) setErrors((prev) => ({ ...prev, name: undefined }))
            }}
            error={!!errors.name}
            helperText={errors.name}
            fullWidth
            required
            disabled={loading}
          />
          <TextField
            label="URL"
            value={url}
            onChange={(e) => {
              setUrl(e.target.value)
              if (errors.url) setErrors((prev) => ({ ...prev, url: undefined }))
            }}
            error={!!errors.url}
            helperText={errors.url || 'Die Feed-URL (RSS/Atom)'}
            fullWidth
            required
            disabled={loading}
          />
          <TextField
            label="Beschreibung"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            multiline
            rows={2}
            fullWidth
            disabled={loading}
            placeholder="Optionale Beschreibung des Feeds"
          />
          
          {/* Kategorien-Auswahl */}
          <Box>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>
              Kategorien
            </Typography>
            {categories.length === 0 ? (
              <Typography color="text.secondary" variant="body2">
                Keine Kategorien vorhanden. Erstellen Sie zuerst Kategorien im Kategorien-Menü.
              </Typography>
            ) : (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {categories.map((cat) => (
                  <Chip
                    key={cat.id}
                    label={cat.name}
                    onClick={() => onToggleCategory(cat.id)}
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
            )}
          </Box>
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
          {loading ? 'Speichern...' : 'Speichern'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
