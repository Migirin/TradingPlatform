# 网络连接问题排查指南

## 问题
`UnknownHostException: Unable to resolve host "bsxlzefzqfbpcwuxcoek.supabase.co"`

## 解决方案

### 1. 检查设备网络连接

**如果是 Android 模拟器：**
- 确保模拟器可以访问互联网
- 在模拟器中打开浏览器，访问 https://www.google.com 测试网络
- 如果无法访问，检查：
  - 模拟器的网络设置
  - 主机电脑的网络连接
  - 防火墙是否阻止了模拟器的网络访问

**如果是真机：**
- 确保手机已连接到 WiFi 或移动数据
- 在手机浏览器中访问 https://bsxlzefzqfbpcwuxcoek.supabase.co 测试
- 如果无法访问，检查：
  - WiFi 是否正常
  - 移动数据是否开启
  - 是否有 VPN 或代理设置

### 2. 检查 DNS 解析

在命令行中测试 DNS 解析：

**Windows PowerShell:**
```powershell
nslookup bsxlzefzqfbpcwuxcoek.supabase.co
```

**或者使用 ping:**
```powershell
ping bsxlzefzqfbpcwuxcoek.supabase.co
```

如果无法解析，可能是：
- DNS 服务器问题
- 网络配置问题

### 3. 检查防火墙和代理

- 确保防火墙没有阻止应用访问网络
- 如果有代理设置，确保应用可以访问外网
- 检查公司/学校网络是否阻止了 Supabase 域名

### 4. 临时解决方案：使用本地数据

如果网络问题暂时无法解决，应用会：
- 自动使用本地数据库中的数据
- 商品仍然可以正常显示
- 只是无法同步到 Supabase

### 5. 测试网络连接

在应用中添加网络测试功能，或者：
1. 在 Android Studio 的 Logcat 中查看网络请求日志
2. 检查是否有其他网络相关的错误

## 验证步骤

1. **测试网络连接：**
   - 在设备浏览器中访问：https://bsxlzefzqfbpcwuxcoek.supabase.co
   - 如果无法访问，说明是网络问题

2. **检查应用权限：**
   - 确保应用有 INTERNET 权限
   - 在 AndroidManifest.xml 中应该有：`<uses-permission android:name="android.permission.INTERNET" />`

3. **重新运行应用：**
   - 重新编译并运行应用
   - 查看 Logcat 中的网络错误信息







