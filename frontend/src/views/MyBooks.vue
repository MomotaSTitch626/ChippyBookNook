<template>
  <div class="app">
    <!-- 顶部导航：共享组件 -->
    <AppNav />

    <main class="container">
      <!-- 书架：竖版卡片网格（像首页卡片） -->
      <div v-if="books.length" class="shelf">
        <div v-for="book in books" :key="book.id" class="card">
          <div class="cover-wrap" @click="goDetail(book)">
            <img v-if="book.cover" :src="'/uploads/' + book.cover" class="cover" alt="封面" />
            <div v-else class="cover placeholder">{{ (book.name || '书').charAt(0) }}</div>
          </div>
          <div class="info">
            <div class="title-row">
              <div class="title" :title="book.name" @click="goDetail(book)">{{ book.name }}</div>
              <span v-if="book.source === 'lent'" class="lent-tag">借</span>
            </div>
            <div class="author">{{ book.author || '未知作者' }}</div>
            <div class="actions">
              <div class="btn-row" v-if="book.source !== 'lent'">
                <button class="act-btn" :class="{ on: book.readStatus === 0 }" @click="updateStatus(book, 0)" :disabled="book.readStatus === 0">未读</button>
                <button class="act-btn" :class="{ on: book.readStatus === 1 }" @click="updateStatus(book, 1)" :disabled="book.readStatus === 1">在读</button>
                <button class="act-btn" :class="{ on: book.readStatus === 2 }" @click="updateStatus(book, 2)" :disabled="book.readStatus === 2">已读</button>
              </div>
              <div class="btn-row">
                <button class="act-btn note" @click="goNote(book)">笔记</button>
                <button v-if="book.source !== 'lent'" class="act-btn danger" @click="deleteBook(book)">删除</button>
                <button v-if="book.source === 'lent'" class="act-btn lent-return" @click="returnBook(book)">还书</button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty">暂无书籍</div>
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
      books: [],           // 我读过的书列表
    }
  },
  created() {
    this.loadMyBooks()
  },
  methods: {
    // 拉我的书库：GET /book/my（后端已按登录用户过滤）
    async loadMyBooks() {
      const res = await request.get('/book/my')
      if (res.code === 200) {
        this.books = res.data
      } else {
        alert(res.message)
      }
    },
    // 切换阅读状态：未读 0 / 在读 1 / 已读 2
    async updateStatus(book, status) {
      const res = await request.put('/book/update-status', { id: book.id, readStatus: status })
      if (res.code === 200) {
        this.loadMyBooks()       // 成功后重新拉列表，按钮高亮跟着刷新
      } else {
        alert(res.message)
      }
    },
    goDetail(book) {
      this.$router.push('/book/' + book.id)
    },
    goNote(book) {
      this.$router.push('/note/' + book.id)
    },
    goLibrary() { this.$router.push('/library') },
    // 删除自己的书：书屋里的书全是自己上传的，调"本人删除"接口（后端还会校验一次 ownerId）
    async deleteBook(book) {
      if (!confirm('确定删除《' + book.name + '》吗？')) return   // 防误删：先弹确认框
      const res = await request.delete('/book/' + book.id)
      if (res.code === 200) {
        alert('删除成功')
        this.loadMyBooks()      // 删完重新拉列表
      } else {
        alert(res.message)
      }
    },
    // 还书：把借来的书归还（后端按借阅记录id归还，还完这本书从书屋消失、笔记保留）
    async returnBook(book) {
      if (!confirm('确定归还《' + book.name + '》吗？')) return
      const res = await request.post('/lend-record/return', null, { params: { recordId: book.recordId } })
      if (res.code === 200) {
        alert('归还成功')
        this.loadMyBooks()      // 还完重新拉列表
      } else {
        alert(res.message)
      }
    }
  }
}
</script>

<style scoped>
.app {
  min-height: 100vh;                                    /* 撑满整屏 */
  background: #f5f6f7;                                  /* 页面浅灰底 */
  font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;   /* 系统字体 */
}

/* 主内容区 */
.container {
  max-width: 1280px;                                      /* 内容区宽：和首页/书库页同宽，4 列卡片更宽 */
  margin: 0 auto;                                        /* 水平居中 */
  padding: 48px 16px 60px;                               /* 顶部 48px：与导航栏拉开间距（原 28px） */
}

/* 书架网格：4 列卡片（一排放四个） */
.shelf {
  display: grid;                                        /* 网格布局 */
  grid-template-columns: repeat(4, 1fr);                /* 固定 4 列，宽度自动平分 */
  gap: 14px;                                             /* 卡片间距 */
}

/* 卡片：整卡正方形（宽=高），内部纵向排 封面区 + 信息区 */
.card {
  aspect-ratio: 1 / 1;                                  /* 整卡锁正方形 */
  display: flex;                                        /* 纵向排列 */
  flex-direction: column;
  background: #fff;                                     /* 白卡片 */
  border-radius: 10px;                                  /* 圆角 */
  overflow: hidden;                                     /* 封面超出圆角的部分剪掉 */
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);           /* 极淡阴影 */
  transition: transform 0.2s, box-shadow 0.2s;         /* 悬停过渡 */
}
.card:hover {
  transform: translateY(-4px);                          /* 悬停上浮 */
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);            /* 阴影加深 */
}

/* 封面区：占卡片剩余高度（卡片高 − 信息区高），封面在其中居中 */
.cover-wrap {
  flex: 1;                                              /* 占满剩余高度 */
  min-height: 0;                                        /* 允许压缩，不撑破正方形 */
  padding: 10px 12px;                                   /* 四周小留白 */
  display: flex;                                        /* 让封面能居中 */
  align-items: center;                                  /* 垂直居中 */
  justify-content: center;
  cursor: pointer;                                       /* 封面可点进详情 */
}
.cover {
  height: 100%;                                         /* 封面高度撑满封面区内容区，宽度按 3:4 自动等比 */
  width: auto;                                          /* 宽度交给比例计算 */
  aspect-ratio: 3 / 4;                                  /* 真实书本比例：宽3高4 */
  object-fit: cover;                                    /* 等比缩放 + 裁剪填满 */
  display: block;
  border-radius: 4px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.15);           /* 封面投影，像书立着 */
}
.cover.placeholder {
  height: 100%;                                         /* 占位块同封面：撑满封面区内容区 */
  width: auto;
  aspect-ratio: 3 / 4;
  background: #f0f1f2;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;                                       /* 首字（封面变大了，字稍调大） */
  color: #c8c9cc;
  font-weight: 600;
}

/* 信息区 */
.info {
  padding: 2px 14px 14px;                              /* 顶部 2px：书名紧贴封面（原 10px） */
}
.title {
  flex: 1;                                               /* 占满剩余宽度，让"借来"标签靠右 */
  min-width: 0;                                          /* 允许压缩，保证省略号生效 */
  font-size: 14px;                                       /* 书名（4 列卡小，字调小） */
  font-weight: 600;
  color: #1a1a1a;
  white-space: nowrap;                                  /* 不换行 */
  overflow: hidden;                                     /* 超出隐藏 */
  text-overflow: ellipsis;                              /* 超长显示省略号 */
  cursor: pointer;                                       /* 可点进详情 */
}
.title:hover {
  color: #02c58d;                                        /* 悬停变绿 */
}
/* 书名行：书名 + 借来标签横排 */
.title-row { display: flex; align-items: center; gap: 6px; }
.lent-tag { flex-shrink: 0; font-size: 11px; color: #e0903a; border: 1px solid #e0903a; border-radius: 4px; padding: 0 4px; }
.author {
  font-size: 12px;                                       /* 作者 */
  color: #8a8a8a;
  margin-top: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 按钮区：两行（状态行 + 工具行），每行按钮均分宽度 */
.actions {
  display: flex;                                        /* 纵向排两行 */
  flex-direction: column;
  gap: 6px;                                              /* 两行间距 */
  margin-top: 10px;
}
.btn-row {
  display: flex;                                        /* 行内横排 */
  gap: 4px;                                              /* 按钮间距 */
}
.act-btn {
  flex: 1;                                              /* 同行按钮等宽均分 */
  padding: 5px 0;                                       /* 上下内边距，宽度交给 flex */
  border: 1px solid #e0e0e0;                             /* 淡边框 */
  background: #fff;
  color: #666;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}
.act-btn:hover:not(:disabled) {
  border-color: #02c58d;                                 /* 悬停绿边 */
  color: #02c58d;
}
.act-btn.on {
  background: #02c58d;                                   /* 当前状态：绿底 */
  border-color: #02c58d;
  color: #fff;                                           /* 白字 */
}
.act-btn:disabled {
  color: #c8c9cc;                                        /* 当前状态按钮置灰 */
  cursor: not-allowed;
  border-color: #f0f0f0;
}
.act-btn.note {
  border-color: #02c58d;                                 /* 笔记按钮绿描边 */
  color: #02c58d;
}
.act-btn.note:hover {
  background: rgba(2, 197, 141, 0.06);                  /* 悬停淡绿 */
}
.act-btn.danger {
  border-color: #e05a5a;                                 /* 删除按钮红描边 */
  color: #e05a5a;
}
.act-btn.danger:hover {
  background: rgba(224, 90, 90, 0.06);                  /* 悬停淡红 */
}
.act-btn.lent-return { border-color: #02c58d; color: #02c58d; }
.act-btn.lent-return:hover { background: rgba(2, 197, 141, 0.06); }

/* 空状态 */
.empty {
  text-align: center;                                    /* 居中 */
  color: #b0b0b0;
  padding: 80px 0;                                       /* 上下大留白 */
  font-size: 14px;
}
</style>
