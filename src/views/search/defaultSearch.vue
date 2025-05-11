<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import CryptoJS from 'crypto-js'
import { getVideoSearchList } from '../../api/search';
import { ElMessage,ElMessageBox } from 'element-plus';

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

const router = useRouter();

const playVideo = (videoId) => {
      router.push(`/video/${videoId}`).then(() => {
        window.location.reload();
      });
    }

const searchTerm = ref({
  id: '',
  title: '',
  description: '',
  tags: [],
  username: '',
  beginDate: '',
  endDate: '',
  beginTime: '',
  endTime: '',
  sortType: '1',
  page: 1,
  pageSize: 30
}); // 搜索关键词

const searchTotal = ref(0); // 搜索结果总数

const chunkArray = (arr, size) => {
  const chunks = []
  for (let i = 0; i < arr.length; i += size) {
    chunks.push(arr.slice(i, i + size))
  }
  return chunks
}

// 页码变化处理
const handlePageChange = (newPage) => {
  searchTerm.value.page = newPage;  // 更新当前页码
  getCurrentSearchVideos();         // 重新加载数据
};

    // AES解密
    const decryptSearchText = (cipherText) => {
  const decoded = atob(cipherText.replace(/-/g, '+').replace(/_/g, '/'))
  return CryptoJS.AES.decrypt(decoded, 'yilena-key').toString(CryptoJS.enc.Utf8)
}

const searchText = ref('')

let param1 = '';
let param2 = '';
let param3 = '';

const getSerachInfo = () => {
 // 拆分路由路径，假设加密文本在路径第三段
 const pathSegments = router.currentRoute.value.path.split('/');
  
  // 提取加密文本（注意索引位置）
  const encryptedText = pathSegments[2] || ''; // 根据实际路由结构调整索引
  
  if (!encryptedText) return '';

  try {
    // 执行解密并返回结果
    searchText.value =  decryptSearchText(encryptedText);
  } catch (error) {
    console.error('解密失败:', error);
    searchText.value =   '';
  }

  param1 = router.currentRoute.value.query.sortType;
  param2 = router.currentRoute.value.query.dateRange;
  param3 = router.currentRoute.value.query.timeSize;
}

const getCurrentSearchVideos = async () => {
  searchTerm.value.title = searchText.value;
  searchTerm.value.description = searchText.value;
  searchTerm.value.tags = searchText.value;
  searchTerm.value.username = searchText.value;

  if(param1){
    searchTerm.value.sortType = param1;
  }

  const adjustedToday = new Date();
  const today = new Date(adjustedToday.getTime() + 8 * 60 * 60 * 1000); // 8小时的毫秒数
  const formatDate = date => date.toISOString().split('.')[0] // 日期格式化函数
  if(param2 === '1'){
    searchTerm.value.beginDate = '1999-01-01T00:00:00';
    searchTerm.value.endDate = formatDate(today);
  }else if(param2 === '2'){
    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);
    searchTerm.value.beginDate = formatDate(yesterday);
    searchTerm.value.endDate = formatDate(today);
  }else if(param2 === '3'){
    const lastWeek = new Date(today);
    lastWeek.setDate(today.getDate() - 7);
    searchTerm.value.beginDate = formatDate(lastWeek);
    searchTerm.value.endDate = formatDate(today);
  }else if(param2 === '4'){
    const halfYearAgo = new Date(today);
    halfYearAgo.setMonth(today.getMonth() - 6);
    searchTerm.value.beginDate = formatDate(halfYearAgo);
    searchTerm.value.endDate = formatDate(today);
  }

  if(param3 === '1'){
    searchTerm.value.beginTime = '0';
  }else if(param3 === '2'){
    searchTerm.value.endTime = '600';
  }else if(param3 === '3'){
    searchTerm.value.beginTime = '600';
    searchTerm.value.endTime = '1800';
  }else if(param3 === '4'){
    searchTerm.value.beginTime = '1800';
    searchTerm.value.endTime = '3600';
  }else if(param3 === '5'){
    searchTerm.value.beginTime = '3600';
  }

  console.log(searchTerm.value);
  const result = await getVideoSearchList(
    searchTerm.value.id,
    searchTerm.value.title,
    searchTerm.value.description,
    searchTerm.value.tags,
    searchTerm.value.username,
    searchTerm.value.beginDate,
    searchTerm.value.endDate,
    searchTerm.value.beginTime,
    searchTerm.value.endTime,
    searchTerm.value.sortType,
    searchTerm.value.page,
    searchTerm.value.pageSize
  )
  console.log(result)
  if (result.code === 1) {

    result.data.rows.forEach((item) => {
  if (searchText.value) {
    // 转义搜索词中的正则特殊字符（如.*+?等）
    const escapedSearch = searchText.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    // 构造捕获组正则，匹配原始文本中的实际内容
    const regex = new RegExp(`(${escapedSearch})`, 'gi'); 

    // 高亮替换逻辑（保留原始大小写）
    item.title = item.title.replace(regex, '<span style="color:rgb(242, 93, 142)">$1</span>');
    item.username = item.username.replace(regex, '<span style="color:rgb(242, 93, 142)">$1</span>');
  }
});
    // 将数据分块为每行5个
    videoGroups.value = chunkArray(result.data.rows, 5);
    searchTotal.value = result.data.total;
    if(searchTotal.value === 0){
      ElMessage.warning('喵？没有搜索到结果喵！');
    }
  } else {
    ElMessage.error('搜索失败')
  }
}

const toUserInfo = (id) => {
      router.push('/userInfo/' + id).then(() => {
        window.location.reload();
      })
    }

const videoGroups = ref([]); // 存储视频分组的数组

onMounted(async() => {
  await getSerachInfo();
  getCurrentSearchVideos();
});

</script>

<template>
  <!-- 搜索视频 -->
   <div class="search-video">
    <el-row v-for="(group, index) in videoGroups" 
    :key="index" class="video-row">
    <el-col 
        v-for="video in group" 
        :key="video.id"
        :span="4" 
        class="video-col"
      >
      <div class="s-botton-video" :id="video.id"  @click="playVideo(video.id)">
        <img :src="video.coverUrl" class="s-thumbnail-img" @click="playVideo(video.id)"/>
      <div class="s-top-shadow-layer-1"></div>
      </div>
      <div class="s-video-info"> 
                          <svg t="1743757350808" class="s-views-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="28554" width="200" height="200"><path d="M759.528727 209.408H264.424727A53.853091 53.853091 0 0 0 209.454545 262.004364v499.898181a53.992727 53.992727 0 0 0 54.970182 52.689455h495.080728a53.829818 53.829818 0 0 0 55.016727-52.689455v-499.898181a53.690182 53.690182 0 0 0-54.993455-52.596364z m0 552.494545H264.424727V262.097455h495.080728v499.80509z" fill="rgb(226, 228, 228)" p-id="28555"></path><path d="M436.386909 360.727273v302.498909L625.524364 512 436.386909 360.727273z" fill="rgb(226, 228, 228)" p-id="28556"></path></svg>
                          <span class="s-views">{{formatViews(video.views)}}</span>
                          <svg t="1743757905181" class="s-barrage-icon" viewBox="0 0 1303 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29556" width="200" height="200"><path d="M110.964364 1023.720727c-60.509091 0-109.847273-48.872727-109.847273-108.916363V109.009455C1.117091 48.965818 50.455273 0 110.964364 0h1081.902545c60.509091 0 109.847273 48.872727 109.847273 108.916364v805.794909c0 60.043636-49.338182 108.916364-109.847273 108.916363H110.964364z m0-927.650909c-6.609455 0-12.101818 5.771636-12.101819 12.939637v805.794909c0 7.168 5.492364 13.032727 12.101819 13.032727h1081.902545c6.609455 0 12.101818-5.864727 12.101818-13.032727V109.009455c0-7.168-5.492364-13.032727-12.101818-13.032728H110.964364z" fill="rgb(226, 228, 228)" p-id="29557"></path><path d="M1003.054545 520.098909a51.572364 51.572364 0 0 1-50.26909 52.689455h-80.058182a51.572364 51.572364 0 0 1-50.269091-52.689455c0-29.137455 22.434909-52.782545 50.269091-52.782545h80.058182c27.834182 0 50.269091 23.645091 50.26909 52.782545zM1096.610909 270.149818c0 28.858182-22.341818 52.410182-49.989818 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273h814.08c27.648 0 50.082909 23.458909 50.082909 52.503273zM768.744727 520.098909c0 28.858182-22.341818 52.410182-50.082909 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273H718.661818c27.648 0 50.082909 23.458909 50.082909 52.503273zM1096.610909 735.976727c0 29.044364-22.341818 52.503273-49.989818 52.503273H560.500364a51.293091 51.293091 0 0 1-50.082909-52.503273c0-28.951273 22.341818-52.410182 50.082909-52.410182h486.120727c27.648 0 50.082909 23.458909 50.082909 52.410182z" fill="rgb(226, 228, 228)" p-id="29558"></path></svg>
                          <span class="s-barrage">{{video.barrages}}</span>
                          <span class="s-f-time">{{formatVideoDuration(video.time)}}</span>
          <div class="s-title" @click="playVideo(video.id)" v-html="video.title"></div>
        <div class="s-meta" @click="toUserInfo(video.userId)">
          <svg t="1744025410333" class="s-up-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="10955" width="200" height="200"><path d="M800 128H224C134.4 128 64 198.4 64 288v448c0 89.6 70.4 160 160 160h576c89.6 0 160-70.4 160-160V288c0-89.6-70.4-160-160-160z m96 608c0 54.4-41.6 96-96 96H224c-54.4 0-96-41.6-96-96V288c0-54.4 41.6-96 96-96h576c54.4 0 96 41.6 96 96v448z" p-id="10956"></path><path d="M419.2 544c0 51.2-3.2 108.8-83.2 108.8S252.8 595.2 252.8 544v-217.6H192v243.2c0 96 51.2 140.8 140.8 140.8 89.6 0 147.2-48 147.2-144v-240h-60.8V544zM710.4 326.4h-156.8V704h60.8v-147.2h96c102.4 0 121.6-67.2 121.6-115.2 0-44.8-19.2-115.2-121.6-115.2z m-3.2 179.2h-92.8V384h92.8c32 0 60.8 12.8 60.8 60.8 0 44.8-32 60.8-60.8 60.8z" p-id="10957"></path></svg>
          <span class="s-vvauthor" v-html="video.username"></span>
          <span class="s-vvtime">·&nbsp;{{ formatDate(video.createdTime) }}</span>
        </div>
    </div>
  </el-col>
  <el-col :span="1">
    <!-- 空列，用于保持布局一致 -->
     <div class="s-botton-padding"></div>
  </el-col>
</el-row>
   </div>

   <!-- 分页 -->
   <div class="s-page">
    <el-pagination 
    background 
    layout="prev, pager, next" 
    :current-page="searchTerm.page"
    :page-size="searchTerm.pageSize"
    :total="searchTotal" 
    @current-change="handlePageChange"
    prev-text="上一页"
    next-text="下一页"/>
   </div>
</template>

<style scoped>
.video-row{
  margin-bottom: 20px;
  margin-left: 80px;
  display: flex;
}

.video-col {
  margin-bottom: 20px;
  display: flex;
  margin-right: 40px;
  flex-direction: column;
  height: 240px;
  width: 300px;
}

/* 保持原有样式基础上添加 */
.s-botton-video {
  width: 100%;
  aspect-ratio: 16/9; /* 保持16:9比例 */
}

.search-video{
  height: 95%;
}

.s-video,.s-botton-video {
  background-color: beige;
  height: 220px;
}

/* 添加新样式 */
.s-video, .s-botton-video {
  height: auto; /* 改为自适应高度 */
  flex-direction: column; /* 垂直布局 */
}
/* 视频区块样式 */
.s-botton-video,.s-video {
  height: 170px;
  width: 300px;
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
  width: 300px;
  height: 170px;
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

.s-top-shadow-layer-1 {
  position: absolute;
  z-index: 0;
  margin-top: 110px;
  margin-bottom: 0px;
  border-radius: 8px;
  width: 300px;
  height: 25%;  /* 控制阴影覆盖范围 */
  background: linear-gradient(
    to top, 
    rgb(0, 0, 0) 0%,  /* 顶部阴影浓度 */
    rgba(0, 0, 0, 0) 85%   /* 渐变到透明 */
  );
  pointer-events: none; /* 防止遮挡交互 */
}

.s-video-info {
  width: 100%;
  z-index: 1;
}
.s-views{
  position: absolute;
  margin-top: -36px;
  margin-left: 31px;
  font-size: 13px;
  font-weight: 500;
  color: rgb(226, 228, 228);
}

.s-views-icon{
  width:25px ;
  height: 25px;
  position:absolute;
  margin-top: -38px;
  margin-left: 5px;
  fill:rgb(255, 255, 255) !important;
}

.s-barrage{
  position: absolute;
  margin-top: -36px;
  margin-left: 94px;
  font-size: 13px;
  font-weight: 500;
  color: rgb(254, 255, 255) !important;
}

.s-barrage-icon{
  width: 17px ;
  height: 17px;
  position:absolute;
  margin-top: -34px;
  margin-left: 72px;
  fill:rgb(226, 228, 228) !important;
}

.s-f-time{
  position: absolute;
  margin-top: -34px;
  margin-left: 260px;
  font-size: 13px;
  font-weight: 500;
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

.s-vvtime{
  margin-left: 10px!important;
  font-size: 13px;
}

.s-page{
  margin-top: 80px;
  display: flex;
  justify-content: center;
  margin-bottom: 60px;
}
</style>
