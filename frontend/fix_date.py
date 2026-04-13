import re

with open('src/App.tsx', 'r') as f:
    content = f.read()

# Pattern für CardContent im Dashboard
# Wir suchen nach der CardContent Section im Dashboard
old_pattern = r'''(<CardMedia[^>]+/>)\s+<CardContent sx=\{\{ flexGrow: 1, overflow: 'hidden' \}\}>\s+<Chip\s+size="small"\s+label=\{article\.feedName \|\| 'News'\}\s+color="primary"\s+sx=\{\{ mb: 1 \}\}\s+/>\s+<Typography variant="h6" sx=\{\{ fontSize: '1rem', fontWeight: 600, mb: 1, lineHeight: 1\.3 \}\}>\s+\{article\.title\}\s+</Typography>\s+<Typography variant="body2" color="text\.secondary" sx=\{\{ mb: 1, height: 60, overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical' \}\}>\s+\{article\.description\?\.substring\(0, 100\) \|\| 'Keine Beschreibung verfügbar'\}\s+</Typography>\s+</CardContent>'''

new_replacement = r'''\1
                <CardContent sx={{ flexGrow: 1, overflow: 'hidden' }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                    <Chip
                      size="small"
                      label={article.feedName || 'News'}
                      color="primary"
                    />
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <CalendarTodayIcon fontSize="inherit" />
                      {article.publishedAt ? new Date(article.publishedAt).toLocaleDateString('de-DE', {
                        day: 'numeric',
                        month: 'short',
                        year: 'numeric',
                      }) : 'Kein Datum'}
                    </Typography>
                  </Box>
                  <Typography variant="h6" sx={{ fontSize: '1rem', fontWeight: 600, mb: 1, lineHeight: 1.3 }}>
                    {article.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1, height: 60, overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical' }}>
                    {article.description?.substring(0, 100) || 'Keine Beschreibung verfügbar'}
                  </Typography>
                </CardContent>'''

# Anzahl der Ersetzungen zählen
result = re.subn(old_pattern, new_replacement, content)
print(f"Ersetzungen: {result[1]}")

with open('src/App.tsx', 'w') as f:
    f.write(result[0])

print("Fertig!")
