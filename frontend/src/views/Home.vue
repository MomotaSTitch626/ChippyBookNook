<template>
  <div class="app">
    <!-- 顶部导航：共享组件 -->
    <AppNav />

    <!-- 搜索区：居中药丸搜索框 -->
    <div class="search-bar">
      <input v-model="keyword" @keyup.enter="doSearch" placeholder="搜索书名 / 作者" class="search-input" />
      <button @click="doSearch" class="search-btn">搜索</button>
    </div>

    <div v-show="false">
      <button @click="loadAll" style="margin:5px;">全部</button>
      <button v-for="tag in tags" :key="tag" @click="page = 1; loadByTag(tag)" style="margin:5px;">{{ tag }}</button>
    </div>
    <!-- 书架：卡片网格 -->
    <main class="container">                                     <!-- 主内容区：限宽 1080 居中 -->
      <div v-if="books.length" class="shelf">                    <!-- 有书才渲染；shelf=网格容器 -->
        <div v-for="book in books" :key="book.id" class="card">  <!-- 每本书一张卡片，key 是书的身份证 -->
          <div class="cover-wrap">                               <!-- 封面盒子：锁 3:4 竖版比例 -->
            <img v-if="book.cover" :src="'/uploads/' + book.cover" class="cover" alt="封面" />  <!-- 有封面 → 拼 URL 显示图片 -->
            <div v-else class="cover placeholder">{{ (book.name || '书').charAt(0) }}</div>     <!-- 没封面 → 显示书名首字占位 -->
          </div>
          <div class="info">                                     <!-- 卡片下半部分：文字信息 -->
            <div class="title" :title="book.name">{{ book.name }}</div>        <!-- 书名；悬停时弹完整名 -->
            <div class="author">{{ book.author || '未知作者' }}</div>          <!-- 作者；空则显示"未知作者" -->
            <div class="meta">                                   <!-- 一行两列：徽章 + 大小 -->
              <span class="badge">{{ book.fileFormat || '—' }}</span>          <!-- 格式徽章：TXT/EPUB/— -->
              <span class="size">{{ formatSize(book.fileSize) }}</span>         <!-- 大小：调方法翻译成 B/KB/MB -->
            </div>
            <div class="actions">                                <!-- 操作按钮组 -->
              <button v-if="book.filePath" class="btn outline" @click="downloadBook(book)">下载</button>      <!-- 有文件才显示：绿描边 -->
              <button v-if="book.fileFormat === 'TXT'" class="btn solid" @click="readBook(book)">在线阅读</button>  <!-- 仅 TXT：绿底实心 -->
              <button v-if="!book.filePath" class="btn disabled" disabled>暂无文件</button>  <!-- 老书：灰禁用 -->
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty">暂无图书</div>                    <!-- 没书时显示空状态（和 v-if 是一对） -->

      <div class="pager">                                        <!-- 分页容器：居中一行 -->
        <button :disabled="page <= 1" @click="changePage(page - 1)" class="page-btn">上一页</button>  <!-- 第1页时禁用 -->
        <span class="page-info">第 {{ page }} / {{ totalPages }} 页 · 共 {{ total }} 本</span>        <!-- 页码信息 -->
        <button :disabled="page >= totalPages" @click="changePage(page + 1)" class="page-btn">下一页</button> <!-- 末页时禁用 -->
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
        books: [],
        tags: [],
        page: 1,
        pageSize: 4,
        total: 0,
        currentTag: null,
        keyword: ''
      }
    },
    computed: {
      // 新增：算出总页数。总数÷每页条数，向上取整；0 本书时算 1 页避免显示 0/0
      totalPages() {
        return this.total === 0 ? 1 : Math.ceil(this.total / this.pageSize)
      }
    },
    created() {
      this.loadTags()
      this.loadBooks()
    },
    methods: {
      async loadTags() {
        const res = await request.get('/book-tag/list')
        if (res.code === 200){
          const tagSet = new Set()
          res.data.forEach(t => tagSet.add(t.tagName))
          this.tags = Array.from(tagSet)
        }
      },
      loadAll(){
        this.page = 1
        this.currentTag = null
        this.loadBooks()
      },
      async loadBooks() {
        const res = await request.get('/book/list', { params: { page: this.page, pageSize: this.pageSize } })
        if (res.code === 200) {
          this.books = res.data.records
          this.total = res.data.total
        }else {
          alert(res.message)
        }
      },

      async loadByTag(tagName) {
        this.currentTag = tagName
        const res = await request.get('/book/by-tag', { params: { tagName: tagName,page: this.page, pageSize: this.pageSize } })
        if (res.code === 200) {
          this.books = res.data.records
          this.total = res.data.total
        }
      },

      //翻页
      changePage(p) {
        this.page = p
        if (this.keyword){
          this.loadSearch()
        } else if (this.currentTag) {
          this.loadByTag(this.currentTag)
        } else {
          this.loadBooks()
        }
      },

      //把字节数换算成人类可读的大小
      formatSize(size) {
        if (!size) {
          return '—'
        }
        if (size < 1024) {
          return size + ' B'
        }
        if (size < 1024 * 1024) {
          return (size / 1024).toFixed(2) + ' KB'
        }
        return (size / 1024 / 1024).toFixed(2) + ' MB'
      },

      //下载：axios 带 token 把文件拿回来，再模拟点击下载
      async downloadBook(book) {
        const res = await request.get('/book/download/' + book.id, { responseType: 'blob' })
        const url = URL.createObjectURL(res)
        const a = document.createElement('a')
        a.href = url
        a.download = book.name + '.' + (book.fileFormat || '').toLowerCase()
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        URL.revokeObjectURL(url)
      },

      //在线阅读：跳到阅读页（第 8 步做那个页面）
      readBook(book) {
        this.$router.push('/read/' + book.id + '?name=' + encodeURIComponent(book.name))
      },

      //回车/点击搜索按钮查询
      doSearch(){
        this.page = 1
        this.currentTag = null
        this.loadSearch()
      },
      //搜索
      async loadSearch(){
        const res = await request.get('/book/search', {params: { keyword: this.keyword, page: this.page, pageSize: this.pageSize}})
        if (res.code === 200){
          this.books = res.data.records
          this.total = res.data.total
        }
      },
    }
}
</script>

<style scoped>
.app {
  min-height: 100vh;                                   /* 至少占满一整个浏览器可视高度 */
  background: #f5f6f7;                                 /* 浅灰底：微信读书的"地板色" */
  font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;  /* 苹方/雅黑，网页更接近 App */
}
.search-bar {
  display: flex;
  justify-content: center;                             /* 搜索区水平居中 */
  gap: 12px;                                           /* 框和按钮间距：10 → 12 */
  margin: 24px 0;                                      /* 上下留白 */
}
.search-input {
  width: 600px;                                        /* 320 → 600：宽度接近翻倍，微信读书那种宽 */
  max-width: 90%;                                      /* 防溢出：小屏幕时最多占容器 90%，不会撑破 */
  padding: 14px 22px;                                  /* 10/18 → 14/22：上下内边距加大 = 框变高 */
  border: 1px solid #e0e0e0;
  border-radius: 25px;                                 /* 25 ：药丸变成微信读书那种偏方的圆角 */
  font-size: 16px;                                     /* 16：字也大一点 */
  outline: none;
  background: #fff;
  transition: border-color 0.2s;
}

.search-input:focus {
  border-color: #02c58d;                               /* 点进去打字 → 边框变绿 */
}
.search-btn {
  padding: 14px 30px;                                  /* 10/22 → 14/30：和输入框同高，视觉齐平 */
  border: none;
  border-radius: 25px;                                 /* 25 ：和输入框圆角一致 */
  background: #02c58d;
  color: #fff;
  font-size: 16px;                                     /* 14 → 16 */
  cursor: pointer;
}

.search-btn:hover {
  background: #02b37e;
}

/* ===== 内容区 ===== */
.container {
  max-width: 1280px;                                  /* 内容最宽 1280px，超宽屏也不拉满 */
  margin: 0 auto;                                     /* 左右 auto = 自动平分外边距 → 水平居中 */
  padding: 0 16px 48px;                               /* 左右 16px、底部 48px 留白 */
}

/* ===== 书架网格 ===== */
.shelf {
  display: grid;                                      /* 开启网格布局 */
  grid-template-columns: repeat(4,1fr);               /* 固定 4 列，但每列宽度 = 1 份"剩余空间" → 自动平分容器宽 */

  gap: 24px;                                          /* 卡片之间 20px 空隙 */
}

/* ===== 卡片 ===== */
.card {
  background: #fff;                                   /* 白卡片 */
  border-radius: 10px;                                /* 圆角 */
  overflow: hidden;                                   /* 子元素超出圆角的部分剪掉（封面图不穿帮） */
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);          /* 极淡阴影，卡片"浮"在灰底上 */
  transition: transform 0.2s, box-shadow 0.2s;        /* 上浮和阴影变化都走 0.2 秒过渡 */
}
.card:hover {
  transform: translateY(-4px);                        /* 悬停：整卡向上平移 4px（负=上） */
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);          /* 阴影加深，配合上浮 */
}

/* ===== 封面区 ===== */
.cover-wrap {
  aspect-ratio: 3 / 4;                  /* 封面区整体锁定 3:4：所有卡片封面区高度统一 */
  padding: 16px;                        /* 内边距 14 → 16，白边再大一点点 */
  display: flex;                        /* 弹性盒子：让封面能在盒子里被"搬运" */
  align-items: center;                  /* 垂直居中 */
  justify-content: center;              /* 水平居中 → 封面永远站在盒子正中央 */
  background: transparent;              /* 白底 */
}


.cover {
  width: 62%;                           /* 100 → 62%：封面只占内宽一半多，四周大留白（微信读书那种） */
  aspect-ratio: 3 / 4;                  /* 强制真实书比例：不管原图什么比例，都裁成 3:4 竖版书 */
  object-fit: cover;                    /* 配合 aspect-ratio：图片等比缩放 + 裁剪填满这个书形 */
  display: block;
  border-radius: 4px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.15);   /* 新增：封面底部的投影，像书立在桌上 */
}


.cover.placeholder {
  width: 62%;                           /* 和封面图一样宽：没封面的"书"也保持同样大小 */
  aspect-ratio: 3 / 4;
  background: #f0f1f2;
  border-radius: 4px;                   /* 灰块也带圆角，和真封面一致 */
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 42px;
  color: #c8c9cc;
  font-weight: 600;
}



/* ===== 卡片文字区 ===== */
.info {
  padding: 10px 12px 12px;                            /* 上 10 左右 12 下 12：内容不贴卡片边 */
}
.title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  white-space: nowrap;        /* 不许换行 */
  overflow: hidden;           /* 超出的藏起来 */
  text-overflow: ellipsis;    /* 藏起来那段变"…" —— 这三件套缺一不可 */
}
.author {
  font-size: 12px;
  color: #8a8a8a;
  margin-top: 3px;                                    /* 和书名隔 3px */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;    /* 作者名超长同样出省略号 */
}
.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;                     /* 徽章最左、大小最右 */
  margin-top: 8px;
}
.badge {
  font-size: 11px;
  color: #02c58d;                                     /* 绿字 */
  background: rgba(2, 197, 141, 0.08);                /* 同款绿的 8% 透明度浅绿底 = 同色系深浅 */
  padding: 2px 8px;
  border-radius: 8px;
}
.size {
  font-size: 11px;
  color: #b0b0b0;
}

/* ===== 操作按钮组 ===== */
.actions {
  display: flex;
  gap: 6px;                                           /* 按钮间距 6px */
  margin-top: 10px;
  flex-wrap: wrap;                                    /* 放不下自动换行（4 个按钮可能挤） */
}
.btn {
  font-size: 12px;
  padding: 5px 10px;
  border-radius: 6px;
  cursor: pointer;
  border: none;
}
.btn.outline {                                        /* 描边款：次要操作（下载） */
  background: #fff;
  border: 1px solid #02c58d;
  color: #02c58d;
}
.btn.outline:hover {
  background: rgba(2, 197, 141, 0.06);                /* 悬停淡绿底 */
}
.btn.solid {                                          /* 实心款：最重要（在线阅读） */
  background: #02c58d;
  color: #fff;
}
.btn.solid:hover {
  background: #02b37e;
}
.btn.disabled {                                       /* 禁用款：点不动 */
  background: #f0f1f2;
  color: #b0b0b0;
  cursor: not-allowed;                                /* 鼠标变禁止符号 */
}
.btn.danger {                                         /* 危险款：删除（弱化存在感） */
  background: none;
  color: #e05a5a;
}
.btn.danger:hover {
  color: #c94343;
}

/* ===== 空状态 ===== */
.empty {
  text-align: center;
  color: #b0b0b0;
  padding: 80px 0;                                    /* 上下大留白，居中显示"暂无图书" */
  font-size: 14px;
}

/* ===== 分页 ===== */
.pager {
  display: flex;
  justify-content: center;                            /* 居中 */
  align-items: center;
  gap: 14px;
  margin-top: 28px;
}
.page-btn {
  padding: 7px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: #fff;
  color: #333;
  font-size: 13px;
  cursor: pointer;
}
.page-btn:hover:not(:disabled) {                      /* :not(:disabled)=排除禁用态，禁用时不触发变绿 */
  border-color: #02c58d;
  color: #02c58d;
}
.page-btn:disabled {
  color: #c8c9cc;
  cursor: not-allowed;
}
.page-info {
  font-size: 13px;
  color: #8a8a8a;
}

/* ===== 响应式：平板（屏幕宽度 ≤ 1200px） ===== */
@media (max-width: 1200px) {
  .container {
    max-width: 100%;       /* 平板不再限宽，占满 */
    padding: 0 24px 48px;  /* 两侧 24px 留白 */
  }
  .shelf {
    grid-template-columns: repeat(3, 1fr);  /* 4 列 → 3 列：窄屏放 4 张会挤 */
    gap: 20px;
  }
}

/* ===== 响应式：手机（屏幕宽度 ≤ 768px） ===== */
@media (max-width: 768px) {
  .search-bar { margin: 16px 0; }       /* 搜索区上下留白缩小 */
  .search-input { width: 100%; padding: 12px 16px; font-size: 15px; }  /* 搜索框占满 + 内边距缩小 */
  .search-btn { padding: 12px 18px; }   /* 搜索按钮同步缩小 */
  .shelf { grid-template-columns: repeat(2, 1fr); gap: 14px; }  /* 手机 2 列 */
  .cover.placeholder { font-size: 30px; }  /* 占位字 42 → 30：手机卡片小，字太大放不下 */
}

</style>

