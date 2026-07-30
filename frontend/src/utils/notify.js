/**
 * Tiny notification helpers. Replace with a toast library later if desired.
 * For now we keep the same alert/confirm UX but centralize it.
 */

export function notifySuccess(message) {
  // Prefer non-blocking in the future; alert is fine for the quick win
  alert(`✅ ${message}`)
}

export function notifyError(message) {
  alert(`❌ ${message}`)
}

export function confirmAction(message) {
  return window.confirm(message)
}
