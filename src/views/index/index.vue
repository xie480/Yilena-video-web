<script>
import { ref,onMounted,computed } from 'vue'
import { useRouter } from 'vue-router';
import { useTransition } from '@vueuse/core'
import CryptoJS from 'crypto-js'
import { ElMessage,ElMessageBox } from 'element-plus';
import { findAllVideos,getUserInfoByUsername,getHotSearch } from '../../api';

export default {
  setup() {

// 热搜数据
const hotSearchData = ref([
  {
    id: '',
    content :'',
    searchCount: '',
    createTime: ''
  }
]);

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

// 当前显示的热搜内容
const currentHotSearchContent = ref('');

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

const addSearch = (content) => {
  searchText.value = content;
  addSearchHistory(content);
}

const clearSearchHistory = () => {
  searchHistory.value = [];
   // 清空localStorage
   localStorage.removeItem('searchHistory');
};


    const showDropdown = ref(false);

    const videosList = ref([]);
    const videosListSize = ref(0);

    const searchAllVideos = async () => {
      const result = await findAllVideos();
      if(result.code === 1){
        videosList.value = result.data;
        videosListSize.value = result.data.length;
      }
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
      reason: ''
    });

    const getUserInfo = async () => {
      //获取登录用户名
      let loginUser = JSON.parse(localStorage.getItem('loginUser'))
      if (loginUser) {
        userInfo.value.username = loginUser.username;
        const result = await getUserInfoByUsername(userInfo.value.username);
        if(result.code === 1){
          userInfo.value = result.data;
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

    const recommendVideos = computed(() => {
      return videosList.value.slice(0, 6); // 取前6条数据
    });

    const getImageUrl = (item) => {
      return `src/assets/pic/index-${item}.png`;
    }

const url = 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg';
const fits = 'scale-down'

    // 在 setup() 中添加状态
const selectedFavoriteId = ref(null);
const videoData = ref([]);
const videoPage = ref(1);
const videoHasMore = ref(true);

// 点击收藏夹的处理函数
const handleFavoriteClick = (favoriteId) => {
  selectedFavoriteId.value = favoriteId;
  videoData.value = [];
  videoPage.value = 1;
  videoHasMore.value = true;
  FavoritesVideoload(); // 立即加载
};

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

// 修改后的视频加载方法
const FavoritesVideoload = () => {
  if (!selectedFavoriteId.value || !videoHasMore.value || loading.value) return;
  loading.value = true;
  
  setTimeout(() => {
    const newVideos = Array.from({ length: 5 }, (_, i) => ({
      id: (videoPage.value - 1) * 10 + i + 1,
      title: `收藏夹${selectedFavoriteId.value}的视频${i + 1}`,
      author: `作者${i + 1}`,
      time: `${Math.floor(Math.random() * 24)}小时前`,
    }));
    
    videoData.value.push(...newVideos);
    videoPage.value += 1;
    loading.value = false;
    
    if (videoPage.value > 1) videoHasMore.value = false;
  }, 5);
};

    const PostHasMore = ref(true)
    const count = ref(0)
    const Postload = () => {
      if(!PostHasMore.value) return;
      count.value += 2
      if (count.value >= 10) {
        PostHasMore.value = false
      }
    } 

    const count1 = ref(0)
    const Favoritesload = () => {
      count1.value += 2 
    }

    const count2 = ref(0)
    const FavoriteVideoHasMore = ref(true)
    // const FavoritesVideoload = () => {
    //   if(!FavoriteVideoHasMore.value) return;
    //   count2.value += 2
    //   if (count2.value >= 10) {
    //     FavoriteVideoHasMore.value = false 
    //   }
    // }

    const source = ref(0)
    const outputValue = useTransition(source, {
      duration: 1500,
    })
    source.value = 172000

    const router = useRouter();

    const testCover = ref("src/assets/pic/index-2.png")

    // 响应式数据
    const videoGroups = ref([])
    const loading = ref(false)
    const hasMore = ref(true)
    let currentPage = 1

    function formatViews(views) {
  if (views >= 10000) {
    return `${Math.round(views / 1000) / 10}万`; // 保留1位小数（如：1.3万）
  }
  return views.toString();
}

function formatTime(createdTime) {
  const now = new Date();
  const targetTime = new Date(createdTime);
  const diff = now - targetTime;
  const diffDays = Math.floor(diff / (1000 * 3600 * 24));

  if (diff < 86400000) {
    const hours = Math.floor(diff / (1000 * 3600));
    return `${hours}小时前`;
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


    // 生成视频数据
    const generateVideos = (page) => {
      const itemsPerPage = 10; // 每页显示10条
      const startIndex = (page - 1) * itemsPerPage + 6;
      const endIndex = startIndex + itemsPerPage;

  // 分页切片后遍历实际数据
  return videosList.value
  .slice(startIndex, endIndex)
  .map((video, index) => ({
    id: video.id,
    title: video.title,
    author: video.username,
    authorId: video.userId,
    time: video.createdTime, // 动态时间显示
    url: video.coverUrl,
    views: formatViews(video.views), // 万级格式化
    barrage: video.barrages,
    time1: video.time
    }));
    }

    // 加载方法
    const load = () => {
      if (loading.value || !hasMore.value) return
      
      loading.value = true
      setTimeout(() => {
        const newVideos = generateVideos(currentPage)
        
        // 分割为两组（两行）
        const group1 = newVideos.slice(0, 5)
        const group2 = newVideos.slice(5, 10)
        videoGroups.value.push(...[group1, group2])
        
        currentPage++
        loading.value = false
        
        // 模拟数据加载完毕（假设加载3页后无数据）
        if(currentPage > (videosListSize.value - 6)/5) hasMore.value = false
      }, 1000)
    }

    const reloadIndex = () => {
      router.push('/index').then(() => {
        window.scrollTo(0, 0); // 滚动到页面顶部
      window.location.reload();
      });
    }

    const toChat = () => {
      router.push('/chat').then(() => {
        window.location.reload();
});
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

    const toUserInfo = (id) => {
      if(id === userInfo.value.id){
       router.push('/userInfo/' + id).then(() => {
        window.location.reload();
      })
      }else{
        router.push('/userInfo/others/' + id).then(() => {
          window.location.reload();
        })
      }
    }

    const toUpload = () => {
      router.push('/upload').then(() => {
        window.location.reload();
      })
    }

    const toFavorites = () => {
      router.push('/userInfo/' + userInfo.value.id + '/favorites').then(() => {
        window.location.reload();
      })
    }

    onMounted(() => {
      load();
      searchAllVideos();
      getUserInfo();
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

  getHotSearchData();
    })

    const playVideo = (videoId) => {
      router.push(`/video/${videoId}`).then(() => {
        window.location.reload();
      });
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

// 处理空格键事件
const handleSpace = () => {
  if (searchText.value.trim() !== '') { 
    addSearchHistory(searchText.value.trim()); // 传递处理后的内容
  }
};

    return { videoGroups,toFavorites, loading, hasMore, load,reloadIndex, 
      outputValue, source, count, Postload, 
      Favoritesload,count1,FavoritesVideoload,count2,
      FavoriteVideoHasMore,PostHasMore,toChat,toHistory,toPost,
      toSearch,toUserInfo,toUpload,handleFavoriteClick,videoData,FavoritesVideoload,videoPage,videoHasMore,selectedFavoriteId,
      url,fits,getImageUrl,testCover,recommendVideos,formatTime,formatViews
    ,searchAllVideos,userInfo,playVideo,logout,
    showDropdown,searchHistory,removeHistory,
  addSearchHistory,searchText,clearSearchHistory,hotSearchData
,addSearch,currentHotSearchContent,formatVideoDuration,handleSpace,toAnswerMe,toAtMe,toLikeMe,toMymessage,toOtherMessage,toSystemMessage }
  }
}
</script>
<!-- ---------------------------------------------------------------------------------------------------------------------- -->
<template>
  <!-- 顶栏 -->
  <el-row>
    <el-col :span="24">

      <!-- 背景图片 -->
      <div class="background-layer"></div>
      <!-- logo -->
      <div class="logo"></div>
      <!-- 渐变阴影 -->
      <div class="top-shadow-layer"></div>

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
             <button class="left-button-4" @click="toFavorites">
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


  <!-- 分类栏 -->
   <el-row>
    <el-col :span="24">
      <div class="sort">

        <el-row gutter="15">
        <!-- 左侧路由跳转 -->
        <el-col :span="4">
         <div class="sort-left">
          <div class="sort-left-botton">
             <button class="sort-left-botton-1">
              <svg t="1743742602191" class="sort-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4373" width="200" height="200"><path d="M505 2l19.3 0.2 9.1 0.3 13 0.7c5.339 0.826 10.726 0.477 15.8 1.3 8.587 1.391 17.1 1.522 25.2 3.1 9.807 1.91 19.486 2.838 28.705 5.1 10.179 2.5 20.1 4.1 29.806 7 24.787 7.4 48.178 15.135 70.414 24.8a491.263 491.263 0 0 1 86.517 48.4c9.082 6.382 17.744 13.267 26.5 19.9 7.845 5.94 14.891 12.676 22.5 18.8l9.5 8.9 7.1 6.8a81.662 81.662 0 0 1 11.2 11.3l12.8 13.6c8.49 10.543 17.69 20.511 25.9 31.4 27.6 36.581 49.9 76.226 68.513 122 8.992 22.108 15.817 45.865 22.107 70.8 2.65 10.513 4.24 21.433 6.4 32.4 2.43 12.364 2.74 25.292 4.8 38.3l0.9 13.6 0.9 19V519q-0.105 6-0.2 12c-0.13 2.933-0.27 5.867-0.4 8.8q-0.945 12.249-1.9 24.5l-1.5 13.1c-1.94 10.547-2.77 21.133-5 31.3-7.64 34.765-16.161 66.865-28.707 96.5-14.983 35.388-32.324 68.03-53.111 97.6-7 9.961-14.384 19.566-21.8 29.1l-9.9 11.7c-4.989 6.269-10.895 11.7-15.9 17.9l-6.8 6.9a57.692 57.692 0 0 1-8.3 8.3c-3.59 2.9-6.483 6.68-10.1 9.6-5.754 4.642-10.781 10.288-16.6 14.9-11.287 8.939-22.353 18.188-34.207 26.6-45.335 32.173-96.364 57.117-156.231 75-19.6 5.85-40.259 9.2-61.812 13.4l-26.205 3.3-23.5 1.8c-2.933 0.13-5.868 0.27-8.8 0.4l-12 0.3q-7-0.045-14-0.1c-2.934-0.07-5.868-0.13-8.8-0.2q-6-0.195-12-0.4l-9.5-0.6c-7.957-1.24-16.261-1.08-23.9-2.4l-16.3-2.1c-12.961-2.55-25.8-4.18-38.108-7.3-18.284-4.64-36.405-8.935-53.11-15.4-14.828-5.738-29.091-11.317-43.109-17.5-35.469-15.644-67.384-35.684-96.618-57.7-9.635-7.256-18.481-15.416-27.806-22.9-5.265-4.226-9.772-9.278-15-13.5l-5.2-5.2a81.662 81.662 0 0 1-11.2-11.3l-6.7-6.9c-5.385-6.667-11.556-12.641-16.9-19.4-13.2-16.686-26.614-33.323-38.207-51.7a566.761 566.761 0 0 1-47.509-94.1c-5.454-13.737-9.576-28.018-14-42.8-2.955-9.867-5.019-20.128-7.6-30.6-2.169-8.794-3.261-17.964-5.1-27.3-2.245-11.394-2.588-23.619-4.5-35.7l-1-13.6-0.822-19V505l0.1-5.3 0.2-9.1c1.127-7.346 0.389-15.118 1.5-22.2l0.7-8.7 3.2-25.2 4.7-26.2c5.739-23.254 10.52-45.842 18.6-66.7 3.595-9.275 6.874-18.586 10.7-27.6l11.1-24.5a232.349 232.349 0 0 1 12-22.9c8.524-14.574 16.627-28.926 26.105-42.4 6.514-9.261 13.043-18.263 19.8-27.2l15.6-18.9 15.7-17.3 5.2-5.2a81.67 81.67 0 0 1 11.3-11.2c4.5-3.636 8.256-8.241 12.8-11.9 8.677-6.983 16.656-14.924 25.6-21.7 12.829-9.714 25.8-19.491 39.608-28.2 10.4-6.559 21.147-12.618 31.906-18.9 22.974-13.413 48.877-23.157 75.115-33.3l14.2-4.6c12.408-3.914 25.42-7.375 38.607-10.7l25.4-5.2L447.387 6c8.885-1.573 18.156-1.287 27.306-2.7l11.4-0.7z m95.218 191.4l-11 0.2-7.6 0.1-16.7 0.8-19 1.5c-2.559 0.434-5.65 0.212-8.4 0.8-3.374 0.72-6.974 0.7-10.6 1.4-6.805 1.316-13.959 2.1-20.4 3.5l-9.6 1.6c-5.793 1.622-12.108 2.506-18 4.3-9.577 2.915-19.431 5.869-28.306 9.3l-5.7 1.8c-11.188 5.266-22.437 9.875-32.807 15.9-30.007 17.435-54.193 41.743-70.013 73.4a138.51 138.51 0 0 0-11.6 33c-0.9 4.158-0.776 8.021-1.5 12.6a62.567 62.567 0 0 0 0.2 17.7l0.1 3.5c0.448 2.251 0.756 5.066 1.4 7.5 4.4 16.6 11.186 29.1 21.105 40 8.466 9.3 24 18.759 38.507 21.9 31.74 6.872 62.858-3.464 83.116-15.4 56.624-33.363 95.068-87.621 116.623-156 3.641-11.55 6.52-24.053 9.2-36.8 1.326-6.3 1.315-12.047 2.4-18.7l1.1-11.2 0.1-4.9a26.557 26.557 0 0 0 0.1-7.6l-0.3-0.1z m56.711 135.2c-2.237 0.877-5.1 0.144-7.7 0.7-5.2 1.11-10.413 2.095-15.3 3.5-16.441 4.724-31.786 16.63-40.808 28.9-7.23 9.832-11.968 21.622-14.7 36-3.087 16.228 0.8 38.447 5.6 50.2 2.031 4.968 3.168 9.94 5.7 14.3 2.65 4.561 4.217 9.461 7 13.8 4.757 7.413 9.54 14.6 14.5 21.7a106 106 0 0 0 10.6 12.2c7.323 7.618 13.952 15.355 22.4 21.9l8.9 7.7c18.034 13.233 36.812 25.564 58.511 35.2 4.546 2.018 9.28 4.647 14 6.4a476.781 476.781 0 0 0 45.409 14.6c10.308 2.7 21.056 3.538 32.006 5.7l6 0.4a95.167 95.167 0 0 0 14.4 1.1c1.487 0.034 7.028 0.442 7.6-0.3v-2.1c-0.643-0.294-0.493-5.159-0.5-6.4l-0.4-18.9-1.3-21c-0.856-5.323-0.55-10.941-1.5-16.5-2.547-14.9-3.77-30.041-7.3-44-2.222-8.784-4.388-17.936-7.1-26.1-0.755-2.272-0.821-4.158-1.6-6.4-15.914-45.784-38.372-80.829-74.315-106.6a153.766 153.766 0 0 0-45.909-22.3c-5.117-1.544-10.71-2.108-16.2-3.1-5.58-1.01-11.753-0.581-17.995-0.6zM193.838 421a1.066 1.066 0 0 0-0.4 0.4l0.1 8.2c0.05 6.854-0.428 14.52 0.6 20.8l1.1 19.9c0.836 4.957 0.52 10.224 1.4 15.4 2.039 11.987 3.086 24.452 5.7 35.7l0.9 5.8c3.344 11.981 5.654 24 9.4 35.5a248.775 248.775 0 0 0 16.6 38.9c1.867 3.513 3.267 7.188 5.4 10.5l5.6 8.4c4.1 6.6 8.785 12.251 13.2 18.4l5.1 5.5a54.355 54.355 0 0 0 7.6 8.1c6.051 4.912 11.469 10.775 18 15.2l12.2 8.1 6 3.2a79.633 79.633 0 0 0 13 6.4 160.925 160.925 0 0 0 26.706 8.4c3.645 0.777 6.863 0.492 10.8 1.2a61.147 61.147 0 0 0 18.2-0.1c4.328-0.722 8.013-0.566 11.9-1.7 6.827-1.99 14.884-4.043 20.4-7.4a166.4 166.4 0 0 0 18-12.7 45.871 45.871 0 0 0 7.1-7.9 108.425 108.425 0 0 0 6.9-9.6c8.96-14.766 14.152-39.314 9.4-62.3-3.778-18.28-10.642-32.927-18.6-46.8-18.184-31.685-44.566-55.229-74.414-75.3-9.046-6.083-18.582-12.18-29.006-16.8l-8.1-4.3-18.4-8c-4.544-1.526-8.8-3.173-13.1-4.9l-6.5-1.9c-6.187-2.487-13.068-3.459-19.8-5.7-4.931-1.641-10.5-2.422-16-3.6a226.057 226.057 0 0 0-46.986-5z m414.981 156.5l-5.3 0.4c-4.461 0.748-8.881 0.829-13.1 1.9a245.175 245.175 0 0 0-26.5 8.7l-8.1 4.2c-3.428 2-6.915 3.289-10.2 5.4l-11.3 7.3c-5.483 3.545-10.2 8.207-15.3 12.1a109.585 109.585 0 0 0-17.9 16.9l-4 3.9c-2.563 3.174-5.327 6.127-7.8 9.4l-4.1 4.6-10.1 13.7c-5.287 7.326-9.468 15.189-14.2 23.1a256.275 256.275 0 0 0-17.3 34.9c-5.994 14.812-10.661 30.434-15.2 46.8-2.859 10.3-3.729 21.319-5.8 32.3-1.289 6.83-0.369 14.053-1.5 21.1-0.2 1.237-0.243 6.378 0.3 6.8h1.3c0.372-0.817 6.707-0.269 8.2-0.5l6.1-0.1 25.2-1c11.83-1.853 23.98-1.5 35.407-3.6 8.914-1.634 17.64-2.569 26.4-4.4a373.131 373.131 0 0 0 36.808-9.5c60.082-19.153 105.55-54.323 128.025-111.2 2.918-7.386 4.03-15.5 5.8-24l0.4-5.6c2.354-13.156-1.334-31.556-5.2-40.5-1.692-3.913-2.494-7.66-4.6-11.1-15.259-24.877-36.297-41.934-76.44-42z" p-id="4374"></path></svg>
    <div class="sort-text">动态</div>
  </button>

  <button class="sort-left-botton-2">
             <svg t="1743743055560" class="sort-icon-1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="7315" width="200" height="200"><path d="M397.679162 13.065239c79.440466-18.143516 163.220116-17.034878 242.124144 3.302072a506.770328 506.770328 0 0 1 121.687916 48.827753c71.370058 39.922887 132.905425 97.238277 177.799302 165.580442 48.124424 72.884005 76.972852 158.368335 82.706775 245.533504 5.948498 85.424726-9.787008 172.351478-45.966752 250.01574-29.539837 63.85993-72.5383 121.413737-125.204563 168.048056-52.451688 46.538952-114.571176 82.158417-181.315949 103.699371-93.173271 30.267008-195.096438 32.806147-289.724051 7.510128-80.394133-21.350221-155.245076-62.942026-216.065192-119.661374C106.131222 832.408289 61.034692 765.484703 33.366426 691.885448 12.23078 636.119767 1.370896 576.610943 0.596042 517.006752v-6.54454c0.202654-22.375413 1.525867-44.762747 4.529918-66.959348 12.588405-96.046193 53.548405-188.086985 116.228173-261.924657C192.080466 97.572061 290.510827 37.252619 397.679162 13.065239M465.508731 149.511153c2.27688 17.857416 1.072875 35.97709-1.656996 53.715296-6.890244 42.1044-24.723818 81.955763-48.982725 116.871898-21.362142 30.815367-47.540303 58.078324-76.281443 82.06305-18.322328 14.293085-33.652526 32.126659-46.693923 51.283446-18.870687 28.049732-31.518696 60.212154-37.061886 93.542817-5.674319 33.592922-4.231898 68.246799 3.147101 101.458254 18.429616 82.90943 74.195297 156.461001 148.712456 197.111059 12.314226 6.592224 24.866868 13.017555 38.146683 17.416345-6.043865-23.031059-13.995064-45.549523-19.037579-68.854762-6.723353-30.088196-10.538021-61.404237-5.769686-92.088475 3.492806-22.852247 12.671851-45.132293 28.097416-62.512875 2.241118-2.372247 4.136531-5.25709 7.164424-6.675669 7.343236 33.974389 16.21234 67.781886 29.814016 99.837019 15.723586 37.395669 37.729453 72.693271 67.603073 100.421141 17.857416 16.724936 38.575832 30.255087 60.772433 40.495088 10.311525-4.255739 19.979325-9.870454 29.623283-15.437486 21.886659-12.934109 42.80773-27.489453 62.632084-43.415692 33.974389-27.465611 64.980489-59.127357 88.786403-95.915065 25.117206-38.563912 41.448754-83.588917 42.223609-129.889452 1.227846-48.410524-14.269243-95.915064-38.158603-137.578394-22.697276-39.720233-52.380163-75.029756-84.804843-107.132573-1.764284-1.811967-3.504726-3.719302-5.82929-4.816018-1.442421 6.83064-2.050384 13.804331-2.896764 20.730337-3.540489 31.232596-7.843912 62.548638-16.49844 92.827567-5.924657 20.074692-13.434785 40.304354-26.893411 56.635903-9.167125 11.22943-22.840326 19.407125-37.646007 19.216391 9.787008-63.299651 11.432084-128.685448-3.015972-191.400978-10.514179-45.501839-30.648475-89.477811-62.334063-124.167451-25.46291-28.228545-58.006799-49.566845-93.16135-63.740721z" p-id="7316"></path></svg>
    <div class="sort-text">热门</div>
  </button>
</div>
         </div>
         </el-col>

        <!-- 中间分类栏 -->
         <el-col :span="16">
          <div class="sort-center">
            <el-row>
              <el-col :span="24">
            <el-row class="sort-center-row">
              <el-col :span="24">
<u><span class="u-text">番剧</span></u>
<u><span class="u-text">国创</span></u>
<u><span class="u-text">综艺</span></u>
<u><span class="u-text">动画</span></u>
<u><span class="u-text">鬼畜</span></u>
<u><span class="u-text">舞蹈</span></u>
<u><span class="u-text">娱乐</span></u>
<u><span class="u-text">科技</span></u>
<u><span class="u-text">美食</span></u>
<u><span class="u-text">汽车</span></u>
<u><span class="u-text">体育</span></u>
<u><span class="u-text">宠物</span></u>
</el-col>
            </el-row>
            <el-row>
              <el-col :span="24">
                <u><span class="u-text">数码</span></u>
<u><span class="u-text">电影</span></u>
<u><span class="u-text">电视</span></u>
<u><span class="u-text">记录</span></u>
<u><span class="u-text">游戏</span></u>
<u><span class="u-text">音乐</span></u>
<u><span class="u-text">影视</span></u>
<u><span class="u-text">知识</span></u>
<u><span class="u-text">咨询</span></u>
<u><span class="u-text">剧场</span></u>
<u><span class="u-text">时尚</span></u>
<u><span class="u-text">更多</span></u>
</el-col>
            </el-row>
          </el-col>
          </el-row>
          </div>
         </el-col>

        <!-- 右侧路由跳转 -->
         <el-col :span="4">
          <div class="sort-right">
            <el-row>
              <el-col :span="3">
                <el-divider class="sort-right-divider" direction="vertical" />
              </el-col>
              <div class="sort-right-botton">
              <el-col :span="21">
                <el-row >
                  <el-col :span="24" class="sort-right-col">
<n class="btn-shine">
    <svg t="1743750775794" class="src-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="9152" width="200" height="200"><path d="M697.344 821.248v184.32L942.08 776.704H743.424c-25.6-1.024-46.08 19.456-46.08 44.544zM849.92 26.624H207.872c-50.688 0-92.16 40.96-92.16 92.16V913.92c0 50.688 40.96 92.16 92.16 92.16h397.312V777.216c0-50.688 40.96-92.16 92.16-92.16H942.08V117.76c0-50.176-40.96-91.136-92.16-91.136z m-92.16 519.68H300.032c-25.6 0-46.08-20.48-46.08-46.08s20.48-46.08 46.08-46.08h459.264c25.6 0 46.08 20.48 46.08 46.08-1.536 25.6-22.016 46.08-47.616 46.08z m0-228.864H300.032c-25.6 0-46.08-20.48-46.08-46.08s20.48-46.08 46.08-46.08h459.264c25.6 0 46.08 20.48 46.08 46.08s-22.016 46.08-47.616 46.08z" p-id="9153"></path></svg>
    <span>专&nbsp;栏</span>
</n>
<n class="btn-shine">
  <svg t="1743751106937" class="src-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="11937" width="200" height="200"><path d="M890.432183 74.964766c-3.020752 2.837676-5.989139 5.659563-8.928147 8.454068-62.608551 59.484869-103.970708 98.789221-333.682163 21.516212-61.817887-20.793102-124.650887-30.07341-186.752981-27.584897-49.741275 1.99185-99.061636 11.5042-146.579019 28.268434-81.188754 28.636385-129.228383 68.639063-131.232025 70.325919-1.777395 1.496985-3.311755 3.161257-4.625864 4.95544-11.752032 7.313042-16.63652 22.34525-10.868631 35.361225l326.330948 730.263681c6.305725 14.256334 22.969826 20.691971 37.220165 14.384048 14.250338-6.305725 20.691771-22.969826 14.384048-37.220165l-135.539504-299.197944c0.909783-1.734024 1.830559-3.490833 2.768923-5.278022 37.386452-71.424574 79.767518-152.37529 297.500695-133.062584 39.697885 3.522212 78.527159-2.99397 115.527073-19.371866 2.293845-1.012114 4.588489-2.068597 6.867544-3.161257 33.563244-16.059112 65.531969-40.344047 95.017978-72.167271 46.148311-49.800235 86.816938-119.70524 111.57795-191.785969 9.430806-27.450189 15.694159-52.876949 18.117916-73.535343 2.288848-19.523363 2.989173-46.990141-17.260098-60.062078C934.658398 62.437659 914.457494 52.354895 890.432183 74.964766z" p-id="11938"></path></svg>
    <span>活&nbsp;动</span>
</n>
<n class="btn-shine-last">
  <svg t="1743751584269" class="src-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="21780" width="200" height="200"><path d="M836.3 170.65c17.102 0 31.284 12.646 33.742 29.081l0.358 5.069v316.16a34.15 34.15 0 0 1-29.082 33.792l-5.017 0.358H733.9V750.9a34.15 34.15 0 0 1-29.133 33.791l-5.018 0.359H386.765L259.43 880.589a34.202 34.202 0 0 1-53.248-17.357l-1.024-4.813-0.358-5.12-0.051-68.3H153.6a34.15 34.15 0 0 1-32.666-24.218l-1.075-4.813-0.41-5.069V341.3c0-17.152 12.647-31.334 29.082-33.792l5.069-0.307h174.234V204.8c0-17.152 12.646-31.334 29.132-33.792l5.018-0.358h474.317z m-477.9 409.6H256l-5.069 0.41a34.15 34.15 0 0 0 0 67.48l5.069 0.41h102.4l5.069-0.41a34.15 34.15 0 0 0-5.069-67.89z m102.4-136.5H256l-5.069 0.41a34.15 34.15 0 0 0 0 67.482l5.069 0.41h204.8l5.069-0.41a34.15 34.15 0 0 0-5.069-67.892zM802.1 238.9H396.082v68.249H699.7c17.152 0 31.386 12.646 33.792 29.133l0.359 5.017v145.46l68.25 0.05V238.9z" p-id="21781"></path></svg>
    <span>社区中心</span>
</n>

                    </el-col>
                </el-row>

                <el-row>
                  <el-col :span="24" class="sort-right-col2">
                    <n class="btn-shine">
                      <svg t="1743751295000" class="src-icon" viewBox="0 0 1331 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="13160" width="200" height="200"><path d="M1284.608 151.8592c26.9824 0 46.592 19.5072 46.592 43.4176v633.4464c0 15.2064-7.3728 28.2112-22.016 36.864-14.7456 6.5536-31.8976 8.704-46.592 2.2016l-220.5696-91.136v162.7136c0 45.568-41.6256 84.6336-95.5392 84.6336H95.5392C44.1856 1024 0 987.136 0 939.3664V84.6336C0 39.0656 41.6768 0 95.5904 0h853.3504C1000.3968 0 1044.48 36.864 1044.48 84.6336V247.296l220.5696-91.136a50.9952 50.9952 0 0 1 19.6096-4.352z m-589.824 378.4704a51.2 51.2 0 0 0 0-87.8592l-258.8672-155.136A51.2 51.2 0 0 0 358.4 331.1616v310.3744a51.2 51.2 0 0 0 77.5168 43.8784l258.816-155.136z"  p-id="13161"></path></svg>
    <span>直&nbsp;播</span>
</n>
<n class="btn-shine">
  <svg t="1743751458647" class="src-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="20702" width="200" height="200"><path d="M518.144 287.744C463.872 259.072 303.104 184.32 143.36 184.32l44.032 625.664c122.88 0 277.504 74.752 330.752 103.424V287.744zM557.056 287.744v625.664c54.272-28.672 207.872-103.424 330.752-103.424L931.84 184.32c-160.768 0-320.512 74.752-374.784 103.424z m265.216 153.6c-2.048 0-3.072 1.024-5.12 1.024-2.048 1.024-4.096 1.024-7.168 1.024-27.648 6.144-70.656 19.456-105.472 41.984-28.672 18.432-51.2 39.936-59.392 47.104v-43.008c2.048-1.024 3.072-2.048 4.096-3.072 77.824-56.32 138.24-65.536 163.84-66.56h11.264c0 3.072 0 8.192-1.024 12.288-1.024 3.072 0 8.192-1.024 9.216z m66.56-138.24c-1.024 3.072-1.024 6.144-1.024 9.216-2.048 8.192-2.048 17.408-2.048 26.624v9.216c0 2.048 1.024 3.072 1.024 4.096-3.072 1.024 1.024-1.024-3.072 0h-7.168c-3.072 0-6.144 0-9.216 1.024-3.072 0-6.144 1.024-10.24 1.024-6.144 1.024-12.288 2.048-18.432 4.096-2.048 0-5.12 1.024-7.168 1.024-46.08 6.144-109.568 26.624-188.416 75.776v-71.68s125.952-77.824 232.448-72.704h7.168c2.048 1.024 5.12 1.024 7.168 2.048 0-1.024-1.024-1.024-1.024-2.048 1.024 1.024 4.096 3.072 1.024 2.048 0 1.024 1.024 1.024 1.024 2.048 0 2.048-1.024 5.12-2.048 8.192z"  p-id="20703"></path></svg>
    <span>课&nbsp;堂</span>
</n>
<n class="btn-shine-last">
  <svg t="1743751652004" class="src-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="22812" width="200" height="200"><path d="M783.36 221.85a102.4 102.4 0 0 1 102.093 94.77l0.307 7.68v409.6a102.4 102.4 0 0 1-94.72 102.094l-7.68 0.256H237.21a102.4 102.4 0 0 1-102.093-94.72l-0.307-7.68v-409.6a102.4 102.4 0 0 1 94.77-102.042l7.68-0.307h546.1z m-155.597 81.408a34.15 34.15 0 0 0-50.79 23.603l-54.17 203.315a128 128 0 1 0-108.288 220.826l6.759 1.024A128 128 0 0 0 562.79 647.168l71.783-253.798 47.462 35.481 3.277 2.202a34.15 34.15 0 0 0 37.58-56.832l-91.852-68.762z" p-id="22813"></path></svg>
    <span>热歌新榜</span>
</n>
                    </el-col>
                </el-row>
              </el-col>
            </div>
            </el-row>
          </div>
        </el-col>
        </el-row>
      </div>
    </el-col>
   </el-row>

   <!-- 中间推荐栏 -->
    <el-row>
      <el-col :span="24">
        <div class="recommend">

           <el-row gutter="20">
            <!-- 左侧走马灯 -->
              <el-col :span="10">
                <div class="recommend-left">
    <div class="block text-center">
      <el-carousel height="406px" motion-blur>
        <el-carousel-item v-for="item in 7" :key="item">
          <div class="carousel-item">
            <div class="image-container">
              <img :src="getImageUrl(item)" alt="carousel image" class="carousel-image">
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>
  </div>
              </el-col>

              <!-- 右侧推荐视频 -->
               <el-col :span="14">
                <div class="recommend-right">
                  <!-- 视频 -->
                  <el-row gutter="30" class="video-row1">
                    <el-col :span="8" v-for="(video, index) in recommendVideos.slice(0,3)" :key="video.id">
                      <div class="video" id="1" @click="playVideo(video.id)">
                          <img :src="video.coverUrl" class="thumbnail-img-1">
                          <svg t="1743757350808" :class="'views' + (index + 1) + '-icon'" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="28554" width="200" height="200"><path d="M759.528727 209.408H264.424727A53.853091 53.853091 0 0 0 209.454545 262.004364v499.898181a53.992727 53.992727 0 0 0 54.970182 52.689455h495.080728a53.829818 53.829818 0 0 0 55.016727-52.689455v-499.898181a53.690182 53.690182 0 0 0-54.993455-52.596364z m0 552.494545H264.424727V262.097455h495.080728v499.80509z" fill="rgb(226, 228, 228)" p-id="28555"></path><path d="M436.386909 360.727273v302.498909L625.524364 512 436.386909 360.727273z" fill="rgb(226, 228, 228)" p-id="28556"></path></svg>
                          <span class="views1">{{formatViews(video.views)}}</span>
                          <svg t="1743757905181" :class="'barrage' + (index + 1) + '-icon'" viewBox="0 0 1303 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29556" width="200" height="200"><path d="M110.964364 1023.720727c-60.509091 0-109.847273-48.872727-109.847273-108.916363V109.009455C1.117091 48.965818 50.455273 0 110.964364 0h1081.902545c60.509091 0 109.847273 48.872727 109.847273 108.916364v805.794909c0 60.043636-49.338182 108.916364-109.847273 108.916363H110.964364z m0-927.650909c-6.609455 0-12.101818 5.771636-12.101819 12.939637v805.794909c0 7.168 5.492364 13.032727 12.101819 13.032727h1081.902545c6.609455 0 12.101818-5.864727 12.101818-13.032727V109.009455c0-7.168-5.492364-13.032727-12.101818-13.032728H110.964364z" fill="rgb(226, 228, 228)" p-id="29557"></path><path d="M1003.054545 520.098909a51.572364 51.572364 0 0 1-50.26909 52.689455h-80.058182a51.572364 51.572364 0 0 1-50.269091-52.689455c0-29.137455 22.434909-52.782545 50.269091-52.782545h80.058182c27.834182 0 50.269091 23.645091 50.26909 52.782545zM1096.610909 270.149818c0 28.858182-22.341818 52.410182-49.989818 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273h814.08c27.648 0 50.082909 23.458909 50.082909 52.503273zM768.744727 520.098909c0 28.858182-22.341818 52.410182-50.082909 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273H718.661818c27.648 0 50.082909 23.458909 50.082909 52.503273zM1096.610909 735.976727c0 29.044364-22.341818 52.503273-49.989818 52.503273H560.500364a51.293091 51.293091 0 0 1-50.082909-52.503273c0-28.951273 22.341818-52.410182 50.082909-52.410182h486.120727c27.648 0 50.082909 23.458909 50.082909 52.410182z" fill="rgb(226, 228, 228)" p-id="29558"></path></svg>
                          <span class="barrage1">{{video.barrages}}</span>
                          <span class="time1">{{formatVideoDuration(video.time)}}</span>
                            <!-- 渐变阴影 -->
      <div class="top-shadow-layer-2"></div>
                        </div>
                      <div class="video-info">
                        <div class="title" @click="playVideo(video.id)">{{video.title}}</div>
                          <div class="meta" @click="toUserInfo(video.userId)">
                            <svg t="1744025410333" class="up-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="10955" width="200" height="200"><path d="M800 128H224C134.4 128 64 198.4 64 288v448c0 89.6 70.4 160 160 160h576c89.6 0 160-70.4 160-160V288c0-89.6-70.4-160-160-160z m96 608c0 54.4-41.6 96-96 96H224c-54.4 0-96-41.6-96-96V288c0-54.4 41.6-96 96-96h576c54.4 0 96 41.6 96 96v448z" p-id="10956"></path><path d="M419.2 544c0 51.2-3.2 108.8-83.2 108.8S252.8 595.2 252.8 544v-217.6H192v243.2c0 96 51.2 140.8 140.8 140.8 89.6 0 147.2-48 147.2-144v-240h-60.8V544zM710.4 326.4h-156.8V704h60.8v-147.2h96c102.4 0 121.6-67.2 121.6-115.2 0-44.8-19.2-115.2-121.6-115.2z m-3.2 179.2h-92.8V384h92.8c32 0 60.8 12.8 60.8 60.8 0 44.8-32 60.8-60.8 60.8z" p-id="10957"></path></svg>
                            <span class="vvauthor">{{video.username}}</span>
                            <span class="vvtime">·&nbsp;{{formatTime(video.createdTime)}}</span>
                          </div>
                        </div>
                    </el-col>
                    <!-- <el-col :span="8">
                      <div class="video" id="2">
                        <img :src="11" class="thumbnail-img-1">
                          <svg t="1743757350808" class="views2-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="28554" width="200" height="200"><path d="M759.528727 209.408H264.424727A53.853091 53.853091 0 0 0 209.454545 262.004364v499.898181a53.992727 53.992727 0 0 0 54.970182 52.689455h495.080728a53.829818 53.829818 0 0 0 55.016727-52.689455v-499.898181a53.690182 53.690182 0 0 0-54.993455-52.596364z m0 552.494545H264.424727V262.097455h495.080728v499.80509z" fill="#666666" p-id="28555"></path><path d="M436.386909 360.727273v302.498909L625.524364 512 436.386909 360.727273z"  p-id="28556"></path></svg>
                          <span class="views2">1000</span>
                          <svg t="1743757905181" class="barrage2-icon" viewBox="0 0 1303 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29556" width="200" height="200"><path d="M110.964364 1023.720727c-60.509091 0-109.847273-48.872727-109.847273-108.916363V109.009455C1.117091 48.965818 50.455273 0 110.964364 0h1081.902545c60.509091 0 109.847273 48.872727 109.847273 108.916364v805.794909c0 60.043636-49.338182 108.916364-109.847273 108.916363H110.964364z m0-927.650909c-6.609455 0-12.101818 5.771636-12.101819 12.939637v805.794909c0 7.168 5.492364 13.032727 12.101819 13.032727h1081.902545c6.609455 0 12.101818-5.864727 12.101818-13.032727V109.009455c0-7.168-5.492364-13.032727-12.101818-13.032728H110.964364z" fill="#5F6269" p-id="29557"></path><path d="M1003.054545 520.098909a51.572364 51.572364 0 0 1-50.26909 52.689455h-80.058182a51.572364 51.572364 0 0 1-50.269091-52.689455c0-29.137455 22.434909-52.782545 50.269091-52.782545h80.058182c27.834182 0 50.269091 23.645091 50.26909 52.782545zM1096.610909 270.149818c0 28.858182-22.341818 52.410182-49.989818 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273h814.08c27.648 0 50.082909 23.458909 50.082909 52.503273zM768.744727 520.098909c0 28.858182-22.341818 52.410182-50.082909 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273H718.661818c27.648 0 50.082909 23.458909 50.082909 52.503273zM1096.610909 735.976727c0 29.044364-22.341818 52.503273-49.989818 52.503273H560.500364a51.293091 51.293091 0 0 1-50.082909-52.503273c0-28.951273 22.341818-52.410182 50.082909-52.410182h486.120727c27.648 0 50.082909 23.458909 50.082909 52.410182z" fill="#5F6269" p-id="29558"></path></svg>
                          <span class="barrage2">100</span>
                          <span class="time2">10:00</span>
                      
      <div class="top-shadow-layer-2"></div>
                        </div>
                      <div class="video-info">
                        <div class="title">标题2</div>
                          <div class="meta">
                            <span class="author">作者2</span>
                            <span class="time">2小时前</span>
                          </div>
                        </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="video" id="1">
                        <img :src="11" class="thumbnail-img-1">
                          <svg t="1743757350808" class="views3-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="28554" width="200" height="200"><path d="M759.528727 209.408H264.424727A53.853091 53.853091 0 0 0 209.454545 262.004364v499.898181a53.992727 53.992727 0 0 0 54.970182 52.689455h495.080728a53.829818 53.829818 0 0 0 55.016727-52.689455v-499.898181a53.690182 53.690182 0 0 0-54.993455-52.596364z m0 552.494545H264.424727V262.097455h495.080728v499.80509z" fill="#666666" p-id="28555"></path><path d="M436.386909 360.727273v302.498909L625.524364 512 436.386909 360.727273z"  p-id="28556"></path></svg>
                          <span class="views3">1000</span>
                          <svg t="1743757905181" class="barrage3-icon" viewBox="0 0 1303 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29556" width="200" height="200"><path d="M110.964364 1023.720727c-60.509091 0-109.847273-48.872727-109.847273-108.916363V109.009455C1.117091 48.965818 50.455273 0 110.964364 0h1081.902545c60.509091 0 109.847273 48.872727 109.847273 108.916364v805.794909c0 60.043636-49.338182 108.916364-109.847273 108.916363H110.964364z m0-927.650909c-6.609455 0-12.101818 5.771636-12.101819 12.939637v805.794909c0 7.168 5.492364 13.032727 12.101819 13.032727h1081.902545c6.609455 0 12.101818-5.864727 12.101818-13.032727V109.009455c0-7.168-5.492364-13.032727-12.101818-13.032728H110.964364z" fill="#5F6269" p-id="29557"></path><path d="M1003.054545 520.098909a51.572364 51.572364 0 0 1-50.26909 52.689455h-80.058182a51.572364 51.572364 0 0 1-50.269091-52.689455c0-29.137455 22.434909-52.782545 50.269091-52.782545h80.058182c27.834182 0 50.269091 23.645091 50.26909 52.782545zM1096.610909 270.149818c0 28.858182-22.341818 52.410182-49.989818 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273h814.08c27.648 0 50.082909 23.458909 50.082909 52.503273zM768.744727 520.098909c0 28.858182-22.341818 52.410182-50.082909 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273H718.661818c27.648 0 50.082909 23.458909 50.082909 52.503273zM1096.610909 735.976727c0 29.044364-22.341818 52.503273-49.989818 52.503273H560.500364a51.293091 51.293091 0 0 1-50.082909-52.503273c0-28.951273 22.341818-52.410182 50.082909-52.410182h486.120727c27.648 0 50.082909 23.458909 50.082909 52.410182z" fill="#5F6269" p-id="29558"></path></svg>
                          <span class="barrage3">100</span>
                          <span class="time3">10:00</span>
                    
      <div class="top-shadow-layer-2"></div>
                        </div>
                      <div class="video-info">
                        <div class="title">标题3</div>
                          <div class="meta">
                            <span class="author">作者3</span>
                            <span class="time">3小时前</span>
                          </div>
                        </div>
                    </el-col> -->
                  </el-row>

                  <el-row gutter="30" class="video-row2">
                    <el-col :span="8" v-for="(video, index) in recommendVideos.slice(3,6)" :key="video.id">
                      <div class="video" id="4"  @click="playVideo(video.id)">
                        <img :src="video.coverUrl" class="thumbnail-img-1">
                          <svg t="1743757350808" :class="'views' + (index + 1) + '-icon'" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="28554" width="200" height="200"><path d="M759.528727 209.408H264.424727A53.853091 53.853091 0 0 0 209.454545 262.004364v499.898181a53.992727 53.992727 0 0 0 54.970182 52.689455h495.080728a53.829818 53.829818 0 0 0 55.016727-52.689455v-499.898181a53.690182 53.690182 0 0 0-54.993455-52.596364z m0 552.494545H264.424727V262.097455h495.080728v499.80509z" fill="rgb(226, 228, 228)" p-id="28555"></path><path d="M436.386909 360.727273v302.498909L625.524364 512 436.386909 360.727273z" fill="rgb(226, 228, 228)" p-id="28556"></path></svg>
                          <span class="views4">{{formatViews(video.views)}}</span>
                          <svg t="1743757905181" :class="'barrage' + (index + 1) + '-icon'" viewBox="0 0 1303 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29556" width="200" height="200"><path d="M110.964364 1023.720727c-60.509091 0-109.847273-48.872727-109.847273-108.916363V109.009455C1.117091 48.965818 50.455273 0 110.964364 0h1081.902545c60.509091 0 109.847273 48.872727 109.847273 108.916364v805.794909c0 60.043636-49.338182 108.916364-109.847273 108.916363H110.964364z m0-927.650909c-6.609455 0-12.101818 5.771636-12.101819 12.939637v805.794909c0 7.168 5.492364 13.032727 12.101819 13.032727h1081.902545c6.609455 0 12.101818-5.864727 12.101818-13.032727V109.009455c0-7.168-5.492364-13.032727-12.101818-13.032728H110.964364z" fill="rgb(226, 228, 228)" p-id="29557"></path><path d="M1003.054545 520.098909a51.572364 51.572364 0 0 1-50.26909 52.689455h-80.058182a51.572364 51.572364 0 0 1-50.269091-52.689455c0-29.137455 22.434909-52.782545 50.269091-52.782545h80.058182c27.834182 0 50.269091 23.645091 50.26909 52.782545zM1096.610909 270.149818c0 28.858182-22.341818 52.410182-49.989818 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273h814.08c27.648 0 50.082909 23.458909 50.082909 52.503273zM768.744727 520.098909c0 28.858182-22.341818 52.410182-50.082909 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273H718.661818c27.648 0 50.082909 23.458909 50.082909 52.503273zM1096.610909 735.976727c0 29.044364-22.341818 52.503273-49.989818 52.503273H560.500364a51.293091 51.293091 0 0 1-50.082909-52.503273c0-28.951273 22.341818-52.410182 50.082909-52.410182h486.120727c27.648 0 50.082909 23.458909 50.082909 52.410182z" fill="rgb(226, 228, 228)" p-id="29558"></path></svg>
                          <span class="barrage4">{{ video.barrages }}</span>
                          <span class="time4">{{formatVideoDuration(video.time)}}</span>
                       <!-- 渐变阴影 -->
      <div class="top-shadow-layer-2"></div>
                        </div>
                      <div class="video-info">
                        <div class="title" @click="playVideo(video.id)">{{video.title}}</div>
                          <div class="meta" @click="toUserInfo(video.userId)">
                            <svg t="1744025410333" class="up-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="10955" width="200" height="200"><path d="M800 128H224C134.4 128 64 198.4 64 288v448c0 89.6 70.4 160 160 160h576c89.6 0 160-70.4 160-160V288c0-89.6-70.4-160-160-160z m96 608c0 54.4-41.6 96-96 96H224c-54.4 0-96-41.6-96-96V288c0-54.4 41.6-96 96-96h576c54.4 0 96 41.6 96 96v448z" p-id="10956"></path><path d="M419.2 544c0 51.2-3.2 108.8-83.2 108.8S252.8 595.2 252.8 544v-217.6H192v243.2c0 96 51.2 140.8 140.8 140.8 89.6 0 147.2-48 147.2-144v-240h-60.8V544zM710.4 326.4h-156.8V704h60.8v-147.2h96c102.4 0 121.6-67.2 121.6-115.2 0-44.8-19.2-115.2-121.6-115.2z m-3.2 179.2h-92.8V384h92.8c32 0 60.8 12.8 60.8 60.8 0 44.8-32 60.8-60.8 60.8z" p-id="10957"></path></svg>
                            <span class="vvauthor">{{video.username}}</span>
                            <span class="vvtime">·&nbsp;{{formatTime(video.createdTime)}}</span>
                          </div>
                        </div>
                    </el-col>
                    <!-- <el-col :span="8">
                      <div class="video" id="1">
                        <img :src="11" class="thumbnail-img-1">
                          <svg t="1743757350808" class="views5-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="28554" width="200" height="200"><path d="M759.528727 209.408H264.424727A53.853091 53.853091 0 0 0 209.454545 262.004364v499.898181a53.992727 53.992727 0 0 0 54.970182 52.689455h495.080728a53.829818 53.829818 0 0 0 55.016727-52.689455v-499.898181a53.690182 53.690182 0 0 0-54.993455-52.596364z m0 552.494545H264.424727V262.097455h495.080728v499.80509z" fill="#666666" p-id="28555"></path><path d="M436.386909 360.727273v302.498909L625.524364 512 436.386909 360.727273z"  p-id="28556"></path></svg>
                          <span class="views5">1000</span>
                          <svg t="1743757905181" class="barrage5-icon" viewBox="0 0 1303 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29556" width="200" height="200"><path d="M110.964364 1023.720727c-60.509091 0-109.847273-48.872727-109.847273-108.916363V109.009455C1.117091 48.965818 50.455273 0 110.964364 0h1081.902545c60.509091 0 109.847273 48.872727 109.847273 108.916364v805.794909c0 60.043636-49.338182 108.916364-109.847273 108.916363H110.964364z m0-927.650909c-6.609455 0-12.101818 5.771636-12.101819 12.939637v805.794909c0 7.168 5.492364 13.032727 12.101819 13.032727h1081.902545c6.609455 0 12.101818-5.864727 12.101818-13.032727V109.009455c0-7.168-5.492364-13.032727-12.101818-13.032728H110.964364z" fill="#5F6269" p-id="29557"></path><path d="M1003.054545 520.098909a51.572364 51.572364 0 0 1-50.26909 52.689455h-80.058182a51.572364 51.572364 0 0 1-50.269091-52.689455c0-29.137455 22.434909-52.782545 50.269091-52.782545h80.058182c27.834182 0 50.269091 23.645091 50.26909 52.782545zM1096.610909 270.149818c0 28.858182-22.341818 52.410182-49.989818 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273h814.08c27.648 0 50.082909 23.458909 50.082909 52.503273zM768.744727 520.098909c0 28.858182-22.341818 52.410182-50.082909 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273H718.661818c27.648 0 50.082909 23.458909 50.082909 52.503273zM1096.610909 735.976727c0 29.044364-22.341818 52.503273-49.989818 52.503273H560.500364a51.293091 51.293091 0 0 1-50.082909-52.503273c0-28.951273 22.341818-52.410182 50.082909-52.410182h486.120727c27.648 0 50.082909 23.458909 50.082909 52.410182z" fill="#5F6269" p-id="29558"></path></svg>
                          <span class="barrage5">100</span>
                          <span class="time5">10:00</span>
      <div class="top-shadow-layer-2"></div>
                        </div>
                      <div class="video-info">
                        <div class="title">标题5</div>
                          <div class="meta">
                            <span class="author">作者5</span>
                            <span class="time">5小时前</span>
                          </div>
                        </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="video" id="1">
                        <img :src="11" class="thumbnail-img-1">
                          <svg t="1743757350808" class="views6-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="28554" width="200" height="200"><path d="M759.528727 209.408H264.424727A53.853091 53.853091 0 0 0 209.454545 262.004364v499.898181a53.992727 53.992727 0 0 0 54.970182 52.689455h495.080728a53.829818 53.829818 0 0 0 55.016727-52.689455v-499.898181a53.690182 53.690182 0 0 0-54.993455-52.596364z m0 552.494545H264.424727V262.097455h495.080728v499.80509z" fill="#666666" p-id="28555"></path><path d="M436.386909 360.727273v302.498909L625.524364 512 436.386909 360.727273z"  p-id="28556"></path></svg>
                          <span class="views6">1000</span>
                          <svg t="1743757905181" class="barrage6-icon" viewBox="0 0 1303 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29556" width="200" height="200"><path d="M110.964364 1023.720727c-60.509091 0-109.847273-48.872727-109.847273-108.916363V109.009455C1.117091 48.965818 50.455273 0 110.964364 0h1081.902545c60.509091 0 109.847273 48.872727 109.847273 108.916364v805.794909c0 60.043636-49.338182 108.916364-109.847273 108.916363H110.964364z m0-927.650909c-6.609455 0-12.101818 5.771636-12.101819 12.939637v805.794909c0 7.168 5.492364 13.032727 12.101819 13.032727h1081.902545c6.609455 0 12.101818-5.864727 12.101818-13.032727V109.009455c0-7.168-5.492364-13.032727-12.101818-13.032728H110.964364z" fill="#5F6269" p-id="29557"></path><path d="M1003.054545 520.098909a51.572364 51.572364 0 0 1-50.26909 52.689455h-80.058182a51.572364 51.572364 0 0 1-50.269091-52.689455c0-29.137455 22.434909-52.782545 50.269091-52.782545h80.058182c27.834182 0 50.269091 23.645091 50.26909 52.782545zM1096.610909 270.149818c0 28.858182-22.341818 52.410182-49.989818 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273h814.08c27.648 0 50.082909 23.458909 50.082909 52.503273zM768.744727 520.098909c0 28.858182-22.341818 52.410182-50.082909 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273H718.661818c27.648 0 50.082909 23.458909 50.082909 52.503273zM1096.610909 735.976727c0 29.044364-22.341818 52.503273-49.989818 52.503273H560.500364a51.293091 51.293091 0 0 1-50.082909-52.503273c0-28.951273 22.341818-52.410182 50.082909-52.410182h486.120727c27.648 0 50.082909 23.458909 50.082909 52.410182z" fill="#5F6269" p-id="29558"></path></svg>
                          <span class="barrage6">100</span>
                          <span class="time6">10:00</span>
      <div class="top-shadow-layer-2"></div>
                        </div>
                      <div class="video-info">
                        <div class="title">标题6</div>
                          <div class="meta">
                            <span class="author">作者6</span>
                            <span class="time">6小时前</span>
                          </div>
                        </div>
                    </el-col> -->
                  </el-row>
                </div>
               </el-col>
           </el-row>
        </div>
      </el-col>
    </el-row>
    
    <div class="botton">
  <ul v-infinite-scroll="load" class="infinite-list" style="overflow: auto">
    <el-row v-for="(group, index) in videoGroups" :key="index" >
      <template v-for="(video, i) in group" :key="video.id">
  <el-col :span="4">
      <div class="botton-video" :id="video.id"  @click="playVideo(video.id)">
        <img :src="video.url" class="thumbnail-img" />
          <!-- 渐变阴影 -->
      <div class="top-shadow-layer-1"></div>
      </div>
      <div class="video-info"> 
                          <svg t="1743757350808" class="views-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="28554" width="200" height="200"><path d="M759.528727 209.408H264.424727A53.853091 53.853091 0 0 0 209.454545 262.004364v499.898181a53.992727 53.992727 0 0 0 54.970182 52.689455h495.080728a53.829818 53.829818 0 0 0 55.016727-52.689455v-499.898181a53.690182 53.690182 0 0 0-54.993455-52.596364z m0 552.494545H264.424727V262.097455h495.080728v499.80509z" fill="rgb(226, 228, 228)" p-id="28555"></path><path d="M436.386909 360.727273v302.498909L625.524364 512 436.386909 360.727273z" fill="rgb(226, 228, 228)" p-id="28556"></path></svg>
                          <span class="views">{{video.views}}</span>
                          <svg t="1743757905181" class="barrage-icon" viewBox="0 0 1303 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="29556" width="200" height="200"><path d="M110.964364 1023.720727c-60.509091 0-109.847273-48.872727-109.847273-108.916363V109.009455C1.117091 48.965818 50.455273 0 110.964364 0h1081.902545c60.509091 0 109.847273 48.872727 109.847273 108.916364v805.794909c0 60.043636-49.338182 108.916364-109.847273 108.916363H110.964364z m0-927.650909c-6.609455 0-12.101818 5.771636-12.101819 12.939637v805.794909c0 7.168 5.492364 13.032727 12.101819 13.032727h1081.902545c6.609455 0 12.101818-5.864727 12.101818-13.032727V109.009455c0-7.168-5.492364-13.032727-12.101818-13.032728H110.964364z" fill="rgb(226, 228, 228)" p-id="29557"></path><path d="M1003.054545 520.098909a51.572364 51.572364 0 0 1-50.26909 52.689455h-80.058182a51.572364 51.572364 0 0 1-50.269091-52.689455c0-29.137455 22.434909-52.782545 50.269091-52.782545h80.058182c27.834182 0 50.269091 23.645091 50.26909 52.782545zM1096.610909 270.149818c0 28.858182-22.341818 52.410182-49.989818 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273h814.08c27.648 0 50.082909 23.458909 50.082909 52.503273zM768.744727 520.098909c0 28.858182-22.341818 52.410182-50.082909 52.410182H232.541091a51.293091 51.293091 0 0 1-50.082909-52.410182c0-29.044364 22.434909-52.503273 50.082909-52.503273H718.661818c27.648 0 50.082909 23.458909 50.082909 52.503273zM1096.610909 735.976727c0 29.044364-22.341818 52.503273-49.989818 52.503273H560.500364a51.293091 51.293091 0 0 1-50.082909-52.503273c0-28.951273 22.341818-52.410182 50.082909-52.410182h486.120727c27.648 0 50.082909 23.458909 50.082909 52.410182z" fill="rgb(226, 228, 228)" p-id="29558"></path></svg>
                          <span class="barrage">{{video.barrage}}</span>
                          <span class="f-time">{{formatVideoDuration(video.time1)}}</span>
          <div class="title" @click="playVideo(video.id)">{{ video.title }}</div>
        <div class="meta" @click="toUserInfo(video.authorId)">
          <svg t="1744025410333" class="up-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="10955" width="200" height="200"><path d="M800 128H224C134.4 128 64 198.4 64 288v448c0 89.6 70.4 160 160 160h576c89.6 0 160-70.4 160-160V288c0-89.6-70.4-160-160-160z m96 608c0 54.4-41.6 96-96 96H224c-54.4 0-96-41.6-96-96V288c0-54.4 41.6-96 96-96h576c54.4 0 96 41.6 96 96v448z" p-id="10956"></path><path d="M419.2 544c0 51.2-3.2 108.8-83.2 108.8S252.8 595.2 252.8 544v-217.6H192v243.2c0 96 51.2 140.8 140.8 140.8 89.6 0 147.2-48 147.2-144v-240h-60.8V544zM710.4 326.4h-156.8V704h60.8v-147.2h96c102.4 0 121.6-67.2 121.6-115.2 0-44.8-19.2-115.2-121.6-115.2z m-3.2 179.2h-92.8V384h92.8c32 0 60.8 12.8 60.8 60.8 0 44.8-32 60.8-60.8 60.8z" p-id="10957"></path></svg>
          <span class="vvauthor">{{ video.author }}</span>
          <span class="vvtime">·&nbsp;{{ formatTime(video.time) }}</span>
        </div>
    </div>
  </el-col>
  <el-col :span="1">
    <!-- 空列，用于保持布局一致 -->
     <div class="botton-padding"></div>
  </el-col>
  </template>
</el-row>
    <!-- 加载状态提示 -->
    <li v-if="loading" class="loading-text"><div class="loader"></div></li>
    <li v-if="!hasMore" class="no-more">没有更多数据了</li>
  </ul>
</div>

     <!-- 回到顶部 -->
     <el-backtop :right="100" :bottom="100" />

</template>
<!-- ---------------------------------------------------------------------------------------------------------------------- -->
<style>
.video {
  transition: all 0.3s ease;
  cursor: pointer;
  overflow: hidden;
  border-radius: 4px;
}

.video:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.662);
}

.video:hover .thumbnail-img {
  transform: scale(1.05);
}

.botton-video {
  position: relative;
  transition: all 0.3s ease;
  cursor: pointer;
  overflow: hidden;
  border-radius: 4px;
}

.botton-video:hover {
  transform: scale(1.03);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
}

.botton-video:hover .thumbnail-img {
  transform: scale(1.05);
}

.thumbnail-img {
  transition: transform 0.3s ease;
  width: 100%;
  height: auto;
  display: block;
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

.botton-right{
  display: flex;
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

.user-icon{
  width: 20px;
  height: 20px;
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


/* 父容器布局 */
.userInfo-item {
}

.el-statistic__title {
  font-size: 14px;
  color: rgb(148, 166, 193);
}

.el-statistic__content {
  font-size: 24px;
  color: #303133;
}

.userInfo{
  margin-top: 80px; 
}

.coins-num{
  color: #070707;
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

.custom-dropdown-menu {
  position: relative;
  width: 300px;
  height: 450px;
  border-radius: 8px; /* 圆角 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); /* 阴影效果 */
}

.example-showcase .el-dropdown-link {
  cursor: pointer;
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
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


/* From Uiverse.io by doniaskima */ 
.loader {
  width: fit-content;
  font-weight: bold;
  font-family: monospace;
  white-space: pre;
  font-size: 30px;
  line-height: 1.2em;
  height: 1.2em;
  overflow: hidden;
  margin: 0 auto;
}

.loader:before {
  content: "Loading...\A⌰oading...\A⌰⍜ading...\A⌰⍜⏃ding...\A⌰⍜⏃⎅ing...\A⌰⍜⏃⎅⟟ng...\A⌰⍜⏃⎅⟟⋏g...\A⌰⍜⏃⎅⟟⋏☌...\A⌰⍜⏃⎅⟟⋏☌⟒..\A⌰⍜⏃⎅⟟⋏☌⟒⏁.\A⌰⍜⏃⎅⟟⋏☌⟒⏁⋔";
  white-space: pre;
  display: inline-block;
  animation: l39 1s infinite steps(11) alternate;
}

@keyframes l39 {
  100% {
    transform: translateY(-100%)
  }
}

.left-button-6 {
  margin-top: -62px;
  margin-left: 260px;
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

.left-button-6:hover {
  transform: scale(1.2);
}

.left-button-5 {
  margin-top: -62px;
  margin-left: 210px;
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

.left-button-5:hover {
  transform: scale(1.2);
}

.left-button-4 {
  margin-top: -62px;
  margin-left: 160px;
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

.left-button-4:hover {
  transform: scale(1.2);
}

.left-button-3 {
  margin-top: -62px;
  margin-left: 110px;
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

.left-button-3:hover {
  transform: scale(1.2);
}

.left-button-2 {
  margin-top: -62px;
  margin-left: 60px;
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

.left-button-2:hover {
  transform: scale(1.2);
}

.vip-button {
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

.vip-button:hover {
  transform: scale(1.2);
}

.vip-icon {
  width: 20px;
  height: 20px;
  margin-bottom: 5px;
}

.vip-text {
  font-size: 12px;
  color: rgba(249, 249, 249, 0.97);
  font-weight: bold;
  text-align: center;
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

/* From Uiverse.io by vinodjangid07 */ 
.bookmarkBtn {
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

.IconContainer {
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

.text {
  height: 100%;
  width: 80px;
  color: rgb(249,249,249);
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


/* From Uiverse.io by joe-watson-sbf */ 
.search {
  display: flex;
  align-items: center;
  justify-content: space-between;
  text-align: center;
  margin-top: 14px;
  margin-left: 20px;
  margin-right: -60px;
  z-index: 10;
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
  z-index: 10;
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

.search__input:focus{
  background-color: #f0eeee;
}

.search__button {
  border: none;
  background-color: transparent !important;
  margin-top: .1em;
  z-index: 10;
}

.search__button:hover {
  cursor: pointer;
}

.search__icon {
  height: 1.3em;
  width: 1.3em;
  background: none !important;
  background-color: transparent !important;
}

/* 覆盖图片 */
.logo {
  margin-left: 100px;
  margin-bottom: 5px;
  position: absolute;
  bottom: 0;
  left: 0;
  width: 120px; /* 覆盖图片的宽度 */
  height: 80px; /* 覆盖图片的高度 */
  background-image: url('@/assets/pic/图标.png'); /* 覆盖图片的路径 */
  background-size: cover;
  background-position: center;
  z-index: 1; /* 确保覆盖图片在背景图片之上 */
}

.button-container {
  display: flex;
  align-items: center;
}

/* From Uiverse.io by MuhammadHasann */ 
.btn-left{
  margin-top: 9px;
  color: rgb(249,249,249) ;
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

.btn-left:hover {
  background: transparent;
  color: #ffc710;
}

.btn-left:hover .star-1 {
  position: absolute;
  top: -100%;
  left: -60%;
  width: 25px;
  height: auto;
  filter: drop-shadow(0 0 10px #fffdef);
  z-index: 2;
}

.btn-left:hover .star-2 {
  position: absolute;
  top: 100%;
  left: -10%;
  width: 15px;
  height: auto;
  filter: drop-shadow(0 0 10px #fffdef);
  z-index: 2;
}

.btn-left:hover .star-3 {
  position: absolute;
  top: -60%;
  left: 25%;
  width: 5px;
  height: auto;
  filter: drop-shadow(0 0 10px #fffdef);
  z-index: 2;
}

.btn-left:hover .star-4 {
  position: absolute;
  top: -50%;
  left: 100%;
  width: 8px;
  height: auto;
  filter: drop-shadow(0 0 10px #fffdef);
  z-index: 2;
}

.btn-left:hover .star-5 {
  position: absolute;
  top: 25%;
  left: 115%;
  width: 15px;
  height: auto;
  filter: drop-shadow(0 0 10px #fffdef);
  z-index: 2;
}

.btn-left:hover .star-6 {
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

/* 顶部阴影层 */
.top-shadow-layer {
  position: absolute;
  z-index: 0;
  top: 0;
  left: 0;
  width: 100%;
  height: 60%;  /* 控制阴影覆盖范围 */
  background: linear-gradient(
    to bottom, 
    rgba(0, 0, 0, 0.575) 0%,  /* 顶部阴影浓度 */
    rgba(0, 0, 0, 0) 100%   /* 渐变到透明 */
  );
  pointer-events: none; /* 防止遮挡交互 */
}

.top-shadow-layer-1 {
  position: absolute;
  z-index: 0;
  margin-top: 114px;
  margin-bottom: 0px;
  border-radius: 8px;
  width: 237px;
  height: 25%;  /* 控制阴影覆盖范围 */
  background: linear-gradient(
    to top, 
    rgb(0, 0, 0) 0%,  /* 顶部阴影浓度 */
    rgba(0, 0, 0, 0) 85%   /* 渐变到透明 */
  );
  pointer-events: none; /* 防止遮挡交互 */
}

.top-shadow-layer-2 {
  position: absolute;
  z-index: 0;
  margin-top: 98px;
  border-radius: 8px;
  margin-bottom: 0px;
  width: 246px;
  height: 25%;  /* 控制阴影覆盖范围 */
  background: linear-gradient(
    to top, 
    rgb(0, 0, 0) 0%,  /* 顶部阴影浓度 */
    rgba(0, 0, 0, 0) 85%   /* 渐变到透明 */
  );
  pointer-events: none; /* 防止遮挡交互 */
}

.btn-text {
  color: rgb(249,249,249);
  font-size: 15px;
  font-weight: bolder;
  text-align: center; /* 文字居中 */
  display: inline-block; /* 将文字和图标都显示为行内元素 */
  vertical-align: middle;
}

.btn-text2{
  color: rgb(249,249,249);
  font-size: 15px;
  font-weight: bolder;
}

.bli-1{
  width: 23px;
  height: 23px;
  margin-right: 5px; /* 图标与文字之间的间距 */
  display: inline-block; /* 将图标和文字都显示为行内元素 */
  vertical-align: middle;
}

.bli-2{
  width: 15px;
  height: 15px;
  margin-right: 5px; /* 图标与文字之间的间距 */
  display: inline-block; /* 将图标和文字都显示为行内元素 */
  vertical-align: middle;
  fill: #f0eeee;
}

/* From Uiverse.io by mobinkakei */ 
.btn-left-1 {
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

.btn-left-1 span {
  font-size: 15px;
  text-transform: uppercase;
  letter-spacing: 1px;
  transition: top 0.5s;
}

.btn-text-one {
  position: absolute;
  width: 100%;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
}

.btn-text-two {
  position: absolute;
  width: 100%;
  top: 150%;
  left: 0;
  transform: translateY(-50%);
}

.btn-left-1:hover .btn-text-one {
  top: -100%;
}

.btn-left-1:hover .btn-text-two {
  top: 50%;
}


/* 新增背景层 */
.background-layer {
  position: absolute;
  z-index: -1; /* 确保在内容层下方 */
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('@/assets/pic/首页顶栏.avif');
  background-size: cover;
  background-position: center;
}

body {
  margin: 0;
  padding: 0;
}

.header {
  height: 150px;
}


.video,.botton-video {
  background-color: beige;
  height: 220px;
}

/* 添加新样式 */
.video, .botton-video {
  height: auto; /* 改为自适应高度 */
  flex-direction: column; /* 垂直布局 */
}

/* .video:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
} */

.header-center,.header-right,.header-left{
  height: 70px;
}

.header-left{
 width: 600px;
}

.video-row2{
  margin-top: 50px;
}

.recommend,.sort{
  margin-left: 150px;
  margin-right: 150px;
  padding: 10px;
}

/* 全局隐藏滚动条 */
html::-webkit-scrollbar {
  display: none;
}

html {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}

.botton{
  margin-left: 140px;
  margin-right: 140px;
  margin-top: 45px;
}

.sort-center,.sort-right,.sort-left{
  display: flex;
  background-color: transparent;
  height: 70px; 
}

.sort-right{
  position: absolute;
  margin-left: -30px;
  width: 250px;
}

.sort-center{
  background-color: transparent;
}

.sort-left{
  background-color: transparent;
}

.sort-left-botton-1{
  position: absolute;
  margin-top: 0px;
  margin-left: 20px;
  background-color: transparent;
  border: none;
}

.sort-left-botton-2{
  position: absolute;
  margin-top: 0px;
  margin-left: 120px;
  background-color: transparent;
  border: none;
}

.sort-text{
  font-size: 14px;
}

.sort-icon{
  width: 50px;
  height: 50px;
  fill: rgb(255, 146, 18);
}

.sort-icon-1{
  width: 50px;
  height: 50px;
  fill: rgb(240, 119, 117);
}


.recommend-left{
  margin-top: 11px;
 height: 400px;
}
.recommend-right{
  height: 460px;
}

/* 无限滚动容器 */
.infinite-list {
  height: 1000px;
  padding: 0;
  margin: 0;
  width: 100%;
  list-style: none;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}


.video-info {
  width: 100%;
  z-index: 1;
}

.meta:hover {
  color: rgb(0, 174, 236);
}

.title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box; /* 兼容 WebKit 浏览器 */
  display: box;         /* 标准属性 */
  -webkit-line-clamp: 1; /* WebKit 私有属性 */
  line-clamp: 1;        /* 标准属性（未来兼容） */
  -webkit-box-orient: vertical;
  box-orient: vertical;  /* 标准属性 */
}

.title:hover{
  color: rgb(0, 174, 236);
}

.meta {
  display: flex;
  font-size: 12px;
  color: #666;
}

/* 调整原有高度 */
.botton-video, .video {
  height: 220px; /* 恢复固定高度保持布局统一 */
}

/* 视频区块样式 */
.botton-video,.video {
  height: 150px;
  background: #000;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 10px 0;
  color: var(--el-color-primary);
  transition: transform 0.3s;
}

.thumbnail-img {
  position: absolute;
  border: none;
  border-radius: 8px;
  margin-left: 0;
  width: 237px;
  height: 150px;
  object-fit: cover; /* 裁剪图片以适应容器 */
  z-index: 0; 
}

.thumbnail-img-1 {
  position: absolute;
  border: none;
  border-radius: 8px;
  margin-left: 0;
  width: 246px;
  height: 150px;
  object-fit: cover; /* 裁剪图片以适应容器 */
  z-index: 0; 
}

.video-thumbnail {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 56.25%; /* 16:9 宽高比 */
  overflow: hidden;
}

/* .botton-video:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
} */

/* 间隔区块 */
.botton-padding {
  height: 10px;
}

/* 加载提示 */
.loading-text, .no-more {
  text-align: center;
  padding: 20px;
  color: #666;
}

.divider-menu{
  width: 350px;
  height: 500px;
}

.divider-text{
  font-size: 12px;
  color: rgb(148, 166, 193);
}

/* 取消 Element Plus 下拉抽屉组件的触发按钮悬停边框效果 */
.el-dropdown:hover {
  border: none !important;
  box-shadow: none !important;
}


.post-infinite-list {
  height: 400px;
  padding: 0;
  margin: 0;
  list-style: none;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}
.post-infinite-list .post-infinite-list-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100px;
  background: transparent;
  margin: 10px;
  color: var(--el-color-primary);
}
.post-infinite-list .post-infinite-list-item + .post-list-item {
  margin-top: 10px;
}

.post-row{
  display:flex !important;
}

.avatar-col{
  position:absolute;
  left: -150px;
  top: -30px;
}

.post-col{
  position:absolute;
  display: flex;
  left: -100px;
  top:-35px;
}

.post-info{
  width: 100px;
}

.post-name{
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 4px;
  color: rgb(119, 135, 159);
}

.post-content{
  position:absolute;
  top: 25px;
  font-size: 15px;
  color: rgb(24, 25, 28);
}

.post-time{
  position:absolute;
  top:60px;
  color: rgb(119, 135, 159);
  font-size: 11px;
}

.favorites-menu{
  width: 500px;
  height: 600px;
}

.favorites-infinite-list {
  height: 600px;
  padding: 0;
  margin: 0;
  list-style: none;
  cursor: pointer;
  padding: 8px;
  transition: background 0.3s;
}

.favorites-infinite-list-item:hover {
  background: #f5f5f5;
}

.video-title {
  font-weight: bold;
  margin-bottom: 4px;
}

.video-title1 {
  position: absolute;
  left: 300px;
  margin-top: -60px;
  font-size: 15px;
  color: #000;
}

.video-meta1 {
  position: absolute;
  color: #666;
  font-size: 0.9em;
  margin-top: 50px;
  left:300px;
}

.video-meta {
  color: #666;
  font-size: 0.9em;
}

.favorites-infinite-list .favorites-infinite-list-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 50px;
  background: transparent;
  color: var(--el-color-primary);
}

.favorites-infinite-list .favorites-infinite-list-item + .favorites-list-item {
  margin-top: 10px;
}

.video-favorites-infinite-list {
  height: 550px;
  padding: 0;
  margin: 0;
  list-style: none;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}
.video-favorites-infinite-list .video-favorites-infinite-list-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100px;
  background: transparent;
  margin: 10px;
  color: var(--el-color-primary);
}
.video-favorites-infinite-list .video-favorites-infinite-list-item + .video-favorites-list-item {
  margin-top: 10px;
}

.favorites-row{
  display:flex!important;
}

.favorites-name{
  position:absolute;
  top: -10px;
  width: 100px;
  left: -60px;
  color: rgb(24, 25, 28);
  font-size: 13px;
  font-weight: 700;
}

.favorites-video-count{
  position:absolute;
  top:-10px;
  width: 100px;
  left:30px;
  color: rgb(119, 135, 159);
}

/* From Uiverse.io by doniaskima */ 
.btn-12,
.btn-12 *,
.btn-12 :after,
.btn-12 :before,
.btn-12:after,
.btn-12:before {
  border: 0 solid;
  box-sizing: border-box;
}

.btn-12 {
  -webkit-tap-highlight-color: transparent;
  -webkit-appearance: button;
  appearance: button;
  background-color: #000;
  background-image: none;
  color: #fff;
  cursor: pointer;
  font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont,
    Segoe UI, Roboto, Helvetica Neue, Arial, Noto Sans, sans-serif,
    Apple Color Emoji, Segoe UI Emoji, Segoe UI Symbol, Noto Color Emoji;
  font-size: 100%;
  font-weight: 900;
  line-height: 1.5;
  margin: 0;
  -webkit-mask-image: -webkit-radial-gradient(#000, #fff);
  mask-image: radial-gradient(#000, #fff);
  padding: 0;
  text-transform: uppercase;
  left: 10px;
}

.btn-12:disabled {
  cursor: default;
}

.btn-12:-moz-focusring {
  outline: auto;
}

.btn-12 svg {
  display: block;
  vertical-align: middle;
}

.btn-12 [hidden] {
  display: none;
}

.btn-12 {
  border-radius: 99rem;
  border-width: 2px;
  overflow: hidden;
  padding: 0.8rem 3rem;
  position: relative;
}

.btn-12 span {
  mix-blend-mode: difference;
}

.btn-12:after,
.btn-12:before {
  background: linear-gradient(
    90deg,
    #fff 25%,
    transparent 0,
    transparent 50%,
    #fff 0,
    #fff 75%,
    transparent 0
  );
  content: "";
  inset: 0;
  position: absolute;
  transform: translateY(var(--progress, 100%));
  transition: transform 0.2s ease;
}

.btn-12:after {
  --progress: -100%;
  background: linear-gradient(
    90deg,
    transparent 0,
    transparent 25%,
    #fff 0,
    #fff 50%,
    transparent 0,
    transparent 75%,
    #fff 0
  );
  z-index: -1;
}

.btn-12:hover:after,
.btn-12:hover:before {
  --progress: 0;
}

/* From Uiverse.io by doniaskima */ 
.btn-13,
.btn-13 *,
.btn-13 :after,
.btn-13 :before,
.btn-13:after,
.btn-13:before {
  border: 0 solid;
  box-sizing: border-box;
}

.btn-13 {
  -webkit-tap-highlight-color: transparent;
  -webkit-appearance: button;
  background-color: #000;
  background-image: none;
  color: #fff;
  cursor: pointer;
  font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont,
    Segoe UI, Roboto, Helvetica Neue, Arial, Noto Sans, sans-serif,
    Apple Color Emoji, Segoe UI Emoji, Segoe UI Symbol, Noto Color Emoji;
  font-size: 100%;
  font-weight: 900;
  line-height: 1.5;
  margin: 0;
  -webkit-mask-image: -webkit-radial-gradient(#000, #fff);
  padding: 0;
  text-transform: uppercase;
  left: 10px;
}

.btn-13:disabled {
  cursor: default;
}

.btn-13:-moz-focusring {
  outline: auto;
}

.btn-13 svg {
  display: block;
  vertical-align: middle;
}

.btn-13 [hidden] {
  display: none;
}

.btn-13 {
  border-radius: 99rem;
  border-width: 2px;
  overflow: hidden;
  padding: 0.8rem 3rem;
  position: relative;
}

.btn-13 span {
  mix-blend-mode: difference;
}

.btn-13:after,
.btn-13:before {
  background: linear-gradient(
    90deg,
    #fff 25%,
    transparent 0,
    transparent 50%,
    #fff 0,
    #fff 75%,
    transparent 0
  );
  content: "";
  inset: 0;
  position: absolute;
  transform: translateY(var(--progress, 100%));
  transition: transform 0.2s ease;
}

.btn-13:after {
  --progress: -100%;
  background: linear-gradient(
    90deg,
    transparent 0,
    transparent 25%,
    #fff 0,
    #fff 50%,
    transparent 0,
    transparent 75%,
    #fff 0
  );
  z-index: -1;
}

.btn-13:hover:after,
.btn-13:hover:before {
  --progress: 0;
}


.play-icon{
  width: 20px;
  height: 20px;
  position:absolute;
  left: 25px;
  top: 12px;
}

.video-cover {
  text-align: center;
  border-right: solid 1px var(--el-border-color);
  display: inline-block;
  width: 20%;
  box-sizing: border-box;
  vertical-align: top;
  border-right: none;
  color: var(--el-text-color-secondary);
  position:absolute;
  left: 190px;
}

/* From Uiverse.io by e-coders */ 
u{
 appearance: none;
 background-color: rgb(246, 247, 248);
 border: none;
 border-radius: 0.2em;
 box-sizing: border-box;
 color: rgb(111, 116, 122);
 cursor: pointer;
 display: inline-block;
 font-family: Roobert,-apple-system,BlinkMacSystemFont,"Segoe UI",Helvetica,Arial,sans-serif,"Apple Color Emoji","Segoe UI Emoji","Segoe UI Symbol";
 font-size: 14px;
 font-weight: 500;
 line-height: normal;
 margin: 0;
 outline: none;
 padding: 1em 2.3em;
 text-align: center;
 text-decoration: none;
 transition: all 300ms cubic-bezier(.23, 1, 0.32, 1);
 user-select: none;
 -webkit-user-select: none;
 touch-action: manipulation;
 will-change: transform;
 height: 20px !important;
 width: 20px!important;
 margin-top: 3px;
 margin-left: 10px;
}

u:disabled {
 pointer-events: none;
}

u:hover {
 color: #fff;
 background-color: #1A1A1A;
 box-shadow: rgba(0, 0, 0, 0.25) 0 8px 15px;
 transform: translateY(-2px);
}

u:active {
 box-shadow: none;
 transform: translateY(0);
}

.sort-center-row{
  display:flex!important;
}

.u-text{
  position:absolute;
  margin-left: -15px!important;
  top: 4px !important;
}

.sort-right-divider{
  height: 70px !important;
}

/* From Uiverse.io by mobinkakei */ 
n{
  position: relative;
  margin: 0;
  outline: none;
  text-decoration: none;
  display: flex;
  cursor: pointer;
  text-transform: uppercase;
  background-color: transparent;
  border: none !important;
  border-radius: 10px;
  font-weight: 400;
  font-family: inherit;
  z-index: 0;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.02, 0.01, 0.47, 1);
  height: 30px!important;
  width: 80px!important;
  margin: 0px;
}

n span {
  margin-top: 5px;
  margin-left: 5px;
  position: absolute;
  left: 50%;
  transform: translate3d(0, 0, 0);
  transition: all 0.3s cubic-bezier(0.02, 0.01, 0.47, 1);
  transform: translate3d(-50%, 0, 0);
  color: rgb(97, 102, 128) !important;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.7px;
}

n:hover {
  animation: rotate624 0.7s ease-in-out both;
}

n:hover span {
  animation: storm1261 0.7s ease-in-out both;
  animation-delay: 0.06s;
}

@keyframes rotate624 {
  0% {
    transform: rotate(0deg) translate3d(0, 0, 0);
  }

  25% {
    transform: rotate(3deg) translate3d(0, 0, 0);
  }

  50% {
    transform: rotate(-3deg) translate3d(0, 0, 0);
  }

  75% {
    transform: rotate(1deg) translate3d(0, 0, 0);
  }

  100% {
    transform: rotate(0deg) translate3d(0, 0, 0);
  }
}

.btn-shine {
  border: 1px solid;
  overflow: hidden;
  position: relative;
}

.btn-shine span {
  z-index: 20;
}

.btn-shine:hover:after {
  left: 120%;
  transition: all 550ms cubic-bezier(0.19, 1, 0.22, 1);
}

.sort-right-col{
  display:flex!important;
}

.sort-right-col2{
  display:flex!important;
  margin-top: 5px;
}

.sort-right-botton{
  position: absolute;
  left: 15px;
}

.btn-shine-last{
  border: #000 1px solid;
  width: 100px !important;
}

.btn-shine-last span{
  position: absolute;
  left: 65px;
  width: 100px;
}

.src-icon{
  width: 15px;
  height: 15px;
  position:absolute;
  left: 5px;
  top: 7px;
  fill:rgb(97, 102, 109);
}

.carousel-item {
  position: relative;
  height: 100%;
}

.image-container {
  height: 100%; 
  overflow: hidden;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.views1{
  position: relative;
  margin-top: 112px;
  left: -80px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.views1-icon{
  width: 20px ;
  height: 20px;
  position:absolute;
  top: 132px;
  left: 20px;
  fill:rgb(226, 228, 228) !important;
  z-index: 1;
}

.barrage1{
  position: relative;
  margin-top: -17px;
  left: -25px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.barrage1-icon{
  width: 15px ;
  height: 15px;
  position:absolute;
  top: 135px;
  left: 85px;
  fill:rgb(226, 228, 228) !important;
  z-index: 1;
}

.time1{
  position: absolute;
  margin-top: 112px;
  margin-left: 190px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.views2{
  position: absolute;
  margin-top: 112px;
  margin-left: -155px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.views2-icon{
  width: 20px ;
  height: 20px;
  position:absolute;
  top: 132px;
  left: 300px;
  fill:rgb(226, 228, 228) !important;
  z-index: 1;
}

.barrage2{
  position: absolute;
  margin-top: 113px;
  margin-left: -35px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.barrage2-icon{
  width: 15px ;
  height: 15px;
  position:absolute;
  top: 135px;
  left: 365px;
  fill:rgb(226, 228, 228) !important;
  z-index: 1;
}

.time2{
  position: absolute;
  margin-top: 113px;
  margin-left: 190px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.views3{
  position: absolute;
  margin-top: 112px;
  margin-left: -175px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.views3-icon{
  width: 20px ;
  height: 20px;
  position:absolute;
  top: 132px;
  left: 575px;
  fill:rgb(226, 228, 228) !important;
  z-index: 1;
}

.barrage3{
  position: absolute;
  margin-top: 113px;
  margin-left: -55px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.barrage3-icon{
  width: 15px ;
  height: 15px;
  position:absolute;
  top: 135px;
  left: 640px;
  fill:rgb(226, 228, 228) !important;
  z-index: 1;
}

.time3{
  position: absolute;
  margin-top: 113px;
  margin-left: 190px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.views4{
  position: relative;
  margin-top: 112px;
  margin-left: -155px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.views4-icon{
  width: 20px ;
  height: 20px;
  position:absolute;
  top: 132px;
  left: 23px;
  fill:rgb(226, 228, 228) !important;
  z-index: 1;
}

.barrage4{
  position: absolute;
  margin-top: 113px;
  margin-left: -45px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.barrage4-icon{
  width: 15px ;
  height: 15px;
  position:absolute;
  top: 135px;
  left: 88px;
  fill:rgb(226, 228, 228) !important;
  z-index: 1;
}

.time4{
  position: absolute;
  margin-top: 113px;
  margin-left: 190px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.views5{
  position: absolute;
  margin-top: 112px;
  margin-left: -155px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.views5-icon{
  width: 20px ;
  height: 20px;
  position:absolute;
  top: 132px;
  left: 300px;
  fill:rgb(226, 228, 228) !important;
  z-index: 1;
}

.barrage5{
  position: absolute;
  margin-top: 113px;
  margin-left: -35px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.barrage5-icon{
  width: 15px ;
  height: 15px;
  position:absolute;
  top: 135px;
  left: 365px;
  fill:rgb(226, 228, 228) !important;
  z-index: 1;
}

.time5{
  position: absolute;
  margin-top: 113px;
  margin-left: 190px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.views6{
  position: absolute;
  margin-top: 112px;
  margin-left: -155px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.views6-icon{
  width: 20px ;
  height: 20px;
  position:absolute;
  top: 132px;
  left: 575px;
  fill:rgb(226, 228, 228) !important;
  z-index: 1;
}

.barrage6{
  position: absolute;
  margin-top: 113px;
  margin-left: -35px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.barrage6-icon{
  width: 15px ;
  height: 15px;
  position:absolute;
  top: 135px;
  left: 640px;
  fill:rgb(226, 228, 228) !important;
  z-index: 1;
}

.time6{
  position: absolute;
  margin-top: 113px;
  margin-left: 190px;
  font-size: 12px;
  color: rgb(226, 228, 228);
  z-index: 1;
}

.views{
  position: absolute;
  margin-top: -35px;
  margin-left: 33px;
  font-size: 12px;
  color: rgb(226, 228, 228);
}

.views-icon{
  width: 20px ;
  height: 20px;
  position:absolute;
  margin-top: -35px;
  margin-left: 8px;
  fill:rgb(255, 255, 255) !important;
}

.barrage{
  position: absolute;
  margin-top: -35px;
  margin-left: 93px;
  font-size: 12px;
  color: rgb(254, 255, 255) !important;
}

.barrage-icon{
  width: 15px ;
  height: 15px;
  position:absolute;
  margin-top: -32.5px;
  margin-left: 73px;
  fill:rgb(226, 228, 228) !important;
}

.f-time{
  position: absolute;
  margin-top: -34px;
  margin-left: 195px;
  font-size: 12px;
  color: rgb(226, 228, 228);
}

dropdown-container {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background: white;
  border: 1px solid #ccc;
  border-top: none;
  border-radius: 0 0 4px 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.dropdown-content {
  position: absolute;
  left: 710px !important;
  border-radius: 1cap;
  width: 520px;
  top: 8px;
  background-color: rgb(255, 255, 255) !important;
  z-index: 8;
}

.dropdown-item {
  padding: 10px;
  cursor: pointer;
}

.dropdown-item:hover {
  background-color: #f5f5f5;
}

.search-history {
  margin-top: 60px;
  height: 120px;
}

.search-history-title {
  margin-left: -430px;
  font-size: 17px;
  font-weight: 500;
}

.hot-search {
  height: 220px;
}

.hot-search-title {
  margin-left: -410px;
  font-size: 17px;
  font-weight: 500;
}

.search-history-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
  margin-left: 20px;
}

.history-button {
  display: flex;
  align-items: center;
  background-color: rgb(246, 247, 248);
  padding: 5px 10px;
  border-radius: 4px;
  height: 10px;
  font-size: 12px;
  cursor: pointer;
}

.delete-icon {
  margin-left: 5px;
  cursor: pointer;
  color: #999;
  z-index: 20;
}

.clear-history-button{
  position: absolute;
  left: 480px;
  font-size: 11px;
  color: rgb(197, 199, 203);
}

.hot-search-container {
  margin-top: 10px;
  display: flex;
}

.hot-search-column {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.hot-search-item1 {
  background-color: transparent;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 10px;
  width: 230px;
  text-align: left;
  font-size: 13px;
}

.hot-search-item2 {
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  width: 250px;
  text-align: left;
  font-size: 13px;
}

.num{
  font-size: 14px;
  font-weight: 600;
  margin-right: 10px;
}

.up-icon{
  margin-top: 2px;
  width: 15px;
  height: 15px;
  fill: rgb(148, 153, 181);
}

.vvauthor{
  margin-left: 5px !important;
}

.vvtime{
  margin-left: 10px!important;
}
</style>