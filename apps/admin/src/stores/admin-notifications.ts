import { ref } from 'vue'
import { defineStore } from 'pinia'

export type AdminNotificationEvent = 'new-order' | 'new-reservation' | 'reservation-check-in' | 'test-notification'

export const useAdminNotificationsStore = defineStore('admin-notifications', () => {
  const version = ref(0)
  const latestEvent = ref<AdminNotificationEvent | null>(null)

  function publish(event: AdminNotificationEvent) {
    latestEvent.value = event
    version.value += 1
  }

  return { version, latestEvent, publish }
})
