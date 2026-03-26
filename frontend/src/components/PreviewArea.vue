<template>
  <div class="preview-area">
    <div class="preview-header">
      <div class="preview-tabs">
        <button 
          v-for="tab in currentTabs" 
          :key="tab.key"
          class="tab"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>
      <div class="preview-actions">
        <el-button @click="openTemplateEditor">
          <el-icon><Edit /></el-icon>
          编辑模板
        </el-button>
        <el-button type="primary" @click="generatePreview" :loading="loading">
          生成预览
        </el-button>
      </div>
    </div>
    
    <div class="editor-container">
      <textarea 
        v-model="editorContent" 
        readonly 
        class="code-editor"
        placeholder="选择表并点击生成预览查看代码"
      ></textarea>
    </div>
    
    <div class="generate-actions">
      <el-button @click="downloadZip" :disabled="!hasPreview">
        下载ZIP
      </el-button>
      <el-button type="primary" @click="generateToDir" :disabled="!hasPreview">
        生成到目录
      </el-button>
    </div>
    
    <el-dialog 
      v-model="showTemplateEditor" 
      title="编辑模板" 
      width="800px"
      top="5vh"
    >
      <div class="template-editor">
        <div class="template-type-selector">
          <el-radio-group v-model="currentTemplateType" @change="loadTemplateContent">
            <el-radio-button 
              v-for="tab in allTemplateTabs" 
              :key="tab.key" 
              :value="tab.key"
            >
              {{ tab.label }}
            </el-radio-button>
          </el-radio-group>
        </div>
        <div class="template-editor-container">
          <textarea 
            v-model="templateContent" 
            class="template-textarea"
            placeholder="加载模板中..."
          ></textarea>
        </div>
      </div>
      <template #footer>
        <el-button @click="resetTemplate">重置为默认</el-button>
        <el-button @click="showTemplateEditor = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate" :loading="savingTemplate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import axios from 'axios'

interface TableConfig {
  tableName: string
  selectedFields: string[]
  generateVO: boolean
  generateDTO: boolean
  generateQuery: boolean
}

const props = defineProps<{
  connectionId: string
  selectedTables: string[]
  tableConfigs: Map<string, TableConfig>
  settings: {
    basePackage: string
    author: string
    outputDir: string
    ormType: string
    enableLombok: boolean
    enableSwagger: boolean
    enableValidation: boolean
    enableChain: boolean
    serializable: boolean
    dateType: string
    entityPrefix: string
    entitySuffix: string
    removeTablePrefix: string
    namingStrategy: string
  }
}>()

const mybatisPlusTabs = [
  { key: 'entity', label: 'Entity' },
  { key: 'mapper', label: 'Mapper' },
  { key: 'service', label: 'Service' },
  { key: 'controller', label: 'Controller' }
]

const mybatisTabs = [
  { key: 'entity', label: 'Entity' },
  { key: 'mapper', label: 'Mapper' },
  { key: 'mapperXml', label: 'MapperXML' },
  { key: 'service', label: 'Service' },
  { key: 'controller', label: 'Controller' }
]

const extraTabs = [
  { key: 'vo', label: 'VO' },
  { key: 'dto', label: 'DTO' },
  { key: 'query', label: 'Query' }
]

const currentTabs = computed(() => {
  let tabs = props.settings.ormType === 'mybatis' ? mybatisTabs : mybatisPlusTabs
  
  const hasVO = Array.from(props.tableConfigs.values()).some(c => c.generateVO)
  const hasDTO = Array.from(props.tableConfigs.values()).some(c => c.generateDTO)
  const hasQuery = Array.from(props.tableConfigs.values()).some(c => c.generateQuery)
  
  if (hasVO) tabs = [...tabs, extraTabs[0]]
  if (hasDTO) tabs = [...tabs, extraTabs[1]]
  if (hasQuery) tabs = [...tabs, extraTabs[2]]
  
  return tabs
})

const allTemplateTabs = computed(() => {
  let tabs = props.settings.ormType === 'mybatis' ? [...mybatisTabs] : [...mybatisPlusTabs]
  return [...tabs, ...extraTabs]
})

const activeTab = ref('entity')
const loading = ref(false)
const hasPreview = ref(false)
const previewResults: any = ref([])
const editorContent = ref('// 选择表并点击生成预览查看代码')

const showTemplateEditor = ref(false)
const currentTemplateType = ref('entity')
const templateContent = ref('')
const savingTemplate = ref(false)

watch(() => props.settings.ormType, () => {
  activeTab.value = 'entity'
})

const getTableConfigsArray = (): TableConfig[] => {
  return Array.from(props.tableConfigs.values())
}

const generatePreview = async () => {
  if (!props.connectionId) {
    ElMessage.warning('请先选择数据库连接')
    return
  }
  
  if (props.selectedTables.length === 0) {
    ElMessage.warning('请选择至少一张表')
    return
  }
  
  loading.value = true
  try {
    const { data } = await axios.post('/api/generate/preview', {
      connectionId: props.connectionId,
      ormType: props.settings.ormType,
      tableNames: props.selectedTables,
      tableConfigs: getTableConfigsArray(),
      basePackage: props.settings.basePackage,
      author: props.settings.author,
      enableLombok: props.settings.enableLombok,
      enableSwagger: props.settings.enableSwagger,
      enableValidation: props.settings.enableValidation,
      enableChain: props.settings.enableChain,
      serializable: props.settings.serializable,
      dateType: props.settings.dateType,
      entityPrefix: props.settings.entityPrefix,
      entitySuffix: props.settings.entitySuffix,
      removeTablePrefix: props.settings.removeTablePrefix,
      namingStrategy: props.settings.namingStrategy
    })
    if (data.code === 200) {
      previewResults.value = data.data || []
      hasPreview.value = true
      updateEditorContent()
      ElMessage.success('生成成功')
    } else {
      ElMessage.error(data.message || '生成失败')
    }
  } catch (error) {
    ElMessage.error('生成失败')
  } finally {
    loading.value = false
  }
}

const updateEditorContent = () => {
  const result = previewResults.value.find((r: any) => r.type === activeTab.value)
  if (result) {
    editorContent.value = result.content
  }
}

const downloadZip = async () => {
  try {
    const response = await axios.post('/api/generate/export', {
      connectionId: props.connectionId,
      ormType: props.settings.ormType,
      tableNames: props.selectedTables,
      tableConfigs: getTableConfigsArray(),
      basePackage: props.settings.basePackage,
      author: props.settings.author,
      enableLombok: props.settings.enableLombok,
      enableSwagger: props.settings.enableSwagger,
      enableValidation: props.settings.enableValidation,
      enableChain: props.settings.enableChain,
      serializable: props.settings.serializable,
      dateType: props.settings.dateType,
      entityPrefix: props.settings.entityPrefix,
      entitySuffix: props.settings.entitySuffix,
      removeTablePrefix: props.settings.removeTablePrefix,
      namingStrategy: props.settings.namingStrategy
    }, { responseType: 'blob' })
    
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'code.zip')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const generateToDir = async () => {
  if (!props.settings.outputDir) {
    ElMessage.warning('请先在设置中配置输出目录')
    return
  }
  
  try {
    const { data } = await axios.post('/api/generate/to-dir', {
      connectionId: props.connectionId,
      ormType: props.settings.ormType,
      tableNames: props.selectedTables,
      tableConfigs: getTableConfigsArray(),
      basePackage: props.settings.basePackage,
      author: props.settings.author,
      outputPath: props.settings.outputDir,
      enableLombok: props.settings.enableLombok,
      enableSwagger: props.settings.enableSwagger,
      enableValidation: props.settings.enableValidation,
      enableChain: props.settings.enableChain,
      serializable: props.settings.serializable,
      dateType: props.settings.dateType,
      entityPrefix: props.settings.entityPrefix,
      entitySuffix: props.settings.entitySuffix,
      removeTablePrefix: props.settings.removeTablePrefix,
      namingStrategy: props.settings.namingStrategy
    })
    if (data.code === 200) {
      ElMessage.success('生成成功')
    } else {
      ElMessage.error(data.message || '生成失败')
    }
  } catch (error) {
    ElMessage.error('生成失败')
  }
}

const openTemplateEditor = async () => {
  showTemplateEditor.value = true
  await loadTemplateContent()
}

const getTemplateGroup = (templateName: string): string => {
  if (['vo', 'dto', 'query'].includes(templateName)) {
    return 'common'
  }
  return props.settings.ormType
}

const loadTemplateContent = async () => {
  const group = getTemplateGroup(currentTemplateType.value)
  try {
    const { data } = await axios.get(`/api/template/${group}/${currentTemplateType.value}`)
    if (data.code === 200) {
      templateContent.value = data.data || ''
    } else {
      ElMessage.error('加载模板失败')
    }
  } catch (error) {
    ElMessage.error('加载模板失败')
  }
}

const saveTemplate = async () => {
  savingTemplate.value = true
  const group = getTemplateGroup(currentTemplateType.value)
  try {
    const { data } = await axios.post('/api/template/save', {
      ormType: group,
      name: currentTemplateType.value,
      content: templateContent.value
    })
    if (data.code === 200) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(data.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    savingTemplate.value = false
  }
}

const resetTemplate = async () => {
  try {
    await ElMessageBox.confirm('确定要将模板重置为默认吗？', '确认重置', {
      type: 'warning'
    })
    const group = getTemplateGroup(currentTemplateType.value)
    const { data } = await axios.post(`/api/template/reset/${group}/${currentTemplateType.value}`)
    if (data.code === 200) {
      await loadTemplateContent()
      ElMessage.success('重置成功')
    } else {
      ElMessage.error(data.message || '重置失败')
    }
  } catch (error) {
    // 用户取消
  }
}

watch(activeTab, () => {
  updateEditorContent()
})
</script>

<style scoped>
.preview-area {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.preview-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fafafa;
}

.preview-tabs {
  display: flex;
  gap: 4px;
}

.tab {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  background: transparent;
  border: none;
  color: #606266;
  transition: all 0.2s;
}

.tab:hover {
  background: #f5f7fa;
}

.tab.active {
  background: #409eff;
  color: white;
}

.preview-actions {
  display: flex;
  gap: 8px;
}

.editor-container {
  flex: 1;
  padding: 12px;
  overflow: hidden;
}

.code-editor {
  width: 100%;
  height: 100%;
  border: 1px solid #e4e7ed;
  background: #1e1e1e;
  color: #d4d4d4;
  font-family: Consolas, Monaco, monospace;
  font-size: 14px;
  line-height: 1.5;
  padding: 16px;
  resize: none;
  outline: none;
  border-radius: 8px;
}

.generate-actions {
  padding: 16px 20px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background: #fafafa;
}

.template-editor {
  height: 500px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.template-type-selector {
  flex-shrink: 0;
}

.template-editor-container {
  flex: 1;
  overflow: hidden;
}

.template-textarea {
  width: 100%;
  height: 100%;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
  line-height: 1.5;
  padding: 12px;
  resize: none;
  outline: none;
  background: #1e1e1e;
  color: #d4d4d4;
}

.template-textarea:focus {
  border-color: #409eff;
}
</style>