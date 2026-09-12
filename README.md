# JEI Crafting Tree

[English](#english) | [中文](#中文) | [日本語](#日本語)

**Current release:** `v0.0.1` · [Download from GitHub Releases](https://github.com/lhy512103/JEI-Crafting-Tree/releases/tag/v0.0.1) · [Changelog](CHANGELOG.md)

JEI Crafting Tree is a NeoForge extension for JEI that turns a selected recipe into an interactive, recursive production plan. It works as a standalone client-side planning tool and exposes integration APIs for storage networks, exact pattern detection, pattern encoding, and machine-aware inventory sources.

---

## English

### Overview

JEI Crafting Tree adds an **Open Crafting Tree** action to JEI recipe layouts. From a single recipe, the tree can expand selected ingredient routes, combine multiple targets into one global plan, allocate available inventory, account for secondary outputs, and produce an ordered execution checklist.

The planning model is independent from AE2. Optional backends such as AE2 Utility can add exact-pattern hints, encoding, upload, substitution controls, reusable-input rules, and machine identifiers without making AE2 a hard dependency of this mod.

### Core capabilities

#### Recursive recipe exploration

- Expands JEI recipes into a navigable dependency tree.
- Supports normal graph and layer-merged material views.
- Lets the user select a child recipe or an alternative ingredient at each input. Recipe choices and manual collapse state are synchronized across visible occurrences of the same material at different tree depths.
- Can automatically expand unresolved inputs when JEI exposes exactly one usable recipe.
- Detects recursive routes, stops unsafe expansion, highlights the affected incoming edge and material entry, and reports cycle diagnostics instead of recursing indefinitely.

#### Global multi-project planning

- Maintains multiple named production targets in one session.
- Supports a spatial multi-tree workspace: complete trees are rendered together on one shared canvas and every visible tree can be edited directly without switching views. New independent trees can be added above, below, left, or right, and the JEI bookmark-side shortcut returns to the last workspace. Each tree owns a separate material, surplus, draft, history, and planning state.
- Uses a shared global material ledger across all projects.
- Reuses overproduction from one branch or project in another branch or project.
- Credits secondary outputs and byproducts back to the global supply pool.
- Uses `long` quantities throughout the planning layer and saturates arithmetic at `Long.MAX_VALUE`.

#### Recipe semantics

- Imports all outputs exposed by a JEI recipe, with one focused primary output and optional secondary outputs.
- Supports item, fluid, chemical, and custom JEI ingredient identities; non-item quantities use the same bottom-right count-overlay style as item stacks.
- Treats JEI catalyst slots and backend-declared reusable inputs as non-consumed requirements.
- Distinguishes “this output is craftable” from “this exact selected recipe already has a matching pattern.”

#### Editable AE2 pattern drafts

When AE2 and a compatible backend such as AE2 Utility are installed, the node inspector includes a compact pattern editor based on AE2's native pattern-mode and slot textures. AE2 remains optional: the editor is not created or rendered when the integration is unavailable.

- Creates one editable draft for each exact selected recipe and shares that draft across repeated occurrences of the same recipe.
- Previews the exact inputs, primary output, secondary outputs, quantities, alternatives, and substitution state that will be sent for encoding.
- Supports AE2 processing-pattern capacity of up to 81 input slots and 27 output slots through a paged 3×3 input / three-output viewport and scrollbar.
- Allows processing slots to be replaced from the held item, cleared with right click, or assigned an exact amount with middle click or `Ctrl + wheel`.
- Cycles legal JEI alternatives, promotes a secondary output to the primary output, removes unwanted byproducts, clears the processing draft, and restores the original recipe snapshot.
- Keeps structured crafting drafts recipe-safe: their slots can cycle legal alternatives but cannot be freely removed, replaced, or assigned arbitrary amounts.
- Shows dirty, invalid, removed-input, removed-output, and changed-primary-output states before encoding.
- Uses the edited draft—not the immutable JEI recipe snapshot—for batch deduplication, validation, encoding, and upload.
- Performs lightweight client checks first and requires the backend/server to validate slot limits, ingredients, quantities, and the primary output before consuming a blank pattern.

#### Inventory allocation

Built-in inventory coverage includes:

- player main inventory;
- armor and offhand slots;
- non-player slots in the currently open container.

External mods can register additional `InventorySource` implementations for AE2, Refined Storage, remote warehouses, or other storage systems. Sources are priority ordered, versioned, cached, and isolated so a failing integration does not break the planner.

#### Alternative-material strategies

The planner provides five allocation modes:

| Strategy | Behavior |
| --- | --- |
| `LOCKED` | Uses the alternative currently selected in the tree. |
| `MIX_AVAILABLE` | Consumes matching alternatives from available inventory before reporting or crafting the remainder. |
| `MOST_AVAILABLE` | Prefers the alternative with the largest available stock. |
| `PREFERRED_NAMESPACE` | Prefers materials from the configured mod namespace. |
| `STRICT_COMPONENTS` | Keeps the exact selected component/subtype identity. |

The active strategy can be changed from the overview settings panel or the client configuration.

#### Plan report

The **Plan** screen contains four projections of the same immutable planning result:

- **Materials** — raw requirements, allocated inventory, inventory-source status, surplus/byproducts, and cycle diagnostics.
- **Checklist** — ordered crafting and machine execution steps.
- **Machines** — total runs grouped by machine identifier.
- **Routes** — background comparison of all substitution strategies, with a deterministic recommendation based on raw inputs, machine runs, machine count, waste, and step count.

#### Search and navigation

- Searches recipe names, stable recipe identities, ingredients, mods, and machines.
- `@namespace` narrows searches toward a mod namespace.
- `#machine` narrows searches toward a machine name.
- Matching nodes use the active theme accent.
- When Just Enough Characters is installed, names and machine labels also support its configured pinyin matching rules.
- Enter focuses the next result; the search key can focus the field from anywhere in the overview.

#### Recipe memory and edit history

- Remembers selected recipes and manually collapsed branches.
- Uses versioned memory keys containing the parent recipe, node path, input slot, ingredient identity, memory scope, and modpack fingerprint.
- Supports global, server, and world memory scopes.
- Automatically reads legacy unscoped entries and migrates them when they are successfully resolved.
- Supports undo and redo for tree selections, expansion state, alternative choices, project creation/removal, project selection, and target amounts.
- Stores up to 64 in-session history snapshots.

#### Performance design

- Runs the global solver on a dedicated background thread.
- Normalizes custom JEI ingredient renderers into a centered 16×16 slot, renders fluid icons at a consistent full fill level, and keeps quantities outside the icon area.
- Caches scoped recipe-memory profiles and collapsed-state lookups, avoids formatter-heavy hashing, and skips repeated traversal of shared graph nodes during unique-recipe expansion.
- Route comparison reuses the already computed active-strategy result and cancels obsolete workers instead of solving every route from scratch.
- Cancels superseded planning requests and prevents stale results from replacing a newer tree.
- Checks interruption during long planning traversals.
- Uses immutable planning snapshots before entering background work.
- Replans only when the tree/configuration fingerprint or inventory version changes.
- Uses versioned inventory snapshot caching.
- Caches JEI output lookups and recipe-ID indexes per JEI runtime.
- Limits lookup result counts and unique-recipe expansion work per tick.
- Uses visible-region culling, row indexes, and render-data caches in the overview.
- Propagates large quantities with saturating arithmetic and reuses the result during one layout refresh.
- Aggregates repeated branches in merged-layer and top-material traversals instead of visiting the same node once per parent link.
- Canonicalizes equivalent immediate branches in the merged projection while leaving the editable recipe tree unchanged.
- Preserves shared DAG branches in undo/redo snapshots, preventing repeated 3x3 compression inputs from expanding exponentially in memory.
- Keeps search filtering render-local instead of rebuilding the complete tree.
- Renders the floating material panel from one GUI render event to prevent duplicate drawing.

### Usage

1. Open any recipe in JEI.
2. Click the JEI Crafting Tree button in the recipe layout.
3. Expand unresolved ingredients and choose the desired child recipes or alternatives.
4. Use **Projects** to add targets and set their requested quantities.
5. Choose an alternative-material strategy from the settings panel when needed.
6. Open **Plan** to inspect materials, execution order, machine runs, and route comparisons.
7. Use the floating material panel to keep requirements visible while browsing JEI or other screens.
8. If a compatible backend is installed, use the exact-pattern, encode, or upload actions supplied by that backend.

### Controls

| Input | Action |
| --- | --- |
| Left click | Select, expand, inspect, or activate the control under the pointer. |
| Right click | Change a recipe or open the corresponding JEI recipe/usage view. |
| Mouse wheel | Zoom or scroll the active panel. |
| Shift + wheel | Horizontal navigation where supported. |
| Ctrl + wheel | Zoom the merged view or scale the floating material panel. |
| Enter in search | Focus the next matching result. |
| `F` | Focus search. |
| `Ctrl+Z` | Undo the last edit. |
| `Ctrl+Y` | Redo the last undone edit. |

Key bindings can be reassigned in Minecraft's Controls screen.

### Client configuration

NeoForge writes the client configuration to `config/jeict-client.toml`.

| Key | Default | Purpose |
| --- | ---: | --- |
| `planning.rememberSelections` | `true` | Enables reading and writing recipe/collapse memory. |
| `planning.autoMergeMaterials` | `true` | Opens the overview in layer-merged material mode. |
| `planning.computeQuantities` | `true` | Enables rolled-up quantities and pattern-count calculations. |
| `planning.autoExpandUniqueRecipes` | `false` | Automatically expands inputs with one usable recipe. |
| `planning.substitutionStrategy` | `LOCKED` | Default alternative-material allocation strategy. |
| `planning.preferredNamespace` | empty | Namespace preferred by `PREFERRED_NAMESPACE`. |
| `planning.memoryScope` | `SERVER` | Memory isolation level: `GLOBAL`, `SERVER`, or `WORLD`. |
| `planning.memoryProfile` | empty | Optional manual modpack/profile identifier; otherwise a mod-list fingerprint is used. |
| `performance.showFloatingMaterials` | `true` | Enables the floating material requirements panel. |
| `performance.maxAutoExpandStepsPerTick` | `32` | Limits unique-recipe expansion work performed in one client tick. |
| `performance.maxRecipeLookupResults` | `512` | Caps cached JEI recipe lookup results per output query. |

### Integration API

#### Crafting backend

Register one `CraftingTreeBackend` through `CraftingTreeBackends.register(...)`.

Important extension points include:

- `isOutputCraftable(...)` for broad output availability;
- `hasExactPattern(...)` and `exactPatternFingerprint(...)` for the currently selected recipe route;
- `isReusableInput(...)` for tools, molds, containers, or catalysts;
- `machineId(...)` for execution and machine-run reports;
- encode/upload capability and actions;
- optional item/fluid substitution controls, rendered with JEICT-native labeled buttons beside the encoding actions.

New integrations should implement exact pattern matching from a normalized recipe fingerprint rather than treating every pattern that produces the same output as equivalent.

#### Inventory source

Register storage through `CraftingTreeInventorySources.register(...)`:

```java
CraftingTreeInventorySources.register(new InventorySource() {
    @Override
    public String id() {
        return "example:network";
    }

    @Override
    public long version() {
        return networkChangeCounter;
    }

    @Override
    public List<InventoryAmount> snapshot() {
        return immutableAmounts;
    }
});
```

`version()` should change only when visible stock changes. `snapshot()` should return an immutable or safely copied view. Material identities use `MaterialKey`, which includes the JEI ingredient type and subtype-aware UID.

The stable API is bundled with the main jar (current major: `1`). New integrations should use named registrations with `ApiRegistration` handles, `authorityGroup()` for mutually visible storage, and `CraftingTreeMenuInventorySources` for an open menu. Full lifecycle, thread, compatibility, menu, and auto-crafting constraints are documented in [docs/API.md](docs/API.md).

### Requirements

JEI Crafting Tree is a client-side mod. Install it on the client together with JEI; dedicated servers do not
need it. Installing the jar on a dedicated server is harmless because its mod entry point is client-only.
The mod does not register a mandatory client/server network channel, so clients can join servers without JEI
Crafting Tree installed. Optional encoding or upload integrations may still have their own server-side requirements.

- Minecraft `1.21.1`
- NeoForge `21.1.233` or newer in the compatible `21.1.x` line
- Java `21`
- JEI `19.21.0` or newer; release `v0.0.1` is tested with JEI `19.27.0.340`

Optional:

- AE2 Utility `1.6.0` or newer for an external AE2-oriented backend, when installed and compatible.
- Just Enough Characters for optional pinyin matching in the overview search field.

### Build and test

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

Artifacts are written to `build/libs/`. A faster Java-only verification is available through `./gradlew compileJava` or `.\gradlew.bat compileJava`.

### Compatibility notes

- The planner can understand generic JEI ingredient identities, but visible rendering and external stock detection still depend on JEI and the installed integrations exposing those ingredient types correctly.
- Exact-pattern accuracy depends on the backend implementing `hasExactPattern(...)` with a route-sensitive fingerprint. The default compatibility implementation only preserves older backend behavior.
- Route comparison evaluates the currently selected recipe tree under different material-allocation strategies; it does not enumerate every possible JEI recipe combination automatically.
- Planning is client-side and does not move items or execute machines by itself.

### License

JEI Crafting Tree is licensed under the [MIT License](LICENSE).

---

## 中文

### 项目简介

JEI Crafting Tree 是面向 Minecraft 1.21.1 / NeoForge 的 JEI 配方规划扩展。它可以把 JEI 中选中的配方转换为可交互的递归配方树，并进一步建立多目标共享的全局生产计划。

模组本体不依赖 AE2。安装兼容后端后，可以在同一界面中获得精确样板检测、样板编码、样板上传、可复用输入判定、机器标识以及外部库存接入能力。

### 主要功能

- **递归配方树**：逐层展开原料配方，支持普通图形视图与同层材料合并视图；同一材料在不同深度出现时，会同步配方选择和手动折叠状态。
- **多配方树工作区**：多棵完整配方树会同时显示在同一个可缩放、可拖动的画布中，每棵树都能在原位置直接选择配方、折叠分支、打开节点详情和编辑样板，无需切换当前树。可从远离树体的上、下、左、右纯文字入口添加新树；JEI 书签区旁的快捷按钮可返回上次工作区。每棵树的总材料、剩余材料、项目、样板草稿、编辑历史和规划结果完全隔离。
- **全局材料账本**：所有项目和分支共享库存、余料与副产物，避免按节点重复统计。
- **多目标项目**：可以建立多个命名目标、设置 `long` 类型目标数量，并在项目之间切换编辑。
- **多输出与副产物**：读取 JEI 暴露的全部输出，将次要输出重新计入全局供给。
- **催化剂与可复用输入**：JEI catalyst 槽位和后端声明的模具、工具、容器等不会按合成次数重复消耗。
- **通用材料身份**：支持物品、流体、化学品及其他 JEI 自定义 ingredient type，并保留 subtype 身份；非物品材料数量与物品一样使用右下角角标。
- **替代材料策略**：提供锁定、混用库存、库存最多、优先模组命名空间和严格组件五种策略。
- **库存聚合**：内置玩家背包、护甲、副手和当前容器；外部模组可以注册网络库存来源。
- **计划报告**：集中展示基础原料、库存分配、余料/副产物、循环依赖、执行清单、机器运行次数和路线比较。
- **精确已有样板判断**：区分“该输出可合成”和“当前选中的精确配方已有样板”；禁用已有样板展开后，会立即折叠已展开分支，并阻止手动选择、配方记忆或唯一配方逻辑再次展开。
- **搜索与定位**：支持材料、配方、`@模组`、`#机器` 搜索，Enter 定位下一个结果；安装 Just Enough Characters 后兼容其拼音匹配规则。
- **节点详情与样板草稿编辑**：右键节点可查看材料、数量、机器和已有样板状态。安装 AE2 与兼容后端后，详情面板使用 AE2 原生样板模式背景、槽位和控制图标，直接编辑最终要写入样板的输入、输出、数量与替换状态；未安装 AE2 时不创建也不绘制该区域。
- **配方记忆**：按父配方、节点路径、输入槽、材料身份、服务器/世界和整合包指纹隔离记忆，并兼容迁移旧记录。
- **撤销与重做**：覆盖配方选择、展开/折叠、替代材料和项目管理操作，最多保存 64 个会话内快照。
- **悬浮材料面板**：可固定材料清单，在其他界面中继续查看并跳转 JEI 配方或用途。
- **外部后端 API**：支持存储网络、精确样板、编码、上传、机器统计和库存来源扩展。

### AE2 样板草稿编辑

节点详情中的样板区不是只读预览，而是批量编码与上传使用的最终草稿：

- 相同精确配方在树中多次出现时共享同一份草稿，任一位置的修改都会同步生效；
- 处理样板支持最多 81 个输入槽和 27 个输出槽，通过 3×3 输入、三个输出的可滚动视窗编辑；
- 左键可轮换 JEI 提供的合法备选原料，手持物品时可填入或替换处理样板槽位；
- 右键可清除处理样板槽位，中键可输入精确数量，`Ctrl + 滚轮` 可快速调整数量；
- 可切换主要输出、把次要输出提升为主输出、删除不需要的副产物、清空草稿并恢复原始配方；
- 可单独设置物品替换、流体替换和输入顺序保留状态；
- 合成、锻造和切石等结构化配方保持配方安全，只允许切换合法备选，不允许自由删除、替换或修改数量；
- 编辑器会明确提示草稿已修改、无效、删除输入、删除输出或主要输出变化；
- 批量收集按最终草稿指纹去重，编码和上传严格使用草稿内容，不会退回未修改的 JEI 配方快照；
- 客户端先执行轻量校验，兼容后端与服务端还会再次检查槽位上限、材料类型、数量和主要输出，校验失败时不会消耗空白样板。

删除副产物只影响最终编码草稿，不会篡改配方树用于材料规划的真实产出与余料语义。

### 全局计划规则

规划器使用统一供给账本处理全部目标：

1. 优先消耗已有余料和副产物；
2. 再分配已注册库存来源中的可用数量；
3. 根据所选替代策略确定输入材料；
4. 计算配方运行次数，并把全部输出写回供给账本；
5. 无法继续展开的需求计入基础原料；
6. 后续产生的副产物可以抵消此前登记的同类基础原料缺口；
7. 循环路线会写入诊断结果，而不会无限递归。

所有规划数量均使用 `long`，溢出时采用饱和运算。

### 替代材料策略

| 策略 | 行为 |
| --- | --- |
| `LOCKED` | 只使用配方树中当前选中的材料。 |
| `MIX_AVAILABLE` | 优先混合消耗库存中的所有匹配替代材料，再处理剩余需求。 |
| `MOST_AVAILABLE` | 优先选择库存数量最多的替代材料。 |
| `PREFERRED_NAMESPACE` | 优先选择配置中指定模组命名空间的材料。 |
| `STRICT_COMPONENTS` | 严格保持当前选中的组件与 subtype 身份。 |

可以在总览设置栏中即时切换策略，也可以修改客户端配置作为默认值。

### 使用方法

1. 在 JEI 中打开任意配方。
2. 点击配方布局中的 JEI Crafting Tree 按钮。
3. 展开未解析原料，并选择需要的下级配方或替代材料。
4. 打开 **项目**，添加生产目标并设置目标数量。
5. 根据需要从设置栏选择替代材料策略。
6. 打开 **计划**，查看材料、执行清单、机器运行统计与路线比较。
7. 可将材料需求固定到悬浮面板，在浏览其他界面时继续查看。
8. 安装兼容后端后，可使用后端提供的样板提示、编码和上传功能。

### 操作与快捷键

| 操作 | 功能 |
| --- | --- |
| 左键 | 选择下级配方或点击控件；禁用已有样板展开时，已有样板节点不会再打开配方选择。 |
| 右键 | 打开节点详情；材料清单与悬浮面板中的右键操作可跳转 JEI 配方/用途。 |
| 鼠标滚轮 | 缩放或滚动当前面板。 |
| Shift + 滚轮 | 在支持的区域横向移动。 |
| Ctrl + 滚轮 | 缩放合并视图或调整悬浮面板比例。 |
| 搜索框内 Enter | 定位下一个匹配结果。 |
| `F` | 聚焦搜索框。 |
| `Ctrl+Z` | 撤销。 |
| `Ctrl+Y` | 重做。 |

快捷键可以在 Minecraft 的按键设置中重新绑定。

### 客户端配置

NeoForge 会将配置写入 `config/jeict-client.toml`。

| 配置项 | 默认值 | 作用 |
| --- | ---: | --- |
| `planning.rememberSelections` | `true` | 启用配方选择和折叠状态的读取与写入。 |
| `planning.autoMergeMaterials` | `true` | 默认使用同层材料合并视图。 |
| `planning.computeQuantities` | `true` | 启用汇总数量和所需样板数量计算。 |
| `planning.autoExpandUniqueRecipes` | `false` | 自动展开只有一个可用配方的输入。 |
| `planning.substitutionStrategy` | `LOCKED` | 默认替代材料分配策略。 |
| `planning.preferredNamespace` | 空 | `PREFERRED_NAMESPACE` 优先使用的模组命名空间。 |
| `planning.memoryScope` | `SERVER` | 记忆范围：`GLOBAL`、`SERVER` 或 `WORLD`。 |
| `planning.memoryProfile` | 空 | 手动整合包/配置档标识；留空时自动计算模组列表指纹。 |
| `performance.showFloatingMaterials` | `true` | 是否显示悬浮材料面板。 |
| `performance.maxAutoExpandStepsPerTick` | `32` | 每个客户端 tick 最多处理的自动展开步骤。 |
| `performance.maxRecipeLookupResults` | `512` | 单次 JEI 输出查询最多缓存的配方数量。 |

### 性能与稳定性

- 全局规划在独立后台线程执行，不阻塞渲染线程。
- 新请求会取消旧请求，并通过代次校验阻止旧结果覆盖新树。
- 长规划循环会检查线程中断，以便更快取消。
- 树、配置或库存版本没有变化时不会重复提交规划。
- 库存快照、JEI 输出查询和配方 ID 索引均带缓存与失效机制。
- 自动展开和 JEI 查询都有可配置上限。
- 总览使用可见区域裁剪、行索引和渲染数据缓存。
- 节点详情的样板终端预览仅在 AE2 已加载时使用其原生纹理，最多缓存并绘制 9 个输入槽和 3 个输出槽；预览不查询 JEI 或存储网络，也不扫描整棵树。
- 大数量传播使用饱和算术，并在一次布局刷新内复用相同计算结果，避免重复乘法和溢出。
- 合并视图与顶部材料收集会聚合同一层级的重复分支，避免同一节点被多个父链接重复遍历。
- 合并视图只对显示投影做等价即时分支归一化，不修改可编辑配方树、配方选择或记忆数据。
- 撤销/重做快照会保留配方树中的共享 DAG 分支，避免 3×3 重复输入在九重压缩链中被指数级复制并耗尽内存。
- 搜索仅过滤当前渲染结果，不会因为每次输入而重建整棵树。
- 外部库存来源发生异常时会被隔离，不会中断其他来源和规划流程。

### 外部集成

#### 配方后端

外部模组可以通过 `CraftingTreeBackends.register(...)` 注册一个 `CraftingTreeBackend`。建议重点实现：

- `isOutputCraftable(...)`：判断某类输出是否可由网络生产；
- `hasExactPattern(...)`：判断当前选中的精确配方是否已有样板；
- `exactPatternFingerprint(...)`：提供稳定、规范化的精确样板指纹；
- `isReusableInput(...)`：标记模具、工具、容器或催化剂；
- `machineId(...)`：为执行清单和机器统计提供稳定机器标识；
- 样板编码、上传，以及位于编码操作左侧、采用 JEICT 原生样式的“物品替换”和“流体替换”开关。

精确样板实现不应只比较输出物，而应把配方身份、规范化输入、输出以及后端需要的处理模式纳入指纹。

#### 库存来源

外部存储系统可以实现 `InventorySource`，并通过 `CraftingTreeInventorySources.register(...)` 注册。来源应提供稳定 ID、优先级、库存版本和不可变快照。库存版本只应在规划器可见库存发生变化时更新。

稳定 API 与主 jar 同发，当前主版本为 `1`。新集成应使用具名注册和 `ApiRegistration` 注销句柄；同一可见存储使用相同的 `authorityGroup()` 防止重复统计；当前菜单库存使用 `CraftingTreeMenuInventorySources`。完整的依赖方式、生命周期、线程限制、菜单扩展和自动合成边界见 [docs/API.md](docs/API.md)。

### 运行要求

JEI Crafting Tree 是客户端模组，只需要与 JEI 一起安装在客户端；专用服务端无需安装。即使把 jar
放入专用服务端，客户端限定的模组入口也不会在服务端加载。本模组没有注册强制双端存在的网络通道，
因此客户端可以正常进入未安装 JEI Crafting Tree 的服务器。样板编码、上传等可选兼容后端可能仍有其
自身的服务端安装要求。

- Minecraft `1.21.1`
- NeoForge `21.1.233` 或兼容的更新版本
- Java `21`
- JEI `19.21.0` 或更新版本；正式版 `v0.0.1` 已使用 JEI `19.27.0.340` 测试

可选集成：

- AE2 Utility `1.6.0` 或更新的兼容版本，可作为 AE2 功能后端。
- Just Enough Characters，可为总览搜索框提供拼音匹配。

### 构建与测试

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

构建产物位于 `build/libs/`。仅进行 Java 编译检查时可执行：

```powershell
.\gradlew.bat compileJava
```

### 兼容性说明

- 通用材料规划依赖 JEI 和对应模组正确暴露 ingredient type、subtype 与配方槽位。
- 精确样板判断的准确性取决于外部后端是否实现了路线敏感的 `hasExactPattern(...)`。
- 路线比较是在当前已选择的配方树上比较不同材料分配策略，不会自动穷举 JEI 中所有配方组合。
- 规划器只生成客户端计划，不会自行移动物品或执行机器。

### 许可证

本项目使用 [MIT License](LICENSE)。

---

## 日本語

### 概要

JEI Crafting Tree は、JEI で選択したレシピを対話的に操作できる再帰的な生産計画へと変換する NeoForge 向けの JEI 拡張です。クライアント単体で動作する計画ツールとして機能しつつ、ストレージネットワーク・正確なパターン検出・パターンエンコード・機械を考慮した在庫ソースなどと連携するための API も提供します。

JEI Crafting Tree は **Open Crafting Tree**（クラフティングツリーを開く）アクションを JEI のレシピ画面に追加します。1 つのレシピから、選択した材料のルートを展開し、複数の目標を 1 つのグローバル計画にまとめ、利用可能な在庫を割り当て、副産物を考慮し、順序立てられた実行チェックリストを生成できます。

この計画モデルは AE2 に依存しません。AE2 Utility のようなオプションのバックエンドを導入すると、AE2 を必須の依存関係にすることなく、正確なパターンのヒント表示・エンコード・アップロード・代替品の制御・再利用可能入力のルール・機械識別子などを追加できます。

### 主な機能

#### 再帰的なレシピ探索

- JEI のレシピを、たどれる依存関係のツリーへと展開します。
- 通常のグラフ表示と、階層ごとに材料を統合した表示の両方に対応しています。
- 各入力ごとに子レシピや代替材料を選択できます。レシピの選択状態と手動での折りたたみ状態は、同じ材料がツリーの異なる深さに表示されている場合でも同期されます。
- JEI 上で使用可能なレシピが 1 つしかない未解決の入力は、自動的に展開できます。
- 循環するルートを検出し、危険な展開を停止させ、該当する入力エッジと材料項目を強調表示したうえで、無限に再帰する代わりに循環診断を報告します。

#### グローバルなマルチプロジェクト計画

- 1 つのセッション内で、複数の名前付き生産目標を管理できます。
- 空間的なマルチツリーのワークスペースに対応しています。完成した複数のツリーが 1 つの共有キャンバス上にまとめて表示され、表示中のどのツリーも画面を切り替えずに直接編集できます。新しい独立したツリーは上下左右に追加でき、JEI のブックマーク側のショートカットから最後に開いていたワークスペースに戻れます。各ツリーは、材料・余剰材料・下書き・履歴・計画状態をそれぞれ独立して保持します。
- すべてのプロジェクトで共有されるグローバルな材料台帳を使用します。
- あるブランチやプロジェクトで生じた過剰生産分を、別のブランチやプロジェクトで再利用します。
- 副産物や余剰生産物はグローバルな供給プールに還元されます。
- 計画レイヤー全体で `long` 型の数量を使用し、`Long.MAX_VALUE` で飽和演算を行います。

#### レシピの扱い

- JEI レシピが公開するすべての出力を取り込み、1 つの主産物と任意の副産物として扱います。
- アイテム・流体・化学物質・その他のカスタム JEI ingredient に対応しています。アイテム以外の数量も、アイテムスタックと同じ右下のカウント表示スタイルで表示されます。
- JEI の触媒スロットやバックエンドが宣言する再利用可能な入力は、消費されない要求として扱います。
- 「この出力自体が製造可能かどうか」と「この正確に選択されたレシピに、すでに一致するパターンが存在するかどうか」を区別します。

#### 編集可能なAE2パターン下書き

AE2 と AE2 Utility のような互換バックエンドが導入されている場合、ノード詳細画面には AE2 純正のパターンモードとスロットテクスチャをベースにしたコンパクトなパターンエディタが表示されます。AE2 はあくまでオプションであり、連携先が存在しない場合はこのエディタ自体が生成・描画されません。

- 正確に選択された各レシピごとに 1 つの編集可能な下書きを作成し、その下書きは同じレシピが繰り返し出現する箇所すべてで共有されます。
- エンコードのために送信される正確な入力・主産物・副産物・数量・代替品・代替状態をプレビューできます。
- 3×3 の入力／3 出力のビューポートとスクロールバーにより、最大 81 個の入力スロットと 27 個の出力スロットという AE2 の加工パターンの容量に対応します。
- 加工スロットは、手持ちアイテムからの入れ替え、右クリックによるクリア、中クリックまたは `Ctrl + ホイール` による正確な数量の指定に対応しています。
- 有効な JEI の代替候補を切り替えたり、副産物を主産物に昇格させたり、不要な副産物を削除したり、加工の下書きをクリアしたり、元のレシピのスナップショットに戻したりできます。
- クラフティングのような構造化されたレシピの下書きは安全性を保ちます。スロットは有効な代替候補への切り替えはできますが、自由な削除・置き換え・任意の数量指定はできません。
- エンコード前に、変更あり・無効・入力削除・出力削除・主産物変更といった状態を表示します。
- バッチ処理での重複排除・検証・エンコード・アップロードには、不変な JEI レシピのスナップショットではなく、編集済みの下書きを使用します。
- まずクライアント側で軽量なチェックを行い、その後バックエンド／サーバー側でスロット数の上限・材料・数量・主産物を検証してから空のパターンを消費します。

#### 在庫の割り当て

標準で対応している在庫は以下のとおりです。

- プレイヤーのメインインベントリ
- 防具スロットとオフハンドスロット
- 現在開いているコンテナ内のプレイヤー以外のスロット

外部 MOD は、AE2・Refined Storage・リモート倉庫・その他のストレージシステム向けに追加の `InventorySource` 実装を登録できます。各ソースは優先度順に並び、バージョン管理され、キャッシュされ、互いに分離されているため、1 つの連携が失敗しても計画全体が壊れることはありません。

#### 代替材料の戦略

計画エンジンは 5 種類の割り当てモードを提供します。

| 戦略 | 動作 |
| --- | --- |
| `LOCKED` | ツリー内で現在選択されている代替品をそのまま使用します。 |
| `MIX_AVAILABLE` | 利用可能な在庫にある代替品を先に消費し、残りを報告または製造対象とします。 |
| `MOST_AVAILABLE` | 在庫数が最も多い代替品を優先します。 |
| `PREFERRED_NAMESPACE` | 設定された MOD の名前空間の材料を優先します。 |
| `STRICT_COMPONENTS` | 選択された正確なコンポーネント／サブタイプの同一性を維持します。 |

現在の戦略は、概要画面の設定パネルまたはクライアント設定から変更できます。

#### 計画レポート

**Plan**（計画）画面では、同一の不変な計画結果を 4 通りの視点で確認できます。

- **Materials（材料）** — 基礎材料の必要量、割り当て済みの在庫、在庫ソースの状態、余剰材料／副産物、循環診断。
- **Checklist（チェックリスト）** — 順序立てられたクラフトおよび機械の実行手順。
- **Machines（機械）** — 機械識別子ごとに集計された合計実行回数。
- **Routes（ルート）** — すべての代替品割り当て戦略をバックグラウンドで比較し、原料量・機械の実行回数・機械の種類数・廃棄量・手順数に基づいた確定的な推奨案を提示します。

#### 検索とナビゲーション

- レシピ名・安定したレシピ識別子・材料・MOD・機械を検索できます。
- `@namespace` で特定の MOD の名前空間に絞り込めます。
- `#machine` で特定の機械名に絞り込めます。
- 一致したノードには、現在のテーマのアクセントカラーが適用されます。
- Just Enough Characters が導入されている場合、名前や機械ラベルの検索にもその設定済みピンイン一致ルールが適用されます。
- Enter キーで次の一致結果にフォーカスします。検索用のキーバインドは、概要画面のどこからでも検索欄にフォーカスできます。

#### レシピ記憶と編集履歴

- 選択したレシピや手動で折りたたんだブランチを記憶します。
- 親レシピ・ノードのパス・入力スロット・材料の識別子・記憶のスコープ・モッドパックのフィンガープリントを含むバージョン管理された記憶キーを使用します。
- グローバル・サーバー・ワールドの各記憶スコープに対応しています。
- 旧形式のスコープなしエントリを自動的に読み込み、解決に成功した時点で移行します。
- ツリーの選択状態・展開状態・代替品の選択・プロジェクトの作成／削除・プロジェクトの選択・目標数量について、元に戻す／やり直す操作に対応しています。
- セッション内の履歴スナップショットを最大 64 件保存します。

#### パフォーマンス設計

- グローバルな計算処理は専用のバックグラウンドスレッドで実行されます。
- カスタム JEI ingredient のレンダラーを中央揃えの 16×16 スロットに正規化し、流体アイコンは常に満タン状態で一定に描画し、数量表示はアイコン領域の外側に配置します。
- スコープ付きのレシピ記憶プロファイルや折りたたみ状態の検索結果をキャッシュし、負荷の高いフォーマット処理によるハッシュ計算を避け、一意なレシピの展開時に共有グラフノードを重複して走査しないようにします。
- ルート比較では、すでに計算済みの現在の戦略の結果を再利用し、不要になった処理を破棄することで、すべてのルートをゼロから計算し直すことを避けます。
- 古くなった計画リクエストをキャンセルし、古い結果が新しいツリーの結果を上書きしないようにします。
- 長時間かかる計画処理の走査中には、割り込みの発生を確認します。
- バックグラウンド処理に入る前に、不変な計画用のスナップショットを作成します。
- ツリーや設定のフィンガープリント、あるいは在庫のバージョンが変化したときのみ再計算します。
- バージョン管理された在庫スナップショットのキャッシュを使用します。
- JEI の出力検索結果とレシピ ID のインデックスを JEI ランタイムごとにキャッシュします。
- 検索結果の件数や、一意なレシピの展開処理量を 1 tick あたりで制限します。
- 概要画面では、表示範囲によるカリング・行インデックス・描画データのキャッシュを利用します。
- 大きな数量は飽和演算で伝播させ、1 回のレイアウト更新内では同じ計算結果を再利用します。
- 統合レイヤー表示や上位材料の集計では、同じノードを親リンクごとに何度も訪問する代わりに、重複するブランチをまとめて処理します。
- 統合表示では等価な直下のブランチを正規化しますが、編集可能なレシピツリー自体は変更しません。
- 元に戻す／やり直すのスナップショットでは、共有された DAG のブランチをそのまま保持し、3×3 の圧縮入力が繰り返されることでメモリ使用量が指数的に増大することを防ぎます。
- 検索によるフィルタリングは描画側のみで完結し、ツリー全体を再構築することはありません。
- 浮動材料パネルは 1 回の GUI 描画イベントの中で描画され、二重描画を防ぎます。

### 使い方

1. JEI で任意のレシピを開きます。
2. レシピ画面にある JEI Crafting Tree のボタンをクリックします。
3. 未解決の材料を展開し、必要な子レシピや代替材料を選択します。
4. **Projects（プロジェクト）** から目標を追加し、必要数量を設定します。
5. 必要に応じて設定パネルから代替材料の戦略を選択します。
6. **Plan（計画）** を開いて、材料・実行順序・機械の実行回数・ルート比較を確認します。
7. 浮動材料パネルを使うと、JEI や他の画面を見ている間も必要数量を表示し続けられます。
8. 互換性のあるバックエンドが導入されている場合は、そのバックエンドが提供する正確なパターン表示・エンコード・アップロードの各機能を利用できます。

### 操作方法

| 入力 | 動作 |
| --- | --- |
| 左クリック | ポインタ下の要素を選択・展開・詳細表示、または操作します。 |
| 右クリック | レシピを変更するか、対応する JEI のレシピ／用途画面を開きます。 |
| マウスホイール | アクティブなパネルを拡大縮小またはスクロールします。 |
| Shift + ホイール | 対応箇所での水平方向の移動。 |
| Ctrl + ホイール | 統合表示の拡大縮小、または浮動材料パネルの縮尺変更。 |
| 検索欄で Enter | 次の一致結果にフォーカスします。 |
| `F` | 検索欄にフォーカスします。 |
| `Ctrl+Z` | 直前の編集を元に戻します。 |
| `Ctrl+Y` | 元に戻した編集をやり直します。 |

キーバインドは Minecraft の「コントロール」設定画面から変更できます。

### クライアント設定

NeoForge はクライアント設定を `config/jeict-client.toml` に書き出します。

| キー | 既定値 | 用途 |
| --- | ---: | --- |
| `planning.rememberSelections` | `true` | レシピ／折りたたみ状態の記憶の読み書きを有効にします。 |
| `planning.autoMergeMaterials` | `true` | 概要画面を階層統合の材料表示で開きます。 |
| `planning.computeQuantities` | `true` | 積み上げ数量とパターン数の計算を有効にします。 |
| `planning.autoExpandUniqueRecipes` | `false` | 使用可能なレシピが 1 つだけの入力を自動的に展開します。 |
| `planning.substitutionStrategy` | `LOCKED` | 既定の代替材料割り当て戦略。 |
| `planning.preferredNamespace` | 空欄 | `PREFERRED_NAMESPACE` が優先する名前空間。 |
| `planning.memoryScope` | `SERVER` | 記憶の分離レベル：`GLOBAL`・`SERVER`・`WORLD` のいずれか。 |
| `planning.memoryProfile` | 空欄 | 手動で指定するモッドパック／プロファイル識別子。未設定時は MOD 一覧のフィンガープリントを使用します。 |
| `performance.showFloatingMaterials` | `true` | 浮動材料要求パネルを有効にします。 |
| `performance.maxAutoExpandStepsPerTick` | `32` | 1 クライアント tick あたりに行う一意なレシピの展開処理数を制限します。 |
| `performance.maxRecipeLookupResults` | `512` | 出力ごとにキャッシュする JEI レシピ検索結果の件数上限。 |

### 連携API

#### クラフティングバックエンド

`CraftingTreeBackends.register(...)` を通じて 1 つの `CraftingTreeBackend` を登録します。

主な拡張ポイントは次のとおりです。

- 出力全般の製造可否を判定する `isOutputCraftable(...)`
- 現在選択されているレシピのルートに対する `hasExactPattern(...)` と `exactPatternFingerprint(...)`
- 工具・型・容器・触媒を判定する `isReusableInput(...)`
- 実行手順や機械別実行回数レポート用の `machineId(...)`
- エンコード／アップロードの機能とアクション
- JEICT ネイティブなラベル付きボタンとしてエンコードアクションの隣に表示される、任意のアイテム／流体代替コントロール

新しい連携を実装する際は、同じ出力を生成するすべてのパターンを同一視するのではなく、正規化されたレシピのフィンガープリントに基づいて正確なパターン一致判定を実装してください。

#### 在庫ソース

`CraftingTreeInventorySources.register(...)` を通じてストレージを登録します。

```java
CraftingTreeInventorySources.register(new InventorySource() {
    @Override
    public String id() {
        return "example:network";
    }

    @Override
    public long version() {
        return networkChangeCounter;
    }

    @Override
    public List<InventoryAmount> snapshot() {
        return immutableAmounts;
    }
});
```

`version()` は、表示上の在庫量が変化したときのみ変更してください。`snapshot()` は不変、または安全にコピーされたビューを返してください。材料の識別には `MaterialKey` が使用され、これは JEI の ingredient type とサブタイプを考慮した UID を含みます。

安定 API はメイン jar に同梱されています（現行のメジャーバージョン：`1`）。新しい連携では、名前付き登録による `ApiRegistration` ハンドル、相互に見える在庫のための `authorityGroup()`、開いているメニュー向けの `CraftingTreeMenuInventorySources` の利用を推奨します。ライフサイクル・スレッド・互換性・メニュー・自動クラフトに関する制約の詳細は [docs/API.md](docs/API.md) を参照してください。

### 必要条件

JEI Crafting Tree はクライアント専用の MOD です。JEI と一緒にクライアント側にインストールしてください。専用サーバーには不要です。専用サーバーの jar フォルダに入れても、エントリポイントがクライアント専用のため害はありません。本 MOD は必須のクライアント／サーバー間ネットワークチャンネルを登録しないため、JEI Crafting Tree が入っていないサーバーにもクライアントは問題なく参加できます。ただし、エンコードやアップロードなどのオプション連携機能は、それぞれ独自のサーバー側要件を持つ場合があります。

- Minecraft `1.21.1`
- NeoForge `21.1.233` 以降の互換する `21.1.x` 系列
- Java `21`
- JEI `19.21.0` 以降。リリース `v0.0.1` は JEI `19.27.0.340` でテスト済み

オプション：

- AE2 Utility `1.6.0` 以降（導入かつ互換性がある場合、外部の AE2 向けバックエンドとして機能）
- Just Enough Characters（概要画面の検索欄でのピンイン一致に対応）

### ビルドとテスト

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

成果物は `build/libs/` に出力されます。より高速な Java のみの検証を行いたい場合は `./gradlew compileJava` または `.\gradlew.bat compileJava` を実行してください。

### 互換性に関する注意

- 計画エンジンは汎用的な JEI ingredient の識別に対応していますが、表示や外部在庫の検出は、JEI および導入済みの連携先がその ingredient type を正しく公開しているかどうかに依存します。
- 正確なパターン判定の精度は、バックエンドがルートを区別できる形で `hasExactPattern(...)` を実装しているかどうかに依存します。既定の互換実装は、古いバックエンドの挙動を維持するためのものにすぎません。
- ルート比較は、現在選択されているレシピツリーを異なる材料割り当て戦略のもとで評価するものであり、JEI に存在するすべてのレシピの組み合わせを自動的に網羅するものではありません。
- 計画処理はすべてクライアント側で完結し、それ自体がアイテムを移動させたり機械を動かしたりすることはありません。

### ライセンス

JEI Crafting Tree は [MIT License](LICENSE) の下で公開されています。
