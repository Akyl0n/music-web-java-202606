<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { placeholderMap } from '../../mock/admin.data'

const route = useRoute()

const moduleKey = computed(() => route.meta.moduleKey)
const info = computed(() => placeholderMap[moduleKey.value] || { title: '模块建设中', desc: '该模块正在准备中。' })
</script>

<template>
  <div class="module-page">
    <section class="module-hero">
      <p class="dashboard-kicker">管理模块</p>
      <h2>{{ info.title }}</h2>
      <p>{{ info.desc }}</p>
    </section>

    <div class="module-grid">
      <el-card shadow="never" class="module-card">
        <template #header>
          <div class="section-head section-head-mini">
            <h3>建议包含的功能</h3>
          </div>
        </template>
        <ul>
          <li>顶部查询区：关键词、状态、时间范围</li>
          <li>中间表格区：核心业务字段、状态字段、操作列</li>
          <li>新增/编辑弹窗：表单字段与校验逻辑</li>
          <li>删除二次确认：避免误操作</li>
        </ul>
      </el-card>

      <el-card shadow="never" class="module-card">
        <template #header>
          <div class="section-head section-head-mini">
            <h3>后续实现顺序</h3>
          </div>
        </template>
        <ol>
          <li>定义查询条件与表格字段</li>
          <li>补 API 封装层</li>
          <li>实现列表、分页、状态展示</li>
          <li>实现新增/编辑/删除交互</li>
        </ol>
      </el-card>
    </div>
  </div>
</template>
