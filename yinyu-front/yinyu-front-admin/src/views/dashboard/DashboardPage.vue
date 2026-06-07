<template>
  <div v-loading="loading" class="dashboard-page">
    <section class="dashboard-hero">
      <div>
        <p class="dashboard-kicker">控制台</p>
        <h2>后台数据看板</h2>
        <p>围绕当前已经完成的歌曲、歌单、歌手、首页推荐、用户管理等功能，统一查看内容资源、运营配置和用户互动数据。</p>
      </div>
      <div class="dashboard-actions">
        <button class="ghost-action" type="button" @click="fetchDashboard">刷新看板</button>
      </div>
    </section>

    <section class="metric-grid">
      <el-card v-for="card in dashboard.metrics" :key="card.title" shadow="never" class="metric-card">
        <template #header>
          <div class="metric-card-header">
            <h3>{{ card.title }}</h3>
          </div>
        </template>

        <div class="metric-item">
          <div>
            <p>{{ card.primaryLabel }}</p>
            <strong>{{ card.primaryValue }}</strong>
          </div>
          <div class="sparkline" :class="`is-${card.accent}`">
            <span v-for="(point, index) in card.trend || []" :key="`${card.title}-${index}`"
              :style="{ height: `${point}px` }"></span>
          </div>
        </div>

        <div class="metric-item">
          <div>
            <p>{{ card.secondaryLabel }}</p>
            <strong>{{ card.secondaryValue }}</strong>
          </div>
          <div class="metric-footnote">当前系统内的关键实时汇总数据</div>
        </div>
      </el-card>
    </section>

    <section class="dashboard-main-grid">
      <el-card shadow="never" class="chart-card">
        <template #header>
          <div class="section-head">
            <div>
              <h3>内容概览</h3>
            </div>
          </div>
        </template>

        <div class="dashboard-stat-grid">
          <div v-for="item in dashboard.contentStats" :key="item.label" class="dashboard-stat-card">
            <strong>{{ item.value }}</strong>
            <h4>{{ item.label }}</h4>
            <p>{{ item.desc }}</p>
          </div>
        </div>
      </el-card>

      <div class="side-info-column">
        <el-card shadow="never" class="notice-card">
          <template #header>
            <div class="section-head section-head-mini">
              <h3>运营概览</h3>
            </div>
          </template>
          <div class="dashboard-operation-grid">
            <div v-for="item in dashboard.operationStats" :key="item.label" class="dashboard-operation-card">
              <strong>{{ item.label }}</strong>
              <span>{{ item.value }}</span>
              <p>{{ item.desc }}</p>
            </div>
          </div>
        </el-card>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getDashboard } from '../../api/dashboard'

const loading = ref(false)
const dashboard = reactive({
  metrics: [],
  contentStats: [],
  operationStats: [],
})

async function fetchDashboard() {
  loading.value = true
  try {
    const data = await getDashboard()
    Object.assign(dashboard, {
      metrics: data?.metrics || [],
      contentStats: data?.contentStats || [],
      operationStats: data?.operationStats || [],
    })
  } catch (error) {
    ElMessage.error(error.message || '获取数据看板失败')
  } finally {
    loading.value = false
  }
}

fetchDashboard()
</script>
