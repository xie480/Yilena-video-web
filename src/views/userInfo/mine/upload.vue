<script lang="ts" setup>
import { ref,onMounted,computed,watch } from 'vue'
import { useRouter } from 'vue-router';
import { useTransition } from '@vueuse/core'
import { getUserInfoVideoList } from '../../../api/userInfo';
import CryptoJS from 'crypto-js'
import { ElMessage,ElMessageBox } from 'element-plus';
import { findAllVideos,getUserInfoByUsername,getHotSearch } from '../../../api';
import { getVideoYesByUserId,getAllCoins,getAllComments,getAllFavorites,getAllLikes,getAllShares,getAllViews,getBarrages } from '../../../api/upload';
import { getUserFavoritesList } from '../../../api/video';
import { getUserCoinsVideosList } from '../../../api/userInfo';
import {
  Document,
  Menu as IconMenu,
  Location,
  Setting,
} from '@element-plus/icons-vue'
const handleOpen = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}
const handleClose = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}
const router = useRouter();

const chunkArray = (arr, size) => {
  const chunks = []
  for (let i = 0; i < arr.length; i += size) {
    chunks.push(arr.slice(i, i + size))
  }
  return chunks
}

const videoList = ref([])
const videoCount = ref('0');

const getCurrentUserVideo = async () => {
  const result = await getVideoYesByUserId(currentUserId.value);
  if (result.code === 1) {
    const data = result.data;
    videoList.value = chunkArray(data, 5);
    videoCount.value = data.length;
  }else{
    ElMessage.error(result.msg);
  }
}


// 格式化视频时长（支持SS -> mm:ss 或 hh:mm:ss）
const formatVideoDuration = (seconds) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60

  // 使用padStart确保分钟和秒始终为两位数
  return h > 0 
    ? `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}` 
    : `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

const formatViews = (views)  =>{
  if (views >= 10000) {
    return `${Math.round(views / 1000) / 10}万`; // 保留1位小数（如：1.3万）
  }
  return views.toString();
}

function formatDate(createdTime) {
  const now = new Date();
  const targetTime = new Date(createdTime);
  const diff = now - targetTime;
  const diffDays = Math.floor(diff / (1000 * 3600 * 24));

  if (diff < 86400000) {
    const hours = Math.floor(diff / (1000 * 3600));
    return `${hours}小时前`;
  } else if (diffDays <= 7) {
    return `${diffDays}天前`;
  } else if (now.getFullYear() === targetTime.getFullYear()) {
    // 使用 padStart 补零，确保两位格式
    const month = String(targetTime.getMonth() + 1).padStart(2, '0');
    const day = String(targetTime.getDate()).padStart(2, '0');
    return `${month}-${day}`;
  } else {
    return targetTime.toISOString().split('T')[0];
  }
}


const playVideo = (videoId) => {
      router.push(`/video/${videoId}`).then(() => {
        window.location.reload();
      });
    }

const currentUserId = ref('');

const getCurrentUserId = async () => {
  const pathArray = router.currentRoute.value.path.split('/');
  console.log(pathArray);
  currentUserId.value = pathArray[2];
}

onMounted(async () => {
   getCurrentUserId();
   getCurrentUserVideo();
})
</script>

<template>
  <div class="uf-upload-menu">
   <el-menu
        default-active="2"
        class="el-menu-vertical-demo"
        @open="handleOpen"
        @close="handleClose"
      >
        <el-menu-item index="2">
          <span>视频
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            {{ videoCount }}</span>
        </el-menu-item>
        <el-menu-item index="3" disabled>
          <span>图文
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            0</span>
        </el-menu-item>
        <el-menu-item index="4" disabled>
          <span>音频
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            0</span>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="uf-upload-content">
      <div class="uf-video"  v-if="videoCount > 0">
    <el-row v-for="(group, index) in videoList" 
    :key="index" class="uf-upload-video-row">
    <el-col 
        v-for="video in group" 
        :key="video.id"
        :span="4" 
        class="uf-upload-video-col"
      >
      <div class="uf-botton-video" :id="video.id"  @click="playVideo(video.id)">
        <img :src="video.coverUrl" class="s-thumbnail-img" @click="playVideo(video.id)"/>
      <div class="uf-upload-top-shadow-layer-1"></div>
      </div>
      <div class="uf-video-info"> 
                          <svg t="1743757350808" class="uf-views-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="28554" width="200" height="200"><path d="M759.528727 209.408H264.424727A53.853091 53.853091 0 0 0 209.454545 262.004364v499.898181a53.992727 53.992727 0 0 0 54.970182 52.689455h495.080728a53.829818 53.829818 0 0 0 55.016727-52.689455v-499.898181a53.690182 53.690182 0 0 0-54.993455-52.596364z m0 552.494545H264.424727V262.097455h495.080728v499.80509z" fill="rgb(226, 228, 228)" p-id="28555"></path><path d="M436.386909 360.727273v302.498909L625.524364 512 436.386909 360.727273z" fill="rgb(226, 228, 228)" p-id="28556"></path></svg>
                          <span class="uf-views">{{formatViews(video.views)}}</span>
                          <svg t="1743757905181" class="uf-barrage-icon" viewBox="0 0 1303 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29556" width="200" height="200"><path d="M110.964364 1023.720727c-60.509091 0-109.847273-48.872727-109.847273-108.916363V109.009455C1.117091 48.965818 50.455273 0 110.964364 0h1081.902545c60.509091 0 109.847273 48.872727 109.847273 108.916364v805.794909c0 60.043636-49.338182 108.916364-109.847273 108.916363H110.964364z m0-927.650909c-6.609455 0-12.101818 5.771636-12.101819 12.939637v805.794909c0 7.168 5.492364 13.032727 12.101819 13.032727h1081.902545c6.609455 0 12.101818-5.864727 12.101818-13.032727V109.009455c0-7.168-5.492364-13.032727-12.101818-13.032728H110.964364z" fill="rgb(226, 228, 228)" p-id="29557"></path><path d="M1003.054545 520.098909a51.572364 51.572364 0 0 1-50.26909 52.689455h-80.058182a51.572364 51.572364 0 0 1-50.269091-52.689455c0-29.137455 22.434909-52.782545 50.269091-52.782545h80.058182c27.834182 0 50.269091 23.645091 50.26909 52.782545zM1096.610909 270.149818c0 28.858182-22.341818 52.410182-49.989818 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273h814.08c27.648 0 50.082909 23.458909 50.082909 52.503273zM768.744727 520.098909c0 28.858182-22.341818 52.410182-50.082909 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273H718.661818c27.648 0 50.082909 23.458909 50.082909 52.503273zM1096.610909 735.976727c0 29.044364-22.341818 52.503273-49.989818 52.503273H560.500364a51.293091 51.293091 0 0 1-50.082909-52.503273c0-28.951273 22.341818-52.410182 50.082909-52.410182h486.120727c27.648 0 50.082909 23.458909 50.082909 52.410182z" fill="rgb(226, 228, 228)" p-id="29558"></path></svg>
                          <span class="uf-barrage">{{video.barrages}}</span>
                          <span class="uf-f-time">{{formatVideoDuration(video.time)}}</span>
          <div class="s-title" @click="playVideo(video.id)" v-html="video.title"></div>
          <span class="uf-vvtime">{{ formatDate(video.createdTime) }}</span>
    </div>
  </el-col>
  <el-col :span="1">
    <!-- 空列，用于保持布局一致 -->
     <div class="s-botton-padding"></div>
  </el-col>
</el-row>
  </div>
  <el-empty v-else description="啊哦！你还没有发布视频哦~" />
    </div>
</template>

<style scoped>
.uf-upload-menu{
  margin-top: 20px;
  margin-left: 125px;
 width: 160px;
}

/* 修改选中项背景色和文字颜色 */
:deep(.el-menu-item.is-active) {
  background-color: rgb(0, 174, 236) !important; /* 自定义背景色 */
  color: #ffffff !important; /* 自定义文字颜色 */
  border-radius: 10px;
}

.uf-upload-content{
  width: 1260px;
  position: absolute;
  top: 230px;
  left: 290px;
}

.userInfoIndex-video-title{
  font-size: 24px;
  font-weight: 600;
  margin-left: 115px;
  position: relative;
  top: 10px;
}

.userInfoIndex-video-count{
  font-size: 16px;
  color: rgb(99, 104, 111);
  font-weight: 500;
  position: relative;
  top: -2px;
}

.uii-segmented{
  position: relative;
  top: 8px;
  margin-left: 30px;
}

.userInfoIndex-video-more{
  margin-left: 945px;
  margin-top: 15px;
}

.userInfoIndex-favorites-title{
  font-size: 24px;
  font-weight: 600;
  margin-left: 115px;
  position: relative;
  top: 10px;
}

.userInfoIndex-favorites-count{
  font-size: 16px;
  color: rgb(99, 104, 111);
  font-weight: 500;
  position: relative;
  top: -2px;
}

.userInfoIndex-favorites-more{
  margin-left: 1190px;
  margin-top: 15px;
}


.uf-upload-video-row{
  margin-bottom: -25px;
  display: flex;
}

.uf-video-col {
  margin-bottom: 20px;
  display: flex;
  margin-right: -25px;
  flex-direction: column;
  height: 240px;
  width: 300px;
}

/* 保持原有样式基础上添加 */
/* .s-botton-video {
  aspect-ratio: 16/9; 
} */

.uf-video{
  margin-top: 30px;
}

.s-video,.s-botton-video {
  background-color: beige;
  height: 220px;
}

/* 添加新样式 */
.s-video, .s-botton-video {
  flex-direction: column; /* 垂直布局 */
}
/* 视频区块样式 */
.uf-botton-video {
  height: 120px;
  width: 212px;
  background: #000;
  border-radius: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 10px 0;
  color: var(--el-color-primary);
  transition: transform 0.3s;
}

.s-thumbnail-img {
  position: absolute;
  border: none;
  border-radius: 8px;
  margin-left: 0;
  width: 212px;
  height: 120px;
  object-fit: cover; /* 裁剪图片以适应容器 */
  z-index: 0; 
}

.s-thumbnail-img-1 {
  position: absolute;
  border: none;
  border-radius: 8px;
  margin-left: 0;
  width: 246px;
  height: 150px;
  object-fit: cover; /* 裁剪图片以适应容器 */
  z-index: 0; 
}

.s-video-thumbnail {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 56.25%; /* 16:9 宽高比 */
  overflow: hidden;
}

/* .botton-video:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
} */

/* 间隔区块 */
.s-botton-padding {
  height: 10px;
}

.uf-upload-top-shadow-layer-1 {
  position: absolute;
  z-index: 0;
  margin-top: 70px;
  margin-bottom: 0px;
  border-radius: 8px;
  width: 212px;
  height: 25%;  /* 控制阴影覆盖范围 */
  background: linear-gradient(
    to top, 
    rgb(0, 0, 0) 0%,  /* 顶部阴影浓度 */
    rgba(0, 0, 0, 0) 85%   /* 渐变到透明 */
  );
  pointer-events: none; /* 防止遮挡交互 */
}

.uf-video-info {
  width: 100%;
  z-index: 1;
}
.uf-views{
  position: absolute;
  top: 107px;
  margin-left: 28px;
  font-size: 13px;
  font-weight: 500;
  color: rgb(226, 228, 228);
}

.uf-views-icon{
  width:25px ;
  height: 25px;
  position:absolute;
  top: 105px;
  margin-left: 5px;
  fill:rgb(255, 255, 255) !important;
}

.uf-barrage{
  position: absolute;
  top: 107px;
  margin-left: 95px;
  font-size: 13px;
  font-weight: 500;
  color: rgb(254, 255, 255) !important;
}

.uf-barrage-icon{
  width: 17px ;
  height: 17px;
  position:absolute;
  top: 110px;
  margin-left: 75px;
  fill:rgb(226, 228, 228) !important;
}

.uf-f-time{
  position: absolute;
  top: 107px;
  margin-left: 175px;
  font-size: 13px;
  font-weight: 400;
  color: rgb(226, 228, 228);
}

.s-title {
  margin-bottom: 20px;
  font-size: 15px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box; /* 兼容 WebKit 浏览器 */
  display: box;         /* 标准属性 */
  -webkit-line-clamp: 1; /* WebKit 私有属性 */
  line-clamp: 1;        /* 标准属性（未来兼容） */
  -webkit-box-orient: vertical;
  box-orient: vertical;  /* 标准属性 */
  color: rgb(24, 25, 28);
  width: 212px;
}

.uf-upload-video-col{
  margin-left: 40px;
}

.s-title:hover {
  color: rgb(0, 174, 236);
}

.s-meta {
  display: flex;
  font-size: 12px;
  color: #666;
}

.s-meta:hover {
  color: rgb(0, 174, 236);
}

/* 调整原有高度 */
.s-botton-video, .s-video {
  height: 220px; /* 恢复固定高度保持布局统一 */
}

.s-up-icon{
  margin-top: 3px;
  width: 15px;
  height: 15px;
  fill: rgb(148, 153, 181);
}

.s-vvauthor{
  margin-left: 5px !important;
  font-size: 13px;
}

.uf-vvtime{
  position: relative;
  top: -15px;
  margin-left: 0px!important;
  font-size: 13px;
  color: #666;
}

.userInfoIndex-video-divider{
  margin-top: 30px;
  margin-left: 115px;
  width: 1410px;
}

.uf-f-count{
  position: absolute;
  top: 107px;
  margin-left: 160px;
  font-size: 13px;
  font-weight: 400;
  color: rgb(226, 228, 228);
}


.uf-ctime{
  position: relative;
  top: -15px;
  margin-left: 0px!important;
  font-size: 13px;
  color: #666;
}
</style>
