<script lang="ts" setup>
import { ref,onMounted,computed } from 'vue'
import { useRouter } from 'vue-router';
import { useTransition } from '@vueuse/core'
import CryptoJS from 'crypto-js'
import { ElMessage,ElMessageBox } from 'element-plus';
import { getHotSearch,findAllVideos,getUserInfoByUsername } from '../../api';
import { getRecommendVideo,getUserFollow,giveComment,getVideoComments,getAuthorIsFollow,giveAuthorFollow,giveAuthorUnfollow,getDanmuEach5s,getAuthorInfo,getVideo,giveToFavorites,giveToUnFavorites,getDanmuCount,getFavorite,like,dislike,getIsLike,getIsCoins,giveCoins, getUserFavoritesList } from '../../api/video';
import UserScroll from '../post/userScroll.vue'
import { getUserFollowingPosts,addPost,getAnyonePosts } from '../../api/post';

const router = useRouter();

    const userInfo = ref({
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

    const currentUserId = ref('');

    const getUserInfo = async () => {
      //获取登录用户名
      let loginUser = JSON.parse(localStorage.getItem('loginUser'))
      if (loginUser) {
        userInfo.value.username = loginUser.username;
        token.value = loginUser.token;
        const result = await getUserInfoByUsername(userInfo.value.username);
        if(result.code === 1){
          userInfo.value = result.data;
          currentUserId.value = result.data.id;
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

    const handleSelect = async(index) => {
  console.log('当前选择索引:', index);
  console.log('当前数组:', AtData.value);
  await getUserInfo();
  if(index == 0){
    getCurrentUserFollowingPosts();
  }else{
    const userId = AtData.value[index - 1].id;
    const result = await getAnyonePosts(userId);
    if(result.code === 1){
      userpostList.value = result.data;
     // 使用 for...of 替代 forEach 以支持 await
     for (const item of userpostList.value) {
      item.createdTime = formatTime(item.createdTime);
      
      // 添加 await 等待异步结果
      const likeStatus = await getCurrentPostIsLike(item.id);
      item.isLike = likeStatus === 1;
    }
    }else{
      ElMessage.error('获取失败');
    }
  }
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

    const gotoFavorites = () => {
      router.push('/userInfo/' + userInfo.value.id + '/favorites').then(() => {
        window.location.reload();
      })
    }

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

  const formatTime = (dateString) => {
    // 解析时间
    const targetDate = new Date(dateString);
    const currentDate = new Date();
    
    // 计算时间差（单位：秒）
    const diffInSeconds = Math.floor((currentDate - targetDate) / 1000);
    
    // 各时间段阈值
    const MINUTE = 60;
    const HOUR = 3600;
    const DAY = 86400;
    const WEEK = 604800;
    const YEAR = 31536000;

    if (diffInSeconds < MINUTE) {
      return '刚刚';
    } else if (diffInSeconds < HOUR) {
      return `${Math.floor(diffInSeconds / MINUTE)}分钟前`;
    } else if (diffInSeconds < DAY) {
      return `${Math.floor(diffInSeconds / HOUR)}小时前`;
    } else if (diffInSeconds < WEEK) {
      return `${Math.floor(diffInSeconds / DAY)}天前`;
    } else if (diffInSeconds < YEAR) {
      // 处理月份和日期补零
      const month = (targetDate.getMonth() + 1).toString().padStart(2, '0');
      const day = targetDate.getDate().toString().padStart(2, '0');
      return `${month}-${day}`;
    } else {
      // 处理完整日期格式
      const year = targetDate.getFullYear();
      const month = (targetDate.getMonth() + 1).toString().padStart(2, '0');
      const day = targetDate.getDate().toString().padStart(2, '0');
      return `${year}-${month}-${day}`;
    }
  };

const textarea2 = ref('')

const tagInput = ref<string[]>()

const imageUrl = ref<string[]>([])


let postEntity = ({
	id: '',
	title: '',
	content: '',
	imageUrl: [],
  videoId: '',
  tags: [],
  userId: ''
})

const addUserPost = async () => {
  if(tagInput.value.length == 0){
  	ElMessage.error('请至少选择一个标签');
    return;
  }
  if(textarea2.value.length == 0){
  	ElMessage.error('请输入内容');
    return;	
  }
  postEntity.title = titleInput.value;
  postEntity.content = textarea2.value;
  postEntity.imageUrl = imageUrl.value;
  postEntity.userId = currentUserId.value;
  postEntity.tags = tagInput.value;
  const result = await addPost(postEntity);	
  if(result.code === 1){
    ElMessage.success('发布成功');
    getCurrentUserFollowingPosts();
    titleInput.value = '';
    textarea2.value = '';
    imageUrl.value = [];
    tagInput.value = [];
  }else{
    ElMessage.error('发布失败');
  }
}

const handleAvatarSuccess = (response,uploadFile) => {
  console.log(response.data)
  imageUrl.value.push(response.data)
  console.log('数组：',imageUrl.value)
  ElMessage.success('上传成功');
}

const handleAvatarSuccessByComment = (response,uploadFile) => {
  console.log(response.data)
  commentEntity.value.imageUrl = response.data;
  ElMessage.success('上传成功');
}

const deleteImage = (index) => {
  imageUrl.value.splice(index, 1);	
}

// 文件上传之前触发
const beforeAvatarUpload = (rawFile) => {
  if(imageUrl.value.length >= 6){
    ElMessage.error('最多上传6张图片')
    return false
  }
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

const AtSonDialogVisibleOn = () =>{
  AtDialogVisible.value = true;
}

const AtSonDialogVisibleOn2 = () =>{
  AtDialogVisible2.value = true;
}

const AtSonDialogVisibleOn3 = () =>{
  AtDialogVisible3.value = true;
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
    console.log(AtData.value)
	}	else{
		ElMessage.error('获取关注列表失败',result.msg);
  }
}

const count = ref(0);
const AtData = ref([]);
const CollectDialogLoad = () => {
  count.value += 2
}

const AtDialogVisible = ref(false)
const AtDialogVisible2 = ref(false)
const AtDialogVisible3 = ref(false)

const AtReslut = () =>{
  AtDialogVisible.value = false;
  textarea2.value = '@' + AtRows.value + ' ';
}

const AtReslut2 = () =>{
  AtDialogVisible2.value = false;
  commentInput.value = '@' + AtRows.value + ' ';
}

const AtReslut3 = () =>{
  AtDialogVisible3.value = false;
  commentSonInput.value = '@' + AtRows.value +' ';	
}

const selectedRows = ref([])  // 存储选中行数据

const handleAtSelectionChange = (selection) => {
  AtRows.value = selection.map(row => row.username);
}

const AtRows = ref([])  // 存储选中行数据

const titleInput = ref('')

const userpostList = ref([{
  videoId: '',
  isOpen: false,
  isLike: '',
  isShare: '',
}]);

const openCommentList = async(index) => {
  userpostList.value = userpostList.value.map((item, i) => ({
    ...item,
    isOpen: i === index ? !item.isOpen : false
  }));
  currentPostId.value = userpostList.value[index].id;
  await getCurrentVideoComments();
}

const getCurrentUserFollowingPosts = async() =>{
  console.log(currentUserId.value)
  const result = await getUserFollowingPosts(currentUserId.value);
  if(result.code === 1){
    userpostList.value = result.data;
     // 使用 for...of 替代 forEach 以支持 await
     for (const item of userpostList.value) {
      item.createdTime = formatTime(item.createdTime);
      
      // 添加 await 等待异步结果
      const likeStatus = await getCurrentPostIsLike(item.id);
      item.isLike = likeStatus === 1;
    }
    console.log('关注动态：',userpostList.value)
  }else{
    ElMessage.error('获取动态失败',result.msg);
  }
}   

const activeTab = ref('hot');

const switchTab = async(tab) => {
  activeTab.value = tab;
  await getCurrentVideoComments(currentPostId.value);
}

// 控制回复列表显示状态
const showReplies = ref({})

// 切换显示状态的方法
const toggleReplies = (index) => {
  showReplies.value[index] = true
}

const commentInput = ref('');

const commentSonInput = ref('');

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

const currentPostId = ref('');

const getCurrentVideoComments = async() =>{
  let result = null;
  if(activeTab.value === 'hot'){
    result = await getVideoComments(currentPostId.value,2,1);
  }else{
    result = await getVideoComments(currentPostId.value,2,2);
  }
  if(result.code === 1){
    commentList.value = result.data;
    console.log('评论：',commentList.value)
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

const toUserInfoIndex = (id) => {
  if(id === currentUserId.value){
  	router.push(`/userInfo/${id}`).then(() => {
      window.scrollTo(0, 0); // 滚动到页面顶部
      window.location.reload();
     });
  }else{
    router.push(`/userInfo/others/${id}`).then(() => {
      window.scrollTo(0, 0); // 滚动到页面顶部
      window.location.reload();
    })
  }
}

const toVideo = (id) => {
  router.push(`/video/${id}`).then(() => {
    window.scrollTo(0, 0); // 滚动到页面顶部
    window.location.reload();
  })
}

// 初始化响应式数组
const isToComment = ref([]);

// 切换指定评论的回复框状态
const toCommentSon = (index) => {
  isToComment.value[index] = !isToComment.value[index];
}

const commentEntity = ref({
  entityType: '',
  entityId: '',
  content: '',
  imageUrl: ''
})

const giveCurrentVideoComment = async(id) =>{
    commentEntity.value.entityType = 2;
    commentEntity.value.entityId = id;
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


// 处理空格键事件
const handleSpace = () => {
  if (searchText.value.trim() !== '') { 
    addSearchHistory(searchText.value.trim()); // 传递处理后的内容
  }
};

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

const likeEntity = ref({ 
  entityId: '',
  entityType: '',
  userId: '',
})

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

const toLike = (index,id) => {
  for(let i = 0;i < userpostList.value.length;i++){
    if(i === index){
      if(userpostList.value[index].isLike){
  	    dislikeCurrentPost(index,id);
      }else{
      	likeCurrentPost(index,id);
      }
    }
  }
  
}


const likeCurrentPost = async(index,id) => {
  likeEntity.value.entityId = id;
  likeEntity.value.entityType = 2;
  const result = await like(likeEntity.value);
  if(result.code === 1){
    for(let i = 0;i < userpostList.value.length;i++){
      if(i === index){
        userpostList.value[i].isLike = true;
        userpostList.value[i].likes += 1;
      }
    }
  }else{
    ElMessage.error('点赞失败',result.msg);
  }
}

const dislikeCurrentPost = async(index,id) => {
  likeEntity.entityId = id;
  likeEntity.entityType = 2;
  const result = await dislike(likeEntity);
  if(result.code === 1){
    for(let i = 0;i < userpostList.value.length;i++){
      if(i === index){
        userpostList.value[i].isLike = false;
        userpostList.value[i].likes -= 1;
      }
    }
  }	else{
    ElMessage.error('取消点赞失败',result.msg);
  }
}

const getCurrentPostIsLike = async(id) =>{
  const result = await getIsLike(userInfo.value.id,id,2);
  if(result.code === 1){
    if(result.data != undefined){
      console.log('当前动态已点赞：',id)
      return 1;
    }else{
      return 0;
    }
  }else{
    ElMessage.error('获取点赞状态失败！',result.msg);
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

const isLoading = ref(true); // 初始状态为加载中

const searchText = ref(''); // 搜索框内容

const toSearch = () =>{
  if(searchText.value === ''){
    ElMessage.error('搜索内容不能为空！');
    return;
  }
  addSearchHistory(searchText.value);
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

    // AES加密（自动处理URL安全编码）
    const encryptSearchText = (text) => {
  const encrypted = CryptoJS.AES.encrypt(text, 'yilena-key').toString()
  return btoa(encrypted).replace(/\+/g, '-').replace(/\//g, '_')
}

const searchHistory = ref([]);


// 最大历史记录数量（假设每行5个按钮，最多3行）
const maxHistoryCount = 10;

// 添加搜索历史
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

const toUpload = () => {
      router.push('/upload').then(() => {
        window.location.reload();
      })
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
      await getUserInfo();
      await getCurrentUserFolow();
      await getCurrentUserFollowingPosts();
  isLoading.value = false;

      // 在父容器（如 #app）监听事件，通过选择器过滤目标
document.getElementById('app').addEventListener('click', (e) => {
  const mentionElement = e.target.closest('.mention');
  if (mentionElement) {
    const username = mentionElement.dataset.username;
    handleMentionClick(username);
  }
});

getHotSearchData();

 //读取本地存储的搜索历史
 const storedHistory = localStorage.getItem('searchHistory');
  if (storedHistory) {
    searchHistory.value = JSON.parse(storedHistory);
  }
    })
</script>

<template>
  
  <el-affix :offset="0">
 <!-- 顶栏 -->
 <el-row class="post-top-bar">
    <el-col :span="24">

<div class="background-layer1"></div>
<div class="top-logo-search"><svg t="1743859746673" class="logo-icon" viewBox="0 0 2299 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6640" ><path d="M1776.040306 322.618853a497.5306 497.5306 0 0 1 55.146298-6.026207c3.013104 4.508817 24.061475 155.944368 18.555516 156.94151s-44.632952 10.036453-44.632952 10.036453c-3.013104-20.5498-28.570291-147.923876-29.090539-160.951756m75.219204-14.54527l20.5498 162.967718a2379.701518 2379.701518 0 0 0 57.16226-4.010246 20057.059972 20057.059972 0 0 0-16.539554-158.957472 156.984864 156.984864 0 0 0-61.172506 0m-40.601029 226.156185s92.755901-23.562904 126.355258-12.030738c17.038125 61.671077 48.144626 407.65774 51.136052 421.70444-21.070048 2.514532-90.263045 8.519063-95.769004 10.036453-4.508817-26.012405-81.722306-403.192278-81.722306-419.688478m343.472131-207.578992a540.971316 540.971316 0 0 1 55.167976-1.51739c0.997142 12.030737 6.026207 157.440081 0.498571 157.938652l-44.632951 4.010246c-0.997142-20.051229-12.030737-146.926734-11.033596-160.409831m75.717776-4.010246l4.010246 160.951756c10.535024 0 52.653443 2.514532 57.16226 2.015961-0.997142-61.671077 0-158.957472 0-158.957471a277.032113 277.032113 0 0 0-61.172506-4.010246m-64.68418 216.119732s94.771862-12.529308 126.853828 2.514532c7.023349 72.206101 6.026207 408.654883 7.023349 422.701581-21.568619 0-90.263045 0.997142-95.769004 2.015962-0.997142-26.012405-39.603887-410.670844-38.108173-427.210398M1945.012553 125.054637c54.647728 278.787951 96.267576 755.140117 97.784966 785.72637 0 0 43.115561 0.997142 91.758759 4.010246-28.591969-300.35657-63.687038-780.220411-63.188468-795.762823-12.030737-13.548128-126.355257 6.026207-126.355257 6.026207m-120.32905 659.371113c-10.535024-78.232308-290.320117-166.912932-447.760198-138.385995 0 0-19.509304-172.483922-27.074578-339.461885-6.503101-143.41506-0.997142-282.798196 0.498571-305.862529-10.535024-7.52192-123.840725 46.627236-185.034908 69.192997 0 0 73.203243 309.98116 126.355257 952.704334A862.57135 862.57135 0 0 0 1521.725693 1003.645293c145.409344-28.180105 317.394695-115.820233 302.849425-219.111158M1482.707085 925.326277l-24.560045-176.992739a1281.501133 1281.501133 0 0 1 172.982493 63.188467c-2.514532 11.033595-148.422448 113.825949-148.422448 113.825949m-871.97917-602.620716a497.5306 497.5306 0 0 1 55.146299-6.026207c3.013104 4.508817 24.061475 155.944368 18.555516 156.94151s-44.632952 10.036453-44.632952 10.036453c-3.013104-20.5498-28.570291-147.923876-29.09054-160.951756m75.219205-14.54527l20.5498 162.967718a2379.701518 2379.701518 0 0 0 57.16226-4.010246c-6.503101-61.671077-16.539554-158.957472-16.539554-158.957472a156.984864 156.984864 0 0 0-61.172506 0M645.324414 534.229768s92.755901-23.562904 126.355257-12.030738c17.038125 61.671077 48.144626 407.65774 51.136053 421.70444-21.070048 2.514532-90.263045 8.519063-95.769004 10.036453-4.508817-26.012405-81.722306-403.192278-81.722306-419.688478m343.472131-207.578992A540.971316 540.971316 0 0 1 1043.964521 325.155063c0.997142 12.030737 6.026207 157.440081 0.498571 157.938652l-44.632952 4.010246c-0.997142-20.051229-11.532166-146.926734-11.033595-160.409831m75.717776-4.010246l4.010246 160.951756c10.535024 0 52.653443 2.514532 57.16226 2.015961-0.997142-61.671077 0-158.957472 0-158.957471a311.173395 311.173395 0 0 0-61.172506-4.010246m-64.684181 216.119732s94.771862-12.529308 126.853829 2.514532c7.023349 72.206101 6.026207 408.654883 7.023349 422.701581-21.568619 0-90.263045 0.997142-95.769004 2.015962-0.498571-26.012405-39.603887-410.670844-38.108174-427.210398M779.72184 125.054637c54.647728 278.787951 96.267576 755.140117 97.784966 785.72637 0 0 43.115561 0.997142 91.758758 4.010246C940.673596 614.45636 905.578526 134.592519 906.098774 118.551536c-12.030737-13.006203-126.355257 6.503101-126.355257 6.503101m-120.372404 659.371113c-10.535024-78.232308-290.320117-166.912932-447.760198-138.385995 0 0-19.509304-172.483922-27.074578-339.461885C178.011558 163.16281 183.517517 23.844705 185.034908 0.715341 174.499884-6.308008 61.172506 47.342577 0 69.908338c0 0 73.203243 309.98116 126.355257 952.704334a862.57135 862.57135 0 0 0 230.166431-18.967379c145.409344-28.180105 317.394695-115.820233 302.849425-219.111158m-341.976418 140.900527l-24.560046-176.992739a1281.501133 1281.501133 0 0 1 172.982494 63.188467c-2.514532 11.033595-148.422448 113.825949-148.422448 113.825949" fill="#1296db" p-id="6641"></path></svg></div>

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
      <el-avatar :src= "userInfo.image" :size="37" class="avatar" @click="toUserInfo(currentUserId)"></el-avatar>
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
<button class="cta-1" @click="toUserInfo(currentUserId)">
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
    </el-col>
  </el-row>
  </el-affix>
<el-affix  z-index="-10" :offset="0">
  <div class="post-bg">
    <img class="post-bg-img" src="YilenaPic/25/04/27-21f6ea11d53849b7aa2372dd6811c2bc.avif" alt=""/>
  </div>
  </el-affix>

  <div class="post-left-top">
    <div class="post-left-top-userInfo">
      <el-avatar :src= "userInfo.image" :size="47" class="avatar-info" @click="toUserInfo(currentUserId)"></el-avatar>
      <span class="post-left-top-userInfo-name">{{ userInfo.username }}</span>
      <div class="post-left-top-userInfo-line">
      <div class="post-left-top-userInfo-report">
        <span class="post-left-top-userInfo-report-count">{{ userInfo.followingCount }}</span>
        <span class="post-left-top-userInfo-report-text">关注</span>
      </div>
      <div class="post-left-top-userInfo-report">
        <span class="post-left-top-userInfo-report-count">{{ userInfo.followersCount }}</span>
        <span class="post-left-top-userInfo-report-text">粉丝</span>
      </div>
      <div class="post-left-top-userInfo-report">
        <span class="post-left-top-userInfo-report-count">{{ userInfo.postCount }}</span>
        <span class="post-left-top-userInfo-report-text">动态</span>
      </div>
    </div>
    </div>

    <el-affix :offset="65">
    <div class="post-left-top-live">
      <img class="post-left-top-live-img" src="YilenaPic/25/04/27-ba0417ef551342e9886a5ab6c4229f55.jpg" />
    </div>
    </el-affix>
  </div>

  <div class="post-center-top">
    <div class="post-center-top-input">
      <input class="post-titleInput" v-model="titleInput" style="width: 695px" placeholder="好的标题更容易获得支持，选填20字" />
      <textarea
    v-model="textarea2"
    style="width: 695px"
    placeholder="有什么想和大家分享的？"
    class="post-center-top-input-textarea"
  />
  <el-input-tag draggable class="post-center-top-input-tag" v-model="tagInput" :max="3" placeholder="最多输入3个标签，按下回车键确认" />
  <div class="post-center-top-imgAndAt">
    <div class="vc-son-at-post" @click="AtSonDialogVisibleOn">
    <botton >
      <svg t="1744279969705" class="vc-son-at-icon-post" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3548" width="200" height="200"><path d="M657.476791 626.845655c-5.884011 26.478048 1.471514 47.072085 22.065552 61.782112l26.478048 0c108.854197-50.01409 161.810293-148.57178 158.868287-295.672046-20.594037-158.868287-136.803759-247.128447-348.628142-264.780479-144.158261 8.826016-248.599961 72.079642-313.324078 189.759855-32.362059 61.782112-50.01409 129.448234-52.956096 202.998367 11.768021 164.752298 85.318154 276.5485 220.650399 335.388607 155.926282 47.072085 300.084543 16.181541 432.474782-92.673679l48.543599 52.956096c-111.796202 97.086176-239.773945 142.68777-383.932206 136.803759-161.810293-8.826016-279.490505-89.731674-353.040638-242.715951-32.362059-76.492138-45.601594-157.397796-39.717583-242.715951 11.768021-152.984277 91.202165-275.078009 238.302431-366.280174 79.434144-29.420053 150.042271-42.659589 211.824383-39.717583 170.636309 2.942005 294.200532 67.666122 370.69267 194.172351 29.420053 44.13008 42.659589 110.325711 39.717583 198.585871-20.594037 164.752298-97.086176 269.193998-229.476415 313.324078l-52.956096 0c-47.072085-14.710027-70.608128-41.188074-70.608128-79.434144l0-4.41352c-52.956096 55.898101-113.267716 80.905658-180.933839 75.021647-58.840106-8.826016-100.028181-38.981826-123.564223-90.466408s-25.007557-117.680213-4.41352-198.585871 66.195631-147.836023 136.803759-200.792119c79.434144-47.072085 155.926282-45.601594 229.476415 4.41352l26.478048 35.304064 13.239536-48.543599 88.26016 0L657.476791 626.845655zM339.739193 560.650023c-5.884011 61.782112 13.239536 100.028181 57.369615 114.738207l57.369615 4.41352c111.796202-47.072085 172.107823-147.100266 180.933839-300.084543-8.826016-44.13008-44.13008-67.666122-105.912192-70.608128C426.528861 329.703117 363.275235 413.549757 339.739193 560.650023z" p-id="3549"></path></svg>
    </botton>
  </div>
    <el-upload
                class="vc-son-pic-ul"
                action="/api/upload"
                :headers="{'token':token}"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                >
    <botton class="vc-son-pic-post">
      <svg t="1744280525370" class="vc-son-pic-icon-post" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6352" width="200" height="200"><path d="M937.472 102.4H91.136c-46.08 0-83.456 37.376-83.456 83.456V841.728c0 46.08 37.376 83.456 83.456 83.456h846.336c46.08 0 83.456-37.376 83.456-83.456V185.856c0-46.08-37.376-83.456-83.456-83.456zM91.136 163.84h846.336c12.288 0 22.016 9.728 22.016 22.016v437.248L762.88 448.512c-10.752-9.728-27.136-10.24-38.912-1.536l-244.224 183.296-129.536-92.16c-10.24-7.68-24.576-7.68-35.328 0l-245.76 174.08V185.856c0-12.288 9.728-22.016 22.016-22.016z m846.336 699.904H91.136c-12.288 0-22.016-9.728-22.016-22.016v-54.272L332.8 601.088l247.808 176.64c13.824 9.728 33.28 6.656 43.008-7.168 9.728-13.824 6.656-33.28-7.168-43.008l-84.48-59.904 208.896-156.672 211.456 187.904c2.048 2.048 4.608 3.072 7.168 4.608v138.24c0 12.288-9.728 22.016-22.016 22.016z" fill="#646464" p-id="6353"></path><path d="M312.32 471.04c70.656 0 128-57.344 128-128S382.976 215.04 312.32 215.04 184.32 272.384 184.32 343.04s57.344 128 128 128z m0-194.56c36.864 0 66.56 29.696 66.56 66.56s-29.696 66.56-66.56 66.56-66.56-29.696-66.56-66.56 29.696-66.56 66.56-66.56z" fill="#646464" p-id="6354"></path></svg>
    </botton>
  </el-upload>
  <div class="post-input-right">
  <span class="text-count">{{ textarea2.length }}</span>
  <el-divider direction="vertical" class="post-input-divider"/>
    <el-button class="post-center-top-publish-btn" type="primary" :disabled="textarea2 === ''" @click="addUserPost">发布</el-button>
  </div>
</div>
<div class="post-img-show" v-if="imageUrl?.length != 0">
  <template  v-for="(item, index) in imageUrl" :key="index">
  <img class="post-img-show-item" :src="item" alt="图片" />
  <button class="post-img-show-item-delete-btn" @click="deleteImage(index)"><el-icon class="post-icon-cha" style="color: black;"><Close /></el-icon></button>
</template>
</div>
    </div>
    <div class="followingAccountAvatar">
      <UserScroll 
    :users="AtData"
    @select="handleSelect"
    class="scroll"
  />
    </div>

    <div v-if="isLoading" class="post-loading">
        <!-- From Uiverse.io by rillala --> 
<div class="spinner">
  <div>N</div>
  <div>O</div>
  <div>R</div>
  <div>A</div>
  <div class="icon-1">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="0 0 50 49"
      height="49"
      width="40"
    >
      <path
        fill="#3EB3C2"
        d="M12.376 0.00166752C13.3938 -0.0161892 14.3938 0.110593 15.3402 0.323088C15.619 0.385826 15.8783 0.515842 16.0954 0.701795C16.3124 0.887749 16.4807 1.12399 16.5855 1.38991C16.6902 1.65583 16.7284 1.94335 16.6965 2.22739C16.6646 2.51143 16.5638 2.78335 16.4027 3.01945C16.3991 3.0248 16.3937 3.03016 16.3937 3.03909C15.983 3.66407 15.2331 3.96764 14.5009 3.80693C13.8581 3.64622 13.1795 3.57479 12.501 3.57479C7.42968 3.57479 3.32264 7.82468 3.5905 12.9495C3.70172 15.0895 4.57963 17.1179 6.06364 18.6637L5.99222 18.7994C7.07605 17.3944 8.46806 16.257 10.0609 15.4749C11.6538 14.6929 13.405 14.2871 15.1795 14.2888C16.9294 14.2888 18.6794 14.6816 20.2508 15.4495L28.7684 19.5922C29.5005 19.9494 30.3398 19.4137 30.3398 18.6101V16.2352C30.3398 14.0031 31.1791 11.9674 32.554 10.4139L39.786 2.36054C40.2324 1.86055 41.0717 2.16411 41.0717 2.84267V7.68183C44.4109 8.44966 47.0358 11.8246 47.7143 15.3066C47.8036 15.753 48.1965 16.0745 48.6429 16.0745C49.0028 16.0745 49.348 16.2174 49.6025 16.472C49.857 16.7265 50 17.0716 50 17.4316V17.8244C50 21.7886 46.7858 25.0028 42.8216 25.0028V27.842C42.8221 29.7979 42.0946 31.684 40.7809 33.133C39.4672 34.5821 37.6612 35.4904 35.7147 35.6811V47.5558C35.7147 47.9129 35.4111 48.2165 35.0361 48.2165H31.0184C30.6434 48.2165 30.3398 47.9129 30.3398 47.5379V37.04L27.8399 47.5379C27.7327 47.9308 27.3756 48.2165 26.9649 48.2165H23.1614C22.7329 48.2165 22.4114 47.8058 22.5186 47.4129L25.2989 35.7168H18.1312L20.9115 47.4129C21.0186 47.8058 20.6972 48.2165 20.2686 48.2165H16.4652C16.0545 48.2165 15.6973 47.9308 15.5902 47.5379L13.5009 38.7703L11.8046 40.4667C11.4296 40.8417 11.3224 41.3952 11.5367 41.8774L13.8581 47.2879C14.0724 47.7165 13.7509 48.2165 13.2688 48.2165H9.37606C9.10821 48.2165 8.85822 48.0558 8.76893 47.8058L4.26905 37.2882C3.80478 36.2525 3.57264 35.1275 3.57264 34.0026V25.8956C3.57264 24.4957 3.82085 23.1546 4.27441 21.9118C2.90905 20.7192 1.81961 19.2438 1.08171 17.5879C0.343819 15.9321 -0.0248528 14.1354 0.00130081 12.3228C0.0905838 5.57295 5.62616 0.0730942 12.376 0.00166752ZM43.1912 15.6209C43.1912 14.9799 42.6734 14.4834 42.0538 14.4834C41.4127 14.4834 40.9163 15.0013 40.9163 15.6209V17.3173C40.9186 17.6182 41.0392 17.9062 41.252 18.119C41.4649 18.3318 41.7528 18.4524 42.0538 18.4548C42.2033 18.4552 42.3514 18.4261 42.4896 18.3691C42.6278 18.3121 42.7534 18.2284 42.8591 18.1227C42.9649 18.0169 43.0486 17.8914 43.1056 17.7531C43.1626 17.6149 43.1917 17.4668 43.1912 17.3173V15.6209Z"
      ></path>
    </svg>
  </div>
  <div>N</div>
  <div>O</div>
  <div>R</div>
  <div>A</div>
  <div class="icon-2">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="0 0 50 47"
      height="47"
      width="40"
    >
      <path
        fill="#3EB3C2"
        d="M25.9824 18.7517L40.625 23.9809V45.3126C40.625 45.727 40.4604 46.1244 40.1674 46.4174C39.8743 46.7104 39.4769 46.875 39.0625 46.875H32.8125C32.3981 46.875 32.0007 46.7104 31.7076 46.4174C31.4146 46.1244 31.25 45.727 31.25 45.3126V34.3757H15.625V45.3126C15.625 45.727 15.4604 46.1244 15.1674 46.4174C14.8743 46.7104 14.4769 46.875 14.0625 46.875H7.8125C7.3981 46.875 7.00067 46.7104 6.70765 46.4174C6.41462 46.1244 6.25 45.727 6.25 45.3126V24.4242C2.62109 23.1303 0 19.695 0 15.6269C0 14.7981 0.32924 14.0033 0.915291 13.4173C1.50134 12.8313 2.2962 12.502 3.125 12.502C3.9538 12.502 4.74866 12.8313 5.33471 13.4173C5.92076 14.0033 6.25 14.7981 6.25 15.6269C6.25155 16.4551 6.58129 17.249 7.167 17.8347C7.75272 18.4204 8.54667 18.7501 9.375 18.7517H25.9824ZM50 7.81483V10.9396C50 12.5971 49.3415 14.1868 48.1694 15.3588C46.9973 16.5308 45.4076 17.1893 43.75 17.1893H40.625V20.6637L28.125 16.2001V1.5652C28.125 0.173679 29.8066 -0.523545 30.792 0.460771L33.4561 3.1276H38.6934C39.7588 3.1276 41.0127 3.901 41.4883 4.85504L42.1875 6.25242H48.4375C48.8519 6.25242 49.2493 6.41703 49.5424 6.71004C49.8354 7.00305 50 7.40045 50 7.81483ZM39.0625 7.81483C39.0625 7.50581 38.9709 7.20374 38.7992 6.9468C38.6275 6.68986 38.3834 6.4896 38.0979 6.37135C37.8124 6.25309 37.4983 6.22215 37.1952 6.28244C36.8921 6.34273 36.6137 6.49153 36.3951 6.71004C36.1766 6.92854 36.0278 7.20694 35.9675 7.51002C35.9072 7.81309 35.9382 8.12724 36.0564 8.41273C36.1747 8.69823 36.375 8.94224 36.6319 9.11392C36.8889 9.2856 37.191 9.37723 37.5 9.37723C37.9144 9.37723 38.3118 9.21262 38.6049 8.91961C38.8979 8.62661 39.0625 8.2292 39.0625 7.81483Z"
      ></path>
    </svg>
  </div>
  <div class="middle">loading</div>
</div>
      </div>
      <div v-else-if="!isLoading && userpostList.length > 0">
        <div class="post-center-content" v-for="(item, index) in userpostList" :key="index">
          <el-avatar :src= "item.image" :size="52" class="post-item-avatar"    @click="toUserInfoIndex(item.userId)"></el-avatar>
          <span class="post-item-username"   @click="toUserInfoIndex(item.userId)" >{{ item.username }}</span>
          <span class="post-item-time">{{ item.createdTime }}</span>
          <el-row  v-if="item.title != ''"><span class="post-item-title">{{ item.title }}</span></el-row>
          <span v-if="item.title != ''" class="post-item-content" v-html="processCommentContent(item.content)"></span>
          <el-row v-else><span class="post-item-content" v-html="processCommentContent(item.content)"></span></el-row>
          <el-row class="post-item-tag-row">
          <div v-for="(tag, index) in item.tags" class="post-item-tag">
            <span>#{{tag}}#</span>
          </div>
        </el-row>
        <div v-if="item.imageUrl?.length > 0">
          <el-row class="post-img-row">
          <div class="post-img-show" v-for="(item, index) in item.imageUrl" :key="index">
            <img class="post-img-show-item" :src="item" alt="图片" />
          </div>
        </el-row>
        </div>
        <div v-if="item.videoId != '' && item.videoId != null">
            <div class="post-video-show">
              <el-row>
              <el-avatar @click="toUserInfoIndex(item.videoUserId)" :src="item.videoUserImage" :size="20" class="post-video-avatar" />
              <span class="post-video-username" @click="toUserInfoIndex(item.videoUserId)">{{ item.videoUsername }}&nbsp;<span style="color: rgb(148, 153, 160);">投稿了视频</span></span>
            </el-row>
            <div class="post-video-show-item" @click="toVideo(item.videoId)">
              <img class="post-video-cover" :src="item.videoCoverUrl" alt="视频" />
              <div class="post-video-title">{{ item.videoTitle }}</div>
              <div class="post-video-description">{{ item.videoDescription }}</div>
              <span class="post-video-time">{{ formatVideoDuration(item.videoTime) }}</span>
              <el-row class="post-video-info">
                <svg t="1743757350808" class="post-video-icon1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="28554" width="200" height="200"><path d="M759.528727 209.408H264.424727A53.853091 53.853091 0 0 0 209.454545 262.004364v499.898181a53.992727 53.992727 0 0 0 54.970182 52.689455h495.080728a53.829818 53.829818 0 0 0 55.016727-52.689455v-499.898181a53.690182 53.690182 0 0 0-54.993455-52.596364z m0 552.494545H264.424727V262.097455h495.080728v499.80509z" p-id="28555"></path><path d="M436.386909 360.727273v302.498909L625.524364 512 436.386909 360.727273z"  p-id="28556"></path></svg>
    <span class="post-video-views">{{ formatViews(item.videoViews)}}</span>
    <svg t="1743757905181" class="post-video-icon2" viewBox="0 0 1303 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29556" width="200" height="200"><path d="M110.964364 1023.720727c-60.509091 0-109.847273-48.872727-109.847273-108.916363V109.009455C1.117091 48.965818 50.455273 0 110.964364 0h1081.902545c60.509091 0 109.847273 48.872727 109.847273 108.916364v805.794909c0 60.043636-49.338182 108.916364-109.847273 108.916363H110.964364z m0-927.650909c-6.609455 0-12.101818 5.771636-12.101819 12.939637v805.794909c0 7.168 5.492364 13.032727 12.101819 13.032727h1081.902545c6.609455 0 12.101818-5.864727 12.101818-13.032727V109.009455c0-7.168-5.492364-13.032727-12.101818-13.032728H110.964364z" p-id="29557"></path><path d="M1003.054545 520.098909a51.572364 51.572364 0 0 1-50.26909 52.689455h-80.058182a51.572364 51.572364 0 0 1-50.269091-52.689455c0-29.137455 22.434909-52.782545 50.269091-52.782545h80.058182c27.834182 0 50.269091 23.645091 50.26909 52.782545zM1096.610909 270.149818c0 28.858182-22.341818 52.410182-49.989818 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273h814.08c27.648 0 50.082909 23.458909 50.082909 52.503273zM768.744727 520.098909c0 28.858182-22.341818 52.410182-50.082909 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273H718.661818c27.648 0 50.082909 23.458909 50.082909 52.503273zM1096.610909 735.976727c0 29.044364-22.341818 52.503273-49.989818 52.503273H560.500364a51.293091 51.293091 0 0 1-50.082909-52.503273c0-28.951273 22.341818-52.410182 50.082909-52.410182h486.120727c27.648 0 50.082909 23.458909 50.082909 52.410182z"  p-id="29558"></path></svg>
    <span class="post-video-barrages">{{ item.videoBarrages }}</span>
              </el-row>
            </div>
            </div>
          </div>
          <el-row class="post-opreate-row">
            <el-col :span="8" class="post-opreate-col">
              <div class="post-opreate-col-item">
              <svg t="1745833409610" class="post-opreate-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4022" width="200" height="200"><path d="M1009.777778 503.466667l-443.733334-455.111111c-5.688889-5.688889-11.377778 0-11.377777 5.688888v267.377778C8.533333 409.6 2.844444 918.755556 17.066667 932.977778c0 0 45.511111-48.355556 164.977777-113.777778 85.333333-48.355556 224.711111-85.333333 369.777778-102.4v261.688889c0 8.533333 11.377778 11.377778 14.222222 5.688889l443.733334-480.711111z m-398.222222 358.4v-199.111111l-36.977778-2.844445c-221.866667 8.533333-378.311111 73.955556-497.777778 156.444445 76.8-275.911111 267.377778-403.911111 466.488889-438.044445l68.266667-2.844444v-199.111111l312.888888 312.888888s8.533333 5.688889 8.533334 14.222223-8.533333 14.222222-8.533334 14.222222l-312.888888 344.177778z" p-id="4023"></path></svg>
              <span class="post-opreate-count">{{ item.shares }}</span>
            </div>
            </el-col>
            <el-col :span="8" class="post-opreate-col" 
            >
              <div class="post-opreate-col-item"
        :class="{ 'active-comment': userpostList[index].isOpen }"
            @click="openCommentList(index)">
              <svg t="1745833949410" class="post-opreate-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="10539" width="200" height="200"><path d="M417.449 973.592a1083.127 1083.127 0 0 1-121.801-6.537c-70.71-6.772-122.556-15.508-158.14-26.731-40.738-12.846-63.186-19.968-61.799-45.773 0.658-6.82 3.253-12.795 7.798-17.925 4.545-5.129 10.162-8.433 16.854-9.906 32.631-5.555 60.003-14.179 82.28-25.866-0.176-8.45-4.226-16.143-11.11-23.897-36.624-40.48-65.458-85.9-86.507-136.27-22.603-53.93-34.428-110.138-35.47-168.607-1.02-62.52 10.632-122.583 34.95-180.186 23.726-55.549 56.651-104.907 97.845-146.683 42.334-42.65 91.093-75.861 146.278-99.64 57.464-24.805 117.485-37.399 180.067-37.784 66.895 0 125.971 11.689 180.422 34.775 55.603 23.55 105.192 56.127 147.49 96.747 43.099 41.657 76.645 89.954 100.62 144.89 24.597 55.488 37.381 115.723 38.02 179.027 1.029 58.912-9.367 115.734-31.191 170.464-21.073 52.65-50.951 99.706-89.626 141.187-38.909 41.414-83.676 75.035-134.3 100.852-52.47 26.907-108.04 43.34-166.703 49.297-14.121 1.968-30.145 3.649-47.1 4.978a1257.813 1257.813 0 0 1-88.877 3.588z m-207.555-86.388c-2.867 1.503-24.274 7.577-27.195 8.967 27.253 5.09 79.27 9.489 118.732 13.252a1028.444 1028.444 0 0 0 146.8 5.787c19.674-0.58 37.437-1.565 53.524-2.838a878.9 878.9 0 0 0 44.786-4.744c51.762-5.204 100.804-19.632 147.144-43.283 44.653-22.774 84.156-52.42 118.504-88.933 33.763-36.226 59.843-77.327 78.228-123.308 18.919-47.604 27.93-97.02 27.022-148.241a395.994 395.994 0 0 0-8.315-78.413 395.976 395.976 0 0 0-23.64-75.222 396.088 396.088 0 0 0-38.042-69.067 396.22 396.22 0 0 0-50.932-60.19c-38.045-36.448-81.362-64.84-129.963-85.177-48.78-20.38-99.603-30.43-152.472-30.145-60.462 0.637-113.466 11.747-162.36 33.04-48.362 20.773-91.085 49.84-128.166 87.199-36.809 37.446-65.416 80.36-85.81 128.745-21.133 50.016-31.238 102.17-30.321 156.46 0.89 50.935 11.151 99.909 30.784 146.914 18.518 44.377 43.901 84.4 76.149 120.067 14.347 16.086 23.142 33.676 26.383 52.479 4.454 25.692-6.827 43.399-30.84 56.65z" :fill="userpostList[index].isOpen ? '#409eff' : '#000000'"  p-id="10540"></path></svg>
              <span class="post-opreate-count"
              :style="{ color: userpostList[index].isOpen ? '#409eff' : 'inherit' }">{{ item.comments }}</span>
            </div>
            </el-col>
            <el-col :span="8" class="post-opreate-col">
              <div class="post-opreate-col-item" @click="toLike(index,item.id)">
              <svg  v-if="item.isLike" :key="'active-icon'" t="1744027672488" class="post-opreate-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="11973" width="200" height="200"><path d="M773.6 912.7h-1.2c-37.2-0.4-74.5-0.4-111.8-0.4h-56.9c-38 0-76 0-114.1-0.5-21.1-0.6-41.9-5-61.5-13-33.3-13-52.3-42-52.2-79.7l0.1-141.4c0-78.3 0-156.7 0.7-235 0.1-21.3 13.8-41.3 25.5-51.8 45.3-41.4 94.5-93 115.1-162.6 5.7-19.4 7.9-40.8 10.2-63.4 4.6-45 33.8-74.3 72.8-74.3 15.3 0 30.6 4.6 45.6 13.5 30.1 18.1 50.2 46.5 61.3 87 17.8 64.3 8.7 126.7-1.3 180.2v0.2c-2.3 12.5 7.1 24.1 19.9 24.1h130c22.4 0 54.5 2.8 74.3 26.8 14.4 17.5 18.5 41.1 12.4 72.3-18.6 95.9-41.4 192.6-63.2 282.7-6.8 28.1-18.1 54.1-29 79.3l-4.7 10.8c-12.4 29.2-38 45.2-72 45.2zM216.1 903.3h-11.9c-43 0-78.2-35.2-78.2-78.2V476.6c0-43 35.2-78.2 78.2-78.2h11.9c43 0 78.2 35.2 78.2 78.2V825c0.1 43.1-35.1 78.3-78.2 78.3z"  fill="rgb(18, 150, 219)" p-id="11974"></path></svg>
              <svg v-else :key="'inactive-icon'" t="1745834034399" class="post-opreate-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="11623" width="200" height="200"><path d="M411.904 153.728c19.797333-63.232 54.186667-90.24 122.026667-70.656l1.706666 0.554667c19.84 6.101333 42.666667 17.706667 64.085334 37.162666 33.706667 30.72 53.76 73.301333 53.76 126.805334 0 47.786667-2.773333 77.312-10.88 110.805333l-0.256 0.938667h175.488c107.264 0 149.888 72.362667 122.922666 192.682666l-2.304 9.856-5.461333 18.005334-20.608 67.114666-9.642667 30.677334-9.173333 28.672-17.066667 51.626666-11.648 33.621334-7.210666 20.053333-9.984 26.368-6.101334 15.232c-29.525333 71.253333-90.453333 103.978667-170.112 94.592l-387.114666-28.8a587.690667 587.690667 0 0 0-7.381334-0.341333l-15.36-0.341334H218.026667l-12.501334-0.213333-9.984-0.426667-8.32-0.768-3.712-0.554666-7.125333-1.408-11.52-3.029334c-59.349333-17.621333-90.24-67.925333-90.24-139.605333v-283.52c0-90.538667 54.954667-142.208 148.565333-142.208l75.776-0.042667 5.205334-3.968a293.632 293.632 0 0 0 72.234666-88.32l6.101334-11.946666c6.101333-12.544 11.093333-25.685333 15.829333-41.002667l0.768-2.602667z m88.661333 8.064c-1.834667-0.426667-2.645333 0.170667-3.541333 2.773333l-3.882667 14.933334-10.666666 38.442666-2.56 8.533334a366.933333 366.933333 0 0 1-20.565334 53.162666 387.754667 387.754667 0 0 1-72.618666 102.442667 333.141333 333.141333 0 0 1-49.28 42.026667l5.504-3.925334v417.408l336.682666 25.344c41.898667 4.906667 65.621333-6.101333 80.213334-36.096l2.858666-6.229333 5.76-14.378667 9.514667-25.173333 6.912-19.285333 11.221333-32.469334 8.064-24.064 17.365334-53.76 19.2-61.354666 15.445333-50.858667c18.986667-76.074667 7.808-94.592-38.357333-94.592h-217.685334a53.632 53.632 0 0 1-50.730666-71.125333l2.176-6.4 3.328-10.922667c10.282667-35.754667 13.226667-59.136 13.226666-108.629333 0-48.426667-26.88-72.96-57.045333-82.261334l-3.712-1.152z m-242.944 270.122667h-34.389333c-47.616 0-63.232 14.72-63.232 56.917333v283.52c0 38.016 9.941333 53.333333 33.792 59.008l1.493333 0.341333 3.754667 0.554667 5.12 0.426667 11.562667 0.256h28.586666l13.312 0.085333v-401.066667z"  p-id="11624"></path></svg>
              <span class="post-opreate-count">{{ item.likes }}</span>
            </div>
            </el-col>
          </el-row>


          <div  v-if="userpostList[index].isOpen">
          <div class="post-comments">
            <div class="video-comment-title1">
    <span class="comment-title-text">评论</span>
    <span class="comment-title-count">{{ item.comments }}</span>
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
</div>
          </div>
                 <!-- 输入评论 -->
<div class="video-comment-input">
  <el-avatar :src="userInfo.image"
   :size="40" 
   class="vc-avatar1" 
   ></el-avatar>
  <!-- From Uiverse.io by ercnersoy --> 
<input type="text" name="text" class="vc-input1" 
placeholder="你猜我的评论区在等待谁？"
v-model="commentInput"
    @keydown.space.prevent="handleSpace"
    @keydown.enter="giveCurrentVideoComment(item.id)" >
 </div>

 <!-- 评论操作 -->
  <div class="video-comment-operation1">
    <botton class="vc-at1" @click="AtSonDialogVisibleOn2">
      <svg t="1744279969705" class="vc-at-icon1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3548" width="200" height="200"><path d="M657.476791 626.845655c-5.884011 26.478048 1.471514 47.072085 22.065552 61.782112l26.478048 0c108.854197-50.01409 161.810293-148.57178 158.868287-295.672046-20.594037-158.868287-136.803759-247.128447-348.628142-264.780479-144.158261 8.826016-248.599961 72.079642-313.324078 189.759855-32.362059 61.782112-50.01409 129.448234-52.956096 202.998367 11.768021 164.752298 85.318154 276.5485 220.650399 335.388607 155.926282 47.072085 300.084543 16.181541 432.474782-92.673679l48.543599 52.956096c-111.796202 97.086176-239.773945 142.68777-383.932206 136.803759-161.810293-8.826016-279.490505-89.731674-353.040638-242.715951-32.362059-76.492138-45.601594-157.397796-39.717583-242.715951 11.768021-152.984277 91.202165-275.078009 238.302431-366.280174 79.434144-29.420053 150.042271-42.659589 211.824383-39.717583 170.636309 2.942005 294.200532 67.666122 370.69267 194.172351 29.420053 44.13008 42.659589 110.325711 39.717583 198.585871-20.594037 164.752298-97.086176 269.193998-229.476415 313.324078l-52.956096 0c-47.072085-14.710027-70.608128-41.188074-70.608128-79.434144l0-4.41352c-52.956096 55.898101-113.267716 80.905658-180.933839 75.021647-58.840106-8.826016-100.028181-38.981826-123.564223-90.466408s-25.007557-117.680213-4.41352-198.585871 66.195631-147.836023 136.803759-200.792119c79.434144-47.072085 155.926282-45.601594 229.476415 4.41352l26.478048 35.304064 13.239536-48.543599 88.26016 0L657.476791 626.845655zM339.739193 560.650023c-5.884011 61.782112 13.239536 100.028181 57.369615 114.738207l57.369615 4.41352c111.796202-47.072085 172.107823-147.100266 180.933839-300.084543-8.826016-44.13008-44.13008-67.666122-105.912192-70.608128C426.528861 329.703117 363.275235 413.549757 339.739193 560.650023z" p-id="3549"></path></svg>
    </botton>
    <el-upload
                class="avatar-uploader"
                action="/api/upload"
                :headers="{'token':token}"
                :show-file-list="false"
                :on-success="handleAvatarSuccessByComment"
                :before-upload="beforeAvatarUpload"
                >
    <botton class="vc-pic1">
      <svg t="1744280525370" class="vc-pic-icon1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6352" width="200" height="200"><path d="M937.472 102.4H91.136c-46.08 0-83.456 37.376-83.456 83.456V841.728c0 46.08 37.376 83.456 83.456 83.456h846.336c46.08 0 83.456-37.376 83.456-83.456V185.856c0-46.08-37.376-83.456-83.456-83.456zM91.136 163.84h846.336c12.288 0 22.016 9.728 22.016 22.016v437.248L762.88 448.512c-10.752-9.728-27.136-10.24-38.912-1.536l-244.224 183.296-129.536-92.16c-10.24-7.68-24.576-7.68-35.328 0l-245.76 174.08V185.856c0-12.288 9.728-22.016 22.016-22.016z m846.336 699.904H91.136c-12.288 0-22.016-9.728-22.016-22.016v-54.272L332.8 601.088l247.808 176.64c13.824 9.728 33.28 6.656 43.008-7.168 9.728-13.824 6.656-33.28-7.168-43.008l-84.48-59.904 208.896-156.672 211.456 187.904c2.048 2.048 4.608 3.072 7.168 4.608v138.24c0 12.288-9.728 22.016-22.016 22.016z" fill="#646464" p-id="6353"></path><path d="M312.32 471.04c70.656 0 128-57.344 128-128S382.976 215.04 312.32 215.04 184.32 272.384 184.32 343.04s57.344 128 128 128z m0-194.56c36.864 0 66.56 29.696 66.56 66.56s-29.696 66.56-66.56 66.56-66.56-29.696-66.56-66.56 29.696-66.56 66.56-66.56z" fill="#646464" p-id="6354"></path></svg>
    </botton>
    </el-upload>
    <el-button type="primary" class="vc-publish1"
     :disabled="commentInput === ''"
     @click="giveCurrentVideoComment(item.id)"
     >发布</el-button>
  </div>
  <!-- 评论列表 -->
  <div class="video-comments-list1" v-if="commentList.length > 0">

<div v-for="(comment, index) in commentList" :key="index">
  <div  class="video-comment-item1">
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

<div class="vc-list-info1">
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
<div class="comment-son-btn1"
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
         class="comment-son-item1">
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

<div class="vc-son-list-info1">
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
  </div>
  <div v-if="isToComment[index]">
    <div class="video-son-comment-input">
<el-avatar :src="userInfo.image"
:size="40" 
class="vc-son-avatar1" 
></el-avatar>
<!-- From Uiverse.io by ercnersoy --> 
<input type="text" name="text" class="vc-son-input1" 
placeholder="回复评论"
v-model="commentSonInput"
@keydown.space.prevent="handleSpace"
@keydown.enter="giveCurrentCommentComment(comment.id)">
</div>
<div class="video-son-comment-operation1">
  <botton class="vc-son-at1" @click="AtSonDialogVisibleOn3">
      <svg t="1744279969705" class="vc-son-at-icon1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3548" width="200" height="200"><path d="M657.476791 626.845655c-5.884011 26.478048 1.471514 47.072085 22.065552 61.782112l26.478048 0c108.854197-50.01409 161.810293-148.57178 158.868287-295.672046-20.594037-158.868287-136.803759-247.128447-348.628142-264.780479-144.158261 8.826016-248.599961 72.079642-313.324078 189.759855-32.362059 61.782112-50.01409 129.448234-52.956096 202.998367 11.768021 164.752298 85.318154 276.5485 220.650399 335.388607 155.926282 47.072085 300.084543 16.181541 432.474782-92.673679l48.543599 52.956096c-111.796202 97.086176-239.773945 142.68777-383.932206 136.803759-161.810293-8.826016-279.490505-89.731674-353.040638-242.715951-32.362059-76.492138-45.601594-157.397796-39.717583-242.715951 11.768021-152.984277 91.202165-275.078009 238.302431-366.280174 79.434144-29.420053 150.042271-42.659589 211.824383-39.717583 170.636309 2.942005 294.200532 67.666122 370.69267 194.172351 29.420053 44.13008 42.659589 110.325711 39.717583 198.585871-20.594037 164.752298-97.086176 269.193998-229.476415 313.324078l-52.956096 0c-47.072085-14.710027-70.608128-41.188074-70.608128-79.434144l0-4.41352c-52.956096 55.898101-113.267716 80.905658-180.933839 75.021647-58.840106-8.826016-100.028181-38.981826-123.564223-90.466408s-25.007557-117.680213-4.41352-198.585871 66.195631-147.836023 136.803759-200.792119c79.434144-47.072085 155.926282-45.601594 229.476415 4.41352l26.478048 35.304064 13.239536-48.543599 88.26016 0L657.476791 626.845655zM339.739193 560.650023c-5.884011 61.782112 13.239536 100.028181 57.369615 114.738207l57.369615 4.41352c111.796202-47.072085 172.107823-147.100266 180.933839-300.084543-8.826016-44.13008-44.13008-67.666122-105.912192-70.608128C426.528861 329.703117 363.275235 413.549757 339.739193 560.650023z" p-id="3549"></path></svg>
    </botton>
<el-upload
            class="vc-son-pic-ul"
            action="/api/upload"
            :headers="{'token':token}"
            :show-file-list="false"
            :on-success="handleAvatarSuccessByComment"
            :before-upload="beforeAvatarUpload"
            >
<botton class="vc-son-pic1">
  <svg t="1744280525370" class="vc-son-pic-icon1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6352" width="200" height="200"><path d="M937.472 102.4H91.136c-46.08 0-83.456 37.376-83.456 83.456V841.728c0 46.08 37.376 83.456 83.456 83.456h846.336c46.08 0 83.456-37.376 83.456-83.456V185.856c0-46.08-37.376-83.456-83.456-83.456zM91.136 163.84h846.336c12.288 0 22.016 9.728 22.016 22.016v437.248L762.88 448.512c-10.752-9.728-27.136-10.24-38.912-1.536l-244.224 183.296-129.536-92.16c-10.24-7.68-24.576-7.68-35.328 0l-245.76 174.08V185.856c0-12.288 9.728-22.016 22.016-22.016z m846.336 699.904H91.136c-12.288 0-22.016-9.728-22.016-22.016v-54.272L332.8 601.088l247.808 176.64c13.824 9.728 33.28 6.656 43.008-7.168 9.728-13.824 6.656-33.28-7.168-43.008l-84.48-59.904 208.896-156.672 211.456 187.904c2.048 2.048 4.608 3.072 7.168 4.608v138.24c0 12.288-9.728 22.016-22.016 22.016z" fill="#646464" p-id="6353"></path><path d="M312.32 471.04c70.656 0 128-57.344 128-128S382.976 215.04 312.32 215.04 184.32 272.384 184.32 343.04s57.344 128 128 128z m0-194.56c36.864 0 66.56 29.696 66.56 66.56s-29.696 66.56-66.56 66.56-66.56-29.696-66.56-66.56 29.696-66.56 66.56-66.56z" fill="#646464" p-id="6354"></path></svg>
</botton>
</el-upload>
<el-button type="primary" class="vc-son-publish1"
 :disabled="commentSonInput === ''"
 @click="giveCurrentCommentComment(comment.id)"
 >回复</el-button>
</div>
</div>
<el-divider class="vc-divider21"/>
</div>

</div>
<div v-else class="no-more-comment1">
<span>————没有更多评论————</span>
</div>
        </div>
    </div>
  </div>
      <div v-else class="post-center-content">
        <el-empty description="这里空空如也……" />
      </div>
  </div>

  <div class="post-right-top">
      <img  class="post-right-top-img" src="YilenaPic/25/04/27-1dd1f61f056746b8b39e0eef47687706.avif">
      <el-affix :offset="75">
      <img  class="post-right-top-img" src="YilenaPic/25/04/27-6461393d97f04f4bb943854e9c82ccc9.jpg">
    </el-affix>
  </div>

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
        @click="AtReslut">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- @对话框 -->
<el-dialog
    v-model="AtDialogVisible2"
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
        @click="AtReslut2">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- @对话框 -->
<el-dialog
    v-model="AtDialogVisible3"
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
        @click="AtReslut3">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.video-comment-input{
  position: relative;
  margin-bottom: 40px;
}

.post-video-views{
  font-size: 14px;
}

.post-video-barrages{
  font-size: 14px;
  margin-left: 5px;
}

.post-video-icon1{
  width: 20px;
  margin-top: -89px;
}

.post-video-icon2{
  width: 14px;	
  margin-top: -89px;
  margin-left: 35px;
}

.post-video-info{
  position: relative;
  top:-150px;
  color: rgb(154, 158, 165);
  fill:rgb(154, 158, 165);
  margin-left: 251px;
  margin-top: 35px;
}

.post-video-time{
  position: absolute;
  color: rgb(255, 255, 255);
  top: 163px;
  left: 222px;
  font-size: 14px;
  font-weight: 500;
  background-color: #00000054;
  border-radius: 5px;
}

.post-video-description{
  /* 基础布局 */
  font-size: 13px;
  position: relative;
  margin-left: 255px;
  top: -118px;
  color: rgb(154, 158, 165);
  
  /* 容器尺寸 */
  width: 300px;
  max-width: 100%;
  min-height: 40px; /* 替代原有 height，防止内容被裁剪 */
  
  /* 文本行为 */
  white-space: normal; /* 允许换行 */
  word-break: break-all; /* 强制长字符断裂 */
  
  /* 核心：多行截断配置 */
  display: -webkit-box;          /* 启用 WebKit 盒模型 */
  -webkit-box-orient: vertical;  /* 内容垂直排列 */
  -webkit-line-clamp: 2;         /* 最多显示 3 行 */
  line-clamp: 2; /* 标准属性 */
  overflow: hidden;              /* 隐藏溢出内容 */
}

.post-video-title {
  /* 基础布局 */
  font-size: 15px;
  position: relative;
  margin-left: 255px;
  top: -125px;
  
  /* 容器尺寸 */
  width: 300px;
  max-width: 100%;
  min-height: 40px; /* 替代原有 height，防止内容被裁剪 */
  
  /* 文本行为 */
  white-space: normal; /* 允许换行 */
  word-break: break-all; /* 强制长字符断裂 */
  
  /* 核心：多行截断配置 */
  display: -webkit-box;          /* 启用 WebKit 盒模型 */
  -webkit-box-orient: vertical;  /* 内容垂直排列 */
  -webkit-line-clamp: 2;         /* 最多显示 3 行 */
  line-clamp: 2; /* 标准属性 */
  overflow: hidden;              /* 隐藏溢出内容 */
}

.post-video-cover{
  height: 135px;
  border-radius: 5px 0px 0px 5px;
}

.post-video-show-item{
  margin-top: 10px;
  margin-left: 23px;
  height: 135px;
  width: 575px;
  border-radius: 5px;
  background-color: rgb(255, 255, 255);
}

.post-video-username{
  margin-left: 10px;
  margin-top: 18px;
  color: rgb(97, 102, 109);
}

.post-video-avatar{
  margin-left: 23px;
  margin-top: 20px;
}

.post-video-show{
  background-color: rgb(246, 247, 248);
  width: 620px;
  height: 205px;
  margin-top: 20px;
  margin-left: 80px;
  position: relative;
  border-radius: 5px;
}

.post-loading{
  position: absolute;
  top: 500px;
  left: 350px;
}

/* From Uiverse.io by rillala */ 
.spinner {
  position: absolute;
  width: 25px;
  height: 40px;
}

.spinner div {
  position: absolute;
  width: 50%;
  height: 150%;
  background: transparent;
  transform: rotate(calc(var(--rotation) * 1deg))
    translate(0, calc(var(--translation) * 1%));
  animation: spinner-fzua35 1s calc(var(--delay) * 1s) infinite ease;
  color: #3eb3c2;
  text-align: end;
  font-weight: 700;
  font-size: 20px;
}

.spinner div:nth-child(1) {
  --delay: 0.1;
  --rotation: 36;
  --translation: 150;
}

.spinner .middle {
  position: relative;
  background: transparent;
  top: 15px;
  left: -30px;
  color: #f09f9f;
}

.spinner div:nth-child(2) {
  --delay: 0.2;
  --rotation: 72;
  --translation: 150;
}

.spinner div:nth-child(3) {
  --delay: 0.3;
  --rotation: 108;
  --translation: 150;
}

.spinner div:nth-child(4) {
  --delay: 0.4;
  --rotation: 144;
  --translation: 150;
}

.spinner div:nth-child(5) {
  --delay: 0.5;
  --rotation: 180;
  --translation: 150;
}

.spinner div:nth-child(6) {
  --delay: 0.6;
  --rotation: 216;
  --translation: 150;
}

.spinner div:nth-child(7) {
  --delay: 0.7;
  --rotation: 252;
  --translation: 150;
}

.spinner div:nth-child(8) {
  --delay: 0.8;
  --rotation: 288;
  --translation: 150;
}

.spinner div:nth-child(9) {
  --delay: 0.9;
  --rotation: 324;
  --translation: 150;
}

.spinner div:nth-child(10) {
  --delay: 1;
  --rotation: 360;
  --translation: 150;
}

div.icon-1,
div.icon-2 {
  background: transparent;
}

.icon-1 svg {
  transform: rotate(180deg);
  position: relative;
  left: -15px;
}

.icon-2 svg {
  position: relative;
  left: -15px;
}

@keyframes spinner-fzua35 {
  0%,
  10%,
  20%,
  30%,
  50%,
  60%,
  70%,
  80%,
  90%,
  100% {
    transform: rotate(calc(var(--rotation) * 1deg))
      translate(0, calc(var(--translation) * 1%));
  }

  50% {
    transform: rotate(calc(var(--rotation) * 1deg))
      translate(0, calc(var(--translation) * 1.5%));
  }
}


.post-item-title{
  position: relative;
  margin-left: 80px;
  font-size: 15px;
  font-weight: 500;
}

.video-son-comment-operation1{
  position: relative;
  top:-110px;
  width: 833px;
  margin-left: 80px;
}

.vc-son-at1{
  width: 20px !important;
  height: 20px !important;
  position: relative;
  margin-left: 60px;
  top:80px;
  background-color: transparent !important;
  border-radius: 2px;
}

.vc-son-at-icon1{
  width: 15px !important;
  height: 20px !important;
  fill: rgb(117, 121, 127);
}

.vc-son-pic1{
  width: 20px;
  height: 20px;
  margin-left: 90px;
  position: relative;
  top: 79px;
}

.vc-son-pic-icon1{
  width: 15px;
  height: 20px;
  fill: rgb(117, 121, 127);
}

.vc-son-publish1{
  position: relative;
  top:55px;
  margin-left: 512px;
  width: 80px;
}


.vc-son-avatar1{
 position: relative;
 top:10px;
 margin-left: 85px;
 border: #409eff 2px solid;
}

.vc-son-input1{
  position: relative;
  width: 505px;
  top:-35px;
  margin-left: 135px;
 border: none;
 padding: 1rem;
 border-radius: 5px;
 background: #e8e8e8;
 z-index: 20;
}

.vc-son-input1:focus {
 outline-color: #e8e8e8;
 background: #e8e8e8;
 box-shadow: inset 20px 20px 60px #c5c5c5,
		inset -20px -20px 60px #ffffff;
 transition: 0.3s;
}

.vc-son-list-info1{
  position: relative;
  margin-left: 35px;
  margin-top: 5px;
  top: 0px;
  font-size: 13px;
  color: rgb(157, 161, 168);
}

.comment-son-item1{
  position: relative;
  margin-left: 80px;
  top:0px;
  margin-top: 10px;
}


.comment-son-btn1{
  position: relative;
  top: 0px;
  font-size: 13px;
  margin-left: 80px;
  color: rgb(157, 161, 168);
  z-index: 20;
}

.comment-son-btn1:hover {
  color: #0084ff;
}

.vc-divider21{
  width: 600px !important;
  position: relative;
  top: -20px!important;
  margin-left: 80px!important;
}

.vc-list-info1{
  position: relative;
  margin-left: 80px;
  margin-top: 5px;
  top: 0px;
  font-size: 13px;
  color: rgb(157, 161, 168);
}

.video-comment-item1{
  position: relative;
  margin-left: 15px;
  margin-top: -65px;
}

.no-more-comment1{
  position: relative;
  margin-top: 40px;
  margin-left: 280px;
  height: 50px;
  font-size: 13px;
  color: rgb(157, 161, 168);
}

.vc-at1{
  position: absolute;
  top: -10px;
  left: 100px;
  width: 20px;
  height: 20px;
  background-color: white;
  border: rgb(241, 242, 243) solid 2px;
  border-radius: 2px;
}

.vc-at-icon1{
  width: 15px;
  height: 20px;
  margin-top: 0px;
  margin-left: 3px;
  fill: rgb(117, 121, 127);
}

.vc-pic1{
  position: absolute;
  top: -10px;
  left: 130px;
  width: 20px;
  height: 20px;
  background-color: white;
  border: rgb(241, 242, 243) solid 2px;
  border-radius: 2px;
}

.vc-pic-icon1{
  width: 15px;
  height: 20px;
  margin-top: 0px;
  margin-left: 3px;
  fill: rgb(117, 121, 127);
}

.vc-publish1{
  position: relative;
  top:-30px;
  left: 610px;
  width: 80px;
}

.vc-avatar1{
 position: relative;
 top: 30px;
 margin-left: 40px;
 border: #409eff 2px solid;
}

.video-comment-operation1{
  height: 40px;
  position: relative;
  top: -40px;
  bottom: 0px;
}

.vc-input1{
  position: relative;
  width: 560px;
  top:-15px;
  margin-left: 100px;
 border: none;
 padding: 1rem;
 border-radius: 5px;
 background: #e8e8e8;
}

.vc-input1:focus {
 outline-color: #e8e8e8;
 background: #e8e8e8;
 box-shadow: inset 20px 20px 60px #c5c5c5,
		inset -20px -20px 60px #ffffff;
 transition: 0.3s;
}

.post-comments{
  margin-top: 5px;
}

.video-comment-title1{
  display: flex;
  margin-left: 25px;
}

.post-opreate-col-item:focus{
  color: rgb(0, 138, 197);
}

.post-opreate-col{
  text-align: center;
  justify-content: center;
  display: flex;
}

.post-opreate-row{
  margin-top: 10px;
  justify-content: space-between;
  height: 40px;
}

.post-opreate-count{
  color: rgb(97, 102, 109);
  position: relative;
  top: -2px;
  margin-left: 5px;
  font-size: 13px;
}

.post-opreate-icon{
  width: 15px;
  height: 15px;
  margin-top: 10px;
  fill:rgb(97, 102, 109)
}

.post-img-row{
  margin-left: 82px;
  display: flex;
}

.post-item-tag-row{
  margin-left: 83px;
}

.post-item-tag{
  font-size: 14px;
  color: rgb(0, 138, 197);
  margin-top: 15px;
}

.post-item-content{
  position: relative;
  top: 10px;
  margin-left: 80px;
  font-size: 14px;
}

.post-item-time{
  font-size: 12px;
  color: rgb(148, 153, 160);
  position: absolute;
  margin-top: 40px;
  margin-left: 15px;
}

.post-item-username{
  font-weight: 600;
  position: absolute;
  margin-top: 15px;
  margin-left: 15px;
}

.post-item-avatar{
  margin-top: 12px;
  margin-left: 15px;
}

.post-center-content{
  margin-top: 5px;
  background-color: #ffffff;
  border-radius: 5px;
}

.post-icon-cha{
  margin-left: -6px;
  margin-top: -1px;
}

.scroll {
  width:665px;
  margin-left: -10px;
  position: relative;
  top: -10px;
}

.followingAccountAvatar{
  width: 100%;
  height: 120px;
  background-color: rgb(255, 255, 255);
  border-radius: 5px;
  margin-top: 5px;
}

.post-img-show-item-delete-btn{
  position: relative;
  width: 15px;
  height: 15px;
}

.post-img-show{
  display: flex;
  flex-direction: row;
  margin-top: 5px;
}

.post-img-show-item{
  max-width: 200px;
  max-height: 100px;
  margin-right: 5px;
}

.post-right-top-img{
  width: 320px;
  border-radius: 5px;
  margin-bottom: 5px;
}

.post-titleInput{
  margin-top: 10px;
  margin-left: 15px;
  font-size: 14px;
  border: none;
}

.post-input-right{
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  width: 120px;
  margin-left: 510px;
}

.post-input-divider{
  margin-top: 13px;
}

.text-count{
  margin-top: 10px;
  font-size: 14px;
  color: rgb(106, 117, 123);
  text-align: right;
}

.vc-son-pic-post{
  margin-top: 33.5px;
  margin-left: 20px;
}

.vc-son-at-post{
  margin-top: 10px;
  margin-left: 5px;
}

.vc-son-pic-icon-post{
  width: 20px;
  height: 20px;
  fill: rgb(148, 153, 160);
}

.vc-son-at-icon-post{
  width: 20px;
  height: 20px;
  fill: rgb(148, 153, 160);
}

.post-center-top-publish-btn{
  margin-top: 5px;
}

.post-center-top-imgAndAt{
  display: flex;
  height: 50px;
  width: 100%;
  margin-left: 15px;
  margin-top: 5px
}

.post-center-top-input-textarea{
  margin-top: 10px;
  margin-left: 15px;
  min-width: 280px !important;
  max-width: 695px !important;
  border: none;
}

.post-center-top-input-tag{
  position: relative;
  top:5px;
  margin-left: 15px;
  min-width: 280px !important;
  max-width: 695px !important;
}

.post-center-top-input{
  width: 100%;
  background-color: rgb(255, 255, 255);
  border-radius: 5px;
}

.post-left-top-live{
  margin-top: 8px;
}

.post-left-top-live-img{
  width: 265px;
}

.post-left-top-userInfo-line{
  display: flex;
  margin-left: 10px;
}

.post-left-top-userInfo-report {
  display: flex;
  flex-direction: column;     /* 垂直排列子元素 */
  align-items: center;        /* 水平居中 */
  justify-content: center;    /* 垂直居中 */
  height: 60px;              /* 给容器一个明确高度 */
  line-height: 1;            /* 重置行高避免间距问题 */
  margin-right: 30px;
}

/* 可选：如果希望容器宽度自适应内容 */
.post-left-top-userInfo-report {
  min-width: 60px;
  padding: 8px 0;
}

/* 调整文字间距 */
.post-left-top-userInfo-report-text {
  margin-top: 4px;           /* 文字与数字的间隔 */
  font-size: 12px;
  color: rgb(151, 155, 160);
  font-weight: 500;
}

.post-left-top-userInfo-report-count{
  display: block;
  font-size: 16px;
  font-weight: 600;
}

.post-left-top-userInfo-name{
  font-weight: 500;
  margin-left: 10px;
  position: relative;
  top: 10px;
}

.post-left-top-userInfo-name:hover{
  color: rgb(64, 197, 241);
}

.avatar-info {
  display: inline-block;  /* 改为块级格式化上下文 */
  vertical-align: middle; /* 关键：垂直对齐方式 */
  margin-top: 20px;
  margin-left: 15px;
  width: 40px;
  height: 40px;
}

.post-left-top-userInfo{
  height: 145px;
  width: 100%;
  background-color: rgb(255, 255, 255);
  border-radius: 5px;
}

.post-right-top{
  margin-top: 10px;
  margin-left: 1185px;
  height: 760px;
  width: 320px;
  position: absolute;
  z-index: 10 !important;
}

.post-center-top{
  margin-top: 10px;
  margin-left: 450px;
  height: 760px;/*760*/
  width: 725px;
  position: absolute;
  z-index: 10 !important;
}

.post-left-top{
  margin-top: 10px;
  margin-left: 175px;
  height: 700px;
  width: 265px;
  position: absolute;
  z-index: 10 !important;
}

.post-top-bar{
  z-index: 10 !important;
  height: 70px;
  box-shadow: 0 0 0 0.05cm rgba(0, 0, 0, 0.05);
}

.post-bg-img{
  width: 1700px;
  z-index: -10 !important;
}

.post-bg{
  position: absolute;
  top:-115px
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

post-opreate-icon {
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
  background-color: transparent;
  margin-top: .1em;
}

.search__button:hover {
  cursor: pointer;
}

.search__icon {
  height: 1.3em;
  width: 1.3em;
  fill: #000000;
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

.botton-right{
  display: flex;
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

.top-logo-search{
  position: absolute;
  width: 100px;
  height: 50px;
  top: 9px;
  left: 20px;
}

.top-divider{
  width: 100%;
  height: 10;
  position: absolute;
  top: -103px;
}
</style>