let playHandler = null

export function registerPlayerHandler(handler) {
  playHandler = handler
}

export function unregisterPlayerHandler(handler) {
  if (playHandler === handler) {
    playHandler = null
  }
}

export async function playByUserGesture(songId, queue) {
  if (!playHandler) {
    return false
  }
  return playHandler(songId, queue)
}
