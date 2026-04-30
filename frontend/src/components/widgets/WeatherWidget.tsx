import { useEffect, useState, useCallback } from 'react'
import {
  Card, CardContent, Typography, Box, Skeleton, Alert, Avatar, Divider
} from '@mui/material'
import {
  Refresh, WbSunny, Cloud, CloudOff, Opacity, AcUnit, Thunderstorm,
  Psychology as PsychologyIcon, LocationOn
} from '@mui/icons-material'
import { IconButton, Tooltip, Fade } from '@mui/material'
import type { WeatherInsight, WeatherForecastDay } from '../../api/client'
import { weatherApi } from '../../api/client'

interface LocationConfig {
  name: string
  lat: number
  lon: number
}

const STORAGE_KEY = 'weather-location-config'

const DEFAULT_LOCATIONS: LocationConfig[] = [
  { name: 'Berlin', lat: 52.52, lon: 13.41 },
  { name: 'München', lat: 48.14, lon: 11.58 },
  { name: 'Hamburg', lat: 53.55, lon: 9.99 },
  { name: 'Köln', lat: 50.94, lon: 6.96 },
  { name: 'Frankfurt', lat: 50.11, lon: 8.68 },
  { name: 'Stuttgart', lat: 48.78, lon: 9.18 },
  { name: 'Düsseldorf', lat: 51.23, lon: 6.78 },
  { name: 'Leipzig', lat: 51.34, lon: 12.37 },
  { name: 'Dresden', lat: 51.05, lon: 13.74 },
  { name: 'Nürnberg', lat: 49.45, lon: 11.08 },
]

function loadLocation(): LocationConfig {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (raw) {
    try {
      const parsed = JSON.parse(raw)
      if (parsed.name && typeof parsed.lat === 'number' && typeof parsed.lon === 'number') {
        return parsed
      }
    } catch {}
  }
  return DEFAULT_LOCATIONS[0]
}

const getWeatherTheme = (code: number) => {
  if (code === 0) {
    return { gradient: 'linear-gradient(135deg, #FF9800 0%, #FFB300 50%, #FFCA28 100%)', iconBg: '#FFF3E0' }
  }
  if (code >= 1 && code <= 3) {
    return { gradient: 'linear-gradient(135deg, #78909C 0%, #90A4AE 50%, #B0BEC5 100%)', iconBg: '#ECEFF1' }
  }
  if (code >= 45 && code <= 48) {
    return { gradient: 'linear-gradient(135deg, #546E7A 0%, #78909C 100%)', iconBg: '#CFD8DC' }
  }
  if (code >= 51 && code <= 57) {
    return { gradient: 'linear-gradient(135deg, #42A5F5 0%, #64B5F6 100%)', iconBg: '#E3F2FD' }
  }
  if (code >= 61 && code <= 67) {
    return { gradient: 'linear-gradient(135deg, #1E88E5 0%, #42A5F5 100%)', iconBg: '#BBDEFB' }
  }
  if (code >= 71 && code <= 77) {
    return { gradient: 'linear-gradient(135deg, #4FC3F7 0%, #81D4FA 100%)', iconBg: '#E1F5FE' }
  }
  if (code >= 80 && code <= 82) {
    return { gradient: 'linear-gradient(135deg, #1565C0 0%, #1976D2 100%)', iconBg: '#90CAF9' }
  }
  if (code >= 95) {
    return { gradient: 'linear-gradient(135deg, #5C6BC0 0%, #7986CB 50%, #9FA8DA 100%)', iconBg: '#C5CAE9' }
  }
  return { gradient: 'linear-gradient(135deg, #FF9800 0%, #FFB300 100%)', iconBg: '#FFF3E0' }
}

const getWeatherIcon = (code: number) => {
  if (code === 0) return WbSunny
  if (code >= 1 && code <= 3) return Cloud
  if (code >= 45 && code <= 48) return CloudOff
  if (code >= 51 && code <= 67) return Opacity
  if (code >= 71 && code <= 77) return AcUnit
  if (code >= 95) return Thunderstorm
  return Cloud
}

function WeatherIconComponent({ code, size = 48 }: { code: number; size?: number }) {
  const Icon = getWeatherIcon(code)
  return <Icon sx={{ fontSize: size }} />
}

function getWeatherDescription(code: number): string {
  if (code === 0) return 'Klarer Himmel'
  if (code === 1) return 'Hauptsächlich klar'
  if (code === 2) return 'Teilweise bewölkt'
  if (code === 3) return 'Bedeckt'
  if (code >= 45 && code <= 48) return 'Nebelig'
  if (code >= 51 && code <= 55) return 'Nieselregen'
  if (code >= 61 && code <= 65) return 'Regen'
  if (code >= 71 && code <= 77) return 'Schneefall'
  if (code >= 80 && code <= 82) return 'Regenschauer'
  if (code >= 95) return 'Gewitter'
  return 'Unbekannt'
}

export function WeatherWidget() {
  const [weather, setWeather] = useState<WeatherInsight | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [mounted, setMounted] = useState(false)
  const [location, setLocation] = useState<LocationConfig>(loadLocation)

  useEffect(() => {
    const timer = setTimeout(() => setMounted(true), 50)
    return () => clearTimeout(timer)
  }, [])

  const fetchWeather = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await weatherApi.getInsight(location.lat, location.lon, location.name)
      setWeather(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Wetterdaten konnten nicht geladen werden')
    } finally {
      setLoading(false)
    }
  }, [location])

  useEffect(() => {
    fetchWeather()
  }, [fetchWeather])

  // Subscribe to location changes from settings
  useEffect(() => {
    const handler = () => {
      setLocation(loadLocation())
    }
    window.addEventListener('weather-location-changed', handler)
    return () => window.removeEventListener('weather-location-changed', handler)
  }, [])

  const theme = weather ? getWeatherTheme(weather.weatherCode) : getWeatherTheme(0)

  return (
    <Fade in={mounted} timeout={500}>
      <Card
        sx={{
          borderRadius: 3,
          overflow: 'hidden',
          background: theme.gradient,
          color: '#fff',
          position: 'relative',
          boxShadow: (t) => t.shadows[4],
        }}
      >
        <CardContent sx={{ p: 3, pb: '16px !important' }}>
          {/* Header */}
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Avatar sx={{ width: 40, height: 40, bgcolor: 'rgba(255,255,255,0.2)', color: '#fff' }}>
                <LocationOn sx={{ fontSize: 22 }} />
              </Avatar>
              <Box>
                <Typography variant="h6" sx={{ fontWeight: 600, lineHeight: 1.2, color: '#fff' }}>
                  Wetter
                </Typography>
                {weather && (
                  <Typography variant="caption" sx={{ opacity: 0.85 }}>
                    {weather.city}
                  </Typography>
                )}
              </Box>
            </Box>
            <Tooltip title="Aktualisieren">
              <IconButton
                size="small"
                onClick={fetchWeather}
                disabled={loading}
                sx={{ color: '#fff' }}
              >
                <Refresh sx={{ fontSize: 20, ...(loading && { animation: 'spin 1s linear infinite' }) }} />
              </IconButton>
            </Tooltip>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 2, bgcolor: 'rgba(255,255,255,0.9)' }}>
              {error}
            </Alert>
          )}

          {loading ? (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
              <Skeleton variant="circular" width={64} height={64} sx={{ bgcolor: 'rgba(255,255,255,0.2)' }} />
              <Box>
                <Skeleton variant="text" width={80} height={32} sx={{ bgcolor: 'rgba(255,255,255,0.2)' }} />
                <Skeleton variant="text" width={120} height={20} sx={{ bgcolor: 'rgba(255,255,255,0.2)' }} />
              </Box>
            </Box>
          ) : weather ? (
            <Box>
              {/* Main weather display */}
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box
                    sx={{
                      width: 72,
                      height: 72,
                      borderRadius: '50%',
                      bgcolor: theme.iconBg,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                      color: '#333',
                    }}
                  >
                    <WeatherIconComponent code={weather.weatherCode} size={40} />
                  </Box>
                  <Box>
                    <Typography variant="h3" sx={{ fontWeight: 700, lineHeight: 1.1, color: '#fff' }}>
                      {Math.round(weather.temperature)}°
                    </Typography>
                    <Typography variant="body2" sx={{ opacity: 0.9, mt: 0.3 }}>
                      {getWeatherDescription(weather.weatherCode)}
                    </Typography>
                  </Box>
                </Box>
                <Box sx={{ textAlign: 'right' }}>
                  <Typography variant="body2" sx={{ opacity: 0.85 }}>
                    H: {Math.round(weather.todayMax)}°
                  </Typography>
                  <Typography variant="body2" sx={{ opacity: 0.85 }}>
                    T: {Math.round(weather.todayMin)}°
                  </Typography>
                </Box>
              </Box>

              {/* KI Insight */}
              {weather.insight && (
                <Box
                  sx={{
                    bgcolor: 'rgba(255,255,255,0.15)',
                    borderRadius: 2,
                    p: 1.5,
                    mb: 2,
                    backdropFilter: 'blur(4px)',
                  }}
                >
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                    <PsychologyIcon sx={{ fontSize: 16, color: '#fff' }} />
                    <Typography variant="caption" sx={{ fontWeight: 600, opacity: 0.9 }}>
                      KI-Einblick
                    </Typography>
                  </Box>
                  <Typography variant="body2" sx={{ opacity: 0.95, fontStyle: 'italic' }}>
                    „{weather.insight}"
                  </Typography>
                </Box>
              )}

              {/* Forecast */}
              {weather.forecast && weather.forecast.length > 0 && (
                <>
                  <Divider sx={{ borderColor: 'rgba(255,255,255,0.3)', mb: 1.5 }} />
                  <Box sx={{ display: 'flex', justifyContent: 'space-around' }}>
                    {weather.forecast.map((day: WeatherForecastDay, index: number) => (
                      <Box
                        key={index}
                        sx={{
                          display: 'flex',
                          flexDirection: 'column',
                          alignItems: 'center',
                          gap: 0.5,
                          minWidth: 45,
                        }}
                      >
                        <Typography variant="caption" sx={{ fontWeight: 600, opacity: 0.9 }}>
                          {day.day}
                        </Typography>
                        <WeatherIconComponent code={day.weatherCode} size={28} />
                        <Typography variant="caption" sx={{ fontWeight: 600 }}>
                          {Math.round(day.maxTemp)}°
                        </Typography>
                        <Typography variant="caption" sx={{ opacity: 0.7, fontSize: '0.65rem' }}>
                          {Math.round(day.minTemp)}°
                        </Typography>
                      </Box>
                    ))}
                  </Box>
                </>
              )}
            </Box>
          ) : null}
        </CardContent>
      </Card>
    </Fade>
  )
}
