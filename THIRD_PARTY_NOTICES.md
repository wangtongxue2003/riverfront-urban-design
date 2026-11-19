# 第三方来源与许可状态

本文件记录代码中已有的来源线索，不是对第三方代码的重新授权。本发布整理版保留原有署名，未擅自添加覆盖整个工程的开源许可证。

| 内容 | 已观察到的来源 | 说明 |
| --- | --- | --- |
| `Zshare` 大部分文件 | ZHANG Baizhou / zhangbz / zbz_lennovo；[ZTools](https://github.com/zhangbz764/ZTools) | 原文件头包含不同研究项目名称。所用历史版本与改动边界仍需原作者核对 |
| `MaximumInscribedCircle` | Martin Davis，文件头注明 JTS 1.18.1 | 保留原文件头；按对应版本核查来源与许可 |
| `subdivision/lb_sub` 中部分文件 | libiao | 保留原署名 |
| `basic/AJLoader` | Baizhou Zhang / archijson-reader | 保留原署名；属于数据适配器 |
| 直骨架底层 | [twak/campskeleton](https://github.com/twak/campskeleton) | Zshare 对其进行包装 |
| 几何、显示与优化依赖 | JTS、HE_Mesh、iGeo、Processing、JOGL/GlueGen、PeasyCam、jMetal、Eclipse Collections 等 | Maven 依赖声明与随包依赖清单记录所用版本；各库按自身条款使用 |
| 门窗构件与成果媒体 | 由项目提供者提供 | 保留用于项目演示，不推定给予独立素材的再授权 |

2026-11-19 整理时，本地原工程未提供总许可证文件；ZTools 上游根目录未见 LICENSE。这不等于对权利归属或既有授权关系作出判断。公开发布时，维护者应确认共享代码和素材的授权范围，并在适用时补入对应许可证文本。

不要仅因本工程包含某个几何算法文件，就将该算法标为本项目原创。项目重点是城市设计流程、场地规则、建筑类型及其组合应用。
