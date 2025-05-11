<script lang="ts" setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router';
import { useTransition } from '@vueuse/core'
import CryptoJS from 'crypto-js'
import { ElMessage, ElMessageBox } from 'element-plus';
import { findAllVideos, getUserInfoByUsername } from '../../api';
import * as echarts from 'echarts';
import { getReportFollowers,getCommentQurey,getReportCoins,getReportComments,getReportFavorites,getReportLikes } from '../../api/upload';

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
      if (loginUser) {
        userInfo.value.username = loginUser.username;
        token.value = loginUser.token;
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

const chartDom = ref(null); // 定义 ref

let myChartFollowers;

const value = ref('1')

const formatDate = (timestamp) => {
  const date = new Date(timestamp);
  const pad = n => n.toString().padStart(2, '0'); // 补零函数
  return `${pad(date.getMonth() + 1)}/${pad(date.getDate())}`;
}

const getReportFollowersTo = async () => {
  const result = await getReportFollowers(userInfo.value.id,value.value);
  if(result.code === 1){
    const data = result.data; 
    const xAxisData = [];
    const seriesData = [];
    let count = 0;
    for(const item of data){
      xAxisData.push(formatDate(item.date));
      seriesData.push(item.count);
      count += item.count;
    }
    followers.value = count;
    if(selectedIndex.value == 1){
    myChartFollowers.setOption({
      title: {
        text: '新增粉丝趋势'
      },
      xAxis: {
        data: xAxisData
      },
      series: [
        {
          data: seriesData,
          name: '新增粉丝'
        }
      ]
    });
  }
  }
}

const getReportLikesTo = async () => {
  const result = await getReportLikes(userInfo.value.id,value.value);
  if(result.code === 1){
    const data = result.data; 
    const xAxisData = [];
    const seriesData = [];
    let count = 0;
    for(const item of data){
      xAxisData.push(formatDate(item.date));
      seriesData.push(item.count);
      count += item.count;
    }
    likes.value = count;
    if(selectedIndex.value == 2){
    myChartFollowers.setOption({
      title: {
        text: '新增点赞趋势'
      },
      xAxis: {
        data: xAxisData
      },
      series: [
        {
          data: seriesData,
          name: '新增点赞'
        }
      ]
    });
  }
  }
}

const getReportFavoritesTo = async () => {
  const result = await getReportFavorites(userInfo.value.id,value.value);
  if(result.code === 1){
    const data = result.data; 
    const xAxisData = [];
    const seriesData = [];
    let count = 0;
    for(const item of data){
      xAxisData.push(formatDate(item.date));
      seriesData.push(item.count);
      count += item.count;
    }
    favorites.value = count;
    if(selectedIndex.value == 3){
    myChartFollowers.setOption({
      title: {
        text: '新增收藏趋势'
      },
      xAxis: {
        data: xAxisData
      },
      series: [
        {
          data: seriesData,
          name: '新增收藏'
        }
      ]
    });
  }
  }
}

const getReportCoinsTo = async () => {
  const result = await getReportCoins(userInfo.value.id,value.value);
  if(result.code === 1){
    const data = result.data; 
    const xAxisData = [];
    const seriesData = [];
    let count = 0;
    for(const item of data){
      xAxisData.push(formatDate(item.date));
      seriesData.push(item.count);
      count += item.count;
    }
    coins.value = count;
    if(selectedIndex.value == 4){
    myChartFollowers.setOption({
      title: {
        text: '新增投币趋势'
      },
      xAxis: {
        data: xAxisData
      },
      series: [
        {
          data: seriesData,
          name: '新增投币'
        }
      ]
    });
  }
  }
}

const getReportCommentsTo = async () => {
  const result = await getReportComments(userInfo.value.id,value.value);
  if(result.code === 1){
    const data = result.data; 
    const xAxisData = [];
    const seriesData = [];
    let count = 0;
    for(const item of data){
      xAxisData.push(formatDate(item.date));
      seriesData.push(item.count);
      count += item.count;
    }
    comments.value = count;
    if(selectedIndex.value == 5){
    myChartFollowers.setOption({
      title: {
        text: '新增评论趋势'
      },
      xAxis: {
        data: xAxisData
      },
      series: [
        {
          data: seriesData,
          name: '新增评论'
        }
      ]
    });
  }
  }
}

const selectedIndex = ref(1);

const activeIndex = ref('1')
const handleSelect = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
  activeIndex.value = key;
}

const followers = ref('0');
const likes = ref('0');
const favorites = ref('0');
const coins = ref('0')
const comments = ref('0');

watch(selectedIndex,(newValue1) => {
  console.log(newValue1)
  if(newValue1 == 1){
  getReportFollowersTo();
  }else if(newValue1 == 2){
    getReportLikesTo();
  }else if(newValue1 == 3){
    getReportFavoritesTo(); 
  }else if(newValue1 == 4){
    getReportCoinsTo(); 
  }else if(newValue1 == 5){
    getReportCommentsTo(); 
  }
});

watch(value,() => {
  if(activeIndex.value == '1'){
  getReportFollowersTo();
    getReportLikesTo();
    getReportFavoritesTo(); 
    getReportCoinsTo(); 
    getReportCommentsTo();
  }
});

watch(activeIndex,() =>{
  if(activeIndex.value == '1'){
  getReportFollowersTo();
    getReportLikesTo();
    getReportFavoritesTo(); 
    getReportCoinsTo(); 
    getReportCommentsTo();
  }
})

onMounted(async() => {
  await getUserInfo();
  if (chartDom.value) {
    myChartFollowers = echarts.init(chartDom.value);
    if(activeIndex.value == '1'){
  getReportFollowersTo();
    getReportLikesTo();
    getReportFavoritesTo(); 
    getReportCoinsTo(); 
    getReportCommentsTo();
  }
    const option = {
      title: {
        text: 'Stacked Line'
      },
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      toolbox: {
        feature: {
          saveAsImage: {}
        }
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: []
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: 'Direct',
          type: 'line',
          stack: 'Total',
          color: 'rgb(255, 102, 153)',
          data: []
        },
      ]
    };

    myChartFollowers.setOption(option);
  } else {
    console.error('Initialize failed: invalid dom.');
  }
});



const options = [
  {
    value: '1',
    label: '近7天',
  },
  {
    value: '2',
    label: '近30天',
  },
  {
    value: '3',
    label: '近90天',
  }
]


</script>

<template>
  <el-menu
    :default-active="activeIndex"
    class="data-menu"
    mode="horizontal"
    @select="handleSelect"
  >
    <el-menu-item index="1">数据概览</el-menu-item>
    <el-menu-item index="2" disabled>粉丝分析</el-menu-item>
  </el-menu>
  <div class="data-core">
    <span class="data-core-title">数据概览</span>
    <el-select class="date-select" v-model="value" placeholder="Select">
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
    <el-row class="data-core-content-row" v-if="activeIndex == '1'">
      <div class="data-core-content"
      :class="{ active: selectedIndex === 1 }"
      @click="selectedIndex = 1">
        <span class="data-core-content-title">新增粉丝</span>
        <span class="data-core-content-number">{{ followers }}</span>
      </div>
      <div class="data-core-content"
      :class="{ active: selectedIndex === 2 }"
      @click="selectedIndex = 2">
        <span class="data-core-content-title">点赞</span>
        <span class="data-core-content-number">{{ likes }}</span>
      </div>
      <div class="data-core-content"
      :class="{ active: selectedIndex === 3 }"
      @click="selectedIndex = 3">
        <span class="data-core-content-title">收藏</span>
        <span class="data-core-content-number">{{ favorites }}</span>
      </div>
      <div class="data-core-content"
      :class="{ active: selectedIndex === 4 }"
      @click="selectedIndex = 4">
        <span class="data-core-content-title">硬币</span>
        <span class="data-core-content-number">{{ coins }}</span>
      </div>
      <div class="data-core-content"
      :class="{ active: selectedIndex === 5 }"
      @click="selectedIndex = 5">
        <span class="data-core-content-title">评论</span>
        <span class="data-core-content-number">{{ comments }}</span>
      </div>
    </el-row>
    <div class="data-show" ref="chartDom" style="width: 100%; height: 400px;"></div> <!-- 添加 ref 绑定 -->
  </div>
</template>

<style scoped>
.data-core {
  height: 400px;
  background-color: rgb(255, 255, 255);
  margin-top: 20px;
  margin-left: 200px;
  width: 1040px;
}

.data-menu {
  background-color: transparent;
  margin-left: 200px;
  width: 1040px;
}

.data-core-title {
  font-weight: 600;
  font-size: 18px;
  position: relative;
  top: 10px;
  margin-left: 5px;
}

.data-core-content-row {
  margin-left: 30px;
}

.data-core-content {
  margin-top: 30px;
  display: flex;
  background-color: rgb(255, 247, 247);
  height: 80px;
  margin-left: 10px;
  width: 185px;
  border-radius: 10px;
  cursor: pointer; /* 添加指针样式 */
}

.data-core-content:hover {
  background-color: rgba(255, 102, 153, 0.2); /* 悬停颜色稍浅 */
}

.data-core-content.active {
  background-color: rgb(255, 102, 153) !important; /* 选中状态 */
}

.data-core-content.active .data-core-content-title {
  color: white !important; /* 标题变白 */
}

.data-core-content.active .data-core-content-number {
  color: white !important; /* 标题变白 */
}
.data-core-content-title {
  margin-top: 10px;
  margin-left: 18px;
  font-size: 15px;
  font-weight: 600;
  color: rgb(102, 102, 102);
}

.data-core-content {
  display: flex;
  flex-direction: column;  /* 垂直排列 */
}

.data-core-content-title {
  margin-bottom: 8px;      /* 标题与数字间距 */
}

.data-core-content-number {
  margin-left: 20px;
  margin-top: -5px;
  font-size: 22px;
  font-weight: 600;
  color: rgb(0, 0, 0);
}

.data-show{
  margin-top: 40px;
}

.date-select{
  position: relative;
  top: 10px;
  margin-left: 815px;
  width: 90px;
}
</style>