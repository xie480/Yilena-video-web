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
  User,
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
  pageSize.value = 10;
  currentPage.value = 1;
    if(tab.uid == 28){
      total.value = noVideosTotal.value;
      currentTag.value = 'no';
    }else if(tab.uid == 31){
      total.value = waitVideosTotal.value;
      currentTag.value = 'wait';
    }else{
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
    tabCounts.value.wait = result3.data.total;
    waitVideosTotal.value = result3.data.total;
  }
}
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

// 初始化时获取数据
onMounted(async() => {
  await getVideoList();
  total.value = waitVideosTotal.value;
  currentTag.value = 'wait';
})
</script>

<template>
  <div class="video-manage">
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
            <el-icon :style="iconStyle"><CircleCheck /></el-icon>
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
    <el-table-column align="center" label="投稿日期" prop="createdTime" />
    <el-table-column align="center" label="修改日期" prop="updatedTime" />
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
            <el-icon :style="iconStyle"><CircleCheck /></el-icon>
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
    <el-table-column align="center" label="投稿日期" prop="createdTime" />
    <el-table-column align="center" label="修改日期" prop="updatedTime" />
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
            <el-icon :style="iconStyle"><CircleCheck /></el-icon>
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
    <el-table-column align="center" label="投稿日期" prop="createdTime" />
    <el-table-column align="center" label="修改日期" prop="updatedTime" />
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