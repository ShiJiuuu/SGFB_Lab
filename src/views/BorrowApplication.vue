<template>
  <div class="borrow-application">
    <div class="page-header">
      <el-icon class="header-icon"><Camera /></el-icon>
      <span>预约申请</span>
      <el-button type="primary" size="default" @click="openAnnounce" class="announce-btn">
  <el-icon><Bell /></el-icon>
  公告
</el-button>
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

    <div v-if="successModalVisible" class="success-modal-overlay" @click="closeSuccessModal">
      <div class="success-modal-content" @click.stop>
        <div class="success-icon">
          <el-icon :size="80" color="#67c23a">
            <CircleCheck />
          </el-icon>
        </div>
        <h2 class="success-title">设备预约成功</h2>
        <p class="success-text">您的预约申请已成功提交，请按时领取设备</p>
      </div>
    </div>
    
    <el-form
      ref="borrowFormRef"
      :model="borrowForm"
      :rules="rules"
      label-width="80px"
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
          <el-form-item prop="borrowDate">
            <TimeSlotPicker
              v-model="borrowForm.borrowDate"
              label="预约时间"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :span="12">
          <el-form-item prop="returnDate">
            <TimeSlotPicker
              v-model="borrowForm.returnDate"
              label="归还时间"
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
import { reactive, ref, onMounted, computed, watch } from 'vue'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'
import { Camera, User, Tickets, Phone, Setting, Calendar, Check, RefreshRight, CircleCheck, Bell } from '@element-plus/icons-vue'
import TimeSlotPicker from '../components/TimeSlotPicker.vue'

const borrowFormRef = ref()
const loading = ref(false)
const cameraList = ref([])
const lensList = ref([])
const otherList = ref([])
const announceVisible = ref(false)
const announceContent = ref('')
const successModalVisible = ref(false)
let successModalTimer = null

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
    callback()
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
    { required: true, validator: validateBorrowDate, trigger: 'blur' }
  ],
  returnDate: [
    { required: true, validator: validateReturnDate, trigger: 'blur' }
  ]
}

const fetchDevices = async () => {
  fetch('http://127.0.0.1:7869/ingest/3985586e-a422-44df-a66f-e33b149f209b',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'479e7b'},body:JSON.stringify({sessionId:'479e7b',id:'log_fetchdev_'+Date.now(),timestamp:Date.now(),location:'BorrowApplication.vue:249',message:'fetchDevices called',data:{borrowDate:borrowForm.borrowDate,returnDate:borrowForm.returnDate,selectedCamera:borrowForm.camera,selectedLens:borrowForm.lens,selectedOther:borrowForm.other,cameraListLen:cameraList.value.length},runId:'initial',hypothesisId:'E'})}).catch(()=>{});
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
    
    if (!borrowForm.camera && !borrowForm.lens && !borrowForm.other) {
      ElMessage.warning('请至少选择一个设备')
      return
    }
    
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
        showSuccessModal()
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

const showSuccessModal = () => {
  successModalVisible.value = true
  if (successModalTimer) {
    clearTimeout(successModalTimer)
  }
  successModalTimer = setTimeout(() => {
    closeSuccessModal()
  }, 3000)
}

const closeSuccessModal = () => {
  successModalVisible.value = false
  if (successModalTimer) {
    clearTimeout(successModalTimer)
    successModalTimer = null
  }
}

watch(() => borrowForm.borrowDate, () => {
  if (borrowForm.borrowDate && borrowForm.returnDate) {
    fetchDevices()
  }
})

watch(() => borrowForm.returnDate, () => {
  if (borrowForm.borrowDate && borrowForm.returnDate) {
    fetchDevices()
  }
})

onMounted(() => {
  fetch('http://127.0.0.1:7869/ingest/3985586e-a422-44df-a66f-e33b149f209b',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'479e7b'},body:JSON.stringify({sessionId:'479e7b',id:'log_onmount_'+Date.now(),timestamp:Date.now(),location:'BorrowApplication.vue:364',message:'onMounted fired',data:{path:location.pathname},runId:'initial',hypothesisId:'E'})}).catch(()=>{});
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

const openAnnounce = async () => {
  try {
    const response = await fetch('/api/announce')
    const data = await response.json()
    if (data.success && data.data) {
      announceContent.value = data.data
      announceVisible.value = true
    } else {
      ElMessage.info('暂无公告')
    }
  } catch (error) {
    console.error('获取公告失败:', error)
    ElMessage.error('获取公告失败')
  }
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

.announce-btn {
  margin-left: auto;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 3px 6px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 500;
}

.announce-btn:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
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

.success-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.success-modal-content {
  background-color: #fff;
  border-radius: 12px;
  padding: 60px 80px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: scaleIn 0.3s ease;
}

@keyframes scaleIn {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.success-icon {
  margin-bottom: 20px;
  animation: iconBounce 0.5s ease;
}

@keyframes iconBounce {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
}

.success-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 15px 0;
}

.success-text {
  font-size: 16px;
  color: #606266;
  margin: 0;
  line-height: 1.6;
}

:deep(.el-divider__text) {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media screen and (max-width: 1600px) {
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

  :deep(.el-form-item__label) {
    width: 80px !important;
  }

  :deep(.el-form-item__content) {
    margin-left: 60px !important;
  }

  :deep(.el-select) {
    width: 100% !important;
  }

  .success-modal-content {
    padding: 40px 30px;
    margin: 20px;
  }

  .success-title {
    font-size: 24px;
  }

  .success-text {
    font-size: 14px;
  }
}
</style>
