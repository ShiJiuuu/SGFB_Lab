<template>
  <el-container class="app-container">
    <el-aside width="200px" class="desktop-aside">
      <div class="logo">
        <img src="../SGFBLogo.png" alt="Logo" class="logo-img" />
        <div class="logo-text">影视器材预约系统</div>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        router
      >
        <el-menu-item index="/borrow-application">
          <el-icon><Document /></el-icon>
          <span>预约申请</span>
        </el-menu-item>
        <el-menu-item index="/borrow-gantt">
          <el-icon><DataLine /></el-icon>
          <span>预约甘特图</span>
        </el-menu-item>
        <el-menu-item index="/rent-details">
          <el-icon><List /></el-icon>
          <span>预约单详情</span>
        </el-menu-item>
        <el-menu-item index="/equipment-management">
          <el-icon><Tools /></el-icon>
          <span>设备管理</span>
        </el-menu-item>
        <el-menu-item index="/time-slot-management">
          <el-icon><Clock /></el-icon>
          <span>预约时间管理</span>
        </el-menu-item>
      </el-menu>
      <div class="footer">
        <span>Developed By Kirbyuuu</span>
      </div>
      <div class="logout-btn">
        <el-button text @click="handleLogout">登出</el-button>
      </div>
    </el-aside>
    
    <el-container class="main-container">
      <el-header class="mobile-header">
        <el-button text @click="drawerVisible = true">
          <el-icon><Menu /></el-icon>
        </el-button>
        <div class="mobile-title">影视器材预约系统</div>
        <div></div>
      </el-header>
      
      <el-main>
        <router-view />
      </el-main>
    </el-container>
    
    <el-drawer
      v-model="drawerVisible"
      direction="ltr"
      size="240px"
      class="mobile-drawer"
    >
      <div class="drawer-logo">
        <img src="../SGFBLogo.png" alt="Logo" class="logo-img" />
        <div class="logo-text">影视器材预约系统</div>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        router
        @select="handleMenuSelect"
      >
        <el-menu-item index="/borrow-application">
          <el-icon><Document /></el-icon>
          <span>预约申请</span>
        </el-menu-item>
        <el-menu-item index="/borrow-gantt">
          <el-icon><DataLine /></el-icon>
          <span>预约甘特图</span>
        </el-menu-item>
        <el-menu-item index="/rent-details">
          <el-icon><List /></el-icon>
          <span>预约单详情</span>
        </el-menu-item>
        <el-menu-item index="/equipment-management">
          <el-icon><Tools /></el-icon>
          <span>设备管理</span>
        </el-menu-item>
        <el-menu-item index="/time-slot-management">
          <el-icon><Clock /></el-icon>
          <span>预约时间管理</span>
        </el-menu-item>
      </el-menu>
      <div class="footer mobile-footer">
        <span>Developed By Kirbyuuu</span>
      </div>
      <div class="logout-btn mobile-logout">
        <el-button text @click="handleLogout">登出</el-button>
      </div>
    </el-drawer>
  </el-container>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Menu, Document, DataLine, Tools, List, Clock } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const activeMenu = ref(route.path)
const drawerVisible = ref(false)

watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
})

const handleMenuSelect = () => {
  drawerVisible.value = false
}

const handleLogout = () => {
  localStorage.removeItem('token')
  ElMessage.success('登出成功')
  router.push('/login')
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.app-container {
  height: 100vh;
}

.desktop-aside {
  position: relative;
  background-color: #304156;
  color: #fff;
}

.logo {
  height: 140px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4a;
  padding: 5px;
}

.logo-text {
  font-size: 14px;
  font-weight: bold;
  color: #fff;
  margin-top: 5px;
}

.logo-img {
  height: 100px;
  width: 100%;
  object-fit: contain;
}

.logo-text {
  font-size: 16px;
  font-weight: bold;
  color: #fff;
  margin-top: 5px;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: visible;
}

.drawer-logo {
  height: 140px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4a;
  padding: 5px;
}

.el-menu-vertical {
  border-right: none;
  background-color: #304156;
}

.el-menu-item {
  color: #bfcbd9;
}

.el-menu-item:hover,
.el-menu-item.is-active {
  background-color: #263445 !important;
  color: #409eff !important;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

.mobile-header {
  display: none;
  align-items: center;
  justify-content: space-between;
  background-color: #304156;
  color: #fff;
  padding: 0 15px;
  height: 60px;
}

.mobile-header .el-button {
  color: #fff;
  font-size: 24px;
}

.mobile-title {
  font-size: 18px;
  font-weight: bold;
}

.mobile-drawer .el-drawer__header {
  display: none;
}

.mobile-drawer .el-drawer__body {
  padding: 0;
  background-color: #304156;
}

.footer {
  position: absolute;
  bottom: 50px;
  left: 0;
  right: 0;
  text-align: center;
  padding: 10px;
  color: #909399;
  font-size: 12px;
}

.mobile-footer {
  position: static;
  margin-top: 20px;
}

.logout-btn {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px;
  text-align: center;
}

.logout-btn .el-button {
  color: #bfcbd9;
  width: 100%;
}

.logout-btn .el-button:hover {
  color: #fff;
}

.mobile-logout {
  position: static;
}

@media screen and (max-width: 768px) {
  .desktop-aside {
    display: none;
  }
  
  .mobile-header {
    display: flex;
  }
}
</style>
