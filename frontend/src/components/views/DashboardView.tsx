import { Box, Card, CardContent, Grid, Typography } from '@mui/material'
import { SmartToy as SmartToyIcon } from '@mui/icons-material'
import { WeatherWidget, StockWidget } from '../widgets'

export function DashboardView() {
  return (
    <Box>
      {/* Widgets Section */}
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 6 }}>
          <WeatherWidget />
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <StockWidget />
        </Grid>

        {/* AI Summary Placeholder – will be replaced with summary widget */}
        <Grid size={{ xs: 12 }}>
          <Card sx={{ minHeight: 200, display: 'flex', flexDirection: 'column' }}>
            <CardContent sx={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
              <SmartToyIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
              <Typography variant="h6" color="text.secondary" gutterBottom>
                KI-Zusammenfassung
              </Typography>
              <Typography variant="body2" color="text.secondary" textAlign="center">
                Hier wird bald eine intelligente Zusammenfassung Ihrer neuesten Artikel angezeigt.
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  )
}
