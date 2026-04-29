import { Box, Grid } from '@mui/material'
import { WeatherWidget, StockWidget, AiSummaryWidget, CryptoPriceWidget, MorningBriefingWidget } from '../widgets'

export function DashboardView() {
  return (
    <Box>
      {/* Morning Briefing — personalisierte Begrüßung + Stats + Quick Actions */}
      <MorningBriefingWidget />

      {/* Klassische Widgets: Wetter, Börsen, Krypto */}
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
          <AiSummaryWidget />
        </Grid>
      </Grid>
    </Box>
  )
}
