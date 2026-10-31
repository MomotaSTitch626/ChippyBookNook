<template>
  <div style="text-align:center;margin-top:50px;font-family:sans-serif;">
    <h2>《{{ bookName }}》的笔记</h2>
    <div>
      <input v-model="newContent" placeholder="写点读书笔记..." style="width:300px;padding:6px;" />
      <button @click="addNote" style="margin-left:10px;">添加笔记</button>
    </div>
    <table style="margin:20px auto;border-collapse:collapse;">
      <tbody>
      <tr>
        <th style="border:1px solid #ccc;padding:8px;">内容</th>
        <th style="border:1px solid #ccc;padding:8px;">时间</th>
        <th style="border:1px solid #ccc;padding:8px;">操作</th>
      </tr>
      <tr v-for="note in notes" :key="note.id">
        <td style="border:1px solid #ccc;padding:8px;">{{ note.content }}</td>
        <td style="border:1px solid #ccc;padding:8px;">{{ note.createTime }}</td>
        <td style="border:1px solid #ccc;padding:8px;">
          <button @click="deleteNote(note)">删除</button>
        </td>
      </tr>
      </tbody>
    </table>
    <br/>
    <button @click="goBack">返回</button>
  </div>
</template>

<script>
import request from '../api/request'

export default {
  data() {
    return {
      bookId: Number(this.$route.params.bookId),
      bookName: '',
      notes: [],
      newContent: ''
    }
  },
  created() {
    this.loadBook()
    this.loadNotes()
  },
  methods: {
    async loadBook() {
      const res = await request.get('/book/' + this.bookId)
      if (res.code === 200) {
        this.bookName = res.data.name
      }
    },
    async loadNotes() {
      const res = await request.get('/note/book/' + this.bookId)
      if (res.code === 200) {
        this.notes = res.data
      }
    },
    async addNote() {
      if (!this.newContent) {
        alert('笔记内容不能为空')
        return
      }
      const res = await request.post('/note', { bookId: this.bookId, content: this.newContent })
      if (res.code === 200) {
        alert('添加成功')
        this.newContent = ''
        this.loadNotes()
      } else {
        alert(res.message)
      }
    },
    async deleteNote(note) {
      const res = await request.delete('/note/' + note.id)
      if (res.code === 200) {
        alert('删除成功')
        this.loadNotes()
      } else {
        alert(res.message)
      }
    },
    goBack() {
      this.$router.back()
    }
  }
}
</script>
