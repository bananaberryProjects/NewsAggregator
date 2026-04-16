import { IconButton, Tooltip } from '@mui/material'
import { Brightness4, Brightness7 } from '@mui/icons-material'

interface ThemeToggleButtonProps {
  isDark: boolean
  onToggle: () => void
}

export function ThemeToggleButton({ isDark, onToggle }: ThemeToggleButtonProps) {
  return (
    <Tooltip title={isDark ? 'Zu Light Mode wechseln' : 'Zu Dark Mode wechseln'}>
      <IconButton
        onClick={onToggle}
        color="inherit"
        aria-label={isDark ? 'Light Mode aktivieren' : 'Dark Mode aktivieren'}
      >
        {isDark ? <Brightness7 /> : <Brightness4 />}
      </IconButton>
    </Tooltip>
  )
}
