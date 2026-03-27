<template>
  <div class="sql-builder-page">
    <header class="page-header">
      <div class="header-left">
        <el-button text @click="$router.push('/')">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <span class="page-title">SQL 构建器</span>
      </div>
    </header>
    
    <main class="page-main">
      <div class="steps-container">
        <el-steps :active="currentStep" finish-status="success" simple>
          <el-step title="基础信息" />
          <el-step title="入参配置" />
          <el-step title="SQL配置" />
          <el-step title="预览生成" />
        </el-steps>
      </div>
      
      <div class="step-content">
        <div v-show="currentStep === 0" class="step-panel">
          <el-form :model="config" label-width="120px">
            <el-form-item label="数据库连接">
              <el-select v-model="config.connectionId" placeholder="选择连接" @change="onConnectionChange">
                <el-option 
                  v-for="conn in connections" 
                  :key="conn.id" 
                  :value="conn.id"
                  :label="conn.name || conn.database"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="目标表">
              <el-select v-model="config.tableName" placeholder="选择表" @change="onTableChange">
                <el-option 
                  v-for="table in tables" 
                  :key="table.tableName" 
                  :value="table.tableName"
                  :label="table.tableName + (table.tableComment ? ` (${table.tableComment})` : '')"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="方法名称">
              <el-input v-model="config.methodName" placeholder="如 batchInsert" />
            </el-form-item>
            
            <el-form-item label="方法描述">
              <el-input v-model="config.methodDesc" placeholder="批量插入" />
            </el-form-item>
            
            <el-form-item label="操作类型">
              <el-radio-group v-model="config.operationType">
                <el-radio value="BATCH_INSERT">批量插入</el-radio>
                <el-radio value="BATCH_INSERT_OR_UPDATE">批量插入或更新</el-radio>
                <el-radio value="BATCH_UPDATE">批量更新</el-radio>
                <el-radio value="BATCH_DELETE">批量删除</el-radio>
                <el-radio value="SELECT_COMPLEX">复杂查询</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="返回类型">
              <el-select v-model="config.returnType" style="width: 200px">
                <el-option value="int" label="int (影响行数)" />
                <el-option value="void" label="void" />
                <el-option value="List" label="List&lt;Entity&gt;" />
                <el-option value="Page" label="Page&lt;Entity&gt; (分页)" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
        
        <div v-show="currentStep === 1" class="step-panel">
          <el-form :model="config" label-width="120px">
            <el-form-item label="入参类型">
              <el-radio-group v-model="config.paramConfig.paramType">
                <el-radio value="LIST_ENTITY">List&lt;Entity&gt;</el-radio>
                <el-radio value="LIST_DTO">List&lt;DTO&gt;</el-radio>
                <el-radio value="BASIC_PARAMS">基础类型参数</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <template v-if="config.paramConfig.paramType === 'LIST_DTO'">
              <el-form-item label="DTO类名">
                <el-input v-model="config.paramConfig.dtoClassName" placeholder="自动生成或自定义" />
              </el-form-item>
              
              <el-form-item label="选择字段">
                <div class="field-actions">
                  <el-button size="small" @click="selectAllDtoFields">全选</el-button>
                  <el-button size="small" @click="clearDtoFields">清空</el-button>
                </div>
                <div class="field-checkbox-list">
                  <el-checkbox 
                    v-for="field in tableFields" 
                    :key="field.columnName"
                    :value="field.columnName"
                    v-model="selectedDtoFields"
                  >
                    {{ field.columnName }} ({{ field.javaType }})
                  </el-checkbox>
                </div>
              </el-form-item>
            </template>
            
            <template v-if="config.paramConfig.paramType === 'BASIC_PARAMS'">
              <el-form-item label="参数列表">
                <div class="basic-params-list">
                  <div v-for="(param, index) in config.paramConfig.basicParams" :key="index" class="param-item">
                    <el-input v-model="param.paramName" placeholder="参数名" style="width: 120px" />
                    <el-select v-model="param.paramType" style="width: 140px">
                      <el-option value="String" />
                      <el-option value="Integer" />
                      <el-option value="Long" />
                      <el-option value="List<Long>" />
                      <el-option value="List<String>" />
                    </el-select>
                    <el-button type="danger" text @click="config.paramConfig.basicParams.splice(index, 1)">
                      删除
                    </el-button>
                  </div>
                  <el-button size="small" @click="addBasicParam">+ 添加参数</el-button>
                </div>
              </el-form-item>
            </template>
          </el-form>
        </div>
        
        <div v-show="currentStep === 2" class="step-panel">
          <el-form :model="config" label-width="120px">
            <template v-if="config.operationType === 'BATCH_INSERT'">
              <el-form-item label="插入字段">
                <div class="field-actions">
                  <el-button size="small" @click="selectAllInsertFields">全选</el-button>
                  <el-button size="small" @click="clearInsertFields">清空</el-button>
                </div>
                <div class="field-checkbox-list">
                  <el-checkbox 
                    v-for="field in tableFields" 
                    :key="field.columnName"
                    :value="field.columnName"
                    v-model="config.sqlDetailConfig.insertFields"
                  >
                    {{ field.columnName }}
                  </el-checkbox>
                </div>
              </el-form-item>
            </template>
            
            <template v-if="config.operationType === 'BATCH_INSERT_OR_UPDATE'">
              <el-form-item label="唯一键字段">
                <el-select v-model="config.sqlDetailConfig.uniqueKeyField" style="width: 200px">
                  <el-option 
                    v-for="field in tableFields" 
                    :key="field.columnName"
                    :value="field.columnName"
                    :label="field.columnName"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item label="插入字段">
                <div class="field-actions">
                  <el-button size="small" @click="selectAllInsertFields">全选</el-button>
                  <el-button size="small" @click="clearInsertFields">清空</el-button>
                </div>
                <div class="field-checkbox-list">
                  <el-checkbox 
                    v-for="field in tableFields" 
                    :key="field.columnName"
                    :value="field.columnName"
                    v-model="config.sqlDetailConfig.insertFields"
                  >
                    {{ field.columnName }}
                  </el-checkbox>
                </div>
              </el-form-item>
              
              <el-form-item label="更新字段">
                <div class="field-actions">
                  <el-button size="small" @click="selectAllUpdateOnDuplicateFields">全选</el-button>
                  <el-button size="small" @click="clearUpdateOnDuplicateFields">清空</el-button>
                </div>
                <div class="field-checkbox-list">
                  <el-checkbox 
                    v-for="field in tableFields" 
                    :key="field.columnName"
                    :value="field.columnName"
                    v-model="config.sqlDetailConfig.updateOnDuplicateFields"
                  >
                    {{ field.columnName }}
                  </el-checkbox>
                </div>
              </el-form-item>
            </template>
            
            <template v-if="config.operationType === 'BATCH_UPDATE'">
              <el-form-item label="更新方式">
                <el-radio-group v-model="config.sqlDetailConfig.updateMode">
                  <el-radio value="CASE_WHEN">CASE WHEN (推荐)</el-radio>
                  <el-radio value="FOREACH">foreach多条UPDATE</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="更新字段">
                <div class="field-actions">
                  <el-button size="small" @click="selectAllUpdateFields">全选</el-button>
                  <el-button size="small" @click="clearUpdateFields">清空</el-button>
                </div>
                <div class="field-checkbox-list">
                  <el-checkbox 
                    v-for="field in tableFields" 
                    :key="field.columnName"
                    :value="field.columnName"
                    v-model="config.sqlDetailConfig.updateFields"
                  >
                    {{ field.columnName }}
                  </el-checkbox>
                </div>
              </el-form-item>
              
              <el-form-item label="条件字段">
                <div class="field-actions">
                  <el-button size="small" @click="selectAllWhereFields">全选</el-button>
                  <el-button size="small" @click="clearWhereFields">清空</el-button>
                </div>
                <div class="field-checkbox-list">
                  <el-checkbox 
                    v-for="field in tableFields" 
                    :key="field.columnName"
                    :value="field.columnName"
                    v-model="config.sqlDetailConfig.whereFields"
                  >
                    {{ field.columnName }}
                  </el-checkbox>
                </div>
              </el-form-item>
            </template>
            
            <template v-if="config.operationType === 'BATCH_DELETE'">
              <el-form-item label="条件字段">
                <el-select v-model="config.sqlDetailConfig.deleteConditionField" style="width: 200px">
                  <el-option 
                    v-for="field in tableFields" 
                    :key="field.columnName"
                    :value="field.columnName"
                    :label="field.columnName"
                  />
                </el-select>
              </el-form-item>
            </template>
            
            <template v-if="config.operationType === 'SELECT_COMPLEX'">
              <el-form-item label="查询字段">
                <div class="field-actions">
                  <el-button size="small" @click="selectAllSelectFields">全选</el-button>
                  <el-button size="small" @click="clearSelectFields">清空</el-button>
                </div>
                <div class="field-checkbox-list">
                  <el-checkbox 
                    v-for="field in tableFields" 
                    :key="field.columnName"
                    :value="field.columnName"
                    v-model="config.dynamicSqlConfig.selectFields"
                  >
                    {{ field.columnName }}
                  </el-checkbox>
                </div>
              </el-form-item>
              
              <el-divider content-position="left">查询条件</el-divider>
              
              <el-form-item label="条件配置">
                <div class="where-conditions">
                  <div v-for="(cond, index) in config.dynamicSqlConfig.whereConditions" :key="index" class="where-condition-item">
                    <el-select v-model="cond.fieldName" placeholder="字段" style="width: 140px">
                      <el-option 
                        v-for="field in tableFields" 
                        :key="field.columnName"
                        :value="field.columnName"
                        :label="field.columnName"
                      />
                    </el-select>
                    <el-select v-model="cond.operator" placeholder="操作符" style="width: 100px">
                      <el-option value="=" label="=" />
                      <el-option value="!=" label="!=" />
                      <el-option value=">" label=">" />
                      <el-option value=">=" label=">=" />
                      <el-option value="<" label="<" />
                      <el-option value="<=" label="<=" />
                      <el-option value="LIKE" label="LIKE" />
                      <el-option value="LIKE_LEFT" label="左模糊" />
                      <el-option value="LIKE_RIGHT" label="右模糊" />
                      <el-option value="IN" label="IN" />
                      <el-option value="NOT_IN" label="NOT IN" />
                      <el-option value="BETWEEN" label="BETWEEN" />
                      <el-option value="IS_NULL" label="IS NULL" />
                      <el-option value="IS_NOT_NULL" label="IS NOT NULL" />
                    </el-select>
                    <el-checkbox v-model="cond.required" title="必填条件">必填</el-checkbox>
                    <el-button type="danger" text @click="config.dynamicSqlConfig.whereConditions.splice(index, 1)">
                      删除
                    </el-button>
                  </div>
                  <el-button size="small" @click="addWhereCondition">+ 添加条件</el-button>
                </div>
              </el-form-item>
              
              <el-divider content-position="left">排序配置</el-divider>
              
              <el-form-item label="排序字段">
                <el-select v-model="config.dynamicSqlConfig.orderByField" placeholder="选择排序字段" clearable style="width: 200px">
                  <el-option 
                    v-for="field in tableFields" 
                    :key="field.columnName"
                    :value="field.columnName"
                    :label="field.columnName"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item label="排序方式">
                <el-radio-group v-model="config.dynamicSqlConfig.orderAsc">
                  <el-radio :value="false">降序 (DESC)</el-radio>
                  <el-radio :value="true">升序 (ASC)</el-radio>
                </el-radio-group>
              </el-form-item>
            </template>
          </el-form>
        </div>
        
        <div v-show="currentStep === 3" class="step-panel preview-panel">
          <el-tabs v-model="previewTab">
            <el-tab-pane label="Mapper接口" name="mapper">
              <pre class="code-preview">{{ previewResult?.mapperMethod || '' }}</pre>
            </el-tab-pane>
            <el-tab-pane label="XML SQL" name="xml">
              <pre class="code-preview xml-code">{{ previewResult?.xmlSql || '' }}</pre>
            </el-tab-pane>
            <el-tab-pane label="DTO类" name="dto" v-if="config.paramConfig.paramType === 'LIST_DTO'">
              <pre class="code-preview">{{ previewResult?.dtoClass || '' }}</pre>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
      
      <div class="step-actions">
        <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="currentStep < 3" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="currentStep === 3" type="success" @click="copyCode">复制代码</el-button>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const currentStep = ref(0)
const previewTab = ref('mapper')

const connections = ref<any[]>([])
const tables = ref<any[]>([])
const tableFields = ref<any[]>([])
const selectedDtoFields = ref<string[]>([])
const previewResult = ref<any>(null)

const config = reactive({
  connectionId: '',
  tableName: '',
  methodName: '',
  methodDesc: '',
  operationType: 'BATCH_INSERT',
  returnType: 'int',
  basePackage: 'com.example.generator',
  paramConfig: {
    paramType: 'LIST_ENTITY',
    dtoClassName: '',
    selectedFields: [],
    basicParams: [] as any[]
  },
  sqlDetailConfig: {
    insertFields: [] as string[],
    useSelectKey: false,
    uniqueKeyField: '',
    updateOnDuplicateFields: [] as string[],
    updateMode: 'CASE_WHEN',
    updateFields: [] as string[],
    whereFields: [] as string[],
    deleteConditionField: 'id'
  },
  dynamicSqlConfig: {
    enableDynamicWhere: false,
    whereConditions: [],
    enableJoin: false,
    joinConfigs: [],
    enableGroupBy: false,
    groupByFields: [],
    selectFunctions: [],
    orderByField: '',
    orderAsc: true
  }
})

const loadConnections = async () => {
  try {
    const { data } = await axios.get('/api/sql-builder/connections')
    if (data.code === 200) {
      connections.value = data.data || []
    }
  } catch (error) {
    console.error('加载连接失败', error)
  }
}

const onConnectionChange = async () => {
  tables.value = []
  config.tableName = ''
  if (!config.connectionId) return
  
  try {
    const { data } = await axios.get('/api/database/tables', {
      params: { connectionId: config.connectionId }
    })
    if (data.code === 200) {
      tables.value = data.data || []
    }
  } catch (error) {
    ElMessage.error('获取表列表失败')
  }
}

const onTableChange = async () => {
  tableFields.value = []
  if (!config.connectionId || !config.tableName) return
  
  try {
    const { data } = await axios.get('/api/sql-builder/table-fields', {
      params: {
        connectionId: config.connectionId,
        tableName: config.tableName
      }
    })
    if (data.code === 200) {
      tableFields.value = data.data || []
      config.sqlDetailConfig.insertFields = tableFields.value.map((f: any) => f.columnName)
    }
  } catch (error) {
    ElMessage.error('获取字段失败')
  }
  
  if (config.tableName) {
    const parts = config.tableName.split('_')
    const className = parts.map(p => p.charAt(0).toUpperCase() + p.slice(1).toLowerCase()).join('')
    config.paramConfig.dtoClassName = className + 'DTO'
  }
}

const addBasicParam = () => {
  config.paramConfig.basicParams.push({
    paramName: '',
    paramType: 'String',
    description: ''
  })
}

const selectAllDtoFields = () => {
  selectedDtoFields.value = tableFields.value.map((f: any) => f.columnName)
}

const clearDtoFields = () => {
  selectedDtoFields.value = []
}

const selectAllInsertFields = () => {
  config.sqlDetailConfig.insertFields = tableFields.value.map((f: any) => f.columnName)
}

const clearInsertFields = () => {
  config.sqlDetailConfig.insertFields = []
}

const selectAllUpdateOnDuplicateFields = () => {
  config.sqlDetailConfig.updateOnDuplicateFields = tableFields.value.map((f: any) => f.columnName)
}

const clearUpdateOnDuplicateFields = () => {
  config.sqlDetailConfig.updateOnDuplicateFields = []
}

const selectAllUpdateFields = () => {
  config.sqlDetailConfig.updateFields = tableFields.value.map((f: any) => f.columnName)
}

const clearUpdateFields = () => {
  config.sqlDetailConfig.updateFields = []
}

const selectAllWhereFields = () => {
  config.sqlDetailConfig.whereFields = tableFields.value.map((f: any) => f.columnName)
}

const clearWhereFields = () => {
  config.sqlDetailConfig.whereFields = []
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const nextStep = async () => {
  if (currentStep.value === 0) {
    if (!config.connectionId || !config.tableName || !config.methodName) {
      ElMessage.warning('请填写完整的基础信息')
      return
    }
  }
  
  if (currentStep.value === 1) {
    if (config.paramConfig.paramType === 'LIST_DTO') {
      config.paramConfig.selectedFields = tableFields.value
        .filter((f: any) => selectedDtoFields.value.includes(f.columnName))
        .map((f: any) => ({
          columnName: f.columnName,
          fieldName: f.fieldName,
          javaType: f.javaType,
          columnComment: f.columnComment,
          selected: true
        }))
    }
  }
  
  if (currentStep.value === 2) {
    await generatePreview()
  }
  
  if (currentStep.value < 3) {
    currentStep.value++
  }
}

const generatePreview = async () => {
  try {
    const { data } = await axios.post('/api/sql-builder/preview', config)
    if (data.code === 200) {
      previewResult.value = data.data
    } else {
      ElMessage.error(data.message || '生成失败')
    }
  } catch (error) {
    ElMessage.error('生成失败')
  }
}

const copyCode = async () => {
  let code = ''
  if (previewTab.value === 'mapper') {
    code = previewResult.value?.mapperMethod || ''
  } else if (previewTab.value === 'xml') {
    code = previewResult.value?.xmlSql || ''
  } else if (previewTab.value === 'dto') {
    code = previewResult.value?.dtoClass || ''
  }
  
  if (code) {
    await navigator.clipboard.writeText(code)
    ElMessage.success('已复制到剪贴板')
  }
}

onMounted(() => {
  loadConnections()
})
</script>

<style scoped>
.sql-builder-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.page-header {
  height: 60px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.page-main {
  flex: 1;
  padding: 24px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.steps-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.step-content {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 24px;
  overflow: auto;
}

.step-panel {
  max-width: 800px;
}

.field-actions {
  margin-bottom: 8px;
  display: flex;
  gap: 8px;
}

.field-checkbox-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.basic-params-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.param-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-panel {
  height: 100%;
}

.code-preview {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 8px;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
  line-height: 1.5;
  overflow: auto;
  max-height: 500px;
  white-space: pre-wrap;
}

.xml-code {
  color: #9cdcfe;
}

.step-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>