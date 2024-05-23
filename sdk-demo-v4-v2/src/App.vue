<script setup>
import { onMounted } from 'vue'
import { createWebglEngine, webglPlugin } from '@tslfe/dt-engine'

let meta, osPlugin;
const FloorMap = {
    0: '/Park_e50d76e1b0bd4a91869076afc36e6a01/Building_48b5fb64ad0340a1b2121478b20a9369/Floor_5bc543e44c994054b3ff843e6da0695c/graphic.glb',
    1: "/Park_e50d76e1b0bd4a91869076afc36e6a01/graphic.glb",
    2: "/Park_e50d76e1b0bd4a91869076afc36e6a01/Building_48b5fb64ad0340a1b2121478b20a9369/graphic.glb",
  };
function initMeta() {
  createWebglEngine((config) => {
    config.scene.cache = true;
    config.scene.cacheType = (url)=>{
      if(url === FloorMap.yuanqu){
        return true;
      }
      else{
        return false;
      }
    };
    return config
  }).then((app) => {
    app.amount('three-container');
    meta = app;
    change('0');
    // let control = meta.plugin.get('orbit-control')
    // control.mode('2d') // 2d | 3d
  })
}
onMounted(() => {
  setTimeout(()=>{
    initMeta()
  }, 1000)
})

function change(type){
  return meta.render(FloorMap[type], true).then(()=>{
    console.log("场景渲染完成.")
  })
}

</script>

<template>
  <p class="nav-box">
    <button @click="change('0')">场景一</button>
    <button @click="change('1')">场景二</button>
    <button @click="change('2')">场景三</button>
  </p>
  <div id="three-container">
  </div>
</template>

<style lang="less">
* {
  margin: 0;
  padding: 0;
}
html,
body,
#app, #three-container {
  width: 100%;
  height: 100%;
  position: absolute;
  overflow: hidden;;
}
.nav-box {
  height: 50px;
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 auto;
  width: 100%;
  justify-content: center;
  position: fixed;
  z-index: 999;
  top: 0;
  left: 0;
  
}
</style>
