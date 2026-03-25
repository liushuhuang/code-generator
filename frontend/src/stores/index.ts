import { defineStore } from 'pinia'
import { ref } from 'vue'
import { connectionApi } from '@/api'
import type { Connection } from '@/types'

export const useAppStore = defineStore('app', () => {
  const connections = ref<Connection[]>([])
  const currentConnection = ref<Connection | null>(null)
  const settings = ref({
    basePackage: 'com.example.generator',
    author: 'generator',
    outputDir: ''
  })

  const loadConnections = async () => {
    const { data } = await connectionApi.list()
    connections.value = data
  }

  const saveConnection = async (conn: Connection) => {
    const { data } = await connectionApi.save(conn)
    await loadConnections()
    return data
  }

  const deleteConnection = async (id: string) => {
    await connectionApi.delete(id)
    await loadConnections()
  }

  const testConnection = async (conn: Connection) => {
    const { data } = await connectionApi.test(conn)
    return data
  }

  const setCurrentConnection = (conn: Connection | null) => {
    currentConnection.value = conn
  }

  return {
    connections,
    currentConnection,
    settings,
    loadConnections,
    saveConnection,
    deleteConnection,
    testConnection,
    setCurrentConnection
  }
})