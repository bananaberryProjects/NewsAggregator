import { Box, Grid } from '@mui/material'
import { WeatherWidget, StockWidget, AiSummaryWidget, CryptoPriceWidget, MorningBriefingWidget } from '../widgets'

export function DashboardView() {
  return (
    <Box>
      {/* 1. Morning Briefing — personalisierte Begrüßung + Stats + Quick Actions */}
      <MorningBriefingWidget />

      {/* 2. KI Tagesüberblick */}
      <AiSummaryWidget />

      {/* 3. Wetter, Börse, Krypto */}
      <Grid container spacing={3} mt={1}>
        <Grid size={{ xs: 12, md: 4 }}>
          <WeatherWidget />
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <StockWidget />
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <CryptoPriceWidget />
        </Grid>
      </Grid>
    </Box>
  )
}
