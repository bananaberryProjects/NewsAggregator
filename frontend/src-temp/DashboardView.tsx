import { Box, Grid } from '@mui/material'
import { WeatherWidget, StockWidget, SummaryWidget } from '../widgets'

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

        {/* KI Summary Widget */}
        <Grid size={{ xs: 12 }}>
          <SummaryWidget />
        </Grid>
      </Grid>
    </Box>
  )
}
