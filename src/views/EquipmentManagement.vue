<template>
  <div class="equipment-management">
    <div class="page-header">
      <el-icon class="header-icon"><Tools /></el-icon>
      <span>设备管理</span>
      <el-button type="primary" size="small" @click="openAnnounceDialog" v-if="isAuthenticated" style="margin-left: auto;">
        编辑公告
      </el-button>
    </div>

    <el-dialog v-model="announceDialogVisible" title="编辑公告" width="70%">
      <el-alert title="支持 Markdown 语法" type="info" :closable="false" style="margin-bottom: 15px;" />
      <el-input v-model="announceContent" type="textarea" :rows="15" placeholder="请输入公告内容（支持 Markdown 语法）" />
      <template #footer>
        <el-button @click="announceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAnnounce">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="修改设备信息" width="500px">
      <el-form :model="editDevice" label-width="80px">
        <el-form-item label="设备名称">
          <el-input v-model="editDevice.name" />
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="editDevice.brand" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="editDevice.type" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editDevice.status" style="width: 100%">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <span :style="{ color: item.color }">{{ item.label }}</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate">保存</el-button>
      </template>
    </el-dialog>
    
    <el-card>
      <div class="add-device-form" v-if="isAuthenticated">
        <el-divider>添加新设备</el-divider>
        <el-form :model="newDevice" inline>
          <el-form-item label="设备名称">
            <el-input v-model="newDevice.name" placeholder="请输入设备名称" />
          </el-form-item>
          <el-form-item label="品牌">
            <el-input v-model="newDevice.brand" placeholder="请输入品牌" style="width: 180px" />
          </el-form-item>
          <el-form-item label="类型">
            <el-input v-model="newDevice.type" placeholder="请输入类型" style="width: 180px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleAdd">添加</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="deviceList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="设备名称" min-width="150" />
        <el-table-column prop="brand" label="品牌" min-width="200">
          <template #default="scope">
            <el-tag type="warning">{{ scope.row.brand }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="200">
          <template #default="scope">
            <el-tag v-if="scope.row.type === '相机/Camara'" type="primary">相机/Camara</el-tag>
            <el-tag v-else-if="scope.row.type === '镜头/Lens'" type="success">镜头/Lens</el-tag>
            <el-tag v-else-if="scope.row.type.includes('其他') || scope.row.type.toLowerCase().includes('other')" type="warning">其他</el-tag>
            <el-tag v-else>{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="200">
          <template #default="scope">
            <el-select 
              v-model="scope.row.status" 
              @change="handleStatusChange(scope.row)"
              :disabled="!isAuthenticated"
              size="small"
              :style="{ width: '120px' }"
            >
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
                <span :style="{ color: item.color }">{{ item.label }}</span>
              </el-option>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" v-if="isAuthenticated">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">修改</el-button>
            <el-popconfirm
              title="确定删除这个设备吗？"
              @confirm="handleDelete(scope.row)"
            >
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Tools } from '@element-plus/icons-vue'

const deviceList = ref([])
const loading = ref(false)
const announceDialogVisible = ref(false)
const announceContent = ref('')
const editDialogVisible = ref(false)
const editDevice = ref({
  id: null,
  name: '',
  brand: '',
  type: '',
  status: 0
})
const newDevice = ref({
  name: '',
  brand: '',
  type: '',
  status: 0
})

const statusOptions = [
  { value: 0, label: '正常库存', color: '#67c23a' },
  { value: 1, label: '已预约借出', color: '#409eff' },
  { value: 2, label: '逾期未还', color: '#f56c6c' },
  { value: 3, label: '设备故障', color: '#e6a23c' }
]

const isAuthenticated = computed(() => {
  return !!localStorage.getItem('token')
})

const fetchDevices = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/devices')
    const data = await response.json()
    if (data.success) {
      deviceList.value = data.data
    }
  } catch (error) {
    console.error('获取设备列表失败:', error)
    ElMessage.error('获取设备列表失败')
  } finally {
    loading.value = false
  }
}

const handleStatusChange = async (device) => {
  try {
    const response = await fetch(`/api/devices/${device.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ status: device.status })
    })
    const data = await response.json()
    if (data.success) {
      ElMessage.success('状态更新成功')
    } else {
      ElMessage.error(data.message || '状态更新失败')
      fetchDevices()
    }
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
    fetchDevices()
  }
}

const handleAdd = async () => {
  if (!newDevice.value.name || !newDevice.value.brand || !newDevice.value.type) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    const response = await fetch('/api/devices', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(newDevice.value)
    })
    const data = await response.json()
    if (data.success) {
      ElMessage.success('添加成功')
      newDevice.value = { name: '', brand: '', type: '', status: 0 }
      fetchDevices()
    } else {
      ElMessage.error(data.message || '添加失败')
    }
  } catch (error) {
    console.error('添加失败:', error)
    ElMessage.error('添加失败')
  }
}

const handleDelete = async (device) => {
  try {
    const response = await fetch(`/api/devices/${device.id}`, {
      method: 'DELETE'
    })
    const data = await response.json()
    if (data.success) {
      ElMessage.success('删除成功')
      fetchDevices()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

const openEditDialog = (device) => {
  editDevice.value = {
    id: device.id,
    name: device.name,
    brand: device.brand,
    type: device.type,
    status: device.status
  }
  editDialogVisible.value = true
}

const handleUpdate = async () => {
  if (!editDevice.value.name || !editDevice.value.brand || !editDevice.value.type) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    const response = await fetch(`/api/devices/${editDevice.value.id}/info`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(editDevice.value)
    })
    const data = await response.json()
    if (data.success) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      fetchDevices()
    } else {
      ElMessage.error(data.message || '更新失败')
    }
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('更新失败')
  }
}

onMounted(() => {
  fetchDevices()
})

const openAnnounceDialog = async () => {
  try {
    const response = await fetch('/api/announce')
    const data = await response.json()
    if (data.success) {
      announceContent.value = data.data || ''
    }
  } catch (error) {
    console.error('获取公告失败:', error)
    announceContent.value = ''
  }
  announceDialogVisible.value = true
}

const saveAnnounce = async () => {
  try {
    const response = await fetch('/api/announce', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ announce: announceContent.value })
    })
    const data = await response.json()
    if (data.success) {
      ElMessage.success('公告保存成功')
      announceDialogVisible.value = false
    } else {
      ElMessage.error(data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存公告失败:', error)
    ElMessage.error('保存失败')
  }
}
</script>

<style scoped>
.equipment-management {
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

.add-device-form {
  margin-bottom: 20px;
}

@media screen and (max-width: 768px) {
  .equipment-management {
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
}
</style>
