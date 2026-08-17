# DaXiaZhuangYuan

Minecraft 1.21.11 Fabric 多模组仓库。

## 目录结构

```text
mods/
  action/        玩家动作与姿势模组
  betterinput/   书与告示牌富文本输入增强模组
```

每个模组都是独立的 Gradle/Fabric 项目，包含自己的 `build.gradle`、`settings.gradle` 和 Gradle Wrapper。以后新增模组时，继续放到 `mods/<mod_id>/` 下。

## 模组

### action

玩家动作面板模组。按 `X` 打开动作选择面板，支持举手、挥手、坐下、躺下、趴下和取消动作，并同步多人可见的姿势与碰撞箱变化。

项目目录：`mods/action`

### betterinput

书与告示牌输入增强模组。为写书和告示牌编辑界面加入颜色、粗体、斜体、下划线、删除线等格式工具；书支持“插入指令”和“指令列表”；告示牌支持右键执行指令。

项目目录：`mods/betterinput`

## 构建

需要 Java 21。当前本机 Minecraft Java 21 路径：

```powershell
$env:JAVA_HOME="C:\Users\ASUS\AppData\Roaming\.minecraft\runtime\java-runtime-delta"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

构建 `action`：

```powershell
cd mods\action
.\gradlew.bat build
```

构建 `betterinput`：

```powershell
cd mods\betterinput
.\gradlew.bat build
```

构建产物在对应模组的 `build/libs/` 目录中。
