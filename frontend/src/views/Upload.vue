<template>
  <div style="text-align: center; margin-top:50px; font-family:sans-serif;">
    <h2>上传图书</h2>
    <div style="width: 400px; margin: 0 auto; text-align: left; line-height: 2.5;">
      <div>
        书名（必填）：<input v-model="name" type="text" placeholder="请输入书名" style="width:200px;" />
      </div>
      <div>
        作者（可选）：<input v-model="author" type="text" placeholder="请输入作者" style="width:200px;" />
      </div>
      <div>
        格式（必填）：
        <select v-model="format">
          <option value="">--请选择--</option>
          <option value="TXT">TXT</option>
          <option value="PDF">PDF</option>
          <option value="EPUB">EPUB</option>
        </select>
      </div>
      <div>
        分类（可选）：
        <select v-model="category">
          <option value="">--请选择--</option>
          <option v-for="c in CATEGORIES" :key="c" :value="c">{{ c }}</option>
        </select>
      </div>
      <div>
        状态（可选）：
        <select v-model="status">
          <option value="">--请选择--</option>
          <option v-for="s in STATUSES" :key="s" :value="s">{{ s }}</option>
        </select>
      </div>

      <div>
        出版时间（可选）：<input v-model="publishTime" type="date" style="width:200px;" />
      </div>
      <div>
        书文件（必填）：<input type="file" @change="onFileChange" />
      </div>
      <div>
        封面（可选）：<input type="file" accept="image/*" @change="onCoverChange" />
      </div>
      <button @click="submit" style="margin-top:20px; padding:6px 24px;">上传图书</button>
    </div>
  </div>
</template>

<script>
import request from "../api/request";
import { CATEGORIES, STATUSES } from '../constants/categories';

export default {
  data(){
    return{
      CATEGORIES,
      STATUSES,
      name: '',
      author: '',
      format: '',
      category: '',
      status: '',
      publishTime: '',
      file: null,
      cover: null
    }
  },
  methods: {
    onFileChange(e){
      this.file = e.target.files[0]
    },
    onCoverChange(e){
      this.cover = e.target.files[0]
    },
    async submit(){
      if (!this.name.trim()) {
        alert('请填写书名');
        return
      }
      if (!this.format) {
        alert('请选择格式');
        return
      }
      if (!this.file) {
        alert('请选择书文件');
        return
      }
      const formData = new FormData()
      formData.append('name', this.name)
      formData.append('author', this.author)
      formData.append('format', this.format)
      if (this.category){
        formData.append('category', this.category)
      }
      if (this.status){
        formData.append('status', this.status)
      }
      if (this.publishTime){
        formData.append('publishTime',this.publishTime)
      }
      if (this.cover){
        formData.append('cover',this.cover)
      }
      formData.append('file',this.file)
      const res = await request.post('/book/upload', formData)
      if (res.code === 200){
        alert('上传成功')
        this.$router.push('/')
      }else {
        alert(res.message)
      }
    }
  }
}
</script>