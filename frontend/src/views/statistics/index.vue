<template>
  <div class="statistics-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon blue">
              <el-icon :size="32"><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalEquipment || 0 }}</div>
              <div class="stat-label">设备总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon green">
              <el-icon :size="32"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.normalEquipment || 0 }}</div>
              <div class="stat-label">正常设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon orange">
              <el-icon :size="32"><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.borrowedEquipment || 0 }}</div>
              <div class="stat-label">借出设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon red">
              <el-icon :size="32"><Tools /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.maintenanceEquipment || 0 }}</div>
              <div class="stat-label">维修设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>设备状态分布</span>
          </template>
          <div ref="statusChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>设备利用率</span>
          </template>
          <div ref="utilizationChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>借用统计</span>
          </template>
          <div ref="borrowChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>设备损坏率</span>
          </template>
          <div ref="damageChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="detail-card">
      <template #header>
        <div class="detail-header">
          <span>详细统计</span>
          <el-button type="primary" :icon="Download" @click="handleExport" size="small">导出报表</el-button>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="设备总数">{{ statistics.totalEquipment || 0 }}</el-descriptions-item>
        <el-descriptions-item label="正常设备">{{ statistics.normalEquipment || 0 }}</el-descriptions-item>
        <el-descriptions-item label="借出设备">{{ statistics.borrowedEquipment || 0 }}</el-descriptions-item>
        <el-descriptions-item label="维修中设备">{{ statistics.maintenanceEquipment || 0 }}</el-descriptions-item>
        <el-descriptions-item label="报废设备">{{ statistics.scrappedEquipment || 0 }}</el-descriptions-item>
        <el-descriptions-item label="设备利用率">{{ statistics.utilizationRate?.toFixed(2) || 0 }}%</el-descriptions-item>
        <el-descriptions-item label="今日借用">{{ statistics.todayBorrowCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="本月借用">{{ statistics.monthBorrowCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="待审核预约">{{ statistics.pendingApprovalCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="待处理报修">{{ statistics.pendingRepairCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="设备损坏率">{{ statistics.damageRate?.toFixed(2) || 0 }}%</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { statisticsApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'

const statistics = ref({})
const statusChartRef = ref(null)
const utilizationChartRef = ref(null)
const borrowChartRef = ref(null)
const damageChartRef = ref(null)

const getStatistics = async () => {
  try {
    const res = await statisticsApi.getStatistics()
    statistics.value = res.data || {}
    nextTick(() => {
      initCharts()
    })
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const initCharts = () => {
  // 设备状态分布
  if (statusChartRef.value) {
    const chart = echarts.init(statusChartRef.value)
    const data = [
      { value: statistics.value.normalEquipment || 0, name: '正常' },
      { value: statistics.value.borrowedEquipment || 0, name: '借出' },
      { value: statistics.value.maintenanceEquipment || 0, name: '维修中' },
      { value: statistics.value.scrappedEquipment || 0, name: '报废' }
    ]
    chart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  }

  // 设备利用率
  if (utilizationChartRef.value) {
    const chart = echarts.init(utilizationChartRef.value)
    const utilization = statistics.value.utilizationRate || 0
    chart.setOption({
      tooltip: {
        formatter: '{b}: {c}%',
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        borderColor: '#409EFF',
        borderWidth: 1,
        textStyle: { color: '#fff', fontSize: 14 }
      },
      series: [{
        type: 'gauge',
        min: 0,
        max: 100,
        startAngle: 210,
        endAngle: -30,
        radius: '90%',
        center: ['50%', '55%'],
        // 仪表盘轴线样式
        axisLine: {
          lineStyle: {
            width: 25,
            color: [
              [0.4, '#67c23a'],
              [0.7, '#e6a23c'],
              [1, '#f56c6c']
            ],
            shadowColor: 'rgba(0, 0, 0, 0.3)',
            shadowBlur: 10,
            shadowOffsetX: 2,
            shadowOffsetY: 2
          }
        },
        // 刻度线样式
        axisTick: {
          distance: -25,
          length: 8,
          lineStyle: { color: '#fff', width: 2 }
        },
        splitLine: {
          distance: -30,
          length: 15,
          lineStyle: { color: '#fff', width: 3 }
        },
        axisLabel: {
          distance: -50,
          color: '#666',
          fontSize: 12,
          fontWeight: 'bold'
        },
        // 指针样式
        pointer: {
          length: '65%',
          width: 6,
          itemStyle: {
            color: 'auto',
            shadowColor: 'rgba(0, 0, 0, 0.3)',
            shadowBlur: 5,
            shadowOffsetX: 1,
            shadowOffsetY: 1
          },
          icon: 'path://M2090.36389,615.30999 L2090.36389,615.30999 C2091.48372,615.30999 2092.40383,616.194028 2092.44859,617.312956 L2096.90698,728.755929 C2097.05155,732.369577 2094.2393,735.416212 2090.62566,735.56078 C2090.53845,735.564269 2090.45117,735.566014 2090.36389,735.566014 L2090.36389,735.566014 C2086.74736,735.566014 2083.81557,732.63423 2083.81557,729.017692 C2083.81557,728.930412 2083.81732,728.84314 2083.82081,728.755929 L2088.2792,617.312956 C2088.32396,616.194028 2089.24407,615.30999 2090.36389,615.30999 Z'
        },
        // 仪表盘详情（数值显示）
        detail: {
          valueAnimation: true,
          formatter: '{value}%',
          color: '#333',
          fontSize: 36,
          fontWeight: 'bold',
          offsetCenter: [0, '25%'],
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          borderColor: '#409EFF',
          borderWidth: 2,
          borderRadius: 10,
          width: 110,
          height: 50,
          lineHeight: 50
        },
        // 标题
        title: {
          offsetCenter: [0, '50%'],
          fontSize: 16,
          color: '#999',
          fontWeight: 'normal'
        },
        data: [{
          value: utilization.toFixed(1),
          name: '设备利用率'
        }],
        // 动画配置
        animationDuration: 2000,
        animationEasing: 'elasticOut'
      }]
    })
  }

  // 借用统计（模拟数据）
  if (borrowChartRef.value) {
    const chart = echarts.init(borrowChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar',
        data: [statistics.value.todayBorrowCount || 0, 5, 8, 3, 6, 2, 1],
        itemStyle: { color: '#409EFF' }
      }]
    })
  }

  // 设备损坏率
  if (damageChartRef.value) {
    const chart = echarts.init(damageChartRef.value)
    const damageRate = statistics.value.damageRate || 0
    chart.setOption({
      tooltip: { formatter: '{b}: {c}%' },
      series: [{
        type: 'pie',
        radius: ['50%', '70%'],
        data: [
          { value: damageRate, name: '损坏率', itemStyle: { color: '#f56c6c' } },
          { value: 100 - damageRate, name: '完好率', itemStyle: { color: '#67c23a' } }
        ],
        label: { show: true, formatter: '{b}: {c}%' }
      }]
    })
  }
}

// 导出统计报表
const handleExport = async () => {
  try {
    // res 已经是 Blob 对象（因为 responseType: 'blob'）
    const blob = await statisticsApi.export()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const now = new Date()
    const fileName = `统计报表_${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}_${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}.xlsx`
    link.download = fileName
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出错误:', error)
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  getStatistics()
  window.addEventListener('resize', () => {
    statusChartRef.value && echarts.getInstanceByDom(statusChartRef.value)?.resize()
    utilizationChartRef.value && echarts.getInstanceByDom(utilizationChartRef.value)?.resize()
    borrowChartRef.value && echarts.getInstanceByDom(borrowChartRef.value)?.resize()
    damageChartRef.value && echarts.getInstanceByDom(damageChartRef.value)?.resize()
  })
})
</script>

<style lang="scss" scoped>
.statistics-page {
  .stats-row {
    margin-bottom: 20px;
  }

  .stat-card {
    cursor: pointer;
    transition: transform 0.3s, box-shadow 0.3s;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .stat-content {
      display: flex;
      align-items: center;
      gap: 20px;
    }

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;

      &.blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
      &.green { background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%); }
      &.orange { background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%); }
      &.red { background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%); }
    }

    .stat-info {
      flex: 1;

      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: #333;
        line-height: 1;
        margin-bottom: 8px;
      }

      .stat-label {
        font-size: 14px;
        color: #999;
      }
    }
  }

  .charts-row {
    margin-bottom: 20px;
  }

  .chart-card {
    margin-bottom: 20px;
  }

  .detail-card {
    :deep(.el-descriptions__label) {
      width: 120px;
    }

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
}
</style>
