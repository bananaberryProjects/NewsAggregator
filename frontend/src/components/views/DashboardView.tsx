import { Box, Grid } from '@mui/material'
import { WeatherWidget, MarketWidget, AiSummaryWidget, MorningBriefingWidget, TrendingWidget } from '../widgets'

export function DashboardView() {
  return (
    <Box>
      {/* 1. Morning Briefing */}
      <MorningBriefingWidget />

      {/* 2. KI Tagesüberblick */}
      <AiSummaryWidget />

      {/* 3. Trending-Themen */}
      <TrendingWidget />

      {/* 4. Wetter, Markt */}
      <Box sx={{ mt: 1 }}>
        <Grid container spacing={3} sx={{ alignItems: 'stretch' }}>
          <Grid size={{ xs: 12, md: 6 }} sx={{ display: 'flex' }}>
            <Box sx={{ width: '100%', display: 'flex', flexDirection: 'column' }}>
              <WeatherWidget />
            </Box>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }} sx={{ display: 'flex' }}>
            <Box sx={{ width: '100%', display: 'flex', flexDirection: 'column' }}>
              <MarketWidget />
            </Box>
          </Grid>
        </Grid>
      </Box>
    </Box>
  )
}
