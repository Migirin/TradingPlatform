# Android 模拟器相机支持说明

## 支持相机的模拟器

### 1. Android Studio 官方模拟器（推荐）

**Android Studio 自带的模拟器支持相机功能**，但需要正确配置：

#### 配置步骤：

1. **创建 AVD（Android Virtual Device）时**：
   - 在 AVD Manager 中创建新设备
   - 选择设备配置时，确保选择了支持相机的设备（如 Pixel 系列）
   - 在 "Show Advanced Settings" 中：
     - **Front Camera**: 选择 "Webcam0" 或 "VirtualScene"
     - **Back Camera**: 选择 "Webcam0" 或 "VirtualScene"

2. **使用 Webcam（真实摄像头）**：
   - 选择 "Webcam0" 会使用你电脑的摄像头
   - 这是最接近真实设备的体验

3. **使用虚拟场景**：
   - 选择 "VirtualScene" 会显示一个虚拟的 3D 场景
   - 这就是你看到的那个客厅场景

#### 如何检查模拟器是否支持相机：

1. 打开模拟器
2. 进入设置 → 应用 → 相机
3. 如果能看到相机应用，说明支持相机

### 2. 其他模拟器

- **Genymotion**: 支持相机，但需要配置
- **BlueStacks**: 主要用于游戏，相机支持有限
- **Nox Player**: 相机支持有限

## 解决黑色屏幕问题

### 方法1：配置模拟器使用 Webcam

1. **关闭当前模拟器**
2. **打开 AVD Manager**：
   - Tools → Device Manager
   - 或点击工具栏的设备图标
3. **编辑 AVD**：
   - 点击设备右侧的编辑图标（铅笔图标）
   - 点击 "Show Advanced Settings"
   - 找到 "Camera" 部分
   - **Front Camera**: 改为 "Webcam0"
   - **Back Camera**: 改为 "Webcam0"
4. **保存并重启模拟器**

### 方法2：使用真实设备（最推荐）

真实设备是最可靠的测试方式：
- 相机功能完全支持
- 性能更好
- 更接近用户实际体验

### 方法3：使用虚拟场景（测试用）

如果只是测试功能，可以使用 "VirtualScene"：
- 虽然显示的是 3D 场景，但可以测试拍照功能
- 拍照后可以识别场景中的物体

## 推荐的模拟器配置

### 最佳配置（使用真实摄像头）：
```
设备：Pixel 6 或 Pixel 7
系统镜像：Android 13 或更高
Front Camera: Webcam0
Back Camera: Webcam0
```

### 测试配置（使用虚拟场景）：
```
设备：Pixel 6
系统镜像：Android 13
Front Camera: VirtualScene
Back Camera: VirtualScene
```

## 检查模拟器相机状态

在模拟器中运行以下命令检查相机：

```bash
adb shell dumpsys media.camera
```

或者使用相机应用测试：
1. 打开模拟器的相机应用
2. 如果能看到画面，说明相机工作正常
3. 如果显示错误，说明相机未正确配置

## 常见问题

### Q: 为什么我的模拟器显示黑色屏幕？

A: 可能的原因：
1. 相机未配置（默认可能是 "None"）
2. 电脑没有摄像头（如果选择了 Webcam0）
3. 相机权限未授予

### Q: 如何知道模拟器是否支持相机？

A: 
1. 检查 AVD 配置中的相机设置
2. 打开模拟器的相机应用测试
3. 查看 Logcat 中的相机相关日志

### Q: VirtualScene 是什么？

A: 
- 这是 Android 模拟器提供的虚拟相机场景
- 显示一个固定的 3D 场景（客厅）
- 用于测试相机功能，但不显示真实画面

### Q: 可以使用手机作为测试设备吗？

A: 
- **强烈推荐！**
- 在真实设备上测试最准确
- 通过 USB 连接，启用 USB 调试即可

## 推荐方案

1. **开发阶段**：使用 Android Studio 模拟器 + Webcam0
2. **测试阶段**：使用真实 Android 设备
3. **演示阶段**：使用真实设备或配置好的模拟器

## 快速配置步骤

1. **打开 AVD Manager**
2. **编辑你的 AVD**
3. **点击 "Show Advanced Settings"**
4. **找到 Camera 部分**
5. **设置 Front Camera 和 Back Camera 为 "Webcam0"**
6. **保存并重启模拟器**

现在你的模拟器应该能正常使用相机了！


