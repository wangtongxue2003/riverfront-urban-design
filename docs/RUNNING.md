# 运行说明

## 环境

- 已验证：Windows x64，JDK 23.0.1，Maven 3.9.9。
- 需要完整 JDK；`java -version`、`javac -version` 应均可用，`JAVA_HOME` 指向 JDK 目录。
- 包内 Maven Wrapper 固定到 Maven 3.9.9，首次执行需要联网下载 Maven 及公开依赖，不需要单独安装 Maven。
- 12 个保留的历史依赖位于 `lib/`，共约 12.23 MiB；其 SHA-256 见 `lib/SHA256SUMS.txt`。
- 当前包保留 Windows x64 的 JOGL/GlueGen 原生库。其他平台的图形入口未经验证。

## 构建与测试

在项目根目录打开 PowerShell：

```powershell
.\mvnw.cmd -B -ntp clean verify
```

这会编译全部业务代码与历史示例，并运行 10 个测试，包括真实门窗读取、六院落生成、3dm 导出和重新读取，以及路网和面积回归测试。

亦可运行 `scripts/build.ps1`。构建缓存进入 `target/`，不进入发布包。

## 默认示例

```powershell
.\mvnw.cmd exec:java
```

无需私有场地模型，无需 Rhino 安装，也不弹出 Processing 窗口。程序直接读取构件文件，构造六个矩形场地，调用现有院落生成类，保存：

```text
output/demo/courtyard-demo.3dm
output/demo/run-summary.json
```

输出显式使用 **Rhino 4 文件格式**：这版 iGeo 默认的 Rhino 5 写出路径在本次回读验证中失败，Rhino 4 路径通过了导出与重新读取测试。格式选择不会改变院落生成规则。

生成结果采用 `Demo_01_` 至 `Demo_06_` 图层前缀，`Entrance` 为门构件，`TypeB` 包含建筑及窗构件，`Plot` 为地块边界。重运行会覆盖该输出目录下同名生成文件。

自定义一个院落：

```powershell
.\mvnw.cmd exec:java "-Dexec.args=14 18 1.5"
```

或使用参数脚本：

```powershell
.\scripts\run-demo.ps1 -Width 14 -Depth 18 -Floors 1.5
```

面宽范围 5–20 m、进深范围 10–30 m，层数模式为 1、1.5、2。`1.5` 是原规则中的混合一/二层模式。无参数运行时生成六个固定样例，无随机选型。

## 常见情况

- **找不到组件文件**：在项目根目录运行，确认 `assets/components/door-and-window.3dm` 存在。
- **找不到 `java` / `javac`**：安装并配置 JDK 23，重新打开终端。
- **依赖下载失败**：首次构建需要访问 Maven Central 和 JitPack；检查网络后重试。不要删除或用未知版本替换 `lib/` 中的历史构建。
- **Maven 关于 `systemPath` 的警告**：为了保留不易从 Maven 获取的历史二进制，本工程仍有 12 项项目内 system 依赖。本包按独立应用构建；不应直接将其作为普通 Maven 库发布给其他工程依赖。
- **iGeo 读取构件时提示 unknown properties type code**：旧 iGeo 对原构件文件的一项属性元数据给出兼容性提示。当前组件几何、后续生成及导出重读均已通过测试，仍应以输出几何验证为准。
- **历史场地入口报缺文件**：这是预期边界；包内没有私有场地数据，见 [数据约定](PRIVATE_DATA.md)。

## 重新制作上传压缩包

```powershell
.\scripts\package-release.ps1 -Destination ..\riverfront-urban-design-release.zip
```

脚本按白名单打包源码、文档、必要依赖、组件和压缩媒体；排除私有数据、`target/` 和 `output/`。发现 CSV、其他 Rhino 模型、编译产物或超过 25 MiB 的单文件时拒绝打包。已有同名 ZIP 不会覆盖，请指定一个新的目标文件名。
