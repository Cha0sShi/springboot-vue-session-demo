<template>
  <div>
    欢迎进入到学习平台
  </div>
  <div>
    <el-button @click="logout()" type="danger" plain>退出登录</el-button>
  </div>
</template>

<script setup>
import  {get} from "@/net"
import router from "@/router";
import {ElMessage} from "element-plus";
import {useStore} from "@/stores";


const logout = () => {get(
  "/api/auth/logout",(message)=>{
  ElMessage.success(message);
      const store = useStore()

      store.auth.user = null

      router.push("/")
  })
}

</script>
import {useStore} from "@/store";
<style scoped>

</style>