<template>
  <div class="gantt-page">
    <div class="page-header">
      <el-icon class="header-icon"><DataLine /></el-icon>
      <span>预约甘特图</span>
    </div>
    
    <div class="date-navigation">
      <el-button-group>
        <el-button @click="previousDay">前一天</el-button>
        <el-button @click="gotoToday">今天</el-button>
        <el-button @click="nextDay">后一天</el-button>
      </el-button-group>
      <el-date-picker
        v-model="currentDate"
        type="date"
        placeholder="选择日期"
        @change="updateTimeline"
        style="margin-left: 20px"
      />
      <div class="legend">
        <div class="legend-item">
          <div class="legend-color status-active"></div>
          <span>借出中</span>
        </div>
        <div class="legend-item">
          <div class="legend-color status-returned"></div>
          <span>已归还</span>
        </div>
        <div class="legend-item">
          <div class="legend-color status-overdue"></div>
          <span>逾期未还</span>
        </div>
      </div>
    </div>
    
    <div class="gantt-container">
      <div class="gantt-left-header">
        <div class="header-cell">设备</div>
      </div>
      <div 
        class="gantt-right-header"
        ref="headerRef"
        @scroll="handleHeaderScroll"
      >
        <div class="header-row">
          <div 
            v-for="(hour, index) in timelineHours" 
            :key="'date-' + index" 
            class="header-date-cell"
            :style="{ width: '20px' }"
          >
            <template v-if="hour.isNewDay">
              {{ hour.dayLabel }}
            </template>
          </div>
        </div>
        <div class="header-row">
          <div 
            v-for="(hour, index) in timelineHours" 
            :key="'hour-' + index" 
            class="header-hour-cell"
            :class="{ 'today': isNow(hour.time) }"
            :style="{ width: '20px' }"
          >
            {{ hour.hourLabel }}
          </div>
        </div>
      </div>
      
      <div class="gantt-left-body">
        <div 
          v-for="equipment in equipmentList" 
          :key="equipment.id" 
          class="row-cell"
        >
          <div class="equipment-name">{{ equipment.name }}</div>
          <div class="equipment-type">{{ equipment.type }}</div>
        </div>
      </div>
      <div 
        class="gantt-right-body"
        ref="bodyRef"
        @scroll="handleBodyScroll"
      >
        <div 
          v-for="equipment in equipmentList" 
          :key="equipment.id" 
          class="row-body"
        >
          <div 
            v-for="task in getEquipmentTasks(equipment.id)" 
            :key="task.id"
            class="task-bar"
            :style="getTaskBarStyle(task)"
            :class="task.status"
          >
            <div class="task-label">{{ task.name }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DataLine } from '@element-plus/icons-vue'

const currentDate = ref(new Date())
const equipmentList = ref([])
const rentalRecords = ref([])
const headerRef = ref(null)
const bodyRef = ref(null)
let isScrolling = false

const timelineHours = computed(() => {
  const hours = []
  const startDate = new Date(currentDate.value)
  startDate.setHours(0, 0, 0, 0)
  
  for (let i = 0; i < 96; i++) {
    const date = new Date(startDate)
    date.setHours(startDate.getHours() + i)
    const hour = date.getHours().toString().padStart(2, '0')
    const day = date.getDate()
    const month = date.getMonth() + 1
    const prevHour = new Date(startDate)
    prevHour.setHours(startDate.getHours() + i - 1)
    const isNewDay = i === 0 || prevHour.getDate() !== day
    
    hours.push({
      time: date.toISOString(),
      hourLabel: hour,
      dayLabel: `${month}/${day}`,
      isNewDay: isNewDay
    })
  }
  return hours
})

const allTasks = computed(() => {
  const tasks = []
  
  rentalRecords.value.forEach(rental => {
    const status = getStatusClass(rental)
    const barColor = status === 'active' ? '#409eff' : status === 'overdue' ? '#f56c6c' : '#67c23a'
    
    if (rental.camara?.id) {
      tasks.push({
        id: `${rental.id}-camara`,
        name: rental.name,
        equipmentId: rental.camara.id,
        startDate: rental.brwtime,
        endDate: rental.rtuntime,
        status: status,
        color: barColor
      })
    }
    
    if (rental.lens?.id) {
      tasks.push({
        id: `${rental.id}-lens`,
        name: rental.name,
        equipmentId: rental.lens.id,
        startDate: rental.brwtime,
        endDate: rental.rtuntime,
        status: status,
        color: barColor
      })
    }
    
    if (rental.other?.id) {
      tasks.push({
        id: `${rental.id}-other`,
        name: rental.name,
        equipmentId: rental.other.id,
        startDate: rental.brwtime,
        endDate: rental.rtuntime,
        status: status,
        color: barColor
      })
    }
  })
  
  return tasks
})

const getEquipmentTasks = (equipmentId) => {
  return allTasks.value.filter(task => task.equipmentId === equipmentId)
}

const getTaskBarStyle = (task) => {
  const hourWidth = 20
  const startDate = new Date(task.startDate)
  const endDate = new Date(task.endDate)
  const timelineStart = new Date(timelineHours.value[0].time)
  const timelineEnd = new Date(timelineHours.value[timelineHours.value.length - 1].time)
  timelineEnd.setMinutes(59, 59, 999)
  
  if (endDate < timelineStart || startDate > timelineEnd) {
    return {
      display: 'none'
    }
  }
  
  const hourStart = Math.max(0, (startDate - timelineStart) / (60 * 60 * 1000))
  const hourEnd = Math.min(timelineHours.value.length - 1, (endDate - timelineStart) / (60 * 60 * 1000))
  
  const left = hourStart * hourWidth
  const width = Math.max(hourWidth, (hourEnd - hourStart) * hourWidth)
  
  return {
    left: `${left}px`,
    width: `${width}px`,
    backgroundColor: task.color
  }
}

const isNow = (timeStr) => {
  const now = new Date()
  const time = new Date(timeStr)
  const nextHour = new Date(timeStr)
  nextHour.setHours(nextHour.getHours() + 1)
  return now >= time && now < nextHour
}

const getStatusClass = (rental) => {
  const status = rental.status
  if (status === 0) return 'active'
  if (status === 1) return 'returned'
  if (status === 2) return 'overdue'
  return 'returned'
}

const previousDay = () => {
  currentDate.value = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth(), currentDate.value.getDate() - 1)
}

const nextDay = () => {
  currentDate.value = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth(), currentDate.value.getDate() + 1)
}

const gotoToday = () => {
  currentDate.value = new Date()
}

const updateTimeline = () => {
}

const handleHeaderScroll = () => {
  if (isScrolling) return
  isScrolling = true
  if (bodyRef.value) {
    bodyRef.value.scrollLeft = headerRef.value.scrollLeft
  }
  requestAnimationFrame(() => {
    isScrolling = false
  })
}

const handleBodyScroll = () => {
  if (isScrolling) return
  isScrolling = true
  if (headerRef.value) {
    headerRef.value.scrollLeft = bodyRef.value.scrollLeft
  }
  requestAnimationFrame(() => {
    isScrolling = false
  })
}

const fetchData = async () => {
  try {
    const [devicesRes, recordsRes] = await Promise.all([
      fetch('/api/devices'),
      fetch('/api/rent-records')
    ])
    
    const devicesData = await devicesRes.json()
    const recordsData = await recordsRes.json()
    
    if (devicesData.success) {
      equipmentList.value = devicesData.data
    }
    
    if (recordsData.success) {
      rentalRecords.value = recordsData.data
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.gantt-page {
  width: 100%;
  min-height: 100%;
  background-color: #fff;
  padding: 30px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.header-icon {
  font-size: 26px;
  color: #409eff;
}

.date-navigation {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.gantt-container {
  display: grid;
  grid-template-columns: 200px 1fr;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.gantt-left-header {
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  position: sticky;
  left: 0;
  z-index: 10;
  display: flex;
}

.gantt-right-header {
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: none;
}

.gantt-right-header::-webkit-scrollbar {
  display: none;
}

.header-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;
}

.header-row:last-child {
  border-bottom: none;
}

.gantt-left-header .header-cell {
  width: 200px;
  height: 80px;
  display: flex;
  align-items: center;
  text-align: left;
  padding-left: 16px;
  font-weight: 600;
  color: #606266;
}

.header-date-cell,
.header-hour-cell {
  flex-shrink: 0;
  padding: 6px 2px;
  text-align: center;
  font-size: 10px;
  border-right: 1px solid #ebeef5;
}

.header-date-cell {
  font-weight: 600;
  color: #606266;
}

.header-hour-cell {
  color: #909399;
}

.header-hour-cell.today {
  background-color: #ecf5ff;
  color: #409eff;
}

.gantt-left-body {
  position: sticky;
  left: 0;
  background-color: #fff;
  z-index: 5;
}

.gantt-right-body {
  position: relative;
  overflow-x: auto;
}

.row-cell,
.row-body {
  height: 60px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
}

.row-cell {
  padding: 8px 16px;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
}

.row-body {
  position: relative;
  min-width: 1920px;
}

.equipment-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.equipment-type {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.task-bar {
  position: absolute;
  height: 36px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  padding: 0 4px;
  color: #fff;
  font-size: 10px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  top: 12px;
  transition: transform 0.2s;
  z-index: 1;
}

.task-bar:hover {
  transform: scale(1.02);
  z-index: 2;
}

.task-bar.status-active {
  background-color: #409eff;
}

.task-bar.status-returned {
  background-color: #67c23a;
}

.task-bar.status-overdue {
  background-color: #f56c6c;
}

.task-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.legend {
  display: flex;
  gap: 20px;
  margin-left: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 3px;
}

.legend-color.status-active {
  background-color: #409eff;
}

.legend-color.status-returned {
  background-color: #67c23a;
}

.legend-color.status-overdue {
  background-color: #f56c6c;
}

@media screen and (max-width: 768px) {
  .gantt-page {
    padding: 20px 15px;
  }
  
  .page-header {
    font-size: 18px;
    margin-bottom: 20px;
    padding-bottom: 15px;
  }
  
  .header-icon {
    font-size: 22px;
  }
  
  .legend {
    flex-wrap: wrap;
    gap: 15px;
  }
}
</style>
