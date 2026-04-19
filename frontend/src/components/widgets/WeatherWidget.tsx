import { useEffect, useState } from 'react'
import { Card, CardContent, Typography, Box, Skeleton, Alert, IconButton, TextField, CardMedia } from '@mui/material'
import { LocationOn, Refresh, WbSunny, Cloud, CloudOff, Opacity, AcUnit, Thunderstorm } from '@mui/icons-material'

interface WeatherData {
  temperature: number
  weatherCode: number
  description: string
}

interface WeatherTheme {
  gradient: string
  iconBg: string
}

interface WeatherWidgetProps {
  defaultLatitude?: number
  defaultLongitude?: number
  defaultCity?: string
}

// WMO Weather interpretation codes - https://open-meteo.com/en/docs
const getWeatherTheme = (code: number): WeatherTheme => {
  // Sonnig: Klarer Himmel
  if (code === 0) {
    return {
      gradient: 'linear-gradient(135deg, #FFD54F 0%, #FFB300 50%, #FF8F00 100%)',
      iconBg: '#FFF8E1'
    }
  }
  // Bewölkt: Leicht bewölkt bis bedeckt
  if (code >= 1 && code <= 3) {
    return {
      gradient: 'linear-gradient(135deg, #ECEFF1 0%, #B0BEC5 50%, #78909C 100%)',
      iconBg: '#ECEFF1'
    }
  }
  // Nebelig
  if (code >= 45 && code <= 48) {
    return {
      gradient: 'linear-gradient(135deg, #CFD8DC 0%, #B0BEC5 100%)',
      iconBg: '#CFD8DC'
    }
  }
  // Nieselregen / leichter Regen
  if (code >= 51 && code <= 57) {
    return {
      gradient: 'linear-gradient(135deg, #90CAF9 0%, #42A5F5 100%)',
      iconBg: '#E3F2FD'
    }
  }
  // Regen
  if (code >= 61 && code <= 67) {
    return {
      gradient: 'linear-gradient(135deg, #42A5F5 0%, #1976D2 100%)',
      iconBg: '#BBDEFB'
    }
  }
  // Schnee
  if (code >= 71 && code <= 77) {
    return {
      gradient: 'linear-gradient(135deg, #B3E5FC 0%, #4FC3F7 50%, #81D4FA 100%)',
      iconBg: '#E1F5FE'
    }
  }
  // Regenschauer
  if (code >= 80 && code <= 82) {
    return {
      gradient: 'linear-gradient(135deg, #1976D2 0%, #0D47A1 100%)',
      iconBg: '#90CAF9'
    }
  }
  // Gewitter
  if (code >= 95) {
    return {
      gradient: 'linear-gradient(135deg, #5C6BC0 0%, #3949AB 50%, #1A237E 100%)',
      iconBg: '#C5CAE9'
    }
  }
  // Default: Sonnig
  return {
    gradient: 'linear-gradient(135deg, #FFD54F 0%, #FFB300 100%)',
    iconBg: '#FFF8E1'
  }
}

const getWeatherIcon = (code: number, sx?: object) => {
  const iconProps = { sx: { fontSize: 64, ...sx } }
  if (code === 0) return <WbSunny {...iconProps} sx={{ ...iconProps.sx, color: '#FF8F00' }} />
  if (code >= 1 && code <= 3) return <Cloud {...iconProps} sx={{ ...iconProps.sx, color: '#546E7A' }} />
  if (code >= 45 && code <= 48) return <CloudOff {...iconProps} sx={{ ...iconProps.sx, color: '#78909C' }} />
  if (code >= 51 && code <= 67) return <Opacity {...iconProps} sx={{ ...iconProps.sx, color: '#1976D2' }} />
  if (code >= 71 && code <= 77) return <AcUnit {...iconProps} sx={{ ...iconProps.sx, color: '#0288D1' }} />
  if (code >= 80 && code <= 82) return <Opacity {...iconProps} sx={{ ...iconProps.sx, color: '#0D47A1' }} />
  if (code >= 95) return <Thunderstorm {...iconProps} sx={{ ...iconProps.sx, color: '#303F9F' }} />
  return <WbSunny {...iconProps} sx={{ ...iconProps.sx, color: '#FF8F00' }} />
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

  const theme = weather ? getWeatherTheme(weather.weatherCode) : getWeatherTheme(0)

  return (
    <Card sx={{ height: '100%', minHeight: 200, overflow: 'hidden' }}>
      {/* Dynamischer Header mit Hintergrund-Farbverlauf */}
      <CardMedia
        sx={{
          height: 80,
          background: theme.gradient,
          position: 'relative',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          px: 2
        }}
      >
        <Typography variant="h6" component="div" sx={{ fontWeight: 600, color: 'white', textShadow: '0 1px 3px rgba(0,0,0,0.3)' }}>
          Wetter
        </Typography>
        <Box>
          <IconButton size="small" onClick={() => setShowSettings(!showSettings)} sx={{ color: 'white' }}>
            <LocationOn />
          </IconButton>
          <IconButton size="small" onClick={handleRefresh} disabled={loading} sx={{ color: 'white' }}>
            <Refresh />
          </IconButton>
        </Box>
      </CardMedia>

      <CardContent sx={{ pt: 2 }}>
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
            <Skeleton variant="circular" width={64} height={64} />
            <Box>
              <Skeleton width={80} height={32} />
              <Skeleton width={120} height={20} />
            </Box>
          </Box>
        ) : weather ? (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 3 }}>
            {/* Grosses Icon mit Hintergrund */}
            <Box
              sx={{
                width: 80,
                height: 80,
                borderRadius: '50%',
                backgroundColor: theme.iconBg,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                flexShrink: 0
              }}
            >
              {getWeatherIcon(weather.weatherCode)}
            </Box>

            {/* Temperatur und Details */}
            <Box sx={{ flex: 1 }}>
              <Typography variant="h3" component="div" sx={{ fontWeight: 700, lineHeight: 1.2 }}>
                {Math.round(weather.temperature)}°C
              </Typography>
              <Typography variant="body1" sx={{ fontWeight: 500, color: 'text.primary', mt: 0.5 }}>
                {weather.description}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                {city}
              </Typography>
            </Box>
          </Box>
        ) : null}
      </CardContent>
    </Card>
  )
}
