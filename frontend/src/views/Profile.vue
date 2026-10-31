<template>
  <div class="app">
    <!-- 顶部导航：共享组件 -->
    <AppNav />

    <main class="container">
      <!-- 个人设置卡片 -->
      <div class="profile-card">
        <h2 class="card-title">个人设置</h2>

        <!-- 头像区：大圆头像 + 更换按钮 -->
        <div class="avatar-row">
          <img v-if="form.avatar" :src="'/uploads/' + form.avatar" class="big-avatar" alt="头像" />
          <span v-else class="big-avatar placeholder">{{ form.nickName ? form.nickName.charAt(0) : '我' }}</span>
          <div class="avatar-actions">
            <button class="btn-outline" @click="chooseAvatar">更换头像</button>
            <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="uploadAvatar" />
          </div>
        </div>

        <!-- 可编辑表单 -->
        <div class="form-row">
          <label class="form-label">昵称</label>
          <input v-model="form.nickName" class="form-input" placeholder="请输入昵称" />
        </div>
        <div class="form-row">
          <label class="form-label">手机</label>
          <input v-model="form.phone" class="form-input" placeholder="请输入手机号" />
        </div>
        <div class="form-row">
          <label class="form-label">性别</label>
          <select v-model="form.sex" class="form-input">
            <option value="">未设置</option>
            <option value="男">男</option>
            <option value="女">女</option>
          </select>
        </div>
        <div class="form-row">
          <label class="form-label">地址</label>
          <input v-model="form.address" class="form-input" placeholder="请输入地址" />
        </div>

        <!-- 只读信息（username 和 createTime 不能改） -->
        <div class="readonly-row">
          <span>用户名：{{ userInfo ? userInfo.username : '' }}</span>
          <span class="readonly-gap">注册时间：{{ userInfo ? userInfo.createTime : '' }}</span>
        </div>

        <button class="btn-save" @click="saveProfile">保存</button>
      </div>

      <!-- 上传图书入口：跳转上传页 -->
      <div class="upload-card">
        <h2 class="card-title">上传图书</h2>
        <button class="btn-outline" @click="goUpload">去上传</button>
      </div>

      <!-- 更新文件入口（占位） -->
      <div class="update-card">
        <h2 class="card-title">更新文件</h2>
        <button class="btn-outline" @click="updateFileTip">更新文件</button>
      </div>
    </main>
  </div>
</template>

<script>
import request from "../api/request";
import AppNav from './AppNav.vue';

export default {
  components: { AppNav },
  data() {
    return {
      userInfo: null,      // 后端返回的完整用户信息（只读展示用）
      form: {              // 表单数据：可编辑的 5 个字段
        avatar: '',
        nickName: '',
        phone: '',
        sex: '',
        address: ''
      }
    }
  },
  created() {
    this.loadUser()        // 一进来拉当前用户资料
  },
  methods: {
    // 拉当前用户，并把可编辑字段填进表单
    async loadUser() {
      const res = await request.get('/user/me')
      if (res.code === 200) {
        this.userInfo = res.data
        this.form.avatar = res.data.avatar || ''
        this.form.nickName = res.data.nickName || ''
        this.form.phone = res.data.phone || ''
        this.form.sex = res.data.sex || ''
        this.form.address = res.data.address || ''
      } else {
        alert(res.message)
      }
    },
    // 点"更换头像" → 模拟点击隐藏的文件选择框
    chooseAvatar() {
      this.$refs.fileInput.click()
    },
    // 选中图片 → 上传拿路径 → 写进表单（还没存库，点保存才生效）
    async uploadAvatar(e) {
      const file = e.target.files[0]
      if (!file) return
      const fd = new FormData()          // 表单数据容器：能装文件
      fd.append('file', file)            // 字段名 file，和后端 @RequestParam("file") 对应
      const res = await request.post('/user/avatar', fd)
      if (res.code === 200) {
        this.form.avatar = res.data      // 后端返回相对路径 avatars/xxx.png
        alert('头像上传成功，点击保存生效')
      } else {
        alert(res.message)
      }
      e.target.value = ''                // 清空选择记录，下次还能选同一张
    },
    // 保存：把表单 5 个字段发给后端白名单更新
    async saveProfile() {
      const res = await request.put('/user/me', this.form)
      if (res.code === 200) {
        alert('保存成功')
        this.loadUser()                  // 重新拉最新资料（含新头像路径）
      } else {
        alert(res.message)
      }
    },
    // 跳转到上传图书页（路由 /upload，Upload.vue 是完整的上传表单）
    goUpload() {
      this.$router.push('/upload')
    },
    // 更新文件：占位
    updateFileTip() {
      alert('更新文件功能开发中，敬请期待')
    }
  }
}
</script>

<style scoped>
.app { min-height: 100vh; background: #f5f6f7; font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif; }

/* 主内容区 */
.container { max-width: 640px; margin: 0 auto; padding: 32px 16px 60px; }

/* 个人设置卡片 */
.profile-card { background: #fff; border-radius: 12px; padding: 28px; }
.card-title { font-size: 18px; font-weight: 700; color: #1a1a1a; margin: 0 0 20px; }

/* 头像区 */
.avatar-row { display: flex; align-items: center; gap: 20px; margin-bottom: 24px; }
.big-avatar { width: 72px; height: 72px; border-radius: 50%; object-fit: cover; }
.big-avatar.placeholder { background: #02c58d; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: 600; }
.btn-outline { padding: 8px 18px; border: 1px solid #02c58d; background: #fff; color: #02c58d; border-radius: 6px; font-size: 13px; cursor: pointer; }
.btn-outline:hover { background: rgba(2, 197, 141, 0.06); }

/* 表单行 */
.form-row { display: flex; align-items: center; margin-bottom: 16px; }
.form-label { width: 70px; font-size: 14px; color: #333; flex-shrink: 0; }
.form-input { flex: 1; padding: 9px 12px; border: 1px solid #e0e0e0; border-radius: 6px; font-size: 14px; outline: none; }
.form-input:focus { border-color: #02c58d; }

/* 只读信息 */
.readonly-row { font-size: 13px; color: #8a8a8a; margin: 8px 0 24px; display: flex; }
.readonly-gap { margin-left: 24px; }

/* 保存按钮 */
.btn-save { width: 100%; padding: 12px 0; background: #02c58d; color: #fff; border: none; border-radius: 6px; font-size: 15px; cursor: pointer; }
.btn-save:hover { background: #01ad7c; }

/* 上传图书入口卡（和更新文件卡同款） */
.upload-card { background: #fff; border-radius: 12px; padding: 28px; margin-top: 20px; }
.upload-tip { font-size: 13px; color: #8a8a8a; margin: 0 0 16px; }

/* 更新文件占位卡 */
.update-card { background: #fff; border-radius: 12px; padding: 28px; margin-top: 20px; }
.update-tip { font-size: 13px; color: #8a8a8a; margin: 0 0 16px; }
</style>
