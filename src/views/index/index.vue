<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTransition } from '@vueuse/core'
import { ElMessage,ElMessageBox } from 'element-plus';
import { updatePassword,getManagerInfoByusername,getVideoCount,getTotalCommentCount,getTotalFavoriteCount,getTotalLikeCount,getTotalPostCount,getTotalUserCount } from '@/api';

const router = useRouter();

const source = ref(0)
const outputValue = useTransition(source, {
  duration: 1500,
})
source.value = 172000;

const videoSource = ref(0)
const videoOutputValue = useTransition(videoSource, {
  duration: 1500,
})

const userSource = ref(0)
const userOutputValue = useTransition(userSource, {
  duration: 1500,
})

const likeSource = ref(0)
const likeOutputValue = useTransition(likeSource, {
  duration: 1500,
})

const commentSource = ref(0)
const commentOutputValue = useTransition(commentSource, {
  duration: 1500,
})

const postSource = ref(0)
const postOutputValue = useTransition(postSource, {
  duration: 1500,
})

const favoriteSource = ref(0)
const favoriteOutputValue = useTransition(favoriteSource, {
  duration: 1500,
})

const logout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    } 
  ).then(() => {
    ElMessage({
      type: 'success',
      message: '已退出登录！', 
    })
  localStorage.removeItem('loginManager');
  router.push('/login');
  })
}

const managerInfo = ref({
  username: '',
  password: '',
})

const getManagerName = () => {
  const manager = JSON.parse(localStorage.getItem('loginManager'));
  managerInfo.value.username =  manager.username;
}

const updatePasswordDialogVisible = ref(false);

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 校验规则
const passwordRules = reactive({
  oldPassword: [
    { required: true, message: '旧密码不能为空', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '新密码不能为空', trigger: 'blur' },
    { 
      validator: (_, value, callback) => {
        if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度6-20位'))
} else if (!/[A-Z]/.test(value) || !/[a-z]/.test(value) || !/\d/.test(value)) {
    callback(new Error('需同时包含大小写字母和数字'))
} else {
    callback()
}
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { 
      validator: (_, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

const isSubmitting = ref(false)
const passwordFormRef = ref(null)

const updatePasswordEntity = ref({
 id: '',
 newPassword: '' ,
 oldPassword: ''
})

// 提交处理
const handlePasswordSubmit = async () => {
  try {
    // 执行表单校验
    await passwordFormRef.value.validate()
    
    isSubmitting.value = true;
    updatePasswordEntity.value.id = managerInfo.value.id
    updatePasswordEntity.value.newPassword = passwordForm.newPassword;
    updatePasswordEntity.value.oldPassword = passwordForm.oldPassword;
    const result = await updatePassword(updatePasswordEntity.value);
    if(result.code === 1){
     ElMessage.success('密码修改成功!请重新登录！')
    updatePasswordDialogVisible.value = false;
    localStorage.removeItem('loginManager');
    router.push('/login').then(() => {
      window.location.reload();
    });
    }else{
      ElMessage.error(result.msg)
    }
  } finally {
    isSubmitting.value = false
  }
}

// 关闭时重置表单
const handleDialogClose = () => {
  passwordFormRef.value?.resetFields()
}

const videoCount = ref({
  total: 0,
  no: 0,
  yes: 0, 
  wait: 0
})

const getVideoCounts = async () => {
  const result = await getVideoCount();
  if(result.code === 1){
    console.log(result.data);
    videoCount.value.total = result.data.total;
    videoCount.value.no = result.data.no;
    videoCount.value.yes = result.data.yes;
    videoCount.value.wait = result.data.wait;
  }
}

const getReportCount = async () => {
  const result1 = await getTotalUserCount();
  if(result1.code === 1){
    userSource.value = result1.data;
  }
  const result2 = await getTotalLikeCount();
  if(result2.code === 1){
    likeSource.value = result2.data;
  }
  const result3 = await getTotalCommentCount();
  if(result3.code === 1){
    commentSource.value = result3.data;
  }
  const result4 = await getTotalPostCount();
  if(result4.code === 1){
    postSource.value = result4.data;
  }
  const result5 = await getTotalFavoriteCount();
  if(result5.code === 1){
    favoriteSource.value = result5.data;
  }
}

const days = ref('');

const getManagerInfo = async () => {
  const result = await getManagerInfoByusername(managerInfo.value.username);
  if(result.code === 1){
    managerInfo.value = result.data;
    days.value = daysDiff(managerInfo.value.createdTime);
    console.log(managerInfo.value);
  }else{
    ElMessage.error(result.msg);
  }
}

const daysDiff = (str) => {
  const targetDate = new Date(str);
  const now = new Date();
  
  // 计算毫秒差
  const diffMs = now.getTime() - targetDate.getTime();
  
  // 转换为天数（向下取整，不足一天不算一天）
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
  
  return diffDays > 0 ? diffDays : 0; // 避免负数
};

onMounted(async() => {
  await getManagerName();
  await getManagerInfo();
  await getVideoCounts();
  await getReportCount();
  videoSource.value = videoCount.value.total;
})

</script>

<template>
 <div class="index-userInfo">
  <img class="index-avatar-img" src="@\assets\pic\20180327213630_isuru.jpg">
  <el-avatar class="index-avatar" :size="70" src="https://yilena-practice-project.oss-cn-beijing.aliyuncs.com/Yilena-video-web/25/04/05-8746f4d1a8404cc5a05c50a4079eda37.jpg"></el-avatar>
  <el-row class="index-username">昵称：<span style="font-weight: 500;font-size: 20px;margin-top: -4px;">{{ managerInfo.username }}</span></el-row>
  <el-row class="index-username">身份：<span style="font-weight: 500;font-size: 20px;margin-top: -4px;">管理员</span></el-row>
  <el-row class="index-operate">
  <el-button @click="updatePasswordDialogVisible = true" type="primary" text class="index-button"><span style="font-size: 18px;"><el-icon><EditPen /></el-icon>&nbsp;&nbsp;修改密码</span></el-button>
  <el-divider direction="vertical"  class="index-divider"/>
  <el-button @click="logout" type="primary" text class="index-button"><span style="font-size: 18px;"><el-icon><SwitchButton /></el-icon>&nbsp;&nbsp;退出登录</span></el-button>
  </el-row>
<div class="index-video">
<!-- From Uiverse.io by kamehame-ha --> 
<div class="cards">
    <div class="card red">
        <p class="tip">{{ videoCount.no }}</p>
        <p class="second-text"><el-icon class="card-icon"><CircleCloseFilled /></el-icon>&nbsp;&nbsp;未通过</p>
    </div>
    <div class="card blue">
        <p class="tip">{{ videoCount.wait }}</p>
        <p class="second-text"><el-icon class="card-icon"><QuestionFilled /></el-icon>&nbsp;&nbsp;未审核</p>
    </div>
    <div class="card green">
        <p class="tip">{{ videoCount.yes }}</p>
        <p class="second-text"><el-icon class="card-icon"><CircleCheckFilled /></el-icon>&nbsp;&nbsp;已通过</p>
    </div>
</div>
<span class="card-title">总审核视频数据</span>
</div>
 </div>

 <img class="left-img" src="@\assets\pic\20200925163201_6882f.jpg">
 <img class="right-img" src="@\assets\pic\20200925163200_c8375.jpg">
 <div class="index-buttom">
  <div class="index-buttom-card">
  <el-row>
  <el-statistic 
  class="custom-statistic" title="总注册用户数" :value="userOutputValue" />
  <el-statistic 
  class="custom-statistic" title="总视频投稿数" :value="videoOutputValue" />
  <el-statistic 
  class="custom-statistic" title="总互动点赞数" :value="likeOutputValue" />
</el-row>
<el-row>
  <el-statistic
  class="custom-statistic" title="总动态投稿数" :value="postOutputValue" />
  <el-statistic
  class="custom-statistic" title="总互动评论数" :value="commentOutputValue" />
  <el-statistic 
  class="custom-statistic" title="总视频收藏数" :value="favoriteOutputValue" />
</el-row>
 </div>
 <div class="a">
  <div class="index-days">
  <el-row>你成为管理员已经&nbsp;&nbsp;<span style="color: rgb(237, 80, 143);margin-top: -20px;font-style: italic; font-weight: 600;font-size: 40px;">{{ days }}</span>&nbsp;&nbsp;&nbsp;&nbsp;天啦！感谢你为仿b站大家庭做出的贡献！</el-row>
</div>
 </div>
</div>

<el-dialog
  v-model="updatePasswordDialogVisible"
  title="修改密码"
  width="500"
  align-center
  @closed="handleDialogClose"
>
  <el-form
    ref="passwordFormRef"
    :model="passwordForm"
    :rules="passwordRules"
    label-position="left"
    label-width="120px"
    status-icon
  >
    <!-- 旧密码 -->
    <el-form-item label="旧密码" prop="oldPassword">
      <el-input
        v-model="passwordForm.oldPassword"
        type="password"
        show-password
        placeholder="请输入当前登录密码"
      />
    </el-form-item>

    <!-- 新密码 -->
    <el-form-item label="新密码" prop="newPassword">
      <el-input
        v-model="passwordForm.newPassword"
        type="password"
        show-password
        placeholder="8-20位，需包含字母和数字"
      />
    </el-form-item>

    <!-- 确认密码 -->
    <el-form-item label="确认新密码" prop="confirmPassword">
      <el-input
        v-model="passwordForm.confirmPassword"
        type="password"
        show-password
        placeholder="请再次输入新密码"
      />
    </el-form-item>
  </el-form>

  <template #footer>
    <el-button @click="updatePasswordDialogVisible = false">返回</el-button>
    <el-button
      type="primary"
      :loading="isSubmitting"
      @click="handlePasswordSubmit"
    >
      确认修改
    </el-button>
  </template>
</el-dialog>
</template>

<style scoped>
.index-operate{
  margin-top: -40px;
  margin-left: 40px;
}

.index-divider{
  margin-top: 8px;
}

.a{
  background-color: rgb(255, 255, 255);
  width: 665px;
  height: 90px;
  border-radius: 10px;
}

/* 修改数值样式 */
.custom-statistic :deep(.el-statistic__content) {
  font-size: 40px;
  margin: 10px;
  color: rgb(61, 61, 61);
  font-weight: 100;
}
/* 修改标题样式 */
.custom-statistic :deep(.el-statistic__title) {
  font-size: 20px;
}

.index-buttom-card{
  margin: 20px;
  margin-left: 45px;
  margin-top: 30px;
}

.el-statistic{
  text-align: center;
  border-radius: 10px;
  margin-bottom: 20px;
  margin-right: 45px;
  background-color: rgb(255, 255, 255);
  width: 165px;
}

.el-col {
  text-align: center;
}

.right-img{
  height: 400px;
  border-radius: 0px 10px 10px 0px ;
  margin-top: 10px;
  position: relative;
  left: 690px;
}

.left-img{
  height: 400px;
  border-radius: 10px 0 0 10px ;
  margin-top: 10px;
  margin-left: 200px;
}

.index-avatar-img{
  position: absolute;
  height: 100px;
  left: 450px;
  z-index: 9;
  border-radius: 10px;
}

.card-icon{
  font-size: 20px;
  position: relative;
  top: 5px;
}

.index-buttom{
  height: 400px;
  border-radius: 10px;
  position: absolute;
  top: 285px;
  margin-left: 410px;
}

.card-title{
  margin-left: 315px;
  color: #393939;
}

/* From Uiverse.io by kamehame-ha */ 
.cards {
  margin: 10px;
  display: flex;
  flex-direction: row;
  gap: 15px;
}

.cards .red {
  background-color: #f43f5e;
}

.cards .blue {
  background-color: #a7a7a7;
}

.cards .green {
  background-color: #22c55e;
}

.cards .card {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  text-align: center;
  height: 150px;
  width: 240px;
  border-radius: 10px;
  color: white;
  cursor: pointer;
  transition: 400ms;
}

.cards .card p.tip {
  font-size: 50px;
  font-weight: 700;
}

.cards .card p.second-text {
  position: relative;
  top: -50px;
  font-size: .7em;
}

.cards .card:hover {
  transform: scale(1.1, 1.1);
}

.cards:hover > .card:not(:hover) {
  filter: blur(10px);
  transform: scale(0.9, 0.9);
}

.index-video{
  background-color: rgb(255, 255, 255);
  height: 200px;
  border-radius: 10px;
  width: 730px;
  position: absolute;
  top: 75px;
  margin-left: 360px;
}

.index-days{
  position: relative;
  top: 30px;
  align-self: center;
  align-items: center;
  justify-self: center;
  color: rgb(134, 213, 254);
  font-size: 20px;
}

.index-username{
  font-size: 15px;
  position: relative;
  top: -70px;
  left: 110px;
  margin-bottom: 10px;
  z-index: 10;
}

.index-avatar{
  border: 0.05cm solid rgb(194, 194, 194);
  margin-top: 20px;
  margin-left: 20px;
}

.index-userInfo{
  background-color: rgb(255, 255, 255);
  height: 200px;
  border-radius: 10px;
  width: 350px;
  margin-top: 75px;
  margin-left: 200px;
}

</style>
