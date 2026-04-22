import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
  IconButton,
  Snackbar,
  Alert,
  Box,
  Typography
} from '@mui/material';
import AddToHomeScreenIcon from '@mui/icons-material/AddToHomeScreen';
import CloseIcon from '@mui/icons-material/Close';
import DownloadIcon from '@mui/icons-material/Download';

// TypeScript Types für die beforeinstallprompt API
interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

export default function PWAInstallPrompt() {
  const [deferredPrompt, setDeferredPrompt] = useState<BeforeInstallPromptEvent | null>(null);
  const [showDialog, setShowDialog] = useState(false);
  const [showSnack, setShowSnack] = useState(false);
  const [snackMessage, setSnackMessage] = useState('');
  const [isStandalone, setIsStandalone] = useState(false);

  useEffect(() => {
    // Prüfen ob App bereits als PWA installiert ist
    const checkStandalone = () => {
      const isInStandaloneMode = 
        window.matchMedia('(display-mode: standalone)').matches || 
        (window.navigator as Navigator & { standalone?: boolean }).standalone === true;
      setIsStandalone(isInStandaloneMode);
    };

    checkStandalone();

    // Event Listener für das Install-Prompt
    const handleBeforeInstallPrompt = (e: Event) => {
      // Standard-Verhalten verhindern
      e.preventDefault();
      // Event speichern
      setDeferredPrompt(e as BeforeInstallPromptEvent);
      
      // Prüfen ob Benutzer das bereits abgelehnt hat
      const promptDismissed = localStorage.getItem('pwa-prompt-dismissed');
      const promptDate = localStorage.getItem('pwa-prompt-date');
      
      if (promptDismissed === 'true' && promptDate) {
        const daysSinceDismissed = (Date.now() - parseInt(promptDate)) / (1000 * 60 * 60 * 24);
        // Nach 7 Tagen erneut fragen
        if (daysSinceDismissed < 7) {
          return;
        }
      }

      // Dialog nach kurzer Verzögerung zeigen
      setTimeout(() => {
        setShowDialog(true);
      }, 3000);
    };

    // Event Listener für App-Installation
    const handleAppInstalled = () => {
      setSnackMessage('NewsWeave wurde erfolgreich installiert! 🎉');
      setShowSnack(true);
      setDeferredPrompt(null);
      setShowDialog(false);
      localStorage.removeItem('pwa-prompt-dismissed');
      localStorage.removeItem('pwa-prompt-date');
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    window.addEventListener('appinstalled', handleAppInstalled);

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
      window.removeEventListener('appinstalled', handleAppInstalled);
    };
  }, []);

  const handleInstall = async () => {
    if (!deferredPrompt) return;

    // Install-Prompt anzeigen
    deferredPrompt.prompt();

    // Auf Benutzer-Antwort warten
    const { outcome } = await deferredPrompt.userChoice;

    if (outcome === 'accepted') {
      setSnackMessage('Installation wird durchgeführt...');
      setShowSnack(true);
    } else {
      // Benutzer hat abgelehnt
      localStorage.setItem('pwa-prompt-dismissed', 'true');
      localStorage.setItem('pwa-prompt-date', Date.now().toString());
      setSnackMessage('Du kannst jederzeit über das Menü installieren.');
      setShowSnack(true);
    }

    setDeferredPrompt(null);
    setShowDialog(false);
  };

  const handleDismiss = () => {
    setShowDialog(false);
    localStorage.setItem('pwa-prompt-dismissed', 'true');
    localStorage.setItem('pwa-prompt-date', Date.now().toString());
  };

  // Wenn bereits als PWA installiert oder kein Prompt verfügbar, nichts anzeigen
  if (isStandalone) return null;

  return (
    <>
      {/* Install-Button in der App Bar (immer sichtbar wenn verfügbar) */}
      {deferredPrompt && (
        <IconButton
          color="inherit"
          onClick={() => setShowDialog(true)}
          title="NewsWeave installieren"
          sx={{ ml: 1 }}
        >
          <AddToHomeScreenIcon />
        </IconButton>
      )}

      {/* Install Dialog */}
      <Dialog
        open={showDialog}
        onClose={handleDismiss}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <DownloadIcon color="primary" />
          NewsWeave App installieren
          <IconButton
            aria-label="close"
            onClick={handleDismiss}
            sx={{ position: 'absolute', right: 8, top: 8 }}
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
            <img 
              src="/newsweave-192x192.png" 
              alt="NewsWeave Logo" 
              style={{ width: 80, height: 80, marginBottom: 16 }}
            />
            <Typography variant="h6" gutterBottom>
              Installiere NewsWeave
            </Typography>
          </Box>
          <DialogContentText>
            Installiere NewsWeave als App auf deinem Gerät für:
          </DialogContentText>
          <Box component="ul" sx={{ mt: 1, pl: 3 }}>
            <Typography component="li" variant="body2" color="text.secondary">
              ⚡ Schnelleren Zugriff direkt vom Startbildschirm
            </Typography>
            <Typography component="li" variant="body2" color="text.secondary">
              📱 Offline-News lesen (wenn vorher geladen)
            </Typography>
            <Typography component="li" variant="body2" color="text.secondary">
              🔔 Bald verfügbar: Push-Benachrichtungen
            </Typography>
          </Box>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button onClick={handleDismiss} color="inherit">
            Später
          </Button>
          <Button 
            onClick={handleInstall} 
            variant="contained" 
            startIcon={<AddToHomeScreenIcon />}
            autoFocus
          >
            Installieren
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar für Benachrichtigungen */}
      <Snackbar
        open={showSnack}
        autoHideDuration={4000}
        onClose={() => setShowSnack(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert 
          severity="success" 
          onClose={() => setShowSnack(false)}
          sx={{ width: '100%' }}
        >
          {snackMessage}
        </Alert>
      </Snackbar>
    </>
  );
}
