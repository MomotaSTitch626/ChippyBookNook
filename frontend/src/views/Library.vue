<template>
  <div class="app">
    <!-- 顶部导航：品牌 + 首页/书库 + 小搜索框（没有上传/退出）+ 头像 -->
    <AppNav />

    <!-- 主内容区 -->
    <main class="container">
      <!-- 筛选区：分类 + 状态 两行 -->
      <div class="filter-bar">
        <div class="filter-row">
          <span class="filter-label">分类</span>
          <span class="filter-item" :class="{ on: selectedCategory === '' }" @click="pickCategory('')">全部</span>
          <span v-for="c in CATEGORIES" :key="c" class="filter-item"
                :class="{ on: selectedCategory === c }" @click="pickCategory(c)">{{ c }}</span>
        </div>
        <div class="filter-row">
          <span class="filter-label">状态</span>
          <span class="filter-item" :class="{ on: selectedStatus === '' }" @click="pickStatus('')">全部</span>
          <span v-for="s in STATUSES" :key="s" class="filter-item"
                :class="{ on: selectedStatus === s }" @click="pickStatus(s)">{{ s }}</span>
        </div>
      </div>
      <div class="divider"></div>
      <!-- 书单列表：番茄风格卡片网格，每行 3 个 -->
      <div v-if="books.length" class="list">
        <div v-for="book in books" :key="book.id" class="book-card">
          <div class="card-cover" @click="goDetail(book)" >
            <img v-if="book.cover" :src="'/uploads/' + book.cover" class="card-cover-img" alt="封面" />
            <div v-else class="card-cover-img placeholder">{{ (book.name || '书').charAt(0) }}</div>
          </div>
          <div class="card-info">
            <div class="card-title" @click="goDetail(book)">{{ book.name }}</div>
            <div class="card-meta">
              <span class="card-author">{{ book.author || '未知作者' }}</span>
            </div>
            <div class="card-status" :class="book.status === '连载中' ? 'ongoing' : ''">{{ book.status || '未知' }}</div>
            <div class="card-desc">{{ book.description || '暂无' }}</div>
          </div>
        </div>
      </div>
      <div v-else class="empty">暂无图书</div>

      <!-- 分页器 -->
      <div class="pager">
        <button :disabled="page <= 1" @click="changePage(page - 1)" class="page-btn">上一页</button>
        <span class="page-info">第 {{ page }} / {{ totalPages }} 页 · 共 {{ total }} 本</span>
        <button :disabled="page >= totalPages" @click="changePage(page + 1)" class="page-btn">下一页</button>
      </div>

    </main>
  </div>
</template>

<script>
import request from "../api/request";
import { CATEGORIES, STATUSES } from '../constants/categories';
import AppNav from './AppNav.vue';


export default {
  components: { AppNav },
  data() {
    return {
      CATEGORIES,
      STATUSES,
      keyword: '',          // 搜索框文字
      selectedCategory: '', // 选中的分类（'' = 全部）
      selectedStatus: '',   // 选中的状态（'' = 全部）
      books: [],            // 当前页的书
      page: 1,              // 当前页
      pageSize: 15,         // 每页 15 条（你拍板的）
      total: 0              // 总条数
    }
  },
  computed: {
    // 总页数：总数 ÷ 每页条数，向上取整；0 本算 1 页避免显示 0/0
    totalPages() {
      return this.total === 0 ? 1 : Math.ceil(this.total / this.pageSize)
    }
  },
  created() {
    this.keyword = this.$route.query.keyword || ''     // 详情页搜索框跳过来时带上关键词
    this.loadBooks()
  },
  methods: {
    // 点分类：记下选中的词，页码回第 1 页
    pickCategory(c) {
      this.selectedCategory = c
      this.page = 1
      this.loadBooks()
    },
    // 点状态：同上
    pickStatus(s) {
      this.selectedStatus = s
      this.page = 1
      this.loadBooks()
    },
    // 核心：按当前条件请求后端
    async loadBooks() {
      let url = '/book/list'                                   // 默认走列表接口
      const params = { page: this.page, pageSize: this.pageSize }
      if (this.keyword) {                                      // ① 有搜索词 → 走搜索接口
        url = '/book/search'                                   //    /search 支持书名/作者模糊
        params.keyword = this.keyword
      } else {                                                 // ② 没搜索词 → 走列表接口 + 筛选
        if (this.selectedCategory) params.category = this.selectedCategory
        if (this.selectedStatus) params.status = this.selectedStatus
      }
      const res = await request.get(url, { params })
      if (res.code === 200) {
        this.books = res.data.records                          // 当前页的书
        this.total = res.data.total                            // 总条数
      } else {
        alert(res.message)
      }
    },
    // 翻页
    changePage(p) {
      this.page = p
      this.loadBooks()
    },
    // 点击封面/书名 → 跳图书详情页
    goDetail(book) {
      this.$router.push('/book/' + book.id)
    }
  },
}
</script>

<style scoped>
.app {
  min-height: 100vh;                                     /* 至少占满一屏高 */
  background: #fff;                                      /* #fff：整页白底 */
  font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;
}

/* ===== 主内容区 ===== */
.container {
  max-width: 1280px;                                     /* 和首页同宽 */
  margin: 0 auto;
  padding: 20px 16px 48px;
}
/* ===== 分割线：与分类/状态栏、卡片封面同一起点 ===== */
.divider {
  border-top: 1px solid #e8e8e8;                         /* 极淡灰线 */
  margin-bottom: 20px;                                   /* 和下面的卡片列表隔开 */
}

/* ===== 筛选区 ===== */
.filter-bar {
  background: transparent;                               /* 白卡 → 透明：筛选区不再是个盒子，直接铺在页面白底上 */
  border-radius: 0;                                      /* 圆角去掉 */
  box-shadow: none;                                      /* 阴影去掉 */
  padding: 16px 0;                                       /* 左右内边距 20 → 0（不再有"盒子"左右边） */
  margin-bottom: 12px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;                                       /* 手机窄屏时自动换行 */
  align-items: center;
  gap: 8px;
}
.filter-row + .filter-row {
  margin-top: 24px;                                      /* 两行筛选之间隔开 */
}
.filter-label {
  font-size: 13px;
  color: #8a8a8a;                                        /* 灰标签：分类/状态 */
  margin-right: 6px;
}
.filter-item {
  font-size: 13px;
  color: #333;
  padding: 5px 14px;
  border-radius: 16px;
  cursor: pointer;
  /* 注意：删掉了 background: #f5f6f7 —— 未选中的词条不再有灰药丸底，
     变成纯文字（番茄那样） */
}
.filter-item:hover {
  color: #02c58d;                                        /* 悬停：文字变绿（没有背景，纯文字反馈） */
}
.filter-item.on {
  background: #02c58d;                                   /* 选中：绿底白字药丸（只有这一项有高亮） */
  color: #fff;
}


/* ===== 空状态 ===== */
.empty {
  text-align: center;
  color: #b0b0b0;
  padding: 80px 0;
  font-size: 14px;
}
/* ===== 书单卡片网格 ===== */
.list {
  display: grid;                                            /* 网格布局 */
  grid-template-columns: repeat(3, 1fr);                    /* 每行固定 3 列，均分宽度 */
  gap: 16px;                                                /* 卡片间距 */
}
.book-card {
  display: flex;                                            /* 卡片内左右排：封面 | 信息 */
  gap: 12px;
  background: #fff;
  border-radius: 8px;
  padding: 14px;                                            /* 12 → 14：卡片内边距加大 */
  min-width: 0;                                             /* 防止卡片内文字把卡片撑破 */
  /* 注意：删掉了原来的 box-shadow —— 番茄卡片就是纯白一块，
     靠灰底和间距区分，看不出"边框"感 */
}
.card-cover {
  flex-shrink: 0;                                           /* 封面不压缩 */
  overflow: hidden;                                         /* ：放大后的图片超出部分被剪掉（放大镜的"框"） */
  border-radius: 4px;                                       /* ：圆角从图片挪到容器——裁剪边沿也是圆角，不露直角 */
  cursor: pointer;
}
.card-cover-img {
  width: 84px;                                              /* 小竖版封面 */
  aspect-ratio: 3 / 4;                                      /* 真实书比例 */
  object-fit: cover;
  display: block;
  border-radius: 0;                                         /* 圆角交给容器裁切 */
  transition: transform 0.3s ease;                          /* ★：放大/缩回的过渡动画（0.3 秒渐变） */
}
.card-cover:hover .card-cover-img {
  transform: scale(1.1);                                    /* ★：鼠标悬停时放大 110%（即放大 10%） */
}

.card-cover-img.placeholder {
  background: #f0f1f2;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;                                          /* 22 → 26：封面大了字也大一点 */
  color: #c8c9cc;
  font-weight: 600;
}
.card-info {
  flex: 1;                                                  /* 信息区吃掉卡片剩余宽度 */
  min-width: 0;
  display: flex;                                            /* 信息区内部竖着排 */
  flex-direction: column;
  justify-content: center;                                  /* 文字内容垂直居中（番茄那种舒展感） */
}
.card-title {
  font-size: 16px;                                          /* 15 → 16：书名稍大 */
  font-weight: 600;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;                                  /* 书名超长出省略号 */
  cursor: pointer;
}
.card-title:hover {
  color: #02c58d;      /* 悬停书名变品牌绿：提示可点 */
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}
.card-author {
  font-size: 12px;
  color: #8a8a8a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-status {
  font-size: 11px;
  padding: 1px 7px;
  border-radius: 8px;
  background: rgba(2, 197, 141, 0.08);                      /* 已完结：浅绿底绿字 */
  color: #02c58d;
  display: inline-block;                                    /* 小块标签 */
  align-self: flex-start;
  margin-top: 8px;                                          /* 和上面的作者隔开：独占一行 */
}
.card-status.ongoing {
  background: rgba(240, 150, 60, 0.12);                     /* 连载中：浅橙底橙字 */
  color: #e0903a;
}
.card-desc {
  font-size: 12px;
  color: #8a8a8a;
  margin-top: 6px;
  line-height: 1.5;
  display: -webkit-box;                                     /* 允许最多 2 行（番茄简介就是两行） */
  -webkit-line-clamp: 2;                                    /* 超过 2 行截断 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 36px;                                         /* 2 行的高度：12px × 1.5 × 2 = 36px —— 没简介时卡片高度也一致 */
}


/* ===== 分页 ===== */
.pager {
  display: flex;
  justify-content: center;
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
.page-btn:hover:not(:disabled) {
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

/* ===== 响应式：手机 ===== */
@media (max-width: 1200px) {
  .list { grid-template-columns: repeat(2, 1fr); }          /* 平板：3 列 → 2 列 */
}

@media (max-width: 768px) {
  .filter-bar { padding: 12px 14px; }
  .filter-item { padding: 4px 10px; font-size: 12px; }
  .list { grid-template-columns: repeat(2, 1fr); gap: 12px; }   /* 手机也 2 列 */
  .card-desc { display: none; }                                 /* 手机卡片太窄，简介藏起来 */
}
</style>
