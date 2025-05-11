<script lang="ts" setup>
import { ref,onMounted,computed,watch } from 'vue'
import { useRouter } from 'vue-router';
import { useTransition } from '@vueuse/core'
import CryptoJS from 'crypto-js'
import { ElMessage,ElMessageBox } from 'element-plus';
import { findAllVideos,getUserInfoByUsername,getHotSearch } from '../../../api';
import { getAllCoins,getAllComments,getAllFavorites,getAllLikes,getAllShares,getAllViews,getBarrages } from '../../../api/upload';
import { getAuthorInfo,getAuthorIsFollow,giveAuthorFollow,giveAuthorUnfollow } from '../../../api/video';

const router = useRouter();

const currentPath = ref('');
const path = ref('');

const getCurrentPath = () => {
  const pathArr = router.currentRoute.value.path.split('/');
  path.value = pathArr[1] + '/' + pathArr[2] + '/' + pathArr[3];
  currentPath.value = router.currentRoute.value.path;
  activeIndex.value = currentPath.value;
}

    // AES加密（自动处理URL安全编码）
    const encryptSearchText = (text) => {
  const encrypted = CryptoJS.AES.encrypt(text, 'yilena-key').toString()
  return btoa(encrypted).replace(/\+/g, '-').replace(/\//g, '_')
}

    const toSearch = () => {
      if (searchText.value.trim() === '') {
        ElMessage.error('请输入搜索内容！');
        return; // 阻止跳转
      }
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

const searchText = ref('');

    // 搜索框历史记录
const searchHistory = ref([]);

// 最大历史记录数量（假设每行5个按钮，最多3行）
const maxHistoryCount = 10;

// 删除历史记录
const removeHistory = (index) => {
  searchHistory.value.splice(index, 1);
  // 更新localStorage
  localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value));
};

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
   toSearch();
};



const showDropdown = ref(false);

const addSearch = (content) => {
  searchText.value = content;
  addSearchHistory(content);
}

const clearSearchHistory = () => {
  searchHistory.value = [];
   // 清空localStorage
   localStorage.removeItem('searchHistory');
};

function formatViews(views) {
  if (views >= 10000) {
    return `${Math.round(views / 1000) / 10}万`; // 保留1位小数（如：1.3万）
  }
  return views.toString();
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

const userInfo = ref({
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

    const getCurrentUserAllViews = async () => {
    const result = await getAllViews(userInfo.value.id);
    console.log('播放量：',result);
    if(result.code === 1){
      userInfo.value.allViews = formatViews(result.data);
    }
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
          followEntity.value.followerId = currentUserInfo.value.id;
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


const getCurrentUserAllLikes = async () => {
    const result = await getAllLikes(userInfo.value.id);
    if(result.code === 1){
      userInfo.value.allLikes = formatViews(result.data);
    }
}

const currentUserId = ref('');
const getCurrentUserId = async() => {
  const pathArray = router.currentRoute.value.path.split('/');
  console.log(pathArray);
  currentUserId.value = pathArray[3];
}

    const getCurrentUserInfo = async () => {
        const result = await getAuthorInfo(currentUserId.value);
        if(result.code === 1){
          userInfo.value = result.data;
          followEntity.value.followedId = userInfo.value.id;
          userInfo.value.followingCount = formatViews(userInfo.value.followingCount);
          userInfo.value.followersCount = formatViews(userInfo.value.followersCount);
          userInfo.value.postCount = formatViews(userInfo.value.postCount);
          console.log(userInfo.value)
          if(userInfo.value.status === 0){
            ElMessage.error('该账号已被封禁!')
            }
          }
    }

    const activeIndex = ref(currentPath.value);
const handleSelect = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
  activeIndex.value = keyPath[0];
}
// 处理空格键事件
const handleSpace = () => {
  if (searchText.value.trim() !== '') { 
    addSearchHistory(searchText.value.trim()); // 传递处理后的内容
  }
};

const reloadIndex = () => {
      router.push('/index').then(() => {
        window.scrollTo(0, 0); // 滚动到页面顶部
      window.location.reload();
      });
    }

    const isFollowing = ref(false)
  const isSpecial = ref(false)
  const fansCount = ref(0) // 初始粉丝数
  const showDropdown1 = ref(false)

  const followEntity = ref({
  followedId: '',
  followerId: '',
  isSpecial: ''
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

const getCurrentAuthorIsFollow = async() =>{
  const result = await getAuthorIsFollow(currentUserInfo.value.id,userInfo.value.id);
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
  
  const handleMainClick = () => {
    if (!isFollowing.value) {
      followEntity.value.isSpecial = 0;// 非特别关注
      giveCurrentAuthorFollow();
    } else {
      showDropdown1.value = !showDropdown1.value
    }
  }
  
  const setSpecial = () => {
    if(isSpecial.value){
      ElMessage.info('您已经是特别关注了');
      return;	
    }
    followEntity.value.isSpecial = 1;// 特别关注
    giveCurrentAuthorFollow();
    showDropdown1.value = false;
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
    showDropdown1.value = false
  }

  const toUserInfo = (id) => {
       router.push('/userInfo/' + id).then(() => {
        window.location.reload();
      })
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

    const toFavorites = () => {
      router.push('/userInfo/' + userInfo.value.id + '/favorites').then(() => {
        window.location.reload();
      })
    }

onMounted(async() => {
  await getUserInfo();
  await getCurrentUserId();
  await getCurrentUserInfo();
  await getUserInfo();
  await getCurrentAuthorIsFollow();
  getCurrentUserAllViews();
  getCurrentUserAllLikes();
  getHotSearchData();
  getCurrentPath();
  // 添加全局点击事件监听
  document.addEventListener('click', (event) => {
    // 如果点击的元素不是输入框，也不是下拉抽屉中的元素，则隐藏下拉抽屉
    if (!event.target.closest('.search') && !event.target.closest('.dropdown-container')) {
      showDropdown.value = false;
    }
});
  //读取本地存储的搜索历史
  const storedHistory = localStorage.getItem('searchHistory');
  if (storedHistory) {
    searchHistory.value = JSON.parse(storedHistory);
  }
})
</script>

<template>
  <!-- 顶栏 -->
  <el-row>
    <el-col :span="24">

      <!-- 背景图片 -->
      <div class="background-layer-userInfo"></div>
      
      <!-- 渐变阴影 -->
      <div class="uf-shadow-layer"></div>
      <div class="uf-shadow-layer-bottom"></div>

      <div class="header">

        <el-row gutter="160">
        <!-- 左侧导航栏 -->
         <el-col :span="8">
          <div class="header-left">
            <div class="button-container">

            <!-- 首页按钮 -->
            <button class="btn-left-1" @click="reloadIndex">
              <span class="btn-text-one"><svg t="1743519580381" class="bli-1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3754" width="200" height="200"><path d="M777.500444 157.582222a49.208889 49.208889 0 0 1 15.644445 35.783111 49.208889 49.208889 0 0 1-15.644445 35.726223l-48.753777 46.193777h49.891555c42.496 0 83.171556 15.928889 113.208889 44.373334 29.980444 28.444444 46.819556 67.015111 46.819556 107.235555v303.217778c0 83.797333-71.68 151.665778-160.028445 151.665778H245.304889C157.013333 881.777778 85.333333 813.909333 85.333333 730.168889v-303.217778c0-83.797333 71.68-151.665778 159.971556-151.665778h49.777778l-48.696889-46.136889a48.810667 48.810667 0 0 1-14.563556-49.152 51.996444 51.996444 0 0 1 38.115556-36.124444 55.296 55.296 0 0 1 51.825778 13.767111l113.208888 107.235556c3.356444 3.185778 6.144 6.656 8.419556 10.410666h137.102222a49.720889 49.720889 0 0 1 8.476445-10.467555l113.095111-107.235556a54.840889 54.840889 0 0 1 37.717333-14.791111c14.165333 0 27.704889 5.347556 37.717333 14.791111z m1.137778 219.136H245.304889c-27.875556 0-51.086222 20.423111-53.191111 46.819556l-0.113778 3.811555v303.217778c0 26.624 21.731556 48.469333 49.379556 50.346667l3.982222 0.170666h533.276444c27.932444 0 51.2-20.366222 53.248-46.762666l0.113778-3.754667v-303.217778c0-27.875556-23.893333-50.574222-53.361778-50.574222zM351.971556 477.866667c29.468444 0 53.361778 22.641778 53.361777 50.574222v50.517333c0 27.875556-23.893333 50.517333-53.361777 50.517334-29.411556 0-53.304889-22.584889-53.304889-50.517334v-50.517333c0-27.875556 23.893333-50.574222 53.304889-50.574222z m320 0c29.468444 0 53.361778 22.641778 53.361777 50.574222v50.517333c0 27.875556-23.893333 50.517333-53.361777 50.517334-29.411556 0-53.304889-22.584889-53.304889-50.517334v-50.517333c0-27.875556 23.893333-50.574222 53.304889-50.574222z" fill="#FFFFFF" p-id="3755"></path></svg>
                <span class="btn-text">首页</span>
              </span>
              <span class="btn-text-two"><el-icon><HomeFilled /></el-icon>
                <span class="btn-text2">返回首页</span>
              </span>
            </button>

            <!-- 番剧按钮 -->
<button class="btn-left">
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
 <button class="btn-left">
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
 <button class="btn-left">
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
 <button class="btn-left">
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
 <button class="btn-left">
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
 <button class="btn-left">
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
<button class="bookmarkBtn">
  <span class="IconContainer">
    <svg t="1743559875398" class="bli-2" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="7787" width="200" height="200"><path d="M219.428571 365.714286a36.571429 36.571429 0 0 1 6.582858 72.557714L219.428571 438.857143H109.714286a36.571429 36.571429 0 0 0-35.986286 29.988571L73.142857 475.428571V438.857143v140.068571L73.142857 578.852571 73.142857 914.944l0.585143 5.851429A36.571429 36.571429 0 0 0 109.714286 950.857143h103.424l-0.146286 0.438857 611.547429 1.828571 122.148571-2.267428H914.285714a36.571429 36.571429 0 0 0 35.986286-29.988572l0.512-6.070857 0.146286-103.936V578.852571l-0.146286-103.936-0.512-6.070857A36.571429 36.571429 0 0 0 914.285714 438.857143H804.571429a36.571429 36.571429 0 0 1 0-73.142857h121.929142A110.957714 110.957714 0 0 1 1024 475.428571v443.465143l-0.292571 0.073143-0.219429 5.851429A109.714286 109.714286 0 0 1 917.504 1024L109.714286 1024a109.714286 109.714286 0 0 1-109.641143-105.033143L0 918.820571V472.283429l0.146286-0.073143 0.365714-7.314286A109.714286 109.714286 0 0 1 95.085714 366.665143L95.451429 365.714286H219.428571z m292.059429-365.714286a36.571429 36.571429 0 0 1 36.571429 36.571429l-0.073143 585.142857 117.321143-117.321143 5.12-4.169143a36.571429 36.571429 0 0 1 46.592 55.954286l-131.657143 131.510857a109.714286 109.714286 0 0 1-152.795429-2.194286L303.250286 556.105143l-4.169143-5.12a36.571429 36.571429 0 0 1 55.954286-46.665143l56.100571 56.246857 63.707429 63.634286V36.571429l0.658285-6.582858A36.571429 36.571429 0 0 1 511.488 0z"  p-id="7788"></path></svg>
  </span>
  <p class="text">下载客户端</p>
</button>

          </div>
        </el-col>

 <!-- 中间搜索框 -->
 <el-col :span="8">
          <div class="header-center">
            <!-- From Uiverse.io by joe-watson-sbf --> 
<div class="search">
    <input type="text" 
    class="search__input" 
    :placeholder= "currentHotSearchContent"
    @keydown.space.prevent="handleSpace"
    @keydown.enter="addSearchHistory(searchText)" 
    @focus="showDropdown = true" v-model="searchText">
    <button class="search__button" @click="addSearchHistory(searchText)">
        <svg class="search__icon" aria-hidden="true" viewBox="0 0 24 24">
            <g>
                <path d="M21.53 20.47l-3.66-3.66C19.195 15.24 20 13.214 20 11c0-4.97-4.03-9-9-9s-9 4.03-9 9 4.03 9 9 9c2.215 0 4.24-.804 5.808-2.13l3.66 3.66c.147.146.34.22.53.22s.385-.073.53-.22c.295-.293.295-.767.002-1.06zM3.5 11c0-4.135 3.365-7.5 7.5-7.5s7.5 3.365 7.5 7.5-3.365 7.5-7.5 7.5-7.5-3.365-7.5-7.5z"></path>
            </g>
        </svg>
    </button>
    <div
      v-if="showDropdown"
      class="dropdown-container"
      @click.stop
    >
      <div class="dropdown-content">
        <div class="search-history">
          <span class="search-history-title">搜索历史</span>
          <botton class="clear-history-button" @click="clearSearchHistory">清空</botton>
          <div class="search-history-buttons">
          <div
            v-for="(item, index) in searchHistory"
            :key="index"
            class="history-button"
          >
            <span @click="addSearch(item)">{{ item }}</span>
            <span class="delete-icon" @click="removeHistory(index)">×</span>
          </div>
        </div>
        </div>
        <div class="hot-search">
          <span class="hot-search-title">bilibili热搜</span>
          <div class="hot-search-container">
          <div class="hot-search-column">
            <div
              v-for="(item, index) in hotSearchData.slice(0, 5)"
              :key="index"
              class="hot-search-item1" @click="addSearch(item.content)"
            >
            <span class="num">{{ index + 1 }}</span>
              {{ item.content }}
            </div>
          </div>
          <div class="hot-search-column">
            <div
              v-for="(item, index) in hotSearchData.slice(5, 10)"
              :key="index + 5"
              class="hot-search-item2" @click="addSearch(item.content)"
            >
            <span class="num">{{ index + 6 }}</span>
              {{ item.content }}
            </div>
          </div>
        </div>
        </div>
      </div>
    </div>
</div>
          </div>
         </el-col>

        <!-- 右侧导航栏 -->
         <el-col :span="8">
          <div class="header-right">
               <!-- 头像下拉菜单 -->
    <el-dropdown>
      <el-avatar :src= "currentUserInfo.image" :size="37" class="avatar" @click="toUserInfo(currentUserInfo.id)"></el-avatar>
      <template #dropdown>
        <el-dropdown-menu class="custom-dropdown-menu"> 
          
          <el-row>
            <el-col :span="24">
            <span class="username">{{ currentUserInfo.username }}</span>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <span class="coins">硬币：<span class="coins-num">{{currentUserInfo.coins}}</span></span>
            </el-col>
          </el-row>
          <el-row class="userInfo">
            <el-col :span="8" class="userInfo-item">
              <el-statistic title="---------关注---------" :value=currentUserInfo.followingCount />
            </el-col>
            <el-col :span="8" class="userInfo-item">
              <el-statistic title="---------粉丝---------" :value=currentUserInfo.followersCount />
            </el-col>
            <el-col :span="8" class="userInfo-item">
              <el-statistic title="---------动态---------" :value=currentUserInfo.postCount />
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
<button class="cta-1" @click="toUserInfo(currentUserInfo.id)">
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
             <button class="vip-button">
    <svg class="vip-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" fill="rgb(249,249,249)">
      <path d="M512 51.2a460.8 460.8 0 1 0 0 921.6 460.8 460.8 0 0 0 0-921.6z m0 83.7632c207.9232 0 377.0368 169.1648 377.0368 377.0368 0 207.872-169.1136 376.9856-376.9856 376.9856-207.9232 0-377.0368-169.1136-377.0368-376.9856 0-207.872 169.1136-377.0368 377.0368-377.0368z" fill="#FFFFFF"></path>
      <path d="M672.6656 412.4672v-0.1536h-121.1904a363.9296 363.9296 0 0 0-0.3072-62.1568l0.1024-2.1504v-0.1024a40.704 40.704 0 0 0-81.408 0v0.1024c0 2.7648 0.3072 5.5296 0.8704 8.1408l-0.5632 0.0512a285.2864 285.2864 0 0 1-0.256 56.1152H345.2416v0.1536A40.704 40.704 0 0 0 307.2 453.0176v0.1536c0 22.4768 17.2544 40.4992 39.68 40.4992 1.7408 0 3.584 0.1024 6.3488 0.0512l95.2832-0.2048a279.3472 279.3472 0 0 1-119.2448 132.3008l-1.6896 1.024H327.68a40.6528 40.6528 0 0 0-19.0976 34.4576v0.1024a40.704 40.704 0 0 0 64.0512 33.28 360.8064 360.8064 0 0 0 162.7648-201.0624h130.816l5.2224-0.1024c24.4736-0.512 40.448-17.7664 40.448-40.2432v-0.1024c0.1024-22.0672-17.408-39.936-39.168-40.704z" fill="#FFFFFF"></path>
      <path d="M705.1776 641.9968l-0.3072-0.4096a41.2672 41.2672 0 0 0-4.9152-7.0144 361.216 361.216 0 0 0-97.4848-94.208v0.0512a40.704 40.704 0 0 0-64.3584 33.024 360.8064 360.8064 0 0 0 162.7648-201.0624h130.816l5.2224-0.1024c24.4736-0.512 40.448-17.7664 40.448-40.2432v-0.1024c0.1024-22.0672-17.408-39.936-39.168-40.704z" fill="#FFFFFF"></path>
    </svg>
    <div class="vip-text">大会员</div>
  </button>
</div>

<!-- 私信 -->
 <div class="botton-right">
  <el-dropdown>
             <button class="left-button-2" @click="toChat">
              <svg t="1743583258372" class="vip-icon" viewBox="0 0 1424 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="21799" width="200" height="200"><path d="M1246.608696 0H178.086957a178.086957 178.086957 0 0 0-178.086957 178.086957v667.826086a178.086957 178.086957 0 0 0 178.086957 178.086957h1068.521739a178.086957 178.086957 0 0 0 178.086956-178.086957V178.086957a178.086957 178.086957 0 0 0-178.086956-178.086957z m89.043478 845.913043a89.043478 89.043478 0 0 1-89.043478 89.043479H178.086957a89.043478 89.043478 0 0 1-89.043479-89.043479V178.086957a89.043478 89.043478 0 0 1 89.043479-89.043479h1068.521739a89.043478 89.043478 0 0 1 89.043478 89.043479zM1082.323478 257.78087l-337.92 293.843478a44.521739 44.521739 0 0 1-64.111304 0l-337.92-293.843478a44.521739 44.521739 0 0 0-63.22087 63.220869l338.365218 293.398261a133.565217 133.565217 0 0 0 189.662608 0l338.365218-293.398261a44.521739 44.521739 0 1 0-63.22087-63.220869z" fill="#FFFFFF" p-id="21800"></path></svg>
    <div class="vip-text">私信</div>
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
              <svg t="1743584588096" class="vip-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="35622" width="200" height="200"><path d="M545.04687527 520.78906277h-26.3671875c-116.36718777 0-210.9375-94.57031223-210.9375-210.9375s94.57031223-210.9375 210.9375-210.9375h26.3671875v421.875z m-52.734375-366.85546902c-74.70703152 12.65625-131.8359375 77.69531223-131.8359375 156.09375s57.12890598 143.4375 131.8359375 156.09375V153.93359375z" fill="#FFFFFF" p-id="35623"></path><path d="M345.00781223 716.25781223c-116.36718777 0-210.9375-94.57031223-210.9375-210.9375v-26.3671875h421.875v26.3671875c0 116.19140598-94.57031223 210.9375-210.9375 210.9375z m-155.91796821-184.5703125c12.65625 74.70703152 77.69531223 131.8359375 156.09375 131.8359375s143.4375-57.12890598 156.09375-131.8359375H189.08984402z" fill="#FFFFFF" p-id="35624"></path><path d="M540.4765625 889.92968777h-26.3671875v-421.875h26.3671875c116.36718777 0 210.9375 94.57031223 210.9375 210.9375s-94.74609402 210.9375-210.9375 210.9375z m26.3671875-366.85546902v312.01171902c74.70703152-12.65625 131.8359375-77.69531223 131.8359375-156.09375s-57.12890598-143.4375-131.8359375-155.91796902z" fill="#FFFFFF" p-id="35625"></path><path d="M925.08593723 509.890625h-421.875v-26.3671875c0-116.36718777 94.57031223-210.9375 210.9375-210.9375s210.9375 94.57031223 210.9375 210.9375v26.3671875z m-366.85546821-52.734375h312.01171821c-12.65625-74.70703152-77.69531223-131.8359375-156.09375-131.8359375s-143.4375 57.12890598-155.91796821 131.8359375z" fill="#FFFFFF" p-id="35626"></path></svg>
    <div class="vip-text">动态</div>
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
             <button class="left-button-4">
              <svg t="1743584811352" class="vip-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="49270" width="200" height="200"><path d="M512.1184 785.408l231.104 121.472a36.8 36.8 0 0 0 53.376-38.784l-44.16-257.28 187.008-182.272a36.8 36.8 0 0 0-20.416-62.72l-258.368-37.568L545.0784 94.08a36.8 36.8 0 0 0-65.92 0L363.5104 328.32l-258.368 37.504a36.8 36.8 0 0 0-20.416 62.72l187.008 182.272-44.16 257.344a36.8 36.8 0 0 0 53.376 38.784l231.04-121.472z m-196.864 186.624a110.336 110.336 0 0 1-160.128-116.352l37.568-219.2L33.4624 481.216a110.336 110.336 0 0 1 61.12-188.224l220.16-32L413.1744 61.568a110.336 110.336 0 0 1 197.888 0l98.432 199.424 220.16 32a110.336 110.336 0 0 1 61.12 188.16L831.4784 636.544l37.632 219.2a110.336 110.336 0 0 1-160.128 116.352L512.1184 868.48l-196.864 103.488z m402.816-693.696l-27.712-20.096 19.2 2.752 8.512 17.344z" fill="#FFFFFF" p-id="49271"></path></svg>
    <div class="vip-text">收藏</div>
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
              <svg t="1743584884837" class="vip-icon" viewBox="0 0 1025 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="52791" width="200" height="200"><path d="M512.61025 108.624553A402.765197 402.765197 0 1 0 915.375447 512.61025 403.253397 403.253397 0 0 0 512.61025 108.624553z m0 732.300358A329.535161 329.535161 0 1 1 842.145411 512.61025 329.779261 329.779261 0 0 1 512.61025 840.924911z m172.578785-358.583075H549.225268v-143.530871a36.615018 36.615018 0 0 0-73.230036 0v180.145888a36.615018 36.615018 0 0 0 36.615018 36.615018h172.578785a36.615018 36.615018 0 0 0 0-73.230035z" p-id="52792" fill="#ffffff"></path></svg>
    <div class="vip-text">历史</div>
  </button>
</div>

<!-- 创作中心 -->
<div class="botton-right">
             <button class="left-button-6" @click="toUpload">
              <svg t="1743584977435" class="vip-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="55639" width="200" height="200"><path d="M526.362676 10.557633a381.527233 381.527233 0 0 0-387.493923 373.813154A369.465995 369.465995 0 0 0 273.332383 667.021436a73.262434 73.262434 0 0 1 27.361538 59.283331v31.921794a75.350776 75.350776 0 0 0 72.9641 75.265538h303.150492a74.02958 74.02958 0 0 0 72.9641-74.967203v-32.262748a76.714591 76.714591 0 0 1 27.361538-59.283331 369.210279 369.210279 0 0 0 134.46363-282.650649A378.032457 378.032457 0 0 0 529.346021 10.472394z m223.409937 620.024372a121.464769 121.464769 0 0 0-43.301125 93.421325v31.921793a29.833452 29.833452 0 0 1-27.361537 29.620356H373.785878a28.256541 28.256541 0 0 1-27.361537-29.151544v-32.390605a121.209054 121.209054 0 0 0-43.301125-93.421325 316.87388 316.87388 0 0 1-118.524043-246.168599 342.232315 342.232315 0 0 1 683.782722 0 316.746023 316.746023 0 0 1-118.524043 246.168599z m-63.928826 246.168599H366.711088a22.801281 22.801281 0 1 0 0 45.602563h319.132699a22.801281 22.801281 0 0 0 0-45.602563z m-45.602562 91.162506H412.313651a22.801281 22.801281 0 0 0 0 45.602563h227.927574a22.801281 22.801281 0 0 0 0-45.602563z" fill="#ffffff" p-id="55640"></path><path d="M640.411702 1024H412.441508a33.242989 33.242989 0 0 1 0-66.485979h227.970194a33.242989 33.242989 0 0 1 0 66.485979z m-227.970194-45.602562a12.359573 12.359573 0 0 0 0 24.676526h227.970194a12.359573 12.359573 0 0 0 0-24.676526z m273.530137-45.602563H366.838946a33.242989 33.242989 0 0 1 0-66.485979h319.132699a33.242989 33.242989 0 0 1 0 66.485979z m-319.132699-45.602562a12.359573 12.359573 0 0 0 0 24.676526h319.132699a12.359573 12.359573 0 1 0 0-24.676526z m6.861694-43.258506a85.451531 85.451531 0 0 1-83.448427-85.749865v-31.964413a63.374776 63.374776 0 0 0-23.483189-51.143061A378.842222 378.842222 0 0 1 128.427045 384.370787a393.11966 393.11966 0 0 1 397.893012-384.38272h5.753594a389.283931 389.283931 0 0 1 389.923219 384.254863 378.842222 378.842222 0 0 1-138.214121 290.833538 65.931929 65.931929 0 0 0-23.568428 51.143061v32.049651a84.599146 84.599146 0 0 1-83.235331 85.749865zM520.268128 20.956722a371.810051 371.810051 0 0 0-370.78719 363.584542A358.001425 358.001425 0 0 0 280.151458 658.923785a84.38605 84.38605 0 0 1 31.154648 67.636698v31.666078a64.568114 64.568114 0 0 0 62.820726 64.82383h302.852158a63.630491 63.630491 0 0 0 62.479773-64.355019v-32.433224a86.943203 86.943203 0 0 1 31.069409-67.253125 357.703091 357.703091 0 0 0 130.755759-274.723474A368.05956 368.05956 0 0 0 533.693182 20.871483h-7.458363z m-146.610107 775.030466a38.698249 38.698249 0 0 1-37.803246-39.891588v-32.049651a110.809965 110.809965 0 0 0-39.593253-85.238435A325.440343 325.440343 0 0 1 174.029607 384.669122a352.674023 352.674023 0 0 1 704.708758-0.511431 326.122251 326.122251 0 0 1-122.445012 254.8203l-1.960484 1.534292a110.469011 110.469011 0 0 0-37.504911 83.704143v31.921793a40.147303 40.147303 0 0 1-36.99348 40.019445z m152.576798-728.788616a332.088941 332.088941 0 0 0-331.236557 317.555788 304.429069 304.429069 0 0 0 114.603075 237.559517 131.736001 131.736001 0 0 1 47.222093 101.689453v31.836555a18.155787 18.155787 0 0 0 4.773352 13.59553 17.772214 17.772214 0 0 0 12.402192 5.540498h304.983119a19.221267 19.221267 0 0 0 17.047687-19.178647v-31.836556a131.352428 131.352428 0 0 1 46.881139-101.476356l3.196441-2.471915a304.727403 304.727403 0 0 0 111.790207-235.72689 329.787503 329.787503 0 0 0-317.257453-316.788642c-4.688114-0.127858-9.290989-0.213096-14.234819-0.213096z" fill="#ffffff" p-id="55641"></path><path d="M507.780697 636.506077a26.381295 26.381295 0 0 1-22.460327-40.104684l86.517011-141.282705h-140.643417a26.296057 26.296057 0 0 1-21.309609-41.809452l141.751517-191.530763a26.338676 26.338676 0 0 1 42.363502 31.367744L483.445124 402.526574h135.358634a26.381295 26.381295 0 0 1 26.338677 26.423914 26.082961 26.082961 0 0 1-3.878349 13.723388l-111.150919 181.387389a26.381295 26.381295 0 0 1-22.33247 12.444812z" fill="#ffffff" p-id="55642"></path><path d="M507.865936 646.947785a36.823004 36.823004 0 0 1-31.410363-56.001652l76.714591-125.385737h-121.933581a36.823004 36.823004 0 0 1-29.833452-58.388328l141.666278-191.786477a36.908242 36.908242 0 0 1 51.526634-7.671459 36.780385 36.780385 0 0 1 7.671459 51.441395l-98.0242 132.758862h114.560456a36.865623 36.865623 0 0 1 31.410364 56.04427l-111.150919 181.430008a36.993481 36.993481 0 0 1-31.154648 17.346022z" fill="#ffffff" p-id="55643"></path></svg>
    <div class="vip-text">创作中心</div>
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

  <div class="userInfo-div">
    <el-avatar :src= "userInfo.image" :size="70" class="uf-avatar"></el-avatar>
    <span class="username1">{{ userInfo.username }}</span>
    <span class="userDescription">{{ userInfo.description }}</span>
    
  <div class="uf-o-btn">
    <div class="follow-container">
      <div class="follow-wrapper1">
        <button 
          class="follow-button1"
          :class="{
            'following': isFollowing,
            'special': isSpecial
          }"
          @click="handleMainClick"
        >
          <span class="textp">
            <span v-if="!isSpecial">
              <span v-if="isFollowing">&nbsp;&nbsp;&nbsp;已关注</span>
              <span v-else>&nbsp;&nbsp;+&nbsp;关注</span>
            </span>
            <span v-else>已特别关注</span>
          </span>
        </button>
  
        <transition name="dropdown">
          <div v-if="showDropdown1" class="dropdown-menu1">
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
    <el-button @click="toChat" class="uf-message-button"><el-icon><ChatDotSquare /></el-icon>&nbsp;&nbsp;发信息</el-button>
  </div>
</div>

  <div>
    <el-menu
    :default-active="activeIndex"
    class="uf-tab"
    mode="horizontal"
    @select="handleSelect"
    router
  >
    <el-menu-item :index="`/${path}/index`">
      <svg t="1745236253166" class="tab-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="9136"><path d="M596.9 634.2c-22.4 18.7-52.6 29-85 29-32.2 0-62.3-10.2-84.6-28.7-12.8-10.6-31.7-8.8-42.2 4-10.6 12.8-8.8 31.7 4 42.2 33.1 27.4 76.8 42.5 122.9 42.5 46.4 0 90.3-15.3 123.5-43 12.7-10.6 14.4-29.5 3.8-42.3-10.7-12.6-29.6-14.3-42.4-3.7z" p-id="9137" fill="#11b81c"></path><path d="M959 413.4c-14.5-33.7-42.3-58.7-63-73.8L604 127.4c-55-40-129-40-184.1 0L128 339.6c-20.6 15-48.2 39.9-62.6 73.8-15.2 35.8-12.3 73 8.6 107.6 14.1 23.4 41.9 37.1 82.7 40.5 5.6 0.5 11.1 0.7 16.3 0.8v203.6c0 89.4 72.8 162.2 162.2 162.2h353.7c89.4 0 162.2-72.8 162.2-162.2V563.2c4.7 0 9.6-0.1 14.7-0.5 27.7-1.9 64.7-10.6 83.9-41.2 21.7-34.5 24.9-71.9 9.3-108.1z m-60.1 76.2c-8.6 13.8-48.5 15.8-72.8 11.9-16.4-2.7-31.8 8.4-34.5 24.7-0.3 1.9-0.4 3.9-0.4 5.8-0.1 1.2-0.2 2.4-0.2 3.7V766c0 27.2-10.7 52.8-30.1 72.2-19.4 19.4-45 30.1-72.2 30.1H335.2c-27.2 0-52.8-10.7-72.2-30.1-19.4-19.4-30.1-45-30.1-72.2V535.6c0-1.2-0.1-2.4-0.2-3.6 0-1.5 0-3-0.2-4.5-2-16.4-17-28.2-33.4-26.2-0.2 0-17.9 2.1-37.4 0.4-24.4-2.1-34.4-8.5-36.3-11.6-11-18.2-12.5-35.1-4.8-53.2 7-16.5 22.2-33.8 42.7-48.7l292-212.2c33.9-24.7 79.6-24.7 113.5 0l292 212.2c20.7 15.1 36.1 32.5 43.2 49 7.7 17.9 6.1 34.6-5.1 52.4z" p-id="9138" fill="#11b81c"></path></svg>
      <span class="tab-text">主页</span></el-menu-item>
    <el-menu-item :index="`/${path}/post`">
      <svg t="1743584588096" class="tab-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="35622"><path d="M545.04687527 520.78906277h-26.3671875c-116.36718777 0-210.9375-94.57031223-210.9375-210.9375s94.57031223-210.9375 210.9375-210.9375h26.3671875v421.875z m-52.734375-366.85546902c-74.70703152 12.65625-131.8359375 77.69531223-131.8359375 156.09375s57.12890598 143.4375 131.8359375 156.09375V153.93359375z" fill="rgb(255, 102, 153)" p-id="35623"></path><path d="M345.00781223 716.25781223c-116.36718777 0-210.9375-94.57031223-210.9375-210.9375v-26.3671875h421.875v26.3671875c0 116.19140598-94.57031223 210.9375-210.9375 210.9375z m-155.91796821-184.5703125c12.65625 74.70703152 77.69531223 131.8359375 156.09375 131.8359375s143.4375-57.12890598 156.09375-131.8359375H189.08984402z" fill="rgb(255, 102, 153)" p-id="35624"></path><path d="M540.4765625 889.92968777h-26.3671875v-421.875h26.3671875c116.36718777 0 210.9375 94.57031223 210.9375 210.9375s-94.74609402 210.9375-210.9375 210.9375z m26.3671875-366.85546902v312.01171902c74.70703152-12.65625 131.8359375-77.69531223 131.8359375-156.09375s-57.12890598-143.4375-131.8359375-155.91796902z" fill="rgb(255, 102, 153)" p-id="35625"></path><path d="M925.08593723 509.890625h-421.875v-26.3671875c0-116.36718777 94.57031223-210.9375 210.9375-210.9375s210.9375 94.57031223 210.9375 210.9375v26.3671875z m-366.85546821-52.734375h312.01171821c-12.65625-74.70703152-77.69531223-131.8359375-156.09375-131.8359375s-143.4375 57.12890598-155.91796821 131.8359375z" fill="rgb(255, 102, 153)" p-id="35626"></path></svg>
      <span class="tab-text">动态</span>
    </el-menu-item>
    <el-menu-item :index="`/${path}/upload`">
      <svg t="1745236745438" class="tab-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="18505" width="200" height="200"><path d="M876.8 269.056H668.416l71.936-71.936-36.224-36.224-107.136 107.136 1.024 1.024H515.84l1.024-1.024L409.6 160.896l-36.224 36.224 71.936 71.936H236.8c-42.368 0-76.8 34.432-76.8 76.8v448c0 42.368 34.432 76.8 76.8 76.8h640c42.368 0 76.8-34.432 76.8-76.8v-448c0-42.368-34.432-76.8-76.8-76.8z m25.6 524.8c0 14.08-11.52 25.6-25.6 25.6h-640c-14.08 0-25.6-11.52-25.6-25.6v-448c0-14.08 11.52-25.6 25.6-25.6h640c14.08 0 25.6 11.52 25.6 25.6v448z" fill="#1296db" p-id="18506"></path><path d="M514.432 449.152c-9.472-7.808-24.32-1.408-24.32 10.496v220.416c0 11.904 14.848 18.176 24.32 10.496l161.28-108.416c7.936-6.4 7.936-18.048 0-24.448l-161.28-108.544z" fill="#1296db" p-id="18507"></path></svg>
      <span class="tab-text">投稿</span>
    </el-menu-item>
    <el-menu-item :index="`/${path}/collection`">
      <svg t="1745236785430" class="tab-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="19585" width="200" height="200"><path d="M56.34645 289.395247l438.317343 181.372694q1.025889 0.425092 2.074451 0.801063 1.046672 0.374081 2.110346 0.695262 1.065565 0.32307 2.144355 0.59324 1.07879 0.27017 2.168915 0.487439 1.090125 0.21538 2.191586 0.379749 1.099572 0.16248 2.206702 0.272059 1.107129 0.10769 2.216147 0.162479 1.110908 0.05479 2.223705 0.05479 1.112797 0 2.223705-0.05479 1.109018-0.05479 2.216147-0.162479 1.107129-0.109579 2.206702-0.272059 1.101461-0.164369 2.191586-0.379749 1.090125-0.217269 2.168915-0.487439 1.07879-0.27017 2.144355-0.59324 1.063675-0.321181 2.110346-0.695262 1.048561-0.37597 2.074451-0.801063l438.317343-181.372694q1.029668-0.425092 2.038553-0.901195 1.006996-0.476103 1.989432-1.001329 0.982435-0.525225 1.938421-1.097682 0.955985-0.572458 1.881741-1.190259 0.925756-0.61969 1.821285-1.282834 0.895528-0.663144 1.757048-1.369741 0.86152-0.706598 1.687143-1.454761t1.613462-1.536q0.787838-0.787838 1.536-1.613461 0.748162-0.825624 1.45476-1.687144t1.369742-1.757048q0.663144-0.895528 1.282833-1.821284 0.617801-0.925756 1.190259-1.881741t1.097682-1.938421q0.525225-0.982435 1.001329-1.989432 0.476103-1.008886 0.901195-2.038553 0.426982-1.027779 0.801063-2.07634 0.374081-1.048561 0.697151-2.112236 0.32307-1.065565 0.59324-2.146244 0.27017-1.07879 0.48555-2.170804 0.217269-1.092015 0.379749-2.193476 0.164369-1.101461 0.272059-2.21048 0.109579-1.10524 0.16248-2.218037 0.05479-1.112797 0.05479-2.225594t-0.05479-2.223705q-0.056679-1.112797-0.164369-2.219926-0.109579-1.107129-0.273948-2.210479-0.164369-1.099572-0.381639-2.191587-0.217269-1.090125-0.487439-2.170805-0.272059-1.07879-0.595129-2.144354-0.32307-1.065565-0.699041-2.114125-0.37597-1.046672-0.801062-2.07445-0.426982-1.029668-0.903085-2.034775-0.476103-1.006996-1.001329-1.987543-0.525225-0.982435-1.097682-1.936531-0.572458-0.954096-1.192148-1.879853-0.617801-0.925756-1.280945-1.819394-0.663144-0.893638-1.369741-1.75327-0.706598-0.859631-1.45476-1.685254-0.748162-0.823734-1.536-1.611572-0.785948-0.785948-1.611572-1.534111-0.825624-0.746273-1.685255-1.452871-0.86152-0.704708-1.755159-1.367852-0.895528-0.661255-1.819394-1.280945-0.925756-0.617801-1.881742-1.190258-0.954096-0.572458-1.936532-1.095794-0.982435-0.525225-1.987542-1.001328-1.006996-0.474214-2.036664-0.899306l-438.317343-181.372694q-1.025889-0.425092-2.074451-0.801063-1.046672-0.374081-2.110346-0.695262-1.065565-0.32307-2.144355-0.59324-1.07879-0.27017-2.168915-0.487439-1.090125-0.21538-2.191586-0.377859-1.099572-0.164369-2.206702-0.273949-1.107129-0.10769-2.216147-0.16248Q513.112797 20.782288 512 20.782288q-1.112797 0-2.223705 0.054789-1.109018 0.05479-2.216147 0.16248-1.107129 0.109579-2.206702 0.272059-1.101461 0.164369-2.191586 0.379749-1.090125 0.217269-2.168915 0.487439-1.07879 0.27017-2.144355 0.59324-1.063675 0.321181-2.110346 0.695262-1.048561 0.37597-2.074451 0.801063l-438.317343 181.372694q-1.029668 0.425092-2.038553 0.901195-1.006996 0.476103-1.989432 1.001329-0.982435 0.525225-1.938421 1.095793-0.955985 0.574347-1.881741 1.192148-0.925756 0.61969-1.821285 1.284723-0.895528 0.661255-1.757048 1.367852-0.86152 0.706598-1.687143 1.45476-0.825624 0.750052-1.613462 1.536-0.787838 0.787838-1.536 1.613462-0.748162 0.825624-1.45476 1.687144t-1.369742 1.757048q-0.663144 0.895528-1.282833 1.821284-0.617801 0.925756-1.190259 1.881741t-1.097682 1.938421q-0.525225 0.982435-1.001329 1.991321-0.476103 1.006996-0.901195 2.036664-0.426982 1.027779-0.801063 2.07634-0.374081 1.048561-0.699041 2.112236-0.321181 1.065565-0.59135 2.146244-0.27017 1.07879-0.487439 2.172693-0.21538 1.090125-0.37786 2.191587-0.164369 1.101461-0.272059 2.21048-0.109579 1.10524-0.16248 2.218037-0.056679 1.110908-0.05479 2.225594 0 1.112797 0.056679 2.223704 0.0529 1.112797 0.16248 2.219927 0.109579 1.107129 0.273948 2.20859 0.164369 1.101461 0.381639 2.193476 0.217269 1.090125 0.487439 2.170805 0.272059 1.07879 0.595129 2.144354 0.32307 1.065565 0.699041 2.112236 0.37597 1.048561 0.801062 2.076339 0.426982 1.029668 0.903085 2.034775 0.476103 1.006996 1.001329 1.987543 0.525225 0.982435 1.095793 1.936531 0.574347 0.954096 1.192148 1.879853 0.61969 0.925756 1.284723 1.819394 0.661255 0.893638 1.367852 1.75327 0.706598 0.859631 1.45476 1.685254 0.748162 0.823734 1.536 1.611572 0.785948 0.785948 1.611572 1.534111 0.825624 0.746273 1.685255 1.452871 0.86152 0.704708 1.757048 1.367852 0.893638 0.663144 1.819395 1.280945 0.925756 0.617801 1.879852 1.190258t1.936532 1.095794q0.982435 0.525225 1.987542 0.999439 1.006996 0.476103 2.036664 0.901195zM512 379.798199L192.273948 247.498155 512 115.198111 831.727941 247.498155 512 379.798199zM91.018863 462.545712L512 636.742849l420.981137-174.197137q2.055557-0.852074 4.184797-1.496325 2.131129-0.64614 4.313269-1.080679 2.18214-0.432649 4.398288-0.651808Q948.091749 459.099631 950.317343 459.099631q1.112797 0 2.225594 0.05479 1.110908 0.05479 2.218037 0.164369 1.109018 0.10769 2.208591 0.272059 1.101461 0.16248 2.193476 0.379749 1.092015 0.217269 2.170804 0.487439 1.080679 0.27017 2.146244 0.59324t2.112236 0.69904q1.048561 0.374081 2.076339 0.801063 1.029668 0.425092 2.034775 0.901196 1.006996 0.476103 1.989432 1.001328 0.980546 0.525225 1.934642 1.095793 0.955985 0.572458 1.881742 1.192148 0.925756 0.617801 1.819394 1.280945 0.893638 0.663144 1.75327 1.369741 0.86152 0.704708 1.685254 1.452871 0.825624 0.748162 1.613462 1.534111 0.785948 0.787838 1.53411 1.613461 0.746273 0.823734 1.452871 1.685255 0.706598 0.859631 1.369742 1.753269 0.663144 0.893638 1.280945 1.819395 0.61969 0.925756 1.190258 1.881742 0.572458 0.954096 1.097683 1.934642 0.525225 0.982435 1.001328 1.989431 0.476103 1.005107 0.901196 2.034775 0.426982 1.027779 0.801062 2.07634 0.37597 1.046672 0.699041 2.112236 0.32307 1.065565 0.59324 2.144354 0.27017 1.080679 0.487439 2.172694 0.217269 1.092015 0.379749 2.191587 0.164369 1.101461 0.272059 2.210479 0.109579 1.107129 0.164369 2.218037 0.05479 1.112797 0.05479 2.225594 0 0.83507-0.030229 1.670141-0.032118 0.833181-0.092576 1.666361-0.062347 0.833181-0.153033 1.662583-0.092576 0.829402-0.21538 1.656915-0.122804 0.825624-0.275838 1.64558-0.153033 0.821845-0.336295 1.636133-0.181373 0.814288-0.394863 1.622907-0.213491 0.806731-0.455321 1.605904t-0.51389 1.588901q-0.272059 0.789727-0.572457 1.568118t-0.629137 1.547336q-0.330627 0.767055-0.687705 1.520885-0.357077 0.75572-0.740605 1.496325-0.385417 0.740605-0.797284 1.467985-0.411867 0.725491-0.850184 1.437757-0.438317 0.710376-0.901196 1.403749-0.464768 0.695262-0.954096 1.371631t-1.003218 1.333845q-0.513889 0.659365-1.05045 1.297948-0.53845 0.638583-1.099572 1.256384-0.561122 0.61969-1.143026 1.216708-0.583793 0.597018-1.188369 1.173255t-1.231823 1.127911q-0.625358 0.553565-1.271498 1.082569-0.644251 0.530893-1.309284 1.035335-0.665033 0.506332-1.34707 0.986215-0.683926 0.479882-1.382967 0.935202-0.70093 0.455321-1.416974 0.884192-0.716044 0.428871-1.447203 0.831292-0.733048 0.402421-1.479321 0.778391-0.744384 0.37597-1.503882 0.723601-0.759498 0.345742-1.532221 0.665034l-438.317343 181.372693q-1.025889 0.425092-2.074451 0.801063-1.046672 0.374081-2.110346 0.695262-1.065565 0.32307-2.144355 0.59324-1.07879 0.27017-2.168915 0.487439-1.090125 0.21538-2.191586 0.379749-1.099572 0.16248-2.206702 0.272059-1.107129 0.10769-2.216147 0.16248-1.110908 0.05479-2.223705 0.05479-1.112797 0-2.223705-0.05479-1.109018-0.05479-2.216147-0.16248-1.107129-0.109579-2.206702-0.272059-1.101461-0.164369-2.191586-0.379749-1.090125-0.217269-2.168915-0.487439-1.07879-0.27017-2.144355-0.59324-1.063675-0.321181-2.110346-0.695262-1.048561-0.37597-2.074451-0.801063l-438.317343-181.372693q-0.772723-0.319292-1.530332-0.665034-0.761387-0.347631-1.505771-0.723601-0.746273-0.37597-1.479321-0.778391-0.731159-0.402421-1.447203-0.831292-0.717934-0.428871-1.416974-0.884192-0.699041-0.455321-1.382967-0.935202-0.682037-0.479882-1.34707-0.986215-0.665033-0.504443-1.309284-1.035335-0.64614-0.529004-1.271498-1.082569-0.625358-0.551675-1.231823-1.127911-0.604576-0.576236-1.188369-1.173255-0.581904-0.597018-1.143026-1.216708-0.561122-0.617801-1.099572-1.256384-0.536561-0.638583-1.05045-1.297948-0.513889-0.657476-1.003218-1.333845-0.491218-0.676369-0.954096-1.371631-0.462878-0.693373-0.901196-1.403749-0.438317-0.712266-0.850184-1.437757-0.411867-0.72738-0.797284-1.467985-0.383528-0.740605-0.740605-1.496325-0.358967-0.75383-0.687705-1.520885-0.328738-0.768945-0.629137-1.547336-0.300399-0.778391-0.572457-1.568118t-0.51389-1.588901q-0.24183-0.799173-0.455321-1.605904-0.213491-0.80862-0.396752-1.622907-0.181373-0.814288-0.334406-1.636133-0.153033-0.819956-0.275838-1.64558-0.122804-0.827513-0.21538-1.656915-0.090686-0.829402-0.151144-1.662583-0.064236-0.833181-0.094465-1.666361Q28.339483 505.277875 28.339483 504.442804q0-1.112797 0.05479-2.225594 0.05479-1.110908 0.164369-2.218037 0.10769-1.109018 0.272059-2.210479 0.16248-1.099572 0.379749-2.191587 0.217269-1.092015 0.487439-2.172694 0.27017-1.07879 0.59324-2.144354 0.32307-1.065565 0.699041-2.112236 0.374081-1.048561 0.801062-2.07634 0.425092-1.029668 0.901196-2.034775 0.476103-1.006996 1.001328-1.989431 0.525225-0.980546 1.095794-1.936532 0.572458-0.954096 1.190258-1.879852 0.61969-0.925756 1.282834-1.819395 0.663144-0.893638 1.369742-1.753269 0.704708-0.86152 1.45476-1.685255 0.746273-0.825624 1.532221-1.613461 0.787838-0.785948 1.613462-1.534111 0.823734-0.748162 1.685254-1.452871 0.859631-0.706598 1.75327-1.369741 0.893638-0.663144 1.819394-1.280945 0.925756-0.61969 1.879853-1.192148 0.955985-0.570568 1.93842-1.095793 0.982435-0.525225 1.987543-1.001328t2.034775-0.901196q1.027779-0.426982 2.076339-0.801063 1.046672-0.37597 2.112236-0.69904 1.065565-0.32307 2.144355-0.59324 1.080679-0.27017 2.172693-0.487439 1.092015-0.217269 2.191587-0.379749 1.101461-0.164369 2.21048-0.272059 1.107129-0.109579 2.218037-0.164369Q72.56986 459.099631 73.682657 459.099631q2.225594 0 4.439852 0.217269 2.216148 0.219159 4.398288 0.651808 2.18214 0.434539 4.313269 1.080679 2.12924 0.644251 4.184797 1.496325z m0.041565 256.961653L512 893.687498l420.947129-174.183911 0.034008-0.013225q2.055557-0.852074 4.184797-1.496325 2.131129-0.64614 4.313269-1.080679 2.18214-0.432649 4.398288-0.651808Q948.091749 716.04428 950.317343 716.04428q1.112797 0 2.225594 0.05479 1.110908 0.05479 2.218037 0.164369 1.109018 0.10769 2.208591 0.272059 1.101461 0.16248 2.193476 0.379749 1.092015 0.217269 2.170804 0.487439 1.080679 0.27017 2.146244 0.59324t2.112236 0.699041q1.048561 0.374081 2.076339 0.801063 1.029668 0.425092 2.034775 0.901195 1.006996 0.476103 1.989432 1.001329 0.980546 0.525225 1.934642 1.095793 0.955985 0.572458 1.881742 1.192147 0.925756 0.617801 1.819394 1.280945 0.893638 0.663144 1.75327 1.369742 0.86152 0.706598 1.685254 1.452871 0.825624 0.748162 1.613462 1.53411 0.785948 0.787838 1.53411 1.613462 0.746273 0.823734 1.452871 1.685254 0.706598 0.859631 1.369742 1.75327 0.663144 0.893638 1.280945 1.819394 0.61969 0.925756 1.190258 1.881742 0.572458 0.954096 1.097683 1.934642 0.525225 0.982435 1.001328 1.989432 0.476103 1.005107 0.901196 2.034775 0.426982 1.027779 0.801062 2.076339 0.37597 1.046672 0.699041 2.112236 0.32307 1.065565 0.59324 2.144355 0.27017 1.080679 0.487439 2.172693 0.217269 1.092015 0.379749 2.191587 0.164369 1.101461 0.272059 2.21048 0.109579 1.107129 0.164369 2.218037 0.05479 1.112797 0.05479 2.225594 0 0.83507-0.030229 1.67014-0.032118 0.833181-0.092576 1.666362-0.062347 0.833181-0.153033 1.662583-0.092576 0.829402-0.21538 1.656915-0.122804 0.825624-0.275838 1.645579-0.153033 0.821845-0.334406 1.636133-0.183262 0.814288-0.396752 1.622908-0.213491 0.806731-0.455321 1.605904t-0.51389 1.5889q-0.272059 0.789727-0.572457 1.568118t-0.629137 1.547336q-0.330627 0.767055-0.687705 1.520886-0.357077 0.75572-0.740605 1.496324-0.385417 0.740605-0.797284 1.467986-0.411867 0.725491-0.850184 1.437756-0.436428 0.710376-0.901196 1.403749-0.464768 0.695262-0.954096 1.371631t-1.003218 1.333845q-0.513889 0.659365-1.05045 1.297949-0.53845 0.638583-1.099572 1.256383-0.561122 0.61969-1.143026 1.216709-0.583793 0.597018-1.188369 1.173254t-1.231823 1.127912q-0.625358 0.553565-1.271498 1.082568-0.644251 0.530893-1.309284 1.035336-0.665033 0.506332-1.34707 0.986214-0.683926 0.479882-1.382967 0.935203-0.70093 0.455321-1.416974 0.884192-0.716044 0.428871-1.447203 0.831291-0.733048 0.402421-1.479321 0.778391-0.744384 0.37597-1.503882 0.723602-0.759498 0.345742-1.532221 0.665033l-0.032118 0.015115-438.285225 181.357579q-1.025889 0.425092-2.074451 0.801063-1.046672 0.374081-2.110346 0.695262-1.065565 0.32307-2.144355 0.593239-1.07879 0.27017-2.168915 0.48744-1.090125 0.21538-2.191586 0.379749-1.099572 0.16248-2.206702 0.272059-1.107129 0.10769-2.216147 0.162479-1.110908 0.05479-2.223705 0.05479-1.112797 0-2.223705-0.05479-1.109018-0.05479-2.216147-0.162479-1.107129-0.109579-2.206702-0.272059-1.101461-0.164369-2.191586-0.379749-1.090125-0.217269-2.168915-0.48744-1.07879-0.27017-2.144355-0.593239-1.063675-0.321181-2.110346-0.695262-1.048561-0.37597-2.074451-0.801063L56.386125 803.30155l-0.039675-0.017004q-0.772723-0.319292-1.530332-0.665033-0.761387-0.347631-1.505771-0.723602-0.746273-0.37597-1.479321-0.778391-0.731159-0.402421-1.447203-0.831291-0.717934-0.428871-1.416974-0.884192-0.699041-0.455321-1.382967-0.935203-0.682037-0.479882-1.34707-0.986214-0.665033-0.504443-1.309284-1.035336-0.64614-0.529004-1.271498-1.082568-0.625358-0.551675-1.231823-1.127912-0.604576-0.576236-1.188369-1.173254-0.581904-0.597018-1.143026-1.216709-0.561122-0.617801-1.099572-1.256383-0.536561-0.638583-1.05045-1.297949-0.513889-0.657476-1.003218-1.333845-0.491218-0.676369-0.954096-1.371631-0.462878-0.693373-0.901196-1.403749-0.438317-0.712266-0.850184-1.437756-0.411867-0.72738-0.797284-1.467986-0.383528-0.740605-0.740605-1.496324-0.358967-0.75383-0.687705-1.520886-0.328738-0.768945-0.629137-1.547336-0.300399-0.778391-0.572457-1.568118t-0.51389-1.5889q-0.24183-0.799173-0.455321-1.605904-0.213491-0.80862-0.396752-1.622908-0.181373-0.814288-0.334406-1.636133-0.153033-0.819956-0.275838-1.645579-0.122804-0.827513-0.21538-1.656915-0.090686-0.829402-0.151144-1.662583-0.064236-0.833181-0.094465-1.666362Q28.339483 762.222524 28.339483 761.387454q0-1.112797 0.05479-2.225594 0.05479-1.110908 0.164369-2.218037 0.10769-1.109018 0.272059-2.21048 0.16248-1.099572 0.379749-2.191587 0.217269-1.092015 0.487439-2.172693 0.27017-1.07879 0.59324-2.144355 0.32307-1.065565 0.699041-2.112236 0.374081-1.048561 0.801062-2.076339 0.425092-1.029668 0.901196-2.034775 0.476103-1.006996 1.001328-1.989432 0.525225-0.980546 1.095794-1.936531 0.572458-0.954096 1.190258-1.879853 0.61969-0.925756 1.282834-1.819394 0.663144-0.893638 1.369742-1.75327 0.704708-0.86152 1.45476-1.685254 0.746273-0.825624 1.532221-1.613462 0.787838-0.785948 1.613462-1.53411 0.823734-0.746273 1.685254-1.452871 0.859631-0.706598 1.75327-1.369742 0.893638-0.663144 1.819394-1.280945 0.925756-0.61969 1.879853-1.192147 0.955985-0.570568 1.93842-1.095793 0.982435-0.525225 1.987543-1.001329t2.034775-0.901195q1.027779-0.426982 2.076339-0.801063 1.046672-0.37597 2.112236-0.699041 1.065565-0.32307 2.144355-0.59324 1.080679-0.27017 2.172693-0.487439 1.092015-0.217269 2.191587-0.379749 1.101461-0.164369 2.21048-0.272059 1.107129-0.109579 2.218037-0.164369Q72.56986 716.04428 73.682657 716.04428q2.225594 0 4.439852 0.21727 2.216148 0.219159 4.398288 0.651808 2.18214 0.434539 4.313269 1.080679 2.12924 0.644251 4.184797 1.496325l0.041565 0.017003z" p-id="19586" fill="#1296db"></path></svg>
      <span class="tab-text">合集</span>
    </el-menu-item>
    <el-menu-item :index="`/${path}/favorites`">
      <svg t="1745236842732" class="tab-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="20636" width="200" height="200"><path d="M447 134.6zM867.7 344.7l-187.3-27.2c-7.8-1.1-14.6-6-18.1-13.1l-83.7-169.7c-12.5-25.3-37.7-40.9-65.8-40.9-28.2 0-53.4 15.7-65.8 40.9l-83.7 169.7c-3.5 7.1-10.3 12-18.1 13.1l-187.3 27.2c-15.9 2.3-30.8 9.9-42 21.4l-2.7 2.9-0.3 0.3c-25.4 29.3-23.5 73.5 4.3 100.6L252.7 602c5.7 5.5 8.2 13.5 6.9 21.2l-32 186.6c-2.7 15.8-0.1 32.4 7.4 46.6l1.8 3.3 0.2 0.3c13.7 22.8 38 35.6 63 35.6 11.5 0 23.2-2.7 34.1-8.4l167.5-88.1c7-3.7 15.3-3.7 22.3 0l167.5 88.1c14.2 7.5 30.8 10.1 46.6 7.4l3.6-0.7 0.3-0.1c38-8.7 62.6-45.6 56-84l-32-186.5c-1.3-7.8 1.2-15.7 6.9-21.2L908.3 470c11.5-11.2 19.1-26.1 21.4-42l0.4-3.6v-0.4c3.7-38.9-23.8-73.7-62.4-79.3zM341.8 603.8c2.2-12.6-2-25.5-11.2-34.4l-135.5-132c-7.1-6.9-3.2-19 6.6-20.5L389 389.7c12.7-1.8 23.6-9.8 29.3-21.3L502 198.8c4.4-8.9 17.1-8.9 21.5 0l83.7 169.6c5.7 11.5 16.6 19.4 29.3 21.3l187.2 27.2c9.8 1.4 13.8 13.5 6.6 20.5l-135.5 132c-9.2 9-13.3 21.8-11.2 34.4l32 186.4c1.7 9.8-8.6 17.3-17.4 12.6l-167.4-88c-11.3-6-24.9-6-36.2 0l-167.4 88c-8.8 4.6-19.1-2.8-17.4-12.6l32-186.4z" fill="#ea9518" p-id="20637" data-spm-anchor-id="a313x.search_index.0.i17.57293a8152IShl" class=""></path><path d="M632.9 548.5c-3.6-13.5-14.3-24.1-27.9-27.6-13.6-3.5-28 0.6-37.7 10.7-12 12.2-28.7 19.1-45.8 19.1h-0.2c-17 0-33.6-6.9-45.6-19-15.2-15.2-39.8-15.2-55 0-15.2 15.2-15.2 39.8 0 55 26.5 26.6 63.1 41.8 100.6 41.8h0.4c37.7 0 74.5-15.4 101.1-42.2 9.8-9.8 13.7-24.3 10.1-37.8z" fill="#ea9518" p-id="20638" data-spm-anchor-id="a313x.search_index.0.i19.57293a8152IShl" class="selected"></path></svg>
      <span class="tab-text">收藏</span>
    </el-menu-item>
  </el-menu>
  </div>
  <div class="userInfo-report">
            <div class="userInfo-item-report">
              <el-statistic title="关注数" :value=userInfo.followingCount value-style="{font-size '8px'}" />
            </div>
            <div class="userInfo-item-report">
              <el-statistic title="粉丝数" :value=userInfo.followersCount />
            </div>
            <div class="userInfo-item-report">
              <el-statistic title="获赞数" :value=userInfo.allLikes />
            </div>
            <div class="userInfo-item-report">
              <el-statistic title="播放数" :value=userInfo.allViews />
            </div>
          </div>
  <div class="userInfo-content">
    <router-view></router-view>
  </div>
</template>

<style scoped>
.uf-o-btn{
  display: flex;
  position: absolute;
  top: 30px;
  left: 1100px;
}


.uf-message-button{
  background-color: transparent;
  color: rgb(255, 255, 255);
  font-size: 15px;
  font-weight: 500;
  width: 150px;
  height: 36px;
  border-radius: 8px;
  margin-left: 10px;
}

/* 控制 el-col 内部元素的居中对齐 */
.userInfo-report {
  display: flex;
  align-items: center;  /* 水平居中 */
  justify-content: center;  /* 垂直居中 */
  width: 500px;
  position: absolute;
  align-self: center;
  top: 185px;
  left: 1130px;
}

.userInfo-item-report{
  align-items: center;  /* 水平居中 */
  justify-content: center;  /* 垂直居中 */
  align-self: center;
  margin-left: 20px;
}

.userInfo{
  display: flex;
  align-items: center;  /* 水平居中 */
  justify-content: center;  /* 垂直居中 */
}

/* 针对 el-statistic 组件的标题和数值单独调整 */
.userInfo-item-report .el-statistic__head,
.userInfo-item-report .el-statistic__content {
  text-align: center;
}

.userInfo-item{
  align-items: center;  /* 水平居中 */
  justify-content: center;  /* 垂直居中 */
}

/* 针对 el-statistic 组件的标题和数值单独调整 */
.userInfo-item .el-statistic__head,
.userInfo-item .el-statistic__content {
  text-align: center;
}

/* 新增背景层 */
.background-layer-userInfo {
  position: absolute;
  z-index: -10; /* 确保在内容层下方 */
  top: 0;
  left: 0;
  width: 100%;
  height: 120%;
  background-image: url('@/assets/pic/个人主页顶栏背景图片.avif');
  background-size: cover;
  background-position: center;
}

/* 顶部阴影层 */
.uf-shadow-layer {
  position: absolute;
  z-index: 0;
  top: 0;
  left: 0;
  width: 100%;
  height: 70%;  /* 控制阴影覆盖范围 */
  background: linear-gradient(
    to bottom, 
    rgba(0, 0, 0, 0.721) 0%,  /* 顶部阴影浓度 */
    rgba(0, 0, 0, 0) 100%   /* 渐变到透明 */
  );
  pointer-events: none; /* 防止遮挡交互 */
}

/* 底部阴影层 */
.uf-shadow-layer-bottom {
  position: absolute;
  z-index: 0;
  top: 75px;
  left: 0;
  width: 100%;
  height: 70%;  /* 控制阴影覆盖范围 */
  background: linear-gradient(
    to top, 
    rgba(72, 72, 72, 0.721) 0%,  /* 顶部阴影浓度 */
    rgb(0, 0, 0, 0) 80%   /* 渐变到透明 */
  );
  pointer-events: none; /* 防止遮挡交互 */
}

.userInfo-div{
  position: absolute;
  top:100px;
  left: 140px;
  width: 1500px;
}

.username1{
  position: absolute;
  top: 0px;
  left: 85px;
  font-size: 25px;
  font-weight: 600;
  color: rgb(255,255,255);
}

.userDescription{
  position: relative;
  top: -15px;
  left: 15px;
  color: rgb(255,255,255);
  font-size: 12px;
  font-size: 500;
}

.update-userInfo{
  position: absolute;
  top: 35px;
  right: 120px;
}

.edit-text{
  color: rgb(255,255,255);
}

.update-userInfo:hover{
  background-color: transparent !important;
}

.update-userInfo .edit-text:hover{
  color: rgb(251, 114, 153);
}

.uf-tab{
  margin-top: 30px;
  width: 1450px;
  margin-left: 100px;
}

.tab-icon{
  width: 20px;
  height: 20px;
}

.tab-text{
  font-weight: 500;
  margin-left: 5px;
}

.follow-wrapper1 {
  }
  
  .follow-button1 {
    display: flex;
    align-items: center;
    padding: 8px 16px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    background: rgb(55, 194, 240);
    color: white;
    position: relative;
    overflow: hidden;
    width: 150px;
    height: 35px;
  
    .textp {
      font-size: 15px;
      font-weight: 500;
      margin-left: 25px;
    }
  
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 3px 8px rgba(0, 132, 255, 0.3);
    }
  
    &.following {
      background: transparent;
      border: #f5f5f5 1px solid;
      &:hover {
        box-shadow: 0 3px 8px rgba(102, 102, 102, 0.3);
      }
    }
  
    &.special {
      background: linear-gradient(45deg, #ff9500, #ff6b6b);
    }
  }
  
  .dropdown-menu1 {
    position: absolute;
    top: 110%;
    left: 7px;
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
