<script lang="ts" setup>
import type { TabsPaneContext } from 'element-plus'
import { ref, reactive, onMounted,computed,watch } from 'vue'
import { useRouter } from 'vue-router'
import { useTransition } from '@vueuse/core'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Iphone,
  Location,
  OfficeBuilding,
  Tickets,
  User,Search 
} from '@element-plus/icons-vue'
import type { ComponentSize } from 'element-plus'
// 视频播放组件
import "vue3-video-play/dist/style.css";
	import videoPlay from "vue3-video-play";
import { getVideoByPage,updateVideoStatus,NoPassVideo } from '@/api/video'

const videoQueryEntity = ref({
  title: '', 
  beginTime: '',
  endTime: '',
  status: 0,
  page: 1, 
  pageSize: 10
})

const getVideoList = async() => {
  videoQueryEntity.value.status = 0;
	const result1 = await getVideoByPage(videoQueryEntity.value.title,
                                      videoQueryEntity.value.beginTime,
                                      videoQueryEntity.value.endTime,
                                      videoQueryEntity.value.status,
                                      videoQueryEntity.value.page,
                                      videoQueryEntity.value.pageSize)
  if(result1.code == 1){
    console.log('未通过视频：',result1.data.rows)
    noVideos.value = result1.data.rows;
    noVideos.value.forEach((item:any) => {
      item.createdTime = formatDate(item.createdTime);
      item.updatedTime = formatDate(item.updatedTime);
    })
    tabCounts.value.no = result1.data.total;
    noVideosTotal.value = result1.data.total;
  }
  videoQueryEntity.value.status = 1;
  const result2 = await getVideoByPage(
                                      videoQueryEntity.value.title,
                                      videoQueryEntity.value.beginTime,
                                      videoQueryEntity.value.endTime,
                                      videoQueryEntity.value.status,
                                      videoQueryEntity.value.page,
                                      videoQueryEntity.value.pageSize)
  if(result2.code == 1){
   console.log('已通过视频：',result2.data.rows)
    yesVideos.value = result2.data.rows;
    yesVideos.value.forEach((item:any) => {
      item.createdTime = formatDate(item.createdTime);
      item.updatedTime = formatDate(item.updatedTime);
    })
    tabCounts.value.yes = result2.data.total;
    yesVideosTotal.value = result2.data.total;
  }
  videoQueryEntity.value.status = 2;
  const result3 = await getVideoByPage(videoQueryEntity.value.title,
                                      videoQueryEntity.value.beginTime,
                                      videoQueryEntity.value.endTime,
                                      videoQueryEntity.value.status,
                                      videoQueryEntity.value.page,
                                      videoQueryEntity.value.pageSize)
  if(result3.code == 1){
    console.log('待审核视频：',result3.data.rows)
    waitVideos.value = result3.data.rows;
    waitVideos.value.forEach((item:any) => {
      item.createdTime = formatDate(item.createdTime);
      item.updatedTime = formatDate(item.updatedTime);
    })
    tabCounts.value.wait = result3.data.total;
    waitVideosTotal.value = result3.data.total;
  }
}

const size = ref<ComponentSize>('default')

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
  collectionId: ''
})

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

const iconStyle = computed(() => {
  const marginMap = {
    large: '8px',
    default: '6px',
    small: '4px',
  }
  return {
    marginRight: marginMap[size.value] || marginMap.default,
  }
})
const blockMargin = computed(() => {
  const marginMap = {
    large: '32px',
    default: '28px',
    small: '24px',
  }
  return {
    marginTop: marginMap[size.value] || marginMap.default,
  }
})
const activeName = ref('wait')

// 模拟数据：未通过、待审核、已通过的数量
const tabCounts = ref({
  no: 0,    // 未通过数量
  wait: 0,  // 待审核数量
  yes: 0   // 已通过数量
})

const noVideos = ref([]);
const waitVideos = ref([]);
const yesVideos = ref([]);
const noVideosTotal = ref(0);
const waitVideosTotal = ref(0);
const yesVideosTotal = ref(0);

const currentTag = ref('wait')

const handleClick = (tab: TabsPaneContext, event: Event) => {
  console.log(tab, event)
  console.log('当前标签:', tab.props.name);
  pageSize.value = 10;
  currentPage.value = 1;
  value2.value = '',
  input.value = '';
 if (tab.props.name === 'no') {
    total.value = noVideosTotal.value;
    currentTag.value = 'no';
  } else if (tab.props.name === 'wait') {
    total.value = waitVideosTotal.value;
    currentTag.value = 'wait';
  } else {
    total.value = yesVideosTotal.value;
    currentTag.value = 'yes';
  }
}

// 分页相关数据
const currentPage = ref(1) // 当前页码
const pageSize = ref(10) // 每页显示条数
const total = ref(0) // 总数据量

// 分页方法
const handleSizeChange = async(val: number) => {
  pageSize.value = val
  currentPage.value = 1 ;
  videoQueryEntity.value.page = currentPage.value;
  videoQueryEntity.value.pageSize = pageSize.value;
  getcurrentVideoList();
}

const handleCurrentChange = async(val: number) => {
  currentPage.value = val
  console.log(currentPage.value)
  videoQueryEntity.value.page = currentPage.value;
  getcurrentVideoList();
}

const setCurrentVideo = (row) => {
  console.log(row)
  videoUrl.value = row.videoUrl;
  coverUrl.value = row.coverUrl;
  Voptions.title = row.title;
  videoKey.value++; // 强制重新渲染

  expandedRows.value = [row.id]
};

const expandedRows = ref([]) // 存储当前展开的行ID

// 处理展开变化
const handleExpandChange = (row) => {
  if (expandedRows.value.includes(row.id)) {
    // 如果点击的是已展开的行，则关闭
    expandedRows.value = []
  } else {
    // 否则展开新行，关闭其他所有行
    expandedRows.value = [row.id]
  }
}

const passEntity = ref({
  id: '',
  status: 0,
  reason: ''
})

const getcurrentVideoList = async() => {
	if(currentTag.value == 'no'){
    videoQueryEntity.value.status = 0;
	const result1 = await getVideoByPage(videoQueryEntity.value.title,
                                      videoQueryEntity.value.beginTime,
                                      videoQueryEntity.value.endTime,
                                      videoQueryEntity.value.status,
                                      videoQueryEntity.value.page,
                                      videoQueryEntity.value.pageSize)
  if(result1.code == 1){
    console.log('未通过视频：',result1.data.rows)
    noVideos.value = result1.data.rows;
    noVideos.value.forEach((item:any) => {
      item.createdTime = formatDate(item.createdTime);
      item.updatedTime = formatDate(item.updatedTime);
    })
    tabCounts.value.no = result1.data.total;
    noVideosTotal.value = result1.data.total;
  }
  }else if(currentTag.value == 'yes'){
  videoQueryEntity.value.status = 1;
  const result2 = await getVideoByPage(
                                      videoQueryEntity.value.title,
                                      videoQueryEntity.value.beginTime,
                                      videoQueryEntity.value.endTime,
                                      videoQueryEntity.value.status,
                                      videoQueryEntity.value.page,
                                      videoQueryEntity.value.pageSize)
  if(result2.code == 1){
   console.log('已通过视频：',result2.data.rows)
    yesVideos.value = result2.data.rows;
    yesVideos.value.forEach((item:any) => {
      item.createdTime = formatDate(item.createdTime);
      item.updatedTime = formatDate(item.updatedTime);
    })
    tabCounts.value.yes = result2.data.total;
    yesVideosTotal.value = result2.data.total;
  }
}else{
  videoQueryEntity.value.status = 2;
  const result3 = await getVideoByPage(videoQueryEntity.value.title,
                                      videoQueryEntity.value.beginTime,
                                      videoQueryEntity.value.endTime,
                                      videoQueryEntity.value.status,
                                      videoQueryEntity.value.page,
                                      videoQueryEntity.value.pageSize)
  if(result3.code == 1){
    console.log('待审核视频：',result3.data.rows)
    waitVideos.value = result3.data.rows;
    waitVideos.value.forEach((item:any) => {
      item.createdTime = formatDate(item.createdTime);
      item.updatedTime = formatDate(item.updatedTime);
    })
    tabCounts.value.wait = result3.data.total;
    waitVideosTotal.value = result3.data.total;
  }
}
}

const formatDate = (dateString) => {
  return dateString.replace('T', ' ');
}

const pass = (id) => {
  ElMessageBox.confirm('确定通过该视频的审核吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async() => {
    passEntity.value.id = id;
    passEntity.value.status = 1;
    const result = await updateVideoStatus(passEntity.value);
    if(result.code == 1){
      ElMessage.success('已通过！')
      getVideoList()
    }
  })
}

const giveVideoNoPass1 = (id) =>{
  dialogTableVisible1.value = true;
  noPassId.value = id;
}

const giveVideoNoPass = async() =>{
  if(passEntity.value.reason == ''){
    ElMessage.error('请填写原因！')
    return;
  }
  passEntity.value.id = noPassId.value;
  const result = await NoPassVideo(passEntity.value);
  if(result.code == 1){
    ElMessage.success('操作成功！')
    passEntity.value.reason = '';
    dialogTableVisible1.value = false;
    getVideoList();
  }	
}

const dialogTableVisible = ref(false)
const dialogTableVisible1 = ref(false)

const noPassId = ref('')

const noPass = (id) => {
  dialogTableVisible.value = true;
  noPassId.value = id;
}

const noPass1 = async() => {
  if(passEntity.value.reason == ''){
    ElMessage.error('请填写原因！')
    return;
  }
  passEntity.value.id = noPassId.value;
  passEntity.value.status = 0;
  const result = await updateVideoStatus(passEntity.value);
  if(result.code == 1){
    ElMessage.success('操作成功！')
    passEntity.value.reason = '';
    dialogTableVisible.value = false;
    getVideoList();
  }	
}

const input = ref('');

const value2 = ref('')

watch(() => value2.value, (newValue, oldValue) => {
   if (!newValue) {
    videoQueryEntity.value.beginTime = '';
    videoQueryEntity.value.endTime = '';
  searchVideo();
    return;
  }
  console.log(newValue)
   videoQueryEntity.value.beginTime = new Date(newValue[0]).toISOString().slice(0, 19);
  videoQueryEntity.value.endTime = new Date(newValue[1]).toISOString().slice(0, 19);
  searchVideo();
})

const searchVideo = async() => {
  videoQueryEntity.value.title = input.value;
  if(currentTag.value == 'wait'){
    videoQueryEntity.value.status = 2;
  }else if(currentTag.value == 'yes'){
    videoQueryEntity.value.status = 1;
  }else if(currentTag.value == 'no'){
    videoQueryEntity.value.status = 0;
  }
  const result = await getVideoByPage(videoQueryEntity.value.title,videoQueryEntity.value.beginTime,videoQueryEntity.value.endTime,videoQueryEntity.value.status,videoQueryEntity.value.page,videoQueryEntity.value.pageSize);
  if(result.code == 1){
    console.log(result.data);
      total.value = result.data.total;
    if(currentTag.value == 'wait'){
      waitVideos.value = result.data.rows;
  }else if(currentTag.value == 'yes'){
    yesVideos.value = result.data.rows;
  }else if(currentTag.value == 'no'){
    noVideos.value = result.data.rows;
  }
  }
}

const shortcuts = [
  {
    text: '7天内',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      return [start, end]
    },
  },
  {
    text: '1个月内',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      return [start, end]
    },
  },
  {
    text: '3个月内',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 3)
      return [start, end]
    },
  },
]

const isLoading = ref(true)

// 初始化时获取数据
onMounted(async() => {
  await getVideoList();
  total.value = waitVideosTotal.value;
  currentTag.value = 'wait';
  isLoading.value = false;
})
</script>

<template>
<div class="loader" v-if="isLoading">
  <div class="wrapper">
    <div class="catContainer">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 733 673"
        class="catbody"
      >
        <path
          fill="#212121"
          d="M111.002 139.5C270.502 -24.5001 471.503 2.4997 621.002 139.5C770.501 276.5 768.504 627.5 621.002 649.5C473.5 671.5 246 687.5 111.002 649.5C-23.9964 611.5 -48.4982 303.5 111.002 139.5Z"
        ></path>
        <path fill="#212121" d="M184 9L270.603 159H97.3975L184 9Z"></path>
        <path fill="#212121" d="M541 0L627.603 150H454.397L541 0Z"></path>
      </svg>
      <svg
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 158 564"
        class="tail"
      >
        <path
          fill="#191919"
          d="M5.97602 76.066C-11.1099 41.6747 12.9018 0 51.3036 0V0C71.5336 0 89.8636 12.2558 97.2565 31.0866C173.697 225.792 180.478 345.852 97.0691 536.666C89.7636 553.378 73.0672 564 54.8273 564V564C16.9427 564 -5.4224 521.149 13.0712 488.085C90.2225 350.15 87.9612 241.089 5.97602 76.066Z"
        ></path>
      </svg>
      <div class="text">
        <span class="bigzzz">Z</span>
        <span class="zzz">Z</span>
      </div>
    </div>
    <div class="wallContainer">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 500 126"
        class="wall"
      >
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="3"
          x2="450"
          y1="3"
          x1="50"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="85"
          x2="400"
          y1="85"
          x1="100"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="122"
          x2="375"
          y1="122"
          x1="125"
        ></line>
        <line stroke-width="6" stroke="#7C7C7C" y2="43" x2="500" y1="43"></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="1.99391"
          x2="115.5"
          y1="43.0061"
          x1="115.5"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="2.00002"
          x2="189"
          y1="43.0122"
          x1="189"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="2.00612"
          x2="262.5"
          y1="43.0183"
          x1="262.5"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="2.01222"
          x2="336"
          y1="43.0244"
          x1="336"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="2.01833"
          x2="409.5"
          y1="43.0305"
          x1="409.5"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="43"
          x2="153"
          y1="84.0122"
          x1="153"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="43"
          x2="228"
          y1="84.0122"
          x1="228"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="43"
          x2="303"
          y1="84.0122"
          x1="303"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="43"
          x2="378"
          y1="84.0122"
          x1="378"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="84"
          x2="192"
          y1="125.012"
          x1="192"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="84"
          x2="267"
          y1="125.012"
          x1="267"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="84"
          x2="342"
          y1="125.012"
          x1="342"
        ></line>
      </svg>
    </div>
  </div>
</div>

  <div class="video-manage" v-else>
    <el-input
      v-model="input"
      style="width: 400px"
      placeholder="请输入视频标题"
      class="video-search"
      @keyup.enter="searchVideo"
    >
    <template #prepend>
        <el-button @click="searchVideo" :icon="Search" />
      </template>
    </el-input>
    <div class="video-datePicker">
    <el-date-picker
      v-model="value2"
      type="datetimerange"
      :shortcuts="shortcuts"
      range-separator="到"
      start-placeholder="开始时间(投稿时间)"
      end-placeholder="结束时间"
    />
    </div>
    <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
      <el-tab-pane name="no">
        <template #label>
          <span>未通过</span>
          <el-badge :value="tabCounts.no" class="badge-item" type="danger" />
        </template>
        <div
        v-if="noVideos.length > 0">
        <el-table
    :data="noVideos"
    class="video-table"
    row-key="id" 
    :expand-row-keys="expandedRows"  
    @expand-change="handleExpandChange"
  >
    <el-table-column type="expand">
      <template #default="props">
        <div m="4" class="video-expand" @vue:mounted="setCurrentVideo(props.row)">
            <el-descriptions
    class="margin-top"
    title="视频详细信息"
    :column="2"
    :size="size"
    border
    label-width="150px"
  >
    <el-descriptions-item
      :rowspan="2"
      :width="140"
      label="视频封面"
      align="center"
    >
      <el-image
        style="width: 500px;"
        :src="props.row.coverUrl"
      />
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">
          <el-icon :style="iconStyle">
            <user />
          </el-icon>
          作者
        </div>
      </template>
      {{ props.row.username }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><ArrowRight /></el-icon>
          作者ID
        </div>
      </template>
      {{ props.row.userId }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Clock /></el-icon>
          视频时长
        </div>
      </template>
      {{ props.row.time }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><CollectionTag /></el-icon>
          标签
        </div>
      </template>
      <el-tag style="margin-left: 5px;" v-for="(item,index) in props.row.tags" size="small">{{ item }}</el-tag>
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><VideoPlay /></el-icon>
          播放量
        </div>
      </template>
      {{ props.row.views }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Pointer /></el-icon>
          点赞量
        </div>
      </template>
      {{ props.row.likes  }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Star /></el-icon>
          收藏量
        </div>
      </template>
      {{ props.row.favorites }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Share /></el-icon>
          转发量
        </div>
      </template>
      {{ props.row.shares }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Coin /></el-icon>
          投币
        </div>
      </template>
      {{ props.row.coins  }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><ChatLineRound /></el-icon>
          评论数
        </div>
      </template>
      {{ props.row.comments }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Switch /></el-icon>
          目前状态
        </div>
      </template>
      <el-tag type="danger">未通过</el-tag>
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Edit /></el-icon>
          修改时间
        </div>
      </template>
      {{ props.row.updatedTime }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><DocumentDelete /></el-icon>
          未予通过原因
        </div>
      </template>
      {{ props.row.reason }}
    </el-descriptions-item>
  </el-descriptions>
  <h3 style="margin-top: 30px;color: rgb(48, 49, 51);">视频源</h3>
  <videoPlay  
  class="video-player"
  ref="aplayVideo" 
  :key="videoKey"
  v-bind="Voptions"  
  @play="onPlay" 
  @pause="onPause"
  @timeupdate="onTimeupdate" 
  @loadedmetadata="loadEdmetadata"
  />
        </div>
      </template>
    </el-table-column>
    <el-table-column align="center" label="ID" prop="id" width="150"/>
    <el-table-column align="center" label="视频标题" prop="title" />
    <el-table-column align="center" label="未予通过原因" prop="reason" />
    <el-table-column align="center" label="投稿时间" prop="createdTime" />
    <el-table-column align="center" label="修改时间" prop="updatedTime" />
  </el-table>
  <el-pagination
  class="el-pagination"
  background
  :current-page="currentPage"
  :hide-on-single-page="total < 10"
  :page-sizes="[10, 20, 50, 100]"
  :page-size="pageSize"
  layout="total, sizes, prev, pager, next, jumper"
  :total="total"
  @size-change="handleSizeChange"
  @current-change="handleCurrentChange"
/>
</div>
  <div v-else>
    <el-empty description="description" />
  </div>
      </el-tab-pane>
      <el-tab-pane name="wait">
        <template #label>
          <span>待审核</span>
          <el-badge :value="tabCounts.wait" class="badge-item" type="info" />
        </template>
        <div
        v-if="waitVideos.length > 0">
        <el-table
    :data="waitVideos"
    class="video-table"
    row-key="id" 
    :expand-row-keys="expandedRows"  
    @expand-change="handleExpandChange"
  >
    <el-table-column type="expand">
      <template #default="props">
        <div m="4" class="video-expand" @vue:mounted="setCurrentVideo(props.row)">
            <el-descriptions
    class="margin-top"
    title="视频详细信息"
    :column="2"
    :size="size"
    border
    label-width="150px"
  >
    <el-descriptions-item
      :rowspan="2"
      :width="140"
      label="视频封面"
      align="center"
    >
      <el-image
        style="width: 500px;"
        :src="props.row.coverUrl"
      />
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">
          <el-icon :style="iconStyle">
            <user />
          </el-icon>
          作者
        </div>
      </template>
      {{ props.row.username }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><ArrowRight /></el-icon>
          作者ID
        </div>
      </template>
      {{ props.row.userId }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Clock /></el-icon>
          视频时长
        </div>
      </template>
      {{ props.row.time }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><CollectionTag /></el-icon>
          标签
        </div>
      </template>
      <el-tag v-for="(item,index) in props.row.tags" size="small">{{ item }}</el-tag>
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><VideoPlay /></el-icon>
          播放量
        </div>
      </template>
      {{ props.row.views }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Pointer /></el-icon>
          点赞量
        </div>
      </template>
      {{ props.row.likes  }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Star /></el-icon>
          收藏量
        </div>
      </template>
      {{ props.row.favorites }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Share /></el-icon>
          转发量
        </div>
      </template>
      {{ props.row.shares }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Coin /></el-icon>
          投币
        </div>
      </template>
      {{ props.row.coins  }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><ChatLineRound /></el-icon>
          评论数
        </div>
      </template>
      {{ props.row.comments }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Switch /></el-icon>
          目前状态
        </div>
      </template>
      <el-tag type="warning">待审核</el-tag>
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Edit /></el-icon>
          修改时间
        </div>
      </template>
      {{ props.row.updatedTime }}
    </el-descriptions-item>
  </el-descriptions>
  <h3 style="margin-top: 30px;color: rgb(48, 49, 51);">视频源</h3>
  <videoPlay  
  class="video-player"
  ref="aplayVideo" 
  :key="videoKey"
  v-bind="Voptions"  
  @play="onPlay" 
  @pause="onPause"
  @timeupdate="onTimeupdate" 
  @loadedmetadata="loadEdmetadata"
  />
  <div class="button-group">
    <el-button type="danger" plain size="large" @click="noPass(props.row.id)">不通过</el-button>
    <el-button type="success" plain size="large" @click="pass(props.row.id)">通过</el-button>
  </div>
        </div>
      </template>
    </el-table-column>
    <el-table-column align="center" label="ID" prop="id" width="150"/>
    <el-table-column align="center" label="视频标题" prop="title" />
    <el-table-column align="center" label="投稿时间" prop="createdTime" />
    <el-table-column align="center" label="修改时间" prop="updatedTime" />
  </el-table>
  <el-pagination
  class="el-pagination"
  background
  :current-page="currentPage"
  :hide-on-single-page="total < 10"
  :page-sizes="[10, 20, 50, 100]"
  :page-size="pageSize"
  layout="total, sizes, prev, pager, next, jumper"
  :total="total"
  @size-change="handleSizeChange"
  @current-change="handleCurrentChange"
/>
</div>
  <div v-else>
    <el-empty description="description" />
  </div>
      </el-tab-pane>
      <el-tab-pane name="yes">
        <template #label>
          <span>已通过</span>
          <el-badge :value="tabCounts.yes" class="badge-item" type="success" />
        </template>
        <div
        v-if="yesVideos.length > 0">
        <el-table
    :data="yesVideos"
    class="video-table"
    row-key="id" 
    :expand-row-keys="expandedRows"  
    @expand-change="handleExpandChange"
  >
    <el-table-column type="expand">
      <template #default="props">
        <div m="4" class="video-expand" @vue:mounted="setCurrentVideo(props.row)">
            <el-descriptions
    class="margin-top"
    title="视频详细信息"
    :column="2"
    :size="size"
    border
    label-width="150px"
  >
    <el-descriptions-item
      :rowspan="2"
      :width="140"
      label="视频封面"
      align="center"
    >
      <el-image
        style="width: 500px;"
        :src="props.row.coverUrl"
      />
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">
          <el-icon :style="iconStyle">
            <user />
          </el-icon>
          作者
        </div>
      </template>
      {{ props.row.username }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><ArrowRight /></el-icon>
          作者ID
        </div>
      </template>
      {{ props.row.userId }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Clock /></el-icon>
          视频时长
        </div>
      </template>
      {{ props.row.time }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><CollectionTag /></el-icon>
          标签
        </div>
      </template>
      <el-tag v-for="(item,index) in props.row.tags" size="small">{{ item }}</el-tag>
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><VideoPlay /></el-icon>
          播放量
        </div>
      </template>
      {{ props.row.views }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Pointer /></el-icon>
          点赞量
        </div>
      </template>
      {{ props.row.likes  }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Star /></el-icon>
          收藏量
        </div>
      </template>
      {{ props.row.favorites }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Share /></el-icon>
          转发量
        </div>
      </template>
      {{ props.row.shares }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Coin /></el-icon>
          投币
        </div>
      </template>
      {{ props.row.coins  }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><ChatLineRound /></el-icon>
          评论数
        </div>
      </template>
      {{ props.row.comments }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Switch /></el-icon>
          目前状态
        </div>
      </template>
      <el-tag type="warning">待审核</el-tag>
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Edit /></el-icon>
          修改时间
        </div>
      </template>
      {{ props.row.updatedTime }}
    </el-descriptions-item>
  </el-descriptions>
  <h3 style="margin-top: 30px;color: rgb(48, 49, 51);">视频源</h3>
  <videoPlay  
  class="video-player"
  ref="aplayVideo" 
  :key="videoKey"
  v-bind="Voptions"  
  @play="onPlay" 
  @pause="onPause"
  @timeupdate="onTimeupdate" 
  @loadedmetadata="loadEdmetadata"
  />
  <div class="button-group1">
    <el-button type="danger" plain size="large" @click="giveVideoNoPass1(props.row.id)">下架</el-button>
  </div>
        </div>
      </template>
    </el-table-column>
    <el-table-column align="center" label="ID" prop="id" width="150"/>
    <el-table-column align="center" label="视频标题" prop="title" />
    <el-table-column align="center" label="投稿时间" prop="createdTime" />
    <el-table-column align="center" label="修改时间" prop="updatedTime" />
  </el-table>
  <el-pagination
  class="el-pagination"
  background
  :current-page="currentPage"
  :hide-on-single-page="total < 10"
  :page-sizes="[10, 20, 50, 100]"
  :page-size="pageSize"
  layout="total, sizes, prev, pager, next, jumper"
  :total="total"
  @size-change="handleSizeChange"
  @current-change="handleCurrentChange"
/>
</div>
  <div v-else>
    <el-empty description="description" />
  </div>
      </el-tab-pane>
    </el-tabs>
  </div>

  <el-dialog v-model="dialogTableVisible" title="不通过审核" width="800">
    <el-form-item label="原因">
      <el-input v-model="passEntity.reason" />
    </el-form-item>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogTableVisible = false">取消</el-button>
        <el-button type="danger" @click="noPass1">确认
        </el-button>
      </div>
    </template>
  </el-dialog>
   <el-dialog v-model="dialogTableVisible1" title="下架视频" width="800">
    <el-form-item label="原因">
      <el-input v-model="passEntity.reason" />
    </el-form-item>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogTableVisible1 = false">取消</el-button>
        <el-button type="danger" @click="giveVideoNoPass">确认
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
/* From Uiverse.io by vinodjangid07 */ 
.loader {
  width: fit-content;
  height: fit-content;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 200px;
  margin-left: 560px;
}
.wrapper {
  width: fit-content;
  height: fit-content;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.catContainer {
  width: 100%;
  height: fit-content;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.catbody {
  width: 80px;
}
.tail {
  position: absolute;
  width: 17px;
  top: 50%;
  animation: tail 0.5s ease-in infinite alternate-reverse;
  transform-origin: top;
}
@keyframes tail {
  0% {
    transform: rotateZ(60deg);
  }
  50% {
    transform: rotateZ(0deg);
  }
  100% {
    transform: rotateZ(-20deg);
  }
}
.wall {
  width: 300px;
}
.text {
  display: flex;
  flex-direction: column;
  width: 50px;
  position: absolute;
  margin: 0px 0px 100px 120px;
}
.zzz {
  color: black;
  font-weight: 700;
  font-size: 15px;
  animation: zzz 2s linear infinite;
}
.bigzzz {
  color: black;
  font-weight: 700;
  font-size: 25px;
  margin-left: 10px;
  animation: zzz 2.3s linear infinite;
}
@keyframes zzz {
  0% {
    color: transparent;
  }
  50% {
    color: black;
  }
  100% {
    color: transparent;
  }
}


.video-datePicker {
  text-align: center;
  border-right: solid 1px var(--el-border-color);
  flex: 1;
}
.video-datePicker:last-child {
  border-right: none;
}
.video-datePicker .demonstration {
  display: block;
  color: var(--el-text-color-secondary);
  font-size: 14px;
  margin-bottom: 20px;
}

.video-datePicker{
  position: absolute;
  margin-top: 7px;
  margin-left: 60px;
}

.video-search{
  position: absolute;
  right: 60px;
  margin-top: 7px;
}

.button-group1{
  margin-bottom: 20px;
  display:flex;
  justify-content:flex-end;
  margin-top: 30px;
}

.button-group{
  margin-bottom: 20px;
  display:flex;
  justify-content: space-between;
  margin-top: 30px;
}

.video-player{
  z-index: 10!important;
}

/* 视频播放器全屏模式下的 z-index */
:deep(.video-player.fullscreen) {
  z-index: 1000 !important; /* 确保全屏时最高 */
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
  margin-bottom: 20px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .el-pagination {
    flex-wrap: wrap;
  }
  .el-pagination__jump {
    margin-top: 10px;
  }
}

.el-descriptions {
  margin-top: 20px;
}
.cell-item {
  display: flex;
  align-items: center;
}
.margin-top {
  margin-top: 20px;
}

.video-expand-row3{
    margin-top: 20px;
}

.video-expand-row1{
    gap: 30px;
    margin-top: 20px;
    margin-left: 50px;
}

.video-expand-row2{
    gap: 30px;
    margin-bottom: 20px;
    margin-left: 50px;
}

.el-row{
  margin-bottom: 10px;
}

.video-expand{
    justify-self: center;
    width: 90%;
}

/* 外层容器固定高度，并启用滚动 */
.video-manage {
  background-color: rgb(240, 248, 255);
  width: 1400px;
  height: calc(100vh - 100px); /* 动态高度（视窗高度 - 顶部间距） */
  margin-top: 20px;
  overflow-y: auto; /* 垂直滚动 */
  position: relative;
  justify-self: center;
}

/* 表格宽度适配 */
.video-table {
  width: 1350px; /* 自动填充父容器宽度 */
  margin-bottom: 20px;
  justify-self: center;
}

.demo-tabs {
  align-items: center;
}

/* 标签页内容区域固定高度 */
.demo-tabs > .el-tabs__content {
  padding: 16px; /* 减少内边距 */
  color: #6b778c;
  min-height: calc(100% - 50px); /* 确保内容区域占满剩余空间 */
}

/* :deep(.el-tabs__header) {
  position: sticky;
  top: 0px;
  background-color: rgb(240, 248, 255); 
  z-index: 1;
} */

/* 调整 badge 样式 */
.badge-item {
  margin-left: 8px;
}

/* 自定义 badge 颜色 */
:deep(.el-badge__content.is-fixed) {
  &.el-badge__content--danger {
    background-color: #f56c6c;
  }
  &.el-badge__content--info {
    background-color: #909399;
  }
  &.el-badge__content--success {
    background-color: #67c23a;
  }
}
</style>