import axios from 'axios'
import type { Connection, TableInfo, PreviewResult } from '@/types'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

export const connectionApi = {
  list: () => api.get<Connection[]>('/connection/list'),
  get: (id: string) => api.get<Connection>(`/connection/${id}`),
  save: (data: Connection) => api.post<Connection>('/connection/save', data),
  delete: (id: string) => api.delete(`/connection/${id}`),
  test: (data: Connection) => api.post<boolean>('/connection/test', data)
}

export const databaseApi = {
  listTables: (connectionId: string) => 
    api.get<TableInfo[]>('/database/tables', { params: { connectionId } }),
  getTable: (connectionId: string, tableName: string) =>
    api.get<TableInfo>('/database/table', { params: { connectionId, tableName } })
}

export const generateApi = {
  preview: (data: {
    connectionId: string
    tableNames: string[]
    basePackage: string
    author: string
  }) => api.post<PreviewResult[]>('/generate/preview', data),
  
  export: (data: {
    connectionId: string
    tableNames: string[]
    basePackage: string
    author: string
  }) => api.post('/generate/export', data, { responseType: 'blob' }),
  
  toDir: (data: {
    connectionId: string
    tableNames: string[]
    basePackage: string
    author: string
    outputPath: string
  }) => api.post('/generate/to-dir', data)
}

export const templateApi = {
  list: () => api.get<string[]>('/template/list'),
  get: (name: string) => api.get<string>(`/template/${name}`),
  save: (name: string, content: string) => 
    api.post('/template/save', { name, content }),
  reset: (name: string) => api.post(`/template/reset/${name}`)
}