import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Alert,
  CircularProgress,
} from '@mui/material'
import type { Feed } from '../../api/client'

interface DeleteFeedDialogProps {
  open: boolean
  onClose: () => void
  onConfirm: () => void
  feed: Feed | null
  loading: boolean
  error: string | null
}

export function DeleteFeedDialog({
  open,
  onClose,
  onConfirm,
  feed,
  loading,
  error,
}: DeleteFeedDialogProps) {
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Feed löschen</DialogTitle>
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        <Typography>
          Möchtest du den Feed "{feed?.name}" wirklich löschen?
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          Alle zugehörigen Artikel werden ebenfalls gelöscht.
        </Typography>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button onClick={onConfirm} color="error" variant="contained" disabled={loading}>
          {loading ? <CircularProgress size={20} /> : 'Löschen'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
