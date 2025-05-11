<script lang="ts" setup>
import { ref,onMounted,computed,watch,reactive } from 'vue'
import { useRouter } from 'vue-router';
import { useTransition } from '@vueuse/core'
import CryptoJS from 'crypto-js'
import { ElMessage,ElMessageBox } from 'element-plus';
import { UploadFilled } from '@element-plus/icons-vue'
import { getVideo } from '../../api/video';
import { findAllVideos,getUserInfoByUsername } from '../../api';
import { addVideo,getVideoPendings,updateVideoYesTo,updateVideoPendingTo,getUserCollection,deleteVideoById,getVideoYesByUserId,getVideoPendingById,updateVideoPending,deleteVideoPending,reuploadVideoPeing } from '../../api/upload';
// 视频播放组件
import "vue3-video-play/dist/style.css";
	import {videoPlay} from "vue3-video-play";
import router from '../../router';
import UserInfo from '../userInfo/mine/userInfo.vue';


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


const video = ref({
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
  collectionId: '',
  userId: ''
})

const progressFlag = ref(false);
const loadProgress = ref(0);

const uploadVideoProcess = (event, file, fileList) => {
          loadProgress.value = parseInt(event.percent); // 动态获取文件上传进度
          if (loadProgress.value >= 100) {
              loadProgress.value = 99
          }
      }

const coverUrl = ref('')

const isuploading = ref(false)

const fileName = ref('')

const aplayVideo = ref(null);

const VcValue = ref('');

const vcOptions =ref([]);

const getCurrentUserCollections = async() => {
  const result = await getUserCollection(userInfo.value.id,userInfo.value.id);
  if(result.code === 1){
    vcOptions.value = result.data;
    console.log(vcOptions.value);
  }else{
    ElMessage.error('获取投稿合集失败！')
  }
}

// 视频上传成功后触发
const handleVideoSuccess = (response,uploadFile) => {
  video.value.videoUrl = response.data;
  videoUrl.value = response.data;
  coverUrl.value = video.value.coverUrl;
  loadProgress.value = 100
  ElMessage.success('上传成功！请等待审核通过！')
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
fileName.value = rawFile.name;
ElMessage.success('开始上传，请等待上传成功提示！');
progressFlag.value = true;
isuploading.value = true;
  return true
}

const videoUrl = ref('');
const videoKey = ref(0)

const Voptions = reactive({
    width: '70%', //播放器高度
    height: '70%', //播放器高度
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

const ruleFormRef = ref<FormInstance>()

const rules = reactive<FormRules<RuleForm>>({
  title: [
    { required: true, message: '请输入视频标题', trigger: 'blur' }
  ],
  coverUrl: [
    { required: true, message: '请上传视频封面', trigger: 'blur' }
  ],
  videoUrl: [
    { required: true, message: '请上传视频', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入简介', trigger: 'blur' }
  ],
  tags: [
    { required: true, message: '请输入标签', trigger: 'blur' }
  ],
  visibility: [
    { required: true, message: '请选择可见权限', trigger: 'blur' }
  ],
  collectionId: [
    { required: true, message: '请选择投稿合集', trigger: 'blur' }
  ]
})

watch([videoUrl, coverUrl], () => {
  videoKey.value++ // key 变化会强制组件重新渲染
  console.log('key变化了！')
})

//文件上传
// 图片上传成功后触发
const handleAvatarSuccess = (response,uploadFile) => {
  video.value.coverUrl = response.data
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
  startInterval();
};

const onPause = () => {
  isPlay.value = false;
  console.log("暂停");
  clearDanmuInterval();
};

const resetForm = () => {
  video.value = {
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
    collectionId: '',
    userId: ''
  }	
}

const uploadVideo = async() => {
  if(video.value.title === ''){
    ElMessage.error('请输入视频标题！')
    return false;
  }
  if(VcValue.value === ''){
    ElMessage.error('请选择投稿合集！')
    return false;
  }
  if(video.value.coverUrl === ''){
    ElMessage.error('请上传视频封面！')
    return false;
  }
  if(video.value.videoUrl === ''){
    ElMessage.error('请上传视频！')
    return false;
  }
  video.value.userId = userInfo.value.id;
  video.value.collectionId = VcValue.value;
  const result = await addVideo(video.value);
  if(result.code === 1){
    ElMessage.success('投稿成功！')
  router.push('/upload/fileManagement').then(() => {
    window.location.reload();
  })
  }else{
    ElMessage.error(result.msg)
  }
}

onMounted(async() => {
  await getUserInfo();
  isuploading.value = false;
  getCurrentUserCollections();
})
</script>

<template>
  <div class="upload-core">
    <div class="upload-core-title">
        <span class="upload-core-title-text" v-if="isuploading == false">视频投稿</span>
        <span class="upload-core-title-text2" v-else>发布视频</span>
    </div>
    <div v-if="isuploading == false">
    <el-upload
    class="upload-demo"
    drag
    action="/api/upload"
    :headers="{'token':token}"
    :show-file-list="false"
    multiple
    :on-success="handleVideoSuccess"
    :before-upload="beforeVideoUpload"
    :on-progress="uploadVideoProcess"
  >
    <el-icon class="el-icon--upload"><upload-filled /></el-icon>
    <div class="el-upload__text">
      拖动文件到此处或者<em>点击此处</em>进行上传
    </div>
  </el-upload>
  <img class="upload-core-img" src="YilenaPic/25/04/20-26c2f4118b1a437e8e82dcc877555cf8.jpg"/>
</div>

 <div v-else>
    <span class="file-name"><el-icon><Folder /></el-icon>&nbsp;&nbsp;{{ fileName }}</span>
    <el-progress class="upload-core-video-progress" v-if="progressFlag" :percentage="loadProgress" ></el-progress>
    <div class="fm-video-player"
  tabindex="0">
  <videoPlay  
  v-if="video.videoUrl != ''"
  ref="aplayVideo" 
  :key="videoKey"
  v-bind="Voptions"  
  @play="onPlay" 
  @pause="onPause"
  @timeupdate="onTimeupdate" 
  @loadedmetadata="loadEdmetadata"
  />
</div>
    <span class="upload-core-video-title">基本信息</span>
    <el-form :rules="rules" ref="ruleFormRef" class="up-video-form" :model="video" label-width="auto" style="max-width: 600px"
    label-position="left">
    <el-form-item label="标题" prop="title">
      <el-input v-model="video.title" placeholder="请输入视频标题"/>
    </el-form-item>
    <el-form-item label="封面" prop="coverUrl">
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
      <img v-if="video.coverUrl != ''" class="up-coverUrl" :src="video.coverUrl" width="270" height="160"/>
    </el-form-item>
    <el-form-item label="简介" prop="description">
      <el-input
    v-model="video.description"
    :autosize="{ minRows: 2, maxRows: 4 }"
    type="textarea"
    placeholder="填写更全面的相关信息，让更多的人都能找到你的视频吧"
  />
    </el-form-item>
    <el-form-item label="标签" prop="tags">
      <el-input-tag v-model="video.tags" draggable placeholder="按回车键Enter创建标签" />
    </el-form-item>

    <el-form-item label="投稿合集" prop="collectionId">
      <el-select v-model="VcValue" placeholder="请选择" style="width: 240px">
      <el-option
        v-for="item in vcOptions"
        :key="item.id"
        :label="item.title"
        :value="item.id"
      />
    </el-select>
    </el-form-item>
    <el-form-item label="可见权限" :key="video.videoVisibility" prop="visibility">
      <el-radio-group v-model="video.videoVisibility" :key="video.videoVisibility">
      <el-radio value="1">公开</el-radio>
      <el-radio value="0">私密</el-radio>
    </el-radio-group>
    </el-form-item>
    </el-form>
    <div class="up-video-form-btn">
    <el-button size="large" @click="resetForm">重置</el-button>
    <el-button type="primary" size="large" @click="uploadVideo">投稿</el-button>
</div>
 </div>
  </div>
</template>

<style scoped>
.upload-core{
    background-color: rgb(255, 255, 255);
    width: 1100px;
    margin-left: 175px;
    border-radius: 5px;
}

.upload-core-title{
    position: relative;
    top: 10px;
    margin-left: 40px;
}

.upload-core-title-text{
    font-size: 18px;
   font-weight: 600; 
   color: rgb(0, 161, 214);
}

.upload-core-title-text2{
    font-size: 18px;
   font-weight: 600; 
   color: rgb(33, 33, 33);
}

.upload-demo{
  margin-top: 45px;
  width: 830px;
  border-radius: 5px;
  margin-left: 140px;
}

.upload-core-img{
  width: 830px;
  height: 330px;
  margin-left: 140px;
  margin-top: 10px;
}

.file-name{
  margin-left: 150px;
  position: relative;
  top:40px
}

.upload-core-video-progress{
    margin-top: 50px;
    width: 800px;
    margin-left: 150px;
}

.upload-core-video-title{
    position: relative;
    top: 25px;
margin-left: 40px;
    font-size: 18px;
   font-weight: 600; 
   color: rgb(33, 33, 33);
}

.fm-video-player{
  margin-top: 50px;
  margin-left: 230px;
}

.up-video-form{
    position: relative;
    margin-top: 70px;
    margin-left: 230px;
}

.el-form-item{
    margin-bottom: 50px;
}

.up-coverUrl{
    margin-left: 50px;
}

.up-video-form-btn{
    position: relative;
    margin-top: 50px;
    margin-left: 450px;
}
</style>
