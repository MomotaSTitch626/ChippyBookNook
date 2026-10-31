<template>
  <div class="app">
    <!-- 顶部导航：共享组件 -->
    <AppNav />

    <main class="container" v-if="book">
      <!-- 返回书库（替代面包屑） -->
      <div class="back-row">
        <span class="back-link" @click="goLibrary">← 返回书库</span>
      </div>

      <!-- 上部：左封面 | 中信息 | 右作者卡 -->
      <div class="detail-top">
        <div class="cover-wrap">
          <img v-if="book.cover" :src="'/uploads/' + book.cover" class="detail-cover" alt="封面" />
          <div v-else class="detail-cover placeholder">{{ (book.name || '书').charAt(0) }}</div>
        </div>
        <div class="info-wrap">
          <h1 class="book-name">{{ book.name }}</h1>
          <div class="tag-row">
            <span class="tag status" :class="book.status === '连载中' ? 'ongoing' : ''">{{ book.status || '未知' }}</span>
            <span class="tag category">{{ book.category || '未分类' }}</span>
          </div>
          <div class="meta-line">
            <span>{{ formatWordCount(book.wordCount) }}</span>
            <span class="dot">·</span>
            <span>出版于 {{ book.publishTime || '未知' }}</span>
          </div>
          <div class="btn-row">
            <button class="btn-read" @click="startRead">开始阅读</button>
            <button class="btn-download" @click="download">下载</button>
            <button v-if="myId && book.ownerId !== myId" class="btn-borrow"
                    :class="{ disabled: lent }" :disabled="lent" @click="borrow">
              {{ lent ? '已被借走' : '借阅' }}
            </button>
          </div>
        </div>
        <div class="author-card">
          <div class="author-name">作者 {{ book.author || '未知作者' }}</div>
          <div class="author-bio">暂无简介</div>
        </div>
      </div>

      <!-- 作品简介 -->
      <section class="section">
        <h2 class="section-title">作品简介</h2>
        <p class="section-body">{{ book.description || '暂无' }}</p>
      </section>

      <!-- 文件信息（替代番茄的目录板块） -->
      <section class="section">
        <h2 class="section-title">文件信息</h2>
        <div class="file-info">
          <div class="file-row"><span class="label">格式</span><span class="value">{{ book.fileFormat || '无' }}</span></div>
          <div class="file-row"><span class="label">大小</span><span class="value">{{ formatSize(book.fileSize) }}</span></div>
          <div class="file-row"><span class="label">出版时间</span><span class="value">{{ book.publishTime || '未知' }}</span></div>
        </div>
      </section>
    </main>
    <main v-else class="container"><div class="empty">加载中…</div></main>
  </div>
</template>

<script>
import request from "../api/request";
import AppNav from './AppNav.vue';

export default {
  components: { AppNav },
  data() {
    return {
      book: null,        // 当前这本书（详情页所有信息都从它取）
      myId: null,        // 当前登录用户 id，从 token 里解出来
      lent: false,       // 这本书是否已被别人借走（check 接口的结果）
    }
  },
  created() {
    this.loadDetail()   // 一进来就按路由里的书 id 查这本书
    this.myId = this.getMyId()
  },
  methods: {
    // 核心：调后端 GET /book/{id} 拿单本书完整信息
    async loadDetail() {
      const id = this.$route.params.id                       // ① 从路由拿 id（/book/:id 里的 :id）
      const res = await request.get('/book/' + id)           // ② 请求详情接口（后端早就写好的）
      if (res.code === 200) {
        this.book = res.data                                 // ③ 成功 → 整个 Book 对象存进 book
        this.checkLent()                                     // 书查到了，再查它是不是被借走
      } else {
        alert(res.message)
      }
    },
    goLibrary() { this.$router.push('/library') },
    // 开始阅读：只有 TXT 能读（和之前拍板一致）
    startRead() {
      if (this.book.fileFormat !== 'TXT') {
        alert('该格式暂不支持在线阅读')
        return
      }
      this.$router.push('/read/' + this.book.id + '?name=' + encodeURIComponent(this.book.name))
    },
    // 从 JWT token 中间那段解出登录用户的 id
    getMyId() {
      const token = localStorage.getItem('token')
      if (!token) return null
      try {
        // token 格式是 "头.载荷.签名"，第 2 段（index 1）是 base64 编码的用户信息
        const payload = JSON.parse(
            atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))
        )
        return payload.userId  // 后端签发 token 时写的字段名
      } catch (e) {
        return null              // 解不出来就返回 null，页面不显示借阅按钮
      }
    },
    // 下载：有文件才给下；用 blob 方式带 token 下载
    async download() {
      if (!this.book.filePath) { alert('这本书没有文件可下载'); return }
      const blob = await request.get('/book/download/' + this.book.id, { responseType: 'blob' })
      const url = URL.createObjectURL(blob)                  // ① 把二进制流做成临时地址
      const a = document.createElement('a')                  // ② 造一个隐藏的 <a> 标签
      a.href = url
      a.download = this.book.name + '.' + (this.book.fileFormat || 'file').toLowerCase()  // ③ 指定下载文件名
      document.body.appendChild(a)
      a.click()                                              // ④ 模拟点击触发下载
      document.body.removeChild(a)
      URL.revokeObjectURL(url)                               // ⑤ 用完释放临时地址
    },
    // 查这本书当前是否被借走（后端 check 接口：true=被借走）
    async checkLent() {
      const res = await request.get('/lend-record/check', { params: { bookId: this.book.id } })
      if (res.code === 200) this.lent = res.data
    },
    // 点击借阅：调后端 borrow 接口
    async borrow() {
      const res = await request.post('/lend-record/borrow', null, { params: { bookId: this.book.id } })
      if (res.code === 200) {
        alert('借阅成功，可在书屋中查看')
        this.lent = true   // 借完立刻把按钮变"已被借走"置灰
      } else {
        alert(res.message) // 后端拒绝的原因（不能借自己的书/已被借走等）
      }
    },
    // 字数格式化：1 万以上显示"x.x 万字"，否则"xx 字"，没有显示"暂无"
    formatWordCount(n) {
      if (!n) return '暂无'
      if (n >= 10000) return (n / 10000).toFixed(1) + ' 万字'
      return n + ' 字'
    },
    // 文件大小格式化：bytes → KB / MB
    formatSize(bytes) {
      if (!bytes) return '未知'
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
      return (bytes / 1024 / 1024).toFixed(2) + ' MB'
    }
  }
}
</script>

<style scoped>
.app { min-height: 100vh; background: #fff; font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif; }

.container { max-width: 1080px; margin: 0 auto; padding: 24px 24px 60px; }

/* 返回书库 */
.back-row { margin-bottom: 18px; }
.back-link { font-size: 13px; color: #8a8a8a; cursor: pointer; }
.back-link:hover { color: #02c58d; }

/* 上部三栏：封面 | 信息 | 作者卡 */
.detail-top { display: flex; gap: 28px; }
.cover-wrap { flex-shrink: 0; }
.detail-cover { width: 220px; aspect-ratio: 3 / 4; object-fit: cover; border-radius: 8px; box-shadow: 0 4px 14px rgba(0,0,0,0.12); }
.detail-cover.placeholder { background: #f0f1f2; display: flex; align-items: center; justify-content: center; font-size: 60px; color: #c8c9cc; font-weight: 600; }
.info-wrap { flex: 1; min-width: 0; padding-top: 6px; }
.book-name { font-size: 28px; font-weight: 700; color: #1a1a1a; margin: 0 0 14px; }
.tag-row { display: flex; gap: 8px; margin-bottom: 14px; }
.tag { font-size: 12px; padding: 3px 10px; border-radius: 10px; display: inline-block; }
.tag.status { background: rgba(2, 197, 141, 0.08); color: #02c58d; }
.tag.status.ongoing { background: rgba(240, 150, 60, 0.12); color: #e0903a; }
.tag.category { background: #f2f3f5; color: #666; }
.meta-line { font-size: 13px; color: #8a8a8a; margin-bottom: 24px; }
.meta-line .dot { margin: 0 8px; }
.btn-row { display: flex; gap: 12px; }
.btn-read { padding: 12px 44px; background: #02c58d; color: #fff; border: none; border-radius: 6px; font-size: 15px; cursor: pointer; }
.btn-read:hover { background: #01ad7c; }
.btn-download { padding: 12px 32px; background: #fff; color: #333; border: 1px solid #e0e0e0; border-radius: 6px; font-size: 15px; cursor: pointer; }
.btn-download:hover { border-color: #02c58d; color: #02c58d; }
.btn-borrow { padding: 12px 32px; background: #fff; color: #02c58d; border: 1px solid #02c58d; border-radius: 6px; font-size: 15px; cursor: pointer; }
.btn-borrow:hover { background: rgba(2, 197, 141, 0.06); }
.btn-borrow.disabled { background: #f2f3f5; color: #b0b3b8; border-color: #e0e0e0; cursor: not-allowed; }
.author-card { flex-shrink: 0; width: 220px; background: #f7f8fa; border-radius: 8px; padding: 18px; align-self: flex-start; }
.author-name { font-size: 15px; font-weight: 600; color: #1a1a1a; margin-bottom: 8px; }
.author-bio { font-size: 12px; color: #8a8a8a; line-height: 1.6; }

/* 板块 */
.section { margin-top: 36px; border-top: 1px solid #f0f0f0; padding-top: 24px; }
.section-title { font-size: 16px; font-weight: 700; color: #1a1a1a; margin: 0 0 12px; }
.section-body { font-size: 14px; color: #444; line-height: 1.9; margin: 0; }

/* 文件信息行 */
.file-row { display: flex; font-size: 14px; padding: 8px 0; border-bottom: 1px dashed #f0f0f0; }
.file-row .label { width: 80px; color: #8a8a8a; }
.file-row .value { color: #333; }

.empty { text-align: center; color: #8a8a8a; padding: 80px 0; font-size: 14px; }
</style>
