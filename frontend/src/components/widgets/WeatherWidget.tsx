import { useEffect, useState } from 'react'
import { Card, CardContent, Typography, Box, Skeleton, Alert, IconButton, TextField } from '@mui/material'
import { LocationOn, Refresh, WbSunny, Cloud, CloudOff, Opacity, AcUnit, Thunderstorm } from '@mui/icons-material'

interface WeatherData {
  temperature: number
  weatherCode: number
  description: string
}

interface WeatherWidgetProps {
  defaultLatitude?: number
  defaultLongitude?: number
  defaultCity?: string
}

// WMO Weather interpretation codes
const getWeatherIcon = (code: number) => {
  if (code === 0) return <WbSunny sx={{ fontSize: 48, color: '#FFB300' }} />
  if (code >= 1 && code <= 3) return <Cloud sx={{ fontSize: 48, color: '#90A4AE' }} />
  if (code >= 45 && code <= 48) return <CloudOff sx={{ fontSize: 48, color: '#B0BEC5' }} />
  if (code >= 51 && code <= 67) return <Opacity sx={{ fontSize: 48, color: '#42A5F5' }} />
  if (code >= 71 && code <= 77) return <AcUnit sx={{ fontSize: 48, color: '#81D4FA' }} />
  if (code >= 80 && code <= 82) return <Opacity sx={{ fontSize: 48, color: '#1976D2' }} />
  if (code >= 95) return <Thunderstorm sx={{ fontSize: 48, color: '#5C6BC0' }} />
  return <WbSunny sx={{ fontSize: 48, color: '#FFB300' }} />
}

const getWeatherDescription = (code: number): string => {
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

export function WeatherWidget({
  defaultLatitude = 52.52,
  defaultLongitude = 13.41,
  defaultCity = 'Berlin'
}: WeatherWidgetProps) {
  const [weather, setWeather] = useState<WeatherData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [latitude, setLatitude] = useState(defaultLatitude)
  const [longitude, setLongitude] = useState(defaultLongitude)
  const [city, setCity] = useState(defaultCity)
  const [showSettings, setShowSettings] = useState(false)

  const fetchWeather = async () => {
    setLoading(true)
    setError(null)

    try {
      const response = await fetch(
        `https://api.open-meteo.com/v1/forecast?latitude=${latitude}&longitude=${longitude}&current=temperature_2m,weather_code`
      )

      if (!response.ok) {
        throw new Error('Wetterdaten konnten nicht geladen werden')
      }

      const data = await response.json()

      setWeather({
        temperature: data.current.temperature_2m,
        weatherCode: data.current.weather_code,
        description: getWeatherDescription(data.current.weather_code)
      })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unbekannter Fehler')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchWeather()
  }, [latitude, longitude])

  const handleRefresh = () => {
    fetchWeather()
  }

  const handleLocationSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    fetchWeather()
  }

  return (
    <Card sx={{ height: '100%', minHeight: 200 }}>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6" component="div" sx={{ fontWeight: 600 }}>
            Wetter
          </Typography>
          <Box>
            <IconButton size="small" onClick={() => setShowSettings(!showSettings)}>
              <LocationOn />
            </IconButton>
            <IconButton size="small" onClick={handleRefresh} disabled={loading}>
              <Refresh />
            </IconButton>
          </Box>
        </Box>

        {showSettings && (
          <Box component="form" onSubmit={handleLocationSubmit} sx={{ mb: 2 }}>
            <TextField
              fullWidth
              size="small"
              label="Stadt"
              value={city}
              onChange={(e) => setCity(e.target.value)}
              sx={{ mb: 1 }}
            />
            <Box sx={{ display: 'flex', gap: 1 }}>
              <TextField
                size="small"
                label="Breitengrad"
                type="number"
                value={latitude}
                onChange={(e) => setLatitude(parseFloat(e.target.value))}
                sx={{ flex: 1 }}
              />
              <TextField
                size="small"
                label="Längengrad"
                type="number"
                value={longitude}
                onChange={(e) => setLongitude(parseFloat(e.target.value))}
                sx={{ flex: 1 }}
              />
            </Box>
          </Box>
        )}

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {loading ? (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Skeleton variant="circular" width={48} height={48} />
            <Box>
              <Skeleton width={80} height={32} />
              <Skeleton width={120} height={20} />
            </Box>
          </Box>
        ) : weather ? (
          <Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
              {getWeatherIcon(weather.weatherCode)}
              <Box>
                <Typography variant="h4" component="div" sx={{ fontWeight: 600 }}>
                  {Math.round(weather.temperature)}°C
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {city}
                </Typography>
              </Box>
            </Box>
            <Typography variant="body1" color="text.primary">
              {weather.description}
            </Typography>
          </Box>
        ) : null}
      </CardContent>
    </Card>
  )
}
