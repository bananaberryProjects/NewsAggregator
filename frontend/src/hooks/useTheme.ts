import { useState } from 'react'
import { createTheme } from '@mui/material'

export function useTheme() {
  const [isDark, setIsDark] = useState(true)

  const theme = createTheme({
    palette: {
      mode: isDark ? 'dark' : 'light',
      primary: {
        main: '#667eea',
      },
      secondary: {
        main: '#764ba2',
      },
      background: {
        default: isDark ? '#0f172a' : '#f8fafc',
        paper: isDark ? '#1e293b' : '#ffffff',
      },
    },
    typography: {
      fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    },
  })

  const toggleTheme = () => setIsDark(prev => !prev)

  return { theme, isDark, toggleTheme, setIsDark }
}
