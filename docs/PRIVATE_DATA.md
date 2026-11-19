# 原场地流程与私有数据

发布包不包含场地 Rhino 模型、结果 CSV 或 ArchiJSON。缺少这些文件时，原场地流程不能复现；请使用默认合成院落示例验证公开版本。

## 文件放置

项目根目录下的 `private-data/` 存放由你自行提供的原场地输入，默认被 Git 忽略：

| 文件 | 对应流程 |
| --- | --- |
| `Rhino data model.3dm` | 基础场地，供南北岸路网准备、更新分析使用 |
| `Rhino data model-north.3dm` | 北岸已准备的场地，可作为预处理中间结果的替代输入 |
| `Rhino data model-south.3dm` | 南岸已准备的场地，可作为预处理中间结果的替代输入 |
| `Rhino data model-Renewal.3dm` | 更新区域已准备的场地，可作为预处理中间结果的替代输入 |

门窗构件单独放在随包的 `assets/components/door-and-window.3dm`。不要把场地输入放进该目录以绕过 Git 忽略规则。

## 图层约定

| 用途 | 图层名 | 内容 |
| --- | --- | --- |
| 门窗模板 | `door`、`window` | 可复制的构件几何 |
| 原始路网 | `ridegs` | 平面线或折线网络 |
| 公共点 | `pubpnts` | 公共区域参考点 |
| 更新地块 | `renewalplotcrvs` | 闭合地块边界 |
| 更新路网 | `newridegs` | 接入公共点及地块后的路网 |
| 北岸边界／脊线／地块 | `northriverbound`、`northriverrideg`、`northriverplotcrvs` | 闭合边界、沿线参数曲线、原居住地块 |
| 北岸准备路网 | `northrivernewridegs` | 地块接入后的路网 |
| 南岸边界／脊线／地块 | `southriverbound`、`southriverrideg`、`southriverplotcrvs` | 与北岸同类 |
| 南岸准备路网 | `southrivernewridegs` | 地块接入后的路网 |
| 河道 | `qinhuairiver` | 河岸参考曲线 |

原拼写 `rideg/ridegs` 为兼容既有模型保留。注意检查图层对象类型；只有名字相同但没有相应曲线/点，仍不能运行。

输入应为平面、单位一致的闭合多边形和直线/折线。原转换代码主要读取控制点，并非对任意 NURBS 曲线按弧长采样。门窗与尺寸阈值也必须使用一致尺度。

## 流程顺序

- 北岸：`GenerationAlongRiver.NorthRidgesCreation` → `GenerationAlongRiver.NorthOfRiverGeneration`。
- 南岸：`GenerationAlongRiver.SouthRidgesCreation` → `GenerationAlongRiver.SouthOfRiverGeneration`。
- 更新：`RenewalGeneration.DepthWithPlots` → `RenewalGeneration.PlotsRenewal`。

这些历史入口位于 `src/test/java`，应在 IDE 中使用测试 classpath 启动；它们使用 Processing/iGeo 图形环境。先运行准备入口，中间模型写入 `output/prepared/`，下游优先读取该目录，缺失时读取 `private-data/` 中同名模型。最终成果写入 `output/`。

南北岸优化的参数使用 JVM 系统属性：`urban.population`、`urban.evaluations`、`urban.threads`、`urban.seed`。具体默认值以源码为准。不要把一次小预算运行称为已收敛的最优解。

部分早期 `NSGAII.test01/test02` 实验依赖其他未提供文件，包括 ArchiJSON 数据；它们保留供研究参考，不在默认示例验证范围内。
