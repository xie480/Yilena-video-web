<script lang="ts" setup>
import { ref,onMounted,computed,watch } from 'vue'
import { useRouter } from 'vue-router';
import { useTransition } from '@vueuse/core'
import CryptoJS from 'crypto-js'
import { ElMessage,ElMessageBox } from 'element-plus';
import { findAllVideos,getUserInfoByUsername } from '../../api';
import {
  Document,
  Menu as IconMenu,
  Location,
  Setting,
} from '@element-plus/icons-vue'

const currentActive = ref('')

const handleOpen = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}
const handleClose = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}

const router = useRouter();

const reloadIndex = () => {
      router.push('/index').then(() => {
        window.scrollTo(0, 0); // 滚动到页面顶部
        window.location.reload();
      });
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

    function formatViews(views) {
  if (views >= 10000) {
    return `${Math.round(views / 1000) / 10}万`; // 保留1位小数（如：1.3万）
  }
  return views.toString();
}

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

const toUploadVideo = () => {
  router.push('/upload/uploadVideo').then(() => {
    window.location.reload();
  });
}

const getCurrentActive = () => {
  const path = router.currentRoute.value.path;
  currentActive.value = path;
  console.log(currentActive.value)
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

const toChat = () => {
      router.push('/chat').then(() => {
        window.location.reload();
});
    }

    

onMounted(() => {
  getUserInfo();
  getCurrentActive();
})
</script>

<template>
  <!-- 顶栏 -->
   <div class="upload-top">
   <!-- logo -->
   <div class="upload-logo"><svg t="1743859746673" class="logo-icon" viewBox="0 0 2299 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6640" ><path d="M1776.040306 322.618853a497.5306 497.5306 0 0 1 55.146298-6.026207c3.013104 4.508817 24.061475 155.944368 18.555516 156.94151s-44.632952 10.036453-44.632952 10.036453c-3.013104-20.5498-28.570291-147.923876-29.090539-160.951756m75.219204-14.54527l20.5498 162.967718a2379.701518 2379.701518 0 0 0 57.16226-4.010246 20057.059972 20057.059972 0 0 0-16.539554-158.957472 156.984864 156.984864 0 0 0-61.172506 0m-40.601029 226.156185s92.755901-23.562904 126.355258-12.030738c17.038125 61.671077 48.144626 407.65774 51.136052 421.70444-21.070048 2.514532-90.263045 8.519063-95.769004 10.036453-4.508817-26.012405-81.722306-403.192278-81.722306-419.688478m343.472131-207.578992a540.971316 540.971316 0 0 1 55.167976-1.51739c0.997142 12.030737 6.026207 157.440081 0.498571 157.938652l-44.632951 4.010246c-0.997142-20.051229-12.030737-146.926734-11.033596-160.409831m75.717776-4.010246l4.010246 160.951756c10.535024 0 52.653443 2.514532 57.16226 2.015961-0.997142-61.671077 0-158.957472 0-158.957471a277.032113 277.032113 0 0 0-61.172506-4.010246m-64.68418 216.119732s94.771862-12.529308 126.853828 2.514532c7.023349 72.206101 6.026207 408.654883 7.023349 422.701581-21.568619 0-90.263045 0.997142-95.769004 2.015962-0.997142-26.012405-39.603887-410.670844-38.108173-427.210398M1945.012553 125.054637c54.647728 278.787951 96.267576 755.140117 97.784966 785.72637 0 0 43.115561 0.997142 91.758759 4.010246-28.591969-300.35657-63.687038-780.220411-63.188468-795.762823-12.030737-13.548128-126.355257 6.026207-126.355257 6.026207m-120.32905 659.371113c-10.535024-78.232308-290.320117-166.912932-447.760198-138.385995 0 0-19.509304-172.483922-27.074578-339.461885-6.503101-143.41506-0.997142-282.798196 0.498571-305.862529-10.535024-7.52192-123.840725 46.627236-185.034908 69.192997 0 0 73.203243 309.98116 126.355257 952.704334A862.57135 862.57135 0 0 0 1521.725693 1003.645293c145.409344-28.180105 317.394695-115.820233 302.849425-219.111158M1482.707085 925.326277l-24.560045-176.992739a1281.501133 1281.501133 0 0 1 172.982493 63.188467c-2.514532 11.033595-148.422448 113.825949-148.422448 113.825949m-871.97917-602.620716a497.5306 497.5306 0 0 1 55.146299-6.026207c3.013104 4.508817 24.061475 155.944368 18.555516 156.94151s-44.632952 10.036453-44.632952 10.036453c-3.013104-20.5498-28.570291-147.923876-29.09054-160.951756m75.219205-14.54527l20.5498 162.967718a2379.701518 2379.701518 0 0 0 57.16226-4.010246c-6.503101-61.671077-16.539554-158.957472-16.539554-158.957472a156.984864 156.984864 0 0 0-61.172506 0M645.324414 534.229768s92.755901-23.562904 126.355257-12.030738c17.038125 61.671077 48.144626 407.65774 51.136053 421.70444-21.070048 2.514532-90.263045 8.519063-95.769004 10.036453-4.508817-26.012405-81.722306-403.192278-81.722306-419.688478m343.472131-207.578992A540.971316 540.971316 0 0 1 1043.964521 325.155063c0.997142 12.030737 6.026207 157.440081 0.498571 157.938652l-44.632952 4.010246c-0.997142-20.051229-11.532166-146.926734-11.033595-160.409831m75.717776-4.010246l4.010246 160.951756c10.535024 0 52.653443 2.514532 57.16226 2.015961-0.997142-61.671077 0-158.957472 0-158.957471a311.173395 311.173395 0 0 0-61.172506-4.010246m-64.684181 216.119732s94.771862-12.529308 126.853829 2.514532c7.023349 72.206101 6.026207 408.654883 7.023349 422.701581-21.568619 0-90.263045 0.997142-95.769004 2.015962-0.498571-26.012405-39.603887-410.670844-38.108174-427.210398M779.72184 125.054637c54.647728 278.787951 96.267576 755.140117 97.784966 785.72637 0 0 43.115561 0.997142 91.758758 4.010246C940.673596 614.45636 905.578526 134.592519 906.098774 118.551536c-12.030737-13.006203-126.355257 6.503101-126.355257 6.503101m-120.372404 659.371113c-10.535024-78.232308-290.320117-166.912932-447.760198-138.385995 0 0-19.509304-172.483922-27.074578-339.461885C178.011558 163.16281 183.517517 23.844705 185.034908 0.715341 174.499884-6.308008 61.172506 47.342577 0 69.908338c0 0 73.203243 309.98116 126.355257 952.704334a862.57135 862.57135 0 0 0 230.166431-18.967379c145.409344-28.180105 317.394695-115.820233 302.849425-219.111158m-341.976418 140.900527l-24.560046-176.992739a1281.501133 1281.501133 0 0 1 172.982494 63.188467c-2.514532 11.033595-148.422448 113.825949-148.422448 113.825949" fill="#1296db" p-id="6641"></path></svg></div>
   <div class="upload-top-title">
    <span class="upload-top-title-text">创作中心</span>
   </div>
    <!-- 首页按钮 -->
    <button class="up-btn-left-1" @click="reloadIndex">
              <span class="up-btn-text-one"><svg t="1743519580381" class="bli-1" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3754" width="200" height="200"><path d="M777.500444 157.582222a49.208889 49.208889 0 0 1 15.644445 35.783111 49.208889 49.208889 0 0 1-15.644445 35.726223l-48.753777 46.193777h49.891555c42.496 0 83.171556 15.928889 113.208889 44.373334 29.980444 28.444444 46.819556 67.015111 46.819556 107.235555v303.217778c0 83.797333-71.68 151.665778-160.028445 151.665778H245.304889C157.013333 881.777778 85.333333 813.909333 85.333333 730.168889v-303.217778c0-83.797333 71.68-151.665778 159.971556-151.665778h49.777778l-48.696889-46.136889a48.810667 48.810667 0 0 1-14.563556-49.152 51.996444 51.996444 0 0 1 38.115556-36.124444 55.296 55.296 0 0 1 51.825778 13.767111l113.208888 107.235556c3.356444 3.185778 6.144 6.656 8.419556 10.410666h137.102222a49.720889 49.720889 0 0 1 8.476445-10.467555l113.095111-107.235556a54.840889 54.840889 0 0 1 37.717333-14.791111c14.165333 0 27.704889 5.347556 37.717333 14.791111z m1.137778 219.136H245.304889c-27.875556 0-51.086222 20.423111-53.191111 46.819556l-0.113778 3.811555v303.217778c0 26.624 21.731556 48.469333 49.379556 50.346667l3.982222 0.170666h533.276444c27.932444 0 51.2-20.366222 53.248-46.762666l0.113778-3.754667v-303.217778c0-27.875556-23.893333-50.574222-53.361778-50.574222zM351.971556 477.866667c29.468444 0 53.361778 22.641778 53.361777 50.574222v50.517333c0 27.875556-23.893333 50.517333-53.361777 50.517334-29.411556 0-53.304889-22.584889-53.304889-50.517334v-50.517333c0-27.875556 23.893333-50.574222 53.304889-50.574222z m320 0c29.468444 0 53.361778 22.641778 53.361777 50.574222v50.517333c0 27.875556-23.893333 50.517333-53.361777 50.517334-29.411556 0-53.304889-22.584889-53.304889-50.517334v-50.517333c0-27.875556 23.893333-50.574222 53.304889-50.574222z" fill="rgb(145, 145, 145)" p-id="3755"></path></svg>
                <span class="up-btn-text">首页</span>
              </span>
              <span class="up-btn-text-two"><el-icon><HomeFilled /></el-icon>
                <span class="up-btn-text2">返回首页</span>
              </span>
            </button>
            <el-avatar :src= "userInfo.image"
      :size="36" 
      class="up-user-avatar" 
      @click="toUserInfo(userInfo.id)"
      ></el-avatar>
      <el-divider direction="vertical" class="up-col-divider"/>
      <!-- 私信 -->
 <div class="up-msg-botton">
  <el-dropdown>
             <button class="left-button-2" @click="toChat">
              <svg t="1743583258372" class="vip-icon" viewBox="0 0 1424 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="21799" width="300" height="300"><path d="M1246.608696 0H178.086957a178.086957 178.086957 0 0 0-178.086957 178.086957v667.826086a178.086957 178.086957 0 0 0 178.086957 178.086957h1068.521739a178.086957 178.086957 0 0 0 178.086956-178.086957V178.086957a178.086957 178.086957 0 0 0-178.086956-178.086957z m89.043478 845.913043a89.043478 89.043478 0 0 1-89.043478 89.043479H178.086957a89.043478 89.043478 0 0 1-89.043479-89.043479V178.086957a89.043478 89.043478 0 0 1 89.043479-89.043479h1068.521739a89.043478 89.043478 0 0 1 89.043478 89.043479zM1082.323478 257.78087l-337.92 293.843478a44.521739 44.521739 0 0 1-64.111304 0l-337.92-293.843478a44.521739 44.521739 0 0 0-63.22087 63.220869l338.365218 293.398261a133.565217 133.565217 0 0 0 189.662608 0l338.365218-293.398261a44.521739 44.521739 0 1 0-63.22087-63.220869z" fill="rgb(145, 145, 145)" p-id="21800"></path></svg>
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
   </div>

   <!-- 核心区域 -->
    <div class="upload-content">
      <el-container>
      <el-aside width="200px" class="upload-aside">
        <!-- 投稿 -->
<div class="up-botton" @click="toUploadVideo">
<button class="up-button-upload"><svg t="1743585506476" class="upload-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="59528" width="15" height="15"><path d="M958.203854 1023.866918H65.590221A65.637783 65.637783 0 0 1 0.072874 958.229135V431.561198c0-36.191108 29.386457-65.637783 65.517347-65.637783H206.621128L180.727323 421.745639H60.291024v541.963347h903.272245V421.745639h-120.436299l-27.098168-55.822224h142.175052c36.13089 0 65.517347 29.446675 65.517346 65.577565v526.728155a65.637783 65.637783 0 0 1-65.517346 65.637783zM659.88314 230.673451L542.036221 120.654891v602.181497H481.818072V120.654891L362.586135 230.673451a36.733071 36.733071 0 0 1-59.134223-11.501667 34.806091 34.806091 0 0 1 7.888578-38.539616L485.611815 10.455677a36.733071 36.733071 0 0 1 51.245645 0l174.271325 170.176491a34.806091 34.806091 0 0 1 0 50.041283 36.913726 36.913726 0 0 1-51.245645 0z" p-id="59529" fill="#ffffff"></path></svg>
  &nbsp;投稿</button>
</div>

      <!-- 菜单 -->
       <div class="upload-menu">
        <el-menu
        :default-active="currentActive"
        class="el-menu-vertical-demo"
        @open="handleOpen"
        @close="handleClose"
        router
      >
        <el-menu-item index="/upload/uploadIndex">
          <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
        <el-menu-item index="/upload/fileManagement">
          <el-icon><Document /></el-icon>
          <span>稿件管理</span>
        </el-menu-item>
        <el-menu-item index="/upload/dataCenter">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据中心</span>
        </el-menu-item>
        <el-menu-item index="/upload/commentManagement">
          <el-icon><ChatLineRound /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
      </el-menu>
       </div>
      </el-aside>
      <el-main class="upload-main"><router-view></router-view></el-main>
    </el-container>
    </div>
</template>

<style scoped>
.upload-top{
  height: 65px;
  width: 100%;
  box-shadow: 0 0 0 0.08cm rgba(0, 0, 0, 0.05);
  z-index: 10000;
}

.upload-logo{
  position: absolute;
  width: 100px;
  height: 50px;
  top: 9px;
  left: 20px;
}

.upload-top-title{
  position: absolute;
  width: 100px;
  height: 50px; 
  top: 15px;
  left: 130px;
}

.upload-top-title-text{
 font-size: 22px; 
 color: rgb(18, 150, 219);
 font-weight: 600;
}

/* From Uiverse.io by mobinkakei */ 
.up-btn-left-1 {
  width: 150px;
  height: 50px;
  background: transparent;
  align-items: center; /* 垂直居中对齐 */
  justify-content: center; /* 水平居中对齐 */
  color: rgb(145, 145, 145);
  border-radius: 50px;
  border: none;
  outline: none;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  padding: 0 10px; /* 添加内边距 */
  top: 8px;
  left: 200px;
}

.up-btn-left-1 span {
  font-size: 15px;
  text-transform: uppercase;
  letter-spacing: 1px;
  transition: top 0.5s;
}

.up-btn-text-one {
  position: absolute;
  width: 100%;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
}

.up-btn-text-two {
  position: absolute;
  width: 100%;
  top: 150%;
  left: 0;
  transform: translateY(-50%);
}

.up-btn-left-1:hover .up-btn-text-one {
  top: -100%;
}

.up-btn-left-1:hover .up-btn-text-two {
  top: 50%;
}

.up-btn-text {
  color: rgb(145, 145, 145);
  font-size: 15px;
  font-weight: bolder;
  text-align: center; /* 文字居中 */
  display: inline-block; /* 将文字和图标都显示为行内元素 */
  vertical-align: middle;
}

.up-btn-text2{
  color: rgb(145, 145, 145);
  font-size: 15px;
  font-weight: bolder;
}

.up-user-avatar{
  position: absolute;
  left: 1400px;
  top: 10px;
}

.up-col-divider{
  position: absolute;
  left: 1470px;
  top: 18px;
  height: 25px;
}

.vip-text1 {
  font-size: 12px;
  color: rgb(50, 51, 54);
  font-weight: bold;
  text-align: center;
}

.up-msg-botton{
  position: absolute;
  margin-top: 10px;
  margin-left: 1440px;
}

.upload-main{
  background-color: rgb(250, 250, 250);
  height: 800px;
}

.up-button{
  position: absolute;
  left: 0px;
  top: 10px;
}

/* From Uiverse.io by shah1345 */ 
.up-button-upload{
  margin-top: 18px;
  margin-left: 32px;
  width: 136px;
  height: 42px;
  display: inline-block;
  transition: all 0.2s ease-in;
  position: relative;
  overflow: hidden;
  z-index: 1;
  color: rgba(249, 249, 249, 0.97);
  padding: 0.7em 1.7em;
  cursor: pointer;
  font-size: 17px;
  text-align: center;
  letter-spacing: 0.05em;
  border-radius: 0.2em;
  font-weight: 700;
  background: rgb(18, 150, 219);
  border: none;
}

.up-button-upload:active {
  color: #666;
  box-shadow: inset 4px 4px 12px #c5c5c5, inset -4px -4px 12px #ffffff;
}

.up-button-upload:before {
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

.up-button-upload:after {
  content: "";
  position: absolute;
  left: 55%;
  transform: translateX(-50%) scaleY(1) scaleX(1.45);
  top: 180%;
  width: 160%;
  height: 190%;
  background-color: rgb(251, 114, 153);
  border-radius: 50%;
  display: block;
  transition: all 0.5s 0.1s cubic-bezier(0.55, 0, 0.1, 1);
  z-index: -1;
}

.up-button-upload:hover {
  color: #ffffff;
  border: 1px solid rgb(251, 114, 153);
}

.up-button-upload:hover:before {
  top: -35%;
  background-color: rgb(251, 114, 153);
  transform: translateX(-50%) scaleY(1.3) scaleX(0.8);
}

.up-button-upload:hover:after {
  top: -45%;
  background-color: rgb(251, 114, 153);
  transform: translateX(-50%) scaleY(1.3) scaleX(0.8);
}

.upload-menu{
  margin-top: 30px;
  margin-left: 20px;
}


</style>
