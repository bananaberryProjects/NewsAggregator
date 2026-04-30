import { Box, Grid } from '@mui/material'
import { WeatherWidget, MarketWidget, AiSummaryWidget, MorningBriefingWidget } from '../widgets'

export function DashboardView() {
  return (
    <Box>
      {/* 1. Morning Briefing */}
      <MorningBriefingWidget />

      {/* 2. KI Tagesüberblick */}
      <AiSummaryWidget />

      {/* 3. Wetter, Markt */}
      <Box sx={{ mt: 1 }}>
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 6 }}>
            <WeatherWidget />
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <MarketWidget />
          </Grid>
        </Grid>
      </Box>
    </Box>
  )
}
