import { useState, useEffect, useRef } from 'react'
import { Box, InputBase, IconButton, Tooltip, CircularProgress } from '@mui/material'
import { Search as SearchIcon, Clear as ClearIcon } from '@mui/icons-material'

interface SearchFilters {
  categoryId?: string
  readFilter?: 'READ' | 'UNREAD'
  favoriteFilter?: 'FAVORITE' | 'NOT_FAVORITE'
}

interface SearchBarProps {
  loading: boolean
  query: string
  onQueryChange: (q: string) => void
  onSearch: (q: string, filters?: SearchFilters) => void
  onReset: () => void
  filters?: SearchFilters
  isSearchActive?: boolean
}

export function SearchBar({
  loading,
  query,
  onQueryChange,
  onSearch,
  onReset,
  filters = {},
  isSearchActive,
}: SearchBarProps) {
  const [inputValue, setInputValue] = useState(query)
  const inputRef = useRef<HTMLInputElement>(null)
  const prevIsActiveRef = useRef(isSearchActive)

  // Sync external query → local input
  useEffect(() => {
    setInputValue(query)
  }, [query])

  // Debounce input (300ms)
  useEffect(() => {
    const timer = setTimeout(() => {
      if (inputValue.trim()) {
        onSearch(inputValue.trim(), filters)
      } else {
        onReset()
      }
    }, 300)
    return () => clearTimeout(timer)
  }, [inputValue, filters])

  // Wenn von aussen zurueckgesetzt wird
  useEffect(() => {
    if (prevIsActiveRef.current && !isSearchActive) {
      setInputValue('')
      onQueryChange('')
      onReset()
    }
    prevIsActiveRef.current = isSearchActive
  }, [isSearchActive, onReset, onQueryChange])

  const handleClear = () => {
    setInputValue('')
    onQueryChange('')
    onReset()
    inputRef.current?.focus()
  }

  return (
    <Box sx={{ px: 2, py: 1, width: '100%' }}>
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          bgcolor: 'background.paper',
          borderRadius: 2,
          px: 1.5,
          py: 0.5,
          border: '1px solid',
          borderColor: 'divider',
        }}
      >
        {loading ? (
          <CircularProgress size={20} sx={{ mr: 1, color: 'text.secondary' }} />
        ) : (
          <SearchIcon sx={{ mr: 1, color: 'text.secondary', fontSize: 20 }} />
        )}
        <InputBase
          inputRef={inputRef}
          placeholder="Volltextsuche..."
          value={inputValue}
          onChange={(e) => {
            setInputValue(e.target.value)
            onQueryChange(e.target.value)
          }}
          sx={{ flex: 1, fontSize: '1rem' }}
        />
        {inputValue && (
          <Tooltip title="Löschen">
            <IconButton size="small" onClick={handleClear}>
              <ClearIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        )}
      </Box>
    </Box>
  )
}
