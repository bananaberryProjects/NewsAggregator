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
  Typography,
} from '@mui/material'
import { useState } from 'react'
import type { Category } from '../../api/client'

interface AddFeedDialogProps {
  open: boolean
  onClose: () => void
  onSubmit: () => void
  url: string
  setUrl: (url: string) => void
  name: string
  setName: (name: string) => void
  blockedKeywords: string[]
  setBlockedKeywords: (keywords: string[]) => void
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
  blockedKeywords,
  setBlockedKeywords,
  selectedCategories,
  toggleCategory,
  categories,
  loading,
  error,
}: AddFeedDialogProps) {
  const [keywordInput, setKeywordInput] = useState('')

  const handleAddKeyword = () => {
    if (keywordInput.trim()) {
      const kw = keywordInput.trim().toLowerCase()
      if (!blockedKeywords.includes(kw)) {
        setBlockedKeywords([...blockedKeywords, kw])
      }
      setKeywordInput('')
    }
  }

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
        <Box sx={{ mt: 2 }}>
          <Typography variant="subtitle2" sx={{ mb: 1 }}>
            Blockierte Keywords
          </Typography>
          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 1 }}>
            Artikel mit diesen Keywords im Titel werden beim Abruf ignoriert
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, mb: 1 }}>
            <TextField
              value={keywordInput}
              onChange={(e) => setKeywordInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && keywordInput.trim()) {
                  e.preventDefault()
                  handleAddKeyword()
                }
              }}
              placeholder="z.B. werbung, anzeige, sponsored"
              size="small"
              fullWidth
            />
            <Button
              variant="outlined"
              size="small"
              onClick={handleAddKeyword}
              disabled={!keywordInput.trim()}
            >
              + Hinzufügen
            </Button>
          </Box>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
            {blockedKeywords.length === 0 ? (
              <Typography variant="body2" color="text.secondary">
                Keine blockierten Keywords
              </Typography>
            ) : (
              blockedKeywords.map((kw, index) => (
                <Chip
                  key={index}
                  label={kw}
                  onDelete={() => setBlockedKeywords(blockedKeywords.filter((_, i) => i !== index))}
                  sx={{
                    backgroundColor: 'error.light',
                    color: 'error.contrastText',
                    '& .MuiChip-deleteIcon': {
                      color: 'error.contrastText',
                    },
                  }}
                  size="small"
                />
              ))
            )}
          </Box>
        </Box>
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
