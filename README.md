# action

Minecraft 1.21.11 Fabric 动作模组。

## 功能

- 按 `X` 打开动作选择面板。
- 支持举右手、举左手、挥动右手、挥动左手、坐下、躺下、趴下和取消动作。
- 动作会同步到多人游戏中的其他玩家。
- 动作会改变玩家碰撞箱；躺下和趴下会变矮变长。
- 坐下不可移动；躺下速度为走路的 `0.2` 倍，趴下速度为走路的 `0.4` 倍。

## 构建

需要 Java 21。

```powershell
$env:JAVA_HOME="C:\Users\ASUS\AppData\Roaming\.minecraft\runtime\java-runtime-delta"
.\gradlew.bat build
```

构建成功后，模组 jar 会在 `build/libs/` 目录中。
