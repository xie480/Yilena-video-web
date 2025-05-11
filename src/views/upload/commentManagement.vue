<script lang="ts" setup>
import { ref,onMounted,computed,watch } from 'vue'
import { useRouter } from 'vue-router';
import { useTransition } from '@vueuse/core'
import CryptoJS from 'crypto-js'
import { Search } from '@element-plus/icons-vue'
import { ElMessage,ElMessageBox } from 'element-plus';
import { findAllVideos,getUserInfoByUsername } from '../../api';
import { image } from '@vee-validate/rules';
import { getCommentQurey,deleteComment } from '../../api/upload';

const router = useRouter();

const tableData = ref([])

const input = ref('')

const value = ref('最新发布')

const options = ['最新发布','最多点赞','最多回复']

const formatDate = (timestamp) => {
  const date = new Date(timestamp);
  
  // 补零函数
  const pad = n => n.toString().padStart(2, '0');
  
  return `
    ${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())}
    ${pad(date.getHours())}:${pad(date.getMinutes())}
  `.replace(/\s+/g, ' ').trim();
}

const getCurrentUserComments = async() =>{
  let sortType = null;
  if(value.value === '最新发布'){
    sortType = 1;
  }else if(value.value === '最多点赞'){
    sortType = 2; 
  }else if(value.value === '最多回复'){
    sortType = 3; 
  }
  const result = await getCommentQurey(input.value,sortType);
  if(result.code === 1){
    tableData.value = result.data;
    tableData.value.forEach(item => {
      item.createdTime = formatDate(item.createdTime);
      item.updatedTime = formatDate(item.updatedTime);
    })
  }else{
    ElMessage.error(result.msg);
  }
}

const selectedIds = ref([]);
//复选框勾选发生变化时触发 - selection: 当前选中的记录 (数组)
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map( item => item.id);
}

const deleteSelectedComments = async() => {
  if(selectedIds.value.length === 0){
    ElMessage.error('请选择要删除的评论');
    return;
  } 
  ElMessageBox.confirm('您确认删除所选中的评论吗?','提示',
    { confirmButtonText: '确认',cancelButtonText: '取消',type: 'warning'}
  ).then(async () => { 
  const result = await deleteComment(selectedIds.value);
  if(result.code === 1){
    ElMessage.success('删除成功');
    getCurrentUserComments(); 
  }else{
    ElMessage.error(result.msg);
  }
}).catch(() => { 
  ElMessage.info('已取消');
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

watch(value,() =>{
  getCurrentUserComments();
})

onMounted(() => {
  getCurrentUserComments();
  getUserInfo();
})
</script>

<template>
  <div class="comments-core">
    <div class="comments-core-title">
      <span class="comments-core-title-text">视频评论</span>
    </div>
    <el-button type="danger" class="cm-del-btn" @click="deleteSelectedComments">删除</el-button>
    <el-input
      v-model="input"
      style="width: 255px"
      placeholder="请输入视频标题以查找对应的评论"
      :prefix-icon="Search"
      class="cm-input"
      @keyup.enter="getCurrentUserComments"
    />
    <el-segmented class="cm-segmented" v-model="value" :options="options" size="default" />

    <el-table :data="tableData" style="width: 100%"
    :cell-style="{ textAlign: 'center' }"
      :header-cell-style="{ 'text-align': 'center' }"
      class="cm-table"
      @selection-change="handleSelectionChange"
      v-if="tableData.length != 0">
    <el-table-column type="selection" width="55" />
    <el-table-column prop="videoTitle" label="来源视频标题" width="128"/>
    <el-table-column prop="content" label="评论内容" width="240"/>
    <el-table-column prop="imageUrl" label="评论图片" width="180">
      <template #default="scope">
          <img v-if="scope.row.imageUrl != null" class="cm-imageUrl" :src="scope.row.imageUrl"/>
          <span v-else style="color: rgb(144, 147, 153);">无</span>
      </template>
    </el-table-column>
    <el-table-column prop="likes" label="点赞数" width="100"/>
    <el-table-column prop="comments" label="回复数" width="100"/>
    <el-table-column prop="username" label="发布用户" width="120">
      <template #default="scope">
          <span style="color: rgb(18, 150, 219);" @click="toUserInfo(scope.row.userId)">{{ scope.row.username }}</span>
      </template>
      </el-table-column>
    <el-table-column prop="createdTime" label="发布时间" width="180"/>
    <el-table-column prop="updatedTime" label="修改时间"width="180"/>
  </el-table>

  <div v-else >
    <img class="no-data-img" src="YilenaPic/25/04/21-a65cae981164481eb2bcd21689dcd3fa.jpg" />
  </div>
  </div>
</template>

<style scoped>

.comments-core{
  background-color: rgb(255, 255, 255);
  margin-left: 90px;
  width: 1283px;
}

.comments-core-title{
  position: relative;
  top: 10px;
  margin-left: 25px;
}

.comments-core-title-text{
  color: rgb(18, 150, 219);
  font-weight: 600;
  font-size: 18px;
}

.cm-input{
  margin-top: 15px;
  margin-left: 1000px;
}

.cm-segmented{
  margin-left: 1020px;
  margin-top: 10px;
}

.el-segmented {
  --el-segmented-item-selected-color: rgb(18, 150, 219);
  --el-segmented-item-selected-bg-color: transparent;
  --el-border-radius-base: 5px;
}

.cm-table{
  margin-top: 20px;
}

.cm-del-btn{
  position: relative;
  top:70px;
  left: 25px
}

.cm-imageUrl{
  max-width: 100px;
  max-height: 200px;
  min-width: 50px;
  min-height: 50px;
}

.no-data-img{
  width: 380px;
  height: 280px;
  margin-top: 50px;
  margin-left: 430px;
}
</style>
