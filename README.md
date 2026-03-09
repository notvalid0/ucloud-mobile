# UCloud Mobile - 云邮作业助手

<div align="center">

📱 A third-party application for BUPT Ucloud

[![Platform](https://img.shields.io/badge/platform-Android-blue.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-purple.svg)](https://kotlinlang.org/)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://developer.android.com/studio/releases/platforms)
[![Version](https://img.shields.io/badge/version-1.0.0-orange.svg)]()

</div>

---

## 📖 项目简介

UCloud Mobile 是一个第三方 Android 客户端，帮助北邮学生便捷地查看和管理日常作业。通过现代化的技术栈和基于Material Design3的简介UI设计，提供流畅的作业查询体验。

### ✨ 主要特性

- 🔐 **安全登录** - 支持记住账号功能，使用 EncryptedSharedPreferences 加密存储
- 📝 **作业列表** - 清晰展示未完成作业，支持下拉刷新
- 📊 **作业详情** - 完整显示作业要求、截止日期等详细信息
- 🔍 **智能搜索** - 支持课程和作业搜索
- 🔔 **定时刷新** - 后台自动更新作业信息（WorkManager）
- 🎨 **Material Design 3** - 遵循最新 Material You 设计规范
- 🌙 **现代架构** - MVVM + Clean Architecture，代码清晰易维护

---

## 🏗️ 技术架构

### 核心技术栈

| 分类        | 技术                                                 |
| --------- | -------------------------------------------------- |
| **开发语言**  | Kotlin 1.9.25                                      |
| **UI 框架** | Jetpack Compose + ViewBinding                      |
| **架构模式**  | MVVM (Model-View-ViewModel)                        |
| **网络请求**  | Retrofit + OkHttp                                  |
| **异步处理**  | Kotlin Coroutines + Flow                           |
| **依赖注入**  | ViewModel 委托注入                                     |
| **数据解析**  | Gson                                               |
| **本地存储**  | DataStore Preferences + EncryptedSharedPreferences |
| **后台任务**  | WorkManager                                        |
| **图片加载**  | Coil                                               |

### 项目结构

```
app/src/main/java/com/youxam/ucloudhomework/
├── adapter/              # RecyclerView 适配器
├── model/                # 数据模型
│   ├── ApiResponse.kt    # API 响应封装
│   ├── HomeworkDetail.kt # 作业详情
│   ├── HomeworkItem.kt   # 作业条目
│   ├── SearchResult.kt   # 搜索结果
│   └── User.kt           # 用户凭证
├── network/              # 网络层
│   ├── NetworkResult.kt  # 网络结果封装
│   ├── RetrofitClient.kt # Retrofit 客户端配置
│   └── UCloudApiService.kt # API 接口定义
├── repository/           # 数据仓库层
│   └── HomeworkRepository.kt
├── viewmodel/            # ViewModel 层
│   ├── LoginViewModel.kt
│   ├── MainViewModel.kt
│   └── HomeworkDetailViewModel.kt
├── util/                 # 工具类
│   └── RefreshWorker.kt  # 后台刷新任务
├── LoginActivity.kt      # 登录页面
├── MainActivity.kt       # 主页面
├── HomeworkDetailActivity.kt # 作业详情页
└── UCloudApplication.kt  # Application 入口
```

### 架构图

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Activity  │────▶│  ViewModel   │────▶│ Repository  │
│   (View)    │◀────│  (Logic)     │◀────│   (Data)    │
└─────────────┘     └──────────────┘     └─────────────┘
                           │                    │
                           ▼                    ▼
                    ┌──────────────┐     ┌─────────────┐
                    │    Model     │     │  Network    │
                    │   (State)    │     │   (Retrofit)│
                    └──────────────┘     └─────────────┘
```

---

## 🚀 快速开始

### 环境要求

- **Android Studio**: Hedgehog (2023.1.1) 或更高版本
- **JDK**: 17+
- **Android SDK**: 
  - Compile SDK: 34
  - Min SDK: 26 (Android 8.0)
  - Target SDK: 34
- **Gradle**: 8.7.3
- **Kotlin**: 1.9.25

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/your-username/ucloud-mobile.git
cd ucloud-mobile
```

#### 2. 打包应用

```bash
chmod +x ./gradlew
./gradlew assembleDebug
```

#### 3. 安装应用

    可以在`/app/build/outputs/apk/debug`中寻找`app-debug.apk`，也可以使用`adb`

```bash
adb install -r /app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 使用说明

### 首次使用

1. **登录账号**
   
   - 打开应用后自动跳转到登录页
   - 输入学号和密码
   - 勾选"记住我"可保存账号（加密存储）
   - 点击登录按钮

2. **查看作业**
   
   - 登录成功后进入主页面
   - 默认显示所有未完成作业
   - 下拉可刷新作业列表
   - 点击作业条目查看详情

3. **后台刷新**
   
   - 应用会定时自动更新作业信息
   - 首次启动时会安排刷新任务
   - 可在设置中调整刷新频率

### 功能菜单

主页右下角的浮动菜单提供以下功能：

- 🔄 **刷新** - 手动刷新作业列表
- 🚪 **退出登录** - 清除登录信息并返回登录页

---

## ⚙️ 构建配置

### 关键依赖版本

```kotlin
// Core Android
androidx.core:core-ktx:1.12.0
androidx.appcompat:appcompat:1.6.1
androidx.activity:activity-ktx:1.8.2

// Material Design 3
material:1.11.0
compose.material3:material3:1.1.2

// Jetpack Compose
compose-bom:2024.02.00

// Networking
retrofit:2.9.0
okhttp:4.12.0
gson:2.10.1

// Coroutines
kotlinx-coroutines:1.7.3

// Lifecycle
lifecycle-viewmodel:2.6.2
lifecycle-livedata:2.6.2

// Security
security-crypto:1.1.0-alpha06

// WorkManager
work-runtime-ktx:2.9.0
```

### 编译选项

```kotlin
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlinOptions {
    jvmTarget = "17"
}
```

### 混淆配置

Release 版本已启用代码混淆和资源压缩：

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

---

## 🛠️ 开发指南

### 添加新页面

1. 创建新的 Activity 和对应的布局文件
2. 在 `AndroidManifest.xml` 中注册
3. 创建对应的 ViewModel
4. 实现导航逻辑

### 调用新 API

1. 在 `UCloudApiService.kt` 中添加接口方法
2. 在 `HomeworkRepository` 中添加对应的数据获取方法
3. 在 ViewModel 中调用并暴露给 View

### 网络错误处理

所有网络请求都通过 `NetworkResult` 封装：

```kotlin
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val message: String, val code: Int? = null) : NetworkResult<T>()
}
```

使用协程的 `Flow` 进行状态观察：

```kotlin
lifecycleScope.launch {
    viewModel.homeworkState.collect { state ->
        when (state) {
            is HomeworkState.Success -> // 显示数据
            is HomeworkState.Error -> // 显示错误
            is HomeworkState.Loading -> // 显示加载动画
        }
    }
}
```



---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📧 联系方式

如有问题或建议，请通过以下方式联系：

- Email: houmingxuan6@gmail.com
- Issues: [GitHub Issues](https://github.com/notvalid0/ucloud-mobile/issues)

---

## 🙏 致谢

- [YouXam Ucloud Api](https://github.com/youxam/ucloud/)

- [JetBrains Kotlin](https://kotlinlang.org/)

- [Android Jetpack](https://developer.android.com/jetpack)

- [Square Retrofit](https://square.github.io/retrofit/)

- [Material Components](https://material.io/)

---

<div align="center">

**Made with ❤️**

如果这个项目对你有帮助，请给一个 ⭐️ Star 支持！

</div>
