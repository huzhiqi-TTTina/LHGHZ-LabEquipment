<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
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
            <div class="stat-icon normal">
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
            <div class="stat-icon borrowed">
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
            <div class="stat-icon warning">
              <el-icon :size="32"><Tools /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.pendingRepairCount || 0 }}</div>
              <div class="stat-label">待处理报修</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- 设备利用率图表 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>设备利用率</span>
            </div>
          </template>
          <div ref="utilizationChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <!-- 设备状态分布 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>设备状态分布</span>
            </div>
          </template>
          <div ref="statusChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待办事项 -->
    <el-card class="todo-card">
      <template #header>
        <div class="card-header">
          <span>待办事项</span>
        </div>
      </template>
      <el-empty v-if="todoList.length === 0" description="暂无待办事项" />
      <el-table v-else :data="todoList" style="width: 100%">
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.tagType">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="内容" />
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleTodo(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { statisticsApi } from '@/api'

const router = useRouter()

const utilizationChartRef = ref(null)
const statusChartRef = ref(null)
const statistics = ref({})
const todoList = ref([])

// 获取统计数据
const getStatistics = async () => {
  try {
    const res = await statisticsApi.getStatistics()
    statistics.value = res.data || {}

    // 模拟待办事项
    todoList.value = [
      {
        type: '预约审核',
        title: `有 ${statistics.value.pendingApprovalCount || 0} 条预约待审核`,
        time: '待处理',
        tagType: 'warning',
        routeName: 'Reservation'
      },
      {
        type: '故障报修',
        title: `有 ${statistics.value.pendingRepairCount || 0} 条报修待处理`,
        time: '待处理',
        tagType: 'danger',
        routeName: 'Repair'
      }
    ]

    nextTick(() => {
      initCharts()
    })
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 初始化图表
const initCharts = () => {
  // 利用率图表
  if (utilizationChartRef.value) {
    const chart = echarts.init(utilizationChartRef.value)
    chart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}%'
      },
      series: [
        {
          type: 'gauge',
          min: 0,
          max: 100,
          axisLine: {
            lineStyle: {
              width: 30,
              color: [[0.3, '#67c23a'], [0.7, '#e6a23c'], [1, '#f56c6c']]
            }
          },
          pointer: {
            itemStyle: {
              color: 'auto'
            }
          },
          axisTick: {
            distance: -30,
            length: 8,
            lineStyle: {
              color: '#fff',
              width: 2
            }
          },
          splitLine: {
            distance: -30,
            length: 30,
            lineStyle: {
              color: '#fff',
              width: 4
            }
          },
          axisLabel: {
            color: 'auto',
            distance: 40,
            fontSize: 20
          },
          detail: {
            valueAnimation: true,
            formatter: '{value}%',
            color: 'auto',
            fontSize: 20
          },
          data: [
            {
              value: statistics.value.utilizationRate?.toFixed(1) || 0
            }
          ]
        }
      ]
    })
  }

  // 状态分布图表
  if (statusChartRef.value) {
    const chart = echarts.init(statusChartRef.value)
    const data = [
      { value: statistics.value.normalEquipment || 0, name: '正常' },
      { value: statistics.value.borrowedEquipment || 0, name: '借出' },
      { value: statistics.value.maintenanceEquipment || 0, name: '维修中' },
      { value: statistics.value.scrappedEquipment || 0, name: '报废' }
    ]
    chart.setOption({
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          type: 'pie',
          radius: '50%',
          data: data,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
  }
}

// 处理待办事项点击
const handleTodo = (row) => {
  if (row.routeName) {
    router.push({ name: row.routeName })
  }
}

onMounted(() => {
  getStatistics()

  // 响应式
  window.addEventListener('resize', () => {
    if (utilizationChartRef.value) {
      echarts.getInstanceByDom(utilizationChartRef.value)?.resize()
    }
    if (statusChartRef.value) {
      echarts.getInstanceByDom(statusChartRef.value)?.resize()
    }
  })
})
</script>

<style lang="scss" scoped>
.dashboard {
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

      &.total {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.normal {
        background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
      }

      &.borrowed {
        background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
      }

      &.warning {
        background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
      }
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
    .card-header {
      font-weight: bold;
    }
  }

  .todo-card {
    .card-header {
      font-weight: bold;
    }
  }
}
</style>
