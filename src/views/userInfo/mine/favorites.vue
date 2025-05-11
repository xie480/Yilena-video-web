<script lang="ts" setup>
import { ref,onMounted,computed,watch } from 'vue'
import { useRouter } from 'vue-router';
import { useTransition } from '@vueuse/core'
import { getUserInfoVideoList } from '../../../api/userInfo';
import CryptoJS from 'crypto-js'
import { ElMessage,ElMessageBox } from 'element-plus';
import { findAllVideos,getUserInfoByUsername,getHotSearch } from '../../../api';
import { getUserCollection,getVideoYesByUserId,getAllCoins,getAllComments,getAllFavorites,getAllLikes,getAllShares,getAllViews,getBarrages } from '../../../api/upload';
import { getUserFavoritesList } from '../../../api/video';
import { notSubscribeOhterFavoritesFolder,isCurrentFavoritesFolderSubscribe,subscribeOhterFavoritesFolder,getOtherFavoritesFolder,addFavoritesFolder,updateFavoritesFolder,getFavoritesFolderVideos,getSubscribeFavoritesFolder,deleteCollection,updateCollection,addCollection,getUserCoinsVideosList,getCollectionVideos } from '../../../api/userInfo';
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

const searchTerm = ref({
  id: '',
  title: '',
  page: 1,
  pageSize: 40
});

const searchTotal = ref(0); // 搜索结果总数

// 页码变化处理
const handlePageChange = (newPage) => {
  searchTerm.value.page = newPage;  // 更新当前页码
};



const currentFavoritesFolderId = ref('');

const currentFavoritesFolder = ref();
const currentFavoritesFolderCopy = ref('');
const newFavoritesFolder = ref('');

const videos = ref([]);
const videosCount = ref(0);

const getCurrentFavoritesFolderVideos = async () => {
  searchTerm.value.id = currentFavoritesFolderId.value;
  const result = await getFavoritesFolderVideos(searchTerm.value.id,searchTerm.value.title, searchTerm.value.page, searchTerm.value.pageSize,currentUserId.value);
  if (result.code === 1) {
    const data = result.data.rows;
    console.log(result.data);
    videosCount.value = result.data.total;
    if(data != null) {
     videos.value = chunkArray(data, 5);
     console.log(videos.value);
    }
  }else{
    ElMessage.error(result.msg);
  }
}

const isSubscribe = ref(false);

const getIsSubscribe = async() => {
  const result = await isCurrentFavoritesFolderSubscribe(currentUserId.value,currentFavoritesFolderId.value);
  if(result.code === 1){
    if(result.data != null){
      isSubscribe.value = true;
    } else{
      isSubscribe.value = false;
    }
  }
}

const subscribe = async() => {
  const result = await subscribeOhterFavoritesFolder(currentFavoritesFolderId.value);
  if(result.code === 1){
    ElMessage.success('订阅成功');
    isSubscribe.value = true;
    await getCurrentUserSubscribeFavoritesFolder(); 
  }else{
    ElMessage.error(result.msg);
  }
}

const notSubscribe = async() => {
  const result = await notSubscribeOhterFavoritesFolder(currentFavoritesFolderId.value);
  if(result.code === 1){
    ElMessage.info('已取消订阅');
    isSubscribe.value = false;
    await getCurrentUserSubscribeFavoritesFolder(); 
  }else{
    ElMessage.error(result.msg);
  }
}

const isCurrentSubscribeIndex = ref(false);

const handleSelect = async(key, keyPath) => {
  console.log(key, keyPath);
  currentFavoritesFolderId.value = key;
  for(let i = 0; i < userFavoritesFolders.value.length; i++) {
   if(userFavoritesFolders.value[i].id === key) {
    currentFavoritesFolder.value = userFavoritesFolders.value[i];
    currentFavoritesFolderCopy.value = userFavoritesFolders.value[i];
    isCurrentSubscribeIndex.value = false;
    console.log('当前收藏夹信息：',currentFavoritesFolder.value) 
   } 
  }

  for(let i = 0; i < userSubscribeFavoritesFolders.value.length; i++) {
   if(userSubscribeFavoritesFolders.value[i].id === key) {
    currentFavoritesFolder.value = userSubscribeFavoritesFolders.value[i];
    currentFavoritesFolderCopy.value = userSubscribeFavoritesFolders.value[i];
    isCurrentSubscribeIndex.value = true;
    console.log('当前收藏夹信息：',currentFavoritesFolder.value) 
   } 
  }

  await getCurrentFavoritesFolderVideos();
  if(currentFavoritesFolder.value.visibility === 1){
      visibility.value = '1';	
    }else{
      visibility.value = '0';
    }
  if(currentFavoritesFolder.value.isCoverAuto === 1){
      isAuto.value = '1';
    }else{
      isAuto.value = '0';
    }
    getIsSubscribe();
}

const userFavoritesFolders = ref([]);
const userSubscribeFavoritesFolders = ref([]);

const currentUserId = ref('');

const getCurrentUserId = async () => {
  const pathArray = router.currentRoute.value.path.split('/');
  console.log(pathArray);
  currentUserId.value = pathArray[2];
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

const chunkArray = (arr, size) => {
  const chunks = []
  for (let i = 0; i < arr.length; i += size) {
    chunks.push(arr.slice(i, i + size))
  }
  return chunks
}

const getFavoritesFolders = async () => {
  const result = await getUserFavoritesList(); 
  if (result.code === 1) {
    userFavoritesFolders.value = result.data;
  }else{
    ElMessage.error(result.msg);
  }
}

const getCurrentUserSubscribeFavoritesFolder = async () => {
  const result = await getSubscribeFavoritesFolder(currentUserId.value);
  if (result.code === 1) {
    userSubscribeFavoritesFolders.value = result.data;
    console.log("订阅的收藏夹：",userSubscribeFavoritesFolders.value);
  }else{
    ElMessage.error(result.msg);
  }
}
const isLoading = ref(true);

const manageFavoritesFolderDialogVisible = ref(false);
const addFavoritesFolderDialogVisible = ref(false);

const visibility = ref('1');
const isAuto = ref('1');

// 图片上传成功后触发
const handleAvatarSuccessNew = (response,uploadFile) => {
  newFavoritesFolder.value.coverUrl = response.data;
  ElMessage.success('上传成功！')
}

// 图片上传成功后触发
const handleAvatarSuccess = (response,uploadFile) => {
  currentFavoritesFolderCopy.value.coverUrl = response.data;
  ElMessage.success('上传成功！')
}
// 文件上传之前触发
const beforeAvatarUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('只支持上传图片')
    return false
  } else if (rawFile.size / 1024 / 1024 > 10) {
    ElMessage.error('只能上传10M以内图片')
    return false
  }
  ElMessage.success('开始上传，请等待上传成功提示！')
  return true
}

const token = ref('');
const getToken = () => {
  let loginUser = JSON.parse(localStorage.getItem('loginUser'))
  if (loginUser) {
    token.value = loginUser.token;
  }else{
    ElMessage.error('请先登录！')
  }
}

const handleDialogConfirm = async() => {
  currentFavoritesFolderCopy.value.visibility = visibility.value;
  currentFavoritesFolderCopy.value.isCoverAuto = isAuto.value;
  if(currentFavoritesFolderCopy.value.isCoverAuto == '1'){
    currentFavoritesFolderCopy.value.coverUrl = '';
  }
  const result = await updateFavoritesFolder(currentFavoritesFolderCopy.value);
  if(result.code === 1){
    ElMessage.success('修改成功');
    manageFavoritesFolderDialogVisible.value = false;
    await getFavoritesFolders(); 
    if(currentFavoritesFolderCopy.value.isCoverAuto == '1'){
    window.location.reload();
  }

  }else{
    ElMessage.error(result.msg);
  }
}

const handleDialogConfirmNew = async() => {
  const result = await addFavoritesFolder(newFavoritesFolder.value);
  if(result.code === 1){
    ElMessage.success('创建成功');
    addFavoritesFolderDialogVisible.value = false;
    await getFavoritesFolders(); 
  }else{
    ElMessage.error(result.msg);
  }
}

const openNewDiglog = () => {
  console.log('打开新的收藏夹')
  addFavoritesFolderDialogVisible.value = true;
  newFavoritesFolder.value = {title:'',coverUrl:'',visibility:'1',isCoverAuto:'0'}; 
}


onMounted(async() => {
  await getCurrentUserId();
  await getFavoritesFolders();
  await getCurrentUserSubscribeFavoritesFolder();
  await handleSelect(userFavoritesFolders.value[0].id, '');
  isLoading.value = false;
  getToken();
})
</script>

<template>
<div v-if="isLoading" class="loading-indicator">
    <!-- From Uiverse.io by Nawsome --> 

  <section class="loader1">

<div class="slider" style="--i:0">
</div>
<div class="slider" style="--i:1">
</div>
<div class="slider" style="--i:2">
</div>
<div class="slider" style="--i:3">
</div>
<div class="slider" style="--i:4">
</div>
</section>
</div>

<div v-else>
  <el-button text class="uf-favorites-add" size="large" @click="openNewDiglog"><el-icon class="el-icon-folder-add"><FolderAdd /></el-icon>&nbsp;&nbsp;创建新的收藏夹</el-button>
  <div class="userInfo-favorites-menu">
    <el-menu
        :default-active="userFavoritesFolders[0]?.id || ''"
        class="el-menu-vertical-demo"
        @open="handleOpen"
        @close="handleClose"
        @select="handleSelect"
      >
      <el-sub-menu index="1">
          <template #title>
            <span>我创建的收藏夹</span>
          </template>
          <template v-for="(item, index) in userFavoritesFolders">
          <el-menu-item-group>
            <el-menu-item :index="item.id">
              <div class="userInfo-menu-title">
              <el-icon><Folder /></el-icon>
              <span slot="title" class="userInfo-menu-title-text">{{ item.title }}</span>
            </div>
            <span class="userInfo-menu-video-count">{{ item.videoCount }}</span>
            </el-menu-item>
          </el-menu-item-group>
        </template>
        </el-sub-menu>
        <el-sub-menu index="2">
          <template #title>
            <span>我订阅的收藏夹</span>
          </template>
          <template v-if="userSubscribeFavoritesFolders.length > 0" v-for="(item, index) in userSubscribeFavoritesFolders">
          <el-menu-item-group>
            <el-menu-item :index="item.id">
              <div class="userInfo-menu-title">
              <el-icon><Folder /></el-icon>
              <span slot="title" class="userInfo-menu-title-text">{{ item.title }}</span>
            </div>
            <span class="userInfo-menu-video-count">{{ item.videoCount }}</span>
            </el-menu-item>
          </el-menu-item-group>
        </template>
        <template v-else>
          <el-menu-item disabled>
            <div class="userInfo-menu-title">
              <span slot="title" class="userInfo-menu-title-text">暂无订阅</span>
            </div>
          </el-menu-item>
        </template>
        </el-sub-menu>
        <el-sub-menu index="3" disabled>
          <template #title>
            <span>其他收藏</span>
          </template>
        </el-sub-menu>
        <el-sub-menu index="4" disabled>
          <template #title>
            <span>稍后再看</span>
          </template>
        </el-sub-menu>
      </el-menu>
  </div>
  <div class="userInfo-favorites-content">
    <div class="userInfo-favorites-content-top">
    <div v-if="currentFavoritesFolder && currentFavoritesFolder.coverUrl !== 'empty'">
  <img class="userInfo-favorites-content-top-cover" :src="currentFavoritesFolder.coverUrl" alt="封面">
</div>
<div v-else>
  <img referrerpolicy="no-referrer" class="userInfo-favorites-content-top-cover" src="YilenaPic/25/04/23-65bccca9dfd74027983df61a5b26f392.jpg" alt="封面">
</div>
<span class="userInfo-favorites-content-top-title">{{ currentFavoritesFolder.title }}</span>
  <div class="userInfo-favorites-content-top-info">
    <span class="userInfo-favorites-content-top-info-visibility" v-if="currentFavoritesFolder.visibility == 1">公开</span>
    <span class="userInfo-favorites-content-top-info-visibility" v-else>私密</span>
    <span class="userInfo-favorites-content-top-info-videoCount">视频数：{{ currentFavoritesFolder.videoCount }}</span>
    <span class="userInfo-favorites-content-top-info-clickCount">播放量{{ currentFavoritesFolder.clicks }}</span>
    <el-button v-if="!isCurrentSubscribeIndex" class="userInfo-favorites-content-top-manage-btn" type="primary" @click="manageFavoritesFolderDialogVisible = true">修改此收藏夹</el-button>
    <div v-else class="userInfo-favorites-content-top-manage-btn-container">
    <el-button class="userInfo-favorites-content-top-manage-btn" type="primary" @click="subscribe" v-if="currentFavoritesFolder.isDefault" disabled>无法订阅默认收藏夹</el-button>
    <div v-else class="userInfo-favorites-content-top-manage-btn-div">
    <el-button class="userInfo-favorites-content-top-manage-btn" type="primary" @click="subscribe" v-if="!isSubscribe">订阅此收藏夹</el-button>
    <el-button class="userInfo-favorites-content-top-manage-btn" type="delete" @click="notSubscribe" v-else>取消订阅此收藏夹</el-button>
  </div>
</div>
  </div>
</div>
  <el-divider class="userInfo-favorites-content-top-divider"/>
  <div class="uf-favorites-content">
      <div class="uf-video"  v-if="videosCount > 0">
    <el-row v-for="(group, index) in videos" 
    :key="index" class="uf-upload-video-row">
    <el-col 
        v-for="video in group" 
        :key="video.id"
        :span="4" 
        class="uf-upload-video-col"
      >
      <div class="uf-botton-video" :id="video.id"  @click="playVideo(video.id)">
        <img :src="video.coverUrl" class="s-thumbnail-img" @click="playVideo(video.id)"/>
      <div class="uf-collection-top-shadow-layer-1"></div>
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
<!-- 分页 -->
<div class="uf-page">
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
  </div>
  <el-empty v-else description="空空如也……" />
    </div>
  </div>
</div>

<el-dialog
  v-model="manageFavoritesFolderDialogVisible"
  title="管理收藏夹"
  width="500"
  align-center
>
  <el-form :model="currentFavoritesFolderCopy" label-width="100px">
    <el-form-item label="收藏夹名称">
      <el-input v-model="currentFavoritesFolderCopy.title" placeholder="请输入收藏夹名称" />
    </el-form-item>
    <el-form-item label="自定义封面">
      <el-radio-group v-model="isAuto" :key="isAuto">
        <el-radio value="0">是</el-radio>
      <el-radio value="1">否</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="封面" v-if="isAuto == '0'">
      <el-upload
                class="avatar-uploader"
                action="/api/upload"
                :headers="{'token':token}"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                >
      <el-button class="up-coverUrl-btn"><el-icon><UploadFilled /></el-icon></el-button>
      </el-upload>
      <el-radio-group v-model="currentFavoritesFolderCopy.coverUrl" :key="currentFavoritesFolder.coverUrl">

        <img :src="currentFavoritesFolderCopy.coverUrl" alt="封面" class="cc-coverUrl">
      </el-radio-group>
    </el-form-item>
    <el-form-item label="可见权限">
      <el-radio-group v-model="visibility" :key="visibility">
        <el-radio value="1">公开</el-radio>
      <el-radio value="0">私密</el-radio>
      </el-radio-group>
    </el-form-item>
  </el-form>
  <div slot="footer" class="dialog-footer">
    <el-button @click="manageFavoritesFolderDialogVisible = false">取消</el-button>
    <el-button type="primary" @click="handleDialogConfirm">确定</el-button>
  </div>
  </el-dialog>

  <el-dialog
  v-model="addFavoritesFolderDialogVisible"
  title="创建收藏夹"
  width="500"
  align-center
>
  <el-form :model="newFavoritesFolder" label-width="100px">
    <el-form-item label="收藏夹名称">
      <el-input v-model="newFavoritesFolder.title" placeholder="请输入收藏夹名称" />
    </el-form-item>
    <el-form-item label="自定义封面">
      <el-radio-group v-model="newFavoritesFolder.isCoverAuto" :key="newFavoritesFolder.isCoverAuto">
        <el-radio value="1">是</el-radio>
      <el-radio value="0">否</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="封面" v-if="newFavoritesFolder.isCoverAuto == '1'">
      <el-upload
                class="avatar-uploader"
                action="/api/upload"
                :headers="{'token':token}"
                :show-file-list="false"
                :on-success="handleAvatarSuccessNew"
                :before-upload="beforeAvatarUpload"
                >
      <el-button class="up-coverUrl-btn"><el-icon><UploadFilled /></el-icon></el-button>
      </el-upload>
      <el-radio-group v-model="newFavoritesFolder.coverUrl" :key="currentFavoritesFolder.coverUrl">

        <img v-if="newFavoritesFolder.coverUrl != ''" :src="newFavoritesFolder.coverUrl" alt="封面" class="cc-coverUrl">
      </el-radio-group>
    </el-form-item>
    <el-form-item label="可见权限">
      <el-radio-group v-model="newFavoritesFolder.visibility" :key="newFavoritesFolder.visibility">
        <el-radio value="1">公开</el-radio>
      <el-radio value="0">私密</el-radio>
      </el-radio-group>
    </el-form-item>
  </el-form>
  <div slot="footer" class="dialog-footer">
    <el-button @click="addFavoritesFolderDialogVisible = false">取消</el-button>
    <el-button type="primary" @click="handleDialogConfirmNew">确定</el-button>
  </div>
  </el-dialog>
</template>

<style scoped>
.userInfo-favorites-content-top-manage-btn-container{
  position: absolute;
}

.dialog-footer{
  margin-left: 335px;
}

.cc-coverUrl{
  width: 212px;
  height: 120px;
  margin-left: 20px;
}

.userInfo-favorites-content-top-manage-btn{
  position: absolute;
  top: 45px;
}

.uf-page{
  margin-top: 60px;
  margin-left: 565px;
}

.uf-favorites-content{
  margin-top: 0px;
  margin-left: -30px;
}

.userInfo-favorites-content-top-divider{
  margin-top: 0px;
  margin-left: 15px;
  width: 1188px
}

.userInfo-favorites-content-top-info{
  display: flex;
  color: rgb(148, 153, 160);
  font-size: 13px;
  position: relative;
  top: -110px;
  left: 205px;
}

.uf-upload-content{
  width: 1260px;
  position: absolute;
  top: 280px;
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

.uf-collection-top-shadow-layer-1 {
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


.userInfo-favorites-content-top-info-clickCount{
  margin-left: 10px;
}

.userInfo-favorites-content-top-info-videoCount{
  margin-left: 10px;
}

.userInfo-favorites-content-top-title{
  position: relative;
  font-size: 20px;
  color: rgb(24, 25, 28);
  top: -115px;
  left: 205px;
}

.uf-favorites-add{
  margin-left: 135px;
  margin-top: 10px;
  font-size: 15px;
}

.el-icon-folder-add{
  font-size: 18px;
  color: rgb(0, 174, 236);
}

.userInfo-favorites-content-top-cover{
  margin-left: 15px;
  margin-top: 25px;
  width: 180px;
  height: 105px;
  border-radius: 10px;
}
/* From Uiverse.io by Nawsome */ 
.loader1 {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: row;
}

.slider {
  overflow: hidden;
  background-color: white;
  margin: 0 15px;
  height: 80px;
  width: 20px;
  border-radius: 30px;
  box-shadow: 15px 15px 20px rgba(0, 0, 0, 0.1), -15px -15px 30px #fff,
    inset -5px -5px 10px rgba(0, 0, 255, 0.1),
    inset 5px 5px 10px rgba(0, 0, 0, 0.1);
  position: relative;
}

.slider::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  height: 20px;
  width: 20px;
  border-radius: 100%;
  box-shadow: inset 0px 0px 0px rgba(0, 0, 0, 0.3), 0px 420px 0 400px #2697f3,
    inset 0px 0px 0px rgba(0, 0, 0, 0.1);
  animation: animate_2 2.5s ease-in-out infinite;
  animation-delay: calc(-0.5s * var(--i));
}

@keyframes animate_2 {
  0% {
    transform: translateY(250px);
    filter: hue-rotate(0deg);
  }

  50% {
    transform: translateY(0);
  }

  100% {
    transform: translateY(250px);
    filter: hue-rotate(180deg);
  }
}

.loading-indicator{
  margin-top: 200px;
}

.userInfo-favorites-content-top{
  width: 1220px;
  height: 150px;
}

.userInfo-favorites-content{
  position: absolute;
  top: 260px;
  left: 340px;
  width: 1220px;
}

.userInfo-menu-video-count{
  font-size: 12px;
  position: absolute;
  top:2px;
  left:175px;
}

.userInfo-menu-title-text{
  position: relative;
  top: 2px;
  font-size: 12px;
}

.userInfo-menu-title{
  margin-left: -25px;
}

.userInfo-favorites-menu{
  width: 220px;
  margin-left: 110px;
}

/* 修改选中项背景色和文字颜色 */
:deep(.el-menu-item.is-active) {
  background-color: rgb(0, 174, 236) !important; /* 自定义背景色 */
  color: #ffffff !important; /* 自定义文字颜色 */
  border-radius: 10px;
}
</style>
