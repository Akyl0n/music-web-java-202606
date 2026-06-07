export function buildSongMediaUrl(songId, fallbackUrl = '') {
  if (songId !== undefined && songId !== null && songId !== '') {
    return `/api/web/songs/${songId}/media`
  }
  if (!fallbackUrl) {
    return ''
  }
  if (fallbackUrl.startsWith('http://') || fallbackUrl.startsWith('https://') || fallbackUrl.startsWith('/api/')) {
    return fallbackUrl
  }
  return fallbackUrl.startsWith('/') ? `/api${fallbackUrl}` : `/api/${fallbackUrl}`
}
