// ESLint flat config(workspace 根统一,覆盖 apps/* 与 packages/shared)。
// 只承担"正确性 + 明显坏味道",风格问题全部交给 Prettier(见 .prettierrc.json),
// 两者用 eslint-config-prettier 关闭冲突规则。
import js from '@eslint/js'
import tseslint from 'typescript-eslint'
import pluginVue from 'eslint-plugin-vue'
import vueTsConfig from '@vue/eslint-config-typescript'
import prettierConfig from 'eslint-config-prettier'

export default tseslint.config(
  { ignores: ['**/dist/**', '**/node_modules/**', '**/*.d.ts', '**/auto-imports.d.ts', '**/components.d.ts', 'backend/**', 'assets/**', 'documents/**'] },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  ...pluginVue.configs['flat/recommended'],
  ...vueTsConfig(),
  {
    files: ['**/*.vue'],
    languageOptions: {
      parserOptions: { parser: tseslint.parser },
    },
  },
  {
    files: ['apps/*/scripts/**/*.mjs'],
    languageOptions: {
      globals: { process: 'readonly', console: 'readonly', fetch: 'readonly' },
    },
  },
  {
    rules: {
      // 项目约定:空参数与未用变量交给 TS 编译器把关(vue-tsc -b),此处放宽避免双重报错。
      '@typescript-eslint/no-unused-vars': 'off',
      '@typescript-eslint/no-explicit-any': 'off',
      // 服务层统一抛带中文业务信息的 Error,页面只展示 message;cause 链对本项目无消费方。
      'preserve-caught-error': 'off',
      // Vue:组件名与文件名一致性对多词组件有价值,但本项目视图以页面名命名,放宽。
      'vue/multi-word-component-names': 'off',
      // 单行/多行属性换行交给 Prettier 的 vue 插件式排版,关闭深度冲突项。
      'vue/max-attributes-per-line': 'off',
      'vue/singleline-html-element-content-newline': 'off',
      'vue/html-self-closing': 'off',
      'vue/html-indent': 'off',
      'vue/html-quotes': 'off',
    },
  },
  prettierConfig,
)
