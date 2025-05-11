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
import { getSystemMsg,getAnswerMeMsg,getUserFollowingUsers,getChatHistory,sendMessage,setMessageRead,deleteMessage } from '../../api/chat';

const count = ref(0)
const load = () => {
  count.value += 2
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

const currentMsg = ref([]); 

const getMsg = async () => {
  const result = await getSystemMsg(currentUserId.value);
  if(result.code === 1){
    console.log(result.data)
    currentMsg.value = result.data;
  }else{
    ElMessage.error(result.message)
  }
}

// 添加格式化消息的方法
const formatMessage = (content) => {
  if (!content) return '';
  
  // 分割文本为行
  const lines = content.split('\n');
  
  // 第一行保持原样
  let result = lines[0] + '<br>';
  
  // 后续行添加样式
  for (let i = 1; i < lines.length; i++) {
    if (lines[i].trim()) {  // 忽略空行
      result += `<span class="new-line" style=" font-size: 12px; 
  color: rgb(34, 34, 34); font-weight: 400">${lines[i]}</span>`;
    }
  }
  
  return result;
};

const formatDate = (timestamp) => {
  const date = new Date(timestamp);
  
  // 补零函数
  const pad = n => n.toString().padStart(2, '0');
  
  return `
    ${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())}
    ${pad(date.getHours())}:${pad(date.getMinutes())}
  `.replace(/\s+/g, ' ').trim();
}

onMounted(async() => {
      await getUserInfo();
      await getMsg();
    })

</script>

<template>
    <div class="answer-me-content">
      <ul v-infinite-scroll="load" class="am-infinite-list1" style="overflow: auto" v-if="currentMsg.length>0">
    <li v-for="(item,index) in currentMsg" :key="i" class="am-infinite-list-item1">
      <el-row>
      
        <span class="am-infinite-list-item-text" v-html="formatMessage(item.content)"></span>
        <span class="am-infinite-list-item-time1">{{ formatDate(item.createdTime) }}</span>
      </el-row>
    </li>
  </ul>
  <div v-else>
    <img class="am-infinite-list-item-img" src="YilenaPic/25/05/03-5541e713df9b442fbc466d65bfac0850.jpg">
  </div>
    </div>
</template>

<style scoped>
.am-infinite-list-item-img{
    height: 250px;
    margin-left: 350px;
    position: relative;
}

.am-divider1{
  margin-bottom: 0px;
}

.am-infinite-list-item-time1{
  font-size: 12px;
  color: rgb(157, 157, 157);
  margin-top: 10px;
  margin-left: 10px;
}

.am-avatar-wrapper{
  width: 42px;
  height: 100%;
}

/* 新增样式 - 控制换行后的文本 */
.am-infinite-list-item-text .new-line {
  display: block; /* 确保换行生效 */
  margin-top: 4px; /* 可选的额外间距 */
  font-size: 12px; 
}

.am-infinite-list-item-text{
  position: relative;
  margin-top: 10px;
  margin-left: 10px;
  color: rgb(34, 34, 34);
  width: 90%;
  font-size: 14px;
  white-space: pre-line; 
  word-break: break-word; /* 可选：确保长单词或URL也能换行 */
  font-weight: 600;
}

.am-avatar{
  margin-top: 10px;
  position: relative;
  margin-left: 0px;
}

.am-infinite-list1 {
  padding: 0;
  margin: 0;
  list-style: none;
}
.am-infinite-list1 .am-infinite-list-item1 {
  margin: 10px;
  margin-bottom: 10px;
  margin-top: 0px;
}
.am-infinite-list1 .am-infinite-list-item1 + .am-list-item1 {
  margin-top: 0px;
}

.answer-me-content{
  width: 100%;
  background-color: rgb(255, 255, 255);
  border-radius: 5px;
}
</style>
