import React, { useEffect, useState } from 'react'
import { Card, CardContent, Typography, Box, Skeleton, Alert, IconButton } from '@mui/material'
import { Refresh } from '@mui/icons-material'

export function SummaryWidget({ refreshIntervalSeconds = 600 }: { refreshIntervalSeconds?: number }) {
  const [summary, setSummary] = useState<string>('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [lastUpdate, setLastUpdate] = useState<Date | null>(null)

  const fetchSummary = async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await fetch('/api/summary')
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const data = await response.json()
      setSummary(data.summary || '')
      setLastUpdate(new Date())
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Fehler beim Laden der Zusammenfassung')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchSummary()
    const interval = setInterval(fetchSummary, refreshIntervalSeconds * 1000)
    return () => clearInterval(interval)
  }, [refreshIntervalSeconds])

  const formatTime = (date: Date) =>
    date.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })

  return (
    <Card sx={{ minHeight: 200, display: 'flex', flexDirection: 'column' }}>
      <CardContent sx={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
        {loading && !summary ? (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Skeleton width={200} height={24} />
            <Skeleton width={150} height={20} />
          </Box>
        ) : error ? (
          <Alert severity="error">{error}</Alert>
        ) : (
          <Typography variant="body1" textAlign="center">
            {summary}
          </Typography>
        )}
        {lastUpdate && (
          <Typography variant="caption" sx={{ mt: 1 }}>
            Aktualisiert: {formatTime(lastUpdate)}
          </Typography>
        )}
      </CardContent>
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', p: 1 }}>
        <IconButton size="small" onClick={fetchSummary} disabled={loading}>
          <Refresh />
        </IconButton>
      </Box>
    </Card>
  )
}
