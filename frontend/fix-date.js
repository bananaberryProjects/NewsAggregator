const fs = require('fs');

let content = fs.readFileSync('src/App.tsx', 'utf8');

// Die drei CardContent-Bereiche identifizieren und korrigieren
// Dashboard View (erste Vorkommen)
let dashboardOld = `                <CardContent sx={{ flexGrow: 1, overflow: 'hidden' }}>
                  <Chip
                    size="small"
                    label={article.feedName || 'News'}
                    color="primary"
                    sx={{ mb: 1 }}
                  />
                  <Typography variant="h6" sx={{ fontSize: '1rem', fontWeight: 600, mb: 1, lineHeight: 1.3 }}>
                    {article.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1, height: 60, overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical' }}>
                    {article.description?.substring(0, 100) || 'Keine Beschreibung verfügbar'}
                  </Typography>
                </CardContent>`;

let dashboardNew = `                <CardContent sx={{ flexGrow: 1, overflow: 'hidden' }}>
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
                </CardContent>`;

// Ersetze ALLE drei Vorkommen
let count = 0;
content = content.split(dashboardOld).join(dashboardNew);
console.log('Replacements:', content.split(dashboardNew).length - 1);

fs.writeFileSync('src/App.tsx', content);
console.log('Done!');
