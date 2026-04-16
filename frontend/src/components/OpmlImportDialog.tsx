import React, { useState, useRef } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  Alert,
  CircularProgress,
} from '@mui/material';
import UploadFileIcon from '@mui/icons-material/UploadFile';

interface OpmlImportDialogProps {
  open: boolean;
  onClose: () => void;
  onImport: () => void;
}

export const OpmlImportDialog: React.FC<OpmlImportDialogProps> = ({
  open,
  onClose,
  onImport,
}) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      if (file.name.endsWith('.opml') || file.name.endsWith('.xml')) {
        setSelectedFile(file);
        setError(null);
      } else {
        setError('Bitte wählen Sie eine gültige OPML-Datei (.opml oder .xml)');
        setSelectedFile(null);
      }
    }
  };

  const handleImport = async () => {
    if (!selectedFile) {
      setError('Bitte wählen Sie eine Datei aus');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append('file', selectedFile);

      const response = await fetch('/api/opml/import', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Import failed');
      }

      setSelectedFile(null);
      onImport();
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Fehler beim Importieren der OPML-Datei');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setSelectedFile(null);
      setError(null);
      onClose();
    }
  };

  const handleDrop = (event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    const file = event.dataTransfer.files[0];
    if (file) {
      if (file.name.endsWith('.opml') || file.name.endsWith('.xml')) {
        setSelectedFile(file);
        setError(null);
      } else {
        setError('Bitte wählen Sie eine gültige OPML-Datei (.opml oder .xml)');
      }
    }
  };

  const handleDragOver = (event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>OPML Import</DialogTitle>
      <DialogContent>
        <Box
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          sx={{
            border: '2px dashed',
            borderColor: selectedFile ? 'primary.main' : 'grey.400',
            borderRadius: 2,
            p: 3,
            textAlign: 'center',
            cursor: 'pointer',
            backgroundColor: selectedFile ? 'action.hover' : 'background.paper',
            '&:hover': {
              backgroundColor: 'action.hover',
            },
          }}
          onClick={() => fileInputRef.current?.click()}
        >
          <input
            type="file"
            accept=".opml,.xml"
            hidden
            ref={fileInputRef}
            onChange={handleFileSelect}
          />
          <UploadFileIcon sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
          <Typography variant="h6" gutterBottom>
            {selectedFile ? selectedFile.name : 'OPML-Datei hierher ziehen oder klicken zum Auswählen'}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Unterstützte Formate: .opml, .xml
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} disabled={loading}>
          Abbrechen
        </Button>
        <Button
          onClick={handleImport}
          variant="contained"
          disabled={!selectedFile || loading}
          startIcon={loading ? <CircularProgress size={20} /> : undefined}
        >
          {loading ? 'Importiere...' : 'Importieren'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
