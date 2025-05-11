<script lang="ts" setup>
import { inject,ref,watch, reactive, onMounted, defineProps,computed,onUnmounted } from 'vue'
import { useRouter,useRoute } from 'vue-router';
import { useTransition } from '@vueuse/core'
import CryptoJS from 'crypto-js'
import { ElMessage,ElMessageBox } from 'element-plus';
import { getUserInfoByUsername,getHotSearch } from '../../api';
import { addPost } from '../../api/post';
import { getIsShare,giveShare,getRecommendVideo,getUserFollow,giveComment,getVideoComments,getAuthorIsFollow,giveAuthorFollow,giveAuthorUnfollow,getDanmuEach5s,getAuthorInfo,getVideo,giveToFavorites,giveToUnFavorites,getDanmuCount,getFavorite,like,dislike,getIsLike,getIsCoins,giveCoins, getUserFavoritesList } from '../../api/video';
import vueDanmaku from "vue3-danmaku";
import axios from 'axios';
import { debounce } from 'lodash';
// 视频播放组件
import "vue3-video-play/dist/style.css";
	import {videoPlay} from "vue3-video-play";
 // 在原有导入基础上添加
import { nextTick } from 'vue'
import { addHistory } from '../../api/history';

const activeTab = ref('hot') // 默认激活"最热"

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

const searchText = ref('');

    // 搜索框历史记录
const searchHistory = ref([]);

// 最大历史记录数量（假设每行5个按钮，最多3行）
const maxHistoryCount = 10;

const addSearchHistory = (newSearch) => {
  if (!searchHistory.value.includes(newSearch)) {
    // 将新搜索记录添加到最前面
  searchHistory.value.unshift(newSearch);
  }else{
    searchHistory.value.splice(searchHistory.value.indexOf(newSearch), 1);
    searchHistory.value.unshift(newSearch);
  }

   // 如果超过最大数量，移除最旧的记录
   if (searchHistory.value.length > maxHistoryCount) {
    searchHistory.value.pop();
  }
   // 保存到localStorage
   localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value));
};

const goToAnotherVideo = (id) =>{
  router.push({path: '/video/'+id}).then(() => {
      window.location.reload();
    });
}

const leftRecommendVideos = ref([]);

const getCurrentLeftRecommendVideos = async() =>{
  const result = await getRecommendVideo();
  if(result.code === 1){
    leftRecommendVideos.value = result.data;
    console.log(leftRecommendVideos.value[leftRecommendVideos.value.length - 1]);
    for(let i = 0; i < leftRecommendVideos.value.length; i++){
      if(leftRecommendVideos.value[i].id === currentVideoId.value){
        leftRecommendVideos.value.splice(i, 1);
      }
    }
  }else{
    ElMessage.error('获取推荐视频失败',result.msg);
  }
}

const getFollowEntity = ref({
  id:'',
  username: '',
  page: '',
  pageSize: ''
})

const getCurrentUserFolow = async() =>{
	getFollowEntity.value.id = currentUserId.value;
	getFollowEntity.value.page = 1;
	getFollowEntity.value.pageSize = 1000;
	const result = await getUserFollow(getFollowEntity.value.id,getFollowEntity.value.username,getFollowEntity.value.page,getFollowEntity.value.pageSize);
	if(result.code === 1){
		AtData.value = result.data.rows;
	}	else{
		ElMessage.error('获取关注列表失败',result.msg);
  }
}

const AtDialogVisibleOn = () =>{
  AtDialogVisible.value = true;
  getCurrentUserFolow();
}

const AtSonDialogVisibleOn = () =>{
  AtSonDialogVisible.value = true;
  getCurrentUserFolow();
}

const AtReslut = (type) =>{
  AtDialogVisible.value = false;
  AtSonDialogVisible.value = false;
  if(type === 1){
  commentInput.value = '@' + AtRows.value + ' ';
  }else{
  commentSonInput.value = '@' + AtRows.value + ' ';
  }
}

const AtRows = ref([])  // 存储选中行数据

// 监听表格选择变化
const handleAtSelectionChange = (selection) => {
  AtRows.value = selection.map(row => row.username);
}

const commentEntity = ref({
  entityType: '',
  entityId: '',
  content: '',
  imageUrl: ''
})

const giveCurrentVideoComment = async() =>{
    commentEntity.value.entityType = 1;
    commentEntity.value.entityId = currentVideoId.value;
    commentEntity.value.content = commentInput.value;
    const result = await giveComment(commentEntity.value);
    if(result.code === 1){
      ElMessage.success('评论成功');
      commentEntity.value.content = '';
      commentEntity.value.imageUrl = '';
      commentInput.value = '';
      switchTab(activeTab.value);
    }	else{
      ElMessage.error('评论失败',result.msg);
    }
}

const giveCurrentCommentComment = async(id) =>{
    commentEntity.value.entityType = 3;
    commentEntity.value.entityId = id;
    commentEntity.value.content = commentSonInput.value;
    const result = await giveComment(commentEntity.value);
    if(result.code === 1){
      ElMessage.success('回复成功');
      commentEntity.value.content = '';
      commentEntity.value.imageUrl = '';
      commentSonInput.value = '';
      switchTab(activeTab.value);
    }	else{
      ElMessage.error('回复失败',result.msg);
    }
}

const token = ref('');

const toUpload = () => {
      router.push('/upload').then(() => {
        window.location.reload();
      })
    }

//文件上传
// 图片上传成功后触发
const handleAvatarSuccess = (response,uploadFile) => {
  commentEntity.value.imageUrl = response.data;
  ElMessage.success('上传成功');
}
// 文件上传之前触发
const beforeAvatarUpload = (rawFile) => {
  ElMessage.info('请等待“上传成功”提示……')
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('只支持上传图片')
    return false
  } else if (rawFile.size / 1024 / 1024 > 10) {
    ElMessage.error('只能上传10M以内图片')
    return false
  }
  return true
}

const commentList = ref([{
  id: '',
  userId:'',
  username: '',
  image: '',
  content: '',
  imageUrl: '',
  likes: '',
  comments: '',
  createdTime: '',
  createdTimestamp: '',
  updatedTime: '',
  isCommentLike: '',
  commentList: [{
    id: '',
    userId:'',
    username: '',
    image: '',
    content: '',
    imageUrl: '',
    likes: '',
    comments: '',
    createdTime: '',
    updatedTime: '',
    isCommentLike: ''
  }]
}]);

const getCurrentVideoComments = async() =>{
  let result = null;
  if(activeTab.value === 'hot'){
    result = await getVideoComments(currentVideoId.value,1,1);
  }else{
    result = await getVideoComments(currentVideoId.value,1,2);
  }
  if(result.code === 1){
    commentList.value = result.data;
    for (const item of commentList.value) {
      item.createdTimestamp = new Date(item.createdTime).getTime();
      const resultSon = await getVideoComments(item.id, 3 ,1);
      if (resultSon.code === 1) {
        item.commentList = resultSon.data;

        for (const itemSon of item.commentList) {
          const result = await getIsLike(userInfo.value.id, itemSon.id, 3);
          if (result.code === 1) {
            if (result.data != null) {
              itemSon.isCommentLike = true;
            } else {
              itemSon.isCommentLike = false;
            }
          } else {
            ElMessage.error('获取点赞状态失败！', result.msg);
          }
        }
      } else {
        ElMessage.error('获取回复失败', resultSon.msg);
      }

      const result = await getIsLike(userInfo.value.id,item.id,3);
      if(result.code === 1){
        if(result.data != null){
          item.isCommentLike = true;
        }else{
          item.isCommentLike = false;
        }
      }else{
        ElMessage.error('获取点赞状态失败！',result.msg);
      }
    }
  }else{
    ElMessage.error('获取评论失败',result.msg);
  }
}

// 方法部分
const processCommentContent = (text) => {
  // 使用正则匹配 @+中文/英文+空格 的模式
  const mentionRegex = /@([\u4e00-\u9fa5a-zA-Z]+)\s/g
  console.log('原始文本:', text)
  
  // 替换为可点击的span
  return text.replace(mentionRegex, (match, username) => {
    return `<span class="mention" 
            style="color: #1890ff; cursor: pointer;" 
            data-username="${username}">@${username} </span>`
  })
}

// 点击事件处理
const handleMentionClick = async(username) => {
  console.log('点击的用户名:', username)
  const result = await getUserInfoByUsername(username);
  if(result.code === 1){
    const userInfo1 = result.data;
    if(userInfo1.id === userInfo.value.id){
    router.push(`/userInfo/${userInfo1.id}`).then(() => {
      window.scrollTo(0, 0); // 滚动到页面顶部
      window.location.reload();
     });
    }else{
      router.push(`/userInfo/others/${userInfo1.id}`).then(() => {
        window.scrollTo(0, 0); // 滚动到页面顶部
      window.location.reload();
     });
    }
  }
}

// 初始化响应式数组
const isToComment = ref([]);

// 切换指定评论的回复框状态
const toCommentSon = (index) => {
  isToComment.value[index] = !isToComment.value[index];
}

const formatViews = (views)  =>{
  if (views >= 10000) {
    return `${Math.round(views / 1000) / 10}万`; // 保留1位小数（如：1.3万）
  }
  return views.toString();
}

const formatDate = (timestamp) => {
  const date = new Date(timestamp);
  
  // 补零函数
  const pad = n => n.toString().padStart(2, '0');
  
  return `
    ${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())}
    ${pad(date.getHours())}:${pad(date.getMinutes())}
  `.replace(/\s+/g, ' ').trim();
}

const router = useRouter();

const currentVideo = ref({
  id: '',
  title: '',
  coverUrl: '',
  videoUrl: '',
  description: '',
  tags: [],
  sort:'',
  barrages:'',
  views:'',
  likes:'',
  favorites:'',
  shares: '',
  coins: '',
  comments:'',
  userId: '',
  collecttionId: '',
  visibility: '',
  time:'',
  createdTime: '',
  updatedTime: ''
})

const authorInfo = ref({
  id: '',
  username: '',
  image: '',
  description: '',
  followersCount: ''
})

const getCurrentAuthorInfo = async() =>{
  const result = await getAuthorInfo(currentVideo.value.userId);
  if(result.code === 1){
    authorInfo.value = result.data;
    fansCount.value = authorInfo.value.followersCount;
    followEntity.value.followedId = authorInfo.value.id;
  }else{
    ElMessage.error('获取作者信息失败',result.msg);
  }
}


// 计算属性统计换行符（兼容不同系统换行符）
const hhfCount = computed(() => {
  // 处理 URL 编码的换行符（如 %0A）
  const decodedText = decodeURIComponent(descriptionText.value);
  
  // 直接统计所有 \n 的出现次数
  let count = 0;
  let pos = -1;
  
  while ((pos = decodedText.indexOf('\n', pos + 1)) !== -1) {
    count++;
  }
  
  return count;
})
const tags = ref([]);

const videoId = router.currentRoute.value.params.videoId;

const getCurrentVideo = async() =>{
  const result = await getVideo(videoId);
  if(result.code === 1){
    currentVideo.value = result.data;
    descriptionText.value = currentVideo.value.description;
    tags.value = currentVideo.value.tags;
    danmuCount.value = currentVideo.value.barrages;
    likesCount.value = currentVideo.value.likes;
    sharesCount.value = currentVideo.value.shares;
    coinsCount.value = currentVideo.value.coins;
    favoritesCount.value = currentVideo.value.favorites;
    videoUrl.value = currentVideo.value.videoUrl;
    coverUrl.value = currentVideo.value.coverUrl;
    currentVideoId.value = currentVideo.value.id;
  }else{
    ElMessage.error('获取视频失败',result.msg);
  }
}

const cson = ref(2);

// 控制回复列表显示状态
const showReplies = ref({})

// 切换显示状态的方法
const toggleReplies = (index) => {
  showReplies.value[index] = true
}

const commentInput = ref('');

const commentSonInput = ref('');



const sortedComments = computed(() => {
  return [...commentList.value].sort((a, b) => {
    return activeTab.value === 'hot' 
      ? new Date(b.createdTime) - new Date(a.createdTime) 
      : b.likes - a.likes
  })
})

const switchTab = async(tab) => {
  activeTab.value = tab;
  await getCurrentVideoComments();
}

const isContinue = ref(true)

// 新增响应式变量
const descTagsContainer = ref(null)
const descTagsContent = ref(null)
const descTagsHeight = ref(0)
const hasDescTagsScroll = ref(false)

// 容器动态样式计算
const descTagsStyle = computed(() => ({
  maxHeight: descTagsHeight.value > window.innerHeight * 0.4 
    ? `${window.innerHeight * 0.4}px` 
    : 'none',
  overflowY: hasDescTagsScroll.value ? 'auto' : 'visible',
  transition: 'max-height 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
}))

// 高度检测方法（防抖处理）
const checkDescTagsHeight = debounce(() => {
  if (!descTagsContent.value || !descTagsContainer.value) return
  
  const contentHeight = descTagsContent.value.scrollHeight
  const viewportThreshold = window.innerHeight * 0.4
  
  hasDescTagsScroll.value = contentHeight > viewportThreshold
  descTagsHeight.value = hasDescTagsScroll.value 
    ? viewportThreshold 
    : contentHeight + 20 // 添加缓冲高度
}, 100)

// 初始化观察器
const initDescTagsObserver = () => {
  const observer = new ResizeObserver(checkDescTagsHeight)
  if (descTagsContent.value) {
    observer.observe(descTagsContent.value)
  }
  onUnmounted(() => observer.disconnect())
}

const toHistory = () => {
      router.push('/history').then(() => {
        window.location.reload();
});
    }



    const toPost = () => {
      router.push('/post').then(() => { 
        window.location.reload();
});
    }

    const toChat = () => {
      router.push('/chat').then(() => {
        window.location.reload();
});
    }

const props = defineProps({
  descriptionText: String
})

const descriptionText = ref('');

const scrollContainer = ref(null)
const contentElement = ref(null)
const loading = ref(false)
const hasScroll = ref(false)
const maxHeight = ref(null)

// 智能计算容器样式
const containerStyle = computed(() => ({
  overflowY: hasScroll.value ? 'auto' : 'visible',
  maxHeight: maxHeight.value ? `${maxHeight.value}px` : 'none',
  transition: 'max-height 0.3s ease'
}))

// 判断是否启用无限滚动
const shouldEnableInfinite = computed(() => hasScroll.value && !loading.value)

// 自动高度检测
const checkContentHeight = async () => {
  await nextTick()
  
  if (!contentElement.value || !scrollContainer.value) return
  

  console.log('检测高度',hhfCount.value)
 // 当换行符数量 >=3 时启用滚动逻辑
 if (hhfCount.value >= 3) {
  console.log('启用滚动逻辑')
    hasScroll.value = true
    maxHeight.value = scrollContainer.value.clientHeight
  } else {
    hasScroll.value = false
    // 根据行数动态计算高度（兼容不同字体/行高）
    const lineHeight = parseInt(getComputedStyle(contentElement.value).lineHeight) || 24
    maxHeight.value = (hhfCount.value + 1) * lineHeight + 20 // +1包含首行，+20缓冲高度
  }
}

// 监听内容变化
watch(() => props.descriptionText, checkContentHeight)

// 模拟加载方法
const videoDescriptionLoad = async () => {
  loading.value = true
  // 模拟数据加载
  await new Promise(resolve => setTimeout(resolve, 1000))
  loading.value = false
  // 加载后重新检测高度
  checkContentHeight()
}

const viewsPeople = ref(0);
const danmuCount = ref(0);

const getCurrentVideoDanmuCount = async() =>{
  const result = await getDanmuCount(videoId);
  if(result.code === 1){
    danmuCount.value = result.data;
  }else{
    ElMessage.error('获取弹幕数量失败',result.msg);
  }
}

const selectedRows = ref([])  // 存储选中行数据

// 监听表格选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection.map(row => row.id);
  console.log('选中的行数据：', selectedRows.value);
}

const tableData = ref([]);

const AtData = ref([]);

const count = ref(0)
const CollectDialogLoad = () => {
  count.value += 2
}

const centerDialogVisible = ref(false)

const AtDialogVisible = ref(false)

const AtSonDialogVisible = ref(false)

const CoinsResult = () =>{
  giveCurrentVideoCoins();
}

const FavoritesReslut = () =>{
	giveCurrentVideoFavorites();
  centerDialogVisible.value = false;
}

const selectedCoins = ref('two');

const selectCoin = (count) => {
  selectedCoins.value = count;
  if(count === 'one'){
    coinsToUp.value = 1;
  }else{
    coinsToUp.value = 2;
  }
}

const coinsToUp = ref(2);

const ConisDialogVisible = ref(false);

const isLike = ref(false);
const isCoin = ref(false);
const isFavorites = ref(false);
const isShare = ref(false);

let likesCount = ref(0);
let coinsCount = ref(0);
let favoritesCount = ref(0);
let sharesCount = ref(0);

const toLike = () => {
  if(isLike.value){
  	dislikeCurrentVideo();
  }else{
  	likeCurrentVideo();
  }
}

const toCommentLike = async(comment) => {
    if (comment.isCommentLike) {
      await dislikeCurrentComment(comment);
    } else {
      await likeCurrentComment(comment);
    }
}

const dislikeCurrentComment = async(comment) => {
  likeEntity.entityId = comment.id;
  likeEntity.entityType = 3;
  likeEntity.value.userId = userInfo.value.id;
  const result = await dislike(likeEntity);
  if(result.code === 1){
    comment.isCommentLike = false;
    comment.likes -= 1; // 直接更新前端计数
  }	else{
    ElMessage.error('取消点赞失败',result.msg);
  }
}

const likeCurrentComment = async(comment) => {
  likeEntity.value.entityId = comment.id;
  likeEntity.value.entityType = 3;
  likeEntity.value.userId = userInfo.value.id;
  const result = await like(likeEntity.value);
  if(result.code === 1){
    comment.isCommentLike = true;
    comment.likes += 1; // 直接更新前端计数
  }else{
    ElMessage.error('点赞失败',result.msg);
  }
}

const getCurrentVideoIsLike = async() =>{
  const result = await getIsLike(userInfo.value.id,currentVideoId.value,1);
  if(result.code === 1){
    if(result.data != undefined){
      isLike.value = true;
    }else{
      isLike.value = false;
    }
  }else{
    ElMessage.error('获取点赞状态失败！',result.msg);
  }
}

const likeEntity = ref({ 
  entityId: '',
  entityType: '',
  userId: '',
})

const likeCurrentVideo = async() => {
  likeEntity.value.entityId = currentVideoId.value;
  likeEntity.value.entityType = 1;
  likeEntity.value.userId = userInfo.value.id;
  const result = await like(likeEntity.value);
  if(result.code === 1){
    likesCount.value += 1;
    isLike.value = true;
  }else{
    ElMessage.error('点赞失败',result.msg);
  }
}

const dislikeCurrentVideo = async() => {
  likeEntity.entityId = currentVideoId.value;
  likeEntity.entityType = 1;
  const result = await dislike(likeEntity);
  if(result.code === 1){
    likesCount.value -= 1;
    isLike.value = false;	
  }	else{
    ElMessage.error('取消点赞失败',result.msg);
  }
}

const toCoin = () => {
  if(!isCoin.value){
  	ConisDialogVisible.value = true;
    isCoin.value = true;	
  }else{
    ElMessage.info('您已经投过币了');
  }
}

const getCurrentVideoCoins = async() =>{
  const result = await getIsCoins(currentVideoId.value,userInfo.value.id);
  if(result.code == 1){
    if(result.data != null){
      isCoin.value = true;
    }else{
      isCoin.value = false;
    }
  }else{
    ElMessage.error("获取投币状态失败！");
  }
}

const coinsEntity = ref({
  videoId: '',
  userId: '',	
  coinsCount: ''
})

const giveCurrentVideoCoins = async() => {
  coinsEntity.videoId = currentVideoId.value;
  coinsEntity.userId = userInfo.value.id;
  coinsEntity.coinsCount = coinsToUp.value;
  const result = await giveCoins(coinsEntity);
  if(result.code === 1){
    coinsCount.value += coinsToUp.value;
    ConisDialogVisible.value = false;
    isCoin.value = true;
  }else{
    ElMessage.error('投币失败',result.msg);
    isCoin.value = false;
  }
}

const CoinsHandleClose = () => {
  ConisDialogVisible.value = false;	
  isCoin.value = false;
}

const getCurrentVideoFavorites = async() =>{
  const result = await getFavorite(currentVideoId.value,userInfo.value.id);
  if(result.code === 1){
    if(result.data != null){
      isFavorites.value = true;
    }else{
      isFavorites.value = false;
    }
  }else{
    ElMessage.error('获取收藏状态失败！',result.msg);
  }
}

const getUserFavorites = async() =>{
  const result = await getUserFavoritesList();
  if(result.code === 1){
    tableData.value = result.data;
  }else{
    ElMessage.error('获取收藏列表失败！',result.msg);
  }
}

const FavoritesEntity = ref({
  id: '',
  favoritesId: ''
})

const giveCurrentVideoFavorites = async() =>{
  FavoritesEntity.id = currentVideoId.value;
  if (selectedRows.value.length > 0) {
    FavoritesEntity.favoritesId = selectedRows.value[0];
    const result = await giveToFavorites(FavoritesEntity);
    
    if (result.code === 1) {
      favoritesCount.value += 1;
      isFavorites.value = true;
      ElMessage.success('收藏成功');
    } else {
      ElMessage.error(result.msg);
    }
  } else {
    ElMessage.warning('请先选择收藏夹');
  }
}

const giveCurrentVideoUnFavorite = async() =>{
  FavoritesEntity.id = currentVideoId.value;
  const result = await giveToUnFavorites(FavoritesEntity);
  if(result.code === 1){
    favoritesCount.value -= 1;
    isFavorites.value = false;
  }else{
    ElMessage.error(result.msg);
  }
}

const toFavorites = () => {
  if(isFavorites.value){
  	giveCurrentVideoUnFavorite();
  }else{
    centerDialogVisible.value = true;
  }
}

const gotoFavorites = () => {
      router.push('/userInfo/' + userInfo.value.id + '/favorites').then(() => {
        window.location.reload();
      })
    }

const toShare = async() => {
  if(!isShare.value){
    shareDialogVisible.value = true;
    shareInput.value = '';
  }	else{
    ElMessage.info('您已经转发过了');
  }
}

const getCurrentVideoIsShare = async() =>{
  const result = await getIsShare(userInfo.value.id,currentVideoId.value,1);
  if(result.code === 1){
    console.log('当前转发状态：',result.data)
    if(result.data != undefined){
      isShare.value = true;	
    }	else{
      isShare.value = false;
    }
  }else{
    ElMessage.error('获取转发状态失败！',result.msg);
  }
}

    // AES加密（自动处理URL安全编码）
    const encryptSearchText = (text) => {
  const encrypted = CryptoJS.AES.encrypt(text, 'yilena-key').toString()
  return btoa(encrypted).replace(/\+/g, '-').replace(/\//g, '_')
}

// 处理空格键事件
const handleSpace = () => {
  if (searchText.value.trim() !== '') { 
    addSearchHistory(searchText.value.trim()); // 传递处理后的内容
  }
};

const videoCurDuration = ref(0) // 当前播放秒数
const videoAllDuration = ref(0) // 总时长秒数
const isDanmuReady = ref(false); // 弹幕是否已初始化
const pageSize = ref(40) // 每次获取的弹幕数量

// 定时器
let intervalId = null

const danmuDTO = ref({
  videoId:'',
  currentTime: '',
  pageSize: ''
})

// 获取弹幕
const fetchDanmu = async() => {

  if(!isActive.value){
    return;
  }

  const currentTime = videoCurDuration.value
  danmuDTO.value.videoId = currentVideoId.value
  danmuDTO.value.currentTime = currentTime
  danmuDTO.value.pageSize = pageSize.value
  const reslut = await getDanmuEach5s(danmuDTO.value);
  if(reslut.code === 1){
  	reslut.data.map(item => {
      danmakuInstance.value.add(item.content);
    });
    console.log(reslut.data)
  }else{
  	ElMessage.error('获取弹幕失败');
  }
}

// 监听视频播放结束事件
const onEnded = () => {
  console.log("视频播放结束");
  danmakuInstance.value.stop(); // 清空弹幕
  danmus.value = []; // 重置弹幕数据
  clearDanmuInterval(); // 清除定时器
  if(isContinue.value){
    const nextId = leftRecommendVideos.value[0].id;
    router.push({path: '/video/'+nextId}).then(() => {
      window.location.reload();
    });
  }
};


// 初始化定时器
const startInterval = () => {
  fetchDanmu();
  intervalId = setInterval(() => {
    console.log('定时器触发')
    fetchDanmu()
  }, 5000) // 每5秒调用一次
}

// 清除定时器
const clearDanmuInterval = () => {
  if (intervalId) {
    clearInterval(intervalId)
    intervalId = null
  }
}

// 监听视频时间更新
const onTimeupdate = (event) => {
  videoCurDuration.value = Math.floor(event.target.currentTime)
}

const loadEdmetadata = (ev) => {
    videoAllDuration.value = ev.target.duration // 总时长秒数
}

// 跳转开始事件
const handleSeeking = () => {
  console.log("开始跳转进度");
  
  // 清空弹幕区
  if(danmakuInstance.value) {
    danmakuInstance.value.stop(); // 停止并清空弹幕
  }
  
  // 停止定时器
  clearDanmuInterval();
  
  // 重置弹幕数据
  danmus.value = [];
}

// 跳转完成事件
const handleSeeked = async () => {
  console.log("完成进度跳转");
  
  isDanmuReady.value = true;
  
  // 重启定时器
  startInterval();
  
  // 同步播放状态
  if(!options.paused) {
    danmakuInstance.value.play();
  }
}

const colorList = ref([
  "rgb(204,255,255)",
  "white",
  "rgb(204,255,204)",
  "white",
  "rgb(0,255,255)",
  "white",
  "rgb(255,204,255)",
  "pink",
]);
// 生成随机颜色的函数
const getRandomColor = () => {
  const color = colorList.value[Math.floor(Math.random() * 8)];
  return color;
};

// 弹幕相关响应式变量
const newDanmu = ref('')
const danmakuInstance = ref(null)
const currentVideoId = ref('') // 根据实际视频ID设置

// WebSocket连接对象
let ws = null;

// 发送弹幕方法（与用户信息整合）
const sendDanmu = async () => {
  if (!isActive.value) return
  try {
    if (!danmakuInstance.value) {
        ElMessage.warning('请等待视频加载完成')
        return
    }
    if (!newDanmu.value.trim()) {
      ElMessage.warning('弹幕内容不能为空')
      return
    }

    const loginUser = JSON.parse(localStorage.getItem('loginUser'))
    if (!loginUser) {
      ElMessage.error('请先登录！')
      return
    }


    // 获取视频的当前播放时间
    const progress = videoCurDuration.value;

    const danmuData = {
      content: newDanmu.value,
      videoId: currentVideoId.value,
      videoTime: progress,
      username: loginUser.username,
      color: '#ffffff',
      createTime: new Date().toISOString() // 当前时间
    }

    // WebSocket发送（推荐）
    if (ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(danmuData))
      console.log('弹幕发送成功:', JSON.stringify(danmuData))
    } else {
      ElMessage.error('发送失败！请检查网络连接。')
    }

    // 本地即时显示
    danmakuInstance.value.add(danmuData.content);

    newDanmu.value = ''
    nextTick(() => document.querySelector('.danmu-input')?.focus())
  } catch (error) {
    ElMessage.error('弹幕发送失败: ' + error.message)
    console.log(error)
  }
}

const isActive = ref(true)

const isPlay = ref(true);

const toggleIcon = () => {
  isActive.value = !isActive.value

  if (isActive.value && isPlay.value) {
    // 激活按钮，显示弹幕
    danmakuInstance.value?.play();
    fetchDanmu(); // 获取并显示弹幕
  } else {
    // 未激活按钮，隐藏弹幕
    danmakuInstance.value?.stop();
    danmus.value = []; // 清空弹幕数据
  }
}

const danmus = ref([]);

const isFollowing = ref(false)
  const isSpecial = ref(false)
  const fansCount = ref(0) // 初始粉丝数
  const showDropdown = ref(false)
  
  const handleMainClick = () => {
    if (!isFollowing.value) {
      followEntity.value.isSpecial = 0;// 非特别关注
      giveCurrentAuthorFollow();
    } else {
      showDropdown.value = !showDropdown.value
    }
  }
  
  const setSpecial = () => {
    if(isSpecial.value){
      ElMessage.info('您已经是特别关注了');
      return;	
    }
    followEntity.value.isSpecial = 1;// 特别关注
    giveCurrentAuthorFollow();
    showDropdown.value = false;
  }
  
  const unfollow = () => {
    if(isSpecial.value){
      isSpecial.value = false
      followEntity.value.isSpecial = 1;// 已经是特别关注
      giveCurrentAuthorUnFollow();
    }else{
      isFollowing.value = false
      fansCount.value -= 1
      followEntity.value.isSpecial = 0;
      giveCurrentAuthorUnFollow();
    }
    showDropdown.value = false
  }

const giveCurrentAuthorFollow = async() =>{
	const result = await giveAuthorFollow(followEntity.value);
	console.log(followEntity.value)
	if(result.code === 1){
    if(followEntity.value.isSpecial === 1){
      isSpecial.value = true;
    }else{
      isFollowing.value = true;
      fansCount.value += 1
    }
	}else{
		ElMessage.error(result.msg);
    console.log(result.msg)
  }
}

const giveCurrentAuthorUnFollow = async() =>{
	const result = await giveAuthorUnfollow(followEntity.value.followedId 
                                            ,followEntity.value.followerId,
                                            followEntity.value.isSpecial);
	if(result.code === 1){
		
	}else{
		ElMessage.error(result.msg);
  }
}

const onPlay = (ev) => {
  isPlay.value = true;
  console.log("播放");
  startInterval();
  danmakuInstance.value?.play(); // 播放弹幕
};

const onPause = () => {
  isPlay.value = false;
  console.log("暂停");
  clearDanmuInterval();
  danmakuInstance.value?.pause(); // 暂停弹幕
};

const aplayVideo = ref(null);

const togglePlay = () => {
  const player = aplayVideo.value?.player;
  player?.paused() ? player.play() : player?.pause();
};

const videoUrl = ref('');
const coverUrl = ref('');

	 //播放器
const options = reactive({
    width: '55%', //播放器高度
    height: '50%', //播放器高度
    color: "#409eff", //主题色
    title: "", //视频名称
    webFullScreen:false,//网页全屏
    speed:true,//是否支持快进快退
    currentTime:0,//跳转到固定播放时间(s)
    muted:true,//静音
    autoPlay: true, //自动播放
    loop:false,//循环播放
    mirror:false,//镜像画面
    control: true, //是否显示控制器
    ligthOff:false,//关灯模式
    volume:0.5,//默认音量0-1
    src: videoUrl, //视频源
    poster: coverUrl, //封面
    speedRate: [2.0,1.5,1.25,1.0,0.5], // 可选的播放速度
    controlBtns: [
    "audioTrack",//音轨切换按钮
    "quality",//视频质量切换按钮
    "speedRate",//速率切换按钮
    "volume",//音量
    "setting",//设置
    "pip",//画中画按钮
    "fullScreen",//全屏按钮
  ], //显示所有按钮,
})


const currentUserId = ref(''); // 当前用户ID

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

    const getUserInfo = async () => {
      //获取登录用户名
      let loginUser = JSON.parse(localStorage.getItem('loginUser'))
      token.value = loginUser.token;
      if (loginUser) {
        userInfo.value.username = loginUser.username;
        const result = await getUserInfoByUsername(userInfo.value.username);
        if(result.code === 1){
          userInfo.value = result.data;
          currentUserId.value = userInfo.value.id;
          followEntity.value.followerId = userInfo.value.id;
          userInfo.value.followingCount = formatViews(userInfo.value.followingCount);
          userInfo.value.followersCount = formatViews(userInfo.value.followersCount);
          userInfo.value.postCount = formatViews(userInfo.value.postCount);
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

    const reloadIndex = () => {
      router.push('/index').then(() => {
        window.scrollTo(0, 0); // 滚动到页面顶部
        window.location.reload();
      });
    }

    
    const toUserInfo = (id) => {
      if(id === userInfo.value.id){
       router.push('/userInfo/' + id).then(() => {
        window.location.reload();
        window.scrollTo(0, 0); // 滚动到页面顶部
      })
      }else{
        router.push('/userInfo/others/' + id).then(() => {
          window.location.reload();
          window.scrollTo(0, 0); // 滚动到页面顶部
        })
      }
    }

const followEntity = ref({
  followedId: '',
  followerId: '',
  isSpecial: ''
})

const getCurrentAuthorIsFollow = async() =>{
  const result = await getAuthorIsFollow(currentUserId.value,authorInfo.value.id);
  if(result.code === 1){
    if(result.data){
      if(result.data.isSpecial === 0){
        isFollowing.value = true;
      }else{
        isFollowing.value = true;
        isSpecial.value = true;
      }
    }
  }
}

// 创建WebSocket
function ws_create(url) {
	 try{
		// 判断是否支持 WebSocket
        if('WebSocket' in window){
          let loginUser = JSON.parse(localStorage.getItem('loginUser'))
          // 将 token 作为 URL 的 query 参数传递
          const token = loginUser.token;
          // 检查 URL 是否已经包含 token 参数
          if (!url.includes('?token=')) {
                // 如果 URL 已经包含查询参数，则添加 & 否则添加 ?
                if (url.includes('?')) {
                    url += `&token=${encodeURIComponent(token)}`;
                } else {
                    url += `?token=${encodeURIComponent(token)}`;
                }
            }
          const wsUrl = new URL(url);
          wsUrl.searchParams.set('token', loginUser.token); // 添加 token 参数
          console.log('WebSocket URL:', wsUrl.toString());
			// 连接WebSocket
            ws = new WebSocket(url);
			// 初始化WebSocket事件(WebSocket对象, WebSocket连接地址)
			ws_event(ws, url);
        }else{
        	ElMessage('您的浏览器不支持WebSocket');
        }
    }catch(e){
		// 重新连接WebSocket
        ws_recontent(url);
        console.log(e);
    }
}


// WebSocket 事件创建
function ws_event(ws, url) {
	ws.onopen = function (event) {
		// 心跳检测重置
		ws_heartCheck.reset().start();
		console.log("WebSocket已连接");
	};

	ws.onclose = function (event) {
		// 重新连接WebSocket
        ws_recontent(url);
		console.log("WebSocket连接已关闭");
	};

	ws.onerror = function (event) {
		// 重新连接WebSocket
        ws_recontent(url);
		console.log("WebSocket错误：", event);
	};

	ws.onmessage = function (event) {
		// 只要有数据，那就说明连接正常
		ws_heartCheck.reset().start();

    try {
        const data = JSON.parse(event.data)
        if (data.type === 'danmu' && isDanmuReady.value) {
            danmakuInstance.value.add({
                text: data.content,
                color: data.color || getRandomColor(),
                time: data.videoTime
            })
        }else if (data.type === 'userCount') {
          viewsPeople.value = data.count;
       }
    } catch(e) {
        console.error('弹幕解析错误', e)
    }
	};
}

// 重新连接websocker(WebSocket连接地址)
function ws_recontent(url) {
	// 延迟避免请求过多
	setTimeout(function () {
		ws_create(url);
	}, 2000);
}

// 监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，这样服务端会抛异常。
window.onbeforeunload = function() {
    ws.close();
} 

// WebSocket心跳检测
var ws_heartCheck = {
    timeout: 5000,			// 5秒一次心跳
    timeoutObj: null,		// 执行心跳的定时器
    serverTimeoutObj: null,	// 服务器超时定时器
    reset: function(){		// 重置方法
        clearTimeout(this.timeoutObj);
        clearTimeout(this.serverTimeoutObj);
        return this;
    },
    start: function(){		// 启动方法
        var self = this;
      //   this.timeoutObj = setTimeout(function(){
      //       // 这里发送一个心跳信息，后端收到后，返回一个消息，在onmessage拿到返回的心跳（信息）就说明连接正常
      //       ws.send("check");
			// // 如果超过一定时间还没重置，说明后端主动断开了
      //       self.serverTimeoutObj = setTimeout(function(){
			// 	// 如果onclose会执行reconnect，我们执行ws.close()就行了.如果直接执行reconnect 会触发onclose导致重连两次
      //           ws.close();
      //       }, self.timeout);
      //   }, this.timeout);
    }
}

const shareDialogVisible = ref(false);

const shareInput = ref('');

let postEntity = {
  content: '',
  userId : '',
  videoId: ''
}

const sendPost = async () => {
  if (!shareInput.value.trim()) {
    shareInput.value = '转发了视频';
  }	
  postEntity.content = shareInput.value;
  postEntity.userId = currentUserId.value;
  postEntity.videoId = currentVideoId.value;
  const result = await addPost(postEntity);	
  if(result.code === 1){
    shareInput.value = '';
    shareDialogVisible.value = false;
  }else{
    ElMessage.error('发布失败');
  }

  const shareEntity = {
      userId: userInfo.value.id,
      entityId: currentVideoId.value,
      entityType: 1	
    }
    const result2 = await giveShare(shareEntity);
    if(result2.code === 1){
      sharesCount.value += 1;
      isShare.value = true;
      ElMessage.success('转发成功');	
    }else{
      ElMessage.error('转发失败',result.msg);
    }
}

const toSearch = () =>{
  if(searchText.value === ''){
    ElMessage.error('搜索内容不能为空！');
    return;
  }
  addSearchHistory(searchText.value);
  clearDanmuInterval();
  const searchTextByP = encryptSearchText(searchText.value)
  const query = { 
    ...router.currentRoute.value.query, // 保留已有参数
    sortType: 1, // 空值时移除参数
    dateRange: 1,
    timeSize: 1,
    userSort: 1
  }
  
  // 执行路由跳转（保留当前path）
  router.push({
    path: '/search/' + searchTextByP,
    query: Object.fromEntries(
      Object.entries(query).filter(([_, v]) => v !== undefined)
    )
  }).then(() => {
    window.location.reload();
  });
}

const contentContainer = ref(null);

const addHistoryVideo = async() => {
  if(localStorage.getItem('loginUser')){
    await addHistory(currentUserId.value,currentVideoId.value);
  }
}

const toAnswerMe = () => {
  router.push('/chat/answerMe').then(() => {
    window.location.reload();
});
}

const toMymessage = () => {
  router.push('/chat/mymessage').then(() => {
    window.location.reload();
});
}

const toAtMe = () => {
  router.push('/chat/atMe').then(() => {
    window.location.reload();
});
}

const toSystemMessage = () => {
  router.push('/chat/systemMessage').then(() => {
    window.location.reload();
});
}

const toLikeMe = () => {
  router.push('/chat/likeMe').then(() => {
    window.location.reload();
  })
}

const toOtherMessage = () => {
  router.push('/chat/otherMessage').then(() => {
    window.location.reload();
  })
}

const currentHotSearchContent = ref('');

// 热搜数据
const hotSearchData = ref([
  {
    id: '',
    content :'',
    searchCount: '',
    createTime: ''
  }
]);
const getHotSearchData = async () => {

  const result = await getHotSearch();
  if(result.code === 1){
    hotSearchData.value = result.data;

    // 初始化显示第一个热搜内容
    if (hotSearchData.value.length > 0) {
      currentHotSearchContent.value = hotSearchData.value[0].content;
    }
    // 启动定时器，每五秒更新一次热搜内容
    setInterval(() => {
      const currentIndex = hotSearchData.value.findIndex(item => item.content === currentHotSearchContent.value);
      const nextIndex = (currentIndex + 1) % hotSearchData.value.length;
      currentHotSearchContent.value = hotSearchData.value[nextIndex].content;
    }, 5000);
  }else{
    ElMessage.error('获取热搜失败！')
  }
}

onMounted(async() => {
  await getCurrentVideo();
  await getUserInfo();

  checkContentHeight();
  window.addEventListener('resize', checkContentHeight);

  initDescTagsObserver();
  window.addEventListener('resize', checkDescTagsHeight);

  isToComment.value = new Array(currentVideo.value.comments).fill(false);
  
  getCurrentVideoDanmuCount();

  ws_create(`ws://localhost:8100/YVW/danmu/${currentVideoId.value}/${currentUserId.value}`);

  getCurrentVideoIsLike();

  getCurrentVideoCoins();

  getCurrentVideoFavorites();

  getUserFavorites();

  await getCurrentAuthorInfo();

  getCurrentAuthorIsFollow();

  getCurrentLeftRecommendVideos();

  switchTab('hot');

  getCurrentVideoIsShare();

  getHotSearchData();

    //读取本地存储的搜索历史
    const storedHistory = localStorage.getItem('searchHistory');
  if (storedHistory) {
    searchHistory.value = JSON.parse(storedHistory);
  }

  window.addEventListener('keyup', (e) => {
    if (e.code === 'Space') {
      e.preventDefault();
      togglePlay();
    }
  });

  // 在父容器（如 #app）监听事件，通过选择器过滤目标
document.getElementById('app').addEventListener('click', (e) => {
  const mentionElement = e.target.closest('.mention');
  if (mentionElement) {
    const username = mentionElement.dataset.username;
    handleMentionClick(username);
  }
});

addHistoryVideo();
})



// 使用 onUnmounted 钩子函数来关闭 WebSocket 连接
onUnmounted(() => {
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.close();
        console.log('WebSocket连接已关闭');
    }
    danmakuInstance.value?.destroy();
    // 清除定时器
    clearDanmuInterval();
    // 移除事件监听器
    window.removeEventListener('resize', checkContentHeight);
    window.removeEventListener('resize', checkDescTagsHeight);
})

    const logout = () => {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('loginUser');
    router.push('/login').then(() => {
      window.location.reload();
    });
  }).catch(() => {
    ElMessage({
      type: 'info',
      message: '已取消退出'
    });
  });



}
</script>

<template>
  <el-affix>
 <!-- 顶栏 -->
 <el-row class="video-top1">
    <el-col :span="24">

<div class="background-layer1"></div>
<div class="top-logo"><svg t="1743859746673" class="logo-icon" viewBox="0 0 2299 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6640" ><path d="M1776.040306 322.618853a497.5306 497.5306 0 0 1 55.146298-6.026207c3.013104 4.508817 24.061475 155.944368 18.555516 156.94151s-44.632952 10.036453-44.632952 10.036453c-3.013104-20.5498-28.570291-147.923876-29.090539-160.951756m75.219204-14.54527l20.5498 162.967718a2379.701518 2379.701518 0 0 0 57.16226-4.010246 20057.059972 20057.059972 0 0 0-16.539554-158.957472 156.984864 156.984864 0 0 0-61.172506 0m-40.601029 226.156185s92.755901-23.562904 126.355258-12.030738c17.038125 61.671077 48.144626 407.65774 51.136052 421.70444-21.070048 2.514532-90.263045 8.519063-95.769004 10.036453-4.508817-26.012405-81.722306-403.192278-81.722306-419.688478m343.472131-207.578992a540.971316 540.971316 0 0 1 55.167976-1.51739c0.997142 12.030737 6.026207 157.440081 0.498571 157.938652l-44.632951 4.010246c-0.997142-20.051229-12.030737-146.926734-11.033596-160.409831m75.717776-4.010246l4.010246 160.951756c10.535024 0 52.653443 2.514532 57.16226 2.015961-0.997142-61.671077 0-158.957472 0-158.957471a277.032113 277.032113 0 0 0-61.172506-4.010246m-64.68418 216.119732s94.771862-12.529308 126.853828 2.514532c7.023349 72.206101 6.026207 408.654883 7.023349 422.701581-21.568619 0-90.263045 0.997142-95.769004 2.015962-0.997142-26.012405-39.603887-410.670844-38.108173-427.210398M1945.012553 125.054637c54.647728 278.787951 96.267576 755.140117 97.784966 785.72637 0 0 43.115561 0.997142 91.758759 4.010246-28.591969-300.35657-63.687038-780.220411-63.188468-795.762823-12.030737-13.548128-126.355257 6.026207-126.355257 6.026207m-120.32905 659.371113c-10.535024-78.232308-290.320117-166.912932-447.760198-138.385995 0 0-19.509304-172.483922-27.074578-339.461885-6.503101-143.41506-0.997142-282.798196 0.498571-305.862529-10.535024-7.52192-123.840725 46.627236-185.034908 69.192997 0 0 73.203243 309.98116 126.355257 952.704334A862.57135 862.57135 0 0 0 1521.725693 1003.645293c145.409344-28.180105 317.394695-115.820233 302.849425-219.111158M1482.707085 925.326277l-24.560045-176.992739a1281.501133 1281.501133 0 0 1 172.982493 63.188467c-2.514532 11.033595-148.422448 113.825949-148.422448 113.825949m-871.97917-602.620716a497.5306 497.5306 0 0 1 55.146299-6.026207c3.013104 4.508817 24.061475 155.944368 18.555516 156.94151s-44.632952 10.036453-44.632952 10.036453c-3.013104-20.5498-28.570291-147.923876-29.09054-160.951756m75.219205-14.54527l20.5498 162.967718a2379.701518 2379.701518 0 0 0 57.16226-4.010246c-6.503101-61.671077-16.539554-158.957472-16.539554-158.957472a156.984864 156.984864 0 0 0-61.172506 0M645.324414 534.229768s92.755901-23.562904 126.355257-12.030738c17.038125 61.671077 48.144626 407.65774 51.136053 421.70444-21.070048 2.514532-90.263045 8.519063-95.769004 10.036453-4.508817-26.012405-81.722306-403.192278-81.722306-419.688478m343.472131-207.578992A540.971316 540.971316 0 0 1 1043.964521 325.155063c0.997142 12.030737 6.026207 157.440081 0.498571 157.938652l-44.632952 4.010246c-0.997142-20.051229-11.532166-146.926734-11.033595-160.409831m75.717776-4.010246l4.010246 160.951756c10.535024 0 52.653443 2.514532 57.16226 2.015961-0.997142-61.671077 0-158.957472 0-158.957471a311.173395 311.173395 0 0 0-61.172506-4.010246m-64.684181 216.119732s94.771862-12.529308 126.853829 2.514532c7.023349 72.206101 6.026207 408.654883 7.023349 422.701581-21.568619 0-90.263045 0.997142-95.769004 2.015962-0.498571-26.012405-39.603887-410.670844-38.108174-427.210398M779.72184 125.054637c54.647728 278.787951 96.267576 755.140117 97.784966 785.72637 0 0 43.115561 0.997142 91.758758 4.010246C940.673596 614.45636 905.578526 134.592519 906.098774 118.551536c-12.030737-13.006203-126.355257 6.503101-126.355257 6.503101m-120.372404 659.371113c-10.535024-78.232308-290.320117-166.912932-447.760198-138.385995 0 0-19.509304-172.483922-27.074578-339.461885C178.011558 163.16281 183.517517 23.844705 185.034908 0.715341 174.499884-6.308008 61.172506 47.342577 0 69.908338c0 0 73.203243 309.98116 126.355257 952.704334a862.57135 862.57135 0 0 0 230.166431-18.967379c145.409344-28.180105 317.394695-115.820233 302.849425-219.111158m-341.976418 140.900527l-24.560046-176.992739a1281.501133 1281.501133 0 0 1 172.982494 63.188467c-2.514532 11.033595-148.422448 113.825949-148.422448 113.825949" fill="#1296db" p-id="6641"></path></svg></div>

      <div class="header1">

        <el-row gutter="160">
        <!-- 左侧导航栏 -->
         <el-col :span="8">
          <div class="header-left1">
            <div class="button-container1">

            <!-- 首页按钮 -->
            <button class="btn-left-11" @click="reloadIndex">
              <span class="btn-text-one1">
                <span class="btn-text1">首页</span>
              </span>
              <span class="btn-text-two1">
                <span class="btn-text21">返回首页</span>
              </span>
            </button>

            <!-- 番剧按钮 -->
<button class="btn-left1">
  番剧
  <div class="star-1">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-2">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-3">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-4">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-5">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-6">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
</button>

 <!-- 直播按钮 -->
 <button class="btn-left1">
  直播
  <div class="star-1">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-2">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-3">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-4">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-5">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-6">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
</button>

 <!-- 游戏中心按钮 -->
 <button class="btn-left1">
  游戏中心
  <div class="star-1">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-2">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-3">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-4">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-5">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-6">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
</button>

 <!-- 会员购按钮 -->
 <button class="btn-left1">
  会员购
  <div class="star-1">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-2">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-3">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-4">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-5">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-6">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
</button>

 <!-- 漫画按钮 -->
 <button class="btn-left1">
  漫画
  <div class="star-1">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-2">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-3">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-4">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-5">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-6">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
</button>

 <!-- 赛事按钮 -->
 <button class="btn-left1">
  赛事
  <div class="star-1">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-2">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-3">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-4">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-5">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
  <div class="star-6">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xml:space="preserve"
      version="1.1"
      style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
      viewBox="0 0 784.11 815.53"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs></defs>
      <g id="Layer_x0020_1">
        <metadata id="CorelCorpID_0Corel-Layer"></metadata>
        <path
          class="fil0"
          d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
        ></path>
      </g>
    </svg>
  </div>
</button>
</div>

<!-- 下载客户端 -->
 <!-- From Uiverse.io by vinodjangid07 --> 
<button class="bookmarkBtn1">
  <span class="IconContainer1">
    <svg t="1743559875398" class="bli-21" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="7787" width="200" height="200"><path d="M219.428571 365.714286a36.571429 36.571429 0 0 1 6.582858 72.557714L219.428571 438.857143H109.714286a36.571429 36.571429 0 0 0-35.986286 29.988571L73.142857 475.428571V438.857143v140.068571L73.142857 578.852571 73.142857 914.944l0.585143 5.851429A36.571429 36.571429 0 0 0 109.714286 950.857143h103.424l-0.146286 0.438857 611.547429 1.828571 122.148571-2.267428H914.285714a36.571429 36.571429 0 0 0 35.986286-29.988572l0.512-6.070857 0.146286-103.936V578.852571l-0.146286-103.936-0.512-6.070857A36.571429 36.571429 0 0 0 914.285714 438.857143H804.571429a36.571429 36.571429 0 0 1 0-73.142857h121.929142A110.957714 110.957714 0 0 1 1024 475.428571v443.465143l-0.292571 0.073143-0.219429 5.851429A109.714286 109.714286 0 0 1 917.504 1024L109.714286 1024a109.714286 109.714286 0 0 1-109.641143-105.033143L0 918.820571V472.283429l0.146286-0.073143 0.365714-7.314286A109.714286 109.714286 0 0 1 95.085714 366.665143L95.451429 365.714286H219.428571z m292.059429-365.714286a36.571429 36.571429 0 0 1 36.571429 36.571429l-0.073143 585.142857 117.321143-117.321143 5.12-4.169143a36.571429 36.571429 0 0 1 46.592 55.954286l-131.657143 131.510857a109.714286 109.714286 0 0 1-152.795429-2.194286L303.250286 556.105143l-4.169143-5.12a36.571429 36.571429 0 0 1 55.954286-46.665143l56.100571 56.246857 63.707429 63.634286V36.571429l0.658285-6.582858A36.571429 36.571429 0 0 1 511.488 0z"  p-id="7788"></path></svg>
  </span>
  <p class="text1">下载客户端</p>
</button>

          </div>
        </el-col>

        <!-- 中间搜索框 -->
         <el-col :span="8">
          <div class="header-center">
            <!-- From Uiverse.io by joe-watson-sbf --> 
<div class="search1">
    <input type="text" 
    class="search__input" 
    :placeholder="currentHotSearchContent"
    v-model="searchText"
    @keydown.space.prevent="handleSpace"
    @keydown.enter="toSearch" 
    >
    <button class="search__button" @click="toSearch">
        <svg class="search__icon" aria-hidden="true" viewBox="0 0 24 24">
            <g>
                <path d="M21.53 20.47l-3.66-3.66C19.195 15.24 20 13.214 20 11c0-4.97-4.03-9-9-9s-9 4.03-9 9 4.03 9 9 9c2.215 0 4.24-.804 5.808-2.13l3.66 3.66c.147.146.34.22.53.22s.385-.073.53-.22c.295-.293.295-.767.002-1.06zM3.5 11c0-4.135 3.365-7.5 7.5-7.5s7.5 3.365 7.5 7.5-3.365 7.5-7.5 7.5-7.5-3.365-7.5-7.5z"></path>
            </g>
        </svg>
    </button>
</div>
          </div>
         </el-col>

        <!-- 右侧导航栏 -->
         <el-col :span="8">
          <div class="header-right">
               <!-- 头像下拉菜单 -->
    <el-dropdown>
      <el-avatar :src= "userInfo.image" :size="37" class="avatar" @click="toUserInfo(userInfo.id)"></el-avatar>
      <template #dropdown>
        <el-dropdown-menu class="custom-dropdown-menu"> 
          
          <el-row>
            <el-col :span="24">
            <span class="username">{{ userInfo.username }}</span>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <span class="coins">硬币：<span class="coins-num">{{userInfo.coins}}</span></span>
            </el-col>
          </el-row>
          <el-row class="userInfo">
            <el-col :span="8" class="userInfo-item">
              <el-statistic title="---------关注---------" :value=userInfo.followingCount />
            </el-col>
            <el-col :span="8" class="userInfo-item">
              <el-statistic title="---------粉丝---------" :value=userInfo.followersCount />
            </el-col>
            <el-col :span="8" class="userInfo-item">
              <el-statistic title="---------动态---------" :value=userInfo.postCount />
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
<button class="cta-1" @click="toUserInfo(userInfo.id)">
  <span class="hover-underline-animation"> 
    <svg t="1743611505760" class="user-icon" viewBox="0 0 1131 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="60744" width="200" height="200"><path d="M1130.10105 436.659501l1.853743 1.467547q-0.913998-0.772393-1.853743-1.467547zM0 438.770708l3.347036-2.651882a32.183041 32.183041 0 0 0-3.347036 2.651882z" fill="#515151" p-id="60745"></path><path d="M117.725564 995.678924c-0.296084 1.982475-0.605041 3.952077-0.875379 5.947426a29.685637 29.685637 0 0 0 0.875379-5.947426zM88.0528 1024zM58.418656 992.087296q0.32183-2.227066 0.669407-4.454132a29.608398 29.608398 0 0 0-0.669407 4.454132z" fill="#515151" p-id="60746"></path><path d="M1074.334276 985.174379c0.373323 2.330052 0.708027 4.672978 1.042731 7.015903a30.715494 30.715494 0 0 0-1.042731-7.015903zM1014.769904 1000.480634c-0.257464-1.84087-0.527802-3.668867-0.811013-5.496864a30.715494 30.715494 0 0 0 0.811013 5.496864z" fill="#515151" p-id="60747"></path><path d="M1074.334276 985.174379C1043.515796 791.239374 897.095833 650.226161 710.253969 593.918713c99.612949-53.114891 184.26722-166.21897 184.26722-286.712276C894.521189 133.057565 741.535885 0 566.897831 0s-327.623358 133.057565-327.623357 307.206437c0 120.493306 84.705764 233.636005 184.318712 286.712276C235.914564 650.445006 89.056911 792.436583 59.049444 987.633164q-0.347577 2.227066-0.669408 4.454132c0 0.7209-0.090113 1.454673-0.090112 2.188447a29.711383 29.711383 0 0 0 29.608398 29.711384h0.180225a29.724257 29.724257 0 0 0 28.771638-22.322157c0.270338-1.995349 0.579295-3.964951 0.875379-5.947426 32.852448-216.270036 221.277717-360.81051 449.172267-360.81051 227.637086 0 414.067006 144.257263 447.06106 360.102483 0.283211 1.827997 0.553548 3.668867 0.811013 5.496863a30.754114 30.754114 0 0 0 60.632849-7.234748c0.012873-0.386196-0.656534-5.767201-1.068477-8.097253z m-773.680307-677.980816c0-141.489522 124.342397-245.749701 266.243862-245.749701s266.243862 104.273053 266.243862 245.749701-124.355271 266.243862-266.243862 266.243862-266.243862-124.741467-266.243862-266.230988z" fill="#515151" p-id="60748"></path></svg>
    个人中心 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
  <svg
    id="arrow-horizontal"
    xmlns="http://www.w3.org/2000/svg"
    width="30"
    height="10"
    viewBox="0 0 46 16"
  >
    <path
      id="Path_10"
      data-name="Path 10"
      d="M8,0,6.545,1.455l5.506,5.506H-30V9.039H12.052L6.545,14.545,8,16l8-8Z"
      transform="translate(30)"
    ></path>
  </svg>
</button>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
<button class="cta-2">
  <span class="hover-underline-animation" @click="toUpload"> 
    <svg t="1743612310473" class="user-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="73019" width="200" height="200"><path d="M621.514105 939.870316a229.052632 229.052632 0 0 0 229.052632-229.052632V187.607579a40.421053 40.421053 0 0 1 80.842105 0v523.210105a309.894737 309.894737 0 0 1-309.894737 309.894737H94.315789a40.421053 40.421053 0 0 1-40.421052-40.421053V40.421053A40.421053 40.421053 0 0 1 94.315789 0h796.672a40.421053 40.421053 0 1 1 0 80.842105H134.736842v859.028211h486.777263z m-319.434105-312.481684a40.421053 40.421053 0 1 1 0-80.842106h242.688a40.421053 40.421053 0 0 1 0 80.842106H302.08z m7.006316-261.281685a40.421053 40.421053 0 1 1 0-80.842105h369.825684a40.421053 40.421053 0 1 1 0 80.842105H309.032421z" fill="#515151" p-id="73020"></path></svg>
    投稿管理 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
  <svg
    id="arrow-horizontal"
    xmlns="http://www.w3.org/2000/svg"
    width="30"
    height="10"
    viewBox="0 0 46 16"
  >
    <path
      id="Path_10"
      data-name="Path 10"
      d="M8,0,6.545,1.455l5.506,5.506H-30V9.039H12.052L6.545,14.545,8,16l8-8Z"
      transform="translate(30)"
    ></path>
  </svg>
</button>
            </el-col>
          </el-row>
         
          <el-row>
            <el-col :span="24">
<button class="cta-3">
  <span class="hover-underline-animation"> 
    <svg t="1743612160512" class="user-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="66143" width="200" height="200"><path d="M753.963821 930.99477c-9.950629 0-19.378349-2.618641-27.234271-7.855922L509.381359 791.681864 277.368168 906.903072c-17.282618 8.379854-38.232766 6.808056-53.94461-4.189416s-23.043832-29.852912-18.854417-47.659463l54.991452-244.582461L81.493036 443.924555c-13.617136-12.56927-18.330484-31.94762-12.045337-49.230238 6.285147-16.759709 23.043832-29.328979 41.898249-31.423687l231.489258-25.662473L467.48311 118.164237c8.903787-15.711843 26.186406-25.662473 45.564755-25.139563s36.137035 10.998495 44.51689 27.234271l109.459994 217.34819 245.106394 25.662473c19.378349 2.094708 35.614126 14.141068 42.422182 31.423687 6.285147 17.282618 1.570775 36.660968-12.045337 49.230238L791.671631 584.808236c-9.426697 8.903787-24.615631 8.903787-34.042327 0-8.903787-8.903787-8.903787-23.043832 0.523933-31.94762l150.310378-140.883681c0.523933-0.523933 1.047866-1.570775 0.523933-2.618641 0-0.523933-1.047866-1.047866-2.094708-1.570775l-271.816732-27.758204L514.094708 139.637294c-0.523933-1.047866-1.047866-1.570775-2.094708-1.570775s-2.094708 0.523933-2.094708 1.047866L373.73496 380.029316l-256.627798 27.758204c-1.047866 0.523933-2.094708 1.047866-2.094708 1.570775-0.523933 1.047866 0 2.094708 0.523933 2.618641l196.399065 183.829795-60.228733 268.674159c-0.523933 0.523933 0 1.570775 0.523933 2.094708 1.047866 0.523933 2.094708 0.523933 3.142573 0L512 739.832985l240.392022 145.597029 1.570775 0.523933 1.570775-0.523933c1.047866-0.523933 1.047866-1.570775 1.047866-2.618641L632.457466 523.531638c-4.189416-11.522428 2.618641-24.615631 15.711843-28.281113 12.56927-3.666506 25.662473 2.618641 29.852912 14.663978l124.123972 359.279736c6.285147 18.854417-0.523933 39.803541-17.283642 51.848878C775.959787 927.328263 764.961292 930.99477 753.963821 930.99477zM374.782825 560.192605c-6.285147 0-12.045337-2.618641-16.759709-6.808056l-71.227228-66.51388c-9.426697-8.379854-9.426697-23.043832 0-31.423687 9.426697-8.903787 24.615631-8.903787 34.042327-0.523933l70.703295 66.51388c9.426697 8.903787 9.426697 23.043832 0 31.94762C386.828163 557.573965 381.066949 560.192605 374.782825 560.192605z" fill="#515151" p-id="66144"></path></svg>
    推荐服务 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
  <svg
    id="arrow-horizontal"
    xmlns="http://www.w3.org/2000/svg"
    width="30"
    height="10"
    viewBox="0 0 46 16"
  >
    <path
      id="Path_10"
      data-name="Path 10"
      d="M8,0,6.545,1.455l5.506,5.506H-30V9.039H12.052L6.545,14.545,8,16l8-8Z"
      transform="translate(30)"
    ></path>
  </svg>
</button>
            </el-col>
          </el-row> 

           <!-- 添加分割线 -->
           <el-row>
            <el-col :span="24">
              <el-divider class="divider"></el-divider>
            </el-col>
          </el-row>

          <el-row>
          </el-row :span="24">
<button class="logout" @click="logout"><svg t="1743613124676" class="logout-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="74023" width="200" height="200"><path d="M868 732h-70.3c-4.8 0-9.3 2.1-12.3 5.8-7 8.5-14.5 16.7-22.4 24.5-32.6 32.5-70.5 58.1-112.7 75.9-43.6 18.4-90 27.8-137.9 27.8-47.9 0-94.3-9.4-137.9-27.8-42.2-17.8-80.1-43.4-112.7-75.9-32.6-32.5-58.1-70.4-76-112.5C167.3 606.2 158 559.9 158 512s9.4-94.2 27.8-137.8c17.8-42.1 43.4-80 76-112.5s70.5-58.1 112.7-75.9c43.6-18.4 90-27.8 137.9-27.8 47.9 0 94.3 9.3 137.9 27.8 42.2 17.8 80.1 43.4 112.7 75.9 7.9 7.9 15.3 16.1 22.4 24.5 3 3.7 7.6 5.8 12.3 5.8H868c6.3 0 10.2-7 6.7-12.3C798 160.5 663.8 81.6 511.3 82 271.7 82.6 79.6 277.1 82 516.4 84.4 751.9 276.2 942 512.4 942c152.1 0 285.7-78.8 362.3-197.7 3.4-5.3-0.4-12.3-6.7-12.3z m88.9-226.3L815 393.7c-5.3-4.2-13-0.4-13 6.3v76H488c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h314v76c0 6.7 7.8 10.5 13 6.3l141.9-112c4.1-3.2 4.1-9.4 0-12.6z"  p-id="74024"></path></svg>
  &nbsp;退出登录</button>
          <el-row>
          </el-row>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
            

             <!-- 大会员 -->
              <div class="botton-right">
             <button class="vip-button1">
    <svg class="vip-icon1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg">
      <path d="M512 51.2a460.8 460.8 0 1 0 0 921.6 460.8 460.8 0 0 0 0-921.6z m0 83.7632c207.9232 0 377.0368 169.1648 377.0368 377.0368 0 207.872-169.1136 376.9856-376.9856 376.9856-207.9232 0-377.0368-169.1136-377.0368-376.9856 0-207.872 169.1136-377.0368 377.0368-377.0368z"></path>
      <path d="M672.6656 412.4672v-0.1536h-121.1904a363.9296 363.9296 0 0 0-0.3072-62.1568l0.1024-2.1504v-0.1024a40.704 40.704 0 0 0-81.408 0v0.1024c0 2.7648 0.3072 5.5296 0.8704 8.1408l-0.5632 0.0512a285.2864 285.2864 0 0 1-0.256 56.1152H345.2416v0.1536A40.704 40.704 0 0 0 307.2 453.0176v0.1536c0 22.4768 17.2544 40.4992 39.68 40.4992 1.7408 0 3.584 0.1024 6.3488 0.0512l95.2832-0.2048a279.3472 279.3472 0 0 1-119.2448 132.3008l-1.6896 1.024H327.68a40.6528 40.6528 0 0 0-19.0976 34.4576v0.1024a40.704 40.704 0 0 0 64.0512 33.28 360.8064 360.8064 0 0 0 162.7648-201.0624h130.816l5.2224-0.1024c24.4736-0.512 40.448-17.7664 40.448-40.2432v-0.1024c0.1024-22.0672-17.408-39.936-39.168-40.704z"></path>
      <path d="M705.1776 641.9968l-0.3072-0.4096a41.2672 41.2672 0 0 0-4.9152-7.0144 361.216 361.216 0 0 0-97.4848-94.208v0.0512a40.704 40.704 0 0 0-64.3584 33.024 360.8064 360.8064 0 0 0 162.7648-201.0624h130.816l5.2224-0.1024c24.4736-0.512 40.448-17.7664 40.448-40.2432v-0.1024c0.1024-22.0672-17.408-39.936-39.168-40.704z"></path>
    </svg>
    <div class="vip-text1">大会员</div>
  </button>
</div>

<!-- 私信 -->
 <div class="botton-right">
  <el-dropdown>
             <button class="left-button-2" @click="toChat">
              <svg t="1743583258372" class="vip-icon" viewBox="0 0 1424 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="21799" width="200" height="200"><path d="M1246.608696 0H178.086957a178.086957 178.086957 0 0 0-178.086957 178.086957v667.826086a178.086957 178.086957 0 0 0 178.086957 178.086957h1068.521739a178.086957 178.086957 0 0 0 178.086956-178.086957V178.086957a178.086957 178.086957 0 0 0-178.086956-178.086957z m89.043478 845.913043a89.043478 89.043478 0 0 1-89.043478 89.043479H178.086957a89.043478 89.043478 0 0 1-89.043479-89.043479V178.086957a89.043478 89.043478 0 0 1 89.043479-89.043479h1068.521739a89.043478 89.043478 0 0 1 89.043478 89.043479zM1082.323478 257.78087l-337.92 293.843478a44.521739 44.521739 0 0 1-64.111304 0l-337.92-293.843478a44.521739 44.521739 0 0 0-63.22087 63.220869l338.365218 293.398261a133.565217 133.565217 0 0 0 189.662608 0l338.365218-293.398261a44.521739 44.521739 0 1 0-63.22087-63.220869z" p-id="21800"></path></svg>
    <div class="vip-text1">私信</div>
  </button>
  <template #dropdown>
    <el-dropdown-menu>
      <el-dropdown-item @click="toAnswerMe">回复我的</el-dropdown-item>
        <el-dropdown-item @click="toAtMe">@ 我的</el-dropdown-item>
        <el-dropdown-item @clcik="toLikeMe">收到的赞</el-dropdown-item>
        <el-dropdown-item @click="toSystemMessage">系统消息</el-dropdown-item>
        <el-dropdown-item @click="toMymessage">我的消息</el-dropdown-item>
    </el-dropdown-menu>
  </template>
</el-dropdown>
</div>
<!-- 动态 -->
<div class="botton-right">
  <!-- <el-dropdown> -->
             <button class="left-button-3" @click="toPost">
              <svg t="1743584588096" class="vip-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="35622" width="200" height="200"><path d="M545.04687527 520.78906277h-26.3671875c-116.36718777 0-210.9375-94.57031223-210.9375-210.9375s94.57031223-210.9375 210.9375-210.9375h26.3671875v421.875z m-52.734375-366.85546902c-74.70703152 12.65625-131.8359375 77.69531223-131.8359375 156.09375s57.12890598 143.4375 131.8359375 156.09375V153.93359375z" p-id="35623"></path><path d="M345.00781223 716.25781223c-116.36718777 0-210.9375-94.57031223-210.9375-210.9375v-26.3671875h421.875v26.3671875c0 116.19140598-94.57031223 210.9375-210.9375 210.9375z m-155.91796821-184.5703125c12.65625 74.70703152 77.69531223 131.8359375 156.09375 131.8359375s143.4375-57.12890598 156.09375-131.8359375H189.08984402z"  p-id="35624"></path><path d="M540.4765625 889.92968777h-26.3671875v-421.875h26.3671875c116.36718777 0 210.9375 94.57031223 210.9375 210.9375s-94.74609402 210.9375-210.9375 210.9375z m26.3671875-366.85546902v312.01171902c74.70703152-12.65625 131.8359375-77.69531223 131.8359375-156.09375s-57.12890598-143.4375-131.8359375-155.91796902z"  p-id="35625"></path><path d="M925.08593723 509.890625h-421.875v-26.3671875c0-116.36718777 94.57031223-210.9375 210.9375-210.9375s210.9375 94.57031223 210.9375 210.9375v26.3671875z m-366.85546821-52.734375h312.01171821c-12.65625-74.70703152-77.69531223-131.8359375-156.09375-131.8359375s-143.4375 57.12890598-155.91796821 131.8359375z" p-id="35626"></path></svg>
    <div class="vip-text1">动态</div>
  </button>
  <!-- <template #dropdown>
    <el-dropdown-menu class="divider-menu">
      <el-row>
            <el-col :span="24">
              <button class="cta-4" @click="toPost">
  <span class="hover-underline-animation"> 
    查看更多 </span>
    <el-row>
      <el-col :span="24">
      </el-col>
    </el-row>
  <svg
    id="arrow-horizontal"
    xmlns="http://www.w3.org/2000/svg"
    width="30"
    height="10"
    viewBox="0 0 46 16"
  >
    <path
      id="Path_10"
      data-name="Path 10"
      d="M8,0,6.545,1.455l5.506,5.506H-30V9.039H12.052L6.545,14.545,8,16l8-8Z"
      transform="translate(30)"
    ></path>
  </svg>
</button>
            </el-col>
        </el-row>
      <el-row>
            <el-col :span="24">
              <el-divider class="divider"><span class="divider-text">历史动态</span></el-divider>
            </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
        <ul v-infinite-scroll="Postload"  class="post-infinite-list" style="overflow: auto">
          <li v-for="i in count" :key="i" class="post-infinite-list-item">
            <botton class="post-button">
            <el-row class="post-row">
              头像
              <el-col :span="4" class="avatar-col">
            <el-avatar :src="userInfo.image" :size="37" class="post-avatar"></el-avatar>
          </el-col>

          动态信息
           <el-col :span="15" class="post-col">
            <el-row>
              <div class="post-info">
              <el-col :span="24">
                <span class="post-name">用户{{ i }}</span>
              </el-col>
              <el-col :span="24">
                <span class="post-content">发布了动态{{ i }}</span>
              </el-col>
              <el-col :span="24">
                <span class="post-time">{{i}}小时前</span>
                </el-col>
              </div>
            </el-row>
           </el-col>
          </el-row>
        </botton>
          </li>
          <li v-if="!PostHasMore" class="no-more">最多展示10条数据</li>
        </ul>
      </el-col>
      </el-row>
    </el-dropdown-menu>
  </template>
</el-dropdown> -->
</div>

<!-- 收藏 -->
<div class="botton-right">
  <!-- <el-dropdown> -->
             <button class="left-button-4" @click="gotoFavorites">
              <svg t="1743584811352" class="vip-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="49270" width="200" height="200"><path d="M512.1184 785.408l231.104 121.472a36.8 36.8 0 0 0 53.376-38.784l-44.16-257.28 187.008-182.272a36.8 36.8 0 0 0-20.416-62.72l-258.368-37.568L545.0784 94.08a36.8 36.8 0 0 0-65.92 0L363.5104 328.32l-258.368 37.504a36.8 36.8 0 0 0-20.416 62.72l187.008 182.272-44.16 257.344a36.8 36.8 0 0 0 53.376 38.784l231.04-121.472z m-196.864 186.624a110.336 110.336 0 0 1-160.128-116.352l37.568-219.2L33.4624 481.216a110.336 110.336 0 0 1 61.12-188.224l220.16-32L413.1744 61.568a110.336 110.336 0 0 1 197.888 0l98.432 199.424 220.16 32a110.336 110.336 0 0 1 61.12 188.16L831.4784 636.544l37.632 219.2a110.336 110.336 0 0 1-160.128 116.352L512.1184 868.48l-196.864 103.488z m402.816-693.696l-27.712-20.096 19.2 2.752 8.512 17.344z" p-id="49271"></path></svg>
    <div class="vip-text1">收藏</div>
  </button>
  <!-- <template #dropdown>
    <el-dropdown-menu class="favorites-menu">
      <el-row>
      <el-col :span="8">
        <ul v-infinite-scroll="Favoritesload" class="favorites-infinite-list" style="overflow: auto">
         <li v-for="i in count1" :key="i" class="favorites-infinite-list-item" @click="handleFavoriteClick(i)">
          <el-row class="favorites-row">
            <el-col :span="14">
              <span class="favorites-name">收藏夹{{ i }}</span>
            </el-col>
            <el-col :span="10">
              <span class="favorites-video-count">{{ i }}</span>
            </el-col>
          </el-row>
         </li>
        </ul>
      </el-col>
      <el-col :span="16">
        <ul v-infinite-scroll="FavoritesVideoload" class="video-favorites-infinite-list">
  <li 
    v-for="video in videoData" 
    :key="video.id" 
    class="video-favorites-infinite-list-item"
  >
  <div class="video-cover">
      <el-image style="width: 100px; height: 100px" :src="url" :fit="fits" />
  </div>
    <div class="video-title1">{{ video.title }}</div>
    <div class="video-meta1">
      <span class="author">{{ video.author }}</span>
    </div>
  </li>
</ul>
        <el-row> -->
          <!-- From Uiverse.io by doniaskima 
<button class="btn-12"><span>查看全部</span></button>
<button class="btn-13"><svg t="1743680362203" class="play-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2981" width="200" height="200"><path d="M844.704269 475.730473L222.284513 116.380385a43.342807 43.342807 0 0 0-65.025048 37.548353v718.692951a43.335582 43.335582 0 0 0 65.025048 37.541128l622.412531-359.342864a43.357257 43.357257 0 0 0 0.007225-75.08948z" fill="" p-id="2982"></path></svg>
  <span>播放全部</span></button>
        </el-row>
      </el-col>
    </el-row>
    </el-dropdown-menu>
    </template>
</el-dropdown> -->
</div>

<!-- 历史 -->
<div class="botton-right">
             <button class="left-button-5" @click="toHistory">
              <svg t="1743584884837" class="vip-icon" viewBox="0 0 1025 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="52791" width="200" height="200"><path d="M512.61025 108.624553A402.765197 402.765197 0 1 0 915.375447 512.61025 403.253397 403.253397 0 0 0 512.61025 108.624553z m0 732.300358A329.535161 329.535161 0 1 1 842.145411 512.61025 329.779261 329.779261 0 0 1 512.61025 840.924911z m172.578785-358.583075H549.225268v-143.530871a36.615018 36.615018 0 0 0-73.230036 0v180.145888a36.615018 36.615018 0 0 0 36.615018 36.615018h172.578785a36.615018 36.615018 0 0 0 0-73.230035z" p-id="52792"></path></svg>
    <div class="vip-text1">历史</div>
  </button>
</div>

<!-- 创作中心 -->
<div class="botton-right">
             <button class="left-button-6" @click="toUpload">
              <svg t="1743584977435" class="vip-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="55639" width="200" height="200"><path d="M526.362676 10.557633a381.527233 381.527233 0 0 0-387.493923 373.813154A369.465995 369.465995 0 0 0 273.332383 667.021436a73.262434 73.262434 0 0 1 27.361538 59.283331v31.921794a75.350776 75.350776 0 0 0 72.9641 75.265538h303.150492a74.02958 74.02958 0 0 0 72.9641-74.967203v-32.262748a76.714591 76.714591 0 0 1 27.361538-59.283331 369.210279 369.210279 0 0 0 134.46363-282.650649A378.032457 378.032457 0 0 0 529.346021 10.472394z m223.409937 620.024372a121.464769 121.464769 0 0 0-43.301125 93.421325v31.921793a29.833452 29.833452 0 0 1-27.361537 29.620356H373.785878a28.256541 28.256541 0 0 1-27.361537-29.151544v-32.390605a121.209054 121.209054 0 0 0-43.301125-93.421325 316.87388 316.87388 0 0 1-118.524043-246.168599 342.232315 342.232315 0 0 1 683.782722 0 316.746023 316.746023 0 0 1-118.524043 246.168599z m-63.928826 246.168599H366.711088a22.801281 22.801281 0 1 0 0 45.602563h319.132699a22.801281 22.801281 0 0 0 0-45.602563z m-45.602562 91.162506H412.313651a22.801281 22.801281 0 0 0 0 45.602563h227.927574a22.801281 22.801281 0 0 0 0-45.602563z"  p-id="55640"></path><path d="M640.411702 1024H412.441508a33.242989 33.242989 0 0 1 0-66.485979h227.970194a33.242989 33.242989 0 0 1 0 66.485979z m-227.970194-45.602562a12.359573 12.359573 0 0 0 0 24.676526h227.970194a12.359573 12.359573 0 0 0 0-24.676526z m273.530137-45.602563H366.838946a33.242989 33.242989 0 0 1 0-66.485979h319.132699a33.242989 33.242989 0 0 1 0 66.485979z m-319.132699-45.602562a12.359573 12.359573 0 0 0 0 24.676526h319.132699a12.359573 12.359573 0 1 0 0-24.676526z m6.861694-43.258506a85.451531 85.451531 0 0 1-83.448427-85.749865v-31.964413a63.374776 63.374776 0 0 0-23.483189-51.143061A378.842222 378.842222 0 0 1 128.427045 384.370787a393.11966 393.11966 0 0 1 397.893012-384.38272h5.753594a389.283931 389.283931 0 0 1 389.923219 384.254863 378.842222 378.842222 0 0 1-138.214121 290.833538 65.931929 65.931929 0 0 0-23.568428 51.143061v32.049651a84.599146 84.599146 0 0 1-83.235331 85.749865zM520.268128 20.956722a371.810051 371.810051 0 0 0-370.78719 363.584542A358.001425 358.001425 0 0 0 280.151458 658.923785a84.38605 84.38605 0 0 1 31.154648 67.636698v31.666078a64.568114 64.568114 0 0 0 62.820726 64.82383h302.852158a63.630491 63.630491 0 0 0 62.479773-64.355019v-32.433224a86.943203 86.943203 0 0 1 31.069409-67.253125 357.703091 357.703091 0 0 0 130.755759-274.723474A368.05956 368.05956 0 0 0 533.693182 20.871483h-7.458363z m-146.610107 775.030466a38.698249 38.698249 0 0 1-37.803246-39.891588v-32.049651a110.809965 110.809965 0 0 0-39.593253-85.238435A325.440343 325.440343 0 0 1 174.029607 384.669122a352.674023 352.674023 0 0 1 704.708758-0.511431 326.122251 326.122251 0 0 1-122.445012 254.8203l-1.960484 1.534292a110.469011 110.469011 0 0 0-37.504911 83.704143v31.921793a40.147303 40.147303 0 0 1-36.99348 40.019445z m152.576798-728.788616a332.088941 332.088941 0 0 0-331.236557 317.555788 304.429069 304.429069 0 0 0 114.603075 237.559517 131.736001 131.736001 0 0 1 47.222093 101.689453v31.836555a18.155787 18.155787 0 0 0 4.773352 13.59553 17.772214 17.772214 0 0 0 12.402192 5.540498h304.983119a19.221267 19.221267 0 0 0 17.047687-19.178647v-31.836556a131.352428 131.352428 0 0 1 46.881139-101.476356l3.196441-2.471915a304.727403 304.727403 0 0 0 111.790207-235.72689 329.787503 329.787503 0 0 0-317.257453-316.788642c-4.688114-0.127858-9.290989-0.213096-14.234819-0.213096z"  p-id="55641"></path><path d="M507.780697 636.506077a26.381295 26.381295 0 0 1-22.460327-40.104684l86.517011-141.282705h-140.643417a26.296057 26.296057 0 0 1-21.309609-41.809452l141.751517-191.530763a26.338676 26.338676 0 0 1 42.363502 31.367744L483.445124 402.526574h135.358634a26.381295 26.381295 0 0 1 26.338677 26.423914 26.082961 26.082961 0 0 1-3.878349 13.723388l-111.150919 181.387389a26.381295 26.381295 0 0 1-22.33247 12.444812z" fill="#ffffff" p-id="55642"></path><path d="M507.865936 646.947785a36.823004 36.823004 0 0 1-31.410363-56.001652l76.714591-125.385737h-121.933581a36.823004 36.823004 0 0 1-29.833452-58.388328l141.666278-191.786477a36.908242 36.908242 0 0 1 51.526634-7.671459 36.780385 36.780385 0 0 1 7.671459 51.441395l-98.0242 132.758862h114.560456a36.865623 36.865623 0 0 1 31.410364 56.04427l-111.150919 181.430008a36.993481 36.993481 0 0 1-31.154648 17.346022z" p-id="55643"></path></svg>
    <div class="vip-text1">创作中心</div>
  </button>
</div>

<!-- 投稿 -->
<div class="botton-right" @click="toUpload">
<button class="button-upload"><svg t="1743585506476" class="upload-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="59528" width="15" height="15"><path d="M958.203854 1023.866918H65.590221A65.637783 65.637783 0 0 1 0.072874 958.229135V431.561198c0-36.191108 29.386457-65.637783 65.517347-65.637783H206.621128L180.727323 421.745639H60.291024v541.963347h903.272245V421.745639h-120.436299l-27.098168-55.822224h142.175052c36.13089 0 65.517347 29.446675 65.517346 65.577565v526.728155a65.637783 65.637783 0 0 1-65.517346 65.637783zM659.88314 230.673451L542.036221 120.654891v602.181497H481.818072V120.654891L362.586135 230.673451a36.733071 36.733071 0 0 1-59.134223-11.501667 34.806091 34.806091 0 0 1 7.888578-38.539616L485.611815 10.455677a36.733071 36.733071 0 0 1 51.245645 0l174.271325 170.176491a34.806091 34.806091 0 0 1 0 50.041283 36.913726 36.913726 0 0 1-51.245645 0z" p-id="59529" fill="#ffffff"></path></svg>
  投稿</button>
</div>

          </div>
        </el-col>
        </el-row>

      </div>

      <el-divider  class="top-divider"/>
    </el-col>
  </el-row>
</el-affix>

<!-- 视频标题以及数据信息 -->
 <div class="video-top">
  <span class="video-title">{{ currentVideo.title }}</span>
  <div class="video-meta">
    <svg t="1743757350808" class="video-icon1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="28554" width="200" height="200"><path d="M759.528727 209.408H264.424727A53.853091 53.853091 0 0 0 209.454545 262.004364v499.898181a53.992727 53.992727 0 0 0 54.970182 52.689455h495.080728a53.829818 53.829818 0 0 0 55.016727-52.689455v-499.898181a53.690182 53.690182 0 0 0-54.993455-52.596364z m0 552.494545H264.424727V262.097455h495.080728v499.80509z" fill="#666666" p-id="28555"></path><path d="M436.386909 360.727273v302.498909L625.524364 512 436.386909 360.727273z"  p-id="28556"></path></svg>
    <span class="video-views">{{ formatViews(currentVideo.views)}}</span>
    <svg t="1743757905181" class="video-icon2" viewBox="0 0 1303 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29556" width="200" height="200"><path d="M110.964364 1023.720727c-60.509091 0-109.847273-48.872727-109.847273-108.916363V109.009455C1.117091 48.965818 50.455273 0 110.964364 0h1081.902545c60.509091 0 109.847273 48.872727 109.847273 108.916364v805.794909c0 60.043636-49.338182 108.916364-109.847273 108.916363H110.964364z m0-927.650909c-6.609455 0-12.101818 5.771636-12.101819 12.939637v805.794909c0 7.168 5.492364 13.032727 12.101819 13.032727h1081.902545c6.609455 0 12.101818-5.864727 12.101818-13.032727V109.009455c0-7.168-5.492364-13.032727-12.101818-13.032728H110.964364z" fill="#5F6269" p-id="29557"></path><path d="M1003.054545 520.098909a51.572364 51.572364 0 0 1-50.26909 52.689455h-80.058182a51.572364 51.572364 0 0 1-50.269091-52.689455c0-29.137455 22.434909-52.782545 50.269091-52.782545h80.058182c27.834182 0 50.269091 23.645091 50.26909 52.782545zM1096.610909 270.149818c0 28.858182-22.341818 52.410182-49.989818 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273h814.08c27.648 0 50.082909 23.458909 50.082909 52.503273zM768.744727 520.098909c0 28.858182-22.341818 52.410182-50.082909 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273H718.661818c27.648 0 50.082909 23.458909 50.082909 52.503273zM1096.610909 735.976727c0 29.044364-22.341818 52.503273-49.989818 52.503273H560.500364a51.293091 51.293091 0 0 1-50.082909-52.503273c0-28.951273 22.341818-52.410182 50.082909-52.410182h486.120727c27.648 0 50.082909 23.458909 50.082909 52.410182z" fill="#5F6269" p-id="29558"></path></svg>
    <span class="video-barrages">{{ currentVideo.barrages }}</span>
    <span class="video-date">{{ formatDate(currentVideo.createdTime) }}</span>
    <span class="video-prohibit">🚫未经作者授权，禁止转载</span>
    </div>
 </div>

 <!-- 视频播放 -->
  <div class="video-player"
  @keyup.space.prevent="togglePlay"
  tabindex="0">
  <videoPlay  
  ref="aplayVideo" 
  v-bind="options"  
  @play="onPlay" 
  @pause="onPause"
  @timeupdate="onTimeupdate" 
  @loadedmetadata="loadEdmetadata"
  @seeking="handleSeeking" 
  @seeked="handleSeeked"
  @ended="onEnded"/>
</div>

<!-- 弹幕区 -->
<div class="baberrage">
  <vue-danmaku v-model:danmus="danmus"
  ref="danmakuInstance"
   style="height:300px; width:915px;">
      <template v-slot:dm="{ danmu }">
        <div class="danmaku-name">
          <span class="bullet-item" 
          :style="{ color: getRandomColor() }">
            {{ danmu }}</span
          >
        </div>
      </template>
    </vue-danmaku>
	</div>


<!-- 发送弹幕 -->
 <div class="video-barrages-container">
  <span class="barrages-message">{{viewsPeople}}&nbsp;人正在看，已装填&nbsp;{{ danmuCount }}&nbsp;条弹幕</span>
  <button class="icon-button" @click="toggleIcon">
    <svg v-if="isActive" :key="'active-icon'" class="icon" viewBox="0 0 1024 1024">
      <path d="M699.050667 57.002667a42.666667 42.666667 0 0 1 62.826666 57.6l-2.496 2.730666-72.832 72.832H832a128 128 0 0 1 128 128v295.637334a212.693333 212.693333 0 0 1 64 152.362666c0 117.824-95.509333 213.333333-213.333333 213.333334a212.394667 212.394667 0 0 1-127.978667-42.624L192 936.832a128 128 0 0 1-128-128v-490.666667a128 128 0 0 1 128-128h142.314667l-69.696-69.717333a42.666667 42.666667 0 1 1 60.330666-60.330667l130.048 130.048h110.848l133.205334-133.162666zM810.666667 616.832a149.333333 149.333333 0 1 0 0 298.666667 149.333333 149.333333 0 0 0 0-298.666667zM770.901333 849.92l-51.242666-51.264a32 32 0 1 1 45.248-45.269333l28.629333 28.650666 75.626667-75.605333a32 32 0 0 1 45.226666 45.248l-98.197333 98.24a31.914667 31.914667 0 0 1-45.290667 0zM832 275.498667H192a42.666667 42.666667 0 0 0-42.56 39.488l-0.106667 3.2v490.666666a42.666667 42.666667 0 0 0 39.466667 42.538667l3.2 0.106667 423.104 0.042666a212.608 212.608 0 0 1-16.938667-104.32l0.064-0.362666c-1.216 10.88-9.109333 16.704-21.226666 16.704-13.418667 0-21.888-6.058667-22.954667-17.258667l-0.106667-2.474667-0.021333-65.28h-97.301333l-2.453334-0.149333a15.829333 15.829333 0 0 1-10.816-5.824c-2.56 32.256-7.957333 52.778667-16.426666 62.016-18.773333 22.549333-50.986667 24.96-95.850667 8.426667-13.269333-5.888-17.365333-17.365333-11.306667-32.362667 6.08-10.624 17.002667-13.738667 31.146667-9.472 23.893333 8.96 36.586667 8.064 40.234667 0.042667 3.989333-8.96 6.698667-30.08 7.893333-63.445334l1.002667-6.826666a65.28 65.28 0 0 0 0.576-7.573334c0.789333-11.136-1.173333-14.912-7.061334-14.890666l-1.557333 0.085333h-38.144l-3.434667 0.085333c-18.218667-0.064-28.202667-10.325333-28.266666-28.181333l0.106666-3.434667 1.664-81.365333-0.085333-3.626667c0.064-20.138667 10.794667-31.253333 29.738667-31.36l3.413333 0.085334 20.736 0.064 2.133333-0.32c5.589333-1.109333 8.469333-4.693333 8.981334-12.330667l0.085333-2.688v-29.76l-0.085333-2.389333c-0.426667-5.354667-2.24-7.616-6.698667-8.042667l-1.813333-0.085333-51.114667-0.085334-2.666667-0.490666c-10.453333-2.474667-15.957333-10.602667-15.914666-23.189334 1.344-12.096 7.914667-19.498667 18.602666-20.565333l2.773334-0.128h63.168l3.605333-0.085333c22.442667 0.128 34.581333 12.522667 34.709333 34.581333l-0.085333 3.562667v66.816l-0.085333 3.946666c-0.96 21.589333-10.261333 33.898667-26.666667 34.069334l-2.986667-0.085334h-28.8l-1.728-0.149333c-2.624-0.042667-3.584 1.024-3.264 6.656l0.192 2.346667-1.728 42.261333-0.32 2.346667c-0.682667 6.634667 0.405333 8.234667 7.04 7.36l1.92-0.298667 40.789334 0.064 3.093333 0.384c15.061333 2.56 23.146667 13.952 24.106667 32.128l0.085333 3.733333c0.213333 14.357333 0.213333 27.584-0.085333 39.68a15.872 15.872 0 0 1 9.130666-4.138666l2.901334-0.170667h96.917333v-25.557333h-60.096l-3.434667 0.106666c-20.48-0.149333-31.488-12.053333-31.552-32.917333l0.106667-3.562667v-142.848l-0.277333-3.584c-1.066667-21.546667 10.048-32.96 30.08-31.914666l3.242666 0.277333h8.32l-2.453333-3.989333c-2.56-3.989333-4.906667-7.530667-7.104-10.624l-2.133333-2.944-2.048-2.666667c-6.997333-11.178667-4.586667-22.016 6.122666-30.293333l2.432-1.749334 0.682667-0.405333c11.541333-5.76 22.485333-3.413333 31.786667 7.594667 5.973333 10.624 11.733333 20.650667 17.301333 30.08l9.024 14.976h39.616l19.2-29.290667c2.496-6.293333 5.12-10.88 7.466667-13.141333 8.448-11.264 19.989333-14.336 33.514666-8.256 11.882667 7.445333 14.357333 18.986667 6.826667 32.490666l-12.906667 18.197334h14.613334l3.114666 0.149333c17.429333 1.365333 26.88 13.482667 27.946667 33.984l0.085333 3.946667v140.202666l-0.106666 3.626667c-0.277333 4.416-1.066667 8.384-2.304 11.946667A211.968 211.968 0 0 1 810.666667 552.832c22.293333 0 43.797333 3.413333 64 9.770667V318.165333a42.666667 42.666667 0 0 0-39.466667-42.538666l-3.2-0.128zM616.106667 678.549333h-17.706667v65.621334l-0.106667 1.728a211.989333 211.989333 0 0 1 17.813334-67.349334z m49.728-69.013333l-1.813334 0.277333a52.906667 52.906667 0 0 1-3.413333 0.32l-3.626667 0.106667H598.4v23.850667l44.693333 0.042666a214.613333 214.613333 0 0 1 22.741334-24.597333z m-111.914667-82.645333h-48.853333v27.306666l-0.170667 2.773334-0.021333 2.133333c0.064 4.629333 0.896 6.357333 3.157333 6.698667l1.066667 0.064 1.237333-0.106667h43.584v-38.869333z m94.997333 0H598.4v37.205333h41.941333l1.984-0.085333c3.690667-0.32 5.546667-1.706667 6.272-4.821334l0.256-1.706666 0.085334-1.984-0.021334-28.586667z m-4.48-85.248l-1.258666 0.021333-2.837334 0.234667H598.4v38.848h48.853333l0.021334-34.069334 0.149333-1.770666v-1.045334c-0.128-1.237333-0.576-1.898667-1.856-2.133333l-1.130667-0.085333z m-136.490666 0.064h-1.045334l-0.810666 0.085333c-0.96 0.213333-1.28 0.789333-1.28 2.304l0.085333 1.344 0.170667 1.728v33.578667h48.853333v-38.826667l-44.117333-0.042667-1.856-0.170666z"  fill-opacity=".88" p-id="5331"></path>
    </svg>
    <svg v-else :key="'inactive-icon'" class="icon-1" viewBox="0 0 1024 1024">
      <path d="M699.050667 57.002667a42.666667 42.666667 0 0 1 62.826666 57.6l-2.496 2.730666-72.832 72.832H832a128 128 0 0 1 128 128v295.637334a212.693333 212.693333 0 0 1 64 152.362666c0 117.824-95.509333 213.333333-213.333333 213.333334a212.394667 212.394667 0 0 1-127.978667-42.624L192 936.832a128 128 0 0 1-128-128v-490.666667a128 128 0 0 1 128-128h142.314667l-69.696-69.717333a42.666667 42.666667 0 1 1 60.330666-60.330667l130.048 130.048h110.848l133.205334-133.162666zM810.666667 616.832a149.333333 149.333333 0 1 0 0 298.666667 149.333333 149.333333 0 0 0 0-298.666667z m21.333333-341.333333H192a42.666667 42.666667 0 0 0-42.56 39.488l-0.106667 3.2v490.666666a42.666667 42.666667 0 0 0 39.466667 42.538667l3.2 0.106667 423.104 0.042666a212.608 212.608 0 0 1-16.938667-104.32l0.064-0.362666c-1.216 10.88-9.109333 16.704-21.226666 16.704-13.418667 0-21.888-6.058667-22.954667-17.258667l-0.106667-2.474667-0.021333-65.28h-97.301333l-2.453334-0.149333a15.829333 15.829333 0 0 1-10.816-5.824c-2.56 32.256-7.957333 52.778667-16.426666 62.016-18.773333 22.549333-50.986667 24.96-95.850667 8.426667-13.269333-5.888-17.365333-17.365333-11.306667-32.362667 6.08-10.624 17.002667-13.738667 31.146667-9.472 23.893333 8.96 36.586667 8.064 40.234667 0.042667 3.989333-8.96 6.698667-30.08 7.893333-63.445334l1.002667-6.826666a65.28 65.28 0 0 0 0.576-7.573334c0.789333-11.136-1.173333-14.912-7.061334-14.890666l-1.557333 0.085333h-38.144l-3.434667 0.085333c-18.218667-0.064-28.202667-10.325333-28.266666-28.181333l0.106666-3.434667 1.664-81.365333-0.085333-3.626667c0.064-20.138667 10.794667-31.253333 29.738667-31.36l3.413333 0.085334 20.736 0.064 2.133333-0.32c5.589333-1.109333 8.469333-4.693333 8.981334-12.330667l0.085333-2.688v-29.76l-0.085333-2.389333c-0.426667-5.354667-2.24-7.616-6.698667-8.042667l-1.813333-0.085333-51.114667-0.085334-2.666667-0.490666c-10.453333-2.474667-15.957333-10.602667-15.914666-23.189334 1.344-12.096 7.914667-19.498667 18.602666-20.565333l2.773334-0.128h63.168l3.605333-0.085333c22.442667 0.128 34.581333 12.522667 34.709333 34.581333l-0.085333 3.562667v66.816l-0.085333 3.946666c-0.96 21.589333-10.261333 33.898667-26.666667 34.069334l-2.986667-0.085334h-28.8l-1.728-0.149333c-2.624-0.042667-3.584 1.024-3.264 6.656l0.192 2.346667-1.728 42.261333-0.32 2.346667c-0.682667 6.634667 0.405333 8.234667 7.04 7.36l1.92-0.298667 40.789334 0.064 3.093333 0.384c15.061333 2.56 23.146667 13.952 24.106667 32.128l0.085333 3.733333c0.213333 14.357333 0.213333 27.584-0.085333 39.68a15.872 15.872 0 0 1 9.130666-4.138666l2.901334-0.170667h96.917333v-25.557333h-60.096l-3.434667 0.106666c-20.48-0.149333-31.488-12.053333-31.552-32.917333l0.106667-3.562667v-142.848l-0.277333-3.584c-1.066667-21.546667 10.048-32.96 30.08-31.914666l3.242666 0.277333h8.32l-2.453333-3.989333c-2.56-3.989333-4.906667-7.530667-7.104-10.624l-2.133333-2.944-2.048-2.666667c-6.997333-11.178667-4.586667-22.016 6.122666-30.293333l2.432-1.749334 0.682667-0.405333c11.541333-5.76 22.485333-3.413333 31.786667 7.594667 5.973333 10.624 11.733333 20.650667 17.301333 30.08l9.024 14.976h39.616l19.2-29.290667c2.496-6.293333 5.12-10.88 7.466667-13.141333 8.448-11.264 19.989333-14.336 33.514666-8.256 11.882667 7.445333 14.357333 18.986667 6.826667 32.490666l-12.906667 18.197334h14.613334l3.114666 0.149333c17.429333 1.365333 26.88 13.482667 27.946667 33.984l0.085333 3.946667v140.202666l-0.106666 3.626667c-0.277333 4.416-1.066667 8.384-2.304 11.946667A211.968 211.968 0 0 1 810.666667 552.832c22.293333 0 43.797333 3.413333 64 9.770667V318.165333a42.666667 42.666667 0 0 0-39.466667-42.538666l-3.2-0.128zM780.501333 690.773333l105.6 105.6a32 32 0 0 1-45.269333 45.226667l-105.6-105.578667a32 32 0 0 1 45.269333-45.248z m-164.394666-12.202666h-17.706667v65.621333l-0.106667 1.728a211.989333 211.989333 0 0 1 17.813334-67.349333z m49.728-69.013334l-1.813334 0.277334a52.906667 52.906667 0 0 1-3.413333 0.32l-3.626667 0.106666H598.4v23.850667l44.693333 0.042667a214.613333 214.613333 0 0 1 22.741334-24.597334z m-111.914667-82.645333h-48.853333v27.306667l-0.170667 2.773333-0.021333 2.133333c0.064 4.629333 0.896 6.357333 3.157333 6.698667l1.066667 0.064 1.237333-0.106667h43.584v-38.869333z m94.997333 0H598.4v37.205333h41.941333l1.984-0.085333c3.690667-0.32 5.546667-1.706667 6.272-4.821333l0.256-1.706667 0.085334-1.984-0.021334-28.586667z m-4.48-85.248l-1.258666 0.021333-2.837334 0.234667H598.4v38.848h48.853333l0.021334-34.069333 0.149333-1.770667v-1.045333c-0.128-1.237333-0.576-1.898667-1.856-2.133334l-1.130667-0.085333z m-136.490666 0.064h-1.045334l-0.810666 0.085333c-0.96 0.213333-1.28 0.789333-1.28 2.304l0.085333 1.344 0.170667 1.728v33.578667h48.853333v-38.826667l-44.117333-0.042666-1.856-0.170667z"  fill-opacity=".88" p-id="9783"></path>
    </svg>
  </button>
  <!-- From Uiverse.io by mpody11 --> 
<div class="messageBox">
  <div class="fileUploadWrapper">
    <input name="file" id="file" type="file"/>
  </div>
  <svg t="1744186800347" class="messageBox-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5204" width="200" height="200"><path d="M470.975 137l-214.462 562.5h102l44.513-131.25h217.949l48.038 131.25h102.002l-214.501-562.5h-85.5zM512 256.55l78.525 233.175h-160.538l82.013-233.212zM212 774.5v112.5h599.999v-112.5h-599.999z" p-id="5205"></path></svg>
  <input id="messageInput"
   type="text" 
   placeholder="发个友善的弹幕见证当下" 
   required="" 
   v-model="newDanmu"
    @keydown.space.prevent="handleSpace"
    @keydown.enter="sendDanmu" />
  <button id="sendButton"  @click="sendDanmu"
  :disabled="!isActive">
    <svg viewBox="0 0 664 663" xmlns="http://www.w3.org/2000/svg">
      <path
        d="M646.293 331.888L17.7538 17.6187L155.245 331.888M646.293 331.888L17.753 646.157L155.245 331.888M646.293 331.888L318.735 330.228L155.245 331.888"
      ></path>
      <path
        d="M646.293 331.888L17.7538 17.6187L155.245 331.888M646.293 331.888L17.753 646.157L155.245 331.888M646.293 331.888L318.735 330.228L155.245 331.888"
        stroke="white"
        stroke-width="33.67"
        stroke-linecap="round"
        stroke-linejoin="round"
      ></path>
    </svg>
  </button>
</div>

 </div>

 <!-- 视频操作区 -->
  <div class="video-do">
    <botton @click="toLike">
    <svg  v-if="isLike" :key="'active-icon'" t="1744027672488" class="video-do-icon1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="11973" width="200" height="200"><path d="M773.6 912.7h-1.2c-37.2-0.4-74.5-0.4-111.8-0.4h-56.9c-38 0-76 0-114.1-0.5-21.1-0.6-41.9-5-61.5-13-33.3-13-52.3-42-52.2-79.7l0.1-141.4c0-78.3 0-156.7 0.7-235 0.1-21.3 13.8-41.3 25.5-51.8 45.3-41.4 94.5-93 115.1-162.6 5.7-19.4 7.9-40.8 10.2-63.4 4.6-45 33.8-74.3 72.8-74.3 15.3 0 30.6 4.6 45.6 13.5 30.1 18.1 50.2 46.5 61.3 87 17.8 64.3 8.7 126.7-1.3 180.2v0.2c-2.3 12.5 7.1 24.1 19.9 24.1h130c22.4 0 54.5 2.8 74.3 26.8 14.4 17.5 18.5 41.1 12.4 72.3-18.6 95.9-41.4 192.6-63.2 282.7-6.8 28.1-18.1 54.1-29 79.3l-4.7 10.8c-12.4 29.2-38 45.2-72 45.2zM216.1 903.3h-11.9c-43 0-78.2-35.2-78.2-78.2V476.6c0-43 35.2-78.2 78.2-78.2h11.9c43 0 78.2 35.2 78.2 78.2V825c0.1 43.1-35.1 78.3-78.2 78.3z"  fill="rgb(18, 150, 219)" p-id="11974"></path></svg>
    <svg  v-else :key="'inactive-icon'" t="1744027672488" class="video-do-icon1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="11973" width="200" height="200"><path d="M773.6 912.7h-1.2c-37.2-0.4-74.5-0.4-111.8-0.4h-56.9c-38 0-76 0-114.1-0.5-21.1-0.6-41.9-5-61.5-13-33.3-13-52.3-42-52.2-79.7l0.1-141.4c0-78.3 0-156.7 0.7-235 0.1-21.3 13.8-41.3 25.5-51.8 45.3-41.4 94.5-93 115.1-162.6 5.7-19.4 7.9-40.8 10.2-63.4 4.6-45 33.8-74.3 72.8-74.3 15.3 0 30.6 4.6 45.6 13.5 30.1 18.1 50.2 46.5 61.3 87 17.8 64.3 8.7 126.7-1.3 180.2v0.2c-2.3 12.5 7.1 24.1 19.9 24.1h130c22.4 0 54.5 2.8 74.3 26.8 14.4 17.5 18.5 41.1 12.4 72.3-18.6 95.9-41.4 192.6-63.2 282.7-6.8 28.1-18.1 54.1-29 79.3l-4.7 10.8c-12.4 29.2-38 45.2-72 45.2zM216.1 903.3h-11.9c-43 0-78.2-35.2-78.2-78.2V476.6c0-43 35.2-78.2 78.2-78.2h11.9c43 0 78.2 35.2 78.2 78.2V825c0.1 43.1-35.1 78.3-78.2 78.3z"  p-id="11974"></path></svg>
  </botton>
    <span class="video-do-likes-text">{{ formatViews(likesCount) }}</span>
    <botton @click="toCoin">
      <svg v-if="isCoin" :key="'active-icon'" t="1744027980656" class="video-do-icon2" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="13011" width="200" height="200"><path d="M512 977.454545C254.929455 977.454545 46.545455 769.070545 46.545455 512S254.929455 46.545455 512 46.545455s465.454545 208.384 465.454545 465.454545a465.454545 465.454545 0 0 1-465.454545 465.454545zM367.941818 246.039273a33.233455 33.233455 0 0 0 0 66.466909h288.116364a33.233455 33.233455 0 0 0 0-66.466909h-288.116364z m177.338182 123.205818V312.506182h-66.56v56.785454A210.106182 210.106182 0 0 0 279.272727 578.466909v44.311273a33.233455 33.233455 0 0 0 66.513455 0v-44.311273a143.639273 143.639273 0 0 1 132.980363-142.754909v310.318545a33.233455 33.233455 0 1 0 66.46691 0V435.758545a143.639273 143.639273 0 0 1 132.980363 142.75491v44.311272a33.233455 33.233455 0 0 0 66.513455 0v-44.311272a210.106182 210.106182 0 0 0-199.493818-209.268364z" fill="rgb(18, 150, 219)" p-id="13012"></path></svg>
    <svg v-else :key="'inactive-icon'" t="1744027980656" class="video-do-icon2" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="13011" width="200" height="200"><path d="M512 977.454545C254.929455 977.454545 46.545455 769.070545 46.545455 512S254.929455 46.545455 512 46.545455s465.454545 208.384 465.454545 465.454545a465.454545 465.454545 0 0 1-465.454545 465.454545zM367.941818 246.039273a33.233455 33.233455 0 0 0 0 66.466909h288.116364a33.233455 33.233455 0 0 0 0-66.466909h-288.116364z m177.338182 123.205818V312.506182h-66.56v56.785454A210.106182 210.106182 0 0 0 279.272727 578.466909v44.311273a33.233455 33.233455 0 0 0 66.513455 0v-44.311273a143.639273 143.639273 0 0 1 132.980363-142.754909v310.318545a33.233455 33.233455 0 1 0 66.46691 0V435.758545a143.639273 143.639273 0 0 1 132.980363 142.75491v44.311272a33.233455 33.233455 0 0 0 66.513455 0v-44.311272a210.106182 210.106182 0 0 0-199.493818-209.268364z"  p-id="13012"></path></svg>
   </botton>
    <span class="video-do-coins-text">{{ formatViews(coinsCount) }}</span>
    <botton @click="toFavorites">
      <svg v-if="isFavorites" :key="'active-icon'" t="1744028154780" class="video-do-icon3" viewBox="0 0 1426 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="13990" width="200" height="200"><path d="M985.6 1022.976c-14.848 0-31.744-4.096-47.104-12.288L716.288 899.584l-223.744 111.104c-14.336 7.68-30.208 11.776-47.104 11.776-21.504 0-42.496-6.656-59.392-19.456-31.232-23.552-47.104-64-39.936-101.376l45.568-237.056-175.616-163.328c-27.136-27.648-37.376-67.072-27.136-104.448l0.512-1.024c12.8-38.4 44.544-65.024 82.944-70.144l243.712-44.544L625.152 58.88C642.56 23.552 678.4 1.024 716.288 1.024c39.424 0 76.288 23.552 91.648 58.368l109.056 221.696 243.712 42.496c38.4 5.632 70.656 33.28 81.408 71.168 12.288 36.864 2.048 77.312-25.6 104.96l-0.512 0.512-174.592 164.864 44.032 237.568c7.168 37.888-8.192 76.288-39.424 100.352-17.92 12.8-38.912 19.968-60.416 19.968z" fill="rgb(18, 150, 219)" p-id="13991"></path></svg>
    <svg v-else :key="'inactive-icon'" t="1744028154780" class="video-do-icon3" viewBox="0 0 1426 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="13990" width="200" height="200"><path d="M985.6 1022.976c-14.848 0-31.744-4.096-47.104-12.288L716.288 899.584l-223.744 111.104c-14.336 7.68-30.208 11.776-47.104 11.776-21.504 0-42.496-6.656-59.392-19.456-31.232-23.552-47.104-64-39.936-101.376l45.568-237.056-175.616-163.328c-27.136-27.648-37.376-67.072-27.136-104.448l0.512-1.024c12.8-38.4 44.544-65.024 82.944-70.144l243.712-44.544L625.152 58.88C642.56 23.552 678.4 1.024 716.288 1.024c39.424 0 76.288 23.552 91.648 58.368l109.056 221.696 243.712 42.496c38.4 5.632 70.656 33.28 81.408 71.168 12.288 36.864 2.048 77.312-25.6 104.96l-0.512 0.512-174.592 164.864 44.032 237.568c7.168 37.888-8.192 76.288-39.424 100.352-17.92 12.8-38.912 19.968-60.416 19.968z"  p-id="13991"></path></svg>
  </botton>
    <span class="video-do-favorites-text">{{ formatViews(favoritesCount) }}</span>
    <botton @click="toShare">
      <svg v-if="isShare" :key="'active-icon'" t="1744028358041" class="video-do-icon4" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="15006" width="200" height="200"><path d="M565.934 817.574a34.816 34.816 0 0 0 9.818 29.394l0.302 0.241a35.539 35.539 0 0 0 25.6 10.963c11.143 0 20.66-5.42 27.226-13.312l354.545-387.072a35.117 35.117 0 0 0 10.24-27.106 35.057 35.057 0 0 0-10.24-27.106L626.892 14.336a36.503 36.503 0 0 0-51.2 0 34.936 34.936 0 0 0-9.758 29.395V253.35c-295.996 0-535.974 238.893-535.974 533.684a529.468 529.468 0 0 0 44.454 212.51C116.7 777.276 329.812 608.437 565.332 608.437l0.602 209.137z" fill="rgb(18, 150, 219)" p-id="15007"></path></svg>
    <svg v-else :key="'inactive-icon'" t="1744028358041" class="video-do-icon4" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="15006" width="200" height="200"><path d="M565.934 817.574a34.816 34.816 0 0 0 9.818 29.394l0.302 0.241a35.539 35.539 0 0 0 25.6 10.963c11.143 0 20.66-5.42 27.226-13.312l354.545-387.072a35.117 35.117 0 0 0 10.24-27.106 35.057 35.057 0 0 0-10.24-27.106L626.892 14.336a36.503 36.503 0 0 0-51.2 0 34.936 34.936 0 0 0-9.758 29.395V253.35c-295.996 0-535.974 238.893-535.974 533.684a529.468 529.468 0 0 0 44.454 212.51C116.7 777.276 329.812 608.437 565.332 608.437l0.602 209.137z" p-id="15007"></path></svg>
  </botton>
    <span class="video-do-zf-text">{{ formatViews(sharesCount) }}</span>
    <svg t="1744032729866" class="video-do-icon5" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="28285" width="200" height="200"><path d="M 985.558 336.376 l -83.7917 -83.7917 c -19.7233 -19.7233 -50.3073 -21.2219 -68.1774 -3.35167 L 387.64 695.311 c -6.60665 6.59054 -11.8114 11.7953 -27.0551 116.019 a 60.2978 60.2978 0 0 0 59.3632 68.9993 a 62.0059 62.0059 0 0 0 10.4417 -0.886258 c 100.953 -17.3384 106.351 -22.6882 112.49 -28.8759 l 446.03 -446.014 c 17.8702 -17.8219 16.3555 -48.4381 -3.35167 -68.1774 Z M 511.475 813.602 c -5.83319 1.93366 -25.6048 7.25121 -89.2543 18.1764 a 11.9403 11.9403 0 0 1 -13.8095 -13.4711 c 9.53936 -65.1158 14.583 -85.7898 16.3877 -91.8486 l 441.034 -441.034 a 7.89576 7.89576 0 0 1 1.80474 1.33745 l 83.7917 83.7917 a 7.55737 7.55737 0 0 1 1.33744 1.80474 Z" fill="#231815" p-id="28286"></path><path d="M 841.968 621.204 a 32.2276 32.2276 0 0 0 -32.2276 32.2276 v 193.978 c 0 61.8286 -56.0276 112.136 -124.882 112.136 H 211.951 c -68.8542 0 -124.882 -50.3073 -124.882 -112.136 V 176.608 C 87.0696 114.763 143.097 64.456 211.951 64.456 H 684.891 c 61.9092 0 115.085 41.5575 123.689 96.6827 a 32.2276 32.2276 0 1 0 63.6817 -9.94221 c -6.5422 -41.8959 -29.2465 -80.3756 -63.9234 -108.269 A 197.313 197.313 0 0 0 684.891 0.000838 H 211.951 C 107.55 0.000838 22.6144 79.2323 22.6144 176.608 v 670.801 c 0 97.3756 84.9358 176.591 189.337 176.591 H 684.891 c 104.401 0 189.337 -79.2154 189.337 -176.591 V 653.431 a 32.2276 32.2276 0 0 0 -32.2598 -32.2276 Z" fill="#231815" p-id="28287"></path><path d="M 492.783 242.159 H 227.501 a 24.1707 24.1707 0 0 0 0 48.3414 h 265.281 a 24.1707 24.1707 0 0 0 0 -48.3414 Z M 492.783 364.044 H 227.501 a 24.1707 24.1707 0 0 0 0 48.3414 h 265.281 a 24.1707 24.1707 0 0 0 0 -48.3414 Z"  p-id="28288"></path></svg>
    <span class="video-do-note-text">记笔记</span>
    <svg t="1744021824914" class="video-do-icon6" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2562" width="200" height="200"><path d="M512 298.6496a85.3504 85.3504 0 1 0 0-170.6496 85.3504 85.3504 0 0 0 0 170.6496z" fill="#5A5A68" p-id="2563"></path><path d="M512 512m-85.3504 0a85.3504 85.3504 0 1 0 170.7008 0 85.3504 85.3504 0 1 0-170.7008 0Z" fill="#5A5A68" p-id="2564"></path><path d="M512 896a85.3504 85.3504 0 1 0 0-170.7008 85.3504 85.3504 0 0 0 0 170.7008z"  p-id="2565"></path></svg>
    <el-divider  class="vd-divider"/>
  </div>

<!-- 视频简介及标签 -->
 <div ref="descTagsContainer"
    class="video-descriptionAndTags"
    :style="descTagsStyle">
    <div class="video-description">
      <!-- 动态绑定高度和滚动 -->
  <ul 
    v-infinite-scroll="ideoDescriptionLoad"
    class="video-description-infinite-list"
    style="overflow: auto"
  >
    <!-- 视频简介内容 -->
    <div class="video-descriptionText" >
      <pre class="lyric-text">{{ descriptionText }}</pre>
    </div>
    
    <!-- 加载状态提示 -->
    <div v-if="loading" class="loading-hint"></div>
  </ul>
    </div>
    <div class="video-tags">
      <el-tag v-for="(tag, index) in tags" :key="index" class="video-tag" type="info">
        {{ tag }}
      </el-tag>
    </div>
    <el-divider  class="vdt-divider"/>
 </div>

<!-- 视频评论 -->
 <div class="video-comments">
  <div class="video-comment-title">
    <span class="comment-title-text">评论</span>
    <span class="comment-title-count">{{ currentVideo.comments }}</span>
    <div class="comment-tabs">
  <span 
    class="video-comment-hotest" 
    :class="{ 'tab-active': activeTab === 'hot' }"
    @click.stop="switchTab('hot')"
  >最热</span>
  <el-divider class="vc-divider" direction="vertical" />
  <span 
    class="video-comment-newest" 
    :class="{ 'tab-active': activeTab === 'new' }"
    @click.stop="switchTab('new')"
  >最新</span>
</div>

<!-- 输入评论 -->
 <div class="video-comment-input">
  <el-avatar :src="userInfo.image"
   :size="40" 
   class="vc-avatar" 
   ></el-avatar>
  <!-- From Uiverse.io by ercnersoy --> 
<input type="text" name="text" class="vc-input" 
placeholder="与其赞同别人的话语，不如自己畅所欲言。"
v-model="commentInput"
    @keydown.space.prevent="handleSpace"
    @keydown.enter="giveCurrentVideoComment" >
 </div>

 <!-- 评论操作 -->
  <div class="video-comment-operation">
    <botton class="vc-at" @click="AtDialogVisibleOn">
      <svg t="1744279969705" class="vc-at-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3548" width="200" height="200"><path d="M657.476791 626.845655c-5.884011 26.478048 1.471514 47.072085 22.065552 61.782112l26.478048 0c108.854197-50.01409 161.810293-148.57178 158.868287-295.672046-20.594037-158.868287-136.803759-247.128447-348.628142-264.780479-144.158261 8.826016-248.599961 72.079642-313.324078 189.759855-32.362059 61.782112-50.01409 129.448234-52.956096 202.998367 11.768021 164.752298 85.318154 276.5485 220.650399 335.388607 155.926282 47.072085 300.084543 16.181541 432.474782-92.673679l48.543599 52.956096c-111.796202 97.086176-239.773945 142.68777-383.932206 136.803759-161.810293-8.826016-279.490505-89.731674-353.040638-242.715951-32.362059-76.492138-45.601594-157.397796-39.717583-242.715951 11.768021-152.984277 91.202165-275.078009 238.302431-366.280174 79.434144-29.420053 150.042271-42.659589 211.824383-39.717583 170.636309 2.942005 294.200532 67.666122 370.69267 194.172351 29.420053 44.13008 42.659589 110.325711 39.717583 198.585871-20.594037 164.752298-97.086176 269.193998-229.476415 313.324078l-52.956096 0c-47.072085-14.710027-70.608128-41.188074-70.608128-79.434144l0-4.41352c-52.956096 55.898101-113.267716 80.905658-180.933839 75.021647-58.840106-8.826016-100.028181-38.981826-123.564223-90.466408s-25.007557-117.680213-4.41352-198.585871 66.195631-147.836023 136.803759-200.792119c79.434144-47.072085 155.926282-45.601594 229.476415 4.41352l26.478048 35.304064 13.239536-48.543599 88.26016 0L657.476791 626.845655zM339.739193 560.650023c-5.884011 61.782112 13.239536 100.028181 57.369615 114.738207l57.369615 4.41352c111.796202-47.072085 172.107823-147.100266 180.933839-300.084543-8.826016-44.13008-44.13008-67.666122-105.912192-70.608128C426.528861 329.703117 363.275235 413.549757 339.739193 560.650023z" p-id="3549"></path></svg>
    </botton>
    <el-upload
                class="avatar-uploader"
                action="/api/upload"
                :headers="{'token':token}"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                >
    <botton class="vc-pic">
      <svg t="1744280525370" class="vc-pic-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6352" width="200" height="200"><path d="M937.472 102.4H91.136c-46.08 0-83.456 37.376-83.456 83.456V841.728c0 46.08 37.376 83.456 83.456 83.456h846.336c46.08 0 83.456-37.376 83.456-83.456V185.856c0-46.08-37.376-83.456-83.456-83.456zM91.136 163.84h846.336c12.288 0 22.016 9.728 22.016 22.016v437.248L762.88 448.512c-10.752-9.728-27.136-10.24-38.912-1.536l-244.224 183.296-129.536-92.16c-10.24-7.68-24.576-7.68-35.328 0l-245.76 174.08V185.856c0-12.288 9.728-22.016 22.016-22.016z m846.336 699.904H91.136c-12.288 0-22.016-9.728-22.016-22.016v-54.272L332.8 601.088l247.808 176.64c13.824 9.728 33.28 6.656 43.008-7.168 9.728-13.824 6.656-33.28-7.168-43.008l-84.48-59.904 208.896-156.672 211.456 187.904c2.048 2.048 4.608 3.072 7.168 4.608v138.24c0 12.288-9.728 22.016-22.016 22.016z" fill="#646464" p-id="6353"></path><path d="M312.32 471.04c70.656 0 128-57.344 128-128S382.976 215.04 312.32 215.04 184.32 272.384 184.32 343.04s57.344 128 128 128z m0-194.56c36.864 0 66.56 29.696 66.56 66.56s-29.696 66.56-66.56 66.56-66.56-29.696-66.56-66.56 29.696-66.56 66.56-66.56z" fill="#646464" p-id="6354"></path></svg>
    </botton>
    </el-upload>
    <el-button type="primary" class="vc-publish"
     :disabled="commentInput === ''"
     @click="giveCurrentVideoComment"
     >发布</el-button>
  </div>
  </div>

  <!-- 评论列表 -->
  <div class="video-comments-list">

    <div v-for="(comment, index) in commentList" :key="index" class="video-comment-item">
      <el-avatar :src= "comment.image"
   :size="40" 
   class="vc-list-avatar" 
   ></el-avatar>
   <div class="vc-list-name">
   <span >{{ comment.username }}</span>
  </div>
  <div class="vc-list-content">
   <span  v-html="processCommentContent(comment.content)"  ref="contentContainer"></span>
   <div class="vc-img" v-if="comment.imageUrl != null">
    <img class="vc-img-item" :src="comment.imageUrl" alt=""></img>
   </div>
  </div>

   <div class="vc-list-info">
   <span class="vc-list-time">{{ formatDate(comment.createdTime) }}</span>
   <botton @click="toCommentLike(comment)">
    <svg  v-if="comment.isCommentLike" :key="'active-icon11'" t="1744027672488" class="vc-icon1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="11973" width="200" height="200"><path d="M411.904 153.728c19.797333-63.232 54.186667-90.24 122.026667-70.656l1.706666 0.554667c19.84 6.101333 42.666667 17.706667 64.085334 37.162666 33.706667 30.72 53.76 73.301333 53.76 126.805334 0 47.786667-2.773333 77.312-10.88 110.805333l-0.256 0.938667h175.488c107.264 0 149.888 72.362667 122.922666 192.682666l-2.304 9.856-5.461333 18.005334-20.608 67.114666-9.642667 30.677334-9.173333 28.672-17.066667 51.626666-11.648 33.621334-7.210666 20.053333-9.984 26.368-6.101334 15.232c-29.525333 71.253333-90.453333 103.978667-170.112 94.592l-387.114666-28.8a587.690667 587.690667 0 0 0-7.381334-0.341333l-15.36-0.341334H218.026667l-12.501334-0.213333-9.984-0.426667-8.32-0.768-3.712-0.554666-7.125333-1.408-11.52-3.029334c-59.349333-17.621333-90.24-67.925333-90.24-139.605333v-283.52c0-90.538667 54.954667-142.208 148.565333-142.208l75.776-0.042667 5.205334-3.968a293.632 293.632 0 0 0 72.234666-88.32l6.101334-11.946666c6.101333-12.544 11.093333-25.685333 15.829333-41.002667l0.768-2.602667z m88.661333 8.064c-1.834667-0.426667-2.645333 0.170667-3.541333 2.773333l-3.882667 14.933334-10.666666 38.442666-2.56 8.533334a366.933333 366.933333 0 0 1-20.565334 53.162666 387.754667 387.754667 0 0 1-72.618666 102.442667 333.141333 333.141333 0 0 1-49.28 42.026667l5.504-3.925334v417.408l336.682666 25.344c41.898667 4.906667 65.621333-6.101333 80.213334-36.096l2.858666-6.229333 5.76-14.378667 9.514667-25.173333 6.912-19.285333 11.221333-32.469334 8.064-24.064 17.365334-53.76 19.2-61.354666 15.445333-50.858667c18.986667-76.074667 7.808-94.592-38.357333-94.592h-217.685334a53.632 53.632 0 0 1-50.730666-71.125333l2.176-6.4 3.328-10.922667c10.282667-35.754667 13.226667-59.136 13.226666-108.629333 0-48.426667-26.88-72.96-57.045333-82.261334l-3.712-1.152z m-242.944 270.122667h-34.389333c-47.616 0-63.232 14.72-63.232 56.917333v283.52c0 38.016 9.941333 53.333333 33.792 59.008l1.493333 0.341333 3.754667 0.554667 5.12 0.426667 11.562667 0.256h28.586666l13.312 0.085333v-401.066667z" fill="rgb(18, 150, 219)" p-id="7369"></path></svg>
    <svg  v-else :key="'inactive-icon11'" t="1744027672488" class="vc-icon1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="11973" width="200" height="200"><path d="M411.904 153.728c19.797333-63.232 54.186667-90.24 122.026667-70.656l1.706666 0.554667c19.84 6.101333 42.666667 17.706667 64.085334 37.162666 33.706667 30.72 53.76 73.301333 53.76 126.805334 0 47.786667-2.773333 77.312-10.88 110.805333l-0.256 0.938667h175.488c107.264 0 149.888 72.362667 122.922666 192.682666l-2.304 9.856-5.461333 18.005334-20.608 67.114666-9.642667 30.677334-9.173333 28.672-17.066667 51.626666-11.648 33.621334-7.210666 20.053333-9.984 26.368-6.101334 15.232c-29.525333 71.253333-90.453333 103.978667-170.112 94.592l-387.114666-28.8a587.690667 587.690667 0 0 0-7.381334-0.341333l-15.36-0.341334H218.026667l-12.501334-0.213333-9.984-0.426667-8.32-0.768-3.712-0.554666-7.125333-1.408-11.52-3.029334c-59.349333-17.621333-90.24-67.925333-90.24-139.605333v-283.52c0-90.538667 54.954667-142.208 148.565333-142.208l75.776-0.042667 5.205334-3.968a293.632 293.632 0 0 0 72.234666-88.32l6.101334-11.946666c6.101333-12.544 11.093333-25.685333 15.829333-41.002667l0.768-2.602667z m88.661333 8.064c-1.834667-0.426667-2.645333 0.170667-3.541333 2.773333l-3.882667 14.933334-10.666666 38.442666-2.56 8.533334a366.933333 366.933333 0 0 1-20.565334 53.162666 387.754667 387.754667 0 0 1-72.618666 102.442667 333.141333 333.141333 0 0 1-49.28 42.026667l5.504-3.925334v417.408l336.682666 25.344c41.898667 4.906667 65.621333-6.101333 80.213334-36.096l2.858666-6.229333 5.76-14.378667 9.514667-25.173333 6.912-19.285333 11.221333-32.469334 8.064-24.064 17.365334-53.76 19.2-61.354666 15.445333-50.858667c18.986667-76.074667 7.808-94.592-38.357333-94.592h-217.685334a53.632 53.632 0 0 1-50.730666-71.125333l2.176-6.4 3.328-10.922667c10.282667-35.754667 13.226667-59.136 13.226666-108.629333 0-48.426667-26.88-72.96-57.045333-82.261334l-3.712-1.152z m-242.944 270.122667h-34.389333c-47.616 0-63.232 14.72-63.232 56.917333v283.52c0 38.016 9.941333 53.333333 33.792 59.008l1.493333 0.341333 3.754667 0.554667 5.12 0.426667 11.562667 0.256h28.586666l13.312 0.085333v-401.066667z" fill="rgb(157, 161, 168)"  p-id="7369"></path></svg>
  </botton>
  <span class="vc-likes-text">{{ comment.likes }}</span>
  <botton>
    <svg t="1744284775717" class="vc-icon2" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="8380" width="200" height="200"><path d="M300 608.864V88h477.916c25.815 0 41.979 5.525 51.808 14.617 6.238 6.125 9.602 13.574 10.735 20.38l0.438 2.633 92.314 402.165 0.176 0.712c5.816 23.53 1.843 43.53-10.447 59.143-9.517 11.702-32.017 21.182-59.61 21.182H546.349l72.213 130.586c7.856 14.206 15.912 31.605 23.947 53.053 10.618 28.344 20.148 61.09 28.115 98.645 0.036 0.32-0.053 0.518-0.461 1.612-1.324 3.544-4.218 8.523-9.47 15.814C644.654 926.839 623.467 936 594.813 936c-18.135 0-28.537-4.288-37.618-12.874-8.405-7.946-14.718-17.855-25.561-39.254l-5.634-11.118-5.344-5.732c-0.433-0.72-0.918-1.551-1.444-2.474-1.787-3.135-7.986-14.904-10.1-18.652l0.01-0.006c-25.204-43.028-36.934-62.463-52.366-85.841-21.447-32.49-42.12-59.384-64.482-82.682-28.251-29.434-58.872-52.508-92.273-68.503z m-88-24.668a289.824 289.824 0 0 0-29.43-1.476H97.667c-6.617 0-8.667-2.052-8.667-8.768V96.256C89 90.049 91.054 88 97.667 88H212v496.196z m483.57 112.636h167.76c53.193 0 101.27-20.48 128.379-54.272 29.665-37.376 39.382-85.504 27.107-135.168l-91.552-398.848c-2.557-15.36-10.74-44.544-36.826-69.632C863.331 13.312 825.482 0 777.916 0H97.667C42.429 0 1 41.472 1 96.256v477.696c0 55.296 41.429 96.768 96.667 96.768h84.903c121.729 0 184.64 107.008 250.618 219.648 1.535 2.56 12.787 25.6 19.947 33.28C471.037 958.976 504.282 1024 594.811 1024c55.239 0 101.782-20.992 135.027-60.928 17.39-23.552 34.268-52.224 27.108-89.088-7.304-34.634-15.547-64.206-23.833-89.152l-37.543-88z" fill="rgb(157, 161, 168)" p-id="8381"></path></svg>
  </botton>
  <botton class="comment-son" @click="toCommentSon(index)">回复</botton>
  </div>

  <div v-if="comment.comments > 0">
    <div class="comment-son-btn"
    v-if="!showReplies[index]" 
    @click="toggleReplies(index)">
      <span>
        共&nbsp;{{comment.comments}}&nbsp;条回复，点击查看
      </span>
    </div>
  </div>
  <div v-if="showReplies[index]">
        <div v-for="(commentSon,sonIndex) in comment.commentList" 
             :key="sonIndex" 
             class="comment-son-item">
        <el-avatar :src= "commentSon.image"
   :size="25" 
   class="vc-son-list-avatar" 
   ></el-avatar>
   <div class="vc-son-list-name">
   <span >{{ commentSon.username }}</span>
  </div>
  <div class="vc-son-list-content">
   <span v-html="processCommentContent(commentSon.content)"  ref="contentContainer"></span>
  </div>
  <div class="vc-son-img" v-if="commentSon.imageUrl != null">
    <img class="vc-son-img-item" :src="commentSon.imageUrl" alt=""></img>
   </div>

   <div class="vc-son-list-info">
   <span class="vc-son-list-time">{{ formatDate(commentSon.createdTime) }}</span>
   <botton @click="toCommentLike(commentSon)">
    <svg  v-if="commentSon.isCommentLike" :key="'active-icon1'" t="1744027672488" class="vc-son-icon1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="11973" width="200" height="200"><path d="M411.904 153.728c19.797333-63.232 54.186667-90.24 122.026667-70.656l1.706666 0.554667c19.84 6.101333 42.666667 17.706667 64.085334 37.162666 33.706667 30.72 53.76 73.301333 53.76 126.805334 0 47.786667-2.773333 77.312-10.88 110.805333l-0.256 0.938667h175.488c107.264 0 149.888 72.362667 122.922666 192.682666l-2.304 9.856-5.461333 18.005334-20.608 67.114666-9.642667 30.677334-9.173333 28.672-17.066667 51.626666-11.648 33.621334-7.210666 20.053333-9.984 26.368-6.101334 15.232c-29.525333 71.253333-90.453333 103.978667-170.112 94.592l-387.114666-28.8a587.690667 587.690667 0 0 0-7.381334-0.341333l-15.36-0.341334H218.026667l-12.501334-0.213333-9.984-0.426667-8.32-0.768-3.712-0.554666-7.125333-1.408-11.52-3.029334c-59.349333-17.621333-90.24-67.925333-90.24-139.605333v-283.52c0-90.538667 54.954667-142.208 148.565333-142.208l75.776-0.042667 5.205334-3.968a293.632 293.632 0 0 0 72.234666-88.32l6.101334-11.946666c6.101333-12.544 11.093333-25.685333 15.829333-41.002667l0.768-2.602667z m88.661333 8.064c-1.834667-0.426667-2.645333 0.170667-3.541333 2.773333l-3.882667 14.933334-10.666666 38.442666-2.56 8.533334a366.933333 366.933333 0 0 1-20.565334 53.162666 387.754667 387.754667 0 0 1-72.618666 102.442667 333.141333 333.141333 0 0 1-49.28 42.026667l5.504-3.925334v417.408l336.682666 25.344c41.898667 4.906667 65.621333-6.101333 80.213334-36.096l2.858666-6.229333 5.76-14.378667 9.514667-25.173333 6.912-19.285333 11.221333-32.469334 8.064-24.064 17.365334-53.76 19.2-61.354666 15.445333-50.858667c18.986667-76.074667 7.808-94.592-38.357333-94.592h-217.685334a53.632 53.632 0 0 1-50.730666-71.125333l2.176-6.4 3.328-10.922667c10.282667-35.754667 13.226667-59.136 13.226666-108.629333 0-48.426667-26.88-72.96-57.045333-82.261334l-3.712-1.152z m-242.944 270.122667h-34.389333c-47.616 0-63.232 14.72-63.232 56.917333v283.52c0 38.016 9.941333 53.333333 33.792 59.008l1.493333 0.341333 3.754667 0.554667 5.12 0.426667 11.562667 0.256h28.586666l13.312 0.085333v-401.066667z" fill="rgb(18, 150, 219)" p-id="7369"></path></svg>
    <svg  v-else :key="'inactive-icon1'" t="1744027672488" class="vc-son-icon1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="11973" width="200" height="200"><path d="M411.904 153.728c19.797333-63.232 54.186667-90.24 122.026667-70.656l1.706666 0.554667c19.84 6.101333 42.666667 17.706667 64.085334 37.162666 33.706667 30.72 53.76 73.301333 53.76 126.805334 0 47.786667-2.773333 77.312-10.88 110.805333l-0.256 0.938667h175.488c107.264 0 149.888 72.362667 122.922666 192.682666l-2.304 9.856-5.461333 18.005334-20.608 67.114666-9.642667 30.677334-9.173333 28.672-17.066667 51.626666-11.648 33.621334-7.210666 20.053333-9.984 26.368-6.101334 15.232c-29.525333 71.253333-90.453333 103.978667-170.112 94.592l-387.114666-28.8a587.690667 587.690667 0 0 0-7.381334-0.341333l-15.36-0.341334H218.026667l-12.501334-0.213333-9.984-0.426667-8.32-0.768-3.712-0.554666-7.125333-1.408-11.52-3.029334c-59.349333-17.621333-90.24-67.925333-90.24-139.605333v-283.52c0-90.538667 54.954667-142.208 148.565333-142.208l75.776-0.042667 5.205334-3.968a293.632 293.632 0 0 0 72.234666-88.32l6.101334-11.946666c6.101333-12.544 11.093333-25.685333 15.829333-41.002667l0.768-2.602667z m88.661333 8.064c-1.834667-0.426667-2.645333 0.170667-3.541333 2.773333l-3.882667 14.933334-10.666666 38.442666-2.56 8.533334a366.933333 366.933333 0 0 1-20.565334 53.162666 387.754667 387.754667 0 0 1-72.618666 102.442667 333.141333 333.141333 0 0 1-49.28 42.026667l5.504-3.925334v417.408l336.682666 25.344c41.898667 4.906667 65.621333-6.101333 80.213334-36.096l2.858666-6.229333 5.76-14.378667 9.514667-25.173333 6.912-19.285333 11.221333-32.469334 8.064-24.064 17.365334-53.76 19.2-61.354666 15.445333-50.858667c18.986667-76.074667 7.808-94.592-38.357333-94.592h-217.685334a53.632 53.632 0 0 1-50.730666-71.125333l2.176-6.4 3.328-10.922667c10.282667-35.754667 13.226667-59.136 13.226666-108.629333 0-48.426667-26.88-72.96-57.045333-82.261334l-3.712-1.152z m-242.944 270.122667h-34.389333c-47.616 0-63.232 14.72-63.232 56.917333v283.52c0 38.016 9.941333 53.333333 33.792 59.008l1.493333 0.341333 3.754667 0.554667 5.12 0.426667 11.562667 0.256h28.586666l13.312 0.085333v-401.066667z" fill="rgb(157, 161, 168)"  p-id="7369"></path></svg>
  </botton>
  <span class="vc-son-likes-text">{{ commentSon.likes }}</span>
  <botton>
    <svg t="1744284775717" class="vc-son-icon2" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="8380" width="200" height="200"><path d="M300 608.864V88h477.916c25.815 0 41.979 5.525 51.808 14.617 6.238 6.125 9.602 13.574 10.735 20.38l0.438 2.633 92.314 402.165 0.176 0.712c5.816 23.53 1.843 43.53-10.447 59.143-9.517 11.702-32.017 21.182-59.61 21.182H546.349l72.213 130.586c7.856 14.206 15.912 31.605 23.947 53.053 10.618 28.344 20.148 61.09 28.115 98.645 0.036 0.32-0.053 0.518-0.461 1.612-1.324 3.544-4.218 8.523-9.47 15.814C644.654 926.839 623.467 936 594.813 936c-18.135 0-28.537-4.288-37.618-12.874-8.405-7.946-14.718-17.855-25.561-39.254l-5.634-11.118-5.344-5.732c-0.433-0.72-0.918-1.551-1.444-2.474-1.787-3.135-7.986-14.904-10.1-18.652l0.01-0.006c-25.204-43.028-36.934-62.463-52.366-85.841-21.447-32.49-42.12-59.384-64.482-82.682-28.251-29.434-58.872-52.508-92.273-68.503z m-88-24.668a289.824 289.824 0 0 0-29.43-1.476H97.667c-6.617 0-8.667-2.052-8.667-8.768V96.256C89 90.049 91.054 88 97.667 88H212v496.196z m483.57 112.636h167.76c53.193 0 101.27-20.48 128.379-54.272 29.665-37.376 39.382-85.504 27.107-135.168l-91.552-398.848c-2.557-15.36-10.74-44.544-36.826-69.632C863.331 13.312 825.482 0 777.916 0H97.667C42.429 0 1 41.472 1 96.256v477.696c0 55.296 41.429 96.768 96.667 96.768h84.903c121.729 0 184.64 107.008 250.618 219.648 1.535 2.56 12.787 25.6 19.947 33.28C471.037 958.976 504.282 1024 594.811 1024c55.239 0 101.782-20.992 135.027-60.928 17.39-23.552 34.268-52.224 27.108-89.088-7.304-34.634-15.547-64.206-23.833-89.152l-37.543-88z" fill="rgb(157, 161, 168)" p-id="8381"></path></svg>
  </botton>
  </div>
  </div>
  
      </div>
      <div v-if="isToComment[index]">
        <div class="video-son-comment-input">
  <el-avatar :src="userInfo.image"
   :size="40" 
   class="vc-son-avatar" 
   ></el-avatar>
  <!-- From Uiverse.io by ercnersoy --> 
<input type="text" name="text" class="vc-son-input" 
placeholder="回复评论"
v-model="commentSonInput"
    @keydown.space.prevent="handleSpace"
    @keydown.enter="giveCurrentCommentComment(comment.id)">
 </div>
    <div class="video-son-comment-operation">
    <div class="vc-son-at" @click="AtSonDialogVisibleOn">
    <button >
      <svg t="1744279969705" class="vc-son-at-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3548" width="200" height="200"><path d="M657.476791 626.845655c-5.884011 26.478048 1.471514 47.072085 22.065552 61.782112l26.478048 0c108.854197-50.01409 161.810293-148.57178 158.868287-295.672046-20.594037-158.868287-136.803759-247.128447-348.628142-264.780479-144.158261 8.826016-248.599961 72.079642-313.324078 189.759855-32.362059 61.782112-50.01409 129.448234-52.956096 202.998367 11.768021 164.752298 85.318154 276.5485 220.650399 335.388607 155.926282 47.072085 300.084543 16.181541 432.474782-92.673679l48.543599 52.956096c-111.796202 97.086176-239.773945 142.68777-383.932206 136.803759-161.810293-8.826016-279.490505-89.731674-353.040638-242.715951-32.362059-76.492138-45.601594-157.397796-39.717583-242.715951 11.768021-152.984277 91.202165-275.078009 238.302431-366.280174 79.434144-29.420053 150.042271-42.659589 211.824383-39.717583 170.636309 2.942005 294.200532 67.666122 370.69267 194.172351 29.420053 44.13008 42.659589 110.325711 39.717583 198.585871-20.594037 164.752298-97.086176 269.193998-229.476415 313.324078l-52.956096 0c-47.072085-14.710027-70.608128-41.188074-70.608128-79.434144l0-4.41352c-52.956096 55.898101-113.267716 80.905658-180.933839 75.021647-58.840106-8.826016-100.028181-38.981826-123.564223-90.466408s-25.007557-117.680213-4.41352-198.585871 66.195631-147.836023 136.803759-200.792119c79.434144-47.072085 155.926282-45.601594 229.476415 4.41352l26.478048 35.304064 13.239536-48.543599 88.26016 0L657.476791 626.845655zM339.739193 560.650023c-5.884011 61.782112 13.239536 100.028181 57.369615 114.738207l57.369615 4.41352c111.796202-47.072085 172.107823-147.100266 180.933839-300.084543-8.826016-44.13008-44.13008-67.666122-105.912192-70.608128C426.528861 329.703117 363.275235 413.549757 339.739193 560.650023z" p-id="3549"></path></svg>
    </button>
  </div>
    <el-upload
                class="vc-son-pic-ul"
                action="/api/upload"
                :headers="{'token':token}"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                >
    <botton class="vc-son-pic">
      <svg t="1744280525370" class="vc-son-pic-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6352" width="200" height="200"><path d="M937.472 102.4H91.136c-46.08 0-83.456 37.376-83.456 83.456V841.728c0 46.08 37.376 83.456 83.456 83.456h846.336c46.08 0 83.456-37.376 83.456-83.456V185.856c0-46.08-37.376-83.456-83.456-83.456zM91.136 163.84h846.336c12.288 0 22.016 9.728 22.016 22.016v437.248L762.88 448.512c-10.752-9.728-27.136-10.24-38.912-1.536l-244.224 183.296-129.536-92.16c-10.24-7.68-24.576-7.68-35.328 0l-245.76 174.08V185.856c0-12.288 9.728-22.016 22.016-22.016z m846.336 699.904H91.136c-12.288 0-22.016-9.728-22.016-22.016v-54.272L332.8 601.088l247.808 176.64c13.824 9.728 33.28 6.656 43.008-7.168 9.728-13.824 6.656-33.28-7.168-43.008l-84.48-59.904 208.896-156.672 211.456 187.904c2.048 2.048 4.608 3.072 7.168 4.608v138.24c0 12.288-9.728 22.016-22.016 22.016z" fill="#646464" p-id="6353"></path><path d="M312.32 471.04c70.656 0 128-57.344 128-128S382.976 215.04 312.32 215.04 184.32 272.384 184.32 343.04s57.344 128 128 128z m0-194.56c36.864 0 66.56 29.696 66.56 66.56s-29.696 66.56-66.56 66.56-66.56-29.696-66.56-66.56 29.696-66.56 66.56-66.56z" fill="#646464" p-id="6354"></path></svg>
    </botton>
  </el-upload>
    <el-button type="primary" class="vc-son-publish"
     :disabled="commentSonInput === ''"
     @click="giveCurrentCommentComment(comment.id)"
     >回复</el-button>
  </div>
  </div>
    <el-divider class="vc-divider2"/>
    </div>

  </div>
  <div class="no-more-comment">
  <span>————没有更多评论————</span>
  </div>
 </div>

<!-- 作者信息 -->
 <div class="author-info">
  <el-avatar :src= "authorInfo.image"
   :size="48" 
   class="author-avatar" 
   @click="toUserInfo(authorInfo.id)"
   ></el-avatar>
   <div class="nameAndChat">
   <span class="author-name" @click="toUserInfo(authorInfo.id)">{{ authorInfo.username }}</span>
   <svg t="1743945644153" class="video-icon3" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2591" width="200" height="200"><path d="M887.296 134.144H136.704c-47.104 0-85.504 38.4-85.504 85.504v585.216c0 47.104 38.4 85.504 85.504 85.504h750.592c47.104 0 85.504-38.4 85.504-85.504V219.648c0-47.616-38.4-85.504-85.504-85.504z m12.8 235.52c0 4.096-2.048 7.68-5.632 9.728L517.12 572.928c-3.072 1.536-6.656 1.536-9.728 0L129.536 379.392c-3.584-2.048-5.632-5.632-5.632-9.728v-36.352c0-7.68 7.68-12.8 14.848-9.728l369.152 155.648c2.56 1.024 5.632 1.024 8.192 0l369.152-155.648c7.168-3.072 14.848 2.048 14.848 9.728v36.352z" p-id="2592"></path></svg>
   <sapn class="author-chat">
   发消息
  </sapn>
  </div>
  <div class="author-description">
    {{ authorInfo.description }}
  </div>
  <div class="follow-container">
      <div class="follow-wrapper">
        <button 
          class="follow-button"
          :class="{
            'following': isFollowing,
            'special': isSpecial
          }"
          @click="handleMainClick"
        >
          <span class="text">
            {{ isSpecial ? '已特别关注' : isFollowing ? '已关注' : '+ 关注' }}
          </span>
          <span class="count">{{ fansCount }}</span>
        </button>
  
        <transition name="dropdown">
          <div v-if="showDropdown" class="dropdown-menu">
            <div class="dropdown-item" @click="setSpecial">
              <span>⭐ 特别关注</span>
            </div>
            <div class="dropdown-item danger" @click="unfollow">
              <span>❌ 取消关注</span>
            </div>
          </div>
        </transition>
      </div>
    </div>
 </div>

 <div class="danmu-list">
  <soan class="danmu-list-text">弹幕列表</soan>
  <svg t="1744021824914" class="danmu-list-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2562" width="200" height="200"><path d="M512 298.6496a85.3504 85.3504 0 1 0 0-170.6496 85.3504 85.3504 0 0 0 0 170.6496z" fill="#5A5A68" p-id="2563"></path><path d="M512 512m-85.3504 0a85.3504 85.3504 0 1 0 170.7008 0 85.3504 85.3504 0 1 0-170.7008 0Z" fill="#5A5A68" p-id="2564"></path><path d="M512 896a85.3504 85.3504 0 1 0 0-170.7008 85.3504 85.3504 0 0 0 0 170.7008z" fill="#5A5A68" p-id="2565"></path></svg>
  <svg t="1744021930536" class="danmu-list-icon1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3527" width="200" height="200"><path d="M232.096 389.248a32 32 0 0 1 44.96-45.344l2.88 2.848L512 607.84l232.096-261.12a32 32 0 0 1 41.984-5.12l3.2 2.496a32 32 0 0 1 5.12 41.984l-2.496 3.2-256 288a32 32 0 0 1-44.96 2.816l-2.88-2.848-256-288z" fill="#979797" p-id="3528"></path></svg>
 </div>

 <!-- 广告 -->
  <div class="ad">
    <img src="YilenaPic/25/04/04-93e2337646d641ebb725719f4d7b3169.png" class="ad-img">
    <div class="ad-text">你的每一次点击，都在塑造最懂你的AI老婆</div>
    <svg t="1744022865981" class="ad-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="8202" width="200" height="200"><path d="M21.333333 341.461333v383.744c0 35.498667 28.672 64.128 64.213334 64.128h852.906666c35.498667 0 64.213333-28.714667 64.213334-64.128V341.461333c0-35.498667-28.672-64.128-64.213334-64.128H85.546667C50.048 277.333333 21.333333 306.048 21.333333 341.461333z m-21.333333 0C0 294.272 38.272 256 85.546667 256h852.906666C985.685333 256 1024 294.144 1024 341.461333v383.744A85.461333 85.461333 0 0 1 938.453333 810.666667H85.546667A85.418667 85.418667 0 0 1 0 725.205333V341.461333z" fill="#303030" p-id="8203"></path><path d="M344.704 352.085333c7.253333 13.824 14.208 29.184 21.12 46.506667h139.776v26.453333H221.013333v86.016c-1.493333 84.096-14.549333 151.296-39.509333 201.984l-22.656-20.352c21.504-43.776 33.024-104.064 33.792-181.632V398.549333h142.848a274.432 274.432 0 0 0-21.12-41.088l30.293333-5.376z m500.352 216.192v141.312h-28.032v-20.352h-194.304v20.352h-28.032V568.32h250.368z m-222.336 94.848h194.304V594.773333h-194.304v68.352z m-4.992-224.64a279.509333 279.509333 0 0 1-46.506667 49.152l-16.469333-21.888c34.56-27.648 58.368-61.824 70.656-102.144l27.648 3.072a249.856 249.856 0 0 1-18.816 45.312h75.264V357.461333h28.032v54.528h125.568v26.496h-125.568v62.208h154.752v27.306667h-347.136v-27.306667h164.352v-62.208h-91.776z"  p-id="8204"></path></svg>
    <span class="ad-text2">端脑AI&nbsp;海量模型</span>
    <svg t="1744021824914" class="ad-icon2" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2562" width="200" height="200"><path d="M512 298.6496a85.3504 85.3504 0 1 0 0-170.6496 85.3504 85.3504 0 0 0 0 170.6496z" fill="#5A5A68" p-id="2563"></path><path d="M512 512m-85.3504 0a85.3504 85.3504 0 1 0 170.7008 0 85.3504 85.3504 0 1 0-170.7008 0Z" fill="#5A5A68" p-id="2564"></path><path d="M512 896a85.3504 85.3504 0 1 0 0-170.7008 85.3504 85.3504 0 0 0 0 170.7008z" fill="#5A5A68" p-id="2565"></path></svg>
  </div>

  <!-- 视频推荐 -->
  <div class="video-div-recommend">
    <span class="video-div-recommend-text">接下来播放</span>
    <span class="video-div-recommend-text1">自动连播</span>
    <el-switch  class="video-div-recommend-switch" v-model="isContinue" />
  <div class="video-div-recommend-videos" v-for="(item,index) in leftRecommendVideos">
    <img class="video-div-recommend-img" :src="item.coverUrl"  @click="goToAnotherVideo(item.id)">
    <span class="video-div-recommend-time">{{ formatVideoDuration(item.time) }}</span>
    <span class="video-div-recommend-title">{{ item.title }}</span>
    <div class="video-div-recommend-author-div">
    <svg t="1744025410333" class="video-div-recommend-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="10955" width="200" height="200"><path d="M800 128H224C134.4 128 64 198.4 64 288v448c0 89.6 70.4 160 160 160h576c89.6 0 160-70.4 160-160V288c0-89.6-70.4-160-160-160z m96 608c0 54.4-41.6 96-96 96H224c-54.4 0-96-41.6-96-96V288c0-54.4 41.6-96 96-96h576c54.4 0 96 41.6 96 96v448z" p-id="10956"></path><path d="M419.2 544c0 51.2-3.2 108.8-83.2 108.8S252.8 595.2 252.8 544v-217.6H192v243.2c0 96 51.2 140.8 140.8 140.8 89.6 0 147.2-48 147.2-144v-240h-60.8V544zM710.4 326.4h-156.8V704h60.8v-147.2h96c102.4 0 121.6-67.2 121.6-115.2 0-44.8-19.2-115.2-121.6-115.2z m-3.2 179.2h-92.8V384h92.8c32 0 60.8 12.8 60.8 60.8 0 44.8-32 60.8-60.8 60.8z" p-id="10957"></path></svg>
    <span class="video-div-recommend-author">
      {{ item.username }}
    </span>
  </div>
  <div class="video-div-recommend-viewAndDanmu">
    <svg t="1743757350808" class="video-div-recommend-icon1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="28554" width="200" height="200"><path d="M759.528727 209.408H264.424727A53.853091 53.853091 0 0 0 209.454545 262.004364v499.898181a53.992727 53.992727 0 0 0 54.970182 52.689455h495.080728a53.829818 53.829818 0 0 0 55.016727-52.689455v-499.898181a53.690182 53.690182 0 0 0-54.993455-52.596364z m0 552.494545H264.424727V262.097455h495.080728v499.80509z" fill="#666666" p-id="28555"></path><path d="M436.386909 360.727273v302.498909L625.524364 512 436.386909 360.727273z"  p-id="28556"></path></svg>
    <span class="video-div-recommend-views">{{ formatViews(item.views) }}</span>
    <svg t="1743757905181" class="video-div-recommend-icon2" viewBox="0 0 1303 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29556" width="200" height="200"><path d="M110.964364 1023.720727c-60.509091 0-109.847273-48.872727-109.847273-108.916363V109.009455C1.117091 48.965818 50.455273 0 110.964364 0h1081.902545c60.509091 0 109.847273 48.872727 109.847273 108.916364v805.794909c0 60.043636-49.338182 108.916364-109.847273 108.916363H110.964364z m0-927.650909c-6.609455 0-12.101818 5.771636-12.101819 12.939637v805.794909c0 7.168 5.492364 13.032727 12.101819 13.032727h1081.902545c6.609455 0 12.101818-5.864727 12.101818-13.032727V109.009455c0-7.168-5.492364-13.032727-12.101818-13.032728H110.964364z" fill="#5F6269" p-id="29557"></path><path d="M1003.054545 520.098909a51.572364 51.572364 0 0 1-50.26909 52.689455h-80.058182a51.572364 51.572364 0 0 1-50.269091-52.689455c0-29.137455 22.434909-52.782545 50.269091-52.782545h80.058182c27.834182 0 50.269091 23.645091 50.26909 52.782545zM1096.610909 270.149818c0 28.858182-22.341818 52.410182-49.989818 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273h814.08c27.648 0 50.082909 23.458909 50.082909 52.503273zM768.744727 520.098909c0 28.858182-22.341818 52.410182-50.082909 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273H718.661818c27.648 0 50.082909 23.458909 50.082909 52.503273zM1096.610909 735.976727c0 29.044364-22.341818 52.503273-49.989818 52.503273H560.500364a51.293091 51.293091 0 0 1-50.082909-52.503273c0-28.951273 22.341818-52.410182 50.082909-52.410182h486.120727c27.648 0 50.082909 23.458909 50.082909 52.410182z"  p-id="29558"></path></svg>
    <span class="video-div-recommend-barrages">{{ item.barrages }}</span>
  </div>
  </div>
<el-affix :offset="70">
  <div class="more">
    <span class="more-text">展开</span>
  </div>
</el-affix>
  </div>


<!-- 投币对话框 -->
  <el-dialog
    v-model="ConisDialogVisible"
    title=""
    width="450"
    :before-close="CoinsHandleClose"
    class="CoinsDialog"
  >
    <span class="CoinsDialog-title">给up主投上
      <span class="CoinsDialog-title-coin">{{ coinsToUp }}</span>
      枚硬币
    </span>
    <div class="CoinsDialog-content">
      <img @click="selectCoin('two')" 
      :class="{ 'selected-border': selectedCoins === 'two' }" 
      class="CoinsDialog-content-img" src="YilenaPic/25/04/09-554416426b004f7ca0402fd998eaa4e3.jpg">
      <img @click="selectCoin('one')" 
      :class="{ 'selected-border': selectedCoins === 'one' }" 
      class="CoinsDialog-content-img" src="YilenaPic/25/04/09-c817dab2ecb8403ab1ad3421e11437ef.jpg">
    </div>
    <template #footer>
      <div class="CoinsDialog-footer">
        <el-button type="primary" @click="CoinsResult">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 收藏对话框 -->
  <el-dialog
    v-model="centerDialogVisible"
    title=""
    width="450"
    align-center
    class="CollectDialog"
  >
    <span class="CollectDialog-title">添加到收藏夹</span>
    <template #footer>
      <div class="CollectDialog-content">
        <ul v-infinite-scroll="CollectDialogLoad" class="CollectDialog-infinite-list" style="overflow: auto">
          <el-table 
    :data="tableData" 
    :show-header="false"
    style="width: 100%"
     class="custom-table"
     @selection-change="handleSelectionChange"
  >
    <el-table-column type="selection" width="40"  :selectable="(row, index) => selectedRows.length === 0 || selectedRows.includes(row.id)"/>
    <el-table-column property="title" label="title" width="265" />
    <el-table-column property="videoCount" label="videoCount" />
  </el-table>
        </ul>
      </div>
      <div class="CollectDialog-footer">
        <el-button type="primary" 
        :disabled="selectedRows.length === 0" 
        @click="FavoritesReslut">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>

    <!-- @对话框 -->
    <el-dialog
    v-model="AtDialogVisible"
    title=""
    width="450"
    align-center
    class="CollectDialog"
  >
    <span class="AtDialog-title">请选择需要@的对象</span>
    <template #footer>
      <div class="CollectDialog-content">
        <ul v-infinite-scroll="CollectDialogLoad" class="CollectDialog-infinite-list" style="overflow: auto">
          <el-table 
    :data="AtData" 
    :show-header="false"
    style="width: 100%"
     class="custom-table"
     @selection-change="handleAtSelectionChange"
  >
  <el-table-column type="selection" width="40"  :selectable="(row, index) => selectedRows.length === 0 || selectedRows.includes(row.id)"/>
    <el-table-column property="username" label="username" />
  </el-table>
        </ul>
      </div>
      <div class="CollectDialog-footer">
        <el-button type="primary" 
        :disabled="AtRows.length === 0"  
        @click="AtReslut(1)">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog
    v-model="AtSonDialogVisible"
    title=""
    width="450"
    align-center
    class="CollectDialog"
  >
    <span class="AtDialog-title">请选择需要@的对象</span>
    <template #footer>
      <div class="CollectDialog-content">
        <ul v-infinite-scroll="CollectDialogLoad" class="CollectDialog-infinite-list" style="overflow: auto">
          <el-table 
    :data="AtData" 
    :show-header="false"
    style="width: 100%"
     class="custom-table"
     @selection-change="handleAtSelectionChange"
  >
  <el-table-column type="selection" width="40"  :selectable="(row, index) => selectedRows.length === 0 || selectedRows.includes(row.id)"/>
    <el-table-column property="username" label="username" />
  </el-table>
        </ul>
      </div>
      <div class="CollectDialog-footer">
        <el-button type="primary" 
        :disabled="AtRows.length === 0"  
        @click="AtReslut(2)">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog
    v-model="shareDialogVisible"
    title="转发到动态"
    width="500"
    :before-close="handleClose"
  >
  <el-input
    v-model="shareInput"
    style="width: 470px"
    :rows="4"
    type="textarea"
    placeholder="说些什么吧"
  />
  <el-row><span>转发当前视频：{{ currentVideo.title }}</span></el-row>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="shareDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="sendPost">
          转发
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style>
/* 3D悬停效果 */
botton {
  perspective: 1000px;
  transition: all 0.3s ease;
}

botton:hover {
  transform: perspective(1000px) rotateY(10deg) scale(1.05);
}

botton:hover svg path {
  fill: rgb(18, 150, 219);
  transition: fill 0.3s ease;
}

/* 已激活状态的按钮悬停效果 */
botton:hover svg[v-if="isLike"] path,
botton:hover svg[v-if="isCoin"] path,
botton:hover svg[v-if="isFavorites"] path,
botton:hover svg[v-if="isShare"] path {
  fill: rgb(10, 100, 160);
}

.header-center,.header-right,.header-left1{
  height: 70px;
}

.header-left1{
 width: 600px;
 position: absolute;
 left: 170px;
}

.button-container1 {
  display: flex;
  align-items: center;
}
/* From Uiverse.io by mobinkakei */ 
.btn-left-11 {
  width: 150px;
  height: 50px;
  background: transparent;
  align-items: center; /* 垂直居中对齐 */
  justify-content: center; /* 水平居中对齐 */
  color: rgb(249,249,249);
  border-radius: 50px;
  border: none;
  outline: none;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  margin-top: 10px;
  padding: 0 10px; /* 添加内边距 */
}

.btn-left-11 span {
  font-size: 15px;
  text-transform: uppercase;
  letter-spacing: 1px;
  transition: top 0.5s;
}

/* From Uiverse.io by MuhammadHasann */ 
.btn-left1{
  margin-top: 9px;
  color: rgb(50, 51, 54) ;
  font-size: 15px !important;
  position: relative;
  background: transparent;
  font-size: 17px;
  font-weight: 500;
  border: none;
  transition: all 0.3s ease-in-out;
  cursor: pointer;
  margin-right: 1px;
}

.video-top1{
  height: 70px;
}

.star-1 {
  position: absolute;
  top: 20%;
  left: 20%;
  width: 25px;
  height: auto;
  filter: drop-shadow(0 0 0 #fffdef);
  z-index: -5;
  transition: all 1s cubic-bezier(0.05, 0.83, 0.43, 0.96);
}

.star-2 {
  position: absolute;
  top: 45%;
  left: 45%;
  width: 15px;
  height: auto;
  filter: drop-shadow(0 0 0 #fffdef);
  z-index: -5;
  transition: all 1s cubic-bezier(0, 0.4, 0, 1.01);
}

.star-3 {
  position: absolute;
  top: 40%;
  left: 40%;
  width: 5px;
  height: auto;
  filter: drop-shadow(0 0 0 #fffdef);
  z-index: -5;
  transition: all 1s cubic-bezier(0, 0.4, 0, 1.01);
}

.star-4 {
  position: absolute;
  top: 20%;
  left: 40%;
  width: 8px;
  height: auto;
  filter: drop-shadow(0 0 0 #fffdef);
  z-index: -5;
  transition: all 0.8s cubic-bezier(0, 0.4, 0, 1.01);
}

.star-5 {
  position: absolute;
  top: 25%;
  left: 45%;
  width: 15px;
  height: auto;
  filter: drop-shadow(0 0 0 #fffdef);
  z-index: -5;
  transition: all 0.6s cubic-bezier(0, 0.4, 0, 1.01);
}

.star-6 {
  position: absolute;
  top: 5%;
  left: 50%;
  width: 5px;
  height: auto;
  filter: drop-shadow(0 0 0 #fffdef);
  z-index: -5;
  transition: all 0.8s ease;
}

.btn-left1:hover {
  background: transparent;
  color: #ffc710;
}

.btn-left1:hover .star-1 {
  position: absolute;
  top: -100%;
  left: -60%;
  width: 25px;
  height: auto;
  filter: drop-shadow(0 0 10px #fffdef);
  z-index: 2;
}

.btn-left1:hover .star-2 {
  position: absolute;
  top: 100%;
  left: -10%;
  width: 15px;
  height: auto;
  filter: drop-shadow(0 0 10px #fffdef);
  z-index: 2;
}

.btn-left1:hover .star-3 {
  position: absolute;
  top: -60%;
  left: 25%;
  width: 5px;
  height: auto;
  filter: drop-shadow(0 0 10px #fffdef);
  z-index: 2;
}

.btn-left1:hover .star-4 {
  position: absolute;
  top: -50%;
  left: 100%;
  width: 8px;
  height: auto;
  filter: drop-shadow(0 0 10px #fffdef);
  z-index: 2;
}

.btn-left1:hover .star-5 {
  position: absolute;
  top: 25%;
  left: 115%;
  width: 15px;
  height: auto;
  filter: drop-shadow(0 0 10px #fffdef);
  z-index: 2;
}

.btn-left1:hover .star-6 {
  position: absolute;
  top: 80%;
  left: 80%;
  width: 5px;
  height: auto;
  filter: drop-shadow(0 0 10px #fffdef);
  z-index: 2;
}

.fil0 {
  fill: #fffdef;
}

.btn-text1-one1 {
  position: absolute;
  width: 100%;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
}

.btn-text-two1 {
  position: absolute;
  width: 100%;
  top: 150%;
  left: 0;
  transform: translateY(-50%);
}

.btn-left-11:hover .btn-text-one1 {
  top: -100%;
}

.btn-left-11:hover .btn-text-two1 {
  top: 50%;
}

.btn-text-one1 {
  position: absolute;
  width: 100%;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
}

.btn-text-two1 {
  position: absolute;
  width: 100%;
  top: 150%;
  left: 0;
  transform: translateY(-50%);
}

.btn-left-11:hover .btn-text-one1 {
  top: -100%;
}

.btn-left-11:hover .btn-text-two1 {
  top: 50%;
}

.btn-text1 {
  color: rgb(50, 51, 54);
  font-size: 15px;
  font-weight: bolder;
  text-align: center; /* 文字居中 */
  display: inline-block; /* 将文字和图标都显示为行内元素 */
  vertical-align: middle;
}

.btn-text21{
  color: rgb(50, 51, 54);
  font-size: 15px;
  font-weight: bolder;
}

/* From Uiverse.io by vinodjangid07 */ 
.bookmarkBtn1 {
  margin-top: -52px;
  margin-left: 440px;
  width: 150px;
  height: 40px;
  background-color: transparent;
  border: none;
  display: flex;
  cursor: pointer;
  transition-duration: 0.3s;
  overflow: hidden;
}

.IconContainer1 {
  margin-left: 8px;
  margin-right: -12px;
  margin-top: 8px;
  width: 40px;
  height: 30px;
  background: transparent;
  border-radius: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  z-index: 2;
  transition-duration: 0.3s;
}

.icon {
  border-radius: 1px;
}

.text1 {
  height: 100%;
  width: 80px;
  color: rgb(50, 51, 54);
  font-size: 15px;
}

.bookmarkBtn:hover .IconContainer {
  width: 90px;
  transition-duration: 0.3s;
}

.bookmarkBtn:hover .text {
  transform: translate(10px);
  width: 0;
  font-size: 0;
  transition-duration: 0.3s;
}

.bookmarkBtn:active {
  transform: scale(0.95);
  transition-duration: 0.3s;
}

.bli-1{
  width: 23px;
  height: 23px;
  margin-right: 5px; /* 图标与文字之间的间距 */
  display: inline-block; /* 将图标和文字都显示为行内元素 */
  vertical-align: middle;
  fill:rgb(50, 51, 54) !important;
}

.bli-21{
  width: 15px;
  height: 15px;
  margin-right: 5px; /* 图标与文字之间的间距 */
  display: inline-block; /* 将图标和文字都显示为行内元素 */
  vertical-align: middle;
  fill: rgb(50, 51, 54);
}

/* From Uiverse.io by joe-watson-sbf */ 
.search1 {
  display: flex;
  align-items: center;
  justify-content: space-between;
  text-align: center;
  margin-top: 14px;
  margin-left: 50px;
  margin-right: -90px;
}

.search__input {
  font-family: inherit;
  font-size: 15px;
  background-color: rgb(223,231,232);
  border: none;
  color: rgb(106, 117, 123);
  padding: 0.7rem 1rem;
  border-radius: 10px;
  height: 18px;
  width: 800px;
  transition: all ease-in-out .5s;
  margin-right: -2rem;
}

.search__input:hover, .search__input:focus {
  box-shadow: 0 0 1em #00000013;
}

.search__input:focus {
  outline: none;
  background-color: rgb(227, 219, 231);
}

.search__input::-webkit-input-placeholder {
  font-weight: 100;
  color: rgb(106, 117, 123);
}

.search__input:focus + .search__button {
  background-color: #f0eeee;
}

.search__button {
  border: none;
  background-color: transparent !important;
  margin-top: .1em;
}

.search__button:hover {
  cursor: pointer;
}

.search__icon {
  height: 1.3em;
  width: 1.3em;
  fill: #000000;
  background-color: transparent !important;
}

.avatar {
  display: inline-block;  /* 改为块级格式化上下文 */
  vertical-align: middle; /* 关键：垂直对齐方式 */
  margin-top: 15px;
  margin-left: -25px;
  width: 40px;
  height: 40px;
}

.avatar:hover{
  transform: scale(1.9);
}
.custom-dropdown-menu {
  position: relative;
  width: 300px;
  height: 450px;
  border-radius: 8px; /* 圆角 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); /* 阴影效果 */
}

.coins{
  color: rgb(148, 166, 193);
  position: absolute; 
  top: 50px;
  left: 50%;
  transform: translate(-50%, -50%);
}

.username{
  color: #090404;
  font-size: x-large;
  font-weight: 400;
  position: absolute; 
  top: 20px;
  left: 50%;
  transform: translate(-50%, -50%);
}

/* 父容器布局 */
.userInfo-item {
  display: flex;
  justify-content: center;
  align-items: center;
}

.el-statistic__title {
  text-align: center;
  font-size: 14px;
  color: rgb(148, 166, 193);
}

.el-statistic__content {
  text-align: center;
  font-size: 24px;
  color: #303133;
}

.userInfo{
  margin-top: 80px; 
}

.coins-num{
  color: #070707;
}

/* From Uiverse.io by alexmaracinaru */ 
.cta-1 {
  position: relative;
  border: none;
  background: none;
  cursor: pointer;
  margin-top: 35px;
  margin-left: 30px;
}

.cta-1 span {
  padding-bottom: 7px;
  letter-spacing: 1px;
  font-size: 14px;
  padding-right: 15px;
  text-transform: uppercase;
}

.cta-1 svg {
  transform: translateX(-8px);
  transition: all 0.3s ease;
}

.cta-1:hover svg {
  transform: translateX(0);
}

.cta-1:active svg {
  transform: scale(0.9);
}

.hover-underline-animation {
  position: relative;
  color: black;
  padding-bottom: 20px;
}

.hover-underline-animation:after {
  content: "";
  position: absolute;
  width: 100%;
  transform: scaleX(0);
  height: 2px;
  bottom: 0;
  left: 0;
  background-color: #000000;
  transform-origin: bottom right;
  transition: transform 0.25s ease-out;
}

.cta-1:hover .hover-underline-animation:after {
  transform: scaleX(1);
  transform-origin: bottom left;
}

.user-icon{
  width: 20px;
  height: 20px;
}

.cta-4 {
  position: relative;
  border: none;
  background: none;
  cursor: pointer;
  margin-left: 270px;
}

.cta-4 span {
  padding-bottom: 7px;
  letter-spacing: 1px;
  font-size: 12px;
  padding-right: 15px;
  text-transform: uppercase;
}

.cta-4 svg {
  transform: translateX(-8px);
  transition: all 0.3s ease;
}

.cta-4:hover svg {
  transform: translateX(0);
}

.cta-4:active svg {
  transform: scale(0.9);
}

.cta-3 {
  position: relative;
  border: none;
  background: none;
  cursor: pointer;
  margin-top: 35px;
  margin-left: 30px;
}

.cta-3 span {
  padding-bottom: 7px;
  letter-spacing: 1px;
  font-size: 14px;
  padding-right: 15px;
  text-transform: uppercase;
}

.cta-3 svg {
  transform: translateX(-8px);
  transition: all 0.3s ease;
}

.cta-3:hover svg {
  transform: translateX(0);
}

.cta-3:active svg {
  transform: scale(0.9);
}

.cta-3:hover .hover-underline-animation:after {
  transform: scaleX(1);
  transform-origin: bottom left;
}

/* From Uiverse.io by alexmaracinaru */ 
.cta-2 {
  position: relative;
  border: none;
  background: none;
  cursor: pointer;
  margin-top: 35px;
  margin-left: 30px;
}

.cta-2 span {
  padding-bottom: 7px;
  letter-spacing: 1px;
  font-size: 14px;
  padding-right: 15px;
  text-transform: uppercase;
}

.cta-2 svg {
  transform: translateX(-8px);
  transition: all 0.3s ease;
}

.cta-2:hover svg {
  transform: translateX(0);
}

.cta-2:active svg {
  transform: scale(0.9);
}

.cta-2:hover .hover-underline-animation:after {
  transform: scaleX(1);
  transform-origin: bottom left;
}

.divider-menu{
  width: 350px;
  height: 500px;
}

.divider-text{
  font-size: 12px;
  color: rgb(148, 166, 193);
}

.logout-icon{
  position: relative;
  top:4px;
  width: 20px;
  height: 20px;
}

/* From Uiverse.io by joe-watson-sbf */ 
.logout{
  margin-left: 70px;
  font-size: 17px;
  background: transparent;
  border: none;
  padding: 1em 1.5em;
  color: #000000cf;
  text-transform: uppercase;
  position: absolute;
  transition: 0.5s ease;
  cursor: pointer;
}

.logout::before {
  content: "";
  position: absolute;
  left: 0;
  bottom: 0;
  height: 2px;
  width: 0;
  background-color: #ff1b06fa;
  transition: 0.5s ease;
}

.logout:hover {
  color: #ffffff;
  transition-delay: 0.5s;
}

.logout:hover::before {
  width: 100%;
}

.logout::after {
  content: "";
  position: absolute;
  left: 0;
  bottom: 0;
  height: 0;
  width: 100%;
  background-color: #ff4406;
  transition: 0.4s ease;
  z-index: -1;
}

.logout:hover::after {
  height: 100%;
  transition-delay: 0.4s;
  color: aliceblue;
}

.vip-button1 {
  margin-top: -50px;
  margin-left: 7px;
  width: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: transparent;
  border: none;
  cursor: pointer;
  padding: 10px;
  transition: transform 0.3s ease;
}

.vip-button1:hover {
  transform: scale(1.2);
}

.vip-icon1 {
  width: 20px;
  height: 20px;
  margin-bottom: 5px;
  fill:rgb(50, 51, 54)!important;
}

.vip-text1 {
  font-size: 12px;
  color: rgb(50, 51, 54);
  font-weight: bold;
  text-align: center;
}

/* From Uiverse.io by shah1345 */ 
.button-upload{
  top: -50px !important;
  margin-left: 340px;
  width: 100px;
  height: 35px;
  display: inline-block;
  transition: all 0.2s ease-in;
  position: relative;
  overflow: hidden;
  z-index: 1;
  color: rgba(249, 249, 249, 0.97);
  padding: 0.7em 1.7em;
  cursor: pointer;
  font-size: 14px;
  border-radius: 0.5em;
  font-weight: 700;
  background: rgb(251, 114, 153);
  border: none;
}

.button-upload:active {
  color: #666;
  box-shadow: inset 4px 4px 12px #c5c5c5, inset -4px -4px 12px #ffffff;
}

.button-upload:before {
  content: "";
  position: absolute;
  left: 50%;
  transform: translateX(-50%) scaleY(1) scaleX(1.25);
  top: 100%;
  width: 140%;
  height: 180%;
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: 50%;
  display: block;
  transition: all 0.5s 0.1s cubic-bezier(0.55, 0, 0.1, 1);
  z-index: -1;
}

.button-upload:after {
  content: "";
  position: absolute;
  left: 55%;
  transform: translateX(-50%) scaleY(1) scaleX(1.45);
  top: 180%;
  width: 160%;
  height: 190%;
  background-color: #009087;
  border-radius: 50%;
  display: block;
  transition: all 0.5s 0.1s cubic-bezier(0.55, 0, 0.1, 1);
  z-index: -1;
}

.button-upload:hover {
  color: #ffffff;
  border: 1px solid #009087;
}

.button-upload:hover:before {
  top: -35%;
  background-color: #009087;
  transform: translateX(-50%) scaleY(1.3) scaleX(0.8);
}

.button-upload:hover:after {
  top: -45%;
  background-color: #009087;
  transform: translateX(-50%) scaleY(1.3) scaleX(0.8);
}
/* 新增背景层 */
.background-layer1 {
  position: absolute;
  z-index: -1; /* 确保在内容层下方 */
  top: 0;
  left: 0;
  width: 100%;
  height: 70px;
  background-color: #ffffff;
  background-size: cover;
  background-position: center;
}

body {
  margin: 0;
  padding: 0;
}

.header1 {
  height: 150px;
}

.top-logo{
  position: absolute;
  width: 100px;
  height: 50px;
  top: 9px;
  left: 20px;
}

.top-divider{
  width: 100%;
  height: 10px;
  position: absolute;
  top: -105px;
}

.video-player{
  position: absolute;
  top:170px;
  left: 163px;
  width: 1670px;
  height: 1050px;
  z-index: -1;
}


.video-top{
  height: 100px;
  width: 918px;
  position: absolute;
  top: 70px;
  left: 164px;
}

.video-title{
  font-size: 22px;
  font-weight: 500;
  position: absolute;
  top:15px
}

.video-meta{
  display: flex;
  position: absolute;
  top: 55px;
}

.video-icon1{
  margin-top: 1.8px;
  width: 20px;
  height: 20px;
  margin-right: 2px;
}

.video-icon2{
  width: 15px;
  height: 15px;
  margin-top: 4px;
  margin-left: 10px;
  margin-right: 6px;
}

.video-date{
  margin-left: 10px;
}

.video-prohibit{
  margin-top: -1px;
  margin-left: 15px;
}

.author-info{
  height: 100px;
  width: 440px;
  position: absolute;
  top: 70px;
  left: 1100px;
}

.author-avatar{
  position: absolute;	
  top: 25px;
  left: 20px;
}

.author-name{
  font-size: 17px;
  margin-left: 80px;
  margin-top: 12px;
  color: rgb(251, 114, 153);
}

.nameAndChat{
  display: flex;
}

.author-chat{
  margin-top: 14px;
  margin-left: 5px;
  font-size: 13px;
  color: rgb(97, 102, 109);
}

.video-icon3{
  width: 15px;
  height: 15px;  
  margin-top: 16px;
  margin-left: 15px;
  fill:rgb(97, 102, 109);
}

.author-description{
  margin-left: 80px;
  font-size: 14px;
  color: rgb(158, 153, 160);
}

.follow-container {
    display: inline-block;
    position: relative;
  }
  
  .follow-wrapper {
    position: relative;
  }
  
  .follow-button {
    display: flex;
    align-items: center;
    padding: 8px 16px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    background: rgb(18, 150, 219);
    color: white;
    position: relative;
    overflow: hidden;
    width: 200px;
    height: 30px;
    margin-left: 80px;
    margin-top: 5px;
  
    .text {
      font-weight: 500;
      margin-right: 8px;
      margin-left: 35px;
      margin-top: -5px;
    }
  
    .count {
    margin-top: 5px;
    margin-left: -5px;
      font-size: 0.9em;
      opacity: 0.8;
    }
  
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 3px 8px rgba(0, 132, 255, 0.3);
    }
  
    &.following {
      background: #666;
      &:hover {
        box-shadow: 0 3px 8px rgba(102, 102, 102, 0.3);
      }
    }
  
    &.special {
      background: linear-gradient(45deg, #ff9500, #ff6b6b);
    }
  }
  
  .dropdown-menu {
    position: absolute;
    top: 110%;
    left: 110px;
    width: 140px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    overflow: hidden;
    z-index: 10;
  }
  
  .dropdown-item {
    padding: 10px 16px;
    cursor: pointer;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
  
    &:hover {
      background: #f5f5f5;
    }
  
    &.danger {
      color: #ff4444;
    }
  
    span {
      font-size: 0.95em;
    }
  }
  
  .dropdown-enter-active,
  .dropdown-leave-active {
    transition: all 0.25s ease;
    transform-origin: top;
  }
  
  .dropdown-enter-from,
  .dropdown-leave-to {
    opacity: 0;
    transform: translateY(-10px);
  }

.video-barrages-container {
  height: 58px;
  width: 918px;
  position: absolute;
  left: 163px;
  top: 695px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.barrages-message{
  position: absolute;
  top: 18px;
  left: 13px;
  font-size: 14px;
  color: rgb(97, 102, 109);
}

.icon-button {
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  margin-top: 15px;
  margin-left: 300px;
}

.icon {
  width: 25px;  /* 统一图标尺寸 */
  height: 25px;
  transition: all 0.3s ease;  /* 添加过渡动画 */
  fill: rgb(18, 150, 219);  /* 继承父级颜色 */
}

.icon:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
}

.icon-1 {
  width: 25px;  /* 统一图标尺寸 */
  height: 25px;
  transition: all 0.3s ease;  /* 添加过渡动画 */
  fill: currentColor;  /* 继承父级颜色 */
}

.icon-1:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
}

/* From Uiverse.io by mpody11 */ 
.messageBox {
  width: fit-content;
  height: 40px;
  width: 530px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgb(241, 242, 243);
  padding: 0 15px;
  border-radius: 10px;
  margin-left: 350px;
  margin-top: -35px;
}

.fileUploadWrapper {
  width: fit-content;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: Arial, Helvetica, sans-serif;
}

#file {
  display: none;
}
.fileUploadWrapper label {
  cursor: pointer;
  width: fit-content;
  height: fit-content;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.fileUploadWrapper label svg {
  height: 18px;
}
.fileUploadWrapper label svg path {
  transition: all 0.3s;
}
.fileUploadWrapper label svg circle {
  transition: all 0.3s;
}
.fileUploadWrapper label:hover svg path {
  stroke: #fff;
}
.fileUploadWrapper label:hover svg circle {
  stroke: #fff;
  fill: #3c3c3c;
}
.fileUploadWrapper label:hover .tooltip {
  display: block;
  opacity: 1;
}
.tooltip {
  position: absolute;
  top: -40px;
  display: none;
  opacity: 0;
  color: white;
  font-size: 10px;
  text-wrap: nowrap;
  background-color: rgb(241, 242, 243);
  padding: 6px 10px;
  border: 1px solid #3c3c3c;
  border-radius: 5px;
  box-shadow: 0px 5px 10px rgba(0, 0, 0, 0.596);
  transition: all 0.3s;
}
#messageInput {
  width: 430px;
  height: 100%;
  background-color: transparent;
  outline: none;
  border: none;
  padding-left: 10px;
  color: rgb(117, 117, 117);
  margin-left: -20px;
}
#messageInput:focus ~ #sendButton svg path,
#messageInput:valid ~ #sendButton svg path {
  fill: #f5f5f5;
  stroke: white;
}

#sendButton {
  width: fit-content;
  height: 40px;
  width: 51px;
  background-color: rgb(18, 150, 219);
  outline: none;
  border-radius: 10px;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  position: absolute;
  left: 860px;
  fill: #f5f5f5 !important;
}
#sendButton svg {
  height: 18px;
  transition: all 0.3s;
}
#sendButton svg path {
  transition: all 0.3s;
}
#sendButton:hover svg path {
  fill: #3682f4;
  stroke: white;
}

#sendButton:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.baberrage{
  width: 600px;
  position: absolute;
  top: 170px;
  left: 163px;
}

.danmu-list{
  position: absolute;
  top:170px;
  left: 1110px;
  height: 45px;
  width: 413px;
  background-color: rgb(241, 242, 243);
  border-radius: 5px;
}

.danmu-list-text{
  position: absolute;
  top:11px;
  left: 20px;
  font-size: 15px;
}

.danmu-list-icon{
  width: 20px;
  height: 20px;
  position: absolute;
  top: 12px;
  left: 90px;
}

.danmu-list-icon1{
  width: 20px;
  height: 20px;
  position: absolute;
  top: 12px;
  left: 380px;
}

.ad-img{
  width: 180px;
  height: 105px;
  border-radius: 10px;
  position: absolute;
  top: 237px;
  left: 1110px;
}

.ad-text{
  position: absolute;
  top: 237px;
  left: 1295px;
  font-size: 16px;
  width: 237px;
}

.ad-icon{
  width: 28px;
  height: 28px;
  position: absolute;
  top: 280px;
  left: 1295px;
  fill:rgb(148, 153, 181)
}

.ad-text2{
  position: absolute;
  top: 285px;
  left: 1330px;
  font-size: 13px;
  color: rgb(148, 153, 181);
}

.ad-icon2{
  position: absolute;
  top: 322px;
  left: 1505px;
  width: 20px;
  height: 20px;
}

.video-div-recommend{
  width: 410px;
  height: 2100px;
  position: absolute;
  top: 370px;
  left:1110px
}

.video-div-recommend-text{
  position: absolute;
  top: 5px;
}

.video-div-recommend-text1{
  position: absolute;
  top: 5px;
  left: 315px;	
  font-size: 13px;
  color: rgb(148, 153, 181);
}

/* From Uiverse.io by Yaya12085 */ 
.switch {
  position: relative;
  left: 355px;
  top: -2px;
  height: 1.5rem;
  width: 3rem;
  cursor: pointer;
  appearance: none;
  -webkit-appearance: none;
  border-radius: 9999px;
  background-color: rgba(100, 116, 139, 0.377);
  transition: all .3s ease;
}

.switch:checked {
  background-color: rgb(18, 150, 219);
}

.switch::before {
  position: absolute;
  content: "";
  left: calc(1.5rem - 1.6rem);
  top: calc(1.5rem - 1.6rem);
  display: block;
  height: 1.6rem;
  width: 1.6rem;
  cursor: pointer;
  border: 1px solid rgba(100, 116, 139, 0.527);
  border-radius: 9999px;
  background-color: rgba(255, 255, 255, 1);
  box-shadow: 0 3px 10px rgba(100, 116, 139, 0.327);
  transition: all .3s ease;
}

.switch:hover::before {
  box-shadow: 0 0 0px 8px rgba(0, 0, 0, .15)
}

.switch:checked:hover::before {
  box-shadow: 0 0 0px 8px rgba(236, 72, 153, .15)
}

.switch:checked:before {
  transform: translateX(100%);
}

.video-div-recommend-videos{
  margin-top: 15px;
  height: 110px;
}

.video-div-recommend-img{
  width: 188px;
  height: 110px;
  border-radius: 10px;
  z-index: -1;
}

.video-div-recommend-title{
  position: absolute;
  left: 195px;
  height: 45px;
  width: 215px;
}

.video-div-recommend-icon{
  left: 195px;
  margin-top: -50px !important;
  width: 15px;
  height: 15px;
  fill: rgb(148, 153, 181);
}

.video-div-recommend-author{
  margin-top: -53px;
  margin-left: 5px;
  font-size: 14px;
  color: rgb(148, 153, 181);
}

.video-div-recommend-author-div{
  display: flex;
  margin-top: 0px;
  margin-left: 197px;
}

.video-div-recommend-viewAndDanmu{
  display: flex;
  margin-top: -30px;
  margin-left: 194px;
}

.video-div-recommend-icon1{
  width: 20px;
  height: 20px;
  fill: rgb(148, 153, 160);
  margin-right: 5px;
  margin-top: 0px;
}

.video-div-recommend-icon2{
  width: 15px;
  height: 15px;
  fill: rgb(148, 153, 160);
  margin-right: 5px;
  margin-top: 2px;
}

.video-div-recommend-views{
  font-size: 14px;
  margin-right: 8px;
  color: rgb(148, 153, 160);
}

.video-div-recommend-barrages{
  font-size: 14px;
  color: rgb(148, 153, 160);
}

.more{
  margin-top: 15px;
  background-color: rgb(241, 242, 243);
  height: 37px;
  border-radius: 5px;
}

.more-text{
  font-size: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.video-div-recommend-time{
  position: absolute;
  margin-top: 87px;
  margin-left: -40px;
  background-color: rgba(96, 96, 97, 0.564);
  border-radius: 5px;
  font-size: 14px;
  color: rgb(241, 240, 242);
}

.video-do{
  width: 919px;
  position: absolute;
  height: 68px;
  top: 750px;
  left: 163px;
  display: flex;
}

.video-do-icon1{
  width: 40px;
  height: 40px;
  position: absolute;
  top: 14px;
  fill:rgb(97, 102, 109);
}

.video-do-likes-text{
  position: absolute;
  top: 28px;
  left: 50px;
  font-size: 14px;
  color: rgb(97, 102, 109);
}

.video-do-icon2{
  width: 35px;
  height: 35px;
  position: absolute;
  top: 19px;
  left: 120px;
  fill:rgb(97, 102, 109);
}

.video-do-coins-text{
  position: absolute;
  top: 28px;
  left: 170px;
  font-size: 14px;
  color: rgb(97, 102, 109);
}

.video-do-icon3{
  width: 40px;
  height: 40px;
  position: absolute;
  top: 16px;
  left: 240px;
  fill:rgb(97, 102, 109);
}

.video-do-favorites-text{
  position: absolute;
  top: 28px;
  left: 290px;
  font-size: 14px;
  color: rgb(97, 102, 109);
}

.video-do-icon4{
  width: 35px;
  height: 35px;
  position: absolute;
  top: 19px;
  left: 360px;
  fill:rgb(97, 102, 109);
}

.video-do-zf-text{
  position: absolute;
  top: 28px;
  left: 410px;
  font-size: 14px;
  color: rgb(97, 102, 109);
}

.video-do-icon5{
  width: 20px;
  height: 20px;
  position: absolute;
  top: 27px;
  left: 800px;
  fill:rgb(97, 102, 109);
}

.video-do-note-text{
  position: absolute;
  top: 28px;
  left: 830px;
  font-size: 14px;
  color: rgb(97, 102, 109);
}

.video-do-icon6{
  width: 20px;
  height: 20px;
  position: absolute;
  top: 27px;
  left: 895px;
  fill:rgb(97, 102, 109);
}

.vd-divider{
  height: 30px;
  top: 43px;
}

.CoinsDialog{
  position: absolute;
  top: 50px;
  height: 440px;
}

.CoinsDialog-footer{
  position: absolute;
  top: 380px;
  left: 205px;
}

.CoinsDialog-title{
  font-size: 15px;
  color: rgb(0, 0, 0);
  position: absolute;
  top: 20px;
  left: 150px;
}

.CoinsDialog-title-coin{
  color: rgb(18, 150, 219);
  font-size: 26px;
}

.CoinsDialog-content{
  margin-top: 30px;
  width: 410px;
  height: 300px;
}

.CoinsDialog-content-img{
  margin-top: 30px;
  margin-left: 30px;
  width: 160px;
  height: 230px;
  border: 5px dashed #ccc;
  border-radius: 10px;
}

.selected-border {
  /* 选中时的边框样式 */
  border: 5px solid #409eff;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.messageBox-icon{
  position: absolute;
  top: 14px;
  left: 364px;
  width: 30px;
  height: 30px;
  fill:rgb(97, 102, 109);
}

.CollectDialog{
  position: absolute;
  top: -50px;
  height: 420px;
}

.CollectDialog-title{
  font-size: 15px;
  color: rgb(0, 0, 0);
  position: relative;
  margin-left: 160px;
}

.AtDialog-title{
  font-size: 15px;
  color: rgb(0, 0, 0);
  position: absolute;
  top: 20px;
  left: 160px;
}

.CollectDialog-content{
  margin-top: 0px;
  height: 290px;
}

.CollectDialog-footer{
  position: absolute;
  top: 375px;
  left: 191px;	
}

.CollectDialog-infinite-list {
  height: 300px;
  padding: 0;
  margin: 0;
  list-style: none;
}
.CollectDialog-infinite-list .CollectDialog-infinite-list-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 50px;
  background: var(--el-color-primary-light-9);
  margin: 10px;
  color: var(--el-color-primary);
}
.CollectDialog-infinite-list .CollectDialog-infinite-list-item + .CollectDialog-list-item {
  margin-top: 10px;
}

/* Vue3 使用 :deep()，Vue2 使用 ::v-deep 或 /deep/ */
:deep(.custom-table) {
  border-collapse: separate !important;
  border-spacing: 0 12px !important; /* 垂直间距 */
}

/* 去除所有横线 */
:deep(.custom-table .el-table__cell) {
  border-bottom: none !important;
}

/* 去除表格底部默认横线 */
:deep(.custom-table.el-table::before) {
  height: 0 !important;
}

/* 处理固定列残留边框 */
:deep(.el-table__fixed::before) {
  width: 0 !important;
}

.video-descriptionAndTags{
  position: absolute !important;
  top: 820px !important;
  left: 163px;
  width: 919px;
  top: 0px;
  white-space: pre-wrap;  /* 新增：保留换行和空格 */
  word-break: break-word; /* 新增：允许单词内换行 */
  line-height: 1.6;
}

.lyric-text {
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  color: #666;
  padding: 12px;
  /* background: linear-gradient(
    to bottom right, 
    rgba(255,255,255,0.9), 
    rgba(245,245,245,0.9)
  ); */
}

.video-description-infinite-list {
  height: 140px;
  width: 919px;
  padding: 0;
  margin: 0;
  list-style: none;
}
.video-description-infinite-list .video-description-infinite-list-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 50px;
  background: var(--el-color-primary-light-9);
  margin: 10px;
  color: var(--el-color-primary);
}
.video-description-infinite-list .video-description-infinite-list-item + .video-description-list-item {
  margin-top: 10px;
}

.video-tags{
  margin-top: 20px;
  height: 40px;
  width: 919px;
}

.vdt-divider{
  height: 30px;
  top: -24px;
}

.video-div-recommend-switch{
  margin-left: 375px;
  margin-top: -2px;
}

.video-tag{
  color: rgb(136, 140, 145) !important;
  font-size: 14px!important;
  margin-right: 15px;
  border-radius: 20px !important;
  height: 30px!important;
}

.video-comments{
  position: absolute;
  top: 1050px;
  left: 163px;
  height: 145px;
  width: 919px;
}

.video-comment-title{
  display: flex;
}

.comment-title-text{
  font-size: 24px;
}

.comment-title-count{
  margin-top: 10px;
  margin-left: 5px;
  font-size: 13px;
  color: rgb(155, 160, 167);
}

.video-comment-hotest{
  margin-left: 40px;
  margin-top: 7px;;
}

.vc-divider{
  color: rgb(148, 153, 160) !important;
  margin-left: 15px!important;
  margin-right: 15px!important;
  margin-top: -2px!important;
}

.video-comment-newest{
  margin-top: 7px;
}

/* 增加样式优先级 */
.comment-tabs span {
  cursor: pointer;
  color: #999 !important;  /* 强制默认灰色 */
  transition: color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;  /* 确保元素可点击 */
}

.comment-tabs .tab-active {
  color: #000 !important;  /* 强制激活黑色 */
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 增加点击反馈动画 */
.comment-tabs span:active {
  transform: scale(0.96);
}

.comment-tabs{
  margin-top: 7px;
}

.vc-input{
  position: absolute;
  top: 48px;
  left: 80px;
  width: 800px;
 border: none;
 padding: 1rem;
 border-radius: 5px;
 background: #e8e8e8;
}

.vc-input:focus {
 outline-color: #e8e8e8;
 background: #e8e8e8;
 box-shadow: inset 20px 20px 60px #c5c5c5,
		inset -20px -20px 60px #ffffff;
 transition: 0.3s;
}

.vc-avatar{
 position: absolute;
 top: 50px;
 left: 20px;
 border: #409eff 2px solid;
}

.video-comment-operation{
  height: 40px;
  width: 919px;
  position: absolute;
  bottom: 0px;
}

.vc-at{
  position: absolute;
  top: 10px;
  left: 90px;
  width: 20px;
  height: 20px;
  background-color: white;
  border: rgb(241, 242, 243) solid 2px;
  border-radius: 2px;
}

.vc-at-icon{
  width: 15px;
  height: 20px;
  margin-top: 0px;
  margin-left: 3px;
  fill: rgb(117, 121, 127);
}

.vc-pic{
  position: absolute;
  top: 10px;
  left: 120px;
  width: 20px;
  height: 20px;
  background-color: white;
  border: rgb(241, 242, 243) solid 2px;
  border-radius: 2px;
}

.vc-pic-icon{
  width: 15px;
  height: 20px;
  margin-top: 0px;
  margin-left: 3px;
  fill: rgb(117, 121, 127);
}

.vc-publish{
  position: absolute;
  top: 5px;
  left: 830px;
  width: 80px;
}

.video-comment-item{
  width: 919px;
  position: relative;
  top: 130px;
  margin-top: -20px;
}

.vc-list-avatar{
  position: relative;
  top: 30px;
  left: 25px;
}

.vc-list-name{
  position: relative;
  margin-top: -15px !important;
  margin-left: 80px!important;
  font-size: 15px;
  color: rgb(131, 135, 140);
}

.vc-list-content{
  position: relative;
  margin-top: 15px;
  margin-left: 80px;
}

.vc-list-info{
  position: relative;
  margin-left: 80px;
  margin-top: 3px;
  font-size: 13px;
  color: rgb(157, 161, 168);
}

.vc-icon1{
  width: 17px;
  height: 17px;
  position: absolute;
  top: 2px;
  left: 130px;
}

.vc-likes-text{
  position: absolute;
  left: 150px;
  color: rgb(157, 161, 168);
}

.vc-divider2{
  width: 840px !important;
  position: relative;
  margin-top: 5px!important;
  margin-left: 80px!important;
}

.vc-icon2{
  width: 17px;
  height: 17px;
  position: absolute;
  top: 2px;
  left: 190px;
}

.comment-son{
  position: absolute;
  top: 0px;
  left: 230px;
  color: rgb(157, 161, 168);
}

.comment-son-btn{
  position: relative;
  bottom: 0px;
  font-size: 13px;
  left: 80px;
  color: rgb(157, 161, 168);
}

.comment-son-item{
  margin-left: 80px;
  margin-top: 10px;
}

.vc-son-list-avatar{
  position: relative;
  top: 10px;
  left: 0px;
}

.vc-son-list-name{
  display: flex;
  position: relative;
  margin-top: -20px !important;
  margin-left: 35px!important;
  font-size: 14px;
  color: rgb(131, 135, 140);
}

.vc-son-list-content{
  position: relative;
  margin-top: 10px;
  margin-left: 35px;
  font-size: 14px;
}

.vc-son-list-info{
  position: relative;
  margin-left: 35px;
  margin-top: 5px;
  font-size: 13px;
  color: rgb(157, 161, 168);
}

.vc-son-icon1{
  width: 17px;
  height: 17px;
  position: absolute;
  top: 2px;
  left: 130px;
}

.vc-son-likes-text{
  position: absolute;
  left: 150px;
  color: rgb(157, 161, 168);
}

.vc-son-divider2{
  width: 840px !important;
  position: relative;
  margin-top: 5px!important;
  margin-left: 80px!important;
}

.vc-son-icon2{
  width: 17px;
  height: 17px;
  position: absolute;
  top: 2px;
  left: 190px;
}

.comment-son-son{
  position: absolute;
  top: 0px;
  left: 230px;
  color: rgb(157, 161, 168);
}
.comment-son-btn:hover {
  color: #0084ff;
}

.comment-son:hover {
  color: #0084ff;
}

.video-son-comment-operation{
  height: 120px;
  width: 833px;
  margin-left: 80px;
  margin-top: 10px;
}

.vc-son-at{
  width: 20px !important;
  height: 20px !important;
  position: absolute;
  margin-top: 80px;
  margin-left: 80px;
  border-radius: 2px;
}

.vc-son-at-icon{
  width: 15px !important;
  height: 20px !important;
  margin-left: 0px;
  fill: rgb(117, 121, 127);
}

.vc-son-pic{
  width: 20px;
  height: 20px;
  margin-left: 105px;
  margin-top: 80px;
}

.vc-son-pic-ul{
  margin-top: -23.3px;
}

.vc-son-pic-icon{
  width: 15px;
  height: 20px;
  fill: rgb(117, 121, 127);
}

.vc-son-publish{
  margin-left: 617px;
  margin-top : -60px;
  margin-left: 741px;
  width: 80px;
}

.vc-son-input{
  position: absolute;
  width: 715px;
  margin-top: 20px;
  margin-left: 155px;
 border: none;
 padding: 1rem;
 border-radius: 5px;
 background: #e8e8e8;
}

.vc-son-input:focus {
 outline-color: #e8e8e8;
 background: #e8e8e8;
 box-shadow: inset 20px 20px 60px #c5c5c5,
		inset -20px -20px 60px #ffffff;
 transition: 0.3s;
}

.vc-son-avatar{
 position: absolute;
 margin-top: 20px;
 margin-left: 100px;
 border: #409eff 2px solid;
}

.no-more-comment{
  position: relative;
  margin-top: 180px;
  margin-left: 385px;
  height: 100px;
  font-size: 13px;
  color: rgb(157, 161, 168);
}

.vc-img{
  margin-top: 10px;
}

.vc-img-item{
  max-width: 200px;
  max-height: 300px;
  min-width: 100px;
  min-height: 100px;
}
.vc-son-img{
  margin-top: 10px;
}

.vc-son-img-item{
  max-width: 100px;
  max-height: 200px;
  min-width: 50px;
  min-height: 50px;
  margin-left: 35px;
}

.video-son-comment-input{
  margin-top: 10px;
}
</style>