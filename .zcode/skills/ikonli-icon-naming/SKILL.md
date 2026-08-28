---
name: ikonli-icon-naming
description: 项目使用 Ikonli Material2 图标包，选择 FontIcon iconLiteral 前缀时使用。适用于在 FXML 中写 iconLiteral 属性、CSS 中写 -fx-icon-code、Java 中 new FontIcon(...) 时，确定图标描述字符串的前缀是否正确。
---

# Ikonli 图标命名格式

## 核心规则：前缀由图标名的首字母范围决定

本项目使用 **Ikonli Material2** 图标包（`ikonli-material2-pack`），包含 4 个枚举类，按图标名首字母 A-L / M-Z 分组：

| 前缀 | 枚举类 | 覆盖范围 | 示例 |
|------|--------|----------|------|
| `mdal-` | `Material2AL` | 图标名首字母 A~L | `mdal-content_copy`, `mdal-arrow_drop_down`, `mdal-edit` |
| `mdmz-` | `Material2MZ` | 图标名首字母 M~Z | `mdmz-refresh`, `mdmz-play_arrow`, `mdmz-settings` |
| `mdoal-` | `Material2OutlinedAL` | Outline 风格，首字母 A~L | `mdoal-close`, `mdoal-cloud_download` |
| `mdomz-` | `Material2OutlinedMZ` | Outline 风格，首字母 M~Z | `mdomz-play_circle_outline` |

另外项目还引入了其他图标包（但使用较少）：

| 前缀 | 图标包 | 示例 |
|------|--------|------|
| `gmi-` | MaterialDesign | `gmi-10k` |
| `mdi-` | MaterialDesign (旧) | `mdi-access-point` |
| `anto-` | AntDesign Outlined | `anto-github`, `anto-qq` |
| `antf-` | AntDesign Filled | `antf-pay-circle` |
| `bi-` | BootstrapIcons | `bi-alarm` |

## 判断方法

1. 取图标名（去掉前缀后的部分），如 `arrow_drop_down`、`refresh`、`content_copy`
2. 看首字母：
   - **A~L**（a b c d e f g h i j k l）→ 用 `mdal-` 前缀
   - **M~Z**（m n o p q r s t u v w x y z）→ 用 `mdmz-` 前缀
3. Outline 风格同理，加 `o`：`mdoal-` / `mdomz-`

## 常见错误

| 错误写法 | 错误原因 | 正确写法 |
|----------|----------|----------|
| `mdal-refresh` | `refresh` 以 R 开头（M~Z），不该用 al | `mdmz-refresh` |
| `mdmz-arrow_drop_down` | `arrow_drop_down` 以 A 开头（A~L），不该用 mz | `mdal-arrow_drop_down` |
| `mdal-play_arrow` | `play_arrow` 以 P 开头（M~Z），不该用 al | `mdmz-play_arrow` |
| `mdal-content_copy` | ✅ 正确，`content_copy` 以 C 开头（A~L） | `mdal-content_copy` |

## 项目中已验证可用的图标

以下图标在本项目 FXML 中实际使用并通过运行时验证：

**mdal- 前缀（A~L）：**
`mdal-content_copy`, `mdal-arrow_drop_down`, `mdal-check_circle_outline`, `mdal-delete_outline`, `mdal-desktop_mac`, `mdal-done_all`, `mdal-edit`, `mdal-folder_open`, `mdal-image`, `mdal-inbox`

**mdmz- 前缀（M~Z）：**
`mdmz-refresh`, `mdmz-play_arrow`, `mdmz-arrow_drop_down`(❌无效), `mdmz-menu_book`, `mdmz-settings`, `mdmz-person_add_alt_1`, `mdmz-person_add_disabled`

## 注意事项

- `arrow_drop_down` 虽然以 `arrow` 开头（A），但 `mdmz-arrow_drop_down` **无效**，必须用 `mdal-arrow_drop_down`
- 规则的本质是：整个图标描述字符串（去掉 `md` 和 `al`/`mz` 前缀后）的首字母决定用 AL 还是 MZ
- 如果不确定图标是否存在，可用 `javap -cp <ikonli-material2-pack.jar> org.kordamp.ikonli.material2.Material2AL` 查看所有可用枚举常量
- FXML 中用 `iconLiteral="mdal-xxx"`，CSS 中用 `-fx-icon-code: "mdal-xxx"`
