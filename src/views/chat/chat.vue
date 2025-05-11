<script setup>
import { ref,onMounted,computed } from 'vue'
import { useRouter } from 'vue-router';
import { useTransition } from '@vueuse/core'
import CryptoJS from 'crypto-js'
import { ElMessage,ElMessageBox } from 'element-plus';
import { findAllVideos,getUserInfoByUsername,getHotSearch } from '../../api';

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

    const getUserInfo = async () => {
      //获取登录用户名
      let loginUser = JSON.parse(localStorage.getItem('loginUser'))
      if (loginUser) {
        userInfo.value.username = loginUser.username;
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

// 当前激活菜单项
const activeIndex = ref('')

// 菜单项数据
const menuItems = [
  { index: '1', label: '我的消息' },
  { index: '2', label: '回复我的' },
  { index: '3', label: '@ 我的' },
  { index: '4', label: '收到的赞' },
  { index: '5', label: '系统通知' },
  { index: '6', label: '其它通知' },
]   

const chatTitle = ref('我的消息');

const handleItemClick = (item) => {
  activeIndex.value = item.index;
  if (item.index === '1') {
    chatTitle.value = '我的消息';
    router.push('/chat/MyMessage').then(() => {
      window.location.reload();
});
  }else if (item.index === '2') {
    chatTitle.value = '回复我的';
    router.push('/chat/AnswerMe').then(() => {
      window.location.reload();
});
  }else if (item.index === '3') {
    chatTitle.value = '@ 我的';
    router.push('/chat/AtMe').then(() => {
      window.location.reload();
});
  }else if (item.index === '4') {
    chatTitle.value = '收到的赞';
    router.push('/chat/LikeMe').then(() => {
      window.location.reload();
});
  }else if (item.index === '5') {
    chatTitle.value = '系统通知';
    router.push('/chat/SystemMessage').then(() => {
      window.location.reload();
});
  }else if (item.index === '6') {
    chatTitle.value = '其它通知';
    router.push('/chat/OtherMessage').then(() => {
      window.location.reload();
});
  }
}

const getCurrentPath = () => {
  const pathArr = router.currentRoute.value.path.split('/');
  const index = pathArr[2];
  if(index === 'MyMessage'){
    activeIndex.value = '1';
    chatTitle.value = '我的消息';
  }else if(index === 'AnswerMe'){
    activeIndex.value = '2';
    chatTitle.value = '回复我的';
  }else if(index === 'AtMe'){
    activeIndex.value = '3';
    chatTitle.value = '@ 我的';
  }else if(index === 'LikeMe'){
    activeIndex.value = '4';
    chatTitle.value = '收到的赞';
  }else if(index === 'SystemMessage'){
    activeIndex.value = '5';
    chatTitle.value = '系统通知';
  }else if(index === 'OtherMessage'){
    activeIndex.value = '6';
    chatTitle.value = '其它通知';
  }
}

// 热搜数据
const hotSearchData = ref([
  {
    id: '',
    content :'',
    searchCount: '',
    createTime: ''
  }
]);
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

onMounted(async() => {
      await getUserInfo();
      await getCurrentPath();
      // JavaScript 动态添加类
document.body.classList.add('no-scroll');

getHotSearchData();

  //读取本地存储的搜索历史
  const storedHistory = localStorage.getItem('searchHistory');
  if (storedHistory) {
    searchHistory.value = JSON.parse(storedHistory);
  }
    })
</script>

<template>
  <div>
<img class="background-layer-chat" src="YilenaPic/25/05/01-b057a26471e14b759bde0c9529f6efe7.png"></img>
 <!-- 顶栏 -->
 <el-row class="chat-top">
    <el-col :span="24" class="chat-top">

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
    @keydown.space.prevent="handleSpace"
    @keydown.enter="addSearchHistory(searchText)" 
    v-model="searchText"
    >
    <button class="search__button" @click="addSearchHistory(searchText)">
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
      <el-dropdown-item>回复我的</el-dropdown-item>
        <el-dropdown-item>@ 我的</el-dropdown-item>
        <el-dropdown-item>收到的赞</el-dropdown-item>
        <el-dropdown-item>系统消息</el-dropdown-item>
        <el-dropdown-item>我的消息</el-dropdown-item>
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
             <button class="left-button-4" @click="toFavorites">
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

  <div class="menu-container1">
    <el-menu
      v-model:active="activeIndex"
      class="el-menu-vertical"
      :default-active="activeIndex"
      background-color="#f5f7fa"
      text-color="#333333"
      active-text-color="#409EFF"
    >
      <!-- 标题栏 -->
       <div class="title-item">
        <span class="title-text"><el-icon><Promotion /></el-icon>&nbsp;&nbsp;消息中心</span>
      </div>

      <!-- 消息项 -->
      <el-menu-item
        v-for="(item, id) in menuItems"
        :key="item.index"
        :index="item.index"
        class="message-item1"
        :class="{ active: activeIndex === item.index }"
        @click="handleItemClick(item)"
      >
        <span class="menu-content1">
          <span class="dot"></span>
          <span style="font-weight: 600;">{{ item.label }}</span>
        </span>
      </el-menu-item>
    </el-menu>
  </div>

  <div class="chat-content-bg">
  <div class="chat-title"><span class="chat-title-text">{{ chatTitle }}</span></div>

  <div class="chat-center">
    <router-view></router-view>
  </div>
</div>
</div>


</template>

<style>

/* 禁止滚动并固定滚动条宽度 */
.no-scroll {
  overflow: hidden;
  width: 100%;
  /* 滚动条宽度需与全局样式一致，避免布局抖动 */
  width: calc(100% - 7px); /* 假设滚动条宽度为7px  */
}

.chat-content-bg{
  position: absolute;
  top: 70px;
  left: 420px;
  width: 1000px;
  background-color: rgb(235, 245, 244);
  height: 765px;
  z-index: 200 !important;
}

.chat-center{
  margin-top: 25px;
  margin-left: 10px;
  position: absolute;
  width: 978px;
  border-radius: 5px;
  box-shadow: 0 0 0 0.07cm rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.chat-title-text{
  position: relative;
  top: 10px;
  left: 10px;
}

.chat-title{
  position: relative;
  top: 10px;
  margin-left: 10px;
  height: 43px;
  width: 978px;
  font-size: 16px;
  border-radius: 5px;
  background-color: rgb(255, 255, 255);
  color: rgb(102, 102, 102);
  box-shadow: 0 0 0 0.07cm rgba(0, 0, 0, 0.05);
}

.menu-container1 {
  width: 145px;
  height: 765px;
  display: flex;
  flex-direction: column;
  background-color: rgb(245, 247, 250);
  position: relative;
  top: 0px;
  margin-left: 275px;
}

.el-menu-vertical {
  flex: 1;
  border-right: none;
  display: flex;
  flex-direction: column;
  padding-bottom: 12px;
}

/* 标题栏样式 */
.title-item {
  height: 48px;
  line-height: 48px;
  cursor: default;
  background-color: transparent !important;
  z-index: 8;
}

.title-text {
  font-size: 14px;
  font-weight: 600;
  margin-left: 25px;
  color: #000000;
  margin-top: 20px;
}

/* 消息项样式 */
.message-item1 {
  margin-top: 10px;
  background-color: transparent !important;
  margin-bottom: -15px;
  z-index: 50;
}

.message-item1.active .menu-content1 {
  color: rgb(47, 174, 227);
}

.menu-content1:hover{
  color: rgb(47, 174, 227);
}

.menu-content1 {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-size: 600 !important;
  color: #333333;
}

/* 圆点图标 */
.dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  background-color: #999999;
  border-radius: 50%;
  margin-right: 10px;
}

/* 设置项样式 */
.settings-item {
  margin-top: auto;
  height: 40px;
  line-height: 40px;
  padding-left: 20px !important;
  font-size: 14px;
  color: #666666;
}

.settings-item:hover,
.settings-item:focus {
  background-color: #e9eef3;
}

.chat-top{
  z-index: 100 !important;
  box-shadow: 0 0 0 0.05cm rgba(0, 0, 0, 0.05);
  background-color: rgb(255, 255, 255);
  height: 70px;
}

.background-layer-chat{
  position: absolute;
  z-index: -100 !important;
  width: 1700px;
}

.header-center,.header-right,.header-left1{
  height: 70px;
  z-index: 10;
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