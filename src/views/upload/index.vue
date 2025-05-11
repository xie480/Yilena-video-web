<script setup>
import { ref,onMounted,computed,watch } from 'vue'
import { useRouter } from 'vue-router';
import { useTransition } from '@vueuse/core'
import CryptoJS from 'crypto-js'
import { ElMessage,ElMessageBox } from 'element-plus';
import { findAllVideos,getUserInfoByUsername } from '../../api';
import { getAllCoins,getAllComments,getAllFavorites,getAllLikes,getAllShares,getAllViews,getBarrages } from '../../api/upload';

const router = useRouter();

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
      allCoins: '',
      allComments: '',
      allFavorites: '',
      allLikes: '',
      allShares: '',
      allViews: '',
      allBrrages: '',
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

const getCurrentUserAllViews = async () => {
    const result = await getAllViews(userInfo.value.id);
    if(result.code === 1){
      userInfo.value.allViews = result.data;
    }
}

const getCurrentUserAllLikes = async () => {
    const result = await getAllLikes(userInfo.value.id);
    if(result.code === 1){
      userInfo.value.allLikes = result.data;
    }
}

const getCurrentUserAllFavorites = async () => {
    const result = await getAllFavorites(userInfo.value.id);
    if(result.code === 1){
      userInfo.value.allFavorites = result.data;
    }
}

const getCurrentUserAllCoins = async () => {
    const result = await getAllCoins(userInfo.value.id);
    if(result.code === 1){
      userInfo.value.allCoins = result.data;
    }
}

const getCurrentUserAllBarrages = async () => {
    const result = await getBarrages(userInfo.value.id);
    if(result.code === 1){
      userInfo.value.allBrrages = result.data;
    }
}

const getCurrentUserAllComments = async () => {
    const result = await getAllComments(userInfo.value.id);
    if(result.code === 1){
      userInfo.value.allComments = result.data;
    }
}

const getCurrentUserAllShares = async () => {
    const result = await getAllShares(userInfo.value.id);
    if(result.code === 1){
      userInfo.value.allShares = result.data;
    }
}



    onMounted(async() => {  
      await getUserInfo();
      getCurrentUserAllViews();
      getCurrentUserAllLikes();
      getCurrentUserAllFavorites();
      getCurrentUserAllCoins();
      getCurrentUserAllBarrages();
      getCurrentUserAllComments();
      getCurrentUserAllShares();
    })

</script>

<template>
  <div class="i-top">
    <img class="i-top-pic" src="YilenaPic/25/04/18-c54fb0473d1b4b7ebf3634ad497ee631.jpg"/>
  </div>
  <div class="i-data">
   <div class="i-data-title">
    <span class="i-data-title-text">视频数据</span>
   </div>

   <div class="i-data-content">
    <el-row>
    <div class="i-data-content-1">
        <svg t="1744971992904" class="i-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3945" width="200" height="200"><path d="M799.125831 1022.776226V733.331004c-64.862547-69.743823-172.768275-31.63092-186.417002 62.172505-13.598469 93.809708 90.423598 190.807009 186.417002 227.272717 95.998429-36.416707 200.020496-133.513266 186.42077-227.272717-13.652496-93.804682-121.55571-131.966587-186.42077-62.222763v289.49548zM511.831637 547.563583c-145.490925-7.701988-259.53814-127.855509-259.653733-273.551235C252.177904 122.670174 375.031519 0.01759 526.526979 0.01759a274.151813 274.151813 0 0 1 274.345306 273.994758c0.010052 98.324103-52.698935 189.099504-138.092997 237.83311 31.531661 14.895117 60.026503 32.928825 79.207343 51.81063 13.568314 13.119764 15.43413 34.216677 4.384981 49.515112-12.956427 19.330356-38.860486 15.893988-50.713757 5.532113-47.33016-41.099465-123.251907-70.442405-176.70345-70.442405a37.307524 37.307524 0 0 1-7.122768-0.697325z m14.695342-74.279578a199.357094 199.357094 0 0 0 141.036839-58.305178 199.359607 199.359607 0 0 0 58.48108-140.966479c0-109.997703-89.319185-199.266631-199.516662-199.26663a199.364633 199.364633 0 0 0-199.521689 199.26663c0 110.096962 89.324211 199.271656 199.520432 199.271657z m-330.63892 111.494125c18.73229-16.94186 46.129001-16.641571 63.114837 2.090719 16.94186 18.73229 14.995632 44.587347-3.736658 61.527951-79.456118 71.834542-129.271519 177.647038-129.271519 286.745129 0 25.312275-17.289894 43.295725-42.597144 43.295725-25.258248 0-43.19144-17.98345-43.19144-43.295725-0.125644-133.577345 56.465747-260.923995 155.681924-350.363799z m0 0" p-id="3946"></path></svg>
        <div class="i-data-content-1-title">
            <span>粉丝总数&nbsp;&nbsp;></span>
        </div>
        <span class="i-count">{{ userInfo.followersCount }}</span>
    </div>
    <div class="i-data-content-1">
        <svg t="1744972351862" class="i-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5797" width="200" height="200"><path d="M102.425742 102.393565v819.148516l614.361387-409.574258z"  p-id="5798"></path><path d="M153.622524 102.393565v819.148516l614.361387-409.574258z"  p-id="5799"></path><path d="M259.599863 15.871003V834.507551l619.481066-405.478515z"  p-id="5800"></path><path d="M261.135767 189.428094l-1.535904 818.636549L875.497154 599.002353z"  p-id="5801"></path><path d="M204.819306 102.393565m-102.393564 0a102.393565 102.393565 0 1 0 204.787129 0 102.393565 102.393565 0 1 0-204.787129 0Z"  p-id="5802"></path><path d="M819.180694 409.574258c-56.316461 0-102.393565 46.077104-102.393565 102.393565s46.077104 100.345693 102.393565 102.393564c57.852364 2.047871 102.905532-45.053168 102.393564-102.393564-0.511968-56.316461-46.077104-102.393565-102.393564-102.393565zM204.819306 819.148517c-56.316461 0-102.393565 46.077104-102.393564 102.393564s46.077104 100.345693 102.393564 102.393565c53.756621 2.047871 100.857661-45.053168 102.393565-102.393565 1.535903-56.316461-46.077104-102.393565-102.393565-102.393564z"  p-id="5803"></path></svg>
        <div class="i-data-content-1-title">
            <span>播放量&nbsp;&nbsp;></span>
        </div>
        <span class="i-count">{{ userInfo.allViews || 0 }}</span>
</div>
<div class="i-data-content-1">
    <svg t="1744972491686" class="i-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="7092" id="mx_n_1744972491687" width="200" height="200"><path d="M960 512C960 759.424 759.424 960 512 960 428.64 960 350.848 936.832 284 897.152L99.232 949.952C83.872 954.304 69.664 940.128 74.048 924.768L126.848 740C87.168 673.152 64 595.36 64 512 64 264.576 264.576 64 512 64 759.424 64 960 264.576 960 512" p-id="7093" ></path></svg>
        <div class="i-data-content-1-title">
            <span>评论&nbsp;&nbsp;></span>
        </div>
        <span class="i-count">{{ userInfo.allComments || 0 }}</span>
</div>
<div class="i-data-content-1">
    <svg t="1744972585081" class="i-icon" viewBox="0 0 1170 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="8965" width="200" height="200"><path d="M961.316571 0C1078.345143 0 1170.285714 93.842286 1170.285714 213.357714v597.284572C1170.285714 930.157714 1078.345143 1024 961.316571 1024H208.969143C91.940571 1024 0 930.157714 0 810.642286V213.357714C0 93.842286 91.940571 0 208.969143 0h752.347428z m-731.428571 426.642286h83.602286V341.357714H229.888v85.284572z m167.204571 0h501.540572V341.357714H397.092571v85.284572z m83.529143 256h459.776V597.357714H480.621714v85.284572z m-167.131428 0h83.602285V597.357714H313.490286v85.284572z"  p-id="8966"></path></svg>
        <div class="i-data-content-1-title">
            <span>弹幕&nbsp;&nbsp;></span>
        </div>
        <span class="i-count">{{ userInfo.allBrrages || 0 }}</span>
</div>
</el-row>
<el-row>
    <div class="i-data-content-1">
        <svg t="1744972893892" class="i-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="10012" width="200" height="200"><path d="M64 483.04V872c0 37.216 30.144 67.36 67.36 67.36H192V416.32l-60.64-0.64A67.36 67.36 0 0 0 64 483.04zM857.28 344.992l-267.808 1.696c12.576-44.256 18.944-83.584 18.944-118.208 0-78.56-68.832-155.488-137.568-145.504-60.608 8.8-67.264 61.184-67.264 126.816v59.264c0 76.064-63.84 140.864-137.856 148L256 416.96v522.4h527.552a102.72 102.72 0 0 0 100.928-83.584l73.728-388.96a102.72 102.72 0 0 0-100.928-121.824z" p-id="10013"></path></svg>
        <div class="i-data-content-1-title">
            <span>点赞&nbsp;&nbsp;></span>
        </div>
        <span class="i-count">{{ userInfo.allLikes || 0 }}</span>
    </div>
    <div class="i-data-content-1">
        <svg t="1744972948534" class="i-icon" viewBox="0 0 1233 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="11003" width="200" height="200"><path d="M106.729318 963.911564a21.561478 21.561478 0 0 1-7.035851-0.79437 49.477919 49.477919 0 0 1-42.101623-54.584585c24.171552-226.96293 104.516429-394.575054 237.85715-499.318446a624.885687 624.885687 0 0 1 361.041281-125.567241l29.618663-1.532V120.687538c0-63.946806 58.499695-77.961766 89.650357-46.811104L1139.921317 444.279936a90.217765 90.217765 0 0 1-2.32637 141.851831l-354.799801 361.041281a55.151992 55.151992 0 0 1-96.459245-38.186513v-172.77553h-36.654514c-133.340721 0-375.850612 26.497922-502.949853 205.117748a43.974068 43.974068 0 0 1-39.718512 22.696293z" p-id="11004"></path></svg>
        <div class="i-data-content-1-title">
            <span>分享&nbsp;&nbsp;></span>
        </div>
        <span class="i-count">{{ userInfo.allShares || 0 }}</span>
</div>
<div class="i-data-content-1">
    <svg t="1744973060723" class="i-icon" viewBox="0 0 1426 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="12033" width="200" height="200"><path d="M985.6 1022.976c-14.848 0-31.744-4.096-47.104-12.288L716.288 899.584l-223.744 111.104c-14.336 7.68-30.208 11.776-47.104 11.776-21.504 0-42.496-6.656-59.392-19.456-31.232-23.552-47.104-64-39.936-101.376l45.568-237.056-175.616-163.328c-27.136-27.648-37.376-67.072-27.136-104.448l0.512-1.024c12.8-38.4 44.544-65.024 82.944-70.144l243.712-44.544L625.152 58.88C642.56 23.552 678.4 1.024 716.288 1.024c39.424 0 76.288 23.552 91.648 58.368l109.056 221.696 243.712 42.496c38.4 5.632 70.656 33.28 81.408 71.168 12.288 36.864 2.048 77.312-25.6 104.96l-0.512 0.512-174.592 164.864 44.032 237.568c7.168 37.888-8.192 76.288-39.424 100.352-17.92 12.8-38.912 19.968-60.416 19.968z" p-id="12034"></path></svg>
        <div class="i-data-content-1-title">
            <span>收藏&nbsp;&nbsp;></span>
        </div>
        <span class="i-count">{{ userInfo.allFavorites || 0 }}</span>
</div>
<div class="i-data-content-1">
    <svg t="1744973286358" class="i-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="13077" width="200" height="200"><path d="M512 977.454545C254.929455 977.454545 46.545455 769.070545 46.545455 512S254.929455 46.545455 512 46.545455s465.454545 208.384 465.454545 465.454545a465.454545 465.454545 0 0 1-465.454545 465.454545zM367.941818 246.039273a33.233455 33.233455 0 0 0 0 66.466909h288.116364a33.233455 33.233455 0 0 0 0-66.466909h-288.116364z m177.338182 123.205818V312.506182h-66.56v56.785454A210.106182 210.106182 0 0 0 279.272727 578.466909v44.311273a33.233455 33.233455 0 0 0 66.513455 0v-44.311273a143.639273 143.639273 0 0 1 132.980363-142.754909v310.318545a33.233455 33.233455 0 1 0 66.46691 0V435.758545a143.639273 143.639273 0 0 1 132.980363 142.75491v44.311272a33.233455 33.233455 0 0 0 66.513455 0v-44.311272a210.106182 210.106182 0 0 0-199.493818-209.268364z"  p-id="13078"></path></svg>
        <div class="i-data-content-1-title">
            <span>投币&nbsp;&nbsp;></span>
        </div>
        <span class="i-count">{{ userInfo.allCoins || 0 }}</span>
</div>
</el-row>
   </div>
  </div>
</template>

<style scoped>
.i-top-pic{
    width: 1290px;
    height: 275px;
    margin-top: 0px;
    margin-left: 75px;
}

.i-data{
    background-color: rgb(255, 255, 255);
    height: 285px;
    width: 1283px;
    margin-left: 75px;
    margin-top: 5px;
    border-radius: 5px;
}

.i-data-title{
 position: absolute;
 margin-left: 25px;
 margin-top: 20px;
}

.i-data-title-text{
   color: rgb(0, 161, 214); 
   font-weight: 700;
}

.i-data-content{
    height: 200px;
    position: relative;
    top:60px
}

.i-data-content-1{
    background-color: rgb(245, 253, 255);
    height: 85px;
    width: 300px;
    position: relative;
    left: 20px;
    border-radius: 5px;
    margin-right: 13px;
    margin-bottom: 13px;
}

.i-data-content-1-title{
    position: relative;
    top: -15px;
    left: 35px;
    font-size: 14px;
    font-weight: 500;
    color: rgb(127, 132, 138);
}

.i-icon{
    position: relative;
    top: 10px;
    left: 10px;
    width: 20px;
    height: 20px;
    fill:rgb(127, 132, 138);
}

.i-count{
    position: relative;
    top: -10px;
    left: 10px;
    font-size: 24px;
    color: rgb(0, 174, 236);
    font-weight: 600;
}
</style>
