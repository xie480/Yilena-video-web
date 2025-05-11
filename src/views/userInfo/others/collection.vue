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
import { deleteCollection,updateCollection,addCollection,getUserCoinsVideosList,getCollectionVideos } from '../../../api/userInfo';
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

const videoCollection = ref([{
  videoCount: 0,
}])

const getCurrentUserCollection = async () => {
  const result = await getUserCollection(currentUserId.value,currentUserInfo.value.id);
  if (result.code === 1) {
    videoCollection.value = result.data;
    videoCollection.value.forEach(item => {
      item.videoCount = item.videoIds.length;
    })
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

const currentUserInfo = ref({
      id: '',
      username: '',
      image: '',
      coins: '',
      followingCount: '',
      followersCount: '',
      postCount: '',
      status: '',
      reason: '',
      allViews: '',
      allLikes: ''
    });

    const getUserInfo = async () => {
      //获取登录用户名
      let loginUser = JSON.parse(localStorage.getItem('loginUser'))
      if (loginUser) {
        currentUserInfo.value.username = loginUser.username;
        const result = await getUserInfoByUsername(currentUserInfo.value.username);
        if(result.code === 1){
          currentUserInfo.value = result.data;
          currentUserInfo.value.followingCount = formatViews(currentUserInfo.value.followingCount);
          currentUserInfo.value.followersCount = formatViews(currentUserInfo.value.followersCount);
          currentUserInfo.value.postCount = formatViews(currentUserInfo.value.postCount);
          if(currentUserInfo.value.status === 0){
            localStorage.removeItem('loginUser');
            router.push('/login');
            ElMessage.error('您的账号已被封禁!\n原因：'+userInfo.value.reason)
            }
          }
      }else{
        ElMessage.error('请先登录！')
      }
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
  currentUserId.value = pathArray[3];
}

const selectedCollectinId = ref('');

const handleSelect = (key, keyPath) => {
  console.log(key, keyPath);
  selectedCollectinId.value = key;
  getCurrentUserVideo();
}

const isLoading = ref(true);
const getCurrentUserVideo = async () => {
  const result = await getCollectionVideos(currentUserId.value,selectedCollectinId.value);
  if (result.code === 1) {
    const data = result.data;
    videoList.value = chunkArray(data, 5);
    console.log(videoList.value);
    videoCount.value = data.length;
  }else{
    ElMessage.error(result.msg);
  }
}

const addCollectionDialogVisible = ref(false);
const manageCollectionDialogVisible = ref(false);

const collection = ref({
  id: '',
  title: '',
  visibility: ''
});

const isSubmitting = ref(false)

const addCollectionFormRef = ref(null);

// 提交处理
const handlePasswordSubmit = async () => {
  try {
    // 触发表单验证
    await addCollectionFormRef.value.validate();
    
    isSubmitting.value = true;
    
    const result = await addCollection(collection.value);

    if (result.code === 1) {
      ElMessage.success('合集创建成功');
      addCollectionDialogVisible.value = false;
      // 刷新合集列表
      await getCurrentUserCollection();
    } else {
      ElMessage.error(result.msg || '创建失败');
    }
  } catch (error) {
      ElMessage.warning('请完善表单信息');
  } finally {
    isSubmitting.value = false;
  }
}

const collectionTableData = ref([])

const formatDateToForm = (timestamp) => {
  const date = new Date(timestamp);
  
  // 补零函数
  const pad = n => n.toString().padStart(2, '0');
  
  return `
    ${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())}
    ${pad(date.getHours())}:${pad(date.getMinutes())}
  `.replace(/\s+/g, ' ').trim();
}

const openManageCollectionDialog = () => {
  collectionTableData.value = videoCollection.value;
  collectionTableData.value.forEach(item => {
    item.createdTime = formatDateToForm(item.createdTime);
  })
  manageCollectionDialogVisible.value = true;
}


// 关闭时重置表单
const handleDialogClose = () => {
  collection.value = {
    id: '',
    title: '',
    visibility: ''
  };
}

const collectionRules = ref({
  title: [
    { required: true, message: '请输入合集标题', trigger: 'blur' },
    { min: 2, max: 20, message: '标题长度2-20个字符', trigger: 'blur' }
  ],
  visibility: [
    { required: true, message: '请选择可见权限', trigger: 'change' }
  ]
});


const value = ref('')

const visibilityOptions = [
  {
    value: 1,
    label: '公开',
  },
  {
    value: 0,
    label: '私密',
  }
]

// 编辑操作处理
const handleEditAction = (row) => {
  if (row.isEditing) {
    // 保存逻辑
    submitChanges(row).then(() => {
      row.isEditing = false
      row.originalData = null
    })
  } else {
    // 进入编辑模式
    row.originalData = { ...row }
    row.isEditing = true
  }
}

// 数据提交方法
const submitChanges = async (row) => {
  try {
    const collection = {
    id: row.id,
    title: row.title,
    visibility: row.visibility
  }
   const result = await updateCollection(collection);
   if (result.code === 1) {
     ElMessage.success('合集修改成功');
   }
  } catch (error) {
    // 恢复原始数据
    Object.assign(row, row.originalData)
    ElMessage.error('保存失败')
  }
}

// 删除处理
const handleDelete = async (id) => {
  const result = await deleteCollection(id);
  if (result.code === 1) {
    ElMessage.success('合集删除成功');
    // 刷新合集列表
    await getCurrentUserCollection();
    manageCollectionDialogVisible.value = false;
  }else{
    ElMessage.error(result.msg);
  }
}

onMounted(async () => {
   await getCurrentUserId();
   await getUserInfo();
   await getCurrentUserCollection();
   isLoading.value = false;
   handleSelect(videoCollection.value[0].id, '');
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
  <div class="uf-upload-title">我的合集列表</div>
  <el-button class="uf-upload-title-btn1" @click="openManageCollectionDialog"><el-icon><Setting /></el-icon>&nbsp;&nbsp;管理合集</el-button>
  <el-button class="uf-upload-title-btn" @click="addCollectionDialogVisible = true"><el-icon><Plus /></el-icon>&nbsp;&nbsp;创建新的合集</el-button>
  <div class="uf-upload-menu">
   <el-menu
        v-if="videoCollection.length > 0"
        :default-active="videoCollection[0]?.id || ''"
        class="el-menu-vertical-demo"
        @open="handleOpen"
        @close="handleClose"
        @select="handleSelect"
      >
      <template v-for="item in videoCollection" :key="item.id">
        <el-menu-item :index="item.id">
          <span class="uf-collection-title-icon">
            <el-tooltip
        class="box-item"
        effect="dark"
        :content="item.title"
        placement="top"
        
      ><span class="uf-collection-title">{{item.title}}</span>
    </el-tooltip>
  </span>
            <div class="uf-collection-count">{{ item.videoCount }}
            <span v-if="item.visibility == 1" class="uf-visibility">公开</span>
           <span v-else class="uf-visibility">私密</span>
          </div>
        </el-menu-item>
      </template>
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
  </div>
  <el-empty v-else description="啊哦！你还没有发布视频哦~" />
    </div>
  </div>

  <el-dialog
  v-model="addCollectionDialogVisible"
  title="添加新的投稿合集"
  width="500"
  align-center
  @closed="handleDialogClose"
>
  <el-form
    ref="addCollectionFormRef"
    :model="collection"
    :rules="collectionRules"
    label-position="left"
    label-width="120px"
    status-icon
  >
    <el-form-item label="标题" prop="title">
      <el-input
        v-model="collection.title"
        type="text"
        placeholder="请输入合集标题"
      />
    </el-form-item>

    <el-form-item label="可见权限" prop="visibility">
  <el-radio-group v-model="collection.visibility">
    <el-radio label="1">公开</el-radio>
    <el-radio label="0">私密</el-radio>
  </el-radio-group>
</el-form-item>
  </el-form>
  <template #footer>
    <el-button @click="addCollectionDialogVisible = false">取消</el-button>
    <el-button
      type="primary"
      :loading="isSubmitting"
      @click="handlePasswordSubmit"
    >
      确认修改
    </el-button>
  </template>
  </el-dialog>

  <el-dialog
  v-model="manageCollectionDialogVisible"
  title="管理合集"
  width="500"
  align-center
  @closed="handleDialogClose"
>
<el-table :data="collectionTableData" style="width: 100%">
    <!-- 标题列 -->
    <el-table-column prop="title" label="标题">
      <template #default="scope">
        <el-input 
          v-model="scope.row.title" 
          :disabled="!scope.row.isEditing"
          placeholder="请输入标题"
        />
      </template>
    </el-table-column>

    <!-- 可见权限列 -->
    <el-table-column prop="visibility" label="可见权限">
      <template #default="scope">
        <el-select
          v-model="scope.row.visibility"
          :disabled="!scope.row.isEditing"
          placeholder="请选择"
          style="width: 120px"
        >
          <el-option
            v-for="item in visibilityOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </template>
    </el-table-column>

    <!-- 操作列 -->
    <el-table-column label="操作" width="150">
      <template #default="scope">
        <div class="action-buttons">
          <el-button
            :type="scope.row.isEditing ? 'success' : 'primary'"
            size="small"
            @click="handleEditAction(scope.row)"
          >
            {{ scope.row.isEditing ? '保存' : '编辑' }}
          </el-button>
          
          <el-popconfirm
            title="确认删除此项？"
            @confirm="handleDelete(scope.row.id)"
          >
            <template #reference>
              <el-button type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </div>
      </template>
    </el-table-column>
  </el-table>
  </el-dialog>
</template>

<style scoped>
.uf-collection-count{
  position: absolute;
  left: 100px;
}

.uf-collection-title-icon{
  margin-left: -13px;
}

.uf-collection-title {
  /* 强制文本不换行 */
  white-space: nowrap;
  /* 隐藏溢出内容 */
  overflow: hidden;
  /* 溢出时显示省略号 */
  text-overflow: ellipsis;
  /* 最大宽度（按需调整这个值） */
  max-width: 90px;
  /* 如果父容器是 flex 布局，需要加这个属性 */
  display: inline-block;
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


.uf-upload-title-btn{
position: absolute;
top: 255px;
left: 1385px;
}

.uf-upload-title-btn1{
position: absolute;
left: 1275px;
top: 255px;
}

.uf-upload-title{
  margin-top: 10px;
  font-size: 22px;
  font-weight: 600;
  margin-left: 130px;
}

.uf-visibility{
  font-size: 12px;
}

.uf-upload-menu{
  margin-top: 30px;
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
