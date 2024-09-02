<template>
  <div class="main">
  <a-form id="components-form-demo-normal-login" :form="form" class="login-form" @submit="handleSubmit">
    <a-form-item>
      <a-input
      v-decorator="[
    'userName',
    { rules: [{ required: true, message: 'Please input your username!' }] },
    ]"
    placeholder="Username">
        <a-icon slot="prefix" type="user" style="color: rgba(0,0,0,.25)" />
      </a-input>
    </a-form-item>
    <a-form-item>
      <a-input
      v-decorator="[
    'password',
    { rules: [{ required: true, message: 'Please input your Password!' }] },
  ]"
  type="password"
  placeholder="Password">
        <a-icon slot="prefix" type="lock" style="color: rgba(0,0,0,.25)" />
      </a-input>
    </a-form-item>
    <a-form-item>
      <a-checkbox
      v-decorator="[
    'remember',
    {
      valuePropName: 'checked',
      initialValue: true,
    },
  ]">
        Remember me
      </a-checkbox>
      <a class="login-form-forgot" href="">
        Forgot password
      </a>
      <a-button type="primary" html-type="submit" class="login-form-button">
        Log in
      </a-button>
      Or
      <a href="">
        register now!
      </a>
    </a-form-item>
  </a-form>
</div>
</template>
<script>

import { getAccessToken } from '@/api/login'
import storage from 'store'
import { AUTHORIZATION } from '@/store/mutation-types'

export default {
  beforeCreate () {
    this.form = this.$form.createForm(this, { name: 'normal_login' })
  },
  methods: {
    handleSubmit (e) {
      e.preventDefault()
      this.form.validateFields((err, values) => {
        if (!err) {
          console.log('Received values of form: ', values)
          var params = {
            username: values.userName,
            password: values.password
          }
          const hide = this.$message.loading('正在登录..', 0)
          getAccessToken(params)
            .then(res => {
              setTimeout(hide, 2500)
              this.$notification['success']({
                message: '提示',
                description: '你的AccessToken为：' + res.access,
                duration: 8
              })
              storage.set(AUTHORIZATION, 'Bearer' + ' ' + res.access, 7 * 24 * 60 * 60 * 1000)
              this.$router.push({ path: '/' })
            })
          }
      })
    }
  }
}
</script>
<style>
#components-form-demo-normal-login .login-form {
  max-width: 300px;
}

#components-form-demo-normal-login .login-form-forgot {
  float: right;
}

#components-form-demo-normal-login .login-form-button {
  width: 100%;
}
</style>
