<script lang="ts" setup>
import { ref,onMounted,computed,watch,reactive } from 'vue'
import { useRouter } from 'vue-router';
import { useTransition } from '@vueuse/core'
import CryptoJS from 'crypto-js'
import { ElMessage,ElMessageBox } from 'element-plus';
import { UploadFilled } from '@element-plus/icons-vue'
import { getVideo } from '../../api/video';
import { findAllVideos,getUserInfoByUsername } from '../../api';
import { getVideoPendings,updateVideoYesTo,updateVideoPendingTo,getUserCollection,deleteVideoById,getVideoYesByUserId,getVideoPendingById,updateVideoPending,deleteVideoPending,reuploadVideoPeing } from '../../api/upload';
// 视频播放组件
import "vue3-video-play/dist/style.css";
	import {videoPlay} from "vue3-video-play";

const value = ref('1')

const userInfo = ref({
      id: '',
      username: '',
      image: '',
      coins: '',
      followingCount: '',
      followersCount: '',
      postCount: '',
      status: '',
      reason: ''
    });

    const token = ref('');

    const getUserInfo = async () => {
      //获取登录用户名
      let loginUser = JSON.parse(localStorage.getItem('loginUser'))
      if (loginUser) {
        userInfo.value.username = loginUser.username;
        token.value = loginUser.token;
        const result = await getUserInfoByUsername(userInfo.value.username);
        if(result.code === 1){
          userInfo.value = result.data;
          console.log(userInfo.value)
          if(userInfo.value.status === 0){
            localStorage.removeItem('loginUser');
            router.push('/login');
            ElMessage.error('您的账号已被封禁!\n原因：'+userInfo.value.reason)
            }
          }
      }else{
        ElMessage.error('请先登录！')
      }
    }

const options = [
  {
    value: '1',
    label: '投稿时间排序',
  },
  {
    value: '2',
    label: '播放数排序',
  },
  {
    value: '3',
    label: '收藏数排序',
  },
  {
    value: '4',
    label: '弹幕数排序',
  },
  {
    value: '5',
    label: '评论数排序',
  },
]

const waitTableData = ref([]);
const waitTavleDataSize = ref(0);

const YesTableData = ref([]);
const YesTavleDataSize = ref(0);

const NoTableData = ref([]);
const NoTavleDataSize = ref(0);

const getCurrentVideoPendingsWait = async() =>{
  const result = await getVideoPendings(userInfo.value.id,3);
  if(result.code === 1){
    waitTableData.value = result.data;
    for(let i = 0;i<waitTableData.value.length;i++){
      waitTableData.value[i].createdTime = formatDtaTime(waitTableData.value[i].createdTime);
      waitTableData.value[i].updatedTime = formatDtaTime(waitTableData.value[i].updatedTime); 
    }
    statusCounts.pending = waitTableData.value.length;
  }else{
    ElMessage.error('获取待审核视频列表失败！')
  }
}

const getCurrentVideoPendingsYes = async() =>{
  const result = await getVideoYesByUserId(userInfo.value.id);
  if(result.code === 1){
    YesTableData.value = result.data;
    for(let i = 0;i<YesTableData.value.length;i++){
      YesTableData.value[i].createdTime = formatDtaTime(YesTableData.value[i].createdTime);
      YesTableData.value[i].updatedTime = formatDtaTime(YesTableData.value[i].updatedTime); 
    }
    statusCounts.approved = YesTableData.value.length;
  }else{
    ElMessage.error('获取已通过视频列表失败！')
  }
}

const getCurrentVideoPendingsNo = async() =>{
  const result = await getVideoPendings(userInfo.value.id,0);
  if(result.code === 1){
    NoTableData.value = result.data;
    for(let i = 0;i<NoTableData.value.length;i++){
      NoTableData.value[i].createdTime = formatDtaTime(NoTableData.value[i].createdTime);
      NoTableData.value[i].updatedTime = formatDtaTime(NoTableData.value[i].updatedTime); 
    }
    statusCounts.rejected = NoTableData.value.length;
  }else{
    ElMessage.error('获取未通过视频列表失败！')
  }
}


const formatDtaTime = (date) => {
  const newDateTime = date.replace('T', ' ');
  return newDateTime; 
}


const statusCounts = reactive({
  pending: 0,
  rejected: 0,
  approved: 0
})

const topValue = ref(`待审核 ${statusCounts.pending}`)
const topOptions = computed(() => [
  `待审核 ${statusCounts.pending}`,
  `未通过 ${statusCounts.rejected}`,
  `已通过 ${statusCounts.approved}`
])


const getDialogVisible = ref(false)

const updateDialogVisible = ref(false)

const updateDialogVisibleYes = ref(false)

const videoPending = ref({
  id: '',
  title: '',
  description: '',
  tags: [],
  createdTime: '',
  updatedTime: '',
  status: '',
  reason: '',
  coverUrl: '',
  videoUrl: '',
  visibility: '',
  collectionId: ''
})

// 排序字段映射表
const sortFieldMap = {
  '1': 'createdTime',
  '2': 'views',
  '3': 'favorites',
  '4': 'barrages',
  '5': 'comments'
}

const sortData = () => {

  let currentStatus = null;
  if(topValue.value.startsWith('待审核')){
    currentStatus = waitTableData.value;
  }else if(topValue.value.startsWith('未通过')){
     currentStatus = NoTableData.value; 
  }else if(topValue.value.startsWith('已通过')){
     currentStatus = YesTableData.value; 
  }

  const sortField = sortFieldMap[value.value]
  
  currentStatus.sort((a, b) => {
    if (sortField === 'createdTime') {
      return new Date(b[sortField].replace(' ','/')) - new Date(a[sortField].replace(' ','/'))
    }
    return b[sortField] - a[sortField]
  })
}

// 监听排序方式和标签变化
watch([value, topValue], () => {
  sortData()
})

const getCurrentVideoPendingById = async(id) => {
  const result = await getVideoPendingById(id);
  if(result.code === 1){
    videoPending.value = result.data;
    videoPending.value.createdTime = formatDtaTime(videoPending.value.createdTime);
    videoPending.value.updatedTime = formatDtaTime(videoPending.value.updatedTime); 
  }else{
    ElMessage.error('查看稿件详细失败！')
  }
  getDialogVisible.value = true;
}

const updateCurrentVideoPendingById = async(id) => {
  const result = await getVideoPendingById(id);
  if(result.code === 1){
    videoPending.value = result.data;
    videoUrl.value = videoPending.value.videoUrl;
    coverUrl.value = videoPending.value.coverUrl;
    VcValue.value = videoPending.value.collectionId;
  }else{
    ElMessage.error('查看稿件详细失败！')
  }
  progressFlag.value = false;
  loadProgress.value = 0;
  updateDialogVisible.value = true;
}

const updateCurrentVideoById = async(id) => {
  const result = await getVideo(id);
  if(result.code === 1){
    videoPending.value = result.data;
    videoUrl.value = videoPending.value.videoUrl;
    coverUrl.value = videoPending.value.coverUrl;
    VcValue.value = videoPending.value.collectionId;
    if(videoPending.value.visibility === 1){
      videoVisibility.value = '1';	
    }else{
      videoVisibility.value = '0';
    }
    console.log('可见权限：',videoPending.value)
  }else{
    ElMessage.error('查看稿件详细失败！')
  }
  progressFlag.value = false;
  loadProgress.value = 0;
  updateDialogVisibleYes.value = true;
}

const getCurrentVideoById = async(id) => {
  const result = await getVideo(id);
  if(result.code === 1){
    videoPending.value = result.data;
    videoUrl.value = videoPending.value.videoUrl;
    coverUrl.value = videoPending.value.coverUrl;
    videoPending.value.createdTime = formatDtaTime(videoPending.value.createdTime);
    videoPending.value.updatedTime = formatDtaTime(videoPending.value.updatedTime); 
    if(videoPending.value.visibility === 1){
      videoVisibility.value = '1';	
    }else{
      videoVisibility.value = '0';
    }
    console.log('可见权限：',videoPending.value)
  }else{
    ElMessage.error('查看稿件详细失败！')
  }
  getDialogVisible.value = true;
}

const formatVideoDuration = (seconds) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60

  // 使用padStart确保分钟和秒始终为两位数
  return h > 0 
    ? `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}` 
    : `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

const deleteCurrentVideoPending = async(id) => {
  const result = await deleteVideoPending(id);
  if(result.code === 1){
    ElMessage.success('删除成功！')
    getCurrentVideoPendingsWait(); 
  getCurrentVideoPendingsNo();
  } else{
    ElMessage.error('删除失败！')
  }
}

const deleteCurrentVideo = async(id) => {
  const result = await deleteVideoById(id);
  if(result.code === 1){
    ElMessage.success('删除成功！')
    getCurrentVideoPendingsYes();
  } else{
    ElMessage.error('删除失败！')
  }
}

const progressFlag = ref(false);
const loadProgress = ref(0);

const uploadVideoProcess = (event, file, fileList) => {
          loadProgress.value = parseInt(event.percent); // 动态获取文件上传进度
          if (loadProgress.value >= 100) {
              loadProgress.value = 99
          }
      }

//文件上传
// 图片上传成功后触发
const handleAvatarSuccess = (response,uploadFile) => {
  videoPending.value.coverUrl = response.data
  coverUrl.value = response.data
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

// 视频上传成功后触发
const handleVideoSuccess = (response,uploadFile) => {
  videoPending.value.videoUrl = response.data
  coverUrl.value = videoPending.value.coverUrl;
  loadProgress.value = 100;
  ElMessage.success('上传成功！')
}
// 文件上传之前触发
const beforeVideoUpload = (rawFile) => {
  if (!['video/mp4', 'video/quicktime', 'video/x-msvideo', 'video/x-matroska'].includes(rawFile.type)) {
    ElMessage.error('仅支持MP4/MOV/AVI/MKV格式视频')
    return false
} else if (rawFile.size > 10 * 1024 * 1024 * 1024) { // 10GB
    ElMessage.error('视频大小不能超过10GB')
    return false
}
ElMessage.success('开始上传，请等待上传成功提示！')
progressFlag.value = true; // 显示进度条
  return true
}

const videoUrl = ref('');
const coverUrl = ref('');
const videoKey = ref(0)

const Voptions = reactive({
    width: '100%', //播放器高度
    height: '100%', //播放器高度
    color: "#409eff", //主题色
    title: "", //视频名称
    webFullScreen:false,//网页全屏
    speed:true,//是否支持快进快退
    currentTime:0,//跳转到固定播放时间(s)
    muted:false,//静音
    autoPlay: false, //自动播放
    loop:false,//循环播放
    mirror:false,//镜像画面
    control: true, //是否显示控制器
    ligthOff:false,//关灯模式
    volume:0.3,//默认音量0-1
    get src() { return videoUrl.value }, // 动态绑定
    get poster() { return coverUrl.value }, // 动态绑定
    speedRate: [2.0,1.5,1.25,1.0,0.5], // 可选的播放速度
    controlBtns: [
    "audioTrack",//音轨切换按钮
    "quality",//视频质量切换按钮
    "speedRate",//速率切换按钮
    "volume",//音量
    "setting",//设置
    "pip",//画中画按钮
    "pageFullScreen",//网页全屏按钮
    "fullScreen",//全屏按钮
  ], //显示所有按钮,
})

watch([videoUrl, coverUrl], () => {
  videoKey.value++ // key 变化会强制组件重新渲染
  console.log('key变化了！')
})

const isPlay = ref(false);
const videoCurDuration = ref(0) // 当前播放秒数
const videoAllDuration = ref(0) // 总时长秒数

// 监听视频时间更新
const onTimeupdate = (event) => {
  videoCurDuration.value = Math.floor(event.target.currentTime)
}

const loadEdmetadata = (ev) => {
    videoAllDuration.value = ev.target.duration // 总时长秒数
}
const onPlay = (ev) => {
  isPlay.value = true;
  console.log("播放");
};

const onPause = () => {
  isPlay.value = false;
  console.log("暂停");
};

const aplayVideo = ref(null);

const VcValue = ref('');

const vcOptions =ref([]);

const videoVisibility = ref('1')

const getCurrentUserCollections = async() => {
  const result = await getUserCollection(userInfo.value.id,userInfo.value.id);
  if(result.code === 1){
    vcOptions.value = result.data;
    console.log(vcOptions.value);
  }else{
    ElMessage.error('获取投稿合集失败！')
  }
}

const updateCurrentVideoPending = async() => {
  videoPending.value.collectionId = VcValue.value;
  
  const result = await updateVideoPendingTo(videoPending.value);
  if(result.code === 1){
    ElMessage.success('修改成功！')
    getCurrentVideoPendingsWait();
    getCurrentVideoPendingsNo();
    updateDialogVisible.value = false;
  }else{
    ElMessage.error(result.msg)
  }
}

const updateCurrentVideoPendingYes = async() => {
  videoPending.value.visibility = videoVisibility.value;
  videoPending.value.collectionId = VcValue.value;
 
  const result = await updateVideoYesTo(videoPending.value);
  if(result.code === 1){
    ElMessage.success('修改成功！')
    getCurrentVideoPendingsYes();
    getCurrentVideoPendingsWait();
    updateDialogVisibleYes.value = false;
  }else{
    ElMessage.error(result.msg)
  }
}

const reuploadCurrentVideoPending = async(id) => {
  const result = await reuploadVideoPeing(id);
  if(result.code === 1){
    ElMessage.success('操作成功！')
    getCurrentVideoPendingsWait();
    getCurrentVideoPendingsNo();	
  }	else{
    ElMessage.error('操作失败')
  }
}

onMounted(async() => {
  await getUserInfo();
  await getCurrentVideoPendingsWait();
  await getCurrentVideoPendingsYes();
  await getCurrentVideoPendingsNo();
  sortData();
  getCurrentUserCollections();
})

</script>

<template>
  <div class="fm-container">
    <span class="fm-title">视频管理</span>
    <div class="fm-status">
      <el-segmented v-model="topValue" :options="topOptions" size="small" />
      <el-select 
      v-model="value" 
      placeholder="Select" 
      style="width: 170px"
      class="fm-select">
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
    </div>

    <div class="fm-list">
      <el-table 
      :data="waitTableData" 
      style="width: 100%" 
      v-show="waitTableData.length != 0 && topValue.startsWith('待审核')"
      :cell-style="{ textAlign: 'center' }"
      :header-cell-style="{ 'text-align': 'center' }">
        <el-table-column prop="coverUrl" label="视频封面" width="120">
          <template #default="scope">
          <img class="fm-coverUrl" :src="scope.row.coverUrl" width="120" height="70"/>
          </template>
        </el-table-column>
        <el-table-column prop="title"  show-overflow-tooltip label="标题" width="120" />
        <el-table-column prop="views" label="播放数" width="100" />
        <el-table-column prop="likes" label="点赞数" width="100" />
        <el-table-column prop="favorites" label="收藏数" width="100" />
        <el-table-column prop="barrages" label="弹幕数" width="100" />
        <el-table-column prop="comments" label="评论数" width="100" />
        <el-table-column prop="createdTime" label="投稿时间" width="180" />
        <el-table-column prop="updatedTime" label="上次修改时间" width="180"/>
        <el-table-column prop="status" label="状态" width="70" >
          <span class="fm-status-3">待审核</span>
          </el-table-column>
        <el-table-column prop="deal" label="操作" >
          <template #default="scope">
            <div style="text-align: center">
    <el-button style="display: block; width: 60px; margin: 4px auto" type="primary" size="small" @click="getCurrentVideoPendingById(scope.row.id)">查看</el-button>
    <el-button style="display: block; width: 60px; margin: 4px auto" type="info" size="small" @click="updateCurrentVideoPendingById(scope.row.id)">修改</el-button>
    <el-popconfirm title="你确定要删除这条投稿吗？"
    @confirm="deleteCurrentVideoPending(scope.row.id)">
    <template #reference>
      <el-button style="display: block; width: 60px; margin: 4px auto" type="danger" size="small">删除</el-button>
    </template>
  </el-popconfirm>
  </div>
          </template>
        </el-table-column>
      </el-table>

      <el-table 
      :data="NoTableData" 
      style="width: 100%" 
      v-show="NoTableData.length != 0 && topValue.startsWith('未通过')"
      :cell-style="{ textAlign: 'center' }"
      :header-cell-style="{ 'text-align': 'center' }">
        <el-table-column prop="coverUrl" label="视频封面" width="120">
          <template #default="scope">
          <img class="fm-coverUrl" :src="scope.row.coverUrl" width="120" height="70"/>
          </template>
        </el-table-column>
        <el-table-column prop="title"  show-overflow-tooltip label="标题" width="120" />
        <el-table-column prop="createdTime" label="投稿时间" width="180" />
        <el-table-column prop="updatedTime" label="上次修改时间" width="180"/>
        <el-table-column prop="status" label="状态" width="70" >
          <span class="fm-status-4">未通过</span>
          </el-table-column>
        <el-table-column prop="reason" label="原因" width="300" style="color: darkgray;"/>
        <el-table-column prop="deal" label="操作" >
          <template #default="scope">
            <div style="text-align: center">
    <el-button  type="primary" size="small" @click="getCurrentVideoPendingById(scope.row.id)">查看</el-button>
    <el-button type="info" size="small" @click="updateCurrentVideoPendingById(scope.row.id)">修改</el-button>
    <el-popconfirm title="你确定要要重新审核吗？"
    @confirm="reuploadCurrentVideoPending(scope.row.id)">
    <template #reference>
      <el-button type="success" size="small" >重新审核</el-button>
    </template>
  </el-popconfirm>
    <el-popconfirm title="你确定要删除这条投稿吗？"
    @confirm="deleteCurrentVideoPending(scope.row.id)">
    <template #reference>
      <el-button type="danger" size="small">删除</el-button>
    </template>
  </el-popconfirm>
  </div>
          </template>
        </el-table-column>
      </el-table>

      <el-table 
      :data="YesTableData" 
      style="width: 100%" 
      v-show="YesTableData.length != 0 && topValue.startsWith('已通过')"
      :cell-style="{ textAlign: 'center' }"
      :header-cell-style="{ 'text-align': 'center' }">
        <el-table-column prop="coverUrl" label="视频封面" width="120">
          <template #default="scope">
          <img class="fm-coverUrl" :src="scope.row.coverUrl" width="120" height="70"/>
          </template>
        </el-table-column>
        <el-table-column prop="title"  show-overflow-tooltip label="标题" width="120" />
        <el-table-column prop="views" label="播放数" width="100" />
        <el-table-column prop="likes" label="点赞数" width="100" />
        <el-table-column prop="favorites" label="收藏数" width="100" />
        <el-table-column prop="barrages" label="弹幕数" width="100" />
        <el-table-column prop="comments" label="评论数" width="100" />
        <el-table-column prop="createdTime" label="投稿时间" width="180" />
        <el-table-column prop="updatedTime" label="上次修改时间" width="180"/>
        <el-table-column prop="status" label="状态" width="70" >
          <span class="fm-status-5">已通过</span>
          </el-table-column>
        <el-table-column prop="deal" label="操作" >
          <template #default="scope">
            <div style="text-align: center">
    <el-button style="display: block; width: 60px; margin: 4px auto" type="primary" size="small" @click="getCurrentVideoById(scope.row.id)">查看</el-button>
    <el-button style="display: block; width: 60px; margin: 4px auto" type="info" size="small" @click="updateCurrentVideoById(scope.row.id)">修改</el-button>
    <el-popconfirm title="你确定要删除这条投稿吗？"
    @confirm="deleteCurrentVideo(scope.row.id)">
    <template #reference>
      <el-button style="display: block; width: 60px; margin: 4px auto" type="danger" size="small">删除</el-button>
    </template>
  </el-popconfirm>
  </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div v-show="waitTableData.length == 0 && topValue.startsWith('待审核')" class="fm-img-container">
      <img  src="YilenaPic/25/04/19-c0377718e01b4d0788a5034a951c9e44.jpg" class="fm-img">
      <span class="fm-text">一个稿件都没有……</span>
    </div>
    <div v-show="YesTableData.length == 0 && topValue.startsWith('已通过')" class="fm-img-container">
      <img  src="YilenaPic/25/04/19-c0377718e01b4d0788a5034a951c9e44.jpg" class="fm-img">
      <span class="fm-text">一个稿件都没有……</span>
    </div>
    <div v-show="NoTableData.length == 0 && topValue.startsWith('未通过')" class="fm-img-container">
      <img  src="YilenaPic/25/04/19-c0377718e01b4d0788a5034a951c9e44.jpg" class="fm-img">
      <span class="fm-text">一个稿件都没有……</span>
    </div>
  </div>

  <el-dialog
    v-model="getDialogVisible"
    title="查看稿件"
    width="800"
  >
  <el-form label-position="left" :model="form" label-width="auto" style="max-width: 600px">
    <el-form-item label="标题">
      <el-input v-model="videoPending.title" disabled/>
    </el-form-item>
    <el-form-item label="封面">
      <img class="fm-coverUrl" :src="videoPending.coverUrl" width="130" height="80"/>
    </el-form-item>
    <el-form-item label="简介">
      <el-input
    v-model="videoPending.description"
    :autosize="{ minRows: 2, maxRows: 4 }"
    type="textarea"
    disabled
  />
    </el-form-item>
    <el-form-item label="标签">
      <div v-for="(item,index) in videoPending.tags">
        <el-tag type="primary" style="margin-right: 10px">{{item}}</el-tag>
      </div>
    </el-form-item>
    <el-form-item label="弹幕数">
      <span>{{ videoPending.barrages }}</span>
    </el-form-item>
    <el-form-item label="播放数">
      <span>{{ videoPending.views }}</span>
    </el-form-item>
    <el-form-item label="点赞数">
      <span>{{ videoPending.likes }}</span>
    </el-form-item>
    <el-form-item label="收藏数">
      <span>{{ videoPending.favorites }}</span>
    </el-form-item>
    <el-form-item label="评论数">
      <span>{{ videoPending.comments }}</span>
    </el-form-item>
    <el-form-item label="投币数">
      <span>{{ videoPending.coins }}</span>
    </el-form-item>
    <el-form-item label="视频时长">
      <span>{{ formatVideoDuration(videoPending.time) }}</span>
    </el-form-item>
    <el-form-item label="投稿合集">
      <span>{{ videoPending.collectionName }}</span>
    </el-form-item>
    <el-form-item label="投稿时间">
      <span>{{ videoPending.createdTime }}</span>
    </el-form-item>
    <el-form-item label="可见权限" :key="videoPending.visibility">
      <span v-if="videoPending.visibility == 1">公开</span>
      <span v-else-if="videoPending.visibility == 0">私密</span>
    </el-form-item>
    <el-form-item label="上次修改时间">
      <span>{{ videoPending.updatedTime }}</span>
    </el-form-item>
    </el-form>
  </el-dialog>

  <el-dialog
    v-model="updateDialogVisible"
    title="修改稿件"
    width="800"
    @close="handleDialogClose"
  >
  <el-form label-position="left" :model="form" label-width="auto" style="max-width: 600px">
    <el-form-item label="标题">
      <el-input v-model="videoPending.title"/>
    </el-form-item>
    <el-form-item label="视频源">
      <el-upload
                class="video-uploader"
                action="/api/upload"
                :headers="{'token':token}"
                :show-file-list="false"
                :on-success="handleVideoSuccess"
                :before-upload="beforeVideoUpload"
                :on-progress="uploadVideoProcess"
                >
      <el-button class="fm-videoUrl-btn"><el-icon><UploadFilled /></el-icon></el-button>
      </el-upload>
      <el-progress class="fm-video-progress" v-if="progressFlag" :percentage="loadProgress" ></el-progress>
      <div class="fm-video-player"
  tabindex="0">
  <videoPlay  
  v-if="videoPending.videoUrl != ''"
  ref="aplayVideo" 
  :key="videoKey"
  v-bind="Voptions"  
  @play="onPlay" 
  @pause="onPause"
  @timeupdate="onTimeupdate" 
  @loadedmetadata="loadEdmetadata"
  />
</div>
    </el-form-item>
    <el-form-item label="封面">
      <el-upload
                class="avatar-uploader"
                action="/api/upload"
                :headers="{'token':token}"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                >
      <el-button class="fm-coverUrl-btn"><el-icon><UploadFilled /></el-icon></el-button>
      </el-upload>
      <img class="fm-coverUrl" :src="videoPending.coverUrl" width="270" height="160"/>
    </el-form-item>
    <el-form-item label="简介">
      <el-input
    v-model="videoPending.description"
    :autosize="{ minRows: 2, maxRows: 4 }"
    type="textarea"
  />
    </el-form-item>
    <el-form-item label="标签">
      <el-input-tag v-model="videoPending.tags" draggable placeholder="请输入标签" />
    </el-form-item>

    <el-form-item label="投稿合集">
      <el-select v-model="VcValue" placeholder="Select" style="width: 240px">
      <el-option
        v-for="item in vcOptions"
        :key="item.id"
        :label="item.title"
        :value="item.id"
      />
    </el-select>
    </el-form-item>
    </el-form>
    <div class="fm-dialog-footer">
    <el-button @click="updateDialogVisible = false">取消</el-button>
    <el-button type="primary" @click="updateCurrentVideoPending">修改</el-button>
  </div>
  </el-dialog>

  <el-dialog
    v-model="updateDialogVisibleYes"
    title="修改稿件"
    width="800"
  >
  <el-form label-position="left" :model="form" label-width="auto" style="max-width: 600px">
    <span class="hint">除了可见权限和投稿合集以外的修改都需要二次审核</span>
    <el-form-item label="标题">
      <el-input v-model="videoPending.title"/>
    </el-form-item>
    <el-form-item label="视频源">
      <el-upload
                class="video-uploader"
                action="/api/upload"
                :headers="{'token':token}"
                :show-file-list="false"
                :on-success="handleVideoSuccess"
                :before-upload="beforeVideoUpload"
                :on-progress="uploadVideoProcess"
                >
      <el-button class="fm-videoUrl-btn"><el-icon><UploadFilled /></el-icon></el-button>
      </el-upload>
      <el-progress class="fm-video-progress" v-if="progressFlag" :percentage="loadProgress" ></el-progress>
      <div class="fm-video-player"
  tabindex="0">
  <videoPlay  
  v-if="videoPending.videoUrl != ''"
  ref="aplayVideo" 
  :key="videoKey"
  v-bind="Voptions"  
  @play="onPlay" 
  @pause="onPause"
  @timeupdate="onTimeupdate" 
  @loadedmetadata="loadEdmetadata"
  />
</div>
    </el-form-item>
    <el-form-item label="封面">
      <el-upload
                class="avatar-uploader"
                action="/api/upload"
                :headers="{'token':token}"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                >
      <el-button class="fm-coverUrl-btn"><el-icon><UploadFilled /></el-icon></el-button>
      </el-upload>
      <img class="fm-coverUrl" :src="videoPending.coverUrl" width="270" height="160"/>
    </el-form-item>
    <el-form-item label="简介">
      <el-input
    v-model="videoPending.description"
    :autosize="{ minRows: 2, maxRows: 4 }"
    type="textarea"
  />
    </el-form-item>
    <el-form-item label="标签">
      <el-input-tag v-model="videoPending.tags" draggable placeholder="请输入标签" />
    </el-form-item>

    <el-form-item label="投稿合集">
      <el-select v-model="VcValue" placeholder="Select" style="width: 240px">
      <el-option
        v-for="item in vcOptions"
        :key="item.id"
        :label="item.title"
        :value="item.id"
      />
    </el-select>
    </el-form-item>
    <el-form-item label="可见权限">
      <el-radio-group v-model="videoVisibility" :key="videoVisibility">
      <el-radio value="1">公开</el-radio>
      <el-radio value="0">私密</el-radio>
    </el-radio-group>
    </el-form-item>
    </el-form>
    <div class="fm-dialog-footer">
    <el-button @click="updateDialogVisibleYes = false">取消</el-button>
    <el-button type="primary" @click="updateCurrentVideoPendingYes">修改</el-button>
  </div>
  </el-dialog>
</template>

<style scoped>
.fm-container{
  background-color: rgb(255, 255, 255);
  margin-left: 85px;
  width: 1285px;
  border-radius: 5px;
}

.fm-title{
  color: rgb(24, 167, 215);
  font-weight: 600;
  font-size: 17px;
  margin-left: 35px;
  position: relative;
  top: 30px;
}

.fm-status{
  margin-left: 35px;
  margin-top: 40px;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: rgb(132, 132, 132);
}

.fm-select{
  margin-left: 860px;
}

.fm-list{
  margin-top: 10px;
  margin-left: 35px;
}

.fm-img-container{
  margin-top: 75px;
  margin-left: 500px;
  height: 300px;
}

.fm-img{
  width: 260px;
  height: 210px;
}

.fm-text{
  position: absolute;
  font-size: 14px;
  color: rgb(34, 34, 34);
  top: 470px;
  left: 870px;
}

.el-segmented {
  --el-segmented-item-selected-color: rgb(24, 167, 215);
  --el-segmented-item-selected-bg-color: #ffffff;
  --el-border-radius-base: 16px;
}

.fm-status-3{
  color: rgb(132, 132, 132);
}

.fm-status-4{
  color: rgb(245, 108, 108);
}

.fm-status-5{
  color: rgb(103, 194, 58);
}

.fm-coverUrl-btn{
  margin-right: 40px;
  margin-top: -90px;
}

.fm-videoUrl-btn{
  margin-top: 10px;
}

.fm-video-player{
  margin-top: 35px;
}

.fm-dialog-footer{
  display: flex;
  margin-top: 50px;
  justify-content:center;
}

.fm-video-progress{
  position: absolute;
  top: 55px;
  left: 0px;
  width: 550px;
  height: 10px;
}

.hint{
  font-size: 12px;
  color: rgb(132, 132, 132);
  position: relative;
  top: -15px;
}
</style>
