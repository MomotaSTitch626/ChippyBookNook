<template>
  <div style="text-align:center;margin-top:100px;font-family:sans-serif;">
    <h2>ChippyBookNook 登录</h2>
    <input v-model="username" placeholder="用户名" /><br/><br/>
    <input v-model="password" type="password" placeholder="密码" /><br/><br/>
    <button @click="login">登录</button>
    <div style="margin-top:10px;font-size:13px;color:#8a8a8a;">
      没有账号？<a style="color:#02c58d;cursor:pointer;" @click="$router.push('/register')">去注册</a>
    </div>
  </div>
</template>

<script>
import request from "../api/request";

export default {
  data() {
    return { username: '', password:''}
  },
  methods: {
    async login() {
      const res = await request.post('/user/login',{
        username: this.username,
        password: this.password
      })
      if (res.code === 200) {
        localStorage.setItem('token', res.data)
        this.$router.push('/')
      }else {
        alert(res.message)
      }
    }
  }
}
</script>