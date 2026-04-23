import { Box, Grid } from '@mui/material'
import { WeatherWidget, StockWidget, SummaryWidget, CryptoPriceWidget } from '../widgets'

export function DashboardView() {
  return (
    <Box>
      {/* Widgets Section - alle 3 nebeneinander */}
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 4 }}>
          <WeatherWidget />
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <StockWidget />
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <CryptoPriceWidget />
        </Grid>

        {/* KI Summary Widget */}
        <Grid size={{ xs: 12 }}>
          <SummaryWidget />
        </Grid>
      </Grid>
    </Box>
  )
}
