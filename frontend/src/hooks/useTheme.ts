import { useState, useEffect } from 'react'
import { createTheme } from '@mui/material'

const STORAGE_KEY = 'theme-preference'

function getInitialTheme(): boolean {
  if (typeof window === 'undefined') {
    return true
  }

  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored !== null) {
    return stored === 'dark'
  }

  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

export function useTheme() {
  const [isDark, setIsDarkState] = useState(() => getInitialTheme())

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, isDark ? 'dark' : 'light')
  }, [isDark])

  const setIsDark = (value: boolean | ((prev: boolean) => boolean)) => {
    setIsDarkState(value)
  }

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
