# Code Generator

基于 Spring Boot + Vue 3 的代码生成器，支持连接 MySQL 数据库，选择表生成 MyBatis-Plus 代码。

## 功能特性

- MySQL 数据库连接管理
- 可视化选择数据表
- 基于模板生成 Entity、Mapper、Service、ServiceImpl、Controller
- 代码实时预览（Monaco Editor）
- 模板在线编辑
- ZIP 打包下载 / 直接生成到目录
- 本地配置持久化

## 技术栈

### 后端
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.3
- FreeMarker（模板引擎）
- MySQL 8.0

### 前端
- Vue 3 + TypeScript
- Element Plus
- Monaco Editor
- Vite

## 项目结构

```
code-generator/
├── backend/                        # 后端项目
│   ├── src/main/java/com/example/generator/
│   │   ├── controller/             # API 控制器
│   │   ├── service/                # 业务服务
│   │   ├── model/                  # 数据模型
│   │   ├── common/                 # 公共组件
│   │   └── config/                 # 配置类
│   └── src/main/resources/
│       └── templates/              # 代码模板
│
├── frontend/                       # 前端项目
│   ├── src/
│   │   ├── views/                  # 页面
│   │   ├── components/             # 组件
│   │   ├── api/                    # API 接口
│   │   ├── stores/                 # 状态管理
│   │   └── styles/                 # 样式
│   └── package.json
│
└── README.md
```

## 快速开始

### 环境要求
- JDK 1.8+
- Node.js 18+
- MySQL 8.0+

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

前端服务将在 http://localhost:3000 启动

## 使用说明

1. 点击"新建连接"创建数据库连接
2. 选择已保存的连接
3. 在左侧表列表中选择要生成代码的表
4. 点击"生成预览"查看生成的代码
5. 如需修改模板，点击"编辑模板"
6. 点击"下载ZIP"或"生成到目录"导出代码

## API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/connection/list` | GET | 获取连接列表 |
| `/api/connection/save` | POST | 保存连接 |
| `/api/connection/test` | POST | 测试连接 |
| `/api/database/tables` | GET | 获取表列表 |
| `/api/generate/preview` | POST | 预览代码 |
| `/api/generate/export` | POST | 导出 ZIP |
| `/api/generate/to-dir` | POST | 生成到目录 |
| `/api/template/list` | GET | 获取模板列表 |
| `/api/template/save` | POST | 保存模板 |

## License

MIT