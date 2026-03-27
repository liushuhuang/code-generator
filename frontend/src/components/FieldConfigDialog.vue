<template>
  <el-dialog 
    :model-value="visible" 
    :title="'配置 - ' + tableName" 
    width="900px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-tabs v-model="activeTab">
      <el-tab-pane label="字段选择" name="fields">
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
            <el-checkbox v-model="generateVO">VO</el-checkbox>
            <el-checkbox v-model="generateDTO">DTO</el-checkbox>
            <el-checkbox v-model="generateQuery">Query</el-checkbox>
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="查询配置" name="query">
        <div class="query-config">
          <div class="config-header">
            <span>选择查询字段并配置查询类型</span>
            <el-button size="small" type="primary" @click="autoAddQueryFields">自动添加</el-button>
          </div>
          
          <div class="query-field-list">
            <div v-for="(qf, index) in queryFields" :key="index" class="query-field-item">
              <el-select v-model="qf.fieldName" placeholder="选择字段" style="width: 150px">
                <el-option 
                  v-for="f in fields" 
                  :key="f.columnName" 
                  :value="f.columnName"
                  :label="f.columnName"
                />
              </el-select>
              <el-select v-model="qf.queryType" placeholder="查询类型" style="width: 120px">
                <el-option value="EQ" label="等于" />
                <el-option value="LIKE" label="模糊" />
                <el-option value="LIKE_LEFT" label="左模糊" />
                <el-option value="LIKE_RIGHT" label="右模糊" />
                <el-option value="IN" label="IN" />
                <el-option value="NOT_IN" label="NOT IN" />
                <el-option value="BETWEEN" label="范围" />
                <el-option value="GT" label="大于" />
                <el-option value="GTE" label="大于等于" />
                <el-option value="LT" label="小于" />
                <el-option value="LTE" label="小于等于" />
                <el-option value="IS_NULL" label="为空" />
                <el-option value="IS_NOT_NULL" label="不为空" />
              </el-select>
              <el-checkbox v-model="qf.required">必填</el-checkbox>
              <el-button type="danger" size="small" text @click="queryFields.splice(index, 1)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            
            <el-button size="small" @click="addQueryField">+ 添加查询字段</el-button>
          </div>
          
          <el-divider />
          
          <el-form label-width="100px">
            <el-form-item label="启用分页">
              <el-switch v-model="enablePage" />
            </el-form-item>
            <el-form-item label="默认排序字段">
              <el-select v-model="defaultSortField" placeholder="选择字段" clearable style="width: 200px">
                <el-option 
                  v-for="f in fields" 
                  :key="f.columnName" 
                  :value="f.columnName"
                  :label="f.columnName"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="排序方向">
              <el-radio-group v-model="defaultSortOrder">
                <el-radio value="ASC">升序</el-radio>
                <el-radio value="DESC">降序</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="自定义方法" name="methods">
        <div class="method-config">
          <div class="method-list">
            <div v-for="(method, index) in customMethods" :key="index" class="method-item">
              <div class="method-header">
                <el-input v-model="method.methodName" placeholder="方法名" style="width: 150px" />
                <el-select v-model="method.methodType" placeholder="类型" style="width: 100px">
                  <el-option value="SINGLE" label="单条" />
                  <el-option value="LIST" label="列表" />
                  <el-option value="PAGE" label="分页" />
                  <el-option value="COUNT" label="统计" />
                  <el-option value="EXISTS" label="存在" />
                  <el-option value="DELETE" label="删除" />
                  <el-option value="UPDATE" label="更新" />
                </el-select>
                <el-input v-model="method.description" placeholder="方法描述" style="width: 200px" />
                <el-button type="danger" size="small" text @click="customMethods.splice(index, 1)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <div class="method-params">
                <span class="param-label">参数：</span>
                <div v-for="(param, pIndex) in method.params" :key="pIndex" class="param-item">
                  <el-input v-model="param.paramName" placeholder="参数名" style="width: 100px" />
                  <el-select v-model="param.paramType" placeholder="类型" style="width: 100px">
                    <el-option value="String" label="String" />
                    <el-option value="Integer" label="Integer" />
                    <el-option value="Long" label="Long" />
                    <el-option value="Double" label="Double" />
                    <el-option value="Boolean" label="Boolean" />
                    <el-option value="Date" label="Date" />
                  </el-select>
                  <el-select v-model="param.columnName" placeholder="对应字段" style="width: 120px">
                    <el-option 
                      v-for="f in fields" 
                      :key="f.columnName" 
                      :value="f.columnName"
                      :label="f.columnName"
                    />
                  </el-select>
                  <el-button type="danger" size="small" text @click="method.params.splice(pIndex, 1)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
                <el-button size="small" text @click="addMethodParam(method)">+ 参数</el-button>
              </div>
            </div>
            
            <el-button type="primary" size="small" @click="addCustomMethod">+ 添加方法</el-button>
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="枚举配置" name="enums">
        <div class="enum-config">
          <div class="enum-list">
            <div v-for="(enumConfig, index) in enumConfigs" :key="index" class="enum-item">
              <div class="enum-header">
                <el-select v-model="enumConfig.columnName" placeholder="选择字段" style="width: 150px">
                  <el-option 
                    v-for="f in fields" 
                    :key="f.columnName" 
                    :value="f.columnName"
                    :label="f.columnName"
                  />
                </el-select>
                <el-input v-model="enumConfig.enumName" placeholder="枚举类名" style="width: 150px" />
                <el-button type="danger" size="small" text @click="enumConfigs.splice(index, 1)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <div class="enum-values">
                <div v-for="(desc, code) in enumConfig.descriptions" :key="code" class="enum-value-item">
                  <el-input :model-value="code" disabled style="width: 80px" />
                  <el-input 
                    :model-value="enumConfig.values[code]" 
                    placeholder="枚举名" 
                    style="width: 100px"
                    @update:model-value="updateEnumValue(enumConfig, code, $event)"
                  />
                  <el-input 
                    :model-value="desc" 
                    placeholder="描述" 
                    style="width: 150px"
                    @update:model-value="updateEnumDesc(enumConfig, code, $event)"
                  />
                  <el-button type="danger" size="small" text @click="removeEnumValue(enumConfig, code)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
                <div class="add-enum-value">
                  <el-input v-model="newEnumCode" placeholder="值" style="width: 80px" />
                  <el-input v-model="newEnumName" placeholder="枚举名" style="width: 100px" />
                  <el-input v-model="newEnumDesc" placeholder="描述" style="width: 150px" />
                  <el-button size="small" type="primary" @click="addEnumValue(enumConfig)">添加</el-button>
                </div>
              </div>
            </div>
            
            <el-button type="primary" size="small" @click="addEnumConfig">+ 添加枚举配置</el-button>
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="关联配置" name="relations">
        <div class="relation-config">
          <div class="relation-list">
            <div v-for="(rel, index) in relations" :key="index" class="relation-item">
              <el-select v-model="rel.sourceColumn" placeholder="当前字段" style="width: 120px">
                <el-option 
                  v-for="f in fields" 
                  :key="f.columnName" 
                  :value="f.columnName"
                  :label="f.columnName"
                />
              </el-select>
              <el-input v-model="rel.targetTable" placeholder="关联表" style="width: 120px" />
              <el-input v-model="rel.targetColumn" placeholder="关联字段" style="width: 100px" />
              <el-radio-group v-model="rel.relationType">
                <el-radio value="ONE_TO_ONE">一对一</el-radio>
                <el-radio value="ONE_TO_MANY">一对多</el-radio>
              </el-radio-group>
              <el-input v-model="rel.fieldName" placeholder="字段名" style="width: 100px" />
              <el-select v-model="rel.fetchType" style="width: 100px">
                <el-option value="EAGER" label="立即加载" />
                <el-option value="LAZY" label="延迟加载" />
              </el-select>
              <el-button type="danger" size="small" text @click="relations.splice(index, 1)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            
            <el-button type="primary" size="small" @click="addRelation">+ 添加关联</el-button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="confirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import axios from 'axios'

interface ColumnInfo {
  columnName: string
  columnType: string
  columnComment: string
  javaType: string
  fieldName: string
  primaryKey: boolean
}

interface QueryFieldConfig {
  fieldName: string
  queryType: string
  required: boolean
}

interface MethodParam {
  paramName: string
  paramType: string
  columnName: string
}

interface CustomMethod {
  methodName: string
  methodType: string
  params: MethodParam[]
  description: string
}

interface EnumConfig {
  columnName: string
  enumName: string
  values: Record<string, string>
  descriptions: Record<string, string>
}

interface TableRelation {
  sourceColumn: string
  targetTable: string
  targetColumn: string
  relationType: string
  fieldName: string
  fetchType: string
}

const props = defineProps<{
  visible: boolean
  connectionId: string
  tableName: string
  config?: any
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'confirm', config: any): void
}>()

const activeTab = ref('fields')
const fields = ref<ColumnInfo[]>([])
const selectedFields = ref<string[]>([])
const generateVO = ref(false)
const generateDTO = ref(false)
const generateQuery = ref(false)
const loading = ref(false)

const queryFields = ref<QueryFieldConfig[]>([])
const enablePage = ref(true)
const defaultSortField = ref('')
const defaultSortOrder = ref('DESC')

const customMethods = ref<CustomMethod[]>([])

const enumConfigs = ref<EnumConfig[]>([])
const newEnumCode = ref('')
const newEnumName = ref('')
const newEnumDesc = ref('')

const relations = ref<TableRelation[]>([])

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

const addQueryField = () => {
  queryFields.value.push({
    fieldName: '',
    queryType: 'EQ',
    required: false
  })
}

const autoAddQueryFields = () => {
  queryFields.value = fields.value
    .filter(f => !f.primaryKey)
    .map(f => ({
      fieldName: f.columnName,
      queryType: 'EQ',
      required: false
    }))
}

const addCustomMethod = () => {
  customMethods.value.push({
    methodName: '',
    methodType: 'LIST',
    params: [],
    description: ''
  })
}

const addMethodParam = (method: CustomMethod) => {
  method.params.push({
    paramName: '',
    paramType: 'String',
    columnName: ''
  })
}

const addEnumConfig = () => {
  enumConfigs.value.push({
    columnName: '',
    enumName: '',
    values: {},
    descriptions: {}
  })
}

const addEnumValue = (enumConfig: EnumConfig) => {
  if (!newEnumCode.value) return
  enumConfig.values[newEnumCode.value] = newEnumName.value
  enumConfig.descriptions[newEnumCode.value] = newEnumDesc.value
  newEnumCode.value = ''
  newEnumName.value = ''
  newEnumDesc.value = ''
}

const updateEnumValue = (enumConfig: EnumConfig, code: string, value: string) => {
  enumConfig.values[code] = value
}

const updateEnumDesc = (enumConfig: EnumConfig, code: string, value: string) => {
  enumConfig.descriptions[code] = value
}

const removeEnumValue = (enumConfig: EnumConfig, code: string) => {
  delete enumConfig.values[code]
  delete enumConfig.descriptions[code]
}

const addRelation = () => {
  relations.value.push({
    sourceColumn: '',
    targetTable: '',
    targetColumn: '',
    relationType: 'ONE_TO_ONE',
    fieldName: '',
    fetchType: 'EAGER'
  })
}

const confirm = () => {
  if (selectedFields.value.length === 0) {
    ElMessage.warning('请至少选择一个字段')
    return
  }
  
  emit('confirm', {
    tableName: props.tableName,
    selectedFields: selectedFields.value,
    generateVO: generateVO.value,
    generateDTO: generateDTO.value,
    generateQuery: generateQuery.value,
    queryFields: queryFields.value.filter(q => q.fieldName),
    enablePage: enablePage.value,
    defaultSortField: defaultSortField.value,
    defaultSortOrder: defaultSortOrder.value,
    customMethods: customMethods.value.filter(m => m.methodName),
    enumConfigs: enumConfigs.value.filter(e => e.columnName && e.enumName),
    relations: relations.value.filter(r => r.sourceColumn && r.targetTable)
  })
  emit('update:visible', false)
}

watch(() => props.visible, (val) => {
  if (val) {
    loadFields()
    if (props.config) {
      selectedFields.value = [...(props.config.selectedFields || [])]
      generateVO.value = props.config.generateVO || false
      generateDTO.value = props.config.generateDTO || false
      generateQuery.value = props.config.generateQuery || false
      queryFields.value = [...(props.config.queryFields || [])]
      enablePage.value = props.config.enablePage !== false
      defaultSortField.value = props.config.defaultSortField || ''
      defaultSortOrder.value = props.config.defaultSortOrder || 'DESC'
      customMethods.value = [...(props.config.customMethods || [])]
      enumConfigs.value = [...(props.config.enumConfigs || [])]
      relations.value = [...(props.config.relations || [])]
    } else {
      selectedFields.value = []
      generateVO.value = false
      generateDTO.value = false
      generateQuery.value = false
      queryFields.value = []
      enablePage.value = true
      defaultSortField.value = ''
      defaultSortOrder.value = 'DESC'
      customMethods.value = []
      enumConfigs.value = []
      relations.value = []
    }
    activeTab.value = 'fields'
  }
})
</script>

<style scoped>
.field-config, .query-config, .method-config, .enum-config, .relation-config {
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

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.query-field-list, .method-list, .enum-list, .relation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.query-field-item, .relation-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 6px;
}

.method-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.method-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.method-params {
  padding-left: 12px;
}

.param-label {
  font-size: 12px;
  color: #909399;
}

.param-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.enum-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.enum-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.enum-values {
  padding-left: 12px;
}

.enum-value-item, .add-enum-value {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
</style>