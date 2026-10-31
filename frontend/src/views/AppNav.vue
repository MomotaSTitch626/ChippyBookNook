<template>
  <header class="topbar">
    <div class="nav-left">
      <span class="brand" @click="goHome">ChippyBookNook</span>
      <span class="nav-item" :class="{ active: $route.path === '/' }" @click="goHome">首页</span>
      <span class="nav-item" :class="{ active: $route.path === '/library' }" @click="goLibrary">书库</span>
      <span class="nav-item" :class="{ active: $route.path === '/my' }" @click="goMyBooks">书屋</span>
    </div>
    <div class="top-right">
      <input v-model="keyword" @keyup.enter="doSearch" placeholder="搜索书名 / 作者" class="mini-search" />
      <div class="user-box" @click.stop="menuOpen = !menuOpen">
        <img v-if="userInfo && userInfo.avatar" :src="'/uploads/' + userInfo.avatar" class="user-avatar" alt="头像" />
        <span v-else class="user-avatar placeholder">{{ userInfo && userInfo.nickName ? userInfo.nickName.charAt(0) : '我' }}</span>
        <div v-if="menuOpen" class="user-menu">
          <div class="menu-item" :class="{ active: $route.path === '/profile' }" @click="goProfile">个人设置</div>
          <div class="menu-item" @click="logout">退出登录</div>
        </div>
      </div>
    </div>
  </header>
</template>

<script>
import request from "../api/request";

export default {
  data() {
    return {
      keyword: '',        // 顶部搜索框文字
      menuOpen: false,    // 下拉菜单开/关
      userInfo: null      // 当前登录用户（头像/昵称显示）
    }
  },
  created() {
    this.loadUser()       // 一挂载就拉用户资料，头像才有得显示
  },
  methods: {
    // 拉当前登录用户
    async loadUser() {
      const res = await request.get('/user/me')
      if (res.code === 200) {
        this.userInfo = res.data
      }
    },
    goHome() { this.$router.push('/') },
    goLibrary() { this.$router.push('/library') },
    goMyBooks() { this.$router.push('/my') },
    goProfile() {
      this.menuOpen = false
      this.$router.push('/profile')
    },
    logout() {
      localStorage.removeItem('token')
      this.$router.push('/login')
    },
    // 顶部搜索：回车跳书库带关键词（书库页会读 query 去搜）
    doSearch() {
      this.$router.push({ path: '/library', query: { keyword: this.keyword } })
    },
    // 点击页面空白处收菜单
    closeMenu() {
      this.menuOpen = false
    }
  },
  mounted() {
    document.addEventListener('click', this.closeMenu)
  },
  beforeDestroy() {
    document.removeEventListener('click', this.closeMenu)
  }
}
</script>

<style scoped>
.topbar {
  display: flex;                                        /* 横向弹性布局 */
  justify-content: space-between;                       /* 左右两边拉开 */
  align-items: center;                                  /* 垂直居中 */
  padding: 0 24px;                                      /* 左右留白 */
  height: 56px;                                          /* 导航高度 */
  background: #fff;                                      /* 白底 */
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.04);              /* 极淡阴影 */
  position: sticky;                                      /* 吸顶 */
  top: 0;
  z-index: 10;                                           /* 盖住下方内容 */
}
.nav-left {
  display: flex;                                        /* 横向排 */
  align-items: center;
  gap: 24px;                                             /* 品牌和导航间距 */
}
.brand {
  font-size: 20px;                                       /* 品牌字号 */
  font-weight: 700;                                      /* 加粗 */
  color: #02c58d;                                        /* 品牌绿 */
  cursor: pointer;
}
.nav-item {
  font-size: 14px;                                       /* 导航文字 */
  color: #333;
  cursor: pointer;
  padding: 6px 4px;                                      /* 点击区域宽松点 */
}
.nav-item:hover {
  color: #02c58d;                                        /* 悬停变绿 */
}
.nav-item.active {
  color: #02c58d;                                        /* 当前页：绿 */
  font-weight: 600;                                      /* 加粗 */
}
.mini-search {
  width: 220px;                                          /* 搜索框宽 */
  padding: 8px 14px;                                     /* 内边距 */
  border: 1px solid #e0e0e0;                             /* 淡边框 */
  border-radius: 20px;                                   /* 药丸圆角 */
  font-size: 13px;
  outline: none;                                         /* 去默认蓝框 */
  background: #f7f8f9;                                   /* 浅灰底 */
}
.mini-search:focus {
  border-color: #02c58d;                                 /* 聚焦变绿 */
  background: #fff;
}
.top-right {
  display: flex;                                        /* 右侧：搜索框+头像横排 */
  align-items: center;
  gap: 16px;
}
.user-box {
  position: relative;                                   /* 菜单定位基准 */
  cursor: pointer;
}
.user-avatar {
  width: 36px;                                           /* 头像大小 */
  height: 36px;
  border-radius: 50%;                                    /* 圆形 */
  object-fit: cover;                                     /* 图片不拉伸变形 */
}
.user-avatar.placeholder {
  background: #02c58d;                                   /* 无头像时绿底 */
  color: #fff;
  display: flex;                                        /* 让首字居中 */
  align-items: center;
  justify-content: center;
  font-size: 15px;                                       /* 首字字号 */
  font-weight: 600;
}
.user-menu {
  position: absolute;                                   /* 浮层，贴着头像 */
  right: 0;                                              /* 右对齐 */
  top: 44px;                                             /* 头像下方 */
  background: #fff;
  border: 1px solid #f0f0f0;                             /* 极淡边框 */
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);              /* 悬浮阴影 */
  min-width: 120px;
  z-index: 100;
}
.menu-item {
  padding: 10px 16px;                                    /* 菜单项内边距 */
  font-size: 13px;
  color: #333;
}
.menu-item:hover {
  background: #f7f8fa;                                   /* 悬停浅灰底 */
  color: #02c58d;
}
.menu-item.active {
  color: #02c58d;                                        /* 当前页菜单项：绿 */
  font-weight: 600;
}

/* 响应式：手机 */
@media (max-width: 768px) {
  .topbar { padding: 0 14px; }
  .nav-left { gap: 12px; }
  .brand { font-size: 16px; }
  .mini-search { width: 130px; padding: 7px 10px; }
}
</style>
