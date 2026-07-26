# ToolDeck

ToolDeck 是一个使用 **Kotlin + Jetpack Compose + Material 3** 构建的安卓原生工具中心。应用提供统一的工具描述、动态表单、执行结果和历史记录框架，后续增加工具时不需要重写首页与运行页面。

## 当前版本

`v0.1.0` 预览版包含 10 个完全离线的内置工具：

- JSON 格式化与压缩
- Base64 编解码
- URL 编解码
- Unix 时间戳转换
- UUID v4 批量生成
- SHA-256 文本摘要
- 文本行去重
- 命名格式转换
- JWT Header/Payload 解析
- 安全随机密码生成

## 功能

- 工具搜索、标签和分类筛选
- 收藏与最近使用
- 根据输入 Schema 自动生成工具表单
- 统一成功、失败与代码结果展示
- 结果复制和系统分享
- 最多保存 50 条执行摘要
- 敏感输入不写入历史记录
- 浅色、深色与跟随系统主题
- 手机底部导航与大屏侧边导航
- 当前版本未声明网络权限

## 技术结构

```text
app/src/main/java/com/weepwood/tooldeck
├── data/       本地偏好与历史持久化
├── engine/     工具协议、注册表和执行实现
├── model/      工具 Schema、输入和结果模型
└── ui/         Compose 页面、动态表单和主题
```

新增工具只需实现 `ToolExecutor` 并注册到 `BuiltInTools.all`。

## 本地构建

要求：JDK 17、Android SDK 36。

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

首次执行脚本时会下载 Gradle 8.13。生成的 APK 位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 发布

- PR 会自动运行单元测试、Android Lint 和 Debug APK 构建。
- `main` 分支中出现新的 `versionName` 时，发布工作流会创建对应的 GitHub 预览版本。
- 当前公开 APK 使用 Android Debug 签名，仅用于安装体验和功能验证；正式分发前应配置长期保存的专用签名密钥。

## 隐私

v0.1.0 不申请网络权限。JWT、摘要计算等标记为敏感的输入不会进入执行历史，历史只保存有限长度的结果摘要。

## License

MIT
