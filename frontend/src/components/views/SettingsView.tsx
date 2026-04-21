import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Chip,
  Alert,
  Divider,
} from '@mui/material'
import {
  CloudDownload as CloudDownloadIcon,
  Storage as StorageIcon,
} from '@mui/icons-material'

interface SettingsViewProps {
  articlesWithoutContent: number
  onOpenExtractionDialog: () => void
}

export function SettingsView({
  articlesWithoutContent,
  onOpenExtractionDialog,
}: SettingsViewProps) {
  return (
    <Box sx={{ maxWidth: 800, mx: 'auto', py: 4 }}>
      <Typography variant="h4" sx={{ mb: 4, fontWeight: 600 }}>
        Einstellungen
      </Typography>

      {/* Content Extraction Card */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
            <StorageIcon color="primary" />
            <Typography variant="h6">Content-Extraktion</Typography>
          </Box>

          <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
            Extrahiere den vollständigen Artikelinhalt für bestehende Artikel.
            Dies ermöglicht die Anzeige im Reader-Modus ohne externe Links.
          </Typography>

          <Alert severity="info" sx={{ mb: 3 }}>
            Es gibt aktuell{' '}
            <strong>{articlesWithoutContent} Artikel</strong> ohne extrahierten
            Content.
          </Alert>

          <Button
            variant="contained"
            startIcon={<CloudDownloadIcon />}
            onClick={onOpenExtractionDialog}
            disabled={articlesWithoutContent === 0}
          >
            Content für bestehende Artikel extrahieren
          </Button>

          {articlesWithoutContent === 0 && (
            <Typography
              variant="caption"
              color="success.main"
              sx={{ display: 'block', mt: 1 }}
            >
              ✓ Alle Artikel haben bereits extrahierten Content
            </Typography>
          )}
        </CardContent>
      </Card>

      <Divider sx={{ my: 3 }} />

      {/* Info */}
      <Typography variant="body2" color="text.secondary">
        Weitere Einstellungen folgen in zukünftigen Versionen.
      </Typography>
    </Box>
  )
}
