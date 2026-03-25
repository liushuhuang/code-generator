<template>
  <div class="app-container">
    <header class="app-header">
      <div class="logo">
        <span class="logo-text">Code Generator</span>
      </div>
      <div class="header-actions">
        <el-button @click="showSettings = true" circle>
          <el-icon><Setting /></el-icon>
        </el-button>
      </div>
    </header>
    
    <main class="app-main">
      <aside class="sidebar">
        <div class="connection-section">
          <div class="section-header">
            <span class="section-title">数据库连接</span>
            <el-button type="primary" size="small" @click="openConnectionDialog()">
              <el-icon><Plus /></el-icon>
              新建
            </el-button>
          </div>
          
          <div class="connection-list">
            <div 
              v-for="conn in connections" 
              :key="conn.id" 
              class="connection-item"
              :class="{ active: selectedConnectionId === conn.id }"
              @click="selectConnection(conn)"
            >
              <div class="connection-info">
                <div class="connection-name">{{ conn.name || '未命名连接' }}</div>
                <div class="connection-detail">{{ conn.host }}:{{ conn.port }}/{{ conn.database || '未指定数据库' }}</div>
              </div>
              <div class="connection-actions" @click.stop>
                <el-button type="primary" size="small" text @click="editConnection(conn)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button type="danger" size="small" text @click="deleteConnection(conn)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            
            <div v-if="connections.length === 0" class="empty-tip">
              暂无连接，请点击上方"新建"按钮添加
            </div>
          </div>
        </div>
        
        <div v-if="selectedConnectionId" class="table-section">
          <div class="section-title">数据表</div>
          <div v-if="!currentConnection?.database" class="warning-tip">
            <el-icon><WarningFilled /></el-icon>
            该连接未配置数据库，请点击编辑补充
          </div>
          <div v-else class="table-list">
            <el-checkbox-group v-model="selectedTables">
              <div v-for="table in tables" :key="table.tableName" class="table-item">
                <el-checkbox :value="table.tableName">
                  <span class="table-name">{{ table.tableName }}</span>
                  <span v-if="table.tableComment" class="table-comment">{{ table.tableComment }}</span>
                </el-checkbox>
              </div>
            </el-checkbox-group>
          </div>
          <div v-if="selectedTables.length > 0" class="selected-count">
            已选择 {{ selectedTables.length }} 张表
          </div>
        </div>
      </aside>
      
      <section class="content-area">
        <PreviewArea 
          :connection-id="selectedConnectionId"
          :selected-tables="selectedTables"
          :settings="settings"
        />
      </section>
    </main>
    
    <el-dialog 
      v-model="showConnectionDialog" 
      :title="isEditMode ? '编辑数据库连接' : '新建数据库连接'" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="connectionForm" :rules="formRules" label-width="100px">
        <el-form-item label="连接名称" prop="name">
          <el-input v-model="connectionForm.name" placeholder="请输入连接名称（必填）" />
        </el-form-item>
        <el-form-item label="主机地址" prop="host">
          <el-input v-model="connectionForm.host" placeholder="localhost" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="connectionForm.port" :min="1" :max="65535" style="width: 100%" />
        </el-form-item>
        <el-form-item label="数据库" prop="database">
          <el-input v-model="connectionForm.database" placeholder="请输入数据库名（必填）" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="connectionForm.username" placeholder="root" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="connectionForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showConnectionDialog = false">取消</el-button>
        <el-button @click="testConnection" :loading="testing">测试连接</el-button>
        <el-button type="primary" @click="saveConnection" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
    
    <el-dialog 
      v-model="showSettings" 
      title="生成设置" 
      width="450px"
    >
      <el-form label-width="80px">
        <el-form-item label="基础包名">
          <el-input v-model="settings.basePackage" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="settings.author" />
        </el-form-item>
        <el-form-item label="输出目录">
          <el-input v-model="settings.outputDir" placeholder="留空则使用ZIP下载" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="showSettings = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Setting, Plus, Edit, Delete, WarningFilled } from '@element-plus/icons-vue'
import PreviewArea from '@/components/PreviewArea.vue'
import axios from 'axios'

interface Connection {
  id: string
  name: string
  host: string
  port: number
  database: string
  username: string
  password: string
}

interface TableInfo {
  tableName: string
  tableComment: string
}

const formRef = ref<FormInstance>()
const connections = ref<Connection[]>([])
const selectedConnectionId = ref('')
const tables = ref<TableInfo[]>([])
const selectedTables = ref<string[]>([])
const showConnectionDialog = ref(false)
const showSettings = ref(false)
const testing = ref(false)
const saving = ref(false)
const isEditMode = ref(false)

const connectionForm = ref<Connection>({
  id: '',
  name: '',
  host: 'localhost',
  port: 3306,
  database: '',
  username: 'root',
  password: ''
})

const settings = ref({
  basePackage: 'com.example.generator',
  author: 'generator',
  outputDir: ''
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入连接名称', trigger: 'blur' }
  ],
  host: [
    { required: true, message: '请输入主机地址', trigger: 'blur' }
  ],
  database: [
    { required: true, message: '请输入数据库名', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ]
}

const currentConnection = computed(() => {
  return connections.value.find(c => c.id === selectedConnectionId.value)
})

const loadConnections = async () => {
  try {
    const { data } = await axios.get('/api/connection/list')
    if (data.code === 200) {
      connections.value = data.data || []
    }
  } catch (error) {
    console.error('加载连接失败', error)
  }
}

const selectConnection = async (conn: Connection) => {
  selectedConnectionId.value = conn.id
  selectedTables.value = []
  
  if (!conn.database) {
    tables.value = []
    return
  }
  
  try {
    const { data } = await axios.get('/api/database/tables', {
      params: { connectionId: conn.id }
    })
    if (data.code === 200) {
      tables.value = data.data || []
    }
  } catch (error) {
    ElMessage.error('获取表列表失败')
  }
}

const openConnectionDialog = () => {
  isEditMode.value = false
  connectionForm.value = {
    id: '',
    name: '',
    host: 'localhost',
    port: 3306,
    database: '',
    username: 'root',
    password: ''
  }
  showConnectionDialog.value = true
}

const editConnection = (conn: Connection) => {
  isEditMode.value = true
  connectionForm.value = { ...conn }
  showConnectionDialog.value = true
}

const deleteConnection = async (conn: Connection) => {
  try {
    await ElMessageBox.confirm(
      `确定删除连接"${conn.name || '未命名连接'}"吗？`,
      '删除确认',
      { type: 'warning' }
    )
    await axios.delete(`/api/connection/${conn.id}`)
    await loadConnections()
    if (selectedConnectionId.value === conn.id) {
      selectedConnectionId.value = ''
      tables.value = []
      selectedTables.value = []
    }
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const testConnection = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  testing.value = true
  try {
    const { data } = await axios.post('/api/connection/test', connectionForm.value)
    if (data.code === 200 && data.data) {
      ElMessage.success('连接成功')
    } else {
      ElMessage.error(data.message || '连接失败')
    }
  } catch (error) {
    ElMessage.error('连接失败')
  } finally {
    testing.value = false
  }
}

const saveConnection = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  saving.value = true
  try {
    await axios.post('/api/connection/save', connectionForm.value)
    await loadConnections()
    showConnectionDialog.value = false
    ElMessage.success(isEditMode.value ? '修改成功' : '保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadConnections()
})
</script>

<style scoped>
.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.app-header {
  height: 60px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.app-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sidebar {
  width: 300px;
  background: white;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  border-right: 1px solid #e4e7ed;
}

.connection-section {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.connection-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.connection-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.connection-item:hover {
  background: #ecf5ff;
}

.connection-item.active {
  background: #ecf5ff;
  border-color: #409eff;
}

.connection-info {
  flex: 1;
  min-width: 0;
}

.connection-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.connection-detail {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.connection-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.connection-item:hover .connection-actions {
  opacity: 1;
}

.empty-tip {
  padding: 20px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.table-section {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.warning-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fdf6ec;
  border-radius: 8px;
  color: #e6a23c;
  font-size: 13px;
  margin-bottom: 12px;
}

.table-list {
  flex: 1;
  overflow-y: auto;
}

.table-item {
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 8px;
}

.table-name {
  font-weight: 500;
  color: #303133;
}

.table-comment {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.selected-count {
  padding: 12px;
  background: #ecf5ff;
  border-radius: 8px;
  color: #409eff;
  font-size: 14px;
  margin-top: 12px;
  text-align: center;
}

.content-area {
  flex: 1;
  background: white;
  margin: 16px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>