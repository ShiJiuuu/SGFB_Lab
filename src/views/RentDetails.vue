<template>
  <div class="rent-details-page">
    <div class="page-header">
      <el-icon class="header-icon"><Document /></el-icon>
      <span>预约单详情</span>
    </div>

    <el-dialog v-model="exportDialogVisible" title="导出Excel" width="400px">
      <el-form label-width="80px">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="exportDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmExport">导出</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="修改订单" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="editForm.num" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.tel" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="相机">
          <el-select
            v-model="editForm.camara"
            placeholder="请选择相机"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="device in camaraList"
              :key="device.id"
              :label="device.name + ' (' + device.brand + ')'"
              :value="device.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="镜头">
          <el-select
            v-model="editForm.lens"
            placeholder="请选择镜头"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="device in lensList"
              :key="device.id"
              :label="device.name + ' (' + device.brand + ')'"
              :value="device.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="其他设备">
          <el-select
            v-model="editForm.other"
            placeholder="请选择其他设备"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="device in otherDeviceList"
              :key="device.id"
              :label="device.name + ' (' + device.brand + ')'"
              :value="device.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预约时间">
          <el-date-picker
            v-model="editForm.brwtime"
            type="datetime"
            placeholder="选择预约时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
          />
        </el-form-item>
        <el-form-item label="归还时间">
          <el-date-picker
            v-model="editForm.rtuntime"
            type="datetime"
            placeholder="选择归还时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="editForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="已预约" :value="0">
              <span style="color: #e6a23c">已预约</span>
            </el-option>
            <el-option label="已借出" :value="3">
              <span style="color: #409eff">已借出</span>
            </el-option>
            <el-option label="已归还" :value="1">
              <span style="color: #67c23a">已归还</span>
            </el-option>
            <el-option label="逾期未还" :value="2">
              <span style="color: #f56c6c">逾期未还</span>
            </el-option>
            <el-option label="预约未取" :value="4">
              <span style="color: #909399">预约未取</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit">确定</el-button>
      </template>
    </el-dialog>
    
    <div class="search-bar">
      <el-input
        v-model="searchText"
        placeholder="搜索姓名、学号或手机号"
        clearable
        style="width: 300px; margin-right: 20px"
        @clear="fetchData"
        @keyup.enter="fetchData"
      />
      <el-select
        v-model="statusFilter"
        placeholder="订单状态"
        clearable
        style="width: 150px; margin-right: 20px"
        @change="fetchData"
        @clear="fetchData"
      >
        <el-option label="全部" value="" />
        <el-option label="已预约" value="reserved" />
        <el-option label="已借出" value="borrowed" />
        <el-option label="已归还" value="returned" />
        <el-option label="逾期未还" value="overdue" />
        <el-option label="预约未取" value="unpicked" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="width: 360px; margin-right: 20px"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        @change="fetchData"
      />
      <el-button type="primary" @click="fetchData">
        <el-icon><Search /></el-icon>
        查询
      </el-button>
      <el-button @click="handleReset">
        <el-icon><RefreshRight /></el-icon>
        重置
      </el-button>
      <el-button 
        :type="returnMode ? 'warning' : 'default'" 
        @click="toggleReturnMode"
        style="margin-left: 20px"
      >
        <el-icon><Wallet /></el-icon>
        归还模式
      </el-button>
      <el-button type="success" @click="handleExport" style="margin-left: 20px">
        <el-icon><Download /></el-icon>
        导出至Excel
      </el-button>
    </div>
    
    <div class="table-container">
      <el-table
        :data="filteredRecords"
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column
          prop="id"
          label="订单号"
          width="180"
        />
        <el-table-column
          prop="name"
          label="姓名"
          width="120"
        />
        <el-table-column
          prop="num"
          label="学号"
          width="120"
        />
        <el-table-column
          prop="tel"
          label="手机号"
          width="130"
        />
        <el-table-column
          label="设备"
          min-width="200"
        >
          <template #default="{ row }">
            <div class="device-list">
              <div v-if="row.camara?.name" class="device-item">
                <el-tag size="small" type="primary">相机</el-tag>
                <span class="device-name">{{ row.camara.name }}</span>
              </div>
              <div v-if="row.lens?.name" class="device-item">
                <el-tag size="small" type="success">镜头</el-tag>
                <span class="device-name">{{ row.lens.name }}</span>
              </div>
              <div v-if="row.other?.name" class="device-item">
                <el-tag size="small" type="warning">其他</el-tag>
                <span class="device-name">{{ row.other.name }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="brwtime"
          label="预约时间"
          width="170"
        >
          <template #default="{ row }">
            {{ formatTime(row.brwtime) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="rtuntime"
          label="归还时间"
          width="170"
        >
          <template #default="{ row }">
            {{ formatTime(row.rtuntime) }}
          </template>
        </el-table-column>
        <el-table-column
          label="状态"
          width="120"
        >
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)" size="small">
              {{ getStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="remark"
          label="备注"
          show-overflow-tooltip
        />
        <el-table-column
          label="状态更改"
          width="160"
        >
          <template #default="{ row }">
            <el-select
              v-model="row.editingStatus"
              placeholder="选择状态"
              size="small"
              style="width: 100%"
              @change="handleStatusChange(row)"
            >
              <el-option label="已预约" :value="0">
                <span style="color: #e6a23c">已预约</span>
              </el-option>
              <el-option label="已借出" :value="3">
                <span style="color: #409eff">已借出</span>
              </el-option>
              <el-option label="已归还" :value="1">
                <span style="color: #67c23a">已归还</span>
              </el-option>
              <el-option label="逾期未还" :value="2">
                <span style="color: #f56c6c">逾期未还</span>
              </el-option>
              <el-option label="预约未取" :value="4">
                <span style="color: #909399">预约未取</span>
              </el-option>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="170"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Search, RefreshRight, Download, Wallet } from '@element-plus/icons-vue'

const loading = ref(false)
const searchText = ref('')
const statusFilter = ref('')
const returnMode = ref(false)
const exportDialogVisible = ref(false)
const exportDateRange = ref(null)
const editDialogVisible = ref(false)
const editForm = ref({
  id: '',
  name: '',
  num: '',
  tel: '',
  camara: null,
  lens: null,
  other: null,
  brwtime: '',
  rtuntime: '',
  status: 0,
  remark: ''
})

const camaraList = ref([])
const lensList = ref([])
const otherDeviceList = ref([])

const getToday = () => {
  const today = new Date()
  const year = today.getFullYear()
  const month = (today.getMonth() + 1).toString().padStart(2, '0')
  const day = today.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getNextMonth = () => {
  const today = new Date()
  const nextMonth = new Date(today)
  nextMonth.setMonth(nextMonth.getMonth() + 1)
  const year = nextMonth.getFullYear()
  const month = (nextMonth.getMonth() + 1).toString().padStart(2, '0')
  const day = nextMonth.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

const today = getToday()
const dateRange = ref([today, getNextMonth()])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rentRecords = ref([])

const fetchDevices = async () => {
  try {
    const brwtime = editForm.value.brwtime
    const rtuntime = editForm.value.rtuntime

    const formatDateParam = (dateStr) => {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    }

    const formattedBrwtime = formatDateParam(brwtime)
    const formattedRtuntime = formatDateParam(rtuntime)

    if (formattedBrwtime && formattedRtuntime) {
      const url = `/api/devices/available?borrowTime=${encodeURIComponent(formattedBrwtime)}&returnTime=${encodeURIComponent(formattedRtuntime)}`
      const response = await fetch(url)
      const data = await response.json()
      if (data.success) {
        const allAvailable = data.data
        const isCamera = (type) => type && (type.includes('相机') || type.toLowerCase().includes('camara'))
        const isLens = (type) => type && (type.includes('镜头') || type.toLowerCase().includes('lens'))
        camaraList.value = allAvailable.filter(d => isCamera(d.type))
        lensList.value = allAvailable.filter(d => isLens(d.type))
        otherDeviceList.value = allAvailable.filter(d => !isCamera(d.type) && !isLens(d.type))

        if (editForm.value.camara) {
          const camaraSelected = camaraList.value.find(d => d.id === editForm.value.camara)
          if (!camaraSelected) {
            const allResponse = await fetch('/api/devices')
            const allData = await allResponse.json()
            if (allData.success) {
              const device = allData.data.find(d => d.id === editForm.value.camara)
              if (device && isCamera(device.type)) {
                camaraList.value.push(device)
              }
            }
          }
        }
        if (editForm.value.lens) {
          const lensSelected = lensList.value.find(d => d.id === editForm.value.lens)
          if (!lensSelected) {
            const allResponse = await fetch('/api/devices')
            const allData = await allResponse.json()
            if (allData.success) {
              const device = allData.data.find(d => d.id === editForm.value.lens)
              if (device && isLens(device.type)) {
                lensList.value.push(device)
              }
            }
          }
        }
        if (editForm.value.other) {
          const otherSelected = otherDeviceList.value.find(d => d.id === editForm.value.other)
          if (!otherSelected) {
            const allResponse = await fetch('/api/devices')
            const allData = await allResponse.json()
            if (allData.success) {
              const device = allData.data.find(d => d.id === editForm.value.other)
              if (device && !isCamera(device.type) && !isLens(device.type)) {
                otherDeviceList.value.push(device)
              }
            }
          }
        }
      }
    } else {
      const response = await fetch('/api/devices')
      const data = await response.json()
      if (data.success) {
        const allDevices = data.data
        const isCamera = (type) => type && (type.includes('相机') || type.toLowerCase().includes('camara'))
        const isLens = (type) => type && (type.includes('镜头') || type.toLowerCase().includes('lens'))
        camaraList.value = allDevices.filter(d => isCamera(d.type))
        lensList.value = allDevices.filter(d => isLens(d.type))
        otherDeviceList.value = allDevices.filter(d => !isCamera(d.type) && !isLens(d.type))
      }
    }
  } catch (error) {
    console.error('获取设备列表失败:', error)
  }
}

const filteredRecords = computed(() => {
  let result = rentRecords.value
  
  if (searchText.value) {
    const text = searchText.value.toLowerCase()
    result = result.filter(record => 
      record.name?.toLowerCase().includes(text) ||
      record.num?.toLowerCase().includes(text) ||
      record.tel?.toLowerCase().includes(text)
    )
  }
  
  if (statusFilter.value && !statusFilter.value.includes(',')) {
    result = result.filter(record => getStatusClass(record) === statusFilter.value)
  }
  
  return result
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const getStatusClass = (rental) => {
  const status = rental.status
  if (status === 0) return 'reserved'   // 已预约
  if (status === 1) return 'returned'   // 已归还
  if (status === 2) return 'overdue'    // 逾期未还
  if (status === 3) return 'borrowed'   // 已借出
  if (status === 4) return 'unpicked'   // 预约未取
  return 'reserved'
}

const getStatusType = (rental) => {
  const status = getStatusClass(rental)
  if (status === 'reserved') return 'warning'   // 已预约 - 黄色
  if (status === 'borrowed') return 'primary'   // 已借出 - 蓝色
  if (status === 'overdue') return 'danger'     // 逾期未还 - 红色
  if (status === 'unpicked') return 'info'      // 预约未取 - 灰色
  return 'success'                              // 已归还 - 绿色
}

const getStatusText = (rental) => {
  const status = getStatusClass(rental)
  if (status === 'reserved') return '已预约'
  if (status === 'borrowed') return '已借出'
  if (status === 'overdue') return '逾期未还'
  if (status === 'unpicked') return '预约未取'
  return '已归还'
}

const fetchData = async () => {
  loading.value = true
  try {
    let url = `/api/rent-records?page=${currentPage.value}&pageSize=${pageSize.value}`
    
    if (dateRange.value && dateRange.value.length === 2) {
      url += `&startDate=${dateRange.value[0]}&endDate=${dateRange.value[1]}`
    }
    
    if (statusFilter.value) {
      url += `&status=${encodeURIComponent(statusFilter.value)}`
    }
    
    const res = await fetch(url)
    const data = await res.json()
    
    if (data.success) {
      rentRecords.value = data.data.map(record => ({
        ...record,
        editingStatus: record.status
      }))
      total.value = data.total
    } else {
      ElMessage.error('获取数据失败')
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleStatusChange = async (row) => {
  try {
    const response = await fetch(`/api/rent-records/${row.id}/status`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        status: row.editingStatus
      })
    })
    
    const data = await response.json()
    
    if (data.success) {
      ElMessage.success(data.message || '状态更新成功')
      await fetchData()
    } else {
      ElMessage.error(data.message || '状态更新失败')
      row.editingStatus = row.status
    }
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败，请稍后重试')
    row.editingStatus = row.status
  }
}

const handleEdit = (row) => {
  editForm.value = {
    id: row.id,
    name: row.name || '',
    num: row.num || '',
    tel: row.tel || '',
    camara: row.camara?.id || null,
    lens: row.lens?.id || null,
    other: row.other?.id || null,
    brwtime: row.brwtime || '',
    rtuntime: row.rtuntime || '',
    status: row.status,
    remark: row.remark || ''
  }
  editDialogVisible.value = true
}

const formatDateTime = (dateValue) => {
  if (!dateValue) return null
  const date = new Date(dateValue)
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const confirmEdit = async () => {
  try {
    const response = await fetch(`/api/rent-records/${editForm.value.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        name: editForm.value.name,
        num: editForm.value.num,
        tel: editForm.value.tel,
        camara: editForm.value.camara,
        lens: editForm.value.lens,
        other: editForm.value.other,
        brwtime: formatDateTime(editForm.value.brwtime),
        rtuntime: formatDateTime(editForm.value.rtuntime),
        status: editForm.value.status,
        remark: editForm.value.remark
      })
    })
    
    const data = await response.json()
    
    if (data.success) {
      ElMessage.success(data.message || '订单更新成功')
      editDialogVisible.value = false
      await fetchData()
    } else {
      ElMessage.error(data.message || '订单更新失败')
    }
  } catch (error) {
    console.error('订单更新失败:', error)
    ElMessage.error('订单更新失败，请稍后重试')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除订单 ${row.id} 吗？该操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await fetch(`/api/rent-records/${row.id}`, {
      method: 'DELETE'
    })
    const data = await response.json()

    if (data.success) {
      ElMessage.success(data.message || '订单删除成功')
      await fetchData()
    } else {
      ElMessage.error(data.message || '订单删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('订单删除失败:', error)
      ElMessage.error('订单删除失败，请稍后重试')
    }
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchData()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  const today = new Date()
  const nextMonth = new Date(today)
  nextMonth.setMonth(nextMonth.getMonth() + 1)
  
  const formatDate = (date) => {
    const year = date.getFullYear()
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    return `${year}-${month}-${day}`
  }
  
  searchText.value = ''
  statusFilter.value = ''
  returnMode.value = false
  dateRange.value = [formatDate(today), formatDate(nextMonth)]
  currentPage.value = 1
  pageSize.value = 10
  fetchData()
}

const toggleReturnMode = () => {
  returnMode.value = !returnMode.value
  
  if (returnMode.value) {
    statusFilter.value = 'borrowed,overdue'
    dateRange.value = null
  } else {
    statusFilter.value = ''
    const today = new Date()
    const nextMonth = new Date(today)
    nextMonth.setMonth(nextMonth.getMonth() + 1)
    const formatDate = (date) => {
      const year = date.getFullYear()
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      return `${year}-${month}-${day}`
    }
    dateRange.value = [formatDate(today), formatDate(nextMonth)]
  }
  
  currentPage.value = 1
  fetchData()
}

const handleExport = () => {
  exportDateRange.value = null
  exportDialogVisible.value = true
}

const confirmExport = async () => {
  let url = '/api/rent-records/export'
  const params = []
  
  if (exportDateRange.value && exportDateRange.value.length === 2) {
    params.push(`startDate=${exportDateRange.value[0]}`)
    params.push(`endDate=${exportDateRange.value[1]}`)
  }
  
  if (params.length > 0) {
    url += '?' + params.join('&')
  }
  
  try {
    const response = await fetch(url)
    if (!response.ok) {
      throw new Error('导出失败')
    }
    
    const blob = await response.blob()
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    
    const contentDisposition = response.headers.get('Content-Disposition')
    let fileName = '预约记录.xlsx'
    if (contentDisposition) {
      const fileNameMatch = contentDisposition.match(/filename[*]?=.*?['"]?([^'"\r\n]+)['"]?/)
      if (fileNameMatch && fileNameMatch.length > 1) {
        let encodedFileName = fileNameMatch[1]
        try {
          fileName = decodeURIComponent(encodedFileName)
        } catch (e) {
          fileName = encodedFileName
        }
      }
    }
    
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)
    
    exportDialogVisible.value = false
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  fetchData()
  fetchDevices()
})

watch([() => editForm.value.brwtime, () => editForm.value.rtuntime], () => {
  if (editDialogVisible.value) {
    fetchDevices()
  }
})
</script>

<style scoped>
.rent-details-page {
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

.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.table-container {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.pagination-container {
  padding: 20px 0;
  display: flex;
  justify-content: flex-end;
}

.device-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.device-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.device-name {
  font-size: 13px;
  color: #606266;
}

@media screen and (max-width: 768px) {
  .rent-details-page {
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
  
  .search-bar {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .search-bar .el-input,
  .search-bar .el-select {
    width: 100% !important;
    margin-right: 0 !important;
  }
}
</style>
