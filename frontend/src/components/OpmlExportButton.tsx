import React, { useState } from 'react';
import { Button, CircularProgress, Tooltip } from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import { API_BASE_URL } from '../api/client';

export const OpmlExportButton: React.FC = () => {
  const [loading, setLoading] = useState(false);

  const handleExport = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/opml/export`, {
        method: 'GET',
        headers: {
          'Accept': 'application/xml',
        },
      });

      if (!response.ok) {
        throw new Error('Export failed');
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `news-aggregator-feeds-${new Date().toISOString().split('T')[0]}.opml`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error exporting OPML:', error);
      alert('Fehler beim Exportieren der OPML-Datei');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Tooltip title="Feeds als OPML exportieren">
      <Button
        variant="outlined"
        startIcon={loading ? <CircularProgress size={20} /> : <DownloadIcon />}
        onClick={handleExport}
        disabled={loading}
      >
        Export
      </Button>
    </Tooltip>
  );
};
