import { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  LinearProgress,
  Alert,
  Slider,
} from '@mui/material';
import {
  CloudDownload as CloudDownloadIcon,
} from '@mui/icons-material';
import { adminApi } from '../api/client';

interface ContentExtractionDialogProps {
  open: boolean;
  onClose: () => void;
  articlesWithoutContent: number;
  onExtractionComplete: () => void;
}

export function ContentExtractionDialog({
  open,
  onClose,
  articlesWithoutContent,
  onExtractionComplete,
}: ContentExtractionDialogProps) {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<{
    success: boolean;
    message: string;
    processedCount: number;
    successCount: number;
    failedCount: number;
  } | null>(null);
  const [limit, setLimit] = useState(50);
  const [delayMs, setDelayMs] = useState(2000);
  const [error, setError] = useState<string | null>(null);

  const handleExtract = async () => {
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await adminApi.extractContent(limit, delayMs);

      setResult({
        success: response.success,
        message: response.message,
        processedCount: response.processedCount,
        successCount: response.successCount,
        failedCount: response.failedCount,
      });

      onExtractionComplete();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Ein Fehler ist aufgetreten');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setResult(null);
      setError(null);
      onClose();
    }
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>Content für bestehende Artikel extrahieren</DialogTitle>

      <DialogContent>
        {articlesWithoutContent === 0 && !result ? (
          <Alert severity="info" sx={{ mb: 2 }}>
            Alle Artikel haben bereits extrahierten Content. Es gibt nichts zu tun!
          </Alert>
        ) : (
          <>
            <Typography variant="body1" sx={{ mb: 2 }}>
              Es gibt <strong>{articlesWithoutContent}</strong> Artikel ohne extrahierten Content.
            </Typography>

            {!result && !loading && (
              <>
                <Box sx={{ mb: 3 }}>
                  <Typography variant="subtitle2" gutterBottom>
                    Maximale Anzahl: {limit} Artikel
                  </Typography>
                  <Slider
                    value={limit}
                    onChange={(_, value) => setLimit(value as number)}
                    min={1}
                    max={100}
                    step={1}
                    marks={[
                      { value: 1, label: '1' },
                      { value: 25, label: '25' },
                      { value: 50, label: '50' },
                      { value: 75, label: '75' },
                      { value: 100, label: '100' },
                    ]}
                    valueLabelDisplay="auto"
                  />
                </Box>

                <Box sx={{ mb: 3 }}>
                  <Typography variant="subtitle2" gutterBottom>
                    Pause zwischen Requests: {delayMs}ms
                  </Typography>
                  <Slider
                    value={delayMs}
                    onChange={(_, value) => setDelayMs(value as number)}
                    min={500}
                    max={5000}
                    step={500}
                    marks={[
                      { value: 500, label: '0.5s' },
                      { value: 1000, label: '1s' },
                      { value: 2000, label: '2s' },
                      { value: 3000, label: '3s' },
                      { value: 5000, label: '5s' },
                    ]}
                    valueLabelDisplay="auto"
                  />
                </Box>

                <Alert severity="info" sx={{ mb: 2 }}>
                  Hinweis: Die Extraktion kann einige Zeit dauern. Die Pause zwischen
                  Requests verhindert Rate-Limiting.
                </Alert>
              </>
            )}

            {loading && (
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" sx={{ mb: 1 }}>
                  Extrahiere Content... Bitte warten.
                </Typography>
                <LinearProgress />
              </Box>
            )}

            {error && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {error}
              </Alert>
            )}

            {result && (
              <Box>
                <Alert severity={result.success ? 'success' : 'warning'} sx={{ mb: 2 }}>
                  {result.message}
                </Alert>

                <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                  <Box sx={{ textAlign: 'center', flex: 1 }}>
                    <Typography variant="h6" color="primary">
                      {result.processedCount}
                    </Typography>
                    <Typography variant="caption">Verarbeitet</Typography>
                  </Box>
                  <Box sx={{ textAlign: 'center', flex: 1 }}>
                    <Typography variant="h6" color="success.main">
                      {result.successCount}
                    </Typography>
                    <Typography variant="caption">Erfolgreich</Typography>
                  </Box>
                  <Box sx={{ textAlign: 'center', flex: 1 }}>
                    <Typography variant="h6" color="error.main">
                      {result.failedCount}
                    </Typography>
                    <Typography variant="caption">Fehlgeschlagen</Typography>
                  </Box>
                </Box>

                {articlesWithoutContent - result.processedCount > 0 && (
                  <Alert severity="info" sx={{ mt: 2 }}>
                    Noch {articlesWithoutContent - result.processedCount} Artikel ohne Content.
                    Klicken Sie erneut auf "Extrahieren", um fortzufahren.
                  </Alert>
                )}
              </Box>
            )}
          </>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} disabled={loading}>
          {result ? 'Schließen' : 'Abbrechen'}
        </Button>
        {articlesWithoutContent > 0 && !result && (
          <Button
            variant="contained"
            onClick={handleExtract}
            disabled={loading}
            startIcon={<CloudDownloadIcon />}
          >
            Extrahieren
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
}
