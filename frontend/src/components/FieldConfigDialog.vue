<template>
  <el-dialog 
    :model-value="visible" 
    :title="'配置字段 - ' + tableName" 
    width="700px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <div class="field-config">
      <div class="field-actions">
        <el-button size="small" @click="selectAll">全选</el-button>
        <el-button size="small" @click="selectNone">清空</el-button>
      </div>
      
      <div class="field-list">
        <el-checkbox-group v-model="selectedFields">
          <div v-for="field in fields" :key="field.columnName" class="field-item">
            <el-checkbox :value="field.columnName">
              <div class="field-info">
                <span class="field-name">{{ field.columnName }}</span>
                <span class="field-type">{{ field.columnType }}</span>
                <span class="field-comment">{{ field.columnComment || '-' }}</span>
              </div>
            </el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      
      <el-divider />
      
      <div class="generate-options">
        <div class="option-label">生成选项：</div>
        <el-checkbox v-model="generateVO">VO（视图对象）</el-checkbox>
        <el-checkbox v-model="generateDTO">DTO（数据传输对象）</el-checkbox>
        <el-checkbox v-model="generateQuery">Query（查询条件）</el-checkbox>
      </div>
    </div>
    
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="confirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

interface ColumnInfo {
  columnName: string
  columnType: string
  columnComment: string
  javaType: string
  fieldName: string
  primaryKey: boolean
}

const props = defineProps<{
  visible: boolean
  connectionId: string
  tableName: string
  config?: {
    selectedFields: string[]
    generateVO: boolean
    generateDTO: boolean
    generateQuery: boolean
  }
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'confirm', config: {
    tableName: string
    selectedFields: string[]
    generateVO: boolean
    generateDTO: boolean
    generateQuery: boolean
  }): void
}>()

const fields = ref<ColumnInfo[]>([])
const selectedFields = ref<string[]>([])
const generateVO = ref(false)
const generateDTO = ref(false)
const generateQuery = ref(false)
const loading = ref(false)

const loadFields = async () => {
  if (!props.connectionId || !props.tableName) return
  
  loading.value = true
  try {
    const { data } = await axios.get('/api/database/columns', {
      params: {
        connectionId: props.connectionId,
        tableName: props.tableName
      }
    })
    if (data.code === 200) {
      fields.value = data.data || []
      if (selectedFields.value.length === 0) {
        selectedFields.value = fields.value.map(f => f.columnName)
      }
    }
  } catch (error) {
    ElMessage.error('加载字段失败')
  } finally {
    loading.value = false
  }
}

const selectAll = () => {
  selectedFields.value = fields.value.map(f => f.columnName)
}

const selectNone = () => {
  selectedFields.value = []
}

const confirm = () => {
  if (selectedFields.value.length === 0) {
    ElMessage.warning('请至少选择一个字段')
    return
  }
  
  if (!generateVO.value && !generateDTO.value && !generateQuery.value) {
    ElMessage.warning('请至少选择一种生成类型')
    return
  }
  
  emit('confirm', {
    tableName: props.tableName,
    selectedFields: selectedFields.value,
    generateVO: generateVO.value,
    generateDTO: generateDTO.value,
    generateQuery: generateQuery.value
  })
  emit('update:visible', false)
}

watch(() => props.visible, (val) => {
  if (val) {
    loadFields()
    if (props.config) {
      selectedFields.value = [...props.config.selectedFields]
      generateVO.value = props.config.generateVO
      generateDTO.value = props.config.generateDTO
      generateQuery.value = props.config.generateQuery
    } else {
      selectedFields.value = []
      generateVO.value = false
      generateDTO.value = false
      generateQuery.value = false
    }
  }
})
</script>

<style scoped>
.field-config {
  max-height: 500px;
  overflow-y: auto;
}

.field-actions {
  margin-bottom: 12px;
}

.field-list {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.field-item {
  padding: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.field-item:last-child {
  border-bottom: none;
}

.field-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.field-name {
  font-weight: 500;
  color: #303133;
  min-width: 120px;
}

.field-type {
  color: #909399;
  font-size: 12px;
  min-width: 80px;
}

.field-comment {
  color: #67c23a;
  font-size: 12px;
}

.generate-options {
  display: flex;
  align-items: center;
  gap: 16px;
}

.option-label {
  font-weight: 500;
  color: #606266;
}
</style>