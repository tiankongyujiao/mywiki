<script setup lang="ts">
import { ref } from "vue"
import request from "./api"

const message = ref("")
const message2 = ref("")

const getHello = async ()=>{
  const res = await request.get("/hello")
  message.value = res.data
}

const getArticle = async ()=>{
  const res = await request.get("/articles/2086995875690233858")
  message2.value = res.data
}

</script>

<template>
  <div>
    <h1>MyWiki</h1>
    <button @click="getHello">调用后端</button>
    <p>{{message}}</p>


    <button @click="getArticle">调用后端</button>
    <p>{{message2}}</p>
  </div>
</template>