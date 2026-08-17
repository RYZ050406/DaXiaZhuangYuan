# betterinput

Minecraft 1.21.11 Fabric 书与告示牌输入增强模组。

## 功能

- 写书页面加入颜色、粗体、斜体、下划线、删除线、乱码和重置按钮。
- 写书页面支持“插入指令”，给选中文字附加点击执行的命令。
- 写书页面支持“指令列表”，管理本书中所有已插入指令。
- 指令输入框使用原版命令建议，支持 Tab 补全。
- 告示牌编辑页面加入富文本按钮。
- 告示牌支持“指令”选项，右键告示牌时执行保存的命令。

书和告示牌的保存逻辑需要客户端和服务端都安装本模组。

## 构建

需要 Java 21。

```powershell
$env:JAVA_HOME="C:\Users\ASUS\AppData\Roaming\.minecraft\runtime\java-runtime-delta"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat build
```

构建成功后，模组 jar 会在 `build/libs/` 目录中。
