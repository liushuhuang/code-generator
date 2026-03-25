export interface Connection {
  id: string
  name: string
  host: string
  port: number
  database: string
  username: string
  password: string
}

export interface TableInfo {
  tableName: string
  tableComment: string
  columns: ColumnInfo[]
}

export interface ColumnInfo {
  columnName: string
  columnType: string
  columnComment: string
  javaType: string
  fieldName: string
  primaryKey: boolean
  nullable: boolean
  defaultValue: string
}

export interface PreviewResult {
  fileName: string
  content: string
  type: string
}

export interface GeneratorSettings {
  basePackage: string
  author: string
  outputDir: string
}