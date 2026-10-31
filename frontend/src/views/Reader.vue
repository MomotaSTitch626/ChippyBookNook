<template>
  <div style="max-width:800px;margin:0 auto;padding:20px;font-family:sans-serif;">
    <h2>{{ bookName }}</h2>
    <pre style="white-space:pre-wrap;line-height:1.8;font-size:16px;font-family:'SimSun',serif;">{{ content }}</pre>
    <button @click="goBack">返回书架</button>
  </div>
</template>

<script>
import request from "../api/request";

export default {
  data() {
    return {
      bookId: this.$route.params.bookId,
      bookName: this.$route.query.name || '阅读',
      content: ''
    }
  },
  created() {
    this.loadContent()
  },
  methods: {
    async loadContent() {
      const res = await request.get('/book/read/' + this.bookId)
      if (res.code === 200) {
        this.content = res.data
      } else {
        alert(res.message)
        this.$router.push('/')
      }
    },
    goBack() {
      this.$router.back()
    }
  }
}
</script>
