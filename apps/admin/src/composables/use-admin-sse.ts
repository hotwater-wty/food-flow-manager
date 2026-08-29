import { onBeforeUnmount, onMounted, ref } from 'vue'
import { getNotificationStreamUrl, getNotificationTicket } from '../services/notifications'
import type { AdminNotificationEvent } from '../stores/admin-notifications'

const RETRY_DELAYS = [1000, 2000, 5000] as const

export function useAdminSse(onNotification: (event: AdminNotificationEvent) => void) {
  const connected = ref(false)
  let source: EventSource | null = null
  let retryTimer: number | null = null
  let retryAttempt = 0
  let stopped = false

  async function connect() {
    if (source || stopped) return
    try {
      const ticket = await getNotificationTicket()
      if (stopped) return
      source = new EventSource(getNotificationStreamUrl(ticket))
      source.addEventListener('connected', () => {
        connected.value = true
        retryAttempt = 0
      })
      const events: AdminNotificationEvent[] = [
        'test-notification',
        'new-order',
        'new-reservation',
        'reservation-check-in',
      ]
      events.forEach((event) => source?.addEventListener(event, () => onNotification(event)))
      source.onerror = scheduleReconnect
    } catch {
      connected.value = false
      scheduleReconnect()
    }
  }

  function scheduleReconnect() {
    source?.close()
    source = null
    connected.value = false
    if (stopped || retryTimer !== null) return
    const delay = RETRY_DELAYS[Math.min(retryAttempt, RETRY_DELAYS.length - 1)]
    retryAttempt += 1
    retryTimer = window.setTimeout(() => {
      retryTimer = null
      void connect()
    }, delay)
  }

  function disconnect() {
    stopped = true
    if (retryTimer !== null) window.clearTimeout(retryTimer)
    retryTimer = null
    source?.close()
    source = null
    connected.value = false
  }

  onMounted(() => {
    stopped = false
    void connect()
  })
  onBeforeUnmount(disconnect)
  return { connected, connect, disconnect }
}
