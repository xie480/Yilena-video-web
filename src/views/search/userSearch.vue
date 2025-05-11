<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import CryptoJS from 'crypto-js'
import { getUserSearchList } from '../../api/search';
import { ElMessage,ElMessageBox } from 'element-plus';
import FollowBotton from '../video/followBotton.vue';
import { getAuthorIsFollow,giveAuthorFollow,giveAuthorUnfollow } from '../../api/video';
import { getUserInfoByUsername } from '../../api';

const router = useRouter();

const searchTerm = ref({
  id: '',
  username: '',
  sortType: '1',
  page: 1,
  pageSize: 30
}); // 搜索关键词

const searchTotal = ref(0); // 搜索结果总数

// 页码变化处理
const handlePageChange = (newPage) => {
  searchTerm.value.page = newPage;  // 更新当前页码
  getCurrentSearchVideos();         // 重新加载数据
};

    // AES解密
    const decryptSearchText = (cipherText) => {
  const decoded = atob(cipherText.replace(/-/g, '+').replace(/_/g, '/'))
  return CryptoJS.AES.decrypt(decoded, 'yilena-key').toString(CryptoJS.enc.Utf8)
}

const toUserInfo = (id) => {
      router.push('/userInfo/others/' + id).then(() => {
        window.location.reload();
      })
    }

const searchText = ref('')

let param4 = '';

const getSerachInfo = () => {
 // 拆分路由路径，假设加密文本在路径第三段
 const pathSegments = router.currentRoute.value.path.split('/');
  
  // 提取加密文本（注意索引位置）
  const encryptedText = pathSegments[2] || ''; // 根据实际路由结构调整索引
  
  if (!encryptedText) return '';

  try {
    // 执行解密并返回结果
    searchText.value =  decryptSearchText(encryptedText);
  } catch (error) {
    console.error('解密失败:', error);
    searchText.value =   '';
  }

  param4 = router.currentRoute.value.query.userSort;
}

const userGroups = ref([{
  id: '',
  username: '',
  image: '',
  followersCount: '',
  videoCount: '',
  description: '',
  isFollow: '',
  isSpecial: ''
}]); // 存储视频分组的数组

const chunkArray = (arr, size) => {
  const chunks = []
  for (let i = 0; i < arr.length; i += size) {
    chunks.push(arr.slice(i, i + size))
  }
  return chunks
}

const currentUserID = ref('') // 当前用户ID

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
const getCurrentSearchUsers = async () => {
  searchTerm.value.username = searchText.value;

  if(param4){
    searchTerm.value.sortType = param4;
  }

  const result = await getUserSearchList(
    searchTerm.value.id,
    searchTerm.value.username,
    searchTerm.value.sortType,
    searchTerm.value.page,
    searchTerm.value.pageSize
  )
  console.log(result)
  if (result.code === 1) {

    for (let i = 0; i < result.data.rows.length; i++) {
    const item = result.data.rows[i];
    
    // 高亮处理逻辑
    if (searchText.value) {
        const escapedSearch = searchText.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        const regex = new RegExp(`(${escapedSearch})`, 'gi');
        item.username = item.username.replace(
            regex, 
            '<span style="color:rgb(242, 93, 142)">$1</span>'
        );
    }

    // 关注状态检查
    const isFollow = await getAuthorIsFollow(currentUserID.value, item.id);
    if(isFollow.code === 1){
    if(isFollow.data){
      if(isFollow.data.isSpecial === 0){
        item.isFollowing = true;
      }else{
        item.isFollowing = true;
        item.isSpecial = true;
      }
    }
  }
}
    // 将数据分块为每行5个
    userGroups.value = chunkArray(result.data.rows, 2);
    searchTotal.value = result.data.total;
    if(searchTotal.value === 0){
      ElMessage.warning('喵？没有搜索到结果喵！');
    }
  } else {
    ElMessage.error('搜索失败')
  }
}

const getUserInfo = async () => {
      //获取登录用户名
      let loginUser = JSON.parse(localStorage.getItem('loginUser'))
      if (loginUser) {
        userInfo.value.username = loginUser.username;
        const result = await getUserInfoByUsername(userInfo.value.username);
        if(result.code === 1){
          userInfo.value = result.data;
          currentUserID.value = userInfo.value.id;
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

    const showDropdown = ref(false)
  
  const handleMainClick = (id) => {
    for(let i = 0; i < userGroups.value.length; i++){
      for(let j = 0; j < userGroups.value[i].length; j++){
        if(userGroups.value[i][j].id === id){
          if (!userGroups.value[i][j].isFollowing) {
            console.log(userGroups.value[i][j].isFollowing);
             giveCurrentAuthorFollow(id,0);
            } else {
             showDropdown.value = !showDropdown.value
            }
        }
      }
    }
  }

  const giveCurrentAuthorFollow = async(id,isSpecial) =>{
    const requestData = {
      followedId: id,
      followerId: currentUserID.value,
      isSpecial: isSpecial
    }
	const result = await giveAuthorFollow(requestData);
	if(result.code === 1){
    for(let i = 0; i < userGroups.value.length; i++){
      for(let j = 0; j < userGroups.value[i].length; j++){
        if(userGroups.value[i][j].id === id){
          if(isSpecial === 1){
            userGroups.value[i][j].isSpecial = true;
          }else{
            userGroups.value[i][j].isFollowing = true;
            userGroups.value[i][j].followersCount += 1
          }
        }
      }
    }
    
	}else{
		ElMessage.error(result.msg);
    console.log(result.msg)
  }
}

const giveCurrentAuthorUnFollow = async(id,isSpecial) =>{

	const result = await giveAuthorUnfollow(id,currentUserID.value,isSpecial);
	if(result.code === 1){
		
	}else{
		ElMessage.error(result.msg);
  }
}
  
  const setSpecial = (id) => {
    for(let i = 0; i < userGroups.value.length; i++){
      for(let j = 0; j < userGroups.value[i].length; j++){
        if(userGroups.value[i][j].id === id){
          if(userGroups.value[i][j].isSpecial){
            ElMessage.info('您已经是特别关注了');
            return;	
            }
            giveCurrentAuthorFollow(id,1);
            showDropdown.value = false;
        }
      }
    }
    showDropdown.value = false
  }
  
  const unfollow = (id) => {
    for(let i = 0; i < userGroups.value.length; i++){
      for(let j = 0; j < userGroups.value[i].length; j++){
        if(userGroups.value[i][j].id === id){
          if(userGroups.value[i][j].isSpecial){
            userGroups.value[i][j].isSpecial = false;
            giveCurrentAuthorUnFollow(id,1);
          }else{
          userGroups.value[i][j].isFollowing = false;
          userGroups.value[i][j].followersCount -= 1;
          giveCurrentAuthorUnFollow(id,0);
        }
        }
      }
    }
    showDropdown.value = false
  }

onMounted(async() => {
  await getUserInfo();
  await getSerachInfo();
  getCurrentSearchUsers();
})
</script>

<template>
  <!-- 搜索用户 -->
  <div class="search-user">
    <el-row v-for="(group, index) in userGroups" 
    :key="index" class="user-row">
    <el-col 
        v-for="user in group" 
        :key="user.id"
        :span="12" 
        class="user-col"
        gutter="20"
      >
      <el-avatar :src= "user.image"
      :size="78" 
      class="user-avatar" 
      @click="toUserInfo(user.id)"
      ></el-avatar>
      <div class="user-name">
        <span class="user-name-text" v-html="user.username"  @click="toUserInfo(user.id)"></span>
      </div>
      <div class="user-info">
        <span class="user-followers">{{ user.followersCount }}粉丝</span>
        <span class="user-videoCount">·&nbsp;{{ user.videoCount || 0 }}个视频</span>
        <span class="user-description">{{ user.description }}</span>
      </div>
      <div
      class="user-follow">
      <!-- <FollowBotton
      :initial-following="user.isFollow"
      :initial-special="user.isSpecial"
      :initial-count="user.followersCount"
      class="user-follow-btn"
      ></FollowBotton> -->
      <div class="follow-container">
      <div class="follow-wrapper">
        <button 
          class="follow-button"
          :class="{
            'following': user.isFollowing,
            'special': user.isSpecial
          }"
          @click="handleMainClick(user.id)"
        >
          <span class="text">
            {{ user.isSpecial ? '特别关注' : user.isFollowing ? '已关注' : '+ 关注' }}
          </span>
        </button>
  
        <transition name="dropdown">
          <div v-if="showDropdown" class="dropdown-menu">
            <div class="dropdown-item" @click="setSpecial(user.id)">
              <span>⭐ 特别关注</span>
            </div>
            <div class="dropdown-item danger" @click="unfollow(user.id)">
              <span>❌ 取消关注</span>
            </div>
          </div>
        </transition>
      </div>
    </div>
    </div>
  </el-col>
</el-row>
   </div>

     <!-- 分页 -->
     <div class="s-page">
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
</template>

<style scoped>
.s-page{
  margin-top: 80px;
  display: flex;
  justify-content: center;
  margin-bottom: 60px;
}

.search-user{
  width: 1600px;
  margin-left: 50px;
}

.user-row{
  margin-bottom: 20px;
  display: flex;
  height: 135px;
}



.user-avatar{
  margin-top: 25px;
  margin-left: 25px;
}

.user-name-text{
  font-size: 18px;
  font-weight: 600;
  color: rgb(24, 25, 28);
}

.user-name{
  margin-top: -85px;
  margin-left: 120px;
}

.user-info{
  display: flex;
  margin-left: 120px;
  margin-top: 6px;
  font-size: 13px;
  color: rgb(112, 116, 123);
}

.user-follow{
  position: absolute;
  left: 40px;
}

.user-description{
  margin-left: 10px;
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
    background: rgb(0, 174, 236);
    color: white;
    position: relative;
    overflow: hidden;
    width: 100px;
  
    .text {
      font-size: 14px;
      font-weight: 500;
      margin-right: 8px;
      margin-left: 2px;
    }
  
    .count {
    margin-top: 5px;
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
    left: 60px;
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
</style>
