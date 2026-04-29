<template>
  <div class="time-slot-management">
    <div class="page-header">
      <el-icon class="header-icon"><Clock /></el-icon>
      <span>预约时间管理</span>
    </div>

    <el-card>
      <div class="tip-text">
        配置每周各天的可预约时间范围，系统将自动按5分钟间隔生成可选时间点。
      </div>

      <div class="slot-grid">
        <div
          v-for="slot in slots"
          :key="slot.dayOfWeek"
          class="slot-card"
          :class="{ 'disabled': slot.enabled === 0 }"
        >
          <div class="slot-header">
            <span class="day-label">{{ slot.dayOfWeekName }}</span>
            <el-switch
              v-model="slot.enabled"
              :active-value="1"
              :inactive-value="0"
              size="small"
            />
          </div>

          <div class="slot-body" v-if="slot.enabled === 1">
            <div class="time-range">
              <div class="time-input-group">
                <span class="time-label">开始</span>
                <el-time-select
                  v-model="slot.startStr"
                  placeholder="开始时间"
                  start="00:00"
                  step="00:05"
                  end="23:55"
                  style="width: 130px"
                  size="small"
                />
              </div>
              <div class="time-input-group">
                <span class="time-label">结束</span>
                <el-time-select
                  v-model="slot.endStr"
                  placeholder="结束时间"
                  start="00:00"
                  step="00:05"
                  end="23:55"
                  style="width: 130px"
                  size="small"
                />
              </div>
            </div>
          </div>

          <div class="slot-disabled-text" v-else>
            <span>已禁用</span>
          </div>

          <div class="slot-footer">
            <el-button
              type="primary"
              size="small"
              @click="handleSave(slot)"
              :loading="slot.saving"
            >
              保存
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock } from '@element-plus/icons-vue'

const slots = ref([])

const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']

onMounted(() => {
  fetchSlots()
})

async function fetchSlots() {
  try {
    const response = await fetch('/api/time-slots')
    const result = await response.json()
    if (result.success && result.data) {
      slots.value = result.data.map(s => ({
        ...s,
        startStr: s.timeRangeStart ? s.timeRangeStart.substring(0, 5) : '',
        endStr: s.timeRangeEnd ? s.timeRangeEnd.substring(0, 5) : '',
        saving: false
      }))
    }
  } catch (e) {
    console.error('获取时间配置失败', e)
    ElMessage.error('获取时间配置失败')
  }
}

async function handleSave(slot) {
  if (!slot.startStr || !slot.endStr) {
    ElMessage.warning('请填写完整的开始和结束时间')
    return
  }

  slot.saving = true
  try {
    const response = await fetch('/api/time-slots', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        dayOfWeek: slot.dayOfWeek,
        timeRangeStart: slot.startStr + ':00',
        timeRangeEnd: slot.endStr + ':00',
        enabled: slot.enabled
      })
    })
    const result = await response.json()
    if (result.success) {
      ElMessage.success(`${slot.dayOfWeekName} 保存成功`)
    } else {
      ElMessage.error(result.message || '保存失败')
    }
  } catch (e) {
    console.error('保存失败', e)
    ElMessage.error('保存失败')
  } finally {
    slot.saving = false
  }
}
</script>

<style scoped>
.time-slot-management {
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

.tip-text {
  color: #909399;
  font-size: 14px;
  margin-bottom: 20px;
  padding: 10px 14px;
  background: #f4f4f5;
  border-radius: 6px;
}

.slot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.slot-card {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 16px;
  transition: all 0.3s;
  background: #fff;
}

.slot-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.slot-card.disabled {
  opacity: 0.65;
}

.slot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.day-label {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.slot-body {
  margin-bottom: 14px;
}

.time-range {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.time-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-label {
  font-size: 13px;
  color: #606266;
  width: 36px;
}

.slot-disabled-text {
  text-align: center;
  color: #c0c4cc;
  font-size: 14px;
  padding: 12px 0;
  margin-bottom: 10px;
}

.slot-footer {
  display: flex;
  justify-content: flex-end;
}

@media screen and (max-width: 768px) {
  .time-slot-management {
    padding: 20px 15px;
  }

  .page-header {
    font-size: 18px;
    margin-bottom: 20px;
    padding-bottom: 15px;
  }

  .slot-grid {
    grid-template-columns: 1fr;
  }
}
</style>
