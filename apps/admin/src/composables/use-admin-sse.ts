import { onBeforeUnmount, onMounted, ref } from 'vue'
import { getNotificationStreamUrl, getNotificationTicket } from '../services/notifications'

export function useAdminSse(onNotification: () => void) {
  const connected = ref(false)
  let source: EventSource | null = null

  async function connect() {
    try {
      const ticket = await getNotificationTicket()
      source = new EventSource(getNotificationStreamUrl(ticket))
      source.addEventListener('connected', () => (connected.value = true))
      source.addEventListener('test-notification', onNotification)
      source.addEventListener('new-order', onNotification)
      source.addEventListener('new-reservation', onNotification)
      source.addEventListener('reservation-check-in', onNotification)
      source.onerror = () => (connected.value = false)
    } catch {
      connected.value = false
    }
  }

  function disconnect() {
    source?.close()
    source = null
    connected.value = false
  }

  onMounted(connect)
  onBeforeUnmount(disconnect)
  return { connected, connect, disconnect }
}
