<script setup>
import * as echarts from 'echarts';
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useTransition } from '@vueuse/core'
import { ElMessage,ElMessageBox } from 'element-plus';
import { getVideoUploadList,getPostList,getUserList,getVideoNoPassList } from '@/api/dataShow';

const videoChartDom = ref(null);
const postChartDom = ref(null);
const userChartDom = ref(null);
const videoNoPassChartDom = ref(null);

let myChartFollowers1;
let myChartFollowers2;
let myChartFollowers3;
let myChartFollowers4;

const isLoading = ref(false);

const value1 = ref('1')
const value2 = ref('1')
const value3 = ref('1')
const value4 = ref('1')

watch(value1, () => {
  getVideoUploadReport(); 
})

watch(value2, () => {
  getPostReport(); 
})

watch(value3, () => {
  getUserReport(); 
})

watch(value4, () => {
  getVideoNoPassReport(); 
})

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

const formatDate = (timestamp) => {
  const date = new Date(timestamp);
  const pad = n => n.toString().padStart(2, '0'); // 补零函数
  return `${pad(date.getMonth() + 1)}/${pad(date.getDate())}`;
}

const getVideoUploadReport = async () => {
  const result = await getVideoUploadList(value1.value);
  if(result.code === 1){
    const data = result.data; 
    const xAxisData = [];
    const seriesData = [];
    for(const item of data){
      xAxisData.push(formatDate(item.date));
      seriesData.push(item.count);
    }
    myChartFollowers1.setOption({
      title: {
        text: '视频投稿趋势'
      },
      xAxis: {
        data: xAxisData
      },
      series: [
        {
          data: seriesData,
          name: '新增投稿视频'
        }
      ]
    });
  }
}

const getPostReport = async () => {
  const result = await getPostList(value2.value);
  if(result.code === 1){
    const data = result.data; 
    const xAxisData = [];
    const seriesData = [];
    for(const item of data){
      xAxisData.push(formatDate(item.date));
      seriesData.push(item.count);
    }
    myChartFollowers2.setOption({
      title: {
        text: '动态上传趋势'
      },
      xAxis: {
        data: xAxisData
      },
      series: [
        {
          data: seriesData,
          name: '新增上传投稿'
        }
      ]
    });
  }
}

const getUserReport = async () => {
  const result = await getUserList(value3.value);
  if(result.code === 1){
    const data = result.data; 
    const xAxisData = [];
    const seriesData = [];
    for(const item of data){
      xAxisData.push(formatDate(item.date));
      seriesData.push(item.count);
    }
    myChartFollowers3.setOption({
      title: {
        text: '新用户注册趋势'
      },
      xAxis: {
        data: xAxisData
      },
      series: [
        {
          data: seriesData,
          name: '新增注册用户'
        }
      ]
    });
  }
}

const getVideoNoPassReport = async () => {
  const result = await getVideoNoPassList(value4.value);
  if(result.code === 1){
    const data = result.data; 
    const xAxisData = [];
    const seriesData = [];
    for(const item of data){
      xAxisData.push(formatDate(item.date));
      seriesData.push(item.count);
    }
    myChartFollowers4.setOption({
      title: {
        text: '视频下架趋势'
      },
      xAxis: {
        data: xAxisData
      },
      series: [
        {
          data: seriesData,
          name: '新增下架视频'
        }
      ]
    });
  }
}

onMounted(() => { 
  if (videoChartDom.value) {
    myChartFollowers1 = echarts.init(videoChartDom.value);
    getVideoUploadReport();
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

    myChartFollowers1.setOption(option);
  } else {
    console.error('Initialize failed: invalid dom.');
  }

  if (postChartDom.value) {
    myChartFollowers2 = echarts.init(postChartDom.value);
    getPostReport();
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

    myChartFollowers2.setOption(option);
  } else {
    console.error('Initialize failed: invalid dom.');
  }

   if (userChartDom.value) {
    myChartFollowers3 = echarts.init(userChartDom.value);
    getUserReport();
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

    myChartFollowers3.setOption(option);
  } else {
    console.error('Initialize failed: invalid dom.');
  }

   if (videoNoPassChartDom.value) {
    myChartFollowers4 = echarts.init(videoNoPassChartDom.value);
    getVideoNoPassReport();
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

    myChartFollowers4.setOption(option);
  } else {
    console.error('Initialize failed: invalid dom.');
  }

  isLoading.value = false;
});
</script>

<template>
    <div class="loader" v-if="isLoading">
  <div class="wrapper">
    <div class="catContainer">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 733 673"
        class="catbody"
      >
        <path
          fill="#212121"
          d="M111.002 139.5C270.502 -24.5001 471.503 2.4997 621.002 139.5C770.501 276.5 768.504 627.5 621.002 649.5C473.5 671.5 246 687.5 111.002 649.5C-23.9964 611.5 -48.4982 303.5 111.002 139.5Z"
        ></path>
        <path fill="#212121" d="M184 9L270.603 159H97.3975L184 9Z"></path>
        <path fill="#212121" d="M541 0L627.603 150H454.397L541 0Z"></path>
      </svg>
      <svg
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 158 564"
        class="tail"
      >
        <path
          fill="#191919"
          d="M5.97602 76.066C-11.1099 41.6747 12.9018 0 51.3036 0V0C71.5336 0 89.8636 12.2558 97.2565 31.0866C173.697 225.792 180.478 345.852 97.0691 536.666C89.7636 553.378 73.0672 564 54.8273 564V564C16.9427 564 -5.4224 521.149 13.0712 488.085C90.2225 350.15 87.9612 241.089 5.97602 76.066Z"
        ></path>
      </svg>
      <div class="text">
        <span class="bigzzz">Z</span>
        <span class="zzz">Z</span>
      </div>
    </div>
    <div class="wallContainer">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 500 126"
        class="wall"
      >
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="3"
          x2="450"
          y1="3"
          x1="50"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="85"
          x2="400"
          y1="85"
          x1="100"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="122"
          x2="375"
          y1="122"
          x1="125"
        ></line>
        <line stroke-width="6" stroke="#7C7C7C" y2="43" x2="500" y1="43"></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="1.99391"
          x2="115.5"
          y1="43.0061"
          x1="115.5"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="2.00002"
          x2="189"
          y1="43.0122"
          x1="189"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="2.00612"
          x2="262.5"
          y1="43.0183"
          x1="262.5"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="2.01222"
          x2="336"
          y1="43.0244"
          x1="336"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="2.01833"
          x2="409.5"
          y1="43.0305"
          x1="409.5"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="43"
          x2="153"
          y1="84.0122"
          x1="153"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="43"
          x2="228"
          y1="84.0122"
          x1="228"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="43"
          x2="303"
          y1="84.0122"
          x1="303"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="43"
          x2="378"
          y1="84.0122"
          x1="378"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="84"
          x2="192"
          y1="125.012"
          x1="192"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="84"
          x2="267"
          y1="125.012"
          x1="267"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="84"
          x2="342"
          y1="125.012"
          x1="342"
        ></line>
      </svg>
    </div>
  </div>
</div>
 <div class="data" v-else>
  <el-row>
    <div class="video-data">
<el-select class="date-select" size="small" v-model="value1" placeholder="近七天">
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
      <div class="data-show" ref="videoChartDom" style="width: 600px; height: 300px;"></div>
    </div>
    <div class="video-data">
<el-select class="date-select" size="small" v-model="value2" placeholder="近七天">
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
      <div class="data-show" ref="postChartDom" style="width: 600px; height: 300px;"></div>
    </div>
    </el-row>
    <el-row>
    <div class="video-data">
<el-select class="date-select1" size="small" v-model="value3" placeholder="近七天">
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
      <div class="data-show" ref="userChartDom" style="width: 600px; height: 300px;"></div>
    </div>
    <div class="video-data">
<el-select class="date-select" size="small" v-model="value4" placeholder="近七天">
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
      <div class="data-show" ref="videoNoPassChartDom" style="width: 600px; height: 300px;"></div>
    </div>
    </el-row>
 </div>
</template>

<style scoped>
.date-select1{
  position: absolute;
  width: 75px;
  margin-left: 150px;
  margin-top: 15px;
  z-index: 9999;
}

.date-select{
  position: absolute;
  width: 75px;
  margin-left: 130px;
  margin-top: 15px;
  z-index: 9999;
}

.video-data{
  margin-top: 40px;
  margin-left: 68px;
  background-color: rgb(240, 248, 255);
  width: 630px;
  height: 330px;
  border-radius: 10px;
}

.data-show{
  position: relative;
  top: 15px;
  margin-left: 10px;
}

.loader {
  width: fit-content;
  height: fit-content;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 200px;
  margin-left: 560px;
}
.wrapper {
  width: fit-content;
  height: fit-content;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.catContainer {
  width: 100%;
  height: fit-content;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.catbody {
  width: 80px;
}
.tail {
  position: absolute;
  width: 17px;
  top: 50%;
  animation: tail 0.5s ease-in infinite alternate-reverse;
  transform-origin: top;
}
@keyframes tail {
  0% {
    transform: rotateZ(60deg);
  }
  50% {
    transform: rotateZ(0deg);
  }
  100% {
    transform: rotateZ(-20deg);
  }
}
.wall {
  width: 300px;
}
.text {
  display: flex;
  flex-direction: column;
  width: 50px;
  position: absolute;
  margin: 0px 0px 100px 120px;
}
.zzz {
  color: black;
  font-weight: 700;
  font-size: 15px;
  animation: zzz 2s linear infinite;
}
.bigzzz {
  color: black;
  font-weight: 700;
  font-size: 25px;
  margin-left: 10px;
  animation: zzz 2.3s linear infinite;
}
@keyframes zzz {
  0% {
    color: transparent;
  }
  50% {
    color: black;
  }
  100% {
    color: transparent;
  }
}
</style>
