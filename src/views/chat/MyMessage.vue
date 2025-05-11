<script setup>
import { ref,onMounted,computed,watch,nextTick,onUnmounted } from 'vue'
import { useRouter } from 'vue-router';
import { useTransition } from '@vueuse/core'
import CryptoJS from 'crypto-js'
import { useInfiniteScroll } from '@vueuse/core'
import { ElMessage,ElMessageBox,ElNotification  } from 'element-plus';
import { getHotSearch,findAllVideos,getUserInfoByUsername } from '../../api';
import { getRecommendVideo,getUserFollow,giveComment,getVideoComments,getAuthorIsFollow,giveAuthorFollow,giveAuthorUnfollow,getDanmuEach5s,getAuthorInfo,getVideo,giveToFavorites,giveToUnFavorites,getDanmuCount,getFavorite,like,dislike,getIsLike,getIsCoins,giveCoins, getUserFavoritesList } from '../../api/video';
import UserScroll from '../post/userScroll.vue'
import { getUserFollowingPosts,addPost,getAnyonePosts } from '../../api/post';
import { getUserFollowingUsers,getChatHistory,sendMessage,setMessageRead,deleteMessage } from '../../api/chat';

const count = ref(10)

const clickMe = () => {
  console.log('点击了按钮'); // 调试日志
}

const selectedItem = ref(null)
const selectedUsername = ref('')
const selectedImage = ref('')

const clickUser = async(id,name,image) => {
  console.log('Selected item:', id);
  selectedItem.value = id;
  selectedUsername.value = name;
  selectedImage.value = image;
  currentChatHistory.value = userList.value.find(item => item.id === id).chatHistory;
  await setCurrentMessageRead(id);

  await getUserList();
  currentChatHistory.value = await getCurrentChatHistory(selectedItem.value);
}

const readEntity = ref({
  currentUserId: '',
  targetUserId: ''
})

const setCurrentMessageRead = async(id) => {
  readEntity.value.currentUserId = currentUserId.value;
  readEntity.value.targetUserId = id;
  const result = await setMessageRead(readEntity.value);
  if(result.code === 1){
    console.log('已设置为已读')
  }else{
    ElMessage.error(result.message)
  }
}

const load = () => {
  count.value += 2
}

const imageUrl = ref('');

const handleAvatarSuccessByComment = (response,uploadFile) => {
  console.log(response.data)
  imageUrl.value = response.data;
  ElMessage.success('上传成功');
}

// 文件上传之前触发
const beforeAvatarUpload = (rawFile) => {
  if(imageUrl.value.length >= 1){
    ElMessage.error('最多发送1张图片')
    return false
  }
  ElMessage.info('请等待“上传成功”提示……')
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('只支持上传 JPG/PNG/GIF 格式图片')
    return false
  }
  
  if (rawFile.size / 1024 / 1024 > 10) {
    ElMessage.error('只能上传10M以内图片')
    return false
  }
  return true
}

const message = ref('');

const userList = ref([{
  chatHistory: [],
}]);

const getUserList = async () => {
  const result = await getUserFollowingUsers(userInfo.value.id);
  if(result.code === 1){
    userList.value = result.data;
    console.log(userList.value)
    for(let i=0;i<userList.value.length;i++){
      const chatHistory = await getCurrentChatHistory(userList.value[i].id);
      userList.value[i].chatHistory = chatHistory;
    }
  }
}

const getLastMessageContent = (item) => {
  // 检查是否有聊天记录且是数组
  if (!item.chatHistory || !Array.isArray(item.chatHistory)) {
    return '……'
  }
  
  // 获取最后一条消息
  let lastMsg = item.chatHistory[0];
  
  console.log(lastMsg)
  // 检查消息内容
  if (!lastMsg) return ' '
  if(lastMsg.type == 8 && lastMsg.sendUserId != currentUserId.value){
      return '发布了新视频'
  }else{
    lastMsg = item.chatHistory[1]
    if(lastMsg.type == 8 && lastMsg.sendUserId != currentUserId.value){
      return '发布了新视频'
    }
  }
  if (lastMsg.imageUrl) return '[图片]'
  if (lastMsg.content) return lastMsg.content.length > 10 
    ? lastMsg.content.substring(0, 10) + '...' 
    : lastMsg.content
    
  return '暂无消息'
}

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

const token = ref('');

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

const currentChatHistory = ref([]); // 当前聊天记录

  const getCurrentChatHistory = async (id) => {
    const result = await getChatHistory(currentUserId.value,id);
    if(result.code === 1){
      console.log('聊天记录：',result.data)
      return result.data;
    }
  }

  // 处理聊天记录时添加时间标记
const processedChatHistory = computed(() => {
  return currentChatHistory.value.map((item, index) => ({
    ...item,
    showTime: shouldShowTime(item.createdTime, index)
  }))
})

// 在消息更新时保持滚动位置
watch(processedChatHistory, () => {
  nextTick(() => {
    const container = document.querySelector('.chat-infinite-list')
    if (container) {
      // 反向滚动布局需要特殊处理
      container.scrollTop = container.scrollHeight - container.clientHeight
    }
  })
}, { deep: true })

  const shouldShowTime = (currentTime, currentIndex) => {
  if (currentIndex === currentChatHistory.value.length) return true // 第一条消息总是显示时间
  
  const prevItem = currentChatHistory.value[currentIndex + 1]
  if (!prevItem) return true
  
  // 精确计算时间差（分钟）
  const current = new Date(currentTime).getTime()
  const prev = new Date(prevItem.createdTime).getTime()
  const diffInMinutes = (current - prev) / (1000 * 60)
  
  return diffInMinutes > 5 // 超过5分钟才显示
}

const formatTime = (timestamp) => {
  const now = new Date();
  const date = new Date(timestamp);
  
  // 获取日期的年、月、日部分，忽略时间
  const getDatePart = (d) => new Date(d.getFullYear(), d.getMonth(), d.getDate());
  
  const nowDate = getDatePart(now);
  const targetDate = getDatePart(date);
  const diffInDays = Math.floor((nowDate - targetDate) / (1000 * 60 * 60 * 24));
  
  // 当天的时间格式
  if (diffInDays === 0) {
    return date.toLocaleTimeString([], { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  }
  
  // 昨天
  if (diffInDays === 1) {
    return `昨天 ${date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
  }
  
  // 前天
  if (diffInDays === 2) {
    return `前天 ${date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
  }
  
  // 今年内（超过两天但不超过一年）
  if (date.getFullYear() === now.getFullYear()) {
    return `${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
  }
  
  // 超过一年
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
}
const chatListRef = ref(null)

// 初始化时滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatListRef.value) {
      chatListRef.value.scrollTop = chatListRef.value.scrollHeight
    }
  })
}

const getCurrentUserIsFollow = async (id) => {
  const result = await getAuthorIsFollow(id,currentUserId.value);
  if(result.code === 1){
    if(result.data){
      return true;
    }else{
      return false;
    }
  }else{
    ElMessage.error(result.message)
  }
}



const messageEntity = ref({
  content: '',
  imageUrl: '',
  type: '',
  sendUserId: '',
  receiveUserId: ''
});

const sendMessageToServer = async () => {
  if(selectedItem.value == null){
    ElMessage.error('请选择用户！')
    return;
  }
  if(await getCurrentUserIsFollow(selectedItem.value) == false){
    ElMessage.error('只有互关用户才能聊天哦！')
    return;	
  }
  if(message.value.trim() === '' && imageUrl.value === ''){
    ElMessage.error('请输入内容！')
    return;
  }
  if(message.length > 500){
   ElMessage.error('内容不能超过500字！')
   return;
  }
  if(imageUrl.value != ''){
    messageEntity.value.imageUrl = imageUrl.value;
    messageEntity.value.type = 2;
  }else{
    messageEntity.value.type = 1;
  }
  messageEntity.value.content = message.value;
  messageEntity.value.sendUserId = currentUserId.value;
  messageEntity.value.receiveUserId = selectedItem.value;
  const result = await sendMessage(messageEntity.value);
  if(result.code === 1){
    ElMessage.success('发送成功！')
    message.value = '';
    imageUrl.value = '';
    await getUserList();
    currentChatHistory.value = await getCurrentChatHistory(selectedItem.value);
  }else{
    ElMessage.error(result.message)
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

// WebSocket连接对象
let ws = null;
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

	ws.onmessage = async(event) => {
		// 只要有数据，那就说明连接正常
		ws_heartCheck.reset().start();

    try {
        const data = JSON.parse(event.data)
        console.log('接收到信息：',data,new Date());
        if(data.status != 3){
          msgCome();
        }
          await getUserList();
          currentChatHistory.value = await getCurrentChatHistory(selectedItem.value);
        if(data.sendUserId == selectedItem.value && data.status != 3){
          console.log('是当前用户!!!!!已设置为已读！！！！！')
          await setCurrentMessageRead(data.sendUserId);
          await getUserList();
          currentChatHistory.value = await getCurrentChatHistory(selectedItem.value);
        }
    } catch(e) {
        console.error('解析错误', e)
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

const msgCome = () => {
  ElNotification({
    title: '私信',
    message: '您有新的消息',
    type: 'warning',
  })
}

const praseJson = (str) => {
  try {
    return JSON.parse(str);
  } catch (e) {
    return str;
  }
}

function formatTime1(createdTime) {
  const now = new Date();
  const targetTime = new Date(createdTime);
  const diff = now - targetTime;
  const diffDays = Math.floor(diff / (1000 * 3600 * 24));

  if (diff < 86400000) {
    const hours = Math.floor(diff / (1000 * 3600));
    if(hours <= 0){
      return '刚刚'
    }
    else{
    return `${hours}小时前`;
    }
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

function formatViews(views) {
  if (views >= 10000) {
    return `${Math.round(views / 1000) / 10}万`; // 保留1位小数（如：1.3万）
  }
  return views.toString();
}

const delMsg = async (id) => {
  const result = await deleteMessage(id,1);
  if(result.code === 1){
    ElMessage.success('删除成功！')
    await getUserList();
    currentChatHistory.value = await getCurrentChatHistory(selectedItem.value);
  }else{
    ElMessage.error(result.msg)
  }
}

const chMsg = async (id) => {
  const result = await deleteMessage(id,2);
  if(result.code === 1){
    ElMessage.success('撤回成功！')
    await getUserList();
    currentChatHistory.value = await getCurrentChatHistory(selectedItem.value);
  }else{
    ElMessage.error(result.msg)
  }
}

onMounted(async() => {
  await getUserInfo();
  await getUserList();
   //clickUser(userList.value[0].id,userList.value[0].username,userList.value[0].image);  
   scrollToBottom();
   ws_create(`ws://localhost:8100/YVW/chat/${currentUserId.value}`);
  })


onUnmounted(() => {
  if (ws) ws.close();
  clearInterval(intervalId);
});
</script>

<template>
    <el-row>
    <div class="chat-left-title"><span class="chat-left-title-text">近期消息</span></div>
    <div class="chat-right-title"><span class="chat-right-title-text">{{ selectedUsername }}</span></div>
</el-row>
<div class="infinite-list-wrapper" style="overflow: auto" v-if="userList.length > 0">
    <ul
      v-infinite-scroll="load"
      class="list"
      :infinite-scroll-disabled="disabled"
    >
    <li
      v-for="(item,index) in userList"
      :key="item.id"
      :class="{ 'selected': selectedItem == item.id }"
      :style="{ backgroundColor: selectedItem === item.id ? '#f0f0f0' : 'white' }"
      @click="clickUser(item.id,item.username,item.image)"
      class="list-item"
    >
        <el-avatar :size="40" :src="item.image" class="chat-avatar"></el-avatar>
        <div class="chat-userInfo-container">
        <el-row class="chat-username-row"><span class="chat-username">{{ item.username }}</span></el-row>
        <el-row class="chat-message-row"><span class="chat-message" :key="index">
         {{ getLastMessageContent(item)}}
        </span></el-row>
      </div>
      <div class="unReadCount" v-if="item.unReadMessageCount > 0">&nbsp;&nbsp;{{ item.unReadMessageCount }}&nbsp;&nbsp;</div>
      </li>
    </ul>
  </div>

  <div class="chat-user-content">
    <ul 
    ref="chatListRef" 
    v-infinite-scroll="load" 
    class="chat-infinite-list" 
    style="display: flex; flex-direction: column-reverse; overflow: auto;"
    >
    <li v-for="(item,index) in processedChatHistory" :key="item.id" class="chat-infinite-list-item">
      <el-row 
          class="chat-message-time-row" 
          v-if="item.showTime == true" 
        >
          <span class="chat-message-time" v-if="item.type != 8">{{ formatTime(item.createdTime) }}</span>
        </el-row>
      <el-row class="chat-other" v-if="item.sendUserId != currentUserId">
        <div class="chat-other-message" v-if="item.type != 8">
        <el-avatar :size="28" :src="selectedImage" class="chat-other-avatar"></el-avatar>
        <div class="chat-other-message-content">
        <span class="chat-other-message-content-text" v-if="item.content != null">
          {{ item.content }}
        </span>
        <el-row style="width: 100%;"><img v-if="item.imageUrl != null" :src="item.imageUrl" class="chat-message-content-image"/></el-row>
        <div class="isRead-other" v-if="item.status == 1"><span style="color: rgb(153, 153, 165);">已读</span></div>
        <div class="isRead-other" v-else>未读</div>
        <el-popover
        class="msg-other-more"
        placement="bottom"
    trigger="hover"
    popper-class="custom-popover" 
      >
      <template #default>
    <el-button size="small" @click="delMsg(item.id)">删除</el-button>
    </template>
        <template #reference> 
          <div class="msg-more">
          <svg t="1746255036424" class="msg-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3547" width="200" height="200"><path d="M544.95418201 707.164625a46.545455 46.545455 0 0 1-65.90836402 0L84.154182 312.27298901a46.545455 46.545455 0 0 1 65.815273-65.90836401L512 608.48826201 874.123636 246.364625A46.545455 46.545455 0 0 1 939.752727 312.272989l-394.891636 394.891636z"  p-id="3548"></path></svg>
        </div>
        </template>
      </el-popover>
        
      </div>
    </div>
    <div class="chat-other-videoShow" v-else-if="item.type == 8">
      <img :src="praseJson(item.content).coverUrl" class="chat-message-content-video-image"/>
      <span class="chat-message-content-video-text">{{ praseJson(item.content).title }}</span>
      <span class="chat-message-content-video-time">{{ formatTime1(praseJson(item.content).createdTime) }}</span>
      <svg t="1743757905181" class="chat-message-content-video-icon" viewBox="0 0 1303 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29556" width="200" height="200"><path d="M110.964364 1023.720727c-60.509091 0-109.847273-48.872727-109.847273-108.916363V109.009455C1.117091 48.965818 50.455273 0 110.964364 0h1081.902545c60.509091 0 109.847273 48.872727 109.847273 108.916364v805.794909c0 60.043636-49.338182 108.916364-109.847273 108.916363H110.964364z m0-927.650909c-6.609455 0-12.101818 5.771636-12.101819 12.939637v805.794909c0 7.168 5.492364 13.032727 12.101819 13.032727h1081.902545c6.609455 0 12.101818-5.864727 12.101818-13.032727V109.009455c0-7.168-5.492364-13.032727-12.101818-13.032728H110.964364z" p-id="29557"></path><path d="M1003.054545 520.098909a51.572364 51.572364 0 0 1-50.26909 52.689455h-80.058182a51.572364 51.572364 0 0 1-50.269091-52.689455c0-29.137455 22.434909-52.782545 50.269091-52.782545h80.058182c27.834182 0 50.269091 23.645091 50.26909 52.782545zM1096.610909 270.149818c0 28.858182-22.341818 52.410182-49.989818 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273h814.08c27.648 0 50.082909 23.458909 50.082909 52.503273zM768.744727 520.098909c0 28.858182-22.341818 52.410182-50.082909 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273H718.661818c27.648 0 50.082909 23.458909 50.082909 52.503273zM1096.610909 735.976727c0 29.044364-22.341818 52.503273-49.989818 52.503273H560.500364a51.293091 51.293091 0 0 1-50.082909-52.503273c0-28.951273 22.341818-52.410182 50.082909-52.410182h486.120727c27.648 0 50.082909 23.458909 50.082909 52.410182z"  p-id="29558"></path></svg>
      <span class="chat-message-content-video-time-barrages">{{ formatViews(praseJson(item.content).barrages) }}</span>
    </div>
      </el-row>
      <el-row class="chat-mine" v-else v-if="item.type != 8">
        <div class="chat-mine-message-content">
          <span class="chat-mine-message-content-text" v-if="item.content != null">
          {{ item.content }}
        </span>
        <el-row style="width: 100%;"><img v-if="item.imageUrl != null" :src="item.imageUrl" class="chat-message-content-image"/></el-row>
        <div class="isRead-mine" v-if="item.status == 1"><span style="color: rgb(153, 153, 165);">已读</span></div>
        <div class="isRead-mine" v-else>未读</div>
        <el-popover
        class="msg-other-more"
        placement="bottom"
    trigger="hover"
    popper-class="custom-popover-mine" 
      >
      <template #default>
    <el-button size="small" @click="delMsg(item.id)">删除</el-button>
    <el-button size="small" @click="chMsg(item.id)">撤回</el-button>
    </template>
        <template #reference> 
          <div class="msg-more-mine">
          <svg t="1746255036424" class="msg-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3547" width="200" height="200"><path d="M544.95418201 707.164625a46.545455 46.545455 0 0 1-65.90836402 0L84.154182 312.27298901a46.545455 46.545455 0 0 1 65.815273-65.90836401L512 608.48826201 874.123636 246.364625A46.545455 46.545455 0 0 1 939.752727 312.272989l-394.891636 394.891636z"  p-id="3548"></path></svg>
        </div>
        </template>
      </el-popover>
      </div>
        <el-avatar :size="28" :src="userInfo.image" class="chat-mine-avatar"></el-avatar>
      </el-row>
    </li>
  </ul>
  </div>
  <div class="chat-send-message">
    <div class="chat-img-show" v-if="imageUrl != ''">
      <span class="chat-img-show-text">按下回车键发送</span>
      <el-row><img :src="imageUrl" class="chat-img-show-img"></img></el-row>
    </div>
    <el-upload
                class="avatar-uploader"
                action="/api/upload"
                :headers="{'token':token}"
                :show-file-list="false"
                :on-success="handleAvatarSuccessByComment"
                :before-upload="beforeAvatarUpload"
                >
                <el-icon size="28" class="chat-send-message-icon"><Picture /></el-icon>
    </el-upload>  <!-- From Uiverse.io by JayRamoliya --> 
    <textarea 
    class="input-style" 
    rows="3" 
    v-model="message"
  @keydown.enter.prevent = "sendMessageToServer"
    ></textarea>
    <el-button class="chat-send-message-btn" :disabled="selectedItem != '' || imageUrl != '' || message.length == 0 || message.length > 500" @click="sendMessageToServer">发送</el-button>
  </div>
</template>

<style>
.custom-popover-mine {
  max-width: 125px;
    min-width: 10px !important;  /* 设置自定义最小宽度 */
    padding: 8px !important;      /* 调整内边距 */
}

.custom-popover {
  max-width: 65px;
    min-width: 10px !important;  /* 设置自定义最小宽度 */
    padding: 8px !important;      /* 调整内边距 */
}

.msg-more-mine{
  position: absolute;
  margin-top: 5px;
  right: 80px;
}

.msg-more{
  position: absolute;
  margin-top: 5px;
  margin-left: 15px;
}

.msg-icon{
  height: 10px;
  width: 10px;
  fill:rgb(118, 118, 118)
}

.unReadCount{
  font-size: 12px;
  position: absolute;
  margin-top: 40px;
  border-radius: 50%;
  background-color: rgb(251, 114, 153);
  color:#ffffff;
  text-align: center;
  margin-left: 190px;
  font-size: 600;
}

.chat-message-content-video-time-barrages{
  font-size: 12px;
  color: rgb(118, 118, 118);
  margin-left: -88px;
}

.chat-message-content-video-icon{
  height: 10px;
  fill:rgb(118, 118, 118);
  position: relative;
  margin-left: -82px;
  top: 1px;
}

.chat-message-content-video-time{
   font-size: 12px;
   position: absolute;
   margin-top: 55px;
   margin-left: 10px;
   color: rgb(153, 153, 153);
}

.chat-message-content-video-text{
  font-size: 14px;
  color: rgb(33, 33, 33);
  position: absolute;
  margin-top: 10px;
  max-width: 290px;
  height: 40px;
  overflow-wrap: break-word; /* 允许在单词内换行 */
  margin-left: 10px;
}

.chat-message-content-video-image{
  max-height: 80px;
  border-radius: 5px;
  margin-top: 10px;
  margin-left: 10px;
}


.chat-other-videoShow{
  background-color: rgb(255, 255, 255);
  height: 100px;
  border-radius: 5px;;
  width: 470px;
  margin-left: 125px;
  margin-top: 20px;
}

.chat-other-message{
  display: flex;
}

.chat-img-show-text{
  font-size: 14px;
  color: rgb(128, 185, 242);
  margin-left: 10px;
}

.chat-img-show{
  position: absolute;
  top: 410px;
  width: 720px;
  background-color: #a5a5a547;
}

.chat-img-show-img{
  max-height: 100px;
  margin-left: 10px;
}

.chat-message-content-image{
  max-height: 200px;
}

.isRead-mine{
  font-size: 10px;
  position: absolute;
  right: 55px;
  height: 15px;
  bottom: -20px;
}

.isRead-other{
  font-size: 10px;
  position: absolute;
  left: 60px;
  height: 15px;
  bottom: -20px;
}

.chat-user-content-no-message{
  position: absolute;
  top: 100px;
  left: 120px;
  transform: translate(-50%, -50%);
  color: rgb(153, 153, 153);
  width: 100px;
}

.chat-send-message-btn{
  margin-left: 670px;
}

/* From Uiverse.io by JayRamoliya */ 
.input-style {
  resize: none;
  padding: 10px;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  color: #555;
  width: 690px;
  outline: none;
  margin-left: 10px;
  overflow-wrap: break-word; /* 允许在单词内换行 */
  background-color: transparent;
}


.chat-send-message-icon{
  color: rgb(170, 170, 170);
  margin-left: 10px;
  margin-top: 5px;
}

.chat-mine-message-content-text{
  font-size: 15px;
  color: rgb(255, 255, 255);
}

.chat-mine-message-content{
  background-color: rgb(128, 185, 242);
  border-radius: 5px;
  margin-right: 10px;
  max-width: 480px;
  overflow-wrap: break-word; /* 允许在单词内换行 */
  padding: 5px 10px 5px 10px;
  text-align: left;
}

.chat-mine-avatar{
  margin-right: 15px;
}

.chat-mine{
  position: relative;
  margin-top: 10px;
  width: 100%;
  text-align: right;
  align-content: right;
  justify-content: right
}

.chat-other-message-content-text{
  font-size: 15px;
  color: rgb(51, 51, 51);
}

.chat-other-message-content{
  background-color: rgb(255, 255, 255);
  border-radius: 5px;
  margin-left: 10px;
  max-width: 480px;
  overflow-wrap: break-word; /* 允许在单词内换行 */
  padding: 5px 10px 5px 10px;
}

.chat-other-avatar{
  margin-left: 20px;
}

.chat-other{
  position: relative;
  margin-top: 10px;
  width: 100%;
}

.chat-message-time-row{
  width: 100%;
  text-align: center;
  align-content: center;
  justify-content: center;
  font-size: 14px;
  color: rgb(153, 153, 153);
}

.chat-infinite-list {
  position: relative;
  width: 747px;
  max-height: 495px;
  padding: 0;
  margin: 0;
  list-style: none;
  margin-left: 230px;
}
.chat-infinite-list .chat-infinite-list-item {
  align-items: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin-bottom: 20px;
}
.chat-infinite-list .chat-infinite-list-item + .list-item {
  margin-bottom: 10px;
}

.chat-send-message{
  width: 739px;
  margin-left: 240px;
  height: 155px;
  background-color: rgb(244, 245, 247);
  border-top: 1px solid rgb(216, 216, 216);

}

.chat-user-content{
  height: 495px;
  background-color: rgb(244, 245, 247);
}

.list-item.selected {
  background-color: #f0f0f0;
}

.chat-userInfo-container{
  align-items: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.chat-username-row{
  width: 155px;
}

.chat-message-row{
  width: 155px;
  margin-top: 3px;
}

.chat-message{
  font-size: 12px;
  color: rgb(153, 153, 153);
  top: 18px;
  margin-left: 10px;
}

.chat-username{
  font-size: 14px;
  top: 18px;
  margin-left: 10px;
}

.chat-avatar{
  position: relative;
  margin-left: 25px;
  top:20px
}

.infinite-list-wrapper {
    position: absolute;
  height: 650px;
  z-index: 1000;
  width: 239px;
  overflow: auto;
  background-color: rgb(255, 255, 255);
}

.infinite-list-wrapper .list {
  padding: 0;
  margin: 0;
  list-style: none;
}

.infinite-list-wrapper .list-item {
  display: flex;
  height: 78px;
  background: rgb(255, 255, 255);
}


.chat-right-title-text{
 font-size: 14px; 
 color: rgb(51, 51, 51);
 position: relative;
 top: 5px;
}

.chat-right-title{
    background-color: rgb(255, 255, 255);
  height: 35px;
  border-radius: 0px 5px 0 0;
  width: 737px;
  text-align: center !important;
  border-bottom: 1px solid rgb(233, 234, 236);
}

.chat-left-title-text{
    font-size: 13px;
  color: rgb(118, 118, 118);
  margin-left: 23px;
  position: relative;
  top: 4px;
}

.chat-left-title{
  background-color: rgb(255, 255, 255);
  height: 35px;
  border-radius: 5px 0 0 0;
  width: 240px;
  border-right: 1px solid rgb(233, 234, 236);
  border-bottom: 1px solid rgb(233, 234, 236);
}
</style>
