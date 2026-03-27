# Code Generator

基于 Spring Boot + Vue 3 的代码生成器，支持连接 MySQL 数据库，灵活配置生成 MyBatis-Plus 或 MyBatis 代码。

## 功能特性

### 核心功能
- MySQL 数据库连接管理
- 可视化选择数据表
- 代码实时预览
- 模板在线编辑
- ZIP 打包下载 / 直接生成到目录
- 配置导入/导出

### 代码生成
- **ORM 框架切换**：支持 MyBatis-Plus 和原生 MyBatis 两种模式
- **多文件生成**：Entity、Mapper、Service、ServiceImpl、Controller
- **扩展对象生成**：VO（视图对象）、DTO（数据传输对象）、Query（查询对象）
- **字段选择**：每张表可单独选择字段生成 VO/DTO/Query

### 策略配置
- **代码风格**：Lombok、Swagger 注解、验证注解、链式调用、序列化
- **日期类型**：LocalDateTime / Date
- **命名策略**：
  - 移除表前缀（如 `t_`, `sys_`，支持多个）
  - 实体类前缀/后缀

### SQL 构建器
- **批量操作**：批量插入、批量插入或更新、批量更新、批量删除
- **入参配置**：List\<Entity\>、List\<DTO\>、基础类型参数
- **代码生成**：自动生成 Mapper 接口方法、XML SQL、DTO 类

## 技术栈

### 后端
- JDK 1.8
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.3
- FreeMarker（模板引擎）
- MySQL 8.0
- Hutool（工具库）

### 前端
- Vue 3 + TypeScript
- Element Plus
- Vite

## 项目结构

```
code-generator/
├── backend/                           # 后端项目
│   ├── src/main/java/com/example/generator/
│   │   ├── controller/                # API 控制器
│   │   │   ├── ConnectionController   # 连接管理
│   │   │   ├── DatabaseController     # 数据库操作
│   │   │   ├── GeneratorController    # 代码生成
│   │   │   ├── TemplateController     # 模板管理
│   │   │   ├── ConfigController       # 配置导入导出
│   │   │   └── SqlBuilderController   # SQL构建器
│   │   ├── service/                   # 业务服务
│   │   │   ├── DatabaseService        # 数据库服务
│   │   │   ├── GeneratorService       # 生成服务
│   │   │   ├── TemplateService        # 模板服务
│   │   │   ├── ConfigService          # 配置服务
│   │   │   └── SqlBuilderService      # SQL构建服务
│   │   ├── model/                     # 数据模型
│   │   │   ├── ConnectionConfig       # 连接配置
│   │   │   ├── TableInfo              # 表信息
│   │   │   ├── ColumnInfo             # 列信息
│   │   │   ├── GeneratorConfig        # 生成配置
│   │   │   ├── GenerateRequest        # 生成请求
│   │   │   ├── TableGenerateConfig    # 表生成配置
│   │   │   └── SqlBuilderConfig       # SQL构建配置
│   │   └── common/                    # 公共组件
│   └── src/main/resources/
│       └── templates/                 # 代码模板
│           ├── mybatis-plus/          # MyBatis-Plus 模板
│           ├── mybatis/               # MyBatis 模板
│           └── common/                # 公共模板(VO/DTO/Query)
│
├── frontend/                          # 前端项目
│   ├── src/
│   │   ├── views/
│   │   │   ├── Home.vue               # 主页面
│   │   │   └── SqlBuilder.vue         # SQL构建器
│   │   └── components/
│   │       ├── PreviewArea.vue        # 预览区域
│   │       └── FieldConfigDialog.vue  # 字段配置弹窗
│   └── package.json
│
└── README.md
```

## 快速开始

### 环境要求
- JDK 1.8+
- Node.js 18+
- MySQL 8.0+
- Maven 3.6+

### 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 http://localhost:5173 启动

## 使用指南

### 1. 创建数据库连接

1. 点击左侧「新建」按钮
2. 填写连接信息：
   - 连接名称（必填）
   - 主机地址（默认 localhost）
   - 端口（默认 3306）
   - 数据库名（必填）
   - 用户名/密码
3. 点击「测试连接」验证
4. 点击「保存」

### 2. 选择数据表

1. 点击已保存的连接
2. 左侧显示该数据库所有表
3. 勾选需要生成代码的表

### 3. 配置字段（可选）

点击表旁的「配置」按钮，可：
- 选择生成 VO、DTO、Query 对象
- 选择包含的字段
- DTO 自动添加验证注解

### 4. 生成设置

点击右上角设置图标，配置：

#### 基础配置
- 基础包名：如 `com.example.project`
- 作者名称
- 输出目录：留空则使用 ZIP 下载

#### ORM 框架
- **MyBatis-Plus**：生成 BaseMapper、IService 等
- **MyBatis**：生成 Mapper 接口和 XML 映射文件

#### 代码风格
| 选项 | 说明 |
|------|------|
| Lombok | 生成 @Data、@Builder 等注解 |
| Swagger | 生成 @ApiModel、@ApiModelProperty 注解 |
| 验证注解 | 生成 @NotNull、@NotBlank 等注解 |
| 链式调用 | 生成 @Accessors(chain = true) |
| 序列化 | 实现 Serializable 接口 |
| 日期类型 | LocalDateTime / Date |

#### 命名策略
| 选项 | 示例 |
|------|------|
| 移除表前缀 | `t_user` → `User`（填写 `t_`） |
| 实体前缀 | `User` → `SysUser`（填写 `Sys`） |
| 实体后缀 | `User` → `UserEntity`（填写 `Entity`） |

### 5. 生成代码

1. 点击「生成预览」查看代码
2. 切换标签查看不同文件：Entity、Mapper、Service、Controller、VO、DTO、Query
3. 点击「编辑模板」可自定义模板
4. 导出代码：
   - 「下载 ZIP」：打包下载
   - 「生成到目录」：直接生成到指定目录

### 6. 配置导入/导出

点击右上角「导入/导出」按钮：
- **导出配置**：将所有连接和设置导出为 JSON 文件
- **导入配置**：从 JSON 文件恢复配置

### 7. SQL 构建器

点击顶部「SQL构建器」按钮，进入独立的 SQL 构建模块，用于生成复杂的 MyBatis XML SQL。

#### 支持的操作类型

| 类型 | 说明 |
|------|------|
| 批量插入 | INSERT INTO ... VALUES (...), (...) |
| 批量插入或更新 | ON DUPLICATE KEY UPDATE ... |
| 批量更新 | CASE WHEN 或 foreach 多条 UPDATE |
| 批量删除 | DELETE FROM ... WHERE id IN (...) |

#### 入参类型

- **List\<Entity\>**：使用实体类作为参数
- **List\<DTO\>**：自动生成 DTO 类
- **基础类型参数**：如 `List<Long> ids`

#### 使用步骤

1. **基础信息**：选择数据库连接、目标表、填写方法名
2. **入参配置**：选择入参类型和字段
3. **SQL配置**：配置插入/更新/删除字段
4. **预览生成**：查看生成的 Mapper 接口和 XML SQL

点击右上角「导入/导出」按钮：
- **导出配置**：将所有连接和设置导出为 JSON 文件
- **导入配置**：从 JSON 文件恢复配置

## API 接口

### 连接管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/connection/list` | GET | 获取连接列表 |
| `/api/connection/save` | POST | 保存连接 |
| `/api/connection/{id}` | DELETE | 删除连接 |
| `/api/connection/test` | POST | 测试连接 |

### 数据库操作
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/database/tables` | GET | 获取表列表 |
| `/api/database/columns` | GET | 获取表字段 |

### 代码生成
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/generate/preview` | POST | 预览代码 |
| `/api/generate/export` | POST | 导出 ZIP |
| `/api/generate/to-dir` | POST | 生成到目录 |

### 模板管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/template/list` | GET | 获取模板列表 |
| `/api/template/{group}/{name}` | GET | 获取模板内容 |
| `/api/template/save` | POST | 保存模板 |
| `/api/template/reset/{group}/{name}` | POST | 重置模板 |

### 配置管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/config/export` | GET | 导出配置 |
| `/api/config/import` | POST | 导入配置 |

### SQL 构建器
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/sql-builder/preview` | POST | 预览生成的 SQL |
| `/api/sql-builder/table-fields` | GET | 获取表字段列表 |
| `/api/sql-builder/connections` | GET | 获取连接列表 |

## 模板变量

在自定义模板时可用以下变量：

| 变量 | 说明 |
|------|------|
| `${packageName}` | 基础包名 |
| `${className}` | 类名 |
| `${tableName}` | 表名 |
| `${tableComment}` | 表注释 |
| `${author}` | 作者 |
| `${date}` | 日期 |
| `${columns}` | 字段列表 |
| `${imports}` | 导入列表 |
| `${primaryKey}` | 主键字段 |
| `${enableLombok}` | 是否启用 Lombok |
| `${enableSwagger}` | 是否启用 Swagger |
| `${enableValidation}` | 是否启用验证注解 |
| `${enableChain}` | 是否启用链式调用 |
| `${serializable}` | 是否序列化 |
| `${dateType}` | 日期类型 |

## License

MIT