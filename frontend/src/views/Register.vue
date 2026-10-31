<template>
  <div style="text-align:center;margin-top:100px;font-family:sans-serif;">
    <h2>ChippyBookNook 注册</h2>
    <input v-model="username" placeholder="用户名" /><br/><br/>
    <input v-model="password" type="password" placeholder="密码" /><br/><br/>
    <input v-model="confirmPassword" type="password" placeholder="确认密码" /><br/><br/>
    <button @click="register">注册</button>
    <div style="margin-top:10px;font-size:13px;color:#8a8a8a;">
      已有账号？<a style="color:#02c58d;cursor:pointer;" @click="$router.push('/login')">去登录</a>
    </div>
  </div>
</template>

<script>
import request from "../api/request";

export default {
  data() {
    return { username: '', password: '', confirmPassword: '' }
  },
  methods: {
    async register() {
      // 前端先校验两次密码是否一致（这是体验层的校验，真正的防线在后端）
      if (this.password !== this.confirmPassword) {
        alert('两次密码不一致')
        return
      }
      const res = await request.post('/user/register', {
        username: this.username,
        password: this.password
      })
      if (res.code === 200) {
        alert('注册成功，请登录')
        this.$router.push('/login')
      } else {
        alert(res.message)
      }
    }
  }
}
</script>
