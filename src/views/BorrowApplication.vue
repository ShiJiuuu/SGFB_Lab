<template>
  <div class="borrow-application">
    <div class="page-header">
      <el-icon class="header-icon"><Camera /></el-icon>
      <span>预约申请</span>
    </div>

    <el-dialog 
      v-model="announceVisible" 
      title="公告" 
      width="60%"
      :close-on-click-modal="false"
      :show-close="true"
    >
      <div class="announce-content" v-html="renderedContent"></div>
      <template #footer>
        <el-button type="primary" @click="closeAnnounce">我已知悉</el-button>
      </template>
    </el-dialog>
    
    <el-form
      ref="borrowFormRef"
      :model="borrowForm"
      :rules="rules"
      label-width="100px"
      class="borrow-form"
    >
      <el-divider content-position="left">
        <el-icon><User /></el-icon>
        个人信息
      </el-divider>
      
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :span="12">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="borrowForm.name" placeholder="请输入姓名">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :span="12">
          <el-form-item label="学号" prop="studentId">
            <el-input v-model="borrowForm.studentId" placeholder="请输入学号">
              <template #prefix>
                <el-icon><Tickets /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="borrowForm.phone" placeholder="请输入手机号">
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-divider content-position="left">
        <el-icon><Calendar /></el-icon>
        预约时间
      </el-divider>
      
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :span="12">
          <el-form-item label="预约时间" prop="borrowDate">
            <el-date-picker
              v-model="borrowForm.borrowDate"
              type="datetime"
              placeholder="选择预约时间"
              style="width: 100%"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm"
              :disabled-date="disabledDate"
              :disabled-hours="disabledHours"
              :disabled-minutes="disabledMinutes"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :span="12">
          <el-form-item label="归还时间" prop="returnDate">
            <el-date-picker
              v-model="borrowForm.returnDate"
              type="datetime"
              placeholder="选择归还时间"
              style="width: 100%"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-divider content-position="left">
        <el-icon><Setting /></el-icon>
        设备选择
      </el-divider>
      
      <el-row :gutter="20">
        <el-col :xs="24" :sm="8" :span="8">
          <el-form-item label="相机" prop="camera">
            <el-select v-model="borrowForm.camera" placeholder="选择相机" clearable style="width: 100%" no-data-text="当前无可用设备">
              <el-option 
                v-for="device in cameraList" 
                :key="device.id" 
                :label="device.name" 
                :value="device.id" 
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="8" :span="8">
          <el-form-item label="镜头" prop="lens">
            <el-select v-model="borrowForm.lens" placeholder="选择镜头" clearable style="width: 100%" no-data-text="当前无可用设备">
              <el-option 
                v-for="device in lensList" 
                :key="device.id" 
                :label="device.name" 
                :value="device.id" 
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="8" :span="8">
          <el-form-item label="其他" prop="other">
            <el-select v-model="borrowForm.other" placeholder="选择其他设备" clearable style="width: 100%" no-data-text="当前无可用设备">
              <el-option 
                v-for="device in otherList" 
                :key="device.id" 
                :label="device.name" 
                :value="device.id" 
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="备注">
            <el-input
              v-model="borrowForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注信息（选填）"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-row>
        <el-col :span="24" class="button-group">
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            <el-icon><Check /></el-icon>
            提交申请
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, watch, computed } from 'vue'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'
import { 
  Camera, 
  User, 
  Tickets, 
  Phone, 
  Setting, 
  Calendar, 
  Document,
  Check,
  RefreshRight
} from '@element-plus/icons-vue'

const borrowFormRef = ref()
const loading = ref(false)
const cameraList = ref([])
const lensList = ref([])
const otherList = ref([])
const announceVisible = ref(false)
const announceContent = ref('')

const renderedContent = computed(() => {
  return announceContent.value ? marked(announceContent.value) : ''
})

const borrowForm = reactive({
  name: '',
  studentId: '',
  phone: '',
  camera: null,
  lens: null,
  other: null,
  borrowDate: '',
  returnDate: '',
  remark: ''
})

const validateBorrowDate = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请选择预约时间'))
  } else {
    const now = new Date()
    now.setSeconds(0, 0)
    if (new Date(value) < now) {
      callback(new Error('预约时间不能早于当前时间'))
    } else {
      callback()
    }
  }
}

const validateReturnDate = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请选择归还时间'))
  } else if (borrowForm.borrowDate && new Date(value) <= new Date(borrowForm.borrowDate)) {
    callback(new Error('归还时间必须晚于预约时间'))
  } else {
    callback()
  }
}

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const disabledHours = () => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const borrowDate = borrowForm.borrowDate ? new Date(borrowForm.borrowDate) : null
  
  if (borrowDate && borrowDate.getFullYear() === today.getFullYear() && borrowDate.getMonth() === today.getMonth() && borrowDate.getDate() === today.getDate()) {
    const hours = []
    for (let i = 0; i < now.getHours(); i++) {
      hours.push(i)
    }
    return hours
  }
  return []
}

const disabledMinutes = (selectedHour) => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const borrowDate = borrowForm.borrowDate ? new Date(borrowForm.borrowDate) : null
  
  if (borrowDate && borrowDate.getFullYear() === today.getFullYear() && borrowDate.getMonth() === today.getMonth() && borrowDate.getDate() === today.getDate() && selectedHour === now.getHours()) {
    const minutes = []
    for (let i = 0; i < now.getMinutes(); i++) {
      minutes.push(i)
    }
    return minutes
  }
  return []
}

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  borrowDate: [
    { required: true, validator: validateBorrowDate, trigger: 'change' }
  ],
  returnDate: [
    { required: true, validator: validateReturnDate, trigger: 'change' }
  ]
}

const fetchDevices = async () => {
  try {
    if (borrowForm.borrowDate && borrowForm.returnDate) {
      const url = `/api/devices/available?borrowTime=${encodeURIComponent(borrowForm.borrowDate)}&returnTime=${encodeURIComponent(borrowForm.returnDate)}`
      const allAvailableRes = await fetch(url)
      const allAvailableData = await allAvailableRes.json()
      if (allAvailableData.success) {
        const allAvailable = allAvailableData.data
        const isCamera = (type) => type && (type.includes('相机') || type.toLowerCase().includes('camara'))
        const isLens = (type) => type && (type.includes('镜头') || type.toLowerCase().includes('lens'))
        cameraList.value = allAvailable.filter(d => isCamera(d.type))
        lensList.value = allAvailable.filter(d => isLens(d.type))
        otherList.value = allAvailable.filter(d => !isCamera(d.type) && !isLens(d.type))
      }
    } else {
      const response = await fetch('/api/devices')
      const data = await response.json()
      if (data.success) {
        const allDevices = data.data
        const isCamera = (type) => type && (type.includes('相机') || type.toLowerCase().includes('camara'))
        const isLens = (type) => type && (type.includes('镜头') || type.toLowerCase().includes('lens'))
        cameraList.value = allDevices.filter(d => isCamera(d.type) && (d.status === 0 || d.status === 1))
        lensList.value = allDevices.filter(d => isLens(d.type) && (d.status === 0 || d.status === 1))
        otherList.value = allDevices.filter(d => !isCamera(d.type) && !isLens(d.type) && (d.status === 0 || d.status === 1))
      }
    }
    
    const cameraSelected = cameraList.value.find(d => d.id === borrowForm.camera)
    if (!cameraSelected) borrowForm.camera = null
    
    const lensSelected = lensList.value.find(d => d.id === borrowForm.lens)
    if (!lensSelected) borrowForm.lens = null
    
    const otherSelected = otherList.value.find(d => d.id === borrowForm.other)
    if (!otherSelected) borrowForm.other = null
    
  } catch (error) {
    console.error('获取设备列表失败:', error)
  }
}

const handleSubmit = async () => {
  if (!borrowFormRef.value) return
  
  await borrowFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    
    try {
      const response = await fetch('/api/rent-records', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          name: borrowForm.name,
          num: borrowForm.studentId,
          tel: borrowForm.phone,
          camara: borrowForm.camera,
          lens: borrowForm.lens,
          other: borrowForm.other,
          brwtime: borrowForm.borrowDate,
          rtuntime: borrowForm.returnDate,
          remark: borrowForm.remark
        })
      })
      
      const data = await response.json()
      
      if (data.success) {
        ElMessage.success('提交成功！')
        handleReset()
      } else {
        ElMessage.error(data.message || '提交失败')
      }
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error('提交失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

const handleReset = () => {
  if (borrowFormRef.value) {
    borrowFormRef.value.resetFields()
  }
  borrowForm.remark = ''
}

watch([() => borrowForm.borrowDate, () => borrowForm.returnDate], ([newBorrow, newReturn]) => {
  if (newBorrow) {
    borrowFormRef.value.validateField('borrowDate')
  }
  if (borrowForm.returnDate) {
    borrowFormRef.value.validateField('returnDate')
  }
  if (newBorrow && newReturn) {
    fetchDevices()
  }
})

onMounted(() => {
  fetchDevices()
  fetchAnnounce()
})

const fetchAnnounce = async () => {
  try {
    const response = await fetch('/api/announce')
    const data = await response.json()
    if (data.success && data.data) {
      announceContent.value = data.data
      announceVisible.value = true
    }
  } catch (error) {
    console.error('获取公告失败:', error)
  }
}

const closeAnnounce = () => {
  announceVisible.value = false
}
</script>

<style scoped>
.borrow-application {
  width: 100%;
  min-height: 100%;
  background-color: #fff;
  padding: 30px;
}

.announce-content {
  min-height: 200px;
}

.announce-content :deep(.v-show-content) {
  padding: 10px;
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

.borrow-form {
  max-width: 1200px;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

:deep(.el-divider__text) {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media screen and (max-width: 768px) {
  .borrow-application {
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
  
  .button-group {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
